from __future__ import annotations

import os
import signal
import subprocess
import threading
from pathlib import Path


class RunCancelled(Exception):
    pass


def terminate_process_tree(process: subprocess.Popen[str]) -> None:
    if process.poll() is not None:
        return
    if os.name == "nt":
        subprocess.run(
            ["taskkill", "/PID", str(process.pid), "/T", "/F"],
            capture_output=True, text=True, encoding="utf-8", errors="replace",
        )
        process.kill()
    else:
        try:
            os.killpg(process.pid, signal.SIGKILL)
        except ProcessLookupError:
            pass


def run_capture(command: list[str], *, cwd: Path | None = None, check: bool = False,
                cancel_event: threading.Event | None = None) -> subprocess.CompletedProcess[str]:
    if cancel_event and cancel_event.is_set():
        raise RunCancelled()
    process = subprocess.Popen(
        command, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
        text=True, encoding="utf-8", errors="replace", start_new_session=os.name != "nt",
    )
    while True:
        if cancel_event and cancel_event.is_set():
            terminate_process_tree(process)
            process.communicate()
            raise RunCancelled()
        try:
            stdout, stderr = process.communicate(timeout=0.5)
            break
        except subprocess.TimeoutExpired:
            continue
    completed = subprocess.CompletedProcess(command, process.returncode, stdout, stderr)
    if check and completed.returncode:
        raise subprocess.CalledProcessError(completed.returncode, command, stdout, stderr)
    return completed
