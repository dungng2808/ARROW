"""Portable class assignments; these are not upstream revision manifests."""
from __future__ import annotations

import hashlib
import json
from pathlib import Path

from .models import ClassCandidate


def assignment_record(item: ClassCandidate) -> dict:
    evidence = sorted(zip(item.source_json_paths, item.source_json_sha256s))
    digest = hashlib.sha256(json.dumps(evidence, separators=(",", ":"), ensure_ascii=True).encode()).hexdigest()
    return {"task_id": item.task_id, "repo_url": item.repo_url,
            "class_path": item.class_path, "class_name": item.class_name,
            "evidence_count": len(evidence), "evidence_sha256": digest}


def select_shard(items: list[ClassCandidate], path: Path) -> tuple[list[ClassCandidate], dict]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or data.get("schema_version") != 1:
        raise ValueError("Unsupported shard schema_version")
    rows = data.get("classes")
    if not isinstance(rows, list) or not rows or data.get("class_count") != len(rows):
        raise ValueError("Shard classes must be nonempty and match class_count")
    if not isinstance(data.get("shard_id"), str) or not data["shard_id"]:
        raise ValueError("Missing shard_id")
    local = {item.task_id: item for item in items}
    selected = []
    seen = set()
    for row in rows:
        if not isinstance(row, dict) or not isinstance(row.get("task_id"), str):
            raise ValueError("Invalid shard class record")
        task = row["task_id"]
        if task in seen:
            raise ValueError(f"Duplicate shard task_id: {task}")
        seen.add(task)
        if task not in local:
            raise ValueError(f"Shard class missing from local dataset: {task}")
        expected = assignment_record(local[task])
        if row != expected:
            raise ValueError(f"Shard metadata/evidence mismatch: {task}; use the same dataset snapshot")
        selected.append(local[task])
    return selected, {"shard_id": data["shard_id"], "shard_class_count": len(selected)}
