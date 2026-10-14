using Arrow.DataAccess;
using Arrow.DataAccess.Files;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.Block2;
using Arrow.Service.Block3;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.IntegrationTests;

/// <summary>Bước C6: một chain Fixed và một chain Adaptive với model giả trên fixture Maven thật (spec 07 Mục 11).</summary>
public class RepairChainIntegrationTests(VerifyFixture fixture) : IClassFixture<VerifyFixture>
{
    private const string TestPath = "src/test/java/com/acme/billing/PriceCalculator_ArrowTest.java";

    private const string Broken = """
        package com.acme.billing;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class PriceCalculator_ArrowTest {
            @Test
            public void multiplies() {
                assertEquals(6, new PriceCalculator().multiply(2, 3));
            }
        }
        """;

    private const string Fixed = """
        package com.acme.billing;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class PriceCalculator_ArrowTest {
            @Test
            public void adds() {
                assertEquals(5, new PriceCalculator().add(2, 3));
            }
        }
        """;

    private async Task<(IRepairRunner Runner, ChainRequest Request, FakeLlmClient Llm)> SetUpAsync(string mechanism, FakeLlmClient llm)
    {
        var log = new FileLogRepository(fixture.Repo.ResultPaths);
        var generator = new TestGenerator(new FilePromptRepository(FixtureRepo.Framework), llm, fixture.Helper, fixture.Repo.Results);
        var runner = new RepairRunner(generator, fixture.Verifier, new FailureDiagnoser(), new ExtraContextBuilder(fixture.Helper), fixture.Helper, fixture.Workspace, fixture.Repo.Results, log);

        var hash = await fixture.Helper.StripCommentsHashAsync(Broken, CancellationToken.None);
        var c0 = new CandidateTest
        {
            TaskId = "800_verify", Model = "fake-model", Strategy = "zero_shot", ValidOutput = true,
            TestFqn = "com.acme.billing.PriceCalculator_ArrowTest", TestPath = TestPath, Code = Broken, CodeHash = hash.CodeHash,
        };
        var options = new VerifyOptions { RetryDelay = TimeSpan.Zero };
        var c0Verify = await fixture.Verifier.VerifyAsync(RepairRunner.Suite(c0, 0), fixture.Task, options with { ResultDir = $"block3/fake-model/zero_shot/c0-{mechanism}" }, CancellationToken.None);
        Assert.Equal(VerificationState.Compile, c0Verify.State);

        var cut = File.ReadAllText(Path.Combine(FixtureRepo.Framework.FrameworkRoot, "tests", "fixtures", "maven-verify-junit4", "src", "main", "java", "com", "acme", "billing", "PriceCalculator.java"));
        var request = new ChainRequest
        {
            Task = fixture.Task,
            Generation = new GenerationContext
            {
                RunId = fixture.Task.RunId, Key = fixture.Task.Key, Metadata = fixture.Task.Metadata, CutSource = cut,
                Model = new ModelConfig { Name = "fake-model", Params = new() { ["temperature"] = 0 } }, Strategy = "zero_shot",
            },
            C0 = c0, C0Verify = c0Verify, Mechanism = mechanism, N = 3, Verify = options, RunOrder = "first",
        };
        return (runner, request, llm);
    }

    private static bool Round(LlmRequest request, int round) => request.CallId.EndsWith($"/round_{round}", StringComparison.Ordinal);

    [Fact]
    public async Task AdaptiveSkipsDuplicateRaisesLevelAndSucceeds()
    {
        // Vòng 1: model trả lại đúng c0 (mã trùng). Vòng 2: chỉ trả bản đúng khi prompt là mức 2 (có thông tin bổ sung).
        var llm = new FakeLlmClient()
            .When(r => Round(r, 1), _ => Broken)
            .When(r => Round(r, 2) && r.Messages[1].Content.Contains("Additional information for this failure", StringComparison.Ordinal), _ => Fixed);
        var (runner, request, _) = await SetUpAsync("adaptive", llm);
        var result = await runner.RunChainAsync(request, CancellationToken.None);

        Assert.Equal(OutcomeKind.Ok, result.Outcome.Outcome);
        var final = result.Final;
        Assert.Equal((true, 2, 2, "valid", VerificationState.Valid, VerificationState.Compile), (final.Success, final.SuccessRound, final.RoundsRun, final.StopReason, final.FinalState, final.C0State));
        Assert.Equal((1, 1), (final.Counters.DuplicateCode, final.Counters.LevelUp));
        Assert.Equal(["multiplies"], final.DroppedMethods);

        var results = fixture.Repo.Results;
        var key = fixture.Task.Key;
        var round1 = results.LoadJson<RoundDecision>("it", key, "block3/fake-model/zero_shot/adaptive/round_1/decision.json")!;
        Assert.Equal((true, 1, 2), (round1.DuplicateCode, round1.Level, round1.LevelAfter));
        Assert.False(results.Exists("it", key, "block3/fake-model/zero_shot/adaptive/round_1/verify.json"));

        var request2 = results.LoadJson<RepairRequest>("it", key, "block3/fake-model/zero_shot/adaptive/round_2/request.json")!;
        Assert.Equal((2, 0, VerificationState.Compile), (request2.Level, request2.BaseRound, request2.State));
        Assert.Contains("Constructors, methods and fields of com.acme.billing.PriceCalculator:", request2.ExtraContext);
        Assert.Contains("public int add(int a, int b)", request2.ExtraContext);
        Assert.Equal("duplicate code (same as round 0)", Assert.Single(request2.FailedAttempts).Signature);
        Assert.Equal(VerificationState.Valid, results.LoadJson<VerifyResult>("it", key, "block3/fake-model/zero_shot/adaptive/round_2/verify.json")!.State);
        Assert.Contains("new PriceCalculator().add(2, 3)", results.LoadText("it", key, ResultPaths.FinalCode("fake-model", "zero_shot", "adaptive")));
        Assert.True(results.LoadJson<FinalTestSuite>("it", key, ResultPaths.Final("fake-model", "zero_shot", "adaptive"))!.Success);
    }

    [Fact]
    public async Task FixedRebuildsDuplicateAndStopsAtBudget()
    {
        // Model luôn trả lại bản lỗi: Fixed build lại mọi vòng, hết N = 3 vòng thì chốt bản mới nhất.
        var llm = new FakeLlmClient { DefaultReply = _ => Broken };
        var (runner, request, fake) = await SetUpAsync("fixed", llm);
        var result = await runner.RunChainAsync(request, CancellationToken.None);

        var final = result.Final;
        Assert.Equal((false, 3, 3, "budget", VerificationState.Compile, 3), (final.Success, final.SuccessRound, final.RoundsRun, final.StopReason, final.FinalState, final.FinalRound));
        Assert.Equal(3, fake.Calls.Count);
        Assert.All(fake.Calls, c => Assert.DoesNotContain("Approaches that already failed", c.Messages[1].Content));
        Assert.True(final.BuildSeconds > 0);
        for (var round = 1; round <= 3; round++)
        {
            Assert.Equal(VerificationState.Compile, fixture.Repo.Results.LoadJson<VerifyResult>("it", fixture.Task.Key, $"block3/fake-model/zero_shot/fixed/round_{round}/verify.json")!.State);
        }
    }
}
