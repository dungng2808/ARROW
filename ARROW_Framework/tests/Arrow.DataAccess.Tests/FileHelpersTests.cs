using Arrow.DataAccess.Common;
using Arrow.Models.Block1;
using Arrow.Models.Common;
using Arrow.Models.Run;

namespace Arrow.DataAccess.Tests;

public class FileHelpersTests
{
    [Fact]
    public void AtomicWriteOverwritesAndLeavesNoTempFiles()
    {
        using var temp = new TempFramework();
        var path = Path.Combine(temp.Root, "a", "b.json");
        AtomicFile.WriteAllText(path, "one\r\n");
        AtomicFile.WriteAllText(path, "two\r\n");
        Assert.Equal("two\n", File.ReadAllText(path));
        Assert.Single(Directory.GetFiles(Path.GetDirectoryName(path)!));
    }

    [Fact]
    public async Task ConcurrentAtomicWritesToSameFileDoNotFail()
    {
        using var temp = new TempFramework();
        var path = Path.Combine(temp.Root, "status.json");
        await Task.WhenAll(Enumerable.Range(0, 40).Select(i => Task.Run(() => AtomicFile.WriteAllText(path, $"value {i}"))));
        Assert.StartsWith("value ", File.ReadAllText(path));
        Assert.Single(Directory.GetFiles(temp.Root, "status.json*"));
    }

    [Fact]
    public async Task JsonlAppendFromManyThreadsKeepsEveryLine()
    {
        using var temp = new TempFramework();
        var path = Path.Combine(temp.Root, "ledger.jsonl");
        await Task.WhenAll(Enumerable.Range(0, 200).Select(i => Task.Run(() =>
            JsonlFile.Append(path, new LedgerEntry { Block = 1, TaskId = $"t{i}", Outcome = OutcomeKind.Ok }))));
        var entries = JsonlFile.ReadAll<LedgerEntry>(path);
        Assert.Equal(200, entries.Count);
        Assert.Equal(200, entries.Select(e => e.TaskId).Distinct().Count());
    }

    [Fact]
    public void JsonlIgnoresHalfWrittenLastLine()
    {
        using var temp = new TempFramework();
        var path = Path.Combine(temp.Root, "events.jsonl");
        JsonlFile.Append(path, new RunEvent { Message = "a" });
        File.AppendAllText(path, "{\"message\": \"b");
        Assert.Equal("a", Assert.Single(JsonlFile.ReadAll<RunEvent>(path)).Message);
    }

    [Fact]
    public void JsonUsesSnakeCaseAndEnumNamesFromSpec()
    {
        var json = JsonFile.Serialize(new WorkspaceInfo { BuildMode = BuildMode.Standalone, JdkMajor = 8, BuildRoot = "3rdParty/x" }, indented: false);
        Assert.Contains("\"build_mode\":\"standalone\"", json);
        Assert.Contains("\"jdk_major\":8", json);
        Assert.Contains("\"build_root\":\"3rdParty/x\"", json);

        var outcome = JsonFile.Serialize(new LedgerEntry { Outcome = OutcomeKind.Infra, NeedsRerun = true }, indented: false);
        Assert.Contains("\"outcome\":\"INFRA\"", outcome);
        Assert.Contains("\"needs_rerun\":true", outcome);

        Assert.Contains("\"NO_TEST_RUN\"", JsonFile.Serialize(PairedTestStatus.NoTestRun));
        Assert.Contains("\"REGRESSION\"", JsonFile.Serialize(VerificationState.Regression));
        Assert.Equal(VerificationState.Compile, JsonFile.Deserialize<VerificationState>("\"COMPILE\""));
    }

    [Fact]
    public void DictionaryKeysAreNotRenamed()
    {
        var json = JsonFile.Serialize(new Dictionary<string, string> { ["8"] = "/jdk8", ["MaxTokens"] = "x" }, indented: false);
        Assert.Equal("{\"8\":\"/jdk8\",\"MaxTokens\":\"x\"}", json);
    }

    [Fact]
    public void Sha256OfTextFileIgnoresLineEndings()
    {
        using var temp = new TempFramework();
        var lf = temp.Write("lf.txt", "a\nb\n");
        var crlf = temp.Write("crlf.txt", "a\r\nb\r\n");
        Assert.Equal(Hashing.Sha256TextFile(lf), Hashing.Sha256TextFile(crlf));
        Assert.Equal(64, Hashing.Sha256TextFile(lf).Length);
    }

    [Fact]
    public void CsvQuotesOnlyWhenNeeded()
    {
        var csv = CsvWriter.Format(["a", "b"], [["x", "1,2"], ["say \"hi\"", "line\nbreak"]]);
        Assert.Equal("a,b\nx,\"1,2\"\n\"say \"\"hi\"\"\",\"line\nbreak\"\n", csv);
        Assert.Throws<ArgumentException>(() => CsvWriter.Format(["a", "b"], [["only one"]]));
    }
}
