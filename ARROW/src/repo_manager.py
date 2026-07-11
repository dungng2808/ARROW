from __future__ import annotations

import re
import shutil
import subprocess
import stat
from pathlib import Path

from .fs_utils import ensure_dir


def _retry_remove_readonly(function, path, _exc_info) -> None:
    Path(path).chmod(stat.S_IWRITE)
    function(path)


def safe_remove_tree(path: Path, allowed_root: Path) -> bool:
    """Remove a tree only when it is inside the expected pipeline-owned root."""
    resolved_path = path.resolve()
    resolved_root = allowed_root.resolve()
    try:
        resolved_path.relative_to(resolved_root)
    except ValueError as exc:
        raise ValueError(f"Refusing to remove path outside allowed root: {resolved_path}") from exc
    if not resolved_path.exists():
        return False
    # ``onexc`` only exists in newer Python releases. ``onerror`` keeps cleanup
    # working on the Python 3.10/3.11 versions commonly shipped by Ubuntu while
    # retaining the same read-only-file handling on Windows.
    shutil.rmtree(resolved_path, onerror=_retry_remove_readonly)
    return True


def _workspace_copy_ignore(directory: str, names: list[str]) -> set[str]:
    """Ignore generated outputs without deleting source packages named build.

    ``shutil.ignore_patterns("build")`` matches at every directory depth. That
    corrupts repositories such as cruise-control, whose Gradle buildSrc source
    package is literally ``com/linkedin/gradle/build``.
    """
    current = Path(directory)
    ignored = {name for name in names if name in {".git", ".gradle", "__pycache__"}}
    is_build_root = any((current / marker).is_file() for marker in ("pom.xml", "build.gradle", "build.gradle.kts"))
    if is_build_root:
        ignored.update(name for name in names if name in {"target", "build"})
    return ignored


def _repository_requires_git_metadata(repository: Path) -> bool:
    command_pattern = re.compile(
        r"(?is)(?:commandLine|command)\s*\(?\s*['\"]git['\"].{0,300}\b(?:describe|rev-parse|log|tag)\b"
    )
    candidates = [repository / "build.gradle", repository / "build.gradle.kts", repository / "pom.xml"]
    candidates.extend(repository.glob("**/*.gradle"))
    candidates.extend(repository.glob("**/*.gradle.kts"))
    seen: set[Path] = set()
    for path in candidates:
        if path in seen or not path.is_file() or ".git" in path.parts:
            continue
        seen.add(path)
        text = path.read_text(encoding="utf-8", errors="ignore")
        if command_pattern.search(text):
            return True
    return False


def _ensure_git_history_for_build(repository: Path) -> None:
    if not _repository_requires_git_metadata(repository):
        return
    result = subprocess.run(
        ["git", "rev-parse", "--is-shallow-repository"],
        cwd=repository,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        check=True,
    )
    if result.stdout.strip().lower() == "true":
        subprocess.run(["git", "fetch", "--unshallow", "--tags"], cwd=repository, check=True)


def copy_isolated_workspace(source_repo: Path, experiment_workspace: Path) -> Path:
    """Create a writable per-experiment copy without depending on Git worktrees."""
    if experiment_workspace.exists():
        safe_remove_tree(experiment_workspace, experiment_workspace.parent)
    shutil.copytree(source_repo, experiment_workspace, ignore=_workspace_copy_ignore)
    if _repository_requires_git_metadata(source_repo) and (source_repo / ".git").is_dir():
        shutil.copytree(source_repo / ".git", experiment_workspace / ".git")
    return experiment_workspace


def clone_repo(repo_url: str, destination: Path, checkout: str | None = None) -> Path:
    if destination.exists():
        _ensure_git_history_for_build(destination)
        return destination
    ensure_dir(destination.parent)
    try:
        subprocess.run(["git", "-c", "core.longpaths=true", "clone", "--depth", "1", repo_url, str(destination)], check=True)
    except subprocess.CalledProcessError:
        if destination.exists():
            safe_remove_tree(destination, destination.parent)
        raise
    if checkout:
        subprocess.run(["git", "checkout", checkout], cwd=destination, check=True)
    _ensure_git_history_for_build(destination)
    return destination


def ensure_experiment_workspace(
    *,
    cached_repo: Path,
    experiment_workspace: Path,
) -> Path:
    return copy_isolated_workspace(cached_repo, experiment_workspace)
