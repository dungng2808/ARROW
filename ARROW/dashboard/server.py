from __future__ import annotations

import argparse
import base64
import json
import os
import re
import shutil
import subprocess
import sys
import threading
from datetime import datetime
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any
from urllib.parse import parse_qs, unquote, urlparse

try:
    import yaml
except ImportError:  # pragma: no cover - the project requirements include PyYAML.
    yaml = None

from src.llm_client import record_token_usage, token_usage_report
from src.java_resolver import platform_config_value
from src.experiment_filters import filter_label, filter_rows
from src.report_writer import load_experiment_jsonl, write_class_report, write_mean_report, write_rq1_exports
from src.rq2_export import build_rq2_rows, write_rq2_csv
from src.rq1_export import (
    RQ1ExcelLimitError,
    RQ1NoDataError,
    RQ1SnapshotChangedError,
    build_rq1_preview,
    load_rq1_snapshot,
    save_rq1_workbook,
)
from src.run_pipeline import _merge_reports
from src.metrics_recompute import recompute_metrics


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DASHBOARD_ROOT = Path(__file__).resolve().parent
STATIC_ROOT = DASHBOARD_ROOT / "static"
LOG_ROOT = DASHBOARD_ROOT / "logs"
RUNTIME_CONFIG_ROOT = DASHBOARD_ROOT / "runtime_configs"
RUN_RECORD_ROOT = DASHBOARD_ROOT / "run_records"
RUNTIME_SHARD_ROOT = DASHBOARD_ROOT / "runtime_shards"
RQ1_EXPORT_ROOT = PROJECT_ROOT / "export" / "RQ1"
SHARD_EXPORT_ROOT = PROJECT_ROOT / "export" / "shards"
DATASET_ROOT = PROJECT_ROOT.parent / "classes2test" / "dataset"
SHARD_ROOT = PROJECT_ROOT / "shards"

RUNS: dict[str, dict[str, Any]] = {}
RUN_LOCK = threading.Lock()
MERGE_LOCK = threading.Lock()
RQ1_EXPORT_LOCK = threading.Lock()
METRICS_RERUN_LOCK = threading.Lock()
DISCONNECTED_ERRORS = (BrokenPipeError, ConnectionResetError, ConnectionAbortedError)
RQ1_EXPORT_FILENAMES = {
    "summary": "rq1_summary.csv",
    "paired": "rq1_paired.csv",
    "details": "rq1_details.csv",
}
SHARD05_PROMPT_STRATEGIES = ("zero-shot", "few-shot", "zero-shot-project-aware")
ANSI_ESCAPE_RE = re.compile(r"\x1b(?:\[[0-?]*[ -/]*[@-~]|\][^\x07]*(?:\x07|\x1b\\))")
ERROR_MARKER_RE = re.compile(
    r"(?im)(?:\bERROR\b|BUILD FAILURE|COMPILATION (?:ERROR|FAILURE)|cannot find symbol|"
    r"failed with an exception|failed to execute goal|exception caught|caused by:|"
    r"there were failing tests|tests run:.*(?:failures|errors):\s*[1-9])"
)


def _ensure_runtime_dirs() -> None:
    LOG_ROOT.mkdir(parents=True, exist_ok=True)
    RUNTIME_CONFIG_ROOT.mkdir(parents=True, exist_ok=True)
    RUN_RECORD_ROOT.mkdir(parents=True, exist_ok=True)
    RUNTIME_SHARD_ROOT.mkdir(parents=True, exist_ok=True)


def _run_record_path(run_id: str) -> Path:
    return RUN_RECORD_ROOT / f"{_safe_name(run_id)}.json"


def _persist_run(run: dict[str, Any]) -> None:
    path = _run_record_path(str(run.get("id") or ""))
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(".json.tmp")
    temporary.write_text(json.dumps(run, ensure_ascii=False, indent=2, default=str) + "\n", encoding="utf-8")
    os.replace(temporary, path)


def _load_run_records() -> None:
    _ensure_runtime_dirs()
    loaded: dict[str, dict[str, Any]] = {}
    for path in sorted(RUN_RECORD_ROOT.glob("*.json"), key=lambda item: item.name):
        run = _read_json(path)
        run_id = _safe_name(str(run.get("id") or path.stem))
        if not run_id:
            continue
        run["id"] = run_id
        if run.get("status") in {"running", "stopping"}:
            run["status"] = "stopped"
            run["finished_at"] = run.get("finished_at") or datetime.now().isoformat(timespec="seconds")
            for project in run.get("project_logs", []):
                if project.get("status") == "running":
                    project["status"] = "stopped"
            _persist_run(run)
        loaded[run_id] = run
    for log_path in sorted(LOG_ROOT.glob("*.log"), key=lambda item: item.name):
        run_id = _safe_name(log_path.stem)
        if not run_id or run_id in loaded:
            continue
        legacy = _legacy_run_record(run_id, log_path)
        if legacy:
            loaded[run_id] = legacy
            _persist_run(legacy)
    with RUN_LOCK:
        RUNS.clear()
        RUNS.update(loaded)


def _json_response(handler: SimpleHTTPRequestHandler, payload: Any, status: int = 200) -> None:
    data = json.dumps(payload, ensure_ascii=False, indent=2).encode("utf-8")
    try:
        handler.send_response(status)
        handler.send_header("Content-Type", "application/json; charset=utf-8")
        handler.send_header("Content-Length", str(len(data)))
        handler.end_headers()
        handler.wfile.write(data)
    except DISCONNECTED_ERRORS:
        return


def _text_response(handler: SimpleHTTPRequestHandler, text: str, status: int = 200, content_type: str = "text/plain; charset=utf-8") -> None:
    data = text.encode("utf-8", errors="replace")
    try:
        handler.send_response(status)
        handler.send_header("Content-Type", content_type)
        handler.send_header("Content-Length", str(len(data)))
        handler.end_headers()
        handler.wfile.write(data)
    except DISCONNECTED_ERRORS:
        return


def _read_json(path: Path) -> dict[str, Any]:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return {}


def _read_text(path: Path, limit: int = 80_000) -> str:
    if not path.is_file():
        return ""
    text = path.read_text(encoding="utf-8", errors="replace")
    if len(text) <= limit:
        return text
    return text[:limit] + "\n\n[truncated]\n"


def _clean_output(text: str) -> str:
    return ANSI_ESCAPE_RE.sub("", text).replace("\r\n", "\n")


def _json_contains_error(value: Any, key: str = "") -> bool:
    if isinstance(value, dict):
        return any(_json_contains_error(item, str(item_key)) for item_key, item in value.items())
    if isinstance(value, list):
        return any(_json_contains_error(item, key) for item in value)
    if value in {None, "", False, 0}:
        return False
    lowered_key = key.lower()
    text = str(value).strip().upper()
    if lowered_key == "error" or lowered_key.endswith("_error"):
        return True
    if lowered_key in {"primary_error", "test_fail_reason", "normalized_error_signature"}:
        return True
    if lowered_key in {"state", "initial_failure_state", "final_failure_state"}:
        return text not in {"TARGET_TEST_PASSED", "MODULE_TESTS_PASSED", "PASSED", "SUCCESS"}
    if lowered_key == "decision":
        return text in {"REGRESSION", "REPEATED_ERROR", "REPEATED_CODE", "NO_PROGRESS", "INVALID_LLM_OUTPUT", "STOP"}
    return False


def _error_artifact_candidates(experiment_dir: Path) -> list[Path]:
    candidates: set[Path] = set()
    for pattern in ("*_build_output.txt", "*_verification.json", "metrics_report.json", "repair_summary.json"):
        candidates.update(path for path in experiment_dir.glob(pattern) if path.is_file())
    checkpoint_root = experiment_dir / "repair" / "checkpoints"
    if checkpoint_root.is_dir():
        for pattern in ("attempt_*/build_output_before.txt", "attempt_*/build_output_after.txt", "attempt_*/decision.json"):
            candidates.update(path for path in checkpoint_root.glob(pattern) if path.is_file())
    return sorted(candidates, key=lambda path: path.relative_to(experiment_dir).as_posix())


def _error_artifact_content(path: Path, limit: int = 500_000) -> str:
    return _clean_output(_read_text(path, limit=limit))


def _is_error_artifact(path: Path) -> tuple[bool, int]:
    content = _error_artifact_content(path)
    if path.suffix.lower() == ".json":
        try:
            has_error = _json_contains_error(json.loads(content))
        except Exception:
            has_error = bool(ERROR_MARKER_RE.search(content))
    else:
        has_error = bool(ERROR_MARKER_RE.search(content))
    return has_error, len(ERROR_MARKER_RE.findall(content))


def _encode_artifact_path(experiment_dir: Path, path: Path) -> str:
    relative = path.resolve().relative_to(experiment_dir.resolve()).as_posix()
    return base64.urlsafe_b64encode(relative.encode("utf-8")).decode("ascii").rstrip("=")


def _decode_artifact_path(experiment_dir: Path, token: str) -> Path:
    padded = token + ("=" * (-len(token) % 4))
    relative = Path(base64.urlsafe_b64decode(padded.encode("ascii")).decode("utf-8"))
    candidate = (experiment_dir / relative).resolve()
    root = experiment_dir.resolve()
    if root not in candidate.parents or not candidate.is_file():
        raise ValueError("Invalid error artifact id")
    return candidate


def _error_artifact_summaries(experiment_dir: Path) -> list[dict[str, Any]]:
    summaries = []
    for path in _error_artifact_candidates(experiment_dir):
        has_error, marker_count = _is_error_artifact(path)
        if not has_error:
            continue
        relative = path.relative_to(experiment_dir).as_posix()
        summaries.append(
            {
                "id": _encode_artifact_path(experiment_dir, path),
                "name": path.name,
                "relative_path": relative,
                "size_bytes": path.stat().st_size,
                "error_markers": marker_count,
            }
        )
    return summaries


def _combined_error_artifacts(experiment_dir: Path) -> str:
    sections = []
    for item in _error_artifact_summaries(experiment_dir):
        path = _decode_artifact_path(experiment_dir, item["id"])
        sections.append(f"===== {item['relative_path']} =====\n{_error_artifact_content(path)}")
    return "\n\n".join(sections)


def _project_config() -> dict[str, Any]:
    config_path = PROJECT_ROOT / "config" / "pipeline.yaml"
    if yaml is None or not config_path.is_file():
        return {}
    return yaml.safe_load(config_path.read_text(encoding="utf-8")) or {}


def _merge_reports_now() -> dict[str, Any]:
    if not MERGE_LOCK.acquire(blocking=False):
        raise RuntimeError("Report merge is already running")
    try:
        config = _project_config()
        runs_dir = (PROJECT_ROOT / "runs").resolve()
        configured_output = Path(config.get("report", {}).get("merged_dir", "merged"))
        output_dir = configured_output if configured_output.is_absolute() else runs_dir / configured_output
        _merge_reports(
            argparse.Namespace(runs_dir=runs_dir, output_dir=output_dir),
            config,
        )
        merged_jsonl = output_dir / "experiments_merged.jsonl"
        rq1_exports = write_rq1_exports(output_dir, load_experiment_jsonl([merged_jsonl]))
        summary = _read_json(output_dir / "merge_summary.json")
        return {
            **summary,
            "output_dir": str(output_dir),
            "artifacts": {
                "merged_jsonl": str(merged_jsonl),
                "class_report": str(output_dir / "output_agone_classes_lite.csv"),
                "mean_report": str(output_dir / "output_agone_mean_lite.csv"),
                "statistics": str(output_dir / "experiment_statistics.json"),
                "statistics_csv": str(output_dir / "experiment_statistics_by_group.csv"),
                "rq1": rq1_exports,
            },
        }
    finally:
        MERGE_LOCK.release()


def _save_rq1_export(export_kind: str) -> dict[str, Any]:
    if export_kind not in RQ1_EXPORT_FILENAMES:
        raise ValueError("Unknown CSV export type")
    merged = _merge_reports_now()
    metadata = merged.get("artifacts", {}).get("rq1", {}).get(export_kind, {})
    source = Path(str(merged.get("output_dir") or "")) / RQ1_EXPORT_FILENAMES[export_kind]
    if not source.is_file():
        raise FileNotFoundError(f"CSV export was not created: {source.name}")

    RQ1_EXPORT_ROOT.mkdir(parents=True, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    base_name = f"rq1_{export_kind}_{timestamp}"
    destination = RQ1_EXPORT_ROOT / f"{base_name}.csv"
    suffix = 2
    while destination.exists():
        destination = RQ1_EXPORT_ROOT / f"{base_name}_{suffix}.csv"
        suffix += 1
    temporary = destination.with_suffix(".csv.tmp")
    shutil.copyfile(source, temporary)
    os.replace(temporary, destination)
    try:
        relative_path = destination.relative_to(PROJECT_ROOT).as_posix()
    except ValueError:
        relative_path = str(destination)
    return {
        "export_type": export_kind,
        "filename": destination.name,
        "path": str(destination),
        "relative_path": relative_path,
        "rows": int(metadata.get("rows") or 0),
    }


def _shard_id_matches(row: dict[str, Any], shard_id: str) -> bool:
    normalized = _safe_name(shard_id)
    candidates = {
        normalized,
        normalized.removesuffix(".txt"),
        f"{normalized}.txt",
    }
    row_shard = _safe_name(str(row.get("shard_id") or ""))
    return row_shard in candidates


def _experiment_passed(row: dict[str, Any]) -> bool:
    return row.get("module_tests_passed") is True or row.get("test_passed") is True


def _first_value(row: dict[str, Any], keys: tuple[str, ...]) -> str:
    for key in keys:
        value = row.get(key)
        if value not in {None, ""}:
            return str(value)
    return ""


def _prompt_strategy(row: dict[str, Any]) -> str:
    return str(row.get("generation_prompt_strategy") or row.get("Prompt_Technique") or "").strip()


def _experiment_logical_key(row: dict[str, Any]) -> tuple[str, str, str, str]:
    return (
        _safe_name(str(row.get("project_id") or row.get("Project_ID") or "")),
        _first_value(row, ("sample_id", "input_id")),
        str(row.get("agent_name") or row.get("Generator(LLM)") or ""),
        _prompt_strategy(row),
    )


def _experiment_sort_key(row: dict[str, Any]) -> tuple[str, str, str, str]:
    return (
        str(row.get("finished_at") or ""),
        str(row.get("started_at") or ""),
        str(row.get("run_id") or ""),
        str(row.get("result_path") or row.get("dashboard_id") or ""),
    )


def _latest_logical_experiment_rows(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    latest: dict[tuple[str, str, str, str], dict[str, Any]] = {}
    for row in rows:
        key = _experiment_logical_key(row)
        if key not in latest or _experiment_sort_key(row) > _experiment_sort_key(latest[key]):
            latest[key] = row
    return sorted(latest.values(), key=_experiment_sort_key, reverse=True)


def _prompt_summary(rows: list[dict[str, Any]], prompt_strategy: str, log: dict[str, Any]) -> dict[str, Any]:
    prompt_rows = [row for row in rows if _prompt_strategy(row) == prompt_strategy]
    failed_rows = [row for row in prompt_rows if not _experiment_passed(row)]
    latest_failed = failed_rows[0] if failed_rows else {}
    if str(log.get("project_status") or "") == "running" and str(log.get("prompt") or "") == prompt_strategy:
        status = "RUNNING"
    elif not prompt_rows:
        status = "NOT_RUN"
    elif failed_rows:
        status = "HAS_FAILURES"
    else:
        status = "DONE"
    return {
        "prompt_strategy": prompt_strategy,
        "status": status,
        "total": len(prompt_rows),
        "passed": len(prompt_rows) - len(failed_rows),
        "failed": len(failed_rows),
        "latest_failed_experiment_id": latest_failed.get("dashboard_id", ""),
        "latest_failed_sample_id": _first_value(latest_failed, ("sample_id", "input_id")) if latest_failed else "",
        "latest_failed_agent": latest_failed.get("agent_name", "") if latest_failed else "",
        "latest_failed_state": latest_failed.get("final_failure_state") or latest_failed.get("initial_failure_state") or "",
    }


def _save_shard_export(shard_id: str = "repo_shard_05") -> dict[str, Any]:
    merged = _merge_reports_now()
    output_dir = Path(str(merged.get("output_dir") or PROJECT_ROOT / "runs" / "merged"))
    merged_jsonl = Path(str(merged.get("artifacts", {}).get("merged_jsonl") or output_dir / "experiments_merged.jsonl"))
    rows = [row for row in load_experiment_jsonl([merged_jsonl]) if _shard_id_matches(row, shard_id)]
    if not rows:
        raise ValueError(f"Không có dữ liệu đã chạy cho shard {shard_id}")

    SHARD_EXPORT_ROOT.mkdir(parents=True, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    safe_shard = _safe_name(shard_id) or "shard"
    base_name = f"{safe_shard}_runs_{timestamp}"
    destination = SHARD_EXPORT_ROOT / f"{base_name}.csv"
    mean_destination = SHARD_EXPORT_ROOT / f"{safe_shard}_mean_{timestamp}.csv"
    suffix = 2
    while destination.exists() or mean_destination.exists():
        destination = SHARD_EXPORT_ROOT / f"{base_name}_{suffix}.csv"
        mean_destination = SHARD_EXPORT_ROOT / f"{safe_shard}_mean_{timestamp}_{suffix}.csv"
        suffix += 1
    temporary = destination.with_suffix(".csv.tmp")
    mean_temporary = mean_destination.with_suffix(".csv.tmp")
    write_class_report(temporary, rows)
    write_mean_report(mean_temporary, rows)
    os.replace(temporary, destination)
    os.replace(mean_temporary, mean_destination)
    try:
        relative_path = destination.relative_to(PROJECT_ROOT).as_posix()
    except ValueError:
        relative_path = str(destination)
    try:
        mean_relative_path = mean_destination.relative_to(PROJECT_ROOT).as_posix()
    except ValueError:
        mean_relative_path = str(mean_destination)
    return {
        "export_type": "shard_runs",
        "shard_id": shard_id,
        "filename": destination.name,
        "path": str(destination),
        "relative_path": relative_path,
        "mean_filename": mean_destination.name,
        "mean_path": str(mean_destination),
        "mean_relative_path": mean_relative_path,
        "rows": len(rows),
    }


def _save_rq2_export(shard_id: str = "repo_shard_05", *, only_generated_failures: bool = True) -> dict[str, Any]:
    """Export the RQ2 repair table for the latest logical shard results."""
    merged = _merge_reports_now()
    output_dir = Path(str(merged.get("output_dir") or PROJECT_ROOT / "runs" / "merged"))
    merged_jsonl = Path(str(merged.get("artifacts", {}).get("merged_jsonl") or output_dir / "experiments_merged.jsonl"))
    raw_rows = [row for row in load_experiment_jsonl([merged_jsonl]) if _shard_id_matches(row, shard_id)]
    source_rows = _latest_logical_experiment_rows(raw_rows)
    if not source_rows:
        raise ValueError(f"Không có dữ liệu đã chạy cho shard {shard_id}")
    mode = "baseline_passed_generated_failed" if only_generated_failures else "all"
    rows, filter_stats = filter_rows(source_rows, mode=mode)
    if not rows:
        raise ValueError("Không có experiment nào thỏa điều kiện lọc RQ2")
    summary_rows = build_rq2_rows(rows)
    if not summary_rows:
        raise ValueError(f"Không có dữ liệu repair hợp lệ cho shard {shard_id}")

    SHARD_EXPORT_ROOT.mkdir(parents=True, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    suffix_label = "filtered" if only_generated_failures else "all"
    base_name = f"{_safe_name(shard_id)}_rq2_{suffix_label}_{timestamp}"
    destination = SHARD_EXPORT_ROOT / f"{base_name}.csv"
    suffix = 2
    while destination.exists():
        destination = SHARD_EXPORT_ROOT / f"{base_name}_{suffix}.csv"
        suffix += 1
    temporary = destination.with_suffix(".csv.tmp")
    write_rq2_csv(temporary, summary_rows)
    os.replace(temporary, destination)
    try:
        relative_path = destination.relative_to(PROJECT_ROOT).as_posix()
    except ValueError:
        relative_path = str(destination)
    return {
        "export_type": "rq2_repair",
        "shard_id": shard_id,
        "filename": destination.name,
        "path": str(destination),
        "relative_path": relative_path,
        "rows": len(summary_rows),
        "source_experiments": len(source_rows),
        "selected_experiments": len(rows),
        "excluded_experiments": len(source_rows) - len(rows),
        "filter_mode": filter_label(mode),
        "excluded_reasons": {key: value for key, value in filter_stats.items() if key not in {"selected", "excluded"}},
        "mechanisms": [row["Repair mechanism"] for row in summary_rows],
        "warning": "Repair time falls back to total experiment time for legacy records without repair_time_seconds.",
    }


def _sample_for_project(project_id: str) -> dict[str, str]:
    project_dir = DATASET_ROOT / project_id
    files = sorted(project_dir.glob("*.json"), key=lambda item: item.name) if project_dir.is_dir() else []
    if not files:
        return {"sample_id": "", "sample_file": "", "focal_class": "", "focal_class_path": ""}
    sample_file = files[0]
    payload = _read_json(sample_file)
    focal = payload.get("focal_class") if isinstance(payload.get("focal_class"), dict) else {}
    return {
        "sample_id": sample_file.stem,
        "sample_file": sample_file.name,
        "focal_class": str(focal.get("identifier") or ""),
        "focal_class_path": str(focal.get("file") or ""),
    }


def _latest_project_log_status(shard_id: str) -> dict[str, dict[str, Any]]:
    statuses: dict[str, dict[str, Any]] = {}
    normalized_shard = _safe_name(shard_id)
    with RUN_LOCK:
        runs = sorted(RUNS.values(), key=lambda item: str(item.get("started_at") or item.get("id") or ""))
        for run in runs:
            request = run.get("request") if isinstance(run.get("request"), dict) else {}
            if _safe_name(str(request.get("shard_id") or Path(str(request.get("repo_shard") or "")).stem)) != normalized_shard:
                continue
            for project in run.get("project_logs", []):
                project_id = _safe_name(str(project.get("project_id") or ""))
                if not project_id:
                    continue
                statuses[project_id] = {
                    "run_id": run.get("id", ""),
                    "run_status": run.get("status", ""),
                    "project_status": project.get("status", ""),
                    "experiments_completed": int(project.get("experiments_completed") or 0),
                    "failed_experiments": int(project.get("failed_experiments") or 0),
                    "last_experiment_passed": project.get("last_experiment_passed"),
                    "last_experiment_status": project.get("last_experiment_status", ""),
                    "agent": project.get("agent", ""),
                    "prompt": project.get("prompt", ""),
                }
    return statuses


def _shard_status(shard_id: str = "repo_shard_05") -> dict[str, Any]:
    shard_file = _safe_shard_file(f"{_safe_name(shard_id).removesuffix('.txt')}.txt")
    if shard_file is None:
        raise ValueError(f"Không tìm thấy shard {shard_id}")
    projects = _shard_project_ids(shard_file)
    rows_by_project: dict[str, list[dict[str, Any]]] = {project: [] for project in projects}
    shard_experiments: list[dict[str, Any]] = []
    for row in _latest_logical_experiment_rows(_experiments()):
        project_id = _safe_name(str(row.get("project_id") or row.get("Project_ID") or ""))
        if project_id in rows_by_project and _shard_id_matches(row, shard_id):
            rows_by_project[project_id].append(row)
            shard_experiments.append(row)
    log_status = _latest_project_log_status(shard_id)
    items = []
    for index, project_id in enumerate(projects, start=1):
        rows = rows_by_project.get(project_id, [])
        sample = _sample_for_project(project_id)
        log = log_status.get(project_id, {})
        completed = len(rows)
        failed_rows = [row for row in rows if not _experiment_passed(row)]
        failed = len(failed_rows)
        passed = completed - failed
        latest_failed = failed_rows[0] if failed_rows else {}
        prompt_statuses = {
            prompt: _prompt_summary(rows, prompt, log)
            for prompt in SHARD05_PROMPT_STRATEGIES
        }
        if str(log.get("project_status") or "") == "running":
            status = "RUNNING"
        elif completed == 0:
            status = "NOT_RUN"
        elif failed:
            status = "HAS_FAILURES"
        else:
            status = "DONE"
        items.append(
            {
                "index": index,
                "project_id": project_id,
                **sample,
                "status": status,
                "experiments_completed": completed,
                "passed_experiments": passed,
                "failed_experiments": failed,
                "last_run_id": log.get("run_id", ""),
                "last_run_status": log.get("run_status", ""),
                "last_project_status": log.get("project_status", ""),
                "last_agent": log.get("agent", ""),
                "last_prompt": log.get("prompt", ""),
                "prompt_statuses": prompt_statuses,
                "latest_failed_experiment_id": latest_failed.get("dashboard_id", ""),
                "latest_failed_sample_id": _first_value(latest_failed, ("sample_id", "input_id")) if latest_failed else "",
                "latest_failed_agent": latest_failed.get("agent_name", "") if latest_failed else "",
                "latest_failed_prompt": latest_failed.get("generation_prompt_strategy", "") if latest_failed else "",
            }
        )
    summary = {
        "total_projects": len(items),
        "not_run": sum(1 for item in items if item["status"] == "NOT_RUN"),
        "running": sum(1 for item in items if item["status"] == "RUNNING"),
        "done": sum(1 for item in items if item["status"] == "DONE"),
        "has_failures": sum(1 for item in items if item["status"] == "HAS_FAILURES"),
        "experiments_completed": sum(int(item["experiments_completed"]) for item in items),
        "failed_experiments": sum(int(item["failed_experiments"]) for item in items),
    }
    return {
        "shard_id": shard_id,
        "shard_file": str(shard_file),
        "summary": summary,
        "projects": items,
        "experiments": shard_experiments,
    }


def _shard_project_rows(project_id: str, shard_id: str = "repo_shard_05") -> list[dict[str, Any]]:
    safe_project = _safe_name(project_id)
    return _latest_logical_experiment_rows([
        row
        for row in _experiments()
        if _safe_name(str(row.get("project_id") or row.get("Project_ID") or "")) == safe_project
        and _shard_id_matches(row, shard_id)
    ])


def _fallback_error_summary(row: dict[str, Any]) -> str:
    keys = [
        "initial_failure_state",
        "final_failure_state",
        "initial_failure_origin",
        "final_failure_origin",
        "repair_status",
        "repair_stopped_reason",
        "test_fail_reason",
        "error",
        "coverage_error",
        "mutation_error",
        "smell_error",
    ]
    lines = []
    for key in keys:
        value = row.get(key)
        if value not in {None, ""}:
            lines.append(f"{key}: {value}")
    return "\n".join(lines)


def _shard_project_errors(project_id: str, shard_id: str = "repo_shard_05", prompt_strategy: str = "") -> dict[str, Any]:
    shard_file = _safe_shard_file(f"{_safe_name(shard_id).removesuffix('.txt')}.txt")
    safe_project = _safe_name(project_id)
    if shard_file is None or safe_project not in _shard_project_ids(shard_file):
        raise ValueError(f"Project {project_id} không nằm trong {shard_id}")
    failed_rows = [row for row in _shard_project_rows(safe_project, shard_id) if not _experiment_passed(row)]
    prompt_strategy = _safe_name(prompt_strategy)
    if prompt_strategy:
        failed_rows = [row for row in failed_rows if _prompt_strategy(row) == prompt_strategy]
    experiments = []
    sections = []
    for row in failed_rows:
        experiment_dir = Path(str(row.get("experiment_dir") or ""))
        error_files = _error_artifact_summaries(experiment_dir) if experiment_dir.is_dir() else []
        content = _combined_error_artifacts(experiment_dir) if experiment_dir.is_dir() else ""
        fallback = _fallback_error_summary(row)
        if not content and fallback:
            content = fallback
        header = (
            f"===== project={safe_project} sample={_first_value(row, ('sample_id', 'input_id'))} "
            f"agent={row.get('agent_name', '')} prompt={row.get('generation_prompt_strategy', '')} ====="
        )
        if content:
            sections.append(f"{header}\n{content}")
        experiments.append(
            {
                "dashboard_id": row.get("dashboard_id", ""),
                "sample_id": _first_value(row, ("sample_id", "input_id")),
                "agent_name": row.get("agent_name", ""),
                "generation_prompt_strategy": row.get("generation_prompt_strategy", ""),
                "state": row.get("final_failure_state") or row.get("initial_failure_state") or "",
                "error_files": error_files,
            }
        )
    return {
        "project_id": safe_project,
        "prompt_strategy": prompt_strategy,
        "failed_experiments": len(failed_rows),
        "experiments": experiments,
        "content": "\n\n".join(sections),
    }


def _rq1_preview(query: str = "") -> dict[str, Any]:
    params = parse_qs(query)
    paired_page = int(params.get("paired_page", ["1"])[0])
    details_page = int(params.get("details_page", ["1"])[0])
    page_size = int(params.get("page_size", ["50"])[0])
    raw_filter = params.get("baseline_valid_only", ["true"])[0].strip().lower()
    if raw_filter not in {"true", "false", "1", "0", "yes", "no"}:
        raise ValueError("baseline_valid_only phải là boolean")
    baseline_valid_only = raw_filter in {"true", "1", "yes"}
    snapshot = load_rq1_snapshot(PROJECT_ROOT / "runs", baseline_valid_only=baseline_valid_only)
    return build_rq1_preview(
        snapshot,
        paired_page=paired_page,
        details_page=details_page,
        page_size=page_size,
    )


def _save_rq1_workbook(preview_revision: str = "", *, baseline_valid_only: bool = True) -> dict[str, Any]:
    if not RQ1_EXPORT_LOCK.acquire(blocking=False):
        raise RuntimeError("Đang có yêu cầu xuất RQ1 khác chạy")
    try:
        snapshot = load_rq1_snapshot(PROJECT_ROOT / "runs", baseline_valid_only=baseline_valid_only)
        return save_rq1_workbook(
            RQ1_EXPORT_ROOT,
            PROJECT_ROOT,
            snapshot,
            preview_revision=preview_revision,
        )
    finally:
        RQ1_EXPORT_LOCK.release()


def _detect_java() -> dict[str, Any]:
    java_exe = shutil.which("java")
    detected = {
        "java_executable": java_exe or "",
        "java_home_env": os.environ.get("JAVA_HOME", ""),
        "java_home_detected": "",
        "version_output": "",
        "available": bool(java_exe),
    }
    if not java_exe:
        return detected
    try:
        result = subprocess.run(
            [java_exe, "-XshowSettings:properties", "-version"],
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=10,
        )
    except Exception as exc:
        detected["version_output"] = str(exc)
        return detected
    output = ((result.stdout or "") + "\n" + (result.stderr or "")).strip()
    detected["version_output"] = output
    match = re.search(r"(?m)^\s*java\.home\s*=\s*(.+?)\s*$", output)
    if match:
        detected["java_home_detected"] = match.group(1)
    return detected


def _encode_result_path(path: Path) -> str:
    relative = path.resolve().relative_to((PROJECT_ROOT / "runs").resolve())
    token = base64.urlsafe_b64encode(str(relative).encode("utf-8")).decode("ascii")
    return token.rstrip("=")


def _decode_result_path(token: str) -> Path:
    padded = token + ("=" * (-len(token) % 4))
    relative = Path(base64.urlsafe_b64decode(padded.encode("ascii")).decode("utf-8"))
    candidate = (PROJECT_ROOT / "runs" / relative).resolve()
    runs_root = (PROJECT_ROOT / "runs").resolve()
    if runs_root not in candidate.parents or candidate.name != "result.json":
        raise ValueError("Invalid experiment id")
    return candidate


def _experiment_dir(row: dict[str, Any], result_path: Path) -> Path:
    configured = row.get("experiment_dir")
    if configured:
        path = Path(str(configured))
        if path.exists():
            return path
    # runs/<repo>/<sample>/reports/records/<sample>/<agent>/<prompt>/result.json
    parts = result_path.resolve().parts
    try:
        reports_index = parts.index("reports")
        run_sample_root = Path(*parts[:reports_index])
        agent = result_path.parent.parent.name
        prompt = result_path.parent.name
        return run_sample_root / agent / prompt
    except ValueError:
        return result_path.parent


def _checkpoint_summaries(experiment_dir: Path) -> list[dict[str, Any]]:
    checkpoint_root = experiment_dir / "repair" / "checkpoints"
    summaries: list[dict[str, Any]] = []
    if not checkpoint_root.is_dir():
        return summaries
    for path in sorted(checkpoint_root.glob("attempt_*"), key=lambda item: int(item.name.split("_")[-1]) if item.name.split("_")[-1].isdigit() else 0):
        decision = _read_json(path / "decision.json")
        summaries.append(
            {
                "attempt": path.name,
                "attempt_number": decision.get("attempt_number") or path.name.replace("attempt_", ""),
                "previous_state": decision.get("previous_state", ""),
                "new_state": decision.get("new_state", ""),
                "decision": decision.get("decision", ""),
                "reason": decision.get("reason", ""),
                "rollback_performed": bool(decision.get("rollback_performed")),
                "prompt_switched": bool(decision.get("prompt_switched")),
                "build_skipped": bool(decision.get("build_skipped")),
                "path": str(path),
            }
        )
    return summaries


def _load_experiment(result_path: Path) -> dict[str, Any]:
    row = _read_json(result_path)
    experiment_dir = _experiment_dir(row, result_path)
    repair_summary = _read_json(experiment_dir / "repair_summary.json")
    row["dashboard_id"] = _encode_result_path(result_path)
    row["result_path"] = str(result_path)
    row["experiment_dir"] = str(experiment_dir)
    row["checkpoint_count"] = len(_checkpoint_summaries(experiment_dir))
    if repair_summary:
        row["repair_summary"] = repair_summary
    token_report = _experiment_token_usage(row, experiment_dir)
    for key, value in token_report.items():
        current = row.get(key)
        if current is None or current == "":
            row[key] = value
    return row


def _experiment_token_usage(row: dict[str, Any], experiment_dir: Path) -> dict[str, Any]:
    saved = _read_json(experiment_dir / "token_usage.json")
    if saved:
        return saved
    generation = _read_json(experiment_dir / "generation_metadata.json")
    metadata = generation.get("metadata")
    if not isinstance(metadata, dict):
        return {}
    bucket: dict[str, dict[str, int]] = {}
    strategy = str(row.get("generation_prompt_strategy") or row.get("Prompt_Technique") or "unknown")
    record_token_usage(bucket, f"generation:{strategy}", metadata)
    return token_usage_report(bucket) if bucket else {}


def _experiments() -> list[dict[str, Any]]:
    runs_root = PROJECT_ROOT / "runs"
    if not runs_root.is_dir():
        return []
    rows = []
    for path in runs_root.rglob("result.json"):
        rows.append(_load_experiment(path))
    rows.sort(key=lambda row: str(row.get("finished_at") or row.get("started_at") or ""), reverse=True)
    return rows


def _tail(path: Path, lines: int = 300) -> str:
    if not path.is_file():
        return ""
    content = path.read_text(encoding="utf-8", errors="replace").splitlines()
    return "\n".join(content[-lines:])


def _safe_name(value: str) -> str:
    return "".join(ch for ch in value if ch.isalnum() or ch in {"_", "-", "."})


def _selected_generation_prompts(payload: dict[str, Any]) -> list[str]:
    raw = payload.get("generation_prompts")
    if raw is None:
        legacy = str(payload.get("generation_prompt", "")).strip()
        return [legacy] if legacy else []
    if not isinstance(raw, list):
        raise ValueError("generation_prompts must be a list")
    selected: list[str] = []
    for item in raw:
        name = _safe_name(str(item).strip())
        if name and name not in selected:
            selected.append(name)
    if not selected:
        raise ValueError("Select at least one generation prompt")
    return selected


def _selected_agents(payload: dict[str, Any]) -> list[str]:
    raw = payload.get("agents")
    if raw is None:
        legacy = str(payload.get("agent", "")).strip()
        return [legacy] if legacy else []
    if not isinstance(raw, list):
        raise ValueError("agents must be a list")
    selected: list[str] = []
    for item in raw:
        name = _safe_name(str(item).strip())
        if name and name not in selected:
            selected.append(name)
    if not selected:
        raise ValueError("Select at least one model")
    return selected


def _start_event(line: str) -> dict[str, str] | None:
    match = re.search(r"(?:^|\]\s*)START\s+(\S+)(.*)$", line.strip())
    if not match:
        return None
    sample_id = match.group(1)
    fields: dict[str, str] = {"sample_id": sample_id}
    for part in match.group(2).split("|"):
        if "=" not in part:
            continue
        key, value = part.split("=", 1)
        fields[key.strip()] = value.strip()
    fields["project_id"] = fields.get("project") or sample_id.rsplit("_", 1)[0]
    return fields


def _finish_event(line: str) -> dict[str, str] | None:
    match = re.search(r"(?:^|\]\s*)FINISH\s+(\S+)\s+status=(\S+)", line.strip())
    if not match:
        return None
    fields = {"sample_id": match.group(1), "status": match.group(2)}
    passed_match = re.search(r"\bpassed=(true|false|1|0)\b", line, flags=re.IGNORECASE)
    if passed_match:
        fields["passed"] = "true" if passed_match.group(1).lower() in {"true", "1"} else "false"
    return fields


def _legacy_run_record(run_id: str, log_path: Path) -> dict[str, Any] | None:
    config_path = RUNTIME_CONFIG_ROOT / f"{run_id}.yaml"
    config: dict[str, Any] = {}
    if yaml is not None and config_path.is_file():
        try:
            config = yaml.safe_load(config_path.read_text(encoding="utf-8")) or {}
        except Exception:
            config = {}

    project_logs: list[dict[str, Any]] = []
    current: dict[str, Any] | None = None
    active_experiment = False
    for line in log_path.read_text(encoding="utf-8", errors="replace").splitlines():
        start = _start_event(line)
        if start:
            project_id = _safe_name(start.get("project_id", ""))
            if current is None or current.get("project_id") != project_id:
                if current is not None:
                    current["status"] = "completed"
                current = {
                    "project_id": project_id,
                    "status": "running",
                    "experiments_completed": 0,
                    "failed_experiments": 0,
                    "last_experiment_passed": None,
                    "last_experiment_status": "",
                    "sample_id": start.get("sample_id", ""),
                    "agent": start.get("agent", ""),
                    "prompt": start.get("prompt", ""),
                }
                project_logs.append(current)
            else:
                current["sample_id"] = start.get("sample_id", "")
                current["agent"] = start.get("agent", "")
                current["prompt"] = start.get("prompt", "")
            active_experiment = True
        finish = _finish_event(line)
        if finish and current is not None:
            current["experiments_completed"] = int(current.get("experiments_completed") or 0) + 1
            current["last_experiment_status"] = finish["status"]
            if "passed" in finish:
                passed = finish["passed"] == "true"
                current["last_experiment_passed"] = passed
                if not passed:
                    current["failed_experiments"] = int(current.get("failed_experiments") or 0) + 1
            active_experiment = False

    if not project_logs:
        return None
    status = "stopped" if active_experiment else "completed"
    project_logs[-1]["status"] = "stopped" if active_experiment else "completed"
    run_cfg = config.get("run", {})
    shard_id = _safe_name(str(run_cfg.get("shard_id") or ""))
    shard_name = shard_id if shard_id.endswith(".txt") else f"{shard_id}.txt"
    shard_path = _safe_shard_file(shard_name)
    input_cfg = config.get("input", {})
    request = {
        "run_scope": "shard" if shard_path else "single",
        "repo_shard": shard_path.name if shard_path else "",
        "shard_id": shard_id,
        "input_mode": input_cfg.get("mode", "sample"),
        "samples_per_project": input_cfg.get("samples_per_project", 1),
        "rerun_mode": "new",
    }
    modified = datetime.fromtimestamp(log_path.stat().st_mtime).isoformat(timespec="seconds")
    return {
        "id": run_id,
        "status": status,
        "started_at": modified,
        "finished_at": modified,
        "return_code": None,
        "command": [],
        "log_path": str(log_path),
        "config_path": str(config_path),
        "pid": None,
        "project_logs": project_logs,
        "request": request,
        "selection": {"mode": "new", "migrated_from_logs": True},
    }


def _project_log_path(run_id: str, project_id: str) -> Path:
    return LOG_ROOT / _safe_name(run_id) / f"{_safe_name(project_id)}.log"


def _safe_shard_file(value: str) -> Path | None:
    name = Path(str(value)).name
    if not name.endswith(".txt"):
        return None
    safe = _safe_name(name)
    if safe != name:
        return None
    path = (SHARD_ROOT / safe).resolve()
    try:
        if SHARD_ROOT.resolve() not in path.parents or not path.is_file():
            return None
    except OSError:
        return None
    return path


def _safe_runtime_shard_file(value: Any) -> Path | None:
    if not value:
        return None
    try:
        path = Path(str(value)).resolve()
        if RUNTIME_SHARD_ROOT.resolve() not in path.parents or not path.is_file() or path.suffix.lower() != ".txt":
            return None
    except OSError:
        return None
    return path


def _shard_project_ids(path: Path) -> list[str]:
    projects: list[str] = []
    seen: set[str] = set()
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        project_id = _safe_name(line.strip())
        if not project_id or project_id in seen:
            continue
        seen.add(project_id)
        projects.append(project_id)
    # input_selector.project_dirs sorts dataset directories, so the derived
    # shard must use that same deterministic project order for resume semantics.
    return sorted(projects)


def _failed_project_ids(run: dict[str, Any]) -> list[str]:
    failed: list[str] = []
    for project in run.get("project_logs", []):
        project_id = _safe_name(str(project.get("project_id") or ""))
        failed_count = int(project.get("failed_experiments") or 0)
        last_passed = project.get("last_experiment_passed")
        if project_id and (failed_count > 0 or last_passed is False or project.get("status") == "failed"):
            failed.append(project_id)
    return sorted(set(failed))


def _source_run(payload: dict[str, Any]) -> dict[str, Any]:
    source_run_id = _safe_name(str(payload.get("source_run_id") or ""))
    if not source_run_id:
        raise ValueError("Select a previous run for this rerun mode")
    with RUN_LOCK:
        source = RUNS.get(source_run_id)
    if not source:
        raise ValueError("Previous run was not found")
    requested_shard = Path(str(payload.get("repo_shard") or "")).name
    source_shard = Path(str(source.get("request", {}).get("repo_shard") or "")).name
    if requested_shard != source_shard:
        raise ValueError("Previous run belongs to a different shard")
    return source


def _write_runtime_shard(run_id: str, mode: str, project_ids: list[str]) -> Path:
    if not project_ids:
        raise ValueError("No projects match the selected rerun mode")
    path = RUNTIME_SHARD_ROOT / f"{_safe_name(run_id)}-{_safe_name(mode)}.txt"
    path.write_text("\n".join(project_ids) + "\n", encoding="utf-8", newline="\n")
    return path


def _write_single_project_runtime_shard(project_id: str) -> Path:
    _ensure_runtime_dirs()
    safe_project = _safe_name(project_id)
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    path = RUNTIME_SHARD_ROOT / f"shard05-project-{safe_project}-{timestamp}.txt"
    path.write_text(f"{safe_project}\n", encoding="utf-8", newline="\n")
    return path


def _start_shard05_project_run(project_id: str, payload: dict[str, Any]) -> dict[str, Any]:
    safe_project = _safe_name(project_id)
    shard_file = _safe_shard_file("repo_shard_05.txt")
    if shard_file is None or safe_project not in _shard_project_ids(shard_file):
        raise ValueError(f"Project {project_id} không nằm trong repo_shard_05")
    runtime_shard = _write_single_project_runtime_shard(safe_project)
    prepared = dict(payload)
    prepared.update(
        {
            "run_scope": "shard",
            "repo_shard": shard_file.name,
            "shard_id": "repo_shard_05",
            "start_index": 0,
            "limit": 0,
            "rerun_mode": "new",
            "_resolved_repo_shard": str(runtime_shard),
            "selection": {
                "mode": "single_project",
                "project_count": 1,
                "first_project_id": safe_project,
                "runtime_shard": str(runtime_shard),
            },
        }
    )
    return _start_run(prepared)


def _prepare_run_payload(payload: dict[str, Any], run_id: str) -> dict[str, Any]:
    prepared = dict(payload)
    mode = _safe_name(str(prepared.get("rerun_mode") or "new")) or "new"
    prepared["rerun_mode"] = mode
    if str(prepared.get("run_scope") or "single") != "shard" or mode == "new":
        return prepared

    original_shard = _safe_shard_file(str(prepared.get("repo_shard") or ""))
    if original_shard is None:
        raise ValueError("Invalid repo shard file")
    all_projects = _shard_project_ids(original_shard)
    selected_projects = all_projects
    source_run_id = ""

    if mode == "rerun_all":
        prepared["start_index"] = 0
        prepared["limit"] = 0
    elif mode == "failed_only":
        source = _source_run(prepared)
        source_run_id = str(source["id"])
        failed = set(_failed_project_ids(source))
        selected_projects = [project_id for project_id in all_projects if project_id in failed]
    elif mode in {"resume", "failed_then_resume"}:
        source = _source_run(prepared)
        source_run_id = str(source["id"])
        if source.get("status") != "stopped":
            raise ValueError("Continue mode requires a stopped previous run")
        project_logs = source.get("project_logs", [])
        if not project_logs:
            selected_projects = all_projects
        else:
            last_project = project_logs[-1]
            last_project_id = _safe_name(str(last_project.get("project_id") or ""))
            if last_project_id not in all_projects:
                raise ValueError("Stopped project is not present in the selected shard")
            start = all_projects.index(last_project_id)
            if last_project.get("status") == "completed":
                start += 1
            selected_projects = all_projects[start:]
        if mode == "failed_then_resume":
            failed = set(_failed_project_ids(source))
            failed_projects = [project_id for project_id in all_projects if project_id in failed]
            selected_projects = list(dict.fromkeys([*failed_projects, *selected_projects]))
    else:
        raise ValueError(f"Unknown rerun mode: {mode}")

    prepared["start_index"] = 0
    prepared["limit"] = 0
    prepared["_resolved_repo_shard"] = str(_write_runtime_shard(run_id, mode, selected_projects))
    prepared["selection"] = {
        "mode": mode,
        "source_run_id": source_run_id,
        "project_count": len(selected_projects),
        "first_project_id": selected_projects[0] if selected_projects else "",
    }
    return prepared


def _parse_java_homes(value: Any) -> dict[str, str]:
    if value is None:
        return {}
    if isinstance(value, dict):
        return {str(key).strip(): str(item).strip().strip('"').strip("'") for key, item in value.items() if str(key).strip() and str(item).strip()}
    if not isinstance(value, str):
        return {}
    homes: dict[str, str] = {}
    for line in value.splitlines():
        text = line.strip()
        if not text or text.startswith("#"):
            continue
        if ":" in text:
            key, item = text.split(":", 1)
        elif "=" in text:
            key, item = text.split("=", 1)
        else:
            continue
        key = key.strip()
        item = item.strip().strip('"').strip("'")
        if key and item:
            homes[key] = item
    return homes


def _write_runtime_config(payload: dict[str, Any], run_id: str) -> Path:
    if yaml is None:
        raise RuntimeError("PyYAML is required to write runtime configs")
    config = _project_config()
    adaptive = dict(config.get("adaptive_repair", {}))
    retry_mode = payload.get("retry_mode") or adaptive.get("retry_mode") or "bounded"
    adaptive["retry_mode"] = retry_mode
    numeric_int_keys = [
        "max_attempts_per_prompt",
        "max_repair_attempts",
        "max_regenerate_attempts",
        "max_total_llm_attempts",
        "no_progress_patience",
        "repeated_error_patience",
        "max_build_timeout_retries",
        "max_tool_error_retries",
    ]
    for key in numeric_int_keys:
        if key in payload and payload[key] not in {"", None}:
            adaptive[key] = int(payload[key])
    if "unlimited_max_wall_clock_minutes" in payload and payload["unlimited_max_wall_clock_minutes"] not in {"", None}:
        adaptive["unlimited_max_wall_clock_minutes"] = float(payload["unlimited_max_wall_clock_minutes"])
    config["adaptive_repair"] = adaptive
    build = dict(config.get("build", {}))
    if "java_default" in payload:
        build["java_default"] = str(payload.get("java_default") or "").strip().strip('"').strip("'")
    if "java_homes" in payload:
        build["java_homes"] = _parse_java_homes(payload.get("java_homes"))
    config["build"] = build
    input_cfg = dict(config.get("input", {}))
    if "input_mode" in payload:
        input_mode = str(payload.get("input_mode") or input_cfg.get("mode", "sample")).strip()
        input_cfg["mode"] = "project" if input_mode == "project" else "sample"
    if "samples_per_project" in payload:
        samples_per_project = str(payload.get("samples_per_project") or input_cfg.get("samples_per_project", 1)).strip()
        input_cfg["samples_per_project"] = samples_per_project if samples_per_project == "all" else int(samples_per_project or 1)
    config["input"] = input_cfg
    run_cfg = dict(config.get("run", {}))
    run_cfg["run_id"] = run_id
    if "shard_id" in payload:
        run_cfg["shard_id"] = _safe_name(str(payload.get("shard_id") or run_cfg.get("shard_id", "local"))) or "local"
    config["run"] = run_cfg
    selected_prompts = _selected_generation_prompts(payload)
    if selected_prompts:
        available_prompts = {
            str(item.get("name", ""))
            for item in config.get("prompts", {}).get("generation_strategies", [])
        }
        unknown = [name for name in selected_prompts if name not in available_prompts]
        if unknown:
            raise ValueError(f"Unknown generation prompts: {', '.join(unknown)}")
        experiment_cfg = dict(config.get("experiment", {}))
        experiment_cfg["run_all_generation_prompts"] = False
        experiment_cfg["selected_generation_prompts"] = selected_prompts
        config["experiment"] = experiment_cfg
    selected_agents = _selected_agents(payload)
    if selected_agents:
        available_agents = {str(item.get("name", "")) for item in config.get("llm", {}).get("agents", [])}
        unknown_agents = [name for name in selected_agents if name not in available_agents]
        if unknown_agents:
            raise ValueError(f"Unknown models: {', '.join(unknown_agents)}")
        experiment_cfg = dict(config.get("experiment", {}))
        experiment_cfg["run_all_agents"] = False
        experiment_cfg["selected_agents"] = selected_agents
        config["experiment"] = experiment_cfg
    path = RUNTIME_CONFIG_ROOT / f"{run_id}.yaml"
    path.write_text(yaml.safe_dump(config, allow_unicode=True, sort_keys=False), encoding="utf-8")
    return path


def _build_pipeline_command(payload: dict[str, Any], config_path: Path) -> list[str]:
    command = [sys.executable, "-m", "src.run_pipeline", "--config", str(config_path)]
    run_scope = str(payload.get("run_scope") or "single")
    project_id = _safe_name(str(payload.get("project_id", "")))
    sample_file = Path(str(payload.get("sample_file", ""))).name
    agents = _selected_agents(payload)
    prompts = _selected_generation_prompts(payload)
    if run_scope == "shard":
        shard_file = _safe_runtime_shard_file(payload.get("_resolved_repo_shard"))
        if shard_file is None:
            shard_file = _safe_shard_file(str(payload.get("repo_shard", "")))
        if shard_file is None:
            raise ValueError("Invalid repo shard file")
        command.extend(["--repo-shard", str(shard_file)])
        shard_id = _safe_name(str(payload.get("shard_id") or shard_file.stem))
        if shard_id:
            command.extend(["--shard-id", shard_id])
        command.extend(["--start-index", str(int(payload.get("start_index") or 0))])
        command.extend(["--limit", str(int(payload.get("limit") or 0))])
    elif project_id:
        command.extend(["--project-id", project_id])
    if run_scope != "shard" and sample_file and sample_file.endswith(".json"):
        command.extend(["--sample-file", sample_file])
    for agent in agents:
        command.extend(["--agent", agent])
    for prompt in prompts:
        command.extend(["--generation-prompt", prompt])
    if payload.get("skip_metrics"):
        command.append("--skip-metrics")
    if payload.get("keep_workspace"):
        command.append("--keep-workspace")
    if payload.get("keep_repo_cache"):
        command.append("--keep-repo-cache")
    if payload.get("mock_llm_smoke"):
        command.append("--mock-llm-smoke")
    java_home = str(payload.get("java_home", "")).strip()
    if java_home:
        command.extend(["--java-home", java_home])
    return command


def _stop_process_tree(pid: int) -> None:
    if os.name == "nt":
        subprocess.run(["taskkill", "/F", "/T", "/PID", str(pid)], capture_output=True, text=True)
    else:
        try:
            os.killpg(pid, 15)
        except Exception:
            subprocess.run(["kill", "-TERM", str(pid)], capture_output=True, text=True)


def _watch_process(run_id: str, process: subprocess.Popen[str], log_path: Path) -> None:
    current_project_id = ""
    project_log = None
    with log_path.open("a", encoding="utf-8", errors="replace") as log:
        assert process.stdout is not None
        try:
            for line in process.stdout:
                start = _start_event(line)
                if start:
                    project_id = _safe_name(start["project_id"])
                    if project_id and project_id != current_project_id:
                        if project_log is not None:
                            project_log.close()
                        with RUN_LOCK:
                            run = RUNS.get(run_id)
                            if run:
                                for item in run.get("project_logs", []):
                                    if item.get("project_id") == current_project_id and item.get("status") == "running":
                                        item["status"] = "completed"
                                entry = next((item for item in run.get("project_logs", []) if item.get("project_id") == project_id), None)
                                if entry is None:
                                    entry = {
                                        "project_id": project_id,
                                        "status": "running",
                                        "experiments_completed": 0,
                                        "failed_experiments": 0,
                                        "last_experiment_passed": None,
                                        "last_experiment_status": "",
                                    }
                                    run.setdefault("project_logs", []).append(entry)
                                else:
                                    entry["status"] = "running"
                                entry["sample_id"] = start.get("sample_id", "")
                                entry["agent"] = start.get("agent", "")
                                entry["prompt"] = start.get("prompt", "")
                                _persist_run(run)
                        current_project_id = project_id
                        project_path = _project_log_path(run_id, project_id)
                        project_path.parent.mkdir(parents=True, exist_ok=True)
                        project_log = project_path.open("a", encoding="utf-8", errors="replace")
                log.write(line)
                log.flush()
                if project_log is not None:
                    project_log.write(line)
                    project_log.flush()
                finish = _finish_event(line)
                if finish and current_project_id:
                    with RUN_LOCK:
                        run = RUNS.get(run_id)
                        if run:
                            entry = next((item for item in run.get("project_logs", []) if item.get("project_id") == current_project_id), None)
                            if entry:
                                entry["experiments_completed"] = int(entry.get("experiments_completed") or 0) + 1
                                entry["last_experiment_status"] = finish["status"]
                                if "passed" in finish:
                                    passed = finish["passed"] == "true"
                                    entry["last_experiment_passed"] = passed
                                    if not passed:
                                        entry["failed_experiments"] = int(entry.get("failed_experiments") or 0) + 1
                                _persist_run(run)
        finally:
            if project_log is not None:
                project_log.close()
    return_code = process.wait()
    with RUN_LOCK:
        run = RUNS.get(run_id)
        if run:
            run["return_code"] = return_code
            run["finished_at"] = datetime.now().isoformat(timespec="seconds")
            if run.get("status") != "stopped":
                run["status"] = "completed" if return_code == 0 else "failed"
            final_project_status = "stopped" if run.get("status") == "stopped" else "completed" if return_code == 0 else "failed"
            for item in run.get("project_logs", []):
                if item.get("status") == "running":
                    item["status"] = final_project_status
            _persist_run(run)


def _start_run(payload: dict[str, Any]) -> dict[str, Any]:
    _ensure_runtime_dirs()
    run_id = datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    prepared_payload = _prepare_run_payload(payload, run_id)
    config_path = _write_runtime_config(prepared_payload, run_id)
    command = _build_pipeline_command(prepared_payload, config_path)
    log_path = LOG_ROOT / f"{run_id}.log"
    env = os.environ.copy()
    env["PYTHONIOENCODING"] = "utf-8"
    creationflags = (subprocess.CREATE_NO_WINDOW | subprocess.CREATE_NEW_PROCESS_GROUP) if os.name == "nt" else 0
    process = subprocess.Popen(
        command,
        cwd=PROJECT_ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
        env=env,
        creationflags=creationflags,
        start_new_session=os.name != "nt",
    )
    run = {
        "id": run_id,
        "status": "running",
        "started_at": datetime.now().isoformat(timespec="seconds"),
        "finished_at": "",
        "return_code": None,
        "command": command,
        "log_path": str(log_path),
        "config_path": str(config_path),
        "pid": process.pid,
        "project_logs": [],
        "request": {key: value for key, value in prepared_payload.items() if not key.startswith("_")},
        "selection": prepared_payload.get("selection", {"mode": prepared_payload.get("rerun_mode", "new")}),
    }
    with RUN_LOCK:
        RUNS[run_id] = run
        _persist_run(run)
    threading.Thread(target=_watch_process, args=(run_id, process, log_path), daemon=True).start()
    return run


class DashboardHandler(SimpleHTTPRequestHandler):
    def log_message(self, format: str, *args: object) -> None:
        return

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        path = parsed.path
        try:
            if path == "/api/config":
                return _json_response(self, self._api_config())
            if path == "/api/projects":
                return _json_response(self, self._api_projects(parsed.query))
            if path.startswith("/api/projects/") and path.endswith("/samples"):
                project_id = unquote(path.split("/")[3])
                return _json_response(self, self._api_samples(project_id))
            if path == "/api/shards":
                return _json_response(self, self._api_shards())
            if path == "/api/shards/shard05/status":
                try:
                    return _json_response(self, _shard_status("repo_shard_05"))
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=404)
            if path.startswith("/api/shards/shard05/projects/") and path.endswith("/errors"):
                parts = path.strip("/").split("/")
                project_id = unquote(parts[4]) if len(parts) >= 6 else ""
                prompt_strategy = parse_qs(parsed.query).get("prompt", [""])[0]
                try:
                    return _json_response(self, _shard_project_errors(project_id, "repo_shard_05", prompt_strategy))
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=404)
            if path == "/api/experiments":
                return _json_response(self, {"experiments": _experiments()})
            if path.startswith("/api/experiments/"):
                return self._api_experiment_route(path)
            if path == "/api/reports/rq1/preview":
                try:
                    return _json_response(self, _rq1_preview(parsed.query))
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=400)
                except RQ1SnapshotChangedError as exc:
                    return _json_response(self, {"error": str(exc)}, status=409)
            if path == "/api/runs":
                with RUN_LOCK:
                    return _json_response(self, {"runs": list(RUNS.values())})
            if path.startswith("/api/runs/"):
                return self._api_run_route(path, parsed.query)
            return self._serve_static(path)
        except DISCONNECTED_ERRORS:
            return
        except Exception as exc:
            return _json_response(self, {"error": str(exc)}, status=500)

    def do_POST(self) -> None:
        parsed = urlparse(self.path)
        try:
            if parsed.path == "/api/reports/merge":
                return _json_response(self, _merge_reports_now(), status=201)
            if parsed.path == "/api/reports/export/rq1":
                length = int(self.headers.get("Content-Length", "0"))
                payload = json.loads(self.rfile.read(length).decode("utf-8") or "{}")
                try:
                    baseline_valid_only = payload.get("baseline_valid_only", True)
                    if not isinstance(baseline_valid_only, bool):
                        raise ValueError("baseline_valid_only phải là boolean")
                    result = _save_rq1_workbook(
                        str(payload.get("preview_revision") or ""),
                        baseline_valid_only=baseline_valid_only,
                    )
                    return _json_response(self, result, status=201)
                except RQ1ExcelLimitError as exc:
                    return _json_response(
                        self,
                        {
                            "error": str(exc),
                            "sheet": exc.sheet,
                            "rows": exc.rows,
                            "max_rows": exc.max_rows,
                        },
                        status=422,
                    )
                except RQ1NoDataError as exc:
                    return _json_response(self, {"error": str(exc)}, status=422)
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=422)
                except RQ1SnapshotChangedError as exc:
                    return _json_response(self, {"error": str(exc)}, status=409)
                except RuntimeError as exc:
                    if str(exc) == "Đang có yêu cầu xuất RQ1 khác chạy":
                        return _json_response(self, {"error": str(exc)}, status=409)
                    raise
            if parsed.path == "/api/reports/export/shard05":
                try:
                    return _json_response(self, _save_shard_export("repo_shard_05"), status=201)
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=422)
            if parsed.path == "/api/reports/export/rq2":
                length = int(self.headers.get("Content-Length", "0"))
                payload = json.loads(self.rfile.read(length).decode("utf-8") or "{}")
                try:
                    only_generated_failures = payload.get("only_generated_failures", True)
                    if not isinstance(only_generated_failures, bool):
                        raise ValueError("only_generated_failures phải là boolean")
                    return _json_response(
                        self,
                        _save_rq2_export("repo_shard_05", only_generated_failures=only_generated_failures),
                        status=201,
                    )
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=422)
            if parsed.path.startswith("/api/shards/shard05/projects/") and parsed.path.endswith("/rerun"):
                parts = parsed.path.strip("/").split("/")
                project_id = unquote(parts[4]) if len(parts) >= 6 else ""
                length = int(self.headers.get("Content-Length", "0"))
                payload = json.loads(self.rfile.read(length).decode("utf-8") or "{}")
                try:
                    return _json_response(self, _start_shard05_project_run(project_id, payload), status=201)
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=422)
            if parsed.path.startswith("/api/experiments/") and parsed.path.endswith("/recompute-metrics"):
                parts = parsed.path.strip("/").split("/")
                if len(parts) != 4 or not parts[2]:
                    return _json_response(self, {"error": "Experiment not found"}, status=404)
                if not METRICS_RERUN_LOCK.acquire(blocking=False):
                    return _json_response(self, {"error": "Đang có một lượt đo lại metrics khác chạy"}, status=409)
                try:
                    result_path = _decode_result_path(parts[2])
                    result = recompute_metrics(
                        result_path=result_path,
                        project_root=PROJECT_ROOT,
                        config=_project_config(),
                    )
                    return _json_response(self, result, status=201)
                except ValueError as exc:
                    return _json_response(self, {"error": str(exc)}, status=422)
                finally:
                    METRICS_RERUN_LOCK.release()
            if parsed.path.startswith("/api/reports/export/"):
                export_kind = parsed.path.removeprefix("/api/reports/export/").strip("/")
                if export_kind not in RQ1_EXPORT_FILENAMES:
                    return _json_response(self, {"error": "Unknown CSV export type"}, status=404)
                return _json_response(self, _save_rq1_export(export_kind), status=201)
            if parsed.path == "/api/runs":
                length = int(self.headers.get("Content-Length", "0"))
                payload = json.loads(self.rfile.read(length).decode("utf-8") or "{}")
                run = _start_run(payload)
                return _json_response(self, run, status=201)
            if parsed.path.startswith("/api/runs/") and parsed.path.endswith("/stop"):
                run_id = _safe_name(parsed.path.strip("/").split("/")[2])
                return _json_response(self, self._api_stop_run(run_id))
            return _json_response(self, {"error": "Not found"}, status=404)
        except DISCONNECTED_ERRORS:
            return
        except Exception as exc:
            return _json_response(self, {"error": str(exc)}, status=500)

    def _serve_static(self, path: str) -> None:
        if path in {"", "/"}:
            target = STATIC_ROOT / "index.html"
        else:
            target = (STATIC_ROOT / path.lstrip("/")).resolve()
            if STATIC_ROOT.resolve() not in target.parents and target != STATIC_ROOT.resolve():
                return _json_response(self, {"error": "Invalid static path"}, status=404)
        if not target.is_file():
            return _json_response(self, {"error": "Not found"}, status=404)
        content_type = "text/html; charset=utf-8"
        if target.suffix == ".css":
            content_type = "text/css; charset=utf-8"
        elif target.suffix == ".js":
            content_type = "application/javascript; charset=utf-8"
        return _text_response(self, target.read_text(encoding="utf-8"), content_type=content_type)

    def _api_config(self) -> dict[str, Any]:
        config = _project_config()
        build = dict(config.get("build", {}))
        build["java_default"] = platform_config_value(build.get("java_default"))
        java_homes = build.get("java_homes", {})
        if isinstance(java_homes, dict):
            build["java_homes"] = {
                str(version): platform_config_value(value)
                for version, value in java_homes.items()
                if platform_config_value(value)
            }
        return {
            "agents": config.get("llm", {}).get("agents", []),
            "generation_prompts": config.get("prompts", {}).get("generation_strategies", []),
            "adaptive_repair": config.get("adaptive_repair", {}),
            "build": build,
            "input": config.get("input", {}),
            "run": config.get("run", {}),
            "experiment": config.get("experiment", {}),
            "java": _detect_java(),
            "dataset_root": str(DATASET_ROOT),
            "runs_root": str(PROJECT_ROOT / "runs"),
        }

    def _api_projects(self, query: str) -> dict[str, Any]:
        params = parse_qs(query)
        limit_text = params.get("limit", [""])[0]
        limit = int(limit_text) if limit_text else 0
        projects = []
        if DATASET_ROOT.is_dir():
            for item in sorted(DATASET_ROOT.iterdir(), key=lambda path: path.name):
                if item.is_dir():
                    projects.append({"project_id": item.name, "sample_count": len(list(item.glob("*.json")))})
                    if limit and len(projects) >= limit:
                        break
        return {"projects": projects, "project_count": len(projects), "limited": bool(limit)}

    def _api_samples(self, project_id: str) -> dict[str, Any]:
        safe_project = _safe_name(project_id)
        project_dir = DATASET_ROOT / safe_project
        samples = []
        if project_dir.is_dir():
            samples = [path.name for path in sorted(project_dir.glob("*.json"), key=lambda item: item.name)]
        return {"project_id": safe_project, "samples": samples}

    def _api_shards(self) -> dict[str, Any]:
        shards = []
        if SHARD_ROOT.is_dir():
            for path in sorted(SHARD_ROOT.glob("*.txt"), key=lambda item: item.name):
                lines = [line.strip() for line in path.read_text(encoding="utf-8", errors="replace").splitlines() if line.strip()]
                shards.append({"name": path.name, "repo_count": len(lines), "path": str(path)})
        return {"shards": shards, "shard_count": len(shards), "shards_root": str(SHARD_ROOT)}

    def _api_experiment_route(self, path: str) -> None:
        parts = path.strip("/").split("/")
        if len(parts) < 3 or not parts[2]:
            return _json_response(self, {"error": "Experiment not found"}, status=404)
        token = parts[2]
        result_path = _decode_result_path(token)
        row = _load_experiment(result_path)
        experiment_dir = Path(str(row["experiment_dir"]))
        if len(parts) == 3:
            return _json_response(
                self,
                {
                    "experiment": row,
                    "repair_summary": _read_json(experiment_dir / "repair_summary.json"),
                    "checkpoints": _checkpoint_summaries(experiment_dir),
                    "error_files": _error_artifact_summaries(experiment_dir),
                },
            )
        if len(parts) == 4 and parts[3] == "errors":
            return _json_response(
                self,
                {
                    "error_files": _error_artifact_summaries(experiment_dir),
                    "content": _combined_error_artifacts(experiment_dir),
                },
            )
        if len(parts) == 5 and parts[3] == "errors":
            artifact = _decode_artifact_path(experiment_dir, parts[4])
            return _json_response(
                self,
                {
                    "id": parts[4],
                    "relative_path": artifact.relative_to(experiment_dir).as_posix(),
                    "content": _error_artifact_content(artifact),
                },
            )
        if len(parts) == 5 and parts[3] == "checkpoints":
            attempt = _safe_name(parts[4])
            checkpoint_dir = experiment_dir / "repair" / "checkpoints" / attempt
            return _json_response(
                self,
                {
                    "attempt": attempt,
                    "decision": _read_json(checkpoint_dir / "decision.json"),
                    "repair_prompt": _read_text(checkpoint_dir / "repair_prompt.txt"),
                    "llm_response": _read_text(checkpoint_dir / "llm_response.txt"),
                    "build_output_before": _read_text(checkpoint_dir / "build_output_before.txt"),
                    "build_output_after": _read_text(checkpoint_dir / "build_output_after.txt"),
                    "generated_test_before": _read_text(checkpoint_dir / "generated_test_before.java"),
                    "generated_test_after": _read_text(checkpoint_dir / "generated_test_after.java"),
                },
            )
        return _json_response(self, {"error": "Not found"}, status=404)

    def _api_run_route(self, path: str, query: str = "") -> None:
        parts = path.strip("/").split("/")
        run_id = _safe_name(parts[2]) if len(parts) > 2 else ""
        with RUN_LOCK:
            run = RUNS.get(run_id)
        if not run:
            return _json_response(self, {"error": "Run not found"}, status=404)
        if len(parts) == 4 and parts[3] == "logs":
            params = parse_qs(query)
            project_id = _safe_name(params.get("project_id", [""])[0])
            if project_id:
                known = any(item.get("project_id") == project_id for item in run.get("project_logs", []))
                if not known:
                    return _json_response(self, {"error": "Project log not found"}, status=404)
                log_path = _project_log_path(run_id, project_id)
            else:
                log_path = Path(str(run["log_path"]))
            return _json_response(self, {"id": run_id, "project_id": project_id, "logs": _tail(log_path)})
        return _json_response(self, run)

    def _api_stop_run(self, run_id: str) -> dict[str, Any]:
        with RUN_LOCK:
            run = RUNS.get(run_id)
            if not run:
                return {"error": "Run not found"}
            if run.get("status") != "running":
                return run
            run["status"] = "stopping"
            pid = int(run.get("pid") or 0)
        if pid:
            _stop_process_tree(pid)
        with RUN_LOCK:
            run = RUNS.get(run_id, run)
            run["status"] = "stopped"
            run["finished_at"] = datetime.now().isoformat(timespec="seconds")
            run["return_code"] = run.get("return_code")
            for project in run.get("project_logs", []):
                if project.get("status") in {"running", "stopping"}:
                    project["status"] = "stopped"
            _persist_run(run)
            return run


def main() -> None:
    parser = argparse.ArgumentParser(description="Run the ARROW dashboard.")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    args = parser.parse_args()
    _ensure_runtime_dirs()
    _load_run_records()
    server = ThreadingHTTPServer((args.host, args.port), DashboardHandler)
    print(f"Dashboard running at http://{args.host}:{args.port}")
    server.serve_forever()


if __name__ == "__main__":
    main()
