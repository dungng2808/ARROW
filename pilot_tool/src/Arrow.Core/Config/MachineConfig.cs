using System.Text.Json.Serialization;
using Arrow.Core.Util;

namespace Arrow.Core.Config;

/// <summary>Cấu hình riêng của máy, không commit (specs/01 §2). Không chứa khóa API.</summary>
public sealed record MachineConfig
{
    public int SchemaVersion { get; init; } = 1;
    public string MachineName { get; init; } = "";
    public string Os { get; init; } = "";
    public string ArrowRoot { get; init; } = "";
    public string Experiment { get; init; } = "";
    public string Shard { get; init; } = "";
    public string WorkDir { get; init; } = "";
    public string RunsDir { get; init; } = "runs";
    public RepoPolicy RepoPolicy { get; init; } = RepoPolicy.CommitOnlyDeleteAfter;
    public bool KeepFailedWorkspaces { get; init; }
    public Dictionary<string, string> JdkHomes { get; init; } = [];
    public string MavenPath { get; init; } = "";
    public string GradlePath { get; init; } = "";
    public string GitPath { get; init; } = "";
    public LlmMachineConfig Llm { get; init; } = new();
    public WorkersConfig Workers { get; init; } = new();
    public DoctorStamp? LastDoctor { get; init; }

    public const string FileName = "machine.local.json";

    public static string DefaultPath(string pilotToolDir) => System.IO.Path.Combine(pilotToolDir, FileName);

    public static MachineConfig? TryLoad(string path) => File.Exists(path) ? Json.Read<MachineConfig>(path) : null;

    public void Save(string path) => Json.WriteAtomic(path, this);
}

[JsonConverter(typeof(LowerSnakeEnumConverter<RepoPolicy>))]
public enum RepoPolicy
{
    CommitOnlyDeleteAfter,
    CommitOnlyKeep,
    FullHistoryKeep,
}

public sealed record LlmMachineConfig
{
    /// <summary><c>auto_start</c>: tool tự bật LiteLLM proxy; <c>external</c>: dùng proxy đang chạy.</summary>
    public string ProxyMode { get; init; } = "auto_start";
    public string ProxyUrl { get; init; } = "http://127.0.0.1:4000";
    public string ProxyConfigFile { get; init; } = "litellm.config.local.yaml";
    public string KeyAlias { get; init; } = "";
    public int RpmLimit { get; init; } = 60;
}

public sealed record WorkersConfig
{
    public int Build { get; init; } = 2;
    public int Llm { get; init; } = 2;
}

public sealed record DoctorStamp
{
    public DateTimeOffset At { get; init; }
    public string Status { get; init; } = "";
}
