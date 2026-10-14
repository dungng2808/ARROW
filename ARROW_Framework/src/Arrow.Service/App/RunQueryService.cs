using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Run;
using Arrow.Service.Block4;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

/// <summary>Đọc trạng thái đã lưu của các run (S1, S6, S7, S9) và gửi lệnh điều khiển (<c>arrow control</c>).</summary>
public sealed class RunQueryService(IRunRepository runs, ITaskResultRepository results, RunResultsLoader loader) : IRunQueryService
{
    /// <summary>Run coi là còn sống khi heartbeat gần hơn khoảng này (heartbeat ghi mỗi 2 giây).</summary>
    public static readonly TimeSpan AliveWindow = TimeSpan.FromSeconds(10);

    public IReadOnlyList<RunInfo> ListRuns() => runs.ListRuns();

    public RunStatusView GetStatus(string runId)
    {
        Require(runId);
        var status = runs.ReadStatus(runId);
        var age = status is null ? (double?)null : Math.Max(0, (DateTimeOffset.Now - status.UpdatedAt).TotalSeconds);
        return new RunStatusView
        {
            Status = status,
            Lock = runs.ReadLock(runId),
            HeartbeatAgeSeconds = age,
            Alive = status?.State is RunState.Running or RunState.Paused or RunState.Stopping && age < AliveWindow.TotalSeconds,
        };
    }

    public TaskDetail GetTaskDetail(string runId, string taskId)
    {
        Require(runId);
        var key = results.ListTasks(runId).FirstOrDefault(k => k.TaskId == taskId)
                  ?? throw new ArgumentException($"Run {runId} không có task {taskId}");
        var experiment = runs.ReadText(runId, RunResultsLoader.ExperimentCopy) is null ? new ExperimentConfig() : loader.LoadExperiment(runId);
        return new TaskDetail(
            loader.LoadTask(runId, key, experiment),
            results.LoadJson<RepositoryMetadata>(runId, key, ResultPaths.Metadata),
            results.LoadJson<Baseline>(runId, key, ResultPaths.Baseline),
            results.TaskDir(runId, key));
    }

    public IReadOnlyList<RerunItem> GetRerunList(string runId)
    {
        Require(runId);
        var attempts = runs.ReadText(runId, RunResultsLoader.ExperimentCopy) is null ? 3 : loader.LoadExperiment(runId).Verification.InfraAttempts;
        return runs.ReadLedger(runId)
            .GroupBy(e => (e.Dataset, e.TaskId, e.Job))
            .Select(g => (Latest: g.OrderBy(e => e.At).Last(), Timeouts: g.Count(e => e.Code?.Contains("TIMEOUT", StringComparison.Ordinal) == true)))
            .Where(x => x.Latest.NeedsRerun)
            .OrderBy(x => x.Latest.Dataset, StringComparer.Ordinal).ThenBy(x => x.Latest.TaskId, StringComparer.Ordinal).ThenBy(x => x.Latest.Job, StringComparer.Ordinal)
            .Select(x => new RerunItem
            {
                Dataset = x.Latest.Dataset,
                TaskId = x.Latest.TaskId,
                Block = x.Latest.Block,
                Job = x.Latest.Job,
                Code = x.Latest.Code,
                Message = x.Latest.Message,
                Attempts = attempts,
                TimeoutRepeated = x.Timeouts >= 2,
                Command = $"arrow run --run-id {runId} --tasks {x.Latest.TaskId} --rerun-infra",
            })
            .ToList();
    }

    public void SendControl(string runId, ControlAction action)
    {
        Require(runId);
        runs.WriteControl(runId, new ControlCommand { Action = action, At = DateTimeOffset.Now, Source = "arrow control" });
    }

    private void Require(string runId)
    {
        if (!runs.Exists(runId))
        {
            throw new ArrowException(ErrorCodes.RunMissing, $"Không có run {runId}");
        }
    }
}
