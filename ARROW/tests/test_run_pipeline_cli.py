from __future__ import annotations

from argparse import Namespace

import pytest

from src.run_pipeline import _agents


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
