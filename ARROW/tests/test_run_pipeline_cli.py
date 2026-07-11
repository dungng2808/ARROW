from __future__ import annotations

import json
from argparse import Namespace

import pytest

from src.models import FailureOrigin, FailureState, GenerationStrategy, VerificationResult
from src.run_pipeline import _agents, _baseline_blocks_generation, _load_examples


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
