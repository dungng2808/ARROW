#!/usr/bin/env python3
"""Ra danh sách lớp CUỐI CÙNG từ kết quả screen.

Đầu vào:
    candidates/*.jsonl            danh sách ứng viên (select_datasets.py)
    screen/screen_results.jsonl   kết quả screen (screen.py); mỗi lớp lấy dòng mới nhất

Quy tắc (chốt 18/11/2026):
  1. Lớp PASS thì giữ. Lớp EXCLUDED thì loại.
  2. Lớp còn ERROR sau các lượt chạy lại cũng loại. Lúc chốt chỉ còn 2 kiểu:
     baseline quá giờ ở cả 3 lần chạy (test của dự án treo hoặc quá 15 phút),
     và dự án cần plugin chỉ có trên JCenter / Gradle Plugin Portal, nơi đang
     chặn tải (429) — giữ lại thì TN1–TN3 cũng build lỗi y như vậy.
  3. Classes2Test: lớp bị loại được thay bằng lớp PASS đầu tiên CHƯA DÙNG trong
     tập dự phòng, theo đúng thứ tự file. Tập đánh giá thay bằng
     classes2test_eval_reserve, pilot thay bằng classes2test_pilot_reserve.
  4. CAT-LM và GitBug-Java không có repo dự phòng và không thay bằng lớp khác
     trong cùng repo (baseline chạy cả module, nên lớp cùng module hỏng y như
     nhau; các repo còn lại chỉ có 1 lớp). Số lớp cuối = số lớp PASS.

Cách chạy:
    python3 finalize.py

Đầu ra trong final/:
    classes2test_eval.jsonl, classes2test_pilot.jsonl, catlm_eval.jsonl,
    gitbug_java_eval.jsonl   mỗi dòng là bản ghi preflight gốc (manifest dùng cho
                             thực nghiệm), theo thứ tự: lớp gốc còn PASS trước,
                             rồi tới các lớp dự phòng được thêm vào
    <tên>.csv                bảng tóm tắt: lấy từ đâu, thay cho lớp nào
    excluded.csv             mọi lớp bị loại (cả trong tập dự phòng đã xét) và lý do
    SHA256SUMS.txt           checksum mọi file trên, kết quả screen và các script
"""
import csv
import hashlib
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
CAND = os.path.join(HERE, "candidates")
RESULTS = os.path.join(HERE, "screen", "screen_results.jsonl")
OUT = os.path.join(HERE, "final")

# Nhóm cần đủ số lớp, và tập dự phòng dùng để thay.
WITH_RESERVE = [
    ("classes2test_eval", 200, "classes2test_eval_reserve"),
    ("classes2test_pilot", 50, "classes2test_pilot_reserve"),
]
# Nhóm không có dự phòng: giữ các lớp PASS.
NO_RESERVE = ["catlm_eval", "gitbug_java_eval"]


# ---------------------------------------------------------------------------
# 1. ĐỌC DỮ LIỆU
# ---------------------------------------------------------------------------

def read_group(name):
    """Danh sách (dòng gốc, bản ghi) của một nhóm ứng viên, đúng thứ tự file."""
    rows = []
    with open(os.path.join(CAND, name + ".jsonl"), encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if line.strip():
                rows.append((line, json.loads(line)))
    return rows


def read_screen():
    """Kết quả screen của mỗi lớp. Một lớp chạy lại có nhiều dòng; dòng cuối thắng."""
    latest = {}
    with open(RESULTS, encoding="utf-8") as f:
        for line in f:
            if line.strip():
                r = json.loads(line)
                latest[r["task_id"]] = r
    return latest


def why_excluded(r):
    """Lý do loại, ngắn gọn, để ghi vào excluded.csv."""
    if r["status"] == "ERROR":
        return f"ERROR_AFTER_RETRIES:{r['reason']}"
    return r.get("detail") or r["reason"]


# ---------------------------------------------------------------------------
# 2. GHI KẾT QUẢ
# ---------------------------------------------------------------------------

written = []


def write_final(name, picked):
    """picked: danh sách (dòng gốc, bản ghi, nguồn, thay cho task_id hoặc None)."""
    jsonl = os.path.join(OUT, name + ".jsonl")
    with open(jsonl, "w", encoding="utf-8", newline="\n") as f:
        for line, *_ in picked:
            f.write(line + "\n")
    csv_path = os.path.join(OUT, name + ".csv")
    with open(csv_path, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["order", "task_id", "repository", "class_fqn", "test_class_path",
                    "testing_framework", "source", "replaces_task_id"])
        for i, (line, rec, source, replaces) in enumerate(picked, 1):
            w.writerow([i, rec["task_id"], rec["repo_url"].replace("https://github.com/", ""),
                        rec["class_fqn"], rec["test_class_path"], rec.get("testing_framework"),
                        source, replaces or ""])
    written.extend([jsonl, csv_path])


# ---------------------------------------------------------------------------
# 3. CHẠY
# ---------------------------------------------------------------------------

def main():
    os.makedirs(OUT, exist_ok=True)
    screen = read_screen()
    excluded = []   # (nhóm, bản ghi, lý do)
    summary = []

    def status(rec):
        r = screen.get(rec["task_id"])
        assert r is not None, f"{rec['task_id']} chưa có kết quả screen"
        return r

    # --- Nhóm có dự phòng (Classes2Test eval, pilot) ---
    for name, size, reserve_name in WITH_RESERVE:
        picked, removed = [], []
        for line, rec in read_group(name):
            r = status(rec)
            if r["status"] == "PASS":
                picked.append((line, rec, name, None))
            else:
                removed.append(rec)
                excluded.append((name, rec, why_excluded(r)))

        # Lấy lớp PASS trong tập dự phòng theo đúng thứ tự file, cho tới khi đủ.
        # Lớp dự phòng nào đã xét mà không PASS cũng ghi vào excluded.csv.
        need = size - len(picked)
        used = 0
        for line, rec in read_group(reserve_name):
            if used == need:
                break
            r = status(rec)
            if r["status"] == "PASS":
                picked.append((line, rec, reserve_name, removed[used]["task_id"]))
                used += 1
            else:
                excluded.append((reserve_name, rec, why_excluded(r)))
        assert len(picked) == size, f"{name}: chỉ có {len(picked)} lớp, cần {size} (thiếu dự phòng)"
        write_final(name, picked)
        summary.append(f"{name}: {size} lớp ({size - used} lớp gốc + {used} lớp dự phòng; loại {len(removed)})")

    # --- Nhóm không có dự phòng (CAT-LM, GitBug-Java) ---
    for name in NO_RESERVE:
        picked = []
        for line, rec in read_group(name):
            r = status(rec)
            if r["status"] == "PASS":
                picked.append((line, rec, name, None))
            else:
                excluded.append((name, rec, why_excluded(r)))
        write_final(name, picked)
        total = len(read_group(name))
        summary.append(f"{name}: {len(picked)} lớp (loại {total - len(picked)} / {total})")

    # --- Kiểm: các tập cuối không dùng chung repo nào ---
    seen = {}
    for name in [n for n, *_ in WITH_RESERVE] + NO_RESERVE:
        with open(os.path.join(OUT, name + ".jsonl"), encoding="utf-8") as f:
            for line in f:
                repo = json.loads(line)["repo_url"].lower().rstrip("/")
                assert repo not in seen, f"repo {repo} có ở cả {seen[repo]} và {name}"
                seen[repo] = name

    # --- Danh sách lớp bị loại ---
    ex_path = os.path.join(OUT, "excluded.csv")
    with open(ex_path, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["group", "task_id", "repository", "class_fqn", "reason"])
        for group, rec, reason in excluded:
            w.writerow([group, rec["task_id"], rec["repo_url"].replace("https://github.com/", ""),
                        rec["class_fqn"], reason])
    written.append(ex_path)

    # --- Checksum: file kết quả, kết quả screen và các script tạo ra chúng ---
    sources = [RESULTS] + [os.path.join(HERE, s) for s in
                           ("select_datasets.py", "screen.py", "finalize.py")]
    with open(os.path.join(OUT, "SHA256SUMS.txt"), "w", encoding="utf-8", newline="\n") as f:
        for path in written + sources:
            with open(path, "rb") as g:
                digest = hashlib.sha256(g.read()).hexdigest()
            f.write(f"{digest}  {os.path.relpath(path, OUT)}\n")

    print("\n".join(summary))
    print(f"excluded.csv: {len(excluded)} lớp")


if __name__ == "__main__":
    main()
