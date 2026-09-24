from __future__ import annotations

import hashlib
import json
import os
import re
import shutil
import subprocess
import time
import threading
import stat
from collections import Counter, defaultdict
import xml.etree.ElementTree as ET
from concurrent.futures import FIRST_COMPLETED, ThreadPoolExecutor, wait
from dataclasses import dataclass, field
from pathlib import Path, PurePosixPath
from typing import Any, Callable

from .checkpoint import CheckpointStore

from .ingest import evidence_paths
from .java_ast import JavaApi, JavaClass, parse_java_source, primitive_arguments
from .models import BuildAttempt, ClassCandidate, PreflightResult, PreflightStatus, RevisionStatus, RunMode
from .policy import derive_eligibility
from .process import RunCancelled, terminate_process_tree
from .revision import RevisionChoice, choose_revision, ensure_mirror
from .util import safe_relative


DEPENDENCY_MARKERS = ("could not resolve", "could not transfer", "could not find artifact", "unable to resolve dependency", "read timed out", "connection timed out", "unknown host")
JDK_MARKERS = ("release version", "source option", "target option", "invalid target release", "unsupported class file major version", "java_home")
_mirror_locks: dict[str, threading.Lock] = {}
_mirror_locks_guard = threading.Lock()


@dataclass(frozen=True)
class BuildPlan:
    tool: str
    module_root: Path
    invocation_root: Path
    module_path: str
    executable: Path | str


@dataclass(frozen=True)
class ToolConfig:
    run_root: Path
    dataset_dir: Path
    database: Path
    cache_dir: Path
    workspace_dir: Path
    log_dir: Path
    workers: int
    max_revision_candidates: int
    timeout_seconds: int
    keep_workspaces: bool
    default_java_home: str
    java_homes: dict[str, str]
    revision_map: dict[str, RevisionChoice]
    fast_mode: bool = False
    keep_repo_cache: bool = False
    cancel_event: threading.Event | None = None
    prepared_repos: set[str] = field(default_factory=set, compare=False, repr=False)


def _path_candidates(workspace: Path, relative: str) -> list[Path]:
    raw = relative.strip().replace("\\", "/")
    if not raw or raw.startswith("/") or re.match(r"^[A-Za-z]:", raw) or "\x00" in raw:
        return []
    parts = PurePosixPath(raw).parts
    if ".." in parts:
        return []
    base = workspace.resolve()
    primary = (base / Path(*parts)).resolve()
    if not primary.is_relative_to(base):
        return []
    variants = [parts, parts[1:]] if len(parts) > 1 else [parts]
    values = [(base / Path(*variant)).resolve() for variant in variants]
    return [path for path in values if path.is_relative_to(base)]


def _resolve_path(workspace: Path, relative: str) -> Path | None:
    return next((path for path in _path_candidates(workspace, relative) if path.is_file()), None)


def _run(command: list[str], cwd: Path, timeout_seconds: int, log_path: Path, env: dict[str, str] | None = None,
         cancel_event: threading.Event | None = None) -> BuildAttempt:
    started = time.monotonic()
    log_path.parent.mkdir(parents=True, exist_ok=True)
    timed_out = False
    code: int | None = None
    output = ""
    process: subprocess.Popen[str] | None = None
    try:
        if cancel_event and cancel_event.is_set():
            raise RunCancelled()
        process = subprocess.Popen(command, cwd=cwd, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, encoding="utf-8", errors="replace", start_new_session=os.name != "nt")
        deadline = time.monotonic() + timeout_seconds
        while True:
            if cancel_event and cancel_event.is_set():
                terminate_process_tree(process)
                stdout, stderr = process.communicate()
                output = f"$ {' '.join(command)}\n\nCANCELLED\n{stdout}\n{stderr}"
                log_path.write_text(output, encoding="utf-8")
                raise RunCancelled()
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise subprocess.TimeoutExpired(command, timeout_seconds)
            try:
                stdout, stderr = process.communicate(timeout=min(1.0, remaining))
                break
            except subprocess.TimeoutExpired:
                continue
        code = process.returncode
        output = f"$ {' '.join(command)}\n\nSTDOUT\n{stdout}\n\nSTDERR\n{stderr}"
    except subprocess.TimeoutExpired as exc:
        timed_out = True
        # Build tools launch descendants, so a timeout must stop the process tree.
        if process is not None:
            if os.name == "nt":
                terminate_process_tree(process)
            else:
                terminate_process_tree(process)
            stdout, stderr = process.communicate()
        else:
            stdout, stderr = exc.stdout or "", exc.stderr or ""
        output = f"$ {' '.join(command)}\n\nTIMEOUT after {timeout_seconds}s\n{stdout}\n{stderr}"
    except FileNotFoundError as exc:
        code = 127
        output = f"$ {' '.join(command)}\n\nEXECUTABLE_NOT_FOUND\n{exc}"
    log_path.write_text(output, encoding="utf-8")
    return BuildAttempt("", str(cwd), command, code, timed_out, round(time.monotonic() - started, 3), str(log_path))


def _output_of(attempt: BuildAttempt) -> str:
    try:
        return Path(attempt.log_path).read_text(encoding="utf-8", errors="replace").lower()
    except OSError:
        return ""


def _failure_status(attempt: BuildAttempt, stage: str) -> str:
    if attempt.timed_out:
        return PreflightStatus.BUILD_TIMEOUT
    output = _output_of(attempt)
    if "executable_not_found" in output or "is not recognized" in output or "no such file or directory" in output:
        return PreflightStatus.BUILD_TOOL_UNSUPPORTED
    if any(item in output for item in DEPENDENCY_MARKERS):
        return PreflightStatus.DEPENDENCY_UNAVAILABLE
    if any(item in output for item in JDK_MARKERS):
        return PreflightStatus.JDK_UNSUPPORTED
    return PreflightStatus.MAIN_BUILD_FAILED if stage == "main_compile" else PreflightStatus.TEST_COMPILE_FAILED


def _find_up(start: Path, stop: Path, names: tuple[str, ...]) -> Path | None:
    current = start if start.is_dir() else start.parent
    stop = stop.resolve()
    while True:
        if any((current / name).is_file() for name in names):
            return current
        if current.resolve() == stop or current.parent == current:
            return None
        current = current.parent


def _wrapper(root: Path, names: tuple[str, ...], fallback: str, workspace: Path) -> Path | str:
    for directory in (root, *_ancestors(root)):
        if not directory.resolve().is_relative_to(workspace.resolve()):
            break
        for name in names:
            path = directory / name
            if path.is_file():
                return path
    return fallback


def _ancestors(root: Path):
    current = root.parent
    while current != current.parent:
        yield current
        current = current.parent


def detect_build(workspace: Path, class_file: Path) -> BuildPlan | None:
    module = _find_up(class_file, workspace, ("pom.xml",))
    if module:
        root = module
        # Highest POM is the reactor candidate; Maven will reject it if it is
        # not an aggregator, in which case the caller falls back to module POM.
        for parent in _ancestors(module):
            if parent == workspace.parent:
                break
            if (parent / "pom.xml").is_file() and str(parent).startswith(str(workspace)):
                root = parent
        rel = "." if module == root else module.relative_to(root).as_posix()
        wrapper = ("mvnw.cmd",) if os.name == "nt" else ("mvnw",)
        return BuildPlan("maven", module, root, rel, _wrapper(root, wrapper, "mvn", workspace))
    module = _find_up(class_file, workspace, ("build.gradle", "build.gradle.kts"))
    if module:
        root = _find_up(module, workspace, ("settings.gradle", "settings.gradle.kts")) or module
        rel = "." if module == root else module.relative_to(root).as_posix()
        wrapper = ("gradlew.bat",) if os.name == "nt" else ("gradlew",)
        return BuildPlan("gradle", module, root, rel, _wrapper(root, wrapper, "gradle", workspace))
    return None


def _java_target(module_root: Path, tool: str) -> str | None:
    if tool == "maven":
        pom = module_root / "pom.xml"
        if not pom.is_file():
            return None
        text = pom.read_text(encoding="utf-8", errors="replace")
        for key in ("maven.compiler.release", "maven.compiler.target", "java.version"):
            match = re.search(rf"<{re.escape(key)}>([^<]+)</{re.escape(key)}>", text)
            if match:
                return match.group(1).strip().removeprefix("1.")
    else:
        path = module_root / "build.gradle"
        if not path.is_file():
            path = module_root / "build.gradle.kts"
        if path.is_file():
            text = path.read_text(encoding="utf-8", errors="replace")
            match = re.search(r"(?:sourceCompatibility|targetCompatibility|JavaVersion\.VERSION_)[^0-9]*(?:VERSION_)?(\d+)", text)
            if match:
                return match.group(1)
    return None


def _java_env(target: str | None, config: ToolConfig) -> tuple[dict[str, str], str, str | None]:
    selected = config.java_homes.get(str(target or ""), config.default_java_home)
    if selected and not Path(selected).is_dir():
        return {}, "", "JDK_HOME_MISSING"
    env = dict(os.environ)
    if selected:
        env["JAVA_HOME"] = selected
        env["PATH"] = str(Path(selected) / "bin") + os.pathsep + env.get("PATH", "")
    bin_dir = Path(selected) / "bin" if selected else None
    suffix = ".exe" if os.name == "nt" else ""
    java = str(bin_dir / f"java{suffix}") if bin_dir else shutil.which("java", path=env.get("PATH"))
    javac = str(bin_dir / f"javac{suffix}") if bin_dir else shutil.which("javac", path=env.get("PATH"))
    if not java or (bin_dir and not Path(java).is_file()):
        return {}, "", "JDK_JAVA_MISSING"
    if not javac or (bin_dir and not Path(javac).is_file()):
        return {}, "", "JDK_JAVAC_MISSING"
    try:
        java_result = subprocess.run([java, "-version"], env=env, capture_output=True, text=True, encoding="utf-8", errors="replace")
        javac_result = subprocess.run([javac, "-version"], env=env, capture_output=True, text=True, encoding="utf-8", errors="replace")
    except OSError:
        return {}, "", "JDK_VERSION_COMMAND_FAILED"
    if java_result.returncode != 0 or javac_result.returncode != 0:
        return {}, "", "JDK_VERSION_COMMAND_FAILED"
    java_output = "\n".join((java_result.stderr, java_result.stdout))
    javac_output = "\n".join((javac_result.stdout, javac_result.stderr))
    java_match = re.search(r"(?:openjdk|java)\s+(?:version\s+)?[\"']?(?:1\.)?(\d+)", java_output, re.I)
    javac_match = re.search(r"\bjavac\s+(?:1\.)?(\d+)", javac_output, re.I)
    if not java_match or not javac_match:
        return {}, "", "JDK_VERSION_UNPARSEABLE"
    java_version = next((line.strip() for line in java_output.splitlines() if java_match.group(0) in line), java_match.group(0))
    if java_match.group(1) != javac_match.group(1) or (target and java_match.group(1) != str(target).removeprefix("1.")):
        return {}, java_version, "JDK_VERSION_MISMATCH"
    return env, java_version, None


def _maven_commands(plan: BuildPlan, phase: str) -> tuple[list[str], Path, list[str], Path]:
    executable = str(plan.executable)
    if plan.module_path == ".":
        command = [executable, "-q", "-DskipTests", phase]
        return command, plan.module_root, command, plan.module_root
    root_command = [executable, "-q", "-pl", plan.module_path, "-am", "-DskipTests", phase]
    fallback = [executable, "-q", "-DskipTests", phase]
    return root_command, plan.invocation_root, fallback, plan.module_root


def _gradle_commands(plan: BuildPlan, phase: str) -> tuple[list[str], Path, list[str], Path]:
    executable = str(plan.executable)
    task = "classes" if phase == "main_compile" else "testClasses"
    if plan.module_path == ".":
        command = [executable, "--no-daemon", task]
        return command, plan.module_root, command, plan.module_root
    project = ":" + ":".join(Path(plan.module_path).parts)
    return [executable, "--no-daemon", f"{project}:{task}"], plan.invocation_root, [executable, "--no-daemon", task], plan.module_root


def _attempt_log(config: ToolConfig, task_id: str, attempt_number: int, name: str) -> Path:
    return config.log_dir / task_id / f"attempt-{attempt_number:03d}" / name


def _compile(plan: BuildPlan, phase: str, config: ToolConfig, task_id: str, env: dict[str, str], attempt_number: int = 1) -> list[BuildAttempt]:
    commands = _maven_commands(plan, "compile" if phase == "main_compile" else "test-compile") if plan.tool == "maven" else _gradle_commands(plan, phase)
    primary, primary_cwd, fallback, fallback_cwd = commands
    log = _attempt_log(config, task_id, attempt_number, f"{phase}.log")
    first = (_run(primary, primary_cwd, config.timeout_seconds, log, env, config.cancel_event)
             if config.cancel_event else _run(primary, primary_cwd, config.timeout_seconds, log, env)); first.stage = phase
    attempts = [first]
    if first.exit_code not in (0, None) and fallback != primary:
        text = _output_of(first)
        if "could not find the selected project" in text or "project" in text and "not found" in text:
            retry_log = _attempt_log(config, task_id, attempt_number, f"{phase}_fallback.log")
            retry = (_run(fallback, fallback_cwd, config.timeout_seconds, retry_log, env, config.cancel_event)
                     if config.cancel_event else _run(fallback, fallback_cwd, config.timeout_seconds, retry_log, env)); retry.stage = f"{phase}_fallback"; attempts.append(retry)
    return attempts


def _test_source_root(class_file: Path, module_root: Path) -> Path:
    parts = class_file.parts
    lower = [part.lower() for part in parts]
    for index in range(len(parts) - 2):
        if lower[index:index + 3] == ["src", "main", "java"]:
            return Path(*parts[:index], "src", "test", "java")
    return module_root / "src" / "test" / "java"


def _framework(source: str, test_source: str) -> str:
    joined = source + "\n" + test_source
    if "org.junit.jupiter" in joined:
        return "junit5"
    if "org.junit.Test" in joined or "junit.framework" in joined:
        return "junit4"
    if "org.testng" in joined:
        return "testng"
    return "unknown"


def _probe_source(java: JavaClass, api: JavaApi, class_name: str, framework: str) -> str:
    package = f"package {java.package};\n\n" if java.package else ""
    annotation = {"junit5": ("import org.junit.jupiter.api.Test;\n\n", "@Test\n    void probe()"), "junit4": ("import org.junit.Test;\n\n", "@Test\n    public void probe()"), "testng": ("import org.testng.annotations.Test;\n\n", "@Test\n    public void probe()")} .get(framework, ("", "void probe()"))
    args = primitive_arguments(api.parameters)
    if api.kind == "constructor":
        call = f"new {java.name}({args});"
    elif api.is_static:
        call = f"{java.name}.{api.name}({args});"
    else:
        call = f"{java.name} value = null;\n        value.{api.name}({args});"
    return f"{package}{annotation[0]}final class {class_name} {{\n    {annotation[1]} {{\n        {call}\n    }}\n}}\n"


def _resolve_parent_apis(java: JavaClass, class_file: Path, workspace: Path, seen: set[str] | None = None) -> list[JavaApi]:
    """Count source-resolvable inherited APIs; Object methods are intentionally excluded."""
    if not java.extends:
        return []
    seen = seen or set()
    parent_simple = re.sub(r"<.*", "", java.extends).split(".")[-1].strip()
    if not parent_simple or parent_simple in seen or parent_simple == "Object":
        return []
    seen.add(parent_simple)
    test_root = _test_source_root(class_file, workspace)
    source_root = Path(str(test_root).replace("src" + os.sep + "test" + os.sep + "java", "src" + os.sep + "main" + os.sep + "java"))
    candidates = [source_root / (java.package.replace(".", "/")) / f"{parent_simple}.java"]
    for imported in java.imports:
        if imported.rstrip(".*").endswith(parent_simple):
            package = imported.rsplit(".", 1)[0]
            candidates.append(source_root / package.replace(".", "/") / f"{parent_simple}.java")
    for candidate in candidates:
        if not candidate.resolve().is_relative_to(workspace.resolve()) or not candidate.is_file():
            continue
        parent = parse_java_source(candidate.read_text(encoding="utf-8", errors="replace"), candidate.as_posix())
        if not parent:
            continue
        allowed = [api for api in parent.apis if api.kind == "method" and ("public" in parent.modifiers or parent.package == java.package)]
        return allowed + _resolve_parent_apis(parent, candidate, workspace, seen)
    return []


def _class_policy(java: JavaClass) -> tuple[str | None, list[str], list[str]]:
    tags = sorted(java.tags)
    reasons: list[str] = []
    if java.source_set == "test": return "EXCLUDE_TEST_SOURCE", tags, ["EXCLUDE_TEST_SOURCE"]
    if java.source_set != "production": return "EXCLUDE_NON_PRODUCTION_SOURCE", tags, ["EXCLUDE_NON_PRODUCTION_SOURCE"]
    if not java.is_top_level: return "EXCLUDE_UNSUPPORTED_DECLARATION", tags, ["EXCLUDE_UNSUPPORTED_DECLARATION"]
    if java.kind in {"interface", "annotation"}: return "EXCLUDE_UNSUPPORTED_DECLARATION", tags, ["EXCLUDE_UNSUPPORTED_DECLARATION"]
    if java.kind in {"enum", "record"}: return "EXCLUDE_STRICT_COHORT", tags, ["EXCLUDE_STRICT_COHORT"]
    if "abstract" in java.modifiers: return "EXCLUDE_ABSTRACT_CLASS", tags, ["EXCLUDE_ABSTRACT_CLASS"]
    if "GENERATED_SOURCE" in java.tags: return "EXCLUDE_GENERATED_SOURCE", tags, ["EXCLUDE_GENERATED_SOURCE"]
    return None, tags, reasons


def _result_from(candidate: ClassCandidate) -> PreflightResult:
    return PreflightResult(task_id=candidate.task_id, source_json_path=candidate.source_json_path, source_json_sha256=candidate.source_json_sha256, source_json_paths=list(candidate.source_json_paths), source_json_sha256s=list(candidate.source_json_sha256s), repo_url=candidate.repo_url, class_path=candidate.class_path, class_fqn=candidate.class_fqn, test_class_path=candidate.test_class_paths[0] if candidate.test_class_paths else "")


def _worktree(mirror: Path, destination: Path, sha: str) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["git", "--git-dir", str(mirror), "worktree", "add", "--detach", "--force", str(destination), sha], check=True, capture_output=True, text=True, encoding="utf-8", errors="replace")


def _mirror_lock(repo_url: str) -> threading.Lock:
    with _mirror_locks_guard:
        return _mirror_locks.setdefault(repo_url, threading.Lock())


def _remove_worktree(mirror: Path, destination: Path) -> None:
    subprocess.run(["git", "--git-dir", str(mirror), "worktree", "remove", "--force", str(destination)], check=False, capture_output=True)
    if destination.exists():
        shutil.rmtree(destination, ignore_errors=True)


def _mirror_path(repo_url: str, config: ToolConfig) -> Path:
    return config.cache_dir / f"{hashlib.sha256(repo_url.encode()).hexdigest()}.git"


def _cleanup_repo(repo_url: str, tasks: list[str], config: ToolConfig, *, write_report: bool = True) -> dict[str, Any]:
    """Called by the coordinator only after every future for this repo finishes."""
    mirror = _mirror_path(repo_url, config)
    event = {"repo_url": repo_url, "mirror_path": str(mirror), "class_count": len(tasks)}
    with _mirror_lock(repo_url):
        try:
            if config.keep_repo_cache or config.keep_workspaces:
                event["status"] = "KEPT"
                event["reason"] = "keep_workspaces" if config.keep_workspaces else "keep_repo_cache"
            else:
                root = config.run_root.absolute()
                expected = root / "cache" / "mirrors"
                if (config.cache_dir.absolute() != expected or
                        any(p.is_symlink() for p in (root, root / "cache", expected, mirror)) or
                        mirror.resolve().parent != expected.resolve()):
                    raise ValueError("UNSAFE_CACHE_PATH: only the run-owned cache/mirrors child may be deleted")
                leftovers = [task for task in tasks if (config.workspace_dir / task).exists() or (config.workspace_dir / task).is_symlink()]
                if leftovers:
                    event.update(status="KEPT", reason="WORKSPACE_CLEANUP_INCOMPLETE", remaining_tasks=leftovers)
                elif not mirror.exists():
                    event["status"] = "ABSENT"
                else:
                    def retry_readonly(function, path, exc_info):
                        if not isinstance(exc_info[1], PermissionError) or Path(path).is_symlink():
                            raise exc_info[1]
                        os.chmod(path, stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)
                        function(path)
                    shutil.rmtree(mirror, onerror=retry_readonly)
                    event["status"] = "DELETED"
        except (OSError, ValueError) as exc:
            event.update(status="FAILED", error=str(exc))
    if write_report:
        report = config.run_root / "reports" / "repo_cleanup.jsonl"
        report.parent.mkdir(parents=True, exist_ok=True)
        with report.open("a", encoding="utf-8") as handle:
            handle.write(json.dumps(event, ensure_ascii=False) + "\n")
    return event


def preflight_one(candidate: ClassCandidate, config: ToolConfig, attempt_number: int = 1) -> PreflightResult:
    started = time.monotonic(); result = _result_from(candidate); workspace: Path | None = None; mirror: Path | None = None
    try:
        with _mirror_lock(candidate.repo_url):
            mirror_path = _mirror_path(candidate.repo_url, config)
            if candidate.repo_url not in config.prepared_repos:
                mirror = (ensure_mirror(candidate.repo_url, mirror_path, config.cancel_event)
                          if config.cancel_event else ensure_mirror(candidate.repo_url, mirror_path))
                config.prepared_repos.add(candidate.repo_url)
            else:
                mirror = mirror_path
        choice_args = (mirror, candidate, config.dataset_dir, evidence_paths(config.database, candidate.task_id), config.revision_map.get(candidate.task_id), config.max_revision_candidates)
        choice = choose_revision(*choice_args, config.cancel_event) if config.cancel_event else choose_revision(*choice_args)
        result.checkout_sha, result.revision_provenance, result.revision_verification_status, result.content_match = choice.checkout_sha, choice.provenance, choice.status, choice.content_match
        if choice.evidence_ref:
            result.content_match = {"evidence_ref": choice.evidence_ref}
        result.run_mode = RunMode.STRICT if choice.status == RevisionStatus.UPSTREAM_PINNED else RunMode.DISCOVERY_ONLY
        if choice.status != RevisionStatus.UPSTREAM_PINNED: result.reason_codes.append(f"REVISION_{choice.status}")
        workspace = config.workspace_dir / candidate.task_id
        with _mirror_lock(candidate.repo_url):
            workspace_root = config.workspace_dir.resolve()
            if workspace.is_symlink() or not workspace.resolve().is_relative_to(workspace_root):
                raise ValueError("UNSAFE_STALE_WORKSPACE")
            if workspace.exists():
                _remove_worktree(mirror, workspace)
            _worktree(mirror, workspace, choice.checkout_sha)
        result.working_directory = str(workspace)
        class_file = _resolve_path(workspace, candidate.class_path)
        if not class_file:
            result.preflight_status = PreflightStatus.SOURCE_INVALID; result.reason_codes.append("SOURCE_PATH_NOT_FOUND"); return result
        source = class_file.read_text(encoding="utf-8", errors="replace")
        java = parse_java_source(source, candidate.class_path, expected_name=candidate.class_name)
        if not java or java.name != candidate.class_name:
            result.preflight_status = PreflightStatus.SOURCE_INVALID; result.reason_codes.append("SOURCE_FQN_MISMATCH"); return result
        result.class_fqn = java.fqn
        policy, tags, reasons = _class_policy(java); result.tags = tags; result.reason_codes.extend(reasons)
        plan = detect_build(workspace, class_file)
        if not plan:
            result.preflight_status = PreflightStatus.BUILD_TOOL_UNSUPPORTED; result.reason_codes.append("BUILD_TOOL_UNSUPPORTED"); return result
        result.build_tool, result.module_path = plan.tool, plan.module_path
        version_args = ([str(plan.executable), "--version"], plan.invocation_root, min(60, config.timeout_seconds), _attempt_log(config, candidate.task_id, attempt_number, "tool_version.log"))
        version = _run(*version_args, cancel_event=config.cancel_event) if config.cancel_event else _run(*version_args)
        version.stage = "tool_version"; result.build_attempts.append(version); result.log_paths.append(version.log_path)
        result.build_tool_version = _output_of(version).splitlines()[2] if len(_output_of(version).splitlines()) > 2 else "unknown"
        env, result.java_version, java_error = _java_env(_java_target(plan.module_root, plan.tool), config)
        if java_error:
            result.preflight_status = PreflightStatus.JDK_UNSUPPORTED; result.reason_codes.append(java_error); return result
        main = (_compile(plan, "main_compile", config, candidate.task_id, env, attempt_number)
                if attempt_number != 1 else _compile(plan, "main_compile", config, candidate.task_id, env)); result.build_attempts.extend(main); result.log_paths.extend(item.log_path for item in main)
        if main[-1].exit_code != 0:
            result.preflight_status = _failure_status(main[-1], "main_compile"); result.reason_codes.append(result.preflight_status); return result
        test = (_compile(plan, "test_compile", config, candidate.task_id, env, attempt_number)
                if attempt_number != 1 else _compile(plan, "test_compile", config, candidate.task_id, env)); result.build_attempts.extend(test); result.log_paths.extend(item.log_path for item in test)
        if test[-1].exit_code != 0:
            result.preflight_status = _failure_status(test[-1], "test_compile"); result.reason_codes.append(result.preflight_status); return result
        if policy:
            result.preflight_status = PreflightStatus.EXCLUDED; return result
        inherited = _resolve_parent_apis(java, class_file, workspace)
        apis = [*java.apis, *[api for api in inherited if api.name not in {item.name for item in java.apis}]]
        constructors = [api for api in apis if api.kind == "constructor"]; methods = [api for api in apis if api.kind == "method"]
        result.constructor_count, result.method_count, result.api_count = len(constructors), len(methods), len(apis)
        if not apis:
            result.preflight_status = PreflightStatus.EXCLUDED; result.reason_codes.append("EXCLUDE_NO_TESTABLE_API"); return result
        if config.fast_mode:
            result.preflight_status = PreflightStatus.PRECHECKED
            result.probe_status = "NOT_RUN"
            result.technical_eligible, result.strict_eligible = derive_eligibility(result.preflight_status, result.run_mode, result.revision_verification_status)
            return result
        test_text = ""
        for test_path in candidate.test_class_paths:
            local_test = _resolve_path(workspace, test_path)
            if local_test: test_text += local_test.read_text(encoding="utf-8", errors="replace")
        result.testing_framework = _framework(source, test_text)
        probe_api = next((api for api in methods if api.is_static), None) or next((api for api in methods if not api.is_static), None) or constructors[0]
        result.probed_api = probe_api.signature
        probe_name = f"PreflightProbe_{candidate.task_id[-8:]}"; probe_root = _test_source_root(class_file, plan.module_root); package_dir = probe_root / Path(*java.package.split(".")) if java.package else probe_root
        probe_path = package_dir / f"{probe_name}.java"
        if not probe_path.resolve().is_relative_to(workspace.resolve()):
            result.preflight_status = PreflightStatus.SOURCE_INVALID; result.reason_codes.append("PROBE_PATH_OUTSIDE_WORKSPACE"); return result
        probe_path.parent.mkdir(parents=True, exist_ok=True); probe_path.write_text(_probe_source(java, probe_api, probe_name, result.testing_framework), encoding="utf-8")
        try:
            probe = (_compile(plan, "probe_compile", config, candidate.task_id, env, attempt_number)
                     if attempt_number != 1 else _compile(plan, "probe_compile", config, candidate.task_id, env)); result.build_attempts.extend(probe); result.log_paths.extend(item.log_path for item in probe)
        finally:
            probe_path.unlink(missing_ok=True)
        if probe[-1].exit_code != 0:
            result.probe_status = "FAILED"; result.preflight_status = PreflightStatus.PROBE_FAILED; result.reason_codes.append("PROBE_FAILED"); return result
        result.probe_status = "PASSED"
        if "DIRECT_CONNECTION" in result.tags:
            result.preflight_status = PreflightStatus.NEEDS_REVIEW; result.reason_codes.append("DIRECT_CONNECTION_NEEDS_REVIEW"); return result
        if result.testing_framework == "unknown":
            result.preflight_status = PreflightStatus.NEEDS_REVIEW; result.reason_codes.append("TEST_FRAMEWORK_UNKNOWN"); return result
        result.preflight_status = PreflightStatus.ELIGIBLE
        result.technical_eligible, result.strict_eligible = derive_eligibility(result.preflight_status, result.run_mode, result.revision_verification_status)
    except RunCancelled:
        raise
    except subprocess.CalledProcessError as exc:
        result.preflight_status = PreflightStatus.CLONE_FAILED if mirror is None else PreflightStatus.CHECKOUT_FAILED
        result.reason_codes.append(result.preflight_status)
    except LookupError as exc:
        result.preflight_status = PreflightStatus.COMMIT_MISSING; result.reason_codes.append(str(exc))
    except Exception as exc:  # Retain row-level isolation for batch runs.
        result.preflight_status = PreflightStatus.SOURCE_INVALID; result.reason_codes.append(f"UNHANDLED_{type(exc).__name__}")
    finally:
        result.duration_seconds = round(time.monotonic() - started, 3)
        if workspace and workspace.exists() and mirror and not config.keep_workspaces:
            with _mirror_lock(candidate.repo_url):
                _remove_worktree(mirror, workspace)
    return result


def run_all(candidates: list[ClassCandidate], config: ToolConfig, checkpoint: CheckpointStore | None = None,
            session_id: int | None = None, on_checkpoint: Callable[[], None] | None = None) -> list[PreflightResult]:
    results: list[PreflightResult] = []
    workers = max(1, config.workers)
    remaining = Counter(candidate.repo_url for candidate in candidates)
    repo_tasks = checkpoint.all_tasks_for_repos() if checkpoint else defaultdict(list)
    if checkpoint is None:
        for candidate in candidates:
            repo_tasks[candidate.repo_url].append(candidate.task_id)
    pending = iter(candidates)
    executor = ThreadPoolExecutor(max_workers=workers)
    futures = {}
    try:
        def submit(candidate: ClassCandidate) -> None:
            attempt = checkpoint.start_task(candidate.task_id, session_id) if checkpoint and session_id is not None else 1
            future = executor.submit(preflight_one, candidate, config, attempt) if checkpoint else executor.submit(preflight_one, candidate, config)
            futures[future] = (candidate, attempt)

        for _ in range(min(len(candidates), workers * 2)):
            if config.cancel_event and config.cancel_event.is_set():
                break
            submit(next(pending))
        if checkpoint and on_checkpoint and futures:
            on_checkpoint()
        while futures:
            done, _ = wait(futures, return_when=FIRST_COMPLETED)
            checkpoint_changed = False
            for future in done:
                completed, attempt = futures.pop(future)
                try:
                    result = future.result()
                except RunCancelled:
                    if checkpoint:
                        checkpoint.interrupt_task(completed.task_id, attempt)
                        checkpoint_changed = True
                    continue
                results.append(result)
                if checkpoint:
                    checkpoint.complete_task(completed.task_id, attempt, result.to_dict())
                    checkpoint_changed = True
                remaining[completed.repo_url] -= 1
                repo_finished = (completed.repo_url in checkpoint.completed_repos()) if checkpoint else remaining[completed.repo_url] == 0
                if repo_finished:
                    event = (_cleanup_repo(completed.repo_url, repo_tasks[completed.repo_url], config, write_report=False)
                             if checkpoint else _cleanup_repo(completed.repo_url, repo_tasks[completed.repo_url], config))
                    if checkpoint and event is not None:
                        checkpoint.put_cleanup_event(event)
                if not (config.cancel_event and config.cancel_event.is_set()):
                    candidate = next(pending, None)
                    if candidate is not None:
                        submit(candidate)
                        checkpoint_changed = True
            if config.cancel_event and config.cancel_event.is_set():
                for future, (candidate, attempt) in list(futures.items()):
                    if future.cancel() and checkpoint:
                        checkpoint.interrupt_task(candidate.task_id, attempt)
                        futures.pop(future)
                        checkpoint_changed = True
            if checkpoint_changed and on_checkpoint:
                on_checkpoint()
    finally:
        executor.shutdown(wait=True, cancel_futures=True)
    return sorted(results, key=lambda item: item.task_id)
