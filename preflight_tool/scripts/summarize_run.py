"""Export a small, path-free statistics bundle from one completed preflight run.

Usage (from ARROW/preflight_tool):
    python scripts/summarize_run.py --run-dir runs/<run-id> --output-dir ../preflight_summary/<NN-NAME>

Writes two files into --output-dir:
    summary.json    byte-for-byte copy of <run-dir>/reports/summary.json
    run_stats.json  counts by status, reason code, tag, revision status, build tool
                    and test framework, plus provenance without local paths

Exits with code 1 and writes nothing if the run is incomplete or its reports disagree.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path

PATH_KEYS = {"config_path", "dataset_dir", "input_root", "shard_path"}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def counter(values) -> dict[str, int]:
    return dict(sorted(Counter(values).items()))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--run-dir", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()

    run_dir = args.run_dir.resolve()
    summary_path = run_dir / "reports" / "summary.json"
    results_path = run_dir / "reports" / "preflight_results.jsonl"
    provenance_path = run_dir / "provenance.json"
    for required in (summary_path, results_path, provenance_path):
        if not required.is_file():
            print(f"ERROR missing {required}", file=sys.stderr)
            return 1

    summary = json.loads(summary_path.read_text(encoding="utf-8"))
    provenance = json.loads(provenance_path.read_text(encoding="utf-8"))
    records = [json.loads(line) for line in results_path.read_text(encoding="utf-8").splitlines() if line.strip()]

    task_ids = [record["task_id"] for record in records]
    eligible = [record for record in records if record.get("technical_eligible")]
    problems = []
    if provenance.get("run_status") != "COMPLETED":
        problems.append(f"run_status={provenance.get('run_status')}")
    if len(set(task_ids)) != len(task_ids):
        problems.append("duplicate task_id in preflight_results.jsonl")
    if not (len(records) == summary.get("total") == provenance.get("classes_selected")):
        problems.append(f"record count {len(records)} != summary.total {summary.get('total')} "
                        f"!= classes_selected {provenance.get('classes_selected')}")
    by_status = counter(record.get("preflight_status") for record in records)
    if by_status != dict(sorted(summary.get("by_status", {}).items())):
        problems.append("by_status differs between summary.json and preflight_results.jsonl")
    if len(eligible) != summary.get("technical_eligible"):
        problems.append("technical_eligible differs between summary.json and preflight_results.jsonl")
    if problems:
        for problem in problems:
            print(f"ERROR {problem}", file=sys.stderr)
        return 1

    stats = {
        "schema_version": 1,
        "provenance": {key: value for key, value in provenance.items() if key not in PATH_KEYS},
        "source_files_sha256": {
            "reports/summary.json": sha256(summary_path),
            "reports/preflight_results.jsonl": sha256(results_path),
            "provenance.json": sha256(provenance_path),
        },
        "all_classes": {
            "total": len(records),
            "repositories": len({record.get("repo_url") for record in records}),
            "by_preflight_status": by_status,
            "by_reason_code": counter(code for record in records for code in record.get("reason_codes") or []),
            "by_revision_verification_status": counter(record.get("revision_verification_status") for record in records),
            "by_run_mode": counter(record.get("run_mode") for record in records),
        },
        "technical_eligible": {
            "total": len(eligible),
            "repositories": len({record.get("repo_url") for record in eligible}),
            "by_revision_verification_status": counter(record.get("revision_verification_status") for record in eligible),
            "by_build_tool": counter(record.get("build_tool") for record in eligible),
            "by_testing_framework": counter(record.get("testing_framework") for record in eligible),
            "by_tag": counter(tag for record in eligible for tag in record.get("tags") or []),
        },
        "strict_eligible": sum(1 for record in records if record.get("strict_eligible")),
    }

    args.output_dir.mkdir(parents=True, exist_ok=True)
    (args.output_dir / "summary.json").write_bytes(summary_path.read_bytes())
    (args.output_dir / "run_stats.json").write_text(json.dumps(stats, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print(f"OK shard={provenance.get('shard_id')} total={len(records)} technical_eligible={len(eligible)} -> {args.output_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
