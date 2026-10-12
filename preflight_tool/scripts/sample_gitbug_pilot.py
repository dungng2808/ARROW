"""Draw the GitBug-Java pilot from the bugs left over after the evaluation manifest was drawn.

Usage (from ARROW/preflight_tool):
    python scripts/sample_gitbug_pilot.py \
        --pool ../preflight_manifest/gitbug-java/technical_eligible_manifest.jsonl \
        --evaluation ../evaluation_manifest/gitbug_java_32_seed42.jsonl \
        --seed 42 --output-dir ../evaluation_manifest/pilot

Rule: only repositories of the evaluation manifest that have at least one eligible CUT on a
bug (checkout_sha) other than their evaluation CUT take part. For each such repository, the
candidates are the eligible CUTs on other bugs whose class file differs from the evaluation
CUT; if there is none, CUTs on other bugs with the same class file are used instead (recorded
as same_class_other_bug). One candidate per repository is drawn with random.Random(seed) from
the sorted task_id list, repositories in sorted order. No result column is read. The pilot
never contains an evaluation task, nor a task on an evaluation bug.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import random
import sys
from collections import defaultdict
from pathlib import Path


def read_jsonl(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--pool", required=True, type=Path)
    parser.add_argument("--evaluation", required=True, type=Path)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--name", default=None)
    args = parser.parse_args()

    pool = [r for r in read_jsonl(args.pool) if r.get("technical_eligible")]
    evaluation = {r["repo_url"]: r for r in read_jsonl(args.evaluation)}
    by_repo: dict[str, list[dict]] = defaultdict(list)
    for record in pool:
        if record["repo_url"] in evaluation:
            by_repo[record["repo_url"]].append(record)

    rng = random.Random(args.seed)
    chosen, selection = [], {}
    for repo in sorted(by_repo):
        picked = evaluation[repo]
        other_bug = [r for r in by_repo[repo] if r["checkout_sha"] != picked["checkout_sha"]]
        if not other_bug:
            continue
        other_class = [r for r in other_bug if r["class_path"] != picked["class_path"]]
        candidates, kind = (other_class, "other_class_other_bug") if other_class else (other_bug, "same_class_other_bug")
        record = rng.choice(sorted(candidates, key=lambda r: r["task_id"]))
        chosen.append(record)
        selection[record["task_id"]] = {"repository": repo, "selection": kind, "candidates": len(candidates),
                                        "evaluation_task_id": picked["task_id"]}

    eval_ids = {r["task_id"] for r in evaluation.values()}
    eval_bugs = {(r["repo_url"], r["checkout_sha"]) for r in evaluation.values()}
    assert not any(r["task_id"] in eval_ids or (r["repo_url"], r["checkout_sha"]) in eval_bugs for r in chosen)

    name = args.name or f"gitbug_java_pilot_{len(chosen)}_seed{args.seed}"
    args.output_dir.mkdir(parents=True, exist_ok=True)
    body = "".join(json.dumps(r, ensure_ascii=False, sort_keys=True) + "\n" for r in chosen).encode("utf-8")
    (args.output_dir / f"{name}.jsonl").write_bytes(body)
    meta = {
        "schema_version": 1,
        "manifest_file": f"{name}.jsonl",
        "manifest_sha256": hashlib.sha256(body).hexdigest(),
        "size": len(chosen),
        "seed": args.seed,
        "rng": f"python random.Random (Python {sys.version_info.major}.{sys.version_info.minor})",
        "rule": __doc__.split("Rule: ", 1)[1].strip().replace("\n", " "),
        "pool_sha256": hashlib.sha256(args.pool.read_bytes()).hexdigest(),
        "evaluation_manifest": args.evaluation.name,
        "evaluation_sha256": hashlib.sha256(args.evaluation.read_bytes()).hexdigest(),
        "selection": selection,
        "selected_task_ids": [r["task_id"] for r in chosen],
    }
    (args.output_dir / f"{name}.meta.json").write_text(json.dumps(meta, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    kinds = defaultdict(int)
    for item in selection.values():
        kinds[item["selection"]] += 1
    print(f"OK {len(chosen)} pilot CUTs from {len(by_repo)} evaluation repositories; {dict(kinds)}; sha256={meta['manifest_sha256']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
