from __future__ import annotations

import json
import subprocess

import pytest

from preflight.models import ClassCandidate, RevisionStatus
from preflight.revision import RevisionChoice, choose_revision, ensure_mirror, load_revision_map
from preflight.util import normalize_java, safe_relative


@pytest.mark.unit
def test_normalization_preserves_comment_markers_inside_java_strings():
    source = 'String url = "https://example.test/a/*x*/"; // remove\nint x = 1; /* remove */'
    assert normalize_java(source) == 'Stringurl="https://example.test/a/*x*/";intx=1;'


@pytest.mark.unit
def test_revision_map_requires_pinned_sha_provenance_and_evidence(tmp_path):
    path = tmp_path / "revisions.jsonl"
    good = {"task_id": "id", "checkout_sha": "a" * 40, "revision_provenance": "upstream_metadata", "revision_verification_status": "UPSTREAM_PINNED", "evidence_ref": "https://example.test/source"}
    path.write_text(json.dumps(good), encoding="utf-8")
    result = load_revision_map(path)
    assert result["id"].status == RevisionStatus.UPSTREAM_PINNED
    good.pop("evidence_ref"); path.write_text(json.dumps(good), encoding="utf-8")
    with pytest.raises(ValueError, match="evidence_ref"):
        load_revision_map(path)


@pytest.mark.unit
@pytest.mark.parametrize("mutate", [
    {"checkout_sha": "short"},
    {"revision_provenance": "manual"},
    {"revision_verification_status": "CONTENT_MATCHED"},
])
def test_invalid_revision_map_is_rejected(tmp_path, mutate):
    entry = {"task_id": "id", "checkout_sha": "b" * 40, "revision_provenance": "dataset_record", "revision_verification_status": "UPSTREAM_PINNED", "evidence_ref": "dataset:1"}
    entry.update(mutate); path = tmp_path / "bad.jsonl"; path.write_text(json.dumps(entry), encoding="utf-8")
    with pytest.raises(ValueError):
        load_revision_map(path)


@pytest.mark.unit
def test_revision_map_csv_and_pinned_missing_commit(tmp_path, monkeypatch):
    path = tmp_path / "revisions.csv"
    path.write_text("task_id,checkout_sha,revision_provenance,revision_verification_status,evidence_ref\nid," + "a" * 40 + ",dataset_record,UPSTREAM_PINNED,dataset:1\n", encoding="utf-8")
    assert load_revision_map(path)["id"].evidence_ref == "dataset:1"
    candidate = ClassCandidate("id", "1", "url", "X.java", "X", "X", (), (), ())
    monkeypatch.setattr("preflight.revision._git", lambda *args, **kwargs: subprocess.CompletedProcess([], 1, "", ""))
    with pytest.raises(LookupError):
        choose_revision(tmp_path, candidate, tmp_path, [], RevisionChoice("a" * 40, "dataset_record", RevisionStatus.UPSTREAM_PINNED), 2)


@pytest.mark.unit
def test_revision_head_fallback_and_safe_relative(tmp_path, monkeypatch):
    candidate = ClassCandidate("id", "1", "url", "X.java", "X", "X", (), (), ())
    calls = []
    def git(repo, args, check=True):
        calls.append(args)
        if args[:2] == ["log", "--all"]:
            return subprocess.CompletedProcess(args, 0, "", "")
        return subprocess.CompletedProcess(args, 0, "c" * 40 + "\n", "")
    monkeypatch.setattr("preflight.revision._git", git)
    result = choose_revision(tmp_path, candidate, tmp_path, [], None, 2)
    assert result.status == RevisionStatus.UNVERIFIED and result.checkout_sha == "c" * 40
    assert safe_relative(tmp_path / "a", tmp_path) == "a"
    assert safe_relative(tmp_path.parent / "outside", tmp_path).endswith("outside")


@pytest.mark.unit
def test_normalization_handles_escaped_quotes_and_unterminated_comments():
    assert normalize_java('String s = "\\\"//"; /* unfinished') == 'Strings="\\\"//";'


@pytest.mark.unit
def test_load_revision_map_none_returns_empty_dict():
    assert load_revision_map(None) == {}


@pytest.mark.unit
def test_ensure_mirror_replaces_interrupted_clone(tmp_path):
    source = tmp_path / "source"
    source.mkdir()
    subprocess.run(["git", "init"], cwd=source, check=True, capture_output=True)
    subprocess.run(["git", "config", "user.email", "test@example.test"], cwd=source, check=True)
    subprocess.run(["git", "config", "user.name", "Test"], cwd=source, check=True)
    (source / "README.md").write_text("fixture\n", encoding="utf-8")
    subprocess.run(["git", "add", "README.md"], cwd=source, check=True)
    subprocess.run(["git", "commit", "-m", "fixture"], cwd=source, check=True, capture_output=True)

    mirror = tmp_path / "cache" / "broken.git"
    mirror.mkdir(parents=True)
    (mirror / "partial-clone-marker").write_text("incomplete", encoding="utf-8")

    assert ensure_mirror(str(source), mirror) == mirror
    bare = subprocess.run(
        ["git", "-C", str(mirror), "rev-parse", "--is-bare-repository"],
        check=True, capture_output=True, text=True, encoding="utf-8",
    )
    assert bare.stdout.strip() == "true"
    assert not (mirror / "partial-clone-marker").exists()


@pytest.mark.unit
def test_ensure_mirror_preserves_healthy_cache_on_remote_failure(tmp_path):
    source = tmp_path / "source"
    source.mkdir()
    subprocess.run(["git", "init"], cwd=source, check=True, capture_output=True)
    subprocess.run(["git", "config", "user.email", "test@example.test"], cwd=source, check=True)
    subprocess.run(["git", "config", "user.name", "Test"], cwd=source, check=True)
    (source / "README.md").write_text("fixture\n", encoding="utf-8")
    subprocess.run(["git", "add", "README.md"], cwd=source, check=True)
    subprocess.run(["git", "commit", "-m", "fixture"], cwd=source, check=True, capture_output=True)
    mirror = ensure_mirror(str(source), tmp_path / "cache" / "mirror.git")
    head_before = subprocess.run(
        ["git", "-C", str(mirror), "rev-parse", "HEAD"],
        check=True, capture_output=True, text=True, encoding="utf-8",
    ).stdout.strip()
    source.rename(tmp_path / "source-gone")

    with pytest.raises(subprocess.CalledProcessError):
        ensure_mirror(str(source), mirror)

    head_after = subprocess.run(
        ["git", "-C", str(mirror), "rev-parse", "HEAD"],
        check=True, capture_output=True, text=True, encoding="utf-8",
    ).stdout.strip()
    assert head_after == head_before


@pytest.mark.unit
def test_ensure_mirror_reclones_corrupt_object_database(tmp_path):
    source = tmp_path / "source"
    source.mkdir()
    subprocess.run(["git", "init"], cwd=source, check=True, capture_output=True)
    subprocess.run(["git", "config", "user.email", "test@example.test"], cwd=source, check=True)
    subprocess.run(["git", "config", "user.name", "Test"], cwd=source, check=True)
    (source / "README.md").write_text("fixture\n", encoding="utf-8")
    subprocess.run(["git", "add", "README.md"], cwd=source, check=True)
    subprocess.run(["git", "commit", "-m", "fixture"], cwd=source, check=True, capture_output=True)
    mirror = ensure_mirror(str(source), tmp_path / "cache" / "mirror.git")
    blob = subprocess.run(
        ["git", "-C", str(source), "rev-parse", "HEAD:README.md"],
        check=True, capture_output=True, text=True, encoding="utf-8",
    ).stdout.strip()
    corrupt_object = mirror / "objects" / blob[:2] / blob[2:]
    assert corrupt_object.is_file()
    corrupt_object.chmod(0o600)
    corrupt_object.unlink()

    ensure_mirror(str(source), mirror)

    healthy = subprocess.run(
        ["git", "-C", str(mirror), "fsck", "--connectivity-only"],
        check=False, capture_output=True, text=True, encoding="utf-8",
    )
    assert healthy.returncode == 0


@pytest.mark.unit
def test_matching_evidence_scores_correctly(tmp_path):
    from preflight.revision import _matching_evidence
    dataset_dir = tmp_path / "dataset"
    (dataset_dir / "42").mkdir(parents=True)
    json_path = dataset_dir / "42/sample.json"
    json_path.write_text(json.dumps({
        "focal_method": {"signature": "int f()", "body": "public int f() { return 42; }"},
        "test_case": {"signature": "void testF()", "body": "void testF() { assertEquals(42, f()); }"},
        "test_class": {"file": "src/test/java/Test.java"}
    }), encoding="utf-8")

    source_code = "package acme; public class X { public int f() { return 42; } }"
    test_code = "package acme; class Test { void testF() { assertEquals(42, f()); } }"

    matches = _matching_evidence(dataset_dir, ["42/sample.json"], source_code, lambda path: test_code)
    assert len(matches) == 1
    assert matches[0]["source_json_path"] == "42/sample.json"
    assert matches[0]["focal_method"] == "int f()"
