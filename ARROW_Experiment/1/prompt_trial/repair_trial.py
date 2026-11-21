#!/usr/bin/env python3
"""Thử bộ prompt SỬA test: bản cũ (v2-draft nhúng trong tool) so với bản mới (team-v1, prompts/override/repair).

Chỉ để tinh chỉnh prompt, KHÔNG phải kết quả thực nghiệm. Không dùng lớp pilot hay lớp của dataset-v1.

Dữ liệu: các c0 chưa VALID của lượt 3 (results_luot3/, prompt sinh test team-v1, GPT-6 Luna) trên 10 lớp
ngoài dataset-v1. Mỗi c0 được sửa bằng:
    2 bản prompt (cu, moi) × 2 mô hình sửa (gpt6luna, qwen3coder-or) × 2 cơ chế (fixed_plus, adaptive)
    tối đa 5 vòng, không dừng sớm.
F+ chỉ dùng mức 1 (prompt hệ thống + câu mô tả lỗi); A dùng đủ mức 1–3.

Cách chạy:
    python3 repair_trial.py <thư mục kết quả> [--workers 4] [--limit 2] [--plan "moi2=gpt6luna,qwen3coder-or;cu=qwen3coder-or"]
    (--plan: bản prompt nào chạy với mô hình nào; "cu" = prompt nhúng v2-draft, tên khác = prompts/override hiện tại)
    python3 repair_trial.py <thư mục kết quả> --report        # chỉ in bảng
"""
import argparse
import json
import os
import shutil
import sys
import threading
import time
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))                 # ARROW_Experiment/1/prompt_trial
EXP = os.path.normpath(os.path.join(HERE, "..", ".."))           # ARROW_Experiment
sys.path.insert(0, os.path.join(EXP, "pilot"))
import pilot_run  # noqa: E402  (dùng lại run_chain, và qua nó là tn1_run)
base = pilot_run.base

ARROW = os.path.expanduser("~/.local/share/arrow-dev/build-tn2/arrow")   # bản 203fb8e (có --c0)
C0_DIR = os.path.join(HERE, "results_luot3")
DATASET = os.path.normpath(os.path.join(EXP, "..", "ARROW_dataset"))
NEW_PROMPTS = os.path.normpath(os.path.join(HERE, "..", "prompts", "override"))
VERSIONS = ["cu", "moi"]
MODELS = ["gpt6luna", "qwen3coder-or"]
MECHS = ["fixed_plus", "adaptive"]
ROUNDS = 5


def load_c0():
    tasks = {}
    with open(os.path.join(DATASET, "screen", "screen_results.jsonl"), encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            tasks[r["task_id"]] = r
    c0s = []
    with open(os.path.join(C0_DIR, "trial_runs.jsonl"), encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            if r.get("result") in pilot_run.C0_STATES:
                path = os.path.join(C0_DIR, "c0", f"{r['task_id']}__{r['strategy']}.java")
                if os.path.exists(path):
                    t = tasks[r["task_id"]]
                    c0s.append(({"task_id": t["task_id"], "class_fqn": t["class_fqn"], "repo_url": t["repo_url"],
                                 "checkout_sha": t["checkout_sha"], "dataset": "trial"},
                                {"task_id": t["task_id"], "model": "gpt6luna", "strategy": r["strategy"],
                                 "result": r["result"], "c0_abs": path, "c0_path": os.path.relpath(path, HERE)}))
    c0s.sort(key=lambda x: (x[0]["task_id"], x[1]["strategy"]))
    return c0s


def key(r):
    return (r["task_id"], r["strategy"], r["repair_model"], r["mechanism"], r["version"])


def report(out):
    path = os.path.join(out, "runs.jsonl")
    latest = {}
    for line in open(path, encoding="utf-8"):
        if line.strip():
            r = json.loads(line)
            latest[key(r)] = r
    # token trung bình mỗi vòng (đọc usage.json)
    def tokens(r):
        import glob
        b3 = os.path.join(r.get("run_dir") or "", "targets", r["class_fqn"], "block3")
        ins = []
        for u in glob.glob(os.path.join(b3, "round_*", "usage.json")):
            try:
                ins.append(json.load(open(u, encoding="utf-8")).get("prompt_tokens") or 0)
            except (OSError, ValueError):
                pass
        return ins
    versions = sorted({r["version"] for r in latest.values()}, key=lambda v: (v != "cu", v))
    L = ["| Mô hình sửa | Cơ chế | Bản prompt | Chain | VALID | Tỷ lệ | Vòng thành công TB | Token vào TB mỗi vòng | Lỗi hạ tầng |",
         "|---|---|---|---|---|---|---|---|---|"]
    for m in MODELS:
        for mech in MECHS:
            for v in versions:
                rs = [r for r in latest.values() if r["repair_model"] == m and r["mechanism"] == mech and r["version"] == v]
                if not rs:
                    continue
                done = [r for r in rs if r["status"] == "DONE" and r.get("success_round") != 0]
                ok = [r for r in done if r.get("result") == "VALID"]
                sr = [r["success_round"] for r in ok if r.get("success_round")]
                tk = [t for r in done for t in tokens(r)]
                L.append(f"| {m} | {mech} | {v} | {len(done)} | {len(ok)} | {100 * len(ok) / len(done):.0f}% | "
                         f"{sum(sr) / len(sr):.2f} | {sum(tk) / len(tk):.0f} | {len(rs) - len(done)} |" if done else
                         f"| {m} | {mech} | {v} | 0 | | | | | {len(rs)} |")
    # ghép cặp cũ/mới trên cùng c0, mô hình, cơ chế
    pairs = defaultdict(dict)
    for r in latest.values():
        if r["status"] == "DONE" and r.get("success_round") != 0:
            pairs[(r["task_id"], r["strategy"], r["repair_model"], r["mechanism"])][r["version"]] = r.get("result") == "VALID"
    for new in [v for v in versions if v != "cu"]:
        L += ["", f"| Mô hình sửa | Cặp | Chỉ {new} VALID | Chỉ cu VALID | Cả hai | Cả hai không |", "|---|---|---|---|---|---|"]
        for m in MODELS + ["(cả hai)"]:
            ps = [p for k, p in pairs.items() if "cu" in p and new in p and (m == "(cả hai)" or k[2] == m)]
            if ps:
                L.append(f"| {m} | {len(ps)} | {sum(p[new] and not p['cu'] for p in ps)} | {sum(p['cu'] and not p[new] for p in ps)} | "
                         f"{sum(p['cu'] and p[new] for p in ps)} | {sum(not p['cu'] and not p[new] for p in ps)} |")
    text = "\n".join(L)
    open(os.path.join(out, "bang.md"), "w", encoding="utf-8").write(text + "\n")
    print(text)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("out")
    ap.add_argument("--workers", type=int, default=4)
    ap.add_argument("--limit", type=int)
    ap.add_argument("--report", action="store_true")
    ap.add_argument("--plan", default="cu=gpt6luna,qwen3coder-or;moi=gpt6luna,qwen3coder-or")
    args = ap.parse_args()
    out = os.path.abspath(args.out)
    if args.report:
        return report(out)
    os.makedirs(os.path.join(out, "logs"), exist_ok=True)
    empty = os.path.join(out, "prompt_cu_rong")        # thư mục ghi đè rỗng → tool dùng prompt sửa nhúng (v2-draft)
    os.makedirs(empty, exist_ok=True)
    plan = {v.strip(): [m.strip() for m in ms.split(",")] for v, ms in (x.split("=") for x in args.plan.split(";"))}
    override = {v: (empty if v == "cu" else NEW_PROMPTS) for v in plan}
    c0s = load_c0()[: args.limit] if args.limit else load_c0()
    runs = os.path.join(out, "runs.jsonl")
    done = set()
    if os.path.exists(runs):
        for line in open(runs, encoding="utf-8"):
            if line.strip():
                r = json.loads(line)
                if r["status"] == "DONE":
                    done.add(key(r))
    # Một job = một c0 với một mô hình sửa: 2 bản × 2 cơ chế, chạy liền trong một home (dùng lại bản clone).
    jobs = []
    for task, c0 in c0s:
        for m in MODELS:
            todo = [(v, mech) for v in plan if m in plan[v] for mech in MECHS
                    if (task["task_id"], c0["strategy"], m, mech, v) not in done]
            if todo:
                jobs.append((task, c0, m, todo))
    total = sum(len(j[3]) for j in jobs)
    print(f"{len(c0s)} c0 · còn {total} chain · {args.workers} worker", flush=True)
    homes, lock, cnt = [], threading.Lock(), {"n": 0, "made": 0}

    def borrow():
        with lock:
            if homes:
                return homes.pop()
            cnt["made"] += 1
            n = cnt["made"]
        h = os.path.join(out, "homes", f"w{n}")
        base.prepare_home(os.path.expanduser("~/.arrow"), h)
        pilot_run.arrow_cfg(ARROW, h, "repair.early_stop_p", "null")   # không dừng sớm
        return h

    def job(task, c0, m, todo, home):
        try:
            for v, mech in todo:
                try:
                    pilot_run.arrow_cfg(ARROW, home, "prompts.override_dir", override[v])
                    pilot_run.arrow_cfg(ARROW, home, "repair.mechanism", mech)
                    rec = pilot_run.run_chain(ARROW, task, c0, mech, home, os.path.join(out, "logs"),
                                              rounds=ROUNDS, repair_model=m, tag=v)
                except Exception as e:
                    rec = dict(task, model=c0["model"], strategy=c0["strategy"], mechanism=mech, repair_model=m,
                               status="INFRA", result="SCRIPT_EXCEPTION", detail=repr(e))
                rec["version"] = v
                with lock:
                    with open(runs, "a", encoding="utf-8") as f:
                        f.write(json.dumps(rec, ensure_ascii=False) + "\n")
                    cnt["n"] += 1
                    print(f"[{cnt['n']}/{total}] {rec['status']:10s} {str(rec.get('result')):10s} "
                          f"vòng {rec.get('success_round')}/{rec.get('rounds_run')} {rec.get('seconds', 0):>6}s "
                          f"{v:3s} {m:13s} {mech:10s} {c0['strategy']:24s} {task['class_fqn'].rsplit('.', 1)[-1]}", flush=True)
        finally:
            base.clean_home(home)
            with lock:
                homes.append(home)

    pending, running = list(jobs), []
    try:
        while pending or running:
            running = [t for t in running if t.is_alive()]
            while pending and len(running) < args.workers:
                task, c0, m, todo = pending.pop(0)
                t = threading.Thread(target=job, args=(task, c0, m, todo, borrow()), daemon=True)
                t.start()
                running.append(t)
            time.sleep(2)
    except KeyboardInterrupt:
        with base.PROCS_LOCK:
            for p in list(base.PROCS):
                base.kill_tree(p)
        raise SystemExit("Đã dừng. Chạy lại đúng lệnh để làm tiếp.")
    report(out)


if __name__ == "__main__":
    main()
