#!/usr/bin/env python3
"""TN3, nhánh ChatUniTest 2.1.1, trên phần lớp của một máy. Thiết kế: cac_thuc_nghiem.md, "Chạy ChatUniTest"; chatunitest_TN3.md.

Mỗi lớp Maven × mỗi mô hình (gpt6luna, qwen3coder-or), 1 lần; lớp Gradle ghi NOT_APPLICABLE (không tính thất bại):
    1. arrow inspect repo gốc → JDK, lệnh build, baseline GỐC
    2. bản sao "gen": thêm chatunitest-starter 1.4.0 (scope test) vào module của lớp, mvn install -DskipTests (module +
       các module nó cần), rồi chạy plugin bằng tọa độ đầy đủ (không sửa khối <plugin> của pom):
         mvn io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class -DselectClass=<FQN> -Dmodel=gpt-4o
             -Durl=<proxy>/<nhãn>/<mô hình>/v1/chat/completions -DtestNumber=5 -DmaxRounds=5 -Dmerge=true ...
       Mô hình gọi qua llm_proxy.py (đổi "gpt-4o" sang mô hình thật, cùng tham số suy luận như ARROW trừ max_tokens).
       JDK chạy plugin = JDK của dự án nếu ≥ 11, không thì JDK 11 (plugin biên dịch cho Java 11).
    3. 1 lớp test: dùng nguyên; nhiều lớp: gộp phẳng thành 1 lớp (merge_flat) → bản sao "ver" (chỉ thêm starter) → arrow gen --project ver --c0 <bộ> --no-repair, có đo
    4. so baseline gốc với bản vá (starter kéo JUnit 5, vintage, Mockito: làm mất test đạt → REGRESSION)
Không cho ra bộ test (lỗi, quá giờ) → NO_OUTPUT, tính vào mẫu số như INVALID_OUTPUT của ARROW.
Kết quả: results/<máy>/chatunitest_runs.jsonl, proxy_calls.jsonl, suites/chatunitest/, logs/chatunitest/

    python3 tn3_chatunitest.py --machine dung --workers 2 --arrow ~/.local/share/arrow-dev/build-tn2/arrow
    Chạy khói (chốt 26/11, thay 50 lớp pilot): --smoke   → 2 lớp Maven của máy (1 JUnit 4, 1 JUnit 5) × 2 mô hình
    Chạy khói tùy chọn: --task <task_id> [--model gpt6luna]
    Chạy lại và ghi đè kết quả thật: --redo <task_id> [--model qwen3coder-or]
"""
import argparse          # tham số dòng lệnh
import os                # đường dẫn
import re                # tìm dòng khai báo lớp (thêm @RunWith)
import shutil            # chép/xóa
import time              # nhãn thời gian cho proxy

import llm_proxy         # proxy mô hình tự viết
import tn3_common as c   # thư viện chung TN3
import tn3_lists         # danh sách đóng băng
from gop_scaffolding import MergeError, merge_flat     # gộp phẳng các lớp test thành một lớp (cách A)

SYSTEM = "chatunitest"
PLUGIN = "io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class"   # groupId viết thường (bản viết hoa chỉ tới 1.4.1)
CLIENT_MODEL = "gpt-4o"          # tên ChatUniTest chấp nhận (ngân sách ngữ cảnh 122.880 token); proxy đổi sang mô hình thật


def key_of(rec):
    """Khóa của một dòng: (lớp, mô hình)."""
    return (rec["task_id"], rec["model"])


def plugin_jdk(snap, base_cfg):
    """JDK chạy plugin: plugin biên dịch cho Java 11 → JDK dự án nếu ≥ 11, không thì JDK 11 (hoặc JDK ≥ 11 nhỏ nhất)."""
    if (snap["workspace"].get("jdk_major") or 0) >= 11:
        return snap["workspace"]["jdk_home"]
    homes = (base_cfg.get("java") or {}).get("jdk_homes") or {}
    return homes.get("11") or next((homes[k] for k in sorted(homes, key=int) if int(k) >= 11), None)


def find_tests(out_dir):
    """Mọi file .java trong thư mục kết quả: tách (các lớp test, các lớp @Suite).
    Lớp @Suite của merge=true chỉ tham chiếu các lớp test (@SelectClasses), không chứa test."""
    tests, suites = [], []
    for r, _, files in os.walk(out_dir):
        for n in sorted(files):
            if n.endswith(".java"):
                p = os.path.join(r, n)
                with open(p, encoding="utf-8", errors="replace") as f:
                    (suites if "@SelectClasses" in f.read() else tests).append(p)
    return tests, suites


RUNWITH = "@org.junit.runner.RunWith(org.junit.platform.runner.JUnitPlatform.class)"


def needs_runwith(v, code, fqn):
    """Có phải thêm @RunWith(JUnitPlatform.class) không: lần xác minh ra DISCOVERY (không tìm thấy test), log chạy test
    cho thấy Surefire dùng provider JUnit 4, và lớp chưa có @RunWith."""
    if v.get("status") != "DONE" or v.get("result") != "DISCOVERY" or "@RunWith" in code or "RunWith(" in code:
        return False
    log = os.path.join(c.target_dir(v["run_dir"], fqn), "block3", "c0", "logs", "run_target.log")
    try:
        with open(log, encoding="utf-8", errors="replace") as f:
            return "JUnit4Provider" in f.read()
    except OSError:
        return False


def add_runwith(code):
    """Chèn @RunWith(JUnitPlatform.class) ngay trên dòng khai báo lớp (lớp ngoài cùng đầu tiên)."""
    m = re.search(r"^[ \t]*(?:public[ \t]+|final[ \t]+|abstract[ \t]+)*class[ \t]+\w+", code, re.M)
    if not m:
        return code
    return code[:m.start()] + RUNWITH + "\n" + code[m.start():]


def smoke_classes(mine):
    """Chạy khói (chốt 26/11/2026): lớp Maven JUnit 4 đầu tiên và lớp Maven JUnit 5 đầu tiên trong phần của máy."""
    pick = []
    for fw in ("junit4", "junit5"):
        hit = next((cl for cl in mine if cl["chatunitest_applicable"] and cl["framework"] == fw), None)
        if hit:
            pick.append(hit)
    if len(pick) < 2:
        print(f"[!] Phần này chỉ có {len(pick)} lớp phù hợp cho chạy khói.")
    return pick


def run_class(cl, home, ctx):
    """Chạy ChatUniTest cho một lớp, lần lượt từng mô hình còn thiếu; mỗi mô hình ghi một dòng."""
    task = {k: cl[k] for k in ("task_id", "dataset", "repo_url", "checkout_sha", "class_fqn")}
    tid, fqn = cl["task_id"], cl["class_fqn"]
    models = ctx["todo"][tid]                                  # các mô hình cần chạy cho lớp này
    logs = os.path.join(ctx["root"], "logs", SYSTEM, tid)
    work = os.path.join(ctx["work_root"], f"cut-{tid}")        # thư mục làm việc (xóa khi xong)
    common = dict(task, system=SYSTEM, machine=ctx["machine"], tool_commit=ctx["commit"], client_model=CLIENT_MODEL)

    def emit(model, **kw):
        """Ghi một dòng kết quả cho một mô hình."""
        rec = dict(common, model=model, finished_at=c.now_iso(), **kw)
        c.base.mark_fatal(rec)                                 # hết tiền / mô hình lỗi → STOPPED, dừng cả lô
        c.append_jsonl(ctx["runs_path"], rec, c.JSONL_LOCK)
        print(f"[{rec.get('status')}] {tid} {model}: {rec.get('final_state') or rec.get('result')}", flush=True)

    if not cl["chatunitest_applicable"]:                       # lớp Gradle: không áp dụng
        for m in models:
            emit(m, status="DONE", result="NOT_APPLICABLE", final_state="NOT_APPLICABLE", reason="Gradle")
        return
    snap = c.inspect(ctx["arrow"], task, home, logs)           # 1. inspect repo gốc
    if snap["status"] != "OK":
        for m in models:
            emit(m, status="INFRA", result="INSPECT_" + snap["status"], error_message=snap.get("reason"))
        return
    info = c.snap_summary(snap)
    orig_base = (set(snap["baseline"].get("passing_in_all_runs") or []), set(snap["baseline"].get("flaky") or []),
                 set(snap["baseline"].get("failing_in_all_runs") or []))   # baseline GỐC
    jdk = plugin_jdk(snap, ctx["base_cfg"])
    ws = snap["workspace"]
    sg, sa, sv = ctx["cfg"]["chatunitest"]["starter"].split(":")           # tọa độ starter 1.4.0
    for model in models:
        if c.base.STOP.is_set():
            break
        rec = dict(info, plugin_jdk=os.path.basename(os.path.dirname(os.path.dirname(jdk))) if jdk else None, starter=f"{sg}:{sa}:{sv}",
                   plugin_maven=ctx["plugin_mvn"])
        gen, ver = os.path.join(work, f"gen-{model}"), os.path.join(work, f"ver-{model}")   # 2 bản sao cho mô hình này
        try:
            c.checkout(home, task, gen)
            c.checkout(home, task, ver)
            for copy in (gen, ver):                            # cả hai bản đều thêm starter (type pom, scope test)
                c.maven_add_dependency(os.path.join(c.module_dir(copy, snap), "pom.xml"), sg, sa, sv, type_="pom")
        except Exception as e:                                   # noqa: BLE001
            emit(model, status="INFRA", result="CHECKOUT_FAILED", error_message=str(e), **rec)
            continue
        exe = c._executable(gen, snap)
        broot = os.path.normpath(os.path.join(gen, snap["baseline"].get("working_dir") or "."))
        sel = ws.get("maven_module_selector") or []            # -pl <module> -am: chỉ module của lớp và các module nó cần
        # plugin cần các module của dự án có sẵn trong ~/.m2 → mvn install (bỏ chạy test)
        inst = c.run_logged([exe] + ws["common_flags"] + sel + ["-DskipTests", "-Dmaven.test.skip.exec=true", "install"],
                            broot, os.path.join(logs, f"{model}__install.log"), c.TIMEOUT_BUILD, env=c.java_env(ws["jdk_home"]))
        if inst["exit_code"] != 0:
            emit(model, status="DONE", result="BUILD_FAILED", final_state="NO_OUTPUT",
                 gen_reason="mvn install lỗi (bản sao có starter): " + c.tail(os.path.join(logs, f"{model}__install.log"), 6)[-600:], **rec)
            continue
        out_dir, tmp_dir = os.path.join(work, f"out-{model}"), os.path.join(work, f"tmp-{model}")   # thư mục ra/tạm riêng
        tag = f"{tid}__{model}__{time.strftime('%Y%m%d%H%M%S')}"     # nhãn riêng mỗi lần chạy: token không cộng dồn khi chạy lại
        cfg = ctx["cfg"]["chatunitest"]
        # Plugin chạy bằng Maven CỦA MÁY (mvn trên PATH, ≥ 3.9), không dùng wrapper mvnw của dự án: plugin 2.1.1 cần
        # maven-resolver mới, wrapper cũ (vd. Maven 3.6.1) báo NoSuchMethodError (ConfigUtils.parseCommaSeparatedUniqueNames).
        # Bước install ở trên và bước xác minh của arrow vẫn dùng đúng Maven của dự án.
        # Chạy từ THƯ MỤC GỐC dự án với cùng bộ chọn module của arrow (-pl <module> -am), không chạy trong thư mục module:
        # dự án nhiều module (nhất là version kiểu ${revision}) chạy riêng trong module thì Maven không phân giải được
        # các module anh em → classpath của ChatUniTest thiếu cả thư viện starter (lỗi "package org.junit.jupiter.api
        # does not exist"). -am làm plugin chạy cả trên các module phụ thuộc; ở đó không có lớp cần test nên plugin
        # báo lỗi ngay (không gọi mô hình) → thêm -fn (fail-never) để Maven vẫn chạy tới module chính.
        cmd = [ctx["plugin_mvn"], "-B", "-ntp"] + (sel + ["-fn"] if sel else []) + [PLUGIN,
               f"-DselectClass={fqn}",                         # lớp cần sinh test
               f"-Dmodel={CLIENT_MODEL}",                      # tên giả ChatUniTest nhận được
               f"-Durl=http://127.0.0.1:{ctx['port']}/{tag}/{model}/v1/chat/completions",   # gọi qua proxy
               "-DapiKeys=proxy",                              # khóa giả; khóa thật nằm ở proxy
               f"-DtestNumber={cfg['testNumber']}", f"-DmaxRounds={cfg['maxRounds']}", f"-Dmerge={str(cfg['merge']).lower()}",
               "-Dtemperature=0", f"-DtestOutput={out_dir}", f"-DtmpOutput={tmp_dir}"]
        if jdk is None:
            emit(model, status="DONE", result="NO_JDK11", final_state="NO_OUTPUT", gen_reason="cần JDK 11+ để chạy plugin", **rec)
            continue
        g = c.run_logged(cmd, broot, os.path.join(logs, f"{model}__chatunitest.log"), cfg["timeout_seconds"], env=c.java_env(jdk))
        rec.update(gen_seconds=g["seconds"], gen_exit=g["exit_code"], gen_timed_out=g["timed_out"])
        # chi phí: đọc log proxy, lọc theo nhãn của lần chạy này
        calls = [x for x in c.read_jsonl(ctx["proxy_log"]) if x.get("tag") == tag]
        rec.update(proxy_tag=tag, cut_llm_calls=len(calls), cut_prompt_tokens=sum(x.get("prompt_tokens") or 0 for x in calls),
                   cut_completion_tokens=sum(x.get("completion_tokens") or 0 for x in calls),
                   cut_llm_errors=sum(1 for x in calls if (x.get("status") or 0) >= 400),
                   cut_llm_seconds=round(sum(x.get("seconds") or 0 for x in calls), 1))
        tests, suites = find_tests(out_dir)
        rec.update(generated_test_classes=len(tests), generated_suite_classes=len(suites))
        dst = os.path.join(ctx["root"], "suites", SYSTEM, tid, model)   # lưu các file ChatUniTest sinh ra
        if tests or suites:
            os.makedirs(dst, exist_ok=True)
            for p in tests + suites:
                shutil.copy(p, os.path.join(dst, os.path.basename(p)))
        if not tests:                                          # không ra lớp test nào
            emit(model, status="DONE", result="NO_OUTPUT", final_state="NO_OUTPUT",
                 gen_reason="quá giờ" if g["timed_out"] else c.tail(os.path.join(logs, f"{model}__chatunitest.log"), 8)[-800:], **rec)
            continue
        try:                                                   # gộp các lớp test thành một lớp <Lớp>_ChatUniTest
            srcs = []
            for p in tests:
                with open(p, encoding="utf-8") as f:
                    srcs.append(f.read())
            if len(srcs) == 1:
                # Chỉ 1 lớp test: dùng nguyên lớp đó, không bọc @Nested. Lý do: arrow chạy test bằng -Dtest=<lớp>;
                # Surefire cũ (vd. 2.22) dùng provider JUnit Platform lọc theo đúng tên lớp nên bỏ mất lớp lồng
                # (Lớp$Con) → chạy 0 test (DISCOVERY), dù bộ test không sai.
                merged, merge_info, merge_mode = srcs[0], {"classes": 1, "renamed": [], "class_annotations": []}, "single"
            else:
                # Nhiều lớp: gộp phẳng thành một lớp thường (chốt 26/11/2026, cách A; tên trùng đổi hậu tố _c<số>)
                merged, merge_info = merge_flat(srcs, fqn.rsplit(".", 1)[-1] + "_ChatUniTest")
                merge_mode = "flat"
        except MergeError as e:
            emit(model, status="DONE", result="MERGE_FAILED", final_state="MERGE_FAILED", merge_error=str(e), **rec)
            continue
        suite = os.path.join(dst, "merged.java")
        with open(suite, "w", encoding="utf-8", newline="\n") as f:
            f.write(merged)
        rec.update(merge_mode=merge_mode, merge_info=merge_info, suite_sha256=c.sha256_file(suite))
        # xác minh 5 bước + đo JaCoCo/PIT/tsDetect bằng arrow trên bản "ver"
        v = c.arrow_check(ctx["arrow"], task, ver, suite, SYSTEM + "-" + model, home, logs, f"{model}__verify")
        if needs_runwith(v, merged, fqn):
            # Surefire chạy bằng provider JUnit 4 (starter kéo junit-platform-runner) nên bỏ qua lớp JUnit 5 → 0 test.
            # Thêm @RunWith(JUnitPlatform.class) như lớp @Suite của ChatUniTest rồi xác minh lại MỘT lần (chốt 26/11/2026).
            rec.update(first_verify_result=v.get("result"), runwith_added=True)
            merged = add_runwith(merged)
            with open(suite, "w", encoding="utf-8", newline="\n") as f:
                f.write(merged)
            rec["suite_sha256"] = c.sha256_file(suite)
            v = c.arrow_check(ctx["arrow"], task, ver, suite, SYSTEM + "-" + model, home, logs, f"{model}__verify_runwith")
        rec.update(v)
        if v["status"] == "DONE":
            bcheck = c.compare_baseline(orig_base, c.baseline_of(v["run_dir"], fqn))   # starter có làm hỏng test có sẵn không
            rec.update(bcheck)
            rec["final_state"] = c.final_state(v, bcheck)
            m = v.get("metrics") or {}
            rec.update(line=m.get("line"), branch=m.get("branch"), method=m.get("method"), mutation=m.get("mutation"),
                       test_strength=m.get("test_strength"), smell_density=m.get("smell_density"),
                       pit_source="arrow" if (v.get("tools") or {}).get("pit") == "OK" else None)
            if rec["final_state"] == "VALID" and (info.get("framework") != "junit5" or (v.get("tools") or {}).get("pit") != "OK"):
                # Test ChatUniTest luôn là JUnit 5. Dự án không phải JUnit 5 (JUnit 4, TestNG): arrow chọn plugin PIT theo
                # framework của dự án nên thiếu pitest-junit5-plugin → PIT vẫn báo OK nhưng không chạy được test nào
                # (mọi đột biến NO_COVERAGE, mutation = 0; thấy khi chạy khói 26/11). Vì vậy LUÔN đo lại như arrow làm với
                # dự án JUnit 5: cùng tham số, thêm pitest-junit5-plugin (chốt 26/11).
                rec["arrow_pit_mutation"] = rec.get("mutation")               # giữ số arrow đo để đối chiếu
                build = c.build_copy(ver, snap, os.path.join(logs, f"{model}__build_ver.log"))
                if build["ok"]:
                    cand = os.path.join(c.target_dir(v["run_dir"], fqn), "block2", "candidate.java")
                    with open(cand, encoding="utf-8") as f:
                        code = f.read()
                    pit = c.pit_measure(snap, build, code, v["test_fqn"], [], home, ctx["base_cfg"],
                                        os.path.join(work, f"pit-{model}"), logs, model, plugin="junit5")
                else:
                    pit = {"status": "FAILED", "reason": "không build được bản sao để đo PIT"}
                rec["pit"] = pit
                if pit["status"] == "OK":
                    rec.update(mutation=pit["score"], test_strength=pit["test_strength"], pit_source="script-junit5")
                elif info.get("framework") != "junit5":
                    # số của arrow chắc chắn sai (không chạy được test JUnit 5) → để trống = không đo được (lớp lọc 3).
                    # Ví dụ: dự án ép JUnit Platform cũ (1.3) mà pitest-junit5-plugin 1.2.1 cần ≥ 1.5 → minion sập.
                    rec.update(mutation=None, test_strength=None, pit_source="script-junit5-failed")
        emit(model, **rec)
    if not ctx.get("keep_work"):
        shutil.rmtree(work, ignore_errors=True)                # xóa bản sao (cách A)


def main():
    ap = argparse.ArgumentParser(description="TN3: ChatUniTest trên phần lớp của một máy.")
    ap.add_argument("--machine", required=True)
    ap.add_argument("--part")
    ap.add_argument("--workers", type=int, default=2)
    ap.add_argument("--arrow", default="arrow")
    ap.add_argument("--base-home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")))
    ap.add_argument("--smoke", action="store_true", help="chạy khói: 2 lớp Maven của máy (1 JUnit 4, 1 JUnit 5) × 2 mô hình")
    ap.add_argument("--task", action="append", help="chạy khói: chỉ lớp này")
    ap.add_argument("--model", action="append", help="chỉ chạy mô hình này (vd. GPT trước, Qwen sau khi nạp tiền)")
    ap.add_argument("--redo", action="append", help="CHẠY LẠI lớp này và XÓA SẠCH kết quả, log, bộ test của lần chạy cũ (lặp lại được)")
    ap.add_argument("--limit", type=int)
    ap.add_argument("--retry-infra", action="store_true")
    ap.add_argument("--homes-dir")
    ap.add_argument("--keep-work", action="store_true")
    a = ap.parse_args()

    cfg = c.load_config()
    smoke = bool(a.smoke or a.task or a.limit)       # chạy thử ghi vào results/<máy>-smoke; --model chỉ lọc mô hình (như tn3_arrow.py)
    root = c.out_root(a.machine, smoke)
    os.makedirs(root, exist_ok=True)
    version, commit = c.check_arrow(a.arrow)
    classes, _ = tn3_lists.load_frozen()
    if a.smoke:
        mine = smoke_classes(c.select_classes(classes, a.part or a.machine))
    else:
        mine = c.select_classes(classes, a.part or a.machine, a.redo or a.task, a.limit)
    models = a.model or cfg["chatunitest"]["models"]
    plugin_mvn = shutil.which("mvn") or shutil.which("mvn.cmd")   # Maven của máy, dùng để chạy plugin ChatUniTest
    if not plugin_mvn:
        raise SystemExit("Không thấy lệnh mvn trên PATH (plugin ChatUniTest cần Maven ≥ 3.9 của máy).")
    base_cfg = c.base.load_config(a.base_home)
    c.base.check_models(base_cfg, models)                      # mô hình phải khai trong arrow
    c.lock_invocation(root, "tn3_chatunitest.py", {"tool_version": version, "tool_commit": commit, "machine": a.machine,
                                                   "workers": a.workers, "plugin": PLUGIN, "starter": cfg["chatunitest"]["starter"],
                                                   "client_model": CLIENT_MODEL, "models": models, "smoke": a.smoke,
                                                   "plugin_maven": plugin_mvn})
    runs_path = os.path.join(root, "chatunitest_runs.jsonl")
    if a.redo:   # --redo: xóa dòng cũ của (lớp, mô hình) cần chạy lại → bên dưới coi là chưa làm
        def old_files(r):                                      # log, bộ test, thư mục pit cũ của đúng (lớp, mô hình)
            return [os.path.join(root, "logs", SYSTEM, r["task_id"], r["model"] + "__*"),
                    os.path.join(root, "suites", SYSTEM, r["task_id"], r["model"])]
        n = c.redo_remove(runs_path, lambda r: r["task_id"] in a.redo and r["model"] in models, old_files)
        # các lượt gọi mô hình cũ của (lớp, mô hình) trong log proxy (nhãn bắt đầu bằng <task_id>__<mô hình>__)
        c.redo_remove(os.path.join(root, "proxy_calls.jsonl"),
                      lambda r: any((r.get("tag") or "").startswith(f"{t}__{m}__") for t in a.redo for m in models))
        print(f"[redo] đã xóa sạch {n} kết quả cũ (dòng, log, bộ test, lượt gọi mô hình, thư mục run)")
    latest = c.latest_by(runs_path, key_of)
    todo = {}
    for cl in mine:
        # mô hình cần chạy: chưa có dòng; dòng STOPPED; hoặc --retry-infra và dòng không DONE
        need = [m for m in models if (r := latest.get((cl["task_id"], m))) is None or r.get("status") == "STOPPED"
                or (a.retry_infra and r.get("status") != "DONE")]
        if need:
            todo[cl["task_id"]] = need
    jobs = [cl for cl in mine if cl["task_id"] in todo]
    print(f"TN3 ChatUniTest · máy {a.machine} · {len(jobs)}/{len(mine)} lớp · mô hình {models} · {a.workers} worker"
          + (" · CHẠY KHÓI" if smoke else ""))
    if not jobs:
        return
    proxy_log = os.path.join(root, "proxy_calls.jsonl")
    srv, port = llm_proxy.start(a.base_home, proxy_log)       # bật proxy (cổng ngẫu nhiên)
    c.base.keep_awake()
    pool = c.HomePool(a.base_home, a.homes_dir or c.default_homes_root(a.machine, smoke), "cut", a.workers, a.arrow)
    ctx = {"arrow": a.arrow, "commit": commit, "machine": a.machine, "root": root, "runs_path": runs_path, "todo": todo,
           "cfg": cfg, "base_cfg": base_cfg, "port": port, "proxy_log": proxy_log, "plugin_mvn": plugin_mvn,
           "work_root": c.default_work_root(a.machine, smoke), "keep_work": a.keep_work}
    try:
        c.run_jobs(jobs, a.workers, lambda cl, home: run_class(cl, home, ctx), pool)
    finally:
        srv.shutdown()                                         # luôn tắt proxy, kể cả khi lỗi
    if c.base.STOP.is_set():
        c.base.notify_stop(root, " ".join(["python3"] + __import__("sys").argv))
    print("Xong. Kết quả:", runs_path)


if __name__ == "__main__":
    main()
