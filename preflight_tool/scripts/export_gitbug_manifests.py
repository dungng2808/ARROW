"""Combine the GitBug-Java preflight runs into one set of manifests for preflight_manifest/.

Usage (from ARROW/preflight_tool):
    python scripts/export_gitbug_manifests.py \
        --run runs/gitbug-full-20261011 --run runs/gitbug-rerun-20261011 --run runs/gitbug-labeled-20261012 \
        --output-dir ../preflight_manifest/gitbug-java

GitBug-Java was preflighted in several completed runs: a full run, a re-run of failed
classes with the probe/JDK fixes of scripts/rerun_fixed_probe.py, and a run of the classes
added by manual test labels. Runs are given oldest first; for every task_id the record of
the latest run that processed it is kept, in each of the four manifests
(class_candidates, locked_input_manifest, technical_eligible_manifest,
strict_eligible_manifest). No record is edited. merge_provenance.json lists the runs,
the SHA-256 of every source manifest, the patch descriptions and the record counts.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

MANIFESTS = ("class_candidates", "locked_input_manifest", "technical_eligible_manifest", "strict_eligible_manifest")


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--run", action="append", required=True, type=Path, help="completed run dir, oldest first (repeatable)")
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()

    runs = []
    for run in args.run:
        provenance = json.loads((run / "provenance.json").read_text(encoding="utf-8"))
        if provenance.get("run_status") != "COMPLETED":
            raise SystemExit(f"{run}: run_status is {provenance.get('run_status')!r}, not COMPLETED")
        patches = run.parent / f"{run.name}.patches.json"
        runs.append({
            "run_id": run.name,
            "classes_selected": provenance.get("classes_selected"),
            "revision_map_sha256": provenance.get("revision_map_sha256"),
            "shard_sha256": provenance.get("shard_sha256"),
            "patches": json.loads(patches.read_text(encoding="utf-8")) if patches.is_file() else None,
            "manifest_sha256": {name: sha256(run / "manifests" / f"{name}.jsonl") for name in MANIFESTS},
        })
    for run_info in runs:
        if run_info["patches"]:
            run_info["patches"].pop("script", None)  # absolute path of this machine
            if run_info["patches"].get("previous_run"):
                run_info["patches"]["previous_run"] = Path(run_info["patches"]["previous_run"]).name

    args.output_dir.mkdir(parents=True, exist_ok=True)
    counts, source_of = {}, {}
    for name in MANIFESTS:
        latest: dict[str, tuple[str, str]] = {}
        for run in args.run:
            for line in (run / "manifests" / f"{name}.jsonl").read_text(encoding="utf-8").splitlines():
                if line.strip():
                    latest[json.loads(line)["task_id"]] = (run.name, line)
        if name == "locked_input_manifest":
            source_of = {task: run_id for task, (run_id, _) in latest.items()}
        # A class re-run later and no longer eligible must not survive from an earlier run.
        if name in ("technical_eligible_manifest", "strict_eligible_manifest"):
            latest = {task: value for task, value in latest.items() if value[0] == source_of.get(task)}
        out = args.output_dir / f"{name}.jsonl"
        out.write_text("".join(line + "\n" for _, (_, line) in sorted(latest.items())), encoding="utf-8")
        counts[name] = len(latest)

    by_run: dict[str, int] = {}
    for run_id in source_of.values():
        by_run[run_id] = by_run.get(run_id, 0) + 1
    (args.output_dir / "merge_provenance.json").write_text(json.dumps({
        "schema_version": 1,
        "rule": "runs oldest first; for each task_id keep the record of the latest run that processed it; records copied unchanged",
        "runs": runs,
        "final_record_source": by_run,
        "record_counts": counts,
        "output_sha256": {name: sha256(args.output_dir / f"{name}.jsonl") for name in MANIFESTS},
    }, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"record_counts": counts, "final_record_source": by_run}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
