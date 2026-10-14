namespace Arrow.Service.Pipeline;

/// <summary>
/// Điều khiển run (spec 07 Mục 8): được kiểm tra trước mỗi bước. Tạm dừng = không bắt đầu bước mới (bước đang chạy chạy nốt);
/// dừng = hủy <see cref="Token"/> (tiến trình đang chạy bị giết, job chưa ghi ledger được làm lại khi chạy tiếp).
/// </summary>
public sealed class ControlGate : IDisposable
{
    private readonly CancellationTokenSource _stop;
    private readonly object _lock = new();
    private TaskCompletionSource? _resumed;

    public ControlGate(CancellationToken outer = default) => _stop = CancellationTokenSource.CreateLinkedTokenSource(outer);

    public CancellationToken Token => _stop.Token;

    public bool IsStopped => _stop.IsCancellationRequested;

    public bool IsPaused
    {
        get
        {
            lock (_lock)
            {
                return _resumed is not null;
            }
        }
    }

    /// <summary>Lý do tạm dừng (ví dụ lỗi cấu hình model), null khi người dùng tự tạm dừng hoặc không tạm dừng.</summary>
    public string? PausedReason { get; private set; }

    public void Pause(string? reason = null)
    {
        lock (_lock)
        {
            _resumed ??= new TaskCompletionSource(TaskCreationOptions.RunContinuationsAsynchronously);
            PausedReason = reason;
        }
    }

    public void Resume()
    {
        TaskCompletionSource? resumed;
        lock (_lock)
        {
            resumed = _resumed;
            _resumed = null;
            PausedReason = null;
        }

        resumed?.TrySetResult();
    }

    public void Stop()
    {
        Resume();
        _stop.Cancel();
    }

    /// <summary>Chờ tới khi không tạm dừng; ném <see cref="OperationCanceledException"/> khi đã dừng.</summary>
    public async Task WaitAsync()
    {
        while (true)
        {
            Token.ThrowIfCancellationRequested();
            Task? wait;
            lock (_lock)
            {
                wait = _resumed?.Task;
            }

            if (wait is null)
            {
                return;
            }

            await wait.WaitAsync(Token).ConfigureAwait(false);
        }
    }

    public void Dispose() => _stop.Dispose();
}
