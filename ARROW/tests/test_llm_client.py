from __future__ import annotations

import json
import sys
from types import SimpleNamespace

import pytest

from src.llm_client import LiteLlmClient, LlmRequest, _safe_litellm_metadata


class Choice:
    finish_reason = "stop"


class Usage:
    def model_dump(self):
        return {"prompt_tokens": 1, "completion_tokens": 2}


def test_safe_litellm_metadata_is_json_serializable():
    result = {
        "id": "abc",
        "model": "ollama/qwen2.5-coder:1.5b",
        "choices": [Choice()],
        "usage": Usage(),
    }
    metadata = _safe_litellm_metadata(result)
    assert metadata["choices_count"] == 1
    assert metadata["finish_reason"] == "stop"
    assert metadata["usage"]["prompt_tokens"] == 1
    json.dumps(metadata)


def test_litellm_reads_api_key_from_configured_environment(monkeypatch):
    captured = {}

    def completion(**kwargs):
        captured.update(kwargs)
        return {"choices": [{"message": {"content": "ok"}}]}

    monkeypatch.setenv("ARROW_TEST_API_KEY", "secret-value")
    monkeypatch.setitem(sys.modules, "litellm", SimpleNamespace(completion=completion))

    response = LiteLlmClient().complete(
        LlmRequest(
            model="openai/gpt-4.1-mini",
            messages=[{"role": "user", "content": "test"}],
            api_key_env="ARROW_TEST_API_KEY",
        )
    )

    assert response.content == "ok"
    assert captured["api_key"] == "secret-value"


def test_litellm_reports_missing_configured_api_key(monkeypatch):
    monkeypatch.delenv("ARROW_MISSING_API_KEY", raising=False)
    monkeypatch.setitem(sys.modules, "litellm", SimpleNamespace(completion=lambda **kwargs: None))

    with pytest.raises(RuntimeError, match="ARROW_MISSING_API_KEY"):
        LiteLlmClient().complete(
            LlmRequest(
                model="openai/gpt-4.1-mini",
                messages=[{"role": "user", "content": "test"}],
                api_key_env="ARROW_MISSING_API_KEY",
            )
        )
