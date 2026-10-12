using System.Collections.Concurrent;
using Arrow.Core.Config;
using Arrow.Core.Prepare;
using Arrow.Core.Util;
using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Git;

/// <summary>
/// Mirror cho mỗi repository và workspace riêng cho mỗi job (specs/02 §1–§2).
/// Mọi thao tác trên cùng một mirror đi qua một khóa, vì git không cho hai thao tác cùng lúc.
/// </summary>
public sealed class GitRepositoryProvider(string gitPath, string workDir, RepoPolicy policy)
{
    private static readonly TimeSpan GitTimeout = TimeSpan.FromMinutes(20);
    private static readonly TimeSpan[] RetryDelays = [TimeSpan.FromSeconds(10), TimeSpan.FromSeconds(30), TimeSpan.FromSeconds(90)];
    private static readonly string[] NetworkMarkers =
    [
        "Could not resolve host", "Connection timed out", "Connection reset", "Operation timed out", "early EOF",
        "The requested URL returned error: 5", "RPC failed", "unable to access", "Failed to connect", "TLS",
    ];

    private readonly ConcurrentDictionary<string, SemaphoreSlim> _locks = new();

    /// <summary>Cho kiểm thử: thời gian chờ giữa các lần thử lại.</summary>
    public TimeSpan[] Delays { get; init; } = RetryDelays;

    public string MirrorPath(string repoUrl) => Path.Combine(workDir, "mirrors", MirrorName(repoUrl));

    public static string MirrorName(string repoUrl)
    {
        var trimmed = repoUrl.TrimEnd('/');
        if (trimmed.EndsWith(".git", StringComparison.OrdinalIgnoreCase))
        {
            trimmed = trimmed[..^4];
        }

        var parts = trimmed.Replace('\\', '/').Split('/', StringSplitOptions.RemoveEmptyEntries);
        var name = parts.Length >= 2 ? $"{parts[^2]}__{parts[^1]}" : parts[^1];
        return string.Concat(name.Select(c => char.IsLetterOrDigit(c) || c is '-' or '_' or '.' ? c : '_')) + ".git";
    }

    /// <summary>Bảo đảm mirror có commit; lỗi mạng thử lại 3 lần, thiếu commit thì <c>COMMIT_MISSING</c>.</summary>
    public async Task EnsureCommitAsync(string repoUrl, string sha, CancellationToken cancellationToken)
    {
        var mirror = MirrorPath(repoUrl);
        await WithLockAsync(mirror, async () =>
        {
            if (await HasCommitAsync(mirror, sha, cancellationToken).ConfigureAwait(false))
            {
                return;
            }

            if (!Directory.Exists(mirror) || !File.Exists(Path.Combine(mirror, "HEAD")))
            {
                await CreateMirrorAsync(repoUrl, mirror, cancellationToken).ConfigureAwait(false);
                if (await HasCommitAsync(mirror, sha, cancellationToken).ConfigureAwait(false))
                {
                    return;
                }
            }

            var fetchArgs = policy == RepoPolicy.FullHistoryKeep
                ? new[] { "fetch", "--no-tags", "origin", sha }
                : ["fetch", "--depth", "1", "--no-tags", "origin", sha];
            var result = await GitWithRetryAsync(mirror, fetchArgs, cancellationToken).ConfigureAwait(false);
            if (!await HasCommitAsync(mirror, sha, cancellationToken).ConfigureAwait(false))
            {
                var output = result.Output;
                throw IsNetworkError(output)
                    ? new PrepareException("GIT_FETCH_FAILED", $"Không tải được {repoUrl}@{sha[..Math.Min(12, sha.Length)]}: {LastLine(output)}")
                    : new PrepareException("COMMIT_MISSING", $"Repository {repoUrl} không có commit {sha}: {LastLine(output)}");
            }
        }, cancellationToken).ConfigureAwait(false);
    }

    private async Task CreateMirrorAsync(string repoUrl, string mirror, CancellationToken cancellationToken)
    {
        if (Directory.Exists(mirror))
        {
            ForceDelete(mirror); // mirror hỏng (thiếu HEAD) từ lần trước
        }

        Directory.CreateDirectory(Path.GetDirectoryName(mirror)!);
        if (policy == RepoPolicy.FullHistoryKeep)
        {
            var clone = await GitWithRetryAsync(null, ["clone", "--mirror", repoUrl, mirror], cancellationToken, cleanup: () => ForceDelete(mirror)).ConfigureAwait(false);
            if (!clone.Succeeded)
            {
                throw new PrepareException("GIT_FETCH_FAILED", $"Không clone được {repoUrl}: {LastLine(clone.Output)}");
            }
        }
        else
        {
            await RunGitAsync(null, ["init", "--bare", mirror], cancellationToken, ensureSuccess: true).ConfigureAwait(false);
            await RunGitAsync(mirror, ["remote", "add", "origin", repoUrl], cancellationToken, ensureSuccess: true).ConfigureAwait(false);
        }

        if (OperatingSystem.IsWindows())
        {
            await RunGitAsync(mirror, ["config", "core.longpaths", "true"], cancellationToken, ensureSuccess: true).ConfigureAwait(false);
        }

        ToolLog.Info($"git: tạo mirror {Path.GetFileName(mirror)} ({policy})");
    }

    /// <summary>Tạo workspace <c>git worktree add --detach</c>; xóa workspace sót lại từ lần chạy trước.</summary>
    public async Task<string> AddWorktreeAsync(string repoUrl, string sha, string workspace, CancellationToken cancellationToken)
    {
        var mirror = MirrorPath(repoUrl);
        await WithLockAsync(mirror, async () =>
        {
            if (Directory.Exists(workspace))
            {
                await RemoveWorktreeUnlockedAsync(mirror, workspace, cancellationToken).ConfigureAwait(false);
            }

            await RunGitAsync(mirror, ["worktree", "prune"], cancellationToken).ConfigureAwait(false);
            Directory.CreateDirectory(Path.GetDirectoryName(workspace)!);
            var result = await RunGitAsync(mirror, ["worktree", "add", "--detach", workspace, sha], cancellationToken).ConfigureAwait(false);
            if (!result.Succeeded)
            {
                throw new PrepareException("WORKTREE_FAILED", $"Không tạo được workspace: {LastLine(result.Output)}");
            }

            if (OperatingSystem.IsWindows())
            {
                await RunGitAsync(workspace, ["config", "core.longpaths", "true"], cancellationToken).ConfigureAwait(false);
            }
        }, cancellationToken).ConfigureAwait(false);

        if (!OperatingSystem.IsWindows())
        {
            MakeWrappersExecutable(workspace);
        }

        return workspace;
    }

    public async Task RemoveWorktreeAsync(string repoUrl, string workspace, CancellationToken cancellationToken)
    {
        var mirror = MirrorPath(repoUrl);
        await WithLockAsync(mirror, () => RemoveWorktreeUnlockedAsync(mirror, workspace, cancellationToken), CancellationToken.None).ConfigureAwait(false);
    }

    /// <summary>Lấy đủ lịch sử và tag khi build cần (<c>git describe</c>, plugin git-commit-id).</summary>
    public async Task UnshallowAsync(string repoUrl, CancellationToken cancellationToken)
    {
        var mirror = MirrorPath(repoUrl);
        await WithLockAsync(mirror, async () =>
        {
            var shallow = File.Exists(Path.Combine(mirror, "shallow"));
            var args = shallow ? new[] { "fetch", "--unshallow", "--tags", "origin" } : ["fetch", "--tags", "origin"];
            var result = await GitWithRetryAsync(mirror, args, cancellationToken).ConfigureAwait(false);
            if (!result.Succeeded)
            {
                throw new PrepareException("GIT_FETCH_FAILED", $"Không lấy được lịch sử đầy đủ: {LastLine(result.Output)}");
            }

            ToolLog.Info($"git: đã lấy lịch sử đầy đủ cho {Path.GetFileName(mirror)}");
        }, cancellationToken).ConfigureAwait(false);
    }

    /// <summary>Xóa mirror (repo_policy <c>commit_only_delete_after</c>, khi mọi job của repository đã xong).</summary>
    public async Task DeleteMirrorAsync(string repoUrl)
    {
        var mirror = MirrorPath(repoUrl);
        await WithLockAsync(mirror, () =>
        {
            ForceDelete(mirror);
            return Task.CompletedTask;
        }, CancellationToken.None).ConfigureAwait(false);
    }

    private async Task RemoveWorktreeUnlockedAsync(string mirror, string workspace, CancellationToken cancellationToken)
    {
        if (Directory.Exists(mirror))
        {
            for (var attempt = 1; ; attempt++)
            {
                var result = await RunGitAsync(mirror, ["worktree", "remove", "--force", "--force", workspace], cancellationToken).ConfigureAwait(false);
                if (result.Succeeded || !Directory.Exists(workspace) || !OperatingSystem.IsWindows() || attempt >= 5)
                {
                    break;
                }

                await Task.Delay(TimeSpan.FromSeconds(2), cancellationToken).ConfigureAwait(false); // file còn bị khóa
            }
        }

        if (Directory.Exists(workspace))
        {
            ForceDelete(workspace);
        }

        if (Directory.Exists(mirror))
        {
            await RunGitAsync(mirror, ["worktree", "prune"], cancellationToken).ConfigureAwait(false);
        }
    }

    private async Task<bool> HasCommitAsync(string mirror, string sha, CancellationToken cancellationToken) =>
        Directory.Exists(mirror) && File.Exists(Path.Combine(mirror, "HEAD"))
        && (await RunGitAsync(mirror, ["cat-file", "-e", sha + "^{commit}"], cancellationToken).ConfigureAwait(false)).Succeeded;

    private async Task<ProcessResult> GitWithRetryAsync(string? gitDir, string[] args, CancellationToken cancellationToken, Action? cleanup = null)
    {
        for (var attempt = 0; ; attempt++)
        {
            var result = await RunGitAsync(gitDir, args, cancellationToken).ConfigureAwait(false);
            if (result.Succeeded || attempt >= Delays.Length || !(result.TimedOut || IsNetworkError(result.Output)))
            {
                return result;
            }

            ToolLog.Warn($"git {args[0]} lỗi mạng (lần {attempt + 1}), thử lại sau {Delays[attempt].TotalSeconds:0}s: {LastLine(result.Output)}");
            cleanup?.Invoke();
            await Task.Delay(Delays[attempt], cancellationToken).ConfigureAwait(false);
        }
    }

    private async Task<ProcessResult> RunGitAsync(string? gitDir, IEnumerable<string> args, CancellationToken cancellationToken, bool ensureSuccess = false)
    {
        var all = new List<string>();
        if (gitDir is not null)
        {
            all.AddRange(Directory.Exists(Path.Combine(gitDir, ".git")) || File.Exists(Path.Combine(gitDir, ".git"))
                ? ["-C", gitDir]
                : ["--git-dir", gitDir]);
        }

        all.AddRange(args);
        var environment = new Dictionary<string, string?>
        {
            ["GIT_TERMINAL_PROMPT"] = "0", // repository riêng tư/không còn: báo lỗi ngay, không hỏi mật khẩu
            ["GIT_ASKPASS"] = "",
            ["LC_ALL"] = "C",
        };
        var result = await ProcessRunner.RunAsync(gitPath, all, workDir, environment, GitTimeout, cancellationToken).ConfigureAwait(false);
        if (ensureSuccess && !result.Succeeded)
        {
            throw new PrepareException("GIT_ERROR", $"git {string.Join(' ', args)}: {LastLine(result.Output)}");
        }

        return result;
    }

    private async Task WithLockAsync(string mirror, Func<Task> action, CancellationToken cancellationToken)
    {
        var gate = _locks.GetOrAdd(Path.GetFullPath(mirror), _ => new SemaphoreSlim(1, 1));
        await gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            await action().ConfigureAwait(false);
        }
        finally
        {
            gate.Release();
        }
    }

    private static bool IsNetworkError(string output) => NetworkMarkers.Any(m => output.Contains(m, StringComparison.OrdinalIgnoreCase));

    private static string LastLine(string output) =>
        output.Split('\n').Select(l => l.Trim()).LastOrDefault(l => l.Length > 0) ?? "";

    [System.Runtime.Versioning.UnsupportedOSPlatform("windows")]
    private static void MakeWrappersExecutable(string workspace)
    {
        foreach (var name in new[] { "mvnw", "gradlew" })
        {
            foreach (var file in SafeEnumerate(workspace, name))
            {
                try
                {
                    File.SetUnixFileMode(file, File.GetUnixFileMode(file) | UnixFileMode.UserExecute | UnixFileMode.GroupExecute | UnixFileMode.OtherExecute);
                }
                catch (IOException)
                {
                }
            }
        }
    }

    private static IEnumerable<string> SafeEnumerate(string root, string name)
    {
        try
        {
            return Directory.EnumerateFiles(root, name, new EnumerationOptions { RecurseSubdirectories = true, MaxRecursionDepth = 6, IgnoreInaccessible = true }).ToList();
        }
        catch (IOException)
        {
            return [];
        }
    }

    /// <summary>Xóa thư mục kể cả file chỉ đọc (pack của git trên Windows).</summary>
    public static void ForceDelete(string directory)
    {
        if (!Directory.Exists(directory))
        {
            return;
        }

        foreach (var file in Directory.EnumerateFiles(directory, "*", new EnumerationOptions { RecurseSubdirectories = true, IgnoreInaccessible = true, AttributesToSkip = 0 }))
        {
            try
            {
                File.SetAttributes(file, FileAttributes.Normal);
            }
            catch (IOException)
            {
            }
        }

        for (var attempt = 1; ; attempt++)
        {
            try
            {
                Directory.Delete(directory, recursive: true);
                return;
            }
            catch (Exception ex) when ((ex is IOException or UnauthorizedAccessException) && attempt < 5)
            {
                Thread.Sleep(TimeSpan.FromSeconds(2));
            }
        }
    }
}
