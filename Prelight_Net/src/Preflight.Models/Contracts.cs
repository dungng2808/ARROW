using System.Text.Json;
using System.Text.Json.Serialization;

namespace Preflight.Models;

/// <summary>Port của models.py: các giá trị enum giữ nguyên để tương thích manifest.</summary>
public enum RunMode { STRICT, DISCOVERY_ONLY }
public enum RevisionStatus { UPSTREAM_PINNED, CONTENT_MATCHED, UNVERIFIED }
public enum PreflightStatus
{
    ELIGIBLE, PRECHECKED, NEEDS_REVIEW, CLONE_FAILED, COMMIT_MISSING, CHECKOUT_FAILED,
    BUILD_TOOL_UNSUPPORTED, JDK_UNSUPPORTED, MAIN_BUILD_FAILED, TEST_COMPILE_FAILED,
    BUILD_TIMEOUT, DEPENDENCY_UNAVAILABLE, SOURCE_INVALID, PROBE_FAILED, EXCLUDED, INTEGRATION_ONLY
}
public enum ProbeProfile { v1, v2 }

/// <summary>Port của models.ClassCandidate.</summary>
public sealed record ClassCandidate(string TaskId, string ProjectId, string RepoUrl, string ClassPath,
    string ClassFqn, string ClassName, IReadOnlyList<string> SourceJsonPaths,
    IReadOnlyList<string> SourceJsonSha256s, IReadOnlyList<string> TestClassPaths)
{
    public string SourceJsonPath => SourceJsonPaths[0];
    public string SourceJsonSha256 => SourceJsonSha256s[0];
}
/// <summary>Port của models.BuildAttempt.</summary>
public sealed record BuildAttempt(string Stage, string WorkingDirectory, IReadOnlyList<string> Command,
    int? ExitCode, bool TimedOut, double DurationSeconds, string LogPath);

/// <summary>Port của models.PreflightResult, kể cả giá trị mặc định.</summary>
public sealed record PreflightResult
{
    public required string TaskId { get; set; }
    public required string SourceJsonPath { get; set; }
    public required string SourceJsonSha256 { get; set; }
    public required IReadOnlyList<string> SourceJsonPaths { get; set; }
    public required IReadOnlyList<string> SourceJsonSha256s { get; set; }
    public required string RepoUrl { get; set; }
    public string CheckoutSha { get; set; } = "";
    public string RevisionProvenance { get; set; } = "";
    public RevisionStatus RevisionVerificationStatus { get; set; } = RevisionStatus.UNVERIFIED;
    public RunMode RunMode { get; set; } = RunMode.DISCOVERY_ONLY;
    public string ClassPath { get; set; } = "";
    public string ClassFqn { get; set; } = "";
    public string TestClassPath { get; set; } = "";
    public string ModulePath { get; set; } = "";
    public string WorkingDirectory { get; set; } = "";
    public string BuildTool { get; set; } = "";
    public string BuildToolVersion { get; set; } = "";
    public string JavaVersion { get; set; } = "";
    public string TestingFramework { get; set; } = "unknown";
    public int ConstructorCount { get; set; }
    public int MethodCount { get; set; }
    public int ApiCount { get; set; }
    public string ProbedApi { get; set; } = "";
    public string ProbeStatus { get; set; } = "NOT_RUN";
    public PreflightStatus PreflightStatus { get; set; } = PreflightStatus.SOURCE_INVALID;
    public bool TechnicalEligible { get; set; }
    public bool StrictEligible { get; set; }
    public IReadOnlyList<string> Tags { get; set; } = [];
    public IReadOnlyList<string> ReasonCodes { get; set; } = [];
    public JsonElement? ContentMatch { get; set; }
    public IReadOnlyList<BuildAttempt> BuildAttempts { get; set; } = [];
    public double DurationSeconds { get; set; }
    public IReadOnlyList<string> LogPaths { get; set; } = [];
    public string Dataset { get; set; } = "";
    public string PairingMethod { get; set; } = "";
    public PairingEvidence? PairingEvidence { get; set; }
    public string RevisionEvidenceRef { get; set; } = "";
    public string ProbeProfile { get; set; } = "v1";
    public string BuildToolVersionDetail { get; set; } = "";
    public IReadOnlyList<string> TestClassPaths { get; set; } = [];
    public Dictionary<string, string> Extra { get; set; } = new(StringComparer.Ordinal);
}
/// <summary>Port của revision.RevisionChoice.</summary>
public sealed record RevisionChoice(string CheckoutSha, string Provenance, RevisionStatus Status,
    JsonElement? ContentMatch = null, string EvidenceRef = "");

/// <summary>Port của java_ast.JavaApi; chưa có parser trong P0/P1.</summary>
public sealed record JavaApi(string Kind, string Name, string Parameters, bool IsStatic, string Signature);
/// <summary>Port của java_ast.JavaClass; dùng dữ liệu giả hoặc golden trước P2.</summary>
public sealed record JavaClass
{
    public string Package { get; init; } = "";
    public string Name { get; init; } = "";
    public string Kind { get; init; } = "class";
    public HashSet<string> Modifiers { get; init; } = new(StringComparer.Ordinal);
    public HashSet<string> Annotations { get; init; } = new(StringComparer.Ordinal);
    public IReadOnlyList<JavaApi> Apis { get; init; } = [];
    public IReadOnlyList<string> Imports { get; init; } = [];
    public string Extends { get; init; } = "";
    public string SourceSet { get; init; } = "unknown";
    public HashSet<string> Tags { get; init; } = new(StringComparer.Ordinal);
    public bool IsTopLevel { get; init; } = true;
    [JsonIgnore] public string Fqn => Package.Length > 0 ? $"{Package}.{Name}" : Name;
}
