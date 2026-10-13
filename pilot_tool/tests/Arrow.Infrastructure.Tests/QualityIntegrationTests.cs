using Arrow.Core.Quality;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Quality;
using Arrow.Infrastructure.Tooling;

namespace Arrow.Infrastructure.Tests;

/// <summary>M7 (specs/06 Mục 4): JaCoCo, PIT, tsDetect trên test VALID của 3 fixture; EvoSuite sinh test VALID trên fixture JUnit 4.</summary>
public class QualityIntegrationTests
{
    private static QualityService Service => new(Env.Paths, Env.Experiment, Env.Machine, Env.Git, Env.Analyzer, Env.WorkDir);

    private static async Task<(CoverageResult Jacoco, MutationResult Pit, SmellResult Smells)> MeasureAsync(PreparedTaskData data, PreparedTask prepared, string code)
    {
        var job = new JobKey { Dataset = prepared.Record.Dataset, TaskId = prepared.Record.TaskId, Stage = "quality", Strategy = "arrow__test" };
        var outcome = await Service.MeasureAsync(prepared.Store, job, new SuiteToMeasure
        {
            System = "arrow__test",
            TestClassFqn = data.Task.GeneratedTestClassFqn,
            TestPath = data.Task.GeneratedTestPath,
            Code = code,
        }, _ => { }, CancellationToken.None);
        Assert.True(outcome.Outcome == "OK", outcome.Message);
        var dir = QualityService.Dir(data.TaskDir, "arrow__test");
        return (Json.Read<CoverageResult>(Path.Combine(dir, "jacoco.json")), Json.Read<MutationResult>(Path.Combine(dir, "pit.json")), Json.Read<SmellResult>(Path.Combine(dir, "tsdetect.json")));
    }

    [Fact]
    public async Task MavenJunit4()
    {
        var prepared = await Env.PreparedAsync(Samples.MavenJunit4);
        var data = PreparedTaskData.Load(prepared.Store, prepared.Record.Dataset, prepared.Record.TaskId);
        var (jacoco, pit, smells) = await MeasureAsync(data, prepared, """
            package com.example.calc;

            import static org.junit.Assert.assertEquals;

            import org.junit.Test;

            public class Calculator_ArrowTest {
                @Test
                public void adds() {
                    assertEquals(5, new Calculator("c").add(2, 3));
                }

                @Test(expected = IllegalArgumentException.class)
                public void rejectsZero() {
                    new Calculator("c").divide(1, 0);
                }
            }
            """);
        // Calculator: 9 dòng; test phủ add và nhánh ném của divide.
        Assert.Equal(9, jacoco.Counters["LINE_MISSED"] + jacoco.Counters["LINE_COVERED"]);
        Assert.InRange(jacoco.Line!.Value, 0.3, 0.9);
        Assert.NotNull(jacoco.Branch);
        Assert.True(pit.Total > 0);
        Assert.InRange(pit.MutationScore!.Value, 0.01, 1.0);
        Assert.True(smells.TotalSmells >= 0);
        Assert.Equal(2, smells.TestMethods);
    }

    [Fact]
    public async Task MavenMultimoduleJunit5()
    {
        var prepared = await Env.PreparedAsync(Samples.MavenMultiJunit5);
        var data = PreparedTaskData.Load(prepared.Store, prepared.Record.Dataset, prepared.Record.TaskId);
        var (jacoco, pit, _) = await MeasureAsync(data, prepared, """
            package com.example.app;

            import static org.junit.jupiter.api.Assertions.*;

            import org.junit.jupiter.api.Test;

            class Greeter_ArrowTest {
                @Test
                void greets() {
                    assertEquals("Hi, Ann!", new Greeter("Hi").greet("ann"));
                }

                @Test
                void rejectsNull() {
                    assertThrows(IllegalArgumentException.class, () -> new Greeter("Hi").greet(null));
                }
            }
            """);
        Assert.NotNull(jacoco.Line);
        Assert.True(jacoco.Line > 0.5);
        Assert.True(pit.Killed > 0);
    }

    [Fact]
    public async Task GradleJunit5()
    {
        var prepared = await Env.PreparedAsync(Samples.GradleJunit5);
        var data = PreparedTaskData.Load(prepared.Store, prepared.Record.Dataset, prepared.Record.TaskId);
        var (jacoco, pit, smells) = await MeasureAsync(data, prepared, """
            package com.example.shop;

            import static org.junit.jupiter.api.Assertions.*;

            import org.junit.jupiter.api.Test;

            class Cart_ArrowTest {
                @Test
                void sums() {
                    Cart cart = new Cart();
                    cart.add(4);
                    cart.add(5);
                    assertEquals(9, cart.total());
                    assertEquals(2, cart.size());
                }

                @Test
                void rejectsNegative() {
                    assertThrows(IllegalArgumentException.class, () -> new Cart().add(-1));
                }
            }
            """);
        Assert.True(jacoco.Line > 0.8);
        Assert.True(pit.MutationScore > 0.5);
        Assert.Contains("Magic Number Test", smells.BySmell.Keys);
    }

    [Fact]
    public async Task EvoSuiteGeneratesValidSuiteForJunit4Fixture()
    {
        Assert.Empty(ToolsCatalog.Verify(Env.Paths.ArrowRoot));
        var prepared = await Env.PreparedAsync(Samples.MavenJunit4);
        var service = new EvoSuiteService(Env.Paths, Env.Experiment, Env.Machine, Env.Git, Env.Candidates, Env.WorkDir);
        var job = new JobKey { Dataset = prepared.Record.Dataset, TaskId = prepared.Record.TaskId, Stage = "evosuite", Strategy = "evosuite" };
        var outcome = await service.RunAsync(prepared.Store, job, _ => { }, CancellationToken.None);
        Assert.True(outcome.Outcome == "VALID", outcome.Message);

        var data = PreparedTaskData.Load(prepared.Store, prepared.Record.Dataset, prepared.Record.TaskId);
        var stats = Json.Read<EvoSuiteStats>(Path.Combine(EvoSuiteService.Dir(data.TaskDir), "evosuite_stats.json"));
        Assert.Equal("com.example.calc.Calculator", stats.Statistics["TARGET_CLASS"]);
        Assert.Equal(8, stats.JdkMajor); // task Java 8 → EvoSuite chạy bằng JDK 8 (00-INDEX P11)
        var verify = Json.Read<VerifyResult>(Path.Combine(EvoSuiteService.Dir(data.TaskDir), "verify.json"));
        Assert.True(verify.TestMethods.Passed > 0);

        // Bộ test EvoSuite đo được bằng cùng bộ đo (scaffolding + runtime trong scope test).
        var suite = EvoSuiteService.ValidSuite(Env.Paths.ArrowRoot, data)!;
        var quality = await new QualityService(Env.Paths, Env.Experiment, Env.Machine, Env.Git, Env.Analyzer, Env.WorkDir)
            .MeasureAsync(prepared.Store, job with { Stage = "quality" }, suite, _ => { }, CancellationToken.None);
        Assert.True(quality.Outcome == "OK", quality.Message);
    }
}
