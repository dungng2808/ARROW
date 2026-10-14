using System.Net;
using System.Text;
using System.Text.Json;
using Arrow.DataAccess.Files;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.Block2;
using Arrow.Service.External;

namespace Arrow.Service.Tests;

/// <summary>Khối 2 (spec 02 Mục 8): dựng prompt 3 quy trình và prompt sửa, tách mã, khóa cache, phát lại, lớp gọi model.</summary>
public class Block2Tests
{
    private static string FrameworkRoot
    {
        get
        {
            for (var dir = new DirectoryInfo(AppContext.BaseDirectory); dir is not null; dir = dir.Parent)
            {
                if (File.Exists(Path.Combine(dir.FullName, "ArrowFramework.sln")))
                {
                    return dir.FullName;
                }
            }

            throw new InvalidOperationException("Không tìm thấy ArrowFramework.sln");
        }
    }

    private static readonly FilePromptRepository Prompts = new(new FrameworkPaths(FrameworkRoot));

    /// <summary>M của CUT minh họa (spec 01 Mục 10).</summary>
    public static readonly RepositoryMetadata M = new()
    {
        TaskId = "555001_x",
        BuildTool = new BuildToolInfo { Kind = "maven", Version = "3.9.6", BuildRoot = ".", ReactorModule = "shop-core" },
        Java = new JavaInfo { Major = 8, JdkMajor = 8 },
        TestFramework = new TestFrameworkInfo { Name = "junit4", Version = "4.13.2", Mocking = ["org.mockito:mockito-core:3.12.4"] },
        Dependencies = new DependencyInfo { Test = [new DependencyRef { Group = "junit", Artifact = "junit", Version = "4.13.2" }] },
        ProjectStructure = new ProjectStructureInfo { MultiModule = true, Modules = ["shop-api", "shop-core"], TargetModule = "shop-core", ModuleRoot = "shop-core" },
        Paths = new PathsInfo
        {
            SourceRoot = "shop-core/src/main/java",
            TestRoot = "shop-core/src/test/java",
            CutPath = "shop-core/src/main/java/com/acme/shop/OrderService.java",
            GeneratedTestPath = "shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java",
            GeneratedTestFqn = "com.acme.shop.OrderService_ArrowTest",
        },
        Cut = new CutInfo { Fqn = "com.acme.shop.OrderService", SimpleName = "OrderService", Package = "com.acme.shop" },
    };

    private static readonly CutSource Cut = new("package com.acme.shop;\npublic class OrderService { public long totalPrice(long id) { return 0; } }\n");

    private static BuiltPrompt Generation(string strategy)
    {
        var (system, user) = PromptFiles.ForStrategy(strategy);
        return PromptBuilder.Generation(strategy, Prompts.Load(system), Prompts.Load(user), M, Cut);
    }

    // ── dựng prompt ────────────────────────────────────────────────────

    [Fact]
    public void GenerationPromptsDifferOnlyInTechniqueAndRepositoryConfiguration()
    {
        var zero = Generation("zero_shot");
        var aware = Generation("zero_shot_project_aware");
        var few = Generation("few_shot");

        foreach (var prompt in new[] { zero, aware, few })
        {
            Assert.Equal(["system", "user"], prompt.Messages.Select(m => m.Role));
            Assert.Equal(2, prompt.Templates.Count);
            Assert.DoesNotContain("{{", prompt.Messages[1].Content);
            Assert.Contains("- Class name: OrderService_ArrowTest", prompt.Messages[1].Content);
            Assert.Contains("- Package: com.acme.shop", prompt.Messages[1].Content);
            Assert.Contains("public long totalPrice(long id)", prompt.Messages[1].Content);
        }

        // Chỉ zero_shot_project_aware có khối M, và khối M nằm đúng giữa phần tên class và mã CUT.
        Assert.DoesNotContain("Repository configuration", zero.Messages[1].Content);
        Assert.DoesNotContain("Repository configuration", few.Messages[1].Content);
        Assert.Contains("Repository configuration (compatibility constraints):\n- Build tool: Maven 3.9.6 (module shop-core, multi-module project, build root .)", aware.Messages[1].Content);
        Assert.Contains("- Test framework: JUnit 4 (4.13.2); mocking: Mockito 3.12.4", aware.Messages[1].Content);
        Assert.Equal(zero.Messages[1].Content, few.Messages[1].Content);
        Assert.Equal("generation.user.project_aware", aware.Templates[1].Id);
        Assert.NotEqual(zero.Messages[0].Content, few.Messages[0].Content);
    }

    private static RepairRequest Request(int level, IReadOnlyList<Attempt>? attempts = null, IReadOnlyList<string>? rewrite = null) => new()
    {
        TaskId = "555001_x", Model = "gpt-6-luna", Strategy = "zero_shot", Mechanism = "adaptive", Round = 3,
        RequestedLevel = level, Level = level, State = VerificationState.Compile,
        BaseCode = "package com.acme.shop;\npublic class OrderService_ArrowTest { }\n", BaseRound = 1,
        ErrorBlock = "OrderService_ArrowTest.java:[14,30] cannot find symbol\n  symbol:   method getTotal(long)",
        ExtraContext = level >= 2 ? "Constructors, methods and fields of com.acme.shop.OrderService:\n  public long totalPrice(long orderId)" : "",
        FailedAttempts = [.. attempts ?? []],
        RewriteMethods = [.. rewrite ?? []],
    };

    private static BuiltPrompt Repair(RepairRequest request) => PromptBuilder.Repair(
        request,
        Prompts.Load(PromptFiles.RepairSystem),
        Prompts.Load(PromptBuilder.RepairUserTemplate(request.Level)),
        Prompts.Load(PromptFiles.FailureSummaries),
        M,
        Cut);

    [Fact]
    public void RepairPromptsByLevelAlwaysCarryFullMetadata()
    {
        var attempts = new List<Attempt> { new() { Round = 1, Signature = "COMPILE | cannot find symbol | method", FirstError = "cannot find symbol: method getTotal(long)", Change = "-  a\n+  b" } };
        var level1 = Repair(Request(1)).Messages[1].Content;
        var level2 = Repair(Request(2, attempts)).Messages[1].Content;
        var level3 = Repair(Request(3, attempts, ["totalPriceAddsTax", "cancelsTwice"])).Messages[1].Content;

        foreach (var text in new[] { level1, level2, level3 })
        {
            Assert.StartsWith("The test class below failed ARROW's verification. It does not compile.", text);
            Assert.Contains("Repository configuration (compatibility constraints):\n- Build tool: Maven 3.9.6", text);
            Assert.Contains("Failure output:\nOrderService_ArrowTest.java:[14,30] cannot find symbol", text);
            Assert.Contains("Current test class:\n```java\npackage com.acme.shop;\npublic class OrderService_ArrowTest { }\n```", text);
            Assert.Contains("Target CUT com.acme.shop.OrderService (shop-core/src/main/java/com/acme/shop/OrderService.java):", text);
            Assert.DoesNotContain("{{", text);
        }

        Assert.DoesNotContain("Approaches that already failed", level1);
        Assert.DoesNotContain("Additional information", level1);
        Assert.Contains("Additional information for this failure:\nConstructors, methods and fields of com.acme.shop.OrderService:", level2);
        Assert.Contains("- Round 1 · COMPILE | cannot find symbol | method\n  First error: cannot find symbol: method getTotal(long)\n  Changed code:\n    -  a\n    +  b", level2);
        Assert.Contains("The failure is located in these test methods: `totalPriceAddsTax`, `cancelsTwice`.", level3);
        Assert.Contains("Additional information for this failure:", level3);
        Assert.Equal("repair/user/level3.md", Repair(Request(3, attempts, ["x"])).UserTemplate);
    }

    [Fact]
    public void FailedAttemptsShowDetailsOnlyForLastThree()
    {
        var change = "-        long total = service.getTotal(1L);\n+        long total = service.calcTotal(1L);";
        var attempts = new List<Attempt>
        {
            new() { Round = 1, Signature = "COMPILE | cannot find symbol | method", FirstError = "cannot find symbol: method getTotal(long, boolean)", Change = "-x\n+y" },
            new() { Round = 2, Signature = "COMPILE | cannot find symbol | method", FirstError = "cannot find symbol: method calcTotal(long) in class com.acme.shop.OrderService", Change = change },
            new() { Round = 3, Signature = "duplicate code (same as round 2)", Change = change },
            new() { Round = 4, Signature = "invalid output: no Java class found" },
        };

        // Đúng ví dụ của prompts/README.md Mục 3.
        Assert.Equal("""
            - Round 1 · COMPILE | cannot find symbol | method
            - Round 2 · COMPILE | cannot find symbol | method
              First error: cannot find symbol: method calcTotal(long) in class com.acme.shop.OrderService
              Changed code:
                -        long total = service.getTotal(1L);
                +        long total = service.calcTotal(1L);
            - Round 3 · duplicate code (same as round 2)
              Changed code:
                -        long total = service.getTotal(1L);
                +        long total = service.calcTotal(1L);
            - Round 4 · invalid output: no Java class found
            """.ReplaceLineEndings("\n"), PromptBuilder.FormatAttempts(attempts));
        Assert.Equal("- (none)", PromptBuilder.FormatAttempts([]));
    }

    [Fact]
    public void FailureSummaryComesFromDataFile()
    {
        var summaries = Prompts.Load(PromptFiles.FailureSummaries);
        Assert.Equal("Its test methods run, but some assertions fail.", PromptBuilder.FailureSummary(summaries, VerificationState.Assertion));
        Assert.StartsWith("Its test methods pass when run alone", PromptBuilder.FailureSummary(summaries, VerificationState.Regression));
        Assert.Throws<ArrowException>(() => PromptBuilder.FailureSummary(summaries, VerificationState.Valid));
    }

    // ── tách mã ────────────────────────────────────────────────────────

    [Fact]
    public void ExtractsPlainCodeWithoutChanges()
    {
        const string code = "package a;\n\nimport org.junit.Test;\n\npublic class X_ArrowTest {\n    @Test public void t() {}\n}\n";
        var extracted = CodeExtractor.Extract(code);
        Assert.Equal(code, extracted.Code);
        Assert.Empty(extracted.Normalization);
    }

    [Fact]
    public void RemovesFenceAndProse()
    {
        var response = "Here is the test class you asked for:\n\n```java\nimport org.junit.Test;\n\npublic class X_ArrowTest {\n    @Test public void t() {}\n}\n```\n\nThis covers the main cases.";
        var extracted = CodeExtractor.Extract(response);
        Assert.Equal("import org.junit.Test;\n\npublic class X_ArrowTest {\n    @Test public void t() {}\n}\n", extracted.Code);
        Assert.Equal([CodeExtractor.FenceRemoved], extracted.Normalization);

        var noFence = CodeExtractor.Extract("Sure!\n@RunWith(JUnit4.class)\npublic class X { }\nHope it helps.");
        Assert.Equal("@RunWith(JUnit4.class)\npublic class X { }\n", noFence.Code);
        Assert.Equal([CodeExtractor.ProseTrimmed], noFence.Normalization);
    }

    [Fact]
    public void PicksFirstFenceWithClassAndHandlesMissingCode()
    {
        var response = "```xml\n<dependency/>\n```\n```java\nclass A {}\n```";
        Assert.Equal("class A {}\n", CodeExtractor.Extract(response).Code);
        Assert.Null(CodeExtractor.Extract("I cannot write a test for this class.").Code);
        Assert.Null(CodeExtractor.Extract("").Code);

        // Mã bị cắt giữa chừng (fence không đóng): vẫn lấy phần mã để bước parse báo parse_error.
        var truncated = CodeExtractor.Extract("```java\npublic class A {\n  @Test public void t() {\n    int x =");
        Assert.NotNull(truncated.Code);
        Assert.Contains("int x =", truncated.Code);
    }

    [Fact]
    public void RemovesDuplicateImports()
    {
        var (code, actions) = CodeExtractor.RemoveDuplicateImports("import a.B;\nimport a.C;\nimport a.B;\nclass X {}");
        Assert.Equal("import a.B;\nimport a.C;\nclass X {}", code);
        Assert.Equal(["import: removed duplicate a.B"], actions);
    }

    // ── cache và phát lại ──────────────────────────────────────────────

    private sealed class MemoryCache : ILlmCacheRepository
    {
        public Dictionary<string, LlmCacheEntry> Entries { get; } = [];
        public LlmCacheEntry? TryGet(string runId, string key) => Entries.GetValueOrDefault(runId + "/" + key);
        public void Put(string runId, LlmCacheEntry entry) => Entries[runId + "/" + entry.Key] = entry;
    }

    private static LlmRequest Req(string callId, Dictionary<string, object?> parameters) => new()
    {
        CallId = callId, Model = "gpt-6-luna", Messages = [new("system", "s"), new("user", "u")], Params = parameters,
    };

    [Fact]
    public void CacheKeyIgnoresParamOrderButSeparatesCallIds()
    {
        var a = Req("repair/t/gpt-6-luna/zero_shot/fixed/round_1", new() { ["temperature"] = 0, ["max_tokens"] = 8192, ["seed"] = 42 });
        var b = Req("repair/t/gpt-6-luna/zero_shot/fixed/round_1", new() { ["seed"] = 42, ["max_tokens"] = 8192, ["temperature"] = 0 });
        var other = Req("repair/t/gpt-6-luna/zero_shot/adaptive/round_1", new() { ["temperature"] = 0, ["max_tokens"] = 8192, ["seed"] = 42 });
        Assert.Equal(ReplayingLlmClient.CacheKey(a), ReplayingLlmClient.CacheKey(b));
        Assert.NotEqual(ReplayingLlmClient.CacheKey(a), ReplayingLlmClient.CacheKey(other));
        Assert.Matches("^[0-9a-f]{64}$", ReplayingLlmClient.CacheKey(a));

        // Tham số đọc từ JSON (JsonElement) cho cùng khóa với giá trị C#.
        var fromJson = JsonSerializer.Deserialize<Dictionary<string, object?>>("{\"max_tokens\": 8192, \"seed\": 42, \"temperature\": 0}");
        Assert.Equal(ReplayingLlmClient.CacheKey(a), ReplayingLlmClient.CacheKey(Req(a.CallId, fromJson!)));
    }

    private sealed class SlowClient : Interfaces.ILlmClient
    {
        public int Calls { get; private set; }

        public Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
        {
            Calls++;
            return Task.FromResult(new LlmResponse
            {
                Text = "class A {}",
                Usage = new LlmUsage { CallId = request.CallId, ModelId = "gpt-6-luna", PromptTokens = 2410, CompletionTokens = 980, LatencySeconds = 8.7, CostUsd = 0.00037, Attempts = 2 },
            });
        }
    }

    [Fact]
    public async Task ReplayKeepsOriginalTimeAndCost()
    {
        var cache = new MemoryCache();
        var inner = new SlowClient();
        var request = Req("gen/t/gpt-6-luna/zero_shot", new() { ["temperature"] = 0 });

        var first = await new ReplayingLlmClient(inner, cache, "run-1").CompleteAsync(request, CancellationToken.None);
        var replay = await new ReplayingLlmClient(inner, cache, "run-1").CompleteAsync(request, CancellationToken.None);
        Assert.Equal(1, inner.Calls);
        Assert.False(first.Usage.Replayed);
        Assert.True(replay.Usage.Replayed);
        Assert.Equal((8.7, 0.00037, 2410L, 980L, 2), (replay.Usage.LatencySeconds, replay.Usage.CostUsd, replay.Usage.PromptTokens, replay.Usage.CompletionTokens, replay.Usage.Attempts));

        // Cùng prompt nhưng lượt gọi khác (vòng khác) → gọi thật.
        await new ReplayingLlmClient(inner, cache, "run-1").CompleteAsync(request with { CallId = "gen/t/gpt-6-luna/few_shot" }, CancellationToken.None);
        Assert.Equal(2, inner.Calls);
    }

    // ── gateway ────────────────────────────────────────────────────────

    private static readonly ModelConfig Luna = new()
    {
        Name = "gpt-6-luna", ExpectedModelIdPrefix = "gpt-6-luna",
        Params = new() { ["temperature"] = 0 }, PriceUsdPerMillion = new PriceConfig { Input = 0.05, Output = 0.25 },
    };

    [Fact]
    public async Task GatewayComputesCostChecksModelAndLimitsCalls()
    {
        var fake = new FakeLlmClient { ModelId = _ => "openai/gpt-6-luna-2026-09-01" };
        fake.DefaultReply = _ => new string('x', 3920);
        using var gateway = new LlmGateway(fake, [Luna], maxConcurrent: 2, rpmLimit: 60, maxCalls: 2);
        var request = Req("gen/t/gpt-6-luna/zero_shot", new()) with { Messages = [new("user", new string('y', 9640))] };
        var response = await gateway.CompleteAsync(request, CancellationToken.None);
        // 2410 token vào × 0,05 + 980 token ra × 0,25 (spec 02 Mục 9.3).
        Assert.Equal(0.0003655, response.Usage.CostUsd, 7);

        fake.ModelId = _ => "gpt-4o";
        Assert.Equal(ErrorCodes.B2WrongModel, (await Assert.ThrowsAsync<ArrowException>(() => gateway.CompleteAsync(request, CancellationToken.None))).Code);
        Assert.Equal(ErrorCodes.B2CallLimit, (await Assert.ThrowsAsync<ArrowException>(() => gateway.CompleteAsync(request, CancellationToken.None))).Code);
        Assert.Equal(ErrorCodes.B2LlmConfig, (await Assert.ThrowsAsync<ArrowException>(() => gateway.CompleteAsync(request with { Model = "nope" }, CancellationToken.None))).Code);
    }

    [Fact]
    public void ModelPrefixMatchesProviderPaths()
    {
        Assert.True(LlmGateway.ModelMatches("gpt-6-luna", "gpt-6-luna"));
        Assert.True(LlmGateway.ModelMatches("gpt-6-luna", "openai/gpt-6-luna"));
        Assert.True(LlmGateway.ModelMatches("qwen3-coder", "ollama_chat/qwen3-coder:30b"));
        Assert.False(LlmGateway.ModelMatches("gpt-6-luna", "gpt-6"));
        Assert.True(LlmGateway.ModelMatches("", "anything"));
    }

    [Fact]
    public async Task GatewayWaitsWhenRateLimitIsReached()
    {
        var now = DateTimeOffset.Parse("2026-10-14T10:00:00Z");
        var fake = new FakeLlmClient();
        using var gateway = new LlmGateway(fake, [Luna with { ExpectedModelIdPrefix = "" }], maxConcurrent: 4, rpmLimit: 2, clock: () => now);
        var request = Req("gen/t/gpt-6-luna/zero_shot", new());
        await gateway.CompleteAsync(request, CancellationToken.None);
        await gateway.CompleteAsync(request, CancellationToken.None);
        var third = gateway.CompleteAsync(request, CancellationToken.None);
        await Task.Delay(200);
        Assert.False(third.IsCompleted);
        now = now.AddSeconds(61);
        await third.WaitAsync(TimeSpan.FromSeconds(5));
        Assert.Equal(3, fake.Calls.Count);
    }

    // ── LiteLLM qua HTTP giả ───────────────────────────────────────────

    private sealed class ScriptedHandler(params (HttpStatusCode Status, string Body)[] replies) : HttpMessageHandler
    {
        private int _index;
        public List<(string Body, string? Auth)> Requests { get; } = [];

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            Requests.Add((await request.Content!.ReadAsStringAsync(cancellationToken), request.Headers.Authorization?.ToString()));
            var (status, body) = replies[Math.Min(_index++, replies.Length - 1)];
            return new HttpResponseMessage(status) { Content = new StringContent(body, Encoding.UTF8, "application/json") };
        }
    }

    private sealed class NullLog : ILogRepository
    {
        public void Write(LogLevel level, string message, Exception? exception = null)
        {
        }
    }

    private const string Ok = """
        {"id":"x","model":"gpt-6-luna","choices":[{"index":0,"message":{"role":"assistant","content":"public class A {}"},"finish_reason":"stop"}],
         "usage":{"prompt_tokens":2410,"completion_tokens":980,"total_tokens":3390}}
        """;

    private static LiteLlmClient Client(ScriptedHandler handler) =>
        new(new HttpClient(handler), "http://127.0.0.1:4000/", "sk-master", new NullLog(), new LlmRetryOptions { Delays = [TimeSpan.Zero, TimeSpan.Zero, TimeSpan.Zero] });

    [Fact]
    public async Task LiteLlmSendsParamsVerbatimAndParsesUsage()
    {
        var handler = new ScriptedHandler((HttpStatusCode.OK, Ok));
        var request = Req("gen/t/gpt-6-luna/zero_shot", new() { ["reasoning_effort"] = "none", ["temperature"] = 0, ["max_tokens"] = 8192, ["seed"] = 42 });
        var response = await Client(handler).CompleteAsync(request, CancellationToken.None);
        Assert.Equal("public class A {}", response.Text);
        Assert.Equal(("gpt-6-luna", 2410L, 980L, "stop", 1), (response.Usage.ModelId, response.Usage.PromptTokens, response.Usage.CompletionTokens, response.Usage.FinishReason, response.Usage.Attempts));
        Assert.Contains("\"reasoning_effort\":\"none\"", handler.Requests[0].Body);
        Assert.Contains("\"max_tokens\":8192", handler.Requests[0].Body);
        Assert.Contains("\"messages\":[{\"role\":\"system\",\"content\":\"s\"},{\"role\":\"user\",\"content\":\"u\"}]", handler.Requests[0].Body);
        Assert.Equal("Bearer sk-master", handler.Requests[0].Auth);
    }

    [Fact]
    public async Task LiteLlmRetriesTransientErrors()
    {
        var handler = new ScriptedHandler((HttpStatusCode.TooManyRequests, "{\"error\":{\"message\":\"rate limited\"}}"), (HttpStatusCode.OK, Ok));
        var response = await Client(handler).CompleteAsync(Req("c", new()), CancellationToken.None);
        Assert.Equal(2, response.Usage.Attempts);

        var down = new ScriptedHandler((HttpStatusCode.BadGateway, "bad gateway"));
        var error = await Assert.ThrowsAsync<ArrowException>(() => Client(down).CompleteAsync(Req("c", new()), CancellationToken.None));
        Assert.Equal(ErrorCodes.B2LlmUnavailable, error.Code);
        Assert.Equal(4, down.Requests.Count);
    }

    [Fact]
    public async Task LiteLlmStopsOnConfigurationErrors()
    {
        var handler = new ScriptedHandler((HttpStatusCode.Unauthorized, "{\"error\":{\"message\":\"Invalid API key\"}}"));
        var error = await Assert.ThrowsAsync<ArrowException>(() => Client(handler).CompleteAsync(Req("c", new()), CancellationToken.None));
        Assert.Equal(ErrorCodes.B2LlmConfig, error.Code);
        Assert.Contains("Invalid API key", error.Message);
        Assert.Single(handler.Requests);
    }

    [Fact]
    public async Task WrongModelIdThroughWholeStackPausesRun()
    {
        var handler = new ScriptedHandler((HttpStatusCode.OK, Ok.Replace("\"model\":\"gpt-6-luna\"", "\"model\":\"gpt-4o-mini\"")));
        using var gateway = new LlmGateway(Client(handler), [Luna], 1, 0);
        Assert.Equal(ErrorCodes.B2WrongModel, (await Assert.ThrowsAsync<ArrowException>(() => gateway.CompleteAsync(Req("c", new()), CancellationToken.None))).Code);
    }
}
