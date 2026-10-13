using System.Net;
using System.Text;
using Arrow.Core.Llm;
using Arrow.Infrastructure.Llm;

namespace Arrow.Infrastructure.Tests;

/// <summary>HTTP giả cho LiteLlmClient (specs/04 Mục 7): trả lần lượt các response đã soạn.</summary>
public sealed class FakeLlmHandler(params (HttpStatusCode Status, string Body)[] responses) : HttpMessageHandler
{
    private int _index;

    public int Calls => _index;
    public List<string> Bodies { get; } = [];

    public static string Ok(string content, string model = "gpt-6-luna-2026-05-01", string finish = "stop") =>
        System.Text.Json.JsonSerializer.Serialize(new
        {
            id = "x",
            model,
            choices = new[] { new { index = 0, finish_reason = finish, message = new { role = "assistant", content } } },
            usage = new { prompt_tokens = 120, completion_tokens = 30 },
        });

    protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        Bodies.Add(await request.Content!.ReadAsStringAsync(cancellationToken));
        var (status, body) = responses[Math.Min(_index, responses.Length - 1)];
        _index++;
        return new HttpResponseMessage(status) { Content = new StringContent(body, Encoding.UTF8, "application/json") };
    }
}

public class LlmClientTests
{
    private static readonly LlmRequest Request = new()
    {
        Model = "gpt-6-luna",
        Messages = [new ChatMessage("system", "s"), new ChatMessage("user", "write a test")],
        Params = new Dictionary<string, object?> { ["temperature"] = 0, ["max_tokens"] = 8192, ["seed"] = 42, ["reasoning_effort"] = "none" },
    };

    private static LiteLlmClient Client(FakeLlmHandler handler) =>
        new(handler, "http://127.0.0.1:4000", "sk-local-test") { RetryDelays = [TimeSpan.Zero, TimeSpan.Zero, TimeSpan.Zero] };

    [Fact]
    public async Task RetriesOn429ThenSucceeds()
    {
        var handler = new FakeLlmHandler((HttpStatusCode.TooManyRequests, "{}"), (HttpStatusCode.OK, FakeLlmHandler.Ok("class A {}")));
        var response = await Client(handler).CompleteAsync(Request, CancellationToken.None);
        Assert.Equal("class A {}", response.Text);
        Assert.Equal(2, response.Usage.Attempts);
        Assert.Equal(1, response.Retries);
        Assert.Equal(120, response.Usage.PromptTokens);
        Assert.Equal("stop", response.Usage.FinishReason);
        Assert.Contains("\"reasoning_effort\":\"none\"", handler.Bodies[0]);
        Assert.Contains("\"seed\":42", handler.Bodies[0]);
    }

    [Fact]
    public async Task UnauthorizedPausesRun()
    {
        var handler = new FakeLlmHandler((HttpStatusCode.Unauthorized, "{\"error\":\"bad key sk-abcdefghijkl\"}"));
        var ex = await Assert.ThrowsAsync<LlmException>(() => Client(handler).CompleteAsync(Request, CancellationToken.None));
        Assert.Equal("LLM_CONFIG_ERROR", ex.Reason);
        Assert.True(ex.PausesRun);
        Assert.DoesNotContain("sk-abcdefghijkl", ex.Message);
        Assert.Equal(1, handler.Calls);
    }

    [Fact]
    public async Task ServerErrorsExhaustRetries()
    {
        var handler = new FakeLlmHandler((HttpStatusCode.BadGateway, "down"));
        var ex = await Assert.ThrowsAsync<LlmException>(() => Client(handler).CompleteAsync(Request, CancellationToken.None));
        Assert.Equal("LLM_UNAVAILABLE", ex.Reason);
        Assert.False(ex.PausesRun);
        Assert.Equal(4, handler.Calls);
    }

    [Fact]
    public async Task WrongModelIdPausesRun()
    {
        var handler = new FakeLlmHandler((HttpStatusCode.OK, FakeLlmHandler.Ok("x", model: "gpt-4o")));
        var gateway = new LlmGateway(Client(handler), 2, 0) { ExpectedPrefixes = new Dictionary<string, string> { ["gpt-6-luna"] = "gpt-6-luna" } };
        var ex = await Assert.ThrowsAsync<LlmException>(() => gateway.CompleteAsync(Request, CancellationToken.None));
        Assert.Equal("LLM_WRONG_MODEL", ex.Reason);
        Assert.True(ex.PausesRun);
    }

    [Fact]
    public async Task CacheHitDoesNotCallHttp()
    {
        var handler = new FakeLlmHandler((HttpStatusCode.OK, FakeLlmHandler.Ok("cached")));
        var dir = Path.Combine(Env.Root, "llm-cache-" + Guid.NewGuid().ToString("N"));
        var client = new CachingLlmClient(new LlmGateway(Client(handler), 2, 60), dir);
        var first = await client.CompleteAsync(Request, CancellationToken.None);
        var second = await client.CompleteAsync(Request, CancellationToken.None);
        Assert.False(first.Usage.CacheHit);
        Assert.True(second.Usage.CacheHit);
        Assert.Equal("cached", second.Text);
        Assert.Equal(1, handler.Calls);
        Assert.Single(Directory.GetFiles(dir));
    }

    [Fact]
    public async Task CallLimitStopsRun()
    {
        var handler = new FakeLlmHandler((HttpStatusCode.OK, FakeLlmHandler.Ok("x")));
        var gateway = new LlmGateway(Client(handler), 1, 0, maxCalls: 1);
        await gateway.CompleteAsync(Request, CancellationToken.None);
        var ex = await Assert.ThrowsAsync<LlmException>(() => gateway.CompleteAsync(Request with { Messages = [new ChatMessage("user", "other")] }, CancellationToken.None));
        Assert.Equal("LLM_CALL_LIMIT", ex.Reason);
    }
}

/// <summary>litellm.config.local.yaml nhiều model: đọc bản cũ một model, ghi và đọc lại, Ollama không cần khóa.</summary>
public class LiteLlmConfigFileTests
{
    [Fact]
    public void ReadsOldSingleModelFile()
    {
        var config = LiteLlmConfigFile.Parse("""
            # Cấu hình LiteLLM proxy của máy này. KHÔNG commit file này (chứa khóa API).
            model_list:
              - model_name: gpt-6-luna
                litellm_params:
                  model: openai/gpt-6-luna
                  api_base: https://api.example.com/v1
                  api_key: sk-test-1234
                  allowed_openai_params: ["reasoning_effort"]
            general_settings:
              master_key: sk-local-abc
            """);
        var route = Assert.Single(config.Routes);
        Assert.Equal("gpt-6-luna", route.ModelName);
        Assert.Equal("openai/gpt-6-luna", route.ProviderModel);
        Assert.Equal("api.example.com", route.EndpointHost);
        Assert.Equal(LlmProviderKind.OpenAiCompatible, route.Kind);
        Assert.True(route.IsComplete);
        Assert.Equal("sk-local-abc", config.MasterKey);
    }

    [Fact]
    public void RoundTripsSeveralModels()
    {
        var config = new LiteLlmConfigFile { MasterKey = "sk-local-x" }
            .WithRoute(new LiteLlmRoute { ModelName = "gpt-6-luna", ProviderModel = "openai/gpt-6-luna", ApiBase = "https://api.example.com/v1", ApiKey = "sk-a" })
            .WithRoute(new LiteLlmRoute { ModelName = "qwen3-coder-30b", ProviderModel = "ollama_chat/qwen3-coder:30b", ApiBase = "http://localhost:11434" })
            .WithRoute(new LiteLlmRoute { ModelName = "gpt-6-luna", ProviderModel = "openai/gpt-6-luna", ApiBase = "https://api.other.com/v1", ApiKey = "sk-b" });
        var yaml = config.ToYaml();
        Assert.DoesNotContain("api_key: \n", yaml);
        var read = LiteLlmConfigFile.Parse(yaml);
        Assert.Equal(["gpt-6-luna", "qwen3-coder-30b"], read.Routes.Select(r => r.ModelName));
        Assert.Equal("sk-b", read.Route("gpt-6-luna")!.ApiKey);
        var qwen = read.Route("qwen3-coder-30b")!;
        Assert.Equal(LlmProviderKind.Ollama, qwen.Kind);
        Assert.False(qwen.NeedsApiKey);
        Assert.True(qwen.IsComplete);
        Assert.Equal("api.other.com, localhost", read.EndpointHosts());
        Assert.Equal("localhost", read.EndpointHosts(["qwen3-coder-30b", "missing"]));
        Assert.Equal("sk-local-x", read.MasterKey);
        Assert.Single(read.WithoutRoute("gpt-6-luna").Routes);
    }

    [Fact]
    public void OpenAiRouteWithoutKeyIsIncomplete() =>
        Assert.False(new LiteLlmRoute { ModelName = "m", ProviderModel = "openai/m", ApiBase = "https://x/v1" }.IsComplete);
}
