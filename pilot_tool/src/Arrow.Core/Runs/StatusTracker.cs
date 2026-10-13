namespace Arrow.Core.Runs;

/// <summary>
/// Giữ trạng thái hiện tại của run và ghi <c>status.json</c> (heartbeat mỗi 5 giây, specs/01 §4.3).
/// Thread-safe: các worker gọi đồng thời.
/// </summary>
public sealed class StatusTracker : IDisposable
{
    private readonly object _lock = new();
    private readonly RunStore _store;
    private readonly ControlGate _gate;
    private readonly Func<DateTimeOffset> _clock;
    private readonly DateTimeOffset _startedAt;
    private readonly double _previousElapsed;
    private readonly double _previousPaused;
    private readonly Dictionary<string, RunningJob> _running = [];
    private readonly Dictionary<string, StageProgress> _stages = [];
    private readonly Dictionary<string, int> _outcomes = [];
    private readonly Timer _heartbeat;
    private RunState _state = RunState.Running;
    private string _stage = "";
    private long _promptTokens;
    private long _completionTokens;
    private double _costUsd;

    public StatusTracker(RunStore store, ControlGate gate, Func<DateTimeOffset>? clock = null)
    {
        _store = store;
        _gate = gate;
        _clock = clock ?? (() => DateTimeOffset.UtcNow);
        _startedAt = _clock();
        var previous = store.ReadStatus(); // chạy tiếp: cộng dồn thời gian của các phiên trước
        _previousElapsed = previous?.ElapsedSeconds ?? 0;
        _previousPaused = previous?.PausedSeconds ?? 0;
        _gate.Changed += OnGateChanged;
        _heartbeat = new Timer(_ => Flush(), null, TimeSpan.Zero, TimeSpan.FromSeconds(5));
    }

    public void SetStage(string stage, int total, int alreadyDone, int infra = 0)
    {
        lock (_lock)
        {
            _stage = stage;
            _stages[stage] = new StageProgress { Stage = stage, Total = total, Done = alreadyDone, Infra = infra };
        }

        Flush();
    }

    public void JobStarted(string worker, string taskId, string label, string step)
    {
        lock (_lock)
        {
            _running[worker] = new RunningJob { Worker = worker, TaskId = taskId, Label = label, Step = step, Since = _clock() };
        }

        Flush();
    }

    public void JobStep(string worker, string step)
    {
        lock (_lock)
        {
            if (_running.TryGetValue(worker, out var job))
            {
                _running[worker] = job with { Step = step };
            }
        }
    }

    public void JobFinished(string worker, string stage, string? outcome, bool infra, long promptTokens = 0, long completionTokens = 0, double costUsd = 0)
    {
        lock (_lock)
        {
            _running.Remove(worker);
            if (_stages.TryGetValue(stage, out var progress))
            {
                _stages[stage] = progress with { Done = progress.Done + 1, Infra = progress.Infra + (infra ? 1 : 0) };
            }

            if (!string.IsNullOrEmpty(outcome))
            {
                _outcomes[outcome] = _outcomes.GetValueOrDefault(outcome) + 1;
            }

            _promptTokens += promptTokens;
            _completionTokens += completionTokens;
            _costUsd += costUsd;
        }

        Flush();
    }

    /// <summary>Đếm kết quả của job đã xong ở phiên trước (khi chạy tiếp).</summary>
    public void SeedOutcome(string outcome)
    {
        lock (_lock)
        {
            _outcomes[outcome] = _outcomes.GetValueOrDefault(outcome) + 1;
        }
    }

    public void SetState(RunState state)
    {
        lock (_lock)
        {
            _state = state;
        }

        Flush();
    }

    private void OnGateChanged()
    {
        lock (_lock)
        {
            if (_state is RunState.Done or RunState.Failed or RunState.Stopped)
            {
                return;
            }

            _state = _gate.StopRequested ? RunState.Stopping
                : _gate.PauseRequested ? (_running.Count > 0 ? RunState.Pausing : RunState.Paused)
                : RunState.Running;
        }

        Flush();
    }

    /// <summary>Khi đang PAUSING mà các job đang chạy đã xong thì chuyển sang PAUSED.</summary>
    public void OnIdleWhilePaused()
    {
        lock (_lock)
        {
            if (_state == RunState.Pausing && _running.Count == 0)
            {
                _state = RunState.Paused;
            }
        }

        Flush();
    }

    public RunStatus Snapshot()
    {
        lock (_lock)
        {
            var now = _clock();
            return new RunStatus
            {
                State = _state,
                Stage = _stage,
                Since = _startedAt,
                RunningJobs = _running.Values.OrderBy(j => j.Worker, StringComparer.Ordinal).ToList(),
                Stages = _stages.Values.ToList(),
                Outcomes = new Dictionary<string, int>(_outcomes),
                ElapsedSeconds = Math.Round(_previousElapsed + (now - _startedAt).TotalSeconds, 1),
                PausedSeconds = Math.Round(_previousPaused + _gate.PausedSeconds, 1),
                PromptTokens = _promptTokens,
                CompletionTokens = _completionTokens,
                CostUsd = Math.Round(_costUsd, 4),
                Pid = System.Environment.ProcessId,
                Host = System.Environment.MachineName,
                HeartbeatAt = now,
            };
        }
    }

    public void Flush()
    {
        try
        {
            _store.WriteStatus(Snapshot());
        }
        catch (IOException)
        {
            // Bỏ qua: lần heartbeat sau sẽ ghi lại.
        }
    }

    public void Dispose()
    {
        _gate.Changed -= OnGateChanged;
        _heartbeat.Dispose();
        Flush();
    }
}
