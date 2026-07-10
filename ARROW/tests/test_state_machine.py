from __future__ import annotations

from src.adaptive_repair import classify_transition
from src.models import FailureOrigin, FailureState, RepairDecision, VerificationResult


def vr(state: FailureState, sig: str = "", signatures: list[str] | None = None, errors: int = 0) -> VerificationResult:
    return VerificationResult(
        state=state,
        failure_origin=FailureOrigin.GENERATED_TEST,
        normalized_error_signature=sig,
        error_signatures=signatures if signatures is not None else ([sig] if sig else []),
        compile_errors=errors,
    )


def test_compile_failed_to_test_discovery_failed_is_progress():
    assert classify_transition(vr(FailureState.COMPILE_FAILED), vr(FailureState.TEST_DISCOVERY_FAILED)) == RepairDecision.PROGRESS


def test_compile_failed_to_assertion_failed_is_progress_even_if_error_count_increases():
    previous = vr(FailureState.COMPILE_FAILED, "a", errors=1)
    new = vr(FailureState.ASSERTION_FAILED, "b", errors=99)
    assert classify_transition(previous, new) == RepairDecision.PROGRESS


def test_assertion_failed_to_compile_failed_is_regression():
    assert classify_transition(vr(FailureState.ASSERTION_FAILED), vr(FailureState.COMPILE_FAILED)) == RepairDecision.REGRESSION


def test_test_discovery_failed_to_compile_failed_is_regression():
    assert classify_transition(vr(FailureState.TEST_DISCOVERY_FAILED), vr(FailureState.COMPILE_FAILED)) == RepairDecision.REGRESSION


def test_same_signature_is_repeated_error():
    assert classify_transition(vr(FailureState.COMPILE_FAILED, "same"), vr(FailureState.COMPILE_FAILED, "same")) == RepairDecision.REPEATED_ERROR


def test_repeated_code_wins_before_build_decision():
    assert classify_transition(vr(FailureState.COMPILE_FAILED, "a"), vr(FailureState.ASSERTION_FAILED, "b"), candidate_hash_seen=True) == RepairDecision.REPEATED_CODE


def test_same_state_previous_primary_removed_is_partial_progress():
    previous = vr(FailureState.COMPILE_FAILED, "missing_symbol", ["missing_symbol", "bad_import"])
    new = vr(FailureState.COMPILE_FAILED, "bad_import", ["bad_import"])
    assert classify_transition(previous, new) == RepairDecision.PARTIAL_PROGRESS


def test_same_state_no_meaningful_signature_change_is_no_progress():
    previous = vr(FailureState.COMPILE_FAILED, "missing_symbol", ["missing_symbol", "bad_import"])
    new = vr(FailureState.COMPILE_FAILED, "other", ["missing_symbol", "other"])
    assert classify_transition(previous, new) == RepairDecision.NO_PROGRESS


def test_module_failed_to_module_passed_is_accepted():
    assert classify_transition(vr(FailureState.MODULE_TESTS_FAILED), vr(FailureState.MODULE_TESTS_PASSED)) == RepairDecision.ACCEPTED


def test_error_counts_do_not_control_decision():
    previous = vr(FailureState.RUNTIME_FAILED, "runtime", errors=100)
    new = vr(FailureState.ASSERTION_FAILED, "assertion", errors=1)
    assert classify_transition(previous, new) == RepairDecision.PROGRESS
