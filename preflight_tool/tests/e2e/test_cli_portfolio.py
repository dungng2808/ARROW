from __future__ import annotations

import json
import sys

import jsonschema
import pytest

from preflight import cli
from preflight.models import PreflightResult, PreflightStatus, RevisionStatus, RunMode
from tests.conftest import sample_payload


@pytest.mark.e2e
def test_cli_writes_all_contract_outputs(dataset_factory, tmp_path, monkeypatch):
    dataset = dataset_factory([sample_payload()]); output = tmp_path / "kết quả có khoảng trắng"

    def simulated_run(candidates, config):
        candidate = candidates[0]
        return [PreflightResult(
            task_id=candidate.task_id, source_json_path=candidate.source_json_path, source_json_sha256=candidate.source_json_sha256,
            source_json_paths=list(candidate.source_json_paths), source_json_sha256s=list(candidate.source_json_sha256s), repo_url=candidate.repo_url,
            checkout_sha="a" * 40, revision_provenance="dataset_record", revision_verification_status=RevisionStatus.UPSTREAM_PINNED,
            run_mode=RunMode.STRICT, class_path=candidate.class_path, class_fqn=candidate.class_fqn, test_class_path=candidate.test_class_paths[0],
            module_path=".", preflight_status=PreflightStatus.ELIGIBLE, technical_eligible=True, strict_eligible=True,
        )]

    monkeypatch.setattr(cli, "run_all", simulated_run)
    monkeypatch.setattr(sys, "argv", ["preflight", "--input-root", str(dataset), "--output-dir", str(output)])
    cli.main()
    reports, manifests = output / "reports", output / "manifests"
    for path in [reports / "preflight_results.jsonl", reports / "preflight_results.csv", reports / "summary.json", manifests / "locked_input_manifest.jsonl", manifests / "technical_eligible_manifest.jsonl", manifests / "strict_eligible_manifest.jsonl"]:
        assert path.is_file(), path
    strict = json.loads((manifests / "strict_eligible_manifest.jsonl").read_text(encoding="utf-8"))
    schema = json.loads((cli.ROOT / "schemas" / "preflight_result.schema.json").read_text(encoding="utf-8"))
    jsonschema.validate(strict, schema)
    assert json.loads((reports / "summary.json").read_text(encoding="utf-8"))["strict_eligible"] == 1


@pytest.mark.e2e
def test_cli_index_only_and_refuses_existing_evidence(dataset_factory, tmp_path, monkeypatch):
    dataset = dataset_factory([sample_payload()]); output = tmp_path / "out"
    monkeypatch.setattr(sys, "argv", ["preflight", "--input-root", str(dataset), "--output-dir", str(output), "--index-only"])
    cli.main()
    monkeypatch.setattr(sys, "argv", ["preflight", "--input-root", str(dataset), "--output-dir", str(output), "--index-only"])
    with pytest.raises(SystemExit, match="Output directory"):
        cli.main()
