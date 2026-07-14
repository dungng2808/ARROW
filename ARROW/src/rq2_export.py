"""RQ2 repair summary export helpers.

The export is intentionally derived from the latest logical experiment rows.  A
row is included in the repair denominator only when its initial failure belongs
to the generated test (or its origin is unavailable for legacy records).  This
keeps infrastructure and existing-project failures out of repair statistics.
"""

from __future__ import annotations

import csv
import statistics
from pathlib import Path
from typing import Any


RQ2_COLUMNS = [
    "Repair mechanism",
    "Initial failed tests",
    "Final compile, n (%)",
    "Final target pass, n (%)",
    "Repair success, n (%)",
    "Repair attempts, Median [IQR]",
    "Repair time, Median [IQR]",
]

_PASS_STATES = {"TARGET_TEST_PASSED", "MODULE_TESTS_PASSED"}
_NON_COMPILE_STATES = {
    "COMPILE_FAILED",
    "TEST_DISCOVERY_FAILED",
    "TOOL_ERROR",
    "BASELINE_BUILD_FAILED",
    "MODULE_BUILD_FAILED",
}
_NON_REPAIR_ORIGINS = {"INFRASTRUCTURE", "EXISTING_PROJECT"}
_NON_REPAIR_STATES = {"TOOL_ERROR", "BASELINE_BUILD_FAILED", "MODULE_BUILD_FAILED"}
_MECHANISM_ORDER = {"No Repair": 0, "Fixed Repair": 1, "Adaptive Repair": 2}


def _as_int(value: Any) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return 0


def _as_float(value: Any) -> float | None:
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _truthy(value: Any) -> bool:
    if isinstance(value, bool):
        return value
    return str(value or "").strip().lower() in {"1", "true", "yes"}


def _state(row: dict[str, Any], final: bool = False) -> str:
    key = "final_failure_state" if final else "initial_failure_state"
    return str(row.get(key) or "").strip()


def _passed(row: dict[str, Any]) -> bool:
    return (
        _truthy(row.get("target_test_passed"))
        or _truthy(row.get("module_tests_passed"))
        or _state(row, final=True) in _PASS_STATES
    )


def _initial_failure_is_repairable(row: dict[str, Any]) -> bool:
    state = _state(row)
    if not state or state in _PASS_STATES or state in _NON_REPAIR_STATES:
        return False
    origin = str(row.get("initial_failure_origin") or "").strip().upper()
    return origin not in _NON_REPAIR_ORIGINS


def repair_attempted(row: dict[str, Any]) -> bool:
    return _as_int(row.get("repair_attempts")) > 0 or _as_int(row.get("regeneration_attempts")) > 0


def repair_mechanism(row: dict[str, Any]) -> str:
    explicit = str(row.get("repair_mechanism") or row.get("repair_mode") or "").strip()
    if explicit:
        return explicit
    return "Adaptive Repair" if repair_attempted(row) else "No Repair"


def _rate_text(count: int, total: int) -> str:
    if not total:
        return "—"
    return f"{count} ({count / total * 100:.2f}%)"


def _summary_text(values: list[float], *, unit: str = "") -> str:
    if not values:
        return "—"
    values = sorted(values)
    median = statistics.median(values)
    if len(values) == 1:
        q1 = q3 = values[0]
    else:
        quartiles = statistics.quantiles(values, n=4, method="inclusive")
        q1, q3 = quartiles[0], quartiles[2]
    suffix = f" {unit}" if unit else ""
    return f"{median:.2f}{suffix} [{q1:.2f}–{q3:.2f}{suffix}]"


def _repair_time(row: dict[str, Any]) -> float | None:
    # New records have repair_time_seconds.  Older records do not, so retain a
    # useful backward-compatible fallback to total experiment elapsed time.
    return _as_float(row.get("repair_time_seconds")) or _as_float(row.get("elapsed_seconds"))


def build_rq2_rows(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    grouped: dict[str, list[dict[str, Any]]] = {}
    for row in rows:
        grouped.setdefault(repair_mechanism(row), []).append(row)

    output: list[dict[str, Any]] = []
    for mechanism, mechanism_rows in sorted(grouped.items(), key=lambda item: (_MECHANISM_ORDER.get(item[0], 99), item[0])):
        failed = [row for row in mechanism_rows if _initial_failure_is_repairable(row)]
        compiled = [
            row
            for row in failed
            if _state(row, final=True) not in _NON_COMPILE_STATES
            and (
                _truthy(row.get("compilation"))
                or _state(row, final=True) in _PASS_STATES | {"ASSERTION_FAILED", "TEST_FAILED", "MODULE_TESTS_FAILED"}
            )
        ]
        target_pass = [row for row in failed if _passed(row)]
        attempted = [row for row in failed if repair_attempted(row)]
        success = [row for row in attempted if _passed(row)]
        # Keep the reported attempts aligned with RepairSummary.repair_attempts;
        # regeneration is tracked separately and is not silently added here.
        attempts = [float(_as_int(row.get("repair_attempts"))) for row in attempted if _as_int(row.get("repair_attempts")) > 0]
        times = [value for value in (_repair_time(row) for row in attempted) if value is not None]
        output.append(
            {
                "Repair mechanism": mechanism,
                "Initial failed tests": len(failed),
                "Final compile, n (%)": _rate_text(len(compiled), len(failed)),
                "Final target pass, n (%)": _rate_text(len(target_pass), len(failed)),
                "Repair success, n (%)": _rate_text(len(success), len(failed)),
                "Repair attempts, Median [IQR]": _summary_text(attempts),
                "Repair time, Median [IQR]": _summary_text(times, unit="s"),
            }
        )
    return output


def write_rq2_csv(path: Path, rows: list[dict[str, Any]]) -> int:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=RQ2_COLUMNS, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)
    return len(rows)
