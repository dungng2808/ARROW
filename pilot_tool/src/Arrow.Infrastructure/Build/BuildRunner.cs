using System.Globalization;
using System.Text;
using Arrow.Core.Prepare;
using Arrow.Core.Util;
using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Build;

/// <summary>Kết quả một lệnh build; <see cref="Output"/> là stdout+stderr đầy đủ (log trên đĩa có thể bị cắt).</summary>
public sealed record StepResult(string Step, int? ExitCode, bool TimedOut, double Seconds, string Output, string LogPath)
{
    public bool Succeeded => !TimedOut && ExitCode == 0;

    public StepRecord ToRecord(string logsRoot) => new()
    {
        Step = Step,
        Log = Path.GetRelativePath(logsRoot, LogPath).Replace('\\', '/'),
        ExitCode = ExitCode,
        TimedOut = TimedOut,
        Seconds = Math.Round(Seconds, 2),
    };
}

/// <summary>
/// Chạy lệnh Maven/Gradle cho một workspace với JDK đã chọn, ghi log theo specs/08 §4
/// (dòng đầu: lệnh, thư mục, JDK, thời điểm; cuối: mã thoát, thời gian, timeout).
/// </summary>
public sealed class BuildRunner
{
    public const long MaxLogBytes = 5L * 1024 * 1024;
    private const int KeepBytes = 2 * 1024 * 1024;

    private readonly string _workspace;
    private readonly string _executable;
    private readonly JdkChoice _jdk;
    private readonly Dictionary<string, string?> _environment;

    public BuildRunner(string workspace, BuildPlan plan, JdkChoice jdk, string machineMaven, string machineGradle)
    {
        _workspace = workspace;
        _jdk = jdk;
        _executable = plan.Wrapper is not null
            ? Path.GetFullPath(Path.Combine(workspace, plan.Wrapper))
            : plan.Tool == BuildToolKind.Maven ? machineMaven : machineGradle;
        _environment = BuildEnvironment(jdk.Home);
    }

    public string Executable => _executable;

    /// <summary>Môi trường của mọi tiến trình build (specs/02 §3 bước 4).</summary>
    public static Dictionary<string, string?> BuildEnvironment(string jdkHome)
    {
        var path = System.Environment.GetEnvironmentVariable("PATH") ?? "";
        var environment = new Dictionary<string, string?>
        {
            ["JAVA_HOME"] = jdkHome,
            ["PATH"] = Path.Combine(jdkHome, "bin") + Path.PathSeparator + path,
            ["MAVEN_OPTS"] = "-Xmx2g",
            ["GRADLE_OPTS"] = "-Xmx2g -Dorg.gradle.daemon=false",
            ["JAVA_TOOL_OPTIONS"] = string.Join(' ', BuildCommands.JvmOptions),
            // Không để cấu hình Java/Maven của người dùng chen vào kết quả.
            ["JDK_JAVA_OPTIONS"] = null,
            ["MAVEN_ARGS"] = null,
        };
        if (!OperatingSystem.IsWindows())
        {
            environment["LC_ALL"] = "C.UTF-8";
        }

        return environment;
    }

    public async Task<StepResult> RunAsync(BuildCommand command, string step, string logPath, TimeSpan timeout, CancellationToken cancellationToken)
    {
        var cwd = Path.GetFullPath(Path.Combine(_workspace, command.WorkingDir == "." ? "" : command.WorkingDir));
        var started = DateTimeOffset.UtcNow;
        var result = await ProcessRunner.RunAsync(_executable, command.Arguments, cwd, _environment, timeout, cancellationToken).ConfigureAwait(false);
        var output = result.Output;
        WriteLog(logPath, command, cwd, started, result);
        return new StepResult(step, result.ExitCode, result.TimedOut, result.Duration.TotalSeconds, output, logPath);
    }

    private void WriteLog(string logPath, BuildCommand command, string cwd, DateTimeOffset started, ProcessResult result)
    {
        var body = ToolLog.Redact(result.Output);
        var bytes = Encoding.UTF8.GetByteCount(body);
        if (bytes > MaxLogBytes)
        {
            var raw = Encoding.UTF8.GetBytes(body);
            body = Encoding.UTF8.GetString(raw, 0, KeepBytes)
                   + $"\n… {raw.Length - 2 * KeepBytes} bytes omitted …\n"
                   + Encoding.UTF8.GetString(raw, raw.Length - KeepBytes, KeepBytes);
        }

        var quoted = string.Join(' ', command.Arguments.Select(a => a.Contains(' ') ? $"\"{a}\"" : a));
        var text = new StringBuilder()
            .Append("$ ").Append(_executable).Append(' ').AppendLine(quoted)
            .Append("cwd: ").AppendLine(cwd)
            .Append("jdk: ").Append(_jdk.Major.ToString(CultureInfo.InvariantCulture)).Append(" (").Append(_jdk.Home).Append(") · started: ")
            .AppendLine(started.ToString("yyyy-MM-ddTHH:mm:ssZ", CultureInfo.InvariantCulture))
            .AppendLine("--- stdout+stderr ---")
            .Append(body);
        if (!body.EndsWith('\n'))
        {
            text.Append('\n');
        }

        text.AppendLine("--- end ---")
            .Append("exit: ").Append(result.ExitCode?.ToString(CultureInfo.InvariantCulture) ?? "none")
            .Append(" · duration: ").Append(result.Duration.TotalSeconds.ToString("0.00", CultureInfo.InvariantCulture)).Append('s')
            .Append(" · timeout: ").AppendLine(result.TimedOut ? "true" : "false");
        AtomicFile.WriteAllText(logPath, text.ToString());
    }

    /// <summary>Xóa báo cáo test cũ của module trước mỗi bước chạy test (specs/02 §4.4).</summary>
    public static void ClearReports(string moduleDirAbsolute, BuildToolKind tool)
    {
        var directory = Path.Combine(moduleDirAbsolute, BuildCommands.ReportDir(tool));
        if (Directory.Exists(directory))
        {
            Directory.Delete(directory, recursive: true);
        }
    }

    /// <summary>Đọc báo cáo XML của module; chép sang <paramref name="copyTo"/> để giữ sau khi xóa workspace.</summary>
    public static (List<TestCaseResult> Results, string Source) ReadReports(string moduleDirAbsolute, BuildToolKind tool, string? copyTo)
    {
        var directory = Path.Combine(moduleDirAbsolute, BuildCommands.ReportDir(tool));
        var files = Directory.Exists(directory)
            ? Directory.EnumerateFiles(directory, "TEST-*.xml", SearchOption.AllDirectories).OrderBy(f => f, StringComparer.Ordinal).ToList()
            : [];
        if (files.Count == 0)
        {
            return ([], "none");
        }

        if (copyTo is not null)
        {
            Directory.CreateDirectory(copyTo);
            foreach (var file in files)
            {
                File.Copy(file, Path.Combine(copyTo, Path.GetFileName(file)), overwrite: true);
            }
        }

        var results = TestReportParser.ParseMany(files.Select(File.ReadAllText));
        return (results, tool == BuildToolKind.Maven ? "surefire-xml" : "gradle-xml");
    }
}
