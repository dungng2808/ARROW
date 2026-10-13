using Arrow.Core.Util;

namespace Arrow.Core.Runs;

/// <summary>
/// Điều khiển tạm dừng/tiếp tục/dừng hẳn qua <c>control.json</c> (specs/01 §4.3, ARCHITECTURE §8).
/// Runner gọi <see cref="WaitIfPausedAsync"/> trước mỗi job; bước đang chạy không bị ngắt.
/// </summary>
public sealed class ControlGate
{
    private readonly RunStore _store;
    private readonly Func<DateTimeOffset> _clock;
    private readonly TimeSpan _pollInterval;
    private readonly List<PauseInterval> _pauses = [];
    private readonly object _lock = new();
    private DateTimeOffset? _lastSeenRequest;
    private DateTimeOffset? _pausedSince;

    public ControlGate(RunStore store, Func<DateTimeOffset>? clock = null, TimeSpan? pollInterval = null)
    {
        _store = store;
        _clock = clock ?? (() => DateTimeOffset.UtcNow);
        _pollInterval = pollInterval ?? TimeSpan.FromSeconds(2);
    }

    public bool PauseRequested { get; private set; }
    public bool StopRequested { get; private set; }
    public IReadOnlyList<PauseInterval> Pauses => _pauses;
    public double PausedSeconds
    {
        get
        {
            lock (_lock)
            {
                return _pauses.Sum(p => p.Seconds) + (_pausedSince is { } from ? (_clock() - from).TotalSeconds : 0);
            }
        }
    }

    /// <summary>Phát sinh khi trạng thái điều khiển đổi (để cập nhật status.json, dashboard).</summary>
    public event Action? Changed;

    /// <summary>Ghi lệnh vào control.json (phím tắt, lệnh <c>control</c>, menu).</summary>
    public static void Request(RunStore store, ControlCommand command, string requestedBy) =>
        store.WriteControl(new ControlFile { Command = command, RequestedAt = DateTimeOffset.UtcNow, RequestedBy = requestedBy });

    /// <summary>Bỏ lệnh cũ khi bắt đầu một phiên chạy mới (lệnh stop của phiên trước không có hiệu lực).</summary>
    public void ResetForNewSession()
    {
        if (File.Exists(_store.ControlPath))
        {
            File.Delete(_store.ControlPath);
        }

        PauseRequested = false;
        StopRequested = false;
        _lastSeenRequest = null;
    }

    /// <summary>Đọc control.json và cập nhật cờ. Gọi định kỳ hoặc trước mỗi job.</summary>
    public void Poll()
    {
        var control = _store.ReadControl();
        lock (_lock)
        {
            if (control is null || control.RequestedAt == _lastSeenRequest)
            {
                return;
            }

            _lastSeenRequest = control.RequestedAt;
            ToolLog.Info($"run {_store.RunId}: nhận lệnh {control.Command.ToString().ToLowerInvariant()} (từ {control.RequestedBy})");
            var wasPaused = PauseRequested;
            switch (control.Command)
            {
                case ControlCommand.Pause:
                    PauseRequested = true;
                    break;
                case ControlCommand.Resume:
                    PauseRequested = false;
                    break;
                case ControlCommand.Stop:
                    StopRequested = true;
                    PauseRequested = false;
                    break;
            }

            // Mỗi lần tạm dừng chỉ ghi một khoảng, dù có bao nhiêu worker đang chờ.
            if (!wasPaused && PauseRequested)
            {
                _pausedSince = _clock();
            }
            else if (wasPaused && !PauseRequested && _pausedSince is { } from)
            {
                var interval = new PauseInterval(from, _clock());
                _pauses.Add(interval);
                _store.AppendLedger(new LedgerEntry { Pause = interval });
                ToolLog.Info($"run {_store.RunId}: đã tạm dừng {interval.Seconds:0.0}s");
                _pausedSince = null;
            }
        }

        Changed?.Invoke();
    }

    /// <summary>
    /// Chờ trong khi đang tạm dừng. Trả về <c>false</c> nếu có lệnh dừng hẳn (runner phải thoát,
    /// không bắt đầu job mới). Khoảng tạm dừng được ghi vào ledger ở <see cref="Poll"/>, một lần cho mỗi lần tạm dừng.
    /// </summary>
    public async Task<bool> WaitIfPausedAsync(CancellationToken cancellationToken)
    {
        Poll();
        if (StopRequested)
        {
            return false;
        }

        if (!PauseRequested)
        {
            return true;
        }

        while (PauseRequested && !StopRequested)
        {
            await Task.Delay(_pollInterval, cancellationToken).ConfigureAwait(false);
            Poll();
        }

        return !StopRequested;
    }
}
