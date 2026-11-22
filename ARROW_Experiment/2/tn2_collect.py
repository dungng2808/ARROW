#!/usr/bin/env python3
"""Gom kết quả TN2 của một máy từ homes/ (không lên Git) ra results/<máy>/report/, rồi kiểm trước khi nộp.

Chạy trên máy đã chạy tn2_run.py, khi tn2_run.py đã xong (trong ARROW_Experiment/2):
    python3 tn2_collect.py --machine <máy>            # in ĐẠT thì được nộp
    python3 tn2_collect.py --machine <máy> --smoke    # xem kết quả chạy khói (không kiểm đủ chain)

Đầu ra (results/<máy>/report/):
    tn2_chains.csv   mỗi chain một dòng: candidate, mô hình sinh, mô hình sửa, nhánh, lần lặp, kết quả,
                     vòng thành công, số vòng đã chạy, thời gian từng khối, mức prompt cao nhất, prompt đã dùng
    tn2_rounds.csv   mỗi vòng sửa một dòng: trạng thái, mức prompt, mã trùng, quay lui (rollback), sửa từ bản
                     của vòng nào (base_round), checkpoint, mức sau vòng, token, thời gian gọi mô hình và từng bước build
    final_code/      mã cuối của mỗi chain (<candidate>__<nhánh>__<mô hình sửa>__rep<k>.java)

Kiểm (in ĐẠT / KHÔNG ĐẠT):
    - đủ 24 chain cho mỗi candidate của phần máy này, mọi chain DONE (không INFRA, BAD_CONFIG, STOPPED)
    - một bản tool (203fb8e), N = 10, prompt sửa đúng bộ team-v1 v3 đã chốt
    - chain không thành công đã chạy đủ 10 vòng; mỗi chain có dữ liệu từng vòng
Khi homes/ của một chain không còn (ví dụ gom lại lần hai sau khi đã xóa homes), dòng cũ trong report/ được giữ.
"""
import argparse
import csv
import glob
import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import tn2_run as run   # noqa: E402  (cấu hình đã chốt: N_ROUNDS, REPS, MECHANISMS, REPAIR_MODELS, REPAIR_COMMIT)

try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

# Prompt sửa team-v1 v3 (chốt 21/11/2026, commit 59ba868): 12 ký tự đầu của sha256
PROMPT_V3 = {"repair.system": "cf5e5868c02d", "repair.user.level1": "f085fe3b62aa",
             "repair.user.level2": "8150e6a250a7", "repair.user.level3": "b45dc39ad007"}
KEY_COLS = ["task_id", "model", "strategy", "repair_model", "mechanism", "rep"]
DECISION_COLS = ["level", "requested_level", "base_round", "duplicate_code", "duplicate_of_round", "invalid_output",
                 "rollback", "repeated_signature", "improved", "level_after", "checkpoint_round", "no_improve",
                 "error_signature", "code_hash", "tests_passed", "assertion_count", "llm_retries"]


def read_json(path):
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except (OSError, json.JSONDecodeError):
        return None


def read_csv(path):
    if not os.path.exists(path):
        return []
    with open(path, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def write_csv(path, rows):
    keys = []
    for r in rows:
        keys += [k for k in r if k not in keys]
    with open(path, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=keys)
        w.writeheader()
        for r in rows:
            w.writerow({k: r.get(k, "") for k in keys})


def step_seconds(verify):
    out = {}
    for st in (verify or {}).get("steps") or []:
        k = f"step_{st.get('step')}_s"
        out[k] = round(out.get(k, 0) + (st.get("seconds") or 0), 3)
    return out


def kstr(row):
    return tuple(str(row.get(k)) for k in KEY_COLS)


def collect(rec, fc_dir):
    """Một chain → (dòng chain, các dòng vòng) đọc từ homes/; None nếu homes/ không còn."""
    b3 = os.path.join(rec.get("run_dir") or "", "targets", rec.get("class_fqn") or "", "block3")
    if not os.path.isdir(b3):
        return None
    bs = rec.get("block_seconds") or {}
    vc0 = read_json(os.path.join(b3, "verify_c0.json")) or {}
    final = read_json(os.path.join(b3, "final.json")) or {}
    row = {k: rec.get(k) for k in KEY_COLS}
    row.update(candidate_id=rec.get("candidate_id"), dataset=rec.get("dataset"), class_fqn=rec.get("class_fqn"),
               same_model=rec.get("same_model"), part=rec.get("part"), machine=rec.get("machine"),
               worker=rec.get("worker"), tool_commit=rec.get("tool_commit"), status=rec.get("status"),
               result=rec.get("result"), c0_previous_state=rec.get("c0_previous_state"),
               c0_state=final.get("c0_state", rec.get("c0_state")),
               success_round=final.get("success_round", rec.get("success_round")),
               rounds_run=final.get("rounds_run", rec.get("rounds_run")),
               stop_reason=final.get("stop_reason", rec.get("stop_reason")),
               seconds=rec.get("seconds"), block1_s=bs.get("block1"), block3_s=bs.get("block3"), total_s=bs.get("total"),
               c0_verify_s=vc0.get("build_seconds"), run_id=rec.get("run_id"),
               **{"c0_" + k: v for k, v in step_seconds(vc0).items()})
    name = f"{rec['candidate_id']}__{rec['mechanism']}__{rec['repair_model']}__rep{rec['rep']}.java"
    if os.path.exists(os.path.join(b3, "final.java")):
        shutil.copyfile(os.path.join(b3, "final.java"), os.path.join(fc_dir, name))
    rounds, tmpl, levels, rollbacks = [], set(), [], 0
    for rd in sorted(glob.glob(os.path.join(b3, "round_*")), key=lambda p: int(p.rsplit("_", 1)[1])):
        dec = read_json(os.path.join(rd, "decision.json")) or {}
        use = read_json(os.path.join(rd, "usage.json")) or {}
        ver = read_json(os.path.join(rd, "verify.json")) or {}
        prm = read_json(os.path.join(rd, "prompt.json")) or {}
        for t in prm.get("templates") or []:
            tmpl.add(f"{t.get('id')}@{t.get('version')}:{str(t.get('sha256'))[:12]}")
        if dec.get("level") is not None:
            levels.append(dec["level"])
        rollbacks += 1 if dec.get("rollback") else 0
        llm_s, build_s = use.get("latency_seconds"), ver.get("build_seconds", dec.get("build_seconds"))
        rounds.append({**{k: rec.get(k) for k in KEY_COLS}, "candidate_id": rec.get("candidate_id"),
                       "dataset": rec.get("dataset"), "machine": rec.get("machine"),
                       "round": int(rd.rsplit("_", 1)[1]), "state": ver.get("state", dec.get("state")),
                       **{c: dec.get(c) for c in DECISION_COLS},
                       "llm_seconds": llm_s, "build_seconds": build_s,
                       "round_seconds": (llm_s or 0) + (build_s or 0) if (llm_s or build_s) else None,
                       "prompt_tokens": use.get("prompt_tokens"), "completion_tokens": use.get("completion_tokens"),
                       "finish_reason": use.get("finish_reason"), **step_seconds(ver)})
    row.update(templates=";".join(sorted(tmpl)), level_max=max(levels) if levels else "",
               rollbacks=rollbacks, n_rounds_logged=len(rounds))
    return row, rounds


def main():
    ap = argparse.ArgumentParser(description="Gom và kiểm kết quả TN2 của một máy.")
    ap.add_argument("--machine", required=True)
    ap.add_argument("--smoke", action="store_true", help="kết quả chạy khói (results/<máy>-smoke)")
    args = ap.parse_args()
    root = run.out_root(args.machine, args.smoke)
    rep_dir, fc_dir = os.path.join(root, "report"), os.path.join(root, "report", "final_code")
    os.makedirs(fc_dir, exist_ok=True)
    latest = run.read_latest(os.path.join(root, "tn2_runs.jsonl"))
    if not latest:
        raise SystemExit(f"Không có {root}/tn2_runs.jsonl.")

    old_chains = {kstr(r): r for r in read_csv(os.path.join(rep_dir, "tn2_chains.csv"))}
    old_rounds = {}
    for r in read_csv(os.path.join(rep_dir, "tn2_rounds.csv")):
        old_rounds.setdefault(kstr(r), []).append(r)

    chains, rounds, kept = [], [], 0
    for key in sorted(latest, key=lambda k: tuple(str(x) for x in k)):
        rec = latest[key]
        got = collect(rec, fc_dir) if rec.get("status") == "DONE" else None
        if got is None and kstr(rec) in old_chains:          # homes/ đã xóa: giữ dòng đã gom trước đó
            chains.append(old_chains[kstr(rec)])
            rounds += old_rounds.get(kstr(rec), [])
            kept += 1
            continue
        if got is None:
            row = {k: rec.get(k) for k in KEY_COLS}
            row.update(candidate_id=rec.get("candidate_id"), status=rec.get("status"), result=rec.get("result"),
                       machine=rec.get("machine"), n_rounds_logged=0)
            chains.append(row)
            continue
        chains.append(got[0])
        rounds += got[1]
    write_csv(os.path.join(rep_dir, "tn2_chains.csv"), chains)
    write_csv(os.path.join(rep_dir, "tn2_rounds.csv"), rounds)

    # --- kiểm ---
    errs = []
    def num(v):
        try:
            return int(float(v))
        except (TypeError, ValueError):
            return None
    if not args.smoke:
        part = {str(c.get("part")) for c in chains} - {"None", ""}
        if len(part) != 1:
            errs.append(f"Nhiều phần trong cùng một máy: {sorted(part)} (chỉ được khi Dung bảo)")
        expected = set()
        for p in part:
            cands, _ = run.load_subset(p)
            for c in cands:
                for rep in range(1, run.REPS + 1):
                    for rm in run.REPAIR_MODELS:
                        for mech in run.MECHANISMS:
                            expected.add((c["task_id"], c["model"], c["strategy"], rm, mech, str(rep)))
        have = {(k[0], k[1], k[2], k[3], k[4], str(k[5])) for k in latest}
        if expected - have:
            errs.append(f"Thiếu {len(expected - have)} chain, ví dụ {sorted(expected - have)[:2]}")
    not_done = [c for c in chains if c.get("status") != "DONE"]
    if not_done:
        errs.append(f"{len(not_done)} chain chưa DONE (INFRA/BAD_CONFIG/STOPPED): chạy lại tn2_run.py (--retry-infra)")
    commits = {str(c.get("tool_commit"))[:7] for c in chains if c.get("tool_commit")}
    if commits - {run.REPAIR_COMMIT}:
        errs.append(f"Bản tool khác {run.REPAIR_COMMIT}: {sorted(commits)}")
    bad_prompt = set()
    for c in chains:
        for t in filter(None, str(c.get("templates") or "").split(";")):
            tid, sha = t.split("@")[0], t.rsplit(":", 1)[-1]
            if tid in PROMPT_V3 and PROMPT_V3[tid] != sha:
                bad_prompt.add(t)
    if bad_prompt:
        errs.append(f"Prompt sửa khác bản v3 đã chốt: {sorted(bad_prompt)}")
    short = [c for c in chains if c.get("status") == "DONE" and num(c.get("success_round")) is None
             and num(c.get("rounds_run")) != run.N_ROUNDS]
    if short:
        errs.append(f"{len(short)} chain không thành công nhưng không chạy đủ {run.N_ROUNDS} vòng")
    no_rounds = [c for c in chains if c.get("status") == "DONE" and num(c.get("n_rounds_logged")) in (None, 0)
                 and num(c.get("success_round")) != 0]
    if no_rounds:
        errs.append(f"{len(no_rounds)} chain không có dữ liệu từng vòng (homes/ đã xóa trước khi gom?)")

    ok_chains = [c for c in chains if c.get("status") == "DONE"]
    succ = sum(1 for c in ok_chains if (num(c.get("success_round")) or 0) >= 1)
    print(f"Máy {args.machine}{' (chạy khói)' if args.smoke else ''}: {len(chains)} chain "
          f"({len(ok_chains)} DONE; giữ dòng cũ {kept}); {len(rounds)} vòng sửa; mã cuối trong report/final_code/")
    print(f"Sửa thành công (mọi nhánh, mọi lần): {succ}/{len(ok_chains)}")
    for e in errs:
        print("[X]", e)
    print("ĐẠT" if not errs else "KHÔNG ĐẠT")
    sys.exit(0 if not errs else 1)


if __name__ == "__main__":
    main()
