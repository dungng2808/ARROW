from __future__ import annotations

import hashlib
import json
import os
import socket
import sqlite3
import sys
import threading
from contextlib import AbstractContextManager
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable


SCHEMA_VERSION = 2


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def atomic_write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + ".tmp")
    with temporary.open("w", encoding="utf-8", newline="\n") as handle:
        json.dump(value, handle, ensure_ascii=False, indent=2, sort_keys=True)
        handle.write("\n")
        handle.flush()
        os.fsync(handle.fileno())
    temporary.replace(path)


def canonical_json(value: object) -> str:
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"), sort_keys=True)


def contract_digest(contract: dict[str, Any]) -> str:
    return hashlib.sha256(canonical_json(contract).encode("utf-8")).hexdigest()


def tool_fingerprint(root: Path) -> str:
    """Hash code and schemas that can affect a preflight decision."""
    digest = hashlib.sha256()
    paths = [root / "pyproject.toml"]
    paths.extend(sorted((root / "preflight").glob("*.py")))
    paths.extend(sorted((root / "schemas").glob("*.json")))
    for path in paths:
        relative = path.relative_to(root).as_posix().encode("utf-8")
        digest.update(len(relative).to_bytes(4, "big"))
        digest.update(relative)
        content = path.read_bytes()
        digest.update(len(content).to_bytes(8, "big"))
        digest.update(content)
    return digest.hexdigest()


class RunLockError(RuntimeError):
    pass


class RunLock(AbstractContextManager["RunLock"]):
    """A process-owned, automatically released lock for one run directory."""

    def __init__(self, path: Path):
        self.path = path
        self._handle = None

    def __enter__(self) -> "RunLock":
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self._handle = self.path.open("a+b")
        try:
            self._handle.seek(0)
            if self._handle.read(1) == b"":
                self._handle.seek(0)
                self._handle.write(b"0")
                self._handle.flush()
            self._handle.seek(0)
            if os.name == "nt":
                import msvcrt

                msvcrt.locking(self._handle.fileno(), msvcrt.LK_NBLCK, 1)
            else:
                import fcntl

                fcntl.flock(self._handle.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
        except (OSError, BlockingIOError) as exc:
            self._handle.close()
            self._handle = None
            raise RunLockError(f"Run is already active: {self.path.parent.parent}") from exc
        self._handle.seek(1)
        metadata = canonical_json({"pid": os.getpid(), "host": socket.gethostname(), "locked_at_utc": utc_now()})
        self._handle.truncate(1)
        self._handle.write(metadata.encode("utf-8"))
        self._handle.flush()
        return self

    def __exit__(self, exc_type, exc, traceback) -> None:
        if self._handle is None:
            return
        # Both msvcrt byte-range locks and flock locks are released when the
        # owning file descriptor closes, including abnormal process exit.
        self._handle.close()
        self._handle = None


class CheckpointError(RuntimeError):
    pass


SCHEMA = """
CREATE TABLE IF NOT EXISTS metadata (
  key TEXT PRIMARY KEY,
  value TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS sessions (
  session_id INTEGER PRIMARY KEY AUTOINCREMENT,
  started_at_utc TEXT NOT NULL,
  ended_at_utc TEXT,
  workers INTEGER NOT NULL,
  status TEXT NOT NULL,
  completed_before INTEGER NOT NULL,
  completed_after INTEGER
);
CREATE TABLE IF NOT EXISTS tasks (
  task_id TEXT PRIMARY KEY,
  ordinal INTEGER NOT NULL UNIQUE,
  repo_url TEXT NOT NULL,
  status TEXT NOT NULL CHECK(status IN ('PENDING','RUNNING','COMPLETED')),
  attempt_count INTEGER NOT NULL DEFAULT 0,
  started_at_utc TEXT,
  completed_at_utc TEXT,
  preflight_status TEXT,
  result_json TEXT
);
CREATE TABLE IF NOT EXISTS task_attempts (
  task_id TEXT NOT NULL,
  attempt_number INTEGER NOT NULL,
  session_id INTEGER NOT NULL,
  started_at_utc TEXT NOT NULL,
  ended_at_utc TEXT,
  status TEXT NOT NULL,
  PRIMARY KEY(task_id, attempt_number)
);
CREATE TABLE IF NOT EXISTS repo_cleanup (
  repo_url TEXT PRIMARY KEY,
  event_json TEXT NOT NULL,
  updated_at_utc TEXT NOT NULL
);
"""


class CheckpointStore(AbstractContextManager["CheckpointStore"]):
    def __init__(self, path: Path, connection: sqlite3.Connection):
        self.path = path
        self._connection = connection
        self._lock = threading.RLock()

    @classmethod
    def create(cls, path: Path, contract: dict[str, Any], candidates: Iterable[Any]) -> "CheckpointStore":
        path.parent.mkdir(parents=True, exist_ok=True)
        if path.exists():
            raise CheckpointError(f"Checkpoint already exists: {path}")
        connection = sqlite3.connect(path, timeout=30, check_same_thread=False)
        store = cls(path, connection)
        store._configure()
        connection.executescript(SCHEMA)
        payload = canonical_json(contract)
        connection.executemany(
            "INSERT INTO metadata(key, value) VALUES (?, ?)",
            [
                ("schema_version", str(SCHEMA_VERSION)),
                ("contract", payload),
                ("contract_sha256", contract_digest(contract)),
                ("created_at_utc", utc_now()),
            ],
        )
        connection.executemany(
            "INSERT INTO tasks(task_id, ordinal, repo_url, status) VALUES (?, ?, ?, 'PENDING')",
            [(item.task_id, ordinal, item.repo_url) for ordinal, item in enumerate(candidates)],
        )
        connection.commit()
        return store

    @classmethod
    def open(cls, path: Path) -> "CheckpointStore":
        if not path.is_file():
            raise CheckpointError(f"Run does not contain a resumable checkpoint: {path}")
        connection = sqlite3.connect(path, timeout=30, check_same_thread=False)
        connection.row_factory = sqlite3.Row
        store = cls(path, connection)
        store._configure()
        try:
            version = int(store._metadata("schema_version"))
        except (sqlite3.Error, TypeError, ValueError) as exc:
            connection.close()
            raise CheckpointError(f"Invalid checkpoint database: {path}") from exc
        if version != SCHEMA_VERSION:
            connection.close()
            raise CheckpointError(f"Unsupported checkpoint schema {version}; expected {SCHEMA_VERSION}")
        return store

    def _configure(self) -> None:
        self._connection.execute("PRAGMA journal_mode=WAL")
        self._connection.execute("PRAGMA synchronous=FULL")
        self._connection.execute("PRAGMA foreign_keys=ON")
        self._connection.row_factory = sqlite3.Row

    def _metadata(self, key: str) -> str:
        row = self._connection.execute("SELECT value FROM metadata WHERE key = ?", (key,)).fetchone()
        if row is None:
            raise CheckpointError(f"Checkpoint metadata is missing {key}")
        return str(row[0])

    @property
    def contract(self) -> dict[str, Any]:
        try:
            contract = json.loads(self._metadata("contract"))
        except json.JSONDecodeError as exc:
            raise CheckpointError("Checkpoint contract is not valid JSON") from exc
        if contract_digest(contract) != self._metadata("contract_sha256"):
            raise CheckpointError("Checkpoint contract digest mismatch")
        return contract

    def recover_abandoned(self) -> int:
        with self._lock, self._connection:
            rows = self._connection.execute("SELECT task_id, attempt_count FROM tasks WHERE status = 'RUNNING'").fetchall()
            for row in rows:
                self._connection.execute(
                    "UPDATE task_attempts SET status='ABANDONED', ended_at_utc=? WHERE task_id=? AND attempt_number=? AND status='RUNNING'",
                    (utc_now(), row["task_id"], row["attempt_count"]),
                )
            self._connection.execute("UPDATE tasks SET status='PENDING', started_at_utc=NULL WHERE status='RUNNING'")
            completed = self._connection.execute("SELECT COUNT(*) FROM tasks WHERE status='COMPLETED'").fetchone()[0]
            self._connection.execute(
                "UPDATE sessions SET status='ABANDONED', ended_at_utc=?, completed_after=? WHERE status='RUNNING'",
                (utc_now(), completed),
            )
        return len(rows)

    def begin_session(self, workers: int) -> int:
        completed = self.counts()["completed"]
        with self._lock, self._connection:
            cursor = self._connection.execute(
                "INSERT INTO sessions(started_at_utc, workers, status, completed_before) VALUES (?, ?, 'RUNNING', ?)",
                (utc_now(), workers, completed),
            )
        return int(cursor.lastrowid)

    def finish_session(self, session_id: int, status: str) -> None:
        completed = self.counts()["completed"]
        with self._lock, self._connection:
            self._connection.execute(
                "UPDATE sessions SET ended_at_utc=?, status=?, completed_after=? WHERE session_id=?",
                (utc_now(), status, completed, session_id),
            )

    def start_task(self, task_id: str, session_id: int) -> int:
        with self._lock, self._connection:
            row = self._connection.execute("SELECT status, attempt_count FROM tasks WHERE task_id=?", (task_id,)).fetchone()
            if row is None:
                raise CheckpointError(f"Unknown task_id: {task_id}")
            if row["status"] == "COMPLETED":
                raise CheckpointError(f"Task is already completed: {task_id}")
            attempt = int(row["attempt_count"]) + 1
            now = utc_now()
            self._connection.execute(
                "UPDATE tasks SET status='RUNNING', attempt_count=?, started_at_utc=? WHERE task_id=?",
                (attempt, now, task_id),
            )
            self._connection.execute(
                "INSERT INTO task_attempts(task_id, attempt_number, session_id, started_at_utc, status) VALUES (?, ?, ?, ?, 'RUNNING')",
                (task_id, attempt, session_id, now),
            )
        return attempt

    def complete_task(self, task_id: str, attempt: int, result: dict[str, Any]) -> None:
        if result.get("task_id") != task_id:
            raise CheckpointError(f"Result task_id mismatch for {task_id}")
        payload = canonical_json(result)
        with self._lock, self._connection:
            row = self._connection.execute("SELECT status, attempt_count FROM tasks WHERE task_id=?", (task_id,)).fetchone()
            if row is None or int(row["attempt_count"]) != attempt or row["status"] != "RUNNING":
                raise CheckpointError(f"Task attempt is not active: {task_id} attempt {attempt}")
            now = utc_now()
            self._connection.execute(
                "UPDATE tasks SET status='COMPLETED', completed_at_utc=?, preflight_status=?, result_json=? WHERE task_id=?",
                (now, str(result.get("preflight_status") or ""), payload, task_id),
            )
            self._connection.execute(
                "UPDATE task_attempts SET status='COMPLETED', ended_at_utc=? WHERE task_id=? AND attempt_number=?",
                (now, task_id, attempt),
            )

    def interrupt_task(self, task_id: str, attempt: int, status: str = "INTERRUPTED") -> None:
        with self._lock, self._connection:
            self._connection.execute(
                "UPDATE task_attempts SET status=?, ended_at_utc=? WHERE task_id=? AND attempt_number=? AND status='RUNNING'",
                (status, utc_now(), task_id, attempt),
            )
            self._connection.execute(
                "UPDATE tasks SET status='PENDING', started_at_utc=NULL WHERE task_id=? AND status='RUNNING' AND attempt_count=?",
                (task_id, attempt),
            )

    def counts(self) -> dict[str, int]:
        with self._lock:
            rows = self._connection.execute("SELECT status, COUNT(*) AS count FROM tasks GROUP BY status").fetchall()
        values = {"total": 0, "completed": 0, "running": 0, "pending": 0}
        for row in rows:
            key = str(row["status"]).lower()
            values[key] = int(row["count"])
            values["total"] += int(row["count"])
        return values

    def pending_task_ids(self) -> list[str]:
        with self._lock:
            rows = self._connection.execute("SELECT task_id FROM tasks WHERE status != 'COMPLETED' ORDER BY ordinal").fetchall()
        return [str(row[0]) for row in rows]

    def completed_task_ids(self) -> set[str]:
        with self._lock:
            rows = self._connection.execute("SELECT task_id FROM tasks WHERE status='COMPLETED'").fetchall()
        return {str(row[0]) for row in rows}

    def task_ids(self) -> list[str]:
        with self._lock:
            rows = self._connection.execute("SELECT task_id FROM tasks ORDER BY ordinal").fetchall()
        return [str(row[0]) for row in rows]

    def results(self) -> list[dict[str, Any]]:
        with self._lock:
            rows = self._connection.execute("SELECT task_id, result_json FROM tasks WHERE status='COMPLETED' ORDER BY task_id").fetchall()
        results: list[dict[str, Any]] = []
        for row in rows:
            try:
                value = json.loads(row["result_json"])
            except (TypeError, json.JSONDecodeError) as exc:
                raise CheckpointError(f"Invalid checkpoint result for {row['task_id']}") from exc
            if value.get("task_id") != row["task_id"]:
                raise CheckpointError(f"Checkpoint result task_id mismatch for {row['task_id']}")
            results.append(value)
        return results

    def status_counts(self) -> dict[str, int]:
        with self._lock:
            rows = self._connection.execute(
                "SELECT preflight_status, COUNT(*) AS count FROM tasks WHERE status='COMPLETED' GROUP BY preflight_status"
            ).fetchall()
        return {str(row["preflight_status"]): int(row["count"]) for row in rows}

    def all_tasks_for_repos(self) -> dict[str, list[str]]:
        with self._lock:
            rows = self._connection.execute("SELECT repo_url, task_id FROM tasks ORDER BY ordinal").fetchall()
        result: dict[str, list[str]] = {}
        for row in rows:
            result.setdefault(str(row["repo_url"]), []).append(str(row["task_id"]))
        return result

    def completed_repos(self) -> set[str]:
        with self._lock:
            rows = self._connection.execute(
                "SELECT repo_url FROM tasks GROUP BY repo_url HAVING SUM(CASE WHEN status='COMPLETED' THEN 0 ELSE 1 END)=0"
            ).fetchall()
        return {str(row[0]) for row in rows}

    def put_cleanup_event(self, event: dict[str, Any]) -> None:
        repo_url = str(event["repo_url"])
        with self._lock, self._connection:
            self._connection.execute(
                "INSERT INTO repo_cleanup(repo_url,event_json,updated_at_utc) VALUES(?,?,?) "
                "ON CONFLICT(repo_url) DO UPDATE SET event_json=excluded.event_json,updated_at_utc=excluded.updated_at_utc",
                (repo_url, canonical_json(event), utc_now()),
            )

    def cleanup_events(self) -> list[dict[str, Any]]:
        with self._lock:
            rows = self._connection.execute("SELECT event_json FROM repo_cleanup ORDER BY repo_url").fetchall()
        return [json.loads(row[0]) for row in rows]

    def cleanup_repo_urls(self) -> set[str]:
        settled: set[str] = set()
        for event in self.cleanup_events():
            status = event.get("status")
            reason = event.get("reason")
            if status in {"DELETED", "ABSENT"} or (status == "KEPT" and reason in {"keep_workspaces", "keep_repo_cache"}):
                settled.add(str(event["repo_url"]))
        return settled

    def sessions(self) -> list[dict[str, Any]]:
        with self._lock:
            rows = self._connection.execute("SELECT * FROM sessions ORDER BY session_id").fetchall()
        return [dict(row) for row in rows]

    def __exit__(self, exc_type, exc, traceback) -> None:
        self._connection.close()


def progress_record(store: CheckpointStore, run_status: str, session_id: int) -> dict[str, Any]:
    counts = store.counts()
    return {
        "checkpoint_schema_version": SCHEMA_VERSION,
        "run_status": run_status,
        **counts,
        "by_status": store.status_counts(),
        "session_id": session_id,
        "last_checkpoint_at_utc": utc_now(),
    }


def write_progress(run_root: Path, store: CheckpointStore, run_status: str, session_id: int) -> None:
    atomic_write_json(run_root / "reports" / "progress.json", progress_record(store, run_status, session_id))
