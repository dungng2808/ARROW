using System.Diagnostics;
using Arrow.DataAccess.Files;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block3;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.IntegrationTests;

/// <summary>Khối 1 chạy một lần trên fixture <c>maven-verify-junit4</c>; các test dùng chung snapshot và baseline.</summary>
public sealed class VerifyFixture : IAsyncLifetime
{
    internal FixtureRepo Repo { get; } = new("maven-verify-junit4");
    public TaskContext Task { get; private set; } = new();
    public JavaHelperClient Helper { get; private set; } = null!;
    public IVerifier Verifier { get; private set; } = null!;
    public IWorkspaceManager Workspace { get; private set; } = null!;

    public async Task InitializeAsync()
    {
        var record = Repo.Task("800_verify", "src/main/java/com/acme/billing/PriceCalculator.java", "com.acme.billing.PriceCalculator", "src/test/java/com/acme/billing/PriceCalculatorTest.java",
            "openjdk version \"1.8.0_504\"", "junit4", ("test_compile", ["mvn", "-q", "-DskipTests", "test-compile"], ".", 0));
        var analysis = await Repo.Analyzer().AnalyzeAsync(record, Repo.Context(), CancellationToken.None);
        Assert.True(analysis.Outcome.Outcome == OutcomeKind.Ok, analysis.Outcome.Message);
        Assert.Contains("com.acme.billing.TotalsTest#appliesDefaultRate", analysis.Baseline!.PassingInAllRuns);
        Task = new TaskContext { RunId = "it", Key = new TaskKey("classes2test", "800_verify"), Metadata = analysis.Metadata!, Baseline = analysis.Baseline, Workspace = analysis.Workspace! };

        var log = new FileLogRepository(Repo.ResultPaths);
        Helper = new JavaHelperClient(JavaHelperOptions.Default(FixtureRepo.Framework, null), new ProcessRunner(), log);
        IProcessRunner runner = new ProcessRunner();
        var git = new GitClient(runner, ToolLocator.Environment(null, FixtureRepo.Framework.ArrowRoot).Git);
        Workspace = new WorkspaceManager(git, runner, Repo.WorkDir);
        Verifier = new Verifier(Workspace, new BuildRunner(runner), new TestReportReader(), Helper, Repo.Results, log);
    }

    public async Task DisposeAsync()
    {
        await Helper.DisposeAsync();
        Repo.Dispose();
    }

    /// <summary>Jar "runtime" giả cho bộ test kiểu EvoSuite (vai trò của <c>evosuite-standalone-runtime</c>), build bằng javac + jar của JDK 8.</summary>
    public string BuildRuntimeJar()
    {
        var jdk = ToolLocator.FindJdkHome(8, null, FixtureRepo.Framework.ArrowRoot) ?? ToolLocator.FindJdkHome(17, null, FixtureRepo.Framework.ArrowRoot)!;
        var dir = Path.Combine(Repo.Root, "runtime-jar");
        Directory.CreateDirectory(Path.Combine(dir, "src", "arrowfake"));
        Directory.CreateDirectory(Path.Combine(dir, "classes")); // javac 8 không tự tạo thư mục đầu ra
        File.WriteAllText(Path.Combine(dir, "src", "arrowfake", "Runtime.java"), """
            package arrowfake;

            public final class Runtime {
                private static boolean ready;

                public static void init() {
                    ready = true;
                }

                public static boolean ready() {
                    return ready;
                }
            }
            """);
        Run(Path.Combine(jdk, "bin", "javac"), dir, "-source", "8", "-target", "8", "-d", "classes", "src/arrowfake/Runtime.java");
        Run(Path.Combine(jdk, "bin", "jar"), dir, "cf", "arrowfake-runtime.jar", "-C", "classes", ".");
        return Path.Combine(dir, "arrowfake-runtime.jar");
    }

    private static void Run(string program, string dir, params string[] args)
    {
        var start = new ProcessStartInfo(program) { WorkingDirectory = dir, RedirectStandardError = true, RedirectStandardOutput = true };
        foreach (var arg in args)
        {
            start.ArgumentList.Add(arg);
        }

        using var process = Process.Start(start)!;
        var output = process.StandardOutput.ReadToEnd() + process.StandardError.ReadToEnd();
        process.WaitForExit();
        Assert.True(process.ExitCode == 0, $"{program} lỗi: {output}");
    }
}

/// <summary>Spec 03 Mục 12: candidate cố ý sai ra đủ 6 trạng thái, và một candidate đúng ra VALID.</summary>
public class VerifierIntegrationTests(VerifyFixture fixture) : IClassFixture<VerifyFixture>
{
    private const string TestFqn = "com.acme.billing.PriceCalculator_ArrowTest";
    private const string TestPath = "src/test/java/com/acme/billing/PriceCalculator_ArrowTest.java";

    private static string Test(string body) => $$"""
        package com.acme.billing;

        import static org.junit.Assert.assertEquals;
        import static org.junit.Assert.assertTrue;

        import org.junit.Test;

        public class PriceCalculator_ArrowTest {
        {{body}}
        }
        """;

    private async Task<VerifyResult> VerifyAsync(string name, string code, Dictionary<string, string>? extraFiles = null)
    {
        var files = new Dictionary<string, string> { [TestPath] = code };
        foreach (var (path, content) in extraFiles ?? [])
        {
            files[path] = content;
        }

        var suite = new SuiteFiles { SuiteId = name, TestFqn = TestFqn, Files = files };
        var options = new VerifyOptions { ResultDir = $"block3/fake/zero_shot/{name}", RetryDelay = TimeSpan.Zero };
        var result = await fixture.Verifier.VerifyAsync(suite, fixture.Task, options, CancellationToken.None);

        // Mọi bản sao bị xóa sau khi xác minh.
        var tmp = Path.Combine(fixture.Workspace.TaskDir("it", fixture.Task.Key), "tmp");
        Assert.True(!Directory.Exists(tmp) || Directory.GetDirectories(tmp).Length == 0, "còn bản sao workspace");
        return result;
    }

    [Fact]
    public async Task ValidCandidatePassesAllFiveSteps()
    {
        var result = await VerifyAsync("valid", Test("""
                @Test
                public void addsTwoNumbers() {
                    assertEquals(5, new PriceCalculator().add(2, 3));
                }

                @Test(expected = IllegalArgumentException.class)
                public void rejectsZeroDivisor() {
                    new PriceCalculator().divide(4, 0);
                }
            """));
        Assert.Equal(VerificationState.Valid, result.State);
        Assert.Null(result.Group);
        Assert.Equal(["compile", "run_target", "run_module"], result.Steps.Select(s => s.Step));
        Assert.All(result.Steps, s => Assert.True(s.Ok));
        Assert.Equal((2, 2, 0), (result.TestMethods.Total, result.TestMethods.Passed, result.TestMethods.Failed));
        Assert.Equal(2, result.AssertionCount);
        Assert.StartsWith("sha256:", result.CodeHash, StringComparison.Ordinal);
        Assert.Null(result.ErrorSignature);
        Assert.True(fixture.Repo.Results.Exists("it", fixture.Task.Key, "block3/fake/zero_shot/valid/logs/run_module.log"));
        Assert.True(result.BuildSeconds > 0);
    }

    [Fact]
    public async Task MissingMethodIsCompile()
    {
        var result = await VerifyAsync("compile", Test("""
                @Test
                public void multiplies() {
                    assertEquals(6, new PriceCalculator().multiply(2, 3));
                }
            """));
        Assert.Equal((VerificationState.Compile, ErrorGroup.Build), (result.State, result.Group!.Value));
        Assert.Equal("COMPILE | cannot find symbol | method", result.ErrorSignature);
        Assert.Equal(["multiplies"], result.FailingMethods);
        var error = Assert.Single(result.CompileErrors);
        Assert.Equal((TestPath, 11), (error.File, error.Line));
        Assert.Equal("multiply(int,int)", error.Symbol);
        Assert.Equal(["compile"], result.Steps.Select(s => s.Step));
    }

    [Fact]
    public async Task BuildFailureOutsideGeneratedFileIsUnknown()
    {
        var result = await VerifyAsync("unknown", Test("""
                @Test
                public void adds() {
                    assertEquals(5, new PriceCalculator().add(2, 3));
                }
            """), new() { ["src/main/java/com/acme/billing/Broken.java"] = "package com.acme.billing;\nclass Broken { int x = ; }\n" });
        Assert.Equal(VerificationState.Unknown, result.State);
        Assert.Equal("UNKNOWN | maven-compiler-plugin | -", result.ErrorSignature);
        Assert.Contains("src/main/java/com/acme/billing/Broken.java", result.BuildErrorExcerpt);
        Assert.DoesNotContain(fixture.Repo.WorkDir, result.BuildErrorExcerpt);
    }

    [Fact]
    public async Task ClassWithoutTestsIsDiscovery()
    {
        var result = await VerifyAsync("discovery", Test("""
                public void addsTwoNumbers() {
                    assertEquals(5, new PriceCalculator().add(2, 3));
                }
            """));
        Assert.Equal(VerificationState.Discovery, result.State);
        Assert.Equal("DISCOVERY | no tests executed | -", result.ErrorSignature);
        Assert.Equal(TestPath, result.TestFile);
    }

    [Fact]
    public async Task NullPointerInCutIsRuntime()
    {
        var result = await VerifyAsync("runtime", Test("""
                @Test
                public void measuresLength() {
                    assertEquals(0, new PriceCalculator().length(null));
                }

                @Test
                public void adds() {
                    assertEquals(5, new PriceCalculator().add(2, 3));
                }
            """));
        Assert.Equal((VerificationState.Runtime, ErrorGroup.Runtime), (result.State, result.Group!.Value));
        Assert.Equal("RUNTIME | NullPointerException | PriceCalculator.length", result.ErrorSignature);
        Assert.Equal(["measuresLength"], result.FailingMethods);
        Assert.Equal((2, 1, 1), (result.TestMethods.Total, result.TestMethods.Passed, result.TestMethods.Failed));
        Assert.Contains("at com.acme.billing.PriceCalculator.length", Assert.Single(result.Failures).TraceHead);
    }

    [Fact]
    public async Task WrongExpectedValueIsAssertion()
    {
        var result = await VerifyAsync("assertion", Test("""
                @Test
                public void addsWrong() {
                    assertEquals(6, new PriceCalculator().add(2, 3));
                }
            """));
        Assert.Equal((VerificationState.Assertion, ErrorGroup.Assertion), (result.State, result.Group!.Value));
        Assert.Equal("ASSERTION | AssertionError | addsWrong", result.ErrorSignature);
        Assert.Contains("expected:<6> but was:<5>", Assert.Single(result.Failures).Message);
    }

    [Fact]
    public async Task ChangingSharedStaticStateIsRegression()
    {
        var result = await VerifyAsync("regression", Test("""
                @Test
                public void addsAfterChangingRate() {
                    Invoice.setDefaultRate(20);
                    assertEquals(5, new PriceCalculator().add(2, 3));
                }
            """));
        Assert.Equal((VerificationState.Regression, ErrorGroup.Runtime), (result.State, result.Group!.Value));
        Assert.Equal("REGRESSION | existing tests fail | TotalsTest", result.ErrorSignature);
        var regression = Assert.Single(result.Regressions);
        Assert.Equal(("com.acme.billing.TotalsTest#appliesDefaultRate", "java.lang.AssertionError"), (regression.TestId, regression.Type));
        Assert.Equal((1, 1), (result.TestMethods.Total, result.TestMethods.Passed));
        Assert.True(fixture.Repo.Results.Exists("it", fixture.Task.Key, "block3/fake/zero_shot/regression/reports/run_module/TEST-com.acme.billing.TotalsTest.xml"));
    }

    private const string EsTest = """
        package com.acme.billing;

        import static org.junit.Assert.assertEquals;
        import static org.junit.Assert.assertTrue;
        import static org.junit.Assert.fail;

        import org.junit.Test;

        public class PriceCalculator_ESTest extends PriceCalculator_ESTest_scaffolding {
            @Test(timeout = 4000)
            public void test0() throws Throwable {
                assertTrue(arrowfake.Runtime.ready());
                assertEquals(5, new PriceCalculator().add(2, 3));
            }

            @Test(timeout = 4000)
            public void test1() throws Throwable {
                try {
                    new PriceCalculator().divide(1, 0);
                    fail("Expecting exception: IllegalArgumentException");
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
        """;

    private const string EsScaffolding = """
        package com.acme.billing;

        import org.junit.BeforeClass;

        public class PriceCalculator_ESTest_scaffolding {
            @BeforeClass
            public static void initEvoSuiteFramework() {
                arrowfake.Runtime.init();
            }
        }
        """;

    /// <summary>Spec 03 Mục 9: bộ EvoSuite đi qua đúng 5 bước; tên lệnh từ test_fqn (_ESTest), scaffolding không phải test class, classpath phụ.</summary>
    [Fact]
    public async Task EvoSuiteStyleSuiteIsVerifiedWithExtraClasspath()
    {
        var jar = fixture.BuildRuntimeJar();
        var suite = new SuiteFiles
        {
            SuiteId = "evosuite",
            TestFqn = "com.acme.billing.PriceCalculator_ESTest",
            Files = new()
            {
                ["src/test/java/com/acme/billing/PriceCalculator_ESTest.java"] = EsTest,
                ["src/test/java/com/acme/billing/PriceCalculator_ESTest_scaffolding.java"] = EsScaffolding,
            },
            ExtraTestClasspath = [jar],
        };
        var options = new VerifyOptions { ResultDir = "block3/evosuite", RetryDelay = TimeSpan.Zero };
        var result = await fixture.Verifier.VerifyAsync(suite, fixture.Task, options, CancellationToken.None);
        Assert.True(result.State == VerificationState.Valid, result.ErrorSignature + " " + result.BuildErrorExcerpt);
        Assert.Equal((2, 2), (result.TestMethods.Total, result.TestMethods.Passed));
        Assert.Equal("src/test/java/com/acme/billing/PriceCalculator_ESTest.java", result.TestFile);
        Assert.Contains("-Dtest=PriceCalculator_ESTest", fixture.Repo.Results.LoadText("it", fixture.Task.Key, "block3/evosuite/logs/run_target.log"));

        // Thiếu classpath phụ: lỗi nằm trong file của bộ (test class và scaffolding) nên là COMPILE, không phải UNKNOWN.
        var missing = await fixture.Verifier.VerifyAsync(suite with { ExtraTestClasspath = [] }, fixture.Task, options with { ResultDir = "block3/evosuite-missing" }, CancellationToken.None);
        Assert.Equal(VerificationState.Compile, missing.State);
        Assert.Equal("COMPILE | package does not exist | -", missing.ErrorSignature);
        Assert.Contains(missing.CompileErrors, e => e.File.EndsWith("PriceCalculator_ESTest_scaffolding.java", StringComparison.Ordinal) && e.InGeneratedFile);
    }

    [Fact]
    public void MavenSystemDependencyIsAddedOnlyToCopy()
    {
        var pom = Path.Combine(fixture.Repo.Root, "pom-copy.xml");
        File.Copy(Path.Combine(FixtureRepo.Framework.FrameworkRoot, "tests", "fixtures", "maven-verify-junit4", "pom.xml"), pom);
        var jar = Path.Combine(Path.GetTempPath(), "evosuite-standalone-runtime-1.2.0.jar");
        Verifier.AddMavenSystemDependencies(pom, [jar]);
        var text = File.ReadAllText(pom);
        Assert.Contains("<scope>system</scope>", text);
        Assert.Contains($"<systemPath>{Path.GetFullPath(jar)}</systemPath>", text);
        Assert.Contains("<artifactId>junit</artifactId>", text);
    }
}

/// <summary>Xác minh trên Gradle: bước 2 dùng <c>--tests &lt;FQN&gt;</c>, bước 5 cùng lệnh với baseline.</summary>
public class GradleVerifierIntegrationTests
{
    [Fact]
    public async Task GradleCandidateIsValidAndWrongOneIsAssertion()
    {
        if (ToolLocator.FindGradle(null) is null)
        {
            return; // máy không có Gradle
        }

        using var repo = new FixtureRepo("gradle-junit5");
        var record = repo.Task("900_gradle", "core/src/main/java/com/acme/calc/Calculator.java", "com.acme.calc.Calculator", "core/src/test/java/com/acme/calc/CalculatorTest.java",
            "openjdk version \"17.0.20.1\" 2026-08-18 LTS", "junit5", ("test_compile", ["/preflight/gradlew", "--no-daemon", ":core:testClasses"], ".", 0));
        var analysis = await repo.Analyzer().AnalyzeAsync(record, repo.Context(), CancellationToken.None);
        Assert.True(analysis.Outcome.Outcome == OutcomeKind.Ok, analysis.Outcome.Message);
        var task = new TaskContext { RunId = "it", Key = new TaskKey("classes2test", "900_gradle"), Metadata = analysis.Metadata!, Baseline = analysis.Baseline!, Workspace = analysis.Workspace! };

        var log = new FileLogRepository(repo.ResultPaths);
        await using var helper = new JavaHelperClient(JavaHelperOptions.Default(FixtureRepo.Framework, null), new ProcessRunner(), log);
        IProcessRunner runner = new ProcessRunner();
        var workspace = new WorkspaceManager(new GitClient(runner, "git"), runner, repo.WorkDir);
        var verifier = new Verifier(workspace, new BuildRunner(runner), new TestReportReader(), helper, repo.Results, log);

        static SuiteFiles Suite(string expected) => new()
        {
            SuiteId = "c0",
            TestFqn = "com.acme.calc.Calculator_ArrowTest",
            Files = new()
            {
                ["core/src/test/java/com/acme/calc/Calculator_ArrowTest.java"] = $$"""
                    package com.acme.calc;

                    import static org.junit.jupiter.api.Assertions.assertEquals;

                    import org.junit.jupiter.api.Test;

                    class Calculator_ArrowTest {
                        @Test
                        void addsNumbers() {
                            assertEquals({{expected}}, new Calculator().add(2, 3));
                        }
                    }
                    """,
            },
        };

        var valid = await verifier.VerifyAsync(Suite("5"), task, new VerifyOptions { ResultDir = "block3/g/valid", RetryDelay = TimeSpan.Zero }, CancellationToken.None);
        Assert.True(valid.State == VerificationState.Valid, valid.ErrorSignature);
        Assert.Equal(1, valid.TestMethods.Passed);
        Assert.Contains("--tests com.acme.calc.Calculator_ArrowTest", repo.Results.LoadText("it", task.Key, "block3/g/valid/logs/run_target.log"));

        var wrong = await verifier.VerifyAsync(Suite("6"), task, new VerifyOptions { ResultDir = "block3/g/wrong", RetryDelay = TimeSpan.Zero }, CancellationToken.None);
        Assert.Equal("ASSERTION | AssertionFailedError | addsNumbers", wrong.ErrorSignature);
    }
}
