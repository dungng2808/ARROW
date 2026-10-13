using System.Security.Cryptography;
using System.Text;
using Arrow.Models.Common;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>
/// Thư mục làm việc trong <c>work_dir</c> (spec 01 Mục 2.3, spec 03 Mục 3.0): <c>mirrors/</c> dùng chung mọi run,
/// <c>ws/&lt;run&gt;/&lt;task&gt;/</c> gồm <c>base</c> (worktree đúng commit), <c>snapshot</c> (đã biên dịch) và <c>tmp/</c> (bản sao cho từng lần chạy).
/// </summary>
public sealed class WorkspaceManager(IGitClient git, IProcessRunner runner, string workDir) : IWorkspaceManager
{
    /// <summary>Đường dẫn thật (đã giải symlink như <c>/var</c> → <c>/private/var</c> trên macOS) để khớp với đường dẫn Gradle/Maven in ra.</summary>
    public string WorkDir { get; } = Canonical(workDir);

    /// <summary>Đường dẫn tuyệt đối với mọi thư mục cha là symlink đã được giải; phần chưa tồn tại giữ nguyên.</summary>
    public static string Canonical(string path)
    {
        var full = Path.GetFullPath(path);
        var root = Path.GetPathRoot(full) ?? "";
        var current = root;
        foreach (var part in full[root.Length..].Split(Path.DirectorySeparatorChar, StringSplitOptions.RemoveEmptyEntries))
        {
            var next = Path.Combine(current, part);
            for (var depth = 0; depth < 8 && new DirectoryInfo(next) is { Exists: true, LinkTarget: { } target }; depth++)
            {
                next = Path.GetFullPath(Path.Combine(current, target));
            }

            current = next;
        }

        return current;
    }

    public string MirrorPath(string repoUrl)
    {
        var (host, segments) = Split(repoUrl);
        return Path.Combine([WorkDir, "mirrors", host, .. segments[..^1], segments[^1] + ".git"]);
    }

    public IReadOnlyList<string> LegacyMirrors(string repoUrl)
    {
        var (_, segments) = Split(repoUrl);
        if (segments.Length < 2)
        {
            return [];
        }

        var legacy = Path.Combine(WorkDir, "mirrors", $"{segments[^2]}__{segments[^1]}.git");
        return Directory.Exists(legacy) ? [legacy] : [];
    }

    /// <summary>Host và các đoạn đường dẫn của URL, bỏ <c>.git</c>; URL <c>file://</c> → host <c>local</c>, tên kèm hash đường dẫn.</summary>
    private static (string Host, string[] Segments) Split(string repoUrl)
    {
        if (!Uri.TryCreate(repoUrl, UriKind.Absolute, out var uri))
        {
            throw new ArgumentException($"repo_url không hợp lệ: {repoUrl}");
        }

        if (uri.IsFile)
        {
            var name = Safe(Path.GetFileName(uri.LocalPath.TrimEnd('/', '\\')).Replace(".git", "", StringComparison.Ordinal));
            var hash = Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(uri.LocalPath)))[..8].ToLowerInvariant();
            return ("local", [$"{name}-{hash}"]);
        }

        var segments = uri.AbsolutePath.Trim('/').Split('/', StringSplitOptions.RemoveEmptyEntries).Select(Safe).ToArray();
        if (segments.Length == 0)
        {
            throw new ArgumentException($"repo_url không có đường dẫn repository: {repoUrl}");
        }

        if (segments[^1].EndsWith(".git", StringComparison.Ordinal))
        {
            segments[^1] = segments[^1][..^4];
        }

        return (Safe(uri.Host), segments);
    }

    private static string Safe(string part) => new(part.Select(c => char.IsAsciiLetterOrDigit(c) || c is '-' or '_' or '.' ? c : '_').ToArray());

    public string TaskDir(string runId, TaskKey task) => Path.Combine(WorkDir, "ws", runId, task.TaskId);

    public async Task CopyDirectoryAsync(string source, string target, CancellationToken cancellationToken)
    {
        if (Directory.Exists(target))
        {
            DeleteTree(target);
        }

        Directory.CreateDirectory(Path.GetDirectoryName(target)!);
        if (OperatingSystem.IsMacOS() || OperatingSystem.IsLinux())
        {
            IReadOnlyList<string>[] attempts = OperatingSystem.IsMacOS()
                ? [["-c", "-R", "-p", source, target], ["-R", "-p", source, target]]
                : [["-a", "--reflink=auto", source, target]];
            foreach (var arguments in attempts)
            {
                var result = await runner.RunAsync(new ProcessSpec("/bin/cp", arguments, Path.GetDirectoryName(target)!, Timeout: TimeSpan.FromMinutes(30)), cancellationToken).ConfigureAwait(false);
                if (result.Succeeded)
                {
                    return;
                }

                if (Directory.Exists(target))
                {
                    DeleteTree(target);
                }
            }
        }

        ManagedCopy(new DirectoryInfo(source), target, cancellationToken);
    }

    /// <summary>Sao chép thường: giữ symlink, quyền chạy (File.Copy giữ mode trên Unix) và thời gian sửa (Maven so thời gian để biết cần biên dịch lại không).</summary>
    private static void ManagedCopy(DirectoryInfo source, string target, CancellationToken cancellationToken)
    {
        Directory.CreateDirectory(target);
        foreach (var entry in source.EnumerateFileSystemInfos())
        {
            cancellationToken.ThrowIfCancellationRequested();
            var destination = Path.Combine(target, entry.Name);
            if (entry.LinkTarget is { } link)
            {
                if (entry is DirectoryInfo)
                {
                    Directory.CreateSymbolicLink(destination, link);
                }
                else
                {
                    File.CreateSymbolicLink(destination, link);
                }
            }
            else if (entry is DirectoryInfo directory)
            {
                ManagedCopy(directory, destination, cancellationToken);
                Directory.SetLastWriteTimeUtc(destination, directory.LastWriteTimeUtc);
            }
            else
            {
                File.Copy(entry.FullName, destination);
                File.SetLastWriteTimeUtc(destination, entry.LastWriteTimeUtc);
            }
        }
    }

    public async Task<string> NewCopyAsync(string snapshot, string runId, TaskKey task, string label, CancellationToken cancellationToken)
    {
        var target = Path.Combine(TaskDir(runId, task), "tmp", $"{label}-{Guid.NewGuid():N}"[..(label.Length + 9)]);
        await CopyDirectoryAsync(snapshot, target, cancellationToken).ConfigureAwait(false);
        return target;
    }

    public void DeleteDirectory(string path)
    {
        if (Directory.Exists(path))
        {
            DeleteTree(path);
        }
    }

    public async Task DeleteTaskAsync(string runId, TaskKey task, string? mirrorPath, CancellationToken cancellationToken)
    {
        var dir = TaskDir(runId, task);
        if (mirrorPath is not null && Directory.Exists(mirrorPath))
        {
            await git.RemoveWorktreeAsync(mirrorPath, Path.Combine(dir, "base"), cancellationToken).ConfigureAwait(false);
        }

        DeleteDirectory(dir);
    }

    /// <summary>Xóa cây thư mục; bỏ thuộc tính chỉ đọc (Windows, file trong <c>.git</c>) rồi thử lại.</summary>
    public static void DeleteTree(string path)
    {
        for (var attempt = 1; ; attempt++)
        {
            try
            {
                Directory.Delete(path, recursive: true);
                return;
            }
            catch (DirectoryNotFoundException)
            {
                return;
            }
            catch (Exception ex) when (ex is IOException or UnauthorizedAccessException && attempt < 3)
            {
                foreach (var file in Directory.EnumerateFiles(path, "*", new EnumerationOptions { RecurseSubdirectories = true, AttributesToSkip = FileAttributes.ReparsePoint }))
                {
                    try
                    {
                        File.SetAttributes(file, FileAttributes.Normal);
                    }
                    catch (IOException)
                    {
                    }
                }

                Thread.Sleep(200 * attempt);
            }
        }
    }
}
