namespace Arrow.Models.Manifest;

/// <summary>Một dòng manifest (bản ghi preflight). Chỉ khai báo các trường khối 1 dùng (spec 01 Mục 1.1); dòng gốc giữ ở <see cref="RawJson"/>.</summary>
public sealed record TaskRecord
{
    /// <summary>Dataset của manifest chứa bản ghi (lấy từ cấu hình thực nghiệm, không có trong dòng manifest).</summary>
    public string Dataset { get; init; } = "";
    public string TaskId { get; init; } = "";
    public string RepoUrl { get; init; } = "";
    public string CheckoutSha { get; init; } = "";
    public string ClassPath { get; init; } = "";
    public string ClassFqn { get; init; } = "";
    public string ModulePath { get; init; } = ".";
    public string WorkingDirectory { get; init; } = "";
    public List<BuildAttempt> BuildAttempts { get; init; } = [];
    public string? TestClassPath { get; init; }
    public string BuildTool { get; init; } = "";
    public string JavaVersion { get; init; } = "";
    public string TestingFramework { get; init; } = "";
    public string RevisionVerificationStatus { get; init; } = "";
    public string RevisionProvenance { get; init; } = "";

    /// <summary>Dòng JSON gốc, chép nguyên vào <c>block1/task.json</c>.</summary>
    public string RawJson { get; init; } = "";
}

/// <summary>Một lệnh preflight đã chạy (<c>build_attempts[]</c>).</summary>
public sealed record BuildAttempt
{
    public string Stage { get; init; } = "";
    public List<string> Command { get; init; } = [];
    public string WorkingDirectory { get; init; } = "";
    public int? ExitCode { get; init; }
    public bool? TimedOut { get; init; }
    public double? DurationSeconds { get; init; }
}
