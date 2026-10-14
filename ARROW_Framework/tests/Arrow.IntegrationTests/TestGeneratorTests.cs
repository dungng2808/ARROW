using Arrow.DataAccess;
using Arrow.DataAccess.Files;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.Block2;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.IntegrationTests;

/// <summary>Khối 2 với model giả và java-helper thật: tách mã, chuẩn hóa tên, ghép method mức 3, ghi đủ file kết quả (spec 02 Mục 4.5, 5).</summary>
public sealed class TestGeneratorTests(JavaHelperFixture fixture) : IClassFixture<JavaHelperFixture>, IDisposable
{
    private readonly string _root = Directory.CreateTempSubdirectory("arrow-gen-").FullName;

    public void Dispose() => Directory.Delete(_root, recursive: true);

    private static readonly RepositoryMetadata M = new()
    {
        TaskId = "555001_x",
        BuildTool = new BuildToolInfo { Kind = "maven", Version = "3.9.6", BuildRoot = "." },
        Java = new JavaInfo { Major = 8, JdkMajor = 8 },
        TestFramework = new TestFrameworkInfo { Name = "junit4", Version = "4.13.2" },
        Paths = new PathsInfo
        {
            SourceRoot = "src/main/java", TestRoot = "src/test/java",
            CutPath = "src/main/java/com/acme/shop/OrderService.java",
            GeneratedTestPath = "src/test/java/com/acme/shop/OrderService_ArrowTest.java",
            GeneratedTestFqn = "com.acme.shop.OrderService_ArrowTest",
        },
        Cut = new CutInfo { Fqn = "com.acme.shop.OrderService", SimpleName = "OrderService", Package = "com.acme.shop" },
    };

    private const string CutSource = "package com.acme.shop;\n\npublic class OrderService {\n    public long totalPrice(long id) { return id * 2; }\n}\n";

    private (TestGenerator Generator, FakeLlmClient Llm, FileTaskResultRepository Results) Create()
    {
        var llm = new FakeLlmClient();
        var results = new FileTaskResultRepository(new FrameworkPaths(_root));
        return (new TestGenerator(new FilePromptRepository(fixture.Paths), llm, fixture.Client, results), llm, results);
    }

    private static GenerationContext Context(string strategy, string resultDir, string cut = CutSource) => new()
    {
        RunId = "run", Key = new TaskKey("classes2test", "555001_x"), Metadata = M, CutSource = cut,
        Model = new ModelConfig { Name = "gpt-6-luna", Params = new() { ["temperature"] = 0, ["max_tokens"] = 8192 } },
        Strategy = strategy, ResultDir = resultDir,
    };

    [Fact]
    public async Task GenerateNormalizesFencedAnswerAndWritesEveryFile()
    {
        var (generator, llm, results) = Create();
        llm.WhenUserContains("Repository configuration", """
            Here is the test:
            ```java
            import org.junit.Test;
            import static org.junit.Assert.assertEquals;
            import org.junit.Test;

            public class OrderServiceTest {
                @Test
                public void doublesPrice() {
                    OrderServiceTest self = new OrderServiceTest();
                    assertEquals(4L, new OrderService().totalPrice(2L));
                }
            }
            ```
            """);
        var dir = ResultPaths.GenerateDir("gpt-6-luna", "zero_shot_project_aware");
        var result = await generator.GenerateAsync(Context("zero_shot_project_aware", dir), CancellationToken.None);

        var candidate = result.Candidate;
        Assert.True(candidate.ValidOutput, candidate.InvalidReason);
        Assert.Equal(("GENERATE", "zero_shot_project_aware", "com.acme.shop.OrderService_ArrowTest"), (candidate.Mode, candidate.Strategy, candidate.TestFqn));
        Assert.StartsWith("package com.acme.shop;", candidate.Code);
        Assert.Contains("public class OrderService_ArrowTest {", candidate.Code);
        Assert.Contains("OrderService_ArrowTest self = new OrderService_ArrowTest();", candidate.Code);
        Assert.Equal(1, candidate.Code!.Split("import org.junit.Test;").Length - 1);
        Assert.Equal(
            ["fence removed", "import: removed duplicate org.junit.Test", "package: added com.acme.shop", "class: OrderServiceTest → OrderService_ArrowTest"],
            candidate.Normalization);
        Assert.Equal((1, 1), (candidate.TestMethodCount, candidate.AssertionCount));
        Assert.StartsWith("sha256:", candidate.CodeHash);

        var key = new TaskKey("classes2test", "555001_x");
        var prompt = results.LoadJson<PromptRecord>("run", key, $"{dir}/prompt.json")!;
        Assert.Equal("gen/555001_x/gpt-6-luna/zero_shot_project_aware", prompt.CallId);
        Assert.Equal("none", prompt.SourceTruncation);
        Assert.Contains("- Java version: 8", prompt.Messages[1].Content);
        Assert.Equal(candidate.Code, results.LoadText("run", key, $"{dir}/candidate.java"));
        Assert.NotNull(results.LoadJson<LlmUsage>("run", key, $"{dir}/usage.json"));
        Assert.Contains("\"fake\":true", results.LoadText("run", key, $"{dir}/response.json"));
        Assert.True(results.LoadJson<CandidateTest>("run", key, $"{dir}/candidate.json")!.ValidOutput);
    }

    [Theory]
    [InlineData("I am unable to generate tests for this class.", "no_code")]
    [InlineData("public class A { @Test public void t() {} }\nclass B {}", "multiple_classes")]
    [InlineData("```java\npublic class OrderServiceTest {\n  @Test public void t() {\n    int x =", "parse_error")]
    public async Task UnusableAnswersAreInvalidOutput(string answer, string reason)
    {
        var (generator, llm, results) = Create();
        llm.DefaultReply = _ => answer;
        var dir = ResultPaths.GenerateDir("gpt-6-luna", "zero_shot");
        var candidate = (await generator.GenerateAsync(Context("zero_shot", dir), CancellationToken.None)).Candidate;
        Assert.False(candidate.ValidOutput);
        Assert.Equal(reason, candidate.InvalidReason);
        Assert.Null(candidate.Code);
        Assert.Null(results.LoadText("run", new TaskKey("classes2test", "555001_x"), $"{dir}/candidate.java"));
    }

    [Fact]
    public async Task Level3SplicesOnlyFailingMethodsIntoCheckpoint()
    {
        var (generator, llm, _) = Create();
        const string checkpoint = """
            package com.acme.shop;

            import static org.junit.Assert.assertEquals;
            import org.junit.Test;

            public class OrderService_ArrowTest {
                @Test
                public void keepsThis() {  assertEquals(2L, new OrderService().totalPrice(1L)); }

                @Test
                public void broken() {
                    assertEquals(5L, new OrderService().totalPrice(2L));
                }

                @Test
                public void alsoBroken() {
                    assertEquals(7L, new OrderService().totalPrice(3L));
                }
            }
            """;
        llm.WhenUserContains("The failure is located in these test methods: `broken`, `alsoBroken`.", """
            package com.acme.shop;

            import static org.junit.Assert.assertEquals;
            import org.junit.Test;

            public class OrderService_ArrowTest {
                private static final long TWO = 2L;

                @Test
                public void keepsThis() { throw new RuntimeException("must be ignored"); }

                @Test
                public void broken() {
                    assertEquals(4L, new OrderService().totalPrice(TWO));
                }
            }
            """);
        var request = new RepairRequest
        {
            TaskId = "555001_x", Model = "gpt-6-luna", Strategy = "zero_shot", Mechanism = "adaptive", Round = 3, RequestedLevel = 3, Level = 3,
            State = VerificationState.Assertion, BaseCode = checkpoint, BaseRound = 1, ErrorBlock = "broken: expected:<5> but was:<4>",
            ExtraContext = "public long totalPrice(long id) { return id * 2; }",
            FailedAttempts = [new Attempt { Round = 2, Signature = "ASSERTION | AssertionError | broken" }],
            RewriteMethods = ["broken", "alsoBroken"],
        };
        var dir = ResultPaths.RoundDir("gpt-6-luna", "zero_shot", "adaptive", 3);
        var result = await generator.RepairAsync(Context("zero_shot", dir), request, CancellationToken.None);

        var candidate = result.Candidate;
        Assert.True(candidate.ValidOutput, candidate.InvalidReason);
        Assert.Equal("REPAIR", candidate.Mode);
        Assert.Equal("repair/555001_x/gpt-6-luna/zero_shot/adaptive/round_3", result.Prompt.CallId);
        Assert.Contains("public void keepsThis() {  assertEquals(2L, new OrderService().totalPrice(1L)); }", candidate.Code);
        Assert.Contains("totalPrice(TWO)", candidate.Code);
        Assert.Contains("private static final long TWO = 2L;", candidate.Code);
        Assert.DoesNotContain("must be ignored", candidate.Code);
        Assert.DoesNotContain("alsoBroken", candidate.Code);
        Assert.Equal(["alsoBroken"], candidate.DroppedMethods);
        Assert.Equal(2, candidate.TestMethodCount);
        Assert.Equal("repair.user.level3", result.Prompt.Templates[1].Id);
    }

    [Fact]
    public async Task LongCutIsShrunkBeforePrompting()
    {
        var (generator, llm, _) = Create();
        llm.DefaultReply = _ => "public class OrderServiceTest { @org.junit.Test public void t() {} }";
        var comment = "// " + new string('x', 200) + "\n";
        var longCut = "package com.acme.shop;\n\npublic class OrderService {\n" + string.Concat(Enumerable.Repeat(comment, 300)) + "    public long totalPrice(long id) { return id * 2; }\n}\n";
        Assert.True(longCut.Length > TestGenerator.MaxCutChars);

        var result = await generator.GenerateAsync(Context("zero_shot", ResultPaths.GenerateDir("gpt-6-luna", "zero_shot"), longCut), CancellationToken.None);
        Assert.Equal("comments_removed", result.Prompt.SourceTruncation);
        Assert.DoesNotContain("xxxxxxxxxx", result.Prompt.Messages[1].Content);
        Assert.Contains("public long totalPrice(long id)", result.Prompt.Messages[1].Content);
        Assert.Single(llm.Calls);
    }
}
