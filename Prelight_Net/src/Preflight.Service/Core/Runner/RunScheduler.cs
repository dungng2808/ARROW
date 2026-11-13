using System.Collections.Concurrent;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Git;

namespace Preflight.Service.Core.Runner;

/// <summary>Scheduler có checkpoint theo task và cleanup sau CUT cuối của repo.</summary>
public sealed class RunScheduler(string runRoot, PreflightConfig config, CheckpointStore checkpoint, PreflightPipeline pipeline)
{
    public async Task<string> RunAsync(IReadOnlyList<CandidateTask> tasks, string inputRoot, CancellationToken cancellationToken)
    {
        checkpoint.Recover(); bool resumingDiskFull = checkpoint.Sessions().LastOrDefault()?.GetValueOrDefault("status") as string == "INFRA_DISK_FULL";
        long session = checkpoint.BeginSession(config.Run.Workers);
        using var cancellation = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
        bool infrastructure = false; string state = "RUNNING";
        using var monitorStop = new CancellationTokenSource();
        async Task Monitor()
        {
            using var timer = new PeriodicTimer(TimeSpan.FromSeconds(config.Disk.CheckIntervalSeconds));
            try { while (await timer.WaitForNextTickAsync(monitorStop.Token)) if (FreeGb(runRoot) < config.Disk.MinFreeGb) { infrastructure = true; cancellation.Cancel(); return; } }
            catch (OperationCanceledException) { }
        }
        Task monitor = Monitor();
        void Progress()
        {
            lock (checkpoint)
            {
                var counts = checkpoint.Counts(); var byStatus = checkpoint.StatusCounts();
                ManifestWriter.WriteJson(Path.Combine(runRoot, "reports/progress.json"), new { CheckpointSchemaVersion = 3, RunStatus = state, Total = counts["total"], Completed = counts["completed"], Running = counts["running"], Pending = counts["pending"], ByStatus = byStatus, SessionId = session, LastCheckpointAtUtc = CheckpointStore.Now(), DiskFreeGb = FreeGb(runRoot) });
            }
        }
        try
        {
            if (config.Disk.SweepOrphansOnStart) await RunMaintenance.SweepAsync(runRoot, checkpoint.Contract(), checkpoint, cancellation.Token);
            RunMaintenance.TrimBuildCache(config);
            foreach (var group in tasks.GroupBy(t => t.Repo.RepoUrl)) if (checkpoint.CompletedRepos().Contains(group.Key)) Cleanup(group.ToArray());
            var pending = checkpoint.Pending();
            foreach (var task in tasks.Where(t => pending.Contains(t.Candidate.TaskId)))
                foreach (var pair in task.Candidate.SourceJsonPaths.Zip(task.Candidate.SourceJsonSha256s))
                {
                    cancellation.Token.ThrowIfCancellationRequested(); string evidence = RuntimePaths.Evidence(task, pair.First, runRoot, inputRoot);
                    if (!File.Exists(evidence) || RuntimePaths.Hash(evidence) != pair.Second) throw new InvalidDataException("Dataset evidence missing/changed: " + evidence);
                }
            if (FreeGb(runRoot) < (resumingDiskFull ? config.Disk.ResumeFreeGb : config.Disk.MinFreeGb)) { infrastructure = true; throw new OperationCanceledException(); }
            Progress();
            var groups = tasks.Where(t => pending.Contains(t.Candidate.TaskId)).GroupBy(t => t.Repo.RepoUrl).Select(g => g.ToArray()).ToArray();
            // Mỗi worker xử lý một repo: giới hạn mirror, không deadlock khi repo
            // nhiều bug có project_id khác nhau; các repo vẫn chạy song song.
            await Parallel.ForEachAsync(groups, new ParallelOptions { MaxDegreeOfParallelism = config.Run.Workers, CancellationToken = cancellation.Token }, async (group, ct) =>
            {
                foreach (var task in group)
                {
                    ct.ThrowIfCancellationRequested();
                    if (FreeGb(runRoot) < config.Disk.MinFreeGb) { infrastructure = true; cancellation.Cancel(); throw new OperationCanceledException(ct); }
                    int attempt = checkpoint.Start(task.Candidate.TaskId, session); Progress();
                    try
                    {
                        var result = await pipeline.RunAsync(task, attempt, ct); checkpoint.Complete(task.Candidate.TaskId, attempt, result);
                    }
                    catch (InfrastructureFailure) { infrastructure = true; checkpoint.Interrupt(task.Candidate.TaskId, attempt, "INFRA_DISK_FULL"); cancellation.Cancel(); throw new OperationCanceledException(ct); }
                    catch (OperationCanceledException) { checkpoint.Interrupt(task.Candidate.TaskId, attempt); throw; }
                    Progress();
                }
                var allRepoTasks = tasks.Where(t => t.Repo.RepoUrl == group[0].Repo.RepoUrl).ToArray();
                if (checkpoint.CompletedRepos().Contains(group[0].Repo.RepoUrl)) Cleanup(allRepoTasks);
            });
            if (checkpoint.Counts()["completed"] != tasks.Count) throw new InvalidDataException("Incomplete run without cancellation");
            state = "COMPLETED";
        }
        catch (OperationCanceledException) { checkpoint.Recover(); state = infrastructure ? "INFRA_DISK_FULL" : "INTERRUPTED"; }
        catch { checkpoint.Recover(); state = "FAILED"; throw; }
        finally
        {
            monitorStop.Cancel(); await monitor;
            checkpoint.FinishSession(session, state); Progress();
            ManifestWriter.WriteJsonl(Path.Combine(runRoot, "reports/repo_cleanup.jsonl"), checkpoint.CleanupEvents());
        }
        return state;
    }
    private void Cleanup(CandidateTask[] tasks)
    {
        string repo = tasks[0].Repo.RepoUrl, path = pipeline.Mirrors.MirrorPath(repo);
        var result = new Dictionary<string, object?> { ["repo_url"] = repo, ["mirror_path"] = path, ["class_count"] = tasks.Length };
        try
        {
            string[] leftovers = tasks.Where(t => Directory.Exists(Path.Combine(runRoot, "workspaces", t.Candidate.TaskId))).Select(t => t.Candidate.TaskId).ToArray();
            if (config.Run.KeepWorkspaces || config.Run.KeepRepoCache) { result["status"] = "KEPT"; result["reason"] = config.Run.KeepWorkspaces ? "keep_workspaces" : "keep_repo_cache"; }
            else if (leftovers.Length > 0) { result["status"] = "KEPT"; result["reason"] = "WORKSPACE_CLEANUP_INCOMPLETE"; result["remaining_tasks"] = leftovers; }
            else
            {
                bool existed = Directory.Exists(path); if (existed) SafeFiles.DeleteTree(path, Path.Combine(runRoot, "cache/mirrors"));
                foreach (string snapshot in tasks.Where(t => t.Repo.Kind == "snapshot").Select(t => t.Repo.SnapshotPath).Distinct(StringComparer.Ordinal)) if (Directory.Exists(snapshot)) SafeFiles.DeleteTree(snapshot, Path.Combine(runRoot, "cache/catlm_snapshots"));
                result["status"] = existed ? "DELETED" : "ABSENT";
            }
        }
        catch (Exception error) when (error is IOException or UnauthorizedAccessException or InvalidDataException) { result["status"] = "FAILED"; result["error"] = error.Message; }
        checkpoint.PutCleanup(result);
    }
    public static double FreeGb(string path) => new DriveInfo(Path.GetPathRoot(Path.GetFullPath(path))!).AvailableFreeSpace / 1073741824.0;
}
