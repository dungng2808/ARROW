#!/usr/bin/env python3
"""TN3, phép phát hiện lỗi thật trên GitBug-Java (chạy SAU tn3_arrow.py, tn3_evosuite.py, tn3_chatunitest.py của máy).

Bản lỗi (bước 0.6 của thiết kế): bản sửa (checkout_sha = commit_hash của bug) rồi `git apply -R bug_patch`
(bug_patch lấy từ gitbug_bugs.jsonl; chỉ đổi mã nguồn, test giữ ở bản sửa).
Mỗi bộ test VALID (sinh trên bản sửa) của cả ba hệ thống, cộng test người viết (hàng tham chiếu):
    arrow gen --project <bản lỗi> --c0 <bộ> --no-repair --no-metrics   (cùng JDK, cùng 5 bước như khi xác minh)
    trạng thái ASSERTION / RUNTIME  → DETECTED       (có test hỏng trên bản lỗi = phát hiện được lỗi)
    VALID / REGRESSION              → NOT_DETECTED   (mọi test của bộ vẫn đạt)
    COMPILE                         → COMPILE_FAILED (báo riêng, không tính)
    khác (DISCOVERY, UNKNOWN, ...)  → NOT_RUN        (báo riêng)
Bộ EvoSuite chạy trên bản lỗi dựng giống lúc xác minh (tn3_common.evosuite_setup); bộ ChatUniTest: bản lỗi có thêm starter.
EvoSuite (chốt 26/11/2026): chạy mọi seed VALID, nhưng KẾT QUẢ CHÍNH của lớp là bộ của seed nhỏ nhất cho bộ VALID
(cấu hình chính) → cột primary = true; các seed khác chỉ để mô tả phụ.
Kết quả: results/<máy>/phat_hien_loi.jsonl

    python3 tn3_phat_hien_loi.py --machine dung --workers 2 --arrow ~/.local/share/arrow-dev/build-tn2/arrow
"""
import argparse          # tham số dòng lệnh
import json              # đọc manifest
import os                # đường dẫn
import shutil            # xóa thư mục
import subprocess        # gọi git

import tn3_common as c   # thư viện chung TN3
import tn3_lists         # danh sách đóng băng

DETECTED = {"ASSERTION", "RUNTIME"}          # trạng thái = có test hỏng trên bản lỗi
NOT_DETECTED = {"VALID", "REGRESSION"}       # trạng thái = mọi test của bộ đạt
BUGS_FROZEN = os.path.join(c.HERE, "gitbug_bugs.jsonl")   # 32 bug của tập đánh giá, chép từ GitBug-Java (+ .sha256)


def verdict(state):
    """Đổi trạng thái xác minh trên bản lỗi thành kết luận phát hiện lỗi."""
    if state in DETECTED:
        return "DETECTED"
    if state in NOT_DETECTED:
        return "NOT_DETECTED"
    if state == "COMPILE":
        return "COMPILE_FAILED"
    return "NOT_RUN"


def find_bug(sha):
    """Bug có commit_hash = checkout_sha của lớp, đọc từ gitbug_bugs.jsonl (đóng băng, kiểm sha256)."""
    with open(BUGS_FROZEN + ".sha256", encoding="utf-8") as f:
        if c.sha256_file(BUGS_FROZEN) != f.read().split()[0]:
            raise SystemExit("gitbug_bugs.jsonl khác bản đã đóng băng. git checkout lại file. Dừng.")
    for b in c.read_jsonl(BUGS_FROZEN):
        if b["commit_hash"] == sha:
            return b
    raise RuntimeError(f"không có bug commit {sha} trong gitbug_bugs.jsonl")


def make_buggy(home, task, bug, dest, log_path):
    """Dựng bản lỗi ở `dest`: chép bản sửa, rồi đảo ngược bug_patch. Kiểm mọi file trong patch đều đã đổi."""
    c.checkout(home, task, dest)                                       # bản sửa (đúng commit của lớp)
    patch = dest.rstrip("/\\") + ".bug.patch"
    with open(patch, "w", encoding="utf-8", newline="\n") as f:
        f.write(bug["bug_patch"])
    # Bản sao nằm trong repo git ARROW: chạy "git apply" ở đó thì git tính đường dẫn từ gốc repo ARROW và lặng lẽ bỏ
    # qua file ngoài thư mục hiện tại (mã thoát vẫn 0). Vì vậy tạo repo git tạm ngay trong bản sao, apply, rồi xóa.
    files = sorted({ln[6:].strip() for ln in bug["bug_patch"].splitlines() if ln.startswith("+++ b/")})   # file patch đụng tới
    before = {p: c.sha256_file(os.path.join(dest, p)) for p in files if os.path.exists(os.path.join(dest, p))}
    subprocess.run(["git", "init", "-q"], cwd=dest, check=True)
    r = subprocess.run(["git", "apply", "-R", "--whitespace=nowarn", patch], cwd=dest, capture_output=True, text=True)   # -R = đảo ngược
    shutil.rmtree(os.path.join(dest, ".git"), ignore_errors=True)      # xóa repo git tạm
    after = {p: (c.sha256_file(os.path.join(dest, p)) if os.path.exists(os.path.join(dest, p)) else None) for p in files}
    changed = [p for p in files if before.get(p) != after.get(p)]
    with open(log_path, "w", encoding="utf-8") as f:
        f.write(f"$ git apply -R {patch}\n{r.stdout}{r.stderr}\n# exit={r.returncode}\n# đổi {len(changed)}/{len(files)} file: {changed}\n")
    os.remove(patch)
    if r.returncode != 0:
        raise RuntimeError("git apply -R lỗi: " + r.stderr[-400:])
    if len(changed) != len(files):                                     # có file không đổi → bản lỗi sai
        raise RuntimeError(f"bản lỗi chỉ đổi {len(changed)}/{len(files)} file của bug_patch")
    return dest


def suites_of(root, tid, fqn):
    """Mọi bộ VALID của lớp trên máy này: danh sách (hệ thống, biến thể, file bộ test, nguồn, có phải kết quả chính)."""
    out = []
    for r in c.latest_by(os.path.join(root, "arrow_runs.jsonl"), lambda x: (x["task_id"], x["model"])).values():
        if r["task_id"] == tid and r.get("final_state") == "VALID" and r.get("run_dir"):
            f = os.path.join(c.target_dir(r["run_dir"], fqn), "block3", "final.java")   # bộ cuối của ARROW
            out.append(("arrow", r["model"], f, r.get("source"), True))
    evo = [r for r in c.latest_by(os.path.join(root, "evosuite_runs.jsonl"),
                                  lambda x: (x["task_id"], x["config"], x["seed"])).values()
           if r["task_id"] == tid and r.get("final_state") == "VALID" and r.get("run_dir")
           and r.get("setup") == c.EVOSUITE_SETUP]
    # kết quả chính của EvoSuite: seed nhỏ nhất ra VALID của cấu hình chính (chốt 26/11/2026)
    default_seeds = sorted(r["seed"] for r in evo if r["config"] == "default")
    primary_seed = default_seeds[0] if default_seeds else None
    for r in evo:
        f = os.path.join(c.target_dir(r["run_dir"], fqn), "block2", "candidate.java")
        out.append(("evosuite", f"{r['config']}-seed{r['seed']}", f, None,
                    r["config"] == "default" and r["seed"] == primary_seed))
    for r in c.latest_by(os.path.join(root, "chatunitest_runs.jsonl"), lambda x: (x["task_id"], x["model"])).values():
        if r["task_id"] == tid and r.get("final_state") == "VALID" and r.get("run_dir"):
            f = os.path.join(c.target_dir(r["run_dir"], fqn), "block2", "candidate.java")
            out.append(("chatunitest", r["model"], f, None, True))
    return out


def run_class(cl, home, ctx):
    """Chạy mọi bộ (của ba hệ thống + test người viết) của một lớp GitBug-Java trên bản lỗi."""
    task = {k: cl[k] for k in ("task_id", "dataset", "repo_url", "checkout_sha", "class_fqn")}
    tid, fqn = cl["task_id"], cl["class_fqn"]
    logs = os.path.join(ctx["root"], "logs", "phat_hien_loi", tid)
    work = os.path.join(ctx["work_root"], f"bug-{tid}")
    done = ctx["done"]                                                 # các (lớp, hệ thống, biến thể) đã xong

    def emit(system, variant, **kw):
        """Ghi một dòng kết quả."""
        rec = dict(task, system=system, variant=variant, machine=ctx["machine"], tool_commit=ctx["commit"],
                   finished_at=c.now_iso(), **kw)
        c.append_jsonl(ctx["runs_path"], rec, c.JSONL_LOCK)
        print(f"[{rec.get('status')}] {tid} {system} {variant}: {rec.get('verdict') or rec.get('result')}", flush=True)

    # Hàng tham chiếu: paired = test người viết ghép với lớp (manifest); bug_tests = test trong test_patch của bug
    # (theo GitBug-Java phải hỏng trên bản lỗi: dùng để kiểm bản lỗi tái hiện được lỗi)
    items = [("human", "paired", None, None, True), ("human", "bug_tests", None, None, True)] + suites_of(ctx["root"], tid, fqn)
    items = [x for x in items if (tid, x[0], x[1]) not in done]        # bỏ việc đã xong
    if not items:
        return
    snap = c.inspect(ctx["arrow"], task, home, logs)                   # inspect bản sửa (lấy JDK, classpath)
    if snap["status"] != "OK":
        for s, v, _, _, _ in items:
            emit(s, v, status="INFRA", result="INSPECT_" + snap["status"])
        return
    try:
        bug = find_bug(task["checkout_sha"])
        buggy = {"plain": make_buggy(home, task, bug, os.path.join(work, "plain"), os.path.join(logs, "apply_plain.log"))}
        fixed = c.checkout(home, task, os.path.join(work, "fixed"))   # bản sửa: để lấy file test người viết
    except Exception as e:                                     # noqa: BLE001
        for s, v, _, _, _ in items:
            emit(s, v, status="INFRA", result="BUGGY_FAILED", error_message=str(e))
        return
    sg, sa, sv = ctx["cfg"]["chatunitest"]["starter"].split(":")
    for system, variant, suite, source, primary in items:
        if c.base.STOP.is_set():
            break
        try:
            if system == "evosuite" and "evo" not in buggy:            # bản lỗi cho EvoSuite: dựng giống lúc xác minh
                buggy["evo"] = make_buggy(home, task, bug, os.path.join(work, "evo"), os.path.join(logs, "apply_evo.log"))
                c.evosuite_setup(buggy["evo"], snap, snap["test_classpath"])
            if system == "chatunitest" and "cut" not in buggy:         # bản lỗi cho ChatUniTest: thêm starter
                buggy["cut"] = make_buggy(home, task, bug, os.path.join(work, "cut"), os.path.join(logs, "apply_cut.log"))
                c.maven_add_dependency(os.path.join(c.module_dir(buggy["cut"], snap), "pom.xml"), sg, sa, sv, type_="pom")
        except Exception as e:                                 # noqa: BLE001
            emit(system, variant, status="INFRA", result="BUGGY_FAILED", error_message=str(e))
            continue
        project = buggy[{"evosuite": "evo", "chatunitest": "cut"}.get(system, "plain")]
        if system == "human":
            # test người viết, lấy từ bản sửa; chạy riêng từng file; phát hiện nếu có file nào hỏng
            if variant == "paired":
                paths = ctx["manifest"].get(tid, {}).get("test_class_paths") or []
            else:
                paths = [ln[6:].strip() for ln in bug.get("test_patch", "").splitlines()
                         if ln.startswith("+++ b/") and ln.strip().endswith(".java")]
            states = []
            for i, rel in enumerate(paths):
                src = os.path.join(fixed, rel)
                if not os.path.exists(src):
                    continue
                r = c.arrow_check(ctx["arrow"], task, project, src, "human", home, logs, f"human_{variant}{i}", metrics=False)
                states.append(r.get("verify_state") or r.get("result"))
            vs = [verdict(s) for s in states]
            v = "DETECTED" if "DETECTED" in vs else ("NOT_DETECTED" if "NOT_DETECTED" in vs else (vs[0] if vs else "NOT_RUN"))
            emit(system, variant, status="DONE", verdict=v, states=states, files=paths, primary=True)
            continue
        if not os.path.exists(suite):
            emit(system, variant, status="INFRA", result="SUITE_MISSING", suite=suite)
            continue
        r = c.arrow_check(ctx["arrow"], task, project, suite, system, home, logs, f"{system}__{variant}", metrics=False)
        state = r.get("verify_state") or r.get("result")
        emit(system, variant, status=r.get("status"), verdict=verdict(state) if r.get("status") == "DONE" else None,
             state=state, source=source, primary=primary, run_id=r.get("run_id"), tests_passed=r.get("tests_passed"),
             test_method_count=r.get("test_method_count"))
    if not ctx.get("keep_work"):
        shutil.rmtree(work, ignore_errors=True)


def main():
    ap = argparse.ArgumentParser(description="TN3: phát hiện lỗi thật trên GitBug-Java.")
    ap.add_argument("--machine", required=True)
    ap.add_argument("--part")
    ap.add_argument("--workers", type=int, default=2)
    ap.add_argument("--arrow", default="arrow")
    ap.add_argument("--base-home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")))
    ap.add_argument("--task", action="append")
    ap.add_argument("--smoke", action="store_true", help="đọc/ghi results/<máy>-smoke")
    ap.add_argument("--keep-work", action="store_true")
    ap.add_argument("--homes-dir")
    a = ap.parse_args()

    smoke = a.smoke or bool(a.task)
    root = c.out_root(a.machine, smoke)
    os.makedirs(root, exist_ok=True)
    version, commit = c.check_arrow(a.arrow)
    c.ensure_evosuite()                                                # bản lỗi EvoSuite cần runtime trong ~/.m2
    cfg = c.load_config()
    classes, _ = tn3_lists.load_frozen()
    mine = [cl for cl in c.select_classes(classes, a.part or a.machine, a.task) if cl["dataset"] == "gitbug-java"]   # chỉ GitBug-Java
    manifest = {}                                                      # manifest GitBug-Java: test người viết ghép với lớp
    with open(os.path.join(c.base.FINAL_DIR, "gitbug_java_eval.jsonl"), encoding="utf-8") as f:
        for line in f:
            if line.strip():
                r = json.loads(line)
                manifest[r["task_id"]] = r
    c.lock_invocation(root, "tn3_phat_hien_loi.py", {"tool_version": version, "tool_commit": commit, "machine": a.machine})
    runs_path = os.path.join(root, "phat_hien_loi.jsonl")
    done = {(r["task_id"], r["system"], r["variant"]) for r in c.read_jsonl(runs_path) if r.get("status") == "DONE"}
    print(f"TN3 phát hiện lỗi · máy {a.machine} · {len(mine)} lớp GitBug-Java")
    if not mine:
        return
    pool = c.HomePool(a.base_home, a.homes_dir or c.default_homes_root(a.machine, smoke), "bug", a.workers, a.arrow)
    ctx = {"arrow": a.arrow, "commit": commit, "machine": a.machine, "root": root, "runs_path": runs_path, "done": done,
           "manifest": manifest, "cfg": cfg, "work_root": c.default_work_root(a.machine, smoke), "keep_work": a.keep_work}
    c.run_jobs(mine, a.workers, lambda cl, home: run_class(cl, home, ctx), pool)
    print("Xong. Kết quả:", runs_path)


if __name__ == "__main__":
    main()
