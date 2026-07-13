from __future__ import annotations

import json
from argparse import Namespace

import pytest

from src.models import FailureOrigin, FailureState, GenerationStrategy, VerificationResult
from src.report_writer import load_experiment_jsonl
from src.run_pipeline import (
    _agents,
    _baseline_blocks_generation,
    _clear_experiment_records,
    _load_examples,
    _reset_experiment_dir,
    _write_experiment_records,
)


CONFIG = {
    "llm": {
        "api_base": "http://localhost:11434",
        "api_key_env": "DEFAULT_API_KEY",
        "agents": [
            {
                "name": "qwen-coder-1.5b",
                "model": "ollama/qwen2.5-coder:1.5b",
                "temperature": 0,
                "num_ctx": 32768,
                "max_tokens": 2048,
            }
        ],
    }
}


def test_agent_can_be_selected_by_model_suffix():
    agents = _agents(CONFIG, Namespace(agent=["qwen2.5-coder:1.5b"]))
    assert len(agents) == 1
    assert agents[0].name == "qwen-coder-1.5b"
    assert agents[0].api_key_env == "DEFAULT_API_KEY"


def test_agent_api_key_env_overrides_global_value():
    config = {
        "llm": {
            "api_key_env": "DEFAULT_API_KEY",
            "agents": [
                {
                    "name": "gpt",
                    "model": "openai/gpt-4.1-mini",
                    "api_key_env": "OPENAI_API_KEY",
                }
            ],
        }
    }
    agents = _agents(config, Namespace(agent=["gpt"]))
    assert agents[0].api_key_env == "OPENAI_API_KEY"


def test_unknown_agent_reports_available_aliases():
    with pytest.raises(ValueError, match="Available agent/model aliases"):
        _agents(CONFIG, Namespace(agent=["missing"]))


def test_build_configuration_baseline_failure_blocks_generation_even_when_parser_calls_it_assertion_failed():
    baseline = VerificationResult(
        state=FailureState.ASSERTION_FAILED,
        failure_origin=FailureOrigin.BUILD_CONFIGURATION,
        raw_output="JaCoCo agent failed and forked VM terminated",
    )

    assert _baseline_blocks_generation(baseline) is True


def test_load_examples_selects_only_matching_framework_and_limits_context(tmp_path):
    examples_path = tmp_path / "examples.json"
    examples_path.write_text(
        json.dumps(
            [
                {"id": "junit4-one", "testing_framework": "junit4"},
                {"id": "junit5-one", "testing_framework": "junit5"},
                {"id": "junit5-two", "testing_framework": "junit5"},
                {"id": "junit5-three", "testing_framework": "junit5"},
            ]
        ),
        encoding="utf-8",
    )
    strategy = GenerationStrategy(name="few-shot", template="few.txt", examples=str(examples_path))

    selected = _load_examples(strategy, testing_framework="junit5")

    assert [example["id"] for example in selected] == ["junit5-one", "junit5-two"]


def test_load_examples_does_not_mix_frameworks_when_detection_is_unknown(tmp_path):
    examples_path = tmp_path / "examples.json"
    examples_path.write_text(
        json.dumps(
            [
                {"id": "junit4", "testing_framework": "junit4"},
                {"id": "generic", "testing_framework": "any"},
            ]
        ),
        encoding="utf-8",
    )
    strategy = GenerationStrategy(name="few-shot", template="few.txt", examples=str(examples_path))

    selected = _load_examples(strategy, testing_framework="unknown")

    assert [example["id"] for example in selected] == ["generic"]


def test_reset_experiment_dir_removes_stale_artifacts_inside_sample_root(tmp_path):
    sample_root = tmp_path / "runs" / "project" / "sample"
    exp_dir = sample_root / "agent" / "few-shot"
    stale = exp_dir / "repair" / "checkpoints" / "attempt_1" / "target_verification.json"
    stale.parent.mkdir(parents=True)
    stale.write_text('{"state":"COMPILE_FAILED"}', encoding="utf-8")

    assert _reset_experiment_dir(exp_dir, sample_root) is True

    assert not exp_dir.exists()


def test_reset_experiment_dir_refuses_path_outside_sample_root(tmp_path):
    sample_root = tmp_path / "runs" / "project" / "sample"
    outside = tmp_path / "other" / "agent" / "few-shot"
    outside.mkdir(parents=True)

    with pytest.raises(ValueError):
        _reset_experiment_dir(outside, sample_root)


def test_write_experiment_records_replaces_stale_jsonl_result_for_rerun(tmp_path):
    report_dir = tmp_path / "reports"
    config = {"report": {"write_per_experiment_json": True, "write_shard_jsonl": True}}
    stale = {
        "run_id": "old-run",
        "shard_id": "repo_shard_05",
        "input_id": "101650961_7",
        "agent_name": "qwen-coder-2.5-7b",
        "generation_prompt_strategy": "few-shot",
        "reports_dir": str(report_dir),
        "test_passed": False,
        "final_failure_state": "TEST_DISCOVERY_FAILED",
    }
    fresh = {
        **stale,
        "run_id": "new-run",
        "test_passed": True,
        "final_failure_state": "MODULE_TESTS_PASSED",
    }

    _write_experiment_records(report_dir, stale, config)
    _write_experiment_records(report_dir, fresh, config)

    rows = load_experiment_jsonl([report_dir / "records" / "experiments.jsonl"])
    assert len(rows) == 1
    assert rows[0]["run_id"] == "new-run"
    assert rows[0]["test_passed"] is True


def test_clear_experiment_records_removes_stale_dashboard_result_before_rerun(tmp_path):
    report_dir = tmp_path / "reports"
    config = {"report": {"write_per_experiment_json": True, "write_shard_jsonl": True}}
    stale = {
        "run_id": "old-run",
        "shard_id": "repo_shard_05",
        "input_id": "101650961_7",
        "agent_name": "qwen-coder-2.5-7b",
        "generation_prompt_strategy": "zero-shot-project-aware",
        "reports_dir": str(report_dir),
        "test_passed": False,
        "final_failure_state": "TEST_DISCOVERY_FAILED",
    }

    _write_experiment_records(report_dir, stale, config)

    removed = _clear_experiment_records(report_dir, stale, config)

    assert report_dir / "records" / "101650961_7" / "qwen-coder-2.5-7b" / "zero-shot-project-aware" / "result.json" in removed
    assert report_dir / "records" / "experiments.jsonl" in removed
    assert not (report_dir / "records" / "101650961_7" / "qwen-coder-2.5-7b" / "zero-shot-project-aware" / "result.json").exists()
    assert load_experiment_jsonl([report_dir / "records" / "experiments.jsonl"]) == []
