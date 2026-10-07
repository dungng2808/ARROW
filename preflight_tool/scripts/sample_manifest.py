"""Draw the frozen evaluation manifest from the preflight technical-eligible pool.

Usage (from ARROW/preflight_tool):
    python scripts/sample_manifest.py --pool-glob "../preflight_manifest/*/technical_eligible_manifest.jsonl" \
        --size 200 --seed 42 --output-dir ../evaluation_manifest

Rule: keep CONTENT_MATCHED records only, group them by repository, randomly select
--size distinct repositories without replacement, then randomly select one focal class
from each. Repositories and classes are sorted before sampling, so the result depends
only on the pool content and the seed, not on file or line order. No result column is
read. Writes <name>.jsonl (records copied unchanged) and <name>.meta.json.
"""
from __future__ import annotations

import argparse
import glob
import hashlib
import json
import random
import sys
from collections import defaultdict
from pathlib import Path

ALLOWED_STATUS = "CONTENT_MATCHED"


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--pool-glob", required=True)
    parser.add_argument("--size", type=int, default=200)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--name", default=None, help="output base name; default classes2test_<size>_seed<seed>")
    args = parser.parse_args()

    pool_files = sorted(Path(path) for path in glob.glob(args.pool_glob))
    if not pool_files:
        print(f"ERROR no pool files match {args.pool_glob}", file=sys.stderr)
        return 1

    records: dict[str, dict] = {}
    pool_sha = {}
    excluded = defaultdict(int)
    for path in pool_files:
        raw = path.read_bytes()
        pool_sha[path.as_posix()] = sha256_bytes(raw)
        for line in raw.decode("utf-8").splitlines():
            if not line.strip():
                continue
            record = json.loads(line)
            if record["task_id"] in records:
                print(f"ERROR duplicate task_id {record['task_id']}", file=sys.stderr)
                return 1
            if not record.get("technical_eligible") or record.get("revision_verification_status") != ALLOWED_STATUS:
                excluded[str(record.get("revision_verification_status"))] += 1
                continue
            records[record["task_id"]] = record

    by_repo: dict[str, list[str]] = defaultdict(list)
    for task_id, record in records.items():
        by_repo[record["repo_url"]].append(task_id)
    repos = sorted(by_repo)
    if len(repos) < args.size:
        print(f"ERROR only {len(repos)} repositories in pool, need {args.size}", file=sys.stderr)
        return 1

    rng = random.Random(args.seed)
    chosen_repos = rng.sample(repos, args.size)
    chosen = [records[rng.choice(sorted(by_repo[repo]))] for repo in chosen_repos]

    name = args.name or f"classes2test_{args.size}_seed{args.seed}"
    args.output_dir.mkdir(parents=True, exist_ok=True)
    manifest_path = args.output_dir / f"{name}.jsonl"
    body = "".join(json.dumps(record, ensure_ascii=False, sort_keys=True) + "\n" for record in chosen)
    manifest_path.write_bytes(body.encode("utf-8"))

    meta = {
        "schema_version": 1,
        "manifest_file": manifest_path.name,
        "manifest_sha256": sha256_bytes(body.encode("utf-8")),
        "size": args.size,
        "seed": args.seed,
        "rng": f"python random.Random (Python {sys.version_info.major}.{sys.version_info.minor})",
        "rule": "technical_eligible and CONTENT_MATCHED; group by repo_url; sample repositories without "
                "replacement from the sorted repository list; choose one task_id per repository from its "
                "sorted task_id list; no result columns used",
        "pool_files_sha256": pool_sha,
        "pool_classes": len(records),
        "pool_repositories": len(repos),
        "excluded_from_pool_by_revision_status": dict(sorted(excluded.items())),
        "selected_task_ids": [record["task_id"] for record in chosen],
    }
    meta_path = args.output_dir / f"{name}.meta.json"
    meta_path.write_text(json.dumps(meta, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print(f"OK pool={len(records)} classes / {len(repos)} repos -> {len(chosen)} selected; "
          f"sha256={meta['manifest_sha256']} -> {manifest_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
