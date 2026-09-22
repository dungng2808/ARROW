from __future__ import annotations

import json
import os
import subprocess
from pathlib import Path

import pytest


def sample_payload(*, repo_url: str = "https://github.com/acme/example", class_name: str = "OrderService", class_path: str = "app/src/main/java/acme/OrderService.java", test_path: str = "app/src/test/java/acme/OrderServiceTest.java", focal_body: str = "public int total(int value) { return value; }", test_body: str = "void verifiesTotal() { }") -> dict:
    return {
        "repository": {"url": repo_url},
        "focal_class": {"identifier": class_name, "file": class_path},
        "test_class": {"identifier": f"{class_name}Test", "file": test_path},
        "focal_method": {"signature": "int total(int value)", "body": focal_body},
        "test_case": {"signature": "void verifiesTotal()", "body": test_body},
    }


@pytest.fixture
def dataset_factory(tmp_path: Path):
    def create(records: list[dict | str | bytes], project: str = "42") -> Path:
        root = tmp_path / "dữ liệu test" / "dataset" / project
        root.mkdir(parents=True, exist_ok=True)
        for index, record in enumerate(records):
            path = root / f"{project}_{index}.json"
            if isinstance(record, bytes):
                path.write_bytes(record)
            elif isinstance(record, str):
                path.write_text(record, encoding="utf-8")
            else:
                path.write_text(json.dumps(record, ensure_ascii=False), encoding="utf-8")
        return root.parent
    return create


@pytest.fixture
def git_repo_factory(tmp_path: Path):
    def create(*, focal_source: str | None = None, test_source: str | None = None, with_pom: bool = True) -> Path:
        repo = tmp_path / "repo"; (repo / "src/main/java/acme").mkdir(parents=True); (repo / "src/test/java/acme").mkdir(parents=True)
        if with_pom:
            (repo / "pom.xml").write_text("""<project xmlns=\"http://maven.apache.org/POM/4.0.0\"><modelVersion>4.0.0</modelVersion><groupId>acme</groupId><artifactId>fixture</artifactId><version>1</version></project>""", encoding="utf-8")
        (repo / "src/main/java/acme/Thing.java").write_text(focal_source or "package acme; public class Thing { public int total(int value) { return value; } }", encoding="utf-8")
        (repo / "src/test/java/acme/ThingTest.java").write_text(test_source or "package acme; class ThingTest { void verifiesTotal() { new Thing().total(1); } }", encoding="utf-8")
        env = {**os.environ, "GIT_AUTHOR_DATE": "2020-01-01T00:00:00+00:00", "GIT_COMMITTER_DATE": "2020-01-01T00:00:00+00:00"}
        for command in (["git", "init"], ["git", "config", "user.email", "test@example.test"], ["git", "config", "user.name", "test"], ["git", "add", "."], ["git", "commit", "-m", "fixture"]):
            subprocess.run(command, cwd=repo, env=env, check=True, capture_output=True)
        return repo
    return create


def pytest_collection_modifyitems(config, items):
    if os.environ.get("RUN_PERFORMANCE") != "1":
        skip = pytest.mark.skip(reason="set RUN_PERFORMANCE=1 to run performance tests")
        for item in items:
            if "performance" in item.keywords:
                item.add_marker(skip)
