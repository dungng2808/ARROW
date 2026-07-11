from __future__ import annotations

import json
import zipfile
from pathlib import Path
from xml.etree import ElementTree

import pytest

from src import rq1_export
from src.rq1_export import (
    RQ1ExcelLimitError,
    RQ1NoDataError,
    RQ1SnapshotChangedError,
    build_rq1_preview,
    load_rq1_snapshot,
    save_rq1_workbook,
    write_rq1_workbook,
)


def _row(
    strategy: str,
    *,
    input_id: str = "p1_0",
    run_id: str = "run-1",
    agent_name: str = "agent",
    model: str = "model",
    initial_state: str = "TARGET_TEST_PASSED",
    final_state: str = "MODULE_TESTS_PASSED",
) -> dict:
    return {
        "run_id": run_id,
        "shard_id": "s0",
        "project_id": "p1",
        "input_id": input_id,
        "sample_id": input_id,
        "focal_class": "Example",
        "agent_name": agent_name,
        "model": model,
        "build_tool": "maven",
        "generation_prompt_strategy": strategy,
        "initial_failure_state": initial_state,
        "final_failure_state": final_state,
        "repair_status": "NOT_NEEDED",
        "repair_attempts": 0,
        "total_llm_attempts": 1,
        "llm_total_tokens": 120,
        "elapsed_seconds": 2.5,
        "started_at": "2026-07-09T23:59:00+00:00",
        "finished_at": "2026-07-10T00:00:00+00:00",
    }


def _write_jsonl(runs_dir: Path, rows: list[dict]) -> Path:
    path = runs_dir / "repo" / "sample" / "reports" / "records" / "experiments.jsonl"
    path.parent.mkdir(parents=True)
    path.write_text("".join(json.dumps(row) + "\n" for row in rows), encoding="utf-8")
    return path


def test_snapshot_preview_filters_rq1_and_marks_incomplete_data(tmp_path):
    runs_dir = tmp_path / "runs"
    _write_jsonl(runs_dir, [_row("zero-shot"), _row("unrelated")])

    snapshot = load_rq1_snapshot(runs_dir)
    preview = build_rq1_preview(snapshot)

    assert len(snapshot.rows) == 1
    assert preview["readiness"]["status"] == "NOT_READY"
    assert preview["readiness"]["available_samples"] == {
        "zero-shot": 1,
        "few-shot": 0,
        "zero-shot-project-aware": 0,
    }
    assert preview["readiness"]["missing_strategies"] == ["Few-shot", "Repository-aware"]
    assert preview["conclusion"]["code"] == "INSUFFICIENT_DATA"
    assert preview["paired"]["total_rows"] == 1
    assert preview["details"]["total_rows"] == 1
    assert preview["result_tables"]["overall"]["columns"] == rq1_export.RESULT_TABLE_COLUMNS
    assert preview["result_tables"]["overall"]["rows"][0] == {
        "prompt_strategy": "zero-shot",
        "prompt_strategy_label": "Zero-shot",
        "total_samples": 1,
        "compiled_count": 1,
        "compiled_rate_pct": 100.0,
        "executed_count": 1,
        "executed_rate_pct": 100.0,
        "target_passed_count": 1,
        "target_passed_rate_pct": 100.0,
    }


def test_preview_uses_complete_triplet_and_validates_pagination(tmp_path):
    runs_dir = tmp_path / "runs"
    _write_jsonl(runs_dir, [_row(strategy) for strategy in rq1_export.RQ1_PROMPT_STRATEGIES])
    snapshot = load_rq1_snapshot(runs_dir)

    preview = build_rq1_preview(snapshot, paired_page=99, details_page=99, page_size=25)

    assert preview["readiness"]["status"] == "READY"
    assert preview["readiness"]["complete_triplets"] == 1
    assert preview["paired"]["page"] == 1
    assert preview["details"]["page"] == 1
    with pytest.raises(ValueError, match="page_size"):
        build_rq1_preview(snapshot, page_size=10)


def test_preview_result_tables_include_model_breakdown(tmp_path):
    runs_dir = tmp_path / "runs"
    rows = [
        *[
            _row(strategy, input_id="p1_0", run_id=f"a-{strategy}", agent_name="agent-a", model="model-a")
            for strategy in rq1_export.RQ1_PROMPT_STRATEGIES
        ],
        _row(
            "zero-shot",
            input_id="p2_0",
            run_id="b-zero",
            agent_name="agent-b",
            model="model-b",
            initial_state="COMPILE_FAILED",
            final_state="COMPILE_FAILED",
        ),
        _row(
            "few-shot",
            input_id="p2_0",
            run_id="b-few",
            agent_name="agent-b",
            model="model-b",
            initial_state="TARGET_TEST_PASSED",
            final_state="TARGET_TEST_PASSED",
        ),
    ]
    _write_jsonl(runs_dir, rows)
    snapshot = load_rq1_snapshot(runs_dir)

    preview = build_rq1_preview(snapshot, paired_page=1, details_page=1, page_size=25)

    overall = {
        row["prompt_strategy"]: row
        for row in preview["result_tables"]["overall"]["rows"]
    }
    assert overall["zero-shot"]["total_samples"] == 2
    assert overall["zero-shot"]["compiled_count"] == 1
    assert overall["zero-shot"]["executed_count"] == 1
    assert overall["zero-shot"]["target_passed_count"] == 1
    assert overall["few-shot"]["total_samples"] == 2
    assert overall["few-shot"]["target_passed_count"] == 2
    assert overall["zero-shot-project-aware"]["total_samples"] == 1

    by_model = preview["result_tables"]["by_model"]
    assert by_model["columns"] == rq1_export.MODEL_RESULT_TABLE_COLUMNS
    assert len(by_model["rows"]) == 6
    model_b_zero = next(
        row
        for row in by_model["rows"]
        if row["agent_name"] == "agent-b" and row["prompt_strategy"] == "zero-shot"
    )
    assert model_b_zero["model"] == "model-b"
    assert model_b_zero["total_samples"] == 1
    assert model_b_zero["compiled_rate_pct"] == 0.0


def test_snapshot_retries_once_when_source_revision_changes(monkeypatch, tmp_path):
    runs_dir = tmp_path / "runs"
    _write_jsonl(runs_dir, [_row("zero-shot")])
    revisions = iter(["before-1", "after-1", "stable", "stable"])
    monkeypatch.setattr(rq1_export, "source_revision", lambda _paths, _runs: next(revisions))

    snapshot = load_rq1_snapshot(runs_dir, retries=1)

    assert snapshot.source_revision == "stable"


def test_snapshot_fails_when_source_keeps_changing(monkeypatch, tmp_path):
    runs_dir = tmp_path / "runs"
    _write_jsonl(runs_dir, [_row("zero-shot")])
    revisions = iter(["a", "b", "c", "d"])
    monkeypatch.setattr(rq1_export, "source_revision", lambda _paths, _runs: next(revisions))

    with pytest.raises(RQ1SnapshotChangedError):
        load_rq1_snapshot(runs_dir, retries=1)


def test_workbook_has_exact_three_sheets_freeze_panes_and_filters(tmp_path):
    runs_dir = tmp_path / "runs"
    _write_jsonl(runs_dir, [_row(strategy) for strategy in rq1_export.RQ1_PROMPT_STRATEGIES])
    snapshot = load_rq1_snapshot(runs_dir)
    output = tmp_path / "rq1.xlsx"

    counts = write_rq1_workbook(output, snapshot)

    assert counts["Paired Samples"] == 2
    assert counts["Raw Details"] == 4
    with zipfile.ZipFile(output) as archive:
        workbook_xml = ElementTree.fromstring(archive.read("xl/workbook.xml"))
        namespace = {"x": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
        names = [sheet.attrib["name"] for sheet in workbook_xml.findall("x:sheets/x:sheet", namespace)]
        assert names == ["Summary", "Paired Samples", "Raw Details"]
        for sheet_name in ("xl/worksheets/sheet2.xml", "xl/worksheets/sheet3.xml"):
            sheet = ElementTree.fromstring(archive.read(sheet_name))
            assert sheet.find("x:sheetViews/x:sheetView/x:pane", namespace) is not None
            assert sheet.find("x:autoFilter", namespace) is not None


def test_workbook_rejects_no_data_and_excel_row_overflow(monkeypatch, tmp_path):
    empty = load_rq1_snapshot(tmp_path / "missing")
    with pytest.raises(RQ1NoDataError):
        write_rq1_workbook(tmp_path / "empty.xlsx", empty)

    runs_dir = tmp_path / "runs"
    _write_jsonl(runs_dir, [_row("zero-shot")])
    snapshot = load_rq1_snapshot(runs_dir)
    monkeypatch.setattr(rq1_export, "EXCEL_MAX_ROWS", 1)
    with pytest.raises(RQ1ExcelLimitError) as exc_info:
        write_rq1_workbook(tmp_path / "overflow.xlsx", snapshot)
    assert exc_info.value.sheet == "Paired Samples"
    assert exc_info.value.rows == 2


def test_save_workbook_reports_stale_preview_and_cleans_temporary_file(tmp_path):
    project_root = tmp_path / "project"
    runs_dir = project_root / "runs"
    _write_jsonl(runs_dir, [_row("zero-shot")])
    snapshot = load_rq1_snapshot(runs_dir)

    result = save_rq1_workbook(
        project_root / "export" / "RQ1",
        project_root,
        snapshot,
        preview_revision="older",
    )

    assert result["preview_was_stale"] is True
    assert result["warning"] == rq1_export.RQ1_NOT_READY_WARNING
    assert Path(result["path"]).is_file()
    assert not list((project_root / "export" / "RQ1").glob("*.tmp"))
