using System.Text.Json;
using Arrow.Core.Util;

namespace Arrow.Core.Llm;

public sealed record ChatMessage(string Role, string Content);

/// <summary>Một lượt gọi model: tên model trên proxy, messages, tham số lấy đúng từ cấu hình thực nghiệm (specs/04 Mục 1).</summary>
public sealed record LlmRequest
{
    public string Model { get; init; } = "";
    public IReadOnlyList<ChatMessage> Messages { get; init; } = [];
    public IReadOnlyDictionary<string, object?> Params { get; init; } = new Dictionary<string, object?>();

    /// <summary>Khóa cache = SHA-256 của JSON chuẩn hóa {model, params, messages} (specs/04 Mục 1.1).</summary>
    public string CacheKey()
    {
        var canonical = new
        {
            model = Model,
            @params = Params.OrderBy(p => p.Key, StringComparer.Ordinal).ToDictionary(p => p.Key, p => Normalize(p.Value)),
            messages = Messages.Select(m => new { role = m.Role, content = m.Content }).ToList(),
        };
        return Hashing.Sha256Text(JsonSerializer.Serialize(canonical, Json.Compact));
    }

    private static object? Normalize(object? value) => value is JsonElement element ? element.ValueKind switch
    {
        JsonValueKind.Number => element.TryGetInt64(out var l) ? l : element.GetDouble(),
        JsonValueKind.String => element.GetString(),
        JsonValueKind.True => true,
        JsonValueKind.False => false,
        JsonValueKind.Null => null,
        _ => element.GetRawText(),
    } : value;
}

/// <summary><c>usage.json</c> (specs/04 Mục 1).</summary>
public sealed record LlmUsage
{
    public string ModelId { get; init; } = "";
    public long PromptTokens { get; init; }
    public long CompletionTokens { get; init; }
    public string? FinishReason { get; init; }
    public double LatencySeconds { get; init; }

    /// <summary>Số lần gửi HTTP (1 = không phải gọi lại).</summary>
    public int Attempts { get; init; } = 1;
    public bool CacheHit { get; init; }
    public string? CacheKey { get; init; }
}

public sealed record LlmResponse
{
    public string Text { get; init; } = "";
    public LlmUsage Usage { get; init; } = new();

    /// <summary>Body JSON gốc của API (<c>response.json</c>).</summary>
    public string RawJson { get; init; } = "{}";

    /// <summary>Số lần gọi lại vì lỗi tạm thời (không tính là vòng sửa).</summary>
    public int Retries => Math.Max(0, Usage.Attempts - 1);
}

public interface ILlmClient
{
    Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken);
}

/// <summary>
/// Lỗi gọi model: <c>LLM_UNAVAILABLE</c> (hết lượt gọi lại), <c>LLM_CONFIG_ERROR</c> (400/401/403/404),
/// <c>LLM_WRONG_MODEL</c> (model id không đúng tiền tố). Hai lỗi sau làm tạm dừng cả run.
/// </summary>
public sealed class LlmException(string reason, string message, bool pausesRun) : Exception(message)
{
    public string Reason { get; } = reason;
    public bool PausesRun { get; } = pausesRun;
}

/// <summary>Giá theo triệu token (specs/04 Mục 1.2); lượt cache_hit không tính tiền.</summary>
public static class LlmCost
{
    public static double Usd(LlmUsage usage, double? inputPerMillion, double? outputPerMillion) =>
        usage.CacheHit || inputPerMillion is null || outputPerMillion is null
            ? 0
            : usage.PromptTokens / 1_000_000.0 * inputPerMillion.Value + usage.CompletionTokens / 1_000_000.0 * outputPerMillion.Value;
}
