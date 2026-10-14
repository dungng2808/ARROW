using System.Text.RegularExpressions;

namespace Arrow.Service.Block2;

/// <summary>Kết quả tách mã: mã (null khi không tìm thấy class), các thao tác chuẩn hóa đã làm.</summary>
public sealed record ExtractedCode(string? Code, IReadOnlyList<string> Normalization);

/// <summary>
/// Spec 02 Mục 4.5 bước 1: lấy mã Java từ nội dung response. Mặc định cả response là mã (output contract: mã thuần, không fence);
/// có fence thì lấy khối đầu tiên có khai báo class (<c>fence removed</c>); cắt chữ thừa trước dòng <c>package</c>/<c>import</c>/khai báo
/// class đầu tiên và sau dấu <c>}</c> đóng class (<c>prose trimmed</c>). Code thuần, không parse (bước 2 do java-helper làm).
/// </summary>
public static partial class CodeExtractor
{
    public const string FenceRemoved = "fence removed";
    public const string ProseTrimmed = "prose trimmed";

    [GeneratedRegex(@"^[ \t]*```[^\n]*\n(?<body>.*?)^[ \t]*```[ \t]*$", RegexOptions.Multiline | RegexOptions.Singleline)]
    private static partial Regex Fence();

    /// <summary>Khai báo kiểu top-level: <c>public final class X</c>, <c>class X</c>, <c>interface</c>, <c>enum</c>, <c>record</c>.</summary>
    [GeneratedRegex(@"^\s*(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed|strictfp)\s+)*(?:class|interface|enum|record|@interface)\s+[\p{L}_$][\p{L}\p{N}_$]*")]
    private static partial Regex TypeDeclaration();

    [GeneratedRegex(@"^\s*(?:package|import)\s+[\p{L}_$]")]
    private static partial Regex PackageOrImport();

    /// <summary>Annotation đứng một mình trên một dòng (ví dụ <c>@RunWith(...)</c> ngay trước class).</summary>
    [GeneratedRegex(@"^\s*@[\p{L}_$][\p{L}\p{N}_$.]*(\(.*\))?\s*$")]
    private static partial Regex AnnotationLine();

    [GeneratedRegex(@"\bclass\s+[\p{L}_$]|\binterface\s+[\p{L}_$]|\benum\s+[\p{L}_$]|\brecord\s+[\p{L}_$]")]
    private static partial Regex AnyClassKeyword();

    public static ExtractedCode Extract(string? response)
    {
        var text = (response ?? "").Replace("\r\n", "\n");
        var actions = new List<string>();

        if (text.Contains("```", StringComparison.Ordinal))
        {
            var block = Fence().Matches(text).Select(m => m.Groups["body"].Value).FirstOrDefault(b => AnyClassKeyword().IsMatch(b));
            if (block is not null)
            {
                text = block;
                actions.Add(FenceRemoved);
            }
            else
            {
                // Fence mở mà không đóng (ví dụ output bị cắt): bỏ dòng fence, xét phần còn lại.
                var stripped = string.Join('\n', text.Split('\n').Where(l => !l.TrimStart().StartsWith("```", StringComparison.Ordinal)));
                if (stripped != text && AnyClassKeyword().IsMatch(stripped))
                {
                    text = stripped;
                    actions.Add(FenceRemoved);
                }
            }
        }

        var lines = text.Split('\n');
        var start = Array.FindIndex(lines, l => PackageOrImport().IsMatch(l));
        var firstType = Array.FindIndex(lines, l => TypeDeclaration().IsMatch(l));
        if (firstType < 0)
        {
            return new ExtractedCode(null, actions);
        }

        if (start < 0 || start > firstType)
        {
            // Không có package/import trước class: bắt đầu ở class, kèm các dòng annotation liền trước nó.
            start = firstType;
            while (start > 0 && AnnotationLine().IsMatch(lines[start - 1]))
            {
                start--;
            }
        }

        var end = Array.FindLastIndex(lines, l => l.Contains('}'));
        if (end < firstType)
        {
            // Không có dấu } nào sau khai báo class (mã bị cắt): giữ tới hết, để bước parse báo lỗi.
            end = lines.Length - 1;
        }

        var before = string.Join('\n', lines.Take(start)).Trim();
        var lastLine = lines[end];
        var closing = lastLine.LastIndexOf('}');
        var tailOnLine = closing >= 0 ? lastLine[(closing + 1)..] : "";
        var after = (tailOnLine + "\n" + string.Join('\n', lines.Skip(end + 1))).Trim();
        if (before.Length > 0 || after.Length > 0)
        {
            actions.Add(ProseTrimmed);
        }

        var kept = lines.Skip(start).Take(end - start + 1).ToArray();
        if (closing >= 0 && tailOnLine.Trim().Length > 0)
        {
            kept[^1] = lastLine[..(closing + 1)];
        }

        return new ExtractedCode(string.Join('\n', kept).Trim('\n') + "\n", actions);
    }

    /// <summary>Spec 02 Mục 4.5 bước 3: bỏ dòng <c>import</c> lặp lại y hệt (giữ lần đầu).</summary>
    public static (string Code, IReadOnlyList<string> Actions) RemoveDuplicateImports(string code)
    {
        var seen = new HashSet<string>(StringComparer.Ordinal);
        var actions = new List<string>();
        var kept = new List<string>();
        foreach (var line in code.Replace("\r\n", "\n").Split('\n'))
        {
            var trimmed = line.Trim();
            if (trimmed.StartsWith("import ", StringComparison.Ordinal) && !seen.Add(Regex.Replace(trimmed, @"\s+", " ")))
            {
                actions.Add($"import: removed duplicate {trimmed.TrimEnd(';')[7..].Trim()}");
                continue;
            }

            kept.Add(line);
        }

        return (string.Join('\n', kept), actions);
    }
}
