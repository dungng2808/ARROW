from __future__ import annotations

import json

from src.input_selector import select_inputs


def write_sample(dataset, project, name):
    path = dataset / project
    path.mkdir(parents=True, exist_ok=True)
    sample = {
        "repository": {"url": f"https://example.com/{project}"},
        "focal_class": {"identifier": "Foo", "file": "src/main/java/demo/Foo.java"},
        "test_class": {"identifier": "FooTest", "file": "src/test/java/demo/FooTest.java"},
    }
    (path / name).write_text(json.dumps(sample), encoding="utf-8")


def test_sample_mode_limit(tmp_path):
    write_sample(tmp_path, "p1", "a.json")
    write_sample(tmp_path, "p2", "b.json")
    config = {"input": {"mode": "sample", "samples_per_project": 1, "project_ids": []}}
    selected = select_inputs(tmp_path, config, start_index=0, limit=1)
    assert len(selected) == 1
    assert selected[0].project_id == "p1"


def test_limit_zero_means_all_remaining_inputs(tmp_path):
    write_sample(tmp_path, "p1", "a.json")
    write_sample(tmp_path, "p2", "b.json")
    write_sample(tmp_path, "p3", "c.json")
    config = {"input": {"mode": "sample", "samples_per_project": 1, "project_ids": []}}
    selected = select_inputs(tmp_path, config, start_index=1, limit=0)
    assert [item.project_id for item in selected] == ["p2", "p3"]


def test_project_mode_samples_per_project_all(tmp_path):
    write_sample(tmp_path, "p1", "a.json")
    write_sample(tmp_path, "p1", "b.json")
    config = {"input": {"mode": "project", "samples_per_project": "all", "project_ids": []}}
    selected = select_inputs(tmp_path, config, start_index=0, limit=10)
    assert [item.sample_file.name for item in selected] == ["a.json", "b.json"]


def test_project_shard_file(tmp_path):
    write_sample(tmp_path, "p1", "a.json")
    write_sample(tmp_path, "p2", "b.json")
    shard = tmp_path / "shard.txt"
    shard.write_text("p2\n", encoding="utf-8")
    config = {"input": {"mode": "sample", "samples_per_project": 1, "project_ids": []}}
    selected = select_inputs(tmp_path, config, start_index=0, limit=10, repo_shard=shard)
    assert [item.project_id for item in selected] == ["p2"]
