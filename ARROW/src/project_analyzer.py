from __future__ import annotations

import hashlib
import re
import xml.etree.ElementTree as ET
from pathlib import Path

from .java_resolver import detect_project_java_version, normalize_java_version
from .models import ExperimentContext, SampleInput


MAVEN_NS = {"mvn": "http://maven.apache.org/POM/4.0.0"}


def _strip_repo_prefix(workspace: Path, relative: str) -> Path:
    path = Path(relative)
    candidate = workspace / path
    if candidate.exists():
        return candidate
    if len(path.parts) > 1:
        candidate = workspace / Path(*path.parts[1:])
        if candidate.exists():
            return candidate
    return workspace / path


def _find_upwards_for_build(start: Path, stop: Path) -> tuple[str, Path]:
    current = start if start.is_dir() else start.parent
    stop = stop.resolve()
    while True:
        if (current / "pom.xml").is_file():
            return "maven", current
        if (current / "build.gradle").is_file() or (current / "build.gradle.kts").is_file():
            return "gradle", current
        if current.resolve() == stop or current.parent == current:
            break
        current = current.parent
    if (stop / "pom.xml").is_file():
        return "maven", stop
    if (stop / "build.gradle").is_file() or (stop / "build.gradle.kts").is_file():
        return "gradle", stop
    return "unknown", stop


def package_from_source(source: str) -> str:
    match = re.search(r"(?m)^package\s+([A-Za-z_][A-Za-z0-9_.]*)\s*;", source)
    return match.group(1) if match else ""


def _text_at(root: ET.Element | None, xpath: str) -> str | None:
    if root is None:
        return None
    element = root.find(xpath, MAVEN_NS)
    return element.text.strip() if element is not None and element.text else None


def _parse_pom(path: Path) -> ET.Element | None:
    if not path.is_file():
        return None
    try:
        return ET.parse(path).getroot()
    except ET.ParseError:
        return None


def _maven_deps(module_root: Path) -> list[dict[str, str]]:
    root = _parse_pom(module_root / "pom.xml")
    if root is None:
        return []
    deps = []
    for dep in root.findall(".//mvn:dependency", MAVEN_NS):
        group = _text_at(dep, "mvn:groupId")
        artifact = _text_at(dep, "mvn:artifactId")
        if group and artifact:
            deps.append({"group_id": group, "artifact_id": artifact, "version": _text_at(dep, "mvn:version") or "", "scope": _text_at(dep, "mvn:scope") or "compile"})
    return deps[:80]


def _detect_testing_framework(deps: list[dict[str, str]]) -> str:
    artifact_text = " ".join(f"{d.get('group_id')}:{d.get('artifact_id')}".lower() for d in deps)
    if "junit-jupiter" in artifact_text or "org.junit.jupiter" in artifact_text:
        return "junit5"
    if "junit:junit" in artifact_text:
        return "junit4"
    if "testng" in artifact_text:
        return "testng"
    return "unknown"


def _detect_testing_framework_from_sources(sources: list[str]) -> str:
    source_text = "\n".join(sources)
    junit4_markers = (
        "org.junit.Test",
        "org.junit.Before",
        "org.junit.After",
        "org.junit.BeforeClass",
        "org.junit.AfterClass",
        "org.junit.Assert",
        "@RunWith",
        "junit.framework",
    )
    junit5_markers = (
        "org.junit.jupiter.api.Test",
        "org.junit.jupiter.api.BeforeEach",
        "org.junit.jupiter.api.AfterEach",
        "org.junit.jupiter.api.BeforeAll",
        "org.junit.jupiter.api.AfterAll",
        "org.junit.jupiter.api.extension.ExtendWith",
        "@ExtendWith",
    )
    if any(marker in source_text for marker in junit4_markers):
        return "junit4"
    if any(marker in source_text for marker in junit5_markers):
        return "junit5"
    if "org.testng" in source_text:
        return "testng"
    return "unknown"


def _public_api(sample: SampleInput) -> dict[str, list[str]]:
    methods = []
    constructors = []
    for method in sample.raw.get("focal_class", {}).get("methods", []):
        full = method.get("full_signature") or method.get("signature") or ""
        if "public" not in (method.get("modifiers") or ""):
            continue
        if method.get("constructor"):
            constructors.append(full)
        else:
            methods.append(full)
    return {"constructors": constructors, "methods": methods}


def unique_generated_class_name(focal_class_name: str, experiment_id: str) -> str:
    digest = hashlib.sha1(experiment_id.encode("utf-8")).hexdigest()[:8]
    return f"{focal_class_name}Test_{digest}"


def analyze_experiment(
    *,
    sample: SampleInput,
    workspace: Path,
    run_id: str,
    shard_id: str,
    agent_name: str,
    generation_prompt: str,
) -> tuple[ExperimentContext, Path]:
    focal_path = _strip_repo_prefix(workspace, sample.focal_class_path)
    original_test_path = _strip_repo_prefix(workspace, sample.test_class_path)
    if not focal_path.is_file():
        raise FileNotFoundError(f"Cannot resolve focal class path: {sample.focal_class_path}")
    focal_source = focal_path.read_text(encoding="utf-8", errors="replace")
    package_name = package_from_source(focal_source)
    build_tool, module_root = _find_upwards_for_build(focal_path, workspace)
    deps = _maven_deps(module_root) if build_tool == "maven" else []
    existing_tests = []
    if original_test_path.is_file():
        existing_tests.append(original_test_path.read_text(encoding="utf-8", errors="replace")[:8000])
    framework = _detect_testing_framework_from_sources(existing_tests)
    if framework == "unknown":
        framework = _detect_testing_framework(deps)
    java_version, _java_source = detect_project_java_version(workspace, module_root)
    experiment_id = f"{run_id}:{shard_id}:{sample.input_id}:{agent_name}:{generation_prompt}"
    generated_class = unique_generated_class_name(sample.focal_class_name, experiment_id)
    generated_path = original_test_path.parent / f"{generated_class}.java"
    context = ExperimentContext(
        run_id=run_id,
        shard_id=shard_id,
        input_id=sample.input_id,
        agent_name=agent_name,
        generation_prompt=generation_prompt,
        workspace=workspace,
        generated_test_path=generated_path,
        generated_test_class_name=generated_class,
        package_name=package_name,
        focal_class_source=focal_source[:30000],
        java_version=normalize_java_version(java_version) if java_version else "unknown",
        testing_framework=framework,
        build_tool=build_tool,
        module_path=str(module_root.relative_to(workspace)) if module_root != workspace else ".",
        dependencies=deps,
        existing_tests=existing_tests,
        public_api=_public_api(sample),
    )
    return context, module_root
