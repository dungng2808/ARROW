namespace Arrow.Core.Config;

/// <summary>
/// Quy tắc thư mục repository/workspace (chốt 13/10/2026, specs/02 §3b). Chi tiết từng lỗi và cách sửa: <see cref="PathDiagnostics"/>.
/// </summary>
public static class WorkDirPolicy
{
    /// <summary>Không có vấn đề chặn chạy (dấu cách, chữ có dấu, ký tự đặc biệt).</summary>
    public static bool IsClean(string path) =>
        !string.IsNullOrEmpty(path) && !PathDiagnostics.Diagnose(path, "", windows: false, home: "").Blocking;

    /// <summary>Thư mục dự phòng ngoài ARROW: <c>~/arrow-repo</c> hoặc <c>C:\arrow-repo</c>.</summary>
    public static string Default(bool windows, string home) => PathDiagnostics.Default(windows, home);

    /// <summary>Đổi <c>~</c> đầu đường dẫn thành thư mục nhà.</summary>
    public static string ExpandHome(string path, string home) =>
        path == "~" ? home : path.StartsWith("~/", StringComparison.Ordinal) || path.StartsWith(@"~\", StringComparison.Ordinal) ? Path.Combine(home, path[2..]) : path;
}
