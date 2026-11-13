"""Remove credential-like strings from exported test payloads before computing the Python oracle.

Only generated golden files are rewritten; original datasets and Python source stay untouched.
"""
from __future__ import annotations

import base64
import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TOKEN_PATTERN = re.compile(rb"\bAPI-[A-Za-z0-9]{20,}\b")
REPLACEMENT = b"REDACTED_TEST_CREDENTIAL"


def sanitize_payload(payload: bytes) -> bytes:
    return TOKEN_PATTERN.sub(REPLACEMENT, payload)


def sha256(path: Path) -> str:
    with path.open("rb") as source:
        return hashlib.file_digest(source, "sha256").hexdigest()


def rewrite_golden(folder: Path, record_identity) -> dict:
    path = folder / "record_identity.jsonl"
    temporary = path.with_suffix(".jsonl.sanitized.tmp")
    work = ROOT / ".work/golden-redaction/records"
    changed = 0
    try:
        with path.open("rb") as source, temporary.open("wb") as target:
            for line in source:
                row = json.loads(line)
                payload = base64.b64decode(row["input"]["bytes_base64"])
                sanitized = sanitize_payload(payload)
                if sanitized == payload:
                    target.write(line)
                    continue
                changed += 1
                fixture = (work / row["input"]["project_id"] / row["input"]["name"]).resolve()
                if not fixture.is_relative_to(work.resolve()):
                    raise ValueError("Unsafe golden fixture path")
                fixture.parent.mkdir(parents=True, exist_ok=True)
                fixture.write_bytes(sanitized)
                row["input"]["bytes_base64"] = base64.b64encode(sanitized).decode("ascii")
                row.pop("output", None)
                row.pop("error", None)
                try:
                    row["output"] = record_identity(fixture)
                except Exception as error:
                    row["error"] = type(error).__name__
                encoded = (json.dumps(row, ensure_ascii=False, sort_keys=True) + "\n").encode("utf-8")
                if TOKEN_PATTERN.search(encoded) or TOKEN_PATTERN.search(sanitized):
                    raise ValueError("Credential-like token remains in golden row")
                target.write(encoded)
        temporary.replace(path)
    finally:
        if temporary.exists():
            temporary.unlink()
    metadata_path = folder / "manifest.json"
    manifest = json.loads(metadata_path.read_text(encoding="utf-8"))
    stats = manifest["files"][path.name]
    stats["sha256"] = sha256(path)
    stats["bytes"] = path.stat().st_size
    manifest["credential_redaction"] = "Octopus API-key-like strings replaced in generated payloads before Python oracle evaluation"
    metadata_path.write_text(json.dumps(manifest, ensure_ascii=False, sort_keys=True, indent=2) + "\n", encoding="utf-8", newline="\n")
    return {"file": str(path.relative_to(ROOT)), "redacted_records": changed, "count": stats["count"], "sha256": stats["sha256"]}


def main() -> None:
    sample = b'{"value":"' + b"API-" + b"A" * 26 + b'"}'
    assert sanitize_payload(sample) == b'{"value":"REDACTED_TEST_CREDENTIAL"}'
    assert sanitize_payload(b"\xff ordinary input\x00") == b"\xff ordinary input\x00"
    sys.path.insert(0, str(ROOT.parent / "preflight_tool"))
    from preflight.ingest import _record_identity
    full = rewrite_golden(ROOT / ".work/golden-full", _record_identity)
    subset = rewrite_golden(ROOT / "tests/golden", _record_identity)
    subset_path = ROOT / "tests/golden/manifest.json"
    metadata = json.loads(subset_path.read_text(encoding="utf-8"))
    metadata["files"]["record_identity.jsonl"]["full_sha256"] = full["sha256"]
    subset_path.write_text(json.dumps(metadata, ensure_ascii=False, sort_keys=True, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(json.dumps({"results": [full, subset]}, sort_keys=True))


if __name__ == "__main__":
    main()
