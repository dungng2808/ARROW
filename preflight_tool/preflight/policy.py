from __future__ import annotations

from .models import PreflightStatus, RevisionStatus, RunMode


_PRIORITY: tuple[tuple[str, ...], ...] = (
    (PreflightStatus.CLONE_FAILED, PreflightStatus.COMMIT_MISSING, PreflightStatus.CHECKOUT_FAILED),
    (PreflightStatus.BUILD_TOOL_UNSUPPORTED, PreflightStatus.JDK_UNSUPPORTED, PreflightStatus.BUILD_TIMEOUT, PreflightStatus.DEPENDENCY_UNAVAILABLE, PreflightStatus.MAIN_BUILD_FAILED, PreflightStatus.TEST_COMPILE_FAILED),
    (PreflightStatus.SOURCE_INVALID,),
    (PreflightStatus.EXCLUDED,),
    (PreflightStatus.PROBE_FAILED,),
    (PreflightStatus.INTEGRATION_ONLY, PreflightStatus.NEEDS_REVIEW),
    (PreflightStatus.PRECHECKED,),
    (PreflightStatus.ELIGIBLE,),
)


def select_primary_status(observed: list[str] | set[str] | tuple[str, ...]) -> str:
    """Return the single status with the precedence mandated by preflight.md."""
    values = set(observed)
    for group in _PRIORITY:
        for status in group:
            if status in values:
                return status
    raise ValueError(f"No recognised preflight status in {sorted(values)!r}")


def derive_eligibility(preflight_status: str, run_mode: str, revision_status: str) -> tuple[bool, bool]:
    technical = preflight_status == PreflightStatus.ELIGIBLE
    strict = (
        run_mode == RunMode.STRICT
        and revision_status == RevisionStatus.UPSTREAM_PINNED
        and technical
    )
    return technical, strict
