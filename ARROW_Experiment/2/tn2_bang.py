#!/usr/bin/env python3
"""Bảng TN2 tạm (xem nhanh), theo Sua/ban_phan_bien/cac_thuc_nghiem.md mục Thực nghiệm 2.

Chỉ ĐỌC results/<máy>/report/ (do tn2_collect.py sinh); không chạy gì, không ghi gì.
Không thay cho tn2_analyze.py / tn2_glmer.R (glmer chưa có ở đây). Không cần scipy/statsmodels.

    python3 tn2_collect.py --machine <máy>       # trước đó, để report/ mới nhất
    python3 tn2_bang.py <máy>                    # một máy, ví dụ: dung
    python3 tn2_bang.py dung,chinh               # gộp vài máy
    python3 tn2_bang.py all                      # gộp mọi máy có report/ (bỏ thư mục *-smoke)
"""
import csv, glob, json, math, os, sys, statistics as st
from collections import defaultdict, Counter
from math import comb

root = os.path.dirname(os.path.abspath(__file__))
arg = sys.argv[1] if len(sys.argv) > 1 else "all"
if arg == "all":
    machs = sorted(os.path.basename(os.path.dirname(os.path.dirname(p)))
                   for p in glob.glob(f"{root}/results/*/report/tn2_chains.csv") if "-smoke" not in p)
else:
    machs = arg.split(",")
if not machs:
    raise SystemExit("Chưa có results/<máy>/report/tn2_chains.csv — chạy tn2_collect.py --machine <máy> trước.")
chains, rounds = [], []
for m in machs:
    rep_dir = f"{root}/results/{m}/report"
    if not os.path.exists(f"{rep_dir}/tn2_chains.csv"):
        raise SystemExit(f"Thiếu {rep_dir}/tn2_chains.csv — chạy: python3 tn2_collect.py --machine {m}")
    chains += list(csv.DictReader(open(f"{rep_dir}/tn2_chains.csv", encoding="utf-8")))
    rounds += list(csv.DictReader(open(f"{rep_dir}/tn2_rounds.csv", encoding="utf-8")))
try:
    out = {json.loads(l)["replaces"] for l in open(f"{root}/tn2_thay_the.jsonl", encoding="utf-8") if l.strip()}
except FileNotFoundError:
    out = set()
n_all = len(chains)
chains = [c for c in chains if c["candidate_id"] not in out and c["status"] == "DONE"]
M = ["fixed_plus", "fixed_minus", "fixed_l2", "adaptive"]; NM = dict(zip(M, ["F+", "F−", "F2", "A"]))
DS = ["classes2test", "catlm", "gitbug-java"]; DN = {"classes2test": "Classes2Test", "catlm": "CAT-LM", "gitbug-java": "GitBug-Java"}
key = lambda r: (r["candidate_id"], r["repair_model"], r["mechanism"], r["rep"])
R = defaultdict(list)
for r in rounds:
    R[key(r)].append(r)
for k in R:
    R[k].sort(key=lambda r: int(r["round"]))
ok = lambda c: c["result"] == "VALID"
f = lambda x, d=1: f"{x:.{d}f}".replace(".", ",")

def wilson(x, n, z=1.959964):
    if n == 0: return (0, 0)
    p = x / n; d = 1 + z * z / n; c = p + z * z / (2 * n); h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n))
    return ((c - h) / d, (c + h) / d)

def mcnemar(b, c):
    n = b + c
    return 1.0 if n == 0 else min(1.0, 2 * sum(comb(n, i) for i in range(min(b, c) + 1)) / 2 ** n)

def holm(ps):
    order = sorted(range(len(ps)), key=lambda i: ps[i]); adj = [0] * len(ps); m = len(ps); run = 0
    for j, i in enumerate(order):
        run = max(run, min(1, (m - j) * ps[i])); adj[i] = run
    return adj

def wilcoxon(d):
    d = [x for x in d if x != 0]; n = len(d)
    if n == 0: return 1.0
    a = sorted(range(n), key=lambda i: abs(d[i])); ranks = [0] * n; i = 0
    while i < n:
        j = i
        while j + 1 < n and abs(d[a[j + 1]]) == abs(d[a[i]]): j += 1
        for k in range(i, j + 1): ranks[a[k]] = (i + j) / 2 + 1
        i = j + 1
    wp = sum(r for r, x in zip(ranks, d) if x > 0); mu = n * (n + 1) / 4
    sd = math.sqrt(n * (n + 1) * (2 * n + 1) / 24); z = (wp - mu) / sd
    return math.erfc(abs(z) / math.sqrt(2))

def a12(x, y):
    g = sum((a > b) + 0.5 * (a == b) for a in x for b in y); return g / (len(x) * len(y))

def rt(c):
    return sum(float(r["llm_seconds"] or 0) + float(r["build_seconds"] or 0) for r in R[key(c)])

def q(xs):
    if not xs: return "—"
    xs = sorted(xs); qs = st.quantiles(xs, n=4) if len(xs) > 1 else [xs[0]] * 3
    return f"{f(st.median(xs))} [{f(qs[0])}–{f(qs[2])}]"

rep1 = [c for c in chains if c["rep"] == "1"]
ncand = len({c["candidate_id"] for c in rep1})
print(f"# TN2 — máy {', '.join(machs)}: {ncand} candidate, lần 1 = {len(rep1)} chain "
      f"(đã loại {len(out)} candidate bị thay; bỏ {n_all - len(chains)} chain không DONE hoặc bị thay)\n")

print("## 1. RSR@10 (chỉ số chính RQ1b, RQ2) — lần 1, Wilson 95%\n")
print("| Dataset | n/nhánh | " + " | ".join(NM[m] for m in M) + " |"); print("|---|---|" + "---|" * 4)
for ds in DS + ["all"]:
    sel = [c for c in rep1 if ds == "all" or c["dataset"] == ds]
    cells = []
    for m in M:
        x = [c for c in sel if c["mechanism"] == m]; v = sum(map(ok, x)); lo, hi = wilson(v, len(x))
        cells.append(f"{f(100*v/len(x))}% [{f(100*lo,0)}–{f(100*hi,0)}]")
    print(f"| {DN.get(ds, '**Tổng**')} | {len(sel)//4} | " + " | ".join(cells) + " |")

print("\n## 2. Kiểm định cặp (McNemar chính xác, Holm trong từng RQ, từng dataset) — lần 1\n")
fam = {"RQ1b": [("fixed_plus", "fixed_minus")], "RQ2": [("adaptive", "fixed_plus"), ("fixed_l2", "fixed_plus"), ("adaptive", "fixed_l2")]}
print("| Dataset | RQ | Cặp | b (đầu đạt) | c (sau đạt) | p | p Holm |"); print("|---|---|---|---|---|---|---|")
for ds in DS + ["all"]:
    sel = [c for c in rep1 if ds == "all" or c["dataset"] == ds]
    d = {(c["candidate_id"], c["repair_model"], c["mechanism"]): ok(c) for c in sel}
    P = sorted({(a, b) for a, b, _ in d})
    for rq, pairs in fam.items():
        res = []
        for a, b_ in pairs:
            b = sum(d[p + (a,)] and not d[p + (b_,)] for p in P); c = sum(d[p + (b_,)] and not d[p + (a,)] for p in P)
            res.append((a, b_, b, c, mcnemar(b, c)))
        adj = holm([r[4] for r in res])
        for (a, b_, b, c, p), pa in zip(res, adj):
            print(f"| {DN.get(ds, 'Tổng (tham khảo)')} | {rq} | {NM[a]} vs {NM[b_]} | {b} | {c} | {f(p,3)} | {f(pa,3)} |")

print("\n## 3. RSR@k — lần 1, mọi dataset\n")
print("| Nhánh | " + " | ".join(f"k={k}" for k in range(1, 11)) + " |"); print("|---|" + "---|" * 10)
for m in M:
    x = [c for c in rep1 if c["mechanism"] == m]
    print(f"| {NM[m]} | " + " | ".join(f(100 * sum(1 for c in x if ok(c) and int(c['success_round']) <= k) / len(x)) for k in range(1, 11)) + " |")

print("\n## 4. RA, RT (chỉ chain thành công; trung vị [IQR]) và chi phí — lần 1\n")
print("| Nhánh | Chain thành công | RA (vòng) | RT (giây) | Lượt gọi LLM / chain | Token vào / chain | Token ra / chain | Vòng trùng hash | Biên dịch được (cuối) | Test đích đạt (cuối) |")
print("|---|---|---|---|---|---|---|---|---|---|")
for m in M:
    x = [c for c in rep1 if c["mechanism"] == m]; s = [c for c in x if ok(c)]
    calls = tin = tout = dup = nr = 0
    for c in x:
        seen = set()
        for r in R[key(c)]:
            nr += 1
            if r["code_hash"] in seen or r["duplicate_code"] == "True": dup += 1
            seen.add(r["code_hash"])
            if r["prompt_tokens"]: calls += 1
            tin += int(float(r["prompt_tokens"] or 0)); tout += int(float(r["completion_tokens"] or 0))
    comp = sum(1 for c in x if c["result"] not in ("COMPILE", "UNKNOWN")); tgt = sum(1 for c in x if c["result"] in ("VALID", "REGRESSION"))
    th = lambda v: f"{v:,}".replace(",", " ")
    print(f"| {NM[m]} | {len(s)} | {q([int(c['success_round']) for c in s])} | {q([rt(c) for c in s])} | {f(calls/len(x))} | "
          f"{th(tin//len(x))} | {th(tout//len(x))} | {dup}/{nr} ({f(100*dup/nr)}%) | {f(100*comp/len(x))}% | {f(100*tgt/len(x))}% |")

print("\n## 5. RT: Wilcoxon hạng có dấu + A12 (cặp mà cả hai nhánh đều thành công) — lần 1\n")
print("| Cặp | n cặp | trung vị RT đầu / sau (giây) | p | A12 (đầu chậm hơn) |"); print("|---|---|---|---|---|")
d = {(c["candidate_id"], c["repair_model"], c["mechanism"]): c for c in rep1}
P = sorted({(a, b) for a, b, _ in d})
for a, b_ in [("fixed_plus", "fixed_minus"), ("adaptive", "fixed_plus"), ("fixed_l2", "fixed_plus"), ("adaptive", "fixed_l2")]:
    both = [p for p in P if ok(d[p + (a,)]) and ok(d[p + (b_,)])]
    xa = [rt(d[p + (a,)]) for p in both]; xb = [rt(d[p + (b_,)]) for p in both]
    print(f"| {NM[a]} vs {NM[b_]} | {len(both)} | {f(st.median(xa)) if xa else '—'} / {f(st.median(xb)) if xb else '—'} | {f(wilcoxon([u-v for u, v in zip(xa, xb)]),3)} | {f(a12(xa, xb),2)} |")

print("\n## 6. Biến thiên giữa 3 lần — RSR@10 mọi dataset\n")
print("| Nhánh | Lần 1 | Lần 2 | Lần 3 | Trung bình | min–max |"); print("|---|---|---|---|---|---|")
for m in M:
    v = []
    for rep in "123":
        x = [c for c in chains if c["rep"] == rep and c["mechanism"] == m]; v.append(100 * sum(map(ok, x)) / len(x) if x else float('nan'))
    print(f"| {NM[m]} | " + " | ".join(f(y) + "%" for y in v) + f" | {f(st.mean(v))}% | {f(min(v))}–{f(max(v))}% |")

print("\n## 7. Cùng mô hình / sửa chéo và mô hình sinh × mô hình sửa (mô tả) — lần 1\n")
print("| Mô hình sinh → sửa | n/nhánh | " + " | ".join(NM[m] for m in M) + " |"); print("|---|---|" + "---|" * 4)
G = {"gpt6luna": "GPT", "qwen3coder-or": "Qwen"}
for lab, flt in [("Cùng mô hình", lambda c: c["same_model"] == "True"), ("Sửa chéo", lambda c: c["same_model"] != "True")] + \
        [(f"{G[g]} → {G[r]}", (lambda g, r: lambda c: c["model"] == g and c["repair_model"] == r)(g, r)) for g in G for r in G]:
    sel = [c for c in rep1 if flt(c)]
    print(f"| {lab} | {len(sel)//4} | " + " | ".join(f(100 * sum(ok(c) for c in sel if c['mechanism'] == m) / max(1, sum(1 for c in sel if c['mechanism'] == m))) + "%" for m in M) + " |")
