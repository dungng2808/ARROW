from __future__ import annotations

import csv
import json
from pathlib import Path
from typing import Any

from .fs_utils import atomic_write_json, ensure_dir


PAPER_REPORT_COLUMNS = [
    "Generator(LLM)",
    "Prompt_Technique",
    "Compilation",
    "Project_ID",
    "Class_Under_Test",
    "Branch_Coverage%",
    "Line_Coverage%",
    "Method_Coverage%",
    "Mutation_Score%",
    "NumberOfMethods",
    "Assertion Roulette",
    "Conditional Test Logic",
    "Constructor Initialization",
    "Default Test",
    "EmptyTest",
    "Exception Handling",
    "General Fixture",
    "Mystery Guest",
    "Print Statement",
    "Redundant Assertion",
    "Sensitive Equality",
    "Verbose Test",
    "Sleepy Test",
    "Eager Test",
    "Lazy Test",
    "Duplicate Assert",
    "Unknown Test",
    "IgnoredTest",
    "Resource Optimism",
    "Magic Number Test",
    "Dependent Test",
]


CLASS_REPORT_COLUMNS = [
    *PAPER_REPORT_COLUMNS,
    "run_id",
    "shard_id",
    "input_id",
    "sample_id",
    "project_id",
    "sample_file",
    "repository_url",
    "repo_name",
    "repo_owner",
    "repo_folder",
    "output_layout",
    "experiment_dir",
    "reports_dir",
    "agent_name",
    "model",
    "generation_prompt_strategy",
    "initial_failure_state",
    "final_failure_state",
    "best_candidate_state",
    "initial_failure_origin",
    "final_failure_origin",
    "baseline_state",
    "baseline_error_signatures",
    "repair_attempts",
    "regeneration_attempts",
    "total_llm_attempts",
    "build_attempts",
    "repair_status",
    "initial_repair_prompt_strategy",
    "final_repair_prompt_strategy",
    "prompt_switch_count",
    "rollback_count",
    "repeated_error_signature",
    "repeated_code_detected",
    "no_progress_count",
    "repair_stopped_reason",
    "regenerated_after_repair_fail",
    "target_test_passed",
    "module_tests_passed",
    "started_at",
    "finished_at",
    "target_passed_at",
    "target_pass_elapsed_seconds",
    "module_passed_at",
    "module_pass_elapsed_seconds",
    "first_passed_at",
    "first_pass_elapsed_seconds",
    "new_module_failures",
    "existing_baseline_failures",
    "best_candidate_hash",
    "experiment_workspace",
    "workspace_deleted",
    "repo_cache_path",
    "repo_cache_deleted",
    "checkpoint_directory",
    "build_tool",
    "module_path",
    "focal_class",
    "focal_class_path",
    "generated_test_path",
    "compilation",
    "test_passed",
    "test_fail_reason",
    "compile_errors",
    "test_failures",
    "test_errors",
    "coverage_branch",
    "coverage_line",
    "coverage_method",
    "mutation_score",
    "mutations_total",
    "mutations_killed",
    "mutations_survived",
    "test_smell_total",
    "test_smell_details",
    "elapsed_seconds",
    "error",
]


SMELL_COLUMNS = PAPER_REPORT_COLUMNS[10:]


def ensure_paper_report_fields(row: dict[str, Any]) -> dict[str, Any]:
    normalized = row.copy()
    normalized["Generator(LLM)"] = normalized.get("Generator(LLM)") or normalized.get("agent_name") or normalized.get("model", "")
    normalized["Prompt_Technique"] = normalized.get("Prompt_Technique") or normalized.get("generation_prompt_strategy", "")
    normalized["Compilation"] = normalized.get("Compilation")
    if normalized["Compilation"] in {None, ""}:
        normalized["Compilation"] = normalized.get("compilation", "")
    normalized["Project_ID"] = normalized.get("Project_ID") or normalized.get("project_id", "")
    normalized["Class_Under_Test"] = normalized.get("Class_Under_Test") or normalized.get("focal_class", "")
    normalized["Branch_Coverage%"] = normalized.get("Branch_Coverage%") or normalized.get("coverage_branch", "")
    normalized["Line_Coverage%"] = normalized.get("Line_Coverage%") or normalized.get("coverage_line", "")
    normalized["Method_Coverage%"] = normalized.get("Method_Coverage%") or normalized.get("coverage_method", "")
    normalized["Mutation_Score%"] = normalized.get("Mutation_Score%") or normalized.get("mutation_score", "")
    normalized["NumberOfMethods"] = normalized.get("NumberOfMethods", "")
    for smell in SMELL_COLUMNS:
        normalized.setdefault(smell, "")
    return normalized


def write_class_report(path: Path, rows: list[dict[str, Any]]) -> None:
    ensure_dir(path.parent)
    rows = [ensure_paper_report_fields(row) for row in rows]
    columns = CLASS_REPORT_COLUMNS.copy()
    for row in rows:
        for key in row:
            if key not in columns:
                columns.append(key)
    with path.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=columns, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def write_run_summary(path: Path, data: dict[str, Any]) -> None:
    atomic_write_json(path, data)


def write_experiment_json(path: Path, row: dict[str, Any]) -> None:
    atomic_write_json(path, ensure_paper_report_fields(row))


def append_experiment_jsonl(path: Path, row: dict[str, Any]) -> None:
    ensure_dir(path.parent)
    with path.open("a", encoding="utf-8", newline="\n") as output_file:
        output_file.write(json.dumps(ensure_paper_report_fields(row), ensure_ascii=False, default=str) + "\n")


def load_experiment_jsonl(paths: list[Path]) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for path in paths:
        if not path.is_file():
            continue
        with path.open("r", encoding="utf-8") as input_file:
            for line_number, line in enumerate(input_file, start=1):
                stripped = line.strip()
                if not stripped:
                    continue
                try:
                    rows.append(json.loads(stripped))
                except json.JSONDecodeError as exc:
                    raise ValueError(f"Invalid JSONL at {path}:{line_number}: {exc}") from exc
    return rows


def merge_rows(rows: list[dict[str, Any]]) -> tuple[list[dict[str, Any]], int]:
    merged: dict[tuple[str, str, str, str, str], dict[str, Any]] = {}
    duplicates = 0
    for row in rows:
        key = (
            str(row.get("run_id", "")),
            str(row.get("shard_id", "")),
            str(row.get("input_id", "")),
            str(row.get("agent_name", "")),
            str(row.get("generation_prompt_strategy", "")),
        )
        if key in merged:
            duplicates += 1
        merged[key] = row
    return list(merged.values()), duplicates


def write_merged_jsonl(path: Path, rows: list[dict[str, Any]]) -> None:
    ensure_dir(path.parent)
    with path.open("w", encoding="utf-8", newline="\n") as output_file:
        for row in rows:
            output_file.write(json.dumps(ensure_paper_report_fields(row), ensure_ascii=False, default=str) + "\n")


def write_mean_report(path: Path, rows: list[dict[str, Any]]) -> None:
    groups: dict[tuple[str, str, str, str], list[dict[str, Any]]] = {}
    for row in rows:
        key = (
            str(row.get("agent_name", "")),
            str(row.get("model", "")),
            str(row.get("generation_prompt_strategy", "")),
            str(row.get("build_tool", "")),
        )
        groups.setdefault(key, []).append(row)
    output_rows = []
    for (agent, model, prompt, build_tool), items in groups.items():
        total = len(items)
        compiled = sum(1 for item in items if str(item.get("compilation")).lower() in {"true", "1"})
        passed = sum(1 for item in items if str(item.get("test_passed")).lower() in {"true", "1"})
        avg_elapsed = _average(items, "elapsed_seconds")
        avg_target_pass = _average(items, "target_pass_elapsed_seconds")
        avg_module_pass = _average(items, "module_pass_elapsed_seconds")
        avg_first_pass = _average(items, "first_pass_elapsed_seconds")
        output_rows.append(
            {
                "agent_name": agent,
                "model": model,
                "generation_prompt_strategy": prompt,
                "build_tool": build_tool,
                "total_inputs": total,
                "compiled_count": compiled,
                "test_passed_count": passed,
                "compilation_rate": compiled / total if total else 0,
                "test_pass_rate": passed / total if total else 0,
                "avg_elapsed_seconds": avg_elapsed,
                "avg_target_pass_elapsed_seconds": avg_target_pass,
                "avg_module_pass_elapsed_seconds": avg_module_pass,
                "avg_first_pass_elapsed_seconds": avg_first_pass,
            }
        )
    ensure_dir(path.parent)
    fieldnames = [
        "agent_name",
        "model",
        "generation_prompt_strategy",
        "build_tool",
        "total_inputs",
        "compiled_count",
        "test_passed_count",
        "compilation_rate",
        "test_pass_rate",
        "avg_elapsed_seconds",
        "avg_target_pass_elapsed_seconds",
        "avg_module_pass_elapsed_seconds",
        "avg_first_pass_elapsed_seconds",
    ]
    with path.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(output_rows)


def experiment_statistics(rows: list[dict[str, Any]]) -> dict[str, Any]:
    groups: dict[tuple[str, str, str, str], list[dict[str, Any]]] = {}
    for row in rows:
        key = (
            str(row.get("agent_name", "")),
            str(row.get("model", "")),
            str(row.get("generation_prompt_strategy", "")),
            str(row.get("build_tool", "")),
        )
        groups.setdefault(key, []).append(row)
    return {
        "overall": _statistics_for_rows(rows),
        "by_agent_prompt_build": [
            {
                "agent_name": agent,
                "model": model,
                "generation_prompt_strategy": prompt,
                "build_tool": build_tool,
                **_statistics_for_rows(items),
            }
            for (agent, model, prompt, build_tool), items in groups.items()
        ],
    }


def write_experiment_statistics(path: Path, rows: list[dict[str, Any]]) -> None:
    atomic_write_json(path, experiment_statistics(rows))


def write_experiment_statistics_csv(path: Path, rows: list[dict[str, Any]]) -> None:
    stats = experiment_statistics(rows)["by_agent_prompt_build"]
    fieldnames = [
        "agent_name",
        "model",
        "generation_prompt_strategy",
        "build_tool",
        "total_experiments",
        "unique_projects",
        "unique_samples",
        "compilation_success_count",
        "compilation_success_rate",
        "target_pass_count",
        "target_pass_rate",
        "module_pass_count",
        "module_pass_rate",
        "initial_failure_count",
        "repair_attempted_count",
        "repair_success_count",
        "repair_success_rate",
        "avg_repair_attempts",
        "avg_total_llm_attempts",
        "avg_elapsed_seconds",
        "avg_line_coverage",
        "avg_branch_coverage",
        "avg_method_coverage",
        "avg_mutation_score",
        "avg_test_smell_total",
    ]
    ensure_dir(path.parent)
    with path.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fieldnames, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(stats)


def repair_summary_to_report_row(summary: dict[str, Any], base_row: dict[str, Any]) -> dict[str, Any]:
    row = base_row.copy()
    row.update(summary)
    row["target_test_passed"] = row.get("final_failure_state") in {"TARGET_TEST_PASSED", "MODULE_TESTS_PASSED"}
    row["module_tests_passed"] = row.get("final_failure_state") == "MODULE_TESTS_PASSED"
    row["test_passed"] = row["module_tests_passed"]
    row["compilation"] = row.get("final_failure_state") not in {"COMPILE_FAILED", "TEST_DISCOVERY_FAILED"}
    return row


def _average(rows: list[dict[str, Any]], key: str) -> float | str:
    values = []
    for row in rows:
        value = row.get(key)
        if value in {"", None}:
            continue
        try:
            values.append(float(value))
        except (TypeError, ValueError):
            continue
    if not values:
        return ""
    return sum(values) / len(values)


def _statistics_for_rows(rows: list[dict[str, Any]]) -> dict[str, Any]:
    total = len(rows)
    compiled = [row for row in rows if _truthy(row.get("compilation")) or _truthy(row.get("Compilation"))]
    target_passed = [row for row in rows if _truthy(row.get("target_test_passed")) or _state_passed(row.get("final_failure_state"))]
    module_passed = [row for row in rows if _truthy(row.get("module_tests_passed")) or _truthy(row.get("test_passed")) or row.get("final_failure_state") == "MODULE_TESTS_PASSED"]
    initial_failures = [row for row in rows if _state_failed(row.get("initial_failure_state"))]
    repair_attempted = [row for row in initial_failures if _int_value(row.get("repair_attempts")) > 0]
    repair_success = [row for row in repair_attempted if row.get("final_failure_state") == "MODULE_TESTS_PASSED" or _truthy(row.get("module_tests_passed")) or _truthy(row.get("test_passed"))]
    return {
        "total_experiments": total,
        "unique_projects": _unique_count(rows, ("project_id", "Project_ID")),
        "unique_samples": _unique_count(rows, ("sample_id", "input_id")),
        "unique_repositories": _unique_count(rows, ("repository_url", "project_id", "Project_ID")),
        "unique_classes": _unique_pair_count(rows, ("project_id", "Project_ID"), ("focal_class", "Class_Under_Test")),
        "compilation_success_count": len(compiled),
        "compilation_success_rate": _rate(len(compiled), total),
        "target_pass_count": len(target_passed),
        "target_pass_rate": _rate(len(target_passed), total),
        "module_pass_count": len(module_passed),
        "module_pass_rate": _rate(len(module_passed), total),
        "initial_failure_count": len(initial_failures),
        "repair_attempted_count": len(repair_attempted),
        "repair_attempt_rate": _rate(len(repair_attempted), len(initial_failures)),
        "repair_success_count": len(repair_success),
        "repair_success_rate": _rate(len(repair_success), len(repair_attempted)),
        "avg_repair_attempts": _average_multi(repair_attempted, ("repair_attempts",)),
        "avg_total_llm_attempts": _average_multi(repair_attempted, ("total_llm_attempts",)),
        "avg_elapsed_seconds": _average_multi(rows, ("elapsed_seconds",)),
        "avg_target_pass_elapsed_seconds": _average_multi(module_passed, ("target_pass_elapsed_seconds",)),
        "avg_module_pass_elapsed_seconds": _average_multi(module_passed, ("module_pass_elapsed_seconds",)),
        "avg_line_coverage": _average_multi(rows, ("coverage_line", "Line_Coverage%")),
        "line_coverage_count": _numeric_count(rows, ("coverage_line", "Line_Coverage%")),
        "avg_branch_coverage": _average_multi(rows, ("coverage_branch", "Branch_Coverage%")),
        "branch_coverage_count": _numeric_count(rows, ("coverage_branch", "Branch_Coverage%")),
        "avg_method_coverage": _average_multi(rows, ("coverage_method", "Method_Coverage%")),
        "method_coverage_count": _numeric_count(rows, ("coverage_method", "Method_Coverage%")),
        "avg_mutation_score": _average_multi(rows, ("mutation_score", "Mutation_Score%")),
        "mutation_score_count": _numeric_count(rows, ("mutation_score", "Mutation_Score%")),
        "avg_mutations_total": _average_multi(rows, ("mutations_total",)),
        "avg_mutations_killed": _average_multi(rows, ("mutations_killed",)),
        "avg_test_smell_total": _average_multi(rows, ("test_smell_total",)),
        "test_smell_count": _numeric_count(rows, ("test_smell_total",)),
        "repair_status_counts": _value_counts(rows, "repair_status"),
        "final_failure_state_counts": _value_counts(rows, "final_failure_state"),
        "build_tool_counts": _value_counts(rows, "build_tool"),
    }


def _truthy(value: Any) -> bool:
    if isinstance(value, bool):
        return value
    return str(value).strip().lower() in {"true", "1", "yes"}


def _state_passed(value: Any) -> bool:
    return str(value or "") in {"TARGET_TEST_PASSED", "MODULE_TESTS_PASSED"}


def _state_failed(value: Any) -> bool:
    text = str(value or "")
    return bool(text) and text not in {"TARGET_TEST_PASSED", "MODULE_TESTS_PASSED"}


def _rate(numerator: int, denominator: int) -> float | str:
    if not denominator:
        return ""
    return numerator / denominator


def _int_value(value: Any) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return 0


def _float_value(row: dict[str, Any], keys: tuple[str, ...]) -> float | None:
    for key in keys:
        value = row.get(key)
        if value in {"", None}:
            continue
        try:
            return float(value)
        except (TypeError, ValueError):
            continue
    return None


def _average_multi(rows: list[dict[str, Any]], keys: tuple[str, ...]) -> float | str:
    values = [_float_value(row, keys) for row in rows]
    numeric = [value for value in values if value is not None]
    if not numeric:
        return ""
    return sum(numeric) / len(numeric)


def _numeric_count(rows: list[dict[str, Any]], keys: tuple[str, ...]) -> int:
    return sum(1 for row in rows if _float_value(row, keys) is not None)


def _unique_count(rows: list[dict[str, Any]], keys: tuple[str, ...]) -> int:
    values = set()
    for row in rows:
        for key in keys:
            value = row.get(key)
            if value not in {"", None}:
                values.add(str(value))
                break
    return len(values)


def _unique_pair_count(rows: list[dict[str, Any]], left_keys: tuple[str, ...], right_keys: tuple[str, ...]) -> int:
    values = set()
    for row in rows:
        left = _first_value(row, left_keys)
        right = _first_value(row, right_keys)
        if left and right:
            values.add((left, right))
    return len(values)


def _first_value(row: dict[str, Any], keys: tuple[str, ...]) -> str:
    for key in keys:
        value = row.get(key)
        if value not in {"", None}:
            return str(value)
    return ""


def _value_counts(rows: list[dict[str, Any]], key: str) -> dict[str, int]:
    counts: dict[str, int] = {}
    for row in rows:
        value = str(row.get(key) or "N/A")
        counts[value] = counts.get(value, 0) + 1
    return counts
