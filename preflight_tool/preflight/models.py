from __future__ import annotations

from dataclasses import asdict, dataclass, field
from enum import StrEnum
from typing import Any


class RunMode(StrEnum):
    STRICT = "STRICT"
    DISCOVERY_ONLY = "DISCOVERY_ONLY"


class RevisionStatus(StrEnum):
    UPSTREAM_PINNED = "UPSTREAM_PINNED"
    CONTENT_MATCHED = "CONTENT_MATCHED"
    UNVERIFIED = "UNVERIFIED"


class PreflightStatus(StrEnum):
    ELIGIBLE = "ELIGIBLE"
    PRECHECKED = "PRECHECKED"
    NEEDS_REVIEW = "NEEDS_REVIEW"
    CLONE_FAILED = "CLONE_FAILED"
    COMMIT_MISSING = "COMMIT_MISSING"
    CHECKOUT_FAILED = "CHECKOUT_FAILED"
    BUILD_TOOL_UNSUPPORTED = "BUILD_TOOL_UNSUPPORTED"
    JDK_UNSUPPORTED = "JDK_UNSUPPORTED"
    MAIN_BUILD_FAILED = "MAIN_BUILD_FAILED"
    TEST_COMPILE_FAILED = "TEST_COMPILE_FAILED"
    BUILD_TIMEOUT = "BUILD_TIMEOUT"
    DEPENDENCY_UNAVAILABLE = "DEPENDENCY_UNAVAILABLE"
    SOURCE_INVALID = "SOURCE_INVALID"
    PROBE_FAILED = "PROBE_FAILED"
    EXCLUDED = "EXCLUDED"
    INTEGRATION_ONLY = "INTEGRATION_ONLY"


@dataclass(frozen=True)
class ClassCandidate:
    task_id: str
    project_id: str
    repo_url: str
    class_path: str
    class_fqn: str
    class_name: str
    source_json_paths: tuple[str, ...]
    source_json_sha256s: tuple[str, ...]
    test_class_paths: tuple[str, ...]

    @property
    def source_json_path(self) -> str:
        return self.source_json_paths[0]

    @property
    def source_json_sha256(self) -> str:
        return self.source_json_sha256s[0]


@dataclass
class BuildAttempt:
    stage: str
    working_directory: str
    command: list[str]
    exit_code: int | None
    timed_out: bool
    duration_seconds: float
    log_path: str

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


@dataclass
class PreflightResult:
    task_id: str
    source_json_path: str
    source_json_sha256: str
    source_json_paths: list[str]
    source_json_sha256s: list[str]
    repo_url: str
    checkout_sha: str = ""
    revision_provenance: str = ""
    revision_verification_status: str = RevisionStatus.UNVERIFIED
    run_mode: str = RunMode.DISCOVERY_ONLY
    class_path: str = ""
    class_fqn: str = ""
    test_class_path: str = ""
    module_path: str = ""
    working_directory: str = ""
    build_tool: str = ""
    build_tool_version: str = ""
    java_version: str = ""
    testing_framework: str = "unknown"
    constructor_count: int = 0
    method_count: int = 0
    api_count: int = 0
    probed_api: str = ""
    probe_status: str = "NOT_RUN"
    preflight_status: str = PreflightStatus.SOURCE_INVALID
    technical_eligible: bool = False
    strict_eligible: bool = False
    tags: list[str] = field(default_factory=list)
    reason_codes: list[str] = field(default_factory=list)
    content_match: dict[str, Any] | None = None
    build_attempts: list[BuildAttempt] = field(default_factory=list)
    duration_seconds: float = 0.0
    log_paths: list[str] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        # dataclasses.asdict already recursively serializes BuildAttempt.
        return asdict(self)
