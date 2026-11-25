#!/usr/bin/env python3
"""Đóng băng đầu vào TN3 và chia máy. CHỈ CHẠY MỘT LẦN, trên máy Dung (chạy lại chỉ khi TN1 đổi, dùng --force).

Ghi ra 3 file (2 file đầu kèm file .sha256 để các máy kiểm không ai sửa):
    tn3_classes.jsonl   268 lớp: dataset, build tool, JDK, framework, vintage, EvoSuite chạy được không (JDK ≤ 11),
                        ChatUniTest chạy được không (Maven), máy được giao
    tn3_arrow.jsonl     536 bộ ARROW (lớp × mô hình), chiến lược Zero-shot-project-aware (PA) của TN1:
                        action = measure        (TN1 VALID ngay → chỉ đo lại)
                                 repair         (TN1 chưa VALID, có file test → sửa Adaptive 10 vòng)
                                 invalid_output (TN1 không ra file test → tính thất bại, không sửa)
                                 missing_tn1    (TN1 thiếu dòng được tính → phải chạy lại TN1 trước)
    tn3_lists.md        bảng tóm tắt cho người đọc

Chia máy (chốt 26/11/2026): 10 phần, Dung 4 phần; 6 phần còn lại cho 4 máy, Chính ít hơn một chút
→ số lớp mỗi máy lấy từ tn3_config.json ("machines"). Lớp xếp theo dataset rồi theo sha256("arrow-tn3-v1|<dataset>|<task_id>"),
giao lần lượt cho máy đang thiếu nhiều nhất so với tỷ lệ (như TN2), nên máy nào cũng nhận đủ ba dataset.

    python3 tn3_lists.py            ghi (từ chối nếu đã có, trừ khi thêm --force)
    python3 tn3_lists.py --check    kiểm sha256 của các file đã đóng băng
"""
import argparse          # đọc tham số dòng lệnh (--check, --force)
import csv               # đọc file tn1_candidates.csv của TN1
import hashlib           # tính sha256 để xếp thứ tự lớp
import json              # đọc/ghi JSON
import os                # đường dẫn file
import sys               # sys.exit(): trả mã thoát cho --check

import tn3_common as c   # thư viện chung của TN3 (đường dẫn, đọc/ghi file, thư viện TN1)

SALT = "arrow-tn3-v1"    # chuỗi cố định ghép vào trước khi băm: đổi chuỗi này là đổi thứ tự lớp, nên KHÔNG đổi
DATASETS = ["classes2test", "catlm", "gitbug-java"]            # thứ tự dataset khi xếp lớp
MODELS = ["gpt6luna", "qwen3coder-or"]                         # hai mô hình của ARROW
STRATEGY = "zero_shot_project_aware"                           # TN3 chỉ dùng bộ test PA của TN1
# Các thư mục kết quả TN1 (thứ tự như tn2_subset.py). Một (lớp, mô hình) chỉ được có đúng một dòng counted = 1.
TN1_MACHINES = ["dung", "dung-qwen", "dung-qwen-fix", "quang-qwen", "han-qwen", "chinh-qwen"]
CLASSES = os.path.join(c.HERE, "tn3_classes.jsonl")            # file danh sách lớp sẽ ghi
ARROW = os.path.join(c.HERE, "tn3_arrow.jsonl")                # file danh sách bộ ARROW sẽ ghi
SUMMARY = os.path.join(c.HERE, "tn3_lists.md")                 # file tóm tắt sẽ ghi


def config():
    """Đọc tn3_config.json (số lớp mỗi máy, phiên bản cấu hình)."""
    with open(os.path.join(c.HERE, "tn3_config.json"), encoding="utf-8") as f:
        return json.load(f)


def rank(dataset, task_id):
    """Khóa xếp thứ tự của một lớp: sha256 của "SALT|dataset|task_id".
    Chỉ phụ thuộc định danh lớp, nên máy nào tính cũng ra cùng thứ tự (tái lập được, không cần seed ngẫu nhiên)."""
    return hashlib.sha256(f"{SALT}|{dataset}|{task_id}".encode()).hexdigest()


def read_tn1():
    """Đọc các dòng TN1 được tính (counted = 1) của chiến lược PA.
    Trả về {(task_id, model): (dòng csv, tên thư mục máy TN1)}."""
    rows = {}                                                   # kết quả
    for m in TN1_MACHINES:                                      # duyệt từng thư mục kết quả TN1
        path = os.path.join(c.TN1_DIR, "results", m, "tn1_candidates.csv")
        with open(path, encoding="utf-8") as f:
            for r in csv.DictReader(f):                         # mỗi r là một dòng csv dạng dict {cột: giá trị}
                if r["strategy"] != STRATEGY or r["counted"] != "1":
                    continue                                    # bỏ chiến lược khác và dòng không được tính
                key = (r["task_id"], r["model"])
                if key in rows:                                 # một bộ có 2 dòng được tính → dữ liệu TN1 sai
                    raise SystemExit(f"{key} có ở cả {rows[key][1]} và {m}. Dừng.")
                rows[key] = (r, m)
    return rows


def evosuite_ok(jdk):
    """Lớp lọc 1 của EvoSuite (chốt 26/11/2026): chạy được khi JDK ≤ 11.
    Framework không còn là điều kiện vì script thêm JUnit 4.12 và vintage khi module thiếu (tn3_common.evosuite_setup)."""
    try:
        return int(jdk) <= 11                                   # "8", "11" → đúng; "17" → sai
    except (TypeError, ValueError):                             # JDK không đọc được (rỗng, None) → coi là không chạy được
        return False


def split(classes, quotas):
    """Giao lớp cho máy. `classes` đã xếp thứ tự; `quotas` = {máy: số lớp}.
    Lần lượt từng lớp: giao cho máy đang thiếu nhiều nhất so với phần nó đáng có ở thời điểm đó.
    Nhờ vậy dataset nào cũng được chia gần đúng tỷ lệ giữa các máy. Trả về {máy: số lớp đã giao}."""
    total = len(classes)
    if sum(quotas.values()) != total:                           # tổng số lớp các máy phải bằng số lớp thật
        raise SystemExit(f"Tổng quota máy {sum(quotas.values())} khác số lớp {total}. Sửa tn3_config.json.")
    got = {m: 0 for m in quotas}                                # số lớp đã giao cho mỗi máy
    for i, cl in enumerate(classes, 1):                         # i = số lớp đã duyệt (đếm từ 1)
        # phần đáng có của máy x sau i lớp = quotas[x] * i / total; trừ số đã giao = mức thiếu.
        # Máy thiếu nhiều nhất được nhận; hòa thì máy có quota lớn hơn nhận trước.
        m = max(quotas, key=lambda x: (quotas[x] * i / total - got[x], quotas[x]))
        cl["machine"] = m                                       # ghi máy vào lớp
        got[m] += 1
    return got


def write_frozen(path, rows):
    """Ghi danh sách thành file .jsonl (mỗi dòng một JSON, khóa xếp theo abc) và file .sha256 bên cạnh."""
    with open(path, "w", encoding="utf-8", newline="\n") as f:  # newline="\n": cùng kiểu xuống dòng trên Windows/macOS
        for r in rows:
            f.write(json.dumps(r, ensure_ascii=False, sort_keys=True) + "\n")
    with open(path + ".sha256", "w", encoding="utf-8", newline="\n") as f:
        f.write(f"{c.sha256_file(path)}  {os.path.basename(path)}\n")   # định dạng giống lệnh shasum


def check():
    """--check: so sha256 hiện tại của 2 file với sha256 đã ghi. Trả về True nếu đều đúng."""
    ok = True
    for path in (CLASSES, ARROW):
        with open(path + ".sha256", encoding="utf-8") as f:
            want = f.read().split()[0]                          # phần đầu dòng là chuỗi sha256
        got = c.sha256_file(path)
        print(f"{os.path.basename(path)}: {'ĐÚNG' if got == want else 'SAI'}")
        ok &= got == want                                       # một file sai là cả kết quả sai
    return ok


def load_frozen():
    """Cho các script chạy (tn3_arrow.py, tn3_evosuite.py, ...): kiểm sha256 rồi trả về (danh sách lớp, danh sách bộ ARROW).
    File bị sửa hoặc chưa có thì dừng, để không máy nào chạy trên danh sách khác."""
    for path in (CLASSES, ARROW):
        if not os.path.exists(path):
            raise SystemExit(f"Chưa có {os.path.basename(path)}. Dung chạy tn3_lists.py rồi commit trước.")
        with open(path + ".sha256", encoding="utf-8") as f:
            if c.sha256_file(path) != f.read().split()[0]:
                raise SystemExit(f"{os.path.basename(path)} khác bản đã đóng băng (sha256). git checkout lại file. Dừng.")
    return c.read_jsonl(CLASSES), c.read_jsonl(ARROW)


def main():
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--check", action="store_true", help="chỉ kiểm sha256")
    ap.add_argument("--force", action="store_true", help="ghi đè danh sách đã đóng băng (chỉ khi TN1 đổi)")
    a = ap.parse_args()
    if a.check:                                                 # chế độ kiểm: in kết quả rồi thoát
        sys.exit(0 if check() else 1)
    if os.path.exists(CLASSES) and not a.force:                 # chống vô tình ghi đè danh sách đã commit
        raise SystemExit("Đã có tn3_classes.jsonl. Dùng --check để kiểm, --force nếu thật sự cần ghi lại.")
    cfg = config()
    c.base.check_manifests(c.base.MANIFESTS["eval"])            # kiểm sha256 manifest dataset-v1 (hàm của TN1)
    tasks = c.base.read_tasks(c.base.MANIFESTS["eval"])         # 268 lớp của tập đánh giá
    tn1 = read_tn1()                                            # các bộ PA của TN1

    # --- 1. Danh sách lớp: lấy thông tin môi trường (JDK, build tool, framework) từ dòng TN1 của lớp đó ---
    classes, arrow = [], []
    for t in tasks:
        rows = [tn1.get((t["task_id"], m)) for m in MODELS]     # dòng TN1 của 2 mô hình (có thể None)
        # lấy dòng TN1 đầu tiên có ghi JDK thật arrow đã dùng (jdk_used); hai mô hình cùng lớp có cùng môi trường
        info = next((r for r, _ in (x for x in rows if x) if r.get("jdk_used")), None)
        if info is None:
            raise SystemExit(f"Không có dòng TN1 nào cho lớp {t['task_id']}")
        vintage = info.get("vintage") == "True"                 # csv lưu chữ "True"/"False"
        cl = dict(t,                                            # chép mọi trường của manifest (task_id, repo_url, ...)
                  build_tool=info["build_tool"], jdk=info["jdk_used"], framework=info["test_framework"],
                  vintage=vintage,
                  evosuite_runnable=evosuite_ok(info["jdk_used"]),              # lớp lọc 1 của EvoSuite
                  chatunitest_applicable=info["build_tool"] == "maven",          # ChatUniTest chỉ có plugin Maven
                  rank=rank(t["dataset"], t["task_id"]))
        classes.append(cl)
    classes.sort(key=lambda x: (DATASETS.index(x["dataset"]), x["rank"]))      # xếp: theo dataset, rồi theo sha256
    got = split(classes, cfg["machines"])                       # giao máy
    machine_of = {cl["task_id"]: cl["machine"] for cl in classes}

    # --- 2. Danh sách bộ ARROW: mỗi lớp × mỗi mô hình một bộ, kèm việc phải làm ở TN3 ---
    for cl in classes:
        for model in MODELS:
            hit = tn1.get((cl["task_id"], model))
            rec = {"task_id": cl["task_id"], "dataset": cl["dataset"], "class_fqn": cl["class_fqn"], "model": model,
                   "strategy": STRATEGY, "machine": machine_of[cl["task_id"]]}
            if hit is None:                                     # TN1 chưa có dòng được tính cho bộ này
                rec.update(action="missing_tn1")
            else:
                r, m = hit
                rec.update(tn1_machine=m, tn1_run_id=r["run_id"], tn1_result=r["result"],
                           tn1_total_s=float(r["total_s"] or 0),       # thời gian TN1 (dùng cho EvoSuite cùng ngân sách)
                           tn1_llm_s=float(r["llm_seconds"] or 0))
                if r["valid_output"] != "True":                 # TN1 không ra file test dùng được (cắt cụt, 2 lớp, ...)
                    rec.update(action="invalid_output", tn1_invalid_reason=r["invalid_reason"])
                else:
                    rel = f"results/{m}/tests/{cl['task_id']}__{model}__{STRATEGY}.java"   # file test c0 của TN1
                    path = os.path.join(c.TN1_DIR, rel)
                    if not os.path.exists(path):
                        raise SystemExit(f"Thiếu file bộ test TN1: {path}")
                    rec.update(action="measure" if r["valid"] == "1" else "repair",       # VALID ngay → đo lại; không → sửa
                               c0_path=rel, c0_sha256=c.sha256_file(path))                # sha256 để kiểm khi chạy
            arrow.append(rec)

    write_frozen(CLASSES, classes)
    write_frozen(ARROW, arrow)

    # --- 3. File tóm tắt cho người đọc (bảng markdown) ---
    L = ["# TN3: danh sách đóng băng", "", f"Sinh bởi `tn3_lists.py` ({cfg['version']}), {c.now_iso()}.", "",
         "## Chia máy", "", "| Máy | Lớp | Classes2Test | CAT-LM | GitBug-Java | EvoSuite chạy được (JDK ≤ 11) | Maven |",
         "|---|---:|---:|---:|---:|---:|---:|"]
    for m in cfg["machines"]:
        mine = [x for x in classes if x["machine"] == m]
        L.append(f"| {m} | {len(mine)} | " + " | ".join(str(sum(x['dataset'] == d for x in mine)) for d in DATASETS)
                 + f" | {sum(x['evosuite_runnable'] for x in mine)} | {sum(x['chatunitest_applicable'] for x in mine)} |")
    L += ["", "## Bộ ARROW (Zero-shot-project-aware của TN1)", "",
          "| Dataset | Mô hình | measure | repair | invalid_output | missing_tn1 |", "|---|---|---:|---:|---:|---:|"]
    for d in DATASETS:
        for model in MODELS:
            sub = [x for x in arrow if x["dataset"] == d and x["model"] == model]
            L.append(f"| {d} | {model} | " + " | ".join(str(sum(x['action'] == k for x in sub))
                                                     for k in ("measure", "repair", "invalid_output", "missing_tn1")) + " |")
    L += ["", "## JDK × framework × build tool (JDK ARROW dùng ở TN1)", "",
          "| Dataset | Build | JDK | JUnit 4 | JUnit 5 + vintage | JUnit 5 không vintage | TestNG | EvoSuite chạy được |",
          "|---|---|---|---:|---:|---:|---:|---:|"]
    for d in DATASETS:
        for bt in ("maven", "gradle"):
            for jdk in sorted({x["jdk"] for x in classes}, key=lambda v: int(v) if v.isdigit() else 99):
                sub = [x for x in classes if x["dataset"] == d and x["build_tool"] == bt and x["jdk"] == jdk]
                if not sub:
                    continue                                    # bỏ ô trống
                L.append(f"| {d} | {bt} | {jdk} | {sum(x['framework'] == 'junit4' for x in sub)} | "
                         f"{sum(x['framework'] == 'junit5' and x['vintage'] for x in sub)} | "
                         f"{sum(x['framework'] == 'junit5' and not x['vintage'] for x in sub)} | "
                         f"{sum(x['framework'] == 'testng' for x in sub)} | {sum(x['evosuite_runnable'] for x in sub)} |")
    L += ["", "| Dataset | Lớp | EvoSuite chạy được | Maven (ChatUniTest) |", "|---|---:|---:|---:|"]
    for d in DATASETS:
        sub = [x for x in classes if x["dataset"] == d]
        L.append(f"| {d} | {len(sub)} | {sum(x['evosuite_runnable'] for x in sub)} | {sum(x['chatunitest_applicable'] for x in sub)} |")
    with open(SUMMARY, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(L) + "\n")
    print("\n".join(L))
    print(f"\nĐã ghi {os.path.basename(CLASSES)}, {os.path.basename(ARROW)} (+ .sha256), {os.path.basename(SUMMARY)}. Máy: {got}")


if __name__ == "__main__":      # chỉ chạy main() khi gọi trực tiếp file này, không chạy khi file khác import
    main()
