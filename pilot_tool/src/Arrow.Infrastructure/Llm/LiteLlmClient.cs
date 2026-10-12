using System.Diagnostics;
using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Arrow.Core.Llm;
using Arrow.Core.Util;

namespace Arrow.Infrastructure.Llm;

/// <summary>
/// Gọi model qua LiteLLM proxy (specs/04 Mục 1): <c>POST {proxy}/v1/chat/completions</c>, timeout 300 giây, lỗi tạm thời
/// (429, 5xx, lỗi mạng, timeout) gọi lại tối đa 3 lần sau 20, 60, 120 giây; 400/401/403/404 là lỗi cấu hình, tạm dừng run.
/// </summary>
public sealed class LiteLlmClient : ILlmClient
{
    private readonly HttpClient _http;
    private readonly string _proxyUrl;
    private readonly string _masterKey;

    public LiteLlmClient(HttpMessageHandler handler, string proxyUrl, string masterKey)
    {
        _http = new HttpClient(handler, disposeHandler: false) { Timeout = Timeout.InfiniteTimeSpan };
        _proxyUrl = proxyUrl.TrimEnd('/');
        _masterKey = masterKey;
    }

    public TimeSpan CallTimeout { get; init; } = TimeSpan.FromSeconds(300);

    /// <summary>Thời gian chờ trước các lần gọi lại (AR 4).</summary>
    public IReadOnlyList<TimeSpan> RetryDelays { get; init; } = [TimeSpan.FromSeconds(20), TimeSpan.FromSeconds(60), TimeSpan.FromSeconds(120)];

    public async Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        var body = new Dictionary<string, object?> { ["model"] = request.Model };
        body["messages"] = request.Messages.Select(m => new Dictionary<string, string> { ["role"] = m.Role, ["content"] = m.Content }).ToList();
        foreach (var (key, value) in request.Params)
        {
            body[key] = value;
        }

        var json = JsonSerializer.Serialize(body);
        string? lastError = null;
        for (var attempt = 1; attempt <= RetryDelays.Count + 1; attempt++)
        {
            if (attempt > 1)
            {
                var delay = RetryDelays[attempt - 2];
                ToolLog.Warn($"LLM: {lastError}; gọi lại lần {attempt - 1} sau {delay.TotalSeconds:0}s");
                await Task.Delay(delay, cancellationToken).ConfigureAwait(false);
            }

            using var message = new HttpRequestMessage(HttpMethod.Post, _proxyUrl + "/v1/chat/completions")
            {
                Content = new StringContent(json, Encoding.UTF8, "application/json"),
            };
            message.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _masterKey);
            using var timeout = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
            timeout.CancelAfter(CallTimeout);
            var watch = Stopwatch.StartNew();
            HttpResponseMessage response;
            string text;
            try
            {
                response = await _http.SendAsync(message, timeout.Token).ConfigureAwait(false);
                text = await response.Content.ReadAsStringAsync(timeout.Token).ConfigureAwait(false);
            }
            catch (OperationCanceledException) when (!cancellationToken.IsCancellationRequested)
            {
                lastError = $"timeout sau {CallTimeout.TotalSeconds:0}s";
                continue;
            }
            catch (HttpRequestException ex)
            {
                lastError = "lỗi mạng: " + ex.Message;
                continue;
            }

            using (response)
            {
                var status = (int)response.StatusCode;
                if (response.StatusCode == HttpStatusCode.TooManyRequests || status >= 500)
                {
                    lastError = $"HTTP {status}: {Short(text)}";
                    continue;
                }

                if (!response.IsSuccessStatusCode)
                {
                    throw new LlmException("LLM_CONFIG_ERROR", $"HTTP {status}: {ToolLog.Redact(Short(text))}", pausesRun: true);
                }

                return Parse(text, watch.Elapsed.TotalSeconds, attempt, request.CacheKey());
            }
        }

        throw new LlmException("LLM_UNAVAILABLE", $"Không gọi được model sau {RetryDelays.Count} lần gọi lại: {ToolLog.Redact(lastError ?? "")}", pausesRun: false);
    }

    public static LlmResponse Parse(string json, double latency, int attempts, string cacheKey)
    {
        using var document = JsonDocument.Parse(json);
        var root = document.RootElement;
        var choice = root.GetProperty("choices")[0];
        var content = choice.GetProperty("message").TryGetProperty("content", out var c) && c.ValueKind == JsonValueKind.String ? c.GetString() ?? "" : "";
        var usage = root.TryGetProperty("usage", out var u) ? u : default;
        long Token(string name) => usage.ValueKind == JsonValueKind.Object && usage.TryGetProperty(name, out var t) && t.ValueKind == JsonValueKind.Number ? t.GetInt64() : 0;
        return new LlmResponse
        {
            Text = content,
            RawJson = json,
            Usage = new LlmUsage
            {
                ModelId = root.TryGetProperty("model", out var m) ? m.GetString() ?? "" : "",
                PromptTokens = Token("prompt_tokens"),
                CompletionTokens = Token("completion_tokens"),
                FinishReason = choice.TryGetProperty("finish_reason", out var f) && f.ValueKind == JsonValueKind.String ? f.GetString() : null,
                LatencySeconds = Math.Round(latency, 3),
                Attempts = attempts,
                CacheKey = cacheKey,
            },
        };
    }

    private static string Short(string text) => text.Length > 300 ? text[..300] + "…" : text;
}
