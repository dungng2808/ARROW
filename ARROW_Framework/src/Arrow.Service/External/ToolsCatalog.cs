using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Common;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>Một công cụ trong <c>tools/tools.lock.json</c>: file trong <c>tools_dir</c>, nguồn tải (Maven hoặc URL), sha256 đã khóa.</summary>
public sealed record ToolEntry
{
    public string Id { get; init; } = "";
    public string File { get; init; } = "";
    public string? Maven { get; init; }
    public string? Url { get; init; }
    public string? Sha256 { get; init; }
}

public sealed record ToolsLock
{
    public List<ToolEntry> Tools { get; init; } = [];
}

/// <summary>Tình trạng một công cụ cho <c>doctor</c>: có file không, sha256 thật, đã khóa và khớp chưa.</summary>
public sealed record ToolStatus(string Id, string Path, bool Present, string? Sha256, bool Pinned, bool Matches);

/// <summary>Công cụ khối 4 trong <c>tools_dir</c> (spec 04 Mục 1, spec 07 Mục 4.3).</summary>
public interface IToolsCatalog
{
    string ToolsDir { get; }

    /// <summary>Đường dẫn file của công cụ; chưa có thì tải (Maven hoặc URL) rồi kiểm sha256 nếu đã khóa.</summary>
    Task<string> EnsureAsync(string id, CancellationToken cancellationToken);

    /// <summary>Jar Maven ngoài danh sách khóa (ví dụ <c>junit-platform-launcher</c> đúng phiên bản của module).</summary>
    Task<string> EnsureMavenAsync(string coordinates, string relativeFile, CancellationToken cancellationToken);

    IReadOnlyList<ToolStatus> Check();
}

/// <summary>
/// Tải công cụ vào <c>tools_dir</c>: artifact Maven qua <c>maven-dependency-plugin:copy</c> (không cần pom), file khác qua HTTP.
/// sha256 lệch với <c>tools.lock.json</c> → lỗi; chưa khóa → ghi sha256 vào <c>tools.installed.json</c> để <c>doctor</c> báo.
/// </summary>
public sealed class ToolsCatalog(string toolsDir, string lockFile, string? maven, string jdkHome, IProcessRunner runner, ILogRepository log, HttpClient? http = null) : IToolsCatalog
{
    private readonly SemaphoreSlim _gate = new(1, 1);
    private ToolsLock? _lock;

    public string ToolsDir => toolsDir;

    private ToolsLock Lock => _lock ??= System.IO.File.Exists(lockFile) ? JsonFile.Read<ToolsLock>(lockFile) : new ToolsLock();

    private ToolEntry Entry(string id) =>
        Lock.Tools.FirstOrDefault(t => t.Id == id) ?? throw new ArrowException(ErrorCodes.ConfigInvalid, $"tools.lock.json không có công cụ {id}");

    public async Task<string> EnsureAsync(string id, CancellationToken cancellationToken)
    {
        var entry = Entry(id);
        var path = Full(entry.File);
        await _gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            if (!System.IO.File.Exists(path))
            {
                if (entry.Maven is not null)
                {
                    await CopyFromMavenAsync(entry.Maven, path, cancellationToken).ConfigureAwait(false);
                }
                else if (entry.Url is not null)
                {
                    await DownloadAsync(entry.Url, path, cancellationToken).ConfigureAwait(false);
                }
                else
                {
                    throw new ArrowException(ErrorCodes.ConfigInvalid, $"tools.lock.json: {id} không có nguồn tải");
                }

                RecordInstalled(entry.Id, path);
            }

            var actual = Hashing.Sha256Bytes(await System.IO.File.ReadAllBytesAsync(path, cancellationToken).ConfigureAwait(false));
            if (entry.Sha256 is { Length: > 0 } expected && !string.Equals(expected, actual, StringComparison.OrdinalIgnoreCase))
            {
                throw new ArrowException(ErrorCodes.ConfigInvalid, $"sha256 của {entry.File} là {actual[..12]}…, tools.lock.json ghi {expected[..Math.Min(12, expected.Length)]}…");
            }

            return path;
        }
        finally
        {
            _gate.Release();
        }
    }

    public async Task<string> EnsureMavenAsync(string coordinates, string relativeFile, CancellationToken cancellationToken)
    {
        var path = Full(relativeFile);
        await _gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            if (!System.IO.File.Exists(path))
            {
                await CopyFromMavenAsync(coordinates, path, cancellationToken).ConfigureAwait(false);
                RecordInstalled(coordinates, path);
            }

            return path;
        }
        finally
        {
            _gate.Release();
        }
    }

    public IReadOnlyList<ToolStatus> Check() => Lock.Tools.Select(entry =>
    {
        var path = Full(entry.File);
        var present = System.IO.File.Exists(path);
        var sha = present ? Hashing.Sha256Bytes(System.IO.File.ReadAllBytes(path)) : null;
        var pinned = entry.Sha256 is { Length: > 0 };
        return new ToolStatus(entry.Id, path, present, sha, pinned, present && pinned && string.Equals(sha, entry.Sha256, StringComparison.OrdinalIgnoreCase));
    }).ToList();

    private string Full(string relative) => Path.GetFullPath(Path.Combine(toolsDir, relative.Replace('/', Path.DirectorySeparatorChar)));

    private async Task CopyFromMavenAsync(string coordinates, string target, CancellationToken cancellationToken)
    {
        var mvn = maven ?? throw new ArrowException(ErrorCodes.MavenMissing, $"Cần Maven để tải {coordinates}");
        var temp = Path.Combine(toolsDir, ".download-" + Guid.NewGuid().ToString("N")[..8]);
        Directory.CreateDirectory(temp);
        try
        {
            var result = await runner.RunAsync(new ProcessSpec(
                mvn,
                ["-q", "-B", "-ntp", BuildCommands.DependencyPlugin(null) + ":copy", "-Dartifact=" + coordinates, "-DoutputDirectory=" + temp, "-Dmdep.overWriteReleases=true"],
                temp,
                new Dictionary<string, string?> { ["JAVA_HOME"] = jdkHome, ["MAVEN_ARGS"] = null, ["JAVA_TOOL_OPTIONS"] = null },
                TimeSpan.FromMinutes(10)), cancellationToken).ConfigureAwait(false);
            var file = Directory.GetFiles(temp, "*.jar").FirstOrDefault();
            if (!result.Succeeded || file is null)
            {
                throw new ArrowException(ErrorCodes.ConfigInvalid, $"Không tải được {coordinates} qua Maven:\n{result.Tail(10)}");
            }

            Directory.CreateDirectory(Path.GetDirectoryName(target)!);
            System.IO.File.Move(file, target, overwrite: true);
            log.Info($"công cụ: tải {coordinates} → {target}");
        }
        finally
        {
            Directory.Delete(temp, recursive: true);
        }
    }

    private async Task DownloadAsync(string url, string target, CancellationToken cancellationToken)
    {
        var client = http ?? new HttpClient { Timeout = TimeSpan.FromMinutes(10) };
        try
        {
            using var response = await client.GetAsync(url, cancellationToken).ConfigureAwait(false);
            if (!response.IsSuccessStatusCode)
            {
                throw new ArrowException(ErrorCodes.ConfigInvalid, $"Không tải được {url}: HTTP {(int)response.StatusCode}");
            }

            Directory.CreateDirectory(Path.GetDirectoryName(target)!);
            var temp = target + ".part";
            await using (var file = System.IO.File.Create(temp))
            {
                await response.Content.CopyToAsync(file, cancellationToken).ConfigureAwait(false);
            }

            System.IO.File.Move(temp, target, overwrite: true);
            log.Info($"công cụ: tải {url} → {target}");
        }
        catch (HttpRequestException ex)
        {
            throw new ArrowException(ErrorCodes.ConfigInvalid, $"Không tải được {url}: {ex.Message}", ex);
        }
        finally
        {
            if (http is null)
            {
                client.Dispose();
            }
        }
    }

    /// <summary><c>tools_dir/tools.installed.json</c>: sha256 của file đã tải trên máy này (để khóa vào <c>tools.lock.json</c>).</summary>
    private void RecordInstalled(string id, string path)
    {
        var file = Path.Combine(toolsDir, "tools.installed.json");
        var installed = System.IO.File.Exists(file) ? JsonFile.Read<Dictionary<string, string>>(file) : [];
        installed[id] = Hashing.Sha256Bytes(System.IO.File.ReadAllBytes(path));
        JsonFile.Write(file, installed);
    }
}
