from __future__ import annotations

import re
import xml.etree.ElementTree as ET
from pathlib import Path

from .models import FailureOrigin, FailureState, VerificationResult


VOLATILE_PATTERNS = [
    (re.compile(r"[A-Za-z]:\\[^\s:]+"), "<path>"),
    (re.compile(r"/(?:[^/\s:]+/)+[^/\s:]+"), "<path>"),
    (re.compile(r":\[\d+,\d+\]"), ":[line,col]"),
    (re.compile(r":\d+"), ":<n>"),
    (re.compile(r"0x[0-9a-fA-F]+"), "0x<hex>"),
    (re.compile(r"\b\d{4}-\d{2}-\d{2}[T ][^\s]+"), "<timestamp>"),
]


def normalize_signature(text: str) -> str:
    normalized = text.strip().lower()
    for pattern, repl in VOLATILE_PATTERNS:
        normalized = pattern.sub(repl, normalized)
    normalized = re.sub(r"\s+", " ", normalized)
    return normalized[:300]


def _parse_xml_reports(report_files: list[Path], raw_output: str, generated_test_class: str) -> tuple[list[str], int, int, list[str]]:
    failed_ids: list[str] = []
    failures = 0
    errors = 0
    signatures: list[str] = []
    for report_file in report_files:
        try:
            root = ET.parse(report_file).getroot()
        except ET.ParseError:
            continue
        for testcase in root.findall(".//testcase"):
            class_name = testcase.attrib.get("classname", "")
            method_name = testcase.attrib.get("name", "")
            test_id = f"{class_name}#{method_name}" if method_name else class_name
            for child in testcase:
                if child.tag not in {"failure", "error"}:
                    continue
                if child.tag == "failure":
                    failures += 1
                else:
                    errors += 1
                failed_ids.append(test_id)
                message = child.attrib.get("message") or child.text or raw_output
                error_type = child.attrib.get("type", child.tag)
                signatures.append(normalize_signature(f"{child.tag}:{error_type}:{message}"))
    return failed_ids, failures, errors, signatures


def _find_reports(module_root: Path, tool_name: str) -> list[Path]:
    if tool_name == "maven":
        return list(module_root.glob("target/surefire-reports/*.xml")) + list(module_root.glob("target/failsafe-reports/*.xml"))
    if tool_name == "gradle":
        return list(module_root.glob("build/test-results/test/*.xml"))
    return []


def _compile_signatures(raw_output: str) -> list[str]:
    lines = []
    for line in raw_output.splitlines():
        lowered = line.lower()
        if any(token in lowered for token in ("compilation failure", "cannot find symbol", "package ", "symbol:", "error:")):
            lines.append(line.strip())
    return [normalize_signature(line) for line in lines[:20]]


def _tests_run_count(raw_output: str) -> int | None:
    matches = re.findall(r"Tests run:\s*(\d+)", raw_output, flags=re.IGNORECASE)
    if not matches:
        return None
    return sum(int(match) for match in matches)


def _classify_origin(raw_output: str, failed_test_ids: list[str], generated_test_class: str, state: FailureState) -> FailureOrigin:
    lowered = raw_output.lower()
    if state in {FailureState.TARGET_TEST_PASSED, FailureState.MODULE_TESTS_PASSED}:
        return FailureOrigin.UNKNOWN
    if state in {FailureState.BUILD_TIMEOUT}:
        return FailureOrigin.INFRASTRUCTURE
    if any(token in lowered for token in ("could not resolve", "failed to execute goal", "plugin", "settings.gradle", "pom.xml", "build.gradle")):
        if generated_test_class.lower() not in lowered:
            return FailureOrigin.BUILD_CONFIGURATION
    if generated_test_class and generated_test_class.lower() in lowered:
        return FailureOrigin.GENERATED_TEST
    if any(generated_test_class and generated_test_class in test_id for test_id in failed_test_ids):
        return FailureOrigin.GENERATED_TEST
    if failed_test_ids:
        return FailureOrigin.EXISTING_PROJECT
    return FailureOrigin.UNKNOWN


def parse_verification_output(
    *,
    raw_output: str,
    exit_code: int | None,
    timed_out: bool,
    tool_name: str,
    command: list[str],
    module_root: Path,
    generated_test_class: str,
    target_only: bool,
) -> VerificationResult:
    if timed_out:
        return VerificationResult(
            state=FailureState.BUILD_TIMEOUT,
            failure_origin=FailureOrigin.INFRASTRUCTURE,
            exit_code=exit_code,
            raw_output=raw_output,
            primary_error="build timeout",
            normalized_error_signature="build_timeout",
            error_signatures=["build_timeout"],
            timed_out=True,
            tool_name=tool_name,
            command=command,
        )

    lowered = raw_output.lower()
    if exit_code is None:
        state = FailureState.TOOL_ERROR
        signatures = ["tool_error"]
    else:
        report_files = _find_reports(module_root, tool_name)
        failed_ids, failures, errors, xml_signatures = _parse_xml_reports(report_files, raw_output, generated_test_class)
        compile_sigs = _compile_signatures(raw_output)
        tests_run = _tests_run_count(raw_output)
        if target_only and exit_code == 0 and tests_run == 0:
            state = FailureState.TEST_DISCOVERY_FAILED
            signatures = [normalize_signature("generated target test was not discovered")]
        elif exit_code == 0 and not failures and not errors:
            state = FailureState.TARGET_TEST_PASSED if target_only else FailureState.MODULE_TESTS_PASSED
            signatures = []
            compile_sigs = []
        elif compile_sigs:
            state = FailureState.COMPILE_FAILED
            signatures = compile_sigs
        elif any(token in lowered for token in ("no tests matching", "no matching tests", "test events were not received", "no tests found")):
            state = FailureState.TEST_DISCOVERY_FAILED
            signatures = [normalize_signature("test discovery failed")]
        elif failures:
            state = FailureState.ASSERTION_FAILED
            signatures = xml_signatures or [normalize_signature("assertion failed")]
        elif errors:
            state = FailureState.RUNTIME_FAILED
            signatures = xml_signatures or [normalize_signature("runtime error")]
        elif "assert" in lowered or "failure" in lowered:
            state = FailureState.ASSERTION_FAILED
            signatures = [normalize_signature(line) for line in raw_output.splitlines() if "failure" in line.lower()][:5]
        elif "exception" in lowered or "error" in lowered:
            state = FailureState.RUNTIME_FAILED
            signatures = [normalize_signature(line) for line in raw_output.splitlines() if "exception" in line.lower() or "error" in line.lower()][:5]
        else:
            state = FailureState.UNKNOWN_FAILED
            signatures = [normalize_signature(raw_output[:500] or "unknown failure")]
        origin = _classify_origin(raw_output, failed_ids, generated_test_class, state)
        primary = signatures[0] if signatures else ""
        return VerificationResult(
            state=state,
            failure_origin=origin,
            exit_code=exit_code,
            raw_output=raw_output,
            primary_error=primary,
            normalized_error_signature=primary,
            error_signatures=signatures,
            failed_test_ids=failed_ids,
            compile_errors=len(compile_sigs),
            test_failures=failures,
            test_errors=errors,
            timed_out=False,
            tool_name=tool_name,
            command=command,
        )

    return VerificationResult(
        state=state,
        failure_origin=FailureOrigin.INFRASTRUCTURE,
        exit_code=exit_code,
        raw_output=raw_output,
        primary_error=signatures[0],
        normalized_error_signature=signatures[0],
        error_signatures=signatures,
        tool_name=tool_name,
        command=command,
    )
