using System.Collections.Concurrent;
using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Runs;
using Arrow.Infrastructure;
using Arrow.Core.Prepare;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Java;
using Arrow.Infrastructure.Prepare;
using Arrow.Infrastructure.Processes;
using Arrow.Infrastructure.Verify;

namespace Arrow.Infrastructure.Tests;

/// <summary>
/// Môi trường integration test: JDK, Maven, Gradle, git lấy từ <c>machine.local.json</c> của máy; 3 repository mẫu
/// trong <c>tests/fixtures</c> được chép ra thư mục tạm và commit thành repository git để S0 tải như repository thật.
/// </summary>
public static class Env
{
    private static readonly Lazy<ToolPaths> LazyPaths = new(() => ToolPaths.Detect(AppContext.BaseDirectory));
    private static readonly ConcurrentDictionary<string, Lazy<FixtureRepo>> Repos = new();

    public static ToolPaths Paths => LazyPaths.Value;

    public static MachineConfig Machine =>
        MachineConfig.TryLoad(Paths.MachineConfigPath)
        ?? throw new InvalidOperationException("Integration test cần machine.local.json (chạy arrow setup trước).");

    public static ExperimentConfig Experiment =>
        ExperimentLoader.Load(Path.Combine(Paths.ExperimentsDir, "pilot-v1.json"), verifyManifests: false).Config;

    /// <summary>Thư mục tạm của cả lần chạy test (không dấu cách, ASCII).</summary>
    public static readonly string Root = Path.Combine(Path.GetTempPath(), "arrow-it-" + Environment.ProcessId);

    public static string WorkDir => Path.Combine(Root, "work");

    public static RunStore NewStore(string name) => new(Path.Combine(Root, "runs"), $"{name}-{Guid.NewGuid():N}"[..20]);

    public static GitRepositoryProvider Git => new(Machine.GitPath, WorkDir, RepoPolicy.CommitOnlyKeep);

    public static FixtureRepo Repo(string fixture) => Repos.GetOrAdd(fixture, f => new Lazy<FixtureRepo>(() => FixtureRepo.Create(f))).Value;

    public static PrepareService Prepare() => new(Git, Machine, Experiment, WorkDir);

    public static JavaAnalyzer Analyzer => new(Paths.PilotToolDir, Machine);

    public static CandidateRunner Candidates => new(Git, WorkDir, Machine, new FiveStepVerifier(Experiment, Machine, Analyzer));

    private static readonly ConcurrentDictionary<string, Lazy<Task<PreparedTask>>> Prepared = new();

    /// <summary>S0 của một task mẫu, chạy một lần cho cả lần chạy test.</summary>
    public static Task<PreparedTask> PreparedAsync(ManifestRecord record) =>
        Prepared.GetOrAdd(record.TaskId, _ => new Lazy<Task<PreparedTask>>(async () =>
        {
            var store = NewStore("prepared");
            store.EnsureCreated();
            var job = new JobKey { Dataset = record.Dataset, TaskId = record.TaskId, Stage = "prepare" };
            var result = await Prepare().RunAsync(record, store, job, _ => { }, CancellationToken.None);
            return new PreparedTask(record, store, result.Task, result.Baseline, result.Context, store.TaskDir(record.Dataset, record.TaskId));
        })).Value;
}

public sealed record PreparedTask(ManifestRecord Record, RunStore Store, TaskInfo Task, Baseline Baseline, RepoContext Context, string TaskDir);

/// <summary>Repository git tạo từ một thư mục fixture; <see cref="Url"/> dạng <c>file://</c>.</summary>
public sealed record FixtureRepo(string Name, string Dir, string Url, string Sha)
{
    public static FixtureRepo Create(string fixture)
    {
        var source = Path.Combine(Env.Paths.PilotToolDir, "tests", "fixtures", fixture);
        var dir = Path.Combine(Env.Root, "repos", "local", fixture);
        if (Directory.Exists(dir))
        {
            Directory.Delete(dir, recursive: true);
        }

        CopyDirectory(source, dir);
        var git = Env.Machine.GitPath;
        foreach (var args in new[]
                 {
                     new[] { "init", "-q", "-b", "main" },
                     ["config", "user.email", "fixture@example.com"],
                     ["config", "user.name", "fixture"],
                     ["config", "uploadpack.allowAnySHA1InWant", "true"],
                     ["add", "-A"],
                     ["commit", "-q", "-m", "fixture"],
                 })
        {
            Run(git, args, dir);
        }

        var sha = Run(git, ["rev-parse", "HEAD"], dir).Trim();
        return new FixtureRepo(fixture, dir, new Uri(dir).AbsoluteUri, sha);
    }

    public ManifestRecord Record(string taskId, string classFqn, string classPath, string modulePath, string buildTool, string framework, string javaVersion, string testClassPath = "") => new()
    {
        TaskId = taskId,
        RepoUrl = Url,
        CheckoutSha = Sha,
        ClassFqn = classFqn,
        ClassPath = classPath,
        ModulePath = modulePath,
        TestClassPath = testClassPath,
        BuildTool = buildTool,
        TestingFramework = framework,
        JavaVersion = javaVersion,
        Dataset = "fixtures",
    };

    private static string Run(string program, string[] args, string cwd)
    {
        // Ngày giờ cố định: cùng nội dung fixture cho cùng commit SHA ở mọi lần chạy test.
        var env = new Dictionary<string, string?>
        {
            ["GIT_AUTHOR_DATE"] = "2026-01-01T00:00:00Z",
            ["GIT_COMMITTER_DATE"] = "2026-01-01T00:00:00Z",
        };
        var result = ProcessRunner.RunAsync(program, args, cwd, env, TimeSpan.FromMinutes(2), CancellationToken.None).GetAwaiter().GetResult();
        if (!result.Succeeded)
        {
            throw new InvalidOperationException($"{program} {string.Join(' ', args)}: {result.Output}");
        }

        return result.StdOut;
    }

    private static void CopyDirectory(string source, string target)
    {
        Directory.CreateDirectory(target);
        foreach (var file in Directory.EnumerateFiles(source, "*", SearchOption.AllDirectories))
        {
            var relative = Path.GetRelativePath(source, file);
            if (relative.Split(Path.DirectorySeparatorChar).Any(p => p is "target" or "build" or ".gradle"))
            {
                continue;
            }

            var destination = Path.Combine(target, relative);
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            File.Copy(file, destination);
        }
    }
}

/// <summary>Task mẫu của 3 fixture (CUT, module, framework, JDK manifest).</summary>
public static class Samples
{
    public static ManifestRecord MavenJunit4 => Env.Repo("sample-maven-junit4").Record(
        "fx-maven-junit4", "com.example.calc.Calculator", "src/main/java/com/example/calc/Calculator.java", ".", "maven", "junit4",
        "openjdk version \"1.8.0_402\"", "src/test/java/com/example/calc/CalculatorTest.java");

    public static ManifestRecord MavenMultiJunit5 => Env.Repo("sample-maven-multimodule-junit5").Record(
        "fx-maven-multi-junit5", "com.example.app.Greeter", "app/src/main/java/com/example/app/Greeter.java", "app", "maven", "junit5",
        "openjdk version \"17.0.10\"", "app/src/test/java/com/example/app/GreeterTests.java");

    public static ManifestRecord GradleJunit5 => Env.Repo("sample-gradle-junit5").Record(
        "fx-gradle-junit5", "com.example.shop.Cart", "src/main/java/com/example/shop/Cart.java", ".", "gradle", "junit5",
        "openjdk version \"17.0.10\"", "src/test/java/com/example/shop/CartTest.java");
}
