from __future__ import annotations

from src.log_parser import parse_verification_output
from src.models import FailureOrigin, FailureState


def test_surefire_xml_preferred_over_console(tmp_path):
    report_dir = tmp_path / "target" / "surefire-reports"
    report_dir.mkdir(parents=True)
    (report_dir / "TEST-demo.FooTest.xml").write_text(
        """<testsuite tests="1" failures="1" errors="0"><testcase classname="demo.FooTest" name="fails"><failure type="java.lang.AssertionError" message="expected 1"/></testcase></testsuite>""",
        encoding="utf-8",
    )
    result = parse_verification_output(
        raw_output="console says something else",
        exit_code=1,
        timed_out=False,
        tool_name="maven",
        command=["mvn", "test"],
        module_root=tmp_path,
        generated_test_class="FooTest",
        target_only=True,
    )
    assert result.state == FailureState.ASSERTION_FAILED
    assert result.failed_test_ids == ["demo.FooTest#fails"]
    assert result.failure_origin == FailureOrigin.GENERATED_TEST


def test_timeout_returns_build_timeout(tmp_path):
    result = parse_verification_output(
        raw_output="timed out",
        exit_code=None,
        timed_out=True,
        tool_name="gradle",
        command=["gradle", "test"],
        module_root=tmp_path,
        generated_test_class="FooTest",
        target_only=True,
    )
    assert result.state == FailureState.BUILD_TIMEOUT
    assert result.failure_origin == FailureOrigin.INFRASTRUCTURE


def test_success_exit_code_ignores_spring_debug_log_noise(tmp_path):
    result = parse_verification_output(
        raw_output=(
            "16:47:57.709 [main] DEBUG org.springframework.test.context.support.AbstractDirtiesContextTestExecutionListener "
            "- before test class: context [DefaultTestContext testException = [null]]\n"
            "Results :\n\n"
            "Tests run: 34, Failures: 0, Errors: 0, Skipped: 0\n"
            "[INFO] BUILD SUCCESS\n"
        ),
        exit_code=0,
        timed_out=False,
        tool_name="maven",
        command=["mvn", "test"],
        module_root=tmp_path,
        generated_test_class="FooTest",
        target_only=False,
    )
    assert result.state == FailureState.MODULE_TESTS_PASSED
    assert result.failure_origin == FailureOrigin.UNKNOWN
    assert result.compile_errors == 0
    assert result.error_signatures == []


def test_target_success_with_zero_tests_is_discovery_failure(tmp_path):
    result = parse_verification_output(
        raw_output=(
            "Results :\n\n"
            "Tests run: 0, Failures: 0, Errors: 0, Skipped: 0\n"
            "[INFO] BUILD SUCCESS\n"
        ),
        exit_code=0,
        timed_out=False,
        tool_name="maven",
        command=["mvn", "-Dtest=FooTest_1234abcd", "test"],
        module_root=tmp_path,
        generated_test_class="FooTest_1234abcd",
        target_only=True,
    )
    assert result.state == FailureState.TEST_DISCOVERY_FAILED
    assert result.failure_origin == FailureOrigin.UNKNOWN
    assert result.normalized_error_signature == "generated target test was not discovered"


def test_module_success_with_zero_tests_can_still_pass(tmp_path):
    result = parse_verification_output(
        raw_output=(
            "Results :\n\n"
            "Tests run: 0, Failures: 0, Errors: 0, Skipped: 0\n"
            "[INFO] BUILD SUCCESS\n"
        ),
        exit_code=0,
        timed_out=False,
        tool_name="maven",
        command=["mvn", "test"],
        module_root=tmp_path,
        generated_test_class="FooTest_1234abcd",
        target_only=False,
    )
    assert result.state == FailureState.MODULE_TESTS_PASSED


def test_missing_selected_maven_reactor_project_is_build_configuration_failure(tmp_path):
    result = parse_verification_output(
        raw_output=(
            "[ERROR] Could not find the selected project in the reactor: "
            "hello-world/src/main/resources/archetype-resources\n"
        ),
        exit_code=1,
        timed_out=False,
        tool_name="maven",
        command=["mvn", "-pl", "hello-world/src/main/resources/archetype-resources", "test"],
        module_root=tmp_path,
        generated_test_class="HelloWorldBuilderTest_abcd1234",
        target_only=False,
    )

    assert result.state == FailureState.RUNTIME_FAILED
    assert result.failure_origin == FailureOrigin.BUILD_CONFIGURATION
