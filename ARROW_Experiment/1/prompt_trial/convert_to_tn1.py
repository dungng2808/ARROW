# Chuyển trial_runs.jsonl (run_trial.py) sang dạng tn1_runs.jsonl (tn1_run.py) để chạy collect/analyze.
import json, os, sys
src_dir, out_dir, machine = sys.argv[1], sys.argv[2], sys.argv[3]
screen = {}
for l in open("/Users/dungng/FPT/Ki9/SET490/DoAnARROW/ARROW/ARROW_dataset/screen/screen_results.jsonl"):
    r = json.loads(l); screen[r["task_id"]] = r
os.makedirs(out_dir, exist_ok=True)
with open(os.path.join(out_dir, "tn1_runs.jsonl"), "w") as f:
    for l in open(os.path.join(src_dir, "trial_runs.jsonl")):
        r = json.loads(l); s = screen[r["task_id"]]
        home = os.path.join(src_dir, "homes", r["strategy"])
        f.write(json.dumps({
            "dataset": s["dataset"], "task_id": r["task_id"], "repo_url": s["repo_url"],
            "checkout_sha": s["checkout_sha"], "class_fqn": r["class_fqn"], "model": "gpt6luna",
            "strategy": r["strategy"], "machine": machine, "worker": r["strategy"], "tool_commit": "d0f2b5c",
            "run_id": r.get("run_id"), "run_dir": os.path.join(home, "runs", r["run_id"]) if r.get("run_id") else None,
            "status": "DONE" if r["result"] not in ("NO_JSON",) else "INFRA", "result": r["result"],
            "exit_code": r["exit_code"], "seconds": r["seconds"]}) + "\n")
print("ok", out_dir)
