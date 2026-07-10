from __future__ import annotations

import pytest
from pathlib import Path

from src.models import SampleInput
from src.output_paths import parse_repo_identity, resolve_output_paths, sanitize_folder_name


def sample(project_id: str = "100", url: str = "https://github.com/acme/demo-repo") -> SampleInput:
    return SampleInput(
        project_id=project_id,
        sample_file=Path(__file__),
        repository_url=url,
        focal_class_name="Foo",
        focal_class_path="src/main/java/Foo.java",
        test_class_name="FooTest",
        test_class_path="src/test/java/FooTest.java",
        raw={},
    )


def test_parse_repo_name_from_github_url():
    identity = parse_repo_identity("https://github.com/habernal/semeval2018-task12", "100")
    assert identity.owner == "habernal"
    assert identity.name == "semeval2018-task12"
    assert identity.folder == "semeval2018-task12"


def test_sanitize_folder_name_for_windows():
    assert sanitize_folder_name("bad:repo/name?") == "bad-repo-name"


def test_repo_sample_output_path(tmp_path):
    paths = resolve_output_paths(
        tmp_path,
        sample(project_id="100035986", url="https://github.com/habernal/semeval2018-task12"),
        {"output": {"root_dir": "runs", "layout": "repo_sample", "repo_folder_name": "repo_name"}},
        "run",
        "local",
    )
    assert paths.sample_root == tmp_path / "runs" / "semeval2018-task12" / "test_output_paths"
    assert paths.reports_dir == paths.sample_root / "reports"


def test_repo_name_conflict_can_append_project_id(tmp_path):
    config = {"output": {"root_dir": "runs", "layout": "repo_sample", "repo_folder_name": "repo_name", "on_repo_name_conflict": "append_project_id"}}
    resolve_output_paths(tmp_path, sample(project_id="1", url="https://github.com/a/common"), config, "run", "local")
    paths = resolve_output_paths(tmp_path, sample(project_id="2", url="https://github.com/b/common"), config, "run", "local")
    assert paths.repo_identity.folder == "common_2"


def test_repo_name_conflict_fails_by_default(tmp_path):
    config = {"output": {"root_dir": "runs", "layout": "repo_sample", "repo_folder_name": "repo_name", "on_repo_name_conflict": "fail"}}
    resolve_output_paths(tmp_path, sample(project_id="1", url="https://github.com/a/common"), config, "run", "local")
    with pytest.raises(ValueError):
        resolve_output_paths(tmp_path, sample(project_id="2", url="https://github.com/b/common"), config, "run", "local")
