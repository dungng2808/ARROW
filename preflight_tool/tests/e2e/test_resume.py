from __future__ import annotations

import json
import sys

import pytest

from preflight import cli
from preflight.models import PreflightResult, PreflightStatus
from tests.conftest import sample_payload


def _result(candidate, status=PreflightStatus.ELIGIBLE):
    return PreflightResult(
        task_id=candidate.task_id,
        source_json_path=candidate.source_json_path,
        source_json_sha256=candidate.source_json_sha256,
        source_json_paths=list(candidate.source_json_paths),
        source_json_sha256s=list(candidate.source_json_sha256s),
        repo_url=candidate.repo_url,
        class_path=candidate.class_path,
        class_fqn=candidate.class_fqn,
        test_class_path=candidate.test_class_paths[0],
        preflight_status=status,
        technical_eligible=status == PreflightStatus.ELIGIBLE,
    )


@pytest.mark.e2e
def test_cli_interrupt_then_resume_runs_only_unfinished_task(dataset_factory, tmp_path, monkeypatch):
    dataset = dataset_factory([
        sample_payload(class_name="One"),
        sample_payload(class_name="Two", class_path="app/src/main/java/acme/Two.java", test_path="app/src/test/java/acme/TwoTest.java"),
    ])
    output = tmp_path / "resumable"
    first_completed = []

    def interrupted_run(selected, config, checkpoint, session_id, on_checkpoint):
        candidate = selected[0]
        attempt = checkpoint.start_task(candidate.task_id, session_id)
        checkpoint.complete_task(candidate.task_id, attempt, _result(candidate).to_dict())
        first_completed.append(candidate.task_id)
        on_checkpoint()
        config.cancel_event.set()
        return []

    monkeypatch.setattr(cli, "run_all", interrupted_run)
    monkeypatch.setattr(sys, "argv", ["preflight", "--input-root", str(dataset), "--output-dir", str(output), "--workers", "2"])
    with pytest.raises(SystemExit) as interrupted:
        cli.main()
    assert interrupted.value.code == 130
    partial = json.loads((output / "reports/progress.json").read_text(encoding="utf-8"))
    assert partial["completed"] == 1 and partial["pending"] == 1
    assert not (output / "reports/preflight_results.jsonl").exists()

    resumed = []

    def resumed_run(selected, config, checkpoint, session_id, on_checkpoint):
        resumed.extend(candidate.task_id for candidate in selected)
        assert config.workers == 3
        for candidate in selected:
            attempt = checkpoint.start_task(candidate.task_id, session_id)
            checkpoint.complete_task(candidate.task_id, attempt, _result(candidate, PreflightStatus.BUILD_TIMEOUT).to_dict())
            on_checkpoint()
        return []

    monkeypatch.setattr(cli, "run_all", resumed_run)
    monkeypatch.setattr(sys, "argv", ["preflight", "--resume", "--output-dir", str(output), "--workers", "3"])
    cli.main()

    assert len(first_completed) == len(resumed) == 1
    assert first_completed[0] != resumed[0]
    rows = [json.loads(line) for line in (output / "reports/preflight_results.jsonl").read_text(encoding="utf-8").splitlines()]
    assert len(rows) == len({row["task_id"] for row in rows}) == 2
    assert {row["preflight_status"] for row in rows} == {"ELIGIBLE", "BUILD_TIMEOUT"}
    progress = json.loads((output / "reports/progress.json").read_text(encoding="utf-8"))
    provenance = json.loads((output / "provenance.json").read_text(encoding="utf-8"))
    assert progress["run_status"] == "COMPLETED" and progress["completed"] == 2
    assert provenance["resume_count"] == 1
    assert [session["status"] for session in provenance["sessions"]] == ["INTERRUPTED", "COMPLETED"]


@pytest.mark.e2e
def test_resume_rejects_non_runtime_options(tmp_path, monkeypatch):
    monkeypatch.setattr(sys, "argv", ["preflight", "--resume", "--output-dir", str(tmp_path), "--fast"])
    with pytest.raises(SystemExit) as exc:
        cli.main()
    assert exc.value.code == 2
