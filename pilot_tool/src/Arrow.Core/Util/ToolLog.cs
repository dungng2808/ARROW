using System.Text.RegularExpressions;

namespace Arrow.Core.Util;

public enum ToolLogLevel
{
    Debug,
    Info,
    Warn,
    Error,
}

/// <summary>
/// Log của tool (specs/08 §2). Core và Infrastructure chỉ gọi lớp này; Cli nối nó với Serilog.
/// Mọi thông điệp đi qua <see cref="Redact"/> trước khi ghi, nên khóa API không bao giờ vào log.
/// </summary>
public static partial class ToolLog
{
    private static Action<ToolLogLevel, string, Exception?>? _sink;

    /// <summary>Nối nơi ghi log (Cli gọi một lần lúc khởi động). Không nối thì log bị bỏ qua.</summary>
    public static void Configure(Action<ToolLogLevel, string, Exception?>? sink) => _sink = sink;

    public static void Debug(string message) => Write(ToolLogLevel.Debug, message, null);

    public static void Info(string message) => Write(ToolLogLevel.Info, message, null);

    public static void Warn(string message, Exception? exception = null) => Write(ToolLogLevel.Warn, message, exception);

    public static void Error(string message, Exception? exception = null) => Write(ToolLogLevel.Error, message, exception);

    private static void Write(ToolLogLevel level, string message, Exception? exception)
    {
        var sink = _sink;
        if (sink is null)
        {
            return;
        }

        try
        {
            sink(level, Redact(message), exception is null ? null : new RedactedException(exception));
        }
        catch (Exception)
        {
            // Lỗi ghi log không được làm hỏng lần chạy.
        }
    }

    /// <summary>Che khóa API: mọi chuỗi dạng <c>sk-…</c> (từ 8 ký tự) thành <c>sk-***</c>.</summary>
    public static string Redact(string text) => string.IsNullOrEmpty(text) ? text : SecretRegex().Replace(text, "sk-***");

    [GeneratedRegex(@"sk-[A-Za-z0-9_\-]{8,}")]
    private static partial Regex SecretRegex();

    /// <summary>Bọc exception để thông điệp và stack trace cũng được che khóa.</summary>
    private sealed class RedactedException(Exception inner) : Exception(Redact(inner.Message))
    {
        public override string? StackTrace => Redact(inner.StackTrace ?? "");

        public override string ToString() => Redact(inner.ToString());
    }
}
