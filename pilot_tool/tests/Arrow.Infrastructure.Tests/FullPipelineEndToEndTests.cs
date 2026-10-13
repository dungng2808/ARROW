using Arrow.Core.Quality;
using Arrow.Core.Shards;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Tests;

/// <summary>
/// M8: <c>arrow run</c> S0→S5 với model giả trên 3 fixture (n_max 3), rồi <c>export</c> và <c>merge</c> thành run gom và báo cáo lại.
/// </summary>
public class FullPipelineEndToEndTests
{
    private static async Task<(int ExitCode, string Output)> CliAsync(params string[] args)
    {
        var cli = Path.Combine(AppContext.BaseDirectory, "Arrow.Cli.dll");
        var result = await ProcessRunner.RunAsync("dotnet", [cli, .. args, "--root", Env.Paths.PilotToolDir], Env.Paths.PilotToolDir,
            new Dictionary<string, string?> { ["ARROW_PILOT_ROOT"] = Env.Paths.PilotToolDir }, TimeSpan.FromMinutes(20), CancellationToken.None);
        return (result.ExitCode ?? -1, result.Output);
    }

    [Fact]
    public async Task AllStagesThenExportAndMerge()
    {
        var setup = EndToEnd.Create("e2e-full", RepairEndToEndTests.Rules(), nMax: 3);
        var (exit, output) = await EndToEnd.RunAsync(setup);
        Assert.True(exit == 0, output);
        var store = setup.Store;

        // S3: EvoSuite sinh test cho fixture JUnit 4 và đạt VALID.
        var evosuite = Json.Read<VerifyResult>(Path.Combine(store.TaskDir("classes2test", "fx-maven-junit4"), "evosuite", "verify.json"));
        Assert.Equal(VerificationState.Valid, evosuite.State);

        // S4: mọi bộ test VALID của fixture JUnit 4 đều có số đo.
        var quality = Path.Combine(store.TaskDir("classes2test", "fx-maven-junit4"), "quality");
        var systems = Directory.EnumerateDirectories(quality).Select(Path.GetFileName).Order(StringComparer.Ordinal).ToList();
        Assert.Contains("arrow__gpt-6-luna__zero_shot_project_aware__gen", systems);
        Assert.Contains("arrow__gpt-6-luna__zero_shot__adaptive", systems);
        Assert.Contains("arrow__gpt-6-luna__few_shot__adaptive", systems);
        Assert.Contains("evosuite", systems);
        foreach (var system in systems)
        {
            Assert.Equal("OK", Json.Read<CoverageResult>(Path.Combine(quality, system!, "jacoco.json")).Status);
            Assert.Equal("OK", Json.Read<MutationResult>(Path.Combine(quality, system!, "pit.json")).Status);
            Assert.Equal("OK", Json.Read<SmellResult>(Path.Combine(quality, system!, "tsdetect.json")).Status);
        }

        // S5: báo cáo và CSV của README Mục 3; N chung hai cơ chế: S = 2, 3, 4 → share95 = 3.
        var reports = Path.Combine(store.Root, "reports");
        foreach (var file in new[] { "part1_cost.csv", "part1_infra_errors.csv", "part1_generation.csv", "part2_cumulative.csv", "pilot_report.md" })
        {
            Assert.True(File.Exists(Path.Combine(reports, file)), file);
        }

        var cost = File.ReadAllText(Path.Combine(reports, "part1_cost.csv"));
        foreach (var step in new[] { "clone,", "baseline,", "llm_generate,", "build_verify,", "llm_repair,", "build_repair,", "evosuite,", "jacoco,", "pit,", "tsdetect," })
        {
            Assert.Contains(step, cost);
        }

        Assert.Equal("round,successes_cumulative,share_of_round10\n1,2,0.5\n2,3,0.75\n3,4,1\n", File.ReadAllText(Path.Combine(reports, "part2_cumulative.csv")));
        var report = File.ReadAllText(Path.Combine(reports, "pilot_report.md"));
        Assert.Contains("**N theo quy tắc `share95`: 3.**", report);
        Assert.Contains("`report2` → 3", report);
        Assert.Contains("Ứng viên không vào bước sửa: 0", report);
        Assert.Contains("### 1.4. Ước tính", report);

        // export → merge (plan một shard) → báo cáo của run gom cho cùng N.
        var bundles = Path.Combine(Env.Root, "bundles-" + setup.RunId);
        var (exportExit, exportOut) = await CliAsync("export", "--run-id", setup.RunId, "--out", bundles);
        Assert.True(exportExit == 0, exportOut);
        var zip = Directory.EnumerateFiles(bundles, "*.zip", SearchOption.AllDirectories).Single();
        Assert.DoesNotContain(System.IO.Compression.ZipFile.OpenRead(zip).Entries, e => e.FullName.StartsWith("llm_cache/", StringComparison.Ordinal));

        var planDir = Path.Combine(Env.Root, "plan-" + setup.RunId);
        Directory.CreateDirectory(planDir);
        var experimentSha = Hashing.Sha256File(setup.ExperimentPath);
        var tasks = new[] { Samples.MavenJunit4, Samples.GradleJunit5, Samples.MavenMultiJunit5 }
            .Select(r => new ShardTask { Dataset = r.TaskId == "fx-maven-multi-junit5" ? "gitbug-java" : "classes2test", TaskId = r.TaskId, RepoUrl = r.RepoUrl }).ToList();
        File.WriteAllText(Path.Combine(planDir, "shard-all.json"), Json.Serialize(new ShardFile { PlanId = "e2e", ShardId = "all", Owner = "m", ExperimentSha256 = experimentSha, Tasks = tasks }));
        File.WriteAllText(Path.Combine(planDir, "plan.json"), Json.Serialize(new ShardPlanFile
        {
            PlanId = "e2e", ExperimentSha256 = experimentSha, Coordinator = "m",
            Shards = [new ShardSummary { ShardId = "all", Owner = "m", File = "shard-all.json", TaskCount = 3 }],
        }));
        var merged = setup.RunId + "-merged";
        var (mergeExit, mergeOut) = await CliAsync("merge", "--plan", Path.Combine(planDir, "plan.json"), "--bundles", Path.GetDirectoryName(zip)!, "--run-id", merged,
            "--experiment", setup.ExperimentPath);
        Assert.True(mergeExit == 0, mergeOut);
        var mergedReport = File.ReadAllText(Path.Combine(Env.Paths.PilotToolDir, Env.Machine.RunsDir, merged, "reports", "pilot_report.md"));
        Assert.Contains("**N theo quy tắc `share95`: 3.**", mergedReport);

        // merge từ chối plan có shard không có gói.
        File.WriteAllText(Path.Combine(planDir, "plan.json"), Json.Serialize(new ShardPlanFile
        {
            PlanId = "e2e", ExperimentSha256 = experimentSha, Coordinator = "m",
            Shards = [new ShardSummary { ShardId = "all", File = "shard-all.json" }, new ShardSummary { ShardId = "shard-02", File = "shard-all.json" }],
        }));
        var (badExit, badOut) = await CliAsync("merge", "--plan", Path.Combine(planDir, "plan.json"), "--bundles", Path.GetDirectoryName(zip)!, "--run-id", merged + "2",
            "--experiment", setup.ExperimentPath);
        Assert.NotEqual(0, badExit);
        Assert.Contains("shard-02: có 0 gói", badOut);
    }
}
