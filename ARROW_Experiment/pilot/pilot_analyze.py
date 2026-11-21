#!/usr/bin/env python3
"""Tính N từ kết quả pilot theo quy tắc B (chốt 18/11/2026, cac_thuc_nghiem.md mục Pilot).

Quy tắc B:
    S(k) = số chain sửa thành công ở vòng ≤ k, gộp 4 cơ chế và 2 mô hình trên 50 lớp pilot.
    Δ(k) = S(k) − S(k−1).
    N là vòng k nhỏ nhất, 5 ≤ k ≤ 10, sao cho Δ(k−1) < 1%·S(12) và Δ(k) < 1%·S(12);
    không có k nào thỏa thì N = 10.

Báo cáo kèm theo (đã công bố trước):
    - tỷ lệ mất ℓ = (S(12) − S(N)) / S(12), cận trên khoảng tin cậy 95% Clopper–Pearson;
    - kiểm công bằng: cơ chế (hoặc mô hình) nào còn ≥ 5% số chain thành công của nó ở vòng N+1..12
      thì ghi là giới hạn. KHÔNG in tỷ lệ thành công theo cơ chế (pilot không so sánh cơ chế).
    - thời gian một vòng sửa (gọi mô hình + build), số vòng trung bình mỗi chain nếu giới hạn N vòng:
      để ước giờ máy cho TN2.

Loại khỏi mẫu số:
    - chain lỗi hạ tầng (status khác DONE);
    - chain có c0 kiểm lại đã VALID (success_round = 0): không có gì để sửa.

Cách chạy:
    python3 pilot_analyze.py                 # gộp mọi máy trong results/ (bỏ *-smoke)
    python3 pilot_analyze.py --dir results/dung-smoke     # xem thử kết quả chạy khói
Đầu ra (reports/ hoặc <dir>/report/):
    pilot_n.md        tóm tắt: N, bảng S(k), tỷ lệ mất, kiểm công bằng, thời gian
    pilot_n.csv       k, S(k), Δ(k), RSR@k gộp
    pilot_chains.csv  mỗi chain một dòng
    pilot_rounds.csv  mỗi vòng sửa một dòng (gọi mô hình, build, và từng bước build: compile, run_target, module_tests…)
    pilot_gen_timing.csv  mỗi lần sinh c0 một dòng (khối 1, gọi mô hình sinh, kiểm c0 và từng bước)
"""
import argparse
import csv
import glob
import json
import math
import os
import statistics
import sys

try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

HERE = os.path.dirname(os.path.abspath(__file__))

# Quy tắc đã chốt trước pilot: KHÔNG sửa các số này sau khi đã thấy kết quả.
EPS = 0.01          # ngưỡng 1% của S(12)
K_MIN, K_MAX = 5, 10
K_PILOT = 12
FAIR = 0.05         # ngưỡng kiểm công bằng
SENSITIVITY = [0.01, 0.02, 0.05]   # chỉ để minh họa trong báo cáo, không dùng chọn N
MECH_LABEL = {"fixed_plus": "F+", "fixed_minus": "F−", "fixed_l2": "F2", "adaptive": "A"}


def read_json(path):
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except (OSError, json.JSONDecodeError):
        return None


def read_runs(dirs):
    """Đọc pilot_runs.jsonl của các máy; khóa trùng giữa các máy thì dừng."""
    latest, source = {}, {}
    for d in dirs:
        path = os.path.join(d, "repair", "pilot_runs.jsonl")
        if not os.path.exists(path):
            print(f"[!] Bỏ qua {d}: không có repair/pilot_runs.jsonl")
            continue
        mine = {}
        with open(path, encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    r = json.loads(line)
                    r["_log_dir"] = os.path.join(d, "repair", "logs")
                    mine[(r["task_id"], r["model"], r["strategy"], r["mechanism"])] = r
        for k, r in mine.items():
            if k in latest:
                raise SystemExit(f"Chain {k} có ở cả {source[k]} và {d}. Xóa bản thừa rồi chạy lại.")
            latest[k], source[k] = r, d
    return latest


def step_seconds(verify):
    """{"step_compile_s": 1.9, "step_run_target_s": 1.9, ...} từ danh sách steps của verify.json."""
    out = {}
    for st in (verify or {}).get("steps") or []:
        k = f"step_{st.get('step')}_s"
        out[k] = round(out.get(k, 0) + (st.get("seconds") or 0), 3)
    return out


def block_seconds(rec, log_dir, name):
    """Thời gian từng khối của arrow: lấy từ dòng kết quả, nếu thiếu thì đọc JSON gốc trong logs/."""
    if rec.get("block_seconds"):
        return rec["block_seconds"]
    data = read_json(os.path.join(log_dir, name + ".json")) or {}
    return ((data.get("targets") or [{}])[0]).get("seconds") or {}


def uniform(rows):
    """Mọi dòng có cùng bộ cột (cột bước build khác nhau giữa các vòng)."""
    keys = []
    for r in rows:
        keys += [k for k in r if k not in keys]
    return [{k: r.get(k, "") for k in keys} for r in rows]


def chain_rows(latest):
    """Mỗi chain → một dòng; kèm danh sách vòng sửa."""
    chains, rounds = [], []
    for (task_id, model, strategy, mech), r in sorted(latest.items()):
        row = {"task_id": task_id, "class_fqn": r.get("class_fqn"), "model": model, "strategy": strategy,
               "mechanism": mech, "machine": r.get("machine"), "status": r.get("status"),
               "result": r.get("result"), "c0_previous_state": r.get("c0_previous_state"),
               "c0_state": r.get("c0_state"), "success_round": r.get("success_round"),
               "rounds_run": r.get("rounds_run"), "stop_reason": r.get("stop_reason"),
               "seconds": r.get("seconds"), "run_id": r.get("run_id"), "templates": "", "level_max": ""}
        b3 = os.path.join(r.get("run_dir") or "", "targets", r.get("class_fqn") or "", "block3")
        bs = block_seconds(r, r.get("_log_dir", ""), f"{task_id}__{model}__{strategy}__{mech}")
        vc0 = read_json(os.path.join(b3, "verify_c0.json")) or {}
        row.update(block1_s=bs.get("block1"), block3_s=bs.get("block3"), total_s=bs.get("total"),
                   c0_verify_s=vc0.get("build_seconds"), **{"c0_" + k: v for k, v in step_seconds(vc0).items()})
        final = read_json(os.path.join(b3, "final.json")) or {}
        if final:
            row["success_round"] = final.get("success_round")
            row["rounds_run"] = final.get("rounds_run")
            row["stop_reason"] = final.get("stop_reason")
            row["c0_state"] = final.get("c0_state")
            row["same_model"] = final.get("same_model")
        tmpl, levels = set(), []
        for rd in sorted(glob.glob(os.path.join(b3, "round_*")), key=lambda p: int(p.rsplit("_", 1)[1])):
            k = int(rd.rsplit("_", 1)[1])
            dec = read_json(os.path.join(rd, "decision.json")) or {}
            use = read_json(os.path.join(rd, "usage.json")) or {}
            ver = read_json(os.path.join(rd, "verify.json")) or {}
            pr = read_json(os.path.join(rd, "prompt.json")) or {}
            for t in pr.get("templates") or []:
                tmpl.add(f"{t.get('id')}@{t.get('version')}:{str(t.get('sha256'))[:12]}")
            if dec.get("level") is not None:
                levels.append(dec["level"])
            llm_s, build_s = use.get("latency_seconds"), ver.get("build_seconds", dec.get("build_seconds"))
            rounds.append({"task_id": task_id, "model": model, "strategy": strategy, "mechanism": mech, "round": k,
                           "state": ver.get("state", dec.get("state")), "level": dec.get("level"),
                           "duplicate_of_round": dec.get("duplicate_of_round"),
                           "llm_seconds": llm_s, "build_seconds": build_s,
                           "round_seconds": (llm_s or 0) + (build_s or 0) if (llm_s or build_s) else None,
                           "prompt_tokens": use.get("prompt_tokens"), "completion_tokens": use.get("completion_tokens"),
                           "finish_reason": use.get("finish_reason"), **step_seconds(ver)})
        row["templates"] = ";".join(sorted(tmpl))
        row["level_max"] = max(levels) if levels else ""
        chains.append(row)
    return chains, rounds


def gen_rows(dirs):
    """Mỗi lần sinh c0 (bước gen, ghi bởi tn1_run.py) một dòng thời gian."""
    rows = []
    for d in dirs:
        path = os.path.join(d, "gen", "tn1_runs.jsonl")
        if not os.path.exists(path):
            continue
        latest = {}
        for line in open(path, encoding="utf-8"):
            if line.strip():
                r = json.loads(line)
                latest[(r["task_id"], r["model"], r["strategy"])] = r
        for (task_id, model, strategy), r in sorted(latest.items()):
            if r.get("status") != "DONE":
                continue
            data = read_json(os.path.join(d, "gen", "logs", f"{task_id}__{model}__{strategy}.json")) or {}
            bs = ((data.get("targets") or [{}])[0]).get("seconds") or {}
            tdir = os.path.join(r.get("run_dir") or "", "targets", r["class_fqn"])
            use = read_json(os.path.join(tdir, "block2", "usage.json")) or {}
            vc0 = read_json(os.path.join(tdir, "block3", "verify_c0.json")) or {}
            rows.append({"task_id": task_id, "model": model, "strategy": strategy, "result": r.get("result"),
                         "block1_s": bs.get("block1"), "llm_s": use.get("latency_seconds"),
                         "c0_verify_s": vc0.get("build_seconds"), "total_s": bs.get("total"), **step_seconds(vc0)})
    return rows


def timing_table(title, rows_by_model, cols):
    """Bảng trung vị / p90 (giây) của các cột thời gian, theo mô hình."""
    L = [f"**{title}** (giây, trung vị / p90; n = số lần đo)\n", "| Mô hình | " + " | ".join(c[1] for c in cols) + " |",
         "|---|" + "---|" * len(cols)]
    for m, rows in rows_by_model.items():
        cells = []
        for key, _ in cols:
            xs = [float(r[key]) for r in rows if r.get(key) not in (None, "")]
            cells.append(f"{quantile(xs, .5):.1f} / {quantile(xs, .9):.1f} (n={len(xs)})" if xs else "—")
        L.append(f"| {m} | " + " | ".join(cells) + " |")
    return L + [""]


def binom_cdf(x, n, p):
    """P(X ≤ x), X ~ Binomial(n, p)."""
    if p <= 0:
        return 1.0
    if p >= 1:
        return 1.0 if x >= n else 0.0
    lp, lq = math.log(p), math.log1p(-p)
    return min(1.0, sum(math.exp(math.lgamma(n + 1) - math.lgamma(i + 1) - math.lgamma(n - i + 1)
                                  + i * lp + (n - i) * lq) for i in range(x + 1)))


def cp_upper(x, n, alpha=0.05):
    """Cận trên Clopper–Pearson của khoảng tin cậy hai phía 1 − alpha."""
    if n == 0:
        return float("nan")
    if x >= n:
        return 1.0
    lo, hi = x / n, 1.0
    for _ in range(100):                      # chia đôi: tìm p sao cho P(X ≤ x; n, p) = alpha/2
        mid = (lo + hi) / 2
        if binom_cdf(x, n, mid) > alpha / 2:
            lo = mid
        else:
            hi = mid
    return hi


def choose_n(S, eps):
    """Quy tắc B. S là danh sách S(0..12). Trả về (N, có k thỏa không)."""
    thr = eps * S[K_PILOT]
    for k in range(K_MIN, K_MAX + 1):
        if S[k - 1] - S[k - 2] < thr and S[k] - S[k - 1] < thr:
            return k, True
    return K_MAX, False


def cumulative(chains):
    return [sum(1 for c in chains if isinstance(c["success_round"], int) and 1 <= c["success_round"] <= k)
            for k in range(K_PILOT + 1)]


def pct(a, b):
    return f"{100 * a / b:.1f}%" if b else "—"


def quantile(xs, q):
    xs = sorted(xs)
    if not xs:
        return None
    i = (len(xs) - 1) * q
    lo, hi = math.floor(i), math.ceil(i)
    return xs[lo] + (xs[hi] - xs[lo]) * (i - lo)


def write_csv(path, rows):
    if not rows:
        return
    with open(path, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys()), lineterminator="\n")
        w.writeheader()
        w.writerows(rows)


def main():
    ap = argparse.ArgumentParser(description="Tính N từ kết quả pilot (quy tắc B).")
    ap.add_argument("--dir", action="append", help="thư mục results/<máy> (lặp lại được); mặc định mọi máy")
    args = ap.parse_args()
    if args.dir:
        dirs = [os.path.abspath(d) for d in args.dir]
        out = os.path.join(dirs[0], "report") if len(dirs) == 1 else os.path.join(HERE, "reports")
    else:
        dirs = [d for d in sorted(glob.glob(os.path.join(HERE, "results", "*")))
                if os.path.isdir(d) and not d.endswith("-smoke") and not os.path.basename(d).startswith("_")]
        out = os.path.join(HERE, "reports")
    os.makedirs(out, exist_ok=True)

    latest = read_runs(dirs)
    if not latest:
        raise SystemExit("Không có chain nào.")
    chains, rounds = chain_rows(latest)
    write_csv(os.path.join(out, "pilot_chains.csv"), uniform(chains))
    write_csv(os.path.join(out, "pilot_rounds.csv"), uniform(rounds))
    gen = gen_rows(dirs)
    write_csv(os.path.join(out, "pilot_gen_timing.csv"), uniform(gen))

    infra = [c for c in chains if c["status"] != "DONE"]
    done = [c for c in chains if c["status"] == "DONE"]
    c0_valid = [c for c in done if c["success_round"] == 0]
    used = [c for c in done if c["success_round"] != 0]
    # Kiểm: chain không thành công phải chạy đủ 12 vòng (không dừng sớm).
    short = [c for c in used if c["result"] != "VALID" and c["rounds_run"] != K_PILOT]
    tmpl_sets = {}
    for c in used:
        for t in filter(None, (c["templates"] or "").split(";")):
            tid, ver = t.split("@", 1)
            ver = ver.replace("+no-config", "")   # F− ghi hậu tố này, cùng file gốc (cùng sha256)
            tmpl_sets.setdefault(tid, set()).add(ver)

    S = cumulative(used)
    n_chain = len(used)
    N, hit = choose_n(S, EPS)
    lost = S[K_PILOT] - S[N]
    ell = lost / S[K_PILOT] if S[K_PILOT] else float("nan")
    up = cp_upper(lost, S[K_PILOT])

    write_csv(os.path.join(out, "pilot_n.csv"),
              [{"k": k, "S": S[k], "delta": S[k] - S[k - 1], "rsr": round(S[k] / n_chain, 4) if n_chain else ""}
               for k in range(1, K_PILOT + 1)])

    L = []
    L.append("# Pilot chọn N: kết quả\n")
    L.append(f"Máy: {', '.join(os.path.basename(d) for d in dirs)} · chain: {len(chains)} "
             f"(dùng {n_chain}; lỗi hạ tầng {len(infra)}; c0 kiểm lại đã VALID {len(c0_valid)})\n")
    L.append(f"## N = {N}\n")
    thr = EPS * S[K_PILOT]
    L.append(f"Quy tắc B, ngưỡng {EPS:.0%} của S(12) = {S[K_PILOT]} → {thr:.2f} chain. "
             + (f"Thỏa lần đầu tại k = {N}." if hit else f"Không có k nào trong {K_MIN}–{K_MAX} thỏa → N = {K_MAX} (chạm biên: ghi là giới hạn)."))
    L.append("")
    L.append("| k | " + " | ".join(str(k) for k in range(1, K_PILOT + 1)) + " |")
    L.append("|---|" + "---|" * K_PILOT)
    L.append("| S(k) | " + " | ".join(str(S[k]) for k in range(1, K_PILOT + 1)) + " |")
    L.append("| Δ(k) | " + " | ".join(str(S[k] - S[k - 1]) for k in range(1, K_PILOT + 1)) + " |")
    L.append("| RSR@k | " + " | ".join(pct(S[k], n_chain) for k in range(1, K_PILOT + 1)) + " |")
    L.append("")
    L.append(f"**Tỷ lệ mất khi dừng ở N:** ℓ = ({S[K_PILOT]} − {S[N]}) / {S[K_PILOT]} = {pct(lost, S[K_PILOT])}, "
             f"cận trên 95% (Clopper–Pearson) {100 * up:.1f}%.\n")

    L.append(f"## Kiểm công bằng (ngưỡng {FAIR:.0%})\n")
    L.append("Số chain còn sửa thành công ở vòng N+1..12, trên số chain thành công của chính nhóm đó. "
             "Không so sánh tỷ lệ thành công giữa các cơ chế.\n")
    L.append("| Nhóm | Thành công ở vòng N+1..12 | Trên tổng thành công | Tỷ lệ | Giới hạn? |")
    L.append("|---|---|---|---|---|")
    for field, values, label in (("mechanism", list(MECH_LABEL), lambda v: f"cơ chế {MECH_LABEL[v]}"),
                                 ("model", sorted({c["model"] for c in used}), lambda v: f"mô hình {v}")):
        for v in values:
            sub = [c for c in used if c[field] == v]
            s = cumulative(sub)
            late = s[K_PILOT] - s[N]
            flag = "**có**" if s[K_PILOT] and late / s[K_PILOT] >= FAIR else "không"
            L.append(f"| {label(v)} | {late} | {s[K_PILOT]} | {pct(late, s[K_PILOT])} | {flag} |")
    L.append("")

    L.append("## Độ nhạy theo ngưỡng (chỉ minh họa, N dùng ngưỡng 1%)\n")
    L.append("| Ngưỡng | N | Chain bị mất | ℓ | Cận trên 95% |")
    L.append("|---|---|---|---|---|")
    for e in SENSITIVITY:
        n_e, _ = choose_n(S, e)
        lo = S[K_PILOT] - S[n_e]
        L.append(f"| {e:.0%} | {n_e} | {lo} | {pct(lo, S[K_PILOT])} | {100 * cp_upper(lo, S[K_PILOT]):.1f}% |")
    L.append("")

    L.append("## Thời gian và token một vòng sửa (để ước giờ máy TN2)\n")
    L.append("| Mô hình sửa | Số vòng | Trung vị (s) | p90 (s) | Gọi mô hình trung vị (s) | Build trung vị (s) | "
             f"Token vào/ra trung bình | Số vòng TB mỗi chain nếu N = {N} |")
    L.append("|---|---|---|---|---|---|---|---|")
    for m in sorted({c["model"] for c in used}):
        rs = [r for r in rounds if r["model"] == m and r["round_seconds"] is not None]
        tot = [r["round_seconds"] for r in rs]
        llm = [r["llm_seconds"] for r in rs if r["llm_seconds"] is not None]
        bld = [r["build_seconds"] for r in rs if r["build_seconds"] is not None]
        tin = [r["prompt_tokens"] for r in rs if r["prompt_tokens"] is not None]
        tout = [r["completion_tokens"] for r in rs if r["completion_tokens"] is not None]
        cm = [c for c in used if c["model"] == m]
        rbar = statistics.mean([c["success_round"] if c["result"] == "VALID" and c["success_round"] <= N else N
                                for c in cm]) if cm else float("nan")
        f = lambda x: f"{x:.1f}" if x is not None else "—"
        L.append(f"| {m} | {len(rs)} | {f(quantile(tot, .5))} | {f(quantile(tot, .9))} | {f(quantile(llm, .5))} | "
                 f"{f(quantile(bld, .5))} | {f(statistics.mean(tin) if tin else None)} / "
                 f"{f(statistics.mean(tout) if tout else None)} | {rbar:.2f} |")
    L.append("")

    L.append("## Thời gian từng công đoạn\n")
    STEPS = [("step_compile_s", "biên dịch"), ("step_run_target_s", "chạy test đích"), ("step_module_tests_s", "chạy test module")]
    models = sorted({c["model"] for c in chains})
    L += timing_table("Sinh c0", {m: [g for g in gen if g["model"] == m] for m in sorted({g["model"] for g in gen})},
                      [("block1_s", "khối 1 (clone, phân tích, baseline)"), ("llm_s", "gọi mô hình sinh"),
                       ("c0_verify_s", "kiểm c0 (tổng build)")] + STEPS + [("total_s", "tổng")])
    L += timing_table("Sửa: mỗi chain", {m: [c for c in used if c["model"] == m] for m in models},
                      [("block1_s", "khối 1"), ("c0_verify_s", "kiểm lại c0"), ("block3_s", "khối 3 (kiểm + mọi vòng sửa)"),
                       ("total_s", "tổng chain")])
    L += timing_table("Sửa: mỗi vòng", {m: [r for r in rounds if r["model"] == m] for m in models},
                      [("llm_seconds", "gọi mô hình sửa"), ("build_seconds", "build (tổng)")] + STEPS + [("round_seconds", "tổng vòng")])
    L.append("## Kiểm tra tính hợp lệ\n")
    L.append(f"- Chain không thành công nhưng chạy chưa đủ {K_PILOT} vòng: {len(short)}"
             + (" (**cần xem**: " + ", ".join(f"{c['task_id']}/{c['model']}/{c['strategy']}/{c['mechanism']}"
                                             for c in short[:5]) + ")" if short else ""))
    multi = {k: v for k, v in tmpl_sets.items() if len(v) > 1}
    L.append(f"- Prompt sửa dùng: " + "; ".join(f"{k} {sorted(v)}" for k, v in sorted(tmpl_sets.items()))
             + (" (**có template nhiều phiên bản: cần xem**)" if multi else ""))
    if infra:
        L.append(f"- Chain lỗi hạ tầng (loại khỏi mẫu số): {len(infra)}; chạy lại bằng --retry-infra trước khi chốt N.")
    if c0_valid:
        L.append(f"- c0 kiểm lại đã VALID (loại khỏi mẫu số): {len(c0_valid)} chain "
                 f"({len({(c['task_id'], c['model'], c['strategy']) for c in c0_valid})} c0).")
    with open(os.path.join(out, "pilot_n.md"), "w", encoding="utf-8") as fh:
        fh.write("\n".join(L) + "\n")
    print("\n".join(L))
    print(f"\n→ {out}")


if __name__ == "__main__":
    main()
