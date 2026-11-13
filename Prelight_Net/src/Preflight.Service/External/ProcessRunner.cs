using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.Text;
using Preflight.Models;

namespace Preflight.Service.External;

public interface IProcessRunner
{
    Task<BuildAttempt> RunAsync(IReadOnlyList<string> command, string workingDirectory, double timeoutSeconds,
        string logPath, IReadOnlyDictionary<string, string>? environment = null, CancellationToken cancellationToken = default);
}

/// <summary>Port của runner._run/process.terminate_process_tree: timeout, hủy, log và kill cả cây.</summary>
public sealed class ProcessRunner : IProcessRunner
{
    public async Task<BuildAttempt> RunAsync(IReadOnlyList<string> command, string workingDirectory, double timeoutSeconds,
        string logPath, IReadOnlyDictionary<string, string>? environment = null, CancellationToken cancellationToken = default)
    {
        if (command.Count == 0) throw new ArgumentException("Command must not be empty", nameof(command));
        if (timeoutSeconds <= 0 || !double.IsFinite(timeoutSeconds)) throw new ArgumentOutOfRangeException(nameof(timeoutSeconds));
        cancellationToken.ThrowIfCancellationRequested();
        var elapsed = Stopwatch.StartNew();
        Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(logPath))!);
        string heading = "$ " + string.Join(' ', command) + "\n\n";
        int? exitCode = null; bool timedOut = false;
        string output;
        string? executable = FindExecutable(command[0], environment);
        if (executable is null)
        {
            output = heading + "EXECUTABLE_NOT_FOUND\n" + command[0]; exitCode = 127;
        }
        else
        {
            var start = new ProcessStartInfo(executable) { WorkingDirectory = workingDirectory, UseShellExecute = false, CreateNoWindow = true,
                RedirectStandardOutput = true, RedirectStandardError = true, RedirectStandardInput = true,
                StandardOutputEncoding = new UTF8Encoding(false), StandardErrorEncoding = new UTF8Encoding(false) };
            if (environment is not null)
            {
                start.Environment.Clear();
                foreach (var pair in environment) start.Environment[pair.Key] = pair.Value;
            }
            if (OperatingSystem.IsWindows() && Path.GetExtension(executable).ToLowerInvariant() is ".cmd" or ".bat")
            {
                start.FileName = Environment.GetEnvironmentVariable("ComSpec") ?? "cmd.exe";
                start.Arguments = "/d /s /c \"" + string.Join(' ', new[] { executable }.Concat(command.Skip(1)).Select(QuoteBatch)) + "\"";
            }
            else foreach (string argument in command.Skip(1)) start.ArgumentList.Add(argument);
            using var process = new Process { StartInfo = start };
            using var job = OperatingSystem.IsWindows() ? new WindowsProcessJob() : null;
            try
            {
                process.Start(); job?.Attach(process); process.StandardInput.Close();
                // Anonymous pipe Windows có thể chặn thread pool khi test host chỉ có
                // ít worker. Luồng đọc riêng giữ timer/hủy hoạt động đúng hạn.
                Task<string> stdoutTask = Task.Factory.StartNew(process.StandardOutput.ReadToEnd, CancellationToken.None, TaskCreationOptions.LongRunning, TaskScheduler.Default);
                Task<string> stderrTask = Task.Factory.StartNew(process.StandardError.ReadToEnd, CancellationToken.None, TaskCreationOptions.LongRunning, TaskScheduler.Default);
                using var timeout = new CancellationTokenSource(TimeSpan.FromSeconds(timeoutSeconds));
                using var linked = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken, timeout.Token);
                bool cancelled = false;
                try
                {
                    await process.WaitForExitAsync(linked.Token).ConfigureAwait(false);
                    // Process cha có thể đã thoát nhưng cháu vẫn giữ pipe mở.
                    await Task.WhenAll(stdoutTask, stderrTask).WaitAsync(linked.Token).ConfigureAwait(false);
                }
                catch (OperationCanceledException)
                {
                    cancelled = cancellationToken.IsCancellationRequested; timedOut = !cancelled;
                    job?.Terminate();
                    try { if (!process.HasExited) process.Kill(entireProcessTree: true); }
                    catch (InvalidOperationException) { }
                    await process.WaitForExitAsync(CancellationToken.None).ConfigureAwait(false);
                }
                string stdout = UniversalNewlines(await stdoutTask.ConfigureAwait(false)), stderr = UniversalNewlines(await stderrTask.ConfigureAwait(false));
                if (cancelled)
                {
                    await WriteLogAsync(logPath, heading + $"CANCELLED\n{stdout}\n{stderr}").ConfigureAwait(false);
                    throw new OperationCanceledException(cancellationToken);
                }
                if (timedOut) output = heading + $"TIMEOUT after {timeoutSeconds.ToString("G", CultureInfo.InvariantCulture)}s\n{stdout}\n{stderr}";
                else { exitCode = process.ExitCode; output = heading + $"STDOUT\n{stdout}\n\nSTDERR\n{stderr}"; }
            }
            catch (Win32Exception error) when (error.NativeErrorCode is 2 or 3)
            { output = heading + "EXECUTABLE_NOT_FOUND\n" + error.Message; exitCode = 127; }
        }
        await WriteLogAsync(logPath, output).ConfigureAwait(false);
        return new("", workingDirectory, command.ToArray(), exitCode, timedOut, Math.Round(elapsed.Elapsed.TotalSeconds, 3, MidpointRounding.ToEven), logPath);
    }
    private static string UniversalNewlines(string value) => value.Replace("\r\n", "\n", StringComparison.Ordinal).Replace('\r', '\n');
    private static Task WriteLogAsync(string path, string output) => File.WriteAllTextAsync(path, output, new UTF8Encoding(false), CancellationToken.None);
    private static string QuoteBatch(string value)
    {
        if (value.IndexOfAny(['"', '\r', '\n', '\0', '%']) >= 0) throw new ArgumentException("Unsupported batch argument character");
        return "\"" + value + "\"";
    }
    private static string? FindExecutable(string executable, IReadOnlyDictionary<string, string>? environment)
    {
        if (Path.IsPathRooted(executable) || executable.Contains(Path.DirectorySeparatorChar) || executable.Contains(Path.AltDirectorySeparatorChar))
            return File.Exists(executable) ? Path.GetFullPath(executable) : null;
        string? path = environment is null ? Environment.GetEnvironmentVariable("PATH") : environment.FirstOrDefault(p => p.Key.Equals("PATH", StringComparison.OrdinalIgnoreCase)).Value;
        string[] extensions = OperatingSystem.IsWindows() && Path.GetExtension(executable).Length == 0 ? ["", ".exe", ".cmd", ".bat", ".com"] : [""];
        foreach (string directory in (path ?? "").Split(Path.PathSeparator))
            foreach (string extension in extensions)
            {
                string candidate = Path.Combine(directory.Trim('"'), executable + extension);
                if (File.Exists(candidate)) return Path.GetFullPath(candidate);
            }
        return null;
    }
}
