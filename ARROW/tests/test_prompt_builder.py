from __future__ import annotations

from pathlib import Path

from src.models import ExperimentContext, FailureOrigin, FailureState, VerificationResult
from src.prompt_builder import build_generation_prompt, build_repair_prompt


def test_generation_prompt_renders_required_class_placeholder():
    context = ExperimentContext(
        run_id="r",
        shard_id="s",
        input_id="i",
        agent_name="a",
        generation_prompt="zero-shot",
        workspace=Path("workspace"),
        generated_test_path=Path("FooTest_1234abcd.java"),
        generated_test_class_name="FooTest_1234abcd",
        package_name="demo",
        testing_framework="junit4",
        build_tool="maven",
        focal_class_source="package demo; public class Foo {}",
    )
    prompt = build_generation_prompt(
        template="Class must be {required_generated_class_name} in package {package_name} using {testing_framework}.",
        context=context,
    )
    assert "FooTest_1234abcd" in prompt
    assert "{required_generated_class_name}" not in prompt


def test_compile_repair_prompt_keeps_diagnostics_and_imports_but_omits_large_sources():
    context = ExperimentContext(
        run_id="r",
        shard_id="s",
        input_id="i",
        agent_name="a",
        generation_prompt="zero-shot",
        workspace=Path("workspace"),
        generated_test_path=Path("FooTest_1234abcd.java"),
        generated_test_class_name="FooTest_1234abcd",
        package_name="demo",
        testing_framework="junit4",
        build_tool="maven",
        focal_class_source="FOCAL_SOURCE_SHOULD_NOT_BE_INCLUDED" * 1000,
        existing_tests=[
            "import java.util.Collections;\nimport java.util.NoSuchElementException;\nclass Existing {\n"
            + ("EXISTING_BODY_SHOULD_NOT_BE_INCLUDED" * 1000)
            + "\n}"
        ],
        public_api={"methods": ["public void work()"], "constructors": []},
    )
    verification = VerificationResult(
        state=FailureState.COMPILE_FAILED,
        failure_origin=FailureOrigin.GENERATED_TEST,
        primary_error="cannot find symbol",
        normalized_error_signature="cannot find symbol",
        raw_output=("unrelated Maven output\n" * 100) + "FooTest_1234abcd.java: cannot find symbol\nsymbol: variable Collections\n",
    )

    prompt = build_repair_prompt(
        template="Repair {required_generated_class_name}",
        context=context,
        best_generated_test="package demo; public class FooTest_1234abcd {}",
        verification=verification,
    )

    assert "symbol: variable Collections" in prompt
    assert "import java.util.Collections;" in prompt
    assert "FOCAL_SOURCE_SHOULD_NOT_BE_INCLUDED" not in prompt
    assert "EXISTING_BODY_SHOULD_NOT_BE_INCLUDED" not in prompt
