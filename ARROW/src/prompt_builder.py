from __future__ import annotations

import json
from pathlib import Path

from .models import ExperimentContext, VerificationResult


def load_template(project_root: Path, template_path: str) -> str:
    path = Path(template_path)
    if not path.is_absolute():
        path = project_root / path
    return path.read_text(encoding="utf-8")


def build_repair_prompt(
    *,
    template: str,
    context: ExperimentContext,
    best_generated_test: str,
    verification: VerificationResult,
    failed_signature_history: list[str] | None = None,
) -> str:
    payload = {
        "failure_state": verification.state.value if verification.state else None,
        "failure_origin": verification.failure_origin.value,
        "normalized_error_signature": verification.normalized_error_signature,
        "primary_error": verification.primary_error,
        "primary_error_lines": verification.raw_output.splitlines()[:80],
        "failed_signature_history": failed_signature_history or [],
        "package_name": context.package_name,
        "required_generated_class_name": context.generated_test_class_name,
        "java_version": context.java_version,
        "testing_framework": context.testing_framework,
        "build_tool": context.build_tool,
        "module_path": context.module_path,
        "dependencies": context.dependencies[:40],
        "public_api": context.public_api,
        "existing_tests": context.existing_tests[:3],
        "focal_class_source": context.focal_class_source,
        "current_best_generated_test": best_generated_test,
    }
    return _render_template(template, payload).rstrip() + "\n\nContext JSON:\n" + json.dumps(payload, indent=2, ensure_ascii=False)


def build_generation_prompt(
    *,
    template: str,
    context: ExperimentContext,
    examples: list[dict] | None = None,
) -> str:
    payload = {
        "package_name": context.package_name,
        "required_generated_class_name": context.generated_test_class_name,
        "java_version": context.java_version,
        "testing_framework": context.testing_framework,
        "build_tool": context.build_tool,
        "module_path": context.module_path,
        "dependencies": context.dependencies[:40],
        "public_api": context.public_api,
        "existing_tests": context.existing_tests[:2],
        "focal_class_source": context.focal_class_source,
        "examples": examples or [],
    }
    return _render_template(template, payload).rstrip() + "\n\nContext JSON:\n" + json.dumps(payload, indent=2, ensure_ascii=False)


def build_regeneration_prompt(
    *,
    template: str,
    context: ExperimentContext,
    failed_signature_history: list[str],
) -> str:
    payload = {
        "package_name": context.package_name,
        "required_generated_class_name": context.generated_test_class_name,
        "java_version": context.java_version,
        "testing_framework": context.testing_framework,
        "build_tool": context.build_tool,
        "module_path": context.module_path,
        "dependencies": context.dependencies[:40],
        "public_api": context.public_api,
        "existing_tests": context.existing_tests[:3],
        "focal_class_source": context.focal_class_source,
        "avoid_failed_signatures_and_patterns": failed_signature_history[-20:],
    }
    return _render_template(template, payload).rstrip() + "\n\nRegenerate from focal/project context only. Do not repeat failed APIs or patterns.\nContext JSON:\n" + json.dumps(payload, indent=2, ensure_ascii=False)


def _render_template(template: str, payload: dict) -> str:
    rendered = template
    for key, value in payload.items():
        if isinstance(value, (str, int, float, bool)) or value is None:
            rendered = rendered.replace("{" + key + "}", "" if value is None else str(value))
    return rendered
