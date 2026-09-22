from __future__ import annotations

import csv
import json
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from .models import ClassCandidate, RevisionStatus
from .util import normalize_java


@dataclass(frozen=True)
class RevisionChoice:
    checkout_sha: str
    provenance: str
    status: str
    content_match: dict[str, Any] | None = None
    evidence_ref: str = ""


def load_revision_map(path: Path | None) -> dict[str, RevisionChoice]:
    if path is None:
        return {}
    entries: list[dict[str, Any]]
    if path.suffix.lower() == ".csv":
        with path.open(encoding="utf-8-sig", newline="") as handle:
            entries = list(csv.DictReader(handle))
    else:
        entries = [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    result: dict[str, RevisionChoice] = {}
    for entry in entries:
        task_id = str(entry.get("task_id") or "")
        sha = str(entry.get("checkout_sha") or "")
        status = str(entry.get("revision_verification_status") or "")
        provenance = str(entry.get("revision_provenance") or "")
        evidence_ref = str(entry.get("evidence_ref") or "").strip()
        if not task_id or len(sha) != 40 or status != RevisionStatus.UPSTREAM_PINNED:
            raise ValueError("revision map requires task_id, full checkout_sha, and UPSTREAM_PINNED status")
        if provenance not in {"upstream_metadata", "dataset_record"}:
            raise ValueError(f"{task_id}: UPSTREAM_PINNED requires upstream_metadata or dataset_record provenance")
        if not evidence_ref:
            raise ValueError(f"{task_id}: UPSTREAM_PINNED requires a non-empty evidence_ref")
        result[task_id] = RevisionChoice(sha, provenance, status, evidence_ref=evidence_ref)
    return result


def _git(repo: Path, args: list[str], *, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(["git", "-C", str(repo), *args], capture_output=True, text=True, encoding="utf-8", errors="replace", check=check)


def ensure_mirror(repo_url: str, mirror: Path) -> Path:
    mirror.parent.mkdir(parents=True, exist_ok=True)
    if not mirror.exists():
        subprocess.run(["git", "clone", "--mirror", repo_url, str(mirror)], check=True, capture_output=True, text=True, encoding="utf-8", errors="replace")
    else:
        _git(mirror, ["remote", "update", "--prune"], check=True)
    return mirror


def _object_exists(mirror: Path, sha: str, path: str) -> bool:
    return _git(mirror, ["cat-file", "-e", f"{sha}:{path}"], check=False).returncode == 0


def _show(mirror: Path, sha: str, path: str) -> str | None:
    result = _git(mirror, ["show", f"{sha}:{path}"], check=False)
    return result.stdout if result.returncode == 0 else None


def _matching_evidence(dataset_dir: Path, evidence_paths: list[str], source: str, test_loader, max_checks: int = 50) -> list[dict[str, str]]:
    source_norm = normalize_java(source)
    matches: list[dict[str, str]] = []
    for relative in evidence_paths[:max_checks]:
        try:
            raw = json.loads((dataset_dir / relative).read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            continue
        test_path = str((raw.get("test_class") or {}).get("file") or "").replace("\\", "/").strip("/")
        test_source = test_loader(test_path)
        focal_body = str((raw.get("focal_method") or {}).get("body") or "")
        test_body = str((raw.get("test_case") or {}).get("body") or "")
        if focal_body and test_body and normalize_java(focal_body) in source_norm and test_source and normalize_java(test_body) in normalize_java(test_source):
            matches.append({"source_json_path": relative, "focal_method": str((raw.get("focal_method") or {}).get("signature") or ""), "test_case": str((raw.get("test_case") or {}).get("signature") or "")})
    return matches


def choose_revision(
    mirror: Path, candidate: ClassCandidate, dataset_dir: Path, evidence_paths: list[str], pinned: RevisionChoice | None, max_candidates: int,
) -> RevisionChoice:
    if pinned:
        if _git(mirror, ["rev-parse", "--verify", f"{pinned.checkout_sha}^{{commit}}"], check=False).returncode != 0:
            raise LookupError("COMMIT_MISSING")
        return pinned
    paths = [candidate.class_path, *candidate.test_class_paths]
    result = _git(mirror, ["log", "--all", "--format=%H", "--", *paths], check=False)
    commits = list(dict.fromkeys(line.strip() for line in result.stdout.splitlines() if line.strip()))[:max_candidates]
    first_pair = ""
    best: tuple[int, str, list[dict[str, str]]] | None = None
    for sha in commits:
        if not _object_exists(mirror, sha, candidate.class_path):
            continue
        existing_tests = [path for path in candidate.test_class_paths if _object_exists(mirror, sha, path)]
        if not existing_tests:
            continue
        if not first_pair:
            first_pair = sha
        source = _show(mirror, sha, candidate.class_path) or ""
        matches = _matching_evidence(dataset_dir, evidence_paths, source, lambda path: _show(mirror, sha, path))
        score = len(matches)
        if score and (best is None or score > best[0] or (score == best[0] and sha < best[1])):
            best = (score, sha, matches)
    if best:
        score, sha, matches = best
        return RevisionChoice(sha, "git_history", RevisionStatus.CONTENT_MATCHED, {"normalization": "strip Java comments then collapse whitespace", "matched_evidence": matches, "matched_evidence_count": score})
    if first_pair:
        return RevisionChoice(first_pair, "git_history", RevisionStatus.UNVERIFIED, {"selection_method": "latest_commit_with_focal_and_test_paths"})
    head = _git(mirror, ["rev-parse", "HEAD"], check=False).stdout.strip()
    if head:
        return RevisionChoice(head, "git_history", RevisionStatus.UNVERIFIED, {"selection_method": "mirror_HEAD_no_path_pair"})
    raise LookupError("COMMIT_MISSING")
