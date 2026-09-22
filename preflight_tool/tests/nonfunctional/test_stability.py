from __future__ import annotations

import os
import time

import psutil
import pytest

from preflight.ingest import build_index, candidates
from tests.conftest import sample_payload


@pytest.mark.performance
def test_index_10000_json_within_windows_ci_budget(dataset_factory, tmp_path):
    dataset = dataset_factory([sample_payload(test_body=f"void t{index}() {{}}") for index in range(10_000)])
    database = tmp_path / "index.sqlite"; process = psutil.Process(); before = process.memory_info().rss; started = time.monotonic()
    raw, selected = build_index(dataset, database)
    elapsed = time.monotonic() - started; peak_delta = process.memory_info().rss - before
    assert (raw, selected) == (10_000, 1)
    assert elapsed <= 120
    assert peak_delta < 512 * 1024 * 1024


@pytest.mark.e2e
def test_index_is_deterministic_for_twenty_runs(dataset_factory, tmp_path):
    dataset = dataset_factory([sample_payload(), sample_payload(class_path="b/src/main/java/acme/OrderService.java")])
    expected = None
    for index in range(20):
        database = tmp_path / f"index-{index}.sqlite"; build_index(dataset, database)
        actual = [(item.task_id, item.class_path, item.source_json_sha256s) for item in candidates(database)]
        assert expected is None or actual == expected
        expected = actual
