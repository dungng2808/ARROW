"""Cài đặt máy để chạy TN1 (Qwen qua OpenRouter). Chạy được trên Windows và macOS.

Script làm thay các lệnh "arrow config set" (tránh lỗi gõ JSON trong PowerShell), rồi tự kiểm:
    1. arrow đúng bản d0f2b5c
    2. JDK 8, 11, 17, 21 (tự tìm; hoặc tự chỉ bằng --jdk8 ... --jdk21)
    3. bộ prompt của nhóm (prompts/override)
    4. nhà cung cấp OpenRouter + model qwen3coder-or (Amazon Bedrock, temperature 0)
    5. bản sao Maven Central (~/.m2/settings.xml, ~/.gradle/init.d/)
    6. khóa OpenRouter đã nhập chưa; arrow doctor; arrow models test

Cách chạy:
    python setup_tn1.py                                        (tự tìm JDK)
    python setup_tn1.py --jdk8 "C:\\Program Files\\Eclipse Adoptium\\jdk-8.0.432.6-hotspot" ...
    python setup_tn1.py --gpt                                  (thêm GPT-6 Luna qua TeamoRouter, cho pilot)
Khóa API KHÔNG nhập ở đây: tự chạy  arrow secrets set openrouter  (và  arrow secrets set teamo  nếu dùng --gpt)
"""
import argparse, glob, json, os, re, shutil, subprocess, sys

try:
    sys.stdout.reconfigure(encoding="utf-8")   # Windows: in được chữ tiếng Việt
except (AttributeError, ValueError):
    pass

HERE = os.path.dirname(os.path.abspath(__file__))                     # ARROW_Experiment/1
PROMPT_DIR = os.path.join(HERE, "prompts", "override")                # bộ prompt team-v1
ENV_DIR = os.path.normpath(os.path.join(HERE, "..", "..", "ARROW_dataset", "env"))
EXPECTED_COMMIT = "d0f2b5c"
PROVIDER = {"kind": "openai-compatible", "base_url": "https://openrouter.ai/api/v1", "secret_ref": "openrouter"}
MODEL = {"provider": "openrouter", "model_id": "qwen/qwen3-coder-30b-a3b-instruct",
         "expected_model_id_prefix": "qwen/qwen3-coder-30b-a3b-instruct",
         "params": {"temperature": 0, "max_tokens": 16384,
                    "provider": {"order": ["amazon-bedrock"], "allow_fallbacks": False}}}
# GPT-6 Luna: đúng cấu hình máy Dung đã dùng cho TN1 (không đổi tham số)
GPT_PROVIDER = {"kind": "openai-compatible", "base_url": "https://api.teamorouter.com/v1", "secret_ref": "teamo"}
GPT_MODEL = {"provider": "teamo", "model_id": "gpt-6-luna", "expected_model_id_prefix": "gpt-6-luna",
             "params": {"temperature": 0, "max_tokens": 16384, "seed": 42, "reasoning_effort": "none"}}
OK, BAD = "[OK]", "[X] "


def arrow(*args, check=True):
    """Chạy một lệnh arrow, trả về chữ in ra."""
    p = subprocess.run(["arrow", *args, "--plain"], capture_output=True, text=True, encoding="utf-8", errors="replace")
    if check and p.returncode != 0:
        raise SystemExit(f"{BAD} arrow {' '.join(args)} lỗi:\n{p.stdout}{p.stderr}")
    return p.stdout + p.stderr


def find_jdk(major):
    """Tìm thư mục JDK theo các chỗ cài thường gặp."""
    pats = []
    if os.name == "nt":
        for base in (r"C:\Program Files\Eclipse Adoptium", r"C:\Program Files\Java", r"C:\Program Files\Microsoft",
                     r"C:\Program Files\Zulu", r"C:\Program Files\Amazon Corretto"):
            pats += [os.path.join(base, f"jdk-{major}*"), os.path.join(base, f"jdk{major}*"),
                     os.path.join(base, f"jdk1.{major}*"), os.path.join(base, f"zulu-{major}*")]
    else:
        pats += [f"/Library/Java/JavaVirtualMachines/*{major}*/Contents/Home",
                 f"/usr/lib/jvm/java-{major}-openjdk*", f"/usr/lib/jvm/temurin-{major}*"]
    for pat in pats:
        for d in sorted(glob.glob(pat), reverse=True):
            if os.path.exists(os.path.join(d, "bin", "java.exe" if os.name == "nt" else "java")):
                return d
    return None


def main():
    ap = argparse.ArgumentParser(description="Cài đặt máy để chạy TN1 Qwen.")
    for m in (8, 11, 17, 21):
        ap.add_argument(f"--jdk{m}", help=f"thư mục JDK {m} (bỏ trống để tự tìm)")
    ap.add_argument("--gpt", action="store_true", help="thêm GPT-6 Luna qua TeamoRouter (khóa: arrow secrets set teamo)")
    args = ap.parse_args()

    # 1. arrow đúng bản
    if not shutil.which("arrow"):
        raise SystemExit(f"{BAD} Không thấy lệnh arrow. Thêm thư mục chứa arrow(.exe) vào PATH rồi mở lại cửa sổ lệnh.")
    ver = arrow("--version").strip()
    if EXPECTED_COMMIT not in ver:
        raise SystemExit(f"{BAD} Sai bản arrow: {ver}. Cần commit {EXPECTED_COMMIT}.")
    print(OK, ver)

    # 2. JDK
    for m in (8, 11, 17, 21):
        path = getattr(args, f"jdk{m}") or find_jdk(m)
        if not path:
            raise SystemExit(f"{BAD} Không tìm thấy JDK {m}. Cài JDK {m} rồi chạy lại, hoặc chỉ đường: --jdk{m} \"<thư mục>\"")
        arrow("config", "set", f"java.jdk_homes.{m}", path)
        print(OK, f"JDK {m}: {path}")

    # 3. bộ prompt
    if not os.path.isdir(os.path.join(PROMPT_DIR, "generation")):
        raise SystemExit(f"{BAD} Không thấy {PROMPT_DIR}. Lấy lại code (git checkout đúng commit).")
    arrow("config", "set", "prompts.override_dir", PROMPT_DIR)
    print(OK, f"Bộ prompt: {PROMPT_DIR}")

    # 4. OpenRouter + model
    arrow("config", "set", "llm.providers.openrouter", json.dumps(PROVIDER))
    arrow("config", "set", "llm.models.qwen3coder-or", json.dumps(MODEL))
    print(OK, "Model qwen3coder-or: qwen/qwen3-coder-30b-a3b-instruct · Amazon Bedrock · temperature 0")
    if args.gpt:
        arrow("config", "set", "llm.providers.teamo", json.dumps(GPT_PROVIDER))
        arrow("config", "set", "llm.models.gpt6luna", json.dumps(GPT_MODEL))
        print(OK, "Model gpt6luna: gpt-6-luna · TeamoRouter · temperature 0, seed 42, reasoning_effort none")

    # 5. bản sao Maven Central
    home = os.path.expanduser("~")
    m2 = os.path.join(home, ".m2", "settings.xml")
    os.makedirs(os.path.dirname(m2), exist_ok=True)
    if not os.path.exists(m2):
        shutil.copyfile(os.path.join(ENV_DIR, "settings.xml"), m2)
        print(OK, f"Đã chép {m2}")
    elif "maven-central.storage-download.googleapis.com" in open(m2, encoding="utf-8", errors="replace").read():
        print(OK, f"{m2} đã có bản sao Google")
    else:
        print(BAD, f"{m2} đã có sẵn nhưng chưa có bản sao Google: chép khối <mirror> từ {ENV_DIR}\\settings.xml vào")
    init_d = os.path.join(home, ".gradle", "init.d")
    os.makedirs(init_d, exist_ok=True)
    shutil.copyfile(os.path.join(ENV_DIR, "google-mirror.gradle"), os.path.join(init_d, "google-mirror.gradle"))
    print(OK, f"Đã chép {os.path.join(init_d, 'google-mirror.gradle')}")

    # 6. khóa, doctor, test model
    print("\n--- arrow doctor ---")
    print(arrow("doctor", check=False).strip())
    if not re.search(r"^openrouter\s", arrow("secrets", "list", check=False), re.M):
        print(f"\n{BAD} Chưa có khóa OpenRouter. Chạy:  arrow secrets set openrouter   rồi chạy lại script này.")
        return
    models = ["qwen3coder-or"]
    if args.gpt:
        if not re.search(r"^teamo\s", arrow("secrets", "list", check=False), re.M):
            print(f"\n{BAD} Chưa có khóa TeamoRouter. Chạy:  arrow secrets set teamo   rồi chạy lại script này (với --gpt).")
            return
        models.append("gpt6luna")
    ready = True
    for m in models:
        print(f"\n--- arrow models test {m} ---")
        out = arrow("models", "test", m, check=False).strip()
        print(out)
        if not ("OK" in out and "khớp" in out):
            ready = False
            print(f"{BAD} Model {m} chưa trả lời đúng. Kiểm khóa và số dư ({'TeamoRouter' if m == 'gpt6luna' else 'OpenRouter'}).")
    if ready:
        print(f"\n{OK} Máy đã sẵn sàng.")


if __name__ == "__main__":
    main()
