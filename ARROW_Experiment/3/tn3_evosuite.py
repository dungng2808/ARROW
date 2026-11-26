#!/usr/bin/env python3
"""TN3, nhánh EvoSuite 1.2.0, trên phần lớp của một máy. Thiết kế: cac_thuc_nghiem.md, "Chạy EvoSuite"; evosuite_TN3.md.

Mỗi lớp (cả lớp không hợp JDK: EvoSuite thất bại thì ghi lại, không bỏ lớp):
    1. arrow inspect repo gốc → JDK, lệnh build, classpath, baseline GỐC
    2. bản sao "orig": build bằng lệnh compile của arrow → class production + classpath cho EvoSuite
    3. bản sao "evo" (tn3_common.evosuite_setup, cách dựng v3): runtime 1.2.0; junit 4.12 nếu module thiếu JUnit 4 hoặc
       JUnit 4 < 4.12; junit-vintage-engine nếu module chạy JUnit 5; listener InitializingListener trong Surefire nếu
       Maven + provider JUnit 4 (scope test, chỉ bản sao này)
    4. mỗi seed (cấu hình chính: seed 1–5; --budget: 1 lần với search_budget theo thời gian ARROW):
         EvoSuite (JDK của dự án, tối đa 20 phút) → gộp scaffolding (gop_scaffolding.py)
         → arrow gen --project evo --c0 <bộ gộp> --no-repair --no-pit   (xác minh 5 bước, tsDetect)
         → so baseline gốc với bản vá (thư viện thêm vào làm mất test đạt → REGRESSION)
         → nếu VALID: JaCoCo bằng script trên bản separateClassLoader = false (JUnitCore, JDK dự án),
                      PIT bằng script trên bộ gốc, cùng tham số PIT của arrow, minion JDK dự án
Kết quả: results/<máy>/evosuite_runs.jsonl (mỗi (lớp, cấu hình, seed) một dòng), suites/evosuite/, logs/evosuite/

    python3 tn3_evosuite.py --machine dung --workers 3 --arrow ~/.local/share/arrow-dev/build-tn2/arrow
    Chạy khói:     --task <task_id> [--task ...] [--seeds 1]    → results/<máy>-smoke/
    Cấu hình phụ:  --budget   (chạy SAU tn3_arrow.py của cùng máy)
    Chạy tiếp:     chạy lại đúng lệnh; --retry-infra chạy lại các dòng INFRA
    Chạy lại và ghi đè kết quả thật: --redo <task_id> [--seeds 3] [--budget]
"""
import argparse          # tham số dòng lệnh
import os                # đường dẫn
import re                # đổi separateClassLoader = true → false
import shutil            # chép/xóa file, thư mục
import statistics        # trung vị (ngân sách cấu hình phụ)

import tn3_common as c   # thư viện chung TN3
import tn3_lists         # danh sách đóng băng
from gop_scaffolding import MergeError, merge   # gộp scaffolding EvoSuite

SYSTEM = "evosuite"      # tên hệ thống ghi vào mỗi dòng


def key_of(rec):
    """Khóa của một dòng: (lớp, cấu hình, seed)."""
    return (rec["task_id"], rec["config"], rec["seed"])


def budget_seconds(task_id, arrow_runs, min_s):
    """search_budget của cấu hình phụ: trung vị qua 2 mô hình của (thời gian sinh ở TN1 + thời gian sửa ở TN3), ≥ min_s giây.
    Trả None nếu nhánh ARROW của lớp này chưa xong đủ 2 mô hình."""
    vals = []
    for rec in arrow_runs:
        if rec["task_id"] != task_id:
            continue
        if rec.get("status") != "DONE":                       # một mô hình chưa xong → chưa tính được
            return None
        repair = 0
        if rec.get("action") == "repair":                     # bộ phải sửa: cộng thời gian sửa ở TN3
            repair = rec.get("repair_seconds") or (rec.get("block_seconds") or {}).get("block3") or 0
        vals.append((rec.get("tn1_total_s") or 0) + repair)
    if len(vals) < 2:
        return None
    return max(min_s, int(round(statistics.median(vals))))


def evosuite_generate(snap, build, fqn, seed, out_dir, log_path, timeout, budget=None):
    """Chạy EvoSuite một lần cho lớp `fqn`. Trả về (kết quả run_logged, file test hoặc None, file scaffolding hoặc None)."""
    cp = [p for p in build["production_classes"] + build["classpath"] if p and os.path.exists(p)]   # EvoSuite dừng nếu có mục không tồn tại
    cmd = [c.java_exe(snap["workspace"]["jdk_home"]),         # java của JDK dự án
           "-cp", c.EVOSUITE_JAR, "org.evosuite.EvoSuite",
           "-class", fqn,                                      # lớp cần sinh test
           "-projectCP", os.pathsep.join(cp),                  # classpath của dự án
           "-seed", str(seed),                                 # seed của lần chạy này
           f"-Dtest_dir={os.path.join(out_dir, 'tests')}", f"-Dreport_dir={os.path.join(out_dir, 'report')}"]
    if budget:
        cmd.append(f"-Dsearch_budget={budget}")                # cấu hình phụ: ngân sách tìm kiếm (giây)
    os.makedirs(out_dir, exist_ok=True)
    res = c.run_logged(cmd, out_dir, log_path, timeout, env=c.java_env(snap["workspace"]["jdk_home"]))
    pkg, _, simple = fqn.rpartition(".")
    gen = os.path.join(out_dir, "tests", *pkg.split(".")) if pkg else os.path.join(out_dir, "tests")
    test = os.path.join(gen, simple + "_ESTest.java")          # hai file EvoSuite sinh ra
    scaf = os.path.join(gen, simple + "_ESTest_scaffolding.java")
    return res, (test if os.path.exists(test) else None), (scaf if os.path.exists(scaf) else None)


def run_class(cl, home, ctx):
    """Chạy mọi (cấu hình, seed) còn thiếu của một lớp; mỗi lần ghi một dòng vào evosuite_runs.jsonl."""
    task = {k: cl[k] for k in ("task_id", "dataset", "repo_url", "checkout_sha", "class_fqn")}
    tid, fqn = cl["task_id"], cl["class_fqn"]
    todo = ctx["todo"][tid]                                    # danh sách (cấu hình, seed, ngân sách) cần chạy
    logs = os.path.join(ctx["root"], "logs", SYSTEM, tid)      # log của lớp
    suites = os.path.join(ctx["root"], "suites", SYSTEM, tid)  # lưu bộ test sinh ra
    work = os.path.join(ctx["work_root"], f"evo-{tid}")        # thư mục làm việc (xóa khi xong)
    common = dict(task, system=SYSTEM, machine=ctx["machine"], evosuite_runnable=cl["evosuite_runnable"],
                  tool_commit=ctx["commit"], setup=c.EVOSUITE_SETUP)

    def emit(config, seed, **kw):
        """Ghi một dòng kết quả cho (cấu hình, seed) và in tóm tắt."""
        rec = dict(common, config=config, seed=seed, finished_at=c.now_iso(), **kw)
        c.append_jsonl(ctx["runs_path"], rec, c.JSONL_LOCK)
        print(f"[{rec.get('status')}] {tid} {config} seed={seed}: {rec.get('final_state') or rec.get('result')}", flush=True)

    def emit_all(**kw):
        """Ghi cùng một kết quả cho mọi seed (khi hỏng ngay ở bước chuẩn bị)."""
        for config, seed, _ in todo:
            emit(config, seed, **kw)

    # --- Chuẩn bị (một lần cho lớp) ---
    snap = c.inspect(ctx["arrow"], task, home, logs)           # 1. inspect repo gốc
    if snap["status"] != "OK":
        return emit_all(status="INFRA", result="INSPECT_" + snap["status"], error_message=snap.get("reason"))
    info = c.snap_summary(snap)                                # build tool, JDK, framework, vintage
    try:
        orig = c.checkout(home, task, os.path.join(work, "orig"))   # 2. bản sao giữ nguyên
        evo = c.checkout(home, task, os.path.join(work, "evo"))     # 3. bản sao sẽ thêm thư viện
    except Exception as e:                                       # noqa: BLE001
        shutil.rmtree(work, ignore_errors=True)
        return emit_all(status="INFRA", result="CHECKOUT_FAILED", error_message=str(e), **info)
    build = c.build_copy(orig, snap, os.path.join(logs, "build_orig.log"))   # build bản orig
    if not build["ok"]:
        shutil.rmtree(work, ignore_errors=True)
        return emit_all(status="INFRA", result="BUILD_FAILED", error_message=build.get("reason") or c.tail(os.path.join(logs, "build_orig.log"), 5), **info)
    try:        # runtime + JUnit 4.12 (+ vintage) + InitializingListener, theo hướng dẫn của EvoSuite
        setup = c.evosuite_setup(evo, snap, build["classpath"])
    except Exception as e:                                       # noqa: BLE001
        shutil.rmtree(work, ignore_errors=True)
        return emit_all(status="INFRA", result="SETUP_FAILED", error_message=str(e), **info)
    extra_cp = setup.pop("extra_cp")                           # jar thêm khi script đo JaCoCo/PIT
    mbuild = c.measure_classpath(build, setup)                 # classpath đo (đã bỏ JUnit 4 cũ nếu đã nâng)
    info = dict(info, **setup)                                 # ghi những gì đã thêm vào mỗi dòng
    patched_file = ",".join(setup["patched_files"])
    # baseline GỐC: (test đạt, test chập chờn, test hỏng) để so với baseline bản vá
    orig_base = (set(snap["baseline"].get("passing_in_all_runs") or []), set(snap["baseline"].get("flaky") or []),
                 set(snap["baseline"].get("failing_in_all_runs") or []))

    # --- Từng seed ---
    for config, seed, budget in todo:
        if c.base.STOP.is_set():                               # Ctrl+C → dừng
            break
        name = f"{config}-seed{seed}"                          # ví dụ default-seed3, budget-seed1
        gen_dir = os.path.join(work, name)
        gres, test, scaf = evosuite_generate(snap, build, fqn, seed, gen_dir, os.path.join(logs, name + "__evosuite.log"),
                                             ctx["cfg"]["evosuite"]["timeout_seconds"], budget)
        rec = dict(info, search_budget=budget, patched_file=patched_file, gen_seconds=gres["seconds"],
                   gen_exit=gres["exit_code"], gen_timed_out=gres["timed_out"])
        if not test:                                           # EvoSuite không ra file test
            emit(config, seed, status="DONE", result="NO_OUTPUT", final_state="NO_OUTPUT",
                 gen_reason="quá 20 phút" if gres["timed_out"] else c.tail(os.path.join(logs, name + "__evosuite.log"), 6)[-800:], **rec)
            continue
        dst = os.path.join(suites, name)                       # lưu 2 file gốc của EvoSuite
        os.makedirs(dst, exist_ok=True)
        shutil.copy(test, dst)
        if scaf:
            shutil.copy(scaf, dst)
        try:                                                   # gộp scaffolding vào lớp test
            with open(test, encoding="utf-8") as f:
                tsrc = f.read()
            with open(scaf, encoding="utf-8") as f:
                ssrc = f.read()
            merged = merge(tsrc, ssrc)
        except (MergeError, TypeError, OSError) as e:          # TypeError: không có file scaffolding (scaf = None)
            emit(config, seed, status="DONE", result="MERGE_FAILED", final_state="MERGE_FAILED", merge_error=str(e), **rec)
            continue
        merged_path = os.path.join(dst, "merged.java")
        with open(merged_path, "w", encoding="utf-8", newline="\n") as f:
            f.write(merged)
        rec["suite_sha256"] = c.sha256_file(merged_path)

        # xác minh 5 bước + tsDetect bằng arrow trên bản evo (PIT đo riêng bằng script)
        v1 = c.arrow_check(ctx["arrow"], task, evo, merged_path, SYSTEM, home, logs, name + "__verify", pit=False)
        rec.update(v1)
        if v1["status"] != "DONE":                             # lỗi hạ tầng → ghi INFRA, sang seed sau
            emit(config, seed, **rec)
            continue
        bcheck = c.compare_baseline(orig_base, c.baseline_of(v1["run_dir"], fqn))   # so baseline gốc với bản vá
        rec.update(bcheck)
        rec["final_state"] = c.final_state(v1, bcheck)
        rec["smell_density"] = (v1.get("metrics") or {}).get("smell_density")
        if rec["final_state"] == "VALID":
            # bộ test arrow đã xác minh (đã đổi tên lớp thành <Lớp>_ArrowTest)
            cand = os.path.join(c.target_dir(v1["run_dir"], fqn), "block2", "candidate.java")
            with open(cand, encoding="utf-8") as f:
                code = f.read()
            # JaCoCo: bản separateClassLoader = false, chỉ chạy bộ này (JUnitCore, JDK dự án)
            scl = re.sub(r"separateClassLoader\s*=\s*true", "separateClassLoader = false", code)
            jac = c.jacoco_measure(snap, mbuild, scl, v1["test_fqn"], extra_cp, home,
                                   os.path.join(gen_dir, "jacoco"), logs, name)
            rec["jacoco"] = {k: v for k, v in jac.items() if k != "counters"}
            if jac["status"] == "OK":
                rec.update(line=jac["line"], branch=jac["branch"], method=jac["method"])
            # PIT: bộ gốc, cùng tham số PIT của arrow; minion chạy bằng JDK của dự án
            pit = c.pit_measure(snap, mbuild, code, v1["test_fqn"], extra_cp, home, ctx["base_cfg"],
                                os.path.join(gen_dir, "pit"), logs, name, minion_jdk=snap["workspace"]["jdk_home"])
            rec["pit"] = pit
            if pit["status"] == "OK":
                rec.update(mutation=pit["score"], test_strength=pit["test_strength"])
            rec["pit_source"] = "script-minion-jdk-du-an"
        emit(config, seed, **rec)
    if not ctx.get("keep_work"):
        shutil.rmtree(work, ignore_errors=True)                # xóa bản sao (cách A)


def main():
    ap = argparse.ArgumentParser(description="TN3: EvoSuite trên phần lớp của một máy.")
    ap.add_argument("--machine", required=True)
    ap.add_argument("--part", help="phần cần chạy (mặc định = --machine)")
    ap.add_argument("--workers", type=int, default=3)
    ap.add_argument("--arrow", default="arrow")
    ap.add_argument("--base-home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")))
    ap.add_argument("--task", action="append", help="chạy khói: chỉ lớp này (lặp lại được)")
    ap.add_argument("--limit", type=int, help="chạy khói: N lớp đầu của phần")
    ap.add_argument("--seeds", help="chạy khói (hoặc kèm --redo): danh sách seed, ví dụ 1 hoặc 1,2")
    ap.add_argument("--redo", action="append", help="CHẠY LẠI lớp này và XÓA SẠCH kết quả, log, bộ test của lần chạy cũ (lặp lại được)")
    ap.add_argument("--budget", action="store_true", help="cấu hình phụ cùng ngân sách (sau tn3_arrow.py)")
    ap.add_argument("--retry-infra", action="store_true")
    ap.add_argument("--homes-dir")
    ap.add_argument("--keep-work", action="store_true", help="giữ thư mục làm việc (để gỡ lỗi)")
    a = ap.parse_args()

    smoke = bool(a.task or a.limit or (a.seeds and not a.redo))         # --seeds đi kèm --redo thì vẫn là kết quả thật
    cfg = c.load_config()
    root = c.out_root(a.machine, smoke)
    os.makedirs(root, exist_ok=True)
    version, commit = c.check_arrow(a.arrow)
    evo_sha = c.ensure_evosuite()                              # tải/kiểm EvoSuite, cài runtime vào ~/.m2
    classes, _ = tn3_lists.load_frozen()
    mine = c.select_classes(classes, a.part or a.machine, a.redo or a.task, a.limit)
    base_cfg = c.base.load_config(a.base_home)
    c.lock_invocation(root, "tn3_evosuite.py", {"tool_version": version, "tool_commit": commit, "evosuite": evo_sha,
                                                "setup": c.EVOSUITE_SETUP, "machine": a.machine,
                                                "part": a.part or a.machine, "workers": a.workers, "budget": a.budget})
    runs_path = os.path.join(root, "evosuite_runs.jsonl")
    # chỉ dòng của cách dựng hiện tại (v3) mới được tính là đã xong
    if a.redo:   # --redo: xóa dòng cũ của (lớp, cấu hình, seed) cần chạy lại → bên dưới coi là chưa làm
        want_cfg = "budget" if a.budget else "default"
        want_seeds = {int(s) for s in a.seeds.split(",")} if a.seeds else None
        def old_files(r):                                      # log và bộ test cũ của đúng (lớp, cấu hình, seed)
            name = f"{r['config']}-seed{r['seed']}"
            return [os.path.join(root, "logs", SYSTEM, r["task_id"], name + "__*"),
                    os.path.join(root, "suites", SYSTEM, r["task_id"], name)]
        n = c.redo_remove(runs_path, lambda r: r["task_id"] in a.redo and r.get("config") == want_cfg
                          and (want_seeds is None or r.get("seed") in want_seeds), old_files)
        print(f"[redo] đã xóa sạch {n} kết quả cũ (dòng, log, bộ test, thư mục run)")
    latest = {k: r for k, r in c.latest_by(runs_path, key_of).items() if r.get("setup") == c.EVOSUITE_SETUP}
    seeds = [int(s) for s in a.seeds.split(",")] if a.seeds else cfg["evosuite"]["seeds"]

    # dòng ARROW của cùng máy: cần cho --budget (thời gian ARROW của từng lớp)
    arrow_runs = list(c.latest_by(os.path.join(root, "arrow_runs.jsonl"), lambda r: (r["task_id"], r["model"])).values())
    todo, waiting = {}, []
    for cl in mine:
        items = []
        if a.budget:                                           # cấu hình phụ: 1 lần, seed đầu tiên, ngân sách theo ARROW
            b = budget_seconds(cl["task_id"], arrow_runs, cfg["evosuite"]["budget_min_seconds"])
            if b is None:
                waiting.append(cl["task_id"])                  # ARROW của lớp chưa xong → chờ
                continue
            items.append(("budget", seeds[0], b))
        else:                                                  # cấu hình chính: mọi seed
            items += [("default", s, None) for s in seeds]
        need = []
        for config, seed, budget in items:
            rec = latest.get((cl["task_id"], config, seed))
            if rec is None or (a.retry_infra and rec.get("status") != "DONE"):
                need.append((config, seed, budget))
        if need:
            todo[cl["task_id"]] = need
    jobs = [cl for cl in mine if cl["task_id"] in todo]
    if waiting:
        print(f"[i] {len(waiting)} lớp chưa có kết quả nhánh ARROW nên chưa chạy cấu hình cùng ngân sách.")
    print(f"TN3 EvoSuite · máy {a.machine} · {len(jobs)}/{len(mine)} lớp cần chạy · seed {seeds} · "
          f"{'cùng ngân sách' if a.budget else 'cấu hình chính'} · {a.workers} worker")
    if not jobs:
        return
    c.base.keep_awake()
    pool = c.HomePool(a.base_home, a.homes_dir or c.default_homes_root(a.machine, smoke), "evo", a.workers, a.arrow)
    ctx = {"arrow": a.arrow, "commit": commit, "machine": a.machine, "root": root, "runs_path": runs_path, "todo": todo,
           "cfg": cfg, "base_cfg": base_cfg, "work_root": c.default_work_root(a.machine, smoke),
           "keep_work": a.keep_work}
    c.run_jobs(jobs, a.workers, lambda cl, home: run_class(cl, home, ctx), pool)
    print("Xong. Kết quả:", runs_path)


if __name__ == "__main__":
    main()
