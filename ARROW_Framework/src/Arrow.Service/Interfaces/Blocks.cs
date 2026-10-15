using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Manifest;
using Arrow.Models.Run;

namespace Arrow.Service.Interfaces;

/// <summary>Công cụ trên máy: git, Maven, Gradle (null nếu không có) và JDK theo major.</summary>
public sealed record ToolEnvironment(string Git, string? Maven, string? Gradle, IReadOnlyDictionary<int, string> JdkHomes);

/// <summary>Những gì một khối cần biết về run đang chạy.</summary>
public sealed record RunContext
{
    public string RunId { get; init; } = "";
    public VerificationConfig Verification { get; init; } = new();
    public ScreeningConfig Screening { get; init; } = new();
    public string GeneratedTestSuffix { get; init; } = "_ArrowTest";
    public ToolEnvironment Tools { get; init; } = new("git", null, null, new Dictionary<int, string>());

    /// <summary>Giữ <c>base</c> và <c>snapshot</c> sau khối 1 (khối 3–4 cần). Lệnh <c>screen</c> tắt để không tốn đĩa.</summary>
    public bool KeepWorkspace { get; init; } = true;

    /// <summary>Thời gian chờ trước lần thử lại thứ 2, 3… khi lỗi mạng (spec 01 Mục 3: 10 s, 30 s).</summary>
    public IReadOnlyList<TimeSpan> RetryDelays { get; init; } = [TimeSpan.FromSeconds(10), TimeSpan.FromSeconds(30)];
}

/// <summary>Khối 1 (spec 01 Mục 8): Flow 1.1–1.4 cho một task.</summary>
public interface IRepositoryAnalyzer
{
    /// <summary>
    /// Không ném lỗi cho task hỏng: trả <c>Outcome</c> = EXCLUDED/INFRA kèm mã. Lưu <c>block1/*</c> theo thứ tự của spec 01 Mục 6.4
    /// (tới <c>outcome.json</c>); dòng ledger do nơi gọi ghi sau cùng.
    /// </summary>
    Task<RepositoryAnalysis> AnalyzeAsync(TaskRecord task, RunContext run, CancellationToken cancellationToken);
}

/// <summary>Yêu cầu sàng lọc (spec 01 Mục 6.3, spec 06 S10).</summary>
public sealed record ScreeningRequest
{
    public string ManifestPath { get; init; } = "";

    /// <summary>Null → đoán theo tên file (<c>gitbug</c> → <c>gitbug-java</c>, còn lại <c>classes2test</c>).</summary>
    public string? Dataset { get; init; }
    public string RunId { get; init; } = "";

    /// <summary>Chỉ chạy các task này (rỗng = mọi task của manifest).</summary>
    public IReadOnlyList<string> TaskIds { get; init; } = [];

    /// <summary>Giá trị thô của <c>--tasks</c>: đường dẫn file (mỗi dòng một task_id, <c>#</c> là chú thích) hoặc danh sách cách nhau dấu phẩy. Service đọc file, UI không.</summary>
    public string? TaskSelection { get; init; }

    /// <summary>Số task chạy song song; null → <c>workers.build</c> của máy.</summary>
    public int? Workers { get; init; }
    public bool RerunInfra { get; init; }
    public bool KeepWorkspace { get; init; }
}

public sealed record ScreeningSummary(string RunId, string ManifestPath, IReadOnlyList<ScreeningRow> Rows, string CsvPath);

/// <summary>Chạy chỉ khối 1 cho mọi CUT của một manifest và ghi <c>reports/screening.csv</c>.</summary>
public interface IScreeningService
{
    Task<ScreeningSummary> RunAsync(ScreeningRequest request, IProgress<RunEvent> progress, CancellationToken cancellationToken);
}

/// <summary>Những gì khối 3 cần biết về task: kết quả khối 1 (M, baseline, workspace).</summary>
public sealed record TaskContext
{
    public string RunId { get; init; } = "";
    public TaskKey Key { get; init; } = new("", "");
    public RepositoryMetadata Metadata { get; init; } = new();
    public Baseline Baseline { get; init; } = new();
    public WorkspaceInfo Workspace { get; init; } = new();
}

/// <summary>Tùy chọn của một lần xác minh.</summary>
public sealed record VerifyOptions
{
    /// <summary>Thư mục kết quả của lần xác minh, tính từ thư mục task (ví dụ <c>block3/gpt-6-luna/zero_shot/adaptive/round_2</c>); log ghi vào <c>&lt;ResultDir&gt;/logs/</c>.</summary>
    public string ResultDir { get; init; } = "";
    public int StepTimeoutSeconds { get; init; } = 900;
    public int InfraAttempts { get; init; } = 3;

    /// <summary>Chờ giữa hai lần thử khi lỗi hạ tầng (spec 03 Mục 3.5: 30 s).</summary>
    public TimeSpan RetryDelay { get; init; } = TimeSpan.FromSeconds(30);

    /// <summary>Giữ bản sao workspace khi không VALID (để gỡ lỗi).</summary>
    public bool KeepFailedWorkspaces { get; init; }
}

/// <summary>Khối 3, Flow 3.1 (spec 03 Mục 3): xác minh 5 bước, dùng chung cho candidate ARROW và bộ EvoSuite.</summary>
public interface IVerifier
{
    Task<VerifyResult> VerifyAsync(SuiteFiles suite, TaskContext task, VerifyOptions options, CancellationToken cancellationToken);
}

/// <summary>Khối 3, Flow 3.3 (spec 03 Mục 5): chẩn đoán lỗi từ kết quả xác minh; code thuần.</summary>
public interface IFailureDiagnoser
{
    Diagnosis Diagnose(VerifyResult result, CandidateTest candidate, RepositoryMetadata metadata);
}

/// <summary>Một chain sửa: c0 và kết quả xác minh c0, cơ chế, N, cùng ngữ cảnh gọi model và xác minh.</summary>
public sealed record ChainRequest
{
    public TaskContext Task { get; init; } = new();

    /// <summary>Ngữ cảnh của khối 2 (model, quy trình, mã CUT); <c>ResultDir</c> do chain đặt cho từng vòng.</summary>
    public GenerationContext Generation { get; init; } = new();
    public CandidateTest C0 { get; init; } = new();
    public VerifyResult C0Verify { get; init; } = new();

    /// <summary><c>fixed</c> hoặc <c>adaptive</c>.</summary>
    public string Mechanism { get; init; } = "";
    public int N { get; init; } = 5;
    public int? EarlyStopP { get; init; }
    public VerifyOptions Verify { get; init; } = new();

    /// <summary><c>first</c> hoặc <c>second</c>: chain này chạy trước hay sau chain của cơ chế kia (AR Mục 8).</summary>
    public string RunOrder { get; init; } = "";

    /// <summary>Chain Adaptive ngoài tập con RQ2, chỉ để RQ3 có bộ đại diện.</summary>
    public bool Rq3Only { get; init; }

    /// <summary>Gọi sau mỗi vòng (để màn hình theo dõi in kết quả từng vòng).</summary>
    public Action<RoundDecision>? OnRound { get; init; }
}

/// <summary>Kết quả chain: <c>Ok</c> (đã chốt, thành công hay không) hoặc <c>Infra</c> (dừng giữa chừng, chạy lại từ đầu chain).</summary>
public sealed record ChainResult(BlockOutcome Outcome, FinalTestSuite Final, string? FinalCode);

/// <summary>Khối 3, Flow 3.4–3.6 (spec 03 Mục 6–8): vòng lặp sửa gọi khối 2, xác minh, policy; ghi từng vòng và <c>final.json</c>.</summary>
public interface IRepairRunner
{
    Task<ChainResult> RunChainAsync(ChainRequest request, CancellationToken cancellationToken);
}
