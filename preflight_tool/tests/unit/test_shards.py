import json
import sys

import pytest

from preflight import cli
from preflight.ingest import build_index, candidates
from preflight.shards import assignment_record, select_shard
from tests.conftest import sample_payload


@pytest.fixture
def shard_fixture(dataset_factory, tmp_path):
    dataset = dataset_factory([sample_payload(class_name="One"), sample_payload(class_name="Two")])
    database = tmp_path / "source.sqlite"
    build_index(dataset, database)
    items = list(candidates(database))
    data = {"schema_version": 1, "shard_id": "shard-01", "class_count": 1, "classes": [assignment_record(items[1])]}
    path = tmp_path / "shard.json"
    path.write_text(json.dumps(data))
    return dataset, items, data, path


@pytest.mark.unit
def test_select_exact_assignment(shard_fixture):
    _, items, _, path = shard_fixture
    selected, info = select_shard(items, path)
    assert selected == [items[1]]
    assert info == {"shard_id": "shard-01", "shard_class_count": 1}


@pytest.mark.unit
@pytest.mark.parametrize("mutation", ["duplicate", "missing", "hash", "metadata", "count", "schema", "empty"])
def test_invalid_shard_rejected(shard_fixture, mutation):
    _, items, data, path = shard_fixture
    if mutation == "duplicate":
        data["classes"] *= 2
        data["class_count"] = 2
    elif mutation == "missing":
        data["classes"][0]["task_id"] = "absent"
    elif mutation == "hash":
        data["classes"][0]["evidence_sha256"] = "0" * 64
    elif mutation == "metadata":
        data["classes"][0]["class_path"] = "Other.java"
    elif mutation == "count":
        data["class_count"] = 999
    elif mutation == "schema":
        data["schema_version"] = 2
    else:
        data["classes"] = []
        data["class_count"] = 0
    path.write_text(json.dumps(data))
    with pytest.raises(ValueError):
        select_shard(items, path)


@pytest.mark.e2e
def test_cli_shard_only_sends_assigned_class_to_runner(shard_fixture, tmp_path, monkeypatch):
    dataset, items, _, path = shard_fixture
    output = tmp_path / "output"
    received = []
    def fake_run(run_root, store, selected, config):
        received.extend(selected)
    monkeypatch.setattr(cli, "_run_checkpointed", fake_run)
    monkeypatch.setattr(sys, "argv", ["preflight", "--input-root", str(dataset), "--output-dir", str(output), "--shard", str(path)])
    cli.main()
    assert received == [items[1]]
    provenance = json.loads((output / "provenance.json").read_text())
    assert provenance["classes_selected"] == 1
    assert provenance["deduplicated_classes"] == 2
    assert provenance["shard_id"] == "shard-01"
    assert len(provenance["shard_sha256"]) == 64


@pytest.mark.e2e
@pytest.mark.parametrize("option", ["--limit", "--max-classes"])
def test_cli_shard_forbids_partial_selection(shard_fixture, monkeypatch, option):
    _, _, _, path = shard_fixture
    monkeypatch.setattr(sys, "argv", ["preflight", "--shard", str(path), option, "1"])
    with pytest.raises(SystemExit) as exc:
        cli.main()
    assert exc.value.code == 2


@pytest.mark.unit
def test_changed_raw_json_fails_evidence_validation(shard_fixture, tmp_path):
    dataset, items, _, path = shard_fixture
    raw = dataset / items[1].source_json_path
    raw.write_bytes(raw.read_bytes() + b"\n")
    database = tmp_path / "changed.sqlite"
    build_index(dataset, database)
    with pytest.raises(ValueError, match="evidence mismatch"):
        select_shard(list(candidates(database)), path)
