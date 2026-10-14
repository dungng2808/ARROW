using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>
/// Hàng đợi <c>build</c> (spec 00 Mục 5.2, spec 07 Mục 8): tối đa <c>workers.build</c> tiến trình ngoài (Maven, Gradle, Java, git) chạy
/// cùng lúc; job chỉ giữ chỗ trong lúc tiến trình chạy. Chỉ giới hạn ở tầng tiến trình nên không có chờ lồng nhau.
/// </summary>
public sealed class ThrottledProcessRunner(IProcessRunner inner, int maxConcurrent) : IProcessRunner, IDisposable
{
    private readonly SemaphoreSlim _gate = new(Math.Max(1, maxConcurrent));

    /// <summary>Số tiến trình đang chạy.</summary>
    public int Running => Math.Max(1, maxConcurrent) - _gate.CurrentCount;

    public async Task<ProcessResult> RunAsync(ProcessSpec spec, CancellationToken cancellationToken)
    {
        await _gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            return await inner.RunAsync(spec, cancellationToken).ConfigureAwait(false);
        }
        finally
        {
            _gate.Release();
        }
    }

    public void Dispose() => _gate.Dispose();
}
