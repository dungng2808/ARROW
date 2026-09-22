from __future__ import annotations

import json
import sqlite3
import hashlib
from pathlib import Path
from typing import Iterator

from .models import ClassCandidate
from .util import stable_task_id


SCHEMA = """
CREATE TABLE IF NOT EXISTS classes (
  task_id TEXT PRIMARY KEY,
  project_id TEXT NOT NULL,
  repo_url TEXT NOT NULL,
  class_path TEXT NOT NULL,
  class_fqn TEXT NOT NULL,
  class_name TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS evidence (
  task_id TEXT NOT NULL,
  source_json_path TEXT NOT NULL,
  source_json_sha256 TEXT NOT NULL,
  test_class_path TEXT NOT NULL,
  PRIMARY KEY(task_id, source_json_path),
  FOREIGN KEY(task_id) REFERENCES classes(task_id)
);
CREATE INDEX IF NOT EXISTS evidence_task_idx ON evidence(task_id);
CREATE TABLE IF NOT EXISTS input_rejections (
  source_json_path TEXT PRIMARY KEY,
  reason_code TEXT NOT NULL
);
"""


def resolve_dataset_dir(input_root: Path) -> Path:
    candidate = input_root / "dataset"
    return candidate if candidate.is_dir() else input_root


def _record_identity(path: Path) -> tuple[tuple[str, str, str, str, str] | None, dict | None, str | None, str | None]:
    try:
        content = path.read_bytes()
        raw = json.loads(content.decode("utf-8-sig"))
    except UnicodeDecodeError:
        return None, None, "INPUT_ENCODING_INVALID", None
    except json.JSONDecodeError:
        return None, None, "INPUT_JSON_INVALID", None
    repo = raw.get("repository") or {}
    focal = raw.get("focal_class") or {}
    project_id = path.parent.name
    repo_url = str(repo.get("url") or "").strip()
    class_path = str(focal.get("file") or "").replace("\\", "/").strip("/")
    class_name = str(focal.get("identifier") or "").strip()
    if not repo_url or not class_path or not class_name:
        return None, raw, "INPUT_SCHEMA_INVALID", None
    # The JSON has no package field.  This provisional FQN is upgraded from
    # the checked-out source before eligibility is decided.
    provisional_fqn = class_name
    return (project_id, repo_url, class_path, class_name, provisional_fqn), raw, None, hashlib.sha256(content).hexdigest()


def build_index(dataset_dir: Path, database: Path, *, limit: int = 0) -> tuple[int, int]:
    """Stream raw JSON into a disk-backed class index; never retain dataset in RAM."""
    database.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(database)
    conn.executescript(SCHEMA)
    json_count = class_count = 0
    try:
        # The standard dataset layout is dataset/<project-id>/<sample>.json.
        # Sorting one project at a time preserves deterministic input order
        # without materializing the full (potentially 780k-file) dataset.
        for project_dir in sorted((item for item in dataset_dir.iterdir() if item.is_dir()), key=lambda item: item.name):
            for path in sorted(project_dir.glob("*.json"), key=lambda item: item.name):
                if limit and json_count >= limit:
                    break
                identity, raw, rejection, content_sha256 = _record_identity(path)
                json_count += 1
                relative_path = path.relative_to(dataset_dir).as_posix()
                if identity is None:
                    conn.execute("INSERT OR REPLACE INTO input_rejections VALUES (?, ?)", (relative_path, rejection or "INPUT_SCHEMA_INVALID"))
                    continue
                project_id, repo_url, class_path, class_name, provisional_fqn = identity
                task_id = stable_task_id(project_id, repo_url, class_path, provisional_fqn)
                inserted = conn.execute(
                    "INSERT OR IGNORE INTO classes VALUES (?, ?, ?, ?, ?, ?)",
                    (task_id, project_id, repo_url, class_path, provisional_fqn, class_name),
                )
                if inserted.rowcount:
                    class_count += 1
                test_path = str((raw.get("test_class") or {}).get("file") or "").replace("\\", "/").strip("/")
                conn.execute(
                    "INSERT OR IGNORE INTO evidence VALUES (?, ?, ?, ?)",
                    (task_id, relative_path, content_sha256 or "", test_path),
                )
                if json_count % 1000 == 0:
                    conn.commit()
            if limit and json_count >= limit:
                break
        conn.commit()
    finally:
        conn.close()
    return json_count, class_count


def candidates(database: Path) -> Iterator[ClassCandidate]:
    conn = sqlite3.connect(database)
    conn.row_factory = sqlite3.Row
    try:
        rows = conn.execute("SELECT * FROM classes ORDER BY project_id, task_id")
        for row in rows:
            evidence = conn.execute(
                "SELECT source_json_path, source_json_sha256, test_class_path FROM evidence WHERE task_id = ? ORDER BY source_json_path",
                (row["task_id"],),
            ).fetchall()
            yield ClassCandidate(
                task_id=row["task_id"], project_id=row["project_id"], repo_url=row["repo_url"],
                class_path=row["class_path"], class_fqn=row["class_fqn"], class_name=row["class_name"],
                source_json_paths=tuple(item["source_json_path"] for item in evidence),
                source_json_sha256s=tuple(item["source_json_sha256"] for item in evidence),
                test_class_paths=tuple(sorted({item["test_class_path"] for item in evidence if item["test_class_path"]})),
            )
    finally:
        conn.close()


def evidence_paths(database: Path, task_id: str) -> list[str]:
    conn = sqlite3.connect(database)
    try:
        return [row[0] for row in conn.execute("SELECT source_json_path FROM evidence WHERE task_id = ? ORDER BY source_json_path", (task_id,))]
    finally:
        conn.close()


def input_rejections(database: Path) -> list[dict[str, str]]:
    conn = sqlite3.connect(database)
    try:
        return [
            {"source_json_path": row[0], "reason_code": row[1]}
            for row in conn.execute("SELECT source_json_path, reason_code FROM input_rejections ORDER BY source_json_path")
        ]
    finally:
        conn.close()
