using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Runner;
using Preflight.Service.Core.Shards;
using Preflight.Service.Datasets;
using Preflight.Service.Datasets.Classes2Test;
using Preflight.Service.Datasets.GitBug;
using Preflight.Service.Datasets.CatLm;

namespace Preflight.Service;

/// <summary>Điều phối normalize/run/resume/finalize; không gọi Python để xử lý nghiệp vụ.</summary>
public sealed class PreflightService
{
    public async Task<NormalizationResult> NormalizeAsync(string dataset, PreflightConfig config, string runRoot, string? inputRoot, int limit, CancellationToken ct)
    {
        if (Directory.Exists(runRoot) && Directory.EnumerateFileSystemEntries(runRoot).Any()) throw new InvalidDataException("Output directory already contains data");
        Directory.CreateDirectory(runRoot);
        IDatasetAdapter adapter = dataset switch { "classes2test" => new Classes2TestAdapter(), "gitbug" or "gitbug-java" => new GitBugAdapter(), "catlm" => new CatLmAdapter(), _ => throw new ArgumentException("Unknown dataset") };
        var datasetConfig = config.Datasets.GetValueOrDefault(dataset) ?? config.Datasets.GetValueOrDefault(adapter.Name) ?? (dataset == "gitbug-java" ? config.Datasets.GetValueOrDefault("gitbug") : null) ?? new DatasetConfig();
        if (inputRoot is not null) datasetConfig = datasetConfig with { Root = inputRoot };
        var result = await adapter.NormalizeAsync(datasetConfig, new(runRoot, config.Run.Offline, limit), ct);
        ManifestWriter.WriteJsonl(Path.Combine(runRoot, "manifests/class_candidates.jsonl"), result.Tasks.Select(CandidateRecord));
        ManifestWriter.WriteJsonl(Path.Combine(runRoot, "reports/input_rejections.jsonl"), result.Rejections);
        ManifestWriter.WriteJson(Path.Combine(runRoot, "state/normalize.json"), new NormalizedState(config, adapter.Name, datasetConfig.Root, result.RawCount, result.Tasks.Count));
        ManifestWriter.WriteJson(Path.Combine(runRoot, "provenance.json"), new { Tool = "preflight-net", StartedAtUtc = CheckpointStore.Now(), Dataset = adapter.Name, InputRoot = datasetConfig.Root, RawJsonIndexed = result.RawCount, DeduplicatedClasses = result.Tasks.Count, ClassesSelected = result.Tasks.Count, RunStatus = "NORMALIZED", ProbeProfile = config.Run.ProbeProfile.ToString(), RevisionPolicy = datasetConfig.RevisionPolicy });
        return result;
    }
    public async Task<int> RunAsync(string runRoot, bool resume, int? workers, bool fast, string? mapPath, string? shardPath, int maxClasses, CancellationToken ct)
    {
        if (workers is < 1 || maxClasses < 0) throw new ArgumentException("workers must be positive; max-classes cannot be negative");
        using var runLock = new RunLock(Path.Combine(runRoot, "state/run.lock"));
        string checkpointPath = Path.Combine(runRoot, "state/run_state.sqlite");
        if (resume && !File.Exists(checkpointPath)) throw new InvalidDataException("No resumable checkpoint");
        if (!resume && File.Exists(checkpointPath)) throw new InvalidDataException("Run already started; use resume");
        var normalized = JsonSerializer.Deserialize<NormalizedState>(File.ReadAllText(Path.Combine(runRoot, "state/normalize.json")), PythonJson.Options)!;
        using var index = new ClassIndexStore(Path.Combine(runRoot, "state/class_index.sqlite"));
        var indexed = index.LoadTasks();
        string library = Path.IsPathRooted(normalized.Config.Java.TreeSitterLibrary) ? normalized.Config.Java.TreeSitterLibrary : Path.Combine(RuntimePaths.Root, normalized.Config.Java.TreeSitterLibrary);
        using var checkpoint = new CheckpointStore(checkpointPath); RunContract contract;
        if (resume)
        {
            contract = checkpoint.Contract();
            var expectedConfig = contract.Config with { Run = contract.Config.Run with { Workers = normalized.Config.Run.Workers, FastMode = normalized.Config.Run.FastMode } };
            if (PythonJson.Serialize(expectedConfig) != PythonJson.Serialize(normalized.Config)) throw new InvalidDataException("Normalized configuration changed");
            if (contract.ToolFingerprint != RuntimePaths.Fingerprint(library)) throw new InvalidDataException("Tool fingerprint changed");
            if (RuntimePaths.Hash(Path.Combine(runRoot, "manifests/class_candidates.jsonl")) != contract.CandidateManifestSha256) throw new InvalidDataException("Candidate manifest changed");
            var current = indexed.ToDictionary(t => t.Candidate.TaskId, StringComparer.Ordinal);
            foreach (var task in contract.Tasks) if (!current.TryGetValue(task.Candidate.TaskId, out var actual) || PythonJson.Serialize(actual) != PythonJson.Serialize(task)) throw new InvalidDataException("Class index differs from run contract");
        }
        else
        {
            var tasks = indexed.OrderBy(t => t.Candidate.ProjectId, PythonCodePointComparer.Instance).ThenBy(t => t.Candidate.TaskId, PythonCodePointComparer.Instance).ToArray();
            if (shardPath is not null && maxClasses > 0) throw new ArgumentException("--shard cannot be combined with --max-classes");
            if (shardPath is not null) tasks = ShardSelector.Select(tasks, shardPath);
            if (maxClasses > 0) tasks = tasks.Take(maxClasses).ToArray();
            if (mapPath is not null)
            {
                var map = RevisionMapReader.Load(mapPath); tasks = tasks.Select(t => map.TryGetValue(t.Candidate.TaskId, out var choice) ? t with { PinnedRevision = choice } : t).ToArray();
                index.SaveTasks(tasks);
            }
            var config = normalized.Config with { Run = normalized.Config.Run with { Workers = workers ?? normalized.Config.Run.Workers, FastMode = fast || normalized.Config.Run.FastMode } };
            ManifestWriter.WriteJsonl(Path.Combine(runRoot, "manifests/class_candidates.jsonl"), tasks.Select(CandidateRecord));
            contract = new(config, normalized.Dataset, normalized.InputRoot, tasks, RuntimePaths.Fingerprint(library), RuntimePaths.Hash(Path.Combine(runRoot, "manifests/class_candidates.jsonl")));
            checkpoint.Initialize(contract);
        }
        if (workers is < 1) throw new ArgumentException("workers must be positive");
        var effective = contract.Config with { Run = contract.Config.Run with { Workers = workers ?? contract.Config.Run.Workers } };
        UpdateProvenance(runRoot, checkpoint, contract, "RUNNING");
        var pipeline = new PreflightPipeline(runRoot, contract.InputRoot, effective);
        string state;
        try { state = await new RunScheduler(runRoot, effective, checkpoint, pipeline).RunAsync(contract.Tasks, contract.InputRoot, ct); }
        catch { UpdateProvenance(runRoot, checkpoint, contract, "FAILED"); throw; }
        if (state == "COMPLETED") Finalize(runRoot, checkpoint);
        UpdateProvenance(runRoot, checkpoint, contract, state);
        string patches = runRoot.TrimEnd(Path.DirectorySeparatorChar) + ".patches.json";
        if (state == "COMPLETED" && File.Exists(patches) && !Directory.Exists(Path.Combine(runRoot, "merged")))
        {
            using var doc = JsonDocument.Parse(File.ReadAllText(patches));
            AuxiliaryCommands.Merge([doc.RootElement.GetProperty("previous_run").GetString()!, runRoot], Path.Combine(runRoot, "merged"));
        }
        return state == "COMPLETED" ? 0 : state == "INFRA_DISK_FULL" ? 75 : 130;
    }
    public static object CandidateRecord(CandidateTask t) => new
    {
        t.Candidate.TaskId, t.Candidate.ProjectId, t.Candidate.RepoUrl, t.Candidate.SourceJsonPath, t.Candidate.SourceJsonSha256, t.Candidate.SourceJsonPaths, t.Candidate.SourceJsonSha256s, t.Candidate.ClassPath, t.Candidate.ClassFqn, t.Candidate.ClassName,
        TestClassPath = t.Candidate.TestClassPaths.FirstOrDefault() ?? "", t.Candidate.TestClassPaths, t.Dataset, PairingMethod = t.Pairing.Method
    };
    public static void Finalize(string runRoot, CheckpointStore checkpoint)
    {
        var results = checkpoint.Results(); var counts = checkpoint.Counts();
        if (counts["completed"] != counts["total"] || results.Select(r => r.TaskId).Distinct(StringComparer.Ordinal).Count() != counts["total"]) throw new InvalidDataException("Cannot finalize incomplete run");
        ManifestWriter.WriteJsonl(Path.Combine(runRoot, "reports/preflight_results.jsonl"), results); ManifestWriter.WriteCsv(Path.Combine(runRoot, "reports/preflight_results.csv"), results);
        string[] omitted = ["build_attempts", "duration_seconds", "log_paths", "preflight_status", "technical_eligible", "strict_eligible", "tags", "reason_codes", "probe_status", "probed_api", "constructor_count", "method_count", "api_count"];
        var locked = results.Select(r => PythonJson.Element(r).EnumerateObject().Where(p => !omitted.Contains(p.Name, StringComparer.Ordinal)).ToDictionary(p => p.Name, p => p.Value.Clone(), StringComparer.Ordinal));
        ManifestWriter.WriteJsonl(Path.Combine(runRoot, "manifests/locked_input_manifest.jsonl"), locked);
        ManifestWriter.WriteJsonl(Path.Combine(runRoot, "manifests/technical_eligible_manifest.jsonl"), results.Where(r => r.TechnicalEligible));
        ManifestWriter.WriteJsonl(Path.Combine(runRoot, "manifests/strict_eligible_manifest.jsonl"), results.Where(r => r.StrictEligible));
        ManifestWriter.WriteJson(Path.Combine(runRoot, "reports/summary.json"), new { Total = results.Count, TechnicalEligible = results.Count(r => r.TechnicalEligible), StrictEligible = results.Count(r => r.StrictEligible), ByStatus = results.GroupBy(r => r.PreflightStatus.ToString()).ToDictionary(g => g.Key, g => g.Count(), StringComparer.Ordinal), RepoCleanup = checkpoint.CleanupEvents().GroupBy(e => e.GetProperty("status").GetString()!).ToDictionary(g => g.Key, g => g.Count(), StringComparer.Ordinal) });
    }
    public static void UpdateProvenance(string runRoot, CheckpointStore checkpoint, RunContract contract, string state, string? reason = null)
    {
        string path = Path.Combine(runRoot, "provenance.json");
        var value = JsonSerializer.Deserialize<Dictionary<string, JsonElement>>(File.ReadAllText(path))!;
        if (reason is not null) { value["reopen_reason"] = PythonJson.Element(reason); value["reopened_at_utc"] = PythonJson.Element(CheckpointStore.Now()); }
        value["run_status"] = PythonJson.Element(state); value["checkpoint_schema_version"] = PythonJson.Element(3); value["sessions"] = PythonJson.Element(checkpoint.Sessions()); value["resume_count"] = PythonJson.Element(Math.Max(0, checkpoint.Sessions().Count - 1)); value["classes_selected"] = PythonJson.Element(contract.Tasks.Count); value["probe_profile"] = PythonJson.Element(contract.Config.Run.ProbeProfile.ToString()); value["tool_fingerprint"] = PythonJson.Element(contract.ToolFingerprint);
        ManifestWriter.WriteJson(path, value);
    }
}
public sealed record NormalizedState(PreflightConfig Config, string Dataset, string InputRoot, int RawCount, int CandidateCount);
