using System.Text.Json.Serialization;
using Arrow.Core.Util;

namespace Arrow.Core.Verify;

/// <summary>Trạng thái theo chuỗi xác minh (specs/03 Mục 2); <see cref="Infra"/> nằm ngoài chuỗi.</summary>
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

[JsonConverter(typeof(UpperSnakeEnumConverter<StepStatus>))]
public enum StepStatus
{
    Pass,
    Fail,
    NotRun,
}

public static class States
{
    /// <summary>Thứ hạng trong chuỗi: COMPILE &lt; UNKNOWN &lt; DISCOVERY &lt; RUNTIME &lt; ASSERTION &lt; REGRESSION &lt; VALID.</summary>
    public static int Rank(VerificationState state) =>
        state == VerificationState.Infra ? throw new ArgumentException("INFRA nằm ngoài chuỗi xác minh") : (int)state;

    /// <summary>Nhóm lỗi (specs/03 Mục 5): BUILD, RUNTIME, ASSERTION; null cho VALID/INFRA.</summary>
    public static string? Group(VerificationState state) => state switch
    {
        VerificationState.Compile or VerificationState.Unknown or VerificationState.Discovery => "BUILD",
        VerificationState.Runtime or VerificationState.Regression => "RUNTIME",
        VerificationState.Assertion => "ASSERTION",
        _ => null,
    };

    public static string Name(VerificationState state) => state.ToString().ToUpperInvariant();
}

public sealed record VerifySteps
{
    public StepStatus Compile { get; init; } = StepStatus.NotRun;
    public StepStatus Discovery { get; init; } = StepStatus.NotRun;
    public StepStatus Execution { get; init; } = StepStatus.NotRun;
    public StepStatus TargetPass { get; init; } = StepStatus.NotRun;
    public StepStatus Regression { get; init; } = StepStatus.NotRun;
}

public sealed record TestMethodCounts
{
    public int Total { get; init; }
    public int Passed { get; init; }
    public int Failed { get; init; }
    public int Skipped { get; init; }
}

/// <summary>Một method test sinh ra bị fail ở bước 4.</summary>
public sealed record TestFailure
{
    public string Method { get; init; } = "";

    /// <summary><c>assertion</c> | <c>exception</c>.</summary>
    public string Kind { get; init; } = "";
    public string? Type { get; init; }
    public string? Message { get; init; }
    public List<string> TraceHead { get; init; } = [];
}

/// <summary>Lỗi javac chỉ vào file test sinh ra (specs/03 Mục 5).</summary>
public sealed record CompileError
{
    public string File { get; init; } = "";
    public int Line { get; init; }
    public int? Column { get; init; }
    public string Message { get; init; } = "";

    /// <summary>Loại lỗi javac đã quy về danh sách cố định (specs/03 Mục 6).</summary>
    public string Kind { get; init; } = "other";
    public string? Symbol { get; init; }

    /// <summary><c>method</c>, <c>variable</c>, <c>class</c>, <c>constructor</c>, <c>package</c> hoặc null.</summary>
    public string? SymbolKind { get; init; }
    public string? Location { get; init; }

    /// <summary>Method chứa dòng lỗi (java-analyzer); null nếu lỗi ngoài method.</summary>
    public string? Method { get; init; }
}

public sealed record NewFailure
{
    public string Id { get; init; } = "";
    public string? Type { get; init; }
    public string? Message { get; init; }
}

/// <summary><c>verify.json</c> (specs/03 Mục 5).</summary>
public sealed record VerifyResult
{
    public VerificationState State { get; init; }
    public string? InfraReason { get; init; }
    public bool InvalidOutput { get; init; }
    public string? InvalidReason { get; init; }
    public VerifySteps Steps { get; init; } = new();
    public TestMethodCounts TestMethods { get; init; } = new();
    public List<TestFailure> Failures { get; init; } = [];
    public List<CompileError> CompileErrors { get; init; } = [];
    public List<NewFailure> NewFailures { get; init; } = [];
    public bool? ModuleRunIncludesTarget { get; init; }
    public string? Group { get; init; }
    public string? ErrorSignature { get; init; }
    public int? AssertionCount { get; init; }
    public Dictionary<string, int> AssertionsPerMethod { get; init; } = [];
    public List<string> TestMethodNames { get; init; } = [];
    public string? CodeHash { get; init; }
    public Dictionary<string, double?> BuildSeconds { get; init; } = [];
    public List<string> Logs { get; init; } = [];

    /// <summary>Phần log dùng cho prompt sửa khi UNKNOWN (specs/04 Mục 6.1, 6.2 [D6]).</summary>
    public string? ErrorLog { get; init; }

    /// <summary>Số lần build lại vì lỗi hạ tầng (timeout, công cụ, tải dependency).</summary>
    public int Rebuilds { get; init; }

    [JsonIgnore]
    public double TotalBuildSeconds => BuildSeconds.Values.Sum(v => v ?? 0);

    /// <summary>Test method của class sinh ra đã fail (để chọn method lỗi ở mức 3).</summary>
    [JsonIgnore]
    public IEnumerable<string> FailingMethods =>
        State is VerificationState.Compile
            ? CompileErrors.Select(e => e.Method).Where(m => m is not null).Select(m => m!).Distinct()
            : Failures.Select(f => f.Method).Distinct();
}
