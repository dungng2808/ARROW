#!/usr/bin/env python3
"""Chạy Thực nghiệm 2 (RQ1b, RQ2) trên phần của một máy. Thiết kế: Sua/ban_phan_bien/cac_thuc_nghiem.md, mục TN2.

Mỗi candidate trong tn2_subset.jsonl (đã đóng băng) được sửa bằng:
    2 mô hình sửa (gpt6luna, qwen3coder-or)  ×  4 nhánh (F+, F−, F2, A)  ×  3 lần lặp  = 24 chain
Mọi chain bắt đầu từ ĐÚNG file c0 của TN1 (kiểm sha256), tối đa N = 10 vòng, không dừng sớm.
Thứ tự: chạy hết lần 1 của mọi candidate trước (lần đo chính), rồi lần 2, rồi lần 3. Trong một candidate,
8 chain (2 mô hình × 4 nhánh) chạy liền nhau trên cùng một worker (xen kẽ theo candidate).

Cách chạy (trong ARROW_Experiment/2):
    macOS:   python3 tn2_run.py --machine dung --workers 6 --arrow ~/.local/share/arrow-dev/build-tn2/arrow
    Windows: python tn2_run.py --machine quang --workers 4 --arrow C:\\tools\\arrow-203\\arrow.exe
Chạy khói:  thêm --limit 1 (1 candidate, chỉ lần 1 = 8 chain) → results/<máy>-smoke/
Bị ngắt / hết tiền: chạy lại đúng lệnh cũ, script bỏ qua chain đã xong.
Lỗi hạ tầng: --retry-infra chạy lại các chain chưa DONE.
Chạy lại một chain bất kỳ và ghi đè hoàn toàn: --redo <candidate_id>__<nhánh>__<mô hình sửa>__rep<k> (lặp lại được)
    Xóa sạch kết quả cũ của chain (dòng trong tn2_runs.jsonl, thư mục run trong homes/, logs/<chain>.*,
    report/final_code/<chain>.java, dòng trong report/tn2_chains.csv và tn2_rounds.csv), chạy lại chỉ chain đó,
    rồi ghi kết quả mới vào report/ (không cần chạy tn2_collect.py cả máy). Kèm --limit thì làm trên results/<máy>-smoke.
Đổi số worker khi đang chạy: ghi số mới vào results/<máy>/workers.txt.
Chạy giúp phần của máy khác (chỉ khi Dung bảo): --part <máy kia>; kết quả vẫn ghi vào results/<máy mình>/.

Kết quả: results/<máy>/tn2_runs.jsonl (mỗi chain một dòng), logs/, tn2.lock.json.
Xong thì chạy: python3 tn2_collect.py --machine <máy>
"""
import argparse
import csv
import glob
import hashlib
import json
import os
import platform
import shutil
import sys
import threading
import time

HERE = os.path.dirname(os.path.abspath(__file__))                     # ARROW_Experiment/2
TN1_DIR = os.path.normpath(os.path.join(HERE, "..", "1"))
PILOT_DIR = os.path.normpath(os.path.join(HERE, "..", "pilot"))
sys.path.insert(0, TN1_DIR)
sys.path.insert(0, PILOT_DIR)
import tn1_run as base     # kiểm dataset, mô hình, ARROW_HOME, dừng khi hết tiền  # noqa: E402
import pilot_run as pr     # run_chain: chạy một chain arrow gen --c0 và kiểm run.json  # noqa: E402

try:
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

# ---------------------------------------------------------------------------
# CẤU HÌNH ĐÃ CHỐT (22/11/2026) — không sửa sau khi bắt đầu chạy
# ---------------------------------------------------------------------------
N_ROUNDS = 10                                        # N từ pilot (quy tắc B, 1%)
REPS = 3                                             # lặp 3 lần; lần 1 là lần đo chính
MECHANISMS = pr.MECHANISMS                           # fixed_plus, fixed_minus, fixed_l2, adaptive
REPAIR_MODELS = ["gpt6luna", "qwen3coder-or"]        # mỗi candidate sửa bằng cả hai mô hình
REPAIR_COMMIT = pr.REPAIR_COMMIT                     # 203fb8e (giống pilot)
SUBSET = os.path.join(HERE, "tn2_subset.jsonl")
REPL = os.path.join(HERE, "tn2_thay_the.jsonl")          # candidate thay thế (nếu có), xem tn2_subset.py


def chain_key(rec):
    return (rec["task_id"], rec["model"], rec["strategy"], rec["repair_model"], rec["mechanism"], rec["rep"])


def read_latest(path):
    latest = {}
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    rec = json.loads(line)
                    latest[chain_key(rec)] = rec
    return latest


def load_subset(part):
    """tn2_subset.jsonl (kiểm mã băm đã đóng băng và mã băm từng c0) → các candidate của phần `part`."""
    with open(SUBSET + ".sha256", encoding="utf-8") as f:
        frozen = f.read().split()[0]
    if base.sha256_file(SUBSET) != frozen:
        raise SystemExit("tn2_subset.jsonl khác bản đã đóng băng (sha256). Chạy git pull / git checkout lại file. Dừng.")
    rows = []
    with open(SUBSET, encoding="utf-8") as f:
        for line in f:
            if line.strip():
                rows.append(json.loads(line))
    # Candidate thay thế (tn2_subset.py --thay): thêm vào; candidate bị thay vẫn giữ để dữ liệu đã chạy không mất.
    if os.path.exists(REPL):
        with open(REPL + ".sha256", encoding="utf-8") as f:
            if base.sha256_file(REPL) != f.read().split()[0]:
                raise SystemExit("tn2_thay_the.jsonl khác bản đã commit (sha256). Chạy git pull / git checkout lại file. Dừng.")
        with open(REPL, encoding="utf-8") as f:
            rows += [json.loads(line) for line in f if line.strip()]
    parts = sorted({r["machine"] for r in rows})
    if part not in parts:
        raise SystemExit(f"Không có phần '{part}' trong tập con. Các phần: {parts}")
    mine = [r for r in rows if r["machine"] == part]
    for c in mine:
        c["c0_abs"] = os.path.normpath(os.path.join(TN1_DIR, c["c0_path"]))
        c["result"] = c["c0_result"]                  # run_chain dùng để truyền --c0-state
        if not os.path.exists(c["c0_abs"]):
            raise SystemExit(f"Thiếu file c0: {c['c0_abs']} (git pull?)")
        if base.sha256_file(c["c0_abs"]) != c["c0_sha256"]:
            raise SystemExit(f"File c0 bị đổi so với lúc đóng băng: {c['c0_abs']}. Dừng.")
    return mine, len(rows)


def out_root(machine, smoke):
    return os.path.join(HERE, "results", machine + ("-smoke" if smoke else ""))


def default_homes(machine, smoke):
    """Windows: ARROW_HOME của worker ở gốc ổ đĩa cho đường dẫn ngắn (giới hạn 260 ký tự)."""
    name = f"tn2-{machine}{'-smoke' if smoke else ''}"
    if IS_WINDOWS:
        return os.path.join(os.environ.get("SystemDrive", "C:") + os.sep, "arrow-homes", name)
    return os.path.join(out_root(machine, smoke), "homes")


IS_WINDOWS = base.IS_WINDOWS


def chain_name(rec):
    """Tên chain như trong logs/ và report/final_code/: <candidate_id>__<nhánh>__<mô hình sửa>__rep<k>."""
    return f"{rec['candidate_id']}__{rec['mechanism']}__{rec['repair_model']}__rep{rec['rep']}"


def parse_chain(name, cands):
    """--redo <chain> → (candidate, mô hình sửa, nhánh, lần); dừng nếu tên sai hoặc không thuộc phần này."""
    parts = name.strip().rsplit("__", 3)
    if len(parts) != 4 or not parts[3].startswith("rep") or not parts[3][3:].isdigit():
        raise SystemExit(f"--redo '{name}': cần dạng <candidate_id>__<nhánh>__<mô hình sửa>__rep<k>")
    cid, mech, rm, rep = parts[0], parts[1], parts[2], int(parts[3][3:])
    by_id = {c["candidate_id"]: c for c in cands}
    if cid not in by_id:
        raise SystemExit(f"--redo '{name}': candidate {cid} không thuộc phần này (hoặc ngoài --limit)")
    if mech not in MECHANISMS or rm not in REPAIR_MODELS or not 1 <= rep <= REPS:
        raise SystemExit(f"--redo '{name}': nhánh phải thuộc {MECHANISMS}, mô hình sửa {REPAIR_MODELS}, lần 1–{REPS}")
    return by_id[cid], rm, mech, rep


def redo_remove(root, runs_path, keys):
    """XÓA SẠCH kết quả cũ của các chain `keys` (chain_key) trước khi chạy lại (--redo):
      - mọi dòng của chain trong tn2_runs.jsonl (kể cả dòng INFRA), ghi file mới qua file tạm;
      - thư mục run cũ (run_dir của các dòng đó, trong homes/);
      - logs/<chain>.err, logs/<chain>.json, report/final_code/<chain>.java;
      - dòng của chain trong report/tn2_chains.csv và report/tn2_rounds.csv.
    Trả về số dòng đã xóa khỏi tn2_runs.jsonl."""
    rows = []
    if os.path.exists(runs_path):
        with open(runs_path, encoding="utf-8") as f:
            rows = [json.loads(line) for line in f if line.strip()]
    gone = [r for r in rows if chain_key(r) in keys]
    if gone:
        tmp = runs_path + ".tmp"
        with open(tmp, "w", encoding="utf-8") as f:
            for r in rows:
                if chain_key(r) not in keys:
                    f.write(json.dumps(r, ensure_ascii=False) + "\n")
        os.replace(tmp, runs_path)
    names = {chain_name(r) for r in gone}
    for r in gone:
        if r.get("run_dir") and os.path.isdir(r["run_dir"]):
            shutil.rmtree(r["run_dir"], ignore_errors=True)
    for n in names:
        for pat in (os.path.join(root, "logs", n + ".*"), os.path.join(root, "report", "final_code", n + ".java")):
            for path in glob.glob(pat):
                os.remove(path)
    rep_keys = {tuple(str(x) for x in k) for k in keys}
    for fn in ("tn2_chains.csv", "tn2_rounds.csv"):
        path = os.path.join(root, "report", fn)
        if not os.path.exists(path):
            continue
        with open(path, encoding="utf-8", newline="") as f:
            rd = csv.DictReader(f)
            cols, keep = rd.fieldnames, [row for row in rd if tuple(row.get(k) for k in
                                         ("task_id", "model", "strategy", "repair_model", "mechanism", "rep")) not in rep_keys]
        with open(path, "w", encoding="utf-8", newline="") as f:
            w = csv.DictWriter(f, fieldnames=cols)
            w.writeheader()
            w.writerows(keep)
    return len(gone)


def redo_report(root, runs_path, keys):
    """Sau khi chạy lại: gom riêng các chain `keys` từ homes/ (như tn2_collect.py) và nối vào report/."""
    import tn2_collect as col                         # nhập ở đây để tránh vòng lặp import
    rep_dir, fc_dir = os.path.join(root, "report"), os.path.join(root, "report", "final_code")
    os.makedirs(fc_dir, exist_ok=True)
    latest = read_latest(runs_path)
    new_chains, new_rounds = [], []
    for k in sorted(keys, key=lambda k: tuple(str(x) for x in k)):
        rec = latest.get(k)
        if rec is None:
            continue
        got = col.collect(rec, fc_dir) if rec.get("status") == "DONE" else None
        if got is None:
            row = {x: rec.get(x) for x in col.KEY_COLS}
            row.update(candidate_id=rec.get("candidate_id"), status=rec.get("status"), result=rec.get("result"),
                       machine=rec.get("machine"), n_rounds_logged=0)
            new_chains.append(row)
        else:
            new_chains.append(got[0])
            new_rounds += got[1]
    for fn, add in (("tn2_chains.csv", new_chains), ("tn2_rounds.csv", new_rounds)):
        path = os.path.join(rep_dir, fn)
        col.write_csv(path, col.read_csv(path) + add)
    return len(new_chains), len(new_rounds)


def main():
    ap = argparse.ArgumentParser(description="Chạy TN2 (RQ1b, RQ2) trên phần của một máy.")
    ap.add_argument("--machine", required=True, help="tên máy: dung / chinh / kieuanh / quang / han")
    ap.add_argument("--part", help="phần tập con cần chạy (mặc định = --machine; chỉ đổi khi Dung bảo)")
    ap.add_argument("--workers", type=int, default=4, help="số candidate chạy cùng lúc (mặc định 4)")
    ap.add_argument("--limit", type=int, help="chạy khói: N candidate đầu, chỉ lần 1 → results/<máy>-smoke")
    ap.add_argument("--retry-infra", action="store_true", help="chạy lại các chain chưa DONE (lỗi hạ tầng)")
    ap.add_argument("--redo", action="append", metavar="CHAIN",
                    help="CHẠY LẠI chain này và XÓA SẠCH kết quả cũ (jsonl, homes, logs, report); "
                         "dạng <candidate_id>__<nhánh>__<mô hình sửa>__rep<k>; lặp lại được")
    ap.add_argument("--base-home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")),
                    help="ARROW_HOME đã cấu hình mô hình, JDK, prompt (mặc định ~/.arrow)")
    ap.add_argument("--arrow", default="arrow", help=f"lệnh arrow bản {REPAIR_COMMIT}")
    ap.add_argument("--homes-dir", help="nơi đặt ARROW_HOME của các worker; mặc định Windows: C:\\arrow-homes\\tn2-<máy>")
    args = ap.parse_args()

    smoke = args.limit is not None
    part = args.part or args.machine
    root = out_root(args.machine, smoke)
    log_dir = os.path.join(root, "logs")
    os.makedirs(log_dir, exist_ok=True)
    stale = os.path.join(root, "DA_DUNG.txt")
    if os.path.exists(stale):                         # báo dừng của lần trước: lần này chạy lại nên xóa
        os.remove(stale)
    homes_root = args.homes_dir or default_homes(args.machine, smoke)
    runs_path = os.path.join(root, "tn2_runs.jsonl")
    base.keep_awake()

    # --- kiểm bản tool, tập con, dataset, mô hình, prompt ---
    version, commit = base.arrow_version(args.arrow)
    if not commit.startswith(REPAIR_COMMIT):
        raise SystemExit(f"Bản tool là '{version}', cần {REPAIR_COMMIT}. Chỉ đường bằng --arrow.")
    cands, n_all = load_subset(part)
    checksums = base.check_manifests(base.MANIFESTS["eval"])
    base_cfg = base.load_config(args.base_home)
    model_info = base.check_models(base_cfg, REPAIR_MODELS)
    override = (base_cfg.get("prompts") or {}).get("override_dir")
    same = lambda a, b: os.path.normcase(os.path.realpath(a)) == os.path.normcase(os.path.realpath(b))
    if not override or not same(override, base.PROMPT_DIR):
        raise SystemExit(f"ARROW_HOME gốc chưa dùng bộ prompt của nhóm. Chạy:\n"
                         f"  arrow config set prompts.override_dir \"{base.PROMPT_DIR}\"")
    prompt_sha = {}
    for r, _, files in os.walk(base.PROMPT_DIR):
        for name in sorted(files):
            if name.endswith(".md"):
                p = os.path.join(r, name)
                prompt_sha[os.path.relpath(p, base.PROMPT_DIR).replace(os.sep, "/")] = base.sha256_file(p)

    lock_path = os.path.join(root, "tn2.lock.json")
    lock = pr.read_json(lock_path) or {"invocations": []}
    old_commits = {inv["tool_commit"] for inv in lock["invocations"]}
    if old_commits and commit not in old_commits:
        raise SystemExit(f"Các lần chạy trước dùng tool {sorted(old_commits)}, lần này {commit}. Không trộn hai bản.")
    old_prompts = [inv["prompt_sha256"] for inv in lock["invocations"]]
    if old_prompts and prompt_sha != old_prompts[-1]:
        raise SystemExit("Bộ prompt (prompts/override) khác các lần chạy trước. Prompt đã chốt, không đổi giữa chừng.")
    lock["invocations"].append({
        "started_at": base.now_iso(), "tool_version": version, "tool_commit": commit,
        "machine": args.machine, "part": part, "os": platform.platform(), "cpu_count": os.cpu_count(),
        "models": model_info, "repair_models": REPAIR_MODELS, "mechanisms": MECHANISMS, "rounds": N_ROUNDS,
        "reps": REPS, "early_stop_p": None, "subset_sha256": base.sha256_file(SUBSET),
        "manifest_sha256": checksums, "prompt_override_dir": base.PROMPT_DIR, "prompt_sha256": prompt_sha,
        "verification": base_cfg.get("verification", {}), "workers_start": args.workers, "argv": sys.argv,
    })
    base.write_json(lock_path, lock)

    # --- danh sách việc: (lần lặp, candidate) → các chain còn thiếu ---
    tasks = {t["task_id"]: t for t in base.read_tasks(base.MANIFESTS["eval"])}
    unknown = [c["task_id"] for c in cands if c["task_id"] not in tasks]
    if unknown:
        raise SystemExit(f"Tập con có lớp không thuộc tập đánh giá: {unknown[:3]}")
    reps = [1] if smoke else list(range(1, REPS + 1))
    if smoke:
        cands = cands[:args.limit]
    redo_keys = set()
    if args.redo:                                      # --redo: chỉ chạy đúng các chain này, sau khi xóa sạch bản cũ
        if args.retry_infra:
            raise SystemExit("--redo không dùng chung với --retry-infra.")
        want = [parse_chain(n, cands) for n in args.redo]
        if smoke and any(rep != 1 for _, _, _, rep in want):
            raise SystemExit("--redo kèm --limit (chạy khói) chỉ có lần 1.")
        redo_keys = {(c["task_id"], c["model"], c["strategy"], rm, mech, rep) for c, rm, mech, rep in want}
        n = redo_remove(root, runs_path, redo_keys)
        print(f"[redo] đã xóa sạch {n} dòng cũ của {len(redo_keys)} chain (dòng, thư mục run, log, report)", flush=True)
    latest = read_latest(runs_path)

    def needs(c, rm, mech, rep):
        if redo_keys:
            return (c["task_id"], c["model"], c["strategy"], rm, mech, rep) in redo_keys
        rec = latest.get((c["task_id"], c["model"], c["strategy"], rm, mech, rep))
        return (rec is None or rec["status"] == "STOPPED"              # STOPPED: dừng vì hết tiền → chạy lại
                or (args.retry_infra and rec["status"] != "DONE"))

    jobs = []
    for rep in reps:                                   # hết lần 1 rồi mới lần 2, lần 3
        for c in cands:
            todo = [(rm, mech) for rm in REPAIR_MODELS for mech in MECHANISMS if needs(c, rm, mech, rep)]
            if todo:
                jobs.append((rep, c, todo))
    total = sum(len(todo) for _, _, todo in jobs)
    print(version)
    print(f"Máy {args.machine} · phần {part}: {len(cands)}/{n_all} candidate · lần {reps} · "
          f"mô hình sửa {REPAIR_MODELS} · nhánh {MECHANISMS} · N = {N_ROUNDS} · "
          f"còn {total} chain · {args.workers} worker", flush=True)

    workers_file = os.path.join(root, "workers.txt")
    with open(workers_file, "w", encoding="utf-8") as f:
        f.write(f"{args.workers}\n")

    def target_workers():
        try:
            with open(workers_file, encoding="utf-8") as f:
                return max(1, min(int(f.read().strip()), 16))
        except (OSError, ValueError):
            return args.workers

    free_homes, made, home_lock = [], {"n": 0}, threading.Lock()

    def borrow_home():
        with home_lock:
            if free_homes:
                return free_homes.pop()
            made["n"] += 1
            num = made["n"]
        home = os.path.join(homes_root, f"w{num}")
        base.prepare_home(args.base_home, home)
        pr.arrow_cfg(args.arrow, home, "repair.early_stop_p", "null")   # không dừng sớm
        return home

    def return_home(home):
        with home_lock:
            free_homes.append(home)

    write_lock, counter = threading.Lock(), {"n": 0}

    def job(rep, c, todo, home):
        task = tasks[c["task_id"]]
        try:
            for rm, mech in todo:
                if base.STOP.is_set():                  # đã gặp lỗi phải dừng: bỏ phần còn lại (lần sau chạy tiếp)
                    break
                try:
                    pr.arrow_cfg(args.arrow, home, "repair.mechanism", mech)
                    rec = pr.run_chain(args.arrow, task, c, mech, home, log_dir, rounds=N_ROUNDS,
                                       repair_model=rm, tag=f"rep{rep}")
                except Exception as e:
                    rec = dict(task, model=c["model"], strategy=c["strategy"], mechanism=mech, repair_model=rm,
                               status="INFRA", result="SCRIPT_EXCEPTION", detail=repr(e), finished_at=base.now_iso())
                rec.update(rep=rep, candidate_id=c["candidate_id"], same_model=(rm == c["model"]),
                           c0_sha256=c["c0_sha256"], part=part, machine=args.machine,
                           worker=os.path.basename(home), tool_commit=commit)
                base.mark_fatal(rec)                    # hết tiền / sai khóa → STOPPED và dừng cả lô
                with write_lock:
                    with open(runs_path, "a", encoding="utf-8") as f:
                        f.write(json.dumps(rec, ensure_ascii=False) + "\n")
                    counter["n"] += 1
                    sr = rec.get("success_round")
                    print(f"[{counter['n']}/{total}] lần {rep} {rec['status']:10s} {str(rec.get('result', '')):11s} "
                          f"vòng {'-' if sr is None else sr:>2}/{rec.get('rounds_run', '-')!s:>2} "
                          f"{rec.get('seconds', 0):>7}s  c0 {c['model']:13s} sửa {rm:13s} {mech:11s} "
                          f"{c['strategy']:24s} {task['class_fqn']}", flush=True)
        finally:
            base.clean_home(home)
            return_home(home)

    pending, running, shown = list(jobs), [], None
    try:
        while pending or running:
            running = [th for th in running if th.is_alive()]
            target = target_workers()
            if target != shown:
                print(f"--- {target} worker", flush=True)
                shown = target
            if base.STOP.is_set():                      # dừng: không mở việc mới, chờ chain đang chạy xong
                pending = []
            while pending and len(running) < target:
                rep, c, todo = pending.pop(0)
                th = threading.Thread(target=job, args=(rep, c, todo, borrow_home()), daemon=True)
                th.start()
                running.append(th)
            time.sleep(2)
    except KeyboardInterrupt:
        print("\nĐang dừng các lần chạy arrow...", flush=True)
        with base.PROCS_LOCK:
            for p in list(base.PROCS):
                base.kill_tree(p)
        raise SystemExit("Đã dừng. Chạy lại đúng lệnh cũ để làm tiếp.")

    if redo_keys:
        nc, nr = redo_report(root, runs_path, redo_keys)
        print(f"[redo] đã ghi {nc} chain, {nr} vòng sửa mới vào report/", flush=True)

    latest = read_latest(runs_path)
    summary = {}
    for rec in latest.values():
        label = rec.get("result", "?") if rec["status"] == "DONE" else rec["status"]
        summary[label] = summary.get(label, 0) + 1
    print("Kết quả:", ", ".join(f"{k} {v}" for k, v in sorted(summary.items())))
    left = sum(1 for r in latest.values() if r["status"] not in ("DONE", "STOPPED"))
    if left:
        print(f"Còn {left} chain không xong (INFRA/BAD_CONFIG): xem log, rồi chạy lại với --retry-infra.")
    if base.STOP.is_set():
        base.notify_stop(root, " ".join(["python"] + sys.argv))
        sys.exit(3)
    if not redo_keys:
        print(f"\nXong phần chạy. Bước tiếp: python tn2_collect.py --machine {args.machine}"
              + (" --smoke" if smoke else ""))


if __name__ == "__main__":
    main()
