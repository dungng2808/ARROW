from __future__ import annotations

import pytest

from src.build_runner import BuildContext, select_gradle_command, select_maven_command, target_test_command
from src.repo_manager import safe_remove_tree
from src.test_writer import JavaValidationError, validate_java_candidate, write_owned_generated_test


def test_validate_java_strips_fence_and_checks_package_and_class():
    code, digest = validate_java_candidate(
        """```java
package demo;
public class FooTest_1234abcd {
}
```""",
        expected_package="demo",
        expected_class_name="FooTest_1234abcd",
    )
    assert code.startswith("package demo;")
    assert digest


def test_validate_java_normalizes_common_generated_class_name_variants():
    code, _digest = validate_java_candidate(
        """package demo;
public class FooGeneratedTest_1234abcd {
}
""",
        expected_package="demo",
        expected_class_name="FooTest_1234abcd",
    )

    assert "public class FooTest_1234abcd" in code


def test_validate_java_normalizes_unhashed_base_test_class_name():
    code, _digest = validate_java_candidate(
        """package demo;
public class FooTest {
}
""",
        expected_package="demo",
        expected_class_name="FooTest_1234abcd",
    )

    assert "public class FooTest_1234abcd" in code
    assert "public class FooTest {" not in code


def test_validate_java_adds_missing_junit5_imports():
    code, _digest = validate_java_candidate(
        """package demo;

import static org.junit.jupiter.api.Assertions.*;

public class FooTest_1234abcd {
    @BeforeEach
    public void setUp() {
    }

    @Test
    public void works() {
        assertEquals(1, 1);
    }
}
""",
        expected_package="demo",
        expected_class_name="FooTest_1234abcd",
        testing_framework="junit5",
    )

    assert "import org.junit.jupiter.api.BeforeEach;" in code
    assert "import org.junit.jupiter.api.Test;" in code


def test_validate_java_normalizes_junit4_lifecycle_annotations():
    code, _digest = validate_java_candidate(
        """package demo;

public class FooTest_1234abcd {
    @BeforeEach
    public void setUp() {
    }

    @Test
    public void works() {
        assertEquals(1, 1);
    }
}
""",
        expected_package="demo",
        expected_class_name="FooTest_1234abcd",
        testing_framework="junit4",
    )

    assert "@Before" in code
    assert "@BeforeEach" not in code
    assert "import org.junit.Before;" in code
    assert "import org.junit.Test;" in code
    assert "import static org.junit.Assert.*;" in code


def test_validate_java_rejects_unrelated_class_name():
    with pytest.raises(JavaValidationError):
        validate_java_candidate(
            "package demo;\npublic class BarGeneratedTest_1234abcd {}\n",
            expected_package="demo",
            expected_class_name="FooTest_1234abcd",
        )


def test_invalid_diff_is_rejected():
    with pytest.raises(JavaValidationError):
        validate_java_candidate(
            "--- a/pom.xml\n+++ b/pom.xml",
            expected_package="demo",
            expected_class_name="FooTest",
        )


def test_human_written_test_is_not_overwritten(tmp_path):
    test_path = tmp_path / "src" / "test" / "java" / "demo" / "FooTest.java"
    test_path.parent.mkdir(parents=True)
    test_path.write_text("package demo; public class FooTest {}", encoding="utf-8")
    with pytest.raises(JavaValidationError):
        write_owned_generated_test(
            experiment_id="exp",
            workspace=tmp_path,
            generated_test_path=test_path,
            generated_test_class_name="FooTest",
            code="package demo; public class FooTest {}",
        )


def test_path_traversal_outside_workspace_is_rejected(tmp_path):
    with pytest.raises(JavaValidationError):
        write_owned_generated_test(
            experiment_id="exp",
            workspace=tmp_path / "workspace",
            generated_test_path=tmp_path / "outside.java",
            generated_test_class_name="FooTest",
            code="package demo; public class FooTest {}",
        )


def test_maven_wrapper_selection_windows(monkeypatch, tmp_path):
    monkeypatch.setattr("src.build_runner._is_windows", lambda: True)
    repo = tmp_path / "repo"
    module = repo / "module"
    module.mkdir(parents=True)
    (repo / "mvnw.cmd").write_text("@echo off", encoding="utf-8")
    (module / "pom.xml").write_text("<project/>", encoding="utf-8")
    ctx = BuildContext(repo, module, "maven", "FooTest", "demo.FooTest")
    command = select_maven_command(ctx)
    assert command[0].endswith("mvnw.cmd")
    assert "-f" in command


def test_maven_root_with_pl_am_for_submodule(tmp_path):
    repo = tmp_path / "repo"
    module = repo / "mysql" / "codec"
    module.mkdir(parents=True)
    (repo / "pom.xml").write_text("<project/>", encoding="utf-8")
    (module / "pom.xml").write_text("<project/>", encoding="utf-8")
    ctx = BuildContext(
        repo,
        module,
        "maven",
        "FooTest",
        "demo.FooTest",
        maven_multi_module_strategy="root_with_pl_am",
        maven_use_also_make=True,
    )
    command = select_maven_command(ctx)
    assert command[command.index("-f") + 1] == str(repo / "pom.xml")
    assert command[command.index("-pl") + 1] == "mysql/codec"
    assert "-am" in command


def test_maven_target_test_ignores_no_matching_tests_in_also_make_modules(tmp_path):
    repo = tmp_path / "repo"
    module = repo / "service"
    module.mkdir(parents=True)
    (repo / "pom.xml").write_text("<project/>", encoding="utf-8")
    (module / "pom.xml").write_text("<project/>", encoding="utf-8")
    ctx = BuildContext(
        repo,
        module,
        "maven",
        "GeneratedTest",
        "demo.GeneratedTest",
        maven_multi_module_strategy="root_with_pl_am",
        maven_use_also_make=True,
        maven_fail_if_no_specified_tests=False,
    )
    _tool, command, _cwd = target_test_command(ctx)
    assert "-Dsurefire.failIfNoSpecifiedTests=false" in command
    assert "-Dtest=GeneratedTest" in command


def test_gradle_wrapper_selection_windows(monkeypatch, tmp_path):
    monkeypatch.setattr("src.build_runner._is_windows", lambda: True)
    repo = tmp_path / "repo"
    module = repo / "module"
    module.mkdir(parents=True)
    (repo / "gradlew.bat").write_text("@echo off", encoding="utf-8")
    ctx = BuildContext(repo, module, "gradle", "FooTest", "demo.FooTest")
    command = select_gradle_command(ctx)
    assert command[0].endswith("gradlew.bat")


def test_safe_remove_tree_removes_only_inside_allowed_root(tmp_path):
    allowed_root = tmp_path / "runs"
    target = allowed_root / "exp" / "workspace"
    target.mkdir(parents=True)
    (target / "file.txt").write_text("temporary checkout", encoding="utf-8")

    assert safe_remove_tree(target, allowed_root) is True
    assert not target.exists()

    outside = tmp_path / "outside"
    outside.mkdir()
    with pytest.raises(ValueError):
        safe_remove_tree(outside, allowed_root)
    assert outside.exists()
