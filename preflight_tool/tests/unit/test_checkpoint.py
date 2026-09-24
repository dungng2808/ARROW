from __future__ import annotations

import json
from types import SimpleNamespace

import pytest

from preflight.checkpoint import CheckpointError, CheckpointStore, RunLock, RunLockError, write_progress
from preflight.runner import ToolConfig, run_all


def _candidate(task_id: str, repo: str = "repo"):
    return SimpleNamespace(task_id=task_id, repo_url=repo)


def _result(task_id: str, status: str = "ELIGIBLE") -> dict:
    return {"task_id": task_id, "preflight_status": status}


@pytest.mark.unit
def test_checkpoint_recovers_abandoned_and_preserves_completed_results(tmp_path):
    path = tmp_path / "state" / "run_state.sqlite"
    with CheckpointStore.create(path, {"name": "fixture"}, [_candidate("a"), _candidate("b")]) as store:
        first_session = store.begin_session(2)
        attempt_a = store.start_task("a", first_session)
        store.complete_task("a", attempt_a, _result("a"))
        assert store.start_task("b", first_session) == 1
        assert store.counts() == {"total": 2, "completed": 1, "running": 1, "pending": 0}

    with CheckpointStore.open(path) as store:
        assert store.recover_abandoned() == 1
        assert store.sessions()[0]["status"] == "ABANDONED"
        assert store.pending_task_ids() == ["b"]
        second_session = store.begin_session(1)
        attempt_b = store.start_task("b", second_session)
        assert attempt_b == 2
        store.complete_task("b", attempt_b, _result("b", "BUILD_TIMEOUT"))
        assert [row["task_id"] for row in store.results()] == ["a", "b"]
        assert store.status_counts() == {"BUILD_TIMEOUT": 1, "ELIGIBLE": 1}
        assert store.counts()["completed"] == 2


@pytest.mark.unit
def test_checkpoint_rejects_mismatched_result_and_corrupt_contract(tmp_path):
    path = tmp_path / "run.sqlite"
    with CheckpointStore.create(path, {"name": "fixture"}, [_candidate("a")]) as store:
        session = store.begin_session(1)
        attempt = store.start_task("a", session)
        with pytest.raises(CheckpointError, match="mismatch"):
            store.complete_task("a", attempt, _result("other"))
        store._connection.execute("UPDATE metadata SET value='{}' WHERE key='contract'")
        store._connection.commit()
        with pytest.raises(CheckpointError, match="digest"):
            _ = store.contract


@pytest.mark.unit
def test_progress_is_atomic_and_reports_partial_counts(tmp_path):
    path = tmp_path / "state.sqlite"
    with CheckpointStore.create(path, {"name": "fixture"}, [_candidate("a"), _candidate("b")]) as store:
        session = store.begin_session(1)
        attempt = store.start_task("a", session)
        store.complete_task("a", attempt, _result("a"))
        write_progress(tmp_path, store, "RUNNING", session)
    progress = json.loads((tmp_path / "reports" / "progress.json").read_text(encoding="utf-8"))
    assert progress["run_status"] == "RUNNING"
    assert progress["completed"] == 1 and progress["pending"] == 1
    assert progress["by_status"] == {"ELIGIBLE": 1}
    assert not (tmp_path / "reports" / "progress.json.tmp").exists()


@pytest.mark.unit
def test_run_lock_rejects_second_owner_and_releases_after_exit(tmp_path):
    path = tmp_path / "state" / "run.lock"
    with RunLock(path):
        with pytest.raises(RunLockError):
            with RunLock(path):
                pass
    with RunLock(path):
        assert path.is_file()


@pytest.mark.unit
def test_checkpointed_runner_commits_all_repo_tasks_before_cleanup(tmp_path, monkeypatch):
    candidates = [_candidate("a"), _candidate("b")]
    database = tmp_path / "state.sqlite"
    cleaned = []

    def fake_one(candidate, config, attempt):
        result = _result(candidate.task_id)
        return SimpleNamespace(task_id=candidate.task_id, to_dict=lambda: result)

    def fake_cleanup(repo_url, tasks, config, write_report=False):
        cleaned.append((repo_url, list(tasks)))
        return {"repo_url": repo_url, "status": "ABSENT"}

    monkeypatch.setattr("preflight.runner.preflight_one", fake_one)
    monkeypatch.setattr("preflight.runner._cleanup_repo", fake_cleanup)
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "index.sqlite", tmp_path / "cache" / "mirrors",
                        tmp_path / "workspaces", tmp_path / "logs", 2, 1, 1, False, "", {}, {})
    with CheckpointStore.create(database, {"name": "fixture"}, candidates) as store:
        session = store.begin_session(2)
        run_all(candidates, config, checkpoint=store, session_id=session)
        assert store.counts()["completed"] == 2
        assert cleaned == [("repo", ["a", "b"])]
        assert store.cleanup_events() == [{"repo_url": "repo", "status": "ABSENT"}]
