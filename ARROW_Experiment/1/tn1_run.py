#!/usr/bin/env python3
# ^ Dòng "shebang": trên Mac/Linux, cho phép chạy file bằng ./tn1_run.py; hệ điều hành
#   sẽ tìm chương trình "python3" (qua lệnh env) để chạy file này.
"""Chạy Thực nghiệm 1 (RQ1a): sinh test bằng 3 cách viết prompt × các mô hình, KHÔNG sửa.

Cách chạy:
    python3 tn1_run.py --machine dung --models gpt6luna --workers 3
    python3 tn1_run.py --machine dung --models gpt6luna --pilot --limit 3   # chạy khói 3 lớp pilot

Chạy lại:
    - bị ngắt: chạy lại đúng lệnh cũ, script bỏ qua phần đã xong
    - --retry-infra: chạy lại các lần bị lỗi hạ tầng (INFRA, quá giờ, không đọc được kết quả)
    - --rerun <file>: chạy lại các dòng "task_id,model,strategy" (hoặc chỉ "task_id") trong file

Đổi số worker khi đang chạy: ghi số mới vào results/<máy>/workers.txt.
Chia việc cho nhiều máy: --shard 2/5 (máy này làm phần thứ 2 trong 5 phần).

Đầu ra (results/<máy>/):
    tn1_runs.jsonl   mỗi lần chạy arrow gen một dòng (dòng mới nhất của mỗi bộ test là kết quả dùng)
    tn1.lock.json    bản tool, cấu hình, mô hình, checksum dataset của mỗi lần gọi script
    logs/            JSON và log gốc của từng lần chạy arrow gen
    homes/w1, w2...  ARROW_HOME riêng của từng worker (chứa runs/ của arrow, cần cho tn1_collect.py)

---------------------------------------------------------------------------
GIẢI NGHĨA CÁC TỪ KHÓA PYTHON DÙNG TRONG FILE NÀY
---------------------------------------------------------------------------
import X          nạp thư viện X để dùng (ví dụ: import json → dùng json.loads)
from X import Y   nạp riêng phần Y trong thư viện X
def f(a, b):      định nghĩa một hàm tên f, nhận đầu vào a và b
return x          kết thúc hàm và trả x về cho nơi gọi
x = ...           gán giá trị cho biến x
if / elif / else  nếu / nếu không thì nếu / còn lại
for x in xs:      lặp: lần lượt lấy từng phần tử của xs đặt vào x
while cond:       lặp chừng nào cond còn đúng
break / continue  thoát vòng lặp / bỏ qua phần còn lại, sang lượt lặp sau
try / except      thử chạy; nếu có lỗi loại nêu trong except thì xử lý ở đó
finally           đoạn luôn chạy dù try có lỗi hay không
with open(...) as f:  mở file; ra khỏi khối with thì file tự đóng
raise SystemExit(m)   dừng cả chương trình và in thông báo m
None              "không có giá trị"
True / False      đúng / sai
and / or / not    và / hoặc / phủ định
in                kiểm "nằm trong" (x in xs)
lambda x: ...     hàm ngắn không tên
[... for x in xs] tạo danh sách mới từ xs (list comprehension)
{k: v ...}        từ điển (dict): ánh xạ khóa k → giá trị v
(a, b)            bộ (tuple): nhóm cố định nhiều giá trị
f"...{x}..."      chuỗi có chèn giá trị của biến x
*args / **kwargs  nhận nhiều đối số / nhiều đối số có tên
global            dùng biến khai báo ở ngoài hàm
"""

# ---------------------------------------------------------------------------
# NẠP THƯ VIỆN (đều có sẵn trong Python, không cần cài thêm)
# ---------------------------------------------------------------------------
import argparse      # đọc các tùy chọn dòng lệnh như --workers 3
import hashlib       # tính mã băm sha256 (kiểm checksum dataset, chia shard)
import json          # đọc/ghi dữ liệu dạng JSON
import os            # thao tác đường dẫn, thư mục, biến môi trường, tín hiệu dừng tiến trình
import platform      # lấy thông tin máy (hệ điều hành, CPU) để ghi vào file lock
import re            # biểu thức chính quy: tìm mẫu chữ (ví dụ mã commit trong "arrow --version")
import shutil        # xóa cả thư mục, chép file
import signal        # gửi tín hiệu SIGKILL để dừng hẳn tiến trình arrow/Maven
import subprocess    # chạy chương trình khác (lệnh arrow) từ Python
import sys           # lấy câu lệnh đã gõ (sys.argv) để ghi vào file lock
import threading    # chạy nhiều worker song song bằng luồng (thread)
import time          # đo thời gian, ngủ chờ (time.sleep)
from datetime import datetime, timezone   # lấy thời điểm hiện tại theo giờ UTC

IS_WINDOWS = os.name == "nt"   # True khi chạy trên Windows (macOS/Linux là "posix")
try:
    sys.stdout.reconfigure(encoding="utf-8")   # Windows: in được chữ tiếng Việt ra cửa sổ lệnh
    sys.stderr.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass


def keep_awake():
    """Windows: không cho máy ngủ khi script đang chạy (macOS dùng caffeinate -i)."""
    if IS_WINDOWS:
        import ctypes                                          # gọi hàm của Windows
        ctypes.windll.kernel32.SetThreadExecutionState(0x80000000 | 0x00000001)
        # ^ ES_CONTINUOUS | ES_SYSTEM_REQUIRED: giữ máy thức tới khi script thoát


def kill_tree(proc):
    """Dừng hẳn arrow và mọi tiến trình con của nó (Maven, Gradle, java)."""
    try:
        if IS_WINDOWS:
            subprocess.run(["taskkill", "/T", "/F", "/PID", str(proc.pid)],
                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            # ^ /T: cả cây tiến trình con; /F: buộc dừng
        else:
            os.killpg(proc.pid, signal.SIGKILL)                # dừng cả nhóm tiến trình
    except ProcessLookupError:                                 # tiến trình đã tự kết thúc
        pass

# ---------------------------------------------------------------------------
# CẤU HÌNH CỐ ĐỊNH
# ---------------------------------------------------------------------------
HERE = os.path.dirname(os.path.abspath(__file__))
# ^ HERE = thư mục chứa file này. __file__ là đường dẫn file đang chạy; abspath đổi thành
#   đường dẫn đầy đủ; dirname lấy phần thư mục.

FINAL_DIR = os.path.normpath(os.path.join(HERE, "..", "..", "ARROW_dataset", "final"))
# ^ Thư mục dataset đã chốt (dataset-v1). join ghép các phần đường dẫn; ".." là "lên một cấp";
#   normpath rút gọn đường dẫn cho gọn.

PROMPT_DIR = os.path.join(HERE, "prompts", "override")
# ^ Bộ prompt của nhóm (team-v1), nạp vào arrow bằng cấu hình prompts.override_dir.

EXPECTED_COMMIT = "d0f2b5c"
# ^ Bản tool đã chốt cho TN1 (commit trong repo nhóm). Bản khác thì script dừng, trừ khi có
#   --allow-other-version.

STRATEGIES = ["zero_shot", "few_shot", "zero_shot_project_aware"]
# ^ 3 cách viết prompt, đúng tên mà "arrow config set prompts.strategy" nhận. Dấu [ ] tạo danh sách.

MANIFESTS = {
    "eval": ["classes2test_eval", "catlm_eval", "gitbug_java_eval"],   # tập đánh giá: 268 lớp
    "pilot": ["classes2test_pilot"],                                  # lớp pilot: chỉ để chạy khói
}
# ^ Từ điển: khóa "eval"/"pilot" → danh sách tên file .jsonl trong FINAL_DIR.

TIMEOUT_PER_RUN = 60 * 60
# ^ Mỗi lần chạy arrow gen tối đa 3600 giây. Tool đã tự giới hạn mỗi bước build 15 phút;
#   mức này chỉ để chặn trường hợp bị treo hẳn.

PROCS = set()               # tập các tiến trình arrow đang chạy, để Ctrl+C dừng được chúng
PROCS_LOCK = threading.Lock()
# ^ "khóa": bảo đảm hai luồng không cùng sửa PROCS một lúc.


# ---------------------------------------------------------------------------
# 1. HÀM TIỆN ÍCH NHỎ
# ---------------------------------------------------------------------------

def now_iso():
    """Thời điểm hiện tại theo UTC, dạng chữ như 2026-11-19T08:30:00+00:00."""
    return datetime.now(timezone.utc).isoformat(timespec="seconds")
    # ^ datetime.now(timezone.utc) = bây giờ theo giờ UTC; isoformat đổi ra chữ chuẩn ISO;
    #   timespec="seconds" bỏ phần lẻ của giây.


def sha256_file(path):
    """Mã băm sha256 của một file (dùng để kiểm file dataset không bị sửa)."""
    h = hashlib.sha256()                      # tạo bộ tính sha256 rỗng
    with open(path, "rb") as f:               # mở file ở chế độ đọc nhị phân ("rb" = read binary)
        for chunk in iter(lambda: f.read(1 << 20), b""):
            # ^ đọc lần lượt từng khúc 1 MB (1 << 20 = 1.048.576 byte) cho tới khi hết (b"" = rỗng)
            h.update(chunk)                   # đưa khúc vừa đọc vào bộ tính
    return h.hexdigest()                      # trả về mã băm dạng chữ hex (64 ký tự)


def write_json(path, data):
    """Ghi dữ liệu ra file JSON, có thụt lề cho dễ đọc."""
    with open(path, "w", encoding="utf-8") as f:   # "w" = ghi đè; utf-8 để giữ tiếng Việt
        json.dump(data, f, ensure_ascii=False, indent=2)
        # ^ json.dump ghi data vào file f; ensure_ascii=False giữ nguyên chữ có dấu; indent=2 thụt 2 cách


# ---------------------------------------------------------------------------
# 2. KIỂM DATASET VÀ ĐỌC DANH SÁCH LỚP
# ---------------------------------------------------------------------------

def check_manifests(names):
    """Kiểm checksum các file .jsonl sẽ dùng, so với SHA256SUMS.txt của dataset-v1.

    Sai checksum nghĩa là dataset đã bị sửa sau khi chốt → dừng ngay.
    Trả về từ điển {tên file: mã băm} để ghi vào file lock.
    """
    sums = {}                                                  # từ điển rỗng: tên file → mã băm đã chốt
    with open(os.path.join(FINAL_DIR, "SHA256SUMS.txt"), encoding="utf-8") as f:
        for line in f:                                         # mỗi dòng dạng "<mã băm>  <tên file>"
            parts = line.split()                               # tách theo khoảng trắng → [mã băm, tên file]
            if len(parts) == 2:                                # chỉ nhận dòng đúng 2 phần
                sums[parts[1]] = parts[0]                      # lưu: tên file → mã băm
    result = {}                                                # kết quả trả về
    for name in names:                                         # với mỗi manifest cần dùng
        fname = name + ".jsonl"                                # thêm đuôi .jsonl vào tên
        actual = sha256_file(os.path.join(FINAL_DIR, fname))   # tính mã băm thật của file
        if sums.get(fname) != actual:                          # .get trả None nếu không có tên đó
            raise SystemExit(f"Checksum sai: {fname}. Dataset dataset-v1 có thể đã bị sửa. Dừng.")
        result[fname] = actual                                 # đúng thì ghi lại
    return result


def read_tasks(names):
    """Đọc các file .jsonl → danh sách lớp, mỗi lớp là một từ điển 5 trường."""
    tasks = []                                                 # danh sách rỗng để thêm dần
    for name in names:
        with open(os.path.join(FINAL_DIR, name + ".jsonl"), encoding="utf-8") as f:
            for line in f:                                     # mỗi dòng là một bản ghi JSON của một lớp
                if not line.strip():                           # strip() bỏ khoảng trắng; dòng rỗng thì bỏ qua
                    continue
                rec = json.loads(line)                         # đổi chữ JSON thành từ điển Python
                tasks.append({                                 # append = thêm vào cuối danh sách
                    "task_id": rec["task_id"],                 # mã lớp, duy nhất
                    "dataset": rec["dataset"],                 # classes2test / catlm / gitbug-java
                    "repo_url": rec["repo_url"],               # địa chỉ repo GitHub
                    "checkout_sha": rec["checkout_sha"],       # commit cần lấy
                    "class_fqn": rec["class_fqn"],             # tên đầy đủ của lớp cần sinh test
                })
    return tasks


def in_shard(task_id, k, n):
    """Lớp này có thuộc phần thứ k trong n phần không (chia việc cho nhiều máy).

    Dùng sha256 của task_id nên máy nào tính cũng ra cùng kết quả: không trùng, không sót.
    """
    return int(hashlib.sha256(task_id.encode()).hexdigest(), 16) % n == k - 1
    # ^ encode() đổi chữ sang byte; hexdigest() ra mã hex; int(..., 16) đổi hex thành số;
    #   % n lấy phần dư khi chia cho n (0..n-1); phần k (1..n) ứng với số dư k-1.


# ---------------------------------------------------------------------------
# 3. KIỂM TOOL, MÔ HÌNH VÀ CHUẨN BỊ ARROW_HOME CHO TỪNG WORKER
# ---------------------------------------------------------------------------

def arrow_version(arrow):
    """Chạy "arrow --version" → (chuỗi đầy đủ, mã commit)."""
    out = subprocess.run([arrow, "--version"], capture_output=True, text=True, encoding="utf-8",
                         errors="replace", check=True).stdout.strip()
    # ^ subprocess.run chạy lệnh; capture_output=True lấy chữ in ra; text=True nhận dạng chữ;
    #   check=True: lệnh lỗi thì báo lỗi luôn; .stdout là phần in ra; .strip() bỏ khoảng trắng thừa.
    m = re.search(r"commit ([0-9a-f]+)", out)     # tìm "commit " rồi chuỗi số hex phía sau
    return out, (m.group(1) if m else "")          # group(1) = phần trong ngoặc; không thấy thì ""


def load_config(home):
    """Đọc config.json của một ARROW_HOME (rỗng nếu chưa có)."""
    path = os.path.join(home, "config.json")
    if not os.path.exists(path):                    # chưa có file thì trả từ điển rỗng
        return {}
    with open(path, encoding="utf-8") as f:
        return json.load(f)                         # json.load đọc file JSON → từ điển


def check_models(base_cfg, models):
    """Kiểm từng mô hình đã khai báo trong ARROW_HOME gốc; trả về thông tin để ghi vào lock.

    Thông tin gồm tên mô hình thật (model_id), nhà cung cấp, tham số (temperature, seed...).
    Không bao giờ chứa khóa API (config.json của arrow không lưu khóa).
    """
    llm = base_cfg.get("llm", {})                   # phần "llm" trong config; không có thì {}
    info = {}
    for m in models:
        model = llm.get("models", {}).get(m)        # khai báo của mô hình m
        if not model:                               # không có → báo cách sửa rồi dừng
            raise SystemExit(f"Chưa khai báo mô hình '{m}' trong ARROW_HOME gốc. "
                             f"Chạy: arrow models add (hoặc arrow setup) rồi thử lại.")
        provider = llm.get("providers", {}).get(model.get("provider"), {})   # nhà cung cấp của mô hình
        info[m] = {"model": model, "provider": {k: v for k, v in provider.items() if k != "secret"}}
        # ^ chép khai báo mô hình và nhà cung cấp; {k: v for ...} tạo từ điển mới, bỏ khóa "secret" cho chắc
    return info


def prepare_home(base_home, home):
    """Tạo ARROW_HOME riêng cho một worker, chép cấu hình từ ARROW_HOME gốc.

    Khóa API: nếu không có biến môi trường ARROW_SECRET_<TÊN>, worker dùng chung thư mục
    secrets/ của ARROW_HOME gốc qua một liên kết (symlink), không chép khóa ra chỗ khác.
    """
    os.makedirs(home, exist_ok=True)                # tạo thư mục; exist_ok=True: có rồi thì thôi
    cfg_path = os.path.join(home, "config.json")
    shutil.copyfile(os.path.join(base_home, "config.json"), cfg_path)
    # ^ chép config.json gốc MỖI lần gọi script (không chỉ lần đầu), để worker có cấu hình mới nhất,
    #   ví dụ mô hình vừa thêm vào ~/.arrow. prompts.strategy được đặt lại trước mỗi lớp nên không mất gì.
    base_secrets = os.path.join(base_home, "secrets")
    link = os.path.join(home, "secrets")
    if os.path.isdir(base_secrets) and not os.path.exists(link):
        try:
            os.symlink(base_secrets, link, target_is_directory=True)   # liên kết home/secrets → base_home/secrets
        except OSError:                             # Windows thường không cho tạo symlink nếu không có quyền admin
            if IS_WINDOWS:                          # thay bằng "junction" (Windows cho tạo không cần admin)
                subprocess.run(["cmd", "/c", "mklink", "/J", link, base_secrets],
                               stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            if not os.path.exists(link):
                print(f"[!] Không tạo được liên kết secrets cho {home}; hãy đặt biến môi trường ARROW_SECRET_<TÊN>.")


def set_strategy(arrow, home, strategy):
    """Đặt cách viết prompt cho ARROW_HOME này (cấu hình prompts.strategy của arrow)."""
    subprocess.run([arrow, "config", "set", "prompts.strategy", strategy, "--home", home, "--plain"],
                   check=True, stdout=subprocess.DEVNULL, stdin=subprocess.DEVNULL)
    # ^ stdout=DEVNULL: bỏ chữ in ra; stdin=DEVNULL: không cho lệnh chờ bàn phím


def clean_home(home):
    """Xóa bản clone repo (mirrors/) và bản sao build (work/) sau mỗi lớp cho đỡ đầy đĩa.

    Giữ runs/ (kết quả của arrow, tn1_collect.py cần) và cache/ (baseline dùng lại).
    """
    for sub in ("mirrors", "work"):                 # ("a", "b") là tuple 2 phần tử
        path = os.path.join(home, sub)
        if os.path.isdir(path):
            shutil.rmtree(path, ignore_errors=True) # xóa cả thư mục; lỗi lặt vặt thì bỏ qua
            os.makedirs(path, exist_ok=True)        # tạo lại thư mục rỗng


# ---------------------------------------------------------------------------
# 4. ĐỌC KẾT QUẢ CŨ ĐỂ BIẾT CÁI GÌ ĐÃ XONG (phục vụ chạy lại)
# ---------------------------------------------------------------------------

def key_of(rec):
    """Khóa của một bộ test: (lớp, mô hình, cách viết prompt)."""
    return (rec["task_id"], rec["model"], rec["strategy"])


def read_latest(runs_path):
    """Đọc tn1_runs.jsonl → {khóa: dòng mới nhất}. Một bộ test chạy lại có nhiều dòng; dòng cuối thắng."""
    latest = {}
    if os.path.exists(runs_path):
        with open(runs_path, encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    rec = json.loads(line)
                    latest[key_of(rec)] = rec       # ghi đè: dòng sau thay dòng trước
    return latest


def read_rerun(path):
    """Đọc file --rerun: mỗi dòng "task_id,model,strategy" hoặc chỉ "task_id" (= mọi cấu hình của lớp đó)."""
    keys = set()                                    # set: tập hợp, không trùng
    with open(path, encoding="utf-8") as f:
        for line in f:
            parts = [p.strip() for p in line.split(",") if p.strip()]   # tách theo dấu phẩy, bỏ ô rỗng
            if parts:
                keys.add(tuple(parts))              # lưu dạng tuple: (task_id,) hoặc (task_id, model, strategy)
    return keys


def needs_run(key, latest, retry_infra, rerun):
    """Bộ test này có phải chạy (lại) không."""
    if key in rerun or (key[0],) in rerun:          # có trong file --rerun (đúng cấu hình hoặc cả lớp)
        return True
    rec = latest.get(key)
    if rec is None:                                 # chưa chạy lần nào
        return True
    if retry_infra and rec["status"] != "DONE":     # có --retry-infra và lần trước không xong
        return True
    return False                                    # đã xong → bỏ qua


# ---------------------------------------------------------------------------
# 5. CHẠY MỘT LẦN "arrow gen" (một lớp × một mô hình × một cách viết prompt)
# ---------------------------------------------------------------------------

def run_gen(arrow, task, model, strategy, home, log_dir):
    """Chạy arrow gen và trả về một dòng kết quả (từ điển) để ghi vào tn1_runs.jsonl."""
    cmd = [arrow, "gen", task["class_fqn"],
           "--repo", task["repo_url"], "--ref", task["checkout_sha"],   # repo và commit của lớp
           "--model", model,                                             # mô hình dùng
           "--no-repair",                                                # TN1: không sửa test
           "--no-metrics",                                               # TN1: không đo chất lượng
           "--yes",                                                      # không hỏi gì (chạy tự động)
           "--json",                                                     # in kết quả dạng JSON
           "--home", home]                                               # ARROW_HOME của worker
    start = time.time()                             # ghi giờ bắt đầu để tính thời gian
    group = ({"creationflags": subprocess.CREATE_NEW_PROCESS_GROUP} if IS_WINDOWS
             else {"start_new_session": True})
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, stdin=subprocess.DEVNULL,
                            text=True, encoding="utf-8", errors="replace", **group)
    # ^ Popen chạy lệnh nhưng không đợi; PIPE = hứng chữ in ra; start_new_session=True gom arrow và
    #   các tiến trình con (mvn, java) vào một nhóm để có thể dừng cả nhóm.
    with PROCS_LOCK:
        PROCS.add(proc)                             # ghi nhận tiến trình đang chạy (cho Ctrl+C)
    try:
        out, err = proc.communicate(timeout=TIMEOUT_PER_RUN)   # đợi chạy xong, lấy chữ in ra
        code = proc.returncode                                 # mã thoát của arrow
    except subprocess.TimeoutExpired:                          # quá giờ
        kill_tree(proc)                                        # dừng hẳn arrow và tiến trình con
        out, err = proc.communicate()
        code = None                                            # None = script tự dừng vì quá giờ
    finally:
        with PROCS_LOCK:
            PROCS.discard(proc)                                # bỏ khỏi danh sách đang chạy
    seconds = round(time.time() - start, 1)                    # số giây đã chạy, làm tròn 1 chữ số

    base = f"{task['task_id']}__{model}__{strategy}"           # tên file log: lớp__mô hình__cách
    with open(os.path.join(log_dir, base + ".json"), "w", encoding="utf-8") as f:
        f.write(out)                                           # lưu JSON gốc arrow in ra
    with open(os.path.join(log_dir, base + ".err"), "w", encoding="utf-8") as f:
        f.write(err)                                           # lưu tiến độ và lỗi arrow in ra

    rec = dict(task, model=model, strategy=strategy, exit_code=code, seconds=seconds,
               finished_at=now_iso())
    # ^ dict(task, ...) = chép các trường của task rồi thêm các trường mới
    try:
        data = json.loads(out)                                 # đọc JSON arrow trả về
    except json.JSONDecodeError:                               # không đọc được (arrow lỗi bất thường)
        data = None
    if code is None:
        rec.update(status="INFRA", result="SCRIPT_TIMEOUT")    # update = thêm/sửa trường
    elif data is None:
        rec.update(status="INFRA", result="NO_JSON")
    elif "error" in data:                                      # arrow báo lỗi trước khi chạy xong
        rec.update(status="INFRA", result=(data["error"] or {}).get("code") or "ERROR")
    else:
        target = (data.get("targets") or [{}])[0]              # kết quả của lớp (chỉ có 1 lớp)
        result = target.get("result") or "UNKNOWN_RESULT"      # VALID, COMPILE, ..., INFRA
        rec.update(run_id=data.get("run_id"), run_dir=data.get("run_dir"), result=result,
                   error_code=(target.get("error") or {}).get("code"))
        rec["status"] = "INFRA" if result in ("INFRA", "CANCELLED") else "DONE"
        # ^ INFRA/CANCELLED là lỗi hạ tầng → chạy lại sau; mọi kết quả khác là xong
    return rec


# ---------------------------------------------------------------------------
# 6. CHƯƠNG TRÌNH CHÍNH
# ---------------------------------------------------------------------------

def main():
    ap = argparse.ArgumentParser(description="Chạy Thực nghiệm 1 (RQ1a) bằng arrow.")   # bộ đọc tùy chọn
    ap.add_argument("--machine", required=True, help="tên máy/người chạy, ví dụ dung (thành results/<machine>)")
    ap.add_argument("--models", required=True, help="danh sách mô hình, cách nhau dấu phẩy, ví dụ gpt6luna")
    ap.add_argument("--workers", type=int, default=3, help="số lớp chạy cùng lúc (mặc định 3)")
    ap.add_argument("--shard", default="1/1", help="phần việc của máy này, ví dụ 2/5 (mặc định 1/1 = tất cả)")
    ap.add_argument("--pilot", action="store_true", help="chạy trên lớp pilot (chạy khói), không phải tập đánh giá")
    ap.add_argument("--limit", type=int, help="chỉ chạy N lớp đầu (để thử)")
    ap.add_argument("--only", help="chỉ chạy các lớp có task_id này (cách nhau dấu phẩy), "
                                   "ví dụ chạy lại trên máy khác lớp bị lỗi hạ tầng ở máy gốc")
    ap.add_argument("--retry-infra", action="store_true", help="chạy lại các lần bị lỗi hạ tầng")
    ap.add_argument("--rerun", help="file danh sách bộ test cần chạy lại")
    ap.add_argument("--base-home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")),
                    help="ARROW_HOME đã cấu hình mô hình và JDK (mặc định ~/.arrow)")
    ap.add_argument("--arrow", default="arrow", help="đường dẫn lệnh arrow")
    ap.add_argument("--homes-dir", help="nơi đặt ARROW_HOME của các worker (mặc định: results/<máy>/homes; "
                                        "Windows: C:\\arrow-homes\\<máy> cho đường dẫn ngắn)")
    ap.add_argument("--allow-other-version", action="store_true", help="cho chạy bản tool khác bản đã chốt")
    args = ap.parse_args()                          # đọc tùy chọn người dùng gõ → args

    models = [m.strip() for m in args.models.split(",") if m.strip()]   # "a,b" → ["a", "b"]
    k, n = (int(x) for x in args.shard.split("/"))                      # "2/5" → k=2, n=5
    group = "pilot" if args.pilot else "eval"                           # chọn nhóm manifest
    out_dir = os.path.join(HERE, "results", args.machine + ("-smoke" if args.pilot else ""))
    if args.homes_dir:
        homes_root = args.homes_dir
    elif IS_WINDOWS:   # Windows giới hạn đường dẫn 260 ký tự: đặt homes ở gốc ổ đĩa cho ngắn
        homes_root = os.path.join(os.environ.get("SystemDrive", "C:") + os.sep, "arrow-homes", os.path.basename(out_dir))
    else:
        homes_root = os.path.join(out_dir, "homes")
    # ^ ARROW_HOME của worker (chứa runs/, mirrors/, work/). tn1_runs.jsonl ghi đường dẫn đầy đủ nên đặt đâu cũng được.
    # ^ chạy khói ghi riêng vào results/<máy>-smoke để không lẫn với kết quả thật
    log_dir = os.path.join(out_dir, "logs")
    os.makedirs(log_dir, exist_ok=True)
    runs_path = os.path.join(out_dir, "tn1_runs.jsonl")

    # --- Kiểm bản tool ---
    keep_awake()
    version, commit = arrow_version(args.arrow)
    if not commit.startswith(EXPECTED_COMMIT) and not args.allow_other_version:
        raise SystemExit(f"Bản tool là '{version}', khác bản đã chốt {EXPECTED_COMMIT}. "
                         f"Cài đúng bản hoặc thêm --allow-other-version (không khuyên).")

    # --- Kiểm dataset, mô hình ---
    checksums = check_manifests(MANIFESTS[group])
    base_cfg = load_config(args.base_home)
    model_info = check_models(base_cfg, models)

    # --- Kiểm bộ prompt: TN1 phải dùng bộ prompt của nhóm (prompts/override) ---
    override = (base_cfg.get("prompts") or {}).get("override_dir")      # thư mục prompt đang cấu hình
    same = lambda a, b: os.path.normcase(os.path.realpath(a)) == os.path.normcase(os.path.realpath(b))
    # ^ normcase: Windows không phân biệt hoa/thường và / với \\
    if not override or not same(override, PROMPT_DIR):
        # ^ realpath đổi về đường dẫn thật để so sánh cho đúng
        raise SystemExit(f"ARROW_HOME gốc chưa dùng bộ prompt của nhóm. Chạy:\n"
                         f"  arrow config set prompts.override_dir \"{PROMPT_DIR}\"")
    prompt_sha = {}                                  # sha256 của từng file prompt, ghi vào file lock
    for root, _, files in os.walk(PROMPT_DIR):       # os.walk duyệt mọi thư mục con
        for name in sorted(files):
            if not name.endswith(".md"):            # chỉ tính file prompt; bỏ file rác như .DS_Store của macOS
                continue
            path = os.path.join(root, name)
            prompt_sha[os.path.relpath(path, PROMPT_DIR)] = sha256_file(path)

    # --- Ghi file lock: mỗi lần gọi script thêm một mục; bản tool phải giống các lần trước ---
    lock_path = os.path.join(out_dir, "tn1.lock.json")
    if os.path.exists(lock_path):                   # có file lock từ lần chạy trước thì đọc
        with open(lock_path, encoding="utf-8") as f:
            lock = json.load(f)
    else:                                           # chưa có thì tạo khung rỗng
        lock = {"invocations": []}
    old_commits = {inv["tool_commit"] for inv in lock["invocations"]}   # các bản tool đã dùng trước
    if old_commits and commit not in old_commits and not args.allow_other_version:
        raise SystemExit(f"Các lần chạy trước dùng tool {sorted(old_commits)}, lần này {commit}. "
                         f"Không trộn hai bản tool trong cùng một thư mục kết quả.")
    old_prompts = [inv["prompt_sha256"] for inv in lock["invocations"] if "prompt_sha256" in inv]
    old_prompts = [{k: v for k, v in d.items() if k.endswith(".md")} for d in old_prompts]   # bỏ .DS_Store của lock cũ
    if old_prompts and prompt_sha != old_prompts[-1]:
        raise SystemExit("Bộ prompt khác với các lần chạy trước trong thư mục này. "
                         "Prompt đã chốt, không được đổi giữa chừng; kiểm tra lại prompts/override/.")
    lock["invocations"].append({
        "started_at": now_iso(), "tool_version": version, "tool_commit": commit,
        "machine": args.machine, "os": platform.platform(), "cpu_count": os.cpu_count(),
        "models": model_info, "strategies": STRATEGIES, "group": group, "shard": args.shard,
        "workers_start": args.workers, "manifest_sha256": checksums,
        "prompt_override_dir": PROMPT_DIR, "prompt_sha256": prompt_sha,   # bộ prompt đã dùng
        "verification": base_cfg.get("verification", {}),       # cài đặt kiểm test của ARROW_HOME gốc
        "argv": sys.argv,                                         # đúng câu lệnh đã gõ
    })
    write_json(lock_path, lock)

    # --- Lập danh sách việc: mỗi lớp → các (mô hình, cách viết prompt) còn phải chạy ---
    tasks = [t for t in read_tasks(MANIFESTS[group]) if in_shard(t["task_id"], k, n)]
    if args.only:                                   # --only: giữ đúng các lớp được chỉ định
        only = {x.strip() for x in args.only.split(",") if x.strip()}
        unknown = only - {t["task_id"] for t in tasks}
        if unknown:
            raise SystemExit(f"--only có task_id không thuộc dataset/phần này: {sorted(unknown)}")
        tasks = [t for t in tasks if t["task_id"] in only]
    if args.limit:
        tasks = tasks[:args.limit]                  # [:N] lấy N phần tử đầu
    latest = read_latest(runs_path)
    rerun = read_rerun(args.rerun) if args.rerun else set()
    jobs = []                                       # mỗi phần tử: (lớp, danh sách cấu hình cần chạy)
    for t in tasks:
        todo = [(m, s) for s in STRATEGIES for m in models
                if needs_run((t["task_id"], m, s), latest, args.retry_infra, rerun)]
        # ^ duyệt theo cách viết prompt trước, mô hình sau: đổi prompts.strategy ít lần nhất
        if todo:
            jobs.append((t, todo))
    total = sum(len(todo) for _, todo in jobs)      # tổng số lần chạy arrow gen; "_" = biến bỏ qua
    print(version)
    print(f"Máy {args.machine} · phần {args.shard} · {len(tasks)} lớp · mô hình {models} · "
          f"còn {total} lần chạy ({len(jobs)} lớp) · {args.workers} worker")

    # --- Số worker đổi được khi đang chạy: ghi số mới vào workers.txt ---
    workers_file = os.path.join(out_dir, "workers.txt")
    with open(workers_file, "w", encoding="utf-8") as f:
        f.write(f"{args.workers}\n")

    def target_workers():
        """Đọc workers.txt; lỗi thì giữ số ban đầu. Giới hạn 1..16."""
        try:
            with open(workers_file, encoding="utf-8") as f:
                return max(1, min(int(f.read().strip()), 16))
        except (OSError, ValueError):               # file hỏng/không đọc được
            return args.workers

    # --- Mỗi worker mượn một ARROW_HOME riêng (vì prompts.strategy là cấu hình của home) ---
    free_homes, made, home_lock = [], {"n": 0}, threading.Lock()

    def borrow_home():
        with home_lock:
            if free_homes:
                return free_homes.pop()             # pop() lấy ra phần tử cuối
            made["n"] += 1                          # += 1 nghĩa là tăng thêm 1
            num = made["n"]
        home = os.path.join(homes_root, f"w{num}")
        prepare_home(args.base_home, home)
        return home

    def return_home(home):
        with home_lock:
            free_homes.append(home)

    write_lock, counter = threading.Lock(), {"n": 0}

    def job(task, todo, home):
        """Chạy mọi cấu hình còn thiếu của một lớp, liền nhau, trong cùng một home."""
        try:
            current = None                          # cách viết prompt đang đặt trong home
            for model, strategy in todo:
                if strategy != current:             # chỉ đổi cấu hình khi sang cách khác
                    set_strategy(args.arrow, home, strategy)
                    current = strategy
                try:
                    rec = run_gen(args.arrow, task, model, strategy, home, log_dir)
                except Exception as e:              # lỗi của chính script: ghi lại, không dừng cả lô
                    rec = dict(task, model=model, strategy=strategy, status="INFRA",
                               result="SCRIPT_EXCEPTION", detail=repr(e), finished_at=now_iso())
                rec.update(machine=args.machine, worker=os.path.basename(home), tool_commit=commit)
                with write_lock:                    # ghi từng dòng ngay khi xong (bị ngắt không mất)
                    with open(runs_path, "a", encoding="utf-8") as f:   # "a" = ghi thêm vào cuối file
                        f.write(json.dumps(rec, ensure_ascii=False) + "\n")
                    counter["n"] += 1
                    print(f"[{counter['n']}/{total}] {rec['status']:5s} {rec.get('result', ''):15s} "
                          f"{rec.get('seconds', 0):>7}s  {model:10s} {strategy:24s} {task['class_fqn']}",
                          flush=True)
                    # ^ :5s = canh lề 5 ký tự; :>7 = canh phải 7 ký tự; flush=True in ngay
        finally:
            clean_home(home)                        # xong lớp: xóa bản clone và bản sao build
            return_home(home)                       # trả home cho worker khác

    # --- Vòng điều phối: mỗi 2 giây, mở thêm lớp nếu đang chạy ít hơn số worker mong muốn ---
    pending, running, shown = list(jobs), [], None
    try:
        while pending or running:
            running = [th for th in running if th.is_alive()]    # bỏ các luồng đã xong
            target = target_workers()
            if target != shown:
                print(f"--- {target} worker", flush=True)
                shown = target
            while pending and len(running) < target:
                task, todo = pending.pop(0)                       # pop(0) lấy phần tử đầu
                th = threading.Thread(target=job, args=(task, todo, borrow_home()), daemon=True)
                # ^ tạo một luồng chạy hàm job; daemon=True: luồng tự tắt khi chương trình thoát
                th.start()
                running.append(th)
            time.sleep(2)                                         # ngủ 2 giây rồi kiểm lại
    except KeyboardInterrupt:                                     # người dùng bấm Ctrl+C
        print("\nĐang dừng các lần chạy arrow...", flush=True)
        with PROCS_LOCK:
            for p in list(PROCS):
                kill_tree(p)                                      # dừng cả arrow + Maven
        raise SystemExit("Đã dừng. Chạy lại đúng lệnh cũ để làm tiếp.")

    # --- Tóm tắt: đếm theo dòng mới nhất của mỗi bộ test ---
    latest = read_latest(runs_path)
    summary = {}
    for rec in latest.values():
        label = rec["status"] if rec["status"] != "DONE" else rec.get("result", "?")
        summary[label] = summary.get(label, 0) + 1
    print("Kết quả:", ", ".join(f"{k} {v}" for k, v in sorted(summary.items())))
    left = sum(1 for r in latest.values() if r["status"] != "DONE")
    if left:
        print(f"Còn {left} bộ test lỗi hạ tầng: chạy lại với --retry-infra.")


if __name__ == "__main__":
    # ^ chỉ chạy main() khi file được chạy trực tiếp (không chạy khi file được import từ nơi khác)
    main()
