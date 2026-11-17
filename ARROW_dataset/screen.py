#!/usr/bin/env python3
"""Screen: kiểm từng lớp ứng viên trước khi chạy thực nghiệm.

Với mỗi lớp, script gọi `arrow inspect`. Lệnh này clone repo, build, rồi chạy
toàn bộ test có sẵn 2 lần (baseline). Sau đó script xem test của người viết
(lớp ở `test_class_path`) có chạy đạt cả 2 lần không.

Kết quả mỗi lớp là một trong ba loại:
    PASS      dùng được.
    EXCLUDED  loại vì dự án có vấn đề (build lỗi, test người viết không đạt...).
    ERROR     lỗi phía mình (mất mạng, hết giờ, thiếu JDK...). Chạy lại sau
              bằng --retry-errors.

Cách chạy:
    python3 screen.py <file.jsonl> [<file.jsonl> ...] --out <thư mục kết quả> --workers 6

    Ví dụ:
    python3 screen.py candidates/catlm_eval.jsonl --out screen --workers 6

Đầu ra trong thư mục --out:
    screen_results.jsonl   mỗi dòng là kết quả của một lớp
    logs/<task_id>.json    JSON gốc của `arrow inspect`
    logs/<task_id>.err     tiến độ và lỗi `arrow inspect` in ra
    homes/w1, w2, ...      ARROW_HOME riêng của từng worker

Dọn đĩa: sau mỗi lớp, script xóa bản clone repo của lớp đó (`mirrors/` của
worker). Tool tự xóa bản sao để build (`work/`), nhưng giữ bản clone mãi;
screen vài trăm repo thì phần này lên tới hàng chục GB.

Chạy tiếp được: bị ngắt giữa chừng thì chạy lại đúng lệnh cũ, script bỏ qua
các lớp đã có kết quả.
"""
import argparse
import json
import os
import queue
import shutil
import signal
import subprocess
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone

# ---------------------------------------------------------------------------
# CẤU HÌNH
# ---------------------------------------------------------------------------

# Thư mục chứa 4 JDK của nhóm (ARROW/Java-version/runtime/jdk-<n>).
# Tool không tự tìm ra chỗ này, nên phải khai báo cho từng ARROW_HOME.
HERE = os.path.dirname(os.path.abspath(__file__))
JDK_DIR = os.path.join(HERE, "..", "Java-version", "runtime")

# Thời gian tối đa cho một lớp. Tool đã tự giới hạn mỗi bước build là 15 phút;
# mức này chỉ để chặn trường hợp bị treo hẳn.
TIMEOUT_PER_CLASS = 60 * 60  # giây

# Mã lỗi nghĩa là repo không còn (bị xóa, chuyển sang riêng tư, mất commit).
# Chạy lại cũng không khá hơn, nên tính là EXCLUDED thay vì ERROR.
REPO_GONE_CODES = {"B1_COMMIT_MISSING", "B1_AUTH_REQUIRED"}


# ---------------------------------------------------------------------------
# 1. ĐỌC DANH SÁCH LỚP ỨNG VIÊN
# ---------------------------------------------------------------------------

def read_candidates(paths):
    """Đọc các file .jsonl do select_datasets.py tạo ra.

    Mỗi dòng là một bản ghi preflight. Script chỉ dùng 6 trường:
    task_id, dataset, repo_url, checkout_sha, class_fqn, test_class_path.
    """
    candidates = []
    for path in paths:
        with open(path, encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    rec = json.loads(line)
                    candidates.append({
                        "task_id": rec["task_id"],
                        "dataset": rec["dataset"],
                        "repo_url": rec["repo_url"],
                        "checkout_sha": rec["checkout_sha"],
                        "class_fqn": rec["class_fqn"],
                        "test_class_path": rec["test_class_path"],
                        "source_file": os.path.basename(path),
                    })
    return candidates


def read_done(results_path, retry_errors):
    """Trả về tập task_id đã có kết quả, để chạy tiếp thì bỏ qua.

    Nếu có --retry-errors thì lớp ERROR không tính là xong, nên được chạy lại.
    """
    done = set()
    if os.path.exists(results_path):
        with open(results_path, encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    r = json.loads(line)
                    if retry_errors and r["status"] == "ERROR":
                        continue
                    done.add(r["task_id"])
    return done


# ---------------------------------------------------------------------------
# 2. CHUẨN BỊ ARROW_HOME CHO TỪNG WORKER
# ---------------------------------------------------------------------------

def prepare_home(arrow, home):
    """Tạo một ARROW_HOME riêng và khai báo 4 JDK cho nó.

    Mỗi worker có ARROW_HOME riêng, để các lần chạy song song không ghi đè
    file của nhau (log, lịch sử, thư mục làm việc).
    """
    if os.path.exists(os.path.join(home, "config.json")):
        return  # đã chuẩn bị ở lần chạy trước
    os.makedirs(home, exist_ok=True)
    jdks = {v: os.path.abspath(os.path.join(JDK_DIR, f"jdk-{v}", "Contents", "Home"))
            for v in ("8", "11", "17", "21")}
    subprocess.run([arrow, "config", "set", "java.jdk_homes", json.dumps(jdks),
                    "--home", home, "--plain"],
                   check=True, stdout=subprocess.DEVNULL, stdin=subprocess.DEVNULL)


def clean_home(home):
    """Xóa repo đã clone sau khi xong một lớp, để không đầy đĩa.

    - mirrors/: bản clone gốc tool giữ lại để dùng lần sau. Mỗi repo chỉ được
      screen một lần nên giữ lại không có lợi gì.
    - work/: bản sao để build. Bình thường tool tự xóa; chỉ còn sót lại khi
      script phải dừng tool giữa chừng vì quá giờ.
    An toàn vì lúc này không lớp nào khác đang dùng ARROW_HOME này.
    runs/ (vài chục KB mỗi lớp, chứa log khối 1) được giữ lại để xem lại.
    """
    for sub in ("mirrors", "work"):
        path = os.path.join(home, sub)
        if os.path.isdir(path):
            shutil.rmtree(path, ignore_errors=True)
            os.makedirs(path, exist_ok=True)


# ---------------------------------------------------------------------------
# 3. CHẠY `arrow inspect` CHO MỘT LỚP
# ---------------------------------------------------------------------------

def run_inspect(arrow, cand, home, log_dir):
    """Chạy `arrow inspect` và trả về (mã thoát, JSON đọc được hoặc None, số giây).

    stdout (JSON) lưu vào logs/<task_id>.json, stderr (tiến độ) vào
    logs/<task_id>.err, để xem lại khi cần.
    """
    cmd = [arrow, "inspect", cand["class_fqn"],
           "--repo", cand["repo_url"], "--ref", cand["checkout_sha"],
           "--home", home, "--json"]
    start = time.time()
    # start_new_session: arrow và các tiến trình con (mvn, java) chung một nhóm,
    # để khi hết giờ có thể dừng cả nhóm, không để Maven chạy mồ côi.
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                            stdin=subprocess.DEVNULL, text=True, start_new_session=True)
    try:
        out, err = proc.communicate(timeout=TIMEOUT_PER_CLASS)
        code = proc.returncode
    except subprocess.TimeoutExpired:
        os.killpg(proc.pid, signal.SIGKILL)
        out, err = proc.communicate()
        code = None  # None = script tự dừng vì quá giờ
    seconds = round(time.time() - start, 1)

    with open(os.path.join(log_dir, cand["task_id"] + ".json"), "w", encoding="utf-8") as f:
        f.write(out)
    with open(os.path.join(log_dir, cand["task_id"] + ".err"), "w", encoding="utf-8") as f:
        f.write(err)

    try:
        data = json.loads(out)
    except json.JSONDecodeError:
        data = None
    return code, data, seconds


# ---------------------------------------------------------------------------
# 4. XÉT TEST CỦA NGƯỜI VIẾT
# ---------------------------------------------------------------------------

def test_fqn_from_path(test_class_path):
    """Đổi đường dẫn file test thành tên lớp đầy đủ.

    Ví dụ: "core/src/test/java/a/b/FooTest.java" -> "a.b.FooTest".
    Nếu không có đoạn "src/test/java/" thì trả về None.
    """
    marker = "src/test/java/"
    if marker not in test_class_path:
        return None
    rel = test_class_path.split(marker)[-1]
    return rel[:-len(".java")].replace("/", ".")


def tests_of_class(test_ids, test_fqn, simple_name):
    """Lọc ra các test thuộc lớp test của người viết.

    Định danh test của tool có dạng "a.b.FooTest#method". Lớp lồng bên trong
    (JUnit 5 @Nested) có dạng "a.b.FooTest$Inner#method", nên cũng tính.
    Nếu không biết tên đầy đủ, so theo tên ngắn (FooTest).
    """
    picked = []
    for tid in test_ids:
        cls = tid.split("#")[0].split("$")[0]
        if test_fqn is not None:
            if cls == test_fqn:
                picked.append(tid)
        elif cls.split(".")[-1] == simple_name:
            picked.append(tid)
    return picked


def check_paired_test(baseline, test_class_path):
    """Test của người viết có đạt cả 2 lần baseline không?

    Quy tắc: ĐẠT khi có ít nhất 1 phương thức test của lớp đó đạt ở cả 2 lần,
    và không có phương thức nào chỉ đạt 1 lần (flaky) hoặc fail cả 2 lần.
    Test bị skip thì tool đã bỏ qua, không tính.

    Trả về (đạt hay không, lý do nếu không đạt, số liệu để ghi lại).
    """
    test_fqn = test_fqn_from_path(test_class_path)
    simple = os.path.basename(test_class_path)[:-len(".java")]

    passing = tests_of_class(baseline["passing_in_all_runs"], test_fqn, simple)
    flaky = tests_of_class(baseline["flaky"], test_fqn, simple)
    failing = tests_of_class(baseline["failing_in_all_runs"], test_fqn, simple)
    info = {"paired_passing": len(passing), "paired_flaky": len(flaky),
            "paired_failing": len(failing), "paired_test_fqn": test_fqn or simple}

    if not passing and not flaky and not failing:
        all_tests = (baseline["passing_in_all_runs"] + baseline["flaky"]
                     + baseline["failing_in_all_runs"])
        if not all_tests:
            # Cả dự án không chạy được test nào (vd. Gradle chưa bật JUnit 5).
            return False, "BASELINE_RAN_NO_TESTS", info
        # Dự án có chạy test, nhưng không có test nào của lớp này.
        return False, "PAIRED_TEST_NOT_FOUND", info
    if failing:
        return False, "PAIRED_TEST_FAILING", info
    if flaky:
        return False, "PAIRED_TEST_FLAKY", info
    return True, None, info


# ---------------------------------------------------------------------------
# 5. QUYẾT ĐỊNH KẾT QUẢ CỦA MỘT LỚP
# ---------------------------------------------------------------------------

def screen_one(arrow, cand, home, log_dir):
    """Chạy một lớp và trả về một dòng kết quả (dict)."""
    code, data, seconds = run_inspect(arrow, cand, home, log_dir)
    result = dict(cand, exit_code=code, seconds=seconds,
                  finished_at=datetime.now(timezone.utc).isoformat(timespec="seconds"))

    def finish(status, reason, **extra):
        result.update(status=status, reason=reason, **extra)
        return result

    # (a) Script phải tự dừng vì quá giờ.
    if code is None:
        return finish("ERROR", "SCRIPT_TIMEOUT")

    # (b) Không đọc được JSON: tool bị lỗi bất thường. Xem logs/<task_id>.err.
    if data is None:
        return finish("ERROR", "NO_JSON")

    # (c) Tool báo lỗi trước khi phân tích xong (không clone được, thiếu JDK...).
    #     Khi đó JSON có dạng {"error": {"code": "...", ...}}.
    if "error" in data:
        err_code = data["error"].get("code") or "UNKNOWN_ERROR"
        if err_code in REPO_GONE_CODES:
            return finish("EXCLUDED", err_code)
        return finish("ERROR", err_code)

    # (d) Khối 1 chạy xong. Tool tự chia kết quả thành OK / BLOCKED / INFRA:
    #     BLOCKED = dự án có vấn đề -> loại.
    #     INFRA   = hạ tầng (vd. baseline quá giờ) -> chạy lại sau.
    outcome = data["outcome"]
    kind = outcome["outcome"]
    result["run_dir"] = data.get("run_dir")
    if kind == "BLOCKED":
        return finish("EXCLUDED", outcome.get("code") or "BLOCKED",
                      first_error=outcome.get("first_error"))
    if kind != "OK":
        return finish("ERROR", outcome.get("code") or kind)

    # (e) Khối 1 OK: ghi lại vài thông tin dự án, rồi xét test người viết.
    analysis = data["analysis"]
    ws = analysis.get("workspace") or {}
    result.update(build_tool=ws.get("build_tool"), jdk_used=ws.get("jdk_major"))
    ok, why, info = check_paired_test(analysis["baseline"], cand["test_class_path"])
    result.update(info)
    if ok:
        return finish("PASS", None)
    return finish("EXCLUDED", "B1_PAIRED_TEST_NOT_PASSING", detail=why)


# ---------------------------------------------------------------------------
# 6. CHẠY TOÀN BỘ
# ---------------------------------------------------------------------------

def main():
    ap = argparse.ArgumentParser(description="Screen các lớp ứng viên bằng arrow inspect.")
    ap.add_argument("inputs", nargs="+", help="file .jsonl danh sách lớp ứng viên")
    ap.add_argument("--out", required=True, help="thư mục ghi kết quả")
    ap.add_argument("--workers", type=int, default=6, help="số lớp chạy cùng lúc (mặc định 6)")
    ap.add_argument("--retry-errors", action="store_true", help="chạy lại các lớp ERROR")
    ap.add_argument("--limit", type=int, help="chỉ chạy N lớp đầu (để thử)")
    ap.add_argument("--arrow", default="arrow", help="đường dẫn lệnh arrow")
    args = ap.parse_args()

    log_dir = os.path.join(args.out, "logs")
    os.makedirs(log_dir, exist_ok=True)
    results_path = os.path.join(args.out, "screen_results.jsonl")

    # Ghi phiên bản tool vào mọi dòng kết quả: mọi máy phải dùng cùng một bản.
    version = subprocess.run([args.arrow, "--version"], capture_output=True,
                             text=True, check=True).stdout.strip()

    # Lấy danh sách lớp, bỏ các lớp đã xong.
    candidates = read_candidates(args.inputs)
    done = read_done(results_path, args.retry_errors)
    todo = [c for c in candidates if c["task_id"] not in done]
    if args.limit:
        todo = todo[:args.limit]
    print(f"{version}")
    print(f"Tổng {len(candidates)} lớp · đã xong {len(done)} · lần này chạy {len(todo)} · {args.workers} worker")

    # Mỗi worker một ARROW_HOME. Hàng đợi `homes` đảm bảo hai lớp chạy cùng
    # lúc không bao giờ dùng chung một ARROW_HOME.
    homes = queue.Queue()
    for i in range(1, args.workers + 1):
        home = os.path.abspath(os.path.join(args.out, "homes", f"w{i}"))
        prepare_home(args.arrow, home)
        homes.put(home)

    write_lock = threading.Lock()
    counter = {"n": 0}

    def job(cand):
        home = homes.get()  # mượn một ARROW_HOME
        try:
            r = screen_one(args.arrow, cand, home, log_dir)
        except Exception as e:  # lỗi của chính script: ghi lại, không dừng cả lô
            r = dict(cand, status="ERROR", reason="SCRIPT_EXCEPTION", detail=repr(e))
        finally:
            clean_home(home)  # xóa repo vừa clone trước khi cho lớp khác dùng
            homes.put(home)   # trả lại
        r["arrow_version"] = version
        # Ghi ngay từng dòng, để bị ngắt thì không mất kết quả đã có.
        with write_lock:
            with open(results_path, "a", encoding="utf-8") as f:
                f.write(json.dumps(r, ensure_ascii=False) + "\n")
            counter["n"] += 1
            print(f"[{counter['n']}/{len(todo)}] {r['status']:8s} {r.get('reason') or '':28s} "
                  f"{r.get('seconds', 0):>6}s  {cand['dataset']}  {cand['class_fqn']}", flush=True)

    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        list(pool.map(job, todo))

    # Tóm tắt (tính trên dòng mới nhất của mỗi lớp, vì lớp chạy lại có nhiều dòng).
    latest = {}
    with open(results_path, encoding="utf-8") as f:
        for line in f:
            if line.strip():
                r = json.loads(line)
                latest[r["task_id"]] = r
    summary = {}
    for r in latest.values():
        summary[r["status"]] = summary.get(r["status"], 0) + 1
    print("Kết quả:", ", ".join(f"{k} {v}" for k, v in sorted(summary.items())))


if __name__ == "__main__":
    main()
