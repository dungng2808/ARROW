from __future__ import annotations

import argparse
import csv
import json
import shutil
import sys
import tomllib
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from .ingest import build_index, candidates, input_rejections, resolve_dataset_dir
from .revision import load_revision_map
from .runner import ToolConfig, run_all
from .shards import select_shard
from .util import json_compact, safe_relative, sha256_file


ROOT = Path(__file__).resolve().parents[1]


def _args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Evidence-preserving Classes2Test Java preflight")
    parser.add_argument("--config", type=Path, default=ROOT / "config.example.toml")
    parser.add_argument("--input-root", type=Path, help="Classes2Test root or its dataset/ directory")
    parser.add_argument("--output-dir", type=Path, help="Run output root; default is <tool>/runs/<timestamp>")
    parser.add_argument("--revision-map", type=Path, help="CSV/JSONL map of audited UPSTREAM_PINNED task_id -> SHA")
    parser.add_argument("--workers", type=int)
    parser.add_argument("--shard", type=Path, help="Portable JSON assignment of classes to execute on this machine")
    parser.add_argument("--limit", type=int, default=0, help="Maximum raw JSON records to ingest (smoke test)")
    parser.add_argument("--max-classes", type=int, default=0, help="Maximum deduplicated classes to execute")
    parser.add_argument("--index-only", action="store_true", help="Build the class candidate manifest but skip clone/build/probe")
    parser.add_argument("--fast", action="store_true", help="Skip compile probe; eligible technical entries become PRECHECKED and never strict")
    parser.add_argument("--keep-workspaces", action="store_true")
    parser.add_argument("--keep-repo-cache", action="store_true", help="Keep cloned mirrors after all classes of a repository finish")
    return parser.parse_args()


def _load_config(path: Path) -> dict[str, Any]:
    with path.open("rb") as handle:
        return tomllib.load(handle)


def _write_jsonl(path: Path, records: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + ".tmp")
    with temporary.open("w", encoding="utf-8", newline="\n") as handle:
        for record in records:
            handle.write(json.dumps(record, ensure_ascii=False, sort_keys=True) + "\n")
    temporary.replace(path)


def _flat(record: dict[str, Any]) -> dict[str, Any]:
    flattened: dict[str, Any] = {}
    for key, value in record.items():
        flattened[key] = json_compact(value) if isinstance(value, (list, dict)) else value
    return flattened


def _write_csv(path: Path, records: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    fields = sorted({key for record in records for key in record})
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(_flat(record) for record in records)


def _candidate_record(item) -> dict[str, Any]:
    return {
        "task_id": item.task_id, "project_id": item.project_id, "repo_url": item.repo_url,
        "source_json_path": item.source_json_path, "source_json_sha256": item.source_json_sha256,
        "source_json_paths": list(item.source_json_paths), "source_json_sha256s": list(item.source_json_sha256s),
        "class_path": item.class_path, "class_fqn": item.class_fqn,
        "test_class_path": item.test_class_paths[0] if item.test_class_paths else "",
        "test_class_paths": list(item.test_class_paths),
    }


def main() -> None:
    # Windows PowerShell can still expose a legacy cp1252 stdout even though
    # dataset paths commonly contain Vietnamese characters.
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    args = _args(); raw_config = _load_config(args.config)
    if args.shard and (args.limit or args.max_classes):
        raise SystemExit("--shard cannot be combined with --limit or --max-classes")
    run_cfg = raw_config.get("run", {}); input_cfg = raw_config.get("input", {}); java_cfg = raw_config.get("java", {})
    input_root = (args.input_root or Path(input_cfg.get("root", ""))).expanduser().resolve()
    if not input_root.is_dir():
        raise SystemExit(f"Input root không tồn tại: {input_root}")
    dataset_dir = resolve_dataset_dir(input_root)
    run_id = run_cfg.get("id", "auto")
    if run_id == "auto": run_id = datetime.now(timezone.utc).strftime("preflight-%Y%m%dT%H%M%SZ")
    run_root = (args.output_dir or ROOT / "runs" / run_id).resolve()
    if run_root.exists() and any(run_root.iterdir()):
        raise SystemExit(f"Output directory đã có dữ liệu: {run_root}; chọn --output-dir mới để tránh ghi đè evidence.")
    manifests, reports = run_root / "manifests", run_root / "reports"
    database = run_root / "state" / "class_index.sqlite"
    json_count, class_count = build_index(dataset_dir, database, limit=args.limit)
    selected = list(candidates(database))
    shard_info = {}
    if args.shard:
        try:
            selected, shard_info = select_shard(selected, args.shard)
        except (ValueError, OSError) as exc:
            raise SystemExit(f"Invalid shard: {exc}") from exc
        shard_info.update({"shard_path": str(args.shard.resolve()), "shard_sha256": sha256_file(args.shard)})
    if args.max_classes: selected = selected[:args.max_classes]
    _write_jsonl(manifests / "class_candidates.jsonl", [_candidate_record(item) for item in selected])
    _write_jsonl(reports / "input_rejections.jsonl", input_rejections(database))
    provenance = {
        "tool": "class2test-preflight", "started_at_utc": datetime.now(timezone.utc).isoformat(),
        "input_root": str(input_root), "dataset_dir": str(dataset_dir), "config_path": str(args.config.resolve()),
        "config_sha256": sha256_file(args.config.resolve()), "raw_json_indexed": json_count,
        "deduplicated_classes": class_count, "classes_selected": len(selected),
        "keep_repo_cache": args.keep_repo_cache or bool(run_cfg.get("keep_repo_cache", False)),
        "keep_workspaces": args.keep_workspaces or bool(run_cfg.get("keep_workspaces", False)),
        "revision_policy": "Raw Classes2Test carries no source commit. History matches are CONTENT_MATCHED/DISCOVERY_ONLY; only an audited revision map may produce UPSTREAM_PINNED/STRICT output.",
    }
    if args.revision_map:
        provenance["revision_map"] = str(args.revision_map.resolve()); provenance["revision_map_sha256"] = sha256_file(args.revision_map.resolve())
    provenance.update(shard_info)
    (run_root / "provenance.json").parent.mkdir(parents=True, exist_ok=True)
    (run_root / "provenance.json").write_text(json.dumps(provenance, ensure_ascii=False, indent=2), encoding="utf-8")
    if args.index_only:
        print(f"Indexed {json_count} JSON -> {len(selected)} class candidates")
        print(manifests / "class_candidates.jsonl")
        return
    revision_map = load_revision_map(args.revision_map.resolve() if args.revision_map else None)
    config = ToolConfig(
        run_root=run_root, dataset_dir=dataset_dir, database=database, cache_dir=run_root / "cache" / "mirrors",
        workspace_dir=run_root / "workspaces", log_dir=run_root / "logs",
        workers=args.workers or int(run_cfg.get("workers", 2)), max_revision_candidates=int(run_cfg.get("max_revision_candidates", 500)),
        timeout_seconds=int(run_cfg.get("build_timeout_seconds", 900)), keep_workspaces=args.keep_workspaces or bool(run_cfg.get("keep_workspaces", False)),
        default_java_home=str(java_cfg.get("default_home") or ""), java_homes={str(key): str(value) for key, value in (java_cfg.get("homes") or {}).items()}, revision_map=revision_map,
        fast_mode=args.fast,
        keep_repo_cache=args.keep_repo_cache or bool(run_cfg.get("keep_repo_cache", False)),
    )
    results = run_all(selected, config); records = [item.to_dict() for item in results]
    _write_jsonl(reports / "preflight_results.jsonl", records); _write_csv(reports / "preflight_results.csv", records)
    locked = [{key: value for key, value in record.items() if key not in {"build_attempts", "duration_seconds", "log_paths", "preflight_status", "technical_eligible", "strict_eligible", "tags", "reason_codes", "probe_status", "probed_api", "constructor_count", "method_count", "api_count"}} for record in records]
    _write_jsonl(manifests / "locked_input_manifest.jsonl", locked)
    technical = [record for record in records if record["technical_eligible"]]
    strict = [record for record in records if record["strict_eligible"]]
    _write_jsonl(manifests / "technical_eligible_manifest.jsonl", technical)
    _write_jsonl(manifests / "strict_eligible_manifest.jsonl", strict)
    summary: dict[str, Any] = {"total": len(records), "technical_eligible": len(technical), "strict_eligible": len(strict), "by_status": {}}
    for record in records: summary["by_status"][record["preflight_status"]] = summary["by_status"].get(record["preflight_status"], 0) + 1
    cleanup_path = reports / "repo_cleanup.jsonl"
    summary["repo_cleanup"] = {}
    if cleanup_path.exists():
        for line in cleanup_path.read_text(encoding="utf-8").splitlines():
            status = json.loads(line)["status"]
            summary["repo_cleanup"][status] = summary["repo_cleanup"].get(status, 0) + 1
    (reports / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False))
    print(f"Strict experiment input: {manifests / 'strict_eligible_manifest.jsonl'}")


if __name__ == "__main__":
    main()
