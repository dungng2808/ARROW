#!/usr/bin/env python3
"""Chạy pilot chọn N (số vòng sửa tối đa cho TN2). Thiết kế: Sua/ban_phan_bien/pilot_chon_N.md.

Hai bước, chạy lần lượt trên cùng một máy:

  1. gen    Sinh c0 cho 50 lớp pilot (classes2test_pilot): 3 cách viết prompt × các mô hình, KHÔNG sửa.
            Dùng đúng tn1_run.py + tn1_collect.py và bản arrow của TN1 (d0f2b5c), nên c0 của pilot
            được sinh y như c0 của TN1 (nguồn của TN2).
            Kết quả: results/<máy>/gen/ (tn2_pool.jsonl = các c0 chưa VALID, c0/ = mã nguồn).

  2. repair Sửa mỗi c0 trong tn2_pool.jsonl bằng 4 cơ chế (F+, F−, F2, A), mô hình sửa = mô hình đã
            sinh c0 (pilot chỉ dùng chain cùng mô hình), tối đa 12 vòng, không dừng sớm.
            Dùng bản arrow có --c0 (203fb8e).
            Kết quả: results/<máy>/repair/ (pilot_runs.jsonl, logs/, homes/).

Sau đó: python3 pilot_analyze.py  → tính N theo quy tắc B (reports/).

Cách chạy (macOS, máy Dung):
    python3 pilot_run.py gen    --machine dung --models gpt6luna,qwen3coder-or --workers 4
    python3 pilot_run.py repair --machine dung --models gpt6luna,qwen3coder-or --workers 4 \\
        --arrow ~/.local/share/arrow-dev/build-tn2/arrow
Chạy khói: thêm --limit 2 (gen: 2 lớp đầu; repair: 2 c0 đầu), kết quả vào results/<máy>-smoke/.
Chia máy: --shard k/4 chia ĐỀU 50 lớp (13/13/12/12, xếp theo sha256, khác cách chia của TN1); mỗi máy chạy
cả GPT và Qwen trên phần của mình (--models gpt6luna,qwen3coder-or). Bước repair đọc tn2_pool.jsonl của chính
máy đó, nên mỗi máy sửa đúng c0 nó sinh. pilot_analyze.py gộp mọi results/<máy>/ rồi mới tính N.

Chạy lại:
    - bị ngắt: chạy lại đúng lệnh cũ, script bỏ qua chain đã xong
    - --retry-infra: chạy lại các chain bị lỗi hạ tầng
Đổi số worker khi đang chạy: ghi số mới vào results/<máy>/repair/workers.txt.

Giải nghĩa từ khóa Python: xem đầu file ../1/tn1_run.py.
"""
import argparse
import json
import os
import platform
import subprocess
import sys
import threading
import time

HERE = os.path.dirname(os.path.abspath(__file__))                     # ARROW_Experiment/pilot
TN1_DIR = os.path.normpath(os.path.join(HERE, "..", "1"))             # ARROW_Experiment/1
sys.path.insert(0, TN1_DIR)
import tn1_run as base   # dùng lại các hàm của TN1: kiểm dataset, mô hình, ARROW_HOME, dừng tiến trình  # noqa: E402

IS_WINDOWS = base.IS_WINDOWS
try:
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

# ---------------------------------------------------------------------------
# CẤU HÌNH CỐ ĐỊNH CỦA PILOT (đã chốt; không sửa sau khi bắt đầu chạy)
# ---------------------------------------------------------------------------
MANIFEST = "classes2test_pilot"                     # 50 lớp pilot (dataset-v1)
MECHANISMS = ["fixed_plus", "fixed_minus", "fixed_l2", "adaptive"]   # F+, F−, F2, A
PILOT_ROUNDS = 12                                   # chạy tới 12 vòng; N chọn trong 5..10
GEN_COMMIT = "d0f2b5c"                              # bản tool sinh c0 (giống TN1)
REPAIR_COMMIT = "203fb8e"                           # bản tool sửa (có --c0, mechanism, 12 vòng)
TIMEOUT_PER_CHAIN = 6 * 60 * 60                     # 12 vòng × (gọi mô hình + build ≤ 15 phút): chặn treo hẳn
C0_STATES = {"COMPILE", "UNKNOWN", "DISCOVERY", "RUNTIME", "ASSERTION", "REGRESSION"}


def out_root(machine, smoke):
    return os.path.join(HERE, "results", machine + ("-smoke" if smoke else ""))


def default_homes(machine, smoke, phase):
    """Windows: đặt ARROW_HOME của worker ở gốc ổ đĩa cho đường dẫn ngắn (giới hạn 260 ký tự)."""
    name = f"pilot-{machine}{'-smoke' if smoke else ''}-{phase}"
    if IS_WINDOWS:
        return os.path.join(os.environ.get("SystemDrive", "C:") + os.sep, "arrow-homes", name)
    return os.path.join(out_root(machine, smoke), phase, "homes")


# ---------------------------------------------------------------------------
# BƯỚC 1: SINH c0
# ---------------------------------------------------------------------------

def pilot_shard(k, n):
    """Chia ĐỀU 50 lớp pilot cho n máy: xếp theo sha256(task_id) rồi lấy lần lượt (13/13/12/12 với n = 4).

    Khác --shard của TN1 (phần dư của mã băm), vốn ra 13/10/22/5 với 50 lớp. Máy nào tính cũng ra cùng kết quả.
    """
    import hashlib
    tasks = sorted(base.read_tasks([MANIFEST]), key=lambda t: hashlib.sha256(t["task_id"].encode()).hexdigest())
    return [t["task_id"] for i, t in enumerate(tasks) if i % n == k - 1]


def cmd_gen(args):
    gen_dir = os.path.join(out_root(args.machine, args.limit is not None), "gen")
    k, n = (int(x) for x in args.shard.split("/"))
    only = pilot_shard(k, n)
    print(f"Phần {args.shard}: {len(only)} lớp pilot", flush=True)
    cmd = [sys.executable, "-u", os.path.join(TN1_DIR, "tn1_run.py"), "--pilot",
           "--machine", args.machine, "--models", args.models, "--workers", str(args.workers),
           "--shard", "1/1", "--only", ",".join(only), "--out-dir", gen_dir, "--base-home", args.base_home,
           "--arrow", args.arrow,
           "--homes-dir", os.path.join(args.homes_dir, "gen") if args.homes_dir
           else default_homes(args.machine, args.limit is not None, "gen")]
    if args.limit is not None:
        cmd += ["--limit", str(args.limit)]
    if args.retry_infra:
        cmd += ["--retry-infra"]
    print("$", " ".join(cmd), flush=True)
    code = subprocess.run(cmd).returncode
    if code == 3:                                   # tn1_run đã dừng vì hết tiền / sai cấu hình mô hình và đã báo
        print("Bước gen đã dừng (xem DA_DUNG.txt trong results/<máy>/gen). Xử lý xong thì chạy lại đúng lệnh gen.")
        sys.exit(3)
    if code != 0:
        raise SystemExit(f"tn1_run.py dừng với mã {code}.")
    code = subprocess.run([sys.executable, os.path.join(TN1_DIR, "tn1_collect.py"), "--dir", gen_dir]).returncode
    if code != 0:
        raise SystemExit(f"tn1_collect.py dừng với mã {code}.")
    print(f"\nXong bước gen. Bước tiếp: python3 pilot_run.py repair --machine {args.machine} "
          f"--models {args.models}{' --limit ' + str(args.limit) if args.limit is not None else ''} "
          f"--arrow <arrow bản {REPAIR_COMMIT}>")


# ---------------------------------------------------------------------------
# BƯỚC 2: SỬA c0 BẰNG 4 CƠ CHẾ
# ---------------------------------------------------------------------------

def key_of(rec):
    """Khóa của một chain: (lớp, mô hình sinh c0, cách viết prompt, cơ chế sửa)."""
    return (rec["task_id"], rec["model"], rec["strategy"], rec["mechanism"])


def read_latest(path):
    latest = {}
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    rec = json.loads(line)
                    latest[key_of(rec)] = rec
    return latest


def read_pool(gen_dir, models):
    """tn2_pool.jsonl của bước gen → danh sách c0 cần sửa (chỉ các mô hình đang chạy)."""
    path = os.path.join(gen_dir, "tn2_pool.jsonl")
    if not os.path.exists(path):
        raise SystemExit(f"Chưa có {path}. Chạy bước gen trước.")
    pool = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            if line.strip():
                rec = json.loads(line)
                if rec["model"] in models:
                    rec["c0_abs"] = os.path.normpath(os.path.join(TN1_DIR, rec["c0_path"]))
                    pool.append(rec)
    return pool


def arrow_cfg(arrow, home, key, value):
    subprocess.run([arrow, "config", "set", key, value, "--home", home, "--plain"],
                   check=True, stdout=subprocess.DEVNULL, stdin=subprocess.DEVNULL)


def read_json(path):
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except (OSError, json.JSONDecodeError):
        return None


def run_chain(arrow, task, c0, mechanism, home, log_dir, rounds=PILOT_ROUNDS, repair_model=None, tag=""):
    """Chạy một chain (arrow gen --c0) và trả về một dòng cho pilot_runs.jsonl.

    Pilot: mô hình sửa = mô hình sinh c0, 12 vòng. rounds/repair_model/tag chỉ để script khác dùng lại
    (ví dụ thử prompt sửa ở prompt_trial/repair_trial.py).
    """
    model = repair_model or c0["model"]                   # pilot: sửa cùng mô hình đã sinh c0
    cmd = [arrow, "gen", task["class_fqn"], "--repo", task["repo_url"], "--ref", task["checkout_sha"],
           "--c0", c0["c0_abs"], "--c0-model", c0["model"], "--model", model,
           "--rounds", str(rounds), "--no-metrics", "--yes", "--json", "--home", home]
    if c0.get("result") in C0_STATES:
        cmd[cmd.index("--model"):cmd.index("--model")] = ["--c0-state", c0["result"]]
    start = time.time()
    group = ({"creationflags": subprocess.CREATE_NEW_PROCESS_GROUP} if IS_WINDOWS
             else {"start_new_session": True})
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, stdin=subprocess.DEVNULL,
                            text=True, encoding="utf-8", errors="replace", **group)
    with base.PROCS_LOCK:
        base.PROCS.add(proc)
    try:
        out, err = proc.communicate(timeout=TIMEOUT_PER_CHAIN)
        code = proc.returncode
    except subprocess.TimeoutExpired:
        base.kill_tree(proc)
        out, err = proc.communicate()
        code = None
    finally:
        with base.PROCS_LOCK:
            base.PROCS.discard(proc)
    seconds = round(time.time() - start, 1)
    name = f"{task['task_id']}__{c0['model']}__{c0['strategy']}__{mechanism}" + (f"__{model}__{tag}" if tag else "")
    with open(os.path.join(log_dir, name + ".json"), "w", encoding="utf-8") as f:
        f.write(out)
    with open(os.path.join(log_dir, name + ".err"), "w", encoding="utf-8") as f:
        f.write(err)
    rec = dict(task, model=c0["model"], strategy=c0["strategy"], mechanism=mechanism, repair_model=model,
               c0_path=c0["c0_path"], c0_previous_state=c0.get("result"), exit_code=code,
               seconds=seconds, finished_at=base.now_iso())
    try:
        data = json.loads(out)
    except json.JSONDecodeError:
        data = None
    if code is None:
        rec.update(status="INFRA", result="SCRIPT_TIMEOUT")
    elif data is None:
        rec.update(status="INFRA", result="NO_JSON")
    elif "error" in data and not data.get("targets"):
        err = data["error"] or {}
        rec.update(status="INFRA", result=err.get("code") or "ERROR",
                   error_message=err.get("message"), error_hint=err.get("hint"))
    else:
        target = (data.get("targets") or [{}])[0]
        result = target.get("result") or "UNKNOWN_RESULT"
        rec.update(run_id=data.get("run_id"), run_dir=data.get("run_dir"), result=result,
                   c0_state=target.get("c0_state"), success_round=target.get("success_round"),
                   rounds_run=target.get("rounds_run"), stop_reason=target.get("stop_reason"),
                   error_code=(target.get("error") or {}).get("code"),
                   error_message=(target.get("error") or {}).get("message"),
                   error_hint=(target.get("error") or {}).get("hint"),
                   block_seconds=target.get("seconds"))   # khối 1 (clone, phân tích, baseline), khối 3 (kiểm + sửa), tổng
        rec["status"] = "INFRA" if result in ("INFRA", "CANCELLED") or rec["stop_reason"] == "infra" else "DONE"
        # Kiểm cấu hình thật arrow đã dùng (run.json), tránh chạy nhầm cơ chế/số vòng/dừng sớm.
        cfg = (read_json(os.path.join(rec["run_dir"] or "", "run.json")) or {}).get("config") or {}
        bad = []
        if cfg.get("mechanism") != mechanism:
            bad.append(f"mechanism={cfg.get('mechanism')}")
        if cfg.get("rounds") != rounds:
            bad.append(f"rounds={cfg.get('rounds')}")
        if cfg.get("early_stop_p") is not None:
            bad.append(f"early_stop_p={cfg.get('early_stop_p')}")
        if cfg.get("model") != model:
            bad.append(f"model={cfg.get('model')}")
        if bad:
            rec.update(status="BAD_CONFIG", detail=", ".join(bad))
    return rec


def cmd_repair(args):
    smoke = args.limit is not None
    root = out_root(args.machine, smoke)
    gen_dir, rep_dir = os.path.join(root, "gen"), os.path.join(root, "repair")
    log_dir = os.path.join(rep_dir, "logs")
    os.makedirs(log_dir, exist_ok=True)
    stale = os.path.join(rep_dir, "DA_DUNG.txt")
    if os.path.exists(stale):                       # báo dừng của lần trước: lần này chạy lại nên xóa
        os.remove(stale)
    homes_root = os.path.join(args.homes_dir, "repair") if args.homes_dir else default_homes(args.machine, smoke, "repair")
    runs_path = os.path.join(rep_dir, "pilot_runs.jsonl")
    models = [m.strip() for m in args.models.split(",") if m.strip()]
    base.keep_awake()

    # --- kiểm bản tool, dataset, mô hình, bộ prompt ---
    version, commit = base.arrow_version(args.arrow)
    if not commit.startswith(REPAIR_COMMIT) and not args.allow_other_version:
        raise SystemExit(f"Bản tool là '{version}', cần {REPAIR_COMMIT} (có --c0). Chỉ đường bằng --arrow.")
    checksums = base.check_manifests([MANIFEST])
    base_cfg = base.load_config(args.base_home)
    model_info = base.check_models(base_cfg, models)
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

    lock_path = os.path.join(rep_dir, "pilot.lock.json")
    lock = read_json(lock_path) or {"invocations": []}
    old_commits = {inv["tool_commit"] for inv in lock["invocations"]}
    if old_commits and commit not in old_commits and not args.allow_other_version:
        raise SystemExit(f"Các lần chạy trước dùng tool {sorted(old_commits)}, lần này {commit}. Không trộn hai bản.")
    old_prompts = [inv["prompt_sha256"] for inv in lock["invocations"]]
    if old_prompts and prompt_sha != old_prompts[-1]:
        raise SystemExit("Bộ prompt (prompts/override) khác các lần chạy trước. Prompt đã chốt, không đổi giữa chừng.")
    lock["invocations"].append({
        "started_at": base.now_iso(), "tool_version": version, "tool_commit": commit,
        "machine": args.machine, "os": platform.platform(), "cpu_count": os.cpu_count(),
        "models": model_info, "mechanisms": MECHANISMS, "rounds": PILOT_ROUNDS, "early_stop_p": None,
        "repair_model": "same as generation model", "manifest_sha256": checksums,
        "prompt_override_dir": base.PROMPT_DIR, "prompt_sha256": prompt_sha,
        "verification": base_cfg.get("verification", {}), "workers_start": args.workers, "argv": sys.argv,
    })
    base.write_json(lock_path, lock)

    # --- danh sách chain ---
    tasks = {t["task_id"]: t for t in base.read_tasks([MANIFEST])}
    pool = read_pool(gen_dir, models)
    unknown = [c["task_id"] for c in pool if c["task_id"] not in tasks]
    if unknown:
        raise SystemExit(f"tn2_pool.jsonl có lớp không thuộc {MANIFEST}: {unknown[:3]}")
    missing = [c["c0_abs"] for c in pool if not os.path.exists(c["c0_abs"])]
    if missing:
        raise SystemExit(f"Thiếu file c0 (chạy lại tn1_collect.py --dir {gen_dir}): {missing[:3]}")
    pool.sort(key=lambda c: (c["task_id"], c["model"], c["strategy"]))
    if args.limit is not None:
        pool = pool[:args.limit]
    latest = read_latest(runs_path)

    def needs(c0, mech):
        rec = latest.get((c0["task_id"], c0["model"], c0["strategy"], mech))
        return (rec is None or rec["status"] == "STOPPED"            # STOPPED: dừng vì hết tiền → chạy lại
                or (args.retry_infra and rec["status"] != "DONE"))

    # Một job = một c0 với các cơ chế còn thiếu, chạy liền nhau trong cùng một home (dùng lại bản clone).
    jobs = [(c0, [m for m in MECHANISMS if needs(c0, m)]) for c0 in pool]
    jobs = [(c0, todo) for c0, todo in jobs if todo]
    total = sum(len(todo) for _, todo in jobs)
    print(version)
    print(f"Máy {args.machine} · {len(pool)} c0 · mô hình {models} · cơ chế {MECHANISMS} · "
          f"{PILOT_ROUNDS} vòng · còn {total} chain · {args.workers} worker", flush=True)

    workers_file = os.path.join(rep_dir, "workers.txt")
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
        arrow_cfg(args.arrow, home, "repair.early_stop_p", "null")   # không dừng sớm (chạy đủ 12 vòng)
        return home

    def return_home(home):
        with home_lock:
            free_homes.append(home)

    write_lock, counter = threading.Lock(), {"n": 0}

    def job(c0, todo, home):
        task = tasks[c0["task_id"]]
        try:
            for mech in todo:
                if base.STOP.is_set():              # đã gặp lỗi phải dừng: bỏ phần còn lại (lần sau chạy tiếp)
                    break
                try:
                    arrow_cfg(args.arrow, home, "repair.mechanism", mech)
                    rec = run_chain(args.arrow, task, c0, mech, home, log_dir)
                except Exception as e:
                    rec = dict(task, model=c0["model"], strategy=c0["strategy"], mechanism=mech,
                               status="INFRA", result="SCRIPT_EXCEPTION", detail=repr(e),
                               finished_at=base.now_iso())
                rec.update(machine=args.machine, worker=os.path.basename(home), tool_commit=commit)
                base.mark_fatal(rec)                # hết tiền / sai khóa → STOPPED và dừng cả lô
                with write_lock:
                    with open(runs_path, "a", encoding="utf-8") as f:
                        f.write(json.dumps(rec, ensure_ascii=False) + "\n")
                    counter["n"] += 1
                    sr = rec.get("success_round")
                    print(f"[{counter['n']}/{total}] {rec['status']:10s} {str(rec.get('result', '')):11s} "
                          f"vòng {'-' if sr is None else sr:>2}/{rec.get('rounds_run', '-')!s:>2} "
                          f"{rec.get('seconds', 0):>7}s  {c0['model']:13s} {mech:11s} "
                          f"{c0['strategy']:24s} {task['class_fqn']}", flush=True)
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
            if base.STOP.is_set():                  # dừng: không mở c0 mới, chờ chain đang chạy xong
                pending = []
            while pending and len(running) < target:
                c0, todo = pending.pop(0)
                th = threading.Thread(target=job, args=(c0, todo, borrow_home()), daemon=True)
                th.start()
                running.append(th)
            time.sleep(2)
    except KeyboardInterrupt:
        print("\nĐang dừng các lần chạy arrow...", flush=True)
        with base.PROCS_LOCK:
            for p in list(base.PROCS):
                base.kill_tree(p)
        raise SystemExit("Đã dừng. Chạy lại đúng lệnh cũ để làm tiếp.")

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
        base.notify_stop(rep_dir, " ".join(["python"] + sys.argv))
        sys.exit(3)


def main():
    ap = argparse.ArgumentParser(description="Chạy pilot chọn N.")
    ap.add_argument("phase", choices=["gen", "repair"], help="gen: sinh c0; repair: sửa bằng 4 cơ chế")
    ap.add_argument("--machine", required=True, help="tên máy/người chạy (thành results/<machine>)")
    ap.add_argument("--models", required=True, help="mô hình, cách nhau dấu phẩy, ví dụ gpt6luna,qwen3coder-or")
    ap.add_argument("--workers", type=int, default=3, help="số job chạy cùng lúc (mặc định 3)")
    ap.add_argument("--shard", default="1/1", help="gen: phần lớp pilot của máy này, chia đều, ví dụ 2/4 (mặc định 1/1)")
    ap.add_argument("--limit", type=int, help="chạy khói: gen N lớp đầu / repair N c0 đầu → results/<máy>-smoke")
    ap.add_argument("--retry-infra", action="store_true", help="chạy lại phần bị lỗi hạ tầng")
    ap.add_argument("--base-home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")),
                    help="ARROW_HOME đã cấu hình mô hình, JDK, prompt (mặc định ~/.arrow)")
    ap.add_argument("--arrow", default="arrow",
                    help=f"lệnh arrow: gen cần bản {GEN_COMMIT}, repair cần bản {REPAIR_COMMIT}")
    ap.add_argument("--homes-dir", help="nơi đặt ARROW_HOME của các worker (thêm thư mục con gen/ và repair/); "
                                        "mặc định Windows: C:\\arrow-homes\\pilot-<máy>-<bước>")
    ap.add_argument("--allow-other-version", action="store_true", help="repair: cho chạy bản tool khác (không khuyên)")
    args = ap.parse_args()
    if args.phase == "gen":
        cmd_gen(args)
    else:
        cmd_repair(args)


if __name__ == "__main__":
    main()
