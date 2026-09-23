"""Export portable JSON class assignments from a verified preflight source index."""
import argparse
import hashlib
import json
from pathlib import Path

from preflight.ingest import candidates
from preflight.shards import assignment_record
from split_dataset import repo_key


def export(database: Path, output: Path, parts: int = 5):
    if not database.is_file() or parts < 1:
        raise ValueError("Require an existing verified index and positive parts")
    groups = {}
    for item in candidates(database):
        groups.setdefault(repo_key(item.repo_url), []).append(assignment_record(item))
    buckets = [[] for _ in range(parts)]
    counts = [0] * parts
    for repo, rows in sorted(groups.items(), key=lambda pair: (-len(pair[1]), pair[0])):
        index = min(range(parts), key=lambda i: (len(buckets[i]), counts[i], i))
        buckets[index].extend(rows)
        counts[index] += 1
    output.mkdir(parents=True, exist_ok=False)
    summary = {"schema_version": 1, "class_count": sum(map(len, buckets)), "repository_count": len(groups), "shards": []}
    seen = set()
    for i, rows in enumerate(buckets):
        rows.sort(key=lambda row: row["task_id"])
        ids = {row["task_id"] for row in rows}
        assert len(ids) == len(rows) and not seen.intersection(ids)
        seen.update(ids)
        name = f"shard-{i + 1:02d}"
        data = {"schema_version": 1, "shard_id": name, "class_count": len(rows), "classes": rows}
        content = json.dumps(data, ensure_ascii=False, separators=(",", ":")) + "\n"
        path = output / f"{name}.json"
        path.write_text(content, encoding="utf-8", newline="\n")
        summary["shards"].append({"file": path.name, "class_count": len(rows), "repository_count": counts[i], "sha256": hashlib.sha256(content.encode()).hexdigest(), "bytes": len(content.encode())})
    (output / "summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--index", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--parts", type=int, default=5)
    args = parser.parse_args()
    export(args.index, args.output_dir, args.parts)
