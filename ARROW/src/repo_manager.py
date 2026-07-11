from __future__ import annotations

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


def copy_isolated_workspace(source_repo: Path, experiment_workspace: Path) -> Path:
    """Create a writable per-experiment copy without depending on Git worktrees."""
    if experiment_workspace.exists():
        safe_remove_tree(experiment_workspace, experiment_workspace.parent)
    ignore = shutil.ignore_patterns(".git", "target", "build", ".gradle", "__pycache__")
    shutil.copytree(source_repo, experiment_workspace, ignore=ignore)
    return experiment_workspace


def clone_repo(repo_url: str, destination: Path, checkout: str | None = None) -> Path:
    if destination.exists():
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
    return destination


def ensure_experiment_workspace(
    *,
    cached_repo: Path,
    experiment_workspace: Path,
) -> Path:
    return copy_isolated_workspace(cached_repo, experiment_workspace)
