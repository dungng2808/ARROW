from __future__ import annotations

from pathlib import Path

import pytest

from preflight.models import BuildAttempt, ClassCandidate, PreflightStatus, RevisionStatus
from preflight.revision import RevisionChoice
from preflight.runner import BuildPlan, ToolConfig, preflight_one


def _candidate() -> ClassCandidate:
    return ClassCandidate("task", "42", "https://example.test/repo", "src/main/java/x/X.java", "x.X", "X", ("42/a.json",), ("a" * 64,), ("src/test/java/x/XTest.java",))


def _config(tmp_path: Path) -> ToolConfig:
    return ToolConfig(tmp_path / "run", tmp_path, tmp_path / "index.sqlite", tmp_path / "cache", tmp_path / "work", tmp_path / "logs", 1, 5, 5, False, "", {}, {})


def _attempt(path: Path, stage: str, exit_code: int = 0, output: str = "Maven\nversion\n3.9") -> BuildAttempt:
    path.parent.mkdir(parents=True, exist_ok=True); path.write_text(output, encoding="utf-8")
    return BuildAttempt(stage, str(path.parent), ["mvn"], exit_code, False, 0.01, str(path))


def _wire(monkeypatch, tmp_path: Path, source: str, *, test_source: str = "", compile_codes: dict[str, int] | None = None, java_error: str | None = None):
    compile_codes = compile_codes or {}
    monkeypatch.setattr("preflight.runner.ensure_mirror", lambda url, mirror: mirror)
    monkeypatch.setattr("preflight.runner.evidence_paths", lambda database, task_id: [])
    monkeypatch.setattr("preflight.runner.choose_revision", lambda *args: RevisionChoice("a" * 40, "git_history", RevisionStatus.CONTENT_MATCHED))

    def worktree(mirror, destination, sha):
        path = destination / "src/main/java/x/X.java"; path.parent.mkdir(parents=True, exist_ok=True); path.write_text(source, encoding="utf-8")
        test = destination / "src/test/java/x/XTest.java"; test.parent.mkdir(parents=True, exist_ok=True); test.write_text(test_source, encoding="utf-8")
    monkeypatch.setattr("preflight.runner._worktree", worktree)
    monkeypatch.setattr("preflight.runner._remove_worktree", lambda mirror, destination: None)
    monkeypatch.setattr("preflight.runner.detect_build", lambda workspace, class_file: BuildPlan("maven", workspace, workspace, ".", "mvn"))
    monkeypatch.setattr("preflight.runner._run", lambda command, cwd, timeout, log, env=None: _attempt(log, "tool_version"))
    monkeypatch.setattr("preflight.runner._java_env", lambda target, config: ({}, "java", java_error))
    monkeypatch.setattr("preflight.runner._java_target", lambda root, tool: "17")
    def compile_(plan, phase, config, task_id, env):
        return [_attempt(config.log_dir / task_id / f"{phase}.log", phase, compile_codes.get(phase, 0))]
    monkeypatch.setattr("preflight.runner._compile", compile_)


@pytest.mark.unit
@pytest.mark.parametrize(
    ("source", "codes", "test_source", "expected"),
    [
        ("package x; public class X { public void run() {} }", {"main_compile": 1}, "", PreflightStatus.MAIN_BUILD_FAILED),
        ("package x; public class X { public void run() {} }", {"test_compile": 1}, "", PreflightStatus.TEST_COMPILE_FAILED),
        ("package x; public interface X { void run(); }", {}, "", PreflightStatus.EXCLUDED),
        ("package x; public class X { public void run() {} }", {"probe_compile": 1}, "import org.junit.jupiter.api.Test;", PreflightStatus.PROBE_FAILED),
        ("package x; public class X { public void run() {} }", {}, "", PreflightStatus.NEEDS_REVIEW),
        ("package x; public class X { public void run() {} }", {}, "import org.junit.jupiter.api.Test;", PreflightStatus.ELIGIBLE),
        ("package x; import java.sql.DriverManager; public class X { public void run() throws Exception { DriverManager.getConnection(\"jdbc:x\"); } }", {}, "import org.junit.jupiter.api.Test;", PreflightStatus.NEEDS_REVIEW),
    ],
)
def test_preflight_status_paths(monkeypatch, tmp_path, source, codes, test_source, expected):
    _wire(monkeypatch, tmp_path, source, test_source=test_source, compile_codes=codes)
    result = preflight_one(_candidate(), _config(tmp_path))
    assert result.preflight_status == expected, result.reason_codes
    assert result.technical_eligible is (expected == PreflightStatus.ELIGIBLE)
    assert not result.strict_eligible


@pytest.mark.unit
def test_preflight_source_missing_and_jdk_error(monkeypatch, tmp_path):
    _wire(monkeypatch, tmp_path, "package x; public class X {}")
    monkeypatch.setattr("preflight.runner._resolve_path", lambda workspace, path: None)
    assert preflight_one(_candidate(), _config(tmp_path)).preflight_status == PreflightStatus.SOURCE_INVALID
    monkeypatch.undo()
    _wire(monkeypatch, tmp_path, "package x; public class X {}", java_error="JDK_HOME_MISSING")
    result = preflight_one(_candidate(), _config(tmp_path))
    assert result.preflight_status == PreflightStatus.JDK_UNSUPPORTED
    assert "JDK_HOME_MISSING" in result.reason_codes


@pytest.mark.unit
def test_preflight_clone_and_commit_errors_are_isolated(monkeypatch, tmp_path):
    candidate = _candidate(); config = _config(tmp_path)
    monkeypatch.setattr("preflight.runner.evidence_paths", lambda database, task_id: [])
    monkeypatch.setattr("preflight.runner.ensure_mirror", lambda url, mirror: (_ for _ in ()).throw(__import__("subprocess").CalledProcessError(1, "git")))
    assert preflight_one(candidate, config).preflight_status == PreflightStatus.CLONE_FAILED
    monkeypatch.setattr("preflight.runner.ensure_mirror", lambda url, mirror: mirror)
    monkeypatch.setattr("preflight.runner.choose_revision", lambda *args: (_ for _ in ()).throw(LookupError("COMMIT_MISSING")))
    assert preflight_one(candidate, config).preflight_status == PreflightStatus.COMMIT_MISSING
    monkeypatch.setattr("preflight.runner.choose_revision", lambda *args: RevisionChoice("a" * 40, "git_history", RevisionStatus.CONTENT_MATCHED))
    monkeypatch.setattr("preflight.runner._worktree", lambda *args: (_ for _ in ()).throw(__import__("subprocess").CalledProcessError(1, "git")))
    assert preflight_one(candidate, config).preflight_status == PreflightStatus.CHECKOUT_FAILED


@pytest.mark.unit
def test_preflight_build_tool_unsupported_when_no_plan(monkeypatch, tmp_path):
    _wire(monkeypatch, tmp_path, "package x; public class X { public void run() {} }", test_source="import org.junit.jupiter.api.Test;")
    monkeypatch.setattr("preflight.runner.detect_build", lambda workspace, class_file: None)
    result = preflight_one(_candidate(), _config(tmp_path))
    assert result.preflight_status == PreflightStatus.BUILD_TOOL_UNSUPPORTED
    assert "BUILD_TOOL_UNSUPPORTED" in result.reason_codes


@pytest.mark.unit
def test_preflight_strict_eligible_when_pinned_and_eligible(monkeypatch, tmp_path):
    _wire(monkeypatch, tmp_path, "package x; public class X { public void run() {} }", test_source="import org.junit.jupiter.api.Test;")
    monkeypatch.setattr("preflight.runner.choose_revision", lambda *args: RevisionChoice("a" * 40, "upstream_metadata", RevisionStatus.UPSTREAM_PINNED, evidence_ref="test:1"))
    result = preflight_one(_candidate(), _config(tmp_path))
    assert result.preflight_status == PreflightStatus.ELIGIBLE
    assert result.technical_eligible is True
    assert result.strict_eligible is True

