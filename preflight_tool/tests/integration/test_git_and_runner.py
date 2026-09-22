from __future__ import annotations

import subprocess
from pathlib import Path

import pytest

from preflight.ingest import build_index, candidates
from preflight.models import BuildAttempt, PreflightStatus
from preflight.runner import ToolConfig, preflight_one, run_all
from tests.conftest import sample_payload


def _config(tmp_path: Path, dataset: Path, database: Path, *, fast: bool = False) -> ToolConfig:
    return ToolConfig(tmp_path / "run", dataset, database, tmp_path / "cache", tmp_path / "workspaces", tmp_path / "logs", 2, 30, 30, False, "", {}, {}, fast)


@pytest.mark.integration
def test_local_git_mirror_worktree_is_cleaned_without_touching_source(dataset_factory, git_repo_factory, tmp_path):
    repo = git_repo_factory(with_pom=False)
    payload = sample_payload(repo_url=str(repo), class_name="Thing", class_path="src/main/java/acme/Thing.java", test_path="src/test/java/acme/ThingTest.java", test_body="void verifiesTotal() { new Thing().total(1); }")
    dataset = dataset_factory([payload]); database = tmp_path / "index.sqlite"; build_index(dataset, database)
    candidate = next(candidates(database)); config = _config(tmp_path, dataset, database)
    result = preflight_one(candidate, config)
    assert result.preflight_status == PreflightStatus.BUILD_TOOL_UNSUPPORTED
    assert result.revision_verification_status == "CONTENT_MATCHED"
    assert not (tmp_path / "workspaces" / candidate.task_id).exists()
    assert subprocess.run(["git", "status", "--porcelain"], cwd=repo, capture_output=True, text=True, check=True).stdout == ""


@pytest.mark.integration
def test_fast_run_is_prechecked_and_never_strict(dataset_factory, git_repo_factory, tmp_path, monkeypatch):
    repo = git_repo_factory()
    payload = sample_payload(repo_url=str(repo), class_name="Thing", class_path="src/main/java/acme/Thing.java", test_path="src/test/java/acme/ThingTest.java")
    dataset = dataset_factory([payload]); database = tmp_path / "index.sqlite"; build_index(dataset, database)
    candidate = next(candidates(database)); config = _config(tmp_path, dataset, database, fast=True)

    def successful_run(command, cwd, timeout_seconds, log_path, env=None):
        log_path.parent.mkdir(parents=True, exist_ok=True); log_path.write_text("Apache Maven\nversion\n3.9", encoding="utf-8")
        return BuildAttempt("", str(cwd), command, 0, False, 0.01, str(log_path))

    monkeypatch.setattr("preflight.runner._run", successful_run)
    result = preflight_one(candidate, config)
    assert result.preflight_status == PreflightStatus.PRECHECKED
    assert result.probe_status == "NOT_RUN"
    assert not result.technical_eligible and not result.strict_eligible


@pytest.mark.integration
def test_parallel_workers_have_stable_nonduplicated_results(dataset_factory, git_repo_factory, tmp_path, monkeypatch):
    repo = git_repo_factory(with_pom=False)
    (repo / "src/main/java/acme/Other.java").write_text("package acme; public class Other { public int total(int value) { return value; } }", encoding="utf-8")
    (repo / "src/test/java/acme/OtherTest.java").write_text("package acme; class OtherTest { void verifiesTotal() { new Other().total(1); } }", encoding="utf-8")
    subprocess.run(["git", "add", "."], cwd=repo, check=True, capture_output=True)
    subprocess.run(["git", "commit", "-m", "add second CUT"], cwd=repo, check=True, capture_output=True)
    first = sample_payload(repo_url=str(repo), class_name="Thing", class_path="src/main/java/acme/Thing.java", test_path="src/test/java/acme/ThingTest.java")
    second = sample_payload(repo_url=str(repo), class_name="Other", class_path="src/main/java/acme/Other.java", test_path="src/test/java/acme/OtherTest.java", test_body="void verifiesTotal() { new Other().total(1); }")
    dataset = dataset_factory([first, second]); database = tmp_path / "index.sqlite"; build_index(dataset, database)
    selected = list(candidates(database)); config = _config(tmp_path, dataset, database)
    one = run_all(selected, config)
    assert len(one) == len({result.task_id for result in one}) == 2
    assert one == sorted(one, key=lambda result: result.task_id)
    assert all(result.preflight_status == PreflightStatus.BUILD_TOOL_UNSUPPORTED for result in one)
    assert all(not (config.workspace_dir / result.task_id).exists() for result in one)
