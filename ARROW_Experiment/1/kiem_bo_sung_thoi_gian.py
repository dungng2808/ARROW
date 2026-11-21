"""Kiểm sau khi gom lại TN1 để bổ sung cột thời gian (xem huong_dan_bo_sung_thoi_gian_TN1.md).

So results/<máy>/tn1_candidates.csv hiện tại với bản đã commit (HEAD):
    - số dòng không đổi
    - KHÔNG ô cũ nào bị đổi (chỉ được điền thêm ô đang trống)
    - đếm số dòng đã có thời gian từng bước build (step_*_s)
Chạy:  python kiem_bo_sung_thoi_gian.py <máy>      (ví dụ: python kiem_bo_sung_thoi_gian.py quang-qwen)
In "ĐẠT" thì được nộp; in "KHÔNG ĐẠT" thì dừng, báo người dùng.
"""
import csv
import io
import os
import subprocess
import sys

try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

HERE = os.path.dirname(os.path.abspath(__file__))
STEPS = ["step_compile_s", "step_run_target_s", "step_module_tests_s"]


def main():
    if len(sys.argv) != 2:
        raise SystemExit("Cách chạy: python kiem_bo_sung_thoi_gian.py <máy>")
    m = sys.argv[1]
    rel = f"results/{m}/tn1_candidates.csv"
    old_text = subprocess.run(["git", "show", f"HEAD:ARROW_Experiment/1/{rel}"], cwd=HERE, capture_output=True,
                              text=True, encoding="utf-8", errors="replace", check=True).stdout
    key = lambda r: (r["task_id"], r["model"], r["strategy"])
    old = {key(r): r for r in csv.DictReader(io.StringIO(old_text))}
    with open(os.path.join(HERE, rel), encoding="utf-8") as f:
        new = {key(r): r for r in csv.DictReader(f)}
    changed = [(k, c, o[c], new[k].get(c)) for k, o in old.items() if k in new
               for c in o if o[c] != "" and o[c] != new[k].get(c)]
    ok = True
    print(f"Máy {m}: {len(old)} dòng trước, {len(new)} dòng sau")
    if set(old) != set(new):
        ok = False
        print("[X] Danh sách bộ test bị đổi")
    if changed:
        ok = False
        print(f"[X] {len(changed)} ô cũ bị đổi, ví dụ: {changed[:3]}")
    filled = {c: sum(1 for r in new.values() if r.get(c, "") != "") for c in STEPS}
    print("Số dòng có thời gian từng bước:", filled)
    if filled["step_compile_s"] == 0:
        ok = False
        print("[X] Chưa lấy được thời gian từng bước: thư mục homes/ (ví dụ C:\\arrow-homes\\<máy>) không còn?")
    print("ĐẠT" if ok else "KHÔNG ĐẠT")
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
