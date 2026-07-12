from __future__ import annotations

import csv

from src.report_writer import (
    PAPER_REPORT_COLUMNS,
    append_experiment_jsonl,
    build_rq1_details_rows,
    build_rq1_paired_rows,
    build_rq1_summary_rows,
    experiment_statistics,
    load_experiment_jsonl,
    repair_summary_to_report_row,
    write_experiment_json,
    write_experiment_statistics,
    write_experiment_statistics_csv,
    write_mean_report,
    write_rq1_exports,
)


def test_repair_summary_to_report_row_sets_pass_flags():
    row = repair_summary_to_report_row({"final_failure_state": "MODULE_TESTS_PASSED"}, {"agent_name": "a"})
    assert row["target_test_passed"] is True
    assert row["module_tests_passed"] is True
    assert row["test_passed"] is True


def test_write_mean_report_groups_rows(tmp_path):
    path = tmp_path / "mean.csv"
    write_mean_report(
        path,
        [
            {"agent_name": "a", "model": "m", "generation_prompt_strategy": "zero", "build_tool": "maven", "compilation": True, "test_passed": True},
            {"agent_name": "a", "model": "m", "generation_prompt_strategy": "zero", "build_tool": "maven", "compilation": True, "test_passed": False},
        ],
    )
    text = path.read_text(encoding="utf-8")
    assert "compilation_rate" in text
    assert "1.0" in text


def test_write_and_load_experiment_json_records(tmp_path):
    result_path = tmp_path / "records" / "sample" / "agent" / "prompt" / "result.json"
    jsonl_path = tmp_path / "records" / "experiments.jsonl"
    row = {"input_id": "sample", "agent_name": "agent", "target_pass_elapsed_seconds": 2.5}
    write_experiment_json(result_path, row)
    append_experiment_jsonl(jsonl_path, row)
    assert '"input_id": "sample"' in result_path.read_text(encoding="utf-8")
    loaded = load_experiment_jsonl([jsonl_path])
    assert loaded[0]["input_id"] == "sample"
    assert loaded[0]["target_pass_elapsed_seconds"] == 2.5


def test_experiment_json_contains_all_paper_columns(tmp_path):
    result_path = tmp_path / "result.json"
    row = {
        "agent_name": "qwen",
        "generation_prompt_strategy": "zero-shot",
        "compilation": True,
        "project_id": "13899",
        "focal_class": "IoUtils",
    }
    write_experiment_json(result_path, row)
    text = result_path.read_text(encoding="utf-8")
    for column in PAPER_REPORT_COLUMNS:
        assert f'"{column}"' in text


def test_write_mean_report_includes_pass_time_averages(tmp_path):
    path = tmp_path / "mean.csv"
    write_mean_report(
        path,
        [
            {"agent_name": "a", "model": "m", "generation_prompt_strategy": "zero", "build_tool": "maven", "compilation": True, "test_passed": True, "target_pass_elapsed_seconds": 2},
            {"agent_name": "a", "model": "m", "generation_prompt_strategy": "zero", "build_tool": "maven", "compilation": True, "test_passed": False, "target_pass_elapsed_seconds": ""},
            {"agent_name": "a", "model": "m", "generation_prompt_strategy": "zero", "build_tool": "maven", "compilation": True, "test_passed": True, "target_pass_elapsed_seconds": 4},
        ],
    )
    text = path.read_text(encoding="utf-8")
    assert "avg_target_pass_elapsed_seconds" in text
    assert "3.0" in text


def test_experiment_statistics_include_repair_and_metric_rates(tmp_path):
    rows = [
        {
            "project_id": "p1",
            "sample_id": "p1_0",
            "focal_class": "Foo",
            "agent_name": "a",
            "model": "m",
            "generation_prompt_strategy": "zero",
            "build_tool": "maven",
            "compilation": True,
            "target_test_passed": True,
            "module_tests_passed": True,
            "test_passed": True,
            "initial_failure_state": "COMPILE_FAILED",
            "final_failure_state": "MODULE_TESTS_PASSED",
            "repair_attempts": 2,
            "total_llm_attempts": 3,
            "coverage_line": "80",
            "coverage_branch": "50",
            "coverage_method": "100",
            "mutation_score": "75",
            "test_smell_total": "1",
        },
        {
            "project_id": "p2",
            "sample_id": "p2_0",
            "focal_class": "Bar",
            "agent_name": "a",
            "model": "m",
            "generation_prompt_strategy": "zero",
            "build_tool": "maven",
            "compilation": False,
            "test_passed": False,
            "initial_failure_state": "COMPILE_FAILED",
            "final_failure_state": "COMPILE_FAILED",
            "repair_attempts": 1,
            "total_llm_attempts": 2,
        },
    ]

    stats = experiment_statistics(rows)["overall"]

    assert stats["total_experiments"] == 2
    assert stats["unique_projects"] == 2
    assert stats["compilation_success_rate"] == 0.5
    assert stats["module_pass_rate"] == 0.5
    assert stats["repair_success_rate"] == 0.5
    assert stats["avg_repair_attempts"] == 1.5
    assert stats["avg_line_coverage"] == 80.0

    json_path = tmp_path / "stats.json"
    csv_path = tmp_path / "stats.csv"
    write_experiment_statistics(json_path, rows)
    write_experiment_statistics_csv(csv_path, rows)
    assert "repair_success_rate" in json_path.read_text(encoding="utf-8")
    assert "avg_mutation_score" in csv_path.read_text(encoding="utf-8")


def _rq1_row(
    strategy: str,
    *,
    run_id: str = "run-1",
    finished_at: str = "2026-07-10T00:00:00+00:00",
    initial_state: str = "TARGET_TEST_PASSED",
    final_state: str = "MODULE_TESTS_PASSED",
    input_id: str = "p1_0",
    build_tool: str = "maven",
    agent_name: str = "agent",
    model: str = "model",
) -> dict:
    return {
        "run_id": run_id,
        "shard_id": "repo_shard_00",
        "project_id": "p1",
        "input_id": input_id,
        "sample_id": input_id,
        "focal_class": "Example",
        "agent_name": agent_name,
        "model": model,
        "build_tool": build_tool,
        "generation_prompt_strategy": strategy,
        "initial_failure_state": initial_state,
        "final_failure_state": final_state,
        "repair_status": "NOT_NEEDED",
        "repair_attempts": 0,
        "total_llm_attempts": 1,
        "llm_input_tokens": 100,
        "llm_output_tokens": 20,
        "llm_total_tokens": 120,
        "elapsed_seconds": 2,
        "started_at": "2026-07-09T23:59:00+00:00",
        "finished_at": finished_at,
    }


def test_rq1_paired_uses_latest_logical_result_and_details_keep_history():
    old_zero = _rq1_row(
        "zero-shot",
        run_id="run-1",
        finished_at="2026-07-10T00:00:00+00:00",
    )
    new_zero = _rq1_row(
        "zero-shot",
        run_id="run-2",
        finished_at="2026-07-11T00:00:00+00:00",
        initial_state="COMPILE_FAILED",
        final_state="COMPILE_FAILED",
    )
    rows = [
        old_zero,
        _rq1_row("few-shot"),
        _rq1_row("zero-shot-project-aware"),
        new_zero,
        _rq1_row("unrelated-prompt"),
    ]

    paired = build_rq1_paired_rows(rows)
    details = build_rq1_details_rows(rows)

    assert len(paired) == 1
    assert paired[0]["complete_triplet"] is True
    assert paired[0]["zero_shot_run_id"] == "run-2"
    assert paired[0]["zero_shot_initial_compile_success"] is False
    assert len(details) == 4
    assert sum(1 for row in details if row["generation_prompt_strategy"] == "zero-shot") == 2
    assert next(row for row in details if row["run_id"] == "run-1")["is_latest_logical_result"] is False
    assert next(row for row in details if row["run_id"] == "run-2")["is_latest_logical_result"] is True
    assert all(row["complete_triplet"] is True for row in details)


def test_rq1_incomplete_triplet_is_paired_but_excluded_from_summary():
    rows = [_rq1_row("zero-shot"), _rq1_row("few-shot")]

    paired = build_rq1_paired_rows(rows)

    assert len(paired) == 1
    assert paired[0]["complete_triplet"] is False
    assert paired[0]["repository_aware_run_id"] == ""
    summary = build_rq1_summary_rows(rows)
    assert len(summary) == 3
    assert {row["prompt_strategy"] for row in summary} == {
        "zero-shot",
        "few-shot",
        "zero-shot-project-aware",
    }
    assert {row["complete_triplets"] for row in summary} == {0}
    assert {row["rq1_conclusion"] for row in summary} == {"INSUFFICIENT_DATA"}
    assert all(row["rq1_answer_en"].startswith("INSUFFICIENT DATA:") for row in summary)
    assert all("CHƯA ĐỦ DỮ LIỆU" in row["rq1_answer_vi"] for row in summary)


def test_rq1_summary_uses_equal_complete_sample_denominators_and_tri_state_metrics():
    rows = [
        _rq1_row("zero-shot", initial_state="TEST_DISCOVERY_FAILED", final_state="TEST_DISCOVERY_FAILED"),
        _rq1_row("few-shot", initial_state="BUILD_TIMEOUT", final_state="TOOL_ERROR"),
        {
            **_rq1_row(
                "zero-shot-project-aware",
                initial_state="COMPILE_FAILED",
                final_state="MODULE_TESTS_PASSED",
            ),
            "repair_attempts": 2,
            "repair_status": "REPAIRED",
            "total_llm_attempts": 3,
        },
    ]

    summary = build_rq1_summary_rows(rows)
    by_prompt = {row["prompt_strategy"]: row for row in summary}

    assert len(summary) == 3
    assert {row["complete_triplets"] for row in summary} == {1}
    assert {row["data_ready"] for row in summary} == {False}
    assert {row["compile_paired_samples"] for row in summary} == {0}
    assert by_prompt["zero-shot"]["compile_success_rate_pct"] == ""
    assert by_prompt["few-shot"]["execution_success_rate_pct"] == ""
    assert {
        row["repo_vs_zero_compile_result"] for row in summary
    } == {"INSUFFICIENT_DATA"}
    assert {row["rq1_conclusion"] for row in summary} == {"INSUFFICIENT_DATA"}


def test_rq1_exports_combine_build_tools_in_summary_and_paired_rows():
    rows = [
        _rq1_row("zero-shot", build_tool="maven"),
        _rq1_row("few-shot", build_tool="gradle"),
        _rq1_row("zero-shot-project-aware", build_tool="maven"),
        _rq1_row("zero-shot", input_id="p1_1", build_tool="gradle"),
        _rq1_row("few-shot", input_id="p1_1", build_tool="gradle"),
        _rq1_row("zero-shot-project-aware", input_id="p1_1", build_tool="gradle"),
    ]

    summary = build_rq1_summary_rows(rows)
    paired = build_rq1_paired_rows(rows)

    assert len(summary) == 3
    assert {row["complete_triplets"] for row in summary} == {2}
    assert {row["build_tools"] for row in summary} == {"gradle|maven"}
    assert len(paired) == 2
    assert paired[0]["complete_triplet"] is True
    assert paired[0]["build_tools"] == "gradle|maven"
    assert paired[1]["build_tools"] == "gradle"


def test_rq1_summary_directly_answers_when_repository_aware_wins_all_pairs():
    rows = []
    for index in range(10):
        input_id = f"p1_{index}"
        rows.extend(
            [
                _rq1_row("zero-shot", input_id=input_id, initial_state="COMPILE_FAILED"),
                _rq1_row("few-shot", input_id=input_id, initial_state="COMPILE_FAILED"),
                _rq1_row(
                    "zero-shot-project-aware",
                    input_id=input_id,
                    initial_state="TARGET_TEST_PASSED",
                ),
            ]
        )

    summary = build_rq1_summary_rows(rows)

    assert len(summary) == 3
    assert {row["prompt_strategy"] for row in summary} == {
        "zero-shot",
        "few-shot",
        "zero-shot-project-aware",
    }
    assert {row["repo_vs_zero_compile_improvement_pp"] for row in summary} == {100.0}
    assert {row["repo_vs_few_execution_improvement_pp"] for row in summary} == {100.0}
    assert {row["repo_vs_zero_compile_wins"] for row in summary} == {10}
    assert {row["repo_vs_few_execution_losses"] for row in summary} == {0}
    assert all(row["repo_vs_zero_compile_holm_p_value"] < 0.05 for row in summary)
    assert {row["repo_vs_zero_compile_result"] for row in summary} == {"IMPROVED"}
    assert {row["repo_vs_few_execution_result"] for row in summary} == {"IMPROVED"}
    assert {row["rq1_conclusion"] for row in summary} == {
        "YES_IMPROVES_COMPILE_AND_EXECUTION"
    }
    assert all(row["rq1_answer_vi"].startswith("CÓ:") for row in summary)
    assert all(row["rq1_answer_en"].startswith("YES:") for row in summary)


def test_rq1_overall_is_not_ready_when_one_model_has_incomplete_triplets():
    complete_model = [
        _rq1_row(strategy, agent_name="agent-a", model="model-a")
        for strategy in ("zero-shot", "few-shot", "zero-shot-project-aware")
    ]
    incomplete_model = [
        _rq1_row("zero-shot", agent_name="agent-b", model="model-b"),
    ]

    summary = build_rq1_summary_rows([*complete_model, *incomplete_model])

    overall = [row for row in summary if row["scope"] == "overall"]
    assert len(overall) == 3
    assert {row["total_samples"] for row in overall} == {2}
    assert {row["complete_triplets"] for row in overall} == {1}
    assert {row["data_ready"] for row in overall} == {False}
    assert {row["rq1_conclusion"] for row in overall} == {"INSUFFICIENT_DATA"}


def test_write_rq1_exports_writes_bom_clean_columns_and_metadata(tmp_path):
    rows = [
        {
            **_rq1_row(strategy),
            "Project_ID": "duplicate-project-id",
            "experiment_workspace": "private/path",
        }
        for strategy in ("zero-shot", "few-shot", "zero-shot-project-aware")
    ]

    metadata = write_rq1_exports(tmp_path, rows)

    assert set(metadata) == {"summary", "paired", "details"}
    assert metadata["summary"]["rows"] == 3
    assert metadata["paired"]["rows"] == 1
    assert metadata["details"]["rows"] == 3
    for item in metadata.values():
        path = tmp_path / item["filename"]
        assert path.read_bytes().startswith(b"\xef\xbb\xbf")

    with (tmp_path / "rq1_details.csv").open(encoding="utf-8-sig", newline="") as input_file:
        reader = csv.DictReader(input_file)
        detail_rows = list(reader)
    assert len(reader.fieldnames) == len(set(reader.fieldnames))
    assert "Project_ID" not in reader.fieldnames
    assert "experiment_workspace" not in reader.fieldnames
    assert detail_rows[0]["project_id"] == "p1"
