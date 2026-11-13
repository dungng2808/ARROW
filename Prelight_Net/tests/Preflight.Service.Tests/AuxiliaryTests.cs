using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Runner;
using Preflight.Service.Core.Shards;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class AuxiliaryTests
{
    private static string Dataset(TestWorkspace work)
    {
        foreach (var (id, repo) in new[] { ("1", "https://github.com/A/Repo.git"), ("2", "https://github.com/a/repo/"), ("3", "https://github.com/B/Other") })
            work.File($"input/{id}/1.json", PythonJson.Serialize(new { Repository = new { Url = repo }, FocalClass = new { Identifier = "X", File = "src/main/java/X.java" }, TestClass = new { File = "src/test/java/XTest.java" } }));
        return Path.Combine(work.Path, "input");
    }
    [Fact]
    public async Task ShardsAreDisjointCoverAllTasksAndRejectChangedEvidence()
    {
        using var w = new TestWorkspace(); string input = Dataset(w), run = Path.Combine(w.Path, "run"), shards = Path.Combine(w.Path, "shards");
        var normalized = await new PreflightService().NormalizeAsync("classes2test", new(), run, input, 0, default);
        AuxiliaryCommands.ExportShards(Path.Combine(run, "state/class_index.sqlite"), shards, 2);
        var selected = Directory.GetFiles(shards, "shard-*.json").SelectMany(p => ShardSelector.Select(normalized.Tasks, p)).ToArray();
        Assert.Equal(3, selected.Length); Assert.Equal(3, selected.Select(t => t.Candidate.TaskId).Distinct(StringComparer.Ordinal).Count());
        Assert.Equal(ShardSelector.RepoKey(normalized.Tasks[0].Repo.RepoUrl), ShardSelector.RepoKey(normalized.Tasks[1].Repo.RepoUrl));
        Assert.Equal(2, ShardSelector.Partition(normalized.Tasks, 2).Max(b => b.Length));
        string file = Directory.GetFiles(shards, "shard-*.json").First(); var bad = JsonSerializer.Deserialize<Dictionary<string, JsonElement>>(File.ReadAllText(file))!;
        bad["class_count"] = PythonJson.Element(99); ManifestWriter.WriteJson(file, bad);
        Assert.Throws<InvalidDataException>(() => ShardSelector.Select(normalized.Tasks, file));
    }
    [Fact]
    public async Task DatasetSplitCopiesAndChecksEveryFileAndStopsOnRejections()
    {
        using var w = new TestWorkspace(); string input = Dataset(w), output = Path.Combine(w.Path, "split");
        await AuxiliaryCommands.SplitAsync(input, output, 2, default);
        var rows = Directory.GetFiles(output, "file_manifest.jsonl", SearchOption.AllDirectories).SelectMany(AuxiliaryCommands.ReadRows).ToArray(); Assert.Equal(3, rows.Length);
        foreach (var row in rows) Assert.Equal(RuntimePaths.Hash(Path.Combine(input, row.GetProperty("path").GetString()!)), row.GetProperty("sha256").GetString());
        w.File("input/bad/1.json", "{}");
        await Assert.ThrowsAsync<InvalidDataException>(() => AuxiliaryCommands.SplitAsync(input, Path.Combine(w.Path, "bad-split"), 2, default));
        Assert.False(File.Exists(Path.Combine(w.Path, "bad-split/split_report.json")));
    }
    [Fact]
    public void MergeCopiesRowsUnchangedAndRemovesStaleEligibility()
    {
        using var w = new TestWorkspace();
        foreach (string run in new[] { "old", "new" })
        {
            w.File(run + "/provenance.json", "{\"run_status\":\"COMPLETED\"}");
            string row = run == "old" ? "{\"task_id\": \"a\", \"marker\": \"old\"}" : "{ \"marker\":\"new\", \"task_id\":\"a\" }";
            w.File(run + "/reports/preflight_results.jsonl", row + "\n");
            foreach (string name in new[] { "class_candidates", "locked_input_manifest", "technical_eligible_manifest", "strict_eligible_manifest" }) w.File(run + "/manifests/" + name + ".jsonl", run == "new" && name.Contains("eligible", StringComparison.Ordinal) ? "" : row + "\n");
        }
        string output = Path.Combine(w.Path, "merged"); AuxiliaryCommands.Merge([Path.Combine(w.Path, "old"), Path.Combine(w.Path, "new")], output);
        Assert.Equal(File.ReadAllText(Path.Combine(w.Path, "new/manifests/locked_input_manifest.jsonl")), File.ReadAllText(Path.Combine(output, "locked_input_manifest.jsonl")));
        Assert.Empty(File.ReadAllText(Path.Combine(output, "technical_eligible_manifest.jsonl"))); Assert.Empty(File.ReadAllText(Path.Combine(output, "strict_eligible_manifest.jsonl")));
    }
    [Fact]
    public void SummarizeChecksAgreementBeforeWritingAndRemovesLocalPaths()
    {
        using var w = new TestWorkspace(); string run = Path.Combine(w.Path, "run"), output = Path.Combine(w.Path, "summary");
        w.File("run/provenance.json", "{\"run_status\":\"COMPLETED\",\"classes_selected\":1,\"input_root\":\"C:/private\",\"previous_run\":\"C:/private/old-run\"}");
        w.File("run/reports/summary.json", "{\"total\":1,\"technical_eligible\":0,\"by_status\":{\"SOURCE_INVALID\":1}}\n");
        var result = new PreflightResult { TaskId = "a", SourceJsonPath = "1/1.json", SourceJsonSha256 = "hash", SourceJsonPaths = ["1/1.json"], SourceJsonSha256s = ["hash"], RepoUrl = "repo" };
        ManifestWriter.WriteJsonl(Path.Combine(run, "reports/preflight_results.jsonl"), new[] { result });
        AuxiliaryCommands.Summarize(run, output);
        Assert.Equal(File.ReadAllBytes(Path.Combine(run, "reports/summary.json")), File.ReadAllBytes(Path.Combine(output, "summary.json"))); Assert.DoesNotContain("C:/private", File.ReadAllText(Path.Combine(output, "run_stats.json")));
        File.AppendAllText(Path.Combine(run, "reports/preflight_results.jsonl"), PythonJson.Serialize(result) + "\n");
        string bad = Path.Combine(w.Path, "bad-summary"); Assert.Throws<InvalidDataException>(() => AuxiliaryCommands.Summarize(run, bad)); Assert.False(Directory.Exists(bad));
    }
    [Fact]
    public void CacheMaintenanceRefusesPathsOutsideWorkspace()
    {
        using var w = new TestWorkspace();
        var config = new PreflightConfig { Tools = new() { MavenRepoLocal = Path.GetPathRoot(w.Path)! } };
        Assert.Throws<InvalidDataException>(() => RunMaintenance.TrimBuildCache(config));
    }
    [Fact]
    public void LegacyConfigurationConvertsTablesAndLiteralWindowsPaths()
    {
        using var w = new TestWorkspace(); string toml = w.File("config.toml", "[run]\nworkers = 2 # comment\n[input]\nroot = 'D:\\data'\n[java]\ndefault_home = \"D:/java\"\n[java.homes]\n\"17\" = 'D:\\jdk-17'\n");
        string output = Path.Combine(w.Path, "config.json"); AuxiliaryCommands.ConvertConfig(toml, output); var config = ConfigLoader.Load(output);
        Assert.Equal(2, config.Run.Workers); Assert.Equal("D:\\data", config.Datasets["classes2test"].Root); Assert.Equal("D:\\jdk-17", config.Java.Homes["17"]);
    }
}
