using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Runs;
using Arrow.Core.Shards;
using Arrow.Core.Util;

namespace Arrow.Core.Tests;

public sealed class TempDir : IDisposable
{
    public string Path { get; } = System.IO.Path.Combine(System.IO.Path.GetTempPath(), "arrow-test-" + Guid.NewGuid().ToString("N"));

    public TempDir() => Directory.CreateDirectory(Path);

    public void Dispose()
    {
        try { Directory.Delete(Path, recursive: true); } catch (IOException) { }
    }
}

public class ManifestTests
{
    [Theory]
    [InlineData("openjdk version \"17.0.20.1\" 2026-08-18", 17)]
    [InlineData("openjdk version \"1.8.0_504\"", 8)]
    [InlineData("openjdk version \"11.0.32.1\" 2026-08-18 LTS", 11)]
    [InlineData("openjdk version \"21.0.12.1\" 2026-08-18 LTS", 21)]
    public void ParsesJdkMajor(string text, int expected) => Assert.Equal(expected, ManifestRecord.ParseJdkMajor(text));

    [Fact]
    public void KeepsUnknownFieldsAndComputesPackage()
    {
        var record = Json.Deserialize<ManifestRecord>(
            """{"task_id":"t1","repo_url":"https://github.com/a/b","checkout_sha":"abc","class_path":"src/main/java/a/B.java","class_fqn":"a.b.C","probe_status":"PASSED"}""");
        Assert.Equal("C", record.ClassName);
        Assert.Equal("a.b", record.PackageName);
        Assert.True(record.Extra!.ContainsKey("probe_status"));
    }
}

public class ShardPlannerTests
{
    private static List<ManifestRecord> Records()
    {
        var records = new List<ManifestRecord>();
        for (var i = 0; i < 20; i++)
        {
            records.Add(new ManifestRecord
            {
                TaskId = $"t{i:00}", RepoUrl = $"https://github.com/o/r{i % 13}", CheckoutSha = "x", ClassPath = "p",
                Dataset = i % 4 == 0 ? "gitbug-java" : "classes2test", DurationSeconds = 10 + i,
            });
        }

        return records;
    }

    private static LoadedExperiment Experiment() =>
        new(new ExperimentConfig { ExperimentId = "e1" }, "/tmp/e1.json", "sha-e1");

    [Fact]
    public void KeepsRepositoryInOneShardAndCoversAllTasks()
    {
        var owners = Enumerable.Range(1, 6).Select(i => new ShardOwner($"m{i}", $"Máy {i}")).ToList();
        var result = ShardPlanner.Plan(Experiment(), Records(), owners, "p6", "m1");

        Assert.Equal(6, result.Shards.Count);
        Assert.True(result.Shards.Max(s => s.Tasks.Count) - result.Shards.Min(s => s.Tasks.Count) <= 2, "số task giữa các shard phải gần đều");
        Assert.Equal(20, result.Shards.Sum(s => s.Tasks.Count));
        Assert.Equal(20, result.Shards.SelectMany(s => s.Tasks).Select(t => t.TaskId).Distinct().Count());
        foreach (var repo in result.Shards.SelectMany(s => s.Tasks).GroupBy(t => t.RepoUrl))
        {
            Assert.Single(result.Shards.Where(s => s.Tasks.Any(t => t.RepoUrl == repo.Key)));
        }
    }

    [Fact]
    public void IsDeterministicRegardlessOfInputOrder()
    {
        var owners = Enumerable.Range(1, 6).Select(i => new ShardOwner($"m{i}", $"m{i}")).ToList();
        var a = ShardPlanner.Plan(Experiment(), Records(), owners, "p6", "m1");
        var shuffled = Records().OrderBy(r => Hashing.Sha256Text(r.TaskId)).ToList();
        var b = ShardPlanner.Plan(Experiment(), shuffled, owners, "p6", "m1");
        Assert.Equal(Json.Serialize(a.Plan), Json.Serialize(b.Plan));
    }

    [Fact]
    public void WritesFilesWhoseHashesMatchPlan()
    {
        using var dir = new TempDir();
        var owners = new[] { ShardOwner.Parse("Dung-mac=Dũng-mac"), ShardOwner.Parse("Quang") };
        var result = ShardPlanner.Plan(Experiment(), Records(), owners, "p2", "Dung-mac");
        ShardPlanner.Write(result, dir.Path);

        var (shard, plan) = ShardPlanner.LoadShard(Path.Combine(dir.Path, "shard-01.json"));
        Assert.NotNull(plan);
        Assert.Equal("Dung-mac", shard.Owner);
        Assert.Equal("Dũng-mac", shard.OwnerDisplay);

        File.AppendAllText(Path.Combine(dir.Path, "shard-02.json"), " ");
        var error = Assert.Throws<ArrowException>(() => ShardPlanner.LoadShard(Path.Combine(dir.Path, "shard-02.json")));
        Assert.Equal("SHARD_HASH_MISMATCH", error.Code);
    }
}

public class LedgerTests
{
    private static JobKey Key(string task) => new() { Dataset = "classes2test", TaskId = task, Stage = "generate", Model = "gpt-6-luna", Strategy = "zero_shot" };

    [Fact]
    public void LatestEntryWinsAndBrokenLineIsIgnored()
    {
        using var dir = new TempDir();
        var store = new RunStore(dir.Path, "r1");
        store.AppendLedger(new LedgerEntry { Job = Key("a"), Status = JobStatus.Infra, Reason = "BUILD_TIMEOUT" });
        store.AppendLedger(new LedgerEntry { Job = Key("a"), Status = JobStatus.Done, Outcome = "VALID" });
        store.AppendLedger(new LedgerEntry { Job = Key("b"), Status = JobStatus.Infra });
        File.AppendAllText(store.LedgerPath, "{\"job\":{\"dataset\":\"x\""); // dòng ghi dở

        var latest = store.LatestByJob();
        Assert.Equal(JobStatus.Done, latest[Key("a").Canonical].Status);
        Assert.True(RunStore.IsFinished(latest[Key("a").Canonical], rerunInfra: true));
        Assert.True(RunStore.IsFinished(latest[Key("b").Canonical], rerunInfra: false));
        Assert.False(RunStore.IsFinished(latest[Key("b").Canonical], rerunInfra: true));
    }

    [Fact]
    public void SerializesStatusesInUpperCase()
    {
        var line = Json.Serialize(new LedgerEntry { Job = Key("a"), Status = JobStatus.Done }, indented: false);
        Assert.Contains("\"status\":\"DONE\"", line);
        Assert.Contains("\"task_id\":\"a\"", line);
    }
}

public class ControlGateTests
{
    [Fact]
    public async Task PausesUntilResumedAndRecordsPause()
    {
        using var dir = new TempDir();
        var store = new RunStore(dir.Path, "r1");
        store.EnsureCreated();
        var gate = new ControlGate(store, pollInterval: TimeSpan.FromMilliseconds(20));

        ControlGate.Request(store, ControlCommand.Pause, "test");
        var waiting = gate.WaitIfPausedAsync(CancellationToken.None);
        await Task.Delay(100);
        Assert.False(waiting.IsCompleted);

        await Task.Delay(5); // khác requested_at
        ControlGate.Request(store, ControlCommand.Resume, "test");
        Assert.True(await waiting.WaitAsync(TimeSpan.FromSeconds(5)));
        Assert.Single(gate.Pauses);
        Assert.Contains(store.ReadLedger(), e => e.Pause is not null);
    }

    [Fact]
    public async Task TwoWaitingWorkersRecordOnePause()
    {
        using var dir = new TempDir();
        var store = new RunStore(dir.Path, "r1");
        store.EnsureCreated();
        var gate = new ControlGate(store, pollInterval: TimeSpan.FromMilliseconds(20));
        ControlGate.Request(store, ControlCommand.Pause, "test");
        var w1 = gate.WaitIfPausedAsync(CancellationToken.None);
        var w2 = gate.WaitIfPausedAsync(CancellationToken.None);
        await Task.Delay(100);
        await Task.Delay(5);
        ControlGate.Request(store, ControlCommand.Resume, "test");
        Assert.True(await w1.WaitAsync(TimeSpan.FromSeconds(5)));
        Assert.True(await w2.WaitAsync(TimeSpan.FromSeconds(5)));
        Assert.Single(store.ReadLedger(), e => e.Pause is not null);
    }

    [Fact]
    public async Task StopEndsWaitWithFalse()
    {
        using var dir = new TempDir();
        var store = new RunStore(dir.Path, "r1");
        store.EnsureCreated();
        var gate = new ControlGate(store, pollInterval: TimeSpan.FromMilliseconds(20));

        ControlGate.Request(store, ControlCommand.Pause, "test");
        var waiting = gate.WaitIfPausedAsync(CancellationToken.None);
        await Task.Delay(50);
        await Task.Delay(5);
        ControlGate.Request(store, ControlCommand.Stop, "test");
        Assert.False(await waiting.WaitAsync(TimeSpan.FromSeconds(5)));
        Assert.True(gate.StopRequested);
    }

    [Fact]
    public async Task NewSessionIgnoresOldStop()
    {
        using var dir = new TempDir();
        var store = new RunStore(dir.Path, "r1");
        store.EnsureCreated();
        ControlGate.Request(store, ControlCommand.Stop, "test");
        var gate = new ControlGate(store);
        gate.ResetForNewSession();
        Assert.True(await gate.WaitIfPausedAsync(CancellationToken.None));
    }
}

public class ToolLogTests
{
    [Fact]
    public void RedactsApiKeys()
    {
        Assert.Equal("api_key: sk-*** done", ToolLog.Redact("api_key: sk-proj-AbC123_xyz-789 done"));
        Assert.Equal("master sk-***", ToolLog.Redact("master sk-local-0123456789abcdef"));
        Assert.Equal("sk-abc ngắn giữ nguyên", ToolLog.Redact("sk-abc ngắn giữ nguyên"));
    }

    [Fact]
    public void SinkReceivesRedactedMessageAndException()
    {
        var lines = new List<string>();
        ToolLog.Configure((level, message, exception) => lines.Add($"{level}|{message}|{exception?.Message}"));
        try
        {
            ToolLog.Error("gọi lỗi với sk-ABCDEFGHIJKL", new InvalidOperationException("khóa sk-ZZZZZZZZZZZZ sai"));
        }
        finally
        {
            ToolLog.Configure(null);
        }

        Assert.Equal("Error|gọi lỗi với sk-***|khóa sk-*** sai", Assert.Single(lines));
    }
}

/// <summary>Sửa models của cấu hình thực nghiệm: giữ trường khác, đổi giá, thêm, bỏ model.</summary>
public class ExperimentModelEditorTests
{
    private const string Experiment = """
        {
          "schema_version": 1,
          "experiment_id": "x",
          "models": [
            {
              "name": "gpt-6-luna",
              "expected_model_id_prefix": "gpt-6-luna",
              "params": { "reasoning_effort": "none", "temperature": 0 },
              "price_usd_per_million": null
            }
          ],
          "n_rule": "share95"
        }
        """;

    [Fact]
    public void SetsPriceAndKeepsOtherFields()
    {
        var json = ExperimentModelEditor.SetPrice(Experiment, "gpt-6-luna", new PriceConfig { Input = 0.05, Output = 0.25 });
        var config = Json.Deserialize<ExperimentConfig>(json);
        Assert.Equal(0.05, config.Models[0].PriceUsdPerMillion!.Input);
        Assert.Equal(0.25, config.Models[0].PriceUsdPerMillion!.Output);
        Assert.Equal("share95", config.NRule);
        Assert.Equal("x", config.ExperimentId);
        Assert.True(json.IndexOf("experiment_id", StringComparison.Ordinal) < json.IndexOf("models", StringComparison.Ordinal));
        Assert.Null(Json.Deserialize<ExperimentConfig>(ExperimentModelEditor.SetPrice(json, "gpt-6-luna", null)).Models[0].PriceUsdPerMillion);
    }

    [Fact]
    public void AddsAndRemovesModels()
    {
        var qwen = new ModelConfig
        {
            Name = "qwen3-coder-30b",
            ExpectedModelIdPrefix = "qwen3-coder",
            Params = new Dictionary<string, object> { ["temperature"] = 0 },
            PriceUsdPerMillion = new PriceConfig { Input = 0, Output = 0 },
        };
        var json = ExperimentModelEditor.AddModel(Experiment, qwen);
        var config = Json.Deserialize<ExperimentConfig>(json);
        Assert.Equal(["gpt-6-luna", "qwen3-coder-30b"], config.Models.Select(m => m.Name));
        Assert.Equal("qwen3-coder", config.Models[1].ExpectedModelIdPrefix);
        Assert.Equal("MODEL_EXISTS", Assert.Throws<ArrowException>(() => ExperimentModelEditor.AddModel(json, qwen)).Code);

        var removed = Json.Deserialize<ExperimentConfig>(ExperimentModelEditor.RemoveModel(json, "gpt-6-luna"));
        Assert.Equal("qwen3-coder-30b", Assert.Single(removed.Models).Name);
        Assert.Equal("MODEL_REQUIRED", Assert.Throws<ArrowException>(() => ExperimentModelEditor.RemoveModel(Experiment, "gpt-6-luna")).Code);
        Assert.Equal("MODEL_MISSING", Assert.Throws<ArrowException>(() => ExperimentModelEditor.SetPrice(Experiment, "nope", null)).Code);
    }
}
