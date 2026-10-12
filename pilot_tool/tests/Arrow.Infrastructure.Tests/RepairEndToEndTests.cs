using Arrow.Core.Repair;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Llm;

namespace Arrow.Infrastructure.Tests;

/// <summary>M6: S2 Fixed và Adaptive qua <c>arrow run</c> với model giả trên fixture (specs/05).</summary>
public class RepairEndToEndTests
{
    private static FakeLlmClient.Rule Rule(string response, params string[] contains) => new() { Contains = [.. contains], Responses = [response] };

    private const string AssertionStillWrong = """
        ```java
        package com.example.calc;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class Calculator_ArrowTest {
            @Test
            public void adds() {
                assertEquals(7, new Calculator("c").add(2, 3));
            }

            @Test
            public void divides() {
                assertEquals(2, new Calculator("c").divide(4, 2));
            }
        }
        ```
        """;

    // Mức 3: method lỗi `adds` được sửa; `divides` trong response sai nhưng không được lấy (giữ bản của checkpoint).
    private const string Level3Response = """
        ```java
        package com.example.calc;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class Calculator_ArrowTest {
            @Test
            public void adds() {
                assertEquals(5, new Calculator("c").add(2, 3));
            }

            @Test
            public void divides() {
                assertEquals(99, new Calculator("c").divide(4, 2));
            }
        }
        ```
        """;

    private const string CartCompile = """
        ```java
        package com.example.shop;

        import org.junit.jupiter.api.Test;

        class Cart_ArrowTest {
            @Test
            void total() {
                int value = new Cart().sum();
            }
        }
        ```
        """;

    [Fact]
    public async Task FixedAndAdaptiveBranches()
    {
        var setup = EndToEnd.Create("e2e-repair",
        [
            // Bước sinh.
            Rule(EndToEnd.CalcAssertion, "Calculator_ArrowTest", "Here are examples"),
            Rule(EndToEnd.CalcValid, "Write a unit test class", "Calculator_ArrowTest", "Repository configuration"),
            Rule(EndToEnd.CalcRuntime, "Write a unit test class", "Calculator_ArrowTest"),
            Rule(EndToEnd.GreeterCompile, "Greeter_ArrowTest"),
            Rule(EndToEnd.CartValid, "Cart_ArrowTest", "Here are examples"),
            Rule(EndToEnd.CartValid, "Write a unit test class", "Cart_ArrowTest", "Repository configuration"),
            Rule(CartCompile, "Write a unit test class", "Cart_ArrowTest"),
            // Sửa: RUNTIME (zero_shot của Calculator).
            Rule(EndToEnd.CalcValid, "Some tests throw an exception", "Additional information:"),
            Rule(EndToEnd.CalcRuntime, "Some tests throw an exception"),
            // Sửa: ASSERTION (few_shot của Calculator).
            Rule(Level3Response, "Some tests fail their assertions", "The failing test methods are"),
            Rule(AssertionStillWrong, "Some tests fail their assertions", "Additional information:"),
            Rule(EndToEnd.Invalid, "Some tests fail their assertions"),
            // Sửa: COMPILE (Gradle).
            Rule(EndToEnd.CartValid, "does not compile", "Cart_ArrowTest"),
        ], nMax: 3);
        var (exit, output) = await EndToEnd.RunAsync(setup, "--stages", "prepare,generate,repair");
        Assert.True(exit == 0, output);

        Assert.Equal("RUNTIME", EndToEnd.Outcome(setup, "generate", "fx-maven-junit4", "zero_shot"));
        Assert.Equal("ASSERTION", EndToEnd.Outcome(setup, "generate", "fx-maven-junit4", "few_shot"));
        Assert.Equal("VALID", EndToEnd.Outcome(setup, "generate", "fx-maven-junit4", "zero_shot_project_aware"));

        var dir = Path.Combine(setup.Store.TaskDir("classes2test", "fx-maven-junit4"), "repair", "gpt-6-luna");
        RepairSummary Summary(string strategy, string mechanism) => Json.Read<RepairSummary>(Path.Combine(dir, strategy, mechanism, "summary.json"));
        RepairDecision Decision(string strategy, string mechanism, int round) => Json.Read<RepairDecision>(Path.Combine(dir, strategy, mechanism, "a1", $"round_{round}", "decision.json"));

        // RUNTIME: Fixed build lại cùng mã 3 lần, không thành công; Adaptive: vòng 1 mã trùng c0 → mức 2 → VALID ở vòng 2.
        var fixedRuntime = Summary("zero_shot", "fixed");
        Assert.False(fixedRuntime.Success);
        Assert.Equal(3, fixedRuntime.RoundsUsed);
        Assert.Equal(VerificationState.Runtime, fixedRuntime.FinalState);
        Assert.Equal(0, fixedRuntime.DuplicateCode);
        Assert.True(Decision("zero_shot", "fixed", 2).RepeatedSignature);
        var adaptiveRuntime = Summary("zero_shot", "adaptive");
        Assert.True(adaptiveRuntime.Success);
        Assert.Equal(2, adaptiveRuntime.SuccessRound);
        Assert.Equal(1, adaptiveRuntime.DuplicateCode);
        Assert.True(Decision("zero_shot", "adaptive", 1).DuplicateCode);
        Assert.Equal("level2.runtime", Decision("zero_shot", "adaptive", 2).PromptKind);
        Assert.Contains("Code of the methods of the class under test", File.ReadAllText(Path.Combine(dir, "zero_shot", "adaptive", "a1", "round_2", "prompt.json")));
        Assert.Equal("DUPLICATE_CODE", EndToEnd.Outcome(setup, "repair", "fx-maven-junit4", "zero_shot", "adaptive", 1));

        // ASSERTION: Fixed gặp output không hợp lệ 3 vòng; Adaptive: không hợp lệ → mức 2 → lặp signature → mức 3 ghép method → VALID.
        var fixedAssertion = Summary("few_shot", "fixed");
        Assert.False(fixedAssertion.Success);
        Assert.Equal(3, fixedAssertion.InvalidOutput);
        var adaptiveAssertion = Summary("few_shot", "adaptive");
        Assert.True(adaptiveAssertion.Success);
        Assert.Equal(3, adaptiveAssertion.SuccessRound);
        Assert.True(Decision("few_shot", "adaptive", 2).RepeatedSignature);
        var level3 = Decision("few_shot", "adaptive", 3);
        Assert.Equal(3, level3.Level);
        Assert.Equal(["adds"], level3.Level3Replaced);
        var spliced = File.ReadAllText(Path.Combine(dir, "few_shot", "adaptive", "a1", "round_3", "cand.java"));
        Assert.Contains("assertEquals(5, new Calculator(\"c\").add(2, 3));", spliced);
        Assert.Contains("assertEquals(2, new Calculator(\"c\").divide(4, 2));", spliced);
        Assert.DoesNotContain("99", spliced);
        Assert.Empty(adaptiveAssertion.DroppedMethods);

        // Thứ tự xen kẽ: ứng viên thứ 2 theo (task_id, model, strategy) chạy Adaptive trước.
        Assert.Equal("first", adaptiveAssertion.RunOrder);
        Assert.Equal("second", fixedAssertion.RunOrder);

        // c0 build lại một lần cho hai cơ chế; GitBug-Java (repair=false) không có job sửa.
        Assert.True(File.Exists(Path.Combine(dir, "zero_shot", "c0", "verify.json")));
        Assert.DoesNotContain(EndToEnd.Latest(setup).Values, e => e.Job!.Stage == "repair" && e.Job.Dataset == "gitbug-java");
        Assert.Equal("VALID", EndToEnd.Outcome(setup, "repair", "fx-gradle-junit5", "zero_shot", "adaptive"));
        Assert.Equal("VALID", EndToEnd.Outcome(setup, "repair", "fx-gradle-junit5", "zero_shot", "fixed"));
    }
}
