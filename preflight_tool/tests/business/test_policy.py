from __future__ import annotations

import pytest

from preflight.models import PreflightStatus, RevisionStatus, RunMode
from preflight.policy import derive_eligibility, select_primary_status


@pytest.mark.business
@pytest.mark.parametrize("status", list(PreflightStatus))
def test_each_preflight_status_is_selectable_on_its_own(status):
    assert select_primary_status([status]) == status


@pytest.mark.business
@pytest.mark.parametrize(
    ("observed", "expected"),
    [
        # Group 0 (checkout/repo) beats all later groups
        ([PreflightStatus.ELIGIBLE, PreflightStatus.CLONE_FAILED], PreflightStatus.CLONE_FAILED),
        ([PreflightStatus.MAIN_BUILD_FAILED, PreflightStatus.COMMIT_MISSING], PreflightStatus.COMMIT_MISSING),
        ([PreflightStatus.CHECKOUT_FAILED, PreflightStatus.BUILD_TIMEOUT], PreflightStatus.CHECKOUT_FAILED),
        # Intra-group 0 precedence
        ([PreflightStatus.CHECKOUT_FAILED, PreflightStatus.CLONE_FAILED], PreflightStatus.CLONE_FAILED),
        ([PreflightStatus.CHECKOUT_FAILED, PreflightStatus.COMMIT_MISSING], PreflightStatus.COMMIT_MISSING),
        # Group 1 (build/JDK) beats Group 2..7
        ([PreflightStatus.SOURCE_INVALID, PreflightStatus.BUILD_TOOL_UNSUPPORTED], PreflightStatus.BUILD_TOOL_UNSUPPORTED),
        ([PreflightStatus.EXCLUDED, PreflightStatus.JDK_UNSUPPORTED], PreflightStatus.JDK_UNSUPPORTED),
        ([PreflightStatus.SOURCE_INVALID, PreflightStatus.BUILD_TIMEOUT], PreflightStatus.BUILD_TIMEOUT),
        ([PreflightStatus.PROBE_FAILED, PreflightStatus.DEPENDENCY_UNAVAILABLE], PreflightStatus.DEPENDENCY_UNAVAILABLE),
        ([PreflightStatus.NEEDS_REVIEW, PreflightStatus.MAIN_BUILD_FAILED], PreflightStatus.MAIN_BUILD_FAILED),
        ([PreflightStatus.ELIGIBLE, PreflightStatus.TEST_COMPILE_FAILED], PreflightStatus.TEST_COMPILE_FAILED),
        # Intra-group 1 precedence
        ([PreflightStatus.MAIN_BUILD_FAILED, PreflightStatus.BUILD_TOOL_UNSUPPORTED], PreflightStatus.BUILD_TOOL_UNSUPPORTED),
        ([PreflightStatus.TEST_COMPILE_FAILED, PreflightStatus.MAIN_BUILD_FAILED], PreflightStatus.MAIN_BUILD_FAILED),
        # Group 2 (SOURCE_INVALID) beats Group 3..7
        ([PreflightStatus.EXCLUDED, PreflightStatus.SOURCE_INVALID], PreflightStatus.SOURCE_INVALID),
        ([PreflightStatus.PROBE_FAILED, PreflightStatus.SOURCE_INVALID], PreflightStatus.SOURCE_INVALID),
        # Group 3 (EXCLUDED) beats Group 4..7
        ([PreflightStatus.PROBE_FAILED, PreflightStatus.EXCLUDED], PreflightStatus.EXCLUDED),
        ([PreflightStatus.NEEDS_REVIEW, PreflightStatus.EXCLUDED], PreflightStatus.EXCLUDED),
        # Group 4 (PROBE_FAILED) beats Group 5..7
        ([PreflightStatus.NEEDS_REVIEW, PreflightStatus.PROBE_FAILED], PreflightStatus.PROBE_FAILED),
        ([PreflightStatus.ELIGIBLE, PreflightStatus.PROBE_FAILED], PreflightStatus.PROBE_FAILED),
        # Group 5 (INTEGRATION_ONLY / NEEDS_REVIEW) beats Group 6..7
        ([PreflightStatus.PRECHECKED, PreflightStatus.INTEGRATION_ONLY], PreflightStatus.INTEGRATION_ONLY),
        ([PreflightStatus.ELIGIBLE, PreflightStatus.NEEDS_REVIEW], PreflightStatus.NEEDS_REVIEW),
        ([PreflightStatus.NEEDS_REVIEW, PreflightStatus.INTEGRATION_ONLY], PreflightStatus.INTEGRATION_ONLY),
        # Group 6 (PRECHECKED) beats Group 7 (ELIGIBLE)
        ([PreflightStatus.ELIGIBLE, PreflightStatus.PRECHECKED], PreflightStatus.PRECHECKED),
        # Multi-element conflict resolution
        (
            [PreflightStatus.ELIGIBLE, PreflightStatus.PROBE_FAILED, PreflightStatus.TEST_COMPILE_FAILED, PreflightStatus.CLONE_FAILED],
            PreflightStatus.CLONE_FAILED,
        ),
    ],
)
def test_primary_status_uses_specified_priority(observed, expected):
    assert select_primary_status(observed) == expected


@pytest.mark.business
@pytest.mark.parametrize(
    ("status", "mode", "revision", "technical", "strict"),
    [
        # Only ELIGIBLE + STRICT + UPSTREAM_PINNED gives technical=True and strict=True
        (PreflightStatus.ELIGIBLE, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, True, True),
        # Technical eligible is True when ELIGIBLE, regardless of mode or revision
        (PreflightStatus.ELIGIBLE, RunMode.DISCOVERY_ONLY, RevisionStatus.UPSTREAM_PINNED, True, False),
        (PreflightStatus.ELIGIBLE, RunMode.STRICT, RevisionStatus.CONTENT_MATCHED, True, False),
        (PreflightStatus.ELIGIBLE, RunMode.DISCOVERY_ONLY, RevisionStatus.CONTENT_MATCHED, True, False),
        (PreflightStatus.ELIGIBLE, RunMode.STRICT, RevisionStatus.UNVERIFIED, True, False),
        (PreflightStatus.ELIGIBLE, RunMode.DISCOVERY_ONLY, RevisionStatus.UNVERIFIED, True, False),
        # Non-ELIGIBLE statuses never give technical=True or strict=True
        (PreflightStatus.PRECHECKED, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.NEEDS_REVIEW, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.EXCLUDED, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.PROBE_FAILED, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.MAIN_BUILD_FAILED, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.TEST_COMPILE_FAILED, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.BUILD_TIMEOUT, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.CLONE_FAILED, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.COMMIT_MISSING, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.INTEGRATION_ONLY, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
    ],
)
def test_technical_and_strict_eligibility_formula(status, mode, revision, technical, strict):
    assert derive_eligibility(status, mode, revision) == (technical, strict)


@pytest.mark.business
def test_unknown_status_is_rejected_by_policy():
    with pytest.raises(ValueError):
        select_primary_status(["NOT_A_STATUS"])

