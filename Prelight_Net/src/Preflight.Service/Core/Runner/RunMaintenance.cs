using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Git;
using Preflight.Service.Core.Common;

namespace Preflight.Service.Core.Runner;

public static class RunMaintenance
{
    public static async Task SweepAsync(string run, RunContract contract, CheckpointStore checkpoint, CancellationToken ct)
    {
        var events = new List<object>(); var pending = checkpoint.Pending();
        var git = new GitCli(run, timeoutSeconds: contract.Config.Run.GitTimeoutSeconds); var mirrors = new MirrorManager(run, git);
        var keep = contract.Tasks.Where(t => pending.Contains(t.Candidate.TaskId)).Select(t => mirrors.MirrorPath(t.Repo.RepoUrl)).ToHashSet(StringComparer.OrdinalIgnoreCase);
        var snapshots = contract.Tasks.Where(t => pending.Contains(t.Candidate.TaskId)).Select(t => t.Repo.SnapshotPath).ToHashSet(StringComparer.OrdinalIgnoreCase);
        void Sweep(string root, Func<string, bool> keepPath)
        {
            if (!Directory.Exists(root)) return;
            foreach (string path in Directory.EnumerateDirectories(root))
            {
                ct.ThrowIfCancellationRequested(); if (keepPath(path)) continue;
                try { SafeFiles.DeleteTree(path, root); events.Add(new { Path = path, Status = "DELETED", AtUtc = CheckpointStore.Now() }); }
                catch (Exception error) when (error is IOException or UnauthorizedAccessException or InvalidDataException) { events.Add(new { Path = path, Status = "FAILED", Error = error.Message, AtUtc = CheckpointStore.Now() }); }
            }
        }
        if (!contract.Config.Run.KeepWorkspaces) Sweep(Path.Combine(run, "workspaces"), _ => false);
        if (!contract.Config.Run.KeepRepoCache && !contract.Config.Run.KeepWorkspaces)
        {
            Sweep(Path.Combine(run, "cache/mirrors"), keep.Contains); Sweep(Path.Combine(run, "cache/catlm_snapshots"), snapshots.Contains);
        }
        foreach (string path in keep.Where(Directory.Exists)) await git.RunAsync(path, ["worktree", "prune"], false, ct);
        string report = Path.Combine(run, "reports/orphan_sweep.jsonl");
        var previous = File.Exists(report) ? AuxiliaryCommands.ReadRows(report).Cast<object>() : [];
        ManifestWriter.WriteJsonl(report, previous.Concat(events));
    }
    public static void TrimBuildCache(PreflightConfig config)
    {
        string maven = Full(config.Tools.MavenRepoLocal), gradle = Full(config.Tools.GradleUserHome);
        long Size(string path) => Directory.Exists(path) ? new DirectoryInfo(path).EnumerateFiles("*", new EnumerationOptions { RecurseSubdirectories = true, AttributesToSkip = FileAttributes.ReparsePoint }).Sum(f => f.Length) : 0;
        if ((Size(maven) + Size(gradle)) / 1073741824.0 <= config.Disk.BuildCacheMaxGb) return;
        foreach (string dir in new[] { Path.Combine(gradle, "daemon"), Path.Combine(gradle, ".tmp") }.Concat(Directory.Exists(Path.Combine(gradle, "caches")) ? Directory.EnumerateDirectories(Path.Combine(gradle, "caches"), "build-cache-*") : [])) if (Directory.Exists(dir)) SafeFiles.DeleteTree(dir, gradle);
        if (Directory.Exists(maven))
            foreach (string dir in Directory.EnumerateDirectories(maven, "*-SNAPSHOT", new EnumerationOptions { RecurseSubdirectories = true, AttributesToSkip = FileAttributes.ReparsePoint }).ToArray())
                if (Directory.Exists(dir) && Directory.GetLastWriteTimeUtc(dir) < DateTime.UtcNow.AddDays(-7)) SafeFiles.DeleteTree(dir, maven);
    }
    private static string Full(string path)
    {
        if (string.IsNullOrWhiteSpace(path)) throw new InvalidDataException("Build cache path must be configured");
        string full = Path.GetFullPath(Path.IsPathRooted(path) ? path : Path.Combine(RuntimePaths.Root, path));
        if (!ResolvePath.IsWithin(ResolvePath.ResolveLinks(full), RuntimePaths.Root) || full.Equals(RuntimePaths.Root, StringComparison.OrdinalIgnoreCase)) throw new InvalidDataException("Build cache must be a child of Prelight_Net");
        return full;
    }
}
