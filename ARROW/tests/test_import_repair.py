from pathlib import Path

from src.import_repair import repair_imports, repair_imports_for_context
from src.models import ExperimentContext


def _context(**overrides):
    values = {
        "run_id": "run",
        "shard_id": "shard",
        "input_id": "sample",
        "agent_name": "agent",
        "generation_prompt": "zero-shot",
        "workspace": Path("."),
        "generated_test_path": Path("FooTest.java"),
        "generated_test_class_name": "FooTest",
        "package_name": "demo",
        "testing_framework": "junit4",
        "dependencies": [{"group_id": "org.mockito", "artifact_id": "mockito-core", "version": "2.28.2"}],
        "existing_tests": [],
    }
    values.update(overrides)
    return ExperimentContext(**values)


def test_repair_adds_mockito_matcher_import_for_any_string():
    code = """package demo;

import org.junit.Test;

public class FooTest {
    @Test
    public void run() {
        when(service.find(anyString())).thenReturn("ok");
    }
}
"""

    result = repair_imports_for_context(code, _context())

    assert result.changed
    assert "import static org.mockito.ArgumentMatchers.*;" in result.code
    assert "import static org.mockito.Mockito.*;" in result.code
    assert result.unavailable_symbols == []


def test_repair_does_not_add_mockito_import_when_dependency_is_unknown():
    code = """package demo;

public class FooTest {
    public void run() {
        mock(Service.class);
    }
}
"""

    result = repair_imports(code, testing_framework="junit4", dependency_text="")

    assert not result.changed
    assert result.unavailable_symbols == ["mockito-core"]
    assert "org.mockito" not in result.code


def test_repair_adds_junit5_test_and_assertion_imports():
    code = """package demo;

public class FooTest {
    @Test
    void run() {
        assertEquals(1, 1);
    }
}
"""

    result = repair_imports(code, testing_framework="junit5", dependency_text="org.junit.jupiter:junit-jupiter")

    assert "import org.junit.jupiter.api.Test;" in result.code
    assert "import static org.junit.jupiter.api.Assertions.*;" in result.code


def test_java_util_wildcard_does_not_hide_missing_assertion_import():
    code = """package demo;

import java.util.*;

public class FooTest {
    @Test
    public void run() {
        List<String> values = new ArrayList<>();
        assertTrue(values.isEmpty());
    }
}
"""

    result = repair_imports(code, testing_framework="junit4", dependency_text="junit:junit")

    assert "import static org.junit.Assert.*;" in result.code
    assert "import java.util.List;" not in result.code
    assert "import java.util.ArrayList;" not in result.code


def test_context_repair_prefers_junit4_existing_annotations_over_junit5_assertions():
    code = """package demo;

public class FooTest {
    @Test
    public void run() {
        assertTrue(true);
    }
}
"""
    context = _context(
        testing_framework="unknown",
        existing_tests=[
            "import org.junit.Before;\nimport org.junit.Test;\nimport static org.junit.jupiter.api.Assertions.*;"
        ],
    )

    result = repair_imports_for_context(code, context)

    assert "import org.junit.Test;" in result.code
    assert "import static org.junit.Assert.*;" in result.code
    assert "junit.jupiter" not in result.code
