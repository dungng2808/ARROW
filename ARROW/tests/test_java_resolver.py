from __future__ import annotations

from src.java_resolver import detect_project_java_version, normalize_java_version, resolve_java_home


def test_detect_maven_java_version(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / "pom.xml").write_text(
        "<project><properties><maven.compiler.source>1.8</maven.compiler.source></properties></project>",
        encoding="utf-8",
    )
    version, source = detect_project_java_version(repo, repo)
    assert normalize_java_version(version) == "8"
    assert source.endswith("pom.xml")


def test_detect_maven_java_version_resolves_property(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / "pom.xml").write_text(
        "<project><properties><version.java>11</version.java></properties><build><plugins><plugin><configuration><release>${version.java}</release></configuration></plugin></plugins></build></project>",
        encoding="utf-8",
    )
    version, _source = detect_project_java_version(repo, repo)
    assert version == "11"


def test_detect_gradle_java_version(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / "build.gradle").write_text("java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }", encoding="utf-8")
    version, _source = detect_project_java_version(repo, repo)
    assert version == "17"


def test_resolve_uses_system_default_when_no_mapping_or_java_default(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("99", encoding="utf-8")

    selection = resolve_java_home(repo, repo, {})

    assert selection.requested_version == "99"
    assert selection.java_home == ""
    assert selection.reason == "JDK 99 not mapped; using system default Java"


def test_resolve_uses_java_default_when_project_version_is_not_mapped(monkeypatch, tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("17", encoding="utf-8")
    default_jdk = tmp_path / "jdk-21"
    default_jdk.mkdir()
    monkeypatch.setattr("src.java_resolver.discover_java_home", lambda _version: "")

    selection = resolve_java_home(repo, repo, {"build": {"java_default": str(default_jdk)}})

    assert selection.requested_version == "17"
    assert selection.java_home == str(default_jdk)
    assert selection.reason == "JDK 17 not mapped; using build.java_default"


def test_resolve_selects_platform_specific_java_home(monkeypatch, tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("1.8", encoding="utf-8")
    windows_jdk = tmp_path / "windows" / "jdk1.8.0_202"
    linux_jdk = tmp_path / "linux" / "java-8"
    windows_jdk.mkdir(parents=True)
    linux_jdk.mkdir(parents=True)
    monkeypatch.setattr("src.java_resolver.platform.system", lambda: "Linux")

    selection = resolve_java_home(
        repo,
        repo,
        {
            "build": {
                "java_homes": {
                    "java-8": {
                        "windows": str(windows_jdk),
                        "linux": str(linux_jdk),
                    }
                }
            }
        },
    )

    assert selection.java_home == str(linux_jdk)
    assert selection.reason == "matched build.java_homes.java-8"


def test_resolve_auto_discovers_matching_jdk_before_incompatible_default(monkeypatch, tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("8", encoding="utf-8")
    discovered = tmp_path / "jdk-8"
    default_jdk = tmp_path / "jdk-17"
    discovered.mkdir()
    default_jdk.mkdir()
    monkeypatch.setattr("src.java_resolver.discover_java_home", lambda version: str(discovered) if version == "8" else "")

    selection = resolve_java_home(repo, repo, {"build": {"java_default": str(default_jdk)}})

    assert selection.java_home == str(discovered)
    assert selection.reason == "auto-discovered JDK 8"


def test_resolve_uses_jdk8_for_legacy_java7_project(monkeypatch, tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / "pom.xml").write_text(
        "<project><build><plugins><plugin><configuration><source>1.7</source><target>1.7</target></configuration></plugin></plugins></build></project>",
        encoding="utf-8",
    )
    jdk8 = tmp_path / "java-8"
    jdk17 = tmp_path / "java-17"
    jdk8.mkdir()
    jdk17.mkdir()
    monkeypatch.setattr("src.java_resolver.discover_java_home", lambda _version: "")

    selection = resolve_java_home(
        repo,
        repo,
        {
            "build": {
                "java_homes": {"java-8": str(jdk8)},
                "java_default": str(jdk17),
            }
        },
    )

    assert selection.requested_version == "7"
    assert selection.java_home == str(jdk8)
    assert selection.reason == "using compatible JDK 8 from build.java_homes.java-8 for Java 7"


def test_resolve_uses_java_default_when_project_version_is_not_detected(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    default_jdk = tmp_path / "jdk-21"
    default_jdk.mkdir()

    selection = resolve_java_home(repo, repo, {"build": {"java_default": str(default_jdk)}})

    assert selection.requested_version == "default"
    assert selection.java_home == str(default_jdk)
    assert selection.reason == "no project java version detected; using build.java_default"


def test_resolve_uses_configured_java_homes_map_before_java_default(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("17", encoding="utf-8")
    configured_jdk = tmp_path / "manual-jdks" / "jdk-17"
    configured_jdk.mkdir(parents=True)
    default_jdk = tmp_path / "jdk-21"
    default_jdk.mkdir()

    selection = resolve_java_home(
        repo,
        repo,
        {
            "build": {
                "java_homes": {"java-17": str(configured_jdk)},
                "java_default": str(default_jdk),
            }
        },
    )

    assert selection.java_home == str(configured_jdk)
    assert selection.reason == "matched build.java_homes.java-17"


def test_resolve_accepts_plain_version_key_in_java_homes(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("11", encoding="utf-8")
    configured_jdk = tmp_path / "jdk-11"
    configured_jdk.mkdir()

    selection = resolve_java_home(repo, repo, {"build": {"java_homes": {"11": str(configured_jdk)}}})

    assert selection.java_home == str(configured_jdk)
    assert selection.reason == "matched build.java_homes.11"


def test_resolve_manual_java_home_wins(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("17", encoding="utf-8")
    configured_jdk = tmp_path / "jdk-17"
    configured_jdk.mkdir()
    manual_jdk = tmp_path / "jdk-21"
    manual_jdk.mkdir()

    selection = resolve_java_home(
        repo,
        repo,
        {"build": {"java_homes": {"java-17": str(configured_jdk)}}},
        manual_java_home=str(manual_jdk),
    )

    assert selection.java_home == str(manual_jdk)
    assert selection.reason == "--java-home"


def test_resolve_normalizes_legacy_java_8_folder_name(tmp_path):
    repo = tmp_path / "repo"
    repo.mkdir()
    (repo / ".java-version").write_text("1.8", encoding="utf-8")
    configured_jdk = tmp_path / "jdk1.8.0_202"
    configured_jdk.mkdir()

    selection = resolve_java_home(repo, repo, {"build": {"java_homes": {"java-8": str(configured_jdk)}}})

    assert selection.requested_version == "8"
    assert selection.java_home == str(configured_jdk)
