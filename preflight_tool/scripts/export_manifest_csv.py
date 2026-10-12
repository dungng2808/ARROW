"""Write the flattened CSV of a frozen manifest, with the columns of classes2test_200_seed42.csv.

Usage (from ARROW/preflight_tool):
    python scripts/export_manifest_csv.py ../evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl
    python scripts/export_manifest_csv.py ../evaluation_manifest/gitbug_java_32_seed42.jsonl \
        --gitbug-candidates ../gitbug_input/candidates.csv

The CSV is written next to the JSONL with the same base name. Lists are joined with ';',
rows keep the JSONL order, line endings are LF. With --gitbug-candidates, three GitBug-Java
columns are appended: bug_id, test_class_path, test_pairing.
"""
from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path

COLUMNS = ["task_id", "repo_url", "checkout_sha", "class_fqn", "class_path", "module_path", "build_tool",
           "build_tool_version", "testing_framework", "java_version", "constructor_count", "method_count",
           "api_count", "probed_api", "probe_status", "preflight_status", "technical_eligible", "strict_eligible",
           "revision_verification_status", "run_mode", "revision_provenance", "source_json_path",
           "source_json_sha256", "tags", "reason_codes", "duration_seconds"]


def cell(value):
    return ";".join(value) if isinstance(value, list) else ("" if value is None else value)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--gitbug-candidates", type=Path)
    args = parser.parse_args()

    rows = [json.loads(line) for line in args.manifest.read_text(encoding="utf-8").splitlines() if line.strip()]
    extra = {}
    if args.gitbug_candidates:
        with args.gitbug_candidates.open(encoding="utf-8-sig", newline="") as handle:
            extra = {row["task_id"]: row for row in csv.DictReader(handle) if row.get("task_id")}
    out = args.manifest.with_suffix(".csv")
    with out.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle, lineterminator="\n")
        writer.writerow(COLUMNS + (["bug_id", "test_class_path", "test_pairing"] if extra else []))
        for row in rows:
            tail = []
            if extra:
                candidate = extra[row["task_id"]]
                tail = [candidate["project_id"], candidate["test_class_path"], candidate["test_pairing"]]
            writer.writerow([cell(row.get(column)) for column in COLUMNS] + tail)
    print(f"OK {len(rows)} rows -> {out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
