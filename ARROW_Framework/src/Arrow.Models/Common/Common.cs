using System.Text.Json;
using System.Text.Json.Serialization;

namespace Arrow.Models.Common;

/// <summary>Lỗi có mã (spec 00 Mục 6): dùng cho lỗi cấu hình hoặc lỗi làm dừng cả lệnh, không dùng cho kết quả của task.</summary>
public sealed class ArrowException(string code, string message, Exception? inner = null) : Exception(message, inner)
{
    public string Code { get; } = code;

    public override string ToString() => $"{Code}: {Message}";
}

/// <summary>Enum ghi ra JSON dạng chữ HOA có gạch dưới (COMPILE, NEEDS_RERUN…).</summary>
public sealed class UpperSnakeEnumConverter<TEnum>() : JsonStringEnumConverter<TEnum>(JsonNamingPolicy.SnakeCaseUpper)
    where TEnum : struct, Enum;

/// <summary>Enum ghi ra JSON dạng chữ thường có gạch dưới (reactor, standalone…).</summary>
public sealed class LowerSnakeEnumConverter<TEnum>() : JsonStringEnumConverter<TEnum>(JsonNamingPolicy.SnakeCaseLower)
    where TEnum : struct, Enum;

/// <summary>Trạng thái xác minh (spec 03). Thứ tự khai báo là thứ tự "đi xa hơn" của chuỗi; INFRA nằm ngoài chuỗi.</summary>
[JsonConverter(typeof(UpperSnakeEnumConverter<VerificationState>))]
public enum VerificationState
{
    Compile,
    Unknown,
    Discovery,
    Runtime,
    Assertion,
    Regression,
    Valid,
    Infra,
}

/// <summary>Ba nhóm lỗi để mô tả và báo cáo (không dùng để quyết định).</summary>
[JsonConverter(typeof(UpperSnakeEnumConverter<ErrorGroup>))]
public enum ErrorGroup
{
    Build,
    Runtime,
    Assertion,
}

/// <summary>Kết quả của một job: xong, bị loại, lỗi hạ tầng (chạy lại được), hoặc thất bại vì lý do khác.</summary>
[JsonConverter(typeof(UpperSnakeEnumConverter<OutcomeKind>))]
public enum OutcomeKind
{
    Ok,
    Excluded,
    Infra,
    Failed,
}

/// <summary>Kết quả có mã của một khối cho một task (ví dụ <c>block1/outcome.json</c>).</summary>
public sealed record BlockOutcome
{
    public OutcomeKind Outcome { get; init; }
    public string? Code { get; init; }
    public string? Message { get; init; }
    public string? Log { get; init; }
    public bool NeedsRerun { get; init; }

    /// <summary>Thời gian của việc (khối 4: từng công cụ đo), để báo cáo tài nguyên.</summary>
    public double? Seconds { get; init; }

    public static BlockOutcome Ok() => new() { Outcome = OutcomeKind.Ok };
}

/// <summary>Định danh một task: dataset + task_id của manifest.</summary>
public sealed record TaskKey(string Dataset, string TaskId)
{
    public override string ToString() => $"{Dataset}/{TaskId}";
}

/// <summary>Mã lỗi của mọi khối (spec 01–04). Tiền tố theo khối.</summary>
public static class ErrorCodes
{
    // Cấu hình, dữ liệu chung
    public const string ConfigMissing = "CONFIG_MISSING";
    public const string ConfigInvalid = "CONFIG_INVALID";
    public const string ManifestMismatch = "MANIFEST_MISMATCH";
    public const string RunConfigMismatch = "RUN_CONFIG_MISMATCH";
    public const string RunMissing = "RUN_MISSING";
    public const string PromptInvalid = "PROMPT_INVALID";
    public const string ModelExists = "MODEL_EXISTS";
    public const string ModelMissing = "MODEL_MISSING";
    public const string ModelRequired = "MODEL_REQUIRED";

    // Công cụ ngoài
    public const string JdkMissing = "JDK_MISSING";
    public const string MavenMissing = "MAVEN_MISSING";
    public const string GradleMissing = "GRADLE_MISSING";
    public const string GitMissing = "GIT_MISSING";
    public const string JavaHelperBuild = "JAVA_HELPER_BUILD";
    public const string JavaHelperTimeout = "JAVA_HELPER_TIMEOUT";
    public const string JavaHelperError = "JAVA_HELPER_ERROR";

    // Khối 1
    public const string B1ManifestInvalid = "B1_MANIFEST_INVALID";
    public const string B1CloneFailed = "B1_CLONE_FAILED";
    public const string B1CommitMissing = "B1_COMMIT_MISSING";
    public const string B1CutMissing = "B1_CUT_MISSING";
    public const string B1BuildRootNotFound = "B1_BUILD_ROOT_NOT_FOUND";
    public const string B1ModuleMismatch = "B1_MODULE_MISMATCH";
    public const string B1DependencyResolution = "B1_DEPENDENCY_RESOLUTION";
    public const string B1BaselineBuildFailed = "B1_BASELINE_BUILD_FAILED";
    public const string B1BaselineTimeout = "B1_BASELINE_TIMEOUT";
    public const string B1PairedTestNotPassing = "B1_PAIRED_TEST_NOT_PASSING";
    public const string B1Workspace = "B1_WORKSPACE";

    // Khối 2
    public const string B2InvalidOutput = "B2_INVALID_OUTPUT";
    public const string B2LlmUnavailable = "B2_LLM_UNAVAILABLE";
    public const string B2LlmConfig = "B2_LLM_CONFIG";
    public const string B2WrongModel = "B2_WRONG_MODEL";
    public const string B2CallLimit = "B2_CALL_LIMIT";

    // Khối 3
    public const string B3Timeout = "B3_TIMEOUT";
    public const string B3Dependency = "B3_DEPENDENCY";
    public const string B3ToolError = "B3_TOOL_ERROR";
    public const string B3Workspace = "B3_WORKSPACE";

    // Khối 4
    public const string B4EvoSuiteUnsupportedJdk = "B4_EVOSUITE_UNSUPPORTED_JDK";
    public const string B4EvoSuiteFailed = "B4_EVOSUITE_FAILED";
    public const string B4JacocoFailed = "B4_JACOCO_FAILED";
    public const string B4PitFailed = "B4_PIT_FAILED";
    public const string B4PitScope = "B4_PIT_SCOPE";
    public const string B4SuiteCompile = "B4_SUITE_COMPILE";
    public const string B4TsDetectFailed = "B4_TSDETECT_FAILED";
    public const string B4ReportMismatch = "B4_REPORT_MISMATCH";
}
