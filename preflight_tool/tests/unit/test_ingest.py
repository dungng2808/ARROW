from __future__ import annotations

import json

import pytest

from preflight.ingest import build_index, candidates, input_rejections
from preflight.util import sha256_file, stable_task_id
from tests.conftest import sample_payload


@pytest.mark.unit
def test_valid_bom_json_hash_and_unicode_path_are_indexed(dataset_factory):
    payload = sample_payload()
    dataset = dataset_factory([b"\xef\xbb\xbf" + json.dumps(payload).encode("utf-8")])
    database = dataset.parent / "index.sqlite"
    assert build_index(dataset, database) == (1, 1)
    item = next(candidates(database))
    assert item.source_json_sha256 == sha256_file(dataset / "42" / "42_0.json")
    assert "dữ liệu test" not in item.source_json_path
    assert input_rejections(database) == []


@pytest.mark.unit
def test_invalid_json_and_missing_required_fields_are_reported(dataset_factory):
    dataset = dataset_factory(["{bad json", {"repository": {"url": "x"}}])
    database = dataset.parent / "index.sqlite"
    assert build_index(dataset, database) == (2, 0)
    assert [item["reason_code"] for item in input_rejections(database)] == ["INPUT_JSON_INVALID", "INPUT_SCHEMA_INVALID"]


@pytest.mark.unit
def test_same_class_deduplicates_but_different_path_does_not(dataset_factory):
    first = sample_payload(test_body="void one() {}")
    second = sample_payload(test_body="void two() {}")
    other = sample_payload(class_path="other/src/main/java/acme/OrderService.java")
    dataset = dataset_factory([first, second, other]); database = dataset.parent / "index.sqlite"
    assert build_index(dataset, database) == (3, 2)
    items = list(candidates(database))
    assert sorted(len(item.source_json_paths) for item in items) == [1, 2]


@pytest.mark.unit
def test_task_id_is_stable_and_sensitive_to_identity():
    value = stable_task_id("1", "url", "a/X.java", "X")
    assert value == stable_task_id("1", "url", "a/X.java", "X")
    assert value != stable_task_id("1", "url", "b/X.java", "X")
