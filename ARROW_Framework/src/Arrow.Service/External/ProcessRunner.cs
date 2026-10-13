using System.Diagnostics;
using System.Text;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>Chạy tiến trình, gom stdout+stderr, giết cả cây tiến trình khi timeout hoặc bị hủy.</summary>
public sealed class ProcessRunner : IProcessRunner
{
    public async Task<ProcessResult> RunAsync(ProcessSpec spec, CancellationToken cancellationToken)
    {
        var start = new ProcessStartInfo(spec.FileName)
        {
            WorkingDirectory = spec.WorkingDirectory,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            RedirectStandardInput = true,
            UseShellExecute = false,
            CreateNoWindow = true,
            StandardOutputEncoding = Encoding.UTF8,
            StandardErrorEncoding = Encoding.UTF8,
        };
        foreach (var argument in spec.Arguments)
        {
            start.ArgumentList.Add(argument);
        }

        foreach (var (name, value) in spec.Environment ?? new Dictionary<string, string?>())
        {
            if (value is null)
            {
                start.Environment.Remove(name);
            }
            else
            {
                start.Environment[name] = value;
            }
        }

        var output = new StringBuilder();
        void Append(string? line)
        {
            if (line is not null)
            {
                lock (output)
                {
                    output.Append(line).Append('\n');
                }
            }
        }

        var watch = Stopwatch.StartNew();
        using var process = new Process { StartInfo = start, EnableRaisingEvents = true };
        process.OutputDataReceived += (_, e) => Append(e.Data);
        process.ErrorDataReceived += (_, e) => Append(e.Data);
        if (!process.Start())
        {
            throw new InvalidOperationException($"Không chạy được {spec.FileName}");
        }

        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
        if (spec.StandardInput is not null)
        {
            await process.StandardInput.WriteAsync(spec.StandardInput).ConfigureAwait(false);
        }

        process.StandardInput.Close();

        using var timeout = new CancellationTokenSource(spec.Timeout ?? Timeout.InfiniteTimeSpan);
        using var linked = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken, timeout.Token);
        var timedOut = false;
        try
        {
            await process.WaitForExitAsync(linked.Token).ConfigureAwait(false);
        }
        catch (OperationCanceledException)
        {
            timedOut = timeout.IsCancellationRequested && !cancellationToken.IsCancellationRequested;
            Kill(process);
            if (!timedOut)
            {
                throw;
            }
        }

        // Chờ đọc nốt output còn trong bộ đệm.
        if (!timedOut)
        {
            process.WaitForExit();
        }

        string text;
        lock (output)
        {
            text = output.ToString();
        }

        return new ProcessResult(timedOut ? null : process.ExitCode, timedOut, text, watch.Elapsed);
    }

    private static void Kill(Process process)
    {
        try
        {
            if (!process.HasExited)
            {
                process.Kill(entireProcessTree: true);
                process.WaitForExit(10_000);
            }
        }
        catch (InvalidOperationException)
        {
            // Tiến trình đã thoát.
        }
    }
}
