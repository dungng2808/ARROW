from __future__ import annotations

import json
from pathlib import Path
from typing import Iterable

from .models import SampleInput


def load_sample(path: Path, dataset_dir: Path) -> SampleInput:
    raw = json.loads(path.read_text(encoding="utf-8"))
    project_id = path.parent.name
    repo = raw.get("repository", {})
    focal = raw.get("focal_class", {})
    test = raw.get("test_class", {})
    return SampleInput(
        project_id=project_id,
        sample_file=path,
        repository_url=repo.get("url", ""),
        focal_class_name=focal.get("identifier", ""),
        focal_class_path=focal.get("file", ""),
        test_class_name=test.get("identifier", ""),
        test_class_path=test.get("file", ""),
        raw=raw,
    )


def project_dirs(dataset_dir: Path, project_ids: list[str] | None = None, shard_file: Path | None = None) -> list[Path]:
    selected = set(project_ids or [])
    if shard_file:
        selected.update(line.strip() for line in shard_file.read_text(encoding="utf-8").splitlines() if line.strip())
    dirs = [item for item in dataset_dir.iterdir() if item.is_dir()]
    if selected:
        dirs = [item for item in dirs if item.name in selected]
    return sorted(dirs, key=lambda item: item.name)


def iter_sample_files(
    dataset_dir: Path,
    *,
    mode: str,
    project_ids: list[str] | None = None,
    project_shard_file: Path | None = None,
    samples_per_project: int | str = 1,
) -> Iterable[Path]:
    dirs = project_dirs(dataset_dir, project_ids, project_shard_file)
    if mode == "sample":
        for project_dir in dirs:
            yield from sorted(project_dir.glob("*.json"), key=lambda item: item.name)
        return
    if mode != "project":
        raise ValueError(f"Unsupported input mode: {mode}")
    for project_dir in dirs:
        samples = sorted(project_dir.glob("*.json"), key=lambda item: item.name)
        if samples_per_project == "all":
            yield from samples
        else:
            yield from samples[: int(samples_per_project)]


def select_inputs(
    dataset_dir: Path,
    config: dict,
    *,
    start_index: int,
    limit: int,
    project_id: str | None = None,
    sample_file: str | None = None,
    repo_shard: Path | None = None,
) -> list[SampleInput]:
    if project_id and sample_file:
        path = dataset_dir / project_id / sample_file
        if not path.is_file():
            raise FileNotFoundError(path)
        return [load_sample(path, dataset_dir)]
    input_cfg = config.get("input", {})
    project_ids = list(input_cfg.get("project_ids") or [])
    if project_id:
        project_ids = [project_id]
    shard = repo_shard or (Path(input_cfg["project_shard_file"]) if input_cfg.get("project_shard_file") else None)
    if shard and not shard.is_absolute():
        shard = Path.cwd() / shard
    files = list(
        iter_sample_files(
            dataset_dir,
            mode=input_cfg.get("mode", "sample"),
            project_ids=project_ids,
            project_shard_file=shard,
            samples_per_project=input_cfg.get("samples_per_project", 1),
        )
    )
    end_index = None if limit <= 0 else start_index + limit
    return [load_sample(path, dataset_dir) for path in files[start_index:end_index]]


def count_dataset(dataset_dir: Path) -> tuple[int, int]:
    projects = sum(1 for item in dataset_dir.iterdir() if item.is_dir())
    samples = sum(1 for _ in dataset_dir.rglob("*.json"))
    return projects, samples
