from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def stable_task_id(project_id: str, repo_url: str, class_path: str, class_fqn: str) -> str:
    identity = "\0".join((project_id, repo_url, class_path, class_fqn))
    return f"{project_id}_{hashlib.sha256(identity.encode()).hexdigest()[:16]}"


def normalize_java(text: str) -> str:
    """Remove Java comments/whitespace without mistaking comment markers in strings for comments."""
    out: list[str] = []
    index = 0
    state = "code"
    while index < len(text):
        current = text[index]
        following = text[index + 1] if index + 1 < len(text) else ""
        if state == "code" and current == "/" and following == "/":
            index = text.find("\n", index)
            if index < 0:
                break
            continue
        if state == "code" and current == "/" and following == "*":
            end = text.find("*/", index + 2)
            index = len(text) if end < 0 else end + 2
            continue
        if state == "code" and current in {"'", '"'}:
            state = current
        elif state in {"'", '"'} and current == "\\":
            out.append(current)
            if index + 1 < len(text):
                index += 1
                out.append(text[index])
            index += 1
            continue
        elif state == current:
            state = "code"
        if not current.isspace():
            out.append(current)
        index += 1
    return "".join(out)


def json_compact(value: object) -> str:
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"), sort_keys=True)


def safe_relative(path: Path, root: Path) -> str:
    try:
        return path.resolve().relative_to(root.resolve()).as_posix()
    except ValueError:
        return path.resolve().as_posix()
