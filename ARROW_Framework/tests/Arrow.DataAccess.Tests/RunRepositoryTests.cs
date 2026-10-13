using Arrow.DataAccess.Common;
using Arrow.DataAccess.Files;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Run;

namespace Arrow.DataAccess.Tests;

public class RunRepositoryTests
{
    [Fact]
    public void LedgerEventsStatusControlAndLock()
    {
        using var temp = new TempFramework();
        var runs = new FileRunRepository(temp.Paths);
        Assert.False(runs.Exists("r1"));
        runs.EnsureCreated("r1");
        runs.WriteLock("r1", new RunLock { RunId = "r1", ExperimentSha256 = "abc", PromptSha256 = new() { ["repair/system.md"] = "123" } });
        runs.AppendLedger("r1", new LedgerEntry { Block = 1, TaskId = "t1", Job = "analyze", Outcome = OutcomeKind.Ok });
        runs.AppendLedger("r1", new LedgerEntry { Block = 3, TaskId = "t1", Job = "chain/m/zero_shot/adaptive", Outcome = OutcomeKind.Infra, Code = ErrorCodes.B3Timeout, NeedsRerun = true });
        for (var i = 0; i < 5; i++)
        {
            runs.AppendEvent("r1", new RunEvent { Block = 1, Message = $"event {i}" });
        }

        runs.WriteStatus("r1", new RunStatus { RunId = "r1", State = RunState.Running, Blocks = [new BlockProgress { Block = 1, Done = 3, Total = 9 }] });
        runs.WriteControl("r1", new ControlCommand { Action = ControlAction.Pause, Source = "test" });

        Assert.Equal("123", runs.ReadLock("r1")!.PromptSha256["repair/system.md"]);
        var ledger = runs.ReadLedger("r1");
        Assert.Equal(2, ledger.Count);
        Assert.True(ledger[1].NeedsRerun);
        Assert.Equal(["event 3", "event 4"], runs.ReadEvents("r1", 2).Select(e => e.Message));
        Assert.Equal(3, runs.ReadStatus("r1")!.Blocks[0].Done);
        Assert.Equal(ControlAction.Pause, runs.ReadControl("r1")!.Action);
        Assert.Contains("\"action\": \"pause\"", File.ReadAllText(Path.Combine(runs.RunRoot("r1"), "control.json")));
        runs.ClearControl("r1");
        Assert.Null(runs.ReadControl("r1"));

        var info = Assert.Single(runs.ListRuns());
        Assert.Equal("r1", info.RunId);
        Assert.Equal(RunState.Running, info.Status!.State);
    }

    [Fact]
    public void EventsAreRedactedBeforeWriting()
    {
        using var temp = new TempFramework();
        var runs = new FileRunRepository(temp.Paths);
        runs.AppendEvent("r", new RunEvent { Message = "Authorization: Bearer abcdefghijklmnopqrstuvwxyz api_key: sk-zzzzzzzzzzzzzzzz9999" });
        var text = File.ReadAllText(Path.Combine(runs.RunRoot("r"), "events.jsonl"));
        Assert.DoesNotContain("abcdefghijklmnop", text);
        Assert.DoesNotContain("zzzzzzzzzzzz", text);
    }

    [Fact]
    public void TaskResultsSaveLoadDeleteAndRejectEscapingPaths()
    {
        using var temp = new TempFramework();
        var results = new FileTaskResultRepository(temp.Paths);
        var task = new TaskKey("classes2test", "555001_ab12");
        results.SaveJson("r", task, ResultPaths.Metadata, new RepositoryMetadata { TaskId = "555001_ab12", BuildTool = new() { Kind = "maven", BuildMode = BuildMode.Reactor } });
        results.SaveText("r", task, ResultPaths.Join(ResultPaths.RoundDir("gpt-6-luna", "zero_shot", "adaptive", 2), ResultPaths.CandidateCode), "class A {}");

        Assert.Equal(BuildMode.Reactor, results.LoadJson<RepositoryMetadata>("r", task, ResultPaths.Metadata)!.BuildTool.BuildMode);
        Assert.Equal("class A {}", results.LoadText("r", task, "block3/gpt-6-luna/zero_shot/adaptive/round_2/candidate.java"));
        Assert.True(results.Exists("r", task, "block3/gpt-6-luna/zero_shot/adaptive"));
        Assert.Equal([task], results.ListTasks("r"));

        results.Delete("r", task, ResultPaths.ChainDir("gpt-6-luna", "zero_shot", "adaptive"));
        Assert.False(results.Exists("r", task, "block3/gpt-6-luna/zero_shot/adaptive"));
        Assert.Null(results.LoadJson<RepositoryMetadata>("r", task, "block1/none.json"));

        Assert.Throws<ArgumentException>(() => results.SaveText("r", task, "../../escape.txt", "x"));
        Assert.Throws<ArgumentException>(() => results.SaveText("r", task, Path.GetFullPath("/tmp/abs.txt"), "x"));
    }

    [Fact]
    public void LlmCacheStoresOriginalUsage()
    {
        using var temp = new TempFramework();
        var cache = new FileLlmCacheRepository(temp.Paths);
        var key = Hashing.Sha256Text("call+request");
        Assert.Null(cache.TryGet("r", key));
        cache.Put("r", new LlmCacheEntry
        {
            Key = key,
            CallId = "repair/t/m/zero_shot/adaptive/round_1",
            Response = new LlmResponse { Text = "class X {}", Usage = new LlmUsage { LatencySeconds = 8.7, PromptTokens = 2410, CompletionTokens = 980, CostUsd = 0.00037 } },
        });
        var entry = cache.TryGet("r", key)!;
        Assert.Equal(8.7, entry.Response.Usage.LatencySeconds);
        Assert.Equal(980, entry.Response.Usage.CompletionTokens);
        Assert.Throws<ArgumentException>(() => cache.TryGet("r", "../x"));
    }

    [Fact]
    public void ReportsAndLogs()
    {
        using var temp = new TempFramework();
        var reports = new FileReportRepository(temp.Paths);
        reports.WriteCsv("r", "rq1_verification.csv", new CsvTable(["dataset", "valid"], [["classes2test", "3"]]));
        reports.WriteText("r", "report.md", "# Báo cáo\n");
        Assert.Equal("dataset,valid\nclasses2test,3\n", File.ReadAllText(Path.Combine(reports.ReportsDir("r"), "rq1_verification.csv")));

        var at = new DateTimeOffset(2026, 10, 14, 10, 0, 0, TimeSpan.FromHours(7));
        ILogRepository log = new FileLogRepository(temp.Paths, verbose: false, clock: () => at);
        Redactor.Register("sk-registered-secret-777777");
        log.Info("gọi model với khóa sk-registered-secret-777777");
        log.Debug("không ghi khi không verbose");
        log.Error("lỗi", new InvalidOperationException("boom"));
        var file = Directory.GetFiles(temp.Paths.LogsDir).Single();
        var text = File.ReadAllText(file);
        Assert.Contains("[INF] gọi model với khóa ***7777", text);
        Assert.DoesNotContain("registered-secret", text);
        Assert.DoesNotContain("không ghi khi không verbose", text);
        Assert.Contains("[ERR] lỗi", text);
        Assert.Contains("InvalidOperationException: boom", text);
    }

    [Fact]
    public void RedactorPatterns()
    {
        Assert.Equal("***", Redactor.Mask("abc"));
        Assert.Equal("***wxyz", Redactor.Mask("abcdefwxyz"));
        var text = Redactor.Redact("api_key: abc123secretvalue\n\"master_key\": \"sk-local-deadbeefdeadbeef\" x-api-key=QQQQQQQQ");
        Assert.DoesNotContain("abc123secretvalue", text);
        Assert.DoesNotContain("deadbeefdeadbeef", text);
        Assert.DoesNotContain("QQQQQQQQ", text);
        Assert.Equal("nothing secret here https://github.com/acme/shop", Redactor.Redact("nothing secret here https://github.com/acme/shop"));
    }
}
