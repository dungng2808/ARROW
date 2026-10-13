using System.Text.RegularExpressions;
using Arrow.Models.Block3;

namespace Arrow.Service.Block3;

/// <summary>
/// Lỗi javac trong log Maven/Gradle (spec 03 Mục 3.2). Nhận mọi dạng đường dẫn: <c>/Users/a/X.java:[13,6]</c> (macOS),
/// <c>/D:/a/X.java:[13,6]</c> (Maven trên Windows), <c>D:\a\X.java:13: error:</c> (Gradle trên Windows).
/// </summary>
public static partial class JavacErrorParser
{
    [GeneratedRegex(@"^(?:\[ERROR\]\s+)?(?<file>(?:/?[A-Za-z]:)?[^:\[\]\r\n]*?\.java):(?:\[(?<line>\d+),(?<col>\d+)\]|(?<line2>\d+):\s*error:)\s*(?<msg>.*)$")]
    private static partial Regex ErrorLine();

    [GeneratedRegex(@"^(?:\[ERROR\])?\s*(?<key>symbol|location)\s*:\s*(?<value>.*)$")]
    private static partial Regex DetailLine();

    /// <summary>Loại lỗi javac cố định (spec 03 Mục 5), theo thứ tự so khớp; "cannot be applied" xét riêng (method hay constructor).</summary>
    private static readonly (string Kind, string[] Markers)[] Kinds =
    [
        ("cannot find symbol", ["cannot find symbol"]),
        ("incompatible types", ["incompatible types", "bad operand types"]),
        ("unreported exception", ["unreported exception"]),
        ("package does not exist", ["does not exist"]),
        ("has private access", ["has private access"]),
        ("is not public", ["is not public in"]),
        ("cannot be referenced from a static context", ["cannot be referenced from a static context"]),
        ("already defined", ["is already defined"]),
        ("missing return statement", ["missing return statement"]),
        ("unreachable statement", ["unreachable statement"]),
        ("class is abstract", ["is abstract; cannot be instantiated"]),
        ("does not override", ["does not override"]),
        ("cannot be accessed", ["cannot be accessed from outside package", "cannot access"]),
    ];

    /// <param name="generatedFileName">Tên file test sinh ra (ví dụ <c>OrderService_ArrowTest.java</c>), để đánh dấu lỗi nằm trong file đó.</param>
    public static IReadOnlyList<CompileError> Parse(string log, string? generatedFileName)
    {
        var lines = log.Replace("\r\n", "\n").Split('\n');
        var errors = new List<CompileError>();
        var seen = new HashSet<string>(StringComparer.Ordinal);
        for (var i = 0; i < lines.Length; i++)
        {
            var match = ErrorLine().Match(lines[i].TrimEnd());
            if (!match.Success)
            {
                continue;
            }

            var message = match.Groups["msg"].Value.Trim();
            if (message.Contains("AssertionError", StringComparison.Ordinal) || message.StartsWith("warning:", StringComparison.Ordinal))
            {
                continue;
            }

            string? symbol = null, location = null;
            for (var j = i + 1; j < lines.Length && j <= i + 6 && !ErrorLine().IsMatch(lines[j].TrimEnd()); j++)
            {
                var detail = DetailLine().Match(lines[j].Trim());
                if (detail.Success)
                {
                    if (detail.Groups["key"].Value == "symbol")
                    {
                        symbol ??= detail.Groups["value"].Value.Trim();
                    }
                    else
                    {
                        location ??= detail.Groups["value"].Value.Trim();
                    }
                }
            }

            var file = match.Groups["file"].Value.Trim();
            var line = int.Parse(match.Groups["line"].Success ? match.Groups["line"].Value : match.Groups["line2"].Value, System.Globalization.CultureInfo.InvariantCulture);
            int? column = match.Groups["col"].Success ? int.Parse(match.Groups["col"].Value, System.Globalization.CultureInfo.InvariantCulture) : null;
            if (!seen.Add($"{FileName(file)}|{line}|{column}|{message}"))
            {
                continue; // Maven in danh sách lỗi hai lần (COMPILATION ERROR và Failed to execute goal)
            }

            var (symbolKind, symbolName) = SplitSymbol(symbol);
            errors.Add(new CompileError
            {
                File = file,
                InGeneratedFile = generatedFileName is not null && string.Equals(FileName(file), generatedFileName, StringComparison.Ordinal),
                Line = line,
                Column = column,
                Message = message,
                Kind = Kind(message),
                SymbolKind = symbolKind,
                Symbol = symbolName,
                Location = location,
            });
        }

        return errors;
    }

    /// <summary>Thông điệp javac → một trong các loại cố định, không khớp thì <c>other</c>.</summary>
    public static string Kind(string message)
    {
        var text = message.ToLowerInvariant();
        if (text.Contains("cannot be applied to given types", StringComparison.Ordinal) || text.Contains("no suitable", StringComparison.Ordinal))
        {
            return text.StartsWith("constructor", StringComparison.Ordinal) || text.Contains("no suitable constructor", StringComparison.Ordinal)
                ? "constructor cannot be applied"
                : "method cannot be applied";
        }

        return Kinds.FirstOrDefault(k => k.Markers.Any(m => text.Contains(m, StringComparison.Ordinal))).Kind ?? "other";
    }

    /// <summary><c>method getTotal(long)</c> → (<c>method</c>, <c>getTotal(long)</c>).</summary>
    public static (string? Kind, string? Name) SplitSymbol(string? symbol)
    {
        if (string.IsNullOrWhiteSpace(symbol))
        {
            return (null, null);
        }

        var space = symbol.IndexOf(' ');
        return space < 0 ? (null, symbol) : (symbol[..space], symbol[(space + 1)..].Trim());
    }

    public static string FileName(string path)
    {
        var normalized = path.Replace('\\', '/');
        return normalized[(normalized.LastIndexOf('/') + 1)..];
    }
}
