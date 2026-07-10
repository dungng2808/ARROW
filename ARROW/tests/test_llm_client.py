from __future__ import annotations

import json
import sys
from types import SimpleNamespace

import pytest

from src.llm_client import (
    LiteLlmClient,
    LlmRequest,
    _safe_litellm_metadata,
    record_token_usage,
    token_usage_from_metadata,
    token_usage_report,
)


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


def test_token_usage_is_aggregated_by_prompt():
    bucket = {}
    record_token_usage(
        bucket,
        "generation:zero-shot",
        {"usage": {"prompt_tokens": 100, "completion_tokens": 20, "total_tokens": 120}},
    )
    record_token_usage(
        bucket,
        "repair:minimal-test",
        {"usage": {"input_tokens": 80, "output_tokens": 15}},
    )

    alias_usage = token_usage_from_metadata({"usage": {"input_tokens": 3, "output_tokens": 2}})
    assert alias_usage is not None
    assert alias_usage["total_tokens"] == 5
    report = token_usage_report(bucket)
    assert report["llm_input_tokens"] == 180
    assert report["llm_output_tokens"] == 35
    assert report["llm_total_tokens"] == 215
    assert report["llm_call_count"] == 2
    assert report["token_usage_by_prompt"]["repair:minimal-test"]["total_tokens"] == 95


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
