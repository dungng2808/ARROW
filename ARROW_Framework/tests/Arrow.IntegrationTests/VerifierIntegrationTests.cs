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
}
