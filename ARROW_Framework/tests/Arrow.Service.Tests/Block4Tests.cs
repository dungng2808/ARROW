using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Run;
using Arrow.Service.Block4;

namespace Arrow.Service.Tests;

/// <summary>Khối 4 (spec 04 Mục 9): đọc kết quả công cụ, công thức RQ1–RQ3, quy tắc N, độ nhạy P.</summary>
public class Block4Tests
{
    // ── công cụ ────────────────────────────────────────────────────────

    [Fact]
    public void JacocoTakesCutAndNestedClassesOnly()
    {
        const string csv = """
            GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
            app,com.acme.shop,Order,10,40,4,6,5,20,3,7,1,6
            app,com.acme.shop,Order.Item,2,8,0,0,1,4,0,2,0,2
            app,com.acme.shop,OrderService,100,0,10,0,30,0,10,0,8,0
            app,com.acme.other,Order,50,0,2,0,9,0,1,0,1,0
            """;
        var coverage = JacocoCsvParser.Parse(csv, "com.acme.shop.Order")!;
        Assert.Equal(new Dictionary<string, int>
        {
            ["LINE_COVERED"] = 24, ["LINE_MISSED"] = 6, ["BRANCH_COVERED"] = 6, ["BRANCH_MISSED"] = 4, ["METHOD_COVERED"] = 8, ["METHOD_MISSED"] = 1,
        }, coverage.Counters);
        Assert.Equal((0.8, 0.6, 0.8889), (coverage.Line!.Value, coverage.Branch!.Value, coverage.Method!.Value));

        // CUT không có nhánh → branch null (không tính là 0); CUT không có trong báo cáo → null.
        var noBranches = JacocoCsvParser.Parse(csv.Replace("4,6,5,20", "0,0,5,20"), "com.acme.shop.Order")!;
        Assert.Null(noBranches.Branch);
        Assert.Null(JacocoCsvParser.Parse(csv, "com.acme.shop.Missing"));
    }

    [Fact]
    public void PitCountsStatusesAndDetectsOutOfScopeMutants()
    {
        static string M(string status, string cls = "com.acme.shop.Order") => $"<mutation detected='{status is "KILLED" or "TIMED_OUT"}' status='{status}'><mutatedClass>{cls}</mutatedClass></mutation>";
        var statuses = Enumerable.Repeat("KILLED", 30).Concat(Enumerable.Repeat("SURVIVED", 12)).Concat(Enumerable.Repeat("NO_COVERAGE", 10)).Concat(Enumerable.Repeat("TIMED_OUT", 2));
        var xml = "<mutations>" + string.Concat(statuses.Select((s, i) => M(s, i == 0 ? "com.acme.shop.Order$Item" : "com.acme.shop.Order"))) + "</mutations>";
        var (result, outOfScope) = PitXmlParser.Parse(xml, "com.acme.shop.Order");
        // Ví dụ spec 04 Mục 10.3: score = (30 + 2) / 54, test strength = 30 / (54 − 10).
        Assert.Equal((54, 30, 12, 10, 2), (result.Total, result.Killed, result.Survived, result.NoCoverage, result.TimedOut));
        Assert.Equal(0.5926, result.Score);
        Assert.Equal(0.6818, result.TestStrength);
        Assert.Empty(outOfScope);

        var leaked = PitXmlParser.Parse("<mutations>" + M("KILLED", "com.acme.shop.OrderService") + "</mutations>", "com.acme.shop.Order");
        Assert.Equal(["com.acme.shop.OrderService"], leaked.OutOfScope);
        Assert.Null(PitXmlParser.Parse("<mutations/>", "com.acme.shop.Order").Result.Score);
    }

    [Fact]
    public void TsDetectSmellsAndDensity()
    {
        const string csv = """
            App,TestClass,TestFilePath,ProductionFilePath,RelativeTestFilePath,RelativeProductionFilePath,NumberOfMethods,Assertion Roulette,Conditional Test Logic,Magic Number Test,Empty Test
            app,OrderService_ArrowTest,/t/OrderService_ArrowTest.java,/m/OrderService.java,,,5,3,0,1,false
            """;
        var smells = TsDetectCsvParser.Parse(csv, 5)!;
        Assert.Equal(new Dictionary<string, int> { ["Assertion Roulette"] = 3, ["Magic Number Test"] = 1 }, smells.Smells);
        Assert.Equal((4, 0.8), (smells.TotalSmells, smells.Density!.Value));
        Assert.Null(TsDetectCsvParser.Parse(csv, 0)!.Density);
    }

    [Fact]
    public void PlatformVersionFromClasspath()
    {
        Assert.Equal("1.10.2", PitTool.PlatformVersion(["/m2/org/junit/platform/junit-platform-engine-1.10.2.jar", "/m2/junit-jupiter-api-5.10.2.jar"]));
        Assert.Null(PitTool.PlatformVersion(["/m2/junit-4.13.2.jar"]));
    }

    // ── quy tắc N, độ nhạy P ───────────────────────────────────────────

    [Fact]
    public void NRuleFindsFirstFlatPairFromRoundFive()
    {
        // S(1..10): vòng 5 và 6 không thêm chain nào (S(4) = S(5) = S(6)) → N = 6.
        Assert.Equal(6, NRule.Choose([3, 6, 8, 10, 10, 10, 11, 11, 11, 11]));
        // S(3) = S(4) = S(5) → k = 5 thỏa ngay.
        Assert.Equal(5, NRule.Choose([3, 5, 7, 7, 7, 8, 9, 10, 11, 12]));
        // Không có k nào thỏa → N = 10.
        Assert.Equal(10, NRule.Choose([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]));
    }

    private static RoundDecision D(int round, bool improved, double llm = 1, double build = 2) => new() { Round = round, Improved = improved, LlmSeconds = llm, BuildSeconds = build };

    [Fact]
    public void PSensitivityCutsChainAfterPRoundsWithoutImprovement()
    {
        // Thành công ở vòng 5; vòng 2, 3 không tốt hơn; vòng 4 tốt hơn.
        var final = new FinalTestSuite { Success = true, SuccessRound = 5, RoundsRun = 5 };
        var decisions = new List<RoundDecision> { D(1, true), D(2, false), D(3, false), D(4, true), D(5, true) };
        Assert.Equal(new PSensitivity.Outcome(false, 2, 6), PSensitivity.Simulate(final, decisions, 1));
        Assert.Equal(new PSensitivity.Outcome(false, 3, 9), PSensitivity.Simulate(final, decisions, 2));
        Assert.Equal(new PSensitivity.Outcome(true, 5, 15), PSensitivity.Simulate(final, decisions, 3));
    }

    // ── báo cáo trên dữ liệu mẫu ───────────────────────────────────────

    private static readonly ExperimentConfig Experiment = new()
    {
        ExperimentId = "pilot-test", Kind = "pilot",
        Models = [new ModelConfig { Name = "m" }], Strategies = ["zero_shot", "zero_shot_project_aware"],
        Repair = new RepairConfig { Mechanisms = ["fixed", "adaptive"], N = 10 },
    };

    private static CandidateData Candidate(string strategy, VerificationState? state, bool valid = true) => new()
    {
        Model = "m", Strategy = strategy,
        Candidate = new CandidateTest { ValidOutput = valid, InvalidReason = valid ? null : "no_code" },
        Usage = new LlmUsage { ModelId = "m-2026", PromptTokens = 100, CompletionTokens = 50, CostUsd = 0.001, LatencySeconds = 2 },
        VerifyC0 = state is null ? null : new VerifyResult { State = state.Value, ErrorSignature = state == VerificationState.Valid ? null : $"{state} | x | -" },
    };

    private static ChainData Chain(string task, string mechanism, bool success, int round, string stop = "budget") => new()
    {
        Final = new FinalTestSuite
        {
            TaskId = task, Model = "m", Strategy = "zero_shot", Mechanism = mechanism, Success = success, SuccessRound = success ? round : 10,
            RoundsRun = success ? round : 10, StopReason = success ? "valid" : stop, RepairSeconds = 10 * (success ? round : 10),
            Counters = new RepairCounters { Rollback = mechanism == "adaptive" ? 1 : 0 },
        },
        Decisions = Enumerable.Range(1, success ? round : 10).Select(r => D(r, r == round && success)).ToList(),
    };

    private static SuiteData Suite(string id, double? line, double? branch, double? score, double? density) => new()
    {
        SuiteId = id,
        Coverage = line is null ? null : new CoverageResult { Line = line, Branch = branch, Method = line },
        Mutation = score is null ? null : new MutationResult { Total = 10, Score = score },
        Smells = density is null ? null : new SmellResult { TestMethods = 2, TotalSmells = 1, Density = density, Smells = new() { ["Magic Number Test"] = 1 } },
        Outcome = new QualityOutcome { SuiteId = id, Tools = new() { ["jacoco"] = new BlockOutcome { Outcome = line is null ? OutcomeKind.Failed : OutcomeKind.Ok, Seconds = 12 } } },
    };

    private static TaskData Task(string id, IEnumerable<CandidateData>? candidates = null, IEnumerable<ChainData>? chains = null, IEnumerable<SuiteData>? suites = null, OutcomeKind block1 = OutcomeKind.Ok) => new()
    {
        Key = new TaskKey("classes2test", id),
        Block1 = new BlockOutcome { Outcome = block1, Code = block1 == OutcomeKind.Excluded ? "B1_CUT_MISSING" : null },
        Candidates = [.. candidates ?? []],
        Chains = [.. chains ?? []],
        Suites = [.. suites ?? []],
    };

    private static UnifiedReport Report(params TaskData[] tasks) => ReportBuilder.Build(
        [new RunData { RunId = "r1", Lock = new RunLock { ExperimentSha256 = "abc" }, Tasks = [.. tasks] }], Experiment);

    private static IReadOnlyList<string> Row(CsvTable table, Func<IReadOnlyList<string>, bool> match) => table.Rows.Single(match);

    [Fact]
    public void Rq1CountsStepsInvalidOutputAndInfra()
    {
        var report = Report(
            Task("t1", [Candidate("zero_shot", VerificationState.Compile), Candidate("zero_shot_project_aware", VerificationState.Valid)]),
            Task("t2", [Candidate("zero_shot", null, valid: false), Candidate("zero_shot_project_aware", VerificationState.Assertion)]),
            Task("t3", [Candidate("zero_shot", VerificationState.Infra), Candidate("zero_shot_project_aware", VerificationState.Regression)]),
            Task("t4", [Candidate("zero_shot", VerificationState.Valid)], block1: OutcomeKind.Excluded));
        var table = report.Csv["rq1_verification.csv"];
        // zero_shot: t1 COMPILE, t2 INVALID_OUTPUT (tính vào mẫu số), t3 INFRA (không tính), t4 bị loại.
        Assert.Equal(["classes2test", "m", "zero_shot", "2", "0", "0", "0", "0", "0", "1", "1"], Row(table, r => r[2] == "zero_shot"));
        Assert.Equal(["classes2test", "m", "zero_shot_project_aware", "3", "3", "3", "3", "2", "1", "0", "0"], Row(table, r => r[2] == "zero_shot_project_aware"));
        Assert.Equal(["classes2test", "t4", "B1_CUT_MISSING", ""], Assert.Single(report.Csv["excluded.csv"].Rows));
    }

    [Fact]
    public void Rq2DropsUnfinishedCandidatesAndComputesCumulativeRates()
    {
        var report = Report(
            Task("t1", chains: [Chain("t1", "fixed", true, 3), Chain("t1", "adaptive", true, 2)]),
            Task("t2", chains: [Chain("t2", "fixed", false, 0), Chain("t2", "adaptive", true, 4)]),
            Task("t3", chains: [Chain("t3", "fixed", true, 1), Chain("t3", "adaptive", false, 0, stop: "infra")]),
            Task("t4", chains: [Chain("t4", "adaptive", true, 1)]));
        // t3 có chain INFRA, t4 thiếu chain Fixed → bỏ cả candidate.
        Assert.Equal(4, report.Csv["rq2_chains.csv"].Rows.Count);
        var summary = report.Csv["rq2_summary.csv"];
        Assert.Equal(["classes2test", "m", "adaptive", "2", "2", "1", "3", "3", "30", "30"], Row(summary, r => r[2] == "adaptive"));
        Assert.Equal(["classes2test", "m", "fixed", "2", "1", "0.5", "6.5", "6.5", "65", "65"], Row(summary, r => r[2] == "fixed"));

        var cumulative = report.Csv["rq2_cumulative.csv"];
        Assert.Equal("0.5", Row(cumulative, r => r[2] == "adaptive" && r[3] == "2")[5]);
        Assert.Equal("1", Row(cumulative, r => r[2] == "adaptive" && r[3] == "4")[5]);

        // Pilot: S(k) gộp hai cơ chế: vòng 2 → 1, vòng 3 → 2, vòng 4 → 3; S(3..10) không đổi sau vòng 4 → N = 6.
        var pilot = report.Csv["pilot_n.csv"];
        Assert.Equal(["4", "3", "1"], pilot.Rows[3]);
        Assert.Contains("N = 6", report.Markdown);
        Assert.Equal("2", Row(report.Csv["rq2_mechanisms.csv"], r => r[2] == "adaptive")[3]);
    }

    [Fact]
    public void Rq3PairsPerMetricWithRepresentativeSuite()
    {
        var report = Report(
            // t1: đại diện là bộ initial; bộ adaptive của zero_shot chỉ báo cáo kèm.
            Task("t1", suites: [Suite("m__zero_shot_project_aware__initial", 0.8, 0.5, 0.6, 1.0), Suite("m__zero_shot__adaptive", 0.1, 0.1, 0.1, 0.1), Suite("evosuite", 0.9, 0.7, 0.4, 2.0)]),
            // t2: không có initial → đại diện là bộ adaptive; PIT của EvoSuite lỗi.
            Task("t2", suites: [Suite("m__zero_shot_project_aware__adaptive", 0.6, null, 0.5, 0.5), Suite("evosuite", 0.7, 0.4, null, 1.0)]),
            // t3: chỉ EvoSuite có bộ VALID.
            Task("t3", suites: [Suite("evosuite", 0.5, 0.5, 0.5, 0.5)]));

        var quality = report.Csv["rq3_quality.csv"];
        Assert.Equal("ARROW", Row(quality, r => r[4] == "m__zero_shot_project_aware__initial")[3]);
        Assert.Equal("ARROW-other", Row(quality, r => r[4] == "m__zero_shot__adaptive")[3]);
        Assert.Equal("ARROW", Row(quality, r => r[4] == "m__zero_shot_project_aware__adaptive")[3]);

        var summary = report.Csv["rq3_summary.csv"];
        Assert.Equal(["2", "0.7", "0.8", "0.7", "0.8", "-0.1"], Row(summary, r => r[2] == "line").Skip(3).Take(6));
        Assert.Equal("1", Row(summary, r => r[2] == "branch")[3]);          // t2 ARROW branch null
        Assert.Equal("1", Row(summary, r => r[2] == "mutation_score")[3]);  // t2 PIT của EvoSuite lỗi
        Assert.Equal(["classes2test", "m", "2", "0", "1", "0", "1", "0", "1", "0"], Assert.Single(report.Csv["rq3_availability.csv"].Rows));
    }

    [Fact]
    public void MergingRunsWithDifferentExperimentsIsRejected()
    {
        var runs = new List<RunData>
        {
            new() { RunId = "a", Lock = new RunLock { ExperimentSha256 = "x" } },
            new() { RunId = "b", Lock = new RunLock { ExperimentSha256 = "y" } },
        };
        Assert.Equal(ErrorCodes.B4ReportMismatch, Assert.Throws<ArrowException>(() => ReportBuilder.Build(runs, Experiment)).Code);

        var duplicate = new List<RunData>
        {
            new() { RunId = "a", Lock = new RunLock { ExperimentSha256 = "x" }, Tasks = [Task("t1")] },
            new() { RunId = "b", Lock = new RunLock { ExperimentSha256 = "x" }, Tasks = [Task("t1")] },
        };
        Assert.Throws<ArrowException>(() => ReportBuilder.Build(duplicate, Experiment));
    }

    [Fact]
    public void NeedsRerunListsLatestInfraJobsWithCommand()
    {
        var at = DateTimeOffset.Parse("2026-10-14T10:00:00Z");
        var run = new RunData
        {
            RunId = "run-1",
            Lock = new RunLock { ExperimentSha256 = "x" },
            Ledger =
            [
                new LedgerEntry { At = at, Block = 3, Dataset = "classes2test", TaskId = "t1", Job = "chain/m/zero_shot/adaptive", Outcome = OutcomeKind.Infra, Code = "B3_TIMEOUT", Message = "vòng 2: B3_TIMEOUT: run_target quá 900 s", NeedsRerun = true },
                new LedgerEntry { At = at.AddHours(1), Block = 3, Dataset = "classes2test", TaskId = "t1", Job = "chain/m/zero_shot/adaptive", Outcome = OutcomeKind.Infra, Code = "B3_TIMEOUT", Message = "vòng 2: run_target quá 900 s", NeedsRerun = true },
                new LedgerEntry { At = at, Block = 1, Dataset = "classes2test", TaskId = "t2", Job = "analyze", Outcome = OutcomeKind.Infra, Code = "B1_BASELINE_TIMEOUT", NeedsRerun = true },
                new LedgerEntry { At = at.AddHours(1), Block = 1, Dataset = "classes2test", TaskId = "t2", Job = "analyze", Outcome = OutcomeKind.Ok },
            ],
        };
        var rows = ReportBuilder.Build([run], Experiment).Csv["needs_rerun.csv"].Rows;
        Assert.Equal(["t1", "classes2test", "3", "chain/m/zero_shot/adaptive", "run_target", "B3_TIMEOUT", "3", "", "true", "arrow-fw run --run-id run-1 --tasks t1 --rerun-infra"], Assert.Single(rows));
    }
}
