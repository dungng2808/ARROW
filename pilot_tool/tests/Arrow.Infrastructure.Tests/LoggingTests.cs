using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Infrastructure.Build;
using Arrow.Infrastructure.Report;

namespace Arrow.Infrastructure.Tests;

/// <summary>specs/08 Mục 7: che khóa, dừng giữa lúc ghi, cắt log lớn, log của bước đạt trong gói export.</summary>
public class LoggingTests
{
    [Fact]
    public void LargeBuildLogKeepsHeadAndTail()
    {
        var body = "HEAD" + new string('x', 6 * 1024 * 1024) + "TAIL";
        var cut = BuildRunner.TruncateLog(body);
        Assert.StartsWith("HEAD", cut);
        Assert.EndsWith("TAIL", cut);
        Assert.Contains("bytes omitted", cut);
        Assert.True(cut.Length < 4 * 1024 * 1024 + 100);
        Assert.Equal("small", BuildRunner.TruncateLog("small"));
    }

    [Fact]
    public void ExportKeepsFailedLogsAndTrimsPassedOnes()
    {
        var lines = Enumerable.Range(1, 500).Select(i => $"line {i}");
        var passed = "$ mvn test\ncwd: /x\njdk: 17\n--- stdout+stderr ---\n" + string.Join('\n', lines) + "\n--- end ---\nexit: 0 · duration: 1.00s · timeout: false\n";
        var trimmed = BundleService.TrimPassedLog(passed);
        Assert.StartsWith("$ mvn test", trimmed);
        Assert.DoesNotContain("line 1\n", trimmed);
        Assert.Contains("line 500", trimmed);
        var failed = passed.Replace("exit: 0 ·", "exit: 1 ·");
        Assert.Equal(failed, BundleService.TrimPassedLog(failed));
    }

    [Fact]
    public void JobWithoutLedgerLineRunsAgainAndTempFilesAreIgnored()
    {
        var store = Env.NewStore("logging");
        store.EnsureCreated();
        var done = new JobKey { Dataset = "d", TaskId = "t1", Stage = "generate", Model = "m", Strategy = "zero_shot" };
        var interrupted = done with { TaskId = "t2" };
        store.AppendLedger(new LedgerEntry { Job = done, Status = JobStatus.Done, Outcome = "VALID" });
        // Máy tắt giữa lúc ghi: dòng ledger dở và file tạm sót lại.
        File.AppendAllText(store.LedgerPath, "{\"job\":{\"dataset\":\"d\",\"task_id\":\"t2\"");
        File.WriteAllText(store.StatusPath + ".123.1.tmp", "{ broken");
        var latest = store.LatestByJob();
        Assert.True(RunStore.IsFinished(latest.GetValueOrDefault(done.Canonical), rerunInfra: false));
        Assert.False(RunStore.IsFinished(latest.GetValueOrDefault(interrupted.Canonical), rerunInfra: false));
        Assert.Null(store.ReadStatus());
    }

    [Fact]
    public void ApiKeysAreMaskedInResponsesAndLogs() =>
        Assert.Equal("key sk-*** and sk-***", ToolLog.Redact("key sk-abcdefghij1234 and sk-ZZZZ_9999-yyyy"));
}
