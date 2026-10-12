using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Util;

namespace Arrow.Core.Shards;

/// <summary>Một máy trong phân công: mã ASCII dùng cho tên file, tên hiển thị có dấu.</summary>
public sealed record ShardOwner(string Id, string DisplayName)
{
    /// <summary>Đọc "Dung-mac=Dũng-mac" hoặc "Quang".</summary>
    public static ShardOwner Parse(string text)
    {
        var parts = text.Split('=', 2, StringSplitOptions.TrimEntries);
        return parts.Length == 2 ? new ShardOwner(parts[0], parts[1]) : new ShardOwner(parts[0], parts[0]);
    }
}

public sealed record ShardTask
{
    public string Dataset { get; init; } = "";
    public string TaskId { get; init; } = "";
    public string RepoUrl { get; init; } = "";
}

public sealed record ShardFile
{
    public int SchemaVersion { get; init; } = 1;
    public string PlanId { get; init; } = "";
    public string ShardId { get; init; } = "";
    public string Owner { get; init; } = "";
    public string OwnerDisplay { get; init; } = "";
    public string ExperimentSha256 { get; init; } = "";
    public List<ShardTask> Tasks { get; init; } = [];
}

public sealed record ShardSummary
{
    public string ShardId { get; init; } = "";
    public string Owner { get; init; } = "";
    public string OwnerDisplay { get; init; } = "";
    public string File { get; init; } = "";
    public string Sha256 { get; init; } = "";
    public int TaskCount { get; init; }
    public int RepositoryCount { get; init; }
    public Dictionary<string, int> TasksByDataset { get; init; } = [];
    public double EstimatedBuildSeconds { get; init; }
}

public sealed record ShardPlanFile
{
    public int SchemaVersion { get; init; } = 1;
    public string PlanId { get; init; } = "";
    public string ExperimentId { get; init; } = "";
    public string ExperimentSha256 { get; init; } = "";
    public Dictionary<string, string> ManifestsSha256 { get; init; } = [];
    public string Coordinator { get; init; } = "";
    public List<ShardSummary> Shards { get; init; } = [];
}

/// <summary>Kết quả chia shard, chưa ghi ra đĩa.</summary>
public sealed record ShardPlanResult(ShardPlanFile Plan, IReadOnlyList<ShardFile> Shards);

public static class ShardPlanner
{
    /// <summary>
    /// Chia task cho các máy (specs/01 §3): mỗi repository nằm trọn một shard; chia đều số task trước,
    /// rồi cân bằng tổng <c>duration_seconds</c> của preflight; tất định với cùng đầu vào.
    /// </summary>
    public static ShardPlanResult Plan(
        LoadedExperiment experiment,
        IReadOnlyList<ManifestRecord> records,
        IReadOnlyList<ShardOwner> owners,
        string planId,
        string coordinator)
    {
        if (owners.Count == 0)
        {
            throw new ArrowException("SHARD_NO_OWNERS", "Cần ít nhất một máy.");
        }

        if (owners.Select(o => o.Id).Distinct(StringComparer.OrdinalIgnoreCase).Count() != owners.Count)
        {
            throw new ArrowException("SHARD_DUPLICATE_OWNER", "Mã máy bị lặp.");
        }

        var durations = records.Where(r => r.DurationSeconds is > 0).Select(r => r.DurationSeconds!.Value).OrderBy(d => d).ToList();
        var median = durations.Count == 0 ? 60.0 : durations[durations.Count / 2];

        var repositories = records
            .GroupBy(r => r.RepoUrl, StringComparer.Ordinal)
            .Select(g => new
            {
                RepoUrl = g.Key,
                Tasks = g.OrderBy(r => r.Dataset, StringComparer.Ordinal).ThenBy(r => r.TaskId, StringComparer.Ordinal).ToList(),
                Weight = g.Sum(r => r.DurationSeconds is > 0 ? r.DurationSeconds!.Value : median),
            })
            .OrderByDescending(r => r.Weight)
            .ThenBy(r => r.RepoUrl, StringComparer.Ordinal)
            .ToList();

        var buckets = owners.Select((owner, index) => new Bucket($"shard-{index + 1:00}", owner)).ToList();
        foreach (var repository in repositories)
        {
            var target = buckets
                .OrderBy(b => b.Tasks.Count)
                .ThenBy(b => b.Weight)
                .ThenBy(b => b.ShardId, StringComparer.Ordinal)
                .First();
            target.Tasks.AddRange(repository.Tasks);
            target.Weight += repository.Weight;
            target.Repositories++;
        }

        var shardFiles = new List<ShardFile>();
        var summaries = new List<ShardSummary>();
        foreach (var bucket in buckets)
        {
            var tasks = bucket.Tasks
                .OrderBy(t => t.Dataset, StringComparer.Ordinal)
                .ThenBy(t => t.TaskId, StringComparer.Ordinal)
                .Select(t => new ShardTask { Dataset = t.Dataset, TaskId = t.TaskId, RepoUrl = t.RepoUrl })
                .ToList();
            var file = new ShardFile
            {
                PlanId = planId,
                ShardId = bucket.ShardId,
                Owner = bucket.Owner.Id,
                OwnerDisplay = bucket.Owner.DisplayName,
                ExperimentSha256 = experiment.Sha256,
                Tasks = tasks,
            };
            shardFiles.Add(file);
            summaries.Add(new ShardSummary
            {
                ShardId = bucket.ShardId,
                Owner = bucket.Owner.Id,
                OwnerDisplay = bucket.Owner.DisplayName,
                File = $"{bucket.ShardId}.json",
                Sha256 = Hashing.Sha256Text(SerializeShard(file)),
                TaskCount = tasks.Count,
                RepositoryCount = bucket.Repositories,
                TasksByDataset = tasks.GroupBy(t => t.Dataset).OrderBy(g => g.Key).ToDictionary(g => g.Key, g => g.Count()),
                EstimatedBuildSeconds = Math.Round(bucket.Weight, 1),
            });
        }

        var plan = new ShardPlanFile
        {
            PlanId = planId,
            ExperimentId = experiment.Config.ExperimentId,
            ExperimentSha256 = experiment.Sha256,
            ManifestsSha256 = experiment.Config.Manifests.ToDictionary(m => m.Dataset, m => m.Sha256),
            Coordinator = coordinator,
            Shards = summaries,
        };
        return new ShardPlanResult(plan, shardFiles);
    }

    /// <summary>Nội dung byte của file shard; hash trong plan.json tính trên đúng chuỗi này.</summary>
    public static string SerializeShard(ShardFile shard) => Json.Serialize(shard) + "\n";

    public static void Write(ShardPlanResult result, string outputDir)
    {
        Directory.CreateDirectory(outputDir);
        foreach (var shard in result.Shards)
        {
            AtomicFile.WriteAllText(Path.Combine(outputDir, $"{shard.ShardId}.json"), SerializeShard(shard));
        }

        Json.WriteAtomic(Path.Combine(outputDir, "plan.json"), result.Plan);
    }

    /// <summary>Đọc một shard và kiểm tra hash với plan.json cùng thư mục (nếu có).</summary>
    public static (ShardFile Shard, ShardPlanFile? Plan) LoadShard(string shardPath)
    {
        var shard = Json.Read<ShardFile>(shardPath);
        var planPath = Path.Combine(Path.GetDirectoryName(Path.GetFullPath(shardPath))!, "plan.json");
        if (!File.Exists(planPath))
        {
            return (shard, null);
        }

        var plan = Json.Read<ShardPlanFile>(planPath);
        var summary = plan.Shards.FirstOrDefault(s => s.ShardId == shard.ShardId)
                      ?? throw new ArrowException("SHARD_NOT_IN_PLAN", $"{shard.ShardId} không có trong plan.json");
        var actual = Hashing.Sha256File(shardPath);
        if (!string.Equals(actual, summary.Sha256, StringComparison.OrdinalIgnoreCase))
        {
            throw new ArrowException("SHARD_HASH_MISMATCH", $"{shardPath} có hash {actual}, plan.json ghi {summary.Sha256}");
        }

        return (shard, plan);
    }

    private sealed class Bucket(string shardId, ShardOwner owner)
    {
        public string ShardId { get; } = shardId;
        public ShardOwner Owner { get; } = owner;
        public List<ManifestRecord> Tasks { get; } = [];
        public double Weight { get; set; }
        public int Repositories { get; set; }
    }
}
