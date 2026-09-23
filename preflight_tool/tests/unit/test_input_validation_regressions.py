"""Regression coverage for F-01..04 in TEST_REPORT_20260923.md."""
import csv
import json
import sys

import pytest

from preflight.ingest import build_index, candidates, input_rejections
from preflight import cli
from preflight.revision import load_revision_map
from tests.conftest import sample_payload


@pytest.mark.unit
@pytest.mark.parametrize("field", ["focal_class", "test_class"])
@pytest.mark.parametrize("unsafe", [
    "/src/main/java/Thing.java", r"\\server\share\Thing.java",
    "//server/share/Thing.java", r"C:\src\Thing.java", "C:/src/Thing.java",
    "C:Thing.java", r"\src\Thing.java", r"\\?\C:\Thing.java",
    r"\\.\C:\Thing.java", "../Thing.java", r"src\..\Thing.java", "src/\x00Thing.java",
])
def test_unsafe_raw_paths_rejected_before_candidate_creation(dataset_factory, field, unsafe):
    bad = sample_payload()
    bad[field]["file"] = unsafe
    dataset = dataset_factory([bad, sample_payload(class_name="Safe")])
    database = dataset.parent / "index.sqlite"
    assert build_index(dataset, database) == (2, 1)
    assert [item.class_name for item in candidates(database)] == ["Safe"]
    assert input_rejections(database) == [{"source_json_path": "42/42_0.json", "reason_code": "INPUT_PATH_INVALID"}]


@pytest.mark.unit
@pytest.mark.parametrize("separator", ["/", "\\"])
def test_relative_paths_remain_accepted(dataset_factory, separator):
    payload = sample_payload()
    for field in ("focal_class", "test_class"):
        payload[field]["file"] = payload[field]["file"].replace("/", separator)
    dataset = dataset_factory([payload])
    database = dataset.parent / "index.sqlite"
    assert build_index(dataset, database) == (1, 1)
    item = next(candidates(database))
    assert item.class_path == sample_payload()["focal_class"]["file"]
    assert item.test_class_paths == (sample_payload()["test_class"]["file"],)


def entry(sha="a" * 40):
    return {"task_id": "fixture", "checkout_sha": sha,
            "revision_verification_status": "UPSTREAM_PINNED",
            "revision_provenance": "dataset_record", "evidence_ref": "fixture.json"}


def write_map(tmp_path, extension, rows):
    path = tmp_path / f"revisions.{extension}"
    if extension == "jsonl":
        path.write_text("\n".join(json.dumps(row) for row in rows), encoding="utf-8")
    else:
        with path.open("w", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
            writer.writeheader()
            writer.writerows(rows)
    return path


@pytest.mark.unit
@pytest.mark.parametrize("extension", ["jsonl", "csv"])
@pytest.mark.parametrize("sha", ["z" * 40, "a" * 39, "a" * 41, "a" * 39 + "\n", "ａ" * 40])
def test_revision_sha_must_be_exactly_40_ascii_hex(tmp_path, extension, sha):
    with pytest.raises(ValueError):
        load_revision_map(write_map(tmp_path, extension, [entry(sha)]))


@pytest.mark.unit
@pytest.mark.parametrize("extension", ["jsonl", "csv"])
@pytest.mark.parametrize("change", ["checkout_sha", "evidence_ref", "revision_provenance"])
def test_conflicting_duplicate_revision_rejected(tmp_path, extension, change):
    first, second = entry(), entry()
    second[change] = {"checkout_sha": "b" * 40, "evidence_ref": "different.json", "revision_provenance": "upstream_metadata"}[change]
    with pytest.raises(ValueError, match="conflicting"):
        load_revision_map(write_map(tmp_path, extension, [first, second]))


@pytest.mark.unit
@pytest.mark.parametrize("extension", ["jsonl", "csv"])
def test_identical_duplicate_and_uppercase_sha_are_safe(tmp_path, extension):
    result = load_revision_map(write_map(tmp_path, extension, [entry("ABCDEF0123" * 4), entry("abcdef0123" * 4)]))
    assert len(result) == 1
    assert result["fixture"].checkout_sha == "abcdef0123" * 4


@pytest.mark.e2e
@pytest.mark.parametrize("unsafe", ["/src/main/java/Thing.java", r"\\server\share\Thing.java"])
def test_cli_unsafe_input_never_reaches_runner(dataset_factory, tmp_path, monkeypatch, unsafe):
    payload = sample_payload()
    payload["focal_class"]["file"] = unsafe
    dataset = dataset_factory([payload])
    output = tmp_path / "output"
    received = []
    def fake_run(selected, config):
        received.extend(selected)
        return []
    monkeypatch.setattr(cli, "run_all", fake_run)
    monkeypatch.setattr(sys, "argv", ["preflight", "--input-root", str(dataset), "--output-dir", str(output)])
    cli.main()
    assert received == []
    rejected = json.loads((output / "reports/input_rejections.jsonl").read_text())
    assert rejected["reason_code"] == "INPUT_PATH_INVALID"


@pytest.mark.e2e
@pytest.mark.parametrize("rows", [[entry("z" * 40)], [entry(), entry("b" * 40)]])
def test_cli_invalid_map_stops_before_runner(dataset_factory, tmp_path, monkeypatch, rows):
    dataset = dataset_factory([sample_payload()])
    path = write_map(tmp_path, "jsonl", rows)
    def forbidden_run(*args):
        pytest.fail("Invalid revision map must stop before runner")
    monkeypatch.setattr(cli, "run_all", forbidden_run)
    monkeypatch.setattr(sys, "argv", ["preflight", "--input-root", str(dataset), "--output-dir", str(tmp_path / "output"), "--revision-map", str(path)])
    with pytest.raises(ValueError):
        cli.main()
