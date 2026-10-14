using Arrow.DataAccess;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Files;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Run;
using Arrow.Service;
using Arrow.Service.External;

namespace Arrow.IntegrationTests;

/// <summary>
/// Bước C8 (spec 07 Mục 11): toàn luồng <c>arrow run</c> 4 khối trên fixture Maven với model giả; lần chạy đầu bị dừng giữa chain sửa,
/// chạy lại cùng run id thì tiếp tục (job đã có ledger không chạy lại, lượt gọi model đã xong được phát lại từ cache).
/// </summary>
public class PipelineIntegrationTests
{
    private const string Broken = """
        package com.acme.billing;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class PriceCalculatorTest {
            @Test
            public void multiplies() {
                assertEquals(6, new PriceCalculator().multiply(2, 3));
            }
        }
        """;

    private const string Valid = """
        package com.acme.billing;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class PriceCalculator_ArrowTest {
            @Test
            public void adds() {
                assertEquals(5, new PriceCalculator().add(2, 3));
            }

            @Test(expected = IllegalArgumentException.class)
            public void rejectsZeroDivisor() {
                new PriceCalculator().divide(1, 0);
            }
        }
        """;

    private static void CopyTree(string source, string target)
    {
        foreach (var file in Directory.GetFiles(source, "*", SearchOption.AllDirectories))
        {
            var destination = Path.Combine(target, Path.GetRelativePath(source, file));
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            File.Copy(file, destination, overwrite: true);
        }
    }

    [Fact]
    public async Task RunAllBlocksStopAndResume()
    {
        using var repo = new FixtureRepo("maven-verify-junit4");
        var real = FixtureRepo.Framework;
        var paths = repo.ResultPaths;

        // Thư mục framework tạm: bộ prompt, java-helper (cả jar đã build), danh sách công cụ; JDK, work_dir, tools_dir qua machine.local.json.
        CopyTree(real.PromptsDir, paths.PromptsDir);
        CopyTree(Path.Combine(real.FrameworkRoot, "tools", "java-helper"), Path.Combine(paths.FrameworkRoot, "tools", "java-helper"));
        File.Copy(Path.Combine(real.FrameworkRoot, "tools", "tools.lock.json"), Path.Combine(paths.FrameworkRoot, "tools", "tools.lock.json"));
        var tools = ToolLocator.Environment(null, real.ArrowRoot);
        new FileConfigRepository(paths).SaveMachine(new MachineConfig
        {
            MachineName = "it",
            JdkHomes = tools.JdkHomes.ToDictionary(j => j.Key.ToString(), j => j.Value),
            WorkDir = repo.WorkDir,
            ToolsDir = Path.Combine(real.ArrowRoot, "tools"),
            Workers = new WorkersConfig { Build = 2, Llm = 2 },
        });

        var record = repo.Task("800_e2e", "src/main/java/com/acme/billing/PriceCalculator.java", "com.acme.billing.PriceCalculator", "src/test/java/com/acme/billing/PriceCalculatorTest.java",
            "openjdk version \"1.8.0_504\"", "junit4", ("test_compile", ["mvn", "-q", "-DskipTests", "test-compile"], ".", 0));
        var manifest = Path.Combine(repo.Root, "e2e.jsonl");
        await File.WriteAllTextAsync(manifest, JsonFile.Serialize(record with { Dataset = "", RawJson = "" }, indented: false) + "\n");
        Directory.CreateDirectory(paths.ExperimentsDir);
        await File.WriteAllTextAsync(Path.Combine(paths.ExperimentsDir, "e2e.json"), JsonFile.Serialize(new ExperimentConfig
        {
            ExperimentId = "e2e",
            Kind = "pilot",
            Manifests = [new ManifestRef { Dataset = "classes2test", Path = manifest, Sha256 = Hashing.Sha256TextFile(manifest), Repair = true }],
            Models = [new ModelConfig { Name = "fake-model", Params = new() { ["temperature"] = 0 } }],
            Strategies = ["zero_shot", "zero_shot_project_aware"],
            Repair = new RepairConfig { Mechanisms = ["fixed", "adaptive"], N = 2 },
            Assessment = new AssessmentConfig { Tools = ["jacoco", "pit"], ComparisonSystems = [] },
        }));

        // Model giả: zero_shot sinh bản lỗi (COMPILE), zero_shot_project_aware sinh bản đúng, mọi lượt sửa trả bản đúng.
        // Lượt sửa đầu tiên của lần chạy đầu bấm "dừng" (như người dùng gõ s giữa chừng).
        ArrowServices? services = null;
        var stopOnce = 1;
        var fake = new FakeLlmClient()
            .When(r => r.CallId.StartsWith("gen/", StringComparison.Ordinal) && r.CallId.EndsWith("/zero_shot", StringComparison.Ordinal), _ => Broken)
            .When(r => r.CallId.StartsWith("gen/", StringComparison.Ordinal), _ => "```java\n" + Valid + "```")
            .When(r => r.CallId.StartsWith("repair/", StringComparison.Ordinal), _ =>
            {
                if (Interlocked.Exchange(ref stopOnce, 0) == 1)
                {
                    services!.Pipeline.Stop();
                }

                return Valid;
            });
        services = ServiceFactory.Create(paths, fakeLlm: () => fake);
        var events = new List<RunEvent>();
        var progress = new Progress(events);

        var first = await services.Pipeline.Start(new RunRequest { RunId = "e2e", FakeLlm = true }, progress).Completion;
        Assert.Equal(RunState.Stopped, first.State);
        var runs = new FileRunRepository(paths);
        Assert.DoesNotContain(runs.ReadLedger("e2e"), e => e.Job.StartsWith("chain/", StringComparison.Ordinal));
        Assert.Contains(runs.ReadLedger("e2e"), e => e.Job == "analyze" && e.Outcome == OutcomeKind.Ok);

        var second = await services.Pipeline.Start(new RunRequest { RunId = "e2e", FakeLlm = true }, progress).Completion;
        Assert.True(second.State == RunState.Done, second.Error + "\n" + string.Join("\n", events.Select(e => e.Message)));

        var ledger = runs.ReadLedger("e2e");
        Assert.Single(ledger, e => e.Job == "analyze");
        Assert.Single(ledger, e => e.Job == "generate/fake-model/zero_shot");
        Assert.Single(ledger, e => e.Job == "chain/fake-model/zero_shot/fixed" && e.Outcome == OutcomeKind.Ok);
        Assert.Single(ledger, e => e.Job == "chain/fake-model/zero_shot/adaptive" && e.Outcome == OutcomeKind.Ok);
        Assert.Equal(3, ledger.Count(e => e.Job.StartsWith("measure/", StringComparison.Ordinal) && e.Outcome == OutcomeKind.Ok));

        // Lượt sửa vòng 1 của Fixed đã hoàn thành ở lần chạy đầu: lần sau phát lại từ cache, không gọi model lần nữa.
        var calls = fake.Calls.GroupBy(c => c.CallId).ToDictionary(g => g.Key, g => g.Count());
        Assert.Equal(1, calls["repair/800_e2e/fake-model/zero_shot/fixed/round_1"]);
        Assert.Equal(1, calls["gen/800_e2e/fake-model/zero_shot"]);
        var results = new FileTaskResultRepository(paths);
        var key = new TaskKey("classes2test", "800_e2e");
        Assert.True(results.LoadJson<LlmUsage>("e2e", key, "block3/fake-model/zero_shot/fixed/round_1/usage.json")!.Replayed);
        Assert.Equal(VerificationState.Compile, results.LoadJson<VerifyResult>("e2e", key, ResultPaths.VerifyC0("fake-model", "zero_shot"))!.State);
        Assert.Equal(VerificationState.Valid, results.LoadJson<VerifyResult>("e2e", key, ResultPaths.VerifyC0("fake-model", "zero_shot_project_aware"))!.State);
        var adaptive = results.LoadJson<FinalTestSuite>("e2e", key, ResultPaths.Final("fake-model", "zero_shot", "adaptive"))!;
        Assert.Equal((true, 1, "second"), (adaptive.Success, adaptive.SuccessRound, adaptive.RunOrder));
        Assert.NotNull(results.LoadJson<CoverageResult>("e2e", key, "block4/fake-model__zero_shot_project_aware__initial/jacoco.json"));
        Assert.NotNull(results.LoadJson<MutationResult>("e2e", key, "block4/fake-model__zero_shot__adaptive/pit.json"));
        Assert.Equal(RunState.Done, runs.ReadStatus("e2e")!.State);

        // Báo cáo (khối 4 phần báo cáo) và các truy vấn của UI.
        var report = services.Reports.Build(["e2e"]);
        Assert.Contains("rq1_verification.csv", report.Files);
        Assert.Contains(report.Tables["rq1_verification.csv"].Rows, r => r[2] == "zero_shot_project_aware" && r[8] == "1");
        Assert.Contains(report.Tables["rq2_summary.csv"].Rows, r => r[2] == "adaptive" && r[5] == "1");
        Assert.Contains(report.Tables["rq3_quality.csv"].Rows, r => r[3] == "ARROW" && r[4] == "fake-model__zero_shot_project_aware__initial");
        Assert.True(File.Exists(Path.Combine(report.ReportsDir, "report.md")));
        Assert.Empty(services.Runs.GetRerunList("e2e"));
        var detail = services.Runs.GetTaskDetail("e2e", "800_e2e");
        Assert.Equal(2, detail.Data.Chains.Count);
        Assert.Equal("com.acme.billing.PriceCalculator", detail.Metadata!.Cut.Fqn);

        // Cấu hình thực nghiệm đổi sau khi tạo run: không cho chạy tiếp.
        var experimentFile = Path.Combine(paths.ExperimentsDir, "e2e.json");
        await File.AppendAllTextAsync(experimentFile, "\n");
        Assert.Equal(ErrorCodes.RunConfigMismatch, Assert.Throws<ArrowException>(() => services.Pipeline.Start(new RunRequest { RunId = "e2e", FakeLlm = true }, progress)).Code);

        // --shard: file phân công chia cho cấu hình cũ bị từ chối; task không có trong thực nghiệm bị báo.
        var shard = Path.Combine(repo.Root, "shard-01.json");
        string Shard(string sha, string taskId) =>
            $$"""{"schema_version":1,"plan_id":"it","shard_id":"shard-01","experiment_sha256":"{{sha}}","tasks":[{"dataset":"classes2test","task_id":"{{taskId}}"}]}""";
        await File.WriteAllTextAsync(shard, Shard(new string('0', 64), "800_e2e"));
        var stale = Assert.Throws<ArrowException>(() => services.Pipeline.Start(new RunRequest { RunId = "e2e-shard", ShardPath = shard, FakeLlm = true }, progress));
        Assert.Equal(ErrorCodes.RunConfigMismatch, stale.Code);
        Assert.Contains("shard-01.json", stale.Message);
        await File.WriteAllTextAsync(shard, Shard(Hashing.Sha256TextFile(experimentFile), "999_missing"));
        Assert.Throws<ArgumentException>(() => services.Pipeline.Start(new RunRequest { RunId = "e2e-shard", ShardPath = shard, FakeLlm = true }, progress));
    }

    private sealed class Progress(List<RunEvent> events) : IProgress<RunEvent>
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
