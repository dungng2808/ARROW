using System.Text.Json;
using System.Text.Json.Nodes;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block2;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>
/// Phát lại lượt gọi đã hoàn thành khi chạy tiếp một run (spec 02 Mục 4.4). Khóa = SHA-256 của JSON chuẩn hóa
/// <c>{call_id, model, params, messages}</c>, nên hai lượt gọi khác <c>call_id</c> không bao giờ dùng chung mục cache.
/// Phát lại giữ nguyên số liệu của lần gọi thật, chỉ thêm <c>replayed = true</c>.
/// </summary>
public sealed class ReplayingLlmClient(ILlmClient inner, ILlmCacheRepository cache, string runId) : ILlmClient
{
    public async Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        var key = CacheKey(request);
        if (cache.TryGet(runId, key) is { } entry)
        {
            return entry.Response with { Usage = entry.Response.Usage with { Replayed = true } };
        }

        var response = await inner.CompleteAsync(request, cancellationToken).ConfigureAwait(false);
        cache.Put(runId, new LlmCacheEntry { Key = key, CallId = request.CallId, Response = response, StoredAt = DateTimeOffset.UtcNow });
        return response;
    }

    /// <summary>Khóa cache: tham số sắp theo tên, nên đổi thứ tự tham số trong cấu hình không đổi khóa.</summary>
    public static string CacheKey(LlmRequest request)
    {
        var parameters = new JsonObject();
        foreach (var (name, value) in request.Params.OrderBy(p => p.Key, StringComparer.Ordinal))
        {
            parameters[name] = JsonNode.Parse(JsonSerializer.Serialize(value, JsonFile.Compact));
        }

        var messages = new JsonArray();
        foreach (var message in request.Messages)
        {
            messages.Add(new JsonObject { ["role"] = message.Role, ["content"] = message.Content });
        }

        var canonical = new JsonObject
        {
            ["call_id"] = request.CallId,
            ["model"] = request.Model,
            ["params"] = parameters,
            ["messages"] = messages,
        };
        return Hashing.Sha256Text(canonical.ToJsonString());
    }
}
