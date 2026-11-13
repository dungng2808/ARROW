using System.Collections.Concurrent;
using System.Security.Cryptography;
using System.Text;
using Preflight.Models;
using Preflight.Service.Core.Common;
using Preflight.Service.External;
using Preflight.Service.Core.Runner;

namespace Preflight.Service.Core.Git;

public sealed record GitResult(int ExitCode, string Output, string Error);
public sealed class GitCommandException(string message) : Exception(message);

/// <summary>Lệnh Git có log, timeout và hủy; không phụ thuộc shell hoặc global git config.</summary>
public sealed class GitCli(string runRoot, IProcessRunner? process = null, int timeoutSeconds = 1800)
{
    private readonly IProcessRunner runner = process ?? new ProcessRunner();
    private int ordinal;
    private readonly string session = Guid.NewGuid().ToString("N")[..12];
    public async Task<GitResult> RunAsync(string? directory, IEnumerable<string> args, bool check = true, CancellationToken ct = default)
    {
        var command = new List<string> { "git", "-c", "credential.helper=", "-c", "core.longpaths=true", "-c", "core.autocrlf=false", "-c", "core.filemode=false" };
        if (directory is not null) { command.Add("-c"); command.Add("safe.directory=" + Path.GetFullPath(directory).Replace('\\', '/')); command.Add("-C"); command.Add(directory); }
        command.AddRange(args);
        var env = Environment.GetEnvironmentVariables().Cast<System.Collections.DictionaryEntry>().ToDictionary(e => (string)e.Key, e => (string)e.Value!, StringComparer.OrdinalIgnoreCase);
        env["GIT_TERMINAL_PROMPT"] = "0"; env["GCM_INTERACTIVE"] = "never";
        string log = Path.Combine(runRoot, "logs/git", $"{session}-git-{Interlocked.Increment(ref ordinal):D8}.log");
        var attempt = await runner.RunAsync(command, directory ?? runRoot, timeoutSeconds, log, env, ct);
        string text = File.ReadAllText(log); string stdout = "", stderr = text;
        int start = text.IndexOf("\n\nSTDOUT\n", StringComparison.Ordinal), end = text.LastIndexOf("\n\nSTDERR\n", StringComparison.Ordinal);
        if (start >= 0 && end >= start) { stdout = text[(start + 9)..end]; stderr = text[(end + 9)..]; }
        var result = new GitResult(attempt.TimedOut ? 124 : attempt.ExitCode ?? 1, stdout, stderr);
        if (result.ExitCode != 0 && InfrastructureFailure.LogIsDiskFull(text)) throw new InfrastructureFailure("INFRA_DISK_FULL: " + stderr);
        if (check && result.ExitCode != 0) throw new GitCommandException($"git {string.Join(' ', args)}: {result.Error}");
        return result;
    }
    public async Task<string?> ShowAsync(string mirror, string sha, string path, CancellationToken ct)
    {
        var result = await RunAsync(mirror, ["show", sha + ":" + path], false, ct); return result.ExitCode == 0 ? result.Output : null;
    }
}

/// <summary>Mirror theo RepoUrl; kiểm origin/fsck và cập nhật một lần mỗi session.</summary>
public sealed class MirrorManager(string runRoot, GitCli git)
{
    private readonly ConcurrentDictionary<string, SemaphoreSlim> locks = new(StringComparer.Ordinal);
    private readonly ConcurrentDictionary<string, byte> prepared = new(StringComparer.Ordinal);
    public SemaphoreSlim Lock(string repo) => locks.GetOrAdd(repo, _ => new(1, 1));
    public string MirrorPath(string repo) => Path.Combine(runRoot, "cache/mirrors", Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(repo))).ToLowerInvariant() + ".git");
    public async Task<string> EnsureAsync(RepoSource source, bool offline = false, CancellationToken ct = default)
    {
        var gate = Lock(source.RepoUrl); await gate.WaitAsync(ct);
        try
        {
            string path = MirrorPath(source.RepoUrl);
            if (prepared.ContainsKey(source.RepoUrl)) return path;
            Directory.CreateDirectory(Path.GetDirectoryName(path)!);
            string origin = source.Kind == "snapshot" ? source.SnapshotPath : source.CloneUrl;
            if (Directory.Exists(path))
            {
                var bare = await git.RunAsync(path, ["rev-parse", "--is-bare-repository"], false, ct);
                var remote = await git.RunAsync(path, ["remote", "get-url", "origin"], false, ct);
                var health = await git.RunAsync(path, ["fsck", "--connectivity-only"], false, ct);
                if (bare.Output.Trim() != "true" || remote.Output.Trim() != origin || health.ExitCode != 0) SafeFiles.DeleteTree(path, Path.Combine(runRoot, "cache/mirrors"));
                else if (source.Kind != "snapshot" && !offline)
                {
                    var update = await git.RunAsync(path, ["remote", "update", "--prune"], false, ct);
                    if (update.ExitCode != 0)
                    {
                        var healthy = await git.RunAsync(path, ["fsck", "--connectivity-only"], false, ct);
                        if (healthy.ExitCode == 0) throw new GitCommandException(update.Error);
                        SafeFiles.DeleteTree(path, Path.Combine(runRoot, "cache/mirrors"));
                    }
                }
            }
            if (!Directory.Exists(path))
            {
                if (offline && source.Kind != "snapshot" && !Directory.Exists(origin)) throw new GitCommandException("OFFLINE_MIRROR_MISSING: " + source.RepoUrl);
                await git.RunAsync(null, ["clone", "--mirror", "--", origin, path], true, ct);
            }
            prepared.TryAdd(source.RepoUrl, 0); return path;
        }
        finally { gate.Release(); }
    }
}

/// <summary>Kiểm tra root và reparse point trước mọi thao tác xóa thư mục do run sở hữu.</summary>
public static class SafeFiles
{
    public static void DeleteTree(string target, string allowedRoot)
    {
        string absolute = Path.GetFullPath(target), root = Path.GetFullPath(allowedRoot);
        if (absolute.Equals(root, StringComparison.OrdinalIgnoreCase) || !ResolvePath.IsWithin(absolute, root)) throw new InvalidDataException("UNSAFE_DELETE_PATH: " + target);
        for (var item = new DirectoryInfo(absolute); item is not null; item = item.Parent)
        {
            if (item.Exists && (item.Attributes & FileAttributes.ReparsePoint) != 0) throw new InvalidDataException("LINKED_DELETE_PATH: " + target);
            if (item.FullName.Equals(root, StringComparison.OrdinalIgnoreCase)) break;
        }
        if (!Directory.Exists(absolute)) return;
        // Không đi xuyên symlink/junction bên trong workspace.
        DeleteContents(new DirectoryInfo(absolute)); Directory.Delete(absolute);
    }
    private static void DeleteContents(DirectoryInfo directory)
    {
        foreach (var item in directory.EnumerateFileSystemInfos())
        {
            if ((item.Attributes & FileAttributes.ReparsePoint) != 0) { if (item is DirectoryInfo) Directory.Delete(item.FullName); else File.Delete(item.FullName); }
            else if (item is DirectoryInfo sub) { DeleteContents(sub); sub.Delete(); }
            else { item.Attributes &= ~FileAttributes.ReadOnly; item.Delete(); }
        }
    }
}
