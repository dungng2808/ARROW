using Arrow.Core.Manifest;
using Arrow.Core.Prepare;
using Arrow.Core.Runs;
using Arrow.Infrastructure.Prepare;

namespace Arrow.Infrastructure.Tests;

/// <summary>S0 trên 3 repository mẫu (specs/02 Mục 8): baseline nhận ra test hỏng sẵn và test flaky.</summary>
public class PrepareIntegrationTests
{
    private static async Task<PrepareResult> PrepareAsync(ManifestRecord record)
    {
        var store = Env.NewStore("prepare");
        store.EnsureCreated();
        var job = new JobKey { Dataset = record.Dataset, TaskId = record.TaskId, Stage = "prepare" };
        return await Env.Prepare().RunAsync(record, store, job, _ => { }, CancellationToken.None);
    }

    [Fact]
    public async Task MavenJunit4SingleModule()
    {
        var result = await PrepareAsync(Samples.MavenJunit4);
        Assert.Equal(8, result.Task.JdkMajor);
        Assert.Equal("declared_level", result.Task.JdkReason);
        Assert.Equal(BuildMode.Root, result.Task.Plan.Mode);
        Assert.Equal("surefire-xml", result.Baseline.ReportSource);
        Assert.Equal(["com.example.calc.BrokenTest#alwaysFails"], result.Baseline.FailingInAllRuns);
        Assert.Equal(["com.example.calc.FlakyTest#failsFirstTime"], result.Baseline.Flaky);
        Assert.Contains("com.example.calc.CalculatorTest#addsNumbers", result.Baseline.PassingInAllRuns);
        Assert.Contains("com.example.calc.SharedStoreTest#storeIsUntouched", result.Baseline.PassingInAllRuns);
        Assert.Contains(result.Context.Dependencies, d => d.Group == "junit" && d.Artifact == "junit" && d.Scope == "test");
        Assert.Equal("src/test/java/com/example/calc/Calculator_ArrowTest.java", result.Context.TestPath);
    }

    [Fact]
    public async Task MavenMultimoduleJunit5UsesReactor()
    {
        var result = await PrepareAsync(Samples.MavenMultiJunit5);
        Assert.Equal(17, result.Task.JdkMajor);
        Assert.Equal(BuildMode.Reactor, result.Task.Plan.Mode);
        Assert.Equal("app", result.Task.Plan.ModulePath);
        Assert.Contains(result.Task.Steps, s => s.Step == BuildStep.ModuleTest);
        Assert.Equal(["com.example.app.BrokenTests#alwaysFails"], result.Baseline.FailingInAllRuns);
        Assert.Equal(["com.example.app.FlakyTests#failsFirstTime"], result.Baseline.Flaky);
        Assert.Contains("com.example.app.GreeterTests#greets", result.Baseline.PassingInAllRuns);
        Assert.Contains(result.Context.Dependencies, d => d.Artifact == "core");
        Assert.Equal("app/src/test/java/com/example/app/Greeter_ArrowTest.java", result.Context.TestPath);
    }

    [Fact]
    public async Task GradleJunit5()
    {
        var result = await PrepareAsync(Samples.GradleJunit5);
        Assert.Equal(17, result.Task.JdkMajor);
        Assert.Equal(BuildToolKind.Gradle, result.Task.Plan.Tool);
        Assert.Equal("gradle-xml", result.Baseline.ReportSource);
        Assert.Equal(["com.example.shop.BrokenTest#alwaysFails"], result.Baseline.FailingInAllRuns);
        Assert.Equal(["com.example.shop.FlakyTest#failsFirstTime"], result.Baseline.Flaky);
        Assert.Contains("com.example.shop.CartTest#sumsPrices", result.Baseline.PassingInAllRuns);
        Assert.Contains(result.Context.Dependencies, d => d.Artifact == "junit-jupiter-api");
        Assert.NotNull(result.Task.GradleTasks);
    }
}
