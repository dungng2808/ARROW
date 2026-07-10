from __future__ import annotations

from src.report_writer import (
    PAPER_REPORT_COLUMNS,
    append_experiment_jsonl,
    experiment_statistics,
    load_experiment_jsonl,
    repair_summary_to_report_row,
    write_experiment_json,
    write_experiment_statistics,
    write_experiment_statistics_csv,
    write_mean_report,
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
