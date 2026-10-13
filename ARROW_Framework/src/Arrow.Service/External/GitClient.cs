using System.Collections.Concurrent;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>
/// Lệnh git của khối 1 (spec 01 Mục 3). Không bao giờ hỏi mật khẩu (<c>GIT_TERMINAL_PROMPT=0</c>), không tải file LFS.
/// Các thao tác ghi vào cùng một mirror được tuần tự hóa.
/// </summary>
public sealed class GitClient(IProcessRunner runner, string gitExecutable) : IGitClient
{
    private static readonly ConcurrentDictionary<string, SemaphoreSlim> MirrorLocks = new(StringComparer.Ordinal);
    private static readonly TimeSpan FetchTimeout = TimeSpan.FromMinutes(20);
    private static readonly TimeSpan ShortTimeout = TimeSpan.FromMinutes(2);

    private static readonly string[] MissingMarkers =
    [
        "repository not found", "could not read username", "authentication failed", "does not appear to be a git repository",
        "not found", "terminal prompts disabled",
    ];

    private static readonly string[] RefusedMarkers =
    [
        "not our ref", "unadvertised object", "couldn't find remote ref", "no such remote ref", "did not send all necessary objects",
        "fatal: remote error",
    ];

    private static readonly string[] NetworkMarkers =
    [
        "could not resolve host", "connection timed out", "connection reset", "connection refused", "early eof", "rpc failed",
        "unable to access", "operation timed out", "the remote end hung up", "network is unreachable", "ssl_error", "gnutls",
        "http2 framing layer", "temporary failure",
    ];

    private async Task<ProcessResult> GitAsync(IReadOnlyList<string> arguments, string workingDirectory, TimeSpan timeout, CancellationToken cancellationToken)
    {
        var args = new List<string> { "-c", "credential.helper=", "-c", "core.autocrlf=false", "-c", "core.longpaths=true" };
        args.AddRange(arguments);
        return await runner.RunAsync(new ProcessSpec(
            gitExecutable,
            args,
            workingDirectory,
            new Dictionary<string, string?>
            {
                ["GIT_TERMINAL_PROMPT"] = "0",
                ["GIT_ASKPASS"] = OperatingSystem.IsWindows() ? null : "/bin/echo",
                ["GIT_LFS_SKIP_SMUDGE"] = "1",
                ["LANG"] = "C",
                ["LC_ALL"] = "C",
            },
            timeout), cancellationToken).ConfigureAwait(false);
    }

    private static async Task<T> WithMirrorLockAsync<T>(string mirrorPath, Func<Task<T>> action, CancellationToken cancellationToken)
    {
        var gate = MirrorLocks.GetOrAdd(Path.GetFullPath(mirrorPath), _ => new SemaphoreSlim(1, 1));
        await gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            return await action().ConfigureAwait(false);
        }
        finally
        {
            gate.Release();
        }
    }

    public Task EnsureMirrorAsync(string mirrorPath, string repoUrl, CancellationToken cancellationToken) =>
        WithMirrorLockAsync<bool>(mirrorPath, async () =>
        {
            if (!File.Exists(Path.Combine(mirrorPath, "HEAD")))
            {
                Directory.CreateDirectory(mirrorPath);
                var init = await GitAsync(["init", "--bare", "-q", mirrorPath], mirrorPath, ShortTimeout, cancellationToken).ConfigureAwait(false);
                if (!init.Succeeded)
                {
                    throw new IOException($"git init --bare lỗi: {init.Tail(5)}");
                }
            }

            var remote = await GitAsync(["-C", mirrorPath, "remote", "get-url", "origin"], mirrorPath, ShortTimeout, cancellationToken).ConfigureAwait(false);
            var set = remote.Succeeded
                ? remote.Output.Trim() == repoUrl ? remote : await GitAsync(["-C", mirrorPath, "remote", "set-url", "origin", repoUrl], mirrorPath, ShortTimeout, cancellationToken).ConfigureAwait(false)
                : await GitAsync(["-C", mirrorPath, "remote", "add", "origin", repoUrl], mirrorPath, ShortTimeout, cancellationToken).ConfigureAwait(false);
            if (!set.Succeeded)
            {
                throw new IOException($"git remote lỗi: {set.Tail(5)}");
            }

            return true;
        }, cancellationToken);

    public async Task<bool> HasCommitAsync(string mirrorPath, string sha, CancellationToken cancellationToken) =>
        (await GitAsync(["-C", mirrorPath, "cat-file", "-e", sha + "^{commit}"], mirrorPath, ShortTimeout, cancellationToken).ConfigureAwait(false)).Succeeded;

    public Task<GitFetchResult> FetchCommitAsync(string mirrorPath, string sha, IReadOnlyList<string> localSources, CancellationToken cancellationToken) =>
        WithMirrorLockAsync(mirrorPath, async () =>
        {
            if (await HasCommitAsync(mirrorPath, sha, cancellationToken).ConfigureAwait(false))
            {
                return new GitFetchResult(GitFetchStatus.Ok, "đã có trong mirror");
            }

            foreach (var source in localSources.Where(Directory.Exists))
            {
                await GitAsync(["-C", mirrorPath, "fetch", "-q", "--depth", "1", new Uri(Path.GetFullPath(source)).AbsoluteUri, sha], mirrorPath, FetchTimeout, cancellationToken).ConfigureAwait(false);
                if (await HasCommitAsync(mirrorPath, sha, cancellationToken).ConfigureAwait(false))
                {
                    return new GitFetchResult(GitFetchStatus.Ok, $"lấy từ mirror cục bộ {source}");
                }
            }

            var shallow = await GitAsync(["-C", mirrorPath, "fetch", "-q", "--depth", "1", "origin", sha], mirrorPath, FetchTimeout, cancellationToken).ConfigureAwait(false);
            if (shallow.Succeeded && await HasCommitAsync(mirrorPath, sha, cancellationToken).ConfigureAwait(false))
            {
                return new GitFetchResult(GitFetchStatus.Ok, "fetch --depth 1");
            }

            var kind = Classify(shallow);
            if (kind != GitFetchStatus.Ok)
            {
                return new GitFetchResult(kind, shallow.TimedOut ? "git fetch quá thời gian" : shallow.Tail(5));
            }

            // Server từ chối tải theo SHA: tải toàn bộ nhánh và tag rồi tìm lại commit.
            List<string> full = ["-C", mirrorPath, "fetch", "-q"];
            if (File.Exists(Path.Combine(mirrorPath, "shallow")))
            {
                full.Add("--unshallow");
            }

            full.AddRange(["origin", "+refs/heads/*:refs/heads/*", "+refs/tags/*:refs/tags/*"]);
            var all = await GitAsync(full, mirrorPath, FetchTimeout, cancellationToken).ConfigureAwait(false);
            if (await HasCommitAsync(mirrorPath, sha, cancellationToken).ConfigureAwait(false))
            {
                return new GitFetchResult(GitFetchStatus.Ok, "fetch toàn bộ lịch sử");
            }

            var allKind = all.Succeeded ? GitFetchStatus.CommitMissing : Classify(all);
            return new GitFetchResult(allKind == GitFetchStatus.Ok ? GitFetchStatus.CommitMissing : allKind,
                all.Succeeded ? $"không có commit {sha} trong repository" : all.Tail(5));
        }, cancellationToken);

    /// <summary>Lỗi mạng → thử lại; repository không còn → loại; server từ chối tải theo SHA → <c>Ok</c> (để thử tải toàn bộ).</summary>
    public static GitFetchStatus Classify(ProcessResult result)
    {
        if (result.TimedOut)
        {
            return GitFetchStatus.NetworkError;
        }

        var text = result.Output.ToLowerInvariant();
        if (RefusedMarkers.Any(text.Contains))
        {
            return GitFetchStatus.Ok;
        }

        if (NetworkMarkers.Any(text.Contains))
        {
            return GitFetchStatus.NetworkError;
        }

        return MissingMarkers.Any(text.Contains) ? GitFetchStatus.CommitMissing : GitFetchStatus.Ok;
    }

    public Task<string> AddWorktreeAsync(string mirrorPath, string sha, string targetDir, CancellationToken cancellationToken) =>
        WithMirrorLockAsync(mirrorPath, async () =>
        {
            await GitAsync(["-C", mirrorPath, "worktree", "prune"], mirrorPath, ShortTimeout, cancellationToken).ConfigureAwait(false);
            if (Directory.Exists(targetDir))
            {
                WorkspaceManager.DeleteTree(targetDir);
            }

            Directory.CreateDirectory(Path.GetDirectoryName(targetDir)!);
            var add = await GitAsync(["-C", mirrorPath, "worktree", "add", "--detach", "-f", targetDir, sha], mirrorPath, FetchTimeout, cancellationToken).ConfigureAwait(false);
            if (!add.Succeeded)
            {
                throw new IOException($"git worktree add lỗi: {add.Tail(5)}");
            }

            var head = await GitAsync(["-C", targetDir, "rev-parse", "HEAD"], targetDir, ShortTimeout, cancellationToken).ConfigureAwait(false);
            return head.Output.Trim().Split('\n')[^1].Trim();
        }, cancellationToken);

    public Task RemoveWorktreeAsync(string mirrorPath, string worktreeDir, CancellationToken cancellationToken) =>
        WithMirrorLockAsync<bool>(mirrorPath, async () =>
        {
            if (Directory.Exists(worktreeDir))
            {
                WorkspaceManager.DeleteTree(worktreeDir);
            }

            if (Directory.Exists(mirrorPath))
            {
                await GitAsync(["-C", mirrorPath, "worktree", "prune"], mirrorPath, ShortTimeout, cancellationToken).ConfigureAwait(false);
            }

            return true;
        }, cancellationToken);
}
