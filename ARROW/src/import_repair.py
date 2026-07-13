from __future__ import annotations

import re
from dataclasses import dataclass, field
from typing import Any

from .models import ExperimentContext


@dataclass(frozen=True)
class ImportRepairResult:
    code: str
    added_imports: list[str] = field(default_factory=list)
    unavailable_symbols: list[str] = field(default_factory=list)

    @property
    def changed(self) -> bool:
        return bool(self.added_imports)

    def to_dict(self) -> dict[str, Any]:
        return {
            "changed": self.changed,
            "added_imports": self.added_imports,
            "unavailable_symbols": self.unavailable_symbols,
        }


MOCKITO_MATCHER_SYMBOLS = {
    "any",
    "anyBoolean",
    "anyByte",
    "anyChar",
    "anyCollection",
    "anyDouble",
    "anyFloat",
    "anyInt",
    "anyList",
    "anyLong",
    "anyMap",
    "anySet",
    "anyShort",
    "anyString",
    "argThat",
    "contains",
    "endsWith",
    "eq",
    "isA",
    "isNull",
    "matches",
    "notNull",
    "nullable",
    "same",
    "startsWith",
}

MOCKITO_CORE_SYMBOLS = {
    "after",
    "atLeast",
    "atLeastOnce",
    "atMost",
    "atMostOnce",
    "doAnswer",
    "doCallRealMethod",
    "doNothing",
    "doReturn",
    "doThrow",
    "inOrder",
    "mock",
    "never",
    "only",
    "reset",
    "spy",
    "timeout",
    "times",
    "verify",
    "verifyNoInteractions",
    "verifyNoMoreInteractions",
    "when",
}

ASSERTION_SYMBOLS = {
    "assertAll",
    "assertArrayEquals",
    "assertDoesNotThrow",
    "assertEquals",
    "assertFalse",
    "assertNotEquals",
    "assertNotNull",
    "assertNotSame",
    "assertNull",
    "assertSame",
    "assertThrows",
    "assertTimeout",
    "assertTrue",
    "fail",
}

JAVA_UTIL_TYPES = {
    "ArrayDeque",
    "ArrayList",
    "Arrays",
    "Collections",
    "Comparator",
    "Date",
    "HashMap",
    "HashSet",
    "LinkedHashMap",
    "LinkedHashSet",
    "LinkedList",
    "List",
    "Map",
    "Objects",
    "Optional",
    "Queue",
    "Random",
    "Set",
    "UUID",
}

JAVA_IO_TYPES = {
    "ByteArrayInputStream",
    "ByteArrayOutputStream",
    "File",
    "IOException",
    "InputStream",
    "OutputStream",
    "PrintStream",
    "Reader",
    "StringReader",
    "StringWriter",
    "Writer",
}


def repair_imports_for_context(code: str, context: ExperimentContext) -> ImportRepairResult:
    dependency_text = _dependency_text(context.dependencies, context.existing_tests)
    testing_framework = _normalize_framework(context.testing_framework, context.existing_tests)
    return repair_imports(
        code,
        testing_framework=testing_framework,
        dependency_text=dependency_text,
    )


def repair_imports(
    code: str,
    *,
    testing_framework: str = "unknown",
    dependency_text: str = "",
) -> ImportRepairResult:
    imports: list[str] = []
    unavailable: list[str] = []
    dependency_text = dependency_text.lower()
    testing_framework = (testing_framework or "unknown").lower()

    has_mockito = "mockito" in dependency_text or "org.mockito" in code
    has_assertj = "assertj" in dependency_text or "org.assertj" in code

    if _uses_any_symbol(code, MOCKITO_MATCHER_SYMBOLS):
        if has_mockito:
            _add_import(imports, code, "import static org.mockito.ArgumentMatchers.*;")
        else:
            unavailable.append("mockito-argument-matchers")

    if _uses_any_symbol(code, MOCKITO_CORE_SYMBOLS):
        if has_mockito:
            _add_import(imports, code, "import static org.mockito.Mockito.*;")
        else:
            unavailable.append("mockito-core")

    if _uses_annotation(code, "Test"):
        if testing_framework == "junit5":
            _add_import(imports, code, "import org.junit.jupiter.api.Test;")
        elif testing_framework == "junit4":
            _add_import(imports, code, "import org.junit.Test;")
        elif testing_framework == "testng":
            _add_import(imports, code, "import org.testng.annotations.Test;")

    for annotation, import_line in _framework_annotation_imports(testing_framework).items():
        if _uses_annotation(code, annotation):
            _add_import(imports, code, import_line)

    if _uses_any_symbol(code, ASSERTION_SYMBOLS):
        if testing_framework == "junit5":
            _add_import(imports, code, "import static org.junit.jupiter.api.Assertions.*;")
        elif testing_framework == "junit4":
            _add_import(imports, code, "import static org.junit.Assert.*;")
        elif testing_framework == "testng":
            _add_import(imports, code, "import static org.testng.Assert.*;")

    if _uses_symbol(code, "assertThat") and has_assertj:
        _add_import(imports, code, "import static org.assertj.core.api.Assertions.assertThat;")

    if _uses_annotation(code, "RunWith"):
        _add_import(imports, code, "import org.junit.runner.RunWith;")
    if _uses_symbol(code, "MockitoJUnitRunner"):
        _add_import(imports, code, "import org.mockito.junit.MockitoJUnitRunner;")
    if _uses_annotation(code, "ExtendWith"):
        _add_import(imports, code, "import org.junit.jupiter.api.extension.ExtendWith;")
    if _uses_symbol(code, "MockitoExtension"):
        _add_import(imports, code, "import org.mockito.junit.jupiter.MockitoExtension;")

    if _uses_symbol(code, "ArgumentCaptor"):
        if has_mockito:
            _add_import(imports, code, "import org.mockito.ArgumentCaptor;")
        else:
            unavailable.append("mockito-argument-captor")
    for annotation in ("Mock", "Spy", "InjectMocks", "Captor"):
        if _uses_annotation(code, annotation) and has_mockito:
            _add_import(imports, code, f"import org.mockito.{annotation};")
    if _uses_symbol(code, "MockitoAnnotations") and has_mockito:
        _add_import(imports, code, "import org.mockito.MockitoAnnotations;")

    for type_name in sorted(JAVA_UTIL_TYPES):
        if _uses_type(code, type_name):
            _add_import(imports, code, f"import java.util.{type_name};")
    for type_name in sorted(JAVA_IO_TYPES):
        if _uses_type(code, type_name):
            _add_import(imports, code, f"import java.io.{type_name};")

    repaired = _insert_imports(code, imports)
    return ImportRepairResult(code=repaired, added_imports=imports, unavailable_symbols=sorted(set(unavailable)))


def _dependency_text(dependencies: list[dict[str, Any]], existing_tests: list[str]) -> str:
    dep_text = " ".join(
        f"{dep.get('group_id', '')}:{dep.get('artifact_id', '')}:{dep.get('version', '')}"
        for dep in dependencies
    )
    return dep_text + "\n" + "\n".join(existing_tests[:3])


def _normalize_framework(framework: str, existing_tests: list[str]) -> str:
    value = (framework or "unknown").lower()
    if value != "unknown":
        return value
    text = "\n".join(existing_tests[:3])
    if "org.junit." in text or "junit.framework" in text:
        return "junit4"
    if "org.junit.jupiter.api.Test" in text or "org.junit.jupiter.api.BeforeEach" in text or "@ExtendWith" in text:
        return "junit5"
    if "org.testng" in text:
        return "testng"
    return value


def _framework_annotation_imports(framework: str) -> dict[str, str]:
    if framework == "junit5":
        return {
            "AfterAll": "import org.junit.jupiter.api.AfterAll;",
            "AfterEach": "import org.junit.jupiter.api.AfterEach;",
            "BeforeAll": "import org.junit.jupiter.api.BeforeAll;",
            "BeforeEach": "import org.junit.jupiter.api.BeforeEach;",
            "Disabled": "import org.junit.jupiter.api.Disabled;",
        }
    if framework == "junit4":
        return {
            "After": "import org.junit.After;",
            "AfterClass": "import org.junit.AfterClass;",
            "Before": "import org.junit.Before;",
            "BeforeClass": "import org.junit.BeforeClass;",
            "Ignore": "import org.junit.Ignore;",
        }
    if framework == "testng":
        return {
            "AfterClass": "import org.testng.annotations.AfterClass;",
            "AfterMethod": "import org.testng.annotations.AfterMethod;",
            "BeforeClass": "import org.testng.annotations.BeforeClass;",
            "BeforeMethod": "import org.testng.annotations.BeforeMethod;",
        }
    return {}


def _uses_any_symbol(code: str, symbols: set[str]) -> bool:
    return any(_uses_symbol(code, symbol) for symbol in symbols)


def _uses_symbol(code: str, symbol: str) -> bool:
    if _is_imported(code, symbol):
        return False
    return re.search(rf"(?<![\w.]){re.escape(symbol)}\s*\(", code) is not None


def _uses_annotation(code: str, annotation: str) -> bool:
    if _is_imported(code, annotation):
        return False
    return re.search(rf"(?<![\w.])@{re.escape(annotation)}\b", code) is not None


def _uses_type(code: str, type_name: str) -> bool:
    if _is_imported(code, type_name):
        return False
    if re.search(rf"\b{re.escape(type_name)}\s*[<\w\[]", code):
        return True
    return re.search(rf"\bnew\s+{re.escape(type_name)}\s*[<(]", code) is not None


def _is_imported(code: str, symbol: str) -> bool:
    if re.search(rf"(?m)^\s*import\s+(?:static\s+)?[A-Za-z_][A-Za-z0-9_.]*\.{re.escape(symbol)}\s*;", code):
        return True
    wildcard_packages = _wildcard_packages_for_symbol(symbol)
    return any(
        re.search(rf"(?m)^\s*import\s+(?:static\s+)?{re.escape(package)}\.\*\s*;", code)
        for package in wildcard_packages
    )


def _wildcard_packages_for_symbol(symbol: str) -> list[str]:
    packages: list[str] = []
    if symbol in JAVA_UTIL_TYPES:
        packages.append("java.util")
    if symbol in JAVA_IO_TYPES:
        packages.append("java.io")
    if symbol in MOCKITO_CORE_SYMBOLS:
        packages.append("org.mockito.Mockito")
    if symbol in MOCKITO_MATCHER_SYMBOLS:
        packages.append("org.mockito.ArgumentMatchers")
    if symbol in ASSERTION_SYMBOLS:
        packages.extend(["org.junit.Assert", "org.junit.jupiter.api.Assertions", "org.testng.Assert"])
    if symbol in {"Test", "Before", "After", "BeforeClass", "AfterClass", "Ignore", "RunWith"}:
        packages.extend(["org.junit", "org.junit.runner"])
    if symbol in {"Test", "BeforeEach", "AfterEach", "BeforeAll", "AfterAll", "Disabled", "ExtendWith"}:
        packages.extend(["org.junit.jupiter.api", "org.junit.jupiter.api.extension"])
    if symbol in {"Mock", "Spy", "InjectMocks", "Captor", "ArgumentCaptor", "MockitoAnnotations"}:
        packages.append("org.mockito")
    return packages


def _add_import(imports: list[str], code: str, import_line: str) -> None:
    if import_line in code or import_line in imports:
        return
    imports.append(import_line)


def _insert_imports(code: str, imports: list[str]) -> str:
    missing = [line for line in dict.fromkeys(imports) if line not in code]
    if not missing:
        return code
    insert_text = "\n".join(missing) + "\n"
    import_matches = list(re.finditer(r"(?m)^\s*import\s+(?:static\s+)?[A-Za-z_][A-Za-z0-9_.*]*\s*;", code))
    if import_matches:
        last = import_matches[-1]
        return code[: last.end()] + "\n" + insert_text + code[last.end():]
    package_match = re.search(r"(?m)^\s*package\s+[A-Za-z_][A-Za-z0-9_.]*\s*;", code)
    if package_match:
        return code[: package_match.end()] + "\n\n" + insert_text + code[package_match.end():].lstrip("\n")
    return insert_text + "\n" + code
