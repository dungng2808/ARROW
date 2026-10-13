using Arrow.Models.Common;

namespace Arrow.Models.Block4;

/// <summary><c>jacoco.json</c> (spec 04 Mục 4.2). Tỷ lệ <c>null</c> khi mẫu số bằng 0.</summary>
public sealed record CoverageResult
{
    public double? Line { get; init; }
    public double? Branch { get; init; }
    public double? Method { get; init; }
    public Dictionary<string, int> Counters { get; init; } = [];
}

/// <summary><c>pit.json</c> (spec 04 Mục 4.3).</summary>
public sealed record MutationResult
{
    public int Total { get; init; }
    public int Killed { get; init; }
    public int Survived { get; init; }
    public int NoCoverage { get; init; }
    public int TimedOut { get; init; }
    public int MemoryError { get; init; }
    public int RunError { get; init; }
    public double? Score { get; init; }
    public double? TestStrength { get; init; }
}

/// <summary><c>tsdetect.json</c> (spec 04 Mục 4.4).</summary>
public sealed record SmellResult
{
    public int TestMethods { get; init; }
    public Dictionary<string, int> Smells { get; init; } = [];
    public int TotalSmells { get; init; }
    public double? Density { get; init; }
}

/// <summary><c>block4/&lt;suite&gt;/outcome.json</c>: kết quả từng công cụ.</summary>
public sealed record QualityOutcome
{
    public string SuiteId { get; init; } = "";
    public Dictionary<string, BlockOutcome> Tools { get; init; } = [];
}
