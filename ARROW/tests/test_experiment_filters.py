import json

from src.experiment_filters import filter_rows
from src.experiment_filters import baseline_passed


def _row(**overrides):
    row = {
        "baseline_state": "MODULE_TESTS_PASSED",
        "initial_failure_state": "COMPILE_FAILED",
        "initial_failure_origin": "GENERATED_TEST",
    }
    row.update(overrides)
    return row


def test_rq1_baseline_filter_keeps_generated_passes_and_failures():
    rows = [
        _row(initial_failure_state="TARGET_TEST_PASSED"),
        _row(initial_failure_state="ASSERTION_FAILED"),
        _row(baseline_state="TOOL_ERROR", initial_failure_origin="INFRASTRUCTURE"),
        _row(baseline_state="", initial_failure_origin=""),
    ]

    selected, stats = filter_rows(rows, mode="baseline_valid")

    assert len(selected) == 2
    assert stats["selected"] == 2
    assert stats["excluded"] == 2
    assert stats["infrastructure_error"] == 1
    assert stats["baseline_unknown"] == 1


def test_rq2_filter_requires_baseline_pass_and_initial_generated_failure():
    rows = [
        _row(),
        _row(initial_failure_state="TARGET_TEST_PASSED"),
        _row(initial_failure_origin="INFRASTRUCTURE"),
        _row(baseline_state="MODULE_TESTS_FAILED"),
    ]

    selected, stats = filter_rows(rows, mode="baseline_passed_generated_failed")

    assert selected == [rows[0]]
    assert stats["selected"] == 1
    assert stats["excluded"] == 3
    assert stats["generated_test_not_failed"] == 2


def test_all_filter_keeps_every_latest_row():
    rows = [_row(), _row(baseline_state="TOOL_ERROR")]
    selected, stats = filter_rows(rows, mode="all")

    assert selected == rows
    assert stats["selected"] == 2
    assert stats["excluded"] == 0


def test_baseline_passed_falls_back_to_verification_artifact(tmp_path):
    experiment_dir = tmp_path / "experiment"
    experiment_dir.mkdir()
    (experiment_dir / "baseline_verification.json").write_text(
        json.dumps({"state": "MODULE_TESTS_PASSED"}), encoding="utf-8"
    )

    assert baseline_passed({"experiment_dir": str(experiment_dir)}) is True
