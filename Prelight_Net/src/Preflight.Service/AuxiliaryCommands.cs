using System.Text;
using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Runner;
using Preflight.Service.Core.Shards;
using Preflight.Service.Datasets.Classes2Test;

namespace Preflight.Service;

public static class AuxiliaryCommands
{
    public static void Fresh(string path)
    {
        if (Directory.Exists(path) || File.Exists(path)) throw new IOException("Output already exists: " + path);
        Directory.CreateDirectory(path);
    }
    public static IReadOnlyList<JsonElement> ReadRows(string path) => File.ReadLines(path).Where(l => !string.IsNullOrWhiteSpace(l)).Select(l => JsonDocument.Parse(l).RootElement.Clone()).ToArray();
    public static void ExportShards(string index, string output, int parts)
    {
        var tasks = ReadOnlyIndex.Load(index); var buckets = ShardSelector.Partition(tasks, parts); Fresh(output);
        var summary = new List<object>();
        for (int i = 0; i < buckets.Count; i++)
        {
            string id = $"shard-{i + 1:00}", file = id + ".json", path = Path.Combine(output, file);
            var rows = buckets[i].Select(t => ShardSelector.Assignment(t.Candidate)).ToArray();
            string json = PythonJson.Render(PythonJson.Element(new { SchemaVersion = 1, ShardId = id, ClassCount = rows.Length, Classes = rows }), compact: true, sortKeys: false) + "\n";
            File.WriteAllText(path, json, new UTF8Encoding(false));
            summary.Add(new { File = file, ClassCount = rows.Length, RepositoryCount = buckets[i].Select(t => ShardSelector.RepoKey(t.Repo.RepoUrl)).Distinct(StringComparer.Ordinal).Count(), Sha256 = RuntimePaths.Hash(path), Bytes = new FileInfo(path).Length });
        }
        ManifestWriter.WriteJson(Path.Combine(output, "summary.json"), new { SchemaVersion = 1, ClassCount = tasks.Count, RepositoryCount = tasks.Select(t => ShardSelector.RepoKey(t.Repo.RepoUrl)).Distinct(StringComparer.Ordinal).Count(), Shards = summary });
    }
    public static async Task SplitAsync(string source, string output, int parts, CancellationToken ct)
    {
        source = Classes2TestAdapter.DatasetRoot(source);
        if (ResolvePath.IsWithin(output, source)) throw new InvalidDataException("Output must be outside source");
        if (parts < 1) throw new ArgumentException("parts must be positive"); Fresh(output);
        var result = await new Classes2TestAdapter().NormalizeAsync(new() { Root = source }, new(output), ct);
        if (result.Rejections.Count > 0) throw new InvalidDataException("Rejected inputs; split aborted. Inspect source index.");
        var buckets = ShardSelector.Partition(result.Tasks, parts); var seen = new HashSet<string>(StringComparer.Ordinal); var assignments = new List<object>(); var stats = new List<object>();
        for (int i = 0; i < buckets.Count; i++)
        {
            string shard = $"shard-{i + 1:00}", folder = Path.Combine(output, shard); var manifest = new List<object>(); long bytes = 0;
            foreach (var task in buckets[i]) foreach (var (relative, hash) in task.Candidate.SourceJsonPaths.Zip(task.Candidate.SourceJsonSha256s))
            {
                ct.ThrowIfCancellationRequested(); if (!seen.Add(relative)) throw new InvalidDataException("Duplicate file assignment: " + relative);
                string src = Path.GetFullPath(Path.Combine(source, relative)), dst = Path.GetFullPath(Path.Combine(folder, "dataset", relative));
                if (!ResolvePath.IsWithin(src, source) || !ResolvePath.IsWithin(dst, Path.Combine(folder, "dataset"))) throw new InvalidDataException("Unsafe source path");
                Directory.CreateDirectory(Path.GetDirectoryName(dst)!); File.Copy(src, dst);
                if (RuntimePaths.Hash(dst) != hash) throw new InvalidDataException("Copy checksum mismatch: " + relative);
                bytes += new FileInfo(dst).Length; manifest.Add(new { Path = relative, Sha256 = hash, task.Candidate.TaskId, task.Candidate.RepoUrl });
            }
            var urls = buckets[i].Select(t => t.Repo.RepoUrl).Distinct(StringComparer.Ordinal).Order(PythonCodePointComparer.Instance).ToArray();
            assignments.AddRange(urls.Select(url => (object)new { RepoUrl = url, RepositoryKey = ShardSelector.RepoKey(url), Shard = shard }));
            ManifestWriter.WriteJsonl(Path.Combine(folder, "file_manifest.jsonl"), manifest.OrderBy(r => PythonJson.Element(r).GetProperty("path").GetString(), StringComparer.Ordinal));
            stats.Add(new { Shard = shard, Classes = buckets[i].Length, Repositories = urls.Select(ShardSelector.RepoKey).Distinct(StringComparer.Ordinal).Count(), JsonFiles = manifest.Count, Bytes = bytes });
        }
        var all = Directory.EnumerateDirectories(source).SelectMany(p => Directory.EnumerateFiles(p, "*.json")).Select(p => Path.GetRelativePath(source, p).Replace('\\', '/')).ToHashSet(StringComparer.Ordinal);
        if (!seen.SetEquals(all) || seen.Count != result.RawCount) throw new InvalidDataException("Source coverage mismatch");
        ManifestWriter.WriteJson(Path.Combine(output, "repository_assignment.json"), assignments);
        ManifestWriter.WriteJson(Path.Combine(output, "split_report.json"), new { Source = source, Parts = parts, RawJson = result.RawCount, Classes = result.Tasks.Count, Rejected = 0, Verified = true, Shards = stats });
    }
    public static void Summarize(string run, string output)
    {
        var rows = ReadRows(Path.Combine(run, "reports/preflight_results.jsonl"));
        var summary = JsonDocument.Parse(File.ReadAllBytes(Path.Combine(run, "reports/summary.json"))).RootElement;
        var provenance = JsonDocument.Parse(File.ReadAllBytes(Path.Combine(run, "provenance.json"))).RootElement;
        Dictionary<string, int> Count(IEnumerable<string> values) => values.GroupBy(x => x).OrderBy(g => g.Key, PythonCodePointComparer.Instance).ToDictionary(g => g.Key, g => g.Count(), StringComparer.Ordinal);
        string S(JsonElement row, string key) => row.GetProperty(key).GetString() ?? "";
        bool Flag(JsonElement row, string key) => row.GetProperty(key).GetBoolean();
        var eligible = rows.Where(r => Flag(r, "technical_eligible")).ToArray(); var statuses = Count(rows.Select(r => S(r, "preflight_status")));
        if (S(provenance, "run_status") != "COMPLETED" || rows.Count != summary.GetProperty("total").GetInt32() || rows.Count != provenance.GetProperty("classes_selected").GetInt32() || rows.Select(r => S(r, "task_id")).Distinct(StringComparer.Ordinal).Count() != rows.Count || eligible.Length != summary.GetProperty("technical_eligible").GetInt32() || PythonJson.Serialize(statuses) != PythonJson.Render(summary.GetProperty("by_status"))) throw new InvalidDataException("Incomplete run or inconsistent reports");
        string[] privateKeys = ["config_path", "dataset_dir", "input_root", "shard_path", "revision_map"];
        var clean = provenance.EnumerateObject().Where(p => !privateKeys.Contains(p.Name, StringComparer.Ordinal)).ToDictionary(p => p.Name, p => p.Value.Clone(), StringComparer.Ordinal);
        if (clean.TryGetValue("previous_run", out var previousRun) && previousRun.ValueKind == JsonValueKind.String) clean["previous_run"] = PythonJson.Element(Path.GetFileName(previousRun.GetString()!.TrimEnd('/', '\\')));
        var hashes = new[] { "reports/summary.json", "reports/preflight_results.jsonl", "provenance.json" }.ToDictionary(p => p, p => RuntimePaths.Hash(Path.Combine(run, p)), StringComparer.Ordinal);
        var stats = new { SchemaVersion = 1, Provenance = clean, SourceFilesSha256 = hashes, AllClasses = new { Total = rows.Count, Repositories = rows.Select(r => S(r, "repo_url")).Distinct(StringComparer.Ordinal).Count(), ByPreflightStatus = statuses, ByReasonCode = Count(rows.SelectMany(r => r.GetProperty("reason_codes").EnumerateArray().Select(v => v.GetString()!))), ByRevisionVerificationStatus = Count(rows.Select(r => S(r, "revision_verification_status"))), ByRunMode = Count(rows.Select(r => S(r, "run_mode"))) }, TechnicalEligible = new { Total = eligible.Length, Repositories = eligible.Select(r => S(r, "repo_url")).Distinct(StringComparer.Ordinal).Count(), ByRevisionVerificationStatus = Count(eligible.Select(r => S(r, "revision_verification_status"))), ByBuildTool = Count(eligible.Select(r => S(r, "build_tool"))), ByTestingFramework = Count(eligible.Select(r => S(r, "testing_framework"))), ByTag = Count(eligible.SelectMany(r => r.GetProperty("tags").EnumerateArray().Select(v => v.GetString()!))) }, StrictEligible = rows.Count(r => Flag(r, "strict_eligible")) };
        Directory.CreateDirectory(output); File.Copy(Path.Combine(run, "reports/summary.json"), Path.Combine(output, "summary.json"), true); ManifestWriter.WriteJson(Path.Combine(output, "run_stats.json"), stats);
    }
    public static void Merge(IReadOnlyList<string> runs, string output)
    {
        if (runs.Count == 0) throw new ArgumentException("At least one run required");
        var sources = new List<object>();
        string[] names = ["preflight_results.jsonl", "class_candidates.jsonl", "locked_input_manifest.jsonl", "technical_eligible_manifest.jsonl", "strict_eligible_manifest.jsonl"];
        var manifests = names.ToDictionary(n => n, _ => new Dictionary<string, (string Line, int Run)>(StringComparer.Ordinal), StringComparer.Ordinal);
        var processed = new Dictionary<string, int>(StringComparer.Ordinal);
        for (int i = 0; i < runs.Count; i++)
        {
            string run = runs[i], folder = Directory.Exists(Path.Combine(run, "merged")) ? Path.Combine(run, "merged") : run;
            using var provenance = JsonDocument.Parse(File.ReadAllText(Path.Combine(run, "provenance.json")));
            if (provenance.RootElement.GetProperty("run_status").GetString() != "COMPLETED") throw new InvalidDataException("Cannot merge incomplete run: " + run);
            string Find(string name) => File.Exists(Path.Combine(folder, name)) ? Path.Combine(folder, name) : Path.Combine(folder, name == names[0] ? "reports" : "manifests", name);
            var present = names.Where(n => n != names[0] || File.Exists(Find(n))).ToArray();
            var hashes = present.ToDictionary(n => n, n => RuntimePaths.Hash(Find(n)), StringComparer.Ordinal);
            foreach (string name in present) foreach (string line in File.ReadLines(Find(name)).Where(l => !string.IsNullOrWhiteSpace(l)))
            {
                using var doc = JsonDocument.Parse(line); string id = doc.RootElement.GetProperty("task_id").GetString()!;
                manifests[name][id] = (line, i); if (name == "locked_input_manifest.jsonl") processed[id] = i;
            }
            sources.Add(new { Run = run, SourceFilesSha256 = hashes });
        }
        Fresh(output);
        foreach (string name in names)
        {
            var rows = manifests[name].Where(p => name is not ("technical_eligible_manifest.jsonl" or "strict_eligible_manifest.jsonl") || processed.GetValueOrDefault(p.Key, -1) == p.Value.Run).OrderBy(p => p.Key, PythonCodePointComparer.Instance);
            File.WriteAllText(Path.Combine(output, name), string.Concat(rows.Select(p => p.Value.Line + "\n")), new UTF8Encoding(false));
        }
        ManifestWriter.WriteJson(Path.Combine(output, "merge_provenance.json"), new { SchemaVersion = 1, Runs = sources, RecordsBySourceRun = processed.Values.GroupBy(v => runs[v]).ToDictionary(g => g.Key, g => g.Count(), StringComparer.Ordinal), OutputSha256 = names.ToDictionary(n => n, n => RuntimePaths.Hash(Path.Combine(output, n)), StringComparer.Ordinal) });
    }
    public static async Task<int> RerunAsync(string previous, string output, string[] statuses, bool newOnly, int? workers, CancellationToken ct, string? inputRoot = null, string? configPath = null, string? revisionMap = null)
    {
        var state = JsonSerializer.Deserialize<NormalizedState>(File.ReadAllText(Path.Combine(previous, "state/normalize.json")), PythonJson.Options)!;
        var config = (configPath is null ? state.Config : ConfigLoader.Load(configPath)) with { Run = (configPath is null ? state.Config : ConfigLoader.Load(configPath)).Run with { ProbeProfile = ProbeProfile.v2 } };
        var tasks = inputRoot is null ? ReadOnlyIndex.Load(Path.Combine(previous, "state/class_index.sqlite")) : (await new PreflightService().NormalizeAsync(state.Dataset, config, output, inputRoot, 0, ct)).Tasks;
        if (inputRoot is not null) state = state with { InputRoot = inputRoot };
        if (revisionMap is not null) { var map = RevisionMapReader.Load(revisionMap); tasks = tasks.Select(t => map.TryGetValue(t.Candidate.TaskId, out var choice) ? t with { PinnedRevision = choice } : t).ToArray(); }
        string priorPath = File.Exists(Path.Combine(previous, "merged/preflight_results.jsonl")) ? Path.Combine(previous, "merged/preflight_results.jsonl") : Path.Combine(previous, "reports/preflight_results.jsonl");
        var prior = ReadRows(priorPath).ToDictionary(r => r.GetProperty("task_id").GetString()!, StringComparer.Ordinal);
        var selected = tasks.Where(t => newOnly ? !prior.ContainsKey(t.Candidate.TaskId) : prior.TryGetValue(t.Candidate.TaskId, out var row) && (statuses.Length > 0 ? statuses.Contains(row.GetProperty("preflight_status").GetString(), StringComparer.Ordinal) : row.GetProperty("preflight_status").GetString() is not ("ELIGIBLE" or "EXCLUDED"))).Select(t => t.Repo.Kind == "snapshot" ? t with { Repo = t.Repo with { SnapshotPath = Path.Combine(output, "cache/catlm_snapshots", Path.GetFileName(t.Repo.SnapshotPath)) } } : t).ToArray();
        if (selected.Length == 0) throw new InvalidDataException("Nothing to rerun: no class matches selection");
        if (inputRoot is null) Fresh(output);
        foreach (var task in selected) foreach (string relative in task.Candidate.SourceJsonPaths)
        {
            if (Path.IsPathRooted(relative)) continue;
            string evidence = RuntimePaths.Evidence(task, relative, previous, state.InputRoot), destination = Path.GetFullPath(Path.Combine(output, "inputs", relative));
            if (!ResolvePath.IsWithin(destination, Path.Combine(output, "inputs"))) throw new InvalidDataException("Unsafe evidence path");
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!); File.Copy(evidence, destination, true);
        }
        using (var index = new ClassIndexStore(Path.Combine(output, "state/class_index.sqlite"))) index.SaveTasks(selected);
        ManifestWriter.WriteJson(Path.Combine(output, "state/normalize.json"), state with { Config = config, CandidateCount = selected.Length });
        ManifestWriter.WriteJsonl(Path.Combine(output, "manifests/class_candidates.jsonl"), selected.Select(PreflightService.CandidateRecord));
        ManifestWriter.WriteJson(Path.Combine(output, "provenance.json"), new { RunStatus = "NORMALIZED", PreviousRun = previous, ProbeProfile = "v2" });
        ManifestWriter.WriteJson(output + ".patches.json", new { ProbeProfile = "v2", PreviousRun = previous, Statuses = statuses, NewOnly = newOnly, Selected = selected.Select(t => t.Candidate.TaskId) });
        int code = await new PreflightService().RunAsync(output, false, workers, false, null, null, 0, ct);
        return code;
    }
    public static void ConvertConfig(string input, string output)
    {
        // Legacy preflight uses scalar TOML tables only. Reject unsupported syntax rather than silently dropping it.
        var root = new Dictionary<string, object?>(StringComparer.Ordinal); var table = root;
        foreach (string raw in File.ReadLines(input))
        {
            string line = raw.Trim(); if (line.Length == 0 || line.StartsWith('#')) continue;
            if (line.StartsWith('[') && line.EndsWith(']'))
            {
                table = root; foreach (string key in line[1..^1].Split('.')) { if (!table.ContainsKey(key)) table[key] = new Dictionary<string, object?>(StringComparer.Ordinal); table = table[key] as Dictionary<string, object?> ?? throw new InvalidDataException("Conflicting TOML table"); } continue;
            }
            int eq = line.IndexOf('='); if (eq < 1) throw new InvalidDataException("Unsupported TOML: " + line);
            string name = line[..eq].Trim().Trim('"', '\''), value = line[(eq + 1)..].Trim(); object? parsed;
            if (value.StartsWith('\'')) { int end = value.IndexOf('\'', 1); if (end < 0) throw new InvalidDataException("Unclosed TOML string"); parsed = value[1..end]; }
            else if (value.StartsWith('"'))
            {
                int end = 1; for (; end < value.Length; end++) { if (value[end] == '\\') { end++; continue; } if (value[end] == '"') break; }
                if (end == value.Length) throw new InvalidDataException("Unclosed TOML string"); parsed = JsonSerializer.Deserialize<string>(value[..(end + 1)]);
            }
            else { value = value.Split('#')[0].Trim(); parsed = JsonSerializer.Deserialize<JsonElement>(value); }
            if (!table.TryAdd(name, parsed)) throw new InvalidDataException("Duplicate TOML key: " + name);
        }
        if (root.Remove("input", out var old) && old is Dictionary<string, object?> oldInput) root["datasets"] = new { Classes2test = new { Root = oldInput.GetValueOrDefault("root") } };
        ManifestWriter.WriteJson(output, root); _ = ConfigLoader.Load(output);
    }
}
