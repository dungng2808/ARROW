using System.Text.Json;
using Arrow.Core.Llm;
using Arrow.Core.Util;

namespace Arrow.Infrastructure.Llm;

/// <summary>
/// Cache response theo khóa của prompt (specs/04 Mục 1.1): <c>runs/&lt;run_id&gt;/llm_cache/&lt;sha256&gt;.json</c>.
/// Có cache thì dùng, không gọi API; response đầu tiên là bản chính thức, không ghi đè.
/// </summary>
public sealed class CachingLlmClient(ILlmClient inner, string cacheDir) : ILlmClient
{
    private sealed record CacheEntry(string Text, string RawJson, LlmUsage Usage);

    public async Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        var key = request.CacheKey();
        var path = Path.Combine(cacheDir, key + ".json");
        if (File.Exists(path))
        {
            var cached = Json.Read<CacheEntry>(path);
            return new LlmResponse { Text = cached.Text, RawJson = cached.RawJson, Usage = cached.Usage with { CacheHit = true, Attempts = 0, LatencySeconds = 0, CacheKey = key } };
        }

        var response = await inner.CompleteAsync(request, cancellationToken).ConfigureAwait(false);
        Directory.CreateDirectory(cacheDir);
        if (!File.Exists(path))
        {
            Json.WriteAtomic(path, new CacheEntry(response.Text, response.RawJson, response.Usage with { CacheKey = key }));
        }

        return response with { Usage = response.Usage with { CacheKey = key } };
    }
}

/// <summary>
/// Giới hạn trên máy (specs/04 Mục 1): tối đa <c>workers.llm</c> lượt gọi đồng thời và <c>rpm_limit</c> lượt/phút;
/// kiểm tra model id do API trả về bắt đầu bằng <c>expected_model_id_prefix</c> (sai → LLM_WRONG_MODEL, tạm dừng run).
/// Đếm số lượt gọi thật để chặn quá giới hạn của lần chạy (smoke).
/// </summary>
public sealed class LlmGateway(ILlmClient inner, int concurrency, int rpmLimit, int? maxCalls = null) : ILlmClient
{
    private readonly SemaphoreSlim _slots = new(Math.Max(1, concurrency), Math.Max(1, concurrency));
    private readonly Queue<DateTimeOffset> _recent = new();
    private readonly object _lock = new();
    private int _calls;

    public IReadOnlyDictionary<string, string> ExpectedPrefixes { get; init; } = new Dictionary<string, string>();

    /// <summary>Số lượt đã gửi tới API (không gồm cache hit).</summary>
    public int Calls => _calls;

    public async Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        await _slots.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            await WaitForRateAsync(cancellationToken).ConfigureAwait(false);
            var response = await inner.CompleteAsync(request, cancellationToken).ConfigureAwait(false);
            if (!response.Usage.CacheHit && maxCalls is not null && Interlocked.Increment(ref _calls) > maxCalls)
            {
                throw new LlmException("LLM_CALL_LIMIT", $"Vượt giới hạn {maxCalls} lượt gọi của lần chạy này", pausesRun: true);
            }

            if (ExpectedPrefixes.TryGetValue(request.Model, out var prefix) && !response.Usage.ModelId.StartsWith(prefix, StringComparison.Ordinal)
                && !response.Usage.ModelId.Contains("/" + prefix, StringComparison.Ordinal))
            {
                throw new LlmException("LLM_WRONG_MODEL", $"API trả model id '{response.Usage.ModelId}', cần bắt đầu bằng '{prefix}'", pausesRun: true);
            }

            return response;
        }
        finally
        {
            _slots.Release();
        }
    }

    private async Task WaitForRateAsync(CancellationToken cancellationToken)
    {
        if (rpmLimit <= 0)
        {
            return;
        }

        while (true)
        {
            TimeSpan wait;
            lock (_lock)
            {
                var now = DateTimeOffset.UtcNow;
                while (_recent.Count > 0 && now - _recent.Peek() >= TimeSpan.FromMinutes(1))
                {
                    _recent.Dequeue();
                }

                if (_recent.Count < rpmLimit)
                {
                    _recent.Enqueue(now);
                    return;
                }

                wait = TimeSpan.FromMinutes(1) - (now - _recent.Peek());
            }

            await Task.Delay(wait, cancellationToken).ConfigureAwait(false);
        }
    }
}

/// <summary>
/// Model giả cho kiểm thử và chạy thử pipeline không tốn tiền: trả response theo kịch bản. Kịch bản là danh sách luật,
/// luật đầu tiên mà mọi chuỗi <c>contains</c> có trong message cuối được dùng; mỗi luật trả lần lượt các response
/// (hết thì lặp lại response cuối).
/// </summary>
public sealed class FakeLlmClient : ILlmClient
{
    public sealed record Rule
    {
        public List<string> Contains { get; init; } = [];
        public List<string> Responses { get; init; } = [];
        public string FinishReason { get; init; } = "stop";
    }

    private readonly List<Rule> _rules;
    private readonly Dictionary<int, int> _used = [];
    private readonly object _lock = new();

    public FakeLlmClient(IEnumerable<Rule> rules, string modelId = "gpt-6-luna-fake")
    {
        _rules = rules.ToList();
        ModelId = modelId;
    }

    public string ModelId { get; }
    public List<LlmRequest> Requests { get; } = [];

    public static FakeLlmClient FromFile(string path) => new(Json.Read<List<Rule>>(path));

    public Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        var last = request.Messages.Count == 0 ? "" : request.Messages[^1].Content;
        lock (_lock)
        {
            Requests.Add(request);
            for (var i = 0; i < _rules.Count; i++)
            {
                var rule = _rules[i];
                if (rule.Contains.All(c => last.Contains(c, StringComparison.Ordinal)) && rule.Responses.Count > 0)
                {
                    var index = _used.GetValueOrDefault(i);
                    _used[i] = index + 1;
                    var text = rule.Responses[Math.Min(index, rule.Responses.Count - 1)];
                    var raw = JsonSerializer.Serialize(new
                    {
                        id = "fake-" + Requests.Count,
                        model = ModelId,
                        choices = new[] { new { index = 0, finish_reason = rule.FinishReason, message = new { role = "assistant", content = text } } },
                        usage = new { prompt_tokens = last.Length / 4, completion_tokens = text.Length / 4, total_tokens = (last.Length + text.Length) / 4 },
                    });
                    return Task.FromResult(LiteLlmClient.Parse(raw, 0.01, 1, request.CacheKey()));
                }
            }
        }

        throw new LlmException("LLM_CONFIG_ERROR", "FakeLlmClient: không có luật nào khớp với prompt", pausesRun: true);
    }
}
