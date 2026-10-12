using System.Diagnostics;
using System.Text;
using Arrow.Core.Util;

namespace Arrow.Infrastructure.Processes;

public sealed record ProcessResult(int? ExitCode, string StdOut, string StdErr, bool TimedOut, TimeSpan Duration)
{
    public bool Succeeded => !TimedOut && ExitCode == 0;
    public string Output => StdOut + (StdErr.Length > 0 ? "\n" + StdErr : "");
}

/// <summary>Chạy tiến trình có timeout; quá thời gian thì dừng cả cây tiến trình (specs/02 §4.4).</summary>
public static class ProcessRunner
{
    public static async Task<ProcessResult> RunAsync(
        string fileName,
        IEnumerable<string> arguments,
        string? workingDirectory = null,
        IReadOnlyDictionary<string, string?>? environment = null,
        TimeSpan? timeout = null,
        CancellationToken cancellationToken = default)
    {
        var startInfo = new ProcessStartInfo
        {
            FileName = fileName,
            WorkingDirectory = workingDirectory ?? Environment.CurrentDirectory,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            RedirectStandardInput = true,
            UseShellExecute = false,
            CreateNoWindow = true,
            StandardOutputEncoding = Encoding.UTF8,
            StandardErrorEncoding = Encoding.UTF8,
        };
        foreach (var argument in arguments)
        {
            startInfo.ArgumentList.Add(argument);
        }

        if (environment is not null)
        {
            foreach (var (key, value) in environment)
            {
                if (value is null)
                {
                    startInfo.Environment.Remove(key);
                }
                else
                {
                    startInfo.Environment[key] = value;
                }
            }
        }

        var commandLine = fileName + " " + string.Join(' ', startInfo.ArgumentList.Select(a => a.Contains(' ') ? $"\"{a}\"" : a));
        ToolLog.Debug($"$ {commandLine} (cwd {startInfo.WorkingDirectory})");
        var stdout = new StringBuilder();
        var stderr = new StringBuilder();
        var stopwatch = Stopwatch.StartNew();
        using var process = new System.Diagnostics.Process { StartInfo = startInfo };
        process.OutputDataReceived += (_, e) => { if (e.Data is not null) { lock (stdout) { stdout.AppendLine(e.Data); } } };
        process.ErrorDataReceived += (_, e) => { if (e.Data is not null) { lock (stderr) { stderr.AppendLine(e.Data); } } };

        try
        {
            process.Start();
        }
        catch (Exception ex) when (ex is System.ComponentModel.Win32Exception or FileNotFoundException)
        {
            ToolLog.Warn($"không chạy được: {commandLine}: {ex.Message}");
            return new ProcessResult(null, "", $"Không chạy được {fileName}: {ex.Message}", false, stopwatch.Elapsed);
        }

        process.StandardInput.Close();
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();

        using var timeoutSource = timeout is null ? new CancellationTokenSource() : new CancellationTokenSource(timeout.Value);
        using var linked = CancellationTokenSource.CreateLinkedTokenSource(timeoutSource.Token, cancellationToken);
        var timedOut = false;
        try
        {
            await process.WaitForExitAsync(linked.Token).ConfigureAwait(false);
        }
        catch (OperationCanceledException)
        {
            timedOut = timeoutSource.IsCancellationRequested;
            KillTree(process);
            if (!timedOut)
            {
                throw;
            }
        }

        process.WaitForExit(); // đợi đọc hết output
        stopwatch.Stop();
        var seconds = stopwatch.Elapsed.TotalSeconds.ToString("0.0", System.Globalization.CultureInfo.InvariantCulture);
        if (timedOut)
        {
            ToolLog.Warn($"quá thời gian sau {seconds}s, đã dừng cả cây tiến trình: {commandLine}");
        }
        else
        {
            ToolLog.Info($"chạy: {commandLine} → mã thoát {process.ExitCode} sau {seconds}s");
        }
        return new ProcessResult(timedOut ? null : process.ExitCode, stdout.ToString(), stderr.ToString(), timedOut, stopwatch.Elapsed);
    }

    private static void KillTree(System.Diagnostics.Process process)
    {
        try
        {
            if (!process.HasExited)
            {
                process.Kill(entireProcessTree: true);
            }
        }
        catch (InvalidOperationException)
        {
        }
    }

    /// <summary>Tìm chương trình trong PATH; trên Windows thử thêm .exe, .cmd, .bat.</summary>
    public static string? FindOnPath(string name)
    {
        var extensions = OperatingSystem.IsWindows() ? new[] { ".exe", ".cmd", ".bat", "" } : new[] { "" };
        var paths = (Environment.GetEnvironmentVariable("PATH") ?? "").Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries);
        var extraUnix = OperatingSystem.IsWindows() ? [] : new[] { "/opt/homebrew/bin", "/usr/local/bin", "/usr/bin" };
        foreach (var directory in paths.Concat(extraUnix).Distinct())
        {
            foreach (var extension in extensions)
            {
                var candidate = Path.Combine(directory, name + extension);
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }
        }

        return null;
    }
}
