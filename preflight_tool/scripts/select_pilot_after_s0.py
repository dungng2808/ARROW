"""Replace pilot CUTs that fail the S0 screening with reserve CUTs, in reserve draw order.

Usage (from ARROW/preflight_tool):
    python scripts/select_pilot_after_s0.py \
        --pilot ../evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl \
        --reserve ../evaluation_manifest/pilot/classes2test_pilot_reserve_50_seed42.jsonl \
        --screening-csv ../ARROW_Framework/runs/<screen run>/reports/screening.csv [--screening-csv <run rerunning some tasks> ...] \
        --name classes2test_pilot_50_seed42_v2 --output-dir ../evaluation_manifest/pilot

Screening input: `screening.csv` written by `arrow-fw screen` (ARROW_Framework, spec 01 section 6.3;
columns task_id, outcome, code, paired_test_status, tests_run, jdk_major, message). The older
`--run-dir` input reads ledger.jsonl + baseline.json of the removed pilot_tool (tag pilot-tool-v1)
and is kept only to reproduce classes2test_pilot_50_seed42_v2.

Rule (criterion B, S0 of pilot_tool): a CUT passes when its prepare job ended READY and the
human-written test class paired with it in the manifest (test_class_path) ran in the baseline
with at least one test case, none of them failing in every run or flaky. Test cases of other
classes may fail (they are left out of the regression comparison). Pilot CUTs that pass keep
their position; each failing one is replaced by the next passing reserve CUT in the order the
reserve was drawn. Records are copied unchanged. With several --run-dir, a later run overrides an
earlier one for the tasks it ran (used to redo S0 for a few tasks after a tool fix).
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import sys
from pathlib import Path


def read_jsonl(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def paired_test_fqcn(record: dict) -> str:
    path = record["test_class_path"].replace("\\", "/")
    marker = "src/test/java/"
    if marker not in path:
        return ""
    return path.split(marker, 1)[1].removesuffix(".java").replace("/", ".")


def latest_outcomes(run_dirs: list[Path], dataset: str) -> dict[str, tuple[dict, Path]]:
    latest: dict[str, tuple[dict, Path]] = {}
    for run_dir in run_dirs:
        for line in (run_dir / "ledger.jsonl").read_text(encoding="utf-8").splitlines():
            entry = json.loads(line) if line.strip() else {}
            job = entry.get("job")
            if job and job["dataset"] == dataset and job["stage"] == "prepare":
                latest[job["task_id"]] = (entry, run_dir)
    return latest


def screen(record: dict, found: tuple[dict, Path] | None, dataset: str) -> tuple[bool, str]:
    if found is None:
        return False, "NOT_RUN"
    entry, run_dir = found
    if entry.get("outcome") != "READY":
        return False, f"S0_{entry.get('status')}:{entry.get('reason')}"
    baseline = json.loads((run_dir / "tasks" / dataset / record["task_id"] / "baseline.json").read_text(encoding="utf-8"))
    fqcn = paired_test_fqcn(record)

    def of_paired(test_ids: list[str]) -> list[str]:
        return [t for t in test_ids if t.split("#")[0] == fqcn or t.split("#")[0].startswith(fqcn + "$")]

    passing = of_paired(baseline.get("passing_in_all_runs", []))
    broken = of_paired(baseline.get("failing_in_all_runs", [])) + of_paired(baseline.get("flaky", []))
    if broken:
        return False, f"PAIRED_TEST_FAILS:{len(broken)}"
    if not passing:
        return False, f"PAIRED_TEST_NOT_RUN(module_tests={baseline.get('tests_total')})"
    return True, f"OK:{len(passing)}"


def screening_csv_verdicts(paths: list[Path]) -> dict[str, tuple[bool, str]]:
    """Criterion B from arrow-fw screen: outcome OK and paired_test_status PASS. A later file overrides an earlier one."""
    verdicts: dict[str, tuple[bool, str]] = {}
    for path in paths:
        with path.open(encoding="utf-8", newline="") as handle:
            for row in csv.DictReader(handle):
                if row["outcome"] != "OK":
                    verdicts[row["task_id"]] = (False, f"K1_{row['outcome']}:{row['code']}")
                elif row["paired_test_status"] != "PASS":
                    verdicts[row["task_id"]] = (False, f"PAIRED_TEST_{row['paired_test_status']}(tests_run={row['tests_run']})")
                else:
                    passing = row["tests_run"].split(";")[0] if row["tests_run"] else "0"
                    verdicts[row["task_id"]] = (True, f"OK:{passing}")
    return verdicts


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--pilot", required=True, type=Path)
    parser.add_argument("--reserve", required=True, type=Path)
    parser.add_argument("--screening-csv", type=Path, action="append", default=[])
    parser.add_argument("--run-dir", type=Path, action="append", default=[], help="pilot_tool run (old format)")
    parser.add_argument("--dataset", default="classes2test")
    parser.add_argument("--name", required=True)
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()

    if bool(args.screening_csv) == bool(args.run_dir):
        print("ERROR give either --screening-csv (arrow-fw screen) or --run-dir (old pilot_tool), not both", file=sys.stderr)
        return 2
    pilot = read_jsonl(args.pilot)
    reserve = read_jsonl(args.reserve)
    if args.screening_csv:
        from_csv = screening_csv_verdicts(args.screening_csv)
        verdicts = {r["task_id"]: from_csv.get(r["task_id"], (False, "NOT_RUN")) for r in pilot + reserve}
    else:
        outcomes = latest_outcomes(args.run_dir, args.dataset)
        verdicts = {r["task_id"]: screen(r, outcomes.get(r["task_id"]), args.dataset) for r in pilot + reserve}

    passing_reserve = [r for r in reserve if verdicts[r["task_id"]][0]]
    chosen: list[dict] = []
    replacements = []
    for record in pilot:
        if verdicts[record["task_id"]][0]:
            chosen.append(record)
            continue
        if not passing_reserve:
            print(f"ERROR reserve exhausted while replacing {record['task_id']}", file=sys.stderr)
            return 1
        substitute = passing_reserve.pop(0)
        chosen.append(substitute)
        replacements.append({"removed": record["task_id"], "removed_repo": record["repo_url"],
                             "reason": verdicts[record["task_id"]][1],
                             "added": substitute["task_id"], "added_repo": substitute["repo_url"]})

    if len({r["repo_url"] for r in chosen}) != len(chosen):
        print("ERROR duplicate repository in result", file=sys.stderr)
        return 1

    args.output_dir.mkdir(parents=True, exist_ok=True)
    manifest_path = args.output_dir / f"{args.name}.jsonl"
    body = "".join(json.dumps(r, ensure_ascii=False, sort_keys=True) + "\n" for r in chosen).encode("utf-8")
    manifest_path.write_bytes(body)

    def sha(path: Path) -> str:
        return hashlib.sha256(path.read_bytes()).hexdigest()

    meta = {
        "schema_version": 1,
        "manifest_file": manifest_path.name,
        "manifest_sha256": hashlib.sha256(body).hexdigest(),
        "size": len(chosen),
        "rule": __doc__.split("Rule (criterion B, S0 of pilot_tool): ", 1)[1].split("\n\nScreening input", 1)[0].strip().replace("\n", " "),
        "pilot_file": args.pilot.as_posix(), "pilot_sha256": sha(args.pilot),
        "reserve_file": args.reserve.as_posix(), "reserve_sha256": sha(args.reserve),
        "screening_runs": [d.name for d in args.run_dir] + [p.parent.parent.name for p in args.screening_csv],
        "replacements": replacements,
        "screening": {tid: v[1] for tid, v in verdicts.items()},
        "selected_task_ids": [r["task_id"] for r in chosen],
    }
    meta_path = args.output_dir / f"{args.name}.meta.json"
    meta_path.write_bytes((json.dumps(meta, indent=2, ensure_ascii=False) + "\n").encode("utf-8"))
    kept = len(chosen) - len(replacements)
    print(f"OK {len(chosen)} CUTs: kept {kept}, replaced {len(replacements)}, reserve left {len(passing_reserve)}; "
          f"sha256={meta['manifest_sha256']} -> {manifest_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
