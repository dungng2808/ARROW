"""Normalize GitBug-Java bugs into Classes2Test-style JSON plus an UPSTREAM_PINNED revision map.

Usage (from ARROW/preflight_tool):
    python scripts/normalize_gitbug.py --gitbug-root ../gitbug-java \
        --mirror-cache runs/gitbug_mirrors --output-dir ../gitbug_input

Rules:
- Revision: the fixed commit (`commit_hash`) of each bug, provenance `dataset_record`.
- CUT candidates: `.java` files under a `src/main/java/` directory changed by `bug_patch`
  and still present at the fixed commit (deleted files, package-info.java and
  module-info.java are skipped). Other source layouts are recorded as excluded.
- Reference test: a file named <Name>Test, Test<Name> or <Name>Tests under `src/test/java/`
  of the same module at the fixed commit; the same package is preferred, otherwise a
  unique name match in the module. Failing tests of the bug are never used, because
  they often belong to a different class. A paired test is required (decided
  11/10/2026): candidates without one are excluded as NO_PAIRED_TEST, so every GitBug-Java
  CUT has a human-written reference test.
- Manual labels (optional, --manual-labels CSV): when no name match exists, a test chosen by
  a human annotator under the written labeling criteria is used (test_pairing = manual).
  Columns: project_id, class_path, decision (PAIRED | NONE), test_class_path, labeler,
  reviewer, rationale. The labeled test must exist at the fixed commit; a NONE decision or
  a missing label keeps the candidate excluded as NO_PAIRED_TEST.
- project_id is the GitBug bug id (<owner>-<repo>-<sha12>), so the same file changed by
  two bugs yields two tasks pinned to different commits.

Writes:
  dataset/<project_id>/<n>.json   records readable by `preflight --input-root`
  revision_map.csv                task_id -> checkout_sha (UPSTREAM_PINNED, dataset_record)
  candidates.csv                  every candidate file, including excluded ones and reasons
  normalize.meta.json             input checksums, gitbug-java commit, counts
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from preflight.util import stable_task_id  # noqa: E402

DIFF_FILE = re.compile(r"^diff --git a/(\S+) b/(\S+)\n((?:(?!diff --git ).*\n?)*)", re.M)
MAIN_MARKER = "src/main/java/"
SKIPPED_NAMES = {"package-info.java", "module-info.java"}


def sha256_file(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def git(args: list[str], cwd: Path | None = None) -> subprocess.CompletedProcess[str]:
    return subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True, encoding="utf-8", errors="replace")


def changed_java_files(patch: str) -> list[tuple[str, bool]]:
    """Return (path, deleted) for every .java file touched by a unified diff."""
    files = []
    for match in DIFF_FILE.finditer(patch):
        path, body = match.group(2), match.group(3)
        if path.endswith(".java"):
            files.append((path, "+++ /dev/null" in body))
    return files


def ensure_mirror(repository: str, cache: Path) -> tuple[Path | None, str]:
    mirror = cache / (repository.replace("/", "-") + ".git")
    url = f"https://github.com/{repository}.git"
    if mirror.is_dir():
        fetched = git(["fetch", "--quiet", "origin", "+refs/heads/*:refs/heads/*", "+refs/tags/*:refs/tags/*"], cwd=mirror)
        return mirror, "" if fetched.returncode == 0 else "FETCH_FAILED_USING_CACHE"
    cache.mkdir(parents=True, exist_ok=True)
    cloned = git(["clone", "--quiet", "--bare", "--filter=blob:none", url, str(mirror)])
    if cloned.returncode != 0:
        return None, "CLONE_FAILED"
    return mirror, ""


def tree_paths(mirror: Path, sha: str) -> set[str] | None:
    if git(["cat-file", "-e", f"{sha}^{{commit}}"], cwd=mirror).returncode != 0:
        git(["fetch", "--quiet", "--filter=blob:none", "origin", sha], cwd=mirror)
    listed = git(["ls-tree", "-r", "--name-only", sha], cwd=mirror)
    return set(listed.stdout.splitlines()) if listed.returncode == 0 else None


def pair_test(class_path: str, paths: set[str]) -> tuple[str, str]:
    module_prefix, relative = class_path.split(MAIN_MARKER, 1)
    package_dir, file_name = relative.rsplit("/", 1) if "/" in relative else ("", relative)
    name = file_name[:-5]
    test_root = f"{module_prefix}src/test/java/"
    names = [f"{name}Test.java", f"Test{name}.java", f"{name}Tests.java"]
    for candidate in names:
        path = f"{test_root}{package_dir}/{candidate}" if package_dir else f"{test_root}{candidate}"
        if path in paths:
            return path, "same_package"
    matches = sorted(p for p in paths if p.startswith(test_root) and p.rsplit("/", 1)[-1] in names)
    if len(matches) == 1:
        return matches[0], "unique_name_in_module"
    return "", "ambiguous_name" if matches else "none"


def load_manual_labels(path: Path | None) -> dict[tuple[str, str], dict[str, str]]:
    if path is None:
        return {}
    labels: dict[tuple[str, str], dict[str, str]] = {}
    with path.open(encoding="utf-8-sig", newline="") as handle:
        for row in csv.DictReader(handle):
            key = (row["project_id"].strip(), row["class_path"].strip())
            decision = row.get("decision", "").strip().upper()
            if decision not in {"PAIRED", "NONE"}:
                raise SystemExit(f"{key}: decision must be PAIRED or NONE, got {decision!r}")
            if decision == "PAIRED" and not row.get("test_class_path", "").strip():
                raise SystemExit(f"{key}: PAIRED label needs test_class_path")
            if key in labels:
                raise SystemExit(f"{key}: duplicate manual label")
            labels[key] = {k: (v or "").strip() for k, v in row.items()}
    return labels


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--gitbug-root", required=True, type=Path)
    parser.add_argument("--mirror-cache", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--manual-labels", type=Path, help="CSV of human-chosen reference tests for CUTs without a name match")
    args = parser.parse_args()
    manual_labels = load_manual_labels(args.manual_labels)
    used_labels: set[tuple[str, str]] = set()

    bug_files = sorted((args.gitbug_root / "data" / "bugs").glob("*.json"))
    if not bug_files:
        print(f"no bug files under {args.gitbug_root / 'data' / 'bugs'}", file=sys.stderr)
        return 2
    dataset_dir = args.output_dir / "dataset"
    if dataset_dir.exists() and any(dataset_dir.iterdir()):
        print(f"{dataset_dir} is not empty; use a new --output-dir", file=sys.stderr)
        return 2
    gitbug_commit = git(["rev-parse", "HEAD"], cwd=args.gitbug_root).stdout.strip()

    candidate_rows: list[dict[str, str]] = []
    revision_rows: list[dict[str, str]] = []
    bug_count = 0
    for bug_file in bug_files:
        bugs = [json.loads(line) for line in bug_file.read_text(encoding="utf-8").splitlines() if line.strip()]
        bug_count += len(bugs)
        repository = bugs[0]["repository"]
        mirror, mirror_note = ensure_mirror(repository, args.mirror_cache)
        print(f"{repository}: {len(bugs)} bugs {mirror_note}", file=sys.stderr)
        for bug in bugs:
            sha = bug["commit_hash"]
            project_id = f"{repository.replace('/', '-')}-{sha[:12]}"
            repo_url = f"https://github.com/{repository}"
            paths = tree_paths(mirror, sha) if mirror else None
            base = {"project_id": project_id, "repository": repository, "checkout_sha": sha,
                    "bug_file": bug_file.name, "strategy": bug["strategy"], "change_type": bug["change_type"]}
            files = changed_java_files(bug["bug_patch"])
            if not files:
                candidate_rows.append({**base, "class_path": "", "status": "EXCLUDED", "reason": "NO_JAVA_IN_BUG_PATCH"})
            written = 0
            for class_path, deleted in files:
                row = {**base, "class_path": class_path}
                file_name = class_path.rsplit("/", 1)[-1]
                reason = ("DELETED_IN_FIX" if deleted else
                          "NOT_UNDER_SRC_MAIN_JAVA" if f"/{MAIN_MARKER}" not in f"/{class_path}" else
                          "NON_CLASS_FILE" if file_name in SKIPPED_NAMES else
                          "CLONE_FAILED" if mirror is None else
                          "COMMIT_MISSING" if paths is None else
                          "FILE_MISSING_AT_COMMIT" if class_path not in paths else "")
                if reason:
                    candidate_rows.append({**row, "status": "EXCLUDED", "reason": reason})
                    continue
                test_path, pairing = pair_test(class_path, paths)
                label = manual_labels.get((project_id, class_path)) if not test_path else None
                if label:
                    used_labels.add((project_id, class_path))
                    if label["decision"] == "PAIRED":
                        if label["test_class_path"] not in paths:
                            raise SystemExit(f"{project_id} {class_path}: labeled test {label['test_class_path']} not found at {sha}")
                        test_path, pairing = label["test_class_path"], "manual"
                    else:
                        pairing = "manual_none"
                if not test_path:
                    candidate_rows.append({**row, "status": "EXCLUDED", "reason": "NO_PAIRED_TEST", "test_pairing": pairing})
                    continue
                class_name = file_name[:-5]
                written += 1
                record = {
                    "repository": {"url": repo_url, "language": "Java"},
                    "focal_class": {"identifier": class_name, "file": class_path},
                    "test_class": {"identifier": test_path.rsplit("/", 1)[-1][:-5] if test_path else "", "file": test_path},
                    "gitbug": {"bug_id": project_id, "bug_file": bug_file.name, "commit_hash": sha,
                               "previous_commit_hash": bug["previous_commit_hash"], "strategy": bug["strategy"],
                               "change_type": bug["change_type"], "test_pairing": pairing,
                               **({"manual_label": {k: label[k] for k in ("labeler", "reviewer", "rationale") if k in label}}
                                  if pairing == "manual" else {}),
                               "benchmark_commit": gitbug_commit},
                }
                out = dataset_dir / project_id / f"{written}.json"
                out.parent.mkdir(parents=True, exist_ok=True)
                out.write_text(json.dumps(record, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
                task_id = stable_task_id(project_id, repo_url, class_path, class_name)
                candidate_rows.append({**row, "status": "CANDIDATE", "reason": "", "task_id": task_id,
                                       "test_class_path": test_path, "test_pairing": pairing})
                revision_rows.append({"task_id": task_id, "checkout_sha": sha,
                                      "revision_verification_status": "UPSTREAM_PINNED",
                                      "revision_provenance": "dataset_record",
                                      "evidence_ref": f"gitbug-java@{gitbug_commit[:12]}:data/bugs/{bug_file.name}#commit_hash={sha}"})

    args.output_dir.mkdir(parents=True, exist_ok=True)
    columns = ["task_id", "project_id", "repository", "checkout_sha", "bug_file", "strategy", "change_type",
               "class_path", "status", "reason", "test_class_path", "test_pairing"]
    with (args.output_dir / "candidates.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns)
        writer.writeheader()
        writer.writerows(candidate_rows)
    with (args.output_dir / "revision_map.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(revision_rows[0]))
        writer.writeheader()
        writer.writerows(revision_rows)

    unused = sorted(set(manual_labels) - used_labels)
    if unused:
        print(f"warning: {len(unused)} manual label(s) not used (name match exists or file not a candidate), e.g. {unused[0]}", file=sys.stderr)
    candidates = [row for row in candidate_rows if row["status"] == "CANDIDATE"]
    excluded: dict[str, int] = {}
    for row in candidate_rows:
        if row["status"] == "EXCLUDED":
            excluded[row["reason"]] = excluded.get(row["reason"], 0) + 1
    pairing: dict[str, int] = {}
    for row in candidates:
        pairing[row["test_pairing"]] = pairing.get(row["test_pairing"], 0) + 1
    meta = {
        "schema_version": 1,
        "gitbug_java_commit": gitbug_commit,
        "bug_files_sha256": {path.name: sha256_file(path) for path in bug_files},
        "repositories": len(bug_files),
        "bugs": bug_count,
        "candidate_classes": len(candidates),
        "candidate_repositories": len({row["repository"] for row in candidates}),
        "candidate_bugs": len({row["project_id"] for row in candidates}),
        "excluded_by_reason": dict(sorted(excluded.items())),
        "test_pairing": dict(sorted(pairing.items())),
        "revision_map_sha256": sha256_file(args.output_dir / "revision_map.csv"),
        "manual_labels": ({"path": str(args.manual_labels.resolve()), "sha256": sha256_file(args.manual_labels),
                           "rows": len(manual_labels), "used": len(used_labels)} if args.manual_labels else None),
    }
    (args.output_dir / "normalize.meta.json").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: v for k, v in meta.items() if k != "bug_files_sha256"}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
