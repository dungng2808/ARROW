from __future__ import annotations

from pathlib import Path

from src.input_selector import load_sample
from src.project_analyzer import analyze_experiment, unique_generated_class_name


def make_sample(tmp_path, build_file: str):
    dataset = tmp_path / "dataset" / "p1"
    dataset.mkdir(parents=True)
    sample = dataset / "p1_0.json"
    sample.write_text(
        '{"repository":{"url":"https://example.com/repo"},"focal_class":{"identifier":"Foo","file":"src/main/java/demo/Foo.java","methods":[{"modifiers":"public","signature":"void run()","full_signature":"public void run()"}]},"test_class":{"identifier":"FooTest","file":"src/test/java/demo/FooTest.java"}}',
        encoding="utf-8",
    )
    workspace = tmp_path / "workspace"
    (workspace / "src/main/java/demo").mkdir(parents=True)
    (workspace / "src/test/java/demo").mkdir(parents=True)
    (workspace / "src/main/java/demo/Foo.java").write_text("package demo; public class Foo { public void run(){} }", encoding="utf-8")
    (workspace / "src/test/java/demo/FooTest.java").write_text("package demo; public class FooTest {}", encoding="utf-8")
    (workspace / build_file).write_text("<project/>" if build_file == "pom.xml" else "plugins { id 'java' }", encoding="utf-8")
    return load_sample(sample, tmp_path / "dataset"), workspace


def test_analyze_maven_module_and_generated_path(tmp_path):
    sample, workspace = make_sample(tmp_path, "pom.xml")
    context, module_root = analyze_experiment(sample=sample, workspace=workspace, run_id="r", shard_id="s", agent_name="a", generation_prompt="zero")
    assert context.build_tool == "maven"
    assert context.package_name == "demo"
    assert context.generated_test_path.parent == workspace / "src/test/java/demo"
    assert context.generated_test_class_name.startswith("FooTest_")
    assert module_root == workspace


def test_unique_generated_class_name_uses_test_hash_convention():
    assert unique_generated_class_name("Foo", "experiment").startswith("FooTest_")


def test_analyze_gradle_module(tmp_path):
    sample, workspace = make_sample(tmp_path, "build.gradle")
    context, _module_root = analyze_experiment(sample=sample, workspace=workspace, run_id="r", shard_id="s", agent_name="a", generation_prompt="zero")
    assert context.build_tool == "gradle"


def test_analyze_detects_junit4_from_existing_test_source(tmp_path):
    sample, workspace = make_sample(tmp_path, "pom.xml")
    (workspace / "src/test/java/demo/FooTest.java").write_text(
        "package demo;\nimport org.junit.Test;\nimport static org.junit.Assert.assertTrue;\npublic class FooTest { @Test public void run() { assertTrue(true); } }",
        encoding="utf-8",
    )

    context, _module_root = analyze_experiment(sample=sample, workspace=workspace, run_id="r", shard_id="s", agent_name="a", generation_prompt="zero")

    assert context.testing_framework == "junit4"


def test_analyze_prefers_junit4_annotations_over_junit5_assertion_import(tmp_path):
    sample, workspace = make_sample(tmp_path, "pom.xml")
    (workspace / "src/test/java/demo/FooTest.java").write_text(
        """package demo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
public class FooTest {
  @Before public void setUp() {}
  @Test public void run() { assertTrue(true); }
}
""",
        encoding="utf-8",
    )

    context, _module_root = analyze_experiment(sample=sample, workspace=workspace, run_id="r", shard_id="s", agent_name="a", generation_prompt="zero")

    assert context.testing_framework == "junit4"
