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

/// <summary>Một bộ test VALID cần đo: <c>suite_id</c>, hệ thống sinh ra nó và các file.</summary>
public sealed record SuiteToMeasure
{
    public string SuiteId { get; init; } = "";

    /// <summary><c>ARROW</c> hoặc <c>EvoSuite</c>.</summary>
    public string System { get; init; } = "ARROW";
    public Block3.SuiteFiles Files { get; init; } = new();
}

/// <summary>Kết quả một công cụ đo trên một bộ test.</summary>
public sealed record ToolResult
{
    public string Tool { get; init; } = "";
    public BlockOutcome Outcome { get; init; } = BlockOutcome.Ok();
    public CoverageResult? Coverage { get; init; }
    public MutationResult? Mutation { get; init; }
    public SmellResult? Smells { get; init; }
    public double Seconds { get; init; }
    public int Attempts { get; init; } = 1;
}

/// <summary>Kết quả đã đọc của một candidate c0 (khối 2 + xác minh c0).</summary>
public sealed record CandidateData
{
    public string Model { get; init; } = "";
    public string Strategy { get; init; } = "";
    public Block2.CandidateTest? Candidate { get; init; }
    public Block2.LlmUsage? Usage { get; init; }
    public Block3.VerifyResult? VerifyC0 { get; init; }
}

/// <summary>Một chain đã chốt kèm <c>decision.json</c> và <c>usage.json</c> từng vòng.</summary>
public sealed record ChainData
{
    public Block3.FinalTestSuite Final { get; init; } = new();
    public List<Block3.RoundDecision> Decisions { get; init; } = [];
    public List<Block2.LlmUsage> Usages { get; init; } = [];
}

/// <summary>Số đo của một bộ test đã đo ở khối 4.</summary>
public sealed record SuiteData
{
    public string SuiteId { get; init; } = "";
    public CoverageResult? Coverage { get; init; }
    public MutationResult? Mutation { get; init; }
    public SmellResult? Smells { get; init; }
    public QualityOutcome? Outcome { get; init; }
}

/// <summary>Mọi kết quả của một task trong một run.</summary>
public sealed record TaskData
{
    public TaskKey Key { get; init; } = new("", "");
    public BlockOutcome? Block1 { get; init; }
    public List<CandidateData> Candidates { get; init; } = [];
    public List<ChainData> Chains { get; init; } = [];
    public List<SuiteData> Suites { get; init; } = [];
    public ComparisonSuite? EvoSuite { get; init; }
    public Block3.VerifyResult? EvoSuiteVerify { get; init; }
}

/// <summary>Kết quả đã đọc của một run (đầu vào của báo cáo).</summary>
public sealed record RunData
{
    public string RunId { get; init; } = "";
    public Config.RunLock? Lock { get; init; }
    public List<Run.LedgerEntry> Ledger { get; init; } = [];
    public List<TaskData> Tasks { get; init; } = [];
}

/// <summary>Báo cáo đã dựng: các CSV và <c>report.md</c>.</summary>
public sealed record UnifiedReport
{
    public Dictionary<string, Run.CsvTable> Csv { get; init; } = [];
    public string Markdown { get; init; } = "";

    /// <summary>N chọn được theo quy tắc pilot (spec 04 Mục 6.3); null khi không phải pilot.</summary>
    public int? PilotN { get; init; }
}

/// <summary><c>block3/evosuite/comparison.json</c>: kết quả sinh bộ test của EvoSuite (spec 04 Mục 3).</summary>
public sealed record ComparisonSuite
{
    /// <summary><c>OK</c>, <c>UNSUPPORTED_JDK</c> hoặc <c>FAILED</c>.</summary>
    public string Status { get; init; } = "";
    public string? Code { get; init; }
    public string? Reason { get; init; }
    public Block3.SuiteFiles? Suite { get; init; }
    public double Seconds { get; init; }
}
