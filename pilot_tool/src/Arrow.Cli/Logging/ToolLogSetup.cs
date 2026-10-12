using Arrow.Core.Util;
using Serilog;
using Serilog.Core;
using Serilog.Events;

namespace Arrow.Cli.Logging;

/// <summary>
/// Nối <see cref="ToolLog"/> với Serilog (specs/08 §2):
/// <c>pilot_tool/logs/arrow-&lt;ngày&gt;.log</c> (giữ 14 file, tối đa 50 MB/file) và, khi đang có run,
/// thêm <c>runs/&lt;run_id&gt;/tool.log</c>. Nhiều tiến trình arrow ghi cùng lúc được (shared).
/// </summary>
public static class ToolLogSetup
{
    private const string Template = "{Timestamp:yyyy-MM-ddTHH:mm:ss.fffzzz} [{Level:u3}] {Message:lj}{NewLine}{Exception}";
    private static Logger? _global;
    private static Logger? _run;
    private static readonly object Lock = new();

    public static void Start(string logDir, bool verbose)
    {
        Directory.CreateDirectory(logDir);
        _global = new LoggerConfiguration()
            .MinimumLevel.Is(verbose ? LogEventLevel.Debug : LogEventLevel.Information)
            .WriteTo.File(Path.Combine(logDir, "arrow-.log"),
                rollingInterval: RollingInterval.Day,
                retainedFileCountLimit: 14,
                fileSizeLimitBytes: 50L * 1024 * 1024,
                rollOnFileSizeLimit: true,
                shared: true,
                outputTemplate: Template)
            .CreateLogger();
        ToolLog.Configure(Write);
    }

    /// <summary>Ghi thêm vào <c>runs/&lt;run_id&gt;/tool.log</c> cho tới khi dispose (không xoay vòng).</summary>
    public static IDisposable AttachRun(string toolLogPath, bool verbose = false)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(toolLogPath)!);
        var logger = new LoggerConfiguration()
            .MinimumLevel.Is(verbose ? LogEventLevel.Debug : LogEventLevel.Information)
            .WriteTo.File(toolLogPath, shared: true, outputTemplate: Template)
            .CreateLogger();
        lock (Lock)
        {
            _run = logger;
        }

        return new Detach(logger);
    }

    public static void Stop()
    {
        ToolLog.Configure(null);
        lock (Lock)
        {
            _run?.Dispose();
            _run = null;
        }

        _global?.Dispose();
        _global = null;
    }

    private static void Write(ToolLogLevel level, string message, Exception? exception)
    {
        var serilogLevel = level switch
        {
            ToolLogLevel.Debug => LogEventLevel.Debug,
            ToolLogLevel.Info => LogEventLevel.Information,
            ToolLogLevel.Warn => LogEventLevel.Warning,
            _ => LogEventLevel.Error,
        };
        // {Message:lj} sẽ hiểu dấu ngoặc nhọn là tham số; escape để in nguyên văn.
        var template = message.Replace("{", "{{").Replace("}", "}}");
        _global?.Write(serilogLevel, exception, template);
        lock (Lock)
        {
            _run?.Write(serilogLevel, exception, template);
        }
    }

    private sealed class Detach(Logger logger) : IDisposable
    {
        public void Dispose()
        {
            lock (Lock)
            {
                if (ReferenceEquals(_run, logger))
                {
                    _run = null;
                }
            }

            logger.Dispose();
        }
    }
}
