#!/usr/bin/env python3
"""Chọn và đóng băng tập con TN2 (RQ1b, RQ2), rồi chia cho các máy. CHỈ CHẠY MỘT LẦN, trên máy Dung.

Thiết kế: Sua/ban_phan_bien/cac_thuc_nghiem.md, mục "Thực nghiệm 2 — Đầu vào" (chốt 17/11/2026).

Điều kiện vào tập con (R03 §6.1): candidate của TN1 đã tính vào mẫu số (counted = 1), có mã
(không INVALID_OUTPUT), không lỗi hạ tầng, c0 chưa VALID.

Cách rút: trong từng dataset, chia theo 6 ô (mô hình sinh × chiến lược); trong mỗi ô xếp theo
    sha256("arrow-tn2-subset-v1|<dataset>|<task_id>|<model>|<strategy>")
rồi lấy các candidate đầu: Classes2Test 15, CAT-LM 8, GitBug-Java 7 mỗi ô (90 / 48 / 42).
Ô có ít hơn số cần lấy thì lấy hết và ghi vào shortfall_by_cell.

Chia máy (chốt 22/11/2026, theo tốc độ đo ở pilot; Quang = Hán theo yêu cầu): mỗi candidate (cả 24 chain
của nó: 2 mô hình sửa × 4 nhánh × 3 lần) chạy trọn trên một máy. Candidate được xếp theo (dataset, ô, thứ hạng)
rồi phát lần lượt theo tỷ lệ của từng máy, nên máy nào cũng có đủ ba dataset và các ô.

Đầu ra (commit lên Git TRƯỚC khi chạy TN2; sau đó không sửa):
    tn2_subset.jsonl         mỗi candidate một dòng: máy được giao, c0 (đường dẫn + sha256), thứ hạng trong ô
    tn2_subset.jsonl.sha256  mã băm của file trên (tn2_run.py kiểm trước khi chạy)
    tn2_subset.md            tóm tắt: số lấy mỗi ô, shortfall_by_cell, số candidate mỗi máy

Cách chạy (trong ARROW_Experiment/2):
    python3 tn2_subset.py            # ghi 3 file trên; đã có tn2_subset.jsonl thì dừng (đã đóng băng)
    python3 tn2_subset.py --check    # chỉ tính lại và so với file đã đóng băng, không ghi gì

Thay thế (quy tắc chốt 22/11/2026, sau khi chạy máy Dung): candidate có c0 đạt VALID ngay ở vòng 0 trong ít nhất
một chain TN2 thì c0 không tái hiện được lỗi (test phụ thuộc môi trường), nên không đo được tác dụng của cơ chế sửa.
Candidate đó được thay bằng candidate hạng kế tiếp CHƯA DÙNG trong cùng ô (cùng thứ tự sha256 ở trên), giao cho cùng
máy. Không sửa tn2_subset.jsonl; các lần thay ghi nối vào tn2_thay_the.jsonl (+ .sha256). Dữ liệu chain của
candidate bị thay giữ nguyên trong results/, chỉ không đưa vào phân tích.
    python3 tn2_subset.py --thay <candidate_id> --ly-do "<lý do + bằng chứng>"
"""
import argparse
import csv
import datetime
import hashlib
import json
import os
import sys
from collections import Counter

try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

HERE = os.path.dirname(os.path.abspath(__file__))                    # ARROW_Experiment/2
TN1_DIR = os.path.normpath(os.path.join(HERE, "..", "1"))            # ARROW_Experiment/1
OUT = os.path.join(HERE, "tn2_subset.jsonl")
REPL = os.path.join(HERE, "tn2_thay_the.jsonl")

# ---------------------------------------------------------------------------
# CẤU HÌNH ĐÃ CHỐT (không sửa sau khi đã commit tn2_subset.jsonl)
# ---------------------------------------------------------------------------
SALT = "arrow-tn2-subset-v1"                                         # chuỗi cố định của phép xếp
PER_CELL = {"classes2test": 15, "catlm": 8, "gitbug-java": 7}        # số lấy mỗi ô (mô hình sinh × chiến lược)
DATASETS = ["classes2test", "catlm", "gitbug-java"]
MODELS = ["gpt6luna", "qwen3coder-or"]
STRATEGIES = ["zero_shot", "few_shot", "zero_shot_project_aware"]
# Thư mục kết quả TN1 (dung-qwen-fix: lớp chạy bù cho lớp bị chặn trên máy Hán)
TN1_MACHINES = ["dung", "dung-qwen", "dung-qwen-fix", "quang-qwen", "han-qwen", "chinh-qwen"]
# Số candidate mỗi máy (tổng = số candidate lấy được: 179 vì ô CAT-LM · GPT · Zero-shot-PA chỉ có 7)
MACHINES = {"dung": 82, "chinh": 33, "kieuanh": 30, "quang": 17, "han": 17}


def sha256_file(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def rank_key(dataset, task_id, model, strategy):
    return hashlib.sha256(f"{SALT}|{dataset}|{task_id}|{model}|{strategy}".encode()).hexdigest()


def read_pool():
    """Mọi candidate thất bại hợp lệ của TN1 (mỗi khóa đúng một dòng)."""
    pool, seen = [], {}
    for m in TN1_MACHINES:
        path = os.path.join(TN1_DIR, "results", m, "tn1_candidates.csv")
        with open(path, encoding="utf-8") as f:
            for r in csv.DictReader(f):
                if r["counted"] != "1":                      # lỗi hạ tầng / bị chặn: không tính
                    continue
                key = (r["task_id"], r["model"], r["strategy"])
                if key in seen:
                    raise SystemExit(f"Candidate {key} có ở cả {seen[key]} và {m}. Dừng.")
                seen[key] = m
                if r["result"] in ("VALID", "INVALID_OUTPUT", "INFRA") or r["valid_output"] != "True":
                    continue
                rel = f"results/{m}/c0/{r['task_id']}__{r['model']}__{r['strategy']}.java"
                c0 = os.path.join(TN1_DIR, rel)
                if not os.path.exists(c0):
                    raise SystemExit(f"Thiếu file c0: {c0}")
                pool.append({
                    "candidate_id": f"{r['task_id']}__{r['model']}__{r['strategy']}",
                    "dataset": r["dataset"], "task_id": r["task_id"], "class_fqn": r["class_fqn"],
                    "model": r["model"], "strategy": r["strategy"], "c0_result": r["result"],
                    "tn1_machine": m, "tn1_run_id": r["run_id"], "tn1_tool_commit": r["tool_commit"],
                    "c0_path": rel, "c0_sha256": sha256_file(c0),
                })
    return pool


def select(pool):
    """Lấy các candidate đầu của mỗi ô theo thứ tự sha256; trả về (tập con, bảng số lấy mỗi ô, shortfall)."""
    chosen, table, shortfall = [], [], {}
    for ds in DATASETS:
        for model in MODELS:
            for strategy in STRATEGIES:
                cell = [c for c in pool if (c["dataset"], c["model"], c["strategy"]) == (ds, model, strategy)]
                cell.sort(key=lambda c: rank_key(ds, c["task_id"], model, strategy))
                need = PER_CELL[ds]
                take = cell[:need]
                for i, c in enumerate(take, 1):
                    chosen.append(dict(c, cell_rank=i))
                table.append((ds, model, strategy, len(cell), need, len(take)))
                if len(take) < need:
                    shortfall[f"{ds}|{model}|{strategy}"] = need - len(take)
    return chosen, table, shortfall


def assign(chosen):
    """Phát candidate cho các máy theo tỷ lệ: ở mỗi bước, máy đang thiếu nhiều nhất so với phần của nó nhận tiếp."""
    total = len(chosen)
    if sum(MACHINES.values()) != total:
        raise SystemExit(f"Tổng số candidate các máy ({sum(MACHINES.values())}) khác số lấy được ({total}). "
                         f"Sửa MACHINES trước khi chạy.")
    got = {m: 0 for m in MACHINES}
    for i, c in enumerate(chosen, 1):
        m = max(MACHINES, key=lambda x: (MACHINES[x] * i / total - got[x], MACHINES[x]))
        c["machine"] = m
        got[m] += 1
    if got != MACHINES:
        raise SystemExit(f"Chia máy ra {got}, khác {MACHINES}.")
    return chosen


def summary(chosen, table, shortfall):
    L = ["# TN2: tập con đóng băng", "",
         f"- Chuỗi xếp: `sha256(\"{SALT}|<dataset>|<task_id>|<model>|<strategy>\")`",
         f"- Số lấy mỗi ô: Classes2Test {PER_CELL['classes2test']}, CAT-LM {PER_CELL['catlm']}, "
         f"GitBug-Java {PER_CELL['gitbug-java']}",
         f"- Tổng: **{len(chosen)}** candidate × 2 mô hình sửa × 4 nhánh × 3 lần = **{len(chosen) * 24}** chain",
         f"- shortfall_by_cell: `{json.dumps(shortfall, ensure_ascii=False)}`", "",
         "## Số lấy mỗi ô", "", "| Dataset | Mô hình sinh | Chiến lược | Có | Cần | Lấy |", "|---|---|---|---|---|---|"]
    L += [f"| {a} | {b} | {c} | {d} | {e} | {f} |" for a, b, c, d, e, f in table]
    L += ["", "## Chia máy", "", "| Máy | Candidate | Chain | Classes2Test | CAT-LM | GitBug-Java |", "|---|---|---|---|---|---|"]
    for m in MACHINES:
        mine = [c for c in chosen if c["machine"] == m]
        n = Counter(c["dataset"] for c in mine)
        L.append(f"| {m} | {len(mine)} | {len(mine) * 24} | {n['classes2test']} | {n['catlm']} | {n['gitbug-java']} |")
    return "\n".join(L) + "\n"


def read_jsonl(path):
    if not os.path.exists(path):
        return []
    with open(path, encoding="utf-8") as f:
        return [json.loads(line) for line in f if line.strip()]


def replace(bad_id, reason):
    """Thay candidate `bad_id` bằng candidate hạng kế tiếp chưa dùng trong cùng ô; ghi nối vào tn2_thay_the.jsonl."""
    frozen = read_jsonl(OUT)
    if not frozen:
        raise SystemExit("Chưa có tn2_subset.jsonl.")
    repl = read_jsonl(REPL)
    used = {c["candidate_id"] for c in frozen} | {r["candidate_id"] for r in repl}
    replaced = {r["replaces"] for r in repl}
    current = {c["candidate_id"]: c for c in frozen + repl}
    if bad_id not in current:
        raise SystemExit(f"{bad_id} không có trong tập con TN2.")
    if bad_id in replaced:
        raise SystemExit(f"{bad_id} đã được thay rồi.")
    bad = current[bad_id]
    ds, model, strategy = bad["dataset"], bad["model"], bad["strategy"]
    cell = [c for c in read_pool() if (c["dataset"], c["model"], c["strategy"]) == (ds, model, strategy)]
    cell.sort(key=lambda c: rank_key(ds, c["task_id"], model, strategy))
    for rank, c in enumerate(cell, 1):
        if c["candidate_id"] not in used:
            new = dict(c, cell_rank=rank, machine=bad["machine"], replaces=bad_id, replace_reason=reason,
                       replaced_at=datetime.date.today().isoformat())
            break
    else:
        raise SystemExit(f"Ô {ds} · {model} · {strategy} hết candidate dự bị.")
    with open(REPL, "a", encoding="utf-8", newline="\n") as f:
        f.write(json.dumps(new, ensure_ascii=False, sort_keys=True) + "\n")
    with open(REPL + ".sha256", "w", encoding="utf-8", newline="\n") as f:
        f.write(f"{sha256_file(REPL)}  tn2_thay_the.jsonl\n")
    print(f"Thay {bad_id} (hạng {bad['cell_rank']}) bằng {new['candidate_id']} (hạng {new['cell_rank']}), "
          f"máy {new['machine']}.\nĐã ghi {REPL} (+ .sha256). Commit hai file này trước khi chạy candidate mới.")


def main():
    ap = argparse.ArgumentParser(description="Chọn và đóng băng tập con TN2.")
    ap.add_argument("--check", action="store_true", help="tính lại và so với tn2_subset.jsonl đã đóng băng")
    ap.add_argument("--thay", metavar="CANDIDATE_ID", help="thay candidate có c0 VALID ở vòng 0 (xem đầu file)")
    ap.add_argument("--ly-do", default="", help="lý do và bằng chứng (bắt buộc với --thay)")
    args = ap.parse_args()
    if args.thay:
        if not args.ly_do.strip():
            raise SystemExit("Cần --ly-do.")
        return replace(args.thay, args.ly_do.strip())
    pool = read_pool()
    chosen, table, shortfall = select(pool)
    chosen = assign(chosen)
    text = "".join(json.dumps(c, ensure_ascii=False, sort_keys=True) + "\n" for c in chosen)
    if args.check:
        if not os.path.exists(OUT):
            raise SystemExit("Chưa có tn2_subset.jsonl.")
        with open(OUT, encoding="utf-8") as f:
            same = f.read() == text
        print("KHỚP với tập con đã đóng băng." if same else "KHÁC tập con đã đóng băng!")
        sys.exit(0 if same else 1)
    if os.path.exists(OUT):
        raise SystemExit("Đã có tn2_subset.jsonl (đã đóng băng). Không ghi đè. Muốn kiểm: --check.")
    with open(OUT, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)
    with open(OUT + ".sha256", "w", encoding="utf-8", newline="\n") as f:
        f.write(f"{sha256_file(OUT)}  tn2_subset.jsonl\n")
    with open(os.path.join(HERE, "tn2_subset.md"), "w", encoding="utf-8", newline="\n") as f:
        f.write(summary(chosen, table, shortfall))
    print(summary(chosen, table, shortfall))
    print(f"Đã ghi {OUT} (+ .sha256, tn2_subset.md). Commit cả ba file trước khi chạy TN2.")


if __name__ == "__main__":
    main()
