using System.Diagnostics;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Common;
using Arrow.Models.Config;

namespace Arrow.Service.External;

/// <summary>Bật/tắt proxy LiteLLM (spec 02 Mục 4.3 bước 2, spec 07 Mục 4.3).</summary>
public interface ILiteLlmProxy : IAsyncDisposable
{
    /// <summary><c>external</c>: chỉ kiểm tra proxy đang chạy; <c>auto_start</c>: chưa chạy thì bật và chờ sẵn sàng.</summary>
    Task EnsureRunningAsync(CancellationToken cancellationToken);
}

/// <summary>
/// Proxy LiteLLM của máy: kiểm tra <c>&lt;proxy_url&gt;/health/liveliness</c>; với <c>auto_start</c> thì chạy
/// <c>litellm --config &lt;file&gt; --host 127.0.0.1 --port &lt;port&gt;</c> (tìm trong <c>ARROW/litellm</c> hoặc PATH) và tắt khi run xong.
/// </summary>
public sealed class LiteLlmProxy(LlmMachineConfig config, FrameworkPaths paths, ILogRepository log, HttpClient? http = null) : ILiteLlmProxy
{
    private readonly HttpClient _http = http ?? new HttpClient { Timeout = TimeSpan.FromSeconds(5) };
    private Process? _process;

    public async Task EnsureRunningAsync(CancellationToken cancellationToken)
    {
        if (await AliveAsync(cancellationToken).ConfigureAwait(false))
        {
            return;
        }

        if (config.ProxyMode != "auto_start")
        {
            throw new ArrowException(ErrorCodes.B2LlmConfig, $"Proxy LiteLLM không trả lời ở {config.ProxyUrl} (proxy_mode = {config.ProxyMode}); hãy bật proxy rồi chạy tiếp");
        }

        var executable = FindExecutable()
                         ?? throw new ArrowException(ErrorCodes.B2LlmConfig, "Không tìm thấy lệnh litellm (ARROW/litellm hoặc PATH); cài LiteLLM hoặc đặt proxy_mode = external");
        var configFile = paths.Resolve(config.ProxyConfigFile);
        if (!File.Exists(configFile))
        {
            throw new ArrowException(ErrorCodes.B2LlmConfig, $"Không có {configFile}; khai báo model và khóa bằng arrow models");
        }

        var uri = new Uri(config.ProxyUrl);
        var start = new ProcessStartInfo(executable) { UseShellExecute = false, RedirectStandardOutput = true, RedirectStandardError = true, CreateNoWindow = true };
        foreach (var argument in new[] { "--config", configFile, "--host", uri.Host, "--port", uri.Port.ToString(System.Globalization.CultureInfo.InvariantCulture) })
        {
            start.ArgumentList.Add(argument);
        }

        _process = Process.Start(start) ?? throw new ArrowException(ErrorCodes.B2LlmConfig, "Không chạy được litellm");
        _process.OutputDataReceived += (_, _) => { };
        _process.ErrorDataReceived += (_, _) => { };
        _process.BeginOutputReadLine();
        _process.BeginErrorReadLine();
        log.Info($"LiteLLM: bật proxy (pid {_process.Id}) ở {uri.Host}:{uri.Port}");
        for (var i = 0; i < 60; i++)
        {
            await Task.Delay(TimeSpan.FromSeconds(1), cancellationToken).ConfigureAwait(false);
            if (await AliveAsync(cancellationToken).ConfigureAwait(false))
            {
                return;
            }

            if (_process.HasExited)
            {
                break;
            }
        }

        throw new ArrowException(ErrorCodes.B2LlmConfig, "Proxy LiteLLM không sẵn sàng sau 60 s");
    }

    private async Task<bool> AliveAsync(CancellationToken cancellationToken)
    {
        try
        {
            using var response = await _http.GetAsync(config.ProxyUrl.TrimEnd('/') + "/health/liveliness", cancellationToken).ConfigureAwait(false);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex) when (ex is HttpRequestException or TaskCanceledException)
        {
            return false;
        }
    }

    private string? FindExecutable()
    {
        var windows = OperatingSystem.IsWindows();
        var venv = Path.Combine(paths.ArrowRoot, "litellm", ".venv", windows ? "Scripts" : "bin", windows ? "litellm.exe" : "litellm");
        return File.Exists(venv) ? venv : ToolLocator.FindOnPath(windows ? "litellm.exe" : "litellm");
    }

    public ValueTask DisposeAsync()
    {
        if (_process is { HasExited: false } process)
        {
            process.Kill(entireProcessTree: true);
            process.WaitForExit(5_000);
            log.Info("LiteLLM: tắt proxy");
        }

        _process?.Dispose();
        if (http is null)
        {
            _http.Dispose();
        }

        return ValueTask.CompletedTask;
    }
}
