from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Any, Protocol


@dataclass
class LlmRequest:
    model: str
    messages: list[dict[str, str]]
    temperature: float = 0.0
    api_base: str | None = None
    api_key_env: str | None = None
    num_ctx: int | None = None
    max_tokens: int | None = None


@dataclass
class LlmResponse:
    content: str
    metadata: dict[str, Any]


class LlmClient(Protocol):
    def complete(self, request: LlmRequest) -> LlmResponse:
        ...


class LiteLlmClient:
    def complete(self, request: LlmRequest) -> LlmResponse:
        try:
            from litellm import completion
        except ImportError as exc:
            raise RuntimeError("litellm is required for model calls") from exc

        kwargs: dict[str, Any] = {
            "model": request.model,
            "messages": request.messages,
            "temperature": request.temperature,
        }
        if request.api_base:
            kwargs["api_base"] = request.api_base
        if request.api_key_env:
            api_key = os.environ.get(request.api_key_env)
            if not api_key:
                raise RuntimeError(
                    f"API key environment variable '{request.api_key_env}' is not set for model '{request.model}'"
                )
            kwargs["api_key"] = api_key
        if request.max_tokens is not None:
            kwargs["max_tokens"] = request.max_tokens
        if request.num_ctx is not None:
            kwargs["num_ctx"] = request.num_ctx
        result = completion(**kwargs)
        message = result["choices"][0]["message"]
        return LlmResponse(content=message.get("content", ""), metadata=_safe_litellm_metadata(result))


class StaticLlmClient:
    def __init__(self, responses: list[str]):
        self.responses = responses
        self.calls: list[LlmRequest] = []

    def complete(self, request: LlmRequest) -> LlmResponse:
        self.calls.append(request)
        if not self.responses:
            return LlmResponse("", {"static": True})
        return LlmResponse(self.responses.pop(0), {"static": True})


def _safe_litellm_metadata(result: Any) -> dict[str, Any]:
    metadata: dict[str, Any] = {}
    for key in ("id", "created", "model", "object", "system_fingerprint"):
        value = _get_value(result, key)
        if value is not None:
            metadata[key] = value
    usage = _get_value(result, "usage")
    if usage is not None:
        metadata["usage"] = _json_safe(usage)
    choices = _get_value(result, "choices")
    if choices is not None:
        metadata["choices_count"] = len(choices) if hasattr(choices, "__len__") else ""
        try:
            first = choices[0]
            metadata["finish_reason"] = _get_value(first, "finish_reason")
        except Exception:
            pass
    return _json_safe(metadata)


def _get_value(obj: Any, key: str) -> Any:
    if isinstance(obj, dict):
        return obj.get(key)
    return getattr(obj, key, None)


def _json_safe(value: Any) -> Any:
    if value is None or isinstance(value, (str, int, float, bool)):
        return value
    if isinstance(value, dict):
        return {str(key): _json_safe(item) for key, item in value.items()}
    if isinstance(value, (list, tuple)):
        return [_json_safe(item) for item in value]
    if hasattr(value, "model_dump"):
        return _json_safe(value.model_dump())
    if hasattr(value, "dict"):
        return _json_safe(value.dict())
    return str(value)
