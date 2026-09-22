from __future__ import annotations

import csv
import json
import warnings
from pathlib import Path

import jsonschema
import pytest

from preflight.cli import _write_csv, _write_jsonl
from preflight.models import BuildAttempt, PreflightResult, PreflightStatus, RevisionStatus, RunMode


def _record() -> dict:
    item = PreflightResult(
        task_id="task-1", source_json_path="42/a.json", source_json_sha256="a" * 64,
        source_json_paths=["42/a.json"], source_json_sha256s=["a" * 64], repo_url="https://example.test/repo.git",
        checkout_sha="b" * 40, revision_provenance="dataset_record", revision_verification_status=RevisionStatus.UPSTREAM_PINNED,
        run_mode=RunMode.STRICT, class_path="src/main/java/x/X.java", class_fqn="x.X", preflight_status=PreflightStatus.ELIGIBLE,
        technical_eligible=True, strict_eligible=True,
        build_attempts=[BuildAttempt("main_compile", "work", ["mvn", "compile"], 0, False, 0.1, "log.txt")],
    )
    return item.to_dict()


@pytest.mark.unit
def test_jsonl_csv_round_trip_and_nested_data(tmp_path):
    record = _record(); jsonl = tmp_path / "result.jsonl"; csv_path = tmp_path / "result.csv"
    _write_jsonl(jsonl, [record]); _write_csv(csv_path, [record])
    assert json.loads(jsonl.read_text(encoding="utf-8")) == record
    row = next(csv.DictReader(csv_path.open(encoding="utf-8")))
    assert json.loads(row["build_attempts"])[0]["command"] == ["mvn", "compile"]


@pytest.mark.unit
def test_preflight_and_strict_schemas_accept_contract_record():
    root = Path(__file__).parents[2] / "schemas"
    result_schema = json.loads((root / "preflight_result.schema.json").read_text(encoding="utf-8"))
    strict_schema = json.loads((root / "strict_manifest.schema.json").read_text(encoding="utf-8"))
    record = _record()
    jsonschema.validate(record, result_schema)
    with warnings.catch_warnings():
        warnings.simplefilter("ignore", DeprecationWarning)
        resolver = jsonschema.RefResolver(base_uri=(root / "strict_manifest.schema.json").as_uri(), referrer=strict_schema)
    jsonschema.Draft202012Validator(strict_schema, resolver=resolver).validate(record)


@pytest.mark.unit
def test_output_is_stably_key_sorted_and_does_not_overwrite_content(tmp_path):
    path = tmp_path / "ordered.jsonl"; _write_jsonl(path, [{"z": 1, "a": 2}])
    first = path.read_bytes(); _write_jsonl(path, [{"z": 1, "a": 2}])
    assert first == path.read_bytes() == b'{"a": 2, "z": 1}\n'
