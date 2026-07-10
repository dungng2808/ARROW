from __future__ import annotations

from pathlib import Path

from src.models import ExperimentContext
from src.prompt_builder import build_generation_prompt


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
