#!/usr/bin/env python3
"""TN3: gom kết quả của 5 máy thành bảng chung, kiểm đủ dữ liệu, gắn cờ đặc biệt. Chạy trên máy Dung sau khi các máy
đã commit thư mục results/<máy>/. Thiết kế: cac_thuc_nghiem.md, phần Thực nghiệm 3; đặc tả dễ hiểu: dac_ta_TN3.md.

Đọc (mỗi máy trong tn3_config.json "machines"; bỏ qua thư mục *-smoke):
    results/<máy>/arrow_runs.jsonl, evosuite_runs.jsonl, chatunitest_runs.jsonl, phat_hien_loi.jsonl, tn3.lock.json
Ghi vào results/_all/:
    arrow.csv          mỗi (lớp, mô hình) một dòng          (dòng mới nhất, chỉ dòng DONE)
    evosuite.csv       mỗi (lớp, cấu hình, seed) một dòng
    chatunitest.csv    mỗi (lớp, mô hình) một dòng
    phat_hien_loi.csv  mỗi (lớp, hệ thống, biến thể) một dòng
    lop.csv            MỖI LỚP MỘT DÒNG: thông tin lớp + kết quả gọn của mọi hệ thống (đầu vào của tn3_analyze.py);
                       EvoSuite cấu hình chính gộp theo thiết kế: số seed VALID / 5 và TRUNG VỊ chỉ số qua các seed VALID
    kiem_tra.md        đủ chưa: thiếu gì, dòng lỗi hạ tầng (INFRA/STOPPED/BAD_CONFIG), lệch bản tool, lớp chạy sai máy

Cờ đặc biệt (chốt khi chạy khói 26/11/2026, để phân tích tách riêng):
    valid_mutation_0       bộ VALID mà điểm đột biến = 0 (vd. bộ EvoSuite chỉ có test "lớp không nạp được")
    dep_breaks_baseline    thư viện thêm vào (runtime EvoSuite / starter ChatUniTest) làm mất test đạt có sẵn
    evo_classloader_reg    EvoSuite REGRESSION do classloader riêng rò sang test có sẵn (Spring/Mockito), không do thư viện
    mutation_not_measured  bộ VALID nhưng không đo được điểm đột biến (vd. pit_source = script-junit5-failed)
    smell_not_measured     bộ VALID có phương thức @Test không public: tsDetect 2.2 chỉ dò test public → smell để trống

    python3 tn3_collect.py                 gom mọi máy
    python3 tn3_collect.py --allow-missing bỏ qua máy chưa có kết quả (xem trước khi các máy chạy xong)
"""
import argparse          # tham số dòng lệnh
import csv               # ghi CSV
import json              # đọc JSON
import os                # đường dẫn
import re                # nhận ra phương thức @Test không public (giới hạn của tsDetect)
import statistics        # trung vị

import tn3_common as c   # thư viện chung TN3 (đọc jsonl, đường dẫn)
import tn3_lists         # danh sách đóng băng (268 lớp, 536 bộ ARROW)

OUT = os.path.join(c.HERE, "results", "_all")          # thư mục ghi kết quả gom
MODELS = ["gpt6luna", "qwen3coder-or"]
STATE_ORDER = ["NO_OUTPUT", "INVALID_OUTPUT", "MERGE_FAILED", "NOT_APPLICABLE",   # chưa có bộ test để xác minh
               "COMPILE", "UNKNOWN", "DISCOVERY", "RUNTIME", "ASSERTION", "REGRESSION", "VALID"]
METRICS = ["mutation", "test_strength", "line", "branch", "method", "smell_density", "test_method_count", "assertion_count"]
# Dấu hiệu REGRESSION do classloader riêng của EvoSuite (separateClassLoader = true) rò sang test có sẵn
CLASSLOADER_SIGNS = ("ClassCastException", "not assignable", "Failed to load ApplicationContext", "cannot mock",
                     "Could not initialize", "NoClassDefFoundError", "LinkageError")


def rank(state):
    """Vị trí của trạng thái trong thứ tự (để lấy trạng thái 'xa nhất' qua các seed EvoSuite). Lạ thì -1."""
    return STATE_ORDER.index(state) if state in STATE_ORDER else -1


def latest(rows, keyfn):
    """Giữ dòng MỚI NHẤT cho mỗi khóa (chạy lại một việc thì dòng sau thắng)."""
    out = {}
    for r in rows:
        out[keyfn(r)] = r
    return out


_NONPUBLIC_TEST = re.compile(r"@Test\b[^\n]*\n(?:\s*@[^\n]*\n)*\s*(?!public\b)(?:(?:protected|private|static|final|synchronized)\s+)*void\s+\w+\s*\(")


def fix_smell(r):
    """tsDetect 2.2 chỉ dò smell trong phương thức test `public` (kiểu JUnit 4). Bộ có phương thức @Test không public
    (JUnit 5 cho phép, vd. ChatUniTest-GPT) thì tsDetect không dò được → mật độ smell 0 GIẢ. Với bộ đó: để trống
    smell_density (không đo được) và gắn cờ smell_not_measured. Đọc bộ test cuối của arrow trong run_dir."""
    r["smell_not_measured"] = False
    if r.get("final_state") != "VALID" or not r.get("run_dir"):
        return r
    import glob as _glob
    files = (_glob.glob(os.path.join(r["run_dir"], "targets", "*", "block3", "final.java"))
             or _glob.glob(os.path.join(r["run_dir"], "targets", "*", "block2", "candidate.java")))
    if not files and r.get("system") == "chatunitest" and r.get("_file_machine"):  # máy khác không nộp homes/ → đọc suites/
        files = _glob.glob(os.path.join(c.HERE, "results", r["_file_machine"], "suites", "chatunitest",
                                        r["task_id"], r["model"], "*.java"))
    if not files:
        return r
    code = ""
    for p in files:
        with open(p, encoding="utf-8", errors="replace") as f:
            code += f.read() + "\n"
    if _NONPUBLIC_TEST.search(code):                          # có ít nhất một phương thức @Test không public
        r["smell_density_tsdetect"] = r.get("smell_density")  # giữ số tsDetect báo để đối chiếu
        r["smell_density"] = None
        r["smell_not_measured"] = True
    return r


def flags(r, system):
    """Gắn các cờ đặc biệt cho một dòng kết quả."""
    f = {"valid_mutation_0": r.get("final_state") == "VALID" and r.get("mutation") == 0,
         "dep_breaks_baseline": r.get("baseline_check") == "DEP_BREAKS_BASELINE",
         "mutation_not_measured": r.get("final_state") == "VALID" and r.get("mutation") is None,
         "smell_not_measured": bool(r.get("smell_not_measured")),
         "evo_classloader_reg": False}
    if system == "evosuite" and r.get("final_state") == "REGRESSION" and not f["dep_breaks_baseline"]:
        text = json.dumps(r.get("regressions") or [], ensure_ascii=False)      # loại lỗi và thông báo của các test hỏng
        f["evo_classloader_reg"] = any(s in text for s in CLASSLOADER_SIGNS)
    return f


def flat(r, keys):
    """Lấy các trường cần ghi CSV từ một dòng (dict lồng → chuỗi JSON)."""
    out = {}
    for k in keys:
        v = r.get(k)
        out[k] = json.dumps(v, ensure_ascii=False) if isinstance(v, (dict, list)) else v
    return out


def write_csv(path, rows, keys):
    """Ghi danh sách dict ra CSV (UTF-8 có BOM để Excel mở đúng tiếng Việt)."""
    with open(path, "w", encoding="utf-8-sig", newline="") as f:
        w = csv.DictWriter(f, fieldnames=keys, extrasaction="ignore")
        w.writeheader()
        for r in rows:
            w.writerow(r)


def med(vals):
    """Trung vị của các giá trị khác None (không có thì None)."""
    vals = [v for v in vals if v is not None]
    return round(statistics.median(vals), 4) if vals else None


def main():
    ap = argparse.ArgumentParser(description="TN3: gom kết quả 5 máy.")
    ap.add_argument("--allow-missing", action="store_true", help="bỏ qua máy chưa có kết quả")
    a = ap.parse_args()
    cfg = c.load_config()
    classes, arrow_items = tn3_lists.load_frozen()                 # 268 lớp, 536 bộ ARROW (đã kiểm sha256)
    cls = {x["task_id"]: x for x in classes}
    tn1 = tn3_lists.read_tn1()                                     # dòng TN1 PA, để lấy token/thời gian sinh của ARROW
    os.makedirs(OUT, exist_ok=True)
    notes = []                                                     # các dòng ghi vào kiem_tra.md

    # --- 1. Đọc dữ liệu từng máy ---
    raw = {"arrow": [], "evosuite": [], "chatunitest": [], "phat_hien_loi": []}
    commits = {}
    for m in cfg["machines"]:
        root = os.path.join(c.HERE, "results", m)
        if not os.path.isdir(root):
            msg = f"- Máy **{m}**: chưa có thư mục results/{m}/"
            if not a.allow_missing:
                raise SystemExit(msg.replace("**", "") + " (thêm --allow-missing để xem trước)")
            notes.append(msg)
            continue
        for kind in raw:
            for r in c.read_jsonl(os.path.join(root, f"{kind}_runs.jsonl" if kind != "phat_hien_loi" else "phat_hien_loi.jsonl")):
                r["_file_machine"] = m                             # máy nộp file (để kiểm lớp chạy đúng máy)
                raw[kind].append(r)
        lock = c.read_json(os.path.join(root, "tn3.lock.json"), {"invocations": []})
        commits[m] = sorted({i.get("tool_commit") for i in lock["invocations"] if i.get("tool_commit")})

    # --- 2. Kiểm bản tool và máy ---
    all_commits = sorted({x for v in commits.values() for x in v})
    if len(all_commits) > 1:
        notes.append(f"- ⚠ Các máy dùng **nhiều bản tool**: {commits}")
    for kind, rows in raw.items():
        wrong = sorted({(r["task_id"], r["_file_machine"]) for r in rows
                        if r["task_id"] in cls and cls[r["task_id"]]["machine"] != r["_file_machine"]})
        if wrong:
            notes.append(f"- ⚠ {kind}: {len(wrong)} lớp chạy trên máy không được giao (vd. {wrong[:3]})")

    # --- 3. Lấy dòng mới nhất mỗi việc; dòng không DONE coi là chưa xong ---
    arrow = latest(raw["arrow"], lambda r: (r["task_id"], r["model"]))
    evo = latest([r for r in raw["evosuite"] if r.get("setup") == c.EVOSUITE_SETUP],   # chỉ cách dựng hiện tại (v3)
                 lambda r: (r["task_id"], r["config"], r["seed"]))
    cut = latest(raw["chatunitest"], lambda r: (r["task_id"], r["model"]))
    det = latest(raw["phat_hien_loi"], lambda r: (r["task_id"], r["system"], r["variant"]))
    for d in (arrow, evo, cut):                                # mật độ smell giả = 0 do tsDetect bỏ qua test không public
        for r in d.values():
            fix_smell(r)
    not_done = {}
    for name, d in (("arrow", arrow), ("evosuite", evo), ("chatunitest", cut), ("phat_hien_loi", det)):
        bad = [k for k, r in d.items() if r.get("status") != "DONE"]
        not_done[name] = bad
        for k in bad:
            d.pop(k)                                               # chưa xong → không đưa vào bảng
        if bad:
            notes.append(f"- {name}: {len(bad)} việc chưa DONE (INFRA/STOPPED/BAD_CONFIG), vd. {bad[:3]}")

    # --- 4. Đủ chưa (so với danh sách đóng băng) ---
    seeds = cfg["evosuite"]["seeds"]
    exp_arrow = {(x["task_id"], x["model"]) for x in arrow_items}
    exp_evo = {(t, "default", s) for t in cls for s in seeds}
    exp_budget = {(t, "budget", seeds[0]) for t in cls}
    exp_cut = {(t, m) for t in cls for m in cfg["chatunitest"]["models"]}
    for name, exp, have in (("ARROW", exp_arrow, set(arrow)), ("EvoSuite cấu hình chính", exp_evo, set(evo)),
                            ("EvoSuite cùng ngân sách", exp_budget, set(evo)), ("ChatUniTest", exp_cut, set(cut))):
        miss = exp - have
        notes.append(f"- {name}: có {len(exp) - len(miss)}/{len(exp)}" + (f"; thiếu vd. {sorted(miss)[:3]}" if miss else " ✅"))
    git = [t for t in cls if cls[t]["dataset"] == "gitbug-java"]
    have_det = {k[0] for k in det}
    notes.append(f"- Phát hiện lỗi thật: {len(have_det & set(git))}/{len(git)} lớp GitBug-Java có kết quả")

    # --- 5. Bảng dài từng hệ thống (gắn thông tin lớp + cờ) ---
    info_keys = ["task_id", "dataset", "class_fqn", "machine", "build_tool", "jdk", "framework", "vintage",
                 "evosuite_runnable", "chatunitest_applicable"]
    flag_keys = ["valid_mutation_0", "dep_breaks_baseline", "evo_classloader_reg", "mutation_not_measured", "smell_not_measured"]

    def enrich(r, system):
        out = {k: cls[r["task_id"]].get(k) for k in info_keys}   # thông tin lớp từ danh sách đóng băng
        out.update(flat(r, [k for k in r if k not in out and not k.startswith("_")]))
        out.update(flags(r, system))
        return out

    a_rows = []
    for k, r in sorted(arrow.items()):
        t = tn1.get(k)                                             # dòng TN1 của bộ này (token sinh c0)
        r = dict(r, tn1_prompt_tokens=float(t[0]["prompt_tokens"] or 0) if t else None,
                 tn1_completion_tokens=float(t[0]["completion_tokens"] or 0) if t else None)
        a_rows.append(enrich(r, "arrow"))
    e_rows = [enrich(r, "evosuite") for _, r in sorted(evo.items())]
    c_rows = [enrich(r, "chatunitest") for _, r in sorted(cut.items())]
    d_rows = [enrich(r, "phat_hien_loi") for _, r in sorted(det.items())]
    a_keys = info_keys + ["model", "action", "source", "tn1_result", "final_state", "rounds_run", "success_round"] + METRICS + \
             ["tn1_total_s", "repair_seconds", "tokens", "llm_calls", "tn1_prompt_tokens", "tn1_completion_tokens"] + flag_keys
    e_keys = info_keys + ["config", "seed", "search_budget", "final_state", "result", "baseline_check", "lost_count",
                          "added", "listener", "pit_source"] + METRICS + ["gen_seconds", "seconds"] + flag_keys
    c_keys = info_keys + ["model", "final_state", "result", "merge_mode", "runwith_added", "first_verify_result",
                          "baseline_check", "lost_count", "pit_source", "arrow_pit_mutation"] + METRICS + \
             ["cut_llm_calls", "cut_prompt_tokens", "cut_completion_tokens", "cut_llm_seconds", "gen_seconds"] + flag_keys
    d_keys = info_keys + ["system", "variant", "verdict", "state", "primary", "source", "tests_passed", "test_method_count"]
    write_csv(os.path.join(OUT, "arrow.csv"), a_rows, a_keys)
    write_csv(os.path.join(OUT, "evosuite.csv"), e_rows, e_keys)
    write_csv(os.path.join(OUT, "chatunitest.csv"), c_rows, c_keys)
    write_csv(os.path.join(OUT, "phat_hien_loi.csv"), d_rows, d_keys)

    # --- 6. Bảng mỗi lớp một dòng (đầu vào của tn3_analyze.py) ---
    lop = []
    for t, x in sorted(cls.items(), key=lambda kv: (kv[1]["dataset"], kv[0])):
        row = {k: x.get(k) for k in info_keys}
        for m in MODELS:                                           # ARROW, mỗi mô hình
            r = arrow.get((t, m))
            p = f"arrow_{m}_"
            row[p + "state"] = r.get("final_state") if r else "MISSING"
            row[p + "source"] = r.get("source") if r else None
            for k in METRICS:
                row[p + k] = r.get(k) if r and r.get("final_state") == "VALID" else None
            tok = (r or {}).get("tokens") or {}
            t1 = tn1.get((t, m))
            row[p + "prompt_tokens"] = (tok.get("in") or 0) + (float(t1[0]["prompt_tokens"] or 0) if t1 else 0) if r else None
            row[p + "completion_tokens"] = (tok.get("out") or 0) + (float(t1[0]["completion_tokens"] or 0) if t1 else 0) if r else None
            row[p + "llm_calls"] = ((r or {}).get("llm_calls") or 0) + 1 if r else None      # +1 lượt sinh c0 ở TN1
            row[p + "seconds"] = ((r or {}).get("tn1_total_s") or 0) + ((r or {}).get("repair_seconds") or 0) if r else None
            row.update({p + f: flags(r, "arrow")[f] for f in flag_keys} if r else {})
        # EvoSuite cấu hình chính: gộp qua các seed
        rs = [evo[(t, "default", s)] for s in seeds if (t, "default", s) in evo]
        valid = [r for r in rs if r.get("final_state") == "VALID"]
        row["evo_seeds_done"] = len(rs)
        row["evo_seeds_valid"] = len(valid)
        row["evo_state"] = ("VALID" if valid else max((r.get("final_state") for r in rs), key=rank)) if rs else "MISSING"
        row["evo_states"] = ",".join(f"{r['seed']}:{r.get('final_state')}" for r in sorted(rs, key=lambda r: r["seed"]))
        for k in METRICS:
            row["evo_" + k] = med([r.get(k) for r in valid])      # trung vị qua các seed VALID
        row["evo_primary_seed"] = min((r["seed"] for r in valid), default=None)   # seed dùng cho phát hiện lỗi thật
        for f in flag_keys:
            row["evo_any_" + f] = any(flags(r, "evosuite")[f] for r in rs)
        b = evo.get((t, "budget", seeds[0]))                       # cấu hình cùng ngân sách
        row["evob_state"] = b.get("final_state") if b else "MISSING"
        row["evob_budget"] = b.get("search_budget") if b else None
        for k in METRICS:
            row["evob_" + k] = b.get(k) if b and b.get("final_state") == "VALID" else None
        for m in MODELS:                                           # ChatUniTest, mỗi mô hình
            r = cut.get((t, m))
            p = f"cut_{m}_"
            row[p + "state"] = r.get("final_state") if r else ("NOT_APPLICABLE" if not x["chatunitest_applicable"] else "MISSING")
            for k in METRICS:
                row[p + k] = r.get(k) if r and r.get("final_state") == "VALID" else None
            row[p + "prompt_tokens"] = (r or {}).get("cut_prompt_tokens")
            row[p + "completion_tokens"] = (r or {}).get("cut_completion_tokens")
            row[p + "llm_calls"] = (r or {}).get("cut_llm_calls")
            row[p + "seconds"] = (r or {}).get("gen_seconds")
            row.update({p + f: flags(r, "chatunitest")[f] for f in flag_keys} if r else {})
        lop.append(row)
    keys = list(dict.fromkeys(k for r in lop for k in r))         # mọi cột, giữ thứ tự xuất hiện
    write_csv(os.path.join(OUT, "lop.csv"), lop, keys)

    # --- 7. kiem_tra.md ---
    L = ["# TN3: kiểm tra dữ liệu đã gom", "", f"Sinh bởi `tn3_collect.py`, {c.now_iso()}. Bản tool: {all_commits}.", ""] + notes
    cnt = lambda rows, f: sum(1 for r in rows if r.get(f))
    L += ["", "## Cờ đặc biệt (số dòng)", "", "| Hệ thống | VALID mà mutation = 0 | Thư viện thêm làm hỏng test có sẵn | "
          "EvoSuite REGRESSION do classloader | VALID nhưng không đo được mutation | VALID nhưng không đo được smell (test không public) |", "|---|---:|---:|---:|---:|---:|"]
    for name, rows in (("ARROW", a_rows), ("EvoSuite", e_rows), ("ChatUniTest", c_rows)):
        L.append(f"| {name} | " + " | ".join(str(cnt(rows, f)) for f in flag_keys) + " |")
    with open(os.path.join(OUT, "kiem_tra.md"), "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(L) + "\n")
    print("\n".join(L))
    print(f"\nĐã ghi {OUT}/: arrow.csv, evosuite.csv, chatunitest.csv, phat_hien_loi.csv, lop.csv, kiem_tra.md")


if __name__ == "__main__":
    main()
