using System.Diagnostics;
using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>Gọi lại khi lỗi tạm thời (spec 02 Mục 4.3 bước 5): chờ 20 s, 60 s, 120 s; mỗi lần gọi tối đa 300 s.</summary>
public sealed record LlmRetryOptions
{
    public IReadOnlyList<TimeSpan> Delays { get; init; } = [TimeSpan.FromSeconds(20), TimeSpan.FromSeconds(60), TimeSpan.FromSeconds(120)];
    public TimeSpan RequestTimeout { get; init; } = TimeSpan.FromSeconds(300);
}

/// <summary>
/// <c>POST &lt;proxy&gt;/v1/chat/completions</c> theo chuẩn OpenAI tới proxy LiteLLM (spec 02 Mục 4.1–4.3). Tham số gửi nguyên văn
/// (không <c>drop_params</c>). 429, 5xx, lỗi mạng, timeout → gọi lại; 400/401/402/403/404 → <c>B2_LLM_CONFIG</c>;
/// hết lượt gọi lại → <c>B2_LLM_UNAVAILABLE</c>.
/// </summary>
public sealed class LiteLlmClient(HttpClient http, string proxyUrl, string? masterKey, ILogRepository log, LlmRetryOptions? retry = null) : ILlmClient
{
    private readonly LlmRetryOptions _retry = retry ?? new LlmRetryOptions();

    public async Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        var body = RequestBody(request);
        var url = proxyUrl.TrimEnd('/') + "/v1/chat/completions";
        string lastError = "";
        for (var attempt = 1; attempt <= _retry.Delays.Count + 1; attempt++)
        {
            if (attempt > 1)
            {
                await Task.Delay(_retry.Delays[attempt - 2], cancellationToken).ConfigureAwait(false);
            }

            using var message = new HttpRequestMessage(HttpMethod.Post, url) { Content = new StringContent(body, Encoding.UTF8, "application/json") };
            if (!string.IsNullOrEmpty(masterKey))
            {
                message.Headers.Authorization = new AuthenticationHeaderValue("Bearer", masterKey);
            }

            using var timeout = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
            timeout.CancelAfter(_retry.RequestTimeout);
            var watch = Stopwatch.StartNew();
            HttpResponseMessage response;
            string text;
            try
            {
                response = await http.SendAsync(message, timeout.Token).ConfigureAwait(false);
                text = await response.Content.ReadAsStringAsync(timeout.Token).ConfigureAwait(false);
            }
            catch (OperationCanceledException) when (!cancellationToken.IsCancellationRequested)
            {
                lastError = $"quá {_retry.RequestTimeout.TotalSeconds:0} s";
                log.Warn($"[K2] {request.CallId}: lượt gọi {attempt} {lastError}");
                continue;
            }
            catch (HttpRequestException ex)
            {
                lastError = "lỗi mạng: " + ex.Message;
                log.Warn($"[K2] {request.CallId}: lượt gọi {attempt} {lastError}");
                continue;
            }

            using (response)
            {
                var status = (int)response.StatusCode;
                if (response.IsSuccessStatusCode)
                {
                    if (TryParse(text, request, attempt, watch.Elapsed) is { } parsed)
                    {
                        return parsed;
                    }

                    lastError = "response không đúng định dạng chat completion";
                }
                else if (status is 400 or 401 or 402 or 403 or 404)
                {
                    throw new ArrowException(ErrorCodes.B2LlmConfig, $"Proxy trả {status} cho model {request.Model}: {Redactor.Redact(ErrorMessage(text))}");
                }
                else
                {
                    lastError = $"HTTP {status} ({Redactor.Redact(ErrorMessage(text))})";
                }

                log.Warn($"[K2] {request.CallId}: lượt gọi {attempt} {lastError}");
            }
        }

        throw new ArrowException(ErrorCodes.B2LlmUnavailable, $"{request.CallId}: không gọi được model {request.Model} sau {_retry.Delays.Count + 1} lần ({lastError})");
    }

    /// <summary>Body JSON: <c>model</c>, <c>messages</c> và các tham số của model, giữ nguyên tên.</summary>
    public static string RequestBody(LlmRequest request)
    {
        var body = new Dictionary<string, object?>(StringComparer.Ordinal)
        {
            ["model"] = request.Model,
            ["messages"] = request.Messages.Select(m => new Dictionary<string, string> { ["role"] = m.Role, ["content"] = m.Content }).ToList(),
        };
        foreach (var (key, value) in request.Params)
        {
            body[key] = value;
        }

        return JsonSerializer.Serialize(body, JsonFile.Compact);
    }

    private static LlmResponse? TryParse(string text, LlmRequest request, int attempt, TimeSpan latency)
    {
        try
        {
            using var document = JsonDocument.Parse(text);
            var root = document.RootElement;
            var choice = root.GetProperty("choices")[0];
            var content = choice.GetProperty("message").TryGetProperty("content", out var c) && c.ValueKind == JsonValueKind.String ? c.GetString() ?? "" : "";
            var finish = choice.TryGetProperty("finish_reason", out var f) && f.ValueKind == JsonValueKind.String ? f.GetString() : null;
            long tokensIn = 0, tokensOut = 0;
            if (root.TryGetProperty("usage", out var usage) && usage.ValueKind == JsonValueKind.Object)
            {
                tokensIn = usage.TryGetProperty("prompt_tokens", out var pi) && pi.ValueKind == JsonValueKind.Number ? pi.GetInt64() : 0;
                tokensOut = usage.TryGetProperty("completion_tokens", out var po) && po.ValueKind == JsonValueKind.Number ? po.GetInt64() : 0;
            }

            return new LlmResponse
            {
                Text = content,
                RawJson = text,
                Usage = new LlmUsage
                {
                    CallId = request.CallId,
                    ModelId = root.TryGetProperty("model", out var model) && model.ValueKind == JsonValueKind.String ? model.GetString() ?? "" : "",
                    PromptTokens = tokensIn,
                    CompletionTokens = tokensOut,
                    FinishReason = finish,
                    LatencySeconds = Math.Round(latency.TotalSeconds, 3),
                    Attempts = attempt,
                },
            };
        }
        catch (Exception ex) when (ex is JsonException or KeyNotFoundException or InvalidOperationException or IndexOutOfRangeException)
        {
            return null;
        }
    }

    /// <summary>Thông điệp lỗi trong body (<c>error.message</c>) hoặc 300 ký tự đầu.</summary>
    private static string ErrorMessage(string text)
    {
        try
        {
            using var document = JsonDocument.Parse(text);
            if (document.RootElement.TryGetProperty("error", out var error))
            {
                return error.ValueKind == JsonValueKind.Object && error.TryGetProperty("message", out var m) ? m.ToString() : error.ToString();
            }
        }
        catch (JsonException)
        {
        }

        return text.Length > 300 ? text[..300] : text;
    }
}
