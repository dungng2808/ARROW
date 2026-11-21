#!/usr/bin/env python3
# ^ Cho phép chạy bằng ./tn1_analyze.py trên Mac/Linux.
"""Phân tích Thực nghiệm 1 (RQ1a): đọc một hoặc nhiều tn1_candidates.csv → các bảng kết quả.

Cách chạy:
    python3 tn1_analyze.py results/dung/tn1_candidates.csv --out results/_report_dung      # một máy
    python3 tn1_analyze.py results/*/tn1_candidates.csv --out results/_report_all          # gộp mọi máy
    python3 tn1_analyze.py results/dung-smoke/tn1_candidates.csv --pilot --out results/_report_smoke

Ghi ra thư mục --out:
    tn1_tables.md        mọi bảng, đọc được ngay
    table1_rq1a.csv ... table7_timing.csv   từng bảng dạng CSV để đưa vào báo cáo

Kiểm khi gộp:
    - bộ test (lớp, mô hình, cách viết prompt) xuất hiện ở hơn một file:
        nếu đúng một file có lần chạy xong (counted = 1), các file kia chỉ là lỗi hạ tầng → dùng lần chạy xong
        (trường hợp chạy lại trên máy khác bằng --only); còn lại → dừng, in danh sách trùng
    - các file dùng bản tool khác nhau → cảnh báo
    - thiếu bộ test so với dataset → bảng ghi "CHƯA ĐỦ DỮ LIỆU"

Thống kê (chỉ dùng thư viện có sẵn của Python):
    Wilson 95% cho tỷ lệ; Cochran's Q (3 cách viết prompt, 2 bậc tự do: p = exp(-Q/2));
    McNemar chính xác (nhị thức) cho từng cặp; Holm: họ chính = ESR (3 cặp × các mô hình) trong từng dataset,
    họ phụ = CSR và các bước còn lại. Dưới 10 cặp thì chỉ mô tả, không kiểm định.

Giải nghĩa từ khóa Python: xem đầu file tn1_run.py.
"""

import argparse      # đọc tùy chọn dòng lệnh
import csv           # đọc/ghi CSV
import math          # căn bậc hai, hàm mũ, tổ hợp (math.comb)
import os
import sys            # đường dẫn, tạo thư mục
import statistics    # trung bình, trung vị
from collections import defaultdict   # từ điển tự tạo giá trị mặc định khi gặp khóa mới

try:
    sys.stdout.reconfigure(encoding="utf-8")   # Windows: in được chữ tiếng Việt ra cửa sổ lệnh
except (AttributeError, ValueError):
    pass

HERE = os.path.dirname(os.path.abspath(__file__))
FINAL_DIR = os.path.normpath(os.path.join(HERE, "..", "..", "ARROW_dataset", "final"))

STRATEGIES = ["zero_shot", "few_shot", "zero_shot_project_aware"]          # thứ tự cố định trong bảng
LABEL = {"zero_shot": "Zero-shot", "few_shot": "Few-shot", "zero_shot_project_aware": "Project-aware"}
PAIRS = [("zero_shot", "few_shot"), ("zero_shot", "zero_shot_project_aware"),
         ("few_shot", "zero_shot_project_aware")]                            # 3 cặp so sánh
DATASETS = ["classes2test", "catlm", "gitbug-java"]
MANIFEST = {"classes2test": "classes2test_eval", "catlm": "catlm_eval", "gitbug-java": "gitbug_java_eval"}
METRICS = [("esr", "ESR"), ("csr", "CSR (bước 1)"), ("pass_s2", "Bước 2"), ("pass_s3", "Bước 3"),
           ("pass_s4", "Bước 4"), ("pass_s5", "VALID (bước 5)")]            # cột 0/1 trong CSV → tên hiển thị
STATES = ["VALID", "COMPILE", "UNKNOWN", "DISCOVERY", "RUNTIME", "ASSERTION", "REGRESSION", "INVALID_OUTPUT"]
MIN_PAIRS = 10                                                               # dưới mức này chỉ mô tả
QWEN_NUM_CTX = 40960                                                         # num_ctx của Qwen


# ---------------------------------------------------------------------------
# 1. THỐNG KÊ
# ---------------------------------------------------------------------------

def wilson(x, n, z=1.959964):
    """Khoảng tin cậy Wilson 95% cho tỷ lệ x/n → (tỷ lệ, cận dưới, cận trên)."""
    if n == 0:
        return (float("nan"),) * 3                  # không có mẫu: trả 3 giá trị "không xác định"
    p = x / n                                       # tỷ lệ quan sát
    d = 1 + z * z / n                               # mẫu số chung của công thức Wilson
    c = (p + z * z / (2 * n)) / d                   # tâm khoảng
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / d   # nửa độ rộng
    return p, max(0.0, c - h), min(1.0, c + h)


def cochran_q(blocks):
    """Cochran's Q cho 3 cách viết prompt. blocks: danh sách [a, b, c] (0/1) của từng lớp → (Q, p)."""
    k = 3                                           # số cách so sánh
    cols = [sum(b[j] for b in blocks) for j in range(k)]   # tổng mỗi cột (mỗi cách viết prompt)
    rows = [sum(b) for b in blocks]                 # tổng mỗi hàng (mỗi lớp)
    n_tot = sum(rows)
    denom = k * n_tot - sum(r * r for r in rows)    # mẫu số của Q
    if denom == 0:                                  # mọi lớp giống nhau ở cả 3 cách → không có khác biệt
        return 0.0, 1.0
    q = (k - 1) * (k * sum(c * c for c in cols) - n_tot * n_tot) / denom
    return q, math.exp(-q / 2)                      # với 2 bậc tự do, p = exp(-Q/2) (chính xác)


def mcnemar_exact(b, c):
    """McNemar chính xác (hai phía): b, c = số cặp bất đồng mỗi chiều."""
    n = b + c
    if n == 0:
        return 1.0                                  # không có cặp bất đồng → không khác
    tail = sum(math.comb(n, i) for i in range(min(b, c) + 1)) / 2 ** n   # P(X ≤ min) với X ~ Nhị thức(n, 0.5)
    return min(1.0, 2 * tail)                       # nhân 2 cho hai phía, không quá 1


def holm(pvals):
    """Hiệu chỉnh Holm: danh sách p gốc → danh sách p đã hiệu chỉnh (cùng thứ tự)."""
    m = len(pvals)
    order = sorted(range(m), key=lambda i: pvals[i])   # vị trí sắp theo p tăng dần
    adj, running = [0.0] * m, 0.0
    for rank, i in enumerate(order):                # enumerate cho (thứ hạng 0,1,2..., vị trí)
        running = max(running, min(1.0, (m - rank) * pvals[i]))   # nhân (m - hạng), giữ tăng dần
        adj[i] = running
    return adj


def paired_diff(b, c, n):
    """Chênh lệch tỷ lệ ghép cặp (A − B) = (b − c)/n, kèm khoảng tin cậy 95% (Wald cho cặp)."""
    d = (b - c) / n
    se = math.sqrt(max(0.0, (b + c) - (b - c) ** 2 / n)) / n    # sai số chuẩn
    return d, d - 1.959964 * se, d + 1.959964 * se


# ---------------------------------------------------------------------------
# 2. ĐỌC DỮ LIỆU
# ---------------------------------------------------------------------------

INT_COLS = ["counted", "pass_s1", "pass_s2", "pass_s3", "pass_s4", "pass_s5", "csr", "esr", "valid",
            "module_flaky", "prompt_chars"]
NUM_COLS = ["prompt_tokens", "completion_tokens", "llm_seconds", "seconds", "build_seconds",
            "block1_s", "block2_s", "block3_s", "total_s", "step_compile_s", "step_run_target_s", "step_module_tests_s"]
# Bảng 7: thời gian từng công đoạn (cột của tn1_collect.py từ 21/11/2026; file CSV cũ không có thì để trống)
TIMING = [("block1_s", "Khối 1: clone, phân tích, baseline"), ("llm_seconds", "Gọi mô hình"),
          ("build_seconds", "Kiểm c0 (tổng)"), ("step_compile_s", "↳ biên dịch"), ("step_run_target_s", "↳ chạy test đích"),
          ("step_module_tests_s", "↳ chạy test module"), ("total_s", "Tổng một lần sinh")]


def load(paths):
    """Đọc các file CSV → (danh sách dòng, danh sách bộ test bị trùng)."""
    rows, seen, dups = [], {}, []
    for path in paths:
        with open(path, encoding="utf-8") as f:
            for r in csv.DictReader(f):             # DictReader: mỗi dòng thành từ điển {tên cột: giá trị}
                for c in INT_COLS:
                    r[c] = int(r[c]) if r.get(c) not in (None, "") else None    # chữ → số nguyên
                for c in NUM_COLS:
                    r[c] = float(r[c]) if r.get(c) not in (None, "") else None  # chữ → số thực
                r["_file"] = path                   # nhớ dòng này đến từ file nào
                key = (r["task_id"], r["model"], r["strategy"])
                if key in seen:                     # cùng bộ test đã có ở file khác → trùng
                    dups.append((key, seen[key], path))
                seen[key] = path
                rows.append(r)
    return rows, dups


def expected_classes(pilot):
    """Số lớp mỗi dataset theo manifest dataset-v1 (để biết đã đủ dữ liệu chưa)."""
    if pilot:
        names = {"classes2test": "classes2test_pilot"}
    else:
        names = MANIFEST
    out = {}
    for ds, name in names.items():
        with open(os.path.join(FINAL_DIR, name + ".jsonl"), encoding="utf-8") as f:
            out[ds] = sum(1 for line in f if line.strip())   # đếm số dòng không rỗng
    return out


# ---------------------------------------------------------------------------
# 3. ĐỊNH DẠNG
# ---------------------------------------------------------------------------

def pct(x, n):
    """Ví dụ "120/200 (60,0%)"; n = 0 thì "-"."""
    return f"{x}/{n} ({100 * x / n:.1f}%)" if n else "-"


def pct_ci(x, n):
    """Ví dụ "120/200 (60,0% [53,1–66,5])" kèm Wilson 95%."""
    if not n:
        return "-"
    p, lo, hi = wilson(x, n)
    return f"{x}/{n} ({100 * p:.1f}% [{100 * lo:.1f}–{100 * hi:.1f}])"


def fmt_p(p):
    """p-value gọn: < 0,001 hoặc 3 chữ số."""
    return "<0.001" if p < 0.001 else f"{p:.3f}"


def md_table(headers, body):
    """Danh sách hàng → bảng Markdown."""
    lines = ["| " + " | ".join(headers) + " |", "|" + "---|" * len(headers)]
    lines += ["| " + " | ".join(str(c) for c in row) + " |" for row in body]
    return "\n".join(lines)


def write_csv(path, headers, body):
    with open(path, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(headers)
        w.writerows(body)


# ---------------------------------------------------------------------------
# 4. CÁC BẢNG
# ---------------------------------------------------------------------------

def table_coverage(rows, models, expected):
    """Bảng 0: đã có bao nhiêu bộ test so với dự kiến."""
    headers = ["Dataset", "Mô hình", "Dự kiến", "Đã có", "Tính vào mẫu số", "Lỗi hạ tầng/bị chặn", "Còn thiếu"]
    body, complete = [], True
    for ds in expected:
        for m in models:
            got = [r for r in rows if r["dataset"] == ds and r["model"] == m]
            exp = expected[ds] * len(STRATEGIES)    # số lớp × 3 cách viết prompt
            counted = sum(1 for r in got if r["counted"] == 1)
            missing = exp - len(got)
            complete = complete and missing == 0 and counted == exp
            body.append([ds, m, exp, len(got), counted, len(got) - counted, missing])
    return headers, body, complete


def table1(rows, ds, models):
    """Bảng 1: kết quả RQ1a cho một dataset."""
    headers = ["Mô hình", "Cách viết prompt", "Số bộ test", "Không ra code", "Qua bước 1 (CSR)",
               "Qua bước 2", "Qua bước 3", "Qua bước 4", "VALID", "ESR"]
    body = []
    for m in models:
        for s in STRATEGIES:
            g = [r for r in rows if r["dataset"] == ds and r["model"] == m and r["strategy"] == s and r["counted"] == 1]
            n = len(g)
            body.append([m, LABEL[s], n, pct(sum(1 for r in g if r["result"] == "INVALID_OUTPUT"), n),
                         pct_ci(sum(r["pass_s1"] for r in g), n), pct(sum(r["pass_s2"] for r in g), n),
                         pct(sum(r["pass_s3"] for r in g), n), pct(sum(r["pass_s4"] for r in g), n),
                         pct_ci(sum(r["pass_s5"] for r in g), n), pct_ci(sum(r["esr"] for r in g), n)])
    return headers, body


def table2(rows, ds, models):
    """Bảng 2: kiểm định cho một dataset. Chỉ dùng các lớp có đủ cả 3 cách viết prompt (ghép cặp)."""
    headers = ["Mô hình", "Chỉ số", "Số lớp ghép cặp", "Cochran's Q (p)", "Cặp so sánh",
               "b / c", "Chênh lệch [CI 95%]", "p McNemar", "p sau Holm", "Họ"]
    tests = []                                      # mỗi phần tử: thông tin một phép McNemar
    for m in models:
        by_task = defaultdict(dict)                 # task_id → {cách viết prompt: dòng}
        for r in rows:
            if r["dataset"] == ds and r["model"] == m and r["counted"] == 1:
                by_task[r["task_id"]][r["strategy"]] = r
        full = [t for t in by_task.values() if all(s in t for s in STRATEGIES)]   # lớp đủ 3 cách
        for col, name in METRICS:
            blocks = [[t[s][col] for s in STRATEGIES] for t in full]
            q, qp = cochran_q(blocks) if len(full) >= MIN_PAIRS else (None, None)
            for a, b_ in PAIRS:
                b = sum(1 for t in full if t[a][col] == 1 and t[b_][col] == 0)   # A đạt, B không
                c = sum(1 for t in full if t[a][col] == 0 and t[b_][col] == 1)   # B đạt, A không
                tests.append({"model": m, "col": col, "name": name, "n": len(full), "q": q, "qp": qp,
                              "pair": f"{LABEL[a]} – {LABEL[b_]}", "b": b, "c": c,
                              "family": "chính" if col == "esr" else "phụ",
                              "p": mcnemar_exact(b, c) if len(full) >= MIN_PAIRS else None})
    for fam in ("chính", "phụ"):                    # Holm riêng cho từng họ trong dataset này
        idx = [i for i, t in enumerate(tests) if t["family"] == fam and t["p"] is not None]
        for i, padj in zip(idx, holm([tests[i]["p"] for i in idx])):   # zip ghép từng cặp phần tử
            tests[i]["padj"] = padj
    body = []
    for t in tests:
        if t["p"] is None:                          # dưới 10 cặp → chỉ mô tả
            body.append([t["model"], t["name"], t["n"], "mô tả (<10 cặp)", t["pair"],
                         f"{t['b']} / {t['c']}", "-", "-", "-", t["family"]])
            continue
        d, lo, hi = paired_diff(t["b"], t["c"], t["n"])
        body.append([t["model"], t["name"], t["n"], f"{t['q']:.2f} ({fmt_p(t['qp'])})", t["pair"],
                     f"{t['b']} / {t['c']}", f"{100 * d:+.1f} [{100 * lo:+.1f}; {100 * hi:+.1f}]",
                     fmt_p(t["p"]), fmt_p(t["padj"]), t["family"]])
    return headers, body


def table3(rows, ds, models):
    """Bảng 3: bộ test dừng ở trạng thái nào."""
    headers = ["Mô hình", "Cách viết prompt"] + STATES + ["Không tính (hạ tầng/bị chặn)", "Có test cũ nghi chập chờn"]
    body = []
    for m in models:
        for s in STRATEGIES:
            g = [r for r in rows if r["dataset"] == ds and r["model"] == m and r["strategy"] == s]
            counted = [r for r in g if r["counted"] == 1]
            body.append([m, LABEL[s]] + [sum(1 for r in counted if r["result"] == st) for st in STATES]
                        + [len(g) - len(counted), sum(1 for r in counted if (r["module_flaky"] or 0) > 0)])
    return headers, body


def table4(rows, ds, models):
    """Bảng 4: độ dài prompt, token và thời gian gọi mô hình."""
    headers = ["Mô hình", "Cách viết prompt", "Token prompt (TB / trung vị)", "Token đầu ra (TB / trung vị)",
               "Thời gian gọi (trung vị, giây)", "Prompt có thể vượt num_ctx (ước tính)"]
    body = []

    def mm(vals):
        vals = [v for v in vals if v is not None]
        return f"{statistics.mean(vals):.0f} / {statistics.median(vals):.0f}" if vals else "-"

    for m in models:
        for s in STRATEGIES:
            g = [r for r in rows if r["dataset"] == ds and r["model"] == m and r["strategy"] == s and r["counted"] == 1]
            secs = [r["llm_seconds"] for r in g if r["llm_seconds"] is not None]
            over = sum(1 for r in g if (r["prompt_chars"] or 0) / 4 > QWEN_NUM_CTX) if "qwen" in m.lower() else "-"
            # ^ ước tính thô 4 ký tự ≈ 1 token; cần đếm lại bằng tokenizer của Qwen trước khi báo cáo
            body.append([m, LABEL[s], mm(r["prompt_tokens"] for r in g), mm(r["completion_tokens"] for r in g),
                         f"{statistics.median(secs):.1f}" if secs else "-", over])
    return headers, body


def unique_tasks(rows, ds):
    """Mỗi lớp lấy một dòng (thông tin dự án giống nhau giữa các cấu hình)."""
    out = {}
    for r in rows:
        if r["dataset"] == ds and r.get("test_framework"):
            out.setdefault(r["task_id"], r)         # setdefault: chỉ lưu dòng đầu tiên của mỗi lớp
    return list(out.values())


def table5(rows):
    """Bảng 5: framework test của từng dataset."""
    headers = ["Dataset", "Số lớp", "JUnit 4", "JUnit 5", "TestNG"]
    body = []
    for ds in DATASETS:
        u = unique_tasks(rows, ds)
        if u:
            body.append([ds, len(u)] + [sum(1 for r in u if r["test_framework"] == f) for f in ("junit4", "junit5", "testng")])
    return headers, body


def table6(rows):
    """Bảng 6: JDK × framework × build tool (đếm theo JDK dùng để build)."""
    headers = ["Dataset", "Build tool", "JDK", "JUnit 4", "JUnit 5 + vintage", "JUnit 5 không vintage", "TestNG"]
    body = []
    for ds in DATASETS:
        u = unique_tasks(rows, ds)
        for bt in ("maven", "gradle"):
            for jdk in ("8", "11", "17", "21"):
                g = [r for r in u if r["build_tool"] == bt and str(r["jdk_used"]) == jdk]
                if not g:
                    continue
                body.append([ds, bt, jdk,
                             sum(1 for r in g if r["test_framework"] == "junit4"),
                             sum(1 for r in g if r["test_framework"] == "junit5" and r["vintage"] == "True"),
                             sum(1 for r in g if r["test_framework"] == "junit5" and r["vintage"] != "True"),
                             sum(1 for r in g if r["test_framework"] == "testng")])
    return headers, body


# ---------------------------------------------------------------------------
# 5. CHƯƠNG TRÌNH CHÍNH
# ---------------------------------------------------------------------------

def quantile(vals, q):
    vals = sorted(vals)
    i = (len(vals) - 1) * q
    lo = int(i)
    hi = min(lo + 1, len(vals) - 1)
    return vals[lo] + (vals[hi] - vals[lo]) * (i - lo)


def table7(rows, models):
    """Bảng 7: thời gian từng công đoạn (giây), trung vị / p90, theo mô hình và máy."""
    headers = ["Mô hình", "Máy", "Số bộ test"] + [name for _, name in TIMING]
    body = []
    for m in models:
        machines = sorted({r["machine"] for r in rows if r["model"] == m})
        groups = [(mc, [r for r in rows if r["model"] == m and r["machine"] == mc]) for mc in machines]
        if len(machines) > 1:
            groups.append(("(mọi máy)", [r for r in rows if r["model"] == m]))
        for mc, g in groups:
            g = [r for r in g if r["counted"] == 1]
            cells = []
            for col, _ in TIMING:
                vals = [r.get(col) for r in g if r.get(col) is not None]
                cells.append(f"{quantile(vals, .5):.1f} / {quantile(vals, .9):.1f} (n={len(vals)})" if vals else "-")
            body.append([m, mc, len(g)] + cells)
    return headers, body


def main():
    ap = argparse.ArgumentParser(description="Phân tích TN1 → các bảng kết quả.")
    ap.add_argument("inputs", nargs="+", help="một hoặc nhiều tn1_candidates.csv")
    ap.add_argument("--out", required=True, help="thư mục ghi bảng, ví dụ results/_report_all")
    ap.add_argument("--pilot", action="store_true", help="dữ liệu chạy khói trên lớp pilot")
    args = ap.parse_args()

    rows, dups = load(args.inputs)
    if dups:
        # Trùng do chạy lại lỗi hạ tầng trên máy khác: giữ lần chạy xong, bỏ các lần INFRA/bị chặn
        by_key = {}
        for r in rows:
            by_key.setdefault((r["task_id"], r["model"], r["strategy"]), []).append(r)
        keep, bad = [], []
        for key, rs in by_key.items():
            done = [r for r in rs if r["counted"] == 1]
            if len(rs) == 1:
                keep.append(rs[0])
            elif len(done) == 1:
                keep.append(done[0])
                print(f"[i] {key}: dùng lần chạy xong ở {done[0]['_file']}, bỏ {len(rs) - 1} lần lỗi hạ tầng")
            else:
                bad.append((key, [r["_file"] for r in rs]))
        if not bad:
            rows, dups = keep, []
    if dups:                                        # có bộ test trùng giữa các file → không gộp bừa
        for key, f1, f2 in dups[:20]:
            print(f"[X] Trùng {key}: {f1} và {f2}")
        raise SystemExit(f"{len(dups)} bộ test bị trùng giữa các file. Xem lại cách chia --shard. Dừng.")
    os.makedirs(args.out, exist_ok=True)

    models = sorted({r["model"] for r in rows})     # các mô hình có trong dữ liệu
    commits = sorted({r["tool_commit"][:12] for r in rows if r.get("tool_commit")})
    machines = sorted({r["machine"] for r in rows})
    expected = expected_classes(args.pilot)

    md = ["# Kết quả Thực nghiệm 1 (RQ1a)", ""]
    h, b, complete = table_coverage(rows, models, expected)
    md += [("**ĐỦ DỮ LIỆU.**" if complete else "**⚠ CHƯA ĐỦ DỮ LIỆU — bảng dưới chỉ là kết quả tạm, chưa dùng cho báo cáo.**"), ""]
    md += [f"- File đầu vào: {len(args.inputs)} · máy: {', '.join(machines)} · mô hình: {', '.join(models)}",
           f"- Bản tool: {', '.join(commits) or '-'}" + ("  ⚠ **nhiều bản tool khác nhau**" if len(commits) > 1 else ""),
           "", "## Bảng 0. Mức đủ dữ liệu", "", md_table(h, b), ""]
    write_csv(os.path.join(args.out, "table0_coverage.csv"), h, b)

    for ds in expected:                             # mỗi dataset một phần riêng
        md += [f"## Dataset: {ds}", ""]
        if ds in ("catlm", "gitbug-java"):
            md += ["_Ít lớp nên lực thống kê thấp (CI 95% khoảng ±16–17 điểm %); "
                   "không có ý nghĩa thống kê không có nghĩa là không có khác biệt._", ""]
        for title, fn, fname in (("Bảng 1. Kết quả RQ1a", table1, "table1_rq1a"),
                                 ("Bảng 2. Kiểm định", table2, "table2_tests"),
                                 ("Bảng 3. Bộ test dừng ở đâu", table3, "table3_stop_states"),
                                 ("Bảng 4. Token và thời gian gọi mô hình", table4, "table4_tokens")):
            h, b = fn(rows, ds, models)
            md += [f"### {title}", "", md_table(h, b), ""]
            write_csv(os.path.join(args.out, f"{fname}_{ds}.csv"), h, b)

    for title, fn, fname in (("Bảng 5. Framework test", table5, "table5_frameworks"),
                             ("Bảng 6. JDK × framework × build tool (dùng cho TN3)", table6, "table6_jdk_framework")):
        h, b = fn(rows)
        md += [f"## {title}", "", md_table(h, b), ""]
        write_csv(os.path.join(args.out, f"{fname}.csv"), h, b)

    h, b = table7(rows, models)
    md += ["## Bảng 7. Thời gian từng công đoạn (giây, trung vị / p90)", "",
           "_Đo khi chạy song song nhiều worker, máy khác nhau thì cấu hình và số worker khác nhau: so thời gian giữa "
           "các mô hình chỉ trên cùng một máy. n = số bộ test có số đo (bước sau không chạy nếu bước trước lỗi; "
           "\"-\" = máy chưa nộp số liệu từng bước)._", "", md_table(h, b), ""]
    write_csv(os.path.join(args.out, "table7_timing.csv"), h, b)

    md += ["## Ghi chú", "",
           "- Mẫu số: bộ test đã chạy xong, kể cả không ra code; không gồm lỗi hạ tầng và lớp bị chặn ở khối 1.",
           "- CSR: qua bước biên dịch; UNKNOWN không tính là biên dịch được. ESR (chỉ số chính): có ≥ 1 phương thức test đã chạy.",
           "- Kiểm định chỉ dùng lớp có đủ 3 cách viết prompt; McNemar chính xác; Holm: họ chính = ESR, họ phụ = các chỉ số còn lại.",
           "- Cột \"Prompt có thể vượt num_ctx\" là ước tính thô (4 ký tự ≈ 1 token)."]
    with open(os.path.join(args.out, "tn1_tables.md"), "w", encoding="utf-8") as f:
        f.write("\n".join(md) + "\n")
    print(f"{'ĐỦ' if complete else 'CHƯA ĐỦ'} dữ liệu · {len(rows)} dòng · ghi {args.out}/tn1_tables.md")


if __name__ == "__main__":
    main()
