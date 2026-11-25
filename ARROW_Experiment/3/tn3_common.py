#!/usr/bin/env python3
"""Hàm dùng chung cho TN3 (RQ3). Thiết kế: Sua/ban_phan_bien/cac_thuc_nghiem.md, phần "Thực nghiệm 3" (chốt 26/11/2026).

Cách đưa bộ test của EvoSuite / ChatUniTest qua đúng 5 bước xác minh và phép đo của ARROW, KHÔNG sửa tool:
    1. inspect()            arrow inspect trên repo gốc → JDK, lệnh build, classpath, baseline của bản GỐC
    2. checkout()           giải nén đúng commit từ mirror của arrow vào thư mục làm việc (bản sao)
    3. build_copy()         build bản sao bằng đúng lệnh compile của arrow → class production + classpath
                            (EvoSuite cần hai thứ này để sinh test)
    4. evosuite_setup()     vá BẢN SAO: thêm thư viện test (runtime EvoSuite, JUnit 4.12, vintage, listener)
    5. arrow_check()        arrow gen --project <bản sao> --c0 <bộ test> --no-repair --dry-run
                            → arrow tự đo baseline của bản đã vá, xác minh 5 bước, đo JaCoCo/PIT/tsDetect
    6. compare_baseline()   so test đạt ở baseline GỐC với baseline BẢN VÁ; test đạt ở bản gốc mà mất ở bản vá
                            → DEP_BREAKS_BASELINE (thư viện thêm vào làm hỏng test có sẵn) → tính là REGRESSION
Lấy repo theo cách A (chốt 26/11): mỗi việc tự clone vào home của worker, xong việc thì xóa (HomePool.give).
Tool dùng cho cả ba hệ thống: arrow 203fb8e (giống pilot, TN2).
"""
import hashlib          # sha256
import json             # đọc/ghi JSON
import os               # đường dẫn, biến môi trường
import re               # biểu thức chính quy
import shutil           # xóa/chép thư mục, tìm lệnh trên PATH
import subprocess       # chạy lệnh ngoài (arrow, mvn, java, git)
import sys              # sys.path, sys.argv, phiên bản Python
import tarfile          # giải nén file .tar do git archive tạo
import time             # đo thời gian

HERE = os.path.dirname(os.path.abspath(__file__))                         # thư mục ARROW_Experiment/3
EXP_DIR = os.path.dirname(HERE)                                           # thư mục ARROW_Experiment
TN1_DIR = os.path.join(EXP_DIR, "1")                                      # ARROW_Experiment/1 (kết quả và code TN1)
PILOT_DIR = os.path.join(EXP_DIR, "pilot")
ARROW_ROOT = os.path.dirname(EXP_DIR)                                     # gốc repo ARROW
sys.path.insert(0, TN1_DIR)                                               # cho phép "import tn1_run"
sys.path.insert(0, PILOT_DIR)
import tn1_run as base   # noqa: E402  dùng lại hàm của TN1: manifest, mô hình, home worker, dừng cả nhóm tiến trình

TOOLS_DIR = os.path.join(ARROW_ROOT, "tools")                             # nơi để jar EvoSuite
EVOSUITE_DIR = os.path.join(TOOLS_DIR, "evosuite-1.2.0")
EVOSUITE_JAR = os.path.join(EVOSUITE_DIR, "evosuite-1.2.0.jar")          # chương trình EvoSuite (sinh test)
EVOSUITE_RUNTIME_JAR = os.path.join(EVOSUITE_DIR, "evosuite-standalone-runtime-1.2.0.jar")   # thư viện để CHẠY test EvoSuite
EVOSUITE_RUNTIME = ("org.evosuite", "evosuite-standalone-runtime", "1.2.0")                   # tọa độ Maven của runtime
# URL và sha256 của 2 jar; bản 1.2.0 chỉ có trên GitHub release (không có trên Maven Central)
EVOSUITE_FILES = {
    EVOSUITE_JAR: ("https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-1.2.0.jar",
                   "0bc0062fdec70c35089ad736c9b88ccf87308c2f0aaaafe43176f3c4a89ddeba"),
    EVOSUITE_RUNTIME_JAR: ("https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-standalone-runtime-1.2.0.jar",
                           "cc856a6d02b391367d2a4516346c699c67e4b4a370203471df1f3d78b46b74dc"),
}
ARROW_COMMIT = "203fb8e"                 # bản tool bắt buộc (cùng bản với pilot và TN2)
TIMEOUT_ARROW_CHECK = 90 * 60            # tối đa 90 phút cho một lần arrow gen --c0 --no-repair có đo (PIT có thể lâu)
TIMEOUT_INSPECT = 45 * 60                # tối đa 45 phút cho arrow inspect (clone + build + baseline 2 lần)
TIMEOUT_BUILD = 30 * 60                  # tối đa 30 phút cho một lần build bản sao
IS_WINDOWS = base.IS_WINDOWS             # True nếu đang chạy trên Windows

now_iso = base.now_iso                   # giờ hiện tại dạng ISO (dùng lại của TN1)
sha256_file = base.sha256_file           # sha256 của một file
write_json = base.write_json             # ghi JSON an toàn (ghi file tạm rồi đổi tên)


def read_json(path, default=None):
    """Đọc file JSON; lỗi (không có file, hỏng) thì trả `default` thay vì dừng."""
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except (OSError, ValueError):
        return default


def append_jsonl(path, rec, lock=None):
    """Ghi thêm một dòng JSON vào cuối file .jsonl. `lock`: khóa luồng, để nhiều worker ghi không lẫn dòng."""
    line = json.dumps(rec, ensure_ascii=False) + "\n"      # ensure_ascii=False: giữ nguyên chữ tiếng Việt
    if lock:
        with lock:
            with open(path, "a", encoding="utf-8") as f:   # "a" = ghi nối vào cuối
                f.write(line)
    else:
        with open(path, "a", encoding="utf-8") as f:
            f.write(line)


def read_jsonl(path):
    """Đọc mọi dòng của file .jsonl thành danh sách dict (file chưa có → danh sách rỗng)."""
    out = []
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            out = [json.loads(line) for line in f if line.strip()]   # bỏ dòng trống
    return out


# ---------------------------------------------------------------------------
# Chạy tiến trình con: ghi log ra file, có giới hạn thời gian, dừng được cả nhóm tiến trình
# ---------------------------------------------------------------------------

def run_logged(cmd, cwd, log_path, timeout, env=None, stdin_devnull=True):
    """Chạy lệnh `cmd` trong thư mục `cwd`, ghi stdout+stderr vào `log_path`.
    Quá `timeout` giây thì giết cả cây tiến trình (mvn sinh nhiều tiến trình con). Trả về {exit_code, timed_out, seconds}."""
    os.makedirs(os.path.dirname(log_path), exist_ok=True)
    # tạo nhóm tiến trình mới để khi giết thì giết được cả các tiến trình con (Windows và macOS/Linux khác cách)
    group = ({"creationflags": subprocess.CREATE_NEW_PROCESS_GROUP} if IS_WINDOWS else {"start_new_session": True})
    start = time.time()
    with open(log_path, "w", encoding="utf-8", errors="replace") as log:
        log.write("$ " + " ".join(cmd) + f"\n# cwd={cwd}\n")              # đầu log: ghi lại lệnh và thư mục chạy
        log.flush()
        proc = subprocess.Popen(cmd, cwd=cwd, env=env, stdout=log, stderr=subprocess.STDOUT,
                                stdin=subprocess.DEVNULL if stdin_devnull else None, **group)
        with base.PROCS_LOCK:
            base.PROCS.add(proc)                    # ghi vào danh sách tiến trình đang chạy (để Ctrl+C giết được)
        timed_out = False
        try:
            proc.wait(timeout=timeout)              # chờ lệnh chạy xong
        except subprocess.TimeoutExpired:
            timed_out = True
            base.kill_tree(proc)                    # quá giờ: giết cả cây tiến trình
            proc.wait()
        finally:
            with base.PROCS_LOCK:
                base.PROCS.discard(proc)            # bỏ khỏi danh sách
        seconds = round(time.time() - start, 1)
        log.write(f"\n# exit={proc.returncode} timeout={'yes' if timed_out else 'no'} seconds={seconds}\n")   # cuối log
    return {"exit_code": None if timed_out else proc.returncode, "timed_out": timed_out, "seconds": seconds}


def tail(path, n=20):
    """n dòng cuối của file (dùng để ghi lý do lỗi vào kết quả)."""
    try:
        with open(path, encoding="utf-8", errors="replace") as f:
            return "".join(f.readlines()[-n:])
    except OSError:
        return ""


def java_env(jdk_home):
    """Biến môi trường để chạy bằng đúng JDK `jdk_home`: JAVA_HOME + đưa bin của JDK lên đầu PATH.
    Bỏ JAVA_TOOL_OPTIONS của máy để không ảnh hưởng kết quả."""
    env = dict(os.environ)
    env["JAVA_HOME"] = jdk_home
    env["PATH"] = os.path.join(jdk_home, "bin") + os.pathsep + env.get("PATH", "")
    env.pop("JAVA_TOOL_OPTIONS", None)
    return env


def java_exe(jdk_home):
    """Đường dẫn file chạy java của một JDK."""
    return os.path.join(jdk_home, "bin", "java.exe" if IS_WINDOWS else "java")


# ---------------------------------------------------------------------------
# Chuẩn bị EvoSuite trên máy: 2 jar đúng sha256; runtime cài vào ~/.m2 (Maven cần, vì không có trên Maven Central)
# ---------------------------------------------------------------------------

def ensure_evosuite():
    """Tải 2 jar EvoSuite 1.2.0 nếu thiếu, kiểm sha256; cài runtime vào kho Maven cục bộ. Trả về {tên file: sha256}."""
    import urllib.request
    os.makedirs(EVOSUITE_DIR, exist_ok=True)
    for path, (url, sha) in EVOSUITE_FILES.items():
        if not os.path.exists(path) or sha256_file(path) != sha:           # chưa có hoặc sai → tải lại
            print(f"Tải {os.path.basename(path)} ...")
            tmp = path + ".part"                                            # tải vào file tạm trước
            urllib.request.urlretrieve(url, tmp)
            if sha256_file(tmp) != sha:                                     # tải về sai nội dung → dừng
                os.remove(tmp)
                raise SystemExit(f"{os.path.basename(path)} tải về sai sha256. Dừng.")
            os.replace(tmp, path)                                           # đúng → đổi tên thành file thật
    g, a, v = EVOSUITE_RUNTIME
    repo = os.path.join(os.path.expanduser("~"), ".m2", "repository", *g.split("."), a, v)   # thư mục của runtime trong ~/.m2
    jar = os.path.join(repo, f"{a}-{v}.jar")
    if not os.path.exists(jar) or sha256_file(jar) != EVOSUITE_FILES[EVOSUITE_RUNTIME_JAR][1]:
        if os.path.isdir(repo):                         # xóa dấu "đã thử tải mà không thấy" (*.lastUpdated) của Maven
            for name in os.listdir(repo):
                if name.endswith(".lastUpdated"):
                    os.remove(os.path.join(repo, name))
        mvn = shutil.which("mvn") or shutil.which("mvn.cmd")
        if not mvn:
            raise SystemExit("Không thấy lệnh mvn trên PATH (cần để cài runtime EvoSuite vào ~/.m2).")
        # maven-install-plugin:install-file: chép jar vào ~/.m2 như thể tải từ kho về
        subprocess.run([mvn, "-B", "-q", "org.apache.maven.plugins:maven-install-plugin:3.1.1:install-file",
                        f"-Dfile={EVOSUITE_RUNTIME_JAR}", f"-DgroupId={g}", f"-DartifactId={a}", f"-Dversion={v}",
                        "-Dpackaging=jar", "-DgeneratePom=true"], check=True)
    return {os.path.basename(p): s for p, (_, s) in EVOSUITE_FILES.items()}


# ---------------------------------------------------------------------------
# Gọi arrow
# ---------------------------------------------------------------------------

def check_arrow(arrow):
    """Kiểm bản tool: commit phải bắt đầu bằng 203fb8e. Trả về (phiên bản, commit)."""
    version, commit = base.arrow_version(arrow)
    if not commit.startswith(ARROW_COMMIT):
        raise SystemExit(f"Bản tool là '{version}', TN3 cần {ARROW_COMMIT}. Chỉ đường bằng --arrow.")
    return version, commit


def run_arrow(arrow, args, home, log_base, timeout):
    """Chạy `arrow <args> --json --home <home>`; lưu stdout vào <log_base>.json, stderr vào <log_base>.err.
    Trả về (dữ liệu JSON hoặc None, mã thoát hoặc None nếu quá giờ, số giây)."""
    cmd = [arrow] + list(args) + ["--json", "--home", home]               # --json: arrow in kết quả dạng JSON
    group = ({"creationflags": subprocess.CREATE_NEW_PROCESS_GROUP} if IS_WINDOWS else {"start_new_session": True})
    start = time.time()
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, stdin=subprocess.DEVNULL,
                            text=True, encoding="utf-8", errors="replace", **group)
    with base.PROCS_LOCK:
        base.PROCS.add(proc)
    try:
        out, err = proc.communicate(timeout=timeout)                       # đọc hết stdout/stderr, chờ xong
        code = proc.returncode
    except subprocess.TimeoutExpired:
        base.kill_tree(proc)
        out, err = proc.communicate()
        code = None                                                        # None = quá giờ
    finally:
        with base.PROCS_LOCK:
            base.PROCS.discard(proc)
    seconds = round(time.time() - start, 1)
    os.makedirs(os.path.dirname(log_base), exist_ok=True)
    with open(log_base + ".json", "w", encoding="utf-8") as f:
        f.write(out)
    with open(log_base + ".err", "w", encoding="utf-8") as f:
        f.write("$ " + " ".join(cmd) + "\n" + err)
    try:
        data = json.loads(out)
    except json.JSONDecodeError:
        data = None                                                        # arrow không in JSON hợp lệ
    return data, code, seconds


def target_dir(run_dir, fqn):
    """Thư mục kết quả của một lớp trong một run arrow: runs/<id>/targets/<FQN>."""
    return os.path.join(run_dir, "targets", fqn)


def inspect(arrow, task, home, log_dir):
    """arrow inspect trên repo gốc (đúng commit). Trả về dict `snap`:
    status (OK / INFRA / BLOCKED), metadata, workspace (JDK, build tool, lệnh), baseline (danh sách test đạt/flaky/hỏng),
    cut (lớp cần test), test_classpath, run_dir."""
    log_base = os.path.join(log_dir, f"{task['task_id']}__inspect")
    data, code, seconds = run_arrow(arrow, ["inspect", task["class_fqn"], "--repo", task["repo_url"],
                                            "--ref", task["checkout_sha"]], home, log_base, TIMEOUT_INSPECT)
    snap = {"task_id": task["task_id"], "inspect_seconds": seconds, "inspect_exit": code}
    if code is None:                                                       # quá giờ
        return dict(snap, status="INFRA", reason="SCRIPT_TIMEOUT")
    if not data or "analysis" not in data:                                 # arrow báo lỗi (mạng, clone, ...)
        err = (data or {}).get("error") or {}
        return dict(snap, status="INFRA", reason=err.get("code") or "NO_JSON", message=err.get("message"))
    ana = data["analysis"]
    outcome = (ana.get("outcome") or {}).get("outcome")
    snap.update(run_dir=data.get("run_dir"), metadata=ana.get("metadata"), workspace=ana.get("workspace"),
                baseline=ana.get("baseline"), cut=ana.get("cut"), outcome=ana.get("outcome"))
    if outcome != "OK":                                                    # dự án build/baseline không qua
        return dict(snap, status="BLOCKED", reason=(ana.get("outcome") or {}).get("code"))
    cp_file = os.path.join(target_dir(data["run_dir"], task["class_fqn"]), "block1", "test_classpath.txt")
    with open(cp_file, encoding="utf-8") as f:
        snap["test_classpath"] = [line.strip() for line in f if line.strip()]   # classpath test arrow đã ghi
    snap["status"] = "OK"
    return snap


# ---------------------------------------------------------------------------
# Bản sao của repo
# ---------------------------------------------------------------------------

def mirror_path(home, repo_url):
    """Mirror (git bare, chỉ commit cần dùng) mà arrow tạo khi inspect: <home>/mirrors/github.com/<owner>/<repo>.git"""
    m = re.match(r"https?://([^/]+)/(.+?)(?:\.git)?/?$", repo_url)         # tách host và owner/repo
    if not m:
        raise ValueError(repo_url)
    return os.path.join(home, "mirrors", m.group(1), *m.group(2).split("/")) + ".git"


def checkout(home, task, dest):
    """Giải nén đúng commit từ mirror của arrow vào `dest` (xóa `dest` nếu đã có).
    Dùng `git archive` vì mirror là bản nông (shallow), không fetch được. Bản sao không có .git:
    arrow sẽ băm cây thư mục (source_tree_hash), nên bản đã vá có baseline riêng, không lẫn với bản gốc."""
    mirror = mirror_path(home, task["repo_url"])
    if not os.path.isdir(mirror):
        raise RuntimeError(f"không có mirror {mirror} (chạy inspect trước)")
    if os.path.exists(dest):
        shutil.rmtree(dest, ignore_errors=True)
    os.makedirs(dest)
    tar_path = dest.rstrip("/\\") + ".tar"
    subprocess.run(["git", "--git-dir", mirror, "archive", "--format=tar", "-o", tar_path, task["checkout_sha"]],
                   check=True, stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
    with tarfile.open(tar_path) as tf:
        if sys.version_info >= (3, 12):
            tf.extractall(dest, filter="tar")         # Python mới: bộ lọc an toàn khi giải nén
        else:
            tf.extractall(dest)
    os.remove(tar_path)
    return dest


def build_root(copy, snap):
    """Thư mục gốc build (nơi có pom/settings.gradle gốc) trong bản sao."""
    return os.path.normpath(os.path.join(copy, snap["workspace"].get("build_root") or "."))


def module_dir(copy, snap):
    """Thư mục module chứa lớp cần test, suy từ paths.source_root (…/src/main/java) của arrow."""
    src = (snap["metadata"].get("paths") or {}).get("source_root") or ""
    for suffix in ("src/main/java", "src/main/kotlin", "src"):
        if src == suffix or src.endswith("/" + suffix):
            return os.path.normpath(os.path.join(copy, src[: len(src) - len(suffix)] or "."))   # cắt đuôi → thư mục module
    return os.path.normpath(os.path.join(copy, (snap["metadata"].get("project_structure") or {}).get("module_root") or "."))


def build_tool(snap):
    """"maven" hoặc "gradle"."""
    return snap["workspace"]["build_tool"]


def _executable(copy, snap):
    """Lệnh build: đường dẫn tuyệt đối (mvn/gradle cài trên máy) hoặc tương đối gốc project (wrapper mvnw/gradlew)."""
    ws = snap["workspace"]
    exe = ws["executable"]
    if ws.get("executable_in_repo"):                  # wrapper nằm trong repo → ghép với bản sao
        exe = os.path.join(copy, exe)
        if not IS_WINDOWS and os.path.exists(exe):
            os.chmod(exe, os.stat(exe).st_mode | 0o111)   # git archive có thể mất quyền chạy → bật lại
    return exe


def _compile_command(copy, snap):
    """Lệnh compile mà arrow dùng khi đo baseline. Với Gradle: bỏ --init-script (file nằm trong work/ của arrow, đã bị
    xóa) và task :arrowClasspath của init script đó; classpath lấy từ test_classpath.txt arrow đã ghi."""
    cmd = list(snap["baseline"]["commands"]["compile"])
    cmd[0] = _executable(copy, snap)                  # thay lệnh build bằng lệnh trong bản sao
    if build_tool(snap) == "gradle":
        out, skip = [], False
        for x in cmd:
            if skip:                                  # bỏ giá trị đi sau --init-script
                skip = False
                continue
            if x == "--init-script":
                skip = True
                continue
            if x.startswith("--init-script=") or x.endswith(":arrowClasspath") or x == "arrowClasspath":
                continue
            out.append(x)
        cmd = out
    return cmd


def build_copy(copy, snap, log_path):
    """Build bản sao bằng đúng lệnh compile của arrow (cùng JDK).
    Trả về {ok, copy, production_classes:[thư mục class], classpath:[jar/thư mục], classpath_missing, seconds, exit_code}."""
    cmd = _compile_command(copy, snap)
    cwd = os.path.normpath(os.path.join(copy, snap["baseline"].get("working_dir") or "."))
    res = run_logged(cmd, cwd, log_path, TIMEOUT_BUILD, env=java_env(snap["workspace"]["jdk_home"]))
    mod = module_dir(copy, snap)
    out = dict(res, ok=res["exit_code"] == 0, copy=copy)
    if build_tool(snap) == "maven":
        prod = [os.path.join(mod, "target", "classes")]                   # nơi Maven để class mặc định
        if not os.path.isdir(prod[0]):      # pom đặt <outputDirectory> khác (vd. src/main/webapp/WEB-INF/classes)
            prod = _maven_output_dir(log_path, mod) or prod
    else:
        prod = [p for p in (os.path.join(mod, "build", "classes", "java", "main"),
                            os.path.join(mod, "build", "resources", "main")) if os.path.isdir(p)]
    cp = _read_classpath_file(mod, cwd) if build_tool(snap) == "maven" else None   # Maven: arrow-cp.txt do lệnh compile ghi
    if cp is None:              # Gradle, hoặc không thấy arrow-cp.txt → dùng classpath arrow đã ghi lúc inspect
        cp = []
        for p in snap["test_classpath"]:
            if not os.path.isabs(p):            # đường dẫn tương đối gốc project (thử thêm build root cho chắc)
                cands = [os.path.normpath(os.path.join(copy, p)), os.path.normpath(os.path.join(build_root(copy, snap), p))]
                p = next((x for x in cands if os.path.exists(x)), cands[0])
            cp.append(p)
    test_out = [os.path.join(mod, "target", "test-classes"), os.path.join(mod, "build", "classes", "java", "test")]
    cp = [p for p in cp if os.path.normpath(p) not in map(os.path.normpath, test_out)]   # bỏ class test có sẵn của dự án
    out["classpath_missing"] = [p for p in cp if not os.path.exists(p)]   # ghi lại mục không tồn tại (để gỡ lỗi)
    cp = [p for p in cp if os.path.exists(p)]        # EvoSuite dừng nếu classpath có mục không tồn tại
    out.update(production_classes=prod, classpath=cp)
    if out["ok"] and not all(os.path.isdir(p) for p in prod[:1]):
        out.update(ok=False, reason="không thấy thư mục class production sau khi build")
    return out


def _maven_output_dir(log_path, mod):
    """Tìm thư mục class production trong log build Maven ("Compiling N source files to <dir>", bỏ test-classes)."""
    try:
        with open(log_path, encoding="utf-8", errors="replace") as f:
            text = f.read()
    except OSError:
        return None
    mod_n = os.path.normpath(mod)
    for m in re.finditer(r"Compiling \d+ source files? to (\S+)", text):
        d = os.path.normpath(m.group(1))
        if os.path.isdir(d) and d.startswith(mod_n + os.sep) and "test-classes" not in d:   # thuộc module, không phải test
            return [d]
    return None


def _read_classpath_file(mod, cwd):
    """Đọc arrow-cp.txt (classpath test do lệnh compile của arrow ghi) ở thư mục module hoặc thư mục chạy."""
    for p in (os.path.join(mod, "arrow-cp.txt"), os.path.join(cwd, "arrow-cp.txt")):
        if os.path.exists(p):
            with open(p, encoding="utf-8") as f:
                text = f.read().strip()
            return [x for x in text.split(os.pathsep) if x]               # tách theo ":" (Windows: ";")
    return None


# ---------------------------------------------------------------------------
# Vá bản sao: thêm thư viện test (Maven: pom.xml của module; Gradle: build.gradle của module).
# Sửa pom bằng thao tác trên chuỗi (không dùng thư viện XML) để giữ nguyên định dạng và comment của file.
# ---------------------------------------------------------------------------

# Một "thẻ" XML: comment, CDATA, chỉ thị <?..?>, hoặc thẻ mở/đóng/tự đóng (nhóm 1 = "/", nhóm 2 = tên, nhóm 4 = "/")
_TAG = re.compile(r"<!--.*?-->|<!\[CDATA\[.*?\]\]>|<\?.*?\?>|<(/?)([\w:.-]+)([^>]*?)(/?)>", re.S)


def _xml_elements(text):
    """Duyệt các thẻ của pom (bỏ comment/CDATA). Sinh ra (loại, tên, vị trí đầu, vị trí cuối, ngăn xếp thẻ cha).
    Thẻ mở mang ngăn xếp TRƯỚC khi vào thẻ; thẻ đóng mang ngăn xếp còn chứa chính thẻ đó."""
    stack = []
    for m in _TAG.finditer(text):
        if m.group(2) is None:                        # comment/CDATA/chỉ thị → bỏ qua
            continue
        closing, name, selfclose = m.group(1) == "/", m.group(2), m.group(4) == "/"
        if closing:
            yield "close", name, m.start(), m.end(), list(stack)
            if stack and stack[-1] == name:
                stack.pop()
        else:
            yield ("empty" if selfclose else "open"), name, m.start(), m.end(), list(stack)
            if not selfclose:
                stack.append(name)


def _find(text, path):
    """Vị trí (cuối thẻ mở, đầu thẻ đóng) của phần tử theo đường dẫn tuyệt đối, ví dụ ['project','build','plugins'].
    Không có thì trả None."""
    open_end = None
    for kind, name, s, e, stack in _xml_elements(text):
        if kind == "open" and stack + [name] == path and open_end is None:
            open_end = e
        elif kind == "close" and open_end is not None and stack == path and name == path[-1]:
            return open_end, s
    return None


def _indent_before(text, pos):
    """Khoảng trắng đầu dòng của dòng chứa vị trí `pos` (để chèn khối mới thụt lề cho đẹp)."""
    line_start = text.rfind("\n", 0, pos) + 1
    return re.match(r"[ \t]*", text[line_start:]).group(0)


def _insert_in(text, path, xml_block, create_parents=True):
    """Chèn `xml_block` vào cuối phần tử `path`; chưa có phần tử (và cha) thì tạo."""
    found = _find(text, path)
    if found:
        _, close_at = found
        ind = _indent_before(text, close_at)
        return text[:close_at] + "  " + xml_block.replace("\n", "\n" + ind + "  ") + "\n" + ind + text[close_at:]
    if not create_parents or len(path) < 2:
        raise ValueError(f"không có <{'/'.join(path)}> trong pom")
    wrapped = f"<{path[-1]}>\n  " + xml_block.replace("\n", "\n  ") + f"\n</{path[-1]}>"   # bọc bằng thẻ còn thiếu
    return _insert_in(text, path[:-1], wrapped, create_parents)            # chèn vào phần tử cha (đệ quy)


def maven_add_dependency(pom_path, group, artifact, version, scope="test", type_=None, force=False):
    """Thêm <dependency> vào <project><dependencies> của pom. Đã có artifactId đó thì bỏ qua (trừ khi force=True).
    type_="pom" dùng cho starter của ChatUniTest. Trả về True nếu đã thêm."""
    with open(pom_path, encoding="utf-8") as f:
        text = f.read()
    if not force and re.search(r"<artifactId>\s*" + re.escape(artifact) + r"\s*</artifactId>", text):
        return False
    block = (f"<dependency>\n  <groupId>{group}</groupId>\n  <artifactId>{artifact}</artifactId>\n"
             f"  <version>{version}</version>\n" + (f"  <type>{type_}</type>\n" if type_ else "") +
             f"  <scope>{scope}</scope>\n</dependency>")
    text = _insert_in(text, ["project", "dependencies"], "<!-- TN3 -->\n" + block)   # comment "TN3" để biết dòng do script thêm
    with open(pom_path, "w", encoding="utf-8", newline="") as f:
        f.write(text)
    return True


def _gradle_version(snap):
    """Phiên bản Gradle của dự án dạng (lớn, nhỏ); không rõ thì coi là mới (99, 0)."""
    v = (snap["metadata"].get("build_tool") or {}).get("version") or ""
    m = re.match(r"(\d+)\.(\d+)", v)
    return (int(m.group(1)), int(m.group(2))) if m else (99, 0)


def gradle_build_file(mod):
    """File build của module Gradle (build.gradle.kts hoặc build.gradle)."""
    for name in ("build.gradle.kts", "build.gradle"):
        p = os.path.join(mod, name)
        if os.path.exists(p):
            return p
    return None


def gradle_add_dependency(mod, snap, group, artifact, version, jar=None):
    """Thêm vào cuối build file của module một khối dependencies. Có `jar` thì dùng files('<jar>') (cho thư viện không có
    trên kho Maven mà dự án khai báo, như runtime EvoSuite 1.2.0); không thì "group:artifact:version"."""
    path = gradle_build_file(mod)
    if path is None:
        raise ValueError(f"không có build.gradle trong {mod}")
    conf = "testImplementation" if _gradle_version(snap) >= (3, 4) else "testCompile"   # Gradle < 3.4 chưa có testImplementation
    if jar:
        ref = jar.replace("\\", "/")
        dep = f'files("{ref}")' if path.endswith(".kts") else f"files('{ref}')"
    else:
        gav = f"{group}:{artifact}:{version}"
        dep = f'("{gav}")' if path.endswith(".kts") else f"'{gav}'"
    if path.endswith(".kts"):                         # cú pháp Kotlin
        block = f'\n\n// TN3\ndependencies {{\n    "{conf}"{dep if dep.startswith("(") else "(" + dep + ")"}\n}}\n'
    else:                                             # cú pháp Groovy
        block = f"\n\n// TN3\ndependencies {{\n    {conf} {dep}\n}}\n"
    with open(path, "a", encoding="utf-8") as f:
        f.write(block)
    return True


def add_test_dependency(copy, snap, group, artifact, version, jar=None):
    """Thêm một thư viện test vào module chứa lớp cần test của BẢN SAO. Trả về đường dẫn file đã sửa (tương đối).
    Maven: <dependency> scope test (thư viện phải có trong ~/.m2, xem ensure_evosuite); Gradle: files(jar) nếu có jar."""
    mod = module_dir(copy, snap)
    if build_tool(snap) == "maven":
        pom = os.path.join(mod, "pom.xml")
        maven_add_dependency(pom, group, artifact, version)
        return os.path.relpath(pom, copy)
    gradle_add_dependency(mod, snap, group, artifact, version, jar=jar)
    return os.path.relpath(gradle_build_file(mod), copy)


# ---------------------------------------------------------------------------
# Môi trường cho bộ EvoSuite (chốt 26/11/2026), theo hướng dẫn chính thức của EvoSuite ("Running EvoSuite tests with
# Maven"): (1) runtime evosuite-standalone-runtime; (2) JUnit 4.12 (EvoSuite chỉ sinh test JUnit 4, runtime không kèm
# JUnit; JUnit 4.5 chưa có @Rule); (3) listener org.evosuite.runtime.InitializingListener trong Surefire. Thêm: module
# chạy JUnit Platform (JUnit 5) mà chưa có vintage → junit-vintage-engine cùng phiên bản platform, nếu không Surefire
# không chạy test JUnit 4.
# ---------------------------------------------------------------------------

EVOSUITE_SETUP = "v3"                     # nhãn cách dựng, ghi vào mỗi dòng kết quả (v1, v2 là bản dựng thử đã bỏ)
JUNIT4_MIN = (4, 12)                      # JUnit 4 nhỏ hơn bản này thì nâng lên 4.12
_OLD_JUNIT_JAR = re.compile(r"junit(?:-dep)?-(4\.\d+)[\w.-]*\.jar")       # tên jar JUnit 4 (junit-4.x.jar, junit-dep-4.x.jar)
JUNIT4 = ("junit", "junit", "4.12")
HAMCREST = ("org.hamcrest", "hamcrest-core", "1.3")                       # thư viện JUnit 4.12 cần
EVOSUITE_LISTENER = "org.evosuite.runtime.InitializingListener"


def m2_jar(group, artifact, version):
    """Đường dẫn jar trong kho Maven cục bộ ~/.m2."""
    return os.path.join(os.path.expanduser("~"), ".m2", "repository", *group.split("."), artifact, version,
                        f"{artifact}-{version}.jar")


def ensure_junit4_jars():
    """Bảo đảm junit 4.12 + hamcrest-core 1.3 có trong ~/.m2 (script đo JaCoCo/PIT cần khi module không có JUnit 4)."""
    jars = [m2_jar(*JUNIT4), m2_jar(*HAMCREST)]
    if not all(os.path.exists(j) for j in jars):
        mvn = shutil.which("mvn") or shutil.which("mvn.cmd")
        if not mvn:
            raise SystemExit("Không thấy lệnh mvn trên PATH (cần để tải junit 4.12 vào ~/.m2).")
        subprocess.run([mvn, "-B", "-q", "org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get",
                        "-Dartifact=" + ":".join(JUNIT4)], check=True)    # tải junit (kéo theo hamcrest)
    missing = [j for j in jars if not os.path.exists(j)]
    if missing:
        raise SystemExit(f"Không tải được {missing}")
    return jars


def junit_on_classpath(cp):
    """Xem classpath test của module đang có gì: JUnit 4 (bản nào), JUnit 5 (jupiter engine), vintage, TestNG."""
    names = [os.path.basename(p) for p in cp]         # chỉ cần tên file jar
    platform = None
    for n in names:
        m = re.fullmatch(r"junit-platform-engine-(1\.\d+\.[\w.-]+)\.jar", n)
        if m:
            platform = m.group(1)                     # phiên bản JUnit Platform, ví dụ 1.8.2
    j4 = [tuple(int(x) for x in m.group(1).split(".")) for n in names for m in [_OLD_JUNIT_JAR.fullmatch(n)] if m]
    return {"junit4": bool(j4),                                         # có JUnit 4 không
            "junit4_version": ".".join(map(str, min(j4))) if j4 else None,
            "junit4_old": bool(j4) and min(j4) < JUNIT4_MIN,             # JUnit 4 cũ hơn 4.12
            "jupiter_engine": any(n.startswith("junit-jupiter-engine-") for n in names),   # module chạy JUnit 5
            "vintage": any(n.startswith("junit-vintage-engine-") for n in names),
            "testng": any(re.fullmatch(r"testng-\d[\w.-]*\.jar", n) for n in names),
            "platform": platform}


def vintage_version(platform):
    """Phiên bản junit-vintage-engine đi cùng junit-platform 1.x.y: 1.0.y → 4.12.y, 1.x.y → 5.x.y."""
    m = re.fullmatch(r"1\.(\d+)\.(.+)", platform or "")
    if not m:
        return None
    return f"4.12.{m.group(2)}" if m.group(1) == "0" else f"5.{m.group(1)}.{m.group(2)}"


def _plugin_spans(text, parent):
    """Các <plugin> con trực tiếp của `parent` (đường dẫn tuyệt đối): danh sách (vị trí đầu, vị trí cuối, artifactId)."""
    spans, start, art, art_open = [], None, None, None
    for kind, name, s, e, stack in _xml_elements(text):
        if name == "plugin" and kind == "open" and stack == parent:
            start, art = s, None                                       # bắt đầu một <plugin>
        elif name == "plugin" and kind == "close" and stack == parent + ["plugin"] and start is not None:
            spans.append((start, e, art))                              # kết thúc <plugin>
            start = None
        elif start is not None and name == "artifactId":
            if kind == "open" and stack == parent + ["plugin"]:        # <artifactId> trực tiếp của plugin (không phải của dependency bên trong)
                art_open = e
            elif kind == "close" and stack == parent + ["plugin", "artifactId"] and art_open is not None:
                art, art_open = text[art_open:s].strip(), None         # đọc tên artifactId
    return spans


def maven_add_surefire_listener(pom_path, listener=EVOSUITE_LISTENER):
    """Thêm listener vào cấu hình Surefire của module. Module đã khai báo Surefire thì sửa tại chỗ; chưa thì thêm <plugin>
    mới (Maven gộp với cấu hình của pom cha / pluginManagement; combine.children="append" để không xóa property của pom cha)."""
    with open(pom_path, encoding="utf-8") as f:
        text = f.read()
    prop = f"<property>\n  <name>listener</name>\n  <value>{listener}</value>\n</property>"
    spans = [sp for sp in _plugin_spans(text, ["project", "build", "plugins"]) if sp[2] == "maven-surefire-plugin"]
    if spans:
        s, e, _ = spans[0]
        sub = text[s:e]                                                # đoạn <plugin>…</plugin> của Surefire
        m = re.search(r"(<name>\s*listener\s*</name>\s*<value>)(.*?)(</value>)", sub, re.S)
        if m:                                   # đã có listener → nối thêm (Surefire nhận danh sách cách nhau dấu phẩy)
            sub = sub[:m.start(2)] + m.group(2).strip() + "," + listener + sub[m.end(2):]
        elif _find(sub, ["plugin", "configuration", "properties"]):    # đã có <properties> → thêm một <property>
            sub = _insert_in(sub, ["plugin", "configuration", "properties"], "<!-- TN3 -->\n" + prop)
        else:                                                          # chưa có → thêm <properties> (tạo <configuration> nếu thiếu)
            block = '<properties combine.children="append">\n  ' + prop.replace("\n", "\n  ") + "\n</properties>"
            sub = _insert_in(sub, ["plugin", "configuration"], "<!-- TN3 -->\n" + block)
        text = text[:s] + sub + text[e:]
    else:                                                              # module chưa khai Surefire → thêm plugin mới
        plugin = ("<plugin>\n  <groupId>org.apache.maven.plugins</groupId>\n  <artifactId>maven-surefire-plugin</artifactId>\n"
                  '  <configuration>\n    <properties combine.children="append">\n      '
                  + prop.replace("\n", "\n      ") + "\n    </properties>\n  </configuration>\n</plugin>")
        text = _insert_in(text, ["project", "build", "plugins"], "<!-- TN3 -->\n" + plugin)
    with open(pom_path, "w", encoding="utf-8", newline="") as f:
        f.write(text)


def evosuite_setup(copy, snap, test_cp):
    """Vá bản sao "evo" theo hướng dẫn của EvoSuite. `test_cp`: classpath test của module (lấy từ bản "orig" đã build).
    Trả về {setup, patched_files, added, listener, junit_cp, extra_cp}; extra_cp = jar cần thêm khi script đo JaCoCo/PIT."""
    mod = module_dir(copy, snap)
    tool = build_tool(snap)
    jn = junit_on_classpath(test_cp)                                   # module đang có JUnit gì
    patched, added, extra = set(), [], [EVOSUITE_RUNTIME_JAR]          # file đã sửa, thư viện đã thêm, jar cho đo
    # (1) runtime EvoSuite: luôn thêm
    patched.add(add_test_dependency(copy, snap, *EVOSUITE_RUNTIME, jar=EVOSUITE_RUNTIME_JAR))
    added.append(":".join(EVOSUITE_RUNTIME))
    deps = []
    # (2) JUnit 4.12 khi thiếu JUnit 4 hoặc có bản cũ hơn 4.12 (Maven: phụ thuộc khai trực tiếp thắng phụ thuộc kéo theo)
    if not jn["junit4"] or jn["junit4_old"]:
        deps.append(JUNIT4)
        extra += ensure_junit4_jars()
    # (2b) module chạy JUnit 5 mà chưa có vintage → thêm vintage đúng phiên bản platform
    if jn["jupiter_engine"] and not jn["vintage"]:
        vv = vintage_version(jn["platform"])
        if not vv:
            raise RuntimeError(f"không suy được phiên bản junit-vintage-engine từ platform {jn['platform']}")
        deps.append(("org.junit.vintage", "junit-vintage-engine", vv))
    for g, a, v in deps:
        if tool == "maven":
            pom = os.path.join(mod, "pom.xml")
            maven_add_dependency(pom, g, a, v, force=True)             # force: pom có thể nhắc junit ở chỗ khác (exclusion, ...)
            patched.add(os.path.relpath(pom, copy))
        else:
            gradle_add_dependency(mod, snap, g, a, v)
            patched.add(os.path.relpath(gradle_build_file(mod), copy))
        added.append(f"{g}:{a}:{v}")
    # (3) Listener: chỉ provider JUnit 4 của Surefire đọc property "listener" như RunListener của JUnit. Provider TestNG
    # đưa nó cho TestNG làm listener TestNG → JVM test sập; JUnit 5 không đọc; Gradle không có cách tương ứng.
    if tool == "maven" and not jn["jupiter_engine"] and not jn["testng"]:
        pom = os.path.join(mod, "pom.xml")
        maven_add_surefire_listener(pom)
        patched.add(os.path.relpath(pom, copy))
        listener = "surefire-junit4"
    else:
        listener = ("not-applicable-gradle" if tool != "maven" else
                    "not-applicable-junit-platform" if jn["jupiter_engine"] else "not-applicable-testng")
    return {"setup": EVOSUITE_SETUP, "patched_files": sorted(patched), "added": added, "listener": listener,
            "junit_cp": jn, "extra_cp": extra}


def measure_classpath(build, setup):
    """Classpath cho JaCoCo/PIT bằng script: nếu đã nâng JUnit 4 cũ thì bỏ jar junit/junit-dep cũ (để 4.12 không bị che)."""
    if not (setup.get("junit_cp") or {}).get("junit4_old"):
        return build
    return dict(build, classpath=[p for p in build["classpath"] if not _OLD_JUNIT_JAR.fullmatch(os.path.basename(p))])


# ---------------------------------------------------------------------------
# Xác minh + đo một bộ test có sẵn bằng arrow (cùng 5 bước, cùng JaCoCo/PIT/tsDetect như bộ ARROW)
# ---------------------------------------------------------------------------

# Thứ tự trạng thái khi báo cáo (trượt càng sớm càng thấp)
STEP_STATES = ["COMPILE", "UNKNOWN", "DISCOVERY", "RUNTIME", "ASSERTION", "REGRESSION", "VALID"]


def arrow_check(arrow, task, project, c0_file, label, home, log_dir, name, metrics=True, timeout=TIMEOUT_ARROW_CHECK,
                model="gpt6luna", pit=True):
    """Đưa một bộ test có sẵn qua arrow: arrow gen --project <bản sao> --c0 <file> --no-repair --dry-run.
    metrics=False → --no-metrics (chỉ xác minh); pit=False → --no-pit (đo JaCoCo, tsDetect nhưng không PIT).
    `model` chỉ để qua bước kiểm cấu hình của arrow: với --no-repair không có lượt gọi mô hình nào.
    Trả về dict kết quả (một dòng jsonl)."""
    args = ["gen", task["class_fqn"],
            "--project", project,                     # chạy trên thư mục bản sao (không clone repo)
            "--c0", c0_file,                          # bộ test có sẵn, không sinh mới
            "--c0-model", label,                      # nhãn nguồn của bộ test (evosuite, chatunitest-<mô hình>)
            "--model", model, "--no-repair",          # không sửa
            "--dry-run",                              # không ghi file test vào bản sao sau khi xong
            "--yes"]                                  # không hỏi xác nhận
    if not metrics:
        args.append("--no-metrics")
    elif not pit:
        args.append("--no-pit")
    data, code, seconds = run_arrow(arrow, args, home, os.path.join(log_dir, name), timeout)
    return parse_gen(data, code, seconds, task)


def parse_gen(data, code, seconds, task):
    """Đọc kết quả `arrow gen --json` và các file trong run_dir thành một dict phẳng."""
    rec = {"exit_code": code, "seconds": seconds}
    if code is None:                                  # quá giờ
        return dict(rec, status="INFRA", result="SCRIPT_TIMEOUT")
    if data is None:                                  # arrow không in JSON
        return dict(rec, status="INFRA", result="NO_JSON")
    if "error" in data and not data.get("targets"):   # arrow báo lỗi trước khi chạy lớp (cấu hình, mạng, ...)
        err = data["error"] or {}
        return dict(rec, status="INFRA", result=err.get("code") or "ERROR", error_message=err.get("message"))
    run_dir = data.get("run_dir")
    t = (data.get("targets") or [{}])[0]              # kết quả của lớp (mỗi lần chỉ chạy 1 lớp)
    rec.update(run_id=data.get("run_id"), run_dir=run_dir, result=t.get("result"), c0_state=t.get("c0_state"),
               rounds_run=t.get("rounds_run"), success_round=t.get("success_round"), stop_reason=t.get("stop_reason"),
               error_code=(t.get("error") or {}).get("code"), error_message=(t.get("error") or {}).get("message"))
    tdir = target_dir(run_dir, task["class_fqn"]) if run_dir else None
    res = read_json(os.path.join(tdir, "result.json"), {}) if tdir else {}     # chỉ số đo, token, thời gian từng khối
    rec.update(metrics=res.get("metrics"), metrics_skipped=res.get("metrics_skipped"), tools=res.get("tools"),
               tokens=res.get("tokens"), llm_calls=res.get("llm_calls"), block_seconds=res.get("seconds"),
               test_fqn=res.get("test_fqn"))
    cand = read_json(os.path.join(tdir, "block2", "candidate.json"), {}) if tdir else {}   # thông tin bộ test đầu vào
    rec.update(valid_output=cand.get("valid_output"), invalid_reason=cand.get("invalid_reason"),
               test_method_count=cand.get("test_method_count"), assertion_count=cand.get("assertion_count"))
    v0 = read_json(os.path.join(tdir, "block3", "verify_c0.json"), {}) if tdir else {}   # kết quả 5 bước xác minh
    rec.update(verify_state=v0.get("state"), tests_passed=v0.get("tests_passed"), regressions=v0.get("regressions"),
               module_flaky=v0.get("module_flaky"), steps=v0.get("steps"))
    # INFRA = lỗi hạ tầng (không tính là kết quả); DONE = có kết quả thật
    rec["status"] = "INFRA" if rec["result"] in ("INFRA", "CANCELLED") or rec.get("stop_reason") == "infra" else "DONE"
    return rec


def baseline_of(run_dir, fqn):
    """Baseline trong một run arrow: (test đạt ở mọi lần, test chập chờn, test hỏng ở mọi lần); None nếu không có."""
    b = read_json(os.path.join(target_dir(run_dir, fqn), "block1", "baseline.json"))
    if not b:
        return None
    return set(b.get("passing_in_all_runs") or []), set(b.get("flaky") or []), set(b.get("failing_in_all_runs") or [])


def compare_baseline(orig, patched):
    """So baseline bản gốc (inspect) với bản đã thêm thư viện (run arrow_check). Chốt 26/11/2026.
    SAME: không mất test đạt nào; DEP_BREAKS_BASELINE: có test đạt ở bản gốc mà ở bản vá không đạt hoặc biến mất
    (không tính test chập chờn ở một trong hai bên)."""
    if orig is None or patched is None:
        return {"baseline_check": "UNKNOWN", "lost": [], "gained": []}
    o_pass, o_flaky, _ = orig
    p_pass, p_flaky, _ = patched
    lost = sorted(o_pass - p_pass - o_flaky - p_flaky)               # đạt ở gốc, mất ở bản vá
    gained = sorted(p_pass - o_pass - o_flaky - p_flaky)             # ngược lại (chỉ ghi số để tham khảo)
    return {"baseline_check": "DEP_BREAKS_BASELINE" if lost else "SAME", "lost": lost[:50], "lost_count": len(lost),
            "gained_count": len(gained), "orig_passing": len(o_pass), "patched_passing": len(p_pass)}


def final_state(rec, check):
    """Trạng thái cuối của một bộ test ngoài (EvoSuite/ChatUniTest): kết quả 5 bước của arrow, rồi áp so baseline.
    Bộ VALID mà thư viện thêm vào làm mất test đạt → REGRESSION (dòng có cờ baseline_check = DEP_BREAKS_BASELINE)."""
    state = rec.get("result")
    if rec.get("valid_output") is False:                              # file đưa vào không phải bộ test hợp lệ
        return "INVALID_OUTPUT"
    if state == "VALID" and check.get("baseline_check") == "DEP_BREAKS_BASELINE":
        return "REGRESSION"
    return state


# ---------------------------------------------------------------------------
# Đo bằng script (chốt 26/11/2026, ngoại lệ ghi vào RP2), cùng tham số với arrow 203fb8e (PitSetup.cs / PitXmlParser):
#   - EvoSuite: bộ gốc (separateClassLoader = true) thì agent JaCoCo không thấy lớp (đo ra 0%); bản false thì PIT không
#     thấy test. Vì vậy JaCoCo đo trên bản false (jacoco_measure), PIT đo trên bản gốc (pit_measure) với minion JDK dự án.
#   - ChatUniTest trên dự án JUnit 4: arrow không nạp pitest-junit5-plugin → script đo lại với plugin="junit5".
# ---------------------------------------------------------------------------

PIT_TIMEOUT = 1800                        # 30 phút: quality.pit_timeout_seconds mặc định của arrow
# Các jar lõi của PIT trong <home>/tools/pit-1.17.4 (arrow tải sẵn khi `arrow tools install`)
PIT_CORE = ["pitest", "pitest-entry", "pitest-command-line", "pitest-html-report", "commons-text", "commons-lang3",
            "jopt-simple"]
# jar của framework test phải có trên classpath khởi động PIT (như arrow)
PIT_FRAMEWORK_PREFIXES = ("junit-platform-", "junit-jupiter-", "opentest4j-", "apiguardian-", "testng-", "jcommander-")


def pit_jars(home):
    """Danh sách jar lõi của PIT trong home của worker."""
    d = os.path.join(home, "tools", "pit-1.17.4")
    if not os.path.isdir(d):
        raise RuntimeError(f"chưa có PIT trong {d} (arrow tải khi đo lần đầu)")
    names = sorted(os.listdir(d))
    jars = []
    for core in PIT_CORE:
        hit = [n for n in names if n.endswith(".jar") and re.fullmatch(re.escape(core) + r"-\d[\w.]*\.jar", n)]
        if not hit:
            raise RuntimeError(f"thiếu {core} trong {d}")
        jars.append(os.path.join(d, hit[0]))
    return jars


def pit_jdk(snap, base_cfg):
    """JDK chạy PIT, như PitSetup.ChooseJdk của arrow: JDK của dự án nếu ≥ 11, không thì JDK ≥ 11 nhỏ nhất trên máy."""
    if (snap["workspace"].get("jdk_major") or 0) >= 11:
        return snap["workspace"]["jdk_home"]
    homes = ((base_cfg.get("java") or {}).get("jdk_homes") or {})     # các JDK khai trong cấu hình arrow
    for major in sorted(int(k) for k in homes):
        if major >= 11:
            return homes[str(major)]
    return None


def compile_test(code, test_fqn, classpath, jdk_home, out_dir, log_path):
    """Biên dịch một file test bằng javac của JDK dự án → out_dir/classes. Trả về thư mục class hoặc None nếu lỗi."""
    pkg, _, name = test_fqn.rpartition(".")                           # tách package và tên lớp
    src = os.path.join(out_dir, "src", *pkg.split(".")) if pkg else os.path.join(out_dir, "src")
    os.makedirs(src, exist_ok=True)
    path = os.path.join(src, name + ".java")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(code)
    classes = os.path.join(out_dir, "classes")
    os.makedirs(classes, exist_ok=True)
    javac = os.path.join(jdk_home, "bin", "javac.exe" if IS_WINDOWS else "javac")
    argfile = os.path.join(out_dir, "javac-cp.txt")                   # classpath dài → để trong file tham số (@file)
    with open(argfile, "w", encoding="utf-8") as f:
        f.write("-cp\n\"" + os.pathsep.join(classpath).replace("\\", "/") + "\"\n")
    res = run_logged([javac, "-nowarn", "-encoding", "UTF-8", "-d", classes, "@" + argfile, path], out_dir, log_path,
                     600, env=java_env(jdk_home))
    return classes if res["exit_code"] == 0 else None


def parse_mutations(xml_path, cut_fqn):
    """Đọc mutations.xml của PIT như PitXmlParser.Parse của arrow:
    score = (killed + timed_out + memory_error) / total; strength = killed / (total - no_coverage)."""
    import xml.etree.ElementTree as ET
    muts = ET.parse(xml_path).getroot().iter("mutation")
    rows = [(m.get("status"), (m.findtext("mutatedClass") or "").strip()) for m in muts]   # (trạng thái, lớp bị đột biến)
    count = lambda s: sum(1 for st, _ in rows if st == s)            # đếm số đột biến theo trạng thái
    total, killed, timed, mem, nocov = len(rows), count("KILLED"), count("TIMED_OUT"), count("MEMORY_ERROR"), count("NO_COVERAGE")
    out_scope = sorted({c for _, c in rows if c != cut_fqn and not c.startswith(cut_fqn + "$")})   # đột biến ngoài lớp cần test
    return {"total": total, "killed": killed, "survived": count("SURVIVED"), "no_coverage": nocov, "timed_out": timed,
            "memory_error": mem, "run_error": count("RUN_ERROR"),
            "score": None if total == 0 else round((killed + timed + mem) / total, 4),
            "test_strength": None if total - nocov == 0 else round(killed / (total - nocov), 4),
            "out_of_scope": out_scope}


JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=UTC"   # như arrow


def classpath_args(cp, out_dir):
    """Tham số ["-cp", ...] cho java (JDK 8 không nhận @argfile). Quá dài cho dòng lệnh Windows (32.767 ký tự) thì
    tạo một jar rỗng có manifest Class-Path trỏ tới các mục, rồi -cp tới jar đó."""
    joined = os.pathsep.join(cp)
    if len(joined) < 30000:
        return ["-cp", joined]
    import pathlib
    import zipfile
    jar = os.path.join(out_dir, "classpath.jar")
    urls = []
    for p in cp:
        u = pathlib.Path(os.path.abspath(p)).as_uri()                 # đường dẫn → URL file://
        urls.append(u + ("/" if os.path.isdir(p) and not u.endswith("/") else ""))   # thư mục phải kết thúc bằng /
    lines, cur = [], "Class-Path: " + " ".join(urls)
    while len(cur.encode("utf-8")) > 70:            # dòng manifest tối đa 72 byte, dòng nối bắt đầu bằng 1 dấu cách
        cut = 70
        while len(cur[:cut].encode("utf-8")) > 70:
            cut -= 1
        lines.append(cur[:cut])
        cur = " " + cur[cut:]
    lines.append(cur)
    with zipfile.ZipFile(jar, "w") as z:
        z.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\r\n" + "\r\n".join(lines) + "\r\n\r\n")
    return ["-cp", jar]


def parse_jacoco_csv(csv_path, cut_fqn):
    """Đọc báo cáo CSV của JaCoCo như JacocoCsvParser của arrow: cộng dòng của lớp cần test và các lớp lồng
    (cột CLASS = tên lớp hoặc bắt đầu bằng 'tên.'). Trả về tỷ lệ line/branch/method, hoặc None nếu không thấy lớp."""
    import csv as _csv
    pkg, _, simple = cut_fqn.rpartition(".")
    counters, found = {}, False
    with open(csv_path, encoding="utf-8") as f:
        for row in _csv.DictReader(f):
            cls = (row.get("CLASS") or "").strip()
            if (row.get("PACKAGE") or "").strip() != pkg or not (cls == simple or cls.startswith(simple + ".")):
                continue                                              # dòng của lớp khác
            found = True
            for kind in ("LINE", "BRANCH", "METHOD"):
                for part in ("COVERED", "MISSED"):
                    k = f"{kind}_{part}"
                    counters[k] = counters.get(k, 0) + int(row.get(k) or 0)
    if not found:
        return None

    def ratio(kind):
        cov = counters.get(kind + "_COVERED", 0)
        tot = cov + counters.get(kind + "_MISSED", 0)
        return None if tot == 0 else round(cov / tot, 4)            # tỷ lệ phủ, làm tròn 4 chữ số
    return {"line": ratio("LINE"), "branch": ratio("BRANCH"), "method": ratio("METHOD"), "counters": counters}


def jacoco_measure(snap, build, code, test_fqn, extra_cp, home, out_dir, log_dir, name):
    """JaCoCo cho bộ EvoSuite (bản separateClassLoader = false): chỉ chạy đúng bộ test này bằng JUnitCore với JDK dự án,
    cùng option agent (includes=<FQN>:<FQN>$*) và cùng cách đọc CSV như arrow. Trả về {status, line, branch, method}."""
    fqn = snap["cut"]["fqn"]
    t0 = time.time()
    tools = os.path.join(home, "tools", "jacoco-0.8.12")
    agent, cli = os.path.join(tools, "jacocoagent.jar"), os.path.join(tools, "jacococli.jar")
    if not (os.path.exists(agent) and os.path.exists(cli)):
        return {"status": "FAILED", "reason": f"thiếu JaCoCo trong {tools}"}
    jdk = snap["workspace"]["jdk_home"]
    prod = build["production_classes"][0]
    module_cp = [p for p in build["classpath"] if p] + list(extra_cp)   # classpath module + jar thêm (runtime, junit 4.12)
    classes = compile_test(code, test_fqn, [prod] + module_cp, jdk, out_dir, os.path.join(log_dir, name + "__jacoco_javac.log"))
    if not classes:
        return {"status": "FAILED", "reason": "không biên dịch được bộ test cho JaCoCo", "seconds": round(time.time() - t0, 1)}
    cp = [classes] + build["production_classes"] + [e for e in module_cp if e not in build["production_classes"]]
    exec_file = os.path.join(out_dir, "jacoco.exec")                   # file dữ liệu phủ do agent ghi
    env = java_env(jdk)
    env["JAVA_TOOL_OPTIONS"] = JAVA_TOOL_OPTIONS
    # java -javaagent:jacocoagent.jar=... -cp ... org.junit.runner.JUnitCore <lớp test>
    run = run_logged([java_exe(jdk), f"-javaagent:{agent}=destfile={exec_file},append=true,includes={fqn}:{fqn}$*",
                      "-Djdk.attach.allowAttachSelf=true"] + classpath_args(cp, out_dir) + ["org.junit.runner.JUnitCore", test_fqn],
                     module_dir(build["copy"], snap), os.path.join(log_dir, name + "__jacoco_test.log"), 15 * 60, env=env)
    if run["exit_code"] != 0:
        return {"status": "FAILED", "seconds": round(time.time() - t0, 1),
                "reason": "bộ test không đạt hết khi đo JaCoCo" if not run["timed_out"] else "quá giờ khi đo JaCoCo"}
    csv_path = os.path.join(out_dir, "jacoco.csv")
    src_root = os.path.join(build["copy"], snap["metadata"]["paths"]["source_root"])
    # jacococli report: đổi jacoco.exec thành báo cáo CSV
    rep = run_logged([java_exe(jdk), "-jar", cli, "report", exec_file, "--classfiles", prod, "--sourcefiles", src_root,
                      "--csv", csv_path], out_dir, os.path.join(log_dir, name + "__jacoco_report.log"), 300, env=java_env(jdk))
    if rep["exit_code"] != 0 or not os.path.exists(csv_path):
        return {"status": "FAILED", "reason": "jacococli report lỗi", "seconds": round(time.time() - t0, 1)}
    cov = parse_jacoco_csv(csv_path, fqn)
    if cov is None:
        return {"status": "FAILED", "reason": f"báo cáo JaCoCo không có {fqn}", "seconds": round(time.time() - t0, 1)}
    return dict(cov, status="OK", seconds=round(time.time() - t0, 1), tool="jacoco 0.8.12")


def pit_measure(snap, build, code, test_fqn, extra_cp, home, base_cfg, out_dir, log_dir, name, minion_jdk=None, plugin=None):
    """Biên dịch bộ test rồi chạy PIT với đúng tham số của arrow. Trả về {status: OK/FAILED/SCOPE, score, test_strength, ...}.
    minion_jdk: JDK chạy test của PIT (--jvmPath). Bộ EvoSuite dùng JDK của dự án, vì runtime EvoSuite 1.2.0 không chạy
    được trên JDK 11 với lớp sinh cho JDK 8 ("Cannot load/analyze class java.util.SubList").
    plugin: "junit5" khi đo bộ ChatUniTest trên dự án JUnit 4 (arrow không tự nạp pitest-junit5-plugin)."""
    fqn = snap["cut"]["fqn"]
    t0 = time.time()
    try:
        jars = pit_jars(home)
    except RuntimeError as e:
        return {"status": "FAILED", "reason": str(e)}
    jdk = pit_jdk(snap, base_cfg)                                       # JDK chạy tiến trình chính của PIT
    if not jdk:
        return {"status": "FAILED", "reason": "PIT cần JDK 11+"}
    prod = build["production_classes"][0]
    module_cp = [p for p in build["classpath"] if p] + list(extra_cp)
    classes = compile_test(code, test_fqn, [prod] + module_cp, snap["workspace"]["jdk_home"], out_dir,
                           os.path.join(log_dir, name + "__javac.log"))
    if not classes:
        return {"status": "FAILED", "reason": "không biên dịch được bộ test cho PIT", "seconds": round(time.time() - t0, 1)}
    cp = [prod, classes]                                               # classpath cho minion: class production, class test, thư viện
    for e in module_cp:
        if e not in cp:
            cp.append(e)
    cp_file = os.path.join(out_dir, "arrow-pit-classpath.txt")        # PIT đọc classpath từ file (--classPathFile)
    with open(cp_file, "w", encoding="utf-8") as f:
        f.write("\n".join(cp) + "\n")
    plugin_jars = []
    if plugin:                      # như PitSetup.PluginTool: junit5 → pitest-junit5-plugin, testng → pitest-testng-plugin
        d = os.path.join(home, "tools", "pit-1.17.4")
        plugin_jars = [os.path.join(d, n) for n in sorted(os.listdir(d)) if n.startswith(f"pitest-{plugin}-plugin")]
        if not plugin_jars:
            return {"status": "FAILED", "reason": f"thiếu pitest-{plugin}-plugin trong {d}"}
    launch = jars + plugin_jars + [c for c in cp if os.path.basename(c).startswith(PIT_FRAMEWORK_PREFIXES)]
    report = os.path.join(out_dir, "arrow-pit-report")
    src_root = os.path.join(build["copy"], snap["metadata"]["paths"]["source_root"])
    cmd = [java_exe(jdk), "-cp", os.pathsep.join(launch), "org.pitest.mutationtest.commandline.MutationCoverageReport",
           "--reportDir", report,
           "--targetClasses", f"{fqn},{fqn}$*",       # chỉ đột biến lớp cần test và lớp lồng
           "--targetTests", test_fqn,                  # chỉ chạy bộ test này
           "--classPathFile", cp_file,
           "--mutators", "DEFAULTS", "--threads", "1", "--timestampedReports=false",
           "--outputFormats", "XML", "--failWhenNoMutations=false", "--sourceDirs", src_root]
    if minion_jdk:
        cmd += ["--jvmPath", java_exe(minion_jdk)]     # minion (chạy test) dùng JDK khác tiến trình chính
    env = java_env(jdk)
    env["JAVA_TOOL_OPTIONS"] = JAVA_TOOL_OPTIONS
    log = os.path.join(log_dir, name + "__pit.log")
    res = run_logged(cmd, module_dir(build["copy"], snap), log, PIT_TIMEOUT, env=env)
    xml = os.path.join(report, "mutations.xml")
    seconds = round(time.time() - t0, 1)
    if res["exit_code"] != 0 or not os.path.exists(xml):
        return {"status": "FAILED", "seconds": seconds,
                "reason": "PIT quá giờ" if res["timed_out"] else f"PIT lỗi (mã {res['exit_code']}): {tail(log, 8)[-600:]}"}
    m = parse_mutations(xml, fqn)
    if m["out_of_scope"]:                              # có đột biến ngoài lớp cần test → kết quả không đúng phạm vi
        return dict(m, status="SCOPE", seconds=seconds)
    try:
        shutil.copyfile(xml, os.path.join(log_dir, name + "__mutations.xml"))   # lưu lại báo cáo để kiểm sau
    except OSError:
        pass
    return dict(m, status="OK", seconds=seconds, tool="pit 1.17.4", jdk=os.path.basename(os.path.dirname(os.path.dirname(jdk))))


# ---------------------------------------------------------------------------
# Khung chạy của các script TN3: thư mục kết quả, home của worker, chạy song song, chạy tiếp khi bị ngắt
# ---------------------------------------------------------------------------

import platform        # noqa: E402   tên hệ điều hành (ghi vào file lock)
import queue           # noqa: E402   hàng đợi home cho các worker
import threading       # noqa: E402   khóa ghi file
from concurrent.futures import ThreadPoolExecutor  # noqa: E402   chạy song song nhiều luồng

JSONL_LOCK = threading.Lock()                       # một khóa chung cho việc ghi các file kết quả .jsonl


def out_root(machine, smoke):
    """Thư mục kết quả: results/<máy> khi chạy thật, results/<máy>-smoke khi chạy khói."""
    return os.path.join(HERE, "results", machine + ("-smoke" if smoke else ""))


def default_homes_root(machine, smoke):
    """Nơi đặt home của các worker. Windows: ở gốc ổ đĩa cho đường dẫn ngắn (giới hạn 260 ký tự), như TN2."""
    name = f"tn3-{machine}{'-smoke' if smoke else ''}"
    if IS_WINDOWS:
        return os.path.join(os.environ.get("SystemDrive", "C:") + os.sep, "arrow-homes", name)
    return os.path.join(out_root(machine, smoke), "homes")


def default_work_root(machine, smoke):
    """Nơi đặt thư mục làm việc (bản sao dự án). Windows: ở gốc ổ đĩa, lý do như trên."""
    name = f"tn3-{machine}{'-smoke' if smoke else ''}"
    if IS_WINDOWS:
        return os.path.join(os.environ.get("SystemDrive", "C:") + os.sep, "arrow-work", name)
    return os.path.join(out_root(machine, smoke), "work")


class HomePool:
    """Mỗi worker một ARROW_HOME riêng (cấu hình chép từ home gốc, khóa API dùng chung qua liên kết),
    để các worker không giẫm lên run của nhau. Lấy repo theo cách A: trả home thì xóa mirrors/ và work/."""

    def __init__(self, base_home, root, prefix, n, arrow=None):
        self.q = queue.Queue()                          # hàng đợi các home đang rảnh
        for i in range(1, n + 1):
            h = os.path.join(root, f"{prefix}-w{i}")    # ví dụ homes/evo-w1, homes/evo-w2, ...
            base.prepare_home(base_home, h)             # chép config.json, liên kết thư mục secrets
            if arrow:                  # cài JaCoCo, PIT, tsDetect đúng sha256 vào home (script đo cần, kể cả khi arrow chạy --no-pit)
                subprocess.run([arrow, "tools", "install", "--home", h, "--plain"], check=True,
                               stdout=subprocess.DEVNULL, stdin=subprocess.DEVNULL)
            self.q.put(h)

    def borrow(self):
        """Lấy một home rảnh (chờ nếu tất cả đang bận)."""
        return self.q.get()

    def give(self, home):
        """Trả home: xóa mirrors/ và work/ (cách A: repo tải về được xóa sau mỗi việc), giữ runs/, cache/, tools/."""
        base.clean_home(home)
        self.q.put(home)


def run_jobs(jobs, workers, fn, pool):
    """Chạy fn(việc, home) cho mọi việc, `workers` luồng song song. Ctrl+C: dừng mọi tiến trình con rồi thoát."""
    def one(job):
        if base.STOP.is_set():                          # đã có lệnh dừng (Ctrl+C, hết tiền) → bỏ việc còn lại
            return
        home = pool.borrow()
        try:
            fn(job, home)
        except Exception as e:                          # lỗi của script ở một việc: báo, không dừng cả lô
            print(f"[!] {job.get('task_id')}: {type(e).__name__}: {e}", flush=True)
            import traceback
            traceback.print_exc()
        finally:
            pool.give(home)                             # luôn trả home, kể cả khi lỗi
    try:
        with ThreadPoolExecutor(max_workers=workers) as ex:
            list(ex.map(one, jobs))                     # list(...) để chờ mọi việc xong
    except KeyboardInterrupt:
        base.STOP.set()
        with base.PROCS_LOCK:
            for p in list(base.PROCS):
                base.kill_tree(p)                       # giết mọi tiến trình con đang chạy
        raise SystemExit("Đã dừng (Ctrl+C). Chạy lại đúng lệnh cũ để chạy tiếp.")


def latest_by(path, keyfn):
    """Đọc file kết quả, giữ dòng MỚI NHẤT cho mỗi khóa (chạy lại một việc thì dòng sau thắng)."""
    latest = {}
    for rec in read_jsonl(path):
        latest[keyfn(rec)] = rec
    return latest


def lock_invocation(root, script, extra):
    """Ghi lần chạy này vào results/<máy>/tn3.lock.json (script, giờ, máy, sha256 cấu hình, tham số dòng lệnh, ...).
    Nếu các lần trước dùng commit tool khác thì dừng: không trộn kết quả của hai bản tool."""
    path = os.path.join(root, "tn3.lock.json")
    lock = read_json(path, {"invocations": []})
    cfg_path = os.path.join(HERE, "tn3_config.json")
    inv = {"script": script, "started_at": now_iso(), "os": platform.platform(), "cpu_count": os.cpu_count(),
           "config_sha256": sha256_file(cfg_path), "argv": sys.argv}
    inv.update(extra)
    old = {i.get("tool_commit") for i in lock["invocations"] if i.get("tool_commit")}
    if old and extra.get("tool_commit") and extra["tool_commit"] not in old:
        raise SystemExit(f"Các lần chạy trước dùng tool {sorted(old)}, lần này {extra['tool_commit']}. Không trộn hai bản.")
    lock["invocations"].append(inv)
    write_json(path, lock)


def load_config():
    """Đọc tn3_config.json."""
    with open(os.path.join(HERE, "tn3_config.json"), encoding="utf-8") as f:
        return json.load(f)


def select_classes(classes, part, task_ids=None, limit=None):
    """Chọn lớp để chạy: lớp của phần `part` (theo cột machine của tn3_classes.jsonl);
    --task chọn đúng các lớp đó (chạy khói); --limit lấy N lớp đầu của phần."""
    if task_ids:
        mine = [cl for cl in classes if cl["task_id"] in set(task_ids)]
        missing = set(task_ids) - {cl["task_id"] for cl in mine}
        if missing:
            raise SystemExit(f"Không có lớp {sorted(missing)} trong tn3_classes.jsonl")
        return mine
    parts = sorted({cl["machine"] for cl in classes})
    if part not in parts:
        raise SystemExit(f"Không có phần '{part}'. Các phần: {parts}")
    mine = [cl for cl in classes if cl["machine"] == part]
    return mine[:limit] if limit else mine


def snap_summary(snap):
    """Vài trường của inspect đáng ghi vào mỗi dòng kết quả: build tool, JDK, framework, vintage."""
    md = snap.get("metadata") or {}
    ws = snap.get("workspace") or {}
    return {"build_tool": ws.get("build_tool"), "jdk": ws.get("jdk_major"),
            "framework": (md.get("test_framework") or {}).get("name"),
            "vintage": (md.get("test_framework") or {}).get("vintage")}
