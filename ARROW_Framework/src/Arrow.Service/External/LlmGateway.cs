using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>
/// Giới hạn và kiểm tra quanh lượt gọi thật (spec 02 Mục 4.3 bước 3–4): tối đa <c>workers.llm</c> lượt cùng lúc, <c>rpm_limit</c> lượt mỗi
/// phút, <c>--max-llm-calls</c> lượt cả run (<c>B2_CALL_LIMIT</c>); model id trả về phải khớp <c>expected_model_id_prefix</c>
/// (<c>B2_WRONG_MODEL</c>); tính <c>cost_usd</c> theo giá của model.
/// </summary>
public sealed class LlmGateway : ILlmClient, IDisposable
{
    private readonly ILlmClient _inner;
    private readonly IReadOnlyDictionary<string, ModelConfig> _models;
    private readonly SemaphoreSlim _concurrency;
    private readonly int _rpmLimit;
    private readonly int? _maxCalls;
    private readonly Func<DateTimeOffset> _clock;
    private readonly Queue<DateTimeOffset> _recent = new();
    private readonly object _lock = new();
    private int _calls;

    public LlmGateway(ILlmClient inner, IEnumerable<ModelConfig> models, int maxConcurrent, int rpmLimit, int? maxCalls = null, Func<DateTimeOffset>? clock = null)
    {
        _inner = inner;
        _models = models.ToDictionary(m => m.Name, StringComparer.Ordinal);
        _concurrency = new SemaphoreSlim(Math.Max(1, maxConcurrent));
        _rpmLimit = rpmLimit;
        _maxCalls = maxCalls;
        _clock = clock ?? (() => DateTimeOffset.UtcNow);
    }

    /// <summary>Số lượt gọi thật đã bắt đầu.</summary>
    public int Calls => Volatile.Read(ref _calls);

    public async Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        if (!_models.TryGetValue(request.Model, out var model))
        {
            throw new ArrowException(ErrorCodes.B2LlmConfig, $"Cấu hình thực nghiệm không có model {request.Model}");
        }

        if (Interlocked.Increment(ref _calls) > _maxCalls)
        {
            Interlocked.Decrement(ref _calls);
            throw new ArrowException(ErrorCodes.B2CallLimit, $"Đã đủ {_maxCalls} lượt gọi model (--max-llm-calls)");
        }

        await _concurrency.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            await WaitForRateAsync(cancellationToken).ConfigureAwait(false);
            var response = await _inner.CompleteAsync(request, cancellationToken).ConfigureAwait(false);
            CheckModel(model, response.Usage.ModelId);
            return response with { Usage = response.Usage with { CostUsd = Cost(model.PriceUsdPerMillion, response.Usage) } };
        }
        finally
        {
            _concurrency.Release();
        }
    }

    /// <summary>Model id trả về bắt đầu bằng tiền tố, hoặc chứa <c>/&lt;tiền tố&gt;</c> (provider thêm tiền tố đường dẫn).</summary>
    public static bool ModelMatches(string expectedPrefix, string modelId) =>
        expectedPrefix.Length == 0
        || modelId.StartsWith(expectedPrefix, StringComparison.OrdinalIgnoreCase)
        || modelId.Contains("/" + expectedPrefix, StringComparison.OrdinalIgnoreCase);

    private static void CheckModel(ModelConfig model, string modelId)
    {
        if (!ModelMatches(model.ExpectedModelIdPrefix, modelId))
        {
            throw new ArrowException(ErrorCodes.B2WrongModel, $"Model {model.Name}: response ghi model '{modelId}', không khớp tiền tố '{model.ExpectedModelIdPrefix}'");
        }
    }

    /// <summary><c>cost_usd</c> = token vào / 1e6 × giá vào + token ra / 1e6 × giá ra; không có giá thì 0.</summary>
    public static double Cost(PriceConfig? price, LlmUsage usage) =>
        price is null ? 0 : Math.Round(usage.PromptTokens / 1e6 * price.Input + usage.CompletionTokens / 1e6 * price.Output, 8);

    private async Task WaitForRateAsync(CancellationToken cancellationToken)
    {
        if (_rpmLimit <= 0)
        {
            return;
        }

        while (true)
        {
            TimeSpan wait;
            lock (_lock)
            {
                var now = _clock();
                while (_recent.Count > 0 && now - _recent.Peek() >= TimeSpan.FromMinutes(1))
                {
                    _recent.Dequeue();
                }

                if (_recent.Count < _rpmLimit)
                {
                    _recent.Enqueue(now);
                    return;
                }

                wait = _recent.Peek() + TimeSpan.FromMinutes(1) - now;
            }

            // Kiểm tra lại ít nhất mỗi giây (đồng hồ có thể được thay trong test).
            var delay = wait < TimeSpan.FromMilliseconds(50) ? TimeSpan.FromMilliseconds(50) : wait > TimeSpan.FromSeconds(1) ? TimeSpan.FromSeconds(1) : wait;
            await Task.Delay(delay, cancellationToken).ConfigureAwait(false);
        }
    }

    public void Dispose() => _concurrency.Dispose();
}
