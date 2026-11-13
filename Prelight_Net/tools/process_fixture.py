"""Tiến trình fixture cục bộ cho ProcessRunner, không dùng dataset hay mạng."""
import os
from pathlib import Path
import subprocess
import sys
import time

sys.stdout.reconfigure(encoding="utf-8")
sys.stderr.reconfigure(encoding="utf-8")
mode = sys.argv[1]
if mode == "success":
    print("dữ liệu", flush=True)
    print("lỗi thử", file=sys.stderr, flush=True)
elif mode == "echo":
    print(sys.argv[2])
elif mode == "environment":
    print(os.environ.get("PREFLIGHT_FIXTURE", "missing"))
    print("closed" if sys.stdin.read() == "" else "open")
elif mode == "exit":
    raise SystemExit(7)
elif mode in ("tree", "orphan"):
    child = subprocess.Popen([sys.executable, "-B", __file__, "sleep"])
    Path(sys.argv[2]).write_text(str(child.pid), encoding="ascii")
    print("ready", flush=True)
    if mode == "tree":
        time.sleep(30)
else:
    print("ready", flush=True)
    time.sleep(30)
