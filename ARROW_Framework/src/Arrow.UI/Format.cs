using System.Globalization;
using Arrow.Models.Common;
using Arrow.Models.Run;

namespace Arrow.UI;

/// <summary>Định dạng dùng chung của các màn hình (chỉ chuyển dữ liệu thành chữ, không xử lý nghiệp vụ).</summary>
public static class Format
{
    public static string State(VerificationState state) => state.ToString().ToUpperInvariant();

    public static string Outcome(OutcomeKind outcome) => outcome.ToString().ToUpperInvariant();

    public static string RunState(RunState state) => state switch
    {
        Models.Run.RunState.Running => "ĐANG CHẠY",
        Models.Run.RunState.Paused => "TẠM DỪNG",
        Models.Run.RunState.Stopping => "ĐANG DỪNG",
        Models.Run.RunState.Stopped => "ĐÃ DỪNG",
        Models.Run.RunState.Done => "XONG",
        _ => "LỖI",
    };

    /// <summary>Tỷ lệ phần trăm làm tròn: <c>"7"/"50"</c> → <c>14%</c>.</summary>
    public static string Percent(string numerator, string denominator) =>
        int.TryParse(numerator, out var n) && int.TryParse(denominator, out var d) && d > 0 ? (100.0 * n / d).ToString("0", CultureInfo.InvariantCulture) + "%" : "-";

    public static string Short(string taskId, int length = 12) => taskId.Length <= length ? taskId : taskId[..length] + "…";

    /// <summary>Một dòng sự kiện: giờ, khối, task, mức, thông điệp (spec 06 Mục 7).</summary>
    public static string Event(RunEvent value)
    {
        var mark = value.Level switch { EventLevel.Warn => "[!] ", EventLevel.Error => "[X] ", _ => "" };
        var block = value.Block is >= 1 and <= 4 ? $"[K{value.Block}] " : "[RUN] ";
        return $"{value.At.ToLocalTime():HH:mm:ss} {block}{(value.TaskId is null ? "" : Short(value.TaskId) + " ")}{mark}{value.Message}";
    }

    /// <summary>Bảng tổng của run (lệnh <c>t</c> của S5, màn hình S6).</summary>
    public static void Summary(ConsoleOutput output, RunStatus status)
    {
        var rows = status.Blocks.Select(b => (IReadOnlyList<string>)
        [
            $"K{b.Block}",
            $"{b.Done} / {b.Total}",
            b.Block switch
            {
                1 => $"EXCLUDED {b.Excluded}" + (b.Infra > 0 ? $" · INFRA {b.Infra}" : ""),
                2 => $"không hợp lệ {status.InvalidOutputs}" + (b.Infra > 0 ? $" · INFRA {b.Infra}" : ""),
                3 => ChainText(status) + (b.Infra > 0 ? $" · INFRA {b.Infra}" : ""),
                _ => b.Infra > 0 ? $"INFRA {b.Infra}" : "",
            },
        ]).ToList();
        output.Table(["Khối", "Xong / Tổng", "Ghi chú"], rows);
        output.Line($"Đang chạy: {status.Running.Count} job" + (status.Running.Count > 0 ? " (" + string.Join(", ", status.Running.Take(4)) + (status.Running.Count > 4 ? ", …" : "") + ")" : ""));
        if (status.C0States.Count > 0)
        {
            output.Line("c0: " + string.Join(" · ", status.C0States.OrderBy(s => Enum.Parse<VerificationState>(s.Key, true)).Select(s => $"{s.Key} {s.Value}")));
        }

        output.Line($"Lượt gọi model: {status.LlmCalls} · {status.CostUsd.ToString("0.####", CultureInfo.InvariantCulture)} USD");
        if (status.PausedReason is not null)
        {
            output.Status(Mark.Fail, "TẠM DỪNG", status.PausedReason);
        }
    }

    private static string ChainText(RunStatus status)
    {
        var mechanisms = status.ChainSuccess.Keys.Where(k => !k.EndsWith("_success", StringComparison.Ordinal)).Order().ToList();
        if (mechanisms.Count == 0)
        {
            return "";
        }

        var done = mechanisms.Sum(m => status.ChainSuccess[m]);
        return $"chain xong {done} (" + string.Join(", ", mechanisms.Select(m => $"{(m == "fixed" ? "Fixed" : m == "adaptive" ? "Adaptive" : m)} {status.ChainSuccess.GetValueOrDefault(m + "_success")}")) + " thành công)";
    }

    /// <summary>Dòng tổng ngắn mỗi 30 giây (spec 06 Mục 7).</summary>
    public static string TotalLine(RunStatus status) =>
        $"{DateTime.Now:HH:mm:ss} [TỔNG] " + string.Join(" · ", status.Blocks.Select(b => $"K{b.Block} {b.Done}/{b.Total}"))
        + $" · INFRA {status.Blocks.Sum(b => b.Infra)} · {status.LlmCalls} lượt gọi · {status.CostUsd.ToString("0.##", CultureInfo.InvariantCulture)} USD";
}
