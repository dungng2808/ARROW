using System.Formats.Tar;
using System.Text;
using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Build;
using Preflight.Service.Core.Git;
using Preflight.Service.Core.JavaAst;
using Preflight.Service.Core.Revision;
using Preflight.Service.Core.Runner;
using Preflight.Service.Datasets;
using Preflight.Service.Datasets.CatLm;
using Preflight.Service.Datasets.GitBug;
using Preflight.Service.External;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class IntegrationTests
{
    private sealed class DiskFullGitRunner : IProcessRunner
    {
        public Task<BuildAttempt> RunAsync(IReadOnlyList<string> command, string workingDirectory, double timeoutSeconds, string logPath, IReadOnlyDictionary<string, string>? environment = null, CancellationToken cancellationToken = default)
        {
            Directory.CreateDirectory(Path.GetDirectoryName(logPath)!); File.WriteAllText(logPath, "\n\nSTDOUT\n\n\nSTDERR\nfatal: No space left on device\n");
            return Task.FromResult(new BuildAttempt("", workingDirectory, command, 128, false, 0, logPath));
        }
    }
    [Fact]
    public async Task GitDiskFullIsInfrastructureEvenForUncheckedCommands()
    {
        using var w = new TestWorkspace(); var git = new GitCli(w.Path, new DiskFullGitRunner());
        await Assert.ThrowsAsync<InfrastructureFailure>(() => git.RunAsync(null, ["clone", "fixture"], false));
    }
    private const string ClassPath = "src/main/java/p/Calculator.java", TestPath = "src/test/java/p/CalculatorTest.java";
    private static PreflightConfig Config => new()
    {
        Run = new() { Workers = 2, BuildTimeoutSeconds = 180, Offline = true }, Disk = new() { MinFreeGb = 0, ResumeFreeGb = 0 },
        Java = new() { DefaultHome = Path.GetFullPath(Path.Combine(TestWorkspace.Root, "../Java-version/runtime/jdk-17")) },
        Tools = new() { MavenHome = Path.GetFullPath(Path.Combine(TestWorkspace.Root, "../Java-version/.local/tools/apache-maven-3.9.9")), GradleHome = Path.GetFullPath(Path.Combine(TestWorkspace.Root, "../Java-version/.local/tools/gradle-8.10.2")), MavenRepoLocal = Path.Combine(TestWorkspace.Root, ".cache/m2"), GradleUserHome = Path.Combine(TestWorkspace.Root, ".cache/gradle") }
    };
    private static async Task<(CandidateTask Task, string Repo, GitCli Git)> Repository(TestWorkspace w, string tool = "maven", string framework = "junit4", bool multi = false)
    {
        string repo = Path.Combine(w.Path, "original"), prefix = "original/" + (multi ? "module/" : "");
        w.File(prefix + ClassPath, "package p; public class Calculator { public int add(int x, int y) { return x+y; } }");
        string annotation = framework == "junit5" ? "org.junit.jupiter.api.Test" : framework == "testng" ? "org.testng.annotations.Test" : "org.junit.Test";
        w.File(prefix + TestPath, $"package p; import {annotation}; public class CalculatorTest {{ @Test public void adds() {{ new Calculator().add(1,2); }} }}");
        string dependency = framework == "junit5" ? "<groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter-api</artifactId><version>5.10.2</version>" : framework == "testng" ? "<groupId>org.testng</groupId><artifactId>testng</artifactId><version>7.10.2</version>" : "<groupId>junit</groupId><artifactId>junit</artifactId><version>4.13.2</version>";
        if (tool == "maven")
        {
            string pom = "<project><modelVersion>4.0.0</modelVersion><groupId>fixture</groupId><artifactId>calc</artifactId><version>1</version><properties><maven.compiler.release>17</maven.compiler.release></properties><dependencies><dependency>" + dependency + "<scope>test</scope></dependency></dependencies><build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.13.0</version></plugin></plugins></build></project>";
            w.File(prefix + "pom.xml", pom);
            if (multi) w.File("original/pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>fixture</groupId><artifactId>parent</artifactId><version>1</version><packaging>pom</packaging><modules><module>module</module></modules></project>");
        }
        else
        {
            w.File("original/settings.gradle", "rootProject.name='fixture'");
            w.File(prefix + "build.gradle", "plugins { id 'java' }\nrepositories { mavenCentral() }\ndependencies { testImplementation 'org.junit.jupiter:junit-jupiter-api:5.10.2' }\n");
        }
        var git = new GitCli(w.Path); Directory.CreateDirectory(repo);
        await git.RunAsync(repo, ["init", "-b", "main"]); await git.RunAsync(repo, ["add", "."]);
        await git.RunAsync(repo, ["-c", "user.name=Fixture", "-c", "user.email=fixture@example.test", "commit", "-m", "fixture"]);
        string sha = (await git.RunAsync(repo, ["rev-parse", "HEAD"])).Output.Trim();
        string evidence = w.File("dataset/1/1.json", PythonJson.Serialize(new { Repository = new { Url = repo }, FocalClass = new { Identifier = "Calculator", File = (multi ? "module/" : "") + ClassPath }, TestClass = new { Identifier = "CalculatorTest", File = (multi ? "module/" : "") + TestPath }, FocalMethod = new { Body = "return x+y;" }, TestCase = new { Body = "new Calculator().add(1,2);" } }));
        var candidate = new ClassCandidate("fixture00000001", "1", repo, (multi ? "module/" : "") + ClassPath, "Calculator", "Calculator", ["1/1.json"], [RuntimePaths.Hash(evidence)], [(multi ? "module/" : "") + TestPath]);
        return (new(candidate, "classes2test", new(repo, repo), new(sha, "dataset_record", RevisionStatus.UPSTREAM_PINNED), new("dataset_record")), repo, git);
    }
    [Theory]
    [InlineData("maven", "junit4", false)]
    [InlineData("maven", "junit5", false)]
    [InlineData("maven", "testng", false)]
    [InlineData("maven", "junit4", true)]
    [InlineData("gradle", "junit5", false)]
    public async Task RealJavaBuildAndProbeThroughEntirePipeline(string tool, string framework, bool multi)
    {
        using var w = new TestWorkspace(); var (task, repo, git) = await Repository(w, tool, framework, multi);
        string run = Path.Combine(w.Path, "run"); Directory.CreateDirectory(run);
        var result = await new PreflightPipeline(run, Path.Combine(w.Path, "dataset"), Config).RunAsync(task, 1, default);
        if (result.PreflightStatus != PreflightStatus.ELIGIBLE)
        {
            string logs = string.Join("\n", result.LogPaths.Where(File.Exists).Select(File.ReadAllText));
            Assert.Fail(PythonJson.Serialize(result) + "\n" + logs);
        }
        Assert.True(result.TechnicalEligible); Assert.True(result.StrictEligible); Assert.Equal("PASSED", result.ProbeStatus);
        Assert.Equal(task.PinnedRevision!.CheckoutSha, result.CheckoutSha); Assert.Equal("p.Calculator", result.ClassFqn);
        Assert.Contains(result.BuildAttempts, a => a.Stage == "main_compile"); Assert.Contains(result.BuildAttempts, a => a.Stage == "test_compile"); Assert.Contains(result.BuildAttempts, a => a.Stage == "probe_compile");
        Assert.False(Directory.Exists(Path.Combine(run, "workspaces", task.Candidate.TaskId)));
        ManifestWriter.WriteJson(Path.Combine(TestWorkspace.Root, ".work/verification/integration", $"{tool}-{framework}-{multi}.json"), result);
        Assert.Empty((await git.RunAsync(repo, ["status", "--porcelain"])).Output.Trim());
    }
    [Fact]
    public async Task MultipleRepositoryMirrorsPrepareConcurrently()
    {
        using var w = new TestWorkspace(); var (_, repo, git) = await Repository(w); string run = Path.Combine(w.Path, "run"); Directory.CreateDirectory(run);
        var mirrors = new MirrorManager(run, git); var paths = new System.Collections.Concurrent.ConcurrentBag<string>();
        await Parallel.ForEachAsync(Enumerable.Range(0, 16), new ParallelOptions { MaxDegreeOfParallelism = 5 }, async (i, ct) =>
        {
            var source = new RepoSource("fixture://repository/" + i, repo); string path = await mirrors.EnsureAsync(source, true, ct);
            Assert.Equal(path, await mirrors.EnsureAsync(source, true, ct)); paths.Add(path);
            Assert.Equal("true", (await git.RunAsync(path, ["rev-parse", "--is-bare-repository"], true, ct)).Output.Trim());
        });
        Assert.Equal(16, paths.Distinct(StringComparer.OrdinalIgnoreCase).Count());
    }
    [Fact]
    public async Task CleanupRefusalRecordsFailureAndPreservesCompletedTask()
    {
        using var w = new TestWorkspace(); var (task, repo, _) = await Repository(w); string run = Path.Combine(w.Path, "run"); Directory.CreateDirectory(run);
        task = task with { Repo = task.Repo with { Kind = "snapshot", SnapshotPath = repo } };
        var config = Config with { Run = Config.Run with { FastMode = true } };
        using var checkpoint = new CheckpointStore(Path.Combine(run, "state/run_state.sqlite"));
        checkpoint.Initialize(new(config, "fixture", Path.Combine(w.Path, "dataset"), [task], "fixture", "fixture"));
        string status = await new RunScheduler(run, config, checkpoint, new(run, Path.Combine(w.Path, "dataset"), config)).RunAsync([task], Path.Combine(w.Path, "dataset"), default);
        Assert.Equal("COMPLETED", status); Assert.Equal(PreflightStatus.PRECHECKED, Assert.Single(checkpoint.Results()).PreflightStatus);
        Assert.Equal("FAILED", Assert.Single(checkpoint.CleanupEvents()).GetProperty("status").GetString()); Assert.True(Directory.Exists(Path.Combine(repo, ".git")));
    }
    [Fact]
    public async Task GitChecksOutAndCleansPathsLongerThanWindowsLegacyLimit()
    {
        using var w = new TestWorkspace(); var (task, repo, git) = await Repository(w);
        string relative = string.Join('/', Enumerable.Repeat(new string('x', 55), 5)) + "/Long.java";
        w.File("original/" + relative, "public class Long {}"); await git.RunAsync(repo, ["add", "."]);
        await git.RunAsync(repo, ["-c", "user.name=Fixture", "-c", "user.email=fixture@example.test", "commit", "-m", "long path"]);
        string sha = (await git.RunAsync(repo, ["rev-parse", "HEAD"])).Output.Trim();
        string run = Path.Combine(w.Path, "run"); Directory.CreateDirectory(run); var mirrors = new MirrorManager(run, git);
        string mirror = await mirrors.EnsureAsync(task.Repo, true, default); var worktrees = new WorktreeManager(run, git, mirrors);
        string workspace = await worktrees.CreateAsync(task.Repo.RepoUrl, mirror, task.Candidate.TaskId, sha, default);
        Assert.True(Path.Combine(workspace, relative).Length > 260); Assert.True(File.Exists(Path.Combine(workspace, relative)));
        await worktrees.RemoveAsync(task.Repo.RepoUrl, mirror, task.Candidate.TaskId); Assert.False(Directory.Exists(workspace));
    }
    [Fact]
    public async Task CorruptMirrorIsRecreatedAndDeletionIsConfined()
    {
        using var w = new TestWorkspace(); var (task, _, git) = await Repository(w); string run = Path.Combine(w.Path, "run"); Directory.CreateDirectory(run);
        string mirror = await new MirrorManager(run, git).EnsureAsync(task.Repo, true, default);
        File.WriteAllText(Path.Combine(mirror, "HEAD"), "invalid head\n");
        string repaired = await new MirrorManager(run, git).EnsureAsync(task.Repo, true, default);
        Assert.Equal(task.PinnedRevision!.CheckoutSha, (await git.RunAsync(repaired, ["rev-parse", "HEAD"])).Output.Trim());
        Assert.Throws<InvalidDataException>(() => SafeFiles.DeleteTree(w.Path, Path.Combine(w.Path, "run")));
        Assert.Throws<InvalidDataException>(() => SafeFiles.DeleteTree(run, run));
    }
    [Fact]
    public async Task ServiceFinalizesResumesAndRerunsWithV2()
    {
        using var w = new TestWorkspace(); var (_, _, _) = await Repository(w); string run = Path.Combine(w.Path, "run"); var service = new PreflightService();
        await service.NormalizeAsync("classes2test", Config, run, Path.Combine(w.Path, "dataset"), 0, default);
        Assert.Equal(0, await service.RunAsync(run, false, 1, true, null, null, 0, default));
        Assert.Equal(0, await service.RunAsync(run, true, 2, false, null, null, 0, default));
        using (var checkpoint = new CheckpointStore(Path.Combine(run, "state/run_state.sqlite"))) { Assert.Equal(2, checkpoint.Sessions().Count); Assert.Equal(PreflightStatus.PRECHECKED, Assert.Single(checkpoint.Results()).PreflightStatus); }
        AuxiliaryCommands.Summarize(run, Path.Combine(w.Path, "summary"));
        string rerun = Path.Combine(w.Path, "rerun"); Assert.Equal(0, await AuxiliaryCommands.RerunAsync(run, rerun, [], false, 1, default));
        var result = Assert.Single(AuxiliaryCommands.ReadRows(Path.Combine(rerun, "reports/preflight_results.jsonl")));
        Assert.Equal("ELIGIBLE", result.GetProperty("preflight_status").GetString()); Assert.Equal("v2", result.GetProperty("probe_profile").GetString());
        Assert.True(result.GetProperty("technical_eligible").GetBoolean()); Assert.False(result.GetProperty("strict_eligible").GetBoolean());
        Assert.Single(AuxiliaryCommands.ReadRows(Path.Combine(rerun, "merged/technical_eligible_manifest.jsonl")));
    }
    [Fact]
    public async Task RevisionEvidenceSelectsContentMatchAndPinnedMissingFails()
    {
        using var w = new TestWorkspace(); var (task, _, git) = await Repository(w); string run = Path.Combine(w.Path, "run"); Directory.CreateDirectory(run);
        var mirror = await new MirrorManager(run, git).EnsureAsync(task.Repo, true, default);
        var choice = await new RevisionResolver(git).ChooseAsync(mirror, task with { PinnedRevision = null }, Path.Combine(w.Path, "dataset"), Config, default);
        Assert.Equal(RevisionStatus.CONTENT_MATCHED, choice.Status); Assert.Equal(task.PinnedRevision!.CheckoutSha, choice.CheckoutSha);
        await Assert.ThrowsAsync<KeyNotFoundException>(() => new RevisionResolver(git).ChooseAsync(mirror, task with { PinnedRevision = new(new('a', 40), "dataset_record", RevisionStatus.UPSTREAM_PINNED) }, w.Path, Config, default));
    }
    [Fact]
    public async Task DiskGuardResumeTamperDetectionAndFinalization()
    {
        using var w = new TestWorkspace(); var (task, _, _) = await Repository(w);
        string run = Path.Combine(w.Path, "run"), input = Path.Combine(w.Path, "dataset"); var service = new PreflightService();
        var config = Config with { Disk = Config.Disk with { MinFreeGb = 1e9, ResumeFreeGb = 1e9 } };
        await service.NormalizeAsync("classes2test", config, run, input, 0, default);
        int code = await service.RunAsync(run, false, 1, true, null, null, 0, default); Assert.Equal(75, code);
        using (var checkpoint = new CheckpointStore(Path.Combine(run, "state/run_state.sqlite"))) { Assert.Equal(1, checkpoint.Counts()["pending"]); Assert.Empty(checkpoint.Results()); }
        string candidates = Path.Combine(run, "manifests/class_candidates.jsonl"), original = File.ReadAllText(candidates); File.AppendAllText(candidates, "\n");
        await Assert.ThrowsAsync<InvalidDataException>(() => service.RunAsync(run, true, 1, false, null, null, 0, default)); File.WriteAllText(candidates, original, new UTF8Encoding(false));
        string source = Path.Combine(input, task.Candidate.SourceJsonPath); File.AppendAllText(source, "\n");
        await Assert.ThrowsAsync<InvalidDataException>(() => service.RunAsync(run, true, 1, false, null, null, 0, default));
    }
    [Fact]
    public async Task CheckpointRecoversAbandonedAttemptAndPreventsConcurrentRuns()
    {
        using var w = new TestWorkspace(); var (task, _, _) = await Repository(w);
        string path = Path.Combine(w.Path, "checkpoint.sqlite");
        using (var store = new CheckpointStore(path)) { store.Initialize(new(Config, "fixture", w.Path, [task], "fingerprint", "manifest")); long session = store.BeginSession(1); store.Start(task.Candidate.TaskId, session); }
        using (var store = new CheckpointStore(path))
        {
            store.Recover(); Assert.Equal(1, store.Counts()["pending"]); long session = store.BeginSession(2); Assert.Equal(2, store.Start(task.Candidate.TaskId, session));
            store.Interrupt(task.Candidate.TaskId, 2); Assert.Equal(1, store.Counts()["pending"]); Assert.Equal("ABANDONED", store.Sessions()[0]["status"]);
            int attempt = store.Start(task.Candidate.TaskId, session);
            store.Complete(task.Candidate.TaskId, attempt, new() { TaskId = task.Candidate.TaskId, RepoUrl = task.Repo.RepoUrl, SourceJsonPath = task.Candidate.SourceJsonPath, SourceJsonSha256 = task.Candidate.SourceJsonSha256, SourceJsonPaths = task.Candidate.SourceJsonPaths, SourceJsonSha256s = task.Candidate.SourceJsonSha256s, PreflightStatus = PreflightStatus.CLONE_FAILED });
            Assert.Equal(1, store.Reopen("CLONE_FAILED", "2020-01-01T07:00:00+07:00", "simulated disk-full evidence")); Assert.Empty(store.Results()); Assert.Equal(1, store.Counts()["pending"]);
        }
        string file = Path.Combine(w.Path, "run.lock"); using (var held = new RunLock(file)) Assert.Throws<IOException>(() => new RunLock(file)); using var released = new RunLock(file);
    }
    [Fact]
    public async Task CatLmArchiveUsesHeadAndRecordsDirtySnapshot()
    {
        using var w = new TestWorkspace(); var (_, repo, git) = await Repository(w); await git.RunAsync(repo, ["remote", "add", "origin", "https://github.com/fixture/calc.git"]);
        w.File("original/" + ClassPath, "// dirty snapshot\npackage p; public class Calculator { public int add(int x,int y){ return -1; }}");
        string archive = Path.Combine(w.Path, "fixture.tar");
        using (var stream = File.Create(archive)) using (var writer = new TarWriter(stream))
        {
            foreach (string file in Directory.EnumerateFiles(repo, "*", SearchOption.AllDirectories)) writer.WriteEntry(file, "TestData/RawData/java/fixture/calc/" + Path.GetRelativePath(repo, file).Replace('\\', '/'));
            var pairs = new { CodeFilename = new[] { "/data/GitHubMining/RawDataSample/java/fixture/calc/" + ClassPath }, TestFilename = new[] { "/data/GitHubMining/RawDataSample/java/fixture/calc/" + TestPath } };
            using var data = new MemoryStream(Encoding.UTF8.GetBytes(PythonJson.Serialize(pairs))); writer.WriteEntry(new PaxTarEntry(TarEntryType.RegularFile, "TestData/RawDataOutput/java/FilePairs/filepairs_fixture__calc.json") { DataStream = data });
        }
        string run = Path.Combine(w.Path, "run"); var result = await new CatLmAdapter().NormalizeAsync(new() { Archive = archive }, new(run, true));
        var task = Assert.Single(result.Tasks); Assert.Equal(RevisionStatus.UNVERIFIED, task.PinnedRevision!.Status); Assert.Contains(ClassPath, task.Extra!["catlm.snapshot_worktree_diff"]); Assert.Equal("CATLM_PAIR_FILE_MODIFIED_IN_SNAPSHOT", task.Extra["revision_warning"]);
        var mirror = await new MirrorManager(run, new GitCli(run)).EnsureAsync(task.Repo, true, default);
        Assert.DoesNotContain("dirty snapshot", (await new GitCli(run).ShowAsync(mirror, task.PinnedRevision.CheckoutSha, ClassPath, default))!);
        Assert.Throws<InvalidDataException>(() => TarIndexer.ValidateArchivePath("../escape")); Assert.Throws<InvalidDataException>(() => TarIndexer.ValidateArchivePath("C:/escape"));
    }
    [Fact]
    public async Task RawGitBugPairsAtFixedCommitAndRejectsDeletedJava()
    {
        using var w = new TestWorkspace(); var (_, repo, git) = await Repository(w);
        const string helper = "src/main/java/p/Helper.java"; w.File("original/" + helper, "package p; public class Helper {} "); await git.RunAsync(repo, ["add", "."]);
        await git.RunAsync(repo, ["-c", "user.name=Fixture", "-c", "user.email=fixture@example.test", "commit", "-m", "manual label fixture"]);
        string sha = (await git.RunAsync(repo, ["rev-parse", "HEAD"])).Output.Trim();
        w.File("bugs/data/bugs/repo.json", PythonJson.Serialize(new { Repository = "fixture/calc", CloneUrl = repo, CommitHash = sha, BugPatch = "diff --git a/" + ClassPath + " b/" + ClassPath + "\n--- a/" + ClassPath + "\n+++ b/" + ClassPath + "\n@@ -1 +1 @@\n-old\n+new\ndiff --git a/" + helper + " b/" + helper + "\n--- a/" + helper + "\n+++ b/" + helper + "\ndiff --git a/src/main/java/p/Gone.java b/src/main/java/p/Gone.java\n--- a/src/main/java/p/Gone.java\n+++ /dev/null\n" }) + "\n");
        string labels = w.File("labels.csv", "project_id,class_path,decision,test_class_path,labeler,reviewer,rationale\nfixture-calc-" + sha[..12] + "," + helper + ",PAIRED," + TestPath + ",fixture,reviewer,explicit fixture pairing\n");
        var result = await new GitBugAdapter().NormalizeAsync(new() { Root = Path.Combine(w.Path, "bugs"), ManualLabels = labels }, new(Path.Combine(w.Path, "run"), true));
        Assert.Equal(2, result.Tasks.Count); var actual = Assert.Single(result.Tasks, t => t.Candidate.ClassName == "Calculator"); Assert.Equal("gitbug-java", actual.Dataset); Assert.Equal("same_package", actual.Pairing.Method); Assert.Equal(repo, actual.Repo.CloneUrl); Assert.Equal(sha, actual.PinnedRevision!.CheckoutSha);
        Assert.Equal("manual", Assert.Single(result.Tasks, t => t.Candidate.ClassName == "Helper").Pairing.Method);
        Assert.Contains(result.Rejections, r => r.GetValueOrDefault("reason") == "DELETED_IN_FIX");
    }
}
