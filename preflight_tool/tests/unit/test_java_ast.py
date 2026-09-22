from __future__ import annotations

import pytest

from preflight.java_ast import parse_java_source, primitive_arguments


@pytest.mark.unit
@pytest.mark.parametrize(("source", "kind"), [
    ("package a; interface X {}", "interface"),
    ("package a; @interface X {}", "annotation"),
    ("package a; enum X { A }", "enum"),
    ("package a; record X(int a) {}", "record"),
])
def test_declaration_kinds(source, kind):
    assert parse_java_source(source, "src/main/java/a/X.java").kind == kind


@pytest.mark.unit
def test_default_constructor_is_counted_and_private_members_are_not():
    java = parse_java_source("package a; public class X { private void hidden() {} public int visible() { return 1; } }", "src/main/java/a/X.java")
    assert {(api.kind, api.name) for api in java.apis} == {("constructor", "X"), ("method", "visible")}


@pytest.mark.unit
def test_nested_target_is_identified_as_non_top_level():
    java = parse_java_source("package a; public class Outer { class Inner { void ok() {} } }", "src/main/java/a/Outer.java", expected_name="Inner")
    assert java and not java.is_top_level and java.name == "Inner"


@pytest.mark.unit
def test_tags_and_probe_arguments_cover_supported_types():
    source = """package a; import java.sql.DataSource; class X { private X() {} static int f(int a) { return a; } }"""
    java = parse_java_source(source, "src/main/java/a/X.java")
    assert {"DB_DEPENDENT", "REQUIRES_MOCK", "STATIC_UTILITY", "PACKAGE_PRIVATE"}.issubset(java.tags)
    assert primitive_arguments("(int a, long b, String c, int[] d, List<String> e, boolean... flags)") == "0, 0L, null, null, null, null"
