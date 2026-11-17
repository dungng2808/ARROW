#!/usr/bin/env python3
"""Chọn danh sách lớp ứng viên cho cả ba dataset: Classes2Test, CAT-LM, GitBug-Java.

Đầu vào là kết quả preflight (các lớp ELIGIBLE). Đầu ra là các file .jsonl,
mỗi dòng một lớp, giữ nguyên bản ghi preflight để `arrow` và screen.py đọc được.

Quy tắc chọn (giống nhau cho cả ba dataset):
  1. Mỗi repo chỉ giữ 1 lớp.
  2. Trong một repo, ưu tiên lớp có test ghép theo tên; chỉ dùng lớp ghép tay
     khi repo không có lớp nào ghép theo tên. (Chỉ GitBug-Java có lớp ghép tay.)
  3. Thứ tự chọn = sha256("<SALT>|<dataset>|<task_id>"). Không dùng seed:
     cùng SALT thì chạy lại bao nhiêu lần cũng ra đúng kết quả cũ.

Riêng từng dataset:
  Classes2Test  chỉ lấy lớp CONTENT_MATCHED, bỏ repo của 2 ví dụ Few-shot;
                rồi cắt theo thứ tự thành: eval 200 → eval_reserve 100
                → pilot 50 → pilot_reserve 25 (không nhóm nào trùng repo).
  CAT-LM        lấy hết (mỗi repo 1 lớp).
  GitBug-Java   lấy hết (mỗi repo 1 lớp).

Cách chạy:
    python3 select_datasets.py <thư mục results/team-r1> <thư mục đầu ra>

    Ví dụ:
    python3 select_datasets.py ../Prelight_Net/results/team-r1 candidates

Đầu ra:
    classes2test_eval.jsonl, classes2test_eval_reserve.jsonl,
    classes2test_pilot.jsonl, classes2test_pilot_reserve.jsonl,
    catlm_eval.jsonl, gitbug_java_eval.jsonl
    + một file .csv tóm tắt cho mỗi file .jsonl
    + SHA256SUMS.txt (checksum mọi file trên và chính script này)
"""
import csv
import glob
import hashlib
import json
import os
import sys
from collections import defaultdict

# ---------------------------------------------------------------------------
# CẤU HÌNH — chốt và commit TRƯỚC khi chạy
# ---------------------------------------------------------------------------

# Chuỗi cố định quyết định thứ tự chọn. Đổi chuỗi là ra tập lớp khác.
SALT = "arrow-dataset-version-1"

# Repo của 2 ví dụ Few-shot (nằm sẵn trong prompt), không được dùng làm lớp đánh giá.
FEW_SHOT_REPOS = {
    "https://github.com/shekhargulati/conduit-on-kubernetes",
    "https://github.com/jtablesaw/tablesaw",
}

# Classes2Test được cắt thành các nhóm này, theo đúng thứ tự.
C2T_GROUPS = [
    ("classes2test_eval", 200),          # tập đánh giá
    ("classes2test_eval_reserve", 100),  # dự phòng, thay lớp bị loại ở bước screen
    ("classes2test_pilot", 50),          # pilot chọn N
    ("classes2test_pilot_reserve", 25),  # dự phòng của pilot
]


# ---------------------------------------------------------------------------
# 1. ĐỌC KẾT QUẢ PREFLIGHT
# ---------------------------------------------------------------------------

def load(results_dir, dataset):
    """Đọc mọi lớp ELIGIBLE của một dataset, từ kết quả của tất cả các máy.

    Preflight ghi kết quả ở <results_dir>/<máy>/<dataset>/
    manifests-technical_eligible_manifest.part-*.jsonl.
    Mỗi phần tử trả về là (dòng gốc, bản ghi đã đọc, file nguồn).
    Giữ dòng gốc để ghi ra nguyên văn, không làm đổi bản ghi preflight.
    """
    items = []
    pattern = os.path.join(results_dir, "*", dataset,
                           "manifests-technical_eligible_manifest.part-*.jsonl")
    for path in sorted(glob.glob(pattern)):
        with open(path, encoding="utf-8-sig") as f:
            for line in f:
                line = line.rstrip("\n")
                if line.strip():
                    rec = json.loads(line)
                    # File này chỉ được phép chứa lớp ELIGIBLE; sai thì dừng ngay.
                    assert rec["preflight_status"] == "ELIGIBLE", rec["task_id"]
                    items.append((line, rec, os.path.relpath(path, results_dir)))
    return items


# ---------------------------------------------------------------------------
# 2. QUY TẮC CHỌN
# ---------------------------------------------------------------------------

def norm(url):
    """Chuẩn hóa URL repo để so sánh: chữ thường, bỏ "/" và ".git" ở cuối."""
    url = url.lower().rstrip("/")
    return url[:-4] if url.endswith(".git") else url


def order_key(dataset, rec):
    """Khóa sắp xếp của một lớp: sha256("<SALT>|<dataset>|<task_id>").

    Khóa chỉ phụ thuộc SALT và mã của lớp, nên thứ tự coi như ngẫu nhiên
    (không liên quan độ khó, kích thước…) nhưng tái lập được trên mọi máy.
    """
    return hashlib.sha256(f"{SALT}|{dataset}|{rec['task_id']}".encode()).hexdigest()


def one_per_repo(dataset, items):
    """Mỗi repo giữ đúng 1 lớp, rồi xếp các lớp được giữ theo khóa sha256.

    Trong một repo:
      - nếu có lớp ghép test theo tên (pairing_method khác "manual") thì chỉ
        chọn trong nhóm đó;
      - nếu không, mới chọn trong các lớp ghép tay;
      - trong nhóm được chọn, lấy lớp có khóa nhỏ nhất.

    Trả về danh sách (dòng gốc, bản ghi, file nguồn, số lớp ELIGIBLE của repo).
    """
    by_repo = defaultdict(list)
    for it in items:
        by_repo[norm(it[1]["repo_url"])].append(it)

    chosen = []
    for repo_items in by_repo.values():
        by_name = [it for it in repo_items if it[1].get("pairing_method") != "manual"]
        pool = by_name or repo_items
        line, rec, src = min(pool, key=lambda it: order_key(dataset, it[1]))
        chosen.append((line, rec, src, len(repo_items)))

    chosen.sort(key=lambda c: order_key(dataset, c[1]))
    return chosen


# ---------------------------------------------------------------------------
# 3. GHI KẾT QUẢ
# ---------------------------------------------------------------------------

def write_group(out_dir, name, dataset, rows, written):
    """Ghi một nhóm lớp ra 2 file:
      <name>.jsonl  mỗi dòng là bản ghi preflight gốc (dùng làm manifest);
      <name>.csv    bảng tóm tắt để người đọc xem nhanh.
    Đường dẫn các file đã ghi được thêm vào `written` để tính checksum.
    """
    jsonl_path = os.path.join(out_dir, name + ".jsonl")
    with open(jsonl_path, "w", encoding="utf-8", newline="\n") as f:
        for line, *_ in rows:
            f.write(line + "\n")

    csv_path = os.path.join(out_dir, name + ".csv")
    with open(csv_path, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["order", "repository", "task_id", "class_path", "test_class_path",
                    "pairing_method", "testing_framework", "revision_verification_status",
                    "eligible_classes_in_repo", "order_key", "source_file"])
        for i, (line, rec, src, n_in_repo) in enumerate(rows, 1):
            w.writerow([i, rec["repo_url"].replace("https://github.com/", ""), rec["task_id"],
                        rec["class_path"], rec.get("test_class_path"), rec.get("pairing_method"),
                        rec.get("testing_framework"), rec.get("revision_verification_status"),
                        n_in_repo, order_key(dataset, rec)[:16], src])

    written += [jsonl_path, csv_path]


def write_checksums(out_dir, written):
    """SHA256SUMS.txt: checksum của mọi file đã ghi và của chính script này.
    Dùng để chứng minh danh sách không bị sửa sau khi commit."""
    with open(os.path.join(out_dir, "SHA256SUMS.txt"), "w", encoding="utf-8", newline="\n") as f:
        for path in written + [os.path.abspath(__file__)]:
            with open(path, "rb") as g:
                digest = hashlib.sha256(g.read()).hexdigest()
            f.write(f"{digest}  {os.path.basename(path)}\n")


# ---------------------------------------------------------------------------
# 4. CHẠY
# ---------------------------------------------------------------------------

def main():
    if len(sys.argv) != 3:
        sys.exit("Cách chạy: python3 select_datasets.py <thư mục results/team-r1> <thư mục đầu ra>")
    results_dir, out_dir = sys.argv[1], sys.argv[2]
    os.makedirs(out_dir, exist_ok=True)
    written = []
    summary = [f"SALT = {SALT}"]

    # --- Classes2Test ---
    # Chỉ lấy lớp CONTENT_MATCHED (đã xác minh đúng revision) và bỏ repo Few-shot.
    few_shot = {norm(u) for u in FEW_SHOT_REPOS}
    c2t = [it for it in load(results_dir, "classes2test")
           if it[1].get("revision_verification_status") == "CONTENT_MATCHED"
           and norm(it[1]["repo_url"]) not in few_shot]
    c2t_chosen = one_per_repo("classes2test", c2t)
    summary.append(f"Classes2Test: pool {len(c2t)} lớp / {len(c2t_chosen)} repo")

    # Cắt danh sách đã xếp thành các nhóm liên tiếp: nhóm sau bắt đầu ở chỗ
    # nhóm trước dừng, nên không có repo nào nằm ở hai nhóm.
    pos = 0
    for name, size in C2T_GROUPS:
        rows = c2t_chosen[pos:pos + size]
        assert len(rows) == size, f"{name}: chỉ còn {len(rows)} repo, cần {size}"
        write_group(out_dir, name, "classes2test", rows, written)
        summary.append(f"  {name}: {size}")
        pos += size
    summary.append(f"  còn lại chưa dùng: {len(c2t_chosen) - pos} repo")

    # --- CAT-LM: lấy hết, mỗi repo 1 lớp ---
    cat = load(results_dir, "catlm")
    cat_chosen = one_per_repo("catlm", cat)
    write_group(out_dir, "catlm_eval", "catlm", cat_chosen, written)
    summary.append(f"CAT-LM: pool {len(cat)} lớp / {len(cat_chosen)} repo → catlm_eval {len(cat_chosen)}")

    # --- GitBug-Java: lấy hết, mỗi repo 1 lớp ---
    gb = load(results_dir, "gitbug-java")
    gb_chosen = one_per_repo("gitbug-java", gb)
    write_group(out_dir, "gitbug_java_eval", "gitbug-java", gb_chosen, written)
    manual = sum(1 for c in gb_chosen if c[1].get("pairing_method") == "manual")
    summary.append(f"GitBug-Java: pool {len(gb)} lớp / {len(gb_chosen)} repo → gitbug_java_eval "
                   f"{len(gb_chosen)} ({len(gb_chosen) - manual} ghép theo tên, {manual} ghép tay)")

    # --- Kiểm: ba dataset không được dùng chung repo nào ---
    repos = {}
    for name, chosen in (("classes2test", c2t_chosen[:pos]), ("catlm", cat_chosen),
                         ("gitbug-java", gb_chosen)):
        for c in chosen:
            r = norm(c[1]["repo_url"])
            assert r not in repos, f"repo {r} nằm ở cả {repos[r]} và {name}"
            repos[r] = name

    write_checksums(out_dir, written)
    print("\n".join(summary))


if __name__ == "__main__":
    main()
