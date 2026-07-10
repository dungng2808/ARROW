from __future__ import annotations

import re
import json
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import urlparse

from .fs_utils import atomic_write_json
from .models import SampleInput


WINDOWS_RESERVED_NAMES = {
    "CON",
    "PRN",
    "AUX",
    "NUL",
    "COM1",
    "COM2",
    "COM3",
    "COM4",
    "COM5",
    "COM6",
    "COM7",
    "COM8",
    "COM9",
    "LPT1",
    "LPT2",
    "LPT3",
    "LPT4",
    "LPT5",
    "LPT6",
    "LPT7",
    "LPT8",
    "LPT9",
}


@dataclass(frozen=True)
class RepoIdentity:
    owner: str
    name: str
    folder: str


@dataclass(frozen=True)
class OutputPaths:
    layout: str
    repo_identity: RepoIdentity
    run_root: Path
    sample_root: Path
    reports_dir: Path


def sanitize_folder_name(value: str) -> str:
    cleaned = re.sub(r'[<>:"/\\|?*\x00-\x1f]', "-", value.strip())
    cleaned = re.sub(r"\s+", "-", cleaned)
    cleaned = re.sub(r"-+", "-", cleaned).strip(" .-")
    if not cleaned:
        cleaned = "unknown-repo"
    if cleaned.upper() in WINDOWS_RESERVED_NAMES:
        cleaned = f"{cleaned}-repo"
    return cleaned[:120]


def parse_repo_identity(repository_url: str, project_id: str, folder_mode: str = "repo_name") -> RepoIdentity:
    parsed = urlparse(repository_url)
    path = parsed.path.strip("/")
    if path.endswith(".git"):
        path = path[:-4]
    parts = [part for part in path.split("/") if part]
    repo_name = parts[-1] if parts else project_id
    owner = parts[-2] if len(parts) >= 2 else ""
    if folder_mode == "project_id":
        folder = project_id
    elif folder_mode == "owner_repo":
        folder = f"{owner}-{repo_name}" if owner else repo_name
    else:
        folder = repo_name
    return RepoIdentity(owner=owner, name=repo_name, folder=sanitize_folder_name(folder))


def resolve_output_paths(project_root: Path, sample: SampleInput, config: dict, run_id: str, shard_id: str, *, persist_identity: bool = True) -> OutputPaths:
    output_cfg = config.get("output", {})
    root_dir = Path(output_cfg.get("root_dir", "runs"))
    if not root_dir.is_absolute():
        root_dir = project_root / root_dir
    layout = output_cfg.get("layout", "repo_sample")
    identity = parse_repo_identity(sample.repository_url, sample.project_id, output_cfg.get("repo_folder_name", "repo_name"))
    if layout == "run_shard":
        run_root = root_dir / run_id / shard_id
        sample_root = run_root / sample.input_id
        reports_dir = run_root / "reports"
    elif layout == "repo_sample":
        repo_root = root_dir / identity.folder
        conflict_mode = output_cfg.get("on_repo_name_conflict", "fail")
        if persist_identity and _has_repo_identity_conflict(repo_root, sample):
            if conflict_mode == "append_project_id":
                identity = RepoIdentity(owner=identity.owner, name=identity.name, folder=sanitize_folder_name(f"{identity.folder}_{sample.project_id}"))
                repo_root = root_dir / identity.folder
            else:
                raise ValueError(f"Repo folder conflict for {repo_root}; existing identity differs from {sample.repository_url}")
        if persist_identity:
            _ensure_repo_identity(repo_root, identity, sample)
        run_root = repo_root
        sample_root = repo_root / sample.input_id
        reports_dir = sample_root / "reports"
    else:
        raise ValueError(f"Unsupported output.layout: {layout}")
    return OutputPaths(layout=layout, repo_identity=identity, run_root=run_root, sample_root=sample_root, reports_dir=reports_dir)


def _has_repo_identity_conflict(repo_root: Path, sample: SampleInput) -> bool:
    marker = repo_root / ".repo_identity.json"
    if not marker.is_file():
        return False
    existing = json.loads(marker.read_text(encoding="utf-8"))
    return existing.get("repository_url") != sample.repository_url and existing.get("project_id") != sample.project_id


def _ensure_repo_identity(repo_root: Path, identity: RepoIdentity, sample: SampleInput) -> None:
    marker = repo_root / ".repo_identity.json"
    data = {
        "repo_owner": identity.owner,
        "repo_name": identity.name,
        "repo_folder": identity.folder,
        "repository_url": sample.repository_url,
        "project_id": sample.project_id,
    }
    atomic_write_json(marker, data)
