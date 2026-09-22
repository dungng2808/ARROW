from __future__ import annotations

import sys
import subprocess
from pathlib import Path

import pytest

from preflight.models import BuildAttempt, PreflightStatus
from preflight.java_ast import JavaApi, parse_java_source
from preflight.runner import BuildPlan, ToolConfig, _class_policy, _failure_status, _framework, _gradle_commands, _java_env, _java_target, _maven_commands, _path_candidates, _probe_source, _resolve_parent_apis, _run, _wrapper, detect_build


def _attempt(tmp_path: Path, output: str = "", *, timed_out: bool = False) -> BuildAttempt:
    log = tmp_path / "attempt.log"; log.write_text(output, encoding="utf-8")
    return BuildAttempt("main_compile", str(tmp_path), ["tool"], 1, timed_out, 0.1, str(log))


@pytest.mark.unit
@pytest.mark.parametrize(
    ("output", "stage", "expected"),
    [
        ("EXECUTABLE_NOT_FOUND", "main_compile", PreflightStatus.BUILD_TOOL_UNSUPPORTED),
        ("Could not resolve dependency", "main_compile", PreflightStatus.DEPENDENCY_UNAVAILABLE),
        ("invalid target release", "main_compile", PreflightStatus.JDK_UNSUPPORTED),
        ("ordinary failure", "main_compile", PreflightStatus.MAIN_BUILD_FAILED),
        ("ordinary failure", "test_compile", PreflightStatus.TEST_COMPILE_FAILED),
    ],
)
def test_build_failure_classification(tmp_path, output, stage, expected):
    assert _failure_status(_attempt(tmp_path, output), stage) == expected
    assert _failure_status(_attempt(tmp_path, timed_out=True), stage) == PreflightStatus.BUILD_TIMEOUT


@pytest.mark.unit
def test_detect_build_prefers_windows_wrappers_and_module_commands(tmp_path):
    root = tmp_path / "project"; module = root / "child"; source = module / "src/main/java/x/X.java"
    source.parent.mkdir(parents=True); source.write_text("class X {}", encoding="utf-8")
    (root / "pom.xml").write_text("<project/>", encoding="utf-8"); (module / "pom.xml").write_text("<project/>", encoding="utf-8")
    (root / "mvnw.cmd").write_text("", encoding="utf-8")
    plan = detect_build(root, source)
    assert plan and plan.tool == "maven" and Path(plan.executable).name == "mvnw.cmd"
    primary, cwd, fallback, fallback_cwd = _maven_commands(plan, "compile")
    assert "-pl" in primary and "-am" in primary and cwd == root and fallback_cwd == module


@pytest.mark.unit
def test_gradle_subproject_command_and_java_target(tmp_path):
    root = tmp_path / "g"; module = root / "sub"; source = module / "src/main/java/x/X.java"
    source.parent.mkdir(parents=True); source.write_text("class X {}", encoding="utf-8")
    (root / "settings.gradle").write_text("include 'sub'", encoding="utf-8")
    (module / "build.gradle").write_text("sourceCompatibility = JavaVersion.VERSION_17", encoding="utf-8")
    plan = detect_build(root, source)
    assert plan and plan.tool == "gradle" and _java_target(module, "gradle") == "17"
    primary, cwd, fallback, fallback_cwd = _gradle_commands(plan, "test_compile")
    assert primary[-1] == ":sub:testClasses" and cwd == root and fallback[-1] == "testClasses" and fallback_cwd == module


@pytest.mark.unit
def test_java_target_and_workspace_path_security(tmp_path):
    pom = tmp_path / "pom.xml"; pom.write_text("<maven.compiler.release>21</maven.compiler.release>", encoding="utf-8")
    assert _java_target(tmp_path, "maven") == "21"
    assert _path_candidates(tmp_path, "../secret.java") == []
    assert _path_candidates(tmp_path, "C:/secret.java") == []
    assert _path_candidates(tmp_path, "src/main/X.java")
    assert _path_candidates(tmp_path, "X.java") == [tmp_path / "X.java"]


@pytest.mark.unit
@pytest.mark.parametrize(("source", "test_source", "expected"), [
    ("", "import org.junit.jupiter.api.Test;", "junit5"),
    ("", "import org.junit.Test;", "junit4"),
    ("", "import org.testng.annotations.Test;", "testng"),
    ("", "", "unknown"),
])
def test_test_framework_detection(source, test_source, expected):
    assert _framework(source, test_source) == expected


@pytest.mark.unit
def test_probe_source_covers_constructor_static_and_instance_calls():
    java = parse_java_source("package x; public class X { public X() {} public void run(int n) {} public static void all() {} }", "src/main/java/x/X.java")
    constructor = next(api for api in java.apis if api.kind == "constructor")
    instance = next(api for api in java.apis if api.name == "run")
    static = next(api for api in java.apis if api.name == "all")
    assert "new X();" in _probe_source(java, constructor, "Probe", "junit4")
    assert "value.run(0);" in _probe_source(java, instance, "Probe", "junit5")
    assert "X.all();" in _probe_source(java, static, "Probe", "testng")


@pytest.mark.unit
def test_process_runner_covers_success_missing_tool_and_timeout(tmp_path):
    success = _run([sys.executable, "-c", "print('ok')"], tmp_path, 5, tmp_path / "success.log")
    missing = _run(["preflight-definitely-missing-tool"], tmp_path, 5, tmp_path / "missing.log")
    timeout = _run([sys.executable, "-c", "import time; time.sleep(2)"], tmp_path, 1, tmp_path / "timeout.log")
    assert success.exit_code == 0 and "ok" in (tmp_path / "success.log").read_text(encoding="utf-8")
    assert missing.exit_code == 127
    assert timeout.timed_out


@pytest.mark.unit
def test_process_runner_handles_timeout_before_process_creation(tmp_path, monkeypatch):
    monkeypatch.setattr("preflight.runner.subprocess.Popen", lambda *args, **kwargs: (_ for _ in ()).throw(subprocess.TimeoutExpired(args[0], 1, output="partial", stderr="error")))
    result = _run(["tool"], tmp_path, 1, tmp_path / "early-timeout.log")
    assert result.timed_out and "partial" in (tmp_path / "early-timeout.log").read_text(encoding="utf-8")


@pytest.mark.unit
def test_java_environment_mapping_and_parent_api_resolution(tmp_path, monkeypatch):
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "db", tmp_path, tmp_path, tmp_path, 1, 1, 1, False, "", {"17": str(tmp_path / "jdk")}, {})
    assert _java_env("17", config)[2] == "JDK_HOME_MISSING"
    monkeypatch.setattr("preflight.runner.subprocess.run", lambda *args, **kwargs: type("Result", (), {"stderr": 'openjdk version "17"\n'})())
    assert _java_env(None, config)[1].startswith("openjdk")
    root = tmp_path / "project"; child_path = root / "src/main/java/x/Child.java"; parent_path = root / "src/main/java/x/Parent.java"
    child_path.parent.mkdir(parents=True); child_path.write_text("package x; public class Child extends Parent {}", encoding="utf-8")
    parent_path.write_text("package x; public class Parent { public void inherited() {} }", encoding="utf-8")
    child = parse_java_source(child_path.read_text(encoding="utf-8"), child_path.as_posix())
    assert [api.name for api in _resolve_parent_apis(child, child_path, root)] == ["inherited"]


@pytest.mark.unit
def test_class_policy_source_set_exclusions(tmp_path):
    test_java = parse_java_source("class X {}", "src/test/java/x/X.java")
    unknown_java = parse_java_source("class X {}", "X.java")
    assert _class_policy(test_java)[0] == "EXCLUDE_TEST_SOURCE"
    assert _class_policy(unknown_java)[0] == "EXCLUDE_NON_PRODUCTION_SOURCE"
