using Arrow.Models.Common;

namespace Arrow.Models.Block3;

/// <summary>Một bộ test cần xác minh hoặc đo (spec 03 Mục 3.0): candidate của ARROW hoặc bộ EvoSuite.</summary>
public sealed record SuiteFiles
{
    public string SuiteId { get; init; } = "";
    public string TestFqn { get; init; } = "";

    /// <summary>Đường dẫn tính từ gốc repository → nội dung file.</summary>
    public Dictionary<string, string> Files { get; init; } = [];
    public List<string> ExtraTestClasspath { get; init; } = [];
}

public sealed record StepRecord
{
    public string Step { get; init; } = "";
    public bool Ok { get; init; }
    public string? Log { get; init; }
    public double Seconds { get; init; }
    public int Attempts { get; init; } = 1;
    public bool TimedOut { get; init; }
}

public sealed record TestMethodCounts
{
    public int Total { get; init; }
    public int Passed { get; init; }
    public int Failed { get; init; }
}

public sealed record FailureInfo
{
    public string Method { get; init; } = "";
    public string? Type { get; init; }
    public string? Message { get; init; }
    public string? TraceHead { get; init; }
}

public sealed record CompileError
{
    public string File { get; init; } = "";
    public int Line { get; init; }
    public int? Column { get; init; }
    public string Message { get; init; } = "";
    public string Kind { get; init; } = "";
    public string? SymbolKind { get; init; }
    public string? Symbol { get; init; }
    public string? Location { get; init; }
}

public sealed record RegressionInfo
{
    public string TestId { get; init; } = "";
    public string Type { get; init; } = "";
    public string? Message { get; init; }
}

/// <summary><c>verify.json</c> (spec 03 Mục 3.7).</summary>
public sealed record VerifyResult
{
    public VerificationState State { get; init; }
    public ErrorGroup? Group { get; init; }
    public string? InfraReason { get; init; }
    public bool NeedsRerun { get; init; }
    public List<StepRecord> Steps { get; init; } = [];
    public TestMethodCounts TestMethods { get; init; } = new();
    public List<FailureInfo> Failures { get; init; } = [];
    public List<CompileError> CompileErrors { get; init; } = [];
    public List<RegressionInfo> Regressions { get; init; } = [];
    public string? ErrorSignature { get; init; }
    public List<string> FailingMethods { get; init; } = [];
    public string? CodeHash { get; init; }
    public int AssertionCount { get; init; }
    public double BuildSeconds { get; init; }
}

/// <summary>Chẩn đoán dùng cho quyết định và prompt sửa (spec 03 Mục 5).</summary>
public sealed record Diagnosis
{
    public VerificationState State { get; init; }
    public ErrorGroup? Group { get; init; }
    public string? ErrorSignature { get; init; }
    public string? CodeHash { get; init; }
    public List<string> FailingMethods { get; init; } = [];
    public string ErrorBlock { get; init; } = "";
    public string? FirstError { get; init; }
}

/// <summary>Một phần tử của danh sách "đã thử và thất bại" (spec 03 Mục 6.4).</summary>
public sealed record Attempt
{
    public int Round { get; init; }
    public string Signature { get; init; } = "";
    public string? FirstError { get; init; }
    public string? Change { get; init; }
}

/// <summary>Yêu cầu sửa khối 3 gửi khối 2 (spec 02 Mục 1.2).</summary>
public sealed record RepairRequest
{
    public string TaskId { get; init; } = "";
    public string Model { get; init; } = "";
    public string Strategy { get; init; } = "";
    public string Mechanism { get; init; } = "";
    public int Round { get; init; }
    public int RequestedLevel { get; init; }
    public int Level { get; init; }
    public VerificationState State { get; init; }
    public string BaseCode { get; init; } = "";
    public int BaseRound { get; init; }
    public string ErrorBlock { get; init; } = "";
    public string ExtraContext { get; init; } = "";
    public List<Attempt> FailedAttempts { get; init; } = [];
    public List<string> RewriteMethods { get; init; } = [];
}

/// <summary><c>decision.json</c> mỗi vòng (spec 03 Mục 6.7).</summary>
public sealed record RoundDecision
{
    public int Round { get; init; }
    public string Mechanism { get; init; } = "";
    public int BaseRound { get; init; }
    public int RequestedLevel { get; init; }
    public int Level { get; init; }
    public string Template { get; init; } = "";
    public VerificationState? State { get; init; }
    public string? ErrorSignature { get; init; }
    public string? CodeHash { get; init; }
    public bool DuplicateCode { get; init; }
    public bool InvalidOutput { get; init; }
    public string? Comparison { get; init; }
    public bool Rollback { get; init; }
    public bool RepeatedSignature { get; init; }
    public bool Improved { get; init; }
    public int LevelAfter { get; init; }
    public int CheckpointRound { get; init; }
    public int TestsPassed { get; init; }
    public int AssertionCount { get; init; }
    public List<string> DroppedMethods { get; init; } = [];
    public double LlmSeconds { get; init; }
    public double BuildSeconds { get; init; }
    public long LlmTokensIn { get; init; }
    public long LlmTokensOut { get; init; }
    public int LlmRetries { get; init; }
    public bool Replayed { get; init; }
}

public sealed record RepairCounters
{
    public int Rollback { get; init; }
    public int DuplicateCode { get; init; }
    public int InvalidOutput { get; init; }
    public int RepeatedSignature { get; init; }
    public int LevelUp { get; init; }
    public int StateChange { get; init; }
    public int Level3Rounds { get; init; }
}

/// <summary><c>final.json</c> của một chain (spec 03 Mục 8).</summary>
public sealed record FinalTestSuite
{
    public string TaskId { get; init; } = "";
    public string Model { get; init; } = "";
    public string Strategy { get; init; } = "";
    public string Mechanism { get; init; } = "";
    public VerificationState C0State { get; init; }
    public bool Success { get; init; }
    public int SuccessRound { get; init; }
    public int RoundsRun { get; init; }
    public string StopReason { get; init; } = "";
    public VerificationState FinalState { get; init; }
    public int FinalRound { get; init; }
    public double RepairSeconds { get; init; }
    public double LlmSeconds { get; init; }
    public double BuildSeconds { get; init; }
    public long TokensIn { get; init; }
    public long TokensOut { get; init; }
    public double CostUsd { get; init; }
    public RepairCounters Counters { get; init; } = new();
    public List<string> DroppedMethods { get; init; } = [];
    public string RunOrder { get; init; } = "";
}
