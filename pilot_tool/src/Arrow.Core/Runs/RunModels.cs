using System.Text.Json.Serialization;
using Arrow.Core.Util;

namespace Arrow.Core.Runs;

/// <summary>Khóa duy nhất của một job trong ledger (specs/01 §4.2).</summary>
public sealed record JobKey
{
    public string Dataset { get; init; } = "";
    public string TaskId { get; init; } = "";
    public string Stage { get; init; } = "";
    public string? Model { get; init; }
    public string? Strategy { get; init; }
    public string? Mechanism { get; init; }
    public int Round { get; init; }

    /// <summary>Chuỗi ổn định để so sánh và băm.</summary>
    public string Canonical =>
        string.Join('|', Dataset, TaskId, Stage, Model ?? "-", Strategy ?? "-", Mechanism ?? "-", Round.ToString(System.Globalization.CultureInfo.InvariantCulture));

    public string ShortHash => Util.Hashing.Sha256Text(Canonical)[..12];
}

[JsonConverter(typeof(UpperSnakeEnumConverter<JobStatus>))]
public enum JobStatus
{
    Done,
    Infra,
    Skipped,
}

public sealed record LedgerEntry
{
    public JobKey? Job { get; init; }
    public JobStatus? Status { get; init; }
    public string? Outcome { get; init; }
    public string? Reason { get; init; }
    public DateTimeOffset? StartedAt { get; init; }
    public DateTimeOffset? EndedAt { get; init; }
    public double? LlmSeconds { get; init; }
    public double? BuildSeconds { get; init; }
    public int Attempt { get; init; } = 1;
    public List<string> Outputs { get; init; } = [];

    /// <summary>Dòng ghi một khoảng tạm dừng; khi có thì các trường job để trống.</summary>
    public PauseInterval? Pause { get; init; }
}

public sealed record PauseInterval(DateTimeOffset From, DateTimeOffset To)
{
    public double Seconds => (To - From).TotalSeconds;
}

[JsonConverter(typeof(LowerSnakeEnumConverter<ControlCommand>))]
public enum ControlCommand
{
    Pause,
    Resume,
    Stop,
}

public sealed record ControlFile
{
    public ControlCommand Command { get; init; }
    public DateTimeOffset RequestedAt { get; init; }

    /// <summary><c>hotkey</c> | <c>cli</c> | <c>menu</c>.</summary>
    public string RequestedBy { get; init; } = "cli";
}

[JsonConverter(typeof(UpperSnakeEnumConverter<RunState>))]
public enum RunState
{
    Running,
    Pausing,
    Paused,
    Stopping,
    Stopped,
    Done,
    Failed,
}

public sealed record RunningJob
{
    public string Worker { get; init; } = "";
    public string TaskId { get; init; } = "";
    public string Label { get; init; } = "";
    public string Step { get; init; } = "";
    public DateTimeOffset Since { get; init; }
}

public sealed record StageProgress
{
    public string Stage { get; init; } = "";
    public int Done { get; init; }
    public int Total { get; init; }
    public int Infra { get; init; }
}

public sealed record RunStatus
{
    public RunState State { get; init; }
    public string Stage { get; init; } = "";
    public DateTimeOffset Since { get; init; }
    public List<RunningJob> RunningJobs { get; init; } = [];
    public List<StageProgress> Stages { get; init; } = [];
    public Dictionary<string, int> Outcomes { get; init; } = [];
    public double ElapsedSeconds { get; init; }
    public double PausedSeconds { get; init; }
    public long PromptTokens { get; init; }
    public long CompletionTokens { get; init; }
    public double CostUsd { get; init; }
    public int Pid { get; init; }
    public string Host { get; init; } = "";
    public DateTimeOffset HeartbeatAt { get; init; }

    /// <summary>Heartbeat cũ hơn 30 giây mà vẫn RUNNING thì coi là không còn chạy (specs/01 §4.3).</summary>
    public bool IsAlive(DateTimeOffset now) =>
        State is RunState.Running or RunState.Pausing or RunState.Paused or RunState.Stopping
        && now - HeartbeatAt < TimeSpan.FromSeconds(30);
}

public sealed record RunEvent
{
    public DateTimeOffset At { get; init; }
    public string Level { get; init; } = "info";
    public string Stage { get; init; } = "";
    public string? TaskId { get; init; }
    public string? Strategy { get; init; }
    public string? Outcome { get; init; }
    public string Message { get; init; } = "";
    public double? LlmSeconds { get; init; }
    public double? BuildSeconds { get; init; }
}
