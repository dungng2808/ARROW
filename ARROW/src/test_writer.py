from __future__ import annotations

import hashlib
import json
import re
from dataclasses import dataclass
from pathlib import Path

from .fs_utils import atomic_write_json, atomic_write_text


class JavaValidationError(ValueError):
    pass


@dataclass
class Ownership:
    experiment_id: str
    generated_test_path: Path
    generated_test_class_name: str
    initial_generated_hash: str
    current_generated_hash: str

    def to_dict(self) -> dict[str, str]:
        return {
            "experiment_id": self.experiment_id,
            "generated_test_path": str(self.generated_test_path),
            "generated_test_class_name": self.generated_test_class_name,
            "initial_generated_hash": self.initial_generated_hash,
            "current_generated_hash": self.current_generated_hash,
        }


def code_hash(code: str) -> str:
    return hashlib.sha256(code.encode("utf-8")).hexdigest()


def strip_markdown_fences(content: str) -> str:
    stripped = content.strip()
    match = re.fullmatch(r"```(?:java)?\s*(.*?)\s*```", stripped, re.DOTALL | re.IGNORECASE)
    if match:
        return match.group(1).strip() + "\n"
    match = re.search(r"```(?:java)?\s*(.*?)\s*```", stripped, re.DOTALL | re.IGNORECASE)
    if match:
        return match.group(1).strip() + "\n"
    start = re.search(r"(?m)^(package|import|public\s+class)\s+", stripped)
    if start:
        return stripped[start.start():].strip() + "\n"
    return stripped + "\n"


def validate_java_candidate(
    raw_content: str,
    *,
    expected_package: str,
    expected_class_name: str,
    testing_framework: str = "",
    attempted_hashes: set[str] | None = None,
) -> tuple[str, str]:
    code = strip_markdown_fences(raw_content)
    if not code.strip():
        raise JavaValidationError("candidate is empty")
    if any(marker in code for marker in ("--- ", "+++ ", "@@")):
        raise JavaValidationError("candidate looks like a diff")
    if any(marker in code.lower() for marker in ("pom.xml", "build.gradle", "settings.gradle", "src/main/java")):
        raise JavaValidationError("candidate includes non-test file content")
    class_matches = re.findall(r"(?m)public\s+class\s+([A-Za-z_][A-Za-z0-9_]*)", code)
    code, class_matches = normalize_generated_class_name(code, class_matches, expected_class_name)
    if class_matches != [expected_class_name]:
        raise JavaValidationError(f"expected exactly one public class {expected_class_name}, found {class_matches}")
    code = normalize_junit_imports(code, testing_framework)
    package_match = re.search(r"(?m)^package\s+([A-Za-z_][A-Za-z0-9_.]*)\s*;", code)
    actual_package = package_match.group(1) if package_match else ""
    if actual_package != expected_package:
        raise JavaValidationError(f"expected package {expected_package!r}, found {actual_package!r}")
    digest = code_hash(code)
    if attempted_hashes and digest in attempted_hashes:
        raise JavaValidationError("candidate hash has already been attempted")
    return code, digest


def normalize_generated_class_name(code: str, class_matches: list[str], expected_class_name: str) -> tuple[str, list[str]]:
    if class_matches == [expected_class_name]:
        return code, class_matches
    if len(class_matches) != 1:
        return code, class_matches
    actual_class_name = class_matches[0]
    if not _is_normalizable_generated_name(actual_class_name, expected_class_name):
        return code, class_matches
    normalized = re.sub(
        rf"(?m)(public\s+class\s+){re.escape(actual_class_name)}\b",
        rf"\1{expected_class_name}",
        code,
        count=1,
    )
    return normalized, [expected_class_name]


def _is_normalizable_generated_name(actual_class_name: str, expected_class_name: str) -> bool:
    expected_match = re.fullmatch(r"(.+)Test_([0-9a-f]{8})", expected_class_name)
    if not expected_match:
        return False
    focal_name, expected_hash = expected_match.groups()
    allowed = {
        f"{focal_name}Test",
        f"{focal_name}GeneratedTest",
        f"{focal_name}AgoneGeneratedTest",
        f"{focal_name}LLMGeneratedTest",
        f"{focal_name}Test_{expected_hash}",
        f"{focal_name}GeneratedTest_{expected_hash}",
        f"{focal_name}AgoneGeneratedTest_{expected_hash}",
        f"{focal_name}LLMGeneratedTest_{expected_hash}",
    }
    return actual_class_name in allowed


def normalize_junit_imports(code: str, testing_framework: str) -> str:
    framework = testing_framework.lower().strip()
    if framework == "junit5":
        return _normalize_junit5_imports(code)
    if framework == "junit4":
        return _normalize_junit4_imports(code)
    return code


def _normalize_junit5_imports(code: str) -> str:
    required: list[str] = []
    for annotation, import_line in {
        "Test": "import org.junit.jupiter.api.Test;",
        "BeforeEach": "import org.junit.jupiter.api.BeforeEach;",
        "AfterEach": "import org.junit.jupiter.api.AfterEach;",
        "BeforeAll": "import org.junit.jupiter.api.BeforeAll;",
        "AfterAll": "import org.junit.jupiter.api.AfterAll;",
        "Disabled": "import org.junit.jupiter.api.Disabled;",
    }.items():
        if _uses_simple_annotation(code, annotation) and import_line not in code:
            required.append(import_line)
    if _uses_simple_assertions(code) and "import static org.junit.jupiter.api.Assertions.*;" not in code:
        required.append("import static org.junit.jupiter.api.Assertions.*;")
    return _insert_missing_imports(code, required)


def _normalize_junit4_imports(code: str) -> str:
    code = re.sub(r"@\s*BeforeEach\b", "@Before", code)
    code = re.sub(r"@\s*AfterEach\b", "@After", code)
    code = re.sub(r"@\s*BeforeAll\b", "@BeforeClass", code)
    code = re.sub(r"@\s*AfterAll\b", "@AfterClass", code)
    required: list[str] = []
    for annotation, import_line in {
        "Test": "import org.junit.Test;",
        "Before": "import org.junit.Before;",
        "After": "import org.junit.After;",
        "BeforeClass": "import org.junit.BeforeClass;",
        "AfterClass": "import org.junit.AfterClass;",
        "Ignore": "import org.junit.Ignore;",
    }.items():
        if _uses_simple_annotation(code, annotation) and import_line not in code:
            required.append(import_line)
    if _uses_simple_assertions(code) and "import static org.junit.Assert.*;" not in code:
        required.append("import static org.junit.Assert.*;")
    return _insert_missing_imports(code, required)


def _uses_simple_annotation(code: str, annotation: str) -> bool:
    return re.search(rf"(?m)^\s*@\s*{re.escape(annotation)}\b", code) is not None


def _uses_simple_assertions(code: str) -> bool:
    return re.search(r"\bassert(?:Equals|True|False|Null|NotNull|Same|NotSame|ArrayEquals|Throws|DoesNotThrow|IterableEquals|LinesMatch|All|Timeout|TimeoutPreemptively)\s*\(", code) is not None


def _insert_missing_imports(code: str, imports: list[str]) -> str:
    missing = [line for line in dict.fromkeys(imports) if line not in code]
    if not missing:
        return code
    insert_text = "\n".join(missing) + "\n"
    import_matches = list(re.finditer(r"(?m)^import\s+(?:static\s+)?[A-Za-z_][A-Za-z0-9_.*]*\s*;", code))
    if import_matches:
        last = import_matches[-1]
        return code[: last.end()] + "\n" + insert_text + code[last.end():]
    package_match = re.search(r"(?m)^package\s+[A-Za-z_][A-Za-z0-9_.]*\s*;", code)
    if package_match:
        return code[: package_match.end()] + "\n\n" + insert_text + code[package_match.end():].lstrip("\n")
    return insert_text + "\n" + code


def _ownership_path(test_path: Path) -> Path:
    return test_path.with_suffix(test_path.suffix + ".agone-ownership.json")


def _is_within(path: Path, root: Path) -> bool:
    try:
        path.resolve().relative_to(root.resolve())
        return True
    except ValueError:
        return False


def write_owned_generated_test(
    *,
    experiment_id: str,
    workspace: Path,
    generated_test_path: Path,
    generated_test_class_name: str,
    code: str,
) -> Ownership:
    if not _is_within(generated_test_path, workspace):
        raise JavaValidationError("generated test path escapes experiment workspace")
    ownership_path = _ownership_path(generated_test_path)
    digest = code_hash(code)
    if generated_test_path.exists() and ownership_path.exists():
        existing = json.loads(ownership_path.read_text(encoding="utf-8"))
        if existing.get("experiment_id") != experiment_id:
            raise JavaValidationError("generated test path belongs to another experiment")
        initial_hash = existing.get("initial_generated_hash") or digest
    elif generated_test_path.exists() and not ownership_path.exists():
        raise JavaValidationError("refusing to overwrite existing human-written test")
    else:
        initial_hash = digest
    ownership = Ownership(
        experiment_id=experiment_id,
        generated_test_path=generated_test_path,
        generated_test_class_name=generated_test_class_name,
        initial_generated_hash=initial_hash,
        current_generated_hash=digest,
    )
    atomic_write_text(generated_test_path, code)
    atomic_write_json(ownership_path, ownership.to_dict())
    return ownership
