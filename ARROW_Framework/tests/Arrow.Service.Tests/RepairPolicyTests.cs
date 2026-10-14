using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block3;

namespace Arrow.Service.Tests;

/// <summary>Thuật toán Fixed/Adaptive (spec 03 Mục 6–7, 14.4–14.5, 15; Q5) bằng các ví dụ của spec.</summary>
public class RepairPolicyTests
{
    private static VerifyResult V(VerificationState state, string? signature = null, int passed = 0, int assertions = 0, params string[] failing) => new()
    {
        State = state,
        ErrorSignature = signature ?? (state == VerificationState.Valid ? null : $"{state.ToString().ToUpperInvariant()} | x | -"),
        TestMethods = new TestMethodCounts { Total = 5, Passed = passed },
        AssertionCount = assertions,
        FailingMethods = [.. failing],
    };

    private static CandidateTest C(string code) => new()
    {
        ValidOutput = true, Code = code, CodeHash = "sha256:" + code.GetHashCode().ToString("x"), TestFqn = "a.X_ArrowTest", TestPath = "src/test/java/a/X_ArrowTest.java",
    };

    private static readonly CandidateTest Invalid = new() { ValidOutput = false, InvalidReason = "no_code" };

    private static Diagnosis D(VerifyResult v) => new()
    {
        State = v.State, ErrorSignature = v.ErrorSignature, FailingMethods = [.. v.FailingMethods], ErrorBlock = $"error of {v.ErrorSignature}", FirstError = $"first error of {v.ErrorSignature}",
    };

    private static ChainVersion Version(int round, string code, VerifyResult v) => new(round, C(code), v, D(v));

    /// <summary>Chạy một vòng: lấy request, kiểm tra mã trùng, áp kết quả.</summary>
    private static (RepairRequest Request, RoundDecision Decision) Round(IRepairPolicy policy, int round, CandidateTest candidate, VerifyResult? verify)
    {
        var request = policy.NextRequest(round);
        var duplicate = candidate.ValidOutput ? policy.DuplicateOf(candidate.CodeHash) : null;
        var built = candidate.ValidOutput && duplicate is null;
        var decision = policy.Apply(new RoundOutcome
        {
            Round = round, Request = request, Candidate = candidate, DuplicateOf = duplicate,
            Verify = built ? verify : null, Diagnosis = built && verify is not null ? D(verify) : null,
        });
        return (request, decision);
    }

    [Fact]
    public void Section7TableAdaptive()
    {
        var c0 = Version(0, "c0", V(VerificationState.Compile, "COMPILE | cannot find symbol | method"));
        var policy = new AdaptiveRepairPolicy(c0);

        // 1: RUNTIME → tốt hơn, đổi trạng thái → checkpoint r1, mức 1.
        var (req1, d1) = Round(policy, 1, C("r1"), V(VerificationState.Runtime, "RUNTIME | NullPointerException | A.b"));
        Assert.Equal((1, 0, VerificationState.Compile), (req1.Level, req1.BaseRound, req1.State));
        Assert.Equal((CheckpointComparer.Better, true, 1, 1), (d1.Comparison, d1.Improved, d1.LevelAfter, d1.CheckpointRound));

        // 2: COMPILE → tệ hơn → rollback, đã thử += r2, mức 2.
        var (req2, d2) = Round(policy, 2, C("r2"), V(VerificationState.Compile, "COMPILE | incompatible types | -"));
        Assert.Equal((1, 1, VerificationState.Runtime), (req2.Level, req2.BaseRound, req2.State));
        Assert.Equal((CheckpointComparer.Worse, true, 2, 1), (d2.Comparison, d2.Rollback, d2.LevelAfter, d2.CheckpointRound));

        // 3: mã trùng r2 → không build, đã thử += r3, mức 3.
        var (req3, d3) = Round(policy, 3, C("r2"), null);
        Assert.Equal(2, req3.Level);
        Assert.Single(req3.FailedAttempts);
        Assert.Equal((true, (VerificationState?)null, 3), (d3.DuplicateCode, d3.State, d3.LevelAfter));
        Assert.Equal("duplicate code (same as round 2)", policy.Attempts[^1].Signature);

        // 4: ASSERTION → tốt hơn → checkpoint r4, mức 1. Vòng này checkpoint RUNTIME có method lỗi? Không → mức 3 thành mức 2.
        var (req4, d4) = Round(policy, 4, C("r4"), V(VerificationState.Assertion, "ASSERTION | AssertionError | t1", passed: 4, failing: "t1"));
        Assert.Equal((3, 2), (req4.RequestedLevel, req4.Level));
        Assert.Equal((true, 1, 4), (d4.Improved, d4.LevelAfter, d4.CheckpointRound));

        // 5: ASSERTION cùng signature, ít test pass hơn → tệ hơn + lặp → mức 2.
        var (_, d5) = Round(policy, 5, C("r5"), V(VerificationState.Assertion, "ASSERTION | AssertionError | t1", passed: 3, failing: "t1"));
        Assert.Equal((true, true, 2, 4), (d5.Rollback, d5.RepeatedSignature, d5.LevelAfter, d5.CheckpointRound));

        // 6: VALID → thành công.
        var (req6, d6) = Round(policy, 6, C("r6"), V(VerificationState.Valid, passed: 5));
        Assert.Equal((2, 4), (req6.Level, req6.BaseRound));
        Assert.Equal(3, req6.FailedAttempts.Count);
        Assert.True(d6.Improved);
        Assert.Equal(VerificationState.Valid, policy.Current.State);
        Assert.Equal(new RepairCounters { Rollback = 2, DuplicateCode = 1, RepeatedSignature = 1, LevelUp = 3, StateChange = 3 }, policy.Counters);
    }

    [Fact]
    public void Section7TableFixed()
    {
        var c0 = Version(0, "c0", V(VerificationState.Compile));
        var policy = new FixedRepairPolicy(c0);
        var results = new[]
        {
            V(VerificationState.Runtime), V(VerificationState.Compile), V(VerificationState.Compile), V(VerificationState.Assertion, passed: 4), V(VerificationState.Assertion, passed: 3), V(VerificationState.Valid),
        };
        var codes = new[] { "r1", "r2", "r2", "r4", "r5", "r6" };
        for (var i = 0; i < results.Length; i++)
        {
            var (request, decision) = Round(policy, i + 1, C(codes[i]), results[i]);
            // Luôn mức 1, luôn sửa từ bản mới nhất (kể cả bản tệ hơn), mã trùng vẫn build.
            Assert.Equal((1, i, false, false), (request.Level, request.BaseRound, decision.DuplicateCode, decision.Rollback));
            Assert.Equal(results[i].State, decision.State);
            Assert.Empty(request.FailedAttempts);
            Assert.Null(policy.DuplicateOf(C(codes[i]).CodeHash));
        }

        Assert.Equal(6, policy.Current.Round);
    }

    [Fact]
    public void FixedKeepsLatestOnInvalidOutputAndRepairsFromWorseVersion()
    {
        var policy = new FixedRepairPolicy(Version(0, "c0", V(VerificationState.Assertion, passed: 4)));
        Round(policy, 1, Invalid, null);
        Assert.Equal(0, policy.NextRequest(2).BaseRound);
        Round(policy, 2, C("worse"), V(VerificationState.Compile));
        var next = policy.NextRequest(3);
        Assert.Equal((2, VerificationState.Compile, "worse"), (next.BaseRound, next.State, next.BaseCode));
        Assert.Equal(1, policy.Counters.InvalidOutput);
    }

    [Fact]
    public void Section14_5AdaptiveChain()
    {
        var getTotal = "COMPILE | cannot find symbol | method";
        var policy = new AdaptiveRepairPolicy(Version(0, "c0", V(VerificationState.Compile, getTotal, failing: "totalPriceAddsTax")));

        // Vòng 1: COMPILE, lỗi khác (constructor) → ngang, khác signature → giữ mức 1.
        var (_, d1) = Round(policy, 1, C("r1"), V(VerificationState.Compile, "COMPILE | constructor cannot be applied | -", failing: "totalPriceAddsTax"));
        Assert.Equal((CheckpointComparer.Equal, false, false, 1), (d1.Comparison, d1.Rollback, d1.RepeatedSignature, d1.LevelAfter));

        // Vòng 2: prompt giống vòng 1 → model trả lại bản vòng 1 → mã trùng → mức 2.
        var (req2, d2) = Round(policy, 2, C("r1"), null);
        Assert.Equal(1, req2.Level);
        Assert.Equal((true, 2), (d2.DuplicateCode, d2.LevelAfter));

        // Vòng 3: mức 2 (API thật + đã thử vòng 1, 2) → ASSERTION → checkpoint vòng 3, về mức 1.
        var (req3, d3) = Round(policy, 3, C("r3"), V(VerificationState.Assertion, "ASSERTION | AssertionError | totalPriceAddsTax", passed: 4, assertions: 7, failing: "totalPriceAddsTax"));
        Assert.Equal(2, req3.Level);
        Assert.Equal(["COMPILE | constructor cannot be applied | -", "duplicate code (same as round 1)"], req3.FailedAttempts.Select(a => a.Signature));
        Assert.Equal((true, 1, 3), (d3.Improved, d3.LevelAfter, d3.CheckpointRound));

        // Vòng 4: ASSERTION cùng signature, cùng 4/5 → lỗi lặp → mức 2.
        var (req4, d4) = Round(policy, 4, C("r4"), V(VerificationState.Assertion, "ASSERTION | AssertionError | totalPriceAddsTax", passed: 4, assertions: 7, failing: "totalPriceAddsTax"));
        Assert.Equal((1, VerificationState.Assertion, 3), (req4.Level, req4.State, req4.BaseRound));
        Assert.Equal((CheckpointComparer.Equal, false, true, 2), (d4.Comparison, d4.Rollback, d4.RepeatedSignature, d4.LevelAfter));

        // Vòng 5: mức 2 ASSERTION → VALID.
        var (req5, d5) = Round(policy, 5, C("r5"), V(VerificationState.Valid, passed: 5));
        Assert.Equal((2, 3), (req5.Level, req5.BaseRound));
        Assert.True(d5.Improved);
    }

    [Fact]
    public void Q5LevelThreeRotatesBackToTwoWithLongerAttemptList()
    {
        var policy = new AdaptiveRepairPolicy(Version(0, "c0", V(VerificationState.Compile, "COMPILE | a | -", failing: "m1")));
        Round(policy, 1, C("r1"), V(VerificationState.Compile, "COMPILE | a | -", failing: "m1"));   // lặp → 2
        Round(policy, 2, C("r2"), V(VerificationState.Compile, "COMPILE | a | -", failing: "m1"));   // lặp → 3
        var request3 = policy.NextRequest(3);
        Assert.Equal((3, 3), (request3.RequestedLevel, request3.Level));
        Assert.Equal(["m1"], request3.RewriteMethods);
        Assert.Equal(2, request3.FailedAttempts.Count);

        // (a) mức 3, mã trùng → về mức 2; prompt vòng sau có thêm một lần thử nên khác vòng trước.
        var (_, d3) = Round(policy, 3, C("r2"), null);
        Assert.Equal((3, true, 2), (d3.Level, d3.DuplicateCode, d3.LevelAfter));
        var request4 = policy.NextRequest(4);
        Assert.Equal((2, 3), (request4.Level, request4.FailedAttempts.Count));
        Assert.Empty(request4.RewriteMethods);
        Assert.Equal(1, policy.Counters.Level3Rounds);
    }

    [Fact]
    public void Q5LevelThreeNotApplicableUsesLevelTwo()
    {
        // (b) mức 3 nhưng checkpoint ở DISCOVERY → vòng đó dùng mức 2.
        var policy = new AdaptiveRepairPolicy(Version(0, "c0", V(VerificationState.Discovery, "DISCOVERY | no tests executed | -")));
        Round(policy, 1, C("r1"), V(VerificationState.Discovery, "DISCOVERY | no tests executed | -"));
        Round(policy, 2, C("r2"), V(VerificationState.Discovery, "DISCOVERY | no tests executed | -"));
        var request = policy.NextRequest(3);
        Assert.Equal((3, 2), (request.RequestedLevel, request.Level));
        Assert.Empty(request.RewriteMethods);

        // COMPILE nhưng không biết method lỗi → cũng mức 2.
        var unknown = new AdaptiveRepairPolicy(Version(0, "c0", V(VerificationState.Compile, "COMPILE | a | -")));
        Assert.Equal(2, AdaptiveRepairPolicy.EffectiveLevel(3, Version(0, "c0", V(VerificationState.Compile))));
        Assert.Equal(1, unknown.NextRequest(1).Level);
    }

    [Fact]
    public void InvalidOutputCountsAsRoundAndRaisesLevel()
    {
        var policy = new AdaptiveRepairPolicy(Version(0, "c0", V(VerificationState.Compile)));
        var (_, decision) = Round(policy, 1, Invalid, null);
        Assert.Equal((true, 2, 0), (decision.InvalidOutput, decision.LevelAfter, decision.CheckpointRound));
        Assert.Equal("invalid output: no Java class found", policy.Attempts[0].Signature);
        Assert.Null(policy.Attempts[0].Change);
    }

    [Theory]
    // Spec 03 Mục 15.2: B1–B5 tệ hơn (rollback); N1–N4 không rollback.
    [InlineData(VerificationState.Assertion, 4, 9, VerificationState.Compile, 0, 0, CheckpointComparer.Worse)]
    [InlineData(VerificationState.Assertion, 4, 9, VerificationState.Assertion, 2, 9, CheckpointComparer.Worse)]
    [InlineData(VerificationState.Assertion, 4, 9, VerificationState.Assertion, 4, 6, CheckpointComparer.Worse)]
    [InlineData(VerificationState.Regression, 5, 9, VerificationState.Assertion, 4, 9, CheckpointComparer.Worse)]
    [InlineData(VerificationState.Runtime, 1, 9, VerificationState.Discovery, 0, 9, CheckpointComparer.Worse)]
    [InlineData(VerificationState.Assertion, 4, 9, VerificationState.Regression, 5, 9, CheckpointComparer.Better)]
    [InlineData(VerificationState.Assertion, 3, 9, VerificationState.Assertion, 4, 9, CheckpointComparer.Better)]
    [InlineData(VerificationState.Assertion, 4, 9, VerificationState.Assertion, 4, 9, CheckpointComparer.Equal)]
    public void CheckpointComparisonFollowsStateThenPassedThenAssertions(
        VerificationState bestState, int bestPassed, int bestAssertions, VerificationState newState, int newPassed, int newAssertions, string expected)
    {
        Assert.Equal(expected, CheckpointComparer.Compare(V(newState, passed: newPassed, assertions: newAssertions), V(bestState, passed: bestPassed, assertions: bestAssertions)));
    }

    [Fact]
    public void Section15_3RegressionThenRollbackThenValid()
    {
        var policy = new AdaptiveRepairPolicy(Version(0, "c0", V(VerificationState.Assertion, "ASSERTION | AssertionError | t", passed: 4, failing: "t")));
        var (_, d1) = Round(policy, 1, C("r1"), V(VerificationState.Regression, "REGRESSION | existing tests fail | InvoiceTest", passed: 5));
        Assert.Equal((true, 1), (d1.Improved, d1.LevelAfter));
        var (_, d2) = Round(policy, 2, C("r2"), V(VerificationState.Assertion, "ASSERTION | AssertionError | t", passed: 4, failing: "t"));
        Assert.Equal((true, 2, 1), (d2.Rollback, d2.LevelAfter, d2.CheckpointRound));
        var request3 = policy.NextRequest(3);
        Assert.Equal((2, VerificationState.Regression, 1), (request3.Level, request3.State, request3.BaseRound));
        Assert.Empty(request3.RewriteMethods);
    }

    [Fact]
    public void EarlyStopAfterPRoundsWithoutImprovement()
    {
        var policy = new AdaptiveRepairPolicy(Version(0, "c0", V(VerificationState.Compile, "COMPILE | a | -")), earlyStopP: 2);
        Round(policy, 1, C("r1"), V(VerificationState.Compile, "COMPILE | b | -"));
        Assert.True(policy.CanContinue(1, 5));
        Round(policy, 2, C("r2"), V(VerificationState.Compile, "COMPILE | c | -"));
        Assert.False(policy.CanContinue(2, 5));
        Assert.Equal("stagnation", policy.StopReason(2, 5));
        Assert.Equal("budget", new FixedRepairPolicy(Version(0, "c0", V(VerificationState.Compile))).StopReason(5, 5));
    }

    [Fact]
    public void AttemptsCarryFirstErrorAndChangedLines()
    {
        var policy = new AdaptiveRepairPolicy(Version(0, "class T {\n  long total = service.getTotal(1L);\n}", V(VerificationState.Compile, "COMPILE | cannot find symbol | method")));
        Round(policy, 1, C("class T {\n  long total = service.calcTotal(1L);\n}"), V(VerificationState.Compile, "COMPILE | cannot find symbol | method"));
        var attempt = Assert.Single(policy.Attempts);
        Assert.Equal("first error of COMPILE | cannot find symbol | method", attempt.FirstError);
        Assert.Equal("-  long total = service.getTotal(1L);\n+  long total = service.calcTotal(1L);", attempt.Change);
    }

    [Fact]
    public void LineDiffLimitsToFifteenLines()
    {
        var before = string.Join('\n', Enumerable.Range(1, 20).Select(i => $"a{i}"));
        var after = string.Join('\n', Enumerable.Range(1, 20).Select(i => $"b{i}"));
        var diff = LineDiff.Changes(before, after)!.Split('\n');
        Assert.Equal(16, diff.Length);
        Assert.Equal("… (25 more changed lines)", diff[^1]);
        Assert.Null(LineDiff.Changes("x\ny\n", "x\ny"));
        Assert.Equal("+z", LineDiff.Changes("x\ny", "x\nz\ny"));
    }

    [Fact]
    public void SiblingModuleClassDirectoriesResolveAgainstSnapshot()
    {
        var snapshot = Path.Combine(Path.GetTempPath(), "ws", "snapshot");
        var jar = Path.Combine(Path.GetTempPath(), "m2", "junit-4.13.2.jar");
        Assert.Equal(
            [Path.Combine(snapshot, "shop-api", "target", "classes"), jar],
            RepairRunner.ResolveClasspath(snapshot, ["shop-api/target/classes", jar]));
    }
}
