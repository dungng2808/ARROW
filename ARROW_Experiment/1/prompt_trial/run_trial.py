"""Thử prompt mới (team-v1) với GPT trên vài lớp NGOÀI dataset-v1.

Mục đích: xem lỗi định dạng, lỗi biên dịch, số test mỗi lớp trước khi chốt prompt.
Không phải thực nghiệm; số liệu không đưa vào kết quả chính.

Cách chạy:  python3 run_trial.py [thư_mục_kết_quả] [thư_mục_prompt]   (mặc định results; chạy tiếp những lần chưa xong)
Kết quả:    results/trial_runs.jsonl, results/c0/<task>__<strategy>.java, results/logs/
"""
import json, os, shutil, subprocess, sys, threading, time

HERE = os.path.dirname(os.path.abspath(__file__))                 # thư mục chứa script này
DATASET = os.path.join(HERE, "..", "..", "..", "ARROW_dataset")   # thư mục ARROW_dataset
OUT = os.path.join(HERE, sys.argv[1] if len(sys.argv) > 1 else "results")   # nơi ghi kết quả (lượt 2: results_luot2)
PROMPT_DIR = os.path.abspath(sys.argv[2]) if len(sys.argv) > 2 else None   # tùy chọn: thư mục prompt khác
MODEL = "gpt6luna"                                                # tên model trong ~/.arrow
STRATEGIES = ["zero_shot", "few_shot", "zero_shot_project_aware"]
BASE_HOME = os.path.expanduser("~/.arrow")                        # ARROW_HOME gốc (có khóa, cấu hình)

# 10 lớp đã qua sàng lọc (PASS) nhưng không thuộc dataset-v1, repo cũng không trùng dataset-v1.
# Chọn đủ loại: JUnit 4 / JUnit 5 / TestNG, Maven / Gradle, JDK 8 / 11 / 17.
TASK_IDS = [
    "27812454_653b47c3f2102492",   # ViterbiAlgorithm     JUnit 4, Maven, JDK 8
    "54650297_ad00821c6aee3606",   # DataExpressionSource JUnit 4, Maven, JDK 11
    "1287669_bd7b530ba0ec962b",    # WithJsonPath         JUnit 5, Gradle
    "78016530_a294240ab434b91c",   # ANNIndex             TestNG, Maven
    "40616171_132d46e755922bf8",   # CompositeParser      JUnit 5, Maven
    "38597960_0eef1b2dd0f15eea",   # Timeline             JUnit 4, Maven, JDK 17
    "89108465_fe3e668f639680e4",   # CryptoUtil           JUnit 4, Maven
    "7334654_eba29221bc69dcd0",    # PathUtils            JUnit 5, Maven
    "60930425_45248e2cedcf1306",   # DeweyVersion         JUnit 4, Maven, JDK 11
    "42201162_7cdd5324e2f84b7d",   # NumberFormatter      JUnit 4, Maven
]


def load_tasks():
    """Lấy repo, commit, tên lớp của các lớp trên từ kết quả sàng lọc."""
    rows = {}
    with open(os.path.join(DATASET, "screen", "screen_results.jsonl")) as f:
        for line in f:
            r = json.loads(line)
            rows[r["task_id"]] = r                 # dòng sau ghi đè dòng trước = lần sàng lọc mới nhất
    return [rows[t] for t in TASK_IDS]


def make_home(strategy):
    """Mỗi cách viết prompt một ARROW_HOME riêng, để 3 cách chạy song song không giẫm nhau."""
    home = os.path.join(OUT, "homes", strategy)
    os.makedirs(home, exist_ok=True)
    shutil.copy(os.path.join(BASE_HOME, "config.json"), os.path.join(home, "config.json"))
    link = os.path.join(home, "secrets")
    if not os.path.exists(link):
        os.symlink(os.path.join(BASE_HOME, "secrets"), link)   # dùng chung khóa, không chép khóa
    subprocess.run(["arrow", "config", "set", "prompts.strategy", strategy, "--home", home, "--plain"],
                   check=True, stdout=subprocess.DEVNULL)
    if PROMPT_DIR:                                  # dùng bộ prompt khác bản trong ~/.arrow (vd. bản lượt 1)
        subprocess.run(["arrow", "config", "set", "prompts.override_dir", PROMPT_DIR, "--home", home, "--plain"],
                       check=True, stdout=subprocess.DEVNULL)
    return home


def summarize(home, run_id, task):
    """Đọc file của arrow: số test, trạng thái, lỗi đầu tiên; chép c0 ra để đọc."""
    tdir = os.path.join(home, "runs", run_id, "targets", task["class_fqn"])
    info = {}
    v = os.path.join(tdir, "block3", "verify_c0.json")
    if os.path.exists(v):
        d = json.load(open(v))
        info["tests"] = d.get("test_methods")
        info["flaky"] = d.get("module_flaky")
        info["first_error"] = json.dumps(d.get("errors") or d.get("compile_errors") or d.get("failures") or "")[:400]
    c = os.path.join(tdir, "block2", "candidate.java")
    if os.path.exists(c):
        return info, c
    return info, None


def worker(strategy, tasks, done, lock):
    home = make_home(strategy)
    for task in tasks:
        key = f'{task["task_id"]}__{strategy}'
        if key in done:
            continue                                # đã chạy rồi thì bỏ qua (chạy tiếp được)
        cmd = ["arrow", "gen", task["class_fqn"], "--repo", task["repo_url"], "--ref", task["checkout_sha"],
               "--model", MODEL, "--no-repair", "--no-metrics", "--yes", "--json", "--home", home]
        t0 = time.time()
        p = subprocess.run(cmd, capture_output=True, text=True, stdin=subprocess.DEVNULL, timeout=3600)
        with open(os.path.join(OUT, "logs", key + ".json"), "w") as f:
            f.write(p.stdout)
        with open(os.path.join(OUT, "logs", key + ".err"), "w") as f:
            f.write(p.stderr)
        rec = {"key": key, "task_id": task["task_id"], "class_fqn": task["class_fqn"], "strategy": strategy,
               "exit_code": p.returncode, "seconds": round(time.time() - t0, 1)}
        try:
            out = json.loads(p.stdout)
            rec["run_id"] = out.get("run_id")
            rec["result"] = out["targets"][0]["result"]
            info, cand = summarize(home, rec["run_id"], task)
            rec.update(info)
            if cand:
                shutil.copy(cand, os.path.join(OUT, "c0", key + ".java"))
        except Exception as e:                      # arrow không in JSON hợp lệ
            rec["result"] = "NO_JSON"
            rec["error"] = str(e)[:200]
        with lock:
            with open(os.path.join(OUT, "trial_runs.jsonl"), "a") as f:
                f.write(json.dumps(rec, ensure_ascii=False) + "\n")
            print(rec["key"], rec["result"], rec.get("tests"), f'{rec["seconds"]}s', flush=True)
        shutil.rmtree(os.path.join(home, "work"), ignore_errors=True)      # dọn bản chép repo cho đỡ đầy ổ
        shutil.rmtree(os.path.join(home, "mirrors"), ignore_errors=True)


def main():
    for d in ("logs", "c0"):
        os.makedirs(os.path.join(OUT, d), exist_ok=True)
    done = set()
    path = os.path.join(OUT, "trial_runs.jsonl")
    if os.path.exists(path):
        for line in open(path):
            r = json.loads(line)
            if r["result"] not in ("NO_JSON", "INFRA"):
                done.add(r["key"])
    tasks = load_tasks()
    lock = threading.Lock()
    threads = [threading.Thread(target=worker, args=(s, tasks, done, lock)) for s in STRATEGIES]
    for t in threads:
        t.start()                                   # 3 cách viết prompt chạy song song
    for t in threads:
        t.join()


if __name__ == "__main__":
    main()
