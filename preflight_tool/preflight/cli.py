from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import signal
import sqlite3
import sys
import threading
import tomllib
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from .checkpoint import (
    SCHEMA_VERSION,
    CheckpointError,
    CheckpointStore,
    RunLock,
    RunLockError,
    atomic_write_json,
    tool_fingerprint,
    write_progress,
)
from .ingest import build_index, candidates, input_rejections, resolve_dataset_dir
from .revision import RevisionChoice, load_revision_map
from .runner import ToolConfig, _cleanup_repo, run_all
from .shards import select_shard
from .util import json_compact, sha256_file


ROOT = Path(__file__).resolve().parents[1]


def _args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Evidence-preserving Classes2Test Java preflight")
    parser.add_argument("--config", type=Path)
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
    parser.add_argument("--resume", action="store_true", help="Resume an interrupted checkpointed run in --output-dir")
    return parser.parse_args()


def _usage_error(message: str) -> None:
    print(message, file=sys.stderr)
    raise SystemExit(2)


def _load_config(path: Path) -> dict[str, Any]:
    with path.open("rb") as handle:
        return tomllib.load(handle)


def _write_jsonl(path: Path, records: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + ".tmp")
    with temporary.open("w", encoding="utf-8", newline="\n") as handle:
        for record in records:
            handle.write(json.dumps(record, ensure_ascii=False, sort_keys=True) + "\n")
        handle.flush()
        os.fsync(handle.fileno())
    temporary.replace(path)


def _flat(record: dict[str, Any]) -> dict[str, Any]:
    flattened: dict[str, Any] = {}
    for key, value in record.items():
        flattened[key] = json_compact(value) if isinstance(value, (list, dict)) else value
    return flattened


def _write_csv(path: Path, records: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    fields = sorted({key for record in records for key in record})
    temporary = path.with_suffix(path.suffix + ".tmp")
    with temporary.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(_flat(record) for record in records)
        handle.flush()
        os.fsync(handle.fileno())
    temporary.replace(path)


def _candidate_record(item) -> dict[str, Any]:
    return {
        "task_id": item.task_id, "project_id": item.project_id, "repo_url": item.repo_url,
        "source_json_path": item.source_json_path, "source_json_sha256": item.source_json_sha256,
        "source_json_paths": list(item.source_json_paths), "source_json_sha256s": list(item.source_json_sha256s),
        "class_path": item.class_path, "class_fqn": item.class_fqn, "class_name": item.class_name,
        "test_class_path": item.test_class_paths[0] if item.test_class_paths else "",
        "test_class_paths": list(item.test_class_paths),
    }


def _revision_entries(revision_map: dict[str, RevisionChoice]) -> list[dict[str, Any]]:
    return [
        {
            "task_id": task_id,
            "checkout_sha": choice.checkout_sha,
            "revision_provenance": choice.provenance,
            "revision_verification_status": choice.status,
            "content_match": choice.content_match,
            "evidence_ref": choice.evidence_ref,
        }
        for task_id, choice in sorted(revision_map.items())
    ]


def _restore_revision_map(entries: list[dict[str, Any]]) -> dict[str, RevisionChoice]:
    return {
        str(entry["task_id"]): RevisionChoice(
            str(entry["checkout_sha"]), str(entry["revision_provenance"]),
            str(entry["revision_verification_status"]), entry.get("content_match"), str(entry.get("evidence_ref") or ""),
        )
        for entry in entries
    }


def _evidence_digest(selected: list[Any]) -> str:
    digest = hashlib.sha256()
    for item in selected:
        value = [item.task_id, list(item.source_json_paths), list(item.source_json_sha256s)]
        digest.update(json.dumps(value, ensure_ascii=True, separators=(",", ":")).encode("utf-8"))
    return digest.hexdigest()


def _validate_pending_evidence(dataset_dir: Path, selected: list[Any], cancel_event: threading.Event | None = None) -> None:
    observed: dict[Path, str] = {}
    for item in selected:
        if cancel_event and cancel_event.is_set():
            raise KeyboardInterrupt
        for relative, expected in zip(item.source_json_paths, item.source_json_sha256s):
            path = dataset_dir / relative
            if path not in observed:
                if not path.is_file():
                    raise CheckpointError(f"Dataset evidence is missing: {path}")
                observed[path] = sha256_file(path)
            if observed[path] != expected:
                raise CheckpointError(f"Dataset evidence changed since the run started: {path}")


def _load_selected(database: Path, task_ids: list[str]) -> list[Any]:
    if not database.is_file():
        raise CheckpointError(f"Class index is missing: {database}")
    wanted = set(task_ids)
    found = {item.task_id: item for item in candidates(database) if item.task_id in wanted}
    missing = wanted - found.keys()
    if missing:
        sample = ", ".join(sorted(missing)[:3])
        raise CheckpointError(f"Class index is missing checkpoint tasks: {sample}")
    return [found[task_id] for task_id in task_ids]


def _effective_config(contract: dict[str, Any], run_root: Path, workers: int, cancel_event: threading.Event) -> ToolConfig:
    effective = contract["effective"]
    return ToolConfig(
        run_root=run_root,
        dataset_dir=Path(contract["dataset_dir"]),
        database=run_root / "state" / "class_index.sqlite",
        cache_dir=run_root / "cache" / "mirrors",
        workspace_dir=run_root / "workspaces",
        log_dir=run_root / "logs",
        workers=workers,
        max_revision_candidates=int(effective["max_revision_candidates"]),
        timeout_seconds=int(effective["timeout_seconds"]),
        keep_workspaces=bool(effective["keep_workspaces"]),
        default_java_home=str(effective["default_java_home"]),
        java_homes={str(key): str(value) for key, value in effective["java_homes"].items()},
        revision_map=_restore_revision_map(contract["revision_map"]),
        fast_mode=bool(effective["fast_mode"]),
        keep_repo_cache=bool(effective["keep_repo_cache"]),
        cancel_event=cancel_event,
    )


def _materialize_cleanup(run_root: Path, store: CheckpointStore) -> None:
    _write_jsonl(run_root / "reports" / "repo_cleanup.jsonl", store.cleanup_events())


def _finalize(run_root: Path, store: CheckpointStore) -> dict[str, Any]:
    records = store.results()
    expected = set(store.task_ids())
    actual = {record["task_id"] for record in records}
    if len(records) != len(actual) or actual != expected:
        raise CheckpointError("Cannot finalize: checkpoint result set does not exactly match the run contract")
    reports, manifests = run_root / "reports", run_root / "manifests"
    _write_jsonl(reports / "preflight_results.jsonl", records)
    _write_csv(reports / "preflight_results.csv", records)
    omitted = {"build_attempts", "duration_seconds", "log_paths", "preflight_status", "technical_eligible", "strict_eligible", "tags", "reason_codes", "probe_status", "probed_api", "constructor_count", "method_count", "api_count"}
    locked = [{key: value for key, value in record.items() if key not in omitted} for record in records]
    technical = [record for record in records if record["technical_eligible"]]
    strict = [record for record in records if record["strict_eligible"]]
    _write_jsonl(manifests / "locked_input_manifest.jsonl", locked)
    _write_jsonl(manifests / "technical_eligible_manifest.jsonl", technical)
    _write_jsonl(manifests / "strict_eligible_manifest.jsonl", strict)
    _materialize_cleanup(run_root, store)
    by_status: dict[str, int] = {}
    for record in records:
        status = record["preflight_status"]
        by_status[status] = by_status.get(status, 0) + 1
    cleanup_counts: dict[str, int] = {}
    for event in store.cleanup_events():
        status = event["status"]
        cleanup_counts[status] = cleanup_counts.get(status, 0) + 1
    summary = {
        "total": len(records), "technical_eligible": len(technical), "strict_eligible": len(strict),
        "by_status": by_status, "repo_cleanup": cleanup_counts,
    }
    atomic_write_json(reports / "summary.json", summary)
    return summary


def _update_provenance(run_root: Path, store: CheckpointStore, run_status: str) -> None:
    path = run_root / "provenance.json"
    try:
        provenance = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise CheckpointError(f"Invalid provenance file: {path}") from exc
    sessions = store.sessions()
    provenance.update({
        "checkpoint_schema_version": SCHEMA_VERSION,
        "run_status": run_status,
        "resume_count": max(0, len(sessions) - 1),
        "sessions": sessions,
    })
    atomic_write_json(path, provenance)


def _install_signal_handlers(cancel_event: threading.Event):
    previous: dict[int, Any] = {}
    count = 0

    def handler(signum, frame):
        nonlocal count
        count += 1
        if count == 1:
            print("Interrupt received; stopping active work and preserving checkpoints...", file=sys.stderr)
            cancel_event.set()
            return
        raise KeyboardInterrupt

    for name in ("SIGINT", "SIGTERM"):
        signum = getattr(signal, name, None)
        if signum is not None:
            previous[signum] = signal.getsignal(signum)
            signal.signal(signum, handler)
    return previous


def _restore_signal_handlers(previous: dict[int, Any]) -> None:
    for signum, handler in previous.items():
        signal.signal(signum, handler)


def _run_checkpointed(run_root: Path, store: CheckpointStore, selected: list[Any], config: ToolConfig) -> None:
    abandoned = store.recover_abandoned()
    pending_ids = store.pending_task_ids()
    by_id = {item.task_id: item for item in selected}
    pending = [by_id[task_id] for task_id in pending_ids]
    session_id = store.begin_session(config.workers)
    counts = store.counts()
    print(f"Checkpoint: total={counts['total']} completed={counts['completed']} pending={counts['pending']} abandoned={abandoned}")
    write_progress(run_root, store, "RUNNING", session_id)
    _update_provenance(run_root, store, "RUNNING")
    previous_handlers = _install_signal_handlers(config.cancel_event or threading.Event())
    try:
        _validate_pending_evidence(config.dataset_dir, pending, config.cancel_event)
        run_all(
            pending, config, checkpoint=store, session_id=session_id,
            on_checkpoint=lambda: write_progress(run_root, store, "RUNNING", session_id),
        )
        if config.cancel_event and config.cancel_event.is_set():
            store.finish_session(session_id, "INTERRUPTED")
            write_progress(run_root, store, "INTERRUPTED", session_id)
            _materialize_cleanup(run_root, store)
            _update_provenance(run_root, store, "INTERRUPTED")
            raise SystemExit(130)

        existing_cleanup = store.cleanup_repo_urls()
        repo_tasks = store.all_tasks_for_repos()
        for repo_url in sorted(store.completed_repos() - existing_cleanup):
            store.put_cleanup_event(_cleanup_repo(repo_url, repo_tasks[repo_url], config, write_report=False))
        if store.counts()["completed"] != store.counts()["total"]:
            raise CheckpointError("Runner stopped without cancellation but checkpoint still has pending tasks")
        summary = _finalize(run_root, store)
        store.finish_session(session_id, "COMPLETED")
        write_progress(run_root, store, "COMPLETED", session_id)
        _update_provenance(run_root, store, "COMPLETED")
        print(json.dumps(summary, ensure_ascii=False))
        print(f"Strict experiment input: {run_root / 'manifests' / 'strict_eligible_manifest.jsonl'}")
    except SystemExit:
        raise
    except KeyboardInterrupt:
        if config.cancel_event:
            config.cancel_event.set()
        store.recover_abandoned()
        store.finish_session(session_id, "INTERRUPTED")
        write_progress(run_root, store, "INTERRUPTED", session_id)
        _update_provenance(run_root, store, "INTERRUPTED")
        raise SystemExit(130)
    except Exception:
        store.finish_session(session_id, "FAILED")
        write_progress(run_root, store, "FAILED", session_id)
        _update_provenance(run_root, store, "FAILED")
        raise
    finally:
        _restore_signal_handlers(previous_handlers)


def _resume(args: argparse.Namespace) -> None:
    if not args.output_dir:
        _usage_error("--resume requires --output-dir pointing to an existing full run")
    incompatible = []
    for name in ("config", "input_root", "revision_map", "shard"):
        if getattr(args, name) is not None:
            incompatible.append("--" + name.replace("_", "-"))
    for name in ("limit", "max_classes"):
        if getattr(args, name):
            incompatible.append("--" + name.replace("_", "-"))
    for name in ("index_only", "fast", "keep_workspaces", "keep_repo_cache"):
        if getattr(args, name):
            incompatible.append("--" + name.replace("_", "-"))
    if incompatible:
        _usage_error("--resume only accepts --output-dir and optional --workers; incompatible: " + ", ".join(incompatible))
    run_root = args.output_dir.expanduser().resolve()
    if not run_root.is_dir():
        _usage_error(f"Resume output directory does not exist: {run_root}")
    try:
        with RunLock(run_root / "state" / "run.lock"), CheckpointStore.open(run_root / "state" / "run_state.sqlite") as store:
            contract = store.contract
            if contract.get("tool_fingerprint") != tool_fingerprint(ROOT):
                raise CheckpointError("Tool fingerprint changed; this run cannot be resumed safely")
            manifest = run_root / "manifests" / "class_candidates.jsonl"
            if not manifest.is_file() or sha256_file(manifest) != contract.get("candidate_manifest_sha256"):
                raise CheckpointError("Candidate manifest changed since the run started")
            selected = _load_selected(run_root / "state" / "class_index.sqlite", store.task_ids())
            if _evidence_digest(selected) != contract.get("evidence_digest"):
                raise CheckpointError("Class index evidence does not match the run contract")
            cancel_event = threading.Event()
            workers = args.workers if args.workers is not None else int(contract["workers_default"])
            if workers < 1:
                _usage_error("--workers must be at least 1")
            config = _effective_config(contract, run_root, workers, cancel_event)
            _run_checkpointed(run_root, store, selected, config)
    except (CheckpointError, RunLockError, OSError, sqlite3.Error) as exc:
        _usage_error(f"Cannot resume run: {exc}")


def _fresh(args: argparse.Namespace) -> None:
    if args.shard and (args.limit or args.max_classes):
        _usage_error("--shard cannot be combined with --limit or --max-classes")
    config_path = (args.config or ROOT / "config.example.toml").expanduser().resolve()
    raw_config = _load_config(config_path)
    run_cfg = raw_config.get("run", {}); input_cfg = raw_config.get("input", {}); java_cfg = raw_config.get("java", {})
    input_root = (args.input_root or Path(input_cfg.get("root", ""))).expanduser().resolve()
    if not input_root.is_dir():
        _usage_error(f"Input root does not exist: {input_root}")
    dataset_dir = resolve_dataset_dir(input_root)
    run_id = run_cfg.get("id", "auto")
    if run_id == "auto":
        run_id = datetime.now(timezone.utc).strftime("preflight-%Y%m%dT%H%M%SZ")
    run_root = (args.output_dir or ROOT / "runs" / run_id).resolve()
    if run_root.exists() and any(run_root.iterdir()):
        _usage_error(f"Output directory already contains data: {run_root}; use a new directory or --resume a checkpointed run")
    manifests, reports = run_root / "manifests", run_root / "reports"
    database = run_root / "state" / "class_index.sqlite"
    json_count, class_count = build_index(dataset_dir, database, limit=args.limit)
    selected = list(candidates(database))
    shard_info: dict[str, Any] = {}
    if args.shard:
        try:
            selected, shard_info = select_shard(selected, args.shard)
        except (ValueError, OSError) as exc:
            _usage_error(f"Invalid shard: {exc}")
        shard_info.update({"shard_path": str(args.shard.resolve()), "shard_sha256": sha256_file(args.shard)})
    if args.max_classes:
        selected = selected[:args.max_classes]
    _write_jsonl(manifests / "class_candidates.jsonl", [_candidate_record(item) for item in selected])
    _write_jsonl(reports / "input_rejections.jsonl", input_rejections(database))
    provenance = {
        "tool": "class2test-preflight", "started_at_utc": datetime.now(timezone.utc).isoformat(),
        "input_root": str(input_root), "dataset_dir": str(dataset_dir), "config_path": str(config_path),
        "config_sha256": sha256_file(config_path), "raw_json_indexed": json_count,
        "deduplicated_classes": class_count, "classes_selected": len(selected),
        "keep_repo_cache": args.keep_repo_cache or bool(run_cfg.get("keep_repo_cache", False)),
        "keep_workspaces": args.keep_workspaces or bool(run_cfg.get("keep_workspaces", False)),
        "revision_policy": "Raw Classes2Test carries no source commit. History matches are CONTENT_MATCHED/DISCOVERY_ONLY; only an audited revision map may produce UPSTREAM_PINNED/STRICT output.",
    }
    if args.revision_map:
        provenance["revision_map"] = str(args.revision_map.resolve())
        provenance["revision_map_sha256"] = sha256_file(args.revision_map.resolve())
    provenance.update(shard_info)
    atomic_write_json(run_root / "provenance.json", provenance)
    if args.index_only:
        print(f"Indexed {json_count} JSON -> {len(selected)} class candidates")
        print(manifests / "class_candidates.jsonl")
        return

    revision_map = load_revision_map(args.revision_map.resolve() if args.revision_map else None)
    workers = args.workers if args.workers is not None else int(run_cfg.get("workers", 2))
    if workers < 1:
        _usage_error("--workers must be at least 1")
    effective = {
        "max_revision_candidates": int(run_cfg.get("max_revision_candidates", 500)),
        "timeout_seconds": int(run_cfg.get("build_timeout_seconds", 900)),
        "keep_workspaces": args.keep_workspaces or bool(run_cfg.get("keep_workspaces", False)),
        "default_java_home": str(java_cfg.get("default_home") or ""),
        "java_homes": {str(key): str(value) for key, value in (java_cfg.get("homes") or {}).items()},
        "fast_mode": args.fast,
        "keep_repo_cache": args.keep_repo_cache or bool(run_cfg.get("keep_repo_cache", False)),
    }
    contract = {
        "schema_version": SCHEMA_VERSION,
        "tool_fingerprint": tool_fingerprint(ROOT),
        "input_root": str(input_root), "dataset_dir": str(dataset_dir),
        "candidate_manifest_sha256": sha256_file(manifests / "class_candidates.jsonl"),
        "evidence_digest": _evidence_digest(selected),
        "selection": {"limit": args.limit, "max_classes": args.max_classes, **shard_info},
        "revision_map": _revision_entries(revision_map),
        "effective": effective,
        "workers_default": workers,
    }
    cancel_event = threading.Event()
    config = _effective_config(contract, run_root, workers, cancel_event)
    try:
        with RunLock(run_root / "state" / "run.lock"), CheckpointStore.create(run_root / "state" / "run_state.sqlite", contract, selected) as store:
            _run_checkpointed(run_root, store, selected, config)
    except (CheckpointError, RunLockError, OSError) as exc:
        print(f"Preflight run failed: {exc}", file=sys.stderr)
        raise SystemExit(1) from exc


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    args = _args()
    if args.resume:
        _resume(args)
    else:
        _fresh(args)


if __name__ == "__main__":
    main()
