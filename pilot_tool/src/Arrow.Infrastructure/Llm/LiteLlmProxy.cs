using System.Diagnostics;
using Arrow.Core.Config;
using Arrow.Core.Util;
using Arrow.Infrastructure.Tooling;

namespace Arrow.Infrastructure.Llm;

/// <summary>
/// LiteLLM proxy của máy (specs/01 §2, SETUP.md): <c>proxy_mode=auto_start</c> thì tool tự bật proxy khi chưa chạy và
/// tắt khi xong; <c>external</c> thì chỉ kiểm tra proxy đang chạy.
/// </summary>
public sealed class LiteLlmProxy : IAsyncDisposable
{
    private readonly Process? _process;
    private readonly IDisposable? _log;

    private LiteLlmProxy(Process? process, string url, IDisposable? log = null)
    {
        _process = process;
        Url = url;
        _log = log;
    }

    public string Url { get; }

    public bool StartedByTool => _process is not null;

    public static async Task<bool> IsAliveAsync(string proxyUrl, CancellationToken cancellationToken)
    {
        using var http = new HttpClient { Timeout = TimeSpan.FromSeconds(5) };
        foreach (var path in new[] { "/health/liveliness", "/health/readiness" })
        {
            try
            {
                using var response = await http.GetAsync(proxyUrl.TrimEnd('/') + path, cancellationToken).ConfigureAwait(false);
                if (response.IsSuccessStatusCode)
                {
                    return true;
                }
            }
            catch (Exception ex) when (ex is HttpRequestException or TaskCanceledException)
            {
            }
        }

        return false;
    }

    public static async Task<LiteLlmProxy> EnsureRunningAsync(string arrowRoot, string pilotToolDir, MachineConfig machine, CancellationToken cancellationToken)
    {
        var url = machine.Llm.ProxyUrl;
        if (await IsAliveAsync(url, cancellationToken).ConfigureAwait(false))
        {
            return new LiteLlmProxy(null, url);
        }

        if (machine.Llm.ProxyMode != "auto_start")
        {
            throw new ArrowException("LLM_PROXY_DOWN", $"LiteLLM proxy không chạy ở {url} (proxy_mode=external). Bật proxy rồi chạy lại.");
        }

        var executable = ToolProbe.LocalLiteLlmPath(arrowRoot);
        if (!File.Exists(executable))
        {
            executable = Processes.ProcessRunner.FindOnPath("litellm") ?? throw new ArrowException("LLM_PROXY_MISSING", "Chưa cài LiteLLM: chạy sh install/install-litellm.sh");
        }

        var config = Path.Combine(pilotToolDir, machine.Llm.ProxyConfigFile);
        var uri = new Uri(url);
        var logDir = Path.Combine(pilotToolDir, "logs");
        Directory.CreateDirectory(logDir);
        var logPath = Path.Combine(logDir, $"litellm-{DateTime.Now:yyyyMMdd}.log");
        var start = new ProcessStartInfo(executable)
        {
            UseShellExecute = false,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            WorkingDirectory = pilotToolDir,
        };
        foreach (var argument in new[] { "--config", config, "--host", uri.Host, "--port", uri.Port.ToString(System.Globalization.CultureInfo.InvariantCulture) })
        {
            start.ArgumentList.Add(argument);
        }

        start.Environment["PYTHONIOENCODING"] = "utf-8";
        start.Environment["PYTHONUTF8"] = "1";
        var process = Process.Start(start) ?? throw new ArrowException("LLM_PROXY_START_FAILED", "Không khởi động được LiteLLM proxy");
        var logStream = new FileStream(logPath, FileMode.Append, FileAccess.Write, FileShare.ReadWrite);
        var log = new StreamWriter(logStream, System.Text.Encoding.UTF8) { AutoFlush = true };
        void Write(string? line)
        {
            if (line is not null)
            {
                lock (log)
                {
                    log.WriteLine(ToolLog.Redact(line));
                }
            }
        }

        process.OutputDataReceived += (_, e) => Write(e.Data);
        process.ErrorDataReceived += (_, e) => Write(e.Data);
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
        ToolLog.Info($"LiteLLM: bật proxy {url} (pid {process.Id}), log {logPath}");

        var deadline = DateTime.UtcNow.AddSeconds(120);
        while (DateTime.UtcNow < deadline)
        {
            if (process.HasExited)
            {
                log.Dispose();
                throw new ArrowException("LLM_PROXY_START_FAILED", $"LiteLLM proxy thoát (mã {process.ExitCode}); xem {logPath}");
            }

            if (await IsAliveAsync(url, cancellationToken).ConfigureAwait(false))
            {
                return new LiteLlmProxy(process, url, log);
            }

            await Task.Delay(1000, cancellationToken).ConfigureAwait(false);
        }

        log.Dispose();
        process.Kill(entireProcessTree: true);
        throw new ArrowException("LLM_PROXY_START_FAILED", $"LiteLLM proxy không sẵn sàng sau 120 giây; xem {logPath}");
    }

    public ValueTask DisposeAsync()
    {
        if (_process is { HasExited: false })
        {
            try
            {
                _process.Kill(entireProcessTree: true);
                ToolLog.Info("LiteLLM: đã tắt proxy do tool bật");
            }
            catch (InvalidOperationException)
            {
            }
        }

        _process?.Dispose();
        _log?.Dispose();
        return ValueTask.CompletedTask;
    }
}
