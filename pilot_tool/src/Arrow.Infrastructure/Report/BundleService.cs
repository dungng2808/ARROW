using System.IO.Compression;
using System.Text;
using Arrow.Core.Llm;
using Arrow.Core.Runs;
using Arrow.Core.Shards;
using Arrow.Core.Util;

namespace Arrow.Infrastructure.Report;

/// <summary><c>bundle_manifest.json</c> trong file zip của <c>export</c> (specs/01 Mục 5).</summary>
public sealed record BundleManifest
{
    public int SchemaVersion { get; init; } = 1;
    public string RunId { get; init; } = "";
    public string PlanId { get; init; } = "";
    public string ShardId { get; init; } = "";
    public string MachineName { get; init; } = "";
    public string ExperimentSha256 { get; init; } = "";
    public string ShardSha256 { get; init; } = "";
    public Dictionary<string, int> JobsByStatus { get; init; } = [];
    public Dictionary<string, string> Files { get; init; } = [];
    public DateTimeOffset CreatedAt { get; init; }
}

/// <summary>
/// <c>arrow export</c> và <c>arrow merge</c> (specs/01 Mục 5, specs/08 Mục 6): đóng gói run của một máy (trừ <c>llm_cache/</c>,
/// log build của bước đạt chỉ giữ 200 dòng cuối) và gom các gói của mọi shard thành một run để báo cáo.
/// </summary>
public static class BundleService
{
    public const long WarnBytes = 50L * 1024 * 1024;
    public const long MaxBytes = 95L * 1024 * 1024;
    public const string ManifestName = "bundle_manifest.json";

    public sealed record ExportResult(string ZipPath, long Bytes, bool Warning, BundleManifest Manifest);

    public static ExportResult Export(RunStore store, string outputDir)
    {
        var runLock = Json.Read<RunConfigLock>(store.RunConfigLockPath);
        var planId = runLock.ShardId is null or "all" ? runLock.ExperimentId : PlanIdOf(runLock);
        var zipPath = Path.Combine(outputDir, planId, $"{runLock.ShardId ?? "all"}-{runLock.MachineName}.zip");
        Directory.CreateDirectory(Path.GetDirectoryName(zipPath)!);
        var files = new Dictionary<string, string>(StringComparer.Ordinal);
        var temp = zipPath + ".tmp";
        File.Delete(temp);
        using (var zip = ZipFile.Open(temp, ZipArchiveMode.Create))
        {
            foreach (var file in Directory.EnumerateFiles(store.Root, "*", SearchOption.AllDirectories).OrderBy(f => f, StringComparer.Ordinal))
            {
                var relative = Path.GetRelativePath(store.Root, file).Replace('\\', '/');
                if (relative.StartsWith("llm_cache/", StringComparison.Ordinal) || relative.EndsWith(".tmp", StringComparison.Ordinal)
                    || relative is "control.json" or "status.json")
                {
                    continue;
                }

                var bytes = File.ReadAllBytes(file);
                if (relative.Contains("/logs/", StringComparison.Ordinal) && relative.EndsWith(".log", StringComparison.Ordinal))
                {
                    bytes = Encoding.UTF8.GetBytes(TrimPassedLog(Encoding.UTF8.GetString(bytes)));
                }

                if (relative == "tool.log")
                {
                    bytes = Encoding.UTF8.GetBytes(ToolLog.Redact(Encoding.UTF8.GetString(bytes)));
                }

                var entry = zip.CreateEntry(relative, CompressionLevel.Optimal);
                using (var stream = entry.Open())
                {
                    stream.Write(bytes);
                }

                files[relative] = Hashing.Sha256Bytes(bytes);
            }

            var manifest = new BundleManifest
            {
                RunId = store.RunId,
                PlanId = planId,
                ShardId = runLock.ShardId ?? "all",
                MachineName = runLock.MachineName,
                ExperimentSha256 = runLock.ExperimentSha256,
                ShardSha256 = runLock.ShardSha256 ?? "",
                JobsByStatus = store.LatestByJob().Values.GroupBy(e => e.Status?.ToString().ToUpperInvariant() ?? "?").ToDictionary(g => g.Key, g => g.Count()),
                Files = files,
                CreatedAt = DateTimeOffset.UtcNow,
            };
            var manifestEntry = zip.CreateEntry(ManifestName);
            using var writer = new StreamWriter(manifestEntry.Open(), new UTF8Encoding(false));
            writer.Write(Json.Serialize(manifest) + "\n");
        }

        var size = new FileInfo(temp).Length;
        if (size > MaxBytes)
        {
            File.Delete(temp);
            throw new ArrowException("BUNDLE_TOO_LARGE", $"Gói {size / 1024 / 1024} MB vượt 95 MB: bỏ bớt log build trong runs/{store.RunId}/tasks/*/logs rồi export lại.");
        }

        File.Move(temp, zipPath, overwrite: true);
        using var read = ZipFile.OpenRead(zipPath);
        var stored = Json.Deserialize<BundleManifest>(new StreamReader(read.GetEntry(ManifestName)!.Open()).ReadToEnd());
        return new ExportResult(zipPath, size, size > WarnBytes, stored);
    }

    /// <summary>Log build của bước đạt (exit 0, không timeout) chỉ giữ phần đầu và 200 dòng cuối; bước lỗi giữ đủ.</summary>
    public static string TrimPassedLog(string log)
    {
        if (!log.Contains("exit: 0 ·", StringComparison.Ordinal) || log.Contains("timeout: true", StringComparison.Ordinal))
        {
            return log;
        }

        var lines = log.Split('\n');
        if (lines.Length <= 210)
        {
            return log;
        }

        var head = lines.TakeWhile(l => l != "--- stdout+stderr ---").Append("--- stdout+stderr ---").ToList();
        return string.Join('\n', head.Append($"… {lines.Length - head.Count - 200} dòng đầu đã bỏ (bước đạt) …").Concat(lines.TakeLast(200)));
    }

    private static string PlanIdOf(RunConfigLock runLock) =>
        runLock.RunId.Contains("-" + runLock.ShardId + "-", StringComparison.Ordinal) ? runLock.RunId[..runLock.RunId.IndexOf("-" + runLock.ShardId + "-", StringComparison.Ordinal)] : runLock.ExperimentId;

    /// <summary>Gom các gói của một plan vào <c>runs/&lt;mergedRunId&gt;/</c>; mọi kiểm tra của specs/01 Mục 5, lỗi nào cũng dừng và liệt kê.</summary>
    public static RunStore Merge(string planPath, IReadOnlyList<string> bundles, string runsDir, string mergedRunId)
    {
        var plan = Json.Read<ShardPlanFile>(planPath);
        var planDir = Path.GetDirectoryName(Path.GetFullPath(planPath))!;
        var errors = new List<string>();
        var opened = bundles.Select(b => (Path: b, Zip: ZipFile.OpenRead(b))).ToList();
        try
        {
            var manifests = opened.Select(o => (o.Path, o.Zip, Manifest: Json.Deserialize<BundleManifest>(new StreamReader(o.Zip.GetEntry(ManifestName)?.Open()
                ?? throw new ArrowException("BUNDLE_INVALID", $"{o.Path} thiếu {ManifestName}")).ReadToEnd()))).ToList();
            var locks = manifests.Select(m => (m.Path, Lock: Json.Deserialize<RunConfigLock>(new StreamReader(m.Zip.GetEntry("run_config.lock.json")!.Open()).ReadToEnd()))).ToList();

            foreach (var m in manifests.Where(m => m.Manifest.ExperimentSha256 != plan.ExperimentSha256))
            {
                errors.Add($"{Path.GetFileName(m.Path)}: experiment_sha256 {m.Manifest.ExperimentSha256} khác plan {plan.ExperimentSha256}");
            }

            var promptHashes = locks.Select(l => Json.Serialize(l.Lock.PromptHashes.OrderBy(p => p.Key).ToList(), false)).Distinct().ToList();
            if (promptHashes.Count > 1)
            {
                errors.Add("prompt_hashes khác nhau giữa các máy: " + string.Join("; ", locks.Select(l => $"{Path.GetFileName(l.Path)} {Hashing.Sha256Text(Json.Serialize(l.Lock.PromptHashes, false))[..12]}")));
            }

            foreach (var shard in plan.Shards)
            {
                var count = manifests.Count(m => m.Manifest.ShardId == shard.ShardId);
                if (count != 1)
                {
                    errors.Add($"{shard.ShardId}: có {count} gói (cần đúng 1)");
                }
            }

            foreach (var extra in manifests.Where(m => plan.Shards.All(s => s.ShardId != m.Manifest.ShardId)))
            {
                errors.Add($"{Path.GetFileName(extra.Path)}: shard {extra.Manifest.ShardId} không có trong plan");
            }

            // Hợp các task bằng đúng tập task của plan, không task nào xuất hiện hai lần.
            var planTasks = plan.Shards.SelectMany(s => Json.Read<ShardFile>(Path.Combine(planDir, s.File)).Tasks).Select(t => $"{t.Dataset}/{t.TaskId}").ToHashSet();
            var seen = new Dictionary<string, string>();
            foreach (var m in manifests)
            {
                foreach (var task in m.Manifest.Files.Keys.Where(f => f.StartsWith("tasks/", StringComparison.Ordinal)).Select(f => string.Join('/', f.Split('/').Skip(1).Take(2))).Distinct())
                {
                    if (seen.TryGetValue(task, out var other))
                    {
                        errors.Add($"task {task} có trong cả {other} và {Path.GetFileName(m.Path)}");
                    }

                    seen[task] = Path.GetFileName(m.Path);
                }
            }

            foreach (var missing in planTasks.Except(seen.Keys).Order(StringComparer.Ordinal))
            {
                errors.Add($"task {missing} của plan không có trong gói nào");
            }

            foreach (var unexpected in seen.Keys.Except(planTasks).Order(StringComparer.Ordinal))
            {
                errors.Add($"task {unexpected} không thuộc plan");
            }

            // Model id trả về giống nhau trên mọi máy.
            var modelIds = new Dictionary<string, HashSet<string>>();
            foreach (var m in manifests)
            {
                foreach (var entry in m.Zip.Entries.Where(e => e.FullName.EndsWith("/usage.json", StringComparison.Ordinal)))
                {
                    var usage = Json.Deserialize<LlmUsage>(new StreamReader(entry.Open()).ReadToEnd());
                    modelIds.TryAdd(usage.ModelId, []);
                    modelIds[usage.ModelId].Add(m.Manifest.MachineName);
                }
            }

            if (modelIds.Count > 1)
            {
                errors.Add("model id khác nhau giữa các máy: " + string.Join("; ", modelIds.Select(p => $"{p.Key} ({string.Join(", ", p.Value)})")));
            }

            if (errors.Count > 0)
            {
                throw new ArrowException("MERGE_INVALID", "Không gom được:\n- " + string.Join("\n- ", errors));
            }

            var store = new RunStore(runsDir, mergedRunId);
            if (store.Exists)
            {
                throw new ArrowException("RUN_EXISTS", $"runs/{mergedRunId} đã có; chọn tên khác hoặc xóa run cũ");
            }

            store.EnsureCreated();
            var ledger = new StringBuilder();
            var events = new StringBuilder();
            foreach (var (path, zip, manifest) in manifests.OrderBy(m => m.Manifest.ShardId, StringComparer.Ordinal))
            {
                foreach (var entry in zip.Entries.Where(e => e.FullName.StartsWith("tasks/", StringComparison.Ordinal)))
                {
                    var target = Path.Combine(store.Root, entry.FullName);
                    Directory.CreateDirectory(Path.GetDirectoryName(target)!);
                    entry.ExtractToFile(target, overwrite: false);
                }

                ledger.Append(new StreamReader(zip.GetEntry("ledger.jsonl")!.Open()).ReadToEnd());
                if (zip.GetEntry("events.jsonl") is { } eventsEntry)
                {
                    events.Append(new StreamReader(eventsEntry.Open()).ReadToEnd());
                }

                var machineDir = Path.Combine(store.Root, "machines");
                Directory.CreateDirectory(machineDir);
                zip.GetEntry("run_config.lock.json")!.ExtractToFile(Path.Combine(machineDir, $"{manifest.ShardId}-{manifest.MachineName}.json"));
                _ = path;
            }

            AtomicFile.WriteAllText(store.LedgerPath, ledger.ToString());
            AtomicFile.WriteAllText(store.EventsPath, events.ToString());
            Json.WriteAtomic(Path.Combine(store.Root, "merge.json"), new
            {
                plan_id = plan.PlanId,
                experiment_sha256 = plan.ExperimentSha256,
                bundles = manifests.Select(m => new { file = Path.GetFileName(m.Path), shard_id = m.Manifest.ShardId, machine = m.Manifest.MachineName, run_id = m.Manifest.RunId }).ToList(),
                merged_at = DateTimeOffset.UtcNow,
            });
            return store;
        }
        finally
        {
            foreach (var (_, zip) in opened)
            {
                zip.Dispose();
            }
        }
    }
}
