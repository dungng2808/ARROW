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
        ([PreflightStatus.ELIGIBLE, PreflightStatus.PROBE_FAILED], PreflightStatus.PROBE_FAILED),
        ([PreflightStatus.NEEDS_REVIEW, PreflightStatus.MAIN_BUILD_FAILED], PreflightStatus.MAIN_BUILD_FAILED),
        ([PreflightStatus.SOURCE_INVALID, PreflightStatus.BUILD_TIMEOUT], PreflightStatus.BUILD_TIMEOUT),
        ([PreflightStatus.ELIGIBLE, PreflightStatus.PRECHECKED], PreflightStatus.PRECHECKED),
    ],
)
def test_primary_status_uses_specified_priority(observed, expected):
    assert select_primary_status(observed) == expected


@pytest.mark.business
@pytest.mark.parametrize(
    ("status", "mode", "revision", "technical", "strict"),
    [
        (PreflightStatus.ELIGIBLE, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, True, True),
        (PreflightStatus.ELIGIBLE, RunMode.DISCOVERY_ONLY, RevisionStatus.UPSTREAM_PINNED, True, False),
        (PreflightStatus.ELIGIBLE, RunMode.STRICT, RevisionStatus.CONTENT_MATCHED, True, False),
        (PreflightStatus.PRECHECKED, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
        (PreflightStatus.NEEDS_REVIEW, RunMode.STRICT, RevisionStatus.UPSTREAM_PINNED, False, False),
    ],
)
def test_technical_and_strict_eligibility_formula(status, mode, revision, technical, strict):
    assert derive_eligibility(status, mode, revision) == (technical, strict)


@pytest.mark.business
def test_unknown_status_is_rejected_by_policy():
    with pytest.raises(ValueError):
        select_primary_status(["NOT_A_STATUS"])
