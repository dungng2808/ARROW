"""Copy Classes2Test into disjoint, repository-grouped, class-balanced shards."""
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import sqlite3
from pathlib import Path
from urllib.parse import urlsplit

from preflight.ingest import build_index, resolve_dataset_dir


def repo_key(url: str) -> str:
    value = url.rstrip("/")
    if value.endswith(".git"):
        value = value[:-4]
    parsed = urlsplit(value)
    if parsed.hostname and parsed.hostname.lower() == "github.com":
        return "github.com/" + parsed.path.strip("/").lower()
    return value


def split(source: Path, output: Path, parts: int) -> dict:
    source = resolve_dataset_dir(source.resolve())
    output = output.resolve()
    if parts < 1 or not source.is_dir():
        raise ValueError("Require positive parts and an existing dataset directory")
    if output == source or source in output.parents:
        raise ValueError("Output must be outside source dataset")
    output.mkdir(parents=True, exist_ok=False)
    database = output / "source_index.sqlite"
    print("Indexing complete source dataset...", flush=True)
    raw_count, class_count = build_index(source, database)
    with sqlite3.connect(database) as conn:
        rejected = conn.execute("SELECT COUNT(*) FROM input_rejections").fetchone()[0]
        if rejected:
            raise ValueError(f"Found {rejected} rejected inputs; abort instead of silently omitting them. See {database}")
        groups = {}
        for repo, count in conn.execute("SELECT repo_url, COUNT(*) FROM classes GROUP BY repo_url"):
            key = repo_key(repo)
            group = groups.setdefault(key, {"urls": [], "classes": 0})
            group["urls"].append(repo)
            group["classes"] += count
        stats = [{"shard": f"shard-{i + 1:02d}", "classes": 0, "repositories": 0, "json_files": 0, "bytes": 0} for i in range(parts)]
        assigned = {}
        for key, group in sorted(groups.items(), key=lambda item: (-item[1]["classes"], item[0])):
            target = min(range(parts), key=lambda i: (stats[i]["classes"], stats[i]["repositories"], i))
            stats[target]["classes"] += group["classes"]
            stats[target]["repositories"] += 1
            for url in group["urls"]:
                assigned[url] = target
        assignment = [{"repo_url": url, "repository_key": repo_key(url), "shard": stats[i]["shard"]} for url, i in sorted(assigned.items())]
        (output / "repository_assignment.json").write_text(json.dumps(assignment, indent=2), encoding="utf-8")
        files_seen = set()
        tasks_by_shard = [set() for _ in stats]
        handles = []
        try:
            for row in stats:
                folder = output / row["shard"]
                (folder / "dataset").mkdir(parents=True)
                handles.append((folder / "file_manifest.jsonl").open("w", encoding="utf-8"))
            query = """SELECT e.source_json_path, e.source_json_sha256, c.repo_url, c.task_id
                       FROM evidence e JOIN classes c ON c.task_id=e.task_id
                       ORDER BY e.source_json_path"""
            for relative, expected_hash, repo, task in conn.execute(query):
                if relative in files_seen:
                    raise ValueError(f"Duplicate input assignment: {relative}")
                files_seen.add(relative)
                i = assigned[repo]
                tasks_by_shard[i].add(task)
                src = source / relative
                dst = output / stats[i]["shard"] / "dataset" / relative
                dst.parent.mkdir(parents=True, exist_ok=True)
                # Independent copies, never symlinks or hardlinks to the original.
                shutil.copyfile(src, dst)
                actual_hash = hashlib.sha256(dst.read_bytes()).hexdigest()
                if actual_hash != expected_hash:
                    raise ValueError(f"Copy hash mismatch/source changed: {relative}")
                size = dst.stat().st_size
                stats[i]["json_files"] += 1
                stats[i]["bytes"] += size
                handles[i].write(json.dumps({"path": relative, "sha256": actual_hash, "task_id": task, "repo_url": repo}) + "\n")
                if len(files_seen) % 20000 == 0:
                    print(f"Copied and SHA-256 verified {len(files_seen)}/{raw_count}", flush=True)
        finally:
            for handle in handles:
                handle.close()
        source_paths = {p.relative_to(source).as_posix() for project in source.iterdir() if project.is_dir() for p in project.glob("*.json")}
        if files_seen != source_paths or len(files_seen) != raw_count:
            raise ValueError("Source coverage mismatch")
        for i, row in enumerate(stats):
            if len(tasks_by_shard[i]) != row["classes"]:
                raise ValueError("Class count mismatch")
            for other in tasks_by_shard[i + 1:]:
                if tasks_by_shard[i] & other:
                    raise ValueError("Overlapping task IDs")
            actual_paths = {p.relative_to(output / row["shard"] / "dataset").as_posix() for p in (output / row["shard"] / "dataset").glob("*/*.json")}
            if len(actual_paths) != row["json_files"]:
                raise ValueError("Output file count mismatch")
    report = {"source": str(source), "parts": parts, "raw_json": raw_count,
              "classes": class_count, "repositories": len(groups), "rejected": rejected,
              "verified": True, "method": "repository-grouped largest-class-count-first greedy balancing",
              "shards": stats}
    (output / "split_report.json").write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(json.dumps(report, indent=2), flush=True)
    return report


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input-root", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--parts", type=int, default=5)
    args = parser.parse_args()
    split(args.input_root, args.output_dir, args.parts)
