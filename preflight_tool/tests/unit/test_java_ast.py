from __future__ import annotations

import pytest

from preflight.java_ast import parse_java_source, primitive_arguments


@pytest.mark.unit
@pytest.mark.parametrize(("source", "kind"), [
    ("package a; interface X {}", "interface"),
    ("package a; @interface X {}", "annotation"),
    ("package a; enum X { A }", "enum"),
    ("package a; record X(int a) {}", "record"),
    ("package a; public class Repository<T> { public T get() { return null; } }", "class"),
])
def test_declaration_kinds(source, kind):
    assert parse_java_source(source, "src/main/java/a/X.java").kind == kind


@pytest.mark.unit
def test_default_constructor_is_counted_and_private_members_are_not():
    java = parse_java_source("package a; public class X { private void hidden() {} public int visible() { return 1; } }", "src/main/java/a/X.java")
    assert {(api.kind, api.name) for api in java.apis} == {("constructor", "X"), ("method", "visible")}


@pytest.mark.unit
def test_explicit_constructor_suppresses_implicit_default_constructor():
    java = parse_java_source("package a; public class X { public X(int val) {} }", "src/main/java/a/X.java")
    constructors = [api for api in java.apis if api.kind == "constructor"]
    assert len(constructors) == 1
    assert constructors[0].parameters == "(int val)"


@pytest.mark.unit
def test_nested_target_is_identified_as_non_top_level():
    java = parse_java_source("package a; public class Outer { class Inner { void ok() {} } }", "src/main/java/a/Outer.java", expected_name="Inner")
    assert java and not java.is_top_level and java.name == "Inner"


@pytest.mark.unit
def test_parse_java_source_returns_none_when_expected_name_missing():
    assert parse_java_source("package a; public class Other {}", "src/main/java/a/Other.java", expected_name="Target") is None


@pytest.mark.unit
def test_tags_and_probe_arguments_cover_supported_types():
    source = """package a; import java.sql.DataSource; class X { private X() {} static int f(int a) { return a; } }"""
    java = parse_java_source(source, "src/main/java/a/X.java")
    assert {"DB_DEPENDENT", "REQUIRES_MOCK", "STATIC_UTILITY", "PACKAGE_PRIVATE"}.issubset(java.tags)
    args = "(boolean b, byte by, short sh, int i, long l, float f, double d, char c, @NotNull final String s, int[] arr, boolean... varargs)"
    assert primitive_arguments(args) == "false, (byte) 0, (short) 0, 0, 0L, 0F, 0D, '\\0', null, null, null"
    assert primitive_arguments("()") == ""

