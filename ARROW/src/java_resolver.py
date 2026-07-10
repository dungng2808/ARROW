from __future__ import annotations

import os
import re
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Any


@dataclass
class JavaSelection:
    requested_version: str = "unknown"
    java_home: str = ""
    source: str = "default"
    reason: str = "no project java version detected"


def resolve_java_home(repository_root: Path, module_root: Path, config: dict[str, Any], manual_java_home: str | None = None) -> JavaSelection:
    if manual_java_home:
        return JavaSelection(requested_version="manual", java_home=manual_java_home, source="manual", reason="--java-home")

    build_cfg = config.get("build", {})
    default_java_home = _configured_java_home_path(build_cfg.get("java_default"))

    version, source = detect_project_java_version(repository_root, module_root)
    if not version:
        if default_java_home:
            return JavaSelection(
                requested_version="default",
                java_home=default_java_home,
                source="build.java_default",
                reason="no project java version detected; using build.java_default",
            )
        return JavaSelection()

    normalized = normalize_java_version(version)
    configured_java_home, configured_key = _configured_java_home_for_version(normalized, build_cfg.get("java_homes"))
    if configured_java_home:
        return JavaSelection(
            requested_version=normalized,
            java_home=configured_java_home,
            source=source,
            reason=f"matched build.java_homes.{configured_key}",
        )
    if default_java_home:
        return JavaSelection(
            requested_version=normalized,
            java_home=default_java_home,
            source=source,
            reason=f"JDK {normalized} not mapped; using build.java_default",
        )
    return JavaSelection(requested_version=normalized, source=source, reason=f"JDK {normalized} not mapped; using system default Java")


def normalize_java_version(version: str) -> str:
    text = str(version).strip().strip('"').strip("'")
    if text.startswith("1."):
        parts = text.split(".")
        if len(parts) > 1 and parts[1].isdigit():
            return parts[1]
    match = re.search(r"\d+", text)
    return match.group(0) if match else text


def detect_project_java_version(repository_root: Path, module_root: Path) -> tuple[str, str]:
    candidates = [
        (module_root / "pom.xml", _detect_maven_java_version),
        (repository_root / "pom.xml", _detect_maven_java_version),
        (module_root / "build.gradle", _detect_gradle_java_version),
        (module_root / "build.gradle.kts", _detect_gradle_java_version),
        (repository_root / "build.gradle", _detect_gradle_java_version),
        (repository_root / "build.gradle.kts", _detect_gradle_java_version),
        (repository_root / "gradle.properties", _detect_properties_java_version),
        (module_root / "gradle.properties", _detect_properties_java_version),
        (repository_root / "system.properties", _detect_properties_java_version),
        (repository_root / ".java-version", _detect_plain_version),
        (module_root / ".java-version", _detect_plain_version),
    ]
    seen: set[Path] = set()
    for path, detector in candidates:
        if path in seen or not path.is_file():
            continue
        seen.add(path)
        version = detector(path)
        if version:
            return version, str(path)
    return "", ""


def _detect_plain_version(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="ignore").strip().splitlines()[0].strip()


def _detect_maven_java_version(path: Path) -> str:
    text = path.read_text(encoding="utf-8", errors="ignore")
    properties = {match.group(1): match.group(2).strip() for match in re.finditer(r"<([A-Za-z0-9_.-]+)>\s*([^<]+)\s*</\1>", text)}
    patterns = [
        r"<maven\.compiler\.release>\s*([^<]+)\s*</maven\.compiler\.release>",
        r"<maven\.compiler\.source>\s*([^<]+)\s*</maven\.compiler\.source>",
        r"<java\.version>\s*([^<]+)\s*</java\.version>",
        r"<source>\s*([^<]+)\s*</source>",
        r"<release>\s*([^<]+)\s*</release>",
    ]
    for pattern in patterns:
        match = re.search(pattern, text, flags=re.IGNORECASE)
        if match:
            return _resolve_maven_property(match.group(1).strip(), properties)
    return ""


def _resolve_maven_property(value: str, properties: dict[str, str]) -> str:
    match = re.fullmatch(r"\$\{([^}]+)\}", value)
    if not match:
        return value
    key = match.group(1)
    resolved = properties.get(key, value)
    if resolved == value:
        return value
    return _resolve_maven_property(resolved, properties)


def _detect_gradle_java_version(path: Path) -> str:
    text = path.read_text(encoding="utf-8", errors="ignore")
    patterns = [
        r"sourceCompatibility\s*=\s*['\"]?([\w.]+)['\"]?",
        r"targetCompatibility\s*=\s*['\"]?([\w.]+)['\"]?",
        r"JavaVersion\.VERSION_(\d+)",
        r"languageVersion\s*=\s*JavaLanguageVersion\.of\((\d+)\)",
        r"sourceCompatibility\s*=\s*JavaVersion\.toVersion\(['\"]?([\w.]+)['\"]?\)",
    ]
    for pattern in patterns:
        match = re.search(pattern, text)
        if match:
            return match.group(1).replace("_", ".").strip()
    return ""


def _detect_properties_java_version(path: Path) -> str:
    text = path.read_text(encoding="utf-8", errors="ignore")
    patterns = [
        r"(?m)^\s*java\.runtime\.version\s*=\s*([\w.]+)",
        r"(?m)^\s*java\.version\s*=\s*([\w.]+)",
        r"(?m)^\s*org\.gradle\.java\.home\s*=\s*(.+)$",
    ]
    for pattern in patterns:
        match = re.search(pattern, text)
        if match:
            value = match.group(1).strip()
            if "org.gradle.java.home" in pattern:
                detected = _java_version_from_home(value)
                if detected:
                    return detected
            else:
                return value
    return ""


def _configured_java_home_for_version(version: str, java_homes: Any) -> tuple[str, str]:
    if not isinstance(java_homes, dict):
        return "", ""
    wanted = normalize_java_version(version)
    for key, value in java_homes.items():
        if not value or _normalize_java_map_key(key) != wanted:
            continue
        home = _expand_path(value)
        if home.is_dir() and _looks_like_java_home(home):
            return str(home), str(key)
    return "", ""


def _configured_java_home_path(value: Any) -> str:
    if not value:
        return ""
    home = _expand_path(value)
    if home.is_dir() and _looks_like_java_home(home):
        return str(home)
    return ""


def _normalize_java_map_key(key: Any) -> str:
    text = str(key).strip().strip('"').strip("'").lower()
    match = re.search(r"(1\.\d+|\d+)", text)
    return normalize_java_version(match.group(1)) if match else normalize_java_version(text)


def _expand_path(value: Any) -> Path:
    text = str(value).strip().strip('"').strip("'")
    return Path(os.path.expanduser(os.path.expandvars(text)))


def _looks_like_java_home(path: Path) -> bool:
    java = path / "bin" / ("java.exe" if os.name == "nt" else "java")
    if java.is_file():
        return True
    return bool(re.search(r"(?:jdk|jre|java|corretto|temurin|zulu|graalvm)[-_]?([0-9][0-9._-]*)", path.name, re.IGNORECASE))


def _java_version_from_home(home: str) -> str:
    java = Path(home) / "bin" / ("java.exe" if os.name == "nt" else "java")
    if not java.is_file():
        return ""
    try:
        result = subprocess.run([str(java), "-version"], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=10)
    except Exception:
        return ""
    output = (result.stderr or "") + "\n" + (result.stdout or "")
    match = re.search(r'version\s+"([^"]+)"', output)
    return normalize_java_version(match.group(1)) if match else ""
