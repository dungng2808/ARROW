"""Shared quality filters for RQ1/RQ2 exports."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any


PASS_STATES = {"TARGET_TEST_PASSED", "MODULE_TESTS_PASSED"}
INFRASTRUCTURE_ORIGINS = {"INFRASTRUCTURE", "EXISTING_PROJECT"}
INFRASTRUCTURE_STATES = {
    "TOOL_ERROR",
    "BASELINE_BUILD_FAILED",
    "MODULE_BUILD_FAILED",
    "BUILD_TIMEOUT",
    "TIMEOUT",
}


def _read_json(path: Path) -> dict[str, Any]:
    try:
        payload = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError, TypeError):
        return {}
    return payload if isinstance(payload, dict) else {}


def baseline_state(row: dict[str, Any]) -> str:
    value = str(row.get("baseline_state") or row.get("baseline_failure_state") or "").strip()
    if value:
        return value
    experiment_dir = str(row.get("experiment_dir") or "").strip()
    if experiment_dir:
        artifact = _read_json(Path(experiment_dir) / "baseline_verification.json")
        return str(artifact.get("state") or "").strip()
    return ""


def baseline_passed(row: dict[str, Any]) -> bool:
    return baseline_state(row) == "MODULE_TESTS_PASSED"


def initial_state(row: dict[str, Any]) -> str:
    return str(row.get("initial_failure_state") or "").strip()


def initial_failure_origin(row: dict[str, Any]) -> str:
    return str(row.get("initial_failure_origin") or "").strip().upper()


def initial_failed(row: dict[str, Any]) -> bool:
    state = initial_state(row)
    return bool(state) and state not in PASS_STATES


def generated_test_failed_initially(row: dict[str, Any]) -> bool:
    return initial_failure_origin(row) == "GENERATED_TEST" and initial_failed(row)


def infrastructure_failure(row: dict[str, Any]) -> bool:
    return initial_failure_origin(row) in INFRASTRUCTURE_ORIGINS or initial_state(row) in INFRASTRUCTURE_STATES


def filter_rows(
    rows: list[dict[str, Any]],
    *,
    mode: str,
) -> tuple[list[dict[str, Any]], dict[str, int]]:
    """Filter rows and return selected rows plus reason counts.

    ``baseline_valid`` is used by RQ1 and keeps both generated-test passes and
    failures. ``baseline_passed_generated_failed`` is used by RQ2.
    ``all`` returns every row while still reporting diagnostic categories.
    """
    if mode not in {"all", "baseline_valid", "baseline_passed_generated_failed"}:
        raise ValueError(f"Unknown experiment filter mode: {mode}")
    selected: list[dict[str, Any]] = []
    reasons = {
        "baseline_failed": 0,
        "infrastructure_error": 0,
        "baseline_unknown": 0,
        "generated_test_not_failed": 0,
    }
    for row in rows:
        if mode == "all":
            selected.append(row)
            continue
        if not baseline_passed(row):
            state = baseline_state(row)
            if infrastructure_failure(row):
                reasons["infrastructure_error"] += 1
            elif state:
                reasons["baseline_failed"] += 1
            else:
                reasons["baseline_unknown"] += 1
            continue
        if mode == "baseline_passed_generated_failed" and not generated_test_failed_initially(row):
            reasons["generated_test_not_failed"] += 1
            continue
        selected.append(row)
    reasons["selected"] = len(selected)
    reasons["excluded"] = len(rows) - len(selected)
    return selected, reasons


def filter_label(mode: str) -> str:
    return {
        "all": "all",
        "baseline_valid": "baseline_valid",
        "baseline_passed_generated_failed": "baseline_passed_generated_failed",
    }[mode]
