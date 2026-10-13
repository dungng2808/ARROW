using System.Text.Json.Serialization;
using Arrow.Models.Common;
using Arrow.Models.Config;

namespace Arrow.Models.Run;

/// <summary>Một dòng <c>ledger.jsonl</c>: ghi **cuối cùng** khi một job xong (spec 00 Mục 5.3).</summary>
public sealed record LedgerEntry
{
    public DateTimeOffset At { get; init; }
    public int Block { get; init; }
    public string Dataset { get; init; } = "";
    public string TaskId { get; init; } = "";

    /// <summary>Định danh job trong task, ví dụ <c>analyze</c>, <c>generate/gpt-6-luna/zero_shot</c>, <c>chain/gpt-6-luna/zero_shot/adaptive</c>.</summary>
    public string Job { get; init; } = "";
    public OutcomeKind Outcome { get; init; }
    public string? Code { get; init; }
    public string? Message { get; init; }
    public bool NeedsRerun { get; init; }
}

[JsonConverter(typeof(UpperSnakeEnumConverter<EventLevel>))]
public enum EventLevel
{
    Info,
    Warn,
    Error,
}

/// <summary>Một dòng <c>events.jsonl</c>: sự kiện đáng chú ý để UI in và để chẩn đoán.</summary>
public sealed record RunEvent
{
    public DateTimeOffset At { get; init; }
    public EventLevel Level { get; init; } = EventLevel.Info;
    public int Block { get; init; }
    public string? TaskId { get; init; }
    public string? Job { get; init; }
    public string Message { get; init; } = "";
}

[JsonConverter(typeof(UpperSnakeEnumConverter<RunState>))]
public enum RunState
{
    Running,
    Paused,
    Stopping,
    Stopped,
    Done,
    Failed,
}

/// <summary><c>status.json</c>: ghi mỗi 2 giây (heartbeat) để màn hình ở terminal khác đọc.</summary>
public sealed record RunStatus
{
    public string RunId { get; init; } = "";
    public RunState State { get; init; }
    public DateTimeOffset StartedAt { get; init; }
    public DateTimeOffset UpdatedAt { get; init; }
    public List<BlockProgress> Blocks { get; init; } = [];
    public List<string> Running { get; init; } = [];
    public Dictionary<string, int> C0States { get; init; } = [];
    public int LlmCalls { get; init; }
    public double CostUsd { get; init; }
}

public sealed record BlockProgress
{
    public int Block { get; init; }
    public int Done { get; init; }
    public int Total { get; init; }
    public int Infra { get; init; }
    public int Excluded { get; init; }
}

[JsonConverter(typeof(LowerSnakeEnumConverter<ControlAction>))]
public enum ControlAction
{
    Pause,
    Resume,
    Stop,
}

/// <summary><c>control.json</c>: lệnh điều khiển gửi từ terminal khác (<c>arrow-fw control</c>).</summary>
public sealed record ControlCommand
{
    public ControlAction Action { get; init; }
    public DateTimeOffset At { get; init; }
    public string Source { get; init; } = "";
}

/// <summary>Thông tin tóm tắt một run trong thư mục <c>runs/</c>.</summary>
public sealed record RunInfo
{
    public string RunId { get; init; } = "";
    public RunLock? Lock { get; init; }
    public RunStatus? Status { get; init; }
}

/// <summary>Một file của <c>reports/</c>.</summary>
public sealed record CsvTable(IReadOnlyList<string> Headers, IReadOnlyList<IReadOnlyList<string>> Rows);

/// <summary>Một dòng <c>reports/screening.csv</c> (spec 01 Mục 6.3): kết quả khối 1 và tiêu chí B của một CUT.</summary>
public sealed record ScreeningRow
{
    public string Dataset { get; init; } = "";
    public string TaskId { get; init; } = "";
    public OutcomeKind Outcome { get; init; }
    public string? Code { get; init; }

    /// <summary><c>PASS</c>, <c>NO_TEST_RUN</c>, <c>FAILING</c>, <c>FLAKY</c>, <c>NO_PAIRED_TEST</c>; rỗng khi khối 1 không tới bước baseline.</summary>
    public string PairedTestStatus { get; init; } = "";
    public List<int> TestsRun { get; init; } = [];
    public int? JdkMajor { get; init; }
    public string? Message { get; init; }
    public bool NeedsRerun { get; init; }
}
