from __future__ import annotations

from pathlib import Path

import pytest

from src import adaptive_repair as ar
from src.build_runner import BuildContext
from src.llm_client import StaticLlmClient
from src.models import ExperimentContext, FailureOrigin, FailureState, RepairConfig, RepairStatus, VerificationResult
from src.test_writer import write_owned_generated_test


def vr(state: FailureState, sig: str, origin: FailureOrigin = FailureOrigin.GENERATED_TEST) -> VerificationResult:
    return VerificationResult(state=state, failure_origin=origin, normalized_error_signature=sig, error_signatures=[sig], raw_output=sig)


def context(tmp_path: Path) -> tuple[ExperimentContext, BuildContext]:
    workspace = tmp_path / "workspace"
    test_path = workspace / "src" / "test" / "java" / "demo" / "FooAgoneGeneratedTest_x.java"
    workspace.mkdir(parents=True)
    test_path.parent.mkdir(parents=True)
    exp = ExperimentContext(
        run_id="run",
        shard_id="shard",
        input_id="input",
        agent_name="agent",
        generation_prompt="zero",
        workspace=workspace,
        generated_test_path=test_path,
        generated_test_class_name="FooAgoneGeneratedTest_x",
        package_name="demo",
    )
    build = BuildContext(workspace, workspace, "maven", "FooAgoneGeneratedTest_x", "demo.FooAgoneGeneratedTest_x")
    return exp, build


def config() -> RepairConfig:
    return RepairConfig(
        max_attempts_per_prompt=1,
        max_repair_attempts=4,
        max_total_llm_attempts=5,
        no_progress_patience=1,
        prompt_order_by_state={
            "COMPILE_FAILED": ["compile-error-focused", "dependency-aware", "minimal-test"],
            "ASSERTION_FAILED": ["minimal-test", "dependency-aware"],
            "UNKNOWN_FAILED": ["minimal-test"],
        },
    )


def runtime(tmp_path: Path, responses: list[str]) -> ar.RepairRuntime:
    exp, build = context(tmp_path)
    write_owned_generated_test(
        experiment_id="run:shard:input:agent:zero",
        workspace=exp.workspace,
        generated_test_path=exp.generated_test_path,
        generated_test_class_name=exp.generated_test_class_name,
        code="package demo;\npublic class FooAgoneGeneratedTest_x {}\n",
    )
    return ar.RepairRuntime(
        config=config(),
        context=exp,
        build_context=build,
        llm_client=StaticLlmClient(responses),
        templates=ar.RepairTemplates(
            repair_templates={
                "compile-error-focused": "repair compile",
                "dependency-aware": "repair dependency",
                "minimal-test": "repair minimal",
            },
            regeneration_template="regenerate",
        ),
        model="fake",
    )


def test_progress_candidate_becomes_best(monkeypatch, tmp_path):
    rt = runtime(tmp_path, ["package demo;\npublic class FooAgoneGeneratedTest_x { public void a(){} }\n"])
    monkeypatch.setattr(ar, "_verify_candidate", lambda runtime: vr(FailureState.ASSERTION_FAILED, "assertion"))
    summary = ar.run_adaptive_repair(rt, initial_verification=vr(FailureState.COMPILE_FAILED, "compile"))
    assert summary.best_candidate_state == "ASSERTION_FAILED"
    assert "public void a" in (rt.repair_dir / "best_candidate.java").read_text(encoding="utf-8")


def test_regression_rolls_back_and_switches_prompt(monkeypatch, tmp_path):
    rt = runtime(tmp_path, ["package demo;\npublic class FooAgoneGeneratedTest_x { public void bad(){} }\n", "package demo;\npublic class FooAgoneGeneratedTest_x { public void ok(){} }\n"])
    results = [vr(FailureState.COMPILE_FAILED, "compile"), vr(FailureState.ASSERTION_FAILED, "assertion")]
    monkeypatch.setattr(ar, "_verify_candidate", lambda runtime: results.pop(0))
    summary = ar.run_adaptive_repair(rt, initial_verification=vr(FailureState.ASSERTION_FAILED, "assertion0"))
    assert summary.rollback_count >= 1
    assert summary.prompt_switch_count >= 1
    assert summary.final_repair_prompt_strategy == "dependency-aware"


def test_repeated_code_skips_build(monkeypatch, tmp_path):
    same = "package demo;\npublic class FooAgoneGeneratedTest_x {}\n"
    rt = runtime(tmp_path, [same])
    called = {"count": 0}
    def fake_verify(runtime):
        called["count"] += 1
        return vr(FailureState.ASSERTION_FAILED, "assertion")
    monkeypatch.setattr(ar, "_verify_candidate", fake_verify)
    summary = ar.run_adaptive_repair(rt, initial_verification=vr(FailureState.COMPILE_FAILED, "compile"))
    assert called["count"] == 0
    assert summary.repeated_code_detected is True
    assert summary.total_llm_attempts >= 1
    assert summary.build_attempts == 0


def test_invalid_output_preserves_best_and_switches_prompt(monkeypatch, tmp_path):
    rt = runtime(tmp_path, ["--- a/pom.xml\n+++ b/pom.xml"])
    monkeypatch.setattr(ar, "_verify_candidate", lambda runtime: pytest.fail("build must not run"))
    summary = ar.run_adaptive_repair(rt, initial_verification=vr(FailureState.COMPILE_FAILED, "compile"))
    assert summary.repair_status in {RepairStatus.INVALID_OUTPUT, RepairStatus.REGENERATED}
    assert summary.prompt_switch_count >= 1


def test_target_pass_runs_module_suite(monkeypatch, tmp_path):
    rt = runtime(tmp_path, ["package demo;\npublic class FooAgoneGeneratedTest_x { public void pass(){} }\n"])
    monkeypatch.setattr(ar, "_verify_candidate", lambda runtime: vr(FailureState.TARGET_TEST_PASSED, ""))
    monkeypatch.setattr(ar, "verify_module_tests", lambda build_context: vr(FailureState.MODULE_TESTS_PASSED, ""))
    summary = ar.run_adaptive_repair(rt, initial_verification=vr(FailureState.COMPILE_FAILED, "compile"), baseline_verification=vr(FailureState.MODULE_TESTS_PASSED, ""))
    assert summary.repair_status == RepairStatus.REPAIRED
    assert summary.repair_stopped_reason == "module_tests_passed"


def test_existing_project_failure_does_not_call_llm(tmp_path):
    rt = runtime(tmp_path, ["package demo;\npublic class FooAgoneGeneratedTest_x { }"])
    summary = ar.run_adaptive_repair(rt, initial_verification=vr(FailureState.MODULE_TESTS_FAILED, "existing", FailureOrigin.EXISTING_PROJECT))
    assert len(rt.llm_client.calls) == 0
    assert summary.repair_status == RepairStatus.EXISTING_PROJECT_FAILURE


def test_checkpoint_rollback_restores_exact_content(monkeypatch, tmp_path):
    rt = runtime(tmp_path, ["package demo;\npublic class FooAgoneGeneratedTest_x { public void bad(){} }\n"])
    original = rt.context.generated_test_path.read_text(encoding="utf-8")
    monkeypatch.setattr(ar, "_verify_candidate", lambda runtime: vr(FailureState.COMPILE_FAILED, "compile2"))
    ar.run_adaptive_repair(rt, initial_verification=vr(FailureState.ASSERTION_FAILED, "assertion"))
    assert rt.context.generated_test_path.read_text(encoding="utf-8") == original


def test_two_experiments_have_different_workspaces(tmp_path):
    exp1, _ = context(tmp_path / "a")
    exp2, _ = context(tmp_path / "b")
    assert exp1.workspace != exp2.workspace


def test_unlimited_retry_ignores_attempt_budget_and_cycles_prompts():
    cfg = RepairConfig(retry_mode="unlimited", max_repair_attempts=1, max_total_llm_attempts=1)
    assert ar._within_attempt_budget(cfg, repair_attempts=99, total_llm_attempts=99) is True
    assert ar._next_prompt_index(["compile", "minimal"], 1, cfg) == 0
