using Arrow.Cli.Ui;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>Kết quả một job do stage trả về; RunHost ghi ledger, sự kiện và cập nhật trạng thái.</summary>
public sealed record JobResult
{
    public JobStatus Status { get; init; } = JobStatus.Done;
    public string? Outcome { get; init; }
    public string? Reason { get; init; }
    public string Message { get; init; } = "";
    public double? LlmSeconds { get; init; }
    public double? BuildSeconds { get; init; }
    public List<string> Outputs { get; init; } = [];
    public long PromptTokens { get; init; }
    public long CompletionTokens { get; init; }

    /// <summary>Lỗi cấu hình model (401, sai model id): tạm dừng cả run (specs/04 Mục 1).</summary>
    public bool PauseRun { get; init; }
}

/// <summary>Worker đang chạy job; <see cref="Step"/> báo bước hiện tại lên dashboard.</summary>
public sealed record JobContext(string Worker, Action<string> Step);

/// <summary>Một stage cần chạy: danh sách job và cách chạy một job.</summary>
public sealed record StageSpec(
    string Stage,
    IReadOnlyList<JobKey> Jobs,
    int Workers,
    string WorkerPrefix,
    Func<JobKey, JobContext, CancellationToken, Task<JobResult>> Execute,
    Func<JobKey, string>? Label = null);

/// <summary>
/// Phần chung của mọi lần chạy (demo và thật): ledger, chạy tiếp, tạm dừng/tiếp tục/dừng hẳn,
/// dashboard hoặc chế độ <c>--plain</c> (ARCHITECTURE §5.1, §8).
/// </summary>
public sealed class RunHost(RunStore store, DashboardView.Header header, bool plain, bool rerunInfra)
{
    /// <summary>Chạy lần lượt các stage. Trả về true nếu dừng vì lệnh dừng hẳn.</summary>
    public Task<bool> RunAsync(IReadOnlyList<StageSpec> stages, CancellationToken cancellationToken) =>
        RunAsync(stages.Select(s => (Func<StageSpec?>)(() => s)).ToList(), cancellationToken);

    /// <summary>
    /// Chạy lần lượt các stage; mỗi stage được dựng ngay trước khi chạy (danh sách job của stage sau phụ thuộc kết quả
    /// stage trước), null thì bỏ qua. Trả về true nếu dừng vì lệnh dừng hẳn.
    /// </summary>
    public async Task<bool> RunAsync(IReadOnlyList<Func<StageSpec?>> stageFactories, CancellationToken cancellationToken)
    {
        var gate = new ControlGate(store);
        gate.ResetForNewSession();
        using var tracker = new StatusTracker(store, gate);
        var latest = store.LatestByJob();
        foreach (var entry in latest.Values.Where(e => e.Outcome is not null))
        {
            tracker.SeedOutcome(entry.Outcome!);
        }

        var interactive = !plain && !Console.IsInputRedirected && !Console.IsOutputRedirected;
        Console.CancelKeyPress += (_, e) =>
        {
            e.Cancel = true;
            ControlGate.Request(store, ControlCommand.Stop, "hotkey");
        };

        var stopped = false;
        foreach (var factory in stageFactories)
        {
            var stage = factory();
            if (stage is null || stage.Jobs.Count == 0)
            {
                continue;
            }

            latest = store.LatestByJob();
            var done = stage.Jobs.Count(j => RunStore.IsFinished(latest.GetValueOrDefault(j.Canonical), rerunInfra));
            var infra = stage.Jobs.Count(j => latest.GetValueOrDefault(j.Canonical)?.Status == JobStatus.Infra && !rerunInfra);
            tracker.SetStage(stage.Stage, stage.Jobs.Count, done, infra);
            ToolLog.Info($"run {store.RunId}: bắt đầu {stage.Stage}, {done}/{stage.Jobs.Count} job đã xong từ trước, {stage.Workers} worker");
            if (done > 0)
            {
                store.AppendEvent(new RunEvent { At = DateTimeOffset.UtcNow, Stage = stage.Stage, Message = $"Chạy tiếp: {done}/{stage.Jobs.Count} job đã xong ở phiên trước" });
            }

            var worker = WorkAsync(stage, gate, tracker, latest, cancellationToken);
            await ShowAsync(worker, gate, tracker, interactive, cancellationToken).ConfigureAwait(false);
            stopped = await worker.ConfigureAwait(false);
            if (stopped)
            {
                break;
            }
        }

        var final = tracker.Snapshot();
        tracker.SetState(stopped ? RunState.Stopped : RunState.Done);
        var finished = store.LatestByJob().Values.Count(e => RunStore.IsFinished(e, rerunInfra: false));
        ToolLog.Info($"run {store.RunId}: kết thúc phiên ({(stopped ? "dừng hẳn" : "xong")}), {finished} job đã có kết quả, tạm dừng {final.PausedSeconds:0.0}s");
        return stopped;
    }

    private async Task ShowAsync(Task worker, ControlGate gate, StatusTracker tracker, bool interactive, CancellationToken cancellationToken)
    {
        if (interactive)
        {
            await AnsiConsole.Live(DashboardView.Render(header, tracker.Snapshot(), store.ReadRecentEvents(5), interactive: true))
                .AutoClear(false)
                .StartAsync(async context =>
                {
                    while (!worker.IsCompleted)
                    {
                        HandleKeys();
                        gate.Poll();
                        if (gate.PauseRequested)
                        {
                            tracker.OnIdleWhilePaused();
                        }

                        context.UpdateTarget(DashboardView.Render(header, tracker.Snapshot(), store.ReadRecentEvents(5), interactive: true));
                        await Task.WhenAny(worker, Task.Delay(300, cancellationToken)).ConfigureAwait(false);
                    }

                    context.UpdateTarget(DashboardView.Render(header, tracker.Snapshot(), store.ReadRecentEvents(5), interactive: true));
                });
            return;
        }

        var printed = AtomicFile.ReadLines(store.EventsPath).Count(); // chỉ in sự kiện của phiên này
        while (!worker.IsCompleted)
        {
            gate.Poll();
            if (gate.PauseRequested)
            {
                tracker.OnIdleWhilePaused();
            }

            printed = PrintNewEvents(printed);
            await Task.WhenAny(worker, Task.Delay(500, cancellationToken)).ConfigureAwait(false);
        }

        PrintNewEvents(printed);
    }

    /// <summary>Trả về true nếu dừng vì lệnh dừng hẳn.</summary>
    private async Task<bool> WorkAsync(StageSpec stage, ControlGate gate, StatusTracker tracker, IReadOnlyDictionary<string, LedgerEntry> latest, CancellationToken cancellationToken)
    {
        await Task.Yield();
        var pending = new Queue<JobKey>(stage.Jobs.Where(j => !RunStore.IsFinished(latest.GetValueOrDefault(j.Canonical), rerunInfra)));
        var queueLock = new object();
        var stopped = false;

        async Task Worker(string name)
        {
            while (true)
            {
                if (!await gate.WaitIfPausedAsync(cancellationToken).ConfigureAwait(false))
                {
                    stopped = true;
                    return;
                }

                JobKey job;
                lock (queueLock)
                {
                    if (pending.Count == 0)
                    {
                        return;
                    }

                    job = pending.Dequeue();
                }

                await RunJobAsync(stage, name, job, tracker, cancellationToken).ConfigureAwait(false);
                if (gate.PauseRequested)
                {
                    tracker.OnIdleWhilePaused();
                }
            }
        }

        var workers = Enumerable.Range(1, Math.Max(1, stage.Workers)).Select(i => Worker($"{stage.WorkerPrefix}-{i}"));
        await Task.WhenAll(workers).ConfigureAwait(false);
        return stopped || gate.StopRequested && pending.Count > 0;
    }

    private async Task RunJobAsync(StageSpec stage, string worker, JobKey job, StatusTracker tracker, CancellationToken cancellationToken)
    {
        var started = DateTimeOffset.UtcNow;
        tracker.JobStarted(worker, job.TaskId, stage.Label?.Invoke(job) ?? job.Strategy ?? stage.Stage, "bắt đầu");
        JobResult result;
        try
        {
            result = await stage.Execute(job, new JobContext(worker, step => tracker.JobStep(worker, step)), cancellationToken).ConfigureAwait(false);
        }
        catch (OperationCanceledException) when (cancellationToken.IsCancellationRequested)
        {
            throw;
        }
        catch (Exception ex)
        {
            // Lỗi không lường trước của tool: ghi INFRA để chạy lại được, không làm hỏng cả run.
            ToolLog.Error($"run {store.RunId}: job {job.Canonical} lỗi", ex);
            result = new JobResult { Status = JobStatus.Infra, Outcome = "INFRA", Reason = "TOOL_ERROR", Message = ex.Message };
        }

        var infra = result.Status == JobStatus.Infra;
        store.AppendLedger(new LedgerEntry
        {
            Job = job,
            Status = result.Status,
            Outcome = result.Outcome,
            Reason = result.Reason,
            StartedAt = started,
            EndedAt = DateTimeOffset.UtcNow,
            LlmSeconds = result.LlmSeconds,
            BuildSeconds = result.BuildSeconds,
            Outputs = result.Outputs,
        });
        store.AppendEvent(new RunEvent
        {
            At = DateTimeOffset.UtcNow,
            Level = infra ? "warn" : "info",
            Stage = stage.Stage,
            TaskId = job.TaskId,
            Strategy = job.Strategy,
            Outcome = result.Reason is null ? result.Outcome : $"{result.Outcome} {result.Reason}",
            Message = ToolLog.Redact(result.Message),
            LlmSeconds = result.LlmSeconds,
            BuildSeconds = result.BuildSeconds,
        });
        if (result.PauseRun)
        {
            // Sau khi đã ghi ledger: lỗi khi ghi control.json không làm mất kết quả của job.
            ToolLog.Warn($"run {store.RunId}: {result.Reason} ở job {job.Canonical}, tạm dừng run");
            try
            {
                ControlGate.Request(store, ControlCommand.Pause, "tool");
            }
            catch (IOException ex)
            {
                ToolLog.Error($"run {store.RunId}: không ghi được control.json", ex);
            }
        }

        ToolLog.Info($"run {store.RunId}: {stage.Stage} {job.TaskId} → {result.Outcome}{(result.Reason is null ? "" : " " + result.Reason)} ({(DateTimeOffset.UtcNow - started).TotalSeconds:0.0}s) {ToolLog.Redact(result.Message)}");
        tracker.JobFinished(worker, stage.Stage, result.Outcome, infra, result.PromptTokens, result.CompletionTokens);
    }

    private void HandleKeys()
    {
        while (Console.KeyAvailable)
        {
            switch (char.ToLowerInvariant(Console.ReadKey(intercept: true).KeyChar))
            {
                case 'p':
                    ControlGate.Request(store, ControlCommand.Pause, "hotkey");
                    break;
                case 'r':
                    ControlGate.Request(store, ControlCommand.Resume, "hotkey");
                    break;
                case 's':
                    ControlGate.Request(store, ControlCommand.Stop, "hotkey");
                    break;
            }

            Thread.Sleep(5); // requested_at khác nhau giữa hai lần bấm liên tiếp
        }
    }

    private int PrintNewEvents(int alreadyPrinted)
    {
        var lines = AtomicFile.ReadLines(store.EventsPath).ToList();
        foreach (var line in lines.Skip(alreadyPrinted))
        {
            var e = Json.Deserialize<RunEvent>(line);
            var timing = string.Join(' ', new[] { e.LlmSeconds is null ? null : $"llm={e.LlmSeconds}s", e.BuildSeconds is null ? null : $"build={e.BuildSeconds}s" }.Where(s => s is not null));
            Console.WriteLine($"{e.At:yyyy-MM-ddTHH:mm:ssZ} {e.Stage} {e.TaskId} {e.Strategy} {e.Outcome} {timing} {e.Message}".Replace("  ", " "));
        }

        return lines.Count;
    }

    /// <summary>Dòng hướng dẫn chạy tiếp sau khi dừng.</summary>
    public static void PrintStopped(string runId, string resumeCommand, int finished, int total)
    {
        AnsiConsole.MarkupLine($"\nĐã dừng run [bold]{Markup.Escape(runId)}[/] ({finished}/{total} job đã có kết quả).");
        AnsiConsole.MarkupLine($"Chạy tiếp: [bold]{Markup.Escape(resumeCommand)}[/]");
    }
}
