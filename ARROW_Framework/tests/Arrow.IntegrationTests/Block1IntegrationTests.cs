using System.Diagnostics;
using Arrow.DataAccess;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Files;
using Arrow.Models.Block1;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Manifest;
using Arrow.Models.Run;
using Arrow.Service;
using Arrow.Service.App;
using Arrow.Service.Block1;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.IntegrationTests;

/// <summary>
/// Một fixture trong <c>tests/fixtures/</c> được chép ra thư mục tạm, tạo git repository một commit, dùng URL <c>file://</c>.
/// Kèm thư mục kết quả và work_dir riêng để mỗi test độc lập.
/// </summary>
internal sealed class FixtureRepo : IDisposable
{
    public static readonly FrameworkPaths Framework = ServiceFactory.Locate(AppContext.BaseDirectory)!;

    public string Root { get; } = Path.Combine(Path.GetTempPath(), "arrow-b1-" + Guid.NewGuid().ToString("N")[..10]);
    public string Url { get; }
    public string Sha { get; }
    public FrameworkPaths ResultPaths { get; }
    public string WorkDir { get; }

    public FixtureRepo(string fixture)
    {
        var source = Path.Combine(Framework.FrameworkRoot, "tests", "fixtures", fixture);
        var repo = Path.Combine(Root, "upstream", fixture);
        Copy(source, repo);
        Git(repo, "init", "-q");
        Git(repo, "add", "-A");
        Git(repo, "-c", "user.email=test@arrow", "-c", "user.name=arrow", "commit", "-q", "-m", "fixture");
        Sha = Git(repo, "rev-parse", "HEAD").Trim();
        Url = new Uri(repo).AbsoluteUri;
        ResultPaths = new FrameworkPaths(Path.Combine(Root, "framework"));
        WorkDir = Path.Combine(Root, "work");
    }

    private static void Copy(string source, string target)
    {
        Directory.CreateDirectory(target);
        foreach (var file in Directory.GetFiles(source, "*", SearchOption.AllDirectories))
        {
            var destination = Path.Combine(target, Path.GetRelativePath(source, file));
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            File.Copy(file, destination);
        }
    }

    private static string Git(string dir, params string[] args)
    {
        var start = new ProcessStartInfo("git") { WorkingDirectory = dir, RedirectStandardOutput = true, RedirectStandardError = true };
        foreach (var arg in args)
        {
            start.ArgumentList.Add(arg);
        }

        using var process = Process.Start(start)!;
        var output = process.StandardOutput.ReadToEnd();
        process.StandardError.ReadToEnd();
        process.WaitForExit();
        Assert.True(process.ExitCode == 0, $"git {string.Join(' ', args)} lỗi");
        return output;
    }

    /// <summary>Bản ghi manifest giống preflight: <c>working_directory</c> giả, các lần thử chạy trong thư mục con của nó.</summary>
    public TaskRecord Task(string taskId, string classPath, string fqn, string? testClassPath, string javaVersion, string framework,
        params (string Stage, string[] Command, string Dir, int Exit)[] attempts)
    {
        const string preflightRoot = "/preflight/workspaces";
        var record = new TaskRecord
        {
            Dataset = "classes2test",
            TaskId = taskId,
            RepoUrl = Url,
            CheckoutSha = Sha,
            ClassPath = classPath,
            ClassFqn = fqn,
            ModulePath = ".",
            WorkingDirectory = $"{preflightRoot}/{taskId}",
            BuildAttempts = attempts.Select(a => new BuildAttempt
            {
                Stage = a.Stage, Command = [.. a.Command], WorkingDirectory = a.Dir == "." ? $"{preflightRoot}/{taskId}" : $"{preflightRoot}/{taskId}/{a.Dir}", ExitCode = a.Exit,
            }).ToList(),
            TestClassPath = testClassPath,
            BuildTool = attempts.Any(a => a.Command[0].Contains("gradle", StringComparison.Ordinal)) ? "gradle" : "maven",
            JavaVersion = javaVersion,
            TestingFramework = framework,
            RevisionProvenance = "git_history",
            RevisionVerificationStatus = "CONTENT_MATCHED",
        };
        return record with { RawJson = JsonFile.Serialize(record with { Dataset = "" }, indented: false) };
    }

    public RunContext Context(bool requirePairedTest = false) => new()
    {
        RunId = "it",
        Tools = ToolLocator.Environment(null, Framework.ArrowRoot),
        Screening = new ScreeningConfig { RequirePairedTestPass = requirePairedTest },
        RetryDelays = [],
    };

    public FileTaskResultRepository Results => new(ResultPaths);

    public IRepositoryAnalyzer Analyzer(string? workDir = null)
    {
        IProcessRunner runner = new ProcessRunner();
        var tools = ToolLocator.Environment(null, Framework.ArrowRoot);
        IGitClient git = new GitClient(runner, tools.Git);
        return new RepositoryAnalyzer(git, new WorkspaceManager(git, runner, workDir ?? WorkDir), new BuildRunner(runner), new TestReportReader(), Results, new FileLogRepository(ResultPaths));
    }

    public void Dispose()
    {
        try
        {
            WorkspaceManager.DeleteTree(Root);
        }
        catch (IOException)
        {
        }
    }
}

public class Block1IntegrationTests
{
    private static readonly string[] MavenTestCompile = ["mvn", "-q", "-DskipTests", "test-compile"];

    private static (string, string[], string, int) Attempt(string stage, string[] command, string dir, int exit = 0) => (stage, command, dir, exit);

    [Fact]
    public async Task SubdirectoryBuildRootJUnit4()
    {
        using var repo = new FixtureRepo("maven-subdir-junit4");
        var task = repo.Task("100_subdir", "lib/src/main/java/com/acme/util/Util.java", "com.acme.util.Util", "lib/src/test/java/com/acme/util/UtilTest.java",
            "openjdk version \"17.0.20.1\" 2026-08-18 LTS", "junit4", Attempt("test_compile", MavenTestCompile, "lib"));
        var analysis = await repo.Analyzer().AnalyzeAsync(task, repo.Context(), CancellationToken.None);

        Assert.Equal(OutcomeKind.Ok, analysis.Outcome.Outcome);
        var m = analysis.Metadata!;
        Assert.Equal(("maven", "lib", BuildMode.Standalone, "lib/pom.xml"), (m.BuildTool.Kind, m.BuildTool.BuildRoot, m.BuildTool.BuildMode, m.BuildTool.BuildFile));
        Assert.Equal(("1.8", "maven.compiler.source", 8), (m.Java.Declared, m.Java.DeclaredSource, m.Java.Major));
        Assert.Equal(8, m.Java.JdkMajor);
        Assert.Equal(("junit4", "4.13.2"), (m.TestFramework.Name, m.TestFramework.Version));
        Assert.Equal("junit", Assert.Single(m.Dependencies.Test).Artifact);
        Assert.Empty(m.Dependencies.Direct);
        Assert.Equal(("lib", "lib/src/main/java", "lib/src/test/java"), (m.ProjectStructure.ModuleRoot, m.Paths.SourceRoot, m.Paths.TestRoot));
        Assert.Equal(("lib/src/test/java/com/acme/util/Util_ArrowTest.java", "com.acme.util.Util_ArrowTest"), (m.Paths.GeneratedTestPath, m.Paths.GeneratedTestFqn));
        Assert.Equal(["com.acme.util"], m.ProjectStructure.Packages);
        Assert.Empty(m.MissingReasons);

        var baseline = analysis.Baseline!;
        Assert.Equal(["com.acme.util.UtilTest#prependsZeros", "com.acme.util.UtilTest#zeroCountKeepsValue"], baseline.PassingInAllRuns);
        Assert.Equal(["com.acme.util.BrokenTest#alwaysFails"], baseline.FailingInAllRuns);
        Assert.Equal(("com.acme.util.UtilTest", PairedTestStatus.Pass), (baseline.PairedTest.Class, baseline.PairedTest.Status));
        Assert.Equal([2, 2], baseline.PairedTest.TestsRun);
        Assert.Equal("lib", baseline.WorkingDir);

        // Snapshot đã biên dịch, chưa chạy test; mọi file của khối 1 đã ghi.
        var info = analysis.Workspace!;
        var snapshot = Path.Combine(repo.WorkDir, info.CompiledSnapshot);
        Assert.True(Directory.Exists(Path.Combine(snapshot, "lib", "target", "test-classes", "com", "acme", "util")));
        Assert.False(Directory.Exists(Path.Combine(snapshot, "lib", "target", "surefire-reports")));
        Assert.False(File.Exists(Path.Combine(snapshot, "lib", BuildCommands.MavenDependencyFile)));
        Assert.Equal("manifest:test_compile", info.BuildRootSource);
        var key = new TaskKey("classes2test", "100_subdir");
        foreach (var file in new[] { ResultPaths.Task, ResultPaths.Metadata, ResultPaths.MetadataPrompt, ResultPaths.DependenciesResolved, ResultPaths.TestClasspath, ResultPaths.Workspace, ResultPaths.Baseline, ResultPaths.Block1Outcome })
        {
            Assert.True(repo.Results.Exists("it", key, file), file);
        }

        Assert.Contains("junit-4.13.2.jar", repo.Results.LoadText("it", key, ResultPaths.TestClasspath));
        Assert.Contains("- Test path: lib/src/test/java/com/acme/util/Util_ArrowTest.java", repo.Results.LoadText("it", key, ResultPaths.MetadataPrompt));
    }

    [Fact]
    public async Task ReactorModuleWithSiblingDependencyJUnit5()
    {
        using var repo = new FixtureRepo("maven-multi-junit5");
        var task = repo.Task("200_reactor", "shop-core/src/main/java/com/acme/shop/OrderService.java", "com.acme.shop.OrderService", "shop-core/src/test/java/com/acme/shop/OrderServiceTest.java",
            "openjdk version \"17.0.20.1\" 2026-08-18 LTS", "junit5", Attempt("test_compile", ["mvn", "-q", "-pl", "shop-core", "-am", "-DskipTests", "test-compile"], "."));
        var analysis = await repo.Analyzer().AnalyzeAsync(task, repo.Context(requirePairedTest: true), CancellationToken.None);

        Assert.Equal(OutcomeKind.Ok, analysis.Outcome.Outcome);
        var m = analysis.Metadata!;
        Assert.Equal((".", BuildMode.Reactor, "shop-core"), (m.BuildTool.BuildRoot, m.BuildTool.BuildMode, m.BuildTool.ReactorModule));
        Assert.Equal(("11", "maven.compiler.release", 11), (m.Java.Declared, m.Java.DeclaredSource, m.Java.JdkMajor));
        Assert.Equal("junit5", m.TestFramework.Name);
        Assert.True(m.ProjectStructure.MultiModule);
        Assert.Equal(["shop-api", "shop-core"], m.ProjectStructure.Modules);
        Assert.Equal("shop-core", m.ProjectStructure.ModuleRoot);
        Assert.Equal("shop-api", Assert.Single(m.Dependencies.Direct).Artifact);
        Assert.Equal("-pl shop-core -am", analysis.Workspace!.MavenModuleSelector);

        // Test lồng (@Nested) và @DisplayName vẫn tính cho class test người viết.
        Assert.Equal(PairedTestStatus.Pass, analysis.Baseline!.PairedTest.Status);
        Assert.Equal([3, 3], analysis.Baseline.PairedTest.TestsRun);
        Assert.Contains("com.acme.shop.OrderServiceTest$WithoutTax#keepsAmount", analysis.Baseline.PassingInAllRuns);

        // Classpath test: thư mục class của module anh em ghi tương đối với gốc workspace.
        var classpath = repo.Results.LoadText("it", new TaskKey("classes2test", "200_reactor"), ResultPaths.TestClasspath)!;
        Assert.Contains("shop-api/target/classes\n", classpath);
        Assert.Contains("junit-jupiter-api-5.10.2.jar", classpath);
    }

    [Fact]
    public async Task ModuleOutsideReactorUsesFallbackAndScreensIgnoredTests()
    {
        using var repo = new FixtureRepo("maven-multi-junit5");
        var task = repo.Task("300_outside", "samples/standalone/src/main/java/com/acme/sample/Greeter.java", "com.acme.sample.Greeter", "samples/standalone/src/test/java/com/acme/sample/GreeterTest.java",
            "openjdk version \"1.8.0_504\"", "junit4",
            Attempt("test_compile", ["mvn", "-q", "-pl", "samples/standalone", "-am", "-DskipTests", "test-compile"], ".", exit: 1),
            Attempt("test_compile_fallback", MavenTestCompile, "samples/standalone"));

        var measured = await repo.Analyzer().AnalyzeAsync(task, repo.Context(), CancellationToken.None);
        Assert.Equal(OutcomeKind.Ok, measured.Outcome.Outcome);
        Assert.Equal(("samples/standalone", BuildMode.Standalone), (measured.Metadata!.BuildTool.BuildRoot, measured.Metadata.BuildTool.BuildMode));
        Assert.Equal("manifest:test_compile_fallback", measured.Workspace!.BuildRootSource);
        Assert.Equal(PairedTestStatus.NoTestRun, measured.Baseline!.PairedTest.Status);

        // Khi chạy thực nghiệm (require_paired_test_pass = true) CUT này bị loại; workspace được xóa.
        var excluded = await repo.Analyzer().AnalyzeAsync(task, repo.Context(requirePairedTest: true), CancellationToken.None);
        Assert.Equal((OutcomeKind.Excluded, ErrorCodes.B1PairedTestNotPassing), (excluded.Outcome.Outcome, excluded.Outcome.Code));
        Assert.Contains("NO_TEST_RUN", excluded.Outcome.Message);
        Assert.False(Directory.Exists(Path.Combine(repo.WorkDir, "ws", "it", "300_outside")));
        Assert.NotNull(repo.Results.LoadJson<Baseline>("it", new TaskKey("classes2test", "300_outside"), ResultPaths.Baseline));
    }

    [Fact]
    public async Task DetectsBuildWhenManifestHasNoAttempts()
    {
        using var repo = new FixtureRepo("maven-multi-junit5");
        var task = repo.Task("400_detect", "shop-core/src/main/java/com/acme/shop/OrderService.java", "com.acme.shop.OrderService", "shop-core/src/test/java/com/acme/shop/OrderServiceTest.java",
            "openjdk version \"17.0.20.1\" 2026-08-18 LTS", "junit5");
        var analysis = await repo.Analyzer().AnalyzeAsync(task, repo.Context(), CancellationToken.None);
        Assert.Equal(OutcomeKind.Ok, analysis.Outcome.Outcome);
        Assert.Equal(("detected:reactor", ".", "shop-core"), (analysis.Workspace!.BuildRootSource, analysis.Metadata!.BuildTool.BuildRoot, analysis.Metadata.BuildTool.ReactorModule));
    }

    [Fact]
    public async Task GradleProjectJUnit5()
    {
        if (ToolLocator.FindGradle(null) is null)
        {
            return; // máy không có Gradle: bỏ qua
        }

        using var repo = new FixtureRepo("gradle-junit5");
        var task = repo.Task("500_gradle", "core/src/main/java/com/acme/calc/Calculator.java", "com.acme.calc.Calculator", "core/src/test/java/com/acme/calc/CalculatorTest.java",
            "openjdk version \"17.0.20.1\" 2026-08-18 LTS", "junit5", Attempt("test_compile", ["/preflight/gradlew", "--no-daemon", ":core:testClasses"], "."));
        var analysis = await repo.Analyzer().AnalyzeAsync(task, repo.Context(), CancellationToken.None);

        Assert.True(analysis.Outcome.Outcome == OutcomeKind.Ok, analysis.Outcome.Message);
        var m = analysis.Metadata!;
        Assert.Equal(("gradle", ".", ":core", "core"), (m.BuildTool.Kind, m.BuildTool.BuildRoot, m.BuildTool.ReactorModule, m.ProjectStructure.ModuleRoot));
        Assert.Equal(("17", 17), (m.Java.Declared, m.Java.JdkMajor));
        Assert.Equal(("junit5", "5.10.2"), (m.TestFramework.Name, m.TestFramework.Version));
        Assert.Equal(["test"], m.Dependencies.Test.Select(d => d.Scope).Distinct());
        Assert.Equal("core/src/test/java", m.Paths.TestRoot);
        Assert.Contains("core", m.ProjectStructure.Modules);
        Assert.Equal(PairedTestStatus.Pass, analysis.Baseline!.PairedTest.Status);
        Assert.Equal([1, 1], analysis.Baseline.PairedTest.TestsRun);
        Assert.Contains("junit-jupiter-api-5.10.2.jar", repo.Results.LoadText("it", new TaskKey("classes2test", "500_gradle"), ResultPaths.TestClasspath));
    }

    [Fact]
    public async Task UnusableTasksAreExcludedWithCodes()
    {
        using var repo = new FixtureRepo("maven-multi-junit5");
        var analyzer = repo.Analyzer();
        var context = repo.Context();
        var good = repo.Task("600_bad", "shop-core/src/main/java/com/acme/shop/OrderService.java", "com.acme.shop.OrderService", null,
            "openjdk version \"17.0.20.1\"", "junit5", Attempt("test_compile", ["mvn", "-q", "-pl", "shop-core", "-am", "-DskipTests", "test-compile"], "."));

        async Task<BlockOutcome> Outcome(TaskRecord task) => (await analyzer.AnalyzeAsync(task, context, CancellationToken.None)).Outcome;

        Assert.Equal(ErrorCodes.B1ManifestInvalid, (await Outcome(good with { ClassFqn = "" })).Code);
        Assert.Equal(ErrorCodes.B1CutMissing, (await Outcome(good with { ClassPath = "shop-core/src/main/java/com/acme/shop/Missing.java", ClassFqn = "com.acme.shop.Missing" })).Code);
        Assert.Equal(ErrorCodes.B1CommitMissing, (await Outcome(good with { CheckoutSha = new string('a', 40) })).Code);

        // Lệnh build standalone ở gốc repository nhưng CUT nằm trong shop-core.
        var mismatch = good with { BuildAttempts = [new BuildAttempt { Stage = "test_compile", Command = MavenTestCompile.ToList(), WorkingDirectory = good.WorkingDirectory, ExitCode = 0 }] };
        var outcome = await Outcome(mismatch);
        Assert.Equal((OutcomeKind.Excluded, ErrorCodes.B1ModuleMismatch), (outcome.Outcome, outcome.Code));

        // Không có test người viết: vẫn OK khi chỉ đo, trạng thái NO_PAIRED_TEST.
        var noPaired = await analyzer.AnalyzeAsync(good, context, CancellationToken.None);
        Assert.Equal(OutcomeKind.Ok, noPaired.Outcome.Outcome);
        Assert.Equal(PairedTestStatus.NoPairedTest, noPaired.Baseline!.PairedTest.Status);
        Assert.Empty(noPaired.Baseline.PairedTest.TestsRun);
    }

    /// <summary>
    /// Spec 01 Mục 4.3 bước 5: JVM chạy test không khởi động được với JDK đã chọn (<c>-Xss256k</c> với JDK 8 trên Mac ARM) → đổi JDK.
    /// Trên máy mà JDK 8 chạy được thì giữ JDK 8; trường hợp nào tiêu chí B cũng phải ra PASS.
    /// </summary>
    [Fact]
    public async Task SwitchesJdkWhenTestJvmCannotStart()
    {
        using var repo = new FixtureRepo("maven-small-stack");
        var task = repo.Task("150_stack", "src/main/java/com/acme/util/Util.java", "com.acme.util.Util", "src/test/java/com/acme/util/UtilTest.java",
            "openjdk version \"1.8.0_504\"", "junit4", Attempt("test_compile", MavenTestCompile, "."));
        var analysis = await repo.Analyzer().AnalyzeAsync(task, repo.Context(), CancellationToken.None);
        Assert.True(analysis.Outcome.Outcome == OutcomeKind.Ok, analysis.Outcome.Message);
        Assert.Equal((PairedTestStatus.Pass, 2), (analysis.Baseline!.PairedTest.Status, analysis.Baseline.PairedTest.TestsRun[0]));

        var jdk8Starts = await JvmStartsAsync(8, "-Xss256k");
        var java = analysis.Metadata!.Java;
        if (jdk8Starts)
        {
            Assert.Equal((8, "declared"), (java.JdkMajor, java.JdkChoiceReason));
        }
        else
        {
            Assert.NotEqual(8, java.JdkMajor);
            Assert.StartsWith("fallback:8,", java.JdkChoiceReason, StringComparison.Ordinal);
            Assert.True(repo.Results.Exists("it", new TaskKey("classes2test", "150_stack"), "block1/logs/03-compile-jdk8.log"));
        }
    }

    private static async Task<bool> JvmStartsAsync(int major, string option)
    {
        var home = ToolLocator.FindJdkHome(major, null, FixtureRepo.Framework.ArrowRoot);
        if (home is null)
        {
            return false;
        }

        var result = await new ProcessRunner().RunAsync(new ProcessSpec(ToolLocator.JavaExecutable(home), [option, "-version"], Path.GetTempPath()), CancellationToken.None);
        return result.Succeeded;
    }

    [Fact]
    public void RecognizesTestJvmStartFailures()
    {
        Assert.True(RepositoryAnalyzer.IsJvmStartFailure("[ERROR] The forked VM terminated without properly saying goodbye. VM crash or System.exit called?", 0));
        Assert.True(RepositoryAnalyzer.IsJvmStartFailure("The stack size specified is too small, Specify at least 640k\nError: Could not create the Java Virtual Machine.", 0));
        Assert.False(RepositoryAnalyzer.IsJvmStartFailure("[ERROR] The forked VM terminated without properly saying goodbye.", 12));
        Assert.False(RepositoryAnalyzer.IsJvmStartFailure("[INFO] Tests run: 0, Failures: 0", 0));
    }

    [Fact]
    public async Task ScreeningWritesLedgerCsvAndSkipsFinishedTasks()
    {
        using var repo = new FixtureRepo("maven-subdir-junit4");
        var task = repo.Task("700_screen", "lib/src/main/java/com/acme/util/Util.java", "com.acme.util.Util", "lib/src/test/java/com/acme/util/UtilTest.java",
            "openjdk version \"1.8.0_504\"", "junit4", Attempt("test_compile", MavenTestCompile, "lib"));
        var broken = task with { TaskId = "701_missing", ClassPath = "lib/src/main/java/com/acme/util/Nope.java", ClassFqn = "com.acme.util.Nope" };
        var manifest = Path.Combine(repo.Root, "pilot_manifest.jsonl");
        await File.WriteAllTextAsync(manifest, string.Concat(new[] { task, broken }.Select(t => JsonFile.Serialize(t with { Dataset = "", RawJson = "" }, indented: false) + "\n")));

        // Thư mục framework tạm nằm ngoài ARROW/: khai báo JDK và work_dir qua machine.local.json như máy thật.
        var paths = repo.ResultPaths;
        var tools = ToolLocator.Environment(null, FixtureRepo.Framework.ArrowRoot);
        new FileConfigRepository(paths).SaveMachine(new MachineConfig
        {
            MachineName = "it",
            JdkHomes = tools.JdkHomes.ToDictionary(j => j.Key.ToString(), j => j.Value),
            WorkDir = repo.WorkDir,
        });
        var runs = new FileRunRepository(paths);
        var calls = 0;
        IRepositoryAnalyzer Factory(ToolEnvironment environment, string workDir)
        {
            calls++;
            Assert.Equal(tools.JdkHomes.Count, environment.JdkHomes.Count);
            return repo.Analyzer(workDir);
        }

        var service = new ScreeningService(paths, new FileConfigRepository(paths), new FileManifestRepository(), runs, repo.Results, new FileReportRepository(paths), Factory, new FileLogRepository(paths));
        var events = new List<RunEvent>();
        var progress = new SyncProgress(events);
        var summary = await service.RunAsync(new ScreeningRequest { ManifestPath = manifest, RunId = "screen-it", Workers = 2 }, progress, CancellationToken.None);

        Assert.Equal(2, summary.Rows.Count);
        var ok = summary.Rows.Single(r => r.TaskId == "700_screen");
        Assert.Equal((OutcomeKind.Ok, "PASS", 8), (ok.Outcome, ok.PairedTestStatus, ok.JdkMajor));
        Assert.Equal((OutcomeKind.Excluded, ErrorCodes.B1CutMissing), (summary.Rows.Single(r => r.TaskId == "701_missing").Outcome, summary.Rows.Single(r => r.TaskId == "701_missing").Code));
        var csv = await File.ReadAllLinesAsync(summary.CsvPath);
        Assert.Equal("task_id,outcome,code,paired_test_status,tests_run,jdk_major,message", csv[0]);
        Assert.Equal("700_screen,OK,,PASS,2;2,8,", csv[1]);
        Assert.StartsWith("701_missing,EXCLUDED,B1_CUT_MISSING,,,,", csv[2], StringComparison.Ordinal);
        Assert.Equal(2, runs.ReadLedger("screen-it").Count(e => e.Block == 1));
        Assert.Contains(events, e => e.TaskId == "700_screen" && e.Message.Contains("PASS", StringComparison.Ordinal));
        Assert.False(Directory.Exists(Path.Combine(repo.WorkDir, "ws", "screen-it", "700_screen")));
        Assert.True(Directory.Exists(Path.Combine(repo.WorkDir, "mirrors", "local")));

        // Chạy lại cùng run id: không phân tích lại, bảng vẫn đủ.
        var again = await service.RunAsync(new ScreeningRequest { ManifestPath = manifest, RunId = "screen-it" }, progress, CancellationToken.None);
        Assert.Equal(2, again.Rows.Count);
        Assert.Equal(2, runs.ReadLedger("screen-it").Count);
        Assert.Equal(2, calls);

        // Run id đã dùng cho manifest khác: từ chối.
        var other = Path.Combine(repo.Root, "other.jsonl");
        await File.WriteAllTextAsync(other, (await File.ReadAllLinesAsync(manifest))[0] + "\n");
        var error = await Assert.ThrowsAsync<ArrowException>(() => service.RunAsync(new ScreeningRequest { ManifestPath = other, RunId = "screen-it" }, progress, CancellationToken.None));
        Assert.Equal(ErrorCodes.RunConfigMismatch, error.Code);
    }

    private sealed class SyncProgress(List<RunEvent> events) : IProgress<RunEvent>
    {
        public void Report(RunEvent value)
        {
            lock (events)
            {
                events.Add(value);
            }
        }
    }
}
