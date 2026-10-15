using Arrow.DataAccess.Files;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Service.Block4;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.IntegrationTests;

/// <summary>
/// Bước C7: đo một bộ test VALID trên fixture JUnit 4 Maven bằng JaCoCo và PIT thật (tải qua Maven Central vào <c>ARROW/tools</c>);
/// PIT chỉ có mutant của CUT. tsDetect và EvoSuite (tải từ GitHub Releases) chỉ chạy khi đã có trong <c>tools_dir</c>.
/// </summary>
public class AssessmentIntegrationTests(VerifyFixture fixture) : IClassFixture<VerifyFixture>
{
    private static readonly SuiteToMeasure Suite = new()
    {
        SuiteId = "fake__zero_shot_project_aware__initial",
        Files = new SuiteFiles
        {
            SuiteId = "fake__zero_shot_project_aware__initial",
            TestFqn = "com.acme.billing.PriceCalculator_ArrowTest",
            Files = new()
            {
                ["src/test/java/com/acme/billing/PriceCalculator_ArrowTest.java"] = """
                    package com.acme.billing;

                    import static org.junit.Assert.assertEquals;

                    import org.junit.Test;

                    public class PriceCalculator_ArrowTest {
                        @Test
                        public void addsTwoNumbers() {
                            assertEquals(5, new PriceCalculator().add(2, 3));
                        }

                        @Test(expected = IllegalArgumentException.class)
                        public void rejectsZeroDivisor() {
                            new PriceCalculator().divide(4, 0);
                        }

                        @Test
                        public void dividesNumbers() {
                            assertEquals(3, new PriceCalculator().divide(9, 3));
                        }
                    }
                    """,
            },
        },
    };

    private (ToolsCatalog Tools, IQualityTool[] All) Create()
    {
        var paths = FixtureRepo.Framework;
        var log = new FileLogRepository(fixture.Repo.ResultPaths);
        IProcessRunner runner = new ProcessRunner();
        var environment = ToolLocator.Environment(null, paths.ArrowRoot);
        var tools = new ToolsCatalog(Path.Combine(paths.ArrowRoot, "tools"), Path.Combine(paths.FrameworkRoot, "tools", "tools.lock.json"),
            environment.Maven, environment.JdkHomes[17], runner, log);
        var build = new BuildRunner(runner);
        var preparer = new SuitePreparer(fixture.Workspace, build, fixture.Repo.Results);
        return (tools,
        [
            new JacocoTool(preparer, build, tools, new TestReportReader(), runner, fixture.Workspace, fixture.Repo.Results),
            new PitTool(preparer, tools, runner, fixture.Workspace, fixture.Repo.Results, environment.JdkHomes),
            new TsDetectTool(tools, fixture.Helper, runner, fixture.Workspace, fixture.Repo.Results),
        ]);
    }

    [Fact]
    public async Task JacocoAndPitMeasureOnlyTheCut()
    {
        var (_, all) = Create();
        var assessor = new QualityAssessor(all, fixture.Repo.Results, new FileLogRepository(fixture.Repo.ResultPaths));
        var options = new MeasureOptions { ResultDir = "block4/" + Suite.SuiteId, RetryDelay = TimeSpan.Zero };
        var outcome = await assessor.MeasureAsync(Suite, fixture.Task, ["jacoco", "pit"], options, CancellationToken.None);

        Assert.True(outcome.Tools["jacoco"].Outcome == OutcomeKind.Ok, outcome.Tools["jacoco"].Message);
        Assert.True(outcome.Tools["pit"].Outcome == OutcomeKind.Ok, outcome.Tools["pit"].Message);
        Assert.False(outcome.Tools.ContainsKey("tsdetect"));

        var results = fixture.Repo.Results;
        var coverage = results.LoadJson<CoverageResult>("it", fixture.Task.Key, $"block4/{Suite.SuiteId}/jacoco.json")!;
        // add, divide và constructor được gọi; length thì không.
        Assert.Equal((3, 1), (coverage.Counters["METHOD_COVERED"], coverage.Counters["METHOD_MISSED"]));
        Assert.Equal(1.0, coverage.Branch);
        Assert.True(coverage.Line is > 0.5 and < 1.0, $"line = {coverage.Line}");

        var mutation = results.LoadJson<MutationResult>("it", fixture.Task.Key, $"block4/{Suite.SuiteId}/pit.json")!;
        Assert.True(mutation.Total > 0);
        Assert.True(mutation.Killed > 0);
        Assert.True(mutation.NoCoverage > 0, "length() không được test gọi nên mutant của nó NO_COVERAGE");
        var xml = results.LoadText("it", fixture.Task.Key, $"block4/{Suite.SuiteId}/mutations.xml")!;
        Assert.Empty(PitXmlParser.Parse(xml, "com.acme.billing.PriceCalculator").OutOfScope);
        Assert.DoesNotContain("Totals", xml);

        // Mọi bản sao dùng để đo đã bị xóa.
        var tmp = Path.Combine(fixture.Workspace.TaskDir("it", fixture.Task.Key), "tmp");
        Assert.True(!Directory.Exists(tmp) || Directory.GetDirectories(tmp).Length == 0, "còn bản sao workspace");
    }

    [Fact]
    public async Task TsDetectWhenInstalled()
    {
        var (tools, all) = Create();
        if (!tools.Check().Single(t => t.Id == "tsdetect").Present)
        {
            return; // tsDetect chưa có trong tools_dir (tải từ GitHub Releases bằng arrow setup)
        }

        var result = await all[2].MeasureAsync(Suite, fixture.Task, new MeasureOptions { ResultDir = "block4/ts" }, CancellationToken.None);
        Assert.Equal(OutcomeKind.Ok, result.Outcome.Outcome);
        Assert.Equal(3, result.Smells!.TestMethods);
    }
}
