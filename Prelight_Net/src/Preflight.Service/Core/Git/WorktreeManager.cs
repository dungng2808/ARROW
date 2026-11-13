using Preflight.Service.Core.Common;

namespace Preflight.Service.Core.Git;

/// <summary>Port runner._worktree/_remove_worktree dưới khóa mirror.</summary>
public sealed class WorktreeManager(string runRoot, GitCli git, MirrorManager mirrors)
{
    public string PathFor(string task)
    {
        string root = Path.Combine(runRoot, "workspaces"), path = Path.GetFullPath(Path.Combine(root, task));
        if (!ResolvePath.IsWithin(path, root) || path.Equals(root, StringComparison.OrdinalIgnoreCase)) throw new InvalidDataException("UNSAFE_WORKSPACE");
        return path;
    }
    public async Task<string> CreateAsync(string repo, string mirror, string task, string sha, CancellationToken ct)
    {
        var gate = mirrors.Lock(repo); await gate.WaitAsync(ct);
        try
        {
            string path = PathFor(task); Directory.CreateDirectory(Path.GetDirectoryName(path)!);
            if (Directory.Exists(path)) await RemoveCore(mirror, path);
            await git.RunAsync(mirror, ["worktree", "add", "--detach", "--force", path, sha], true, ct); return path;
        }
        finally { gate.Release(); }
    }
    public async Task RemoveAsync(string repo, string mirror, string task)
    {
        var gate = mirrors.Lock(repo); await gate.WaitAsync();
        try { await RemoveCore(mirror, PathFor(task)); } finally { gate.Release(); }
    }
    private async Task RemoveCore(string mirror, string path)
    {
        await git.RunAsync(mirror, ["worktree", "remove", "--force", path], false);
        if (Directory.Exists(path)) SafeFiles.DeleteTree(path, Path.Combine(runRoot, "workspaces"));
    }
}
