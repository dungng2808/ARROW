namespace Arrow.Core.Prepare;

/// <summary>
/// <c>task.json</c>: bản ghi manifest rút gọn + kế hoạch build + JDK + lịch sử git, do S0 ghi và các bước sau đọc lại
/// để dựng đúng lệnh build mà không phải dò lại (specs/02).
/// </summary>
public sealed record TaskInfo
{
    public string Dataset { get; init; } = "";
    public string TaskId { get; init; } = "";
    public string RepoUrl { get; init; } = "";
    public string CheckoutSha { get; init; } = "";
    public string ClassFqn { get; init; } = "";
    public string ClassPath { get; init; } = "";
    public string TestClassPath { get; init; } = "";
    public string ManifestModulePath { get; init; } = ".";
    public string BuildTool { get; init; } = "";
    public string TestingFramework { get; init; } = "";
    public string ManifestJavaVersion { get; init; } = "";

    public BuildPlan Plan { get; init; } = new();
    public int JdkMajor { get; init; }
    public string JdkHome { get; init; } = "";

    /// <summary><c>manifest</c> | <c>release_flag</c>.</summary>
    public string JdkReason { get; init; } = "";
    /// <summary>Các JDK đã thử ở baseline, theo thứ tự; JDK cuối là JDK được chọn.</summary>
    public List<JdkAttempt> JdkAttempts { get; init; } = [];
    public string BuildToolVersion { get; init; } = "unknown";

    /// <summary>Task của module Gradle (đọc một lần, specs/02 §4.3); null với Maven.</summary>
    public List<string>? GradleTasks { get; init; }

    /// <summary><c>shallow</c> (<c>--depth 1</c>) | <c>full</c> (đã <c>--unshallow</c> vì build cần lịch sử git).</summary>
    public string RepoHistory { get; init; } = "shallow";

    /// <summary>Tên class test sinh ra (D1) và đường dẫn ghi file, tương đối so với gốc repository.</summary>
    public string GeneratedTestClassFqn { get; init; } = "";
    public string GeneratedTestPath { get; init; } = "";

    public List<StepRecord> Steps { get; init; } = [];
    public DateTimeOffset PreparedAt { get; init; }
}

/// <summary>Một lần thử JDK ở baseline: <c>Result</c> là <c>OK</c> hoặc mã lỗi (<c>BASELINE_BUILD_FAILED</c>, <c>BASELINE_TEST_RUN_FAILED</c>).</summary>
public sealed record JdkAttempt(int Major, string Reason, string Result);

/// <summary>Một lệnh đã chạy: để đối chiếu thời gian và log.</summary>
public sealed record StepRecord
{
    public string Step { get; init; } = "";
    public string Log { get; init; } = "";
    public int? ExitCode { get; init; }
    public bool TimedOut { get; init; }
    public double Seconds { get; init; }
}
