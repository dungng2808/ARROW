from __future__ import annotations

import os
import sys
import subprocess
import time
from concurrent.futures import ThreadPoolExecutor, wait
from pathlib import Path
from types import SimpleNamespace

import pytest

from preflight.models import BuildAttempt, PreflightStatus
from preflight.java_ast import JavaApi, parse_java_source
from preflight.runner import BuildPlan, ToolConfig, _class_policy, _failure_status, _framework, _gradle_commands, _java_env, _java_target, _maven_commands, _path_candidates, _probe_source, _resolve_parent_apis, _run, _wrapper, detect_build, run_all


def _attempt(tmp_path: Path, output: str = "", *, timed_out: bool = False) -> BuildAttempt:
    log = tmp_path / "attempt.log"; log.write_text(output, encoding="utf-8")
    return BuildAttempt("main_compile", str(tmp_path), ["tool"], 1, timed_out, 0.1, str(log))


@pytest.mark.unit
@pytest.mark.parametrize(
    ("output", "stage", "expected"),
    [
        ("EXECUTABLE_NOT_FOUND", "main_compile", PreflightStatus.BUILD_TOOL_UNSUPPORTED),
        ("'mvn' is not recognized as an internal or external command", "main_compile", PreflightStatus.BUILD_TOOL_UNSUPPORTED),
        ("no such file or directory", "main_compile", PreflightStatus.BUILD_TOOL_UNSUPPORTED),
        ("Could not resolve dependency", "main_compile", PreflightStatus.DEPENDENCY_UNAVAILABLE),
        ("could not transfer artifact com.acme:lib:pom:1.0", "main_compile", PreflightStatus.DEPENDENCY_UNAVAILABLE),
        ("unable to resolve dependency", "main_compile", PreflightStatus.DEPENDENCY_UNAVAILABLE),
        ("connection timed out", "main_compile", PreflightStatus.DEPENDENCY_UNAVAILABLE),
        ("unknown host repo.maven.apache.org", "main_compile", PreflightStatus.DEPENDENCY_UNAVAILABLE),
        ("invalid target release: 17", "main_compile", PreflightStatus.JDK_UNSUPPORTED),
        ("unsupported class file major version 61", "main_compile", PreflightStatus.JDK_UNSUPPORTED),
        ("JAVA_HOME is set to an invalid directory", "main_compile", PreflightStatus.JDK_UNSUPPORTED),
        ("ordinary compilation failure", "main_compile", PreflightStatus.MAIN_BUILD_FAILED),
        ("ordinary compilation failure", "test_compile", PreflightStatus.TEST_COMPILE_FAILED),
    ],
)
def test_build_failure_classification(tmp_path, output, stage, expected):
    assert _failure_status(_attempt(tmp_path, output), stage) == expected
    assert _failure_status(_attempt(tmp_path, timed_out=True), stage) == PreflightStatus.BUILD_TIMEOUT


@pytest.mark.unit
def test_compile_triggers_fallback_when_reactor_project_not_found(tmp_path, monkeypatch):
    from preflight.runner import _compile
    root = tmp_path / "root"; child = root / "child"
    root.mkdir(); child.mkdir()
    plan = BuildPlan("maven", child, root, "child", "mvn")
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "db", tmp_path, tmp_path, tmp_path / "logs", 1, 1, 1, False, "", {}, {})

    attempts_made = []
    def mock_run(cmd, cwd, timeout, log, env=None):
        log.parent.mkdir(parents=True, exist_ok=True)
        if "-pl" in cmd:
            log.write_text("could not find the selected project in the reactor", encoding="utf-8")
            attempt = BuildAttempt("main_compile", str(cwd), cmd, 1, False, 0.1, str(log))
        else:
            log.write_text("BUILD SUCCESS", encoding="utf-8")
            attempt = BuildAttempt("main_compile_fallback", str(cwd), cmd, 0, False, 0.1, str(log))
        attempts_made.append(attempt)
        return attempt

    monkeypatch.setattr("preflight.runner._run", mock_run)
    results = _compile(plan, "main_compile", config, "task_1", {})
    assert len(results) == 2
    assert results[0].exit_code == 1
    assert results[1].stage == "main_compile_fallback"
    assert results[1].exit_code == 0



@pytest.mark.unit
def test_detect_build_prefers_platform_wrapper_and_module_commands(tmp_path):
    root = tmp_path / "project"; module = root / "child"; source = module / "src/main/java/x/X.java"
    source.parent.mkdir(parents=True); source.write_text("class X {}", encoding="utf-8")
    (root / "pom.xml").write_text("<project/>", encoding="utf-8"); (module / "pom.xml").write_text("<project/>", encoding="utf-8")
    (root / "mvnw.cmd").write_text("", encoding="utf-8")
    (root / "mvnw").write_text("", encoding="utf-8")
    plan = detect_build(root, source)
    assert plan and plan.tool == "maven" and Path(plan.executable).name == ("mvnw.cmd" if os.name == "nt" else "mvnw")
    primary, cwd, fallback, fallback_cwd = _maven_commands(plan, "compile")
    assert "-pl" in primary and "-am" in primary and cwd == root and fallback_cwd == module


@pytest.mark.unit
def test_gradle_subproject_command_and_java_target(tmp_path):
    root = tmp_path / "g"; module = root / "sub"; source = module / "src/main/java/x/X.java"
    source.parent.mkdir(parents=True); source.write_text("class X {}", encoding="utf-8")
    (root / "settings.gradle").write_text("include 'sub'", encoding="utf-8")
    (root / "gradlew.bat").write_text("", encoding="utf-8")
    (root / "gradlew").write_text("", encoding="utf-8")
    (module / "build.gradle").write_text("sourceCompatibility = JavaVersion.VERSION_17", encoding="utf-8")
    plan = detect_build(root, source)
    assert plan and plan.tool == "gradle" and _java_target(module, "gradle") == "17"
    assert Path(plan.executable).name == ("gradlew.bat" if os.name == "nt" else "gradlew")
    primary, cwd, fallback, fallback_cwd = _gradle_commands(plan, "test_compile")
    assert primary[-1] == ":sub:testClasses" and cwd == root and fallback[-1] == "testClasses" and fallback_cwd == module


@pytest.mark.unit
def test_detect_build_does_not_use_wrapper_outside_workspace(tmp_path):
    workspace = tmp_path / "project"; source = workspace / "src/main/java/X.java"
    source.parent.mkdir(parents=True); source.write_text("class X {}", encoding="utf-8")
    (workspace / "pom.xml").write_text("<project/>", encoding="utf-8")
    (tmp_path / ("mvnw.cmd" if os.name == "nt" else "mvnw")).touch()
    plan = detect_build(workspace, source)
    assert plan and plan.executable == "mvn"


@pytest.mark.unit
def test_java_target_and_workspace_path_security(tmp_path):
    pom = tmp_path / "pom.xml"; pom.write_text("<maven.compiler.release>21</maven.compiler.release>", encoding="utf-8")
    assert _java_target(tmp_path, "maven") == "21"
    assert _path_candidates(tmp_path, "../secret.java") == []
    assert _path_candidates(tmp_path, "C:/secret.java") == []
    assert _path_candidates(tmp_path, "src/main/X.java")
    assert _path_candidates(tmp_path, "X.java") == [tmp_path / "X.java"]


@pytest.mark.unit
@pytest.mark.parametrize("relative", ["", "/etc/passwd", "../secret.java", "src/../secret.java", "C:/secret.java", "C:\\secret.java", "C:secret.java", "\\\\server\\share\\secret.java", "\\\\?\\C:\\secret.java", "src/\x00.java"])
def test_path_candidates_reject_unsafe_paths_on_every_platform(tmp_path, relative):
    assert _path_candidates(tmp_path, relative) == []


@pytest.mark.unit
def test_path_candidates_normalize_separators_and_contain_symlinks(tmp_path):
    workspace = tmp_path / "workspace"; workspace.mkdir()
    inside = workspace / "src/main/java/acme/Thing.java"
    inside.parent.mkdir(parents=True); inside.write_text("class Thing {}", encoding="utf-8")
    assert inside in _path_candidates(workspace, "src\\main\\java\\acme\\Thing.java")
    outside = tmp_path / "outside"; outside.mkdir()
    (outside / "secret.java").write_text("secret", encoding="utf-8")
    (workspace / "secret.java").write_text("inside", encoding="utf-8")
    try:
        (workspace / "linked").symlink_to(outside, target_is_directory=True)
    except OSError:
        pytest.skip("directory symlinks unavailable")
    assert _path_candidates(workspace, "linked/secret.java") == []


@pytest.mark.unit
def test_inherited_source_does_not_follow_symlink_outside_workspace(tmp_path):
    workspace = tmp_path / "workspace"
    source_root = workspace / "src/main/java/acme"; source_root.mkdir(parents=True)
    child_path = source_root / "Child.java"
    child_path.write_text("package acme; public class Child extends Parent {}", encoding="utf-8")
    outside = tmp_path / "outside.java"; outside.write_text("package acme; public class Parent { public void leaked() {} }", encoding="utf-8")
    try:
        (source_root / "Parent.java").symlink_to(outside)
    except OSError:
        pytest.skip("file symlinks unavailable")
    child = parse_java_source(child_path.read_text(encoding="utf-8"), child_path.as_posix())
    assert _resolve_parent_apis(child, child_path, workspace) == []


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
def test_timeout_stops_child_process_on_host_platform(tmp_path):
    psutil = pytest.importorskip("psutil")
    pid_file = tmp_path / "child.pid"
    script = f"import subprocess, sys, time; child = subprocess.Popen([sys.executable, '-c', 'import time; time.sleep(30)']); open({str(pid_file)!r}, 'w').write(str(child.pid)); time.sleep(30)"
    attempt = _run([sys.executable, "-c", script], tmp_path, 1, tmp_path / "timeout-tree.log")
    assert attempt.timed_out and pid_file.is_file()
    child_pid = int(pid_file.read_text(encoding="utf-8"))
    def running() -> bool:
        try:
            return psutil.Process(child_pid).status() != psutil.STATUS_ZOMBIE
        except psutil.NoSuchProcess:
            return False
    try:
        deadline = time.monotonic() + 5
        while time.monotonic() < deadline and running():
            time.sleep(0.05)
        assert not running()
    finally:
        if running():
            psutil.Process(child_pid).kill()


@pytest.mark.unit
def test_java_environment_mapping_and_parent_api_resolution(tmp_path, monkeypatch):
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "db", tmp_path, tmp_path, tmp_path, 1, 1, 1, False, "", {"17": str(tmp_path / "jdk")}, {})
    assert _java_env("17", config)[2] == "JDK_HOME_MISSING"
    root = tmp_path / "project"; child_path = root / "src/main/java/x/Child.java"; parent_path = root / "src/main/java/x/Parent.java"
    child_path.parent.mkdir(parents=True); child_path.write_text("package x; public class Child extends Parent {}", encoding="utf-8")
    parent_path.write_text("package x; public class Parent { public void inherited() {} }", encoding="utf-8")
    child = parse_java_source(child_path.read_text(encoding="utf-8"), child_path.as_posix())
    assert [api.name for api in _resolve_parent_apis(child, child_path, root)] == ["inherited"]


@pytest.mark.unit
@pytest.mark.parametrize(("target", "java_version", "javac_version", "exit_code", "expected"), [
    ("8", 'java version "1.8.0_402"', "javac 1.8.0_402", 0, None),
    ("11", 'openjdk version "11.0.22"', "javac 11.0.22", 0, None),
    ("17", 'openjdk version "17.0.10"', "javac 17.0.10", 0, None),
    ("21", 'openjdk version "21.0.2"', "javac 21.0.2", 0, None),
    ("21", 'openjdk version "17.0.10"', "javac 17.0.10", 0, "JDK_VERSION_MISMATCH"),
    ("17", 'openjdk version "17.0.10"', "javac 11.0.22", 0, "JDK_VERSION_MISMATCH"),
    ("17", "broken launcher", "javac 17.0.10", 0, "JDK_VERSION_UNPARSEABLE"),
    ("17", 'openjdk version "17.0.10"', "javac 17.0.10", 1, "JDK_VERSION_COMMAND_FAILED"),
])
def test_java_environment_validates_both_tools_and_major_version(tmp_path, monkeypatch, target, java_version, javac_version, exit_code, expected):
    home = tmp_path / "jdk"; (home / "bin").mkdir(parents=True)
    for tool in ("java", "javac"):
        (home / "bin" / f"{tool}{'.exe' if os.name == 'nt' else ''}").touch()
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "db", tmp_path, tmp_path, tmp_path, 1, 1, 1, False, "", {target: str(home)}, {})
    def fake_run(command, **kwargs):
        is_javac = Path(command[0]).stem == "javac"
        return subprocess.CompletedProcess(command, exit_code if not is_javac else 0, "" if not is_javac else javac_version, java_version if not is_javac else "")
    monkeypatch.setattr("preflight.runner.subprocess.run", fake_run)
    assert _java_env(target, config)[2] == expected


@pytest.mark.unit
def test_java_environment_rejects_missing_compiler(tmp_path):
    home = tmp_path / "jdk"; (home / "bin").mkdir(parents=True)
    (home / "bin" / f"java{'.exe' if os.name == 'nt' else ''}").touch()
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "db", tmp_path, tmp_path, tmp_path, 1, 1, 1, False, str(home), {}, {})
    assert _java_env(None, config)[2] == "JDK_JAVAC_MISSING"


@pytest.mark.toolchain
@pytest.mark.parametrize("target", ["8", "11", "17", "21"])
def test_real_jdk_version_matrix_when_configured(tmp_path, target):
    home = os.environ.get(f"PREFLIGHT_JDK_{target}")
    if not home:
        pytest.skip(f"set PREFLIGHT_JDK_{target} to run real JDK check")
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "db", tmp_path, tmp_path, tmp_path, 1, 1, 1, False, "", {target: home}, {})
    env, version, error = _java_env(target, config)
    assert error is None and env["JAVA_HOME"] == home and version


@pytest.mark.unit
def test_run_all_limits_submissions_between_completion_waits(tmp_path, monkeypatch):
    import preflight.runner as runner
    submissions = 0

    class CheckedExecutor(ThreadPoolExecutor):
        def submit(self, *args, **kwargs):
            nonlocal submissions
            submissions += 1
            assert submissions <= 4
            return super().submit(*args, **kwargs)

    def checked_wait(*args, **kwargs):
        nonlocal submissions
        completed = wait(*args, **kwargs)
        submissions = 0
        return completed

    monkeypatch.setattr(runner, "ThreadPoolExecutor", CheckedExecutor)
    monkeypatch.setattr(runner, "wait", checked_wait)
    monkeypatch.setattr(runner, "preflight_one", lambda candidate, config: SimpleNamespace(task_id=candidate))
    config = ToolConfig(tmp_path, tmp_path, tmp_path / "db", tmp_path, tmp_path, tmp_path, 2, 1, 1, False, "", {}, {})
    assert [result.task_id for result in run_all(list(reversed(range(20))), config)] == list(range(20))


@pytest.mark.unit
def test_class_policy_source_set_exclusions(tmp_path):
    test_java = parse_java_source("class X {}", "src/test/java/x/X.java")
    unknown_java = parse_java_source("class X {}", "X.java")
    assert _class_policy(test_java)[0] == "EXCLUDE_TEST_SOURCE"
    assert _class_policy(unknown_java)[0] == "EXCLUDE_NON_PRODUCTION_SOURCE"
