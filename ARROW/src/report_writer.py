from __future__ import annotations

import csv
import json
from datetime import datetime, timezone
from math import erfc, sqrt
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
    "llm_input_tokens",
    "llm_output_tokens",
    "llm_total_tokens",
    "llm_call_count",
    "token_usage_by_prompt",
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


RQ1_PROMPT_STRATEGIES = (
    "zero-shot",
    "few-shot",
    "zero-shot-project-aware",
)

RQ1_STRATEGY_PREFIXES = {
    "zero-shot": "zero_shot",
    "few-shot": "few_shot",
    "zero-shot-project-aware": "repository_aware",
}

RQ1_EXPORT_FILENAMES = {
    "summary": "rq1_summary.csv",
    "paired": "rq1_paired.csv",
    "details": "rq1_details.csv",
}

RQ1_SUMMARY_COLUMNS = [
    "scope",
    "agent_name",
    "model",
    "build_tools",
    "total_samples",
    "zero_shot_available_samples",
    "few_shot_available_samples",
    "repository_aware_available_samples",
    "complete_triplets",
    "data_ready",
    "prompt_strategy",
    "compile_paired_samples",
    "compile_success_count",
    "compile_success_rate_pct",
    "execution_paired_samples",
    "execution_success_count",
    "execution_success_rate_pct",
    "repo_vs_zero_compile_improvement_pp",
    "repo_vs_few_compile_improvement_pp",
    "repo_vs_zero_execution_improvement_pp",
    "repo_vs_few_execution_improvement_pp",
    "repo_vs_zero_compile_wins",
    "repo_vs_zero_compile_losses",
    "repo_vs_zero_compile_ties",
    "repo_vs_zero_compile_p_value",
    "repo_vs_zero_compile_p_method",
    "repo_vs_zero_compile_holm_p_value",
    "repo_vs_zero_compile_result",
    "repo_vs_few_compile_wins",
    "repo_vs_few_compile_losses",
    "repo_vs_few_compile_ties",
    "repo_vs_few_compile_p_value",
    "repo_vs_few_compile_p_method",
    "repo_vs_few_compile_holm_p_value",
    "repo_vs_few_compile_result",
    "repo_vs_zero_execution_wins",
    "repo_vs_zero_execution_losses",
    "repo_vs_zero_execution_ties",
    "repo_vs_zero_execution_p_value",
    "repo_vs_zero_execution_p_method",
    "repo_vs_zero_execution_holm_p_value",
    "repo_vs_zero_execution_result",
    "repo_vs_few_execution_wins",
    "repo_vs_few_execution_losses",
    "repo_vs_few_execution_ties",
    "repo_vs_few_execution_p_value",
    "repo_vs_few_execution_p_method",
    "repo_vs_few_execution_holm_p_value",
    "repo_vs_few_execution_result",
    "alpha",
    "rq1_conclusion",
    "rq1_answer_en",
    "rq1_answer_vi",
]

_RQ1_PAIRED_METRICS = [
    "run_id",
    "initial_state",
    "initial_compile_success",
    "initial_target_pass",
    "final_state",
    "final_compile_success",
    "final_target_pass",
    "final_module_pass",
    "repair_status",
    "repair_attempts",
    "total_llm_attempts",
    "elapsed_seconds",
    "llm_total_tokens",
]

RQ1_PAIRED_COLUMNS = [
    "project_id",
    "input_id",
    "sample_id",
    "focal_class",
    "agent_name",
    "model",
    "build_tools",
    "complete_triplet",
    *[
        f"{RQ1_STRATEGY_PREFIXES[strategy]}_{metric}"
        for strategy in RQ1_PROMPT_STRATEGIES
        for metric in _RQ1_PAIRED_METRICS
    ],
]

RQ1_DETAILS_COLUMNS = [
    "run_id",
    "shard_id",
    "project_id",
    "input_id",
    "sample_id",
    "focal_class",
    "agent_name",
    "model",
    "generation_prompt_strategy",
    "build_tool",
    "initial_state",
    "initial_compile_success",
    "initial_target_pass",
    "final_state",
    "final_compile_success",
    "final_target_pass",
    "final_module_pass",
    "repair_status",
    "repair_attempts",
    "regeneration_attempts",
    "total_llm_attempts",
    "llm_input_tokens",
    "llm_output_tokens",
    "llm_total_tokens",
    "elapsed_seconds",
    "coverage_branch",
    "coverage_line",
    "coverage_method",
    "mutation_score",
    "mutations_total",
    "mutations_killed",
    "mutations_survived",
    "test_smell_total",
    "test_smell_details",
    "initial_failure_origin",
    "final_failure_origin",
    "repair_stopped_reason",
    "test_fail_reason",
    "error",
    "started_at",
    "finished_at",
    "is_latest_logical_result",
    "complete_triplet",
]


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


def build_rq1_summary_rows(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Build a direct RQ1 comparison table for repository-aware versus both baselines."""
    latest = _latest_rq1_rows(rows)
    grouped_triplets: dict[tuple[str, str, str, str], dict[str, dict[str, Any]]] = {}
    for logical_key, row in latest.items():
        grouped_triplets.setdefault(logical_key[:-1], {})[logical_key[-1]] = row

    model_groups: dict[tuple[str, str], list[dict[str, dict[str, Any]]]] = {}
    for base_key, triplet in grouped_triplets.items():
        model_groups.setdefault((base_key[2], base_key[3]), []).append(triplet)

    scopes: list[tuple[str, str, str, list[dict[str, dict[str, Any]]]]] = [
        ("model", agent, model, model_groups[(agent, model)])
        for agent, model in sorted(model_groups)
    ]
    if len(model_groups) > 1:
        scopes.append(("overall", "ALL", "ALL", list(grouped_triplets.values())))

    output_rows: list[dict[str, Any]] = []
    for scope, agent, model, triplets in scopes:
        output_rows.extend(_rq1_prompt_summary_rows(scope, agent, model, triplets))
    return output_rows


def build_rq1_paired_rows(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Build one wide row per sample/model combination, including incomplete triplets."""
    latest = _latest_rq1_rows(rows)
    grouped: dict[tuple[str, str, str, str], dict[str, dict[str, Any]]] = {}
    for logical_key, row in latest.items():
        grouped.setdefault(logical_key[:-1], {})[logical_key[-1]] = row

    output_rows: list[dict[str, Any]] = []
    for base_key in sorted(grouped):
        project_id, input_id, agent, model = base_key
        triplet = grouped[base_key]
        representative = next((triplet.get(strategy) for strategy in RQ1_PROMPT_STRATEGIES if strategy in triplet), {})
        output: dict[str, Any] = {
            "project_id": project_id,
            "input_id": input_id,
            "sample_id": _first_value(representative, ("sample_id", "input_id")),
            "focal_class": _first_value(representative, ("focal_class", "Class_Under_Test")),
            "agent_name": agent,
            "model": model,
            "build_tools": _rq1_build_tools(list(triplet.values())),
            "complete_triplet": _is_complete_rq1_triplet(triplet),
        }
        for strategy in RQ1_PROMPT_STRATEGIES:
            prefix = RQ1_STRATEGY_PREFIXES[strategy]
            item = triplet.get(strategy)
            values = _rq1_paired_values(item) if item is not None else {}
            for metric in _RQ1_PAIRED_METRICS:
                output[f"{prefix}_{metric}"] = values.get(metric, "")
        output_rows.append(output)
    return output_rows


def build_rq1_details_rows(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Build history-preserving RQ1 detail rows with latest/triplet markers."""
    filtered = _filtered_rq1_rows(rows)
    latest_indices = _latest_rq1_row_indices(filtered)
    latest = {key: filtered[index][1] for key, index in latest_indices.items()}
    complete_keys = {
        key[:-1]
        for key in latest
        if all((*key[:-1], strategy) in latest for strategy in RQ1_PROMPT_STRATEGIES)
    }
    output_rows: list[dict[str, Any]] = []
    for filtered_index, (_original_index, row) in enumerate(filtered):
        logical_key = _rq1_logical_key(row)
        output_rows.append(
            {
                "run_id": row.get("run_id", ""),
                "shard_id": row.get("shard_id", ""),
                "project_id": logical_key[0],
                "input_id": logical_key[1],
                "sample_id": _first_value(row, ("sample_id", "input_id")),
                "focal_class": _first_value(row, ("focal_class", "Class_Under_Test")),
                "agent_name": logical_key[2],
                "model": logical_key[3],
                "generation_prompt_strategy": logical_key[4],
                "build_tool": _rq1_build_tool(row),
                "initial_state": _rq1_state(row, "initial"),
                "initial_compile_success": _rq1_initial_compile(row),
                "initial_target_pass": _rq1_initial_target_pass(row),
                "final_state": _rq1_state(row, "final"),
                "final_compile_success": _rq1_final_compile(row),
                "final_target_pass": _rq1_final_target_pass(row),
                "final_module_pass": _rq1_final_module_pass(row),
                "repair_status": row.get("repair_status", ""),
                "repair_attempts": row.get("repair_attempts", ""),
                "regeneration_attempts": row.get("regeneration_attempts", ""),
                "total_llm_attempts": row.get("total_llm_attempts", ""),
                "llm_input_tokens": row.get("llm_input_tokens", ""),
                "llm_output_tokens": row.get("llm_output_tokens", ""),
                "llm_total_tokens": row.get("llm_total_tokens", ""),
                "elapsed_seconds": row.get("elapsed_seconds", ""),
                "coverage_branch": _first_present(row, ("coverage_branch", "Branch_Coverage%")),
                "coverage_line": _first_present(row, ("coverage_line", "Line_Coverage%")),
                "coverage_method": _first_present(row, ("coverage_method", "Method_Coverage%")),
                "mutation_score": _first_present(row, ("mutation_score", "Mutation_Score%")),
                "mutations_total": row.get("mutations_total", ""),
                "mutations_killed": row.get("mutations_killed", ""),
                "mutations_survived": row.get("mutations_survived", ""),
                "test_smell_total": row.get("test_smell_total", ""),
                "test_smell_details": _csv_safe_value(row.get("test_smell_details", "")),
                "initial_failure_origin": row.get("initial_failure_origin", ""),
                "final_failure_origin": row.get("final_failure_origin", ""),
                "repair_stopped_reason": row.get("repair_stopped_reason", ""),
                "test_fail_reason": row.get("test_fail_reason", ""),
                "error": row.get("error", ""),
                "started_at": row.get("started_at", ""),
                "finished_at": row.get("finished_at", ""),
                "is_latest_logical_result": latest_indices.get(logical_key) == filtered_index,
                "complete_triplet": logical_key[:-1] in complete_keys,
            }
        )
    return output_rows


def write_rq1_exports(output_dir: Path, rows: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
    """Write all RQ1 CSV exports using UTF-8 BOM and return artifact metadata."""
    ensure_dir(output_dir)
    export_rows = {
        "summary": build_rq1_summary_rows(rows),
        "paired": build_rq1_paired_rows(rows),
        "details": build_rq1_details_rows(rows),
    }
    columns = {
        "summary": RQ1_SUMMARY_COLUMNS,
        "paired": RQ1_PAIRED_COLUMNS,
        "details": RQ1_DETAILS_COLUMNS,
    }
    metadata: dict[str, dict[str, Any]] = {}
    for export_type in ("summary", "paired", "details"):
        path = output_dir / RQ1_EXPORT_FILENAMES[export_type]
        _write_csv_with_bom(path, export_rows[export_type], columns[export_type])
        metadata[export_type] = {
            "path": str(path),
            "filename": path.name,
            "rows": len(export_rows[export_type]),
        }
    return metadata


def write_mean_report(path: Path, rows: list[dict[str, Any]]) -> None:
    rows = [ensure_paper_report_fields(row) for row in rows]
    groups: dict[tuple[str, str], list[dict[str, Any]]] = {}
    for row in rows:
        key = (
            str(row.get("Generator(LLM)", "")),
            str(row.get("Prompt_Technique", "")),
        )
        groups.setdefault(key, []).append(row)
    output_rows = []
    for (generator, prompt), items in groups.items():
        total = len(items)
        compiled = sum(1 for item in items if _truthy(item.get("Compilation")))
        output = {
            "Generator(LLM)": generator,
            "Prompt_Technique": prompt,
            "Total_Samples": total,
            "Compilation_0_Count": total - compiled,
            "Compilation_1_Count": compiled,
            "Compilation_Success_Rate": compiled / total if total else 0,
            "Branch_Coverage%_Mean": _average_multi(items, ("Branch_Coverage%",)),
            "Line_Coverage%_Mean": _average_multi(items, ("Line_Coverage%",)),
            "Method_Coverage%_Mean": _average_multi(items, ("Method_Coverage%",)),
            "Mutation_Score%_Mean": _average_multi(items, ("Mutation_Score%",)),
        }
        for smell in SMELL_COLUMNS:
            output[f"{smell}_Mean"] = _average_multi(items, (smell,))
        output_rows.append(output)
    ensure_dir(path.parent)
    fieldnames = [
        "Generator(LLM)",
        "Prompt_Technique",
        "Total_Samples",
        "Compilation_0_Count",
        "Compilation_1_Count",
        "Compilation_Success_Rate",
        "Branch_Coverage%_Mean",
        "Line_Coverage%_Mean",
        "Method_Coverage%_Mean",
        "Mutation_Score%_Mean",
        *[f"{smell}_Mean" for smell in SMELL_COLUMNS],
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
        "avg_llm_input_tokens",
        "avg_llm_output_tokens",
        "avg_llm_total_tokens",
        "avg_llm_call_count",
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
        "avg_llm_input_tokens": _average_multi(rows, ("llm_input_tokens",)),
        "avg_llm_output_tokens": _average_multi(rows, ("llm_output_tokens",)),
        "avg_llm_total_tokens": _average_multi(rows, ("llm_total_tokens",)),
        "avg_llm_call_count": _average_multi(rows, ("llm_call_count",)),
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


def _zero_numeric_count(rows: list[dict[str, Any]], keys: tuple[str, ...]) -> int:
    return sum(1 for row in rows if _float_value(row, keys) == 0)


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


_RQ1_INFRASTRUCTURE_STATES = {
    "BUILD_TIMEOUT",
    "TIMEOUT",
    "TOOL_ERROR",
    "UNKNOWN",
    "UNKNOWN_FAILED",
}

_RQ1_EVALUABLE_STATES = {
    "COMPILE_FAILED",
    "TEST_DISCOVERY_FAILED",
    "RUNTIME_FAILED",
    "ASSERTION_FAILED",
    "TARGET_TEST_PASSED",
    "MODULE_TESTS_FAILED",
    "MODULE_TESTS_PASSED",
}


def _filtered_rq1_rows(rows: list[dict[str, Any]]) -> list[tuple[int, dict[str, Any]]]:
    return [
        (index, row)
        for index, row in enumerate(rows)
        if _rq1_strategy(row) in RQ1_PROMPT_STRATEGIES
    ]


def _latest_rq1_row_indices(
    filtered: list[tuple[int, dict[str, Any]]],
) -> dict[tuple[str, str, str, str, str], int]:
    latest: dict[tuple[str, str, str, str, str], int] = {}
    latest_ranks: dict[tuple[str, str, str, str, str], tuple[Any, ...]] = {}
    for filtered_index, (original_index, row) in enumerate(filtered):
        key = _rq1_logical_key(row)
        rank = (
            _rq1_timestamp_rank(row.get("finished_at")),
            _rq1_timestamp_rank(row.get("started_at")),
            str(row.get("run_id") or ""),
            original_index,
        )
        if key not in latest_ranks or rank > latest_ranks[key]:
            latest[key] = filtered_index
            latest_ranks[key] = rank
    return latest


def _latest_rq1_rows(
    rows: list[dict[str, Any]],
) -> dict[tuple[str, str, str, str, str], dict[str, Any]]:
    filtered = _filtered_rq1_rows(rows)
    indices = _latest_rq1_row_indices(filtered)
    return {key: filtered[index][1] for key, index in indices.items()}


def _rq1_timestamp_rank(value: Any) -> tuple[int, float, str]:
    text = str(value or "").strip()
    if not text:
        return (0, float("-inf"), "")
    try:
        parsed = datetime.fromisoformat(text.replace("Z", "+00:00"))
        if parsed.tzinfo is None:
            parsed = parsed.replace(tzinfo=timezone.utc)
        return (1, parsed.timestamp(), text)
    except (OverflowError, ValueError):
        return (0, float("-inf"), text)


def _rq1_logical_key(row: dict[str, Any]) -> tuple[str, str, str, str, str]:
    return (
        _first_value(row, ("project_id", "Project_ID")),
        _first_value(row, ("input_id", "sample_id")),
        _rq1_agent(row),
        _rq1_model(row),
        _rq1_strategy(row),
    )


def _rq1_agent(row: dict[str, Any]) -> str:
    return _first_value(row, ("agent_name", "Generator(LLM)"))


def _rq1_model(row: dict[str, Any]) -> str:
    return _first_value(row, ("model",))


def _rq1_build_tool(row: dict[str, Any]) -> str:
    return _first_value(row, ("build_tool",)).strip().lower()


def _rq1_build_tools(rows: list[dict[str, Any]]) -> str:
    tools = sorted({_rq1_build_tool(row) for row in rows if _rq1_build_tool(row)})
    return "|".join(tools)


def _rq1_strategy(row: dict[str, Any]) -> str:
    return _first_value(row, ("generation_prompt_strategy", "Prompt_Technique")).strip().lower()


def _is_complete_rq1_triplet(triplet: dict[str, dict[str, Any]]) -> bool:
    return all(strategy in triplet for strategy in RQ1_PROMPT_STRATEGIES)


def _rq1_state(row: dict[str, Any], phase: str) -> str:
    value = _first_value(row, (f"{phase}_state", f"{phase}_failure_state"))
    return value.strip().upper()


def _rq1_initial_compile(row: dict[str, Any]) -> bool | None:
    state = _rq1_state(row, "initial")
    if state:
        return _rq1_compile_from_state(state)
    return _rq1_tri_bool(_first_present(row, ("initial_compile_success", "initial_compilation_success")))


def _rq1_initial_target_pass(row: dict[str, Any]) -> bool | None:
    state = _rq1_state(row, "initial")
    if state:
        return _rq1_target_from_state(state)
    return _rq1_tri_bool(_first_present(row, ("initial_target_pass", "initial_target_test_passed")))


def _rq1_final_compile(row: dict[str, Any]) -> bool | None:
    state = _rq1_state(row, "final")
    if state:
        return _rq1_compile_from_state(state)
    return _rq1_tri_bool(
        _first_present(
            row,
            ("final_compile_success", "final_compilation_success", "compilation", "Compilation"),
        )
    )


def _rq1_final_target_pass(row: dict[str, Any]) -> bool | None:
    state = _rq1_state(row, "final")
    if state:
        return _rq1_target_from_state(state)
    return _rq1_tri_bool(
        _first_present(row, ("final_target_pass", "final_target_test_passed", "target_test_passed"))
    )


def _rq1_final_module_pass(row: dict[str, Any]) -> bool | None:
    state = _rq1_state(row, "final")
    if state in _RQ1_INFRASTRUCTURE_STATES:
        return None
    if state in _RQ1_EVALUABLE_STATES:
        return state == "MODULE_TESTS_PASSED"
    if state:
        return None
    return _rq1_tri_bool(
        _first_present(
            row,
            ("final_module_pass", "final_module_tests_passed", "module_tests_passed", "test_passed"),
        )
    )


def _rq1_compile_from_state(state: str) -> bool | None:
    if state in _RQ1_INFRASTRUCTURE_STATES:
        return None
    if state == "COMPILE_FAILED":
        return False
    if state in _RQ1_EVALUABLE_STATES:
        # Discovery failure means Java compilation completed but no generated test ran.
        return True
    return None


def _rq1_target_from_state(state: str) -> bool | None:
    if state in _RQ1_INFRASTRUCTURE_STATES:
        return None
    if state in {"TARGET_TEST_PASSED", "MODULE_TESTS_FAILED", "MODULE_TESTS_PASSED"}:
        return True
    if state in _RQ1_EVALUABLE_STATES:
        return False
    return None


def _rq1_prompt_summary_rows(
    scope: str,
    agent: str,
    model: str,
    triplets: list[dict[str, dict[str, Any]]],
) -> list[dict[str, Any]]:
    complete = [triplet for triplet in triplets if _is_complete_rq1_triplet(triplet)]
    compile_ready = [
        triplet
        for triplet in complete
        if all(_rq1_initial_compile(triplet[strategy]) is not None for strategy in RQ1_PROMPT_STRATEGIES)
    ]
    execution_ready = [
        triplet
        for triplet in complete
        if all(_rq1_initial_target_pass(triplet[strategy]) is not None for strategy in RQ1_PROMPT_STRATEGIES)
    ]
    all_items = [item for triplet in triplets for item in triplet.values()]
    data_ready = bool(triplets) and len(complete) == len(triplets) and bool(compile_ready and execution_ready)
    common = {
        "scope": scope,
        "agent_name": agent,
        "model": model,
        "build_tools": _rq1_build_tools(all_items),
        "total_samples": len(triplets),
        "zero_shot_available_samples": sum("zero-shot" in triplet for triplet in triplets),
        "few_shot_available_samples": sum("few-shot" in triplet for triplet in triplets),
        "repository_aware_available_samples": sum(
            "zero-shot-project-aware" in triplet for triplet in triplets
        ),
        "complete_triplets": len(complete),
        "data_ready": data_ready,
    }
    comparison_specs = (
        ("repo_vs_zero_compile", compile_ready, "zero-shot", _rq1_initial_compile),
        ("repo_vs_few_compile", compile_ready, "few-shot", _rq1_initial_compile),
        ("repo_vs_zero_execution", execution_ready, "zero-shot", _rq1_initial_target_pass),
        ("repo_vs_few_execution", execution_ready, "few-shot", _rq1_initial_target_pass),
    )
    comparisons = {
        name: _rq1_pair_comparison(metric_triplets, baseline, getter)
        for name, metric_triplets, baseline, getter in comparison_specs
    }
    adjusted = _holm_adjusted_p_values(
        [comparisons[name]["p_value"] for name, *_rest in comparison_specs]
    )
    for (name, *_rest), adjusted_p in zip(comparison_specs, adjusted):
        comparison = comparisons[name]
        comparison["holm_p_value"] = adjusted_p
        comparison["result"] = _rq1_comparison_result(
            comparison["paired_samples"],
            comparison["improvement_pp"],
            adjusted_p,
        )
    conclusion = (
        _rq1_conclusion([comparisons[name]["result"] for name, *_rest in comparison_specs])
        if data_ready
        else "INSUFFICIENT_DATA"
    )

    comparison_columns: dict[str, Any] = {}
    for name, comparison in comparisons.items():
        comparison_columns.update(
            {
                f"{name}_improvement_pp": comparison["improvement_pp"],
                f"{name}_wins": comparison["wins"],
                f"{name}_losses": comparison["losses"],
                f"{name}_ties": comparison["ties"],
                f"{name}_p_value": comparison["p_value"],
                f"{name}_p_method": comparison["p_method"],
                f"{name}_holm_p_value": comparison["holm_p_value"],
                f"{name}_result": comparison["result"],
            }
        )

    output: list[dict[str, Any]] = []
    for strategy in RQ1_PROMPT_STRATEGIES:
        compile_success = sum(
            _rq1_initial_compile(triplet[strategy]) is True for triplet in compile_ready
        )
        execution_success = sum(
            _rq1_initial_target_pass(triplet[strategy]) is True for triplet in execution_ready
        )
        output.append(
            {
                **common,
                "prompt_strategy": strategy,
                "compile_paired_samples": len(compile_ready),
                "compile_success_count": compile_success,
                "compile_success_rate_pct": _percentage(compile_success, len(compile_ready)),
                "execution_paired_samples": len(execution_ready),
                "execution_success_count": execution_success,
                "execution_success_rate_pct": _percentage(execution_success, len(execution_ready)),
                **comparison_columns,
                "alpha": 0.05,
                "rq1_conclusion": conclusion,
                "rq1_answer_en": _rq1_answer_en(conclusion),
                "rq1_answer_vi": _rq1_answer_vi(conclusion),
            }
        )
    return output


def _rq1_pair_comparison(
    triplets: list[dict[str, dict[str, Any]]],
    baseline_prompt: str,
    getter: Any,
) -> dict[str, Any]:
    pairs = [
        (
            bool(getter(triplet[baseline_prompt])),
            bool(getter(triplet["zero-shot-project-aware"])),
        )
        for triplet in triplets
    ]
    baseline_success = sum(baseline for baseline, _repository in pairs)
    repository_success = sum(repository for _baseline, repository in pairs)
    wins = sum((not baseline) and repository for baseline, repository in pairs)
    losses = sum(baseline and (not repository) for baseline, repository in pairs)
    ties = len(pairs) - wins - losses
    baseline_rate = _percentage(baseline_success, len(pairs))
    repository_rate = _percentage(repository_success, len(pairs))
    improvement = (
        repository_rate - baseline_rate
        if isinstance(repository_rate, float) and isinstance(baseline_rate, float)
        else ""
    )
    if pairs:
        p_value, p_method = _mcnemar_p_value(wins, losses)
    else:
        p_value, p_method = "", ""
    return {
        "paired_samples": len(pairs),
        "improvement_pp": improvement,
        "wins": wins,
        "losses": losses,
        "ties": ties,
        "p_value": p_value,
        "p_method": p_method,
        "holm_p_value": "",
        "result": "",
    }


def _percentage(numerator: int, denominator: int) -> float | str:
    if not denominator:
        return ""
    return numerator * 100.0 / denominator


def _mcnemar_p_value(wins: int, losses: int) -> tuple[float, str]:
    discordant = wins + losses
    if discordant == 0:
        return 1.0, "exact_binomial"
    if discordant <= 2000:
        tail_end = min(wins, losses)
        term = 1
        numerator = 1
        for index in range(1, tail_end + 1):
            term = term * (discordant - index + 1) // index
            numerator += term
        return min(1.0, (2 * numerator) / (2**discordant)), "exact_binomial"
    continuity_corrected_z = max(0.0, abs(wins - losses) - 1) / sqrt(discordant)
    return erfc(continuity_corrected_z / sqrt(2.0)), "normal_approximation"


def _holm_adjusted_p_values(values: list[Any]) -> list[float | str]:
    adjusted: list[float | str] = [""] * len(values)
    valid = sorted(
        (float(value), index)
        for index, value in enumerate(values)
        if value not in {"", None}
    )
    previous = 0.0
    total = len(valid)
    for rank, (p_value, index) in enumerate(valid):
        current = min(1.0, p_value * (total - rank))
        previous = max(previous, current)
        adjusted[index] = previous
    return adjusted


def _rq1_comparison_result(
    paired_samples: int,
    improvement_pp: Any,
    adjusted_p_value: Any,
) -> str:
    if paired_samples <= 0 or improvement_pp in {"", None} or adjusted_p_value in {"", None}:
        return "INSUFFICIENT_DATA"
    improvement = float(improvement_pp)
    significant = float(adjusted_p_value) < 0.05
    if significant and improvement > 0:
        return "IMPROVED"
    if significant and improvement < 0:
        return "WORSE"
    return "NO_SIGNIFICANT_DIFFERENCE"


def _rq1_conclusion(results: list[str]) -> str:
    if not results or any(result == "INSUFFICIENT_DATA" for result in results):
        return "INSUFFICIENT_DATA"
    if all(result == "IMPROVED" for result in results):
        return "YES_IMPROVES_COMPILE_AND_EXECUTION"
    if any(result == "IMPROVED" for result in results):
        return "PARTIAL_IMPROVEMENT"
    if any(result == "WORSE" for result in results):
        return "NO_REPOSITORY_AWARE_IS_WORSE"
    return "NO_SIGNIFICANT_IMPROVEMENT"


def _rq1_answer_vi(conclusion: str) -> str:
    answers = {
        "YES_IMPROVES_COMPILE_AND_EXECUTION": (
            "CÓ: Repository-aware cải thiện có ý nghĩa thống kê cả khả năng biên dịch và thực thi "
            "so với Zero-shot và Few-shot."
        ),
        "PARTIAL_IMPROVEMENT": (
            "CẢI THIỆN MỘT PHẦN: Repository-aware chỉ tốt hơn có ý nghĩa ở một số metric hoặc baseline."
        ),
        "NO_REPOSITORY_AWARE_IS_WORSE": (
            "KHÔNG: Repository-aware không cải thiện và có ít nhất một so sánh kém hơn có ý nghĩa thống kê."
        ),
        "NO_SIGNIFICANT_IMPROVEMENT": (
            "CHƯA CÓ BẰNG CHỨNG: chênh lệch của Repository-aware chưa có ý nghĩa thống kê."
        ),
        "INSUFFICIENT_DATA": (
            "CHƯA ĐỦ DỮ LIỆU: cần chạy đủ ba prompt trên cùng sample và model để trả lời RQ1."
        ),
    }
    return answers[conclusion]


def _rq1_answer_en(conclusion: str) -> str:
    answers = {
        "YES_IMPROVES_COMPILE_AND_EXECUTION": (
            "YES: Repository-aware prompting significantly improves both initial compilation "
            "and test execution compared with Zero-shot and Few-shot."
        ),
        "PARTIAL_IMPROVEMENT": (
            "PARTIAL IMPROVEMENT: Repository-aware prompting is significantly better for only "
            "some metrics or baselines."
        ),
        "NO_REPOSITORY_AWARE_IS_WORSE": (
            "NO: Repository-aware prompting does not improve the results and is significantly "
            "worse in at least one comparison."
        ),
        "NO_SIGNIFICANT_IMPROVEMENT": (
            "NO SIGNIFICANT IMPROVEMENT: The observed Repository-aware differences are not "
            "statistically significant."
        ),
        "INSUFFICIENT_DATA": (
            "INSUFFICIENT DATA: Run all three prompting strategies on the same sample and model "
            "before drawing an RQ1 conclusion."
        ),
    }
    return answers[conclusion]


def _rq1_tri_bool(value: Any) -> bool | None:
    if isinstance(value, bool):
        return value
    if isinstance(value, (int, float)) and value in {0, 1}:
        return bool(value)
    text = str(value or "").strip().lower()
    if text in {"true", "1", "yes"}:
        return True
    if text in {"false", "0", "no"}:
        return False
    return None


def _rq1_metric_summary(
    rows: list[dict[str, Any]],
    getter: Any,
) -> tuple[int, int, float | str]:
    values = [getter(row) for row in rows]
    evaluable = [value for value in values if value is not None]
    passed = sum(1 for value in evaluable if value is True)
    return len(evaluable), passed, _rate(passed, len(evaluable))


def _rq1_repair_attempted(row: dict[str, Any]) -> bool:
    return _int_value(row.get("repair_attempts")) > 0 or _int_value(row.get("regeneration_attempts")) > 0


def _rq1_paired_values(row: dict[str, Any]) -> dict[str, Any]:
    return {
        "run_id": row.get("run_id", ""),
        "initial_state": _rq1_state(row, "initial"),
        "initial_compile_success": _rq1_initial_compile(row),
        "initial_target_pass": _rq1_initial_target_pass(row),
        "final_state": _rq1_state(row, "final"),
        "final_compile_success": _rq1_final_compile(row),
        "final_target_pass": _rq1_final_target_pass(row),
        "final_module_pass": _rq1_final_module_pass(row),
        "repair_status": row.get("repair_status", ""),
        "repair_attempts": row.get("repair_attempts", ""),
        "total_llm_attempts": row.get("total_llm_attempts", ""),
        "elapsed_seconds": row.get("elapsed_seconds", ""),
        "llm_total_tokens": row.get("llm_total_tokens", ""),
    }


def _first_present(row: dict[str, Any], keys: tuple[str, ...]) -> Any:
    for key in keys:
        value = row.get(key)
        if value not in {"", None}:
            return value
    return ""


def _csv_safe_value(value: Any) -> Any:
    if isinstance(value, (dict, list, tuple, set)):
        return json.dumps(value, ensure_ascii=False, default=str, sort_keys=True)
    return value


def _write_csv_with_bom(path: Path, rows: list[dict[str, Any]], fieldnames: list[str]) -> None:
    ensure_dir(path.parent)
    with path.open("w", newline="", encoding="utf-8-sig") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fieldnames, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)
