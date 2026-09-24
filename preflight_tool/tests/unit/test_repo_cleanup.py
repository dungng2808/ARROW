import json
import threading
from collections import Counter
from dataclasses import replace
from types import SimpleNamespace

import pytest

from preflight import runner


@pytest.fixture
def config(tmp_path):
    root = tmp_path / "run"
    return runner.ToolConfig(root, tmp_path / "dataset", root / "index.sqlite",
                             root / "cache/mirrors", root / "workspaces", root / "logs",
                             3, 30, 30, False, "", {}, {})


def events(config):
    return [json.loads(line) for line in (config.run_root / "reports/repo_cleanup.jsonl").read_text(encoding="utf-8").splitlines()]


def mirror(config, repo="repo"):
    path = runner._mirror_path(repo, config)
    path.mkdir(parents=True)
    (path / "HEAD").write_text("fixture")
    return path


@pytest.mark.unit
@pytest.mark.parametrize("workers", [1, 3])
def test_cleanup_only_after_all_active_queued_and_unscheduled_classes_finish(config, monkeypatch, workers):
    config = replace(config, workers=workers)
    selected = [SimpleNamespace(task_id=str(i), repo_url=f"repo-{i // 7}") for i in range(21)]
    paths = {f"repo-{i}": mirror(config, f"repo-{i}") for i in range(3)}
    sentinel = config.log_dir / "evidence.log"
    sentinel.parent.mkdir(parents=True)
    sentinel.write_text("preserve")
    completed = Counter()
    lock = threading.Lock()
    barrier = threading.Barrier(3) if workers == 3 else None
    def fake_one(candidate, cfg):
        if barrier and int(candidate.task_id) < 3:
            barrier.wait(timeout=5)
        assert paths[candidate.repo_url].exists(), "Mirror deleted before last user completed"
        with lock:
            completed[candidate.repo_url] += 1
        return SimpleNamespace(task_id=candidate.task_id, preflight_status="CLONE_FAILED")
    original = runner._cleanup_repo
    def checked_cleanup(repo, tasks, cfg):
        assert completed[repo] == 7
        original(repo, tasks, cfg)
    monkeypatch.setattr(runner, "preflight_one", fake_one)
    monkeypatch.setattr(runner, "_cleanup_repo", checked_cleanup)
    assert len(runner.run_all(selected, config)) == 21
    assert not any(path.exists() for path in paths.values())
    assert len(events(config)) == 3
    assert {event["status"] for event in events(config)} == {"DELETED"}
    assert sentinel.read_text(encoding="utf-8") == "preserve"


@pytest.mark.unit
@pytest.mark.parametrize("flag", ["keep_repo_cache", "keep_workspaces"])
def test_explicit_preserve_options_keep_mirror(config, flag):
    config = replace(config, **{flag: True})
    path = mirror(config)
    runner._cleanup_repo("repo", ["a"], config)
    assert path.exists()
    assert events(config)[0]["status"] == "KEPT"
    assert events(config)[0]["reason"] == flag


@pytest.mark.unit
def test_remaining_workspace_keeps_mirror(config):
    path = mirror(config)
    (config.workspace_dir / "a").mkdir(parents=True)
    runner._cleanup_repo("repo", ["a"], config)
    assert path.exists()
    assert events(config)[0]["reason"] == "WORKSPACE_CLEANUP_INCOMPLETE"


@pytest.mark.unit
def test_missing_and_partial_clone_cleanup(config):
    runner._cleanup_repo("missing", ["a"], config)
    partial = mirror(config, "partial")
    runner._cleanup_repo("partial", ["b"], config)
    assert not partial.exists()
    assert [event["status"] for event in events(config)] == ["ABSENT", "DELETED"]


@pytest.mark.unit
def test_external_cache_is_never_deleted(config, tmp_path):
    config = replace(config, cache_dir=tmp_path / "external")
    path = mirror(config)
    runner._cleanup_repo("repo", ["a"], config)
    assert path.exists()
    assert events(config)[0]["status"] == "FAILED"
    assert "UNSAFE_CACHE_PATH" in events(config)[0]["error"]


@pytest.mark.unit
def test_symlink_mirror_is_never_followed(config, tmp_path):
    outside = tmp_path / "outside"
    outside.mkdir()
    sentinel = outside / "keep"
    sentinel.write_text("preserve")
    target = runner._mirror_path("repo", config)
    target.parent.mkdir(parents=True)
    try:
        target.symlink_to(outside, target_is_directory=True)
    except OSError:
        pytest.skip("Host does not permit directory symlink creation")
    runner._cleanup_repo("repo", ["a"], config)
    assert sentinel.read_text(encoding="utf-8") == "preserve"
    assert target.is_symlink()
    assert events(config)[0]["status"] == "FAILED"


@pytest.mark.unit
def test_deletion_failure_is_reported_without_losing_results(config, monkeypatch):
    path = mirror(config)
    def failed_delete(*args, **kwargs):
        raise PermissionError("locked file")
    monkeypatch.setattr(runner.shutil, "rmtree", failed_delete)
    monkeypatch.setattr(runner, "preflight_one", lambda c, cfg: SimpleNamespace(task_id=c.task_id))
    result = runner.run_all([SimpleNamespace(task_id="a", repo_url="repo")], config)
    assert len(result) == 1 and path.exists()
    assert events(config)[0]["status"] == "FAILED"


@pytest.mark.unit
def test_readonly_retry_callback(config, monkeypatch):
    path = mirror(config)
    actual_rmtree = runner.shutil.rmtree
    retried = []
    def simulate_readonly(target, onerror):
        onerror(lambda p: retried.append(p), str(target / "HEAD"), (PermissionError, PermissionError("read only"), None))
        actual_rmtree(target)
    monkeypatch.setattr(runner.shutil, "rmtree", simulate_readonly)
    runner._cleanup_repo("repo", ["a"], config)
    assert retried == [str(path / "HEAD")]
    assert events(config)[0]["status"] == "DELETED"
