using Arrow.Core.Repair;
using Arrow.Core.Verify;

namespace Arrow.Core.Tests;

/// <summary>Fixed và Adaptive Repair trên kết quả xác minh giả (specs/05 Mục 9, không cần build).</summary>
public class RepairPolicyTests
{
    private static BuiltCandidate B(int round, VerificationState state, string? signature, int passed = 0, int assertions = 0, string? hash = null, params string[] failing) => new()
    {
        Round = round,
        State = state,
        Signature = signature,
        TestsPassed = passed,
        AssertionCount = assertions,
        CodeHash = hash ?? $"h{round}",
        Code = $"code{round}",
        FailingMethods = [.. failing],
    };

    private static RoundEvent Built(BuiltCandidate b) => new() { Round = b.Round, Built = b, CodeHash = b.CodeHash };

    [Fact]
    public void AdaptiveFollowsTheSpecScenario()
    {
        var c0 = B(0, VerificationState.Runtime, "RUNTIME | NullPointerException | Calc.add", 1, 2, failing: "adds");
        var policy = new AdaptiveRepairPolicy(c0);

        // c0 RUNTIME → checkpoint = c0, mức 1.
        var p1 = policy.NextPrompt();
        Assert.Equal(0, p1.Base.Round);
        Assert.Equal(1, p1.Level);
        Assert.Equal("level1", p1.PromptKind);

        // Vòng 1 COMPILE: tệ hơn → rollback, đã_thử +1, mức 2; vòng 2 dùng prompt RUNTIME mức 2, sửa từ c0.
        var d1 = policy.Apply(Built(B(1, VerificationState.Compile, "COMPILE | cannot find symbol | method")), p1);
        Assert.Equal(Comparison.Worse, d1.Comparison);
        Assert.True(d1.Rollback);
        Assert.Equal(2, d1.LevelAfter);
        Assert.Equal(0, d1.CheckpointRound);
        Assert.Single(policy.Attempts);
        var p2 = policy.NextPrompt();
        Assert.Equal("level2.runtime", p2.PromptKind);
        Assert.Equal(0, p2.Base.Round);
        Assert.Equal("COMPILE — COMPILE | cannot find symbol | method", Assert.Single(p2.Attempts).Outcome);

        // Vòng 2 ASSERTION: tốt hơn, trạng thái đổi → checkpoint = 2, mức 1, đã_thử giữ nguyên.
        var v2 = B(2, VerificationState.Assertion, "ASSERTION | AssertionError | adds", 1, 3, failing: "adds");
        var d2 = policy.Apply(Built(v2), p2);
        Assert.True(d2.Improved);
        Assert.Equal(1, d2.LevelAfter);
        Assert.Equal(2, d2.CheckpointRound);
        Assert.Single(policy.Attempts);

        // Vòng 3 mã trùng vòng 2: không build, đã_thử +1 ("mã trùng"), mức 2.
        var p3 = policy.NextPrompt();
        Assert.True(policy.IsDuplicate("h2"));
        var d3 = policy.Apply(new RoundEvent { Round = 3, DuplicateCode = true, CodeHash = "h2" }, p3);
        Assert.True(d3.DuplicateCode);
        Assert.Equal(2, d3.LevelAfter);
        Assert.Equal(FailedAttempt.Duplicate, policy.Attempts[^1].Outcome);

        // Vòng 4 ASSERTION, cùng signature, cùng số pass và assertion: Tie + trùng signature → mức 3.
        var p4 = policy.NextPrompt();
        Assert.Equal("level2.assertion", p4.PromptKind);
        var d4 = policy.Apply(Built(B(4, VerificationState.Assertion, "ASSERTION | AssertionError | adds", 1, 3, failing: "adds")), p4);
        Assert.Equal(Comparison.Tie, d4.Comparison);
        Assert.True(d4.RepeatedSignature);
        Assert.Equal(3, d4.LevelAfter);

        // Vòng 5 mức 3 (ghép method) VALID → thành công ở vòng 5.
        var p5 = policy.NextPrompt();
        Assert.Equal("level3.assertion", p5.PromptKind);
        Assert.Equal(["adds"], p5.FailingMethods);
        var d5 = policy.Apply(Built(B(5, VerificationState.Valid, null, 2, 3)), p5);
        Assert.True(d5.Success);
        Assert.True(policy.Succeeded);
    }

    [Fact]
    public void TieWithDifferentSignatureKeepsLevelAndInvalidOutputCountsAsRound()
    {
        var policy = new AdaptiveRepairPolicy(B(0, VerificationState.Assertion, "ASSERTION | AssertionError | a", 1, 2, failing: "a"));
        var d1 = policy.Apply(Built(B(1, VerificationState.Assertion, "ASSERTION | AssertionError | b", 1, 2)), policy.NextPrompt());
        Assert.Equal(Comparison.Tie, d1.Comparison);
        Assert.False(d1.Rollback);
        Assert.Equal(1, d1.LevelAfter);

        var d2 = policy.Apply(new RoundEvent { Round = 2, InvalidOutput = true }, policy.NextPrompt());
        Assert.True(d2.InvalidOutput);
        Assert.Equal(2, d2.LevelAfter);
        Assert.Equal(FailedAttempt.Invalid, policy.Attempts[^1].Outcome);
    }

    [Fact]
    public void Level3FallsBackToLevel2ForDiscoveryOrUnknownMethods()
    {
        var policy = new AdaptiveRepairPolicy(B(0, VerificationState.Discovery, "DISCOVERY | no tests executed | -"));
        policy.Apply(new RoundEvent { Round = 1, InvalidOutput = true }, policy.NextPrompt());
        policy.Apply(new RoundEvent { Round = 2, InvalidOutput = true }, policy.NextPrompt());
        var plan = policy.NextPrompt();
        Assert.Equal(3, plan.RequestedLevel);
        Assert.Equal(2, plan.Level);
        Assert.Equal("level2.discovery", plan.PromptKind);

        var compileNoMethod = new AdaptiveRepairPolicy(B(0, VerificationState.Compile, "COMPILE | package does not exist | -"));
        compileNoMethod.Apply(new RoundEvent { Round = 1, InvalidOutput = true }, compileNoMethod.NextPrompt());
        compileNoMethod.Apply(new RoundEvent { Round = 2, InvalidOutput = true }, compileNoMethod.NextPrompt());
        Assert.Equal(2, compileNoMethod.NextPrompt().Level);
    }

    [Fact]
    public void LevelStaysAtThree()
    {
        var policy = new AdaptiveRepairPolicy(B(0, VerificationState.Runtime, "RUNTIME | X | -", failing: "m"));
        for (var t = 1; t <= 5; t++)
        {
            policy.Apply(new RoundEvent { Round = t, InvalidOutput = true }, policy.NextPrompt());
        }

        Assert.Equal(3, policy.Level);
    }

    [Fact]
    public void BetterWithSameStateKeepsLevel()
    {
        var policy = new AdaptiveRepairPolicy(B(0, VerificationState.Assertion, "ASSERTION | AssertionError | a", 1, 2, failing: "a"));
        policy.Apply(new RoundEvent { Round = 1, InvalidOutput = true }, policy.NextPrompt()); // mức 2
        var d = policy.Apply(Built(B(2, VerificationState.Assertion, "ASSERTION | AssertionError | b", 2, 2)), policy.NextPrompt());
        Assert.True(d.Improved);
        Assert.Equal(2, d.LevelAfter);
    }

    [Fact]
    public void FixedAlwaysRepairsLatestAndRebuildsDuplicates()
    {
        var c0 = B(0, VerificationState.Runtime, "RUNTIME | X | -", 1, 1);
        var policy = new FixedRepairPolicy(c0);
        Assert.False(policy.SkipsDuplicates);
        Assert.False(policy.IsDuplicate("h0"));

        var worse = B(1, VerificationState.Compile, "COMPILE | other | -");
        var d1 = policy.Apply(Built(worse), policy.NextPrompt());
        Assert.Equal(Comparison.Worse, d1.Comparison);
        Assert.False(d1.Rollback);
        Assert.Equal(1, policy.NextPrompt().Base.Round); // sửa tiếp từ bản mới nhất kể cả khi tệ hơn
        Assert.Equal("level1", policy.NextPrompt().PromptKind);

        var d2 = policy.Apply(new RoundEvent { Round = 2, InvalidOutput = true }, policy.NextPrompt());
        Assert.True(d2.InvalidOutput);
        Assert.Equal(1, policy.NextPrompt().Base.Round); // output không hợp lệ: giữ bản mới nhất

        var d3 = policy.Apply(Built(B(3, VerificationState.Valid, null, 2, 2)), policy.NextPrompt());
        Assert.True(d3.Success);
    }

    [Fact]
    public void ComparerOrdersByStateThenPassedThenAssertions()
    {
        var baseCandidate = B(0, VerificationState.Assertion, "s", 2, 3);
        Assert.Equal(Comparison.Better, CheckpointComparer.Compare(B(1, VerificationState.Regression, "s"), baseCandidate));
        Assert.Equal(Comparison.Worse, CheckpointComparer.Compare(B(1, VerificationState.Runtime, "s", 9, 9), baseCandidate));
        Assert.Equal(Comparison.Better, CheckpointComparer.Compare(B(1, VerificationState.Assertion, "s", 3, 0), baseCandidate));
        Assert.Equal(Comparison.Worse, CheckpointComparer.Compare(B(1, VerificationState.Assertion, "s", 2, 2), baseCandidate));
        Assert.Equal(Comparison.Tie, CheckpointComparer.Compare(B(1, VerificationState.Assertion, "x", 2, 3), baseCandidate));
    }

    [Fact]
    public void ReplayGivesSameStateAsContinuousRun()
    {
        var c0 = B(0, VerificationState.Runtime, "RUNTIME | NPE | -", 1, 2, failing: "m");
        var events = new List<RoundEvent>
        {
            Built(B(1, VerificationState.Compile, "COMPILE | other | -")),
            new() { Round = 2, InvalidOutput = true },
            Built(B(3, VerificationState.Assertion, "ASSERTION | AssertionError | m", 1, 2, failing: "m")),
        };
        var continuous = new AdaptiveRepairPolicy(c0);
        foreach (var e in events)
        {
            continuous.Apply(e, continuous.NextPrompt());
        }

        var replayed = AdaptiveRepairPolicy.Replay(c0, events);
        Assert.Equal(continuous.Level, replayed.Level);
        Assert.Equal(continuous.Best.Round, replayed.Best.Round);
        Assert.Equal(continuous.Attempts, replayed.Attempts);
        Assert.Equal(continuous.NextPrompt().PromptKind, replayed.NextPrompt().PromptKind);
        Assert.True(replayed.IsDuplicate("h1"));
    }

    [Fact]
    public void SummaryCountsMechanismEvents()
    {
        var c0 = B(0, VerificationState.Runtime, "r", 1, 1, failing: "a");
        var policy = new AdaptiveRepairPolicy(c0);
        var decisions = new List<RepairDecision>
        {
            policy.Apply(Built(B(1, VerificationState.Compile, "c")), policy.NextPrompt()),
            policy.Apply(new RoundEvent { Round = 2, DuplicateCode = true, CodeHash = "h1" }, policy.NextPrompt()),
            policy.Apply(Built(B(3, VerificationState.Valid, null, 2, 2)), policy.NextPrompt()),
        };
        var summary = RepairSummary.From("a1", "adaptive", "first", c0, decisions, 10, policy.Result.State, ["a", "b"], ["a"]);
        Assert.True(summary.Success);
        Assert.Equal(3, summary.SuccessRound);
        Assert.Equal(1, summary.Rollbacks);
        Assert.Equal(1, summary.DuplicateCode);
        Assert.Equal(2, summary.LevelUps);
        Assert.Equal(["b"], summary.DroppedMethods);
        Assert.Equal(VerificationState.Valid, summary.FinalState);
    }
}

public class NRuleTests
{
    private static readonly int[] Readme = [40, 58, 66, 70, 72, 73, 73, 73, 74, 74];

    [Fact]
    public void ReadmeExample()
    {
        Assert.Equal(5, Arrow.Core.Report.NRules.Share(Readme, 0.95));
        Assert.Equal(8, Arrow.Core.Report.NRules.Report2(Readme));
        Assert.Equal(5, Arrow.Core.Report.NRules.Apply("share95", Readme));
        Assert.Equal(4, Arrow.Core.Report.NRules.Share(Readme, 0.90));
        Assert.Equal(9, Arrow.Core.Report.NRules.Share(Readme, 0.99));
    }

    [Fact]
    public void NoSuccessGivesTen()
    {
        var zero = new int[10];
        Assert.Equal(10, Arrow.Core.Report.NRules.Share(zero));
        Assert.Equal(5, Arrow.Core.Report.NRules.Report2(zero)); // S không đổi từ đầu: vòng 4 và 5 không thêm ca nào
    }

    [Fact]
    public void MinimumThreeAndCumulative()
    {
        var s = Arrow.Core.Report.NRules.Cumulative([1, 1, 1, 2, null, null], 10);
        Assert.Equal([3, 4, 4, 4, 4, 4, 4, 4, 4, 4], s);
        Assert.Equal(3, Arrow.Core.Report.NRules.Share(s));
        Assert.Equal(10, Arrow.Core.Report.NRules.Report2([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]));
    }
}

public class QualityParserTests
{
    [Fact]
    public void JacocoCsvForCutIncludingInnerClasses()
    {
        const string csv = """
            GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
            g,a,Other,1,1,0,0,1,1,1,1,1,1
            g,a,Calc,12,16,1,1,3,6,3,3,2,3
            g,a,Calc.Inner,0,4,0,0,0,1,0,1,0,1
            """;
        var result = Arrow.Core.Quality.QualityParsers.ParseJacocoCsv(csv, "a.Calc")!;
        Assert.Equal(7.0 / 10, result.Line!.Value, 4);
        Assert.Equal(0.5, result.Branch);
        Assert.Equal(4.0 / 6, result.Method!.Value, 4);
        Assert.Null(Arrow.Core.Quality.QualityParsers.ParseJacocoCsv(csv, "a.Missing"));
        var noBranches = Arrow.Core.Quality.QualityParsers.ParseJacocoCsv(csv.Replace("g,a,Calc,12,16,1,1", "g,a,Calc,12,16,0,0"), "a.Calc")!;
        Assert.Null(noBranches.Branch);
    }

    [Fact]
    public void PitCsvScoreAndStrength()
    {
        const string csv = """
            Calc.java,a.Calc,M1,add,1,KILLED,t
            Calc.java,a.Calc,M2,add,2,SURVIVED,none
            Calc.java,a.Calc,M3,div,3,NO_COVERAGE,none
            Calc.java,a.Calc$1,M4,run,4,TIMED_OUT,t
            Calc.java,a.Calc,M5,div,5,NON_VIABLE,none
            Other.java,a.Other,M1,x,1,KILLED,t
            """;
        var result = Arrow.Core.Quality.QualityParsers.ParsePitCsv(csv, "a.Calc")!;
        Assert.Equal(4, result.Total);
        Assert.Equal(2, result.Killed);
        Assert.Equal(0.5, result.MutationScore);
        Assert.Equal(Math.Round(2.0 / 3, 4), result.TestStrength);
    }

    [Fact]
    public void TsDetectCsv()
    {
        const string csv = """
            App,TestClass,TestFilePath,ProductionFilePath,RelativeTestFilePath,RelativeProductionFilePath,NumberOfMethods,Assertion Roulette,Magic Number Test,Eager Test
            app,CalcTest.java,/t/CalcTest.java,/m/Calc.java,,,2,1,2,0
            """;
        var result = Arrow.Core.Quality.QualityParsers.ParseTsDetectCsv(csv)!;
        Assert.Equal(3, result.TotalSmells);
        Assert.Equal(2, result.TestMethods);
        Assert.Equal(2, result.BySmell["Magic Number Test"]);
    }
}

public class PilotReportTests
{
    [Fact]
    public void PercentilesAndCostRows()
    {
        Assert.Equal(2.5, Arrow.Core.Report.PilotReport.Percentile([1, 2, 3, 4], 0.5));
        Assert.Equal(3.7, Arrow.Core.Report.PilotReport.Percentile([1, 2, 3, 4], 0.9));
        var rows = Arrow.Core.Report.PilotReport.Costs(
        [
            new("llm_generate", "c2t", 2, 100, 20, 0.01),
            new("llm_generate", "c2t", 4, 50, 10, 0.02),
            new("clone", "c2t", 10),
        ]);
        Assert.Equal("clone", rows[0].Step);
        var gen = rows[1];
        Assert.Equal(2, gen.Count);
        Assert.Equal(3, gen.MedianSeconds);
        Assert.Equal(150, gen.PromptTokens);
        Assert.Equal(0.03, gen.CostUsd, 6);
    }

    [Fact]
    public void NIsNotComputedWhileChainsAreUnfinished()
    {
        var chains = new List<Arrow.Core.Report.RepairChain>
        {
            new("t1", "zero_shot", "fixed", true, 1, false),
            new("t1", "zero_shot", "adaptive", false, null, false),
            new("t2", "few_shot", "fixed", false, null, true),
        };
        var selection = Arrow.Core.Report.PilotReport.SelectN(chains, 10, "share95");
        Assert.False(selection.Computable);
        Assert.Null(selection.N);
        Assert.Equal(1, selection.Unfinished);
        Assert.Equal(1, selection.ExcludedInfra);

        chains[1] = chains[1] with { Finished = true };
        var done = Arrow.Core.Report.PilotReport.SelectN(chains, 10, "share95");
        Assert.True(done.Computable);
        Assert.Equal(3, done.N);
        Assert.Equal("round,successes_cumulative,share_of_round10\n1,1,1\n", Arrow.Core.Report.PilotReport.CumulativeCsv(done).Split("2,")[0]);
    }

    [Fact]
    public void CsvQuotesCells() =>
        Assert.Equal("a,b\n\"x,y\",\"say \"\"hi\"\"\"\n", Arrow.Core.Report.PilotReport.Csv(new[] { ("x,y", "say \"hi\"") }, ("a", r => r.Item1), ("b", r => r.Item2)));
}
