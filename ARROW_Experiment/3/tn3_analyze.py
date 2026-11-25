#!/usr/bin/env python3
"""TN3 (RQ3): lập các bảng kết quả từ results/_all/ (do tn3_collect.py ghi). Thiết kế: cac_thuc_nghiem.md, phần
Thực nghiệm 3 ("Các phép so sánh", "Đầu ra"); đặc tả dễ hiểu: dac_ta_TN3.md.

Đọc:  results/_all/lop.csv (mỗi lớp một dòng), results/_all/evosuite.csv, results/_all/phat_hien_loi.csv
Ghi:  results/_all/bao_cao/rq3.md (mọi bảng, đọc được ngay) và từng bảng dạng CSV (b1_...csv … b11_...csv)

Các bảng (làm riêng từng dataset; "Tất cả" chỉ để tham khảo, không kiểm định):
    B1  tỷ lệ qua từng bước của 5 bộ hệ thống (ARROW-GPT, ARROW-Qwen, EvoSuite, ChatUniTest-GPT, ChatUniTest-Qwen), Wilson 95%
    B2  EvoSuite theo từng lần chạy (seed): phân bố trạng thái, số seed VALID mỗi lớp
    B3  chỉ số chất lượng trên bộ VALID (mô tả: trung vị [Q1–Q3]); ARROW tách nguồn initial / adaptive
    B4  HAI PHÉP SO SÁNH CHÍNH: ARROW–EvoSuite, ARROW–ChatUniTest (cùng mô hình); Wilcoxon hạng có dấu + A12,
        Holm cho 2 phép trong từng (dataset, mô hình, chỉ số); dưới 10 cặp → chỉ mô tả. Chỉ số chính: điểm đột biến
    B5  McNemar chính xác: tỷ lệ VALID ARROW với ChatUniTest (cùng mô hình) trên lớp Maven
    B6  chi phí ARROW với ChatUniTest: lượt gọi mô hình, token vào/ra, thời gian mỗi lớp (chỉ mô tả)
    B7  ARROW với EvoSuite cùng ngân sách (phụ, không Holm)
    B8  phân tích phụ trên mọi lớp, lớp không có bộ VALID gán 0 ("theo cách tính của AgoneTest")
    B9  phát hiện lỗi thật trên GitBug-Java: phát hiện / số bộ VALID chạy được, Wilson 95%; hàng test người viết tham chiếu
    B10 đối chiếu khung đánh giá: số bộ được chấp nhận nếu chỉ đòi biên dịch so với khi đòi đủ 5 bước
    B11 cờ đặc biệt (VALID mà mutation = 0, thư viện thêm làm hỏng test có sẵn, REGRESSION do classloader EvoSuite, ...)
    B12 JDK × framework × build tool

Thống kê chỉ dùng thư viện có sẵn của Python (như tn1_analyze.py, tn2_bang.py):
    Wilson 95% cho tỷ lệ; McNemar chính xác (nhị thức); Holm; Wilcoxon hạng có dấu hai phía (chính xác khi n ≤ 30 và
    không có giá trị trùng, còn lại xấp xỉ chuẩn có hiệu chỉnh trùng); A12 của Vargha–Delaney (A12 > 0,5 = ARROW cao hơn).

    python3 tn3_analyze.py
"""
import csv               # đọc/ghi CSV
import math              # căn, hàm erfc, tổ hợp
import os                # đường dẫn
import statistics        # trung vị, tứ phân vị
from collections import Counter, defaultdict   # đếm, từ điển mặc định

import tn3_common as c   # đường dẫn chung, cấu hình

ALL = os.path.join(c.HERE, "results", "_all")              # nơi tn3_collect.py ghi
OUT = os.path.join(ALL, "bao_cao")                          # nơi ghi các bảng
DATASETS = ["classes2test", "catlm", "gitbug-java"]
DN = {"classes2test": "Classes2Test", "catlm": "CAT-LM", "gitbug-java": "GitBug-Java", "all": "Tất cả"}
MODELS = ["gpt6luna", "qwen3coder-or"]
MN = {"gpt6luna": "GPT-6 Luna", "qwen3coder-or": "Qwen3-Coder"}
# 5 "hệ thống" khi lập bảng: (tên hiển thị, tiền tố cột trong lop.csv)
SYSTEMS = [("ARROW-GPT", "arrow_gpt6luna_"), ("ARROW-Qwen", "arrow_qwen3coder-or_"), ("EvoSuite", "evo_"),
           ("ChatUniTest-GPT", "cut_gpt6luna_"), ("ChatUniTest-Qwen", "cut_qwen3coder-or_")]
NO_SUITE = {"NO_OUTPUT", "INVALID_OUTPUT", "MERGE_FAILED"}  # không có bộ test để xác minh (vẫn tính vào mẫu số)
ORDER = ["COMPILE", "UNKNOWN", "DISCOVERY", "RUNTIME", "ASSERTION", "REGRESSION", "VALID"]   # thứ tự 5 bước
METRICS = [("mutation", "Điểm đột biến"), ("line", "Bao phủ dòng"), ("branch", "Bao phủ nhánh"),
           ("test_strength", "Test strength"), ("smell_density", "Mật độ smell")]
N_MIN = c.load_config()["n_pairs_threshold"]               # dưới 10 cặp → chỉ mô tả


# ---------------------------------------------------------------------------
# Đọc dữ liệu
# ---------------------------------------------------------------------------

def conv(v):
    """Chuỗi CSV → giá trị Python: "" → None, "True"/"False" → bool, số → float, còn lại giữ chuỗi."""
    if v is None or v == "":
        return None
    if v in ("True", "False"):
        return v == "True"
    try:
        return float(v)
    except ValueError:
        return v


def read_csv(name):
    """Đọc một file CSV trong results/_all/ thành danh sách dict đã chuyển kiểu."""
    path = os.path.join(ALL, name)
    if not os.path.exists(path):
        raise SystemExit(f"Chưa có {path}. Chạy tn3_collect.py trước.")
    with open(path, encoding="utf-8-sig") as f:
        return [{k: conv(v) for k, v in r.items()} for r in csv.DictReader(f)]


# ---------------------------------------------------------------------------
# Thống kê (Python thuần)
# ---------------------------------------------------------------------------

def wilson(x, n, z=1.959964):
    """Khoảng tin cậy Wilson 95% cho tỷ lệ x/n."""
    if n == 0:
        return (0.0, 0.0)
    p = x / n
    d = 1 + z * z / n
    centre = p + z * z / (2 * n)
    half = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n))
    return ((centre - half) / d, (centre + half) / d)


def mcnemar_exact(b, c_):
    """McNemar chính xác hai phía: b = chỉ A đạt, c = chỉ B đạt (kiểm định nhị thức với p = 0,5)."""
    n = b + c_
    if n == 0:
        return 1.0
    return min(1.0, 2 * sum(math.comb(n, i) for i in range(min(b, c_) + 1)) / 2 ** n)


def holm(ps):
    """Hiệu chỉnh Holm cho một họ p (None giữ nguyên None)."""
    idx = [i for i, p in enumerate(ps) if p is not None]
    order = sorted(idx, key=lambda i: ps[i])
    adj, run, m = [None] * len(ps), 0.0, len(idx)
    for j, i in enumerate(order):
        run = max(run, min(1.0, (m - j) * ps[i]))                # nhân (m - j), giữ đơn điệu tăng
        adj[i] = run
    return adj


def _ranks(vals):
    """Hạng trung bình (giá trị trùng nhận hạng trung bình) của danh sách số."""
    order = sorted(range(len(vals)), key=lambda i: vals[i])
    ranks, i = [0.0] * len(vals), 0
    while i < len(order):
        j = i
        while j + 1 < len(order) and vals[order[j + 1]] == vals[order[i]]:
            j += 1
        for k in range(i, j + 1):
            ranks[order[k]] = (i + j) / 2 + 1
        i = j + 1
    return ranks


def wilcoxon(a, b):
    """Wilcoxon hạng có dấu hai phía cho các cặp (a_i, b_i). Bỏ cặp bằng nhau. Trả về p (None nếu không còn cặp)."""
    d = [x - y for x, y in zip(a, b) if x != y]
    n = len(d)
    if n == 0:
        return None
    r = _ranks([abs(x) for x in d])
    w_plus = sum(rk for rk, x in zip(r, d) if x > 0)              # tổng hạng của hiệu dương
    ties = len(set(abs(x) for x in d)) < n
    if n <= 30 and not ties:                                       # chính xác: đếm mọi cách gán dấu (quy hoạch động)
        total = n * (n + 1) // 2
        cnt = [1] + [0] * total
        for k in range(1, n + 1):
            for s in range(total, k - 1, -1):
                cnt[s] += cnt[s - k]
        w = int(round(min(w_plus, total - w_plus)))
        return min(1.0, 2 * sum(cnt[: w + 1]) / 2 ** n)
    mu = n * (n + 1) / 4                                           # xấp xỉ chuẩn, hiệu chỉnh giá trị trùng
    tie_corr = sum(t ** 3 - t for t in Counter(abs(x) for x in d).values()) / 48
    sd = math.sqrt(n * (n + 1) * (2 * n + 1) / 24 - tie_corr)
    if sd == 0:
        return 1.0
    z = (w_plus - mu) / sd
    return math.erfc(abs(z) / math.sqrt(2))


def a12(x, y):
    """A12 của Vargha–Delaney: xác suất một giá trị của x lớn hơn một giá trị của y (bằng nhau tính 0,5)."""
    if not x or not y:
        return None
    g = sum((p > q) + 0.5 * (p == q) for p in x for q in y)
    return g / (len(x) * len(y))


def qtxt(xs, d=3):
    """'trung vị [Q1–Q3]' của danh sách số."""
    xs = sorted(v for v in xs if v is not None)
    if not xs:
        return "—"
    q = statistics.quantiles(xs, n=4) if len(xs) > 1 else [xs[0]] * 3
    return f"{fmt(statistics.median(xs), d)} [{fmt(q[0], d)}–{fmt(q[2], d)}]"


def fmt(v, d=3):
    """Số → chuỗi kiểu Việt Nam (dấu phẩy thập phân); None → '—'."""
    if v is None:
        return "—"
    if isinstance(v, float) and v.is_integer() and d == 0:
        return str(int(v))
    return f"{v:.{d}f}".replace(".", ",")


def pct(x, n):
    """'x/n (p% [lo–hi])' với Wilson 95%."""
    if n == 0:
        return "—"
    lo, hi = wilson(x, n)
    return f"{x}/{n} ({fmt(100 * x / n, 1)}% [{fmt(100 * lo, 1)}–{fmt(100 * hi, 1)}])"


# ---------------------------------------------------------------------------
# Ghi bảng
# ---------------------------------------------------------------------------

MD = []                                                            # nội dung rq3.md


def table(name, title, header, rows, note=None):
    """Ghi một bảng: thêm vào rq3.md và ghi ra <name>.csv."""
    MD.extend([f"## {title}", ""])
    if note:
        MD.extend([note, ""])
    MD.append("| " + " | ".join(header) + " |")
    MD.append("|" + "---|" * len(header))
    for r in rows:
        MD.append("| " + " | ".join(str(x) for x in r) + " |")
    MD.append("")
    with open(os.path.join(OUT, name + ".csv"), "w", encoding="utf-8-sig", newline="") as f:
        w = csv.writer(f)
        w.writerow(header)
        w.writerows(rows)


def subset(lop, ds):
    """Các lớp của một dataset ("all" = mọi lớp)."""
    return [r for r in lop if ds == "all" or r["dataset"] == ds]


def state_of(r, prefix):
    """Trạng thái của một hệ thống ở một lớp (cột <tiền tố>state)."""
    return r.get(prefix + "state")


def denom(rows, prefix):
    """Mẫu số của tỷ lệ: bỏ lớp chưa có kết quả (MISSING) và lớp không áp dụng (NOT_APPLICABLE, ChatUniTest + Gradle)."""
    return [r for r in rows if state_of(r, prefix) not in (None, "MISSING", "NOT_APPLICABLE")]


def reached(state, step):
    """Bộ test có qua được bước `step` không (theo thứ tự trạng thái): trạng thái phải ĐỨNG SAU bước đó."""
    if state in NO_SUITE or state not in ORDER:
        return False
    return ORDER.index(state) > ORDER.index(step)


# ---------------------------------------------------------------------------
# Các bảng
# ---------------------------------------------------------------------------

def b1_steps(lop):
    """B1: số và tỷ lệ qua từng bước của từng hệ thống (EvoSuite: lớp có ≥ 1 seed VALID; trạng thái lớp = xa nhất qua seed)."""
    rows = []
    for ds in DATASETS + ["all"]:
        for name, p in SYSTEMS:
            sel = denom(subset(lop, ds), p)
            st_ = [state_of(r, p) for r in sel]
            n = len(sel)
            has = sum(1 for s in st_ if s not in NO_SUITE)                       # có bộ test để xác minh
            comp = sum(reached(s, "UNKNOWN") for s in st_)                       # qua biên dịch (UNKNOWN không tính)
            disc = sum(reached(s, "DISCOVERY") for s in st_)                     # được phát hiện
            run = sum(reached(s, "RUNTIME") for s in st_)                        # chạy được ≥ 1 test
            passed = sum(reached(s, "ASSERTION") for s in st_)                   # test đích đạt
            valid = sum(1 for s in st_ if s == "VALID")                          # không hồi quy = VALID
            miss = sum(1 for r in subset(lop, ds) if state_of(r, p) == "MISSING")
            rows.append([DN[ds], name, n, has, comp, disc, run, passed, pct(valid, n), miss])
    table("b1_qua_tung_buoc", "B1. Qua từng bước xác minh (mỗi lớp một bộ; EvoSuite: lớp có ≥ 1 seed VALID)",
          ["Dataset", "Hệ thống", "n lớp", "Có bộ test", "Biên dịch", "Được phát hiện", "Chạy được",
           "Test đạt", "VALID (Wilson 95%)", "Chưa có kết quả"], rows,
          "Mẫu số: mọi lớp có kết quả; ChatUniTest bỏ lớp Gradle (NOT_APPLICABLE, không tính là thất bại). "
          "Không có bộ test (NO_OUTPUT, INVALID_OUTPUT, MERGE_FAILED) vẫn tính vào mẫu số.")


def b2_evosuite_seeds(lop, evo_rows):
    """B2: EvoSuite theo từng lần chạy (seed) của cấu hình chính."""
    rows = []
    for ds in DATASETS + ["all"]:
        sel = [r for r in evo_rows if r.get("config") == "default" and (ds == "all" or r["dataset"] == ds)]
        cnt = Counter(r.get("final_state") for r in sel)
        per = Counter(int(r["evo_seeds_valid"] or 0) for r in subset(lop, ds) if r.get("evo_state") != "MISSING")
        rows.append([DN[ds], len(sel)] + [cnt.get(s, 0) for s in ["NO_OUTPUT", "MERGE_FAILED"] + ORDER] +
                    [", ".join(f"{k}: {per[k]}" for k in sorted(per))])
    table("b2_evosuite_seed", "B2. EvoSuite theo từng seed (cấu hình chính)",
          ["Dataset", "Số lần chạy", "NO_OUTPUT", "MERGE_FAILED"] + ORDER + ["Số lớp theo số seed VALID (0–5)"], rows)


def b3_quality(lop):
    """B3: chỉ số chất lượng trên bộ VALID (mô tả)."""
    rows = []
    for ds in DATASETS + ["all"]:
        for name, p in SYSTEMS:
            sel = [r for r in subset(lop, ds) if state_of(r, p) == "VALID"]
            src = ""
            if p.startswith("arrow_"):                                  # cột source của bảng RQ3
                src_c = Counter(r.get(p + "source") for r in sel)
                src = f"initial {src_c.get('initial', 0)} / adaptive {src_c.get('adaptive', 0)}"
            rows.append([DN[ds], name, len(sel), src] +
                        [qtxt([r.get(p + k) for r in sel]) for k, _ in METRICS] +
                        [qtxt([r.get(p + "test_method_count") for r in sel], 0)])
    table("b3_chat_luong", "B3. Chỉ số trên bộ VALID: trung vị [Q1–Q3] (EvoSuite: trung vị qua các seed VALID của lớp)",
          ["Dataset", "Hệ thống", "n VALID", "Nguồn bộ ARROW"] + [t for _, t in METRICS] + ["Số test"], rows)


def paired(sel, pa, pb, key):
    """Các cặp (giá trị A, giá trị B) của chỉ số `key` trên lớp mà cả hai VALID và cùng đo được."""
    out = []
    for r in sel:
        if state_of(r, pa) == "VALID" and state_of(r, pb) == "VALID":
            x, y = r.get(pa + key), r.get(pb + key)
            if x is not None and y is not None:
                out.append((x, y))
    return out


def compare(sel, pa, pb, key):
    """Một phép so sánh cặp: (n, trung vị A, trung vị B, A12, p Wilcoxon hoặc None nếu < 10 cặp)."""
    pr = paired(sel, pa, pb, key)
    xa, xb = [x for x, _ in pr], [y for _, y in pr]
    p = wilcoxon(xa, xb) if len(pr) >= N_MIN else None
    return (len(pr), statistics.median(xa) if xa else None, statistics.median(xb) if xb else None, a12(xa, xb), p)


def b4_main(lop):
    """B4: hai phép so sánh chính, Holm trong từng (dataset, mô hình, chỉ số)."""
    rows = []
    for ds in DATASETS:
        sel = subset(lop, ds)
        for m in MODELS:
            pa = f"arrow_{m}_"
            for key, title in METRICS:
                res = [("EvoSuite", compare(sel, pa, "evo_", key)),
                       (f"ChatUniTest-{MN[m]}", compare(sel, pa, f"cut_{m}_", key))]
                adj = holm([r[4] for _, r in res])                   # họ Holm = 2 phép so sánh chính
                for (other, (n, ma, mb, a, p)), ph in zip(res, adj):
                    note = "chỉ mô tả (< 10 cặp)" if n < N_MIN else ""
                    rows.append([DN[ds], MN[m], title + (" (chính)" if key == "mutation" else ""), f"ARROW – {other}", n,
                                 fmt(ma), fmt(mb), fmt(a), fmt(p, 4), fmt(ph, 4), note])
    table("b4_so_sanh_chinh", "B4. Hai phép so sánh chính (Wilcoxon hạng có dấu + A12, Holm cho 2 phép trong từng dataset, mô hình, chỉ số)",
          ["Dataset", "Mô hình ARROW", "Chỉ số", "So sánh", "n cặp", "Trung vị ARROW", "Trung vị đối thủ",
           "A12", "p", "p Holm", "Ghi chú"], rows,
          "Cặp = lớp mà cả hai hệ thống có bộ VALID và cùng đo được chỉ số. A12 > 0,5: ARROW cao hơn "
          "(với mật độ smell, thấp hơn mới tốt). Không viết 'không có ý nghĩa thống kê' thành 'không có khác biệt'.")


def b5_mcnemar(lop):
    """B5: McNemar tỷ lệ VALID, ARROW với ChatUniTest cùng mô hình, trên lớp Maven."""
    rows = []
    for ds in DATASETS + ["all"]:
        for m in MODELS:
            pa, pb = f"arrow_{m}_", f"cut_{m}_"
            sel = [r for r in subset(lop, ds) if state_of(r, pb) not in (None, "MISSING", "NOT_APPLICABLE")
                   and state_of(r, pa) not in (None, "MISSING")]
            va = [state_of(r, pa) == "VALID" for r in sel]
            vb = [state_of(r, pb) == "VALID" for r in sel]
            b = sum(1 for x, y in zip(va, vb) if x and not y)           # chỉ ARROW VALID
            c_ = sum(1 for x, y in zip(va, vb) if y and not x)          # chỉ ChatUniTest VALID
            p = mcnemar_exact(b, c_) if len(sel) >= N_MIN and ds != "all" else None
            rows.append([DN[ds], MN[m], len(sel), pct(sum(va), len(sel)), pct(sum(vb), len(sel)), b, c_, fmt(p, 4)])
    table("b5_mcnemar", "B5. Tỷ lệ VALID: ARROW với ChatUniTest cùng mô hình, lớp Maven (McNemar chính xác)",
          ["Dataset", "Mô hình", "n lớp", "ARROW VALID", "ChatUniTest VALID", "Chỉ ARROW", "Chỉ ChatUniTest", "p"], rows)


def b6_cost(lop):
    """B6: chi phí mỗi lớp, ARROW với ChatUniTest (lớp Maven cả hai có kết quả). Chỉ mô tả."""
    rows = []
    for ds in DATASETS + ["all"]:
        for m in MODELS:
            sel = [r for r in subset(lop, ds) if state_of(r, f"cut_{m}_") not in (None, "MISSING", "NOT_APPLICABLE")
                   and state_of(r, f"arrow_{m}_") not in (None, "MISSING")]
            for name, p in (("ARROW", f"arrow_{m}_"), ("ChatUniTest", f"cut_{m}_")):
                rows.append([DN[ds], MN[m], name, len(sel)] +
                            [qtxt([r.get(p + k) for r in sel], 0) for k in ("llm_calls", "prompt_tokens", "completion_tokens")] +
                            [qtxt([r.get(p + "seconds") for r in sel], 0),
                             fmt(sum(r.get(p + "prompt_tokens") or 0 for r in sel), 0),
                             fmt(sum(r.get(p + "completion_tokens") or 0 for r in sel), 0)])
    table("b6_chi_phi", "B6. Chi phí mỗi lớp: trung vị [Q1–Q3] (ARROW = sinh ở TN1 + sửa ở TN3)",
          ["Dataset", "Mô hình", "Hệ thống", "n lớp", "Lượt gọi mô hình", "Token vào", "Token ra", "Giây", "Tổng token vào",
           "Tổng token ra"], rows)


def b7_budget(lop):
    """B7: ARROW với EvoSuite cấu hình cùng ngân sách (phân tích phụ, không Holm)."""
    rows = []
    for ds in DATASETS:
        sel = subset(lop, ds)
        n_valid = sum(1 for r in sel if r.get("evob_state") == "VALID")
        for m in MODELS:
            for key, title in METRICS:
                n, ma, mb, a, p = compare(sel, f"arrow_{m}_", "evob_", key)
                rows.append([DN[ds], MN[m], title, n, fmt(ma), fmt(mb), fmt(a), fmt(p, 4),
                             "chỉ mô tả (< 10 cặp)" if n < N_MIN else "", n_valid,
                             qtxt([r.get("evob_budget") for r in sel], 0)])
    table("b7_cung_ngan_sach", "B7. ARROW với EvoSuite cùng ngân sách (phụ)",
          ["Dataset", "Mô hình ARROW", "Chỉ số", "n cặp", "Trung vị ARROW", "Trung vị EvoSuite", "A12", "p", "Ghi chú",
           "EvoSuite VALID", "search_budget (giây)"], rows)


def b8_zero(lop):
    """B8: mọi lớp của dataset, lớp không có bộ VALID gán 0 (độ bao phủ, điểm đột biến; bỏ smell)."""
    rows = []
    for ds in DATASETS:
        for m in MODELS:
            pa = f"arrow_{m}_"
            for other, pb, maven_only in (("EvoSuite", "evo_", False), (f"ChatUniTest-{MN[m]}", f"cut_{m}_", True)):
                sel = [r for r in subset(lop, ds) if state_of(r, pa) not in (None, "MISSING")
                       and state_of(r, pb) not in (None, "MISSING")
                       and (not maven_only or state_of(r, pb) != "NOT_APPLICABLE")]
                for key, title in METRICS[:3]:                    # điểm đột biến, bao phủ dòng, bao phủ nhánh
                    def val(r, p):                                      # VALID → chỉ số (không đo được → bỏ); không VALID → 0
                        return (r.get(p + key) if state_of(r, p) == "VALID" else 0.0)
                    pr = [(val(r, pa), val(r, pb)) for r in sel]
                    pr = [(x, y) for x, y in pr if x is not None and y is not None]
                    xa, xb = [x for x, _ in pr], [y for _, y in pr]
                    p = wilcoxon(xa, xb) if len(pr) >= N_MIN else None
                    rows.append([DN[ds], MN[m], f"ARROW – {other}", title, len(pr),
                                 fmt(statistics.mean(xa) if xa else None), fmt(statistics.mean(xb) if xb else None),
                                 fmt(statistics.median(xa) if xa else None), fmt(statistics.median(xb) if xb else None),
                                 fmt(p, 4)])
    table("b8_gan_0", "B8. Phân tích phụ trên mọi lớp, không VALID gán 0 (theo cách tính của AgoneTest)",
          ["Dataset", "Mô hình ARROW", "So sánh", "Chỉ số", "n lớp", "TB ARROW", "TB đối thủ", "Trung vị ARROW",
           "Trung vị đối thủ", "p Wilcoxon"], rows,
          "Mẫu số ARROW–EvoSuite: mọi lớp; ARROW–ChatUniTest: lớp Maven. Bộ VALID mà không đo được chỉ số thì bỏ lớp đó.")


def b9_bugs(det):
    """B9: phát hiện lỗi thật trên GitBug-Java."""
    groups = [("ARROW-GPT", lambda r: r["system"] == "arrow" and r["variant"] == "gpt6luna"),
              ("ARROW-Qwen", lambda r: r["system"] == "arrow" and r["variant"] == "qwen3coder-or"),
              ("EvoSuite (seed nhỏ nhất ra VALID)", lambda r: r["system"] == "evosuite" and r.get("primary") is True),
              ("EvoSuite (mọi seed VALID, mô tả phụ)", lambda r: r["system"] == "evosuite"),
              ("ChatUniTest-GPT", lambda r: r["system"] == "chatunitest" and r["variant"] == "gpt6luna"),
              ("ChatUniTest-Qwen", lambda r: r["system"] == "chatunitest" and r["variant"] == "qwen3coder-or"),
              ("Test người viết ghép với lớp (tham chiếu)", lambda r: r["system"] == "human" and r["variant"] == "paired"),
              ("Test trong test_patch của bug (kiểm bản lỗi)", lambda r: r["system"] == "human" and r["variant"] == "bug_tests")]
    rows = []
    for name, pick in groups:
        sel = [r for r in det if pick(r)]
        v = Counter(r.get("verdict") for r in sel)
        n = v.get("DETECTED", 0) + v.get("NOT_DETECTED", 0)          # chỉ tính bộ chạy được trên bản lỗi
        rows.append([name, len(sel), pct(v.get("DETECTED", 0), n), v.get("NOT_DETECTED", 0),
                     v.get("COMPILE_FAILED", 0), v.get("NOT_RUN", 0)])
    table("b9_phat_hien_loi", "B9. Phát hiện lỗi thật (GitBug-Java): phát hiện / số bộ chạy được trên bản lỗi",
          ["Hệ thống", "Số bộ", "Phát hiện (Wilson 95%)", "Không phát hiện", "Không biên dịch (báo riêng)", "Không chạy"], rows,
          "Bộ test sinh trên bản đã sửa; có test hỏng trên bản lỗi = phát hiện. Hai hàng cuối chỉ là tham chiếu.")


def b10_frame(lop):
    """B10: số bộ được chấp nhận nếu chỉ đòi biên dịch, so với khi đòi đủ 5 bước."""
    rows = []
    for ds in DATASETS + ["all"]:
        for name, p in SYSTEMS:
            sel = denom(subset(lop, ds), p)
            comp = sum(reached(state_of(r, p), "UNKNOWN") for r in sel)
            valid = sum(1 for r in sel if state_of(r, p) == "VALID")
            rows.append([DN[ds], name, len(sel), comp, valid, comp - valid,
                         fmt(100 * (comp - valid) / comp, 1) + "%" if comp else "—"])
    table("b10_khung_danh_gia", "B10. Đối chiếu khung đánh giá: chỉ đòi biên dịch so với đủ 5 bước",
          ["Dataset", "Hệ thống", "n lớp", "Chấp nhận nếu chỉ đòi biên dịch", "Chấp nhận khi đủ 5 bước",
           "Bị loại thêm nhờ 4 bước sau", "Tỷ lệ bị loại thêm"], rows)


def b11_flags(lop):
    """B11: cờ đặc biệt theo hệ thống (đếm lớp)."""
    flag_names = [("valid_mutation_0", "VALID mà mutation = 0"), ("dep_breaks_baseline", "Thư viện thêm làm hỏng test có sẵn"),
                  ("evo_classloader_reg", "REGRESSION do classloader EvoSuite"),
                  ("mutation_not_measured", "VALID nhưng không đo được mutation")]
    rows = []
    for ds in DATASETS + ["all"]:
        for name, p in SYSTEMS:
            pre = "evo_any_" if p == "evo_" else p                   # EvoSuite: lớp có ít nhất một seed bị cờ
            rows.append([DN[ds], name] + [sum(1 for r in subset(lop, ds) if r.get(pre + f) is True) for f, _ in flag_names])
    table("b11_co_dac_biet", "B11. Cờ đặc biệt (số lớp; EvoSuite: lớp có ít nhất một seed bị cờ)",
          ["Dataset", "Hệ thống"] + [t for _, t in flag_names], rows)


def b12_env(lop):
    """B12: JDK × framework × build tool của từng dataset."""
    rows = []
    for ds in DATASETS:
        groups = defaultdict(Counter)
        for r in subset(lop, ds):
            fw = r.get("framework")
            col = "JUnit 4" if fw == "junit4" else "TestNG" if fw == "testng" else \
                  ("JUnit 5 + vintage" if r.get("vintage") is True else "JUnit 5 không vintage")
            groups[(r.get("build_tool"), int(r.get("jdk") or 0))][col] += 1
        for (bt, jdk), cnt in sorted(groups.items()):
            n_evo = sum(1 for r in subset(lop, ds) if r.get("build_tool") == bt and int(r.get("jdk") or 0) == jdk
                        and r.get("evosuite_runnable") is True)
            rows.append([DN[ds], bt, jdk] + [cnt.get(k, 0) for k in ("JUnit 4", "JUnit 5 + vintage", "JUnit 5 không vintage", "TestNG")]
                        + [n_evo])
    table("b12_moi_truong", "B12. JDK × framework × build tool (JDK ARROW dùng ở TN1)",
          ["Dataset", "Build", "JDK", "JUnit 4", "JUnit 5 + vintage", "JUnit 5 không vintage", "TestNG",
           "EvoSuite chạy được (JDK ≤ 11)"], rows)


def main():
    lop = read_csv("lop.csv")
    evo_rows = read_csv("evosuite.csv")
    det = read_csv("phat_hien_loi.csv")
    os.makedirs(OUT, exist_ok=True)
    MD.extend(["# TN3 (RQ3): kết quả", "", f"Sinh bởi `tn3_analyze.py`, {c.now_iso()}, từ `results/_all/` "
               f"({len(lop)} lớp). Mỗi dataset kiểm định riêng; dưới {N_MIN} cặp chỉ mô tả.", ""])
    b1_steps(lop)
    b2_evosuite_seeds(lop, evo_rows)
    b3_quality(lop)
    b4_main(lop)
    b5_mcnemar(lop)
    b6_cost(lop)
    b7_budget(lop)
    b8_zero(lop)
    b9_bugs(det)
    b10_frame(lop)
    b11_flags(lop)
    b12_env(lop)
    with open(os.path.join(OUT, "rq3.md"), "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(MD) + "\n")
    print(f"Đã ghi {OUT}/rq3.md và các bảng b1…b12 (.csv)")


if __name__ == "__main__":
    main()
