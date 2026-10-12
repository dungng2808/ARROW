using System.Globalization;
using System.Text;

namespace Arrow.Core.Config;

/// <summary>Một vấn đề của đường dẫn: phần nào sai, sai gì, hậu quả. <see cref="Blocking"/>: chặn chạy (doctor ✘).</summary>
public sealed record PathIssue(string Segment, string Problem, string Effect, bool Blocking);

/// <summary>Kết quả chẩn đoán kèm các bước làm tiếp, để setup/doctor/run hiển thị giống nhau.</summary>
public sealed record PathAdvice(string Path, IReadOnlyList<PathIssue> Issues, IReadOnlyList<string> Steps)
{
    public bool Blocking => Issues.Any(i => i.Blocking);
    public bool Clean => Issues.Count == 0;

    /// <summary>Văn bản nhiều dòng (cho thông báo lỗi của lệnh run và log).</summary>
    public string Describe()
    {
        var text = new StringBuilder();
        foreach (var issue in Issues)
        {
            text.Append("- ").Append(issue.Problem).Append(". ").AppendLine(issue.Effect);
        }

        text.AppendLine("Cách sửa:");
        for (var i = 0; i < Steps.Count; i++)
        {
            text.Append(i + 1).Append(". ").AppendLine(Steps[i]);
        }

        return text.ToString().TrimEnd();
    }
}

/// <summary>
/// Kiểm tra thư mục repository/workspace (specs/02 §3b): dấu cách, chữ có dấu, ký tự đặc biệt làm hỏng URL/classpath,
/// đường dẫn dài trên Windows. Mỗi vấn đề chỉ rõ thư mục nào sai và cách sửa.
/// </summary>
public static class PathDiagnostics
{
    /// <summary>Ký tự làm hỏng đường dẫn khi Java/Maven/Gradle đổi sang URL, classpath hoặc chạy qua script.</summary>
    private static readonly Dictionary<char, string> SpecialChars = new()
    {
        ['%'] = "Java giải mã URL sẽ đọc %xx thành ký tự khác",
        ['#'] = "URL bị cắt tại dấu #",
        ['!'] = "Java coi ! là dấu tách trong đường dẫn jar",
        [';'] = "là dấu tách classpath trên Windows",
        ['&'] = "làm hỏng mvnw.cmd/gradlew.bat trên Windows",
        ['^'] = "làm hỏng mvnw.cmd/gradlew.bat trên Windows",
        ['$'] = "Maven/Gradle và shell hiểu là biến",
        ['\''] = "làm hỏng lệnh trong script build",
        ['"'] = "làm hỏng lệnh trong script build",
        ['`'] = "làm hỏng lệnh trong script build",
    };

    /// <summary>Độ dài đường dẫn gốc trên Windows từ đó nên cảnh báo (repository có thư mục sâu dễ vượt 260 ký tự).</summary>
    public const int WindowsLengthWarning = 60;

    public static PathAdvice Diagnose(string path, string arrowRoot, bool windows, string home)
    {
        var issues = new List<PathIssue>();
        var segments = path.Split(['/', '\\'], StringSplitOptions.RemoveEmptyEntries);
        foreach (var segment in segments.Distinct())
        {
            if (segment.Any(char.IsWhiteSpace))
            {
                issues.Add(new PathIssue(segment, $"Thư mục \"{segment}\" có dấu cách",
                    "Java đổi dấu cách thành %20 khi đọc file qua URL, nhiều test sẽ báo không tìm thấy file (đã gặp ở doraemon: \"Ki 9\" thành \"Ki%209\")", true));
            }

            var nonAscii = segment.Where(c => c > 127).Distinct().ToArray();
            if (nonAscii.Length > 0)
            {
                issues.Add(new PathIssue(segment, $"Thư mục \"{segment}\" có chữ có dấu hoặc ký tự ngoài ASCII ({string.Join(' ', nonAscii)})",
                    "Java mã hóa thành %C3%… trong URL, và một số plugin Maven/Gradle cũ đọc sai mã hóa", true));
            }

            foreach (var c in segment.Where(SpecialChars.ContainsKey).Distinct())
            {
                issues.Add(new PathIssue(segment, $"Thư mục \"{segment}\" có ký tự '{c}'", SpecialChars[c], true));
            }
        }

        if (windows && path.Length > WindowsLengthWarning)
        {
            issues.Add(new PathIssue(path, $"Đường dẫn dài {path.Length} ký tự",
                "Repository có thư mục sâu dễ vượt giới hạn 260 ký tự của Windows nếu chưa bật LongPathsEnabled", false));
        }

        return new PathAdvice(path, issues, issues.Count == 0 ? [] : Steps(path, issues, arrowRoot, windows, home));
    }

    private static List<string> Steps(string path, IReadOnlyList<PathIssue> issues, string arrowRoot, bool windows, string home)
    {
        var steps = new List<string>();
        var badSegments = issues.Where(i => i.Blocking).Select(i => i.Segment).Distinct().ToList();
        var arrowSegments = arrowRoot.Split(['/', '\\'], StringSplitOptions.RemoveEmptyEntries);
        var inArrowPath = badSegments.Count > 0 && badSegments.All(arrowSegments.Contains);
        var alternative = Default(windows, home);

        if (inArrowPath)
        {
            var renames = string.Join(", ", badSegments.Select(s => $"\"{s}\" thành \"{SuggestName(s)}\""));
            steps.Add($"Cách A (giữ thư mục repo trong ARROW): đóng mọi terminal, IDE đang mở trong thư mục ARROW, rồi đổi tên {renames}.");
            steps.Add(windows
                ? @"Sau khi đổi tên, mở terminal mới trong ARROW\pilot_tool và chạy: powershell -ExecutionPolicy Bypass -File install\install-arrow.ps1, rồi powershell -ExecutionPolicy Bypass -File install\install-litellm.ps1 (môi trường Python ghi cứng đường dẫn cũ)."
                : "Sau khi đổi tên, mở terminal mới trong ARROW/pilot_tool và chạy: sh install/install-arrow.sh, rồi sh install/install-litellm.sh (môi trường Python ghi cứng đường dẫn cũ).");
            steps.Add("Chạy arrow setup: tool tự cập nhật đường dẫn ARROW và JDK; ở bước 3 nhấn Enter giữ thư mục repo trong ARROW. Cuối cùng chạy arrow doctor, phải ra READY.");
            steps.Add($"Cách B (không đổi tên): chạy arrow setup, ở bước 3 chọn thư mục ngoài ARROW, ví dụ {alternative}.");
        }
        else if (badSegments.Count > 0)
        {
            steps.Add($"Chạy arrow setup, ở bước 3 chọn thư mục không có dấu cách, chữ có dấu hay ký tự đặc biệt, ví dụ {alternative}.");
            steps.Add("Chạy arrow doctor, phải ra READY rồi mới chạy arrow run.");
        }

        if (issues.Any(i => !i.Blocking))
        {
            steps.Add(windows
                ? @"Đường dẫn dài: bật LongPathsEnabled (xem mục Windows LongPathsEnabled của doctor) hoặc chọn thư mục ngắn như C:\aw ở bước 3 của arrow setup."
                : "Chọn thư mục ngắn hơn ở bước 3 của arrow setup.");
        }

        return steps;
    }

    /// <summary>Tên gợi ý khi đổi tên: bỏ dấu tiếng Việt, bỏ dấu cách và ký tự đặc biệt. "Ki 9" → "Ki9", "Hán" → "Han".</summary>
    public static string SuggestName(string segment)
    {
        var decomposed = segment.Replace('đ', 'd').Replace('Đ', 'D').Normalize(NormalizationForm.FormD);
        var builder = new StringBuilder();
        foreach (var c in decomposed)
        {
            if (CharUnicodeInfo.GetUnicodeCategory(c) == UnicodeCategory.NonSpacingMark || char.IsWhiteSpace(c) || SpecialChars.ContainsKey(c) || c > 127)
            {
                continue;
            }

            builder.Append(c);
        }

        return builder.Length == 0 ? "arrow" : builder.ToString();
    }

    /// <summary>Thư mục dự phòng ngoài ARROW: <c>~/arrow-repo</c> hoặc <c>C:\arrow-repo</c> (tên người dùng Windows có thể có dấu).</summary>
    public static string Default(bool windows, string home) => windows ? @"C:\arrow-repo" : System.IO.Path.Combine(home, "arrow-repo");
}
