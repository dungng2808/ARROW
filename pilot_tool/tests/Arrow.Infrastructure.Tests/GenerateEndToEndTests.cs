using Arrow.Core.Generation;
using Arrow.Core.Llm;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Llm;

namespace Arrow.Infrastructure.Tests;

/// <summary>M5: S0 + S1 qua lệnh <c>arrow run</c> với model giả; chạy lại không gọi model lần hai (cache, ledger).</summary>
public class GenerateEndToEndTests
{
    private static FakeLlmClient.Rule Rule(string response, params string[] contains) => new() { Contains = [.. contains], Responses = [response] };

    [Fact]
    public async Task PrepareAndGenerateWithFakeModel()
    {
        var setup = EndToEnd.Create("e2e-gen",
        [
            Rule(EndToEnd.CalcAssertion, "Calculator_ArrowTest", "Here are examples"),
            Rule(EndToEnd.Invalid, "Calculator_ArrowTest", "Repository configuration"),
            Rule(EndToEnd.CalcValid, "Calculator_ArrowTest"),
            Rule(EndToEnd.GreeterCompile, "Greeter_ArrowTest"),
            Rule(EndToEnd.CartValid, "Cart_ArrowTest"),
        ]);
        var (exit, output) = await EndToEnd.RunAsync(setup, "--stages", "prepare,generate");
        Assert.True(exit == 0, output);

        Assert.Equal("READY", EndToEnd.Outcome(setup, "prepare", "fx-maven-junit4"));
        Assert.Equal("VALID", EndToEnd.Outcome(setup, "generate", "fx-maven-junit4", "zero_shot"));
        Assert.Equal("ASSERTION", EndToEnd.Outcome(setup, "generate", "fx-maven-junit4", "few_shot"));
        Assert.Equal("COMPILE", EndToEnd.Outcome(setup, "generate", "fx-maven-junit4", "zero_shot_project_aware"));
        Assert.Equal("COMPILE", EndToEnd.Outcome(setup, "generate", "fx-maven-multi-junit5", "zero_shot"));
        Assert.Equal("VALID", EndToEnd.Outcome(setup, "generate", "fx-gradle-junit5", "few_shot"));

        var store = setup.Store;
        var gen = Path.Combine(store.TaskDir("classes2test", "fx-maven-junit4"), "gen", "gpt-6-luna");
        // zero_shot: tên class CalculatorTest được đổi thành Calculator_ArrowTest; few_shot: import JUnit 5 đổi sang JUnit 4.
        var normalization = Json.Read<NormalizationResult>(Path.Combine(gen, "zero_shot", "normalization.json"));
        Assert.Equal("CalculatorTest", normalization.ClassBefore);
        Assert.Contains("@Test", File.ReadAllText(Path.Combine(gen, "few_shot", "cand_0.java")));
        Assert.DoesNotContain("jupiter", File.ReadAllText(Path.Combine(gen, "few_shot", "cand_0.java")));
        var invalid = Json.Read<VerifyResult>(Path.Combine(gen, "zero_shot_project_aware", "verify_0.json"));
        Assert.True(invalid.InvalidOutput);
        Assert.False(File.Exists(Path.Combine(gen, "zero_shot_project_aware", "cand_0.java")));
        var usage = Json.Read<LlmUsage>(Path.Combine(gen, "zero_shot", "usage.json"));
        Assert.Equal("gpt-6-luna-fake", usage.ModelId);
        Assert.False(usage.CacheHit);
        Assert.Contains("generation/zero_shot.md", Json.Read<RunConfigLock>(store.RunConfigLockPath).PromptHashes.Keys);

        // Chạy lại: mọi job đã có trong ledger, không chạy lại, không gọi model.
        var ledgerLines = File.ReadAllLines(store.LedgerPath).Length;
        var (exit2, output2) = await EndToEnd.RunAsync(setup, "--stages", "prepare,generate");
        Assert.True(exit2 == 0, output2);
        Assert.Equal(ledgerLines, File.ReadAllLines(store.LedgerPath).Length);
    }
}
