#!/usr/bin/env python3
# ^ Cho phép chạy bằng ./tn1_collect.py trên Mac/Linux (hệ điều hành tìm "python3" để chạy file).
"""Gom kết quả Thực nghiệm 1 thành bảng phẳng tn1_candidates.csv (mỗi bộ test một dòng).

Đọc:
    results/<máy>/tn1_runs.jsonl        do tn1_run.py ghi (dòng mới nhất của mỗi bộ test là kết quả dùng)
    results/<máy>/homes/w*/runs/...     thư mục kết quả của arrow (metadata, token, kết quả kiểm test)

Ghi (cùng thư mục results/<máy>/):
    tn1_candidates.csv   mỗi bộ test một dòng; đủ cột để tn1_analyze.py tính mọi bảng
    tn2_pool.jsonl       bộ test có code, không lỗi hạ tầng, chưa VALID → nguồn để rút 180 bộ cho TN2
    c0/                  mã nguồn (c0) của các bộ test trong tn2_pool.jsonl

Cách chạy:
    python3 tn1_collect.py --machine dung          # gom một máy
    python3 tn1_collect.py --all                   # gom mọi máy trong results/ (bỏ thư mục *-smoke)

Giải nghĩa từ khóa Python: xem đầu file tn1_run.py.
"""

# ---------------------------------------------------------------------------
# NẠP THƯ VIỆN (đều có sẵn trong Python)
# ---------------------------------------------------------------------------
import argparse      # đọc tùy chọn dòng lệnh (--machine, --all)
import csv           # ghi file CSV
import glob          # tìm file/thư mục theo mẫu, ví dụ results/*/
import json          # đọc file JSON
import os
import sys            # ghép đường dẫn, kiểm file có tồn tại không
import shutil        # chép file mã c0 sang thư mục c0/

try:
    sys.stdout.reconfigure(encoding="utf-8")   # Windows: in được chữ tiếng Việt ra cửa sổ lệnh
except (AttributeError, ValueError):
    pass

HERE = os.path.dirname(os.path.abspath(__file__))   # thư mục chứa file này
RESULTS = os.path.join(HERE, "results")             # nơi tn1_run.py ghi kết quả

# Thứ tự trạng thái: bộ test dừng ở bước nào. "Qua bước k" = trạng thái nằm SAU các trạng thái lỗi của bước k.
PASSED_STEP = {
    1: {"DISCOVERY", "RUNTIME", "ASSERTION", "REGRESSION", "VALID"},   # qua biên dịch (UNKNOWN KHÔNG tính là qua)
    2: {"RUNTIME", "ASSERTION", "REGRESSION", "VALID"},                # được tìm thấy
    3: {"ASSERTION", "REGRESSION", "VALID"},                           # chạy được
    4: {"REGRESSION", "VALID"},                                        # test đạt
    5: {"VALID"},                                                      # không hỏng test cũ
}
# ^ Từ điển: số bước → tập (set) các trạng thái được coi là "đã qua bước đó".

# Kết quả không tính vào mẫu số (lỗi hạ tầng, lớp bị chặn ở khối 1, bị hủy).
NOT_COUNTED = {"INFRA", "BLOCKED", "CANCELLED", "SCRIPT_TIMEOUT", "NO_JSON", "SCRIPT_EXCEPTION"}

# Thứ tự cột của tn1_candidates.csv (giữ cố định để mọi máy ra file giống nhau).
COLUMNS = [
    # định danh
    "dataset", "task_id", "repo_url", "checkout_sha", "class_fqn", "model", "strategy",
    "machine", "worker", "tool_commit", "run_id", "status", "result", "exit_code", "seconds",
    # dự án (từ khối 1)
    "build_tool", "java_declared_major", "jdk_used", "test_framework", "vintage",
    # sinh test (từ khối 2)
    "valid_output", "invalid_reason", "model_id", "prompt_tokens", "completion_tokens",
    "usage_estimated", "finish_reason", "llm_seconds", "prompt_chars", "test_method_count", "assertion_count",
    # kiểm test (từ khối 3)
    "verify_state", "tests_total", "tests_passed", "module_flaky", "regressions", "build_seconds",
    # tính sẵn cho phân tích
    "counted", "pass_s1", "pass_s2", "pass_s3", "pass_s4", "pass_s5", "csr", "esr", "valid",
    # Thời gian từng công đoạn (giây), thêm 21/11/2026:
    #   block1_s..total_s: từ JSON gốc của arrow trong logs/ (đã commit, có cho mọi máy)
    #   step_*_s: các bước kiểm c0, từ verify_c0.json trong homes/ (chỉ có trên máy đã chạy)
    "block1_s", "block2_s", "block3_s", "total_s",
    "step_compile_s", "step_run_target_s", "step_module_tests_s",
]
TIMING_STEPS = ("compile", "run_target", "module_tests")


# ---------------------------------------------------------------------------
# 1. ĐỌC FILE
# ---------------------------------------------------------------------------

def read_json(path):
    """Đọc file JSON; file không có hoặc hỏng thì trả về None (không làm dừng cả lô)."""
    try:
        with open(path, encoding="utf-8") as f:     # mở file đọc chữ, mã hóa utf-8
            return json.load(f)                     # đổi nội dung JSON thành từ điển Python
    except (OSError, json.JSONDecodeError):         # OSError: không mở được; JSONDecodeError: JSON hỏng
        return None


def read_latest(runs_path):
    """tn1_runs.jsonl → {(task_id, model, strategy): dòng mới nhất}."""
    latest = {}
    with open(runs_path, encoding="utf-8") as f:
        for line in f:
            if line.strip():                        # bỏ dòng trống
                r = json.loads(line)                # một dòng = một bản ghi JSON
                latest[(r["task_id"], r["model"], r["strategy"])] = r   # dòng sau ghi đè dòng trước
    return latest


def target_dir(run_dir, class_fqn):
    """Thư mục kết quả của lớp trong một run của arrow: <run_dir>/targets/<tên lớp>."""
    return os.path.join(run_dir, "targets", class_fqn)


# ---------------------------------------------------------------------------
# 2. GOM MỘT BỘ TEST THÀNH MỘT DÒNG
# ---------------------------------------------------------------------------

def collect_row(rec, mdir=None):
    """Từ một dòng tn1_runs.jsonl → một dòng đầy đủ cho tn1_candidates.csv."""
    row = {c: "" for c in COLUMNS}                  # bắt đầu bằng mọi cột rỗng
    if mdir:                                        # thời gian từng khối: JSON gốc arrow in ra (logs/)
        log = read_json(os.path.join(mdir, "logs", f"{rec['task_id']}__{rec['model']}__{rec['strategy']}.json")) or {}
        secs = ((log.get("targets") or [{}])[0]).get("seconds") or {}
        for k in ("block1", "block2", "block3", "total"):
            row[k + "_s"] = secs.get(k) if secs.get(k) is not None else ""
    for c in COLUMNS:                               # chép các trường có sẵn trong bản ghi chạy
        if c in rec and rec[c] is not None:
            row[c] = rec[c]

    tdir = target_dir(rec["run_dir"], rec["class_fqn"]) if rec.get("run_dir") else None
    # ^ "A if điều_kiện else B": có run_dir thì tính đường dẫn, không thì None
    if tdir and os.path.isdir(tdir):                # có thư mục kết quả của arrow thì đọc thêm
        meta = read_json(os.path.join(tdir, "block1", "metadata.json")) or {}   # "or {}": None thì dùng {}
        ws = read_json(os.path.join(tdir, "block1", "workspace.json")) or {}
        row["build_tool"] = (meta.get("build_tool") or {}).get("kind", "")       # maven / gradle
        row["java_declared_major"] = (meta.get("java") or {}).get("major", "")   # Java dự án khai báo
        row["jdk_used"] = ws.get("jdk_major", "")                                 # JDK thật sự dùng để build
        tf = meta.get("test_framework") or {}
        row["test_framework"] = tf.get("name", "")                               # junit4 / junit5 / testng
        row["vintage"] = tf.get("vintage", "")                                   # JUnit 5 có chạy được test JUnit 4 không

        usage = read_json(os.path.join(tdir, "block2", "usage.json")) or {}
        for src, dst in (("model_id", "model_id"), ("prompt_tokens", "prompt_tokens"),
                         ("completion_tokens", "completion_tokens"), ("usage_estimated", "usage_estimated"),
                         ("finish_reason", "finish_reason"), ("latency_seconds", "llm_seconds")):
            row[dst] = usage.get(src, "")           # chép từng trường token/thời gian gọi mô hình

        prompt = read_json(os.path.join(tdir, "block2", "prompt.json")) or {}
        row["prompt_chars"] = sum(len(m.get("content", "")) for m in prompt.get("messages", []))
        # ^ tổng số ký tự của prompt gửi đi (để sau này đếm lại token, kiểm Qwen có bị cắt vì vượt num_ctx)

        cand = read_json(os.path.join(tdir, "block2", "candidate.json")) or {}
        row["valid_output"] = cand.get("valid_output", "")      # mô hình có trả về đúng một lớp Java không
        row["invalid_reason"] = cand.get("invalid_reason") or ""
        row["test_method_count"] = cand.get("test_method_count", "")
        row["assertion_count"] = cand.get("assertion_count", "")

        ver = read_json(os.path.join(tdir, "block3", "verify_c0.json")) or {}
        tm = ver.get("test_methods") or {}
        row["verify_state"] = ver.get("state", "")
        row["tests_total"] = tm.get("total", "")                # số phương thức test đã chạy
        row["tests_passed"] = tm.get("passed", "")
        row["module_flaky"] = len(ver.get("module_flaky") or [])  # số test cũ bị nghi chập chờn
        row["regressions"] = len(ver.get("regressions") or [])
        row["build_seconds"] = ver.get("build_seconds", "")
        for st in ver.get("steps") or []:           # thời gian từng bước kiểm c0
            if st.get("step") in TIMING_STEPS:
                k = f"step_{st['step']}_s"
                row[k] = round((row[k] or 0) + (st.get("seconds") or 0), 3)

    # --- Cột tính sẵn ---
    result = rec.get("result", "")
    counted = rec.get("status") == "DONE" and result not in NOT_COUNTED
    # ^ chỉ tính vào mẫu số khi chạy xong và không phải lỗi hạ tầng / bị chặn
    row["counted"] = int(counted)                   # int(True) = 1, int(False) = 0
    if counted:
        for k, states in PASSED_STEP.items():       # .items() lấy từng cặp (khóa, giá trị)
            row[f"pass_s{k}"] = int(result in states)
        row["csr"] = row["pass_s1"]                 # CSR = qua bước biên dịch
        total = row["tests_total"]
        row["esr"] = int(isinstance(total, int) and total >= 1)
        # ^ ESR = có ít nhất một phương thức test đã chạy; isinstance kiểm total là số nguyên
        row["valid"] = int(result == "VALID")
    return row


# ---------------------------------------------------------------------------
# 3. GOM MỘT MÁY
# ---------------------------------------------------------------------------

def collect_machine(mdir):
    """Gom một thư mục results/<máy>/ → ghi tn1_candidates.csv và tn2_pool.jsonl."""
    runs_path = os.path.join(mdir, "tn1_runs.jsonl")
    if not os.path.exists(runs_path):
        print(f"[!] Bỏ qua {mdir}: không có tn1_runs.jsonl")
        return
    latest = read_latest(runs_path)
    rows = [collect_row(r, mdir) for r in latest.values()]  # mỗi bộ test một dòng
    # Máy khác đã gom trên máy của họ (homes/ không đưa lên Git): ô nào lần này rỗng vì thiếu homes/
    # thì giữ giá trị trong tn1_candidates.csv cũ, để gom lại không làm mất số liệu.
    old_csv = os.path.join(mdir, "tn1_candidates.csv")
    if os.path.exists(old_csv):
        with open(old_csv, encoding="utf-8") as f:
            old = {(r["task_id"], r["model"], r["strategy"]): r for r in csv.DictReader(f)}
        for row in rows:
            prev = old.get((row["task_id"], row["model"], row["strategy"]), {})
            for c in COLUMNS:
                if row[c] == "" and prev.get(c, "") != "":
                    row[c] = prev[c]
            if row["counted"] == 1:                 # ESR tính lại sau khi đã lấy lại tests_total
                try:
                    row["esr"] = int(int(row["tests_total"]) >= 1)
                except (TypeError, ValueError):
                    row["esr"] = 0
    rows.sort(key=lambda r: (r["dataset"], r["task_id"], r["model"], r["strategy"]))
    # ^ sắp xếp cố định để file giống nhau giữa các lần gom

    out_csv = os.path.join(mdir, "tn1_candidates.csv")
    with open(out_csv, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=COLUMNS, lineterminator="\n")
        # ^ DictWriter ghi mỗi từ điển thành một dòng theo thứ tự cột COLUMNS; lineterminator "\n" (LF)
        w.writeheader()                             # dòng tiêu đề (tên cột)
        w.writerows(rows)                           # các dòng dữ liệu

    # Nguồn cho TN2: có code, đã chạy xong, chưa VALID (R03 §6.1).
    # Mã c0 được chép sang results/<máy>/c0/ (nhỏ, commit được), vì homes/ không đưa lên Git.
    pool_path = os.path.join(mdir, "tn2_pool.jsonl")
    c0_dir = os.path.join(mdir, "c0")
    os.makedirs(c0_dir, exist_ok=True)
    n_pool = 0
    with open(pool_path, "w", encoding="utf-8") as f:
        for r in rows:
            if r["counted"] == 1 and r["valid_output"] in (True, "True") and r["valid"] == 0:
                rec = latest[(r["task_id"], r["model"], r["strategy"])]
                src = os.path.join(target_dir(rec["run_dir"], r["class_fqn"]), "block2", "candidate.java")
                dst = os.path.join(c0_dir, f"{r['task_id']}__{r['model']}__{r['strategy']}.java")
                if os.path.exists(src):
                    shutil.copyfile(src, dst)       # chép mã c0 (bộ test lỗi) ra chỗ commit được
                f.write(json.dumps({k: r[k] for k in ("dataset", "task_id", "class_fqn", "model", "strategy",
                                                      "result", "machine", "run_id")}
                                   | {"c0_path": os.path.relpath(dst, HERE).replace(os.sep, "/")}, ensure_ascii=False) + "\n")
                # ^ "a | b" gộp hai từ điển (Python 3.9+); relpath ghi đường dẫn tương đối so với thư mục script
                n_pool += 1

    counted = sum(r["counted"] for r in rows)
    print(f"{os.path.basename(mdir)}: {len(rows)} bộ test ({counted} tính vào mẫu số, "
          f"{len(rows) - counted} lỗi hạ tầng/bị chặn) → tn1_candidates.csv; {n_pool} bộ vào tn2_pool.jsonl")


def main():
    ap = argparse.ArgumentParser(description="Gom kết quả TN1 thành tn1_candidates.csv.")
    ap.add_argument("--machine", help="tên máy (thư mục results/<machine>)")
    ap.add_argument("--all", action="store_true", help="gom mọi máy trong results/ (trừ thư mục *-smoke)")
    ap.add_argument("--dir", help="gom một thư mục kết quả bất kỳ (pilot dùng: ../pilot/results/<máy>/gen)")
    args = ap.parse_args()
    if args.all:
        dirs = [d for d in sorted(glob.glob(os.path.join(RESULTS, "*")))
                if os.path.isdir(d) and not d.endswith("-smoke") and not os.path.basename(d).startswith("_")]
    elif args.dir:
        dirs = [os.path.abspath(args.dir)]
    elif args.machine:
        dirs = [os.path.join(RESULTS, args.machine)]
    else:
        raise SystemExit("Cần --machine <tên> hoặc --all.")
    for d in dirs:
        collect_machine(d)


if __name__ == "__main__":
    main()
