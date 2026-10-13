using Arrow.Core.Util;

namespace Arrow.Core.Runs;

/// <summary>
/// <c>run_config.lock.json</c> (specs/01 §4.1): ghi lần đầu khi tạo run; các lần chạy tiếp phải khớp các trường khóa,
/// lệch thì từ chối (<c>RUN_CONFIG_MISMATCH</c>). Không chứa khóa API.
/// </summary>
public sealed record RunConfigLock
{
    public string RunId { get; init; } = "";
    public DateTimeOffset CreatedAt { get; init; }
    public string ExperimentId { get; init; } = "";
    public string ExperimentSha256 { get; init; } = "";
    public string? ShardId { get; init; }
    public string? ShardSha256 { get; init; }
    public string ToolVersion { get; init; } = "";
    public string? ToolGitCommit { get; init; }
    public bool? ToolGitDirty { get; init; }
    public Dictionary<string, string> PromptHashes { get; init; } = [];
    public string MachineName { get; init; } = "";
    public string Os { get; init; } = "";
    public int CpuCount { get; init; }
    public double RamGb { get; init; }
    public Dictionary<string, string> JdkVersions { get; init; } = [];
    public string? MavenVersion { get; init; }
    public string? GradleVersion { get; init; }
    public string? GitVersion { get; init; }
    public string? LlmEndpointHost { get; init; }
    public string? LlmKeyAlias { get; init; }
    public Dictionary<string, List<string>> BuildFlags { get; init; } = [];
    public int NMax { get; init; }
    public string NRule { get; init; } = "";

    /// <summary>Khác biệt ở các trường khóa (đánh dấu * trong specs/01 §4.1).</summary>
    public List<string> Differences(RunConfigLock other)
    {
        var differences = new List<string>();
        void Check(string name, object? a, object? b)
        {
            if (!Equals(a, b))
            {
                differences.Add($"{name}: {a ?? "–"} → {b ?? "–"}");
            }
        }

        Check("experiment_sha256", ExperimentSha256, other.ExperimentSha256);
        Check("shard_sha256", ShardSha256, other.ShardSha256);
        Check("tool_version", ToolVersion, other.ToolVersion);
        Check("tool_git_commit", ToolGitCommit, other.ToolGitCommit);
        Check("prompt_hashes", Json.Serialize(PromptHashes.OrderBy(p => p.Key).ToList(), false), Json.Serialize(other.PromptHashes.OrderBy(p => p.Key).ToList(), false));
        Check("build_flags", Json.Serialize(BuildFlags.OrderBy(p => p.Key).ToList(), false), Json.Serialize(other.BuildFlags.OrderBy(p => p.Key).ToList(), false));
        Check("n_max", NMax, other.NMax);
        Check("n_rule", NRule, other.NRule);
        return differences;
    }

    /// <summary>Tạo mới nếu chưa có; có rồi thì so trường khóa và giữ bản cũ.</summary>
    public static RunConfigLock WriteOrVerify(RunStore store, RunConfigLock current)
    {
        if (!File.Exists(store.RunConfigLockPath))
        {
            Json.WriteAtomic(store.RunConfigLockPath, current);
            return current;
        }

        var existing = Json.Read<RunConfigLock>(store.RunConfigLockPath);
        var differences = existing.Differences(current);
        if (differences.Count > 0)
        {
            throw new ArrowException("RUN_CONFIG_MISMATCH",
                $"Run {store.RunId} được tạo với cấu hình khác: {string.Join("; ", differences)}. Dùng --run-id mới để chạy với cấu hình hiện tại.");
        }

        return existing;
    }
}
