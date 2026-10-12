using System.Text;
using System.Text.RegularExpressions;

namespace Arrow.Core.Generation;

/// <summary><c>normalization.json</c> (specs/04 Mục 3).</summary>
public sealed record NormalizationResult
{
    public bool Valid { get; init; }
    public string? RejectReason { get; init; }
    public string? Code { get; init; }
    public bool HadFence { get; init; }
    public string? ClassBefore { get; init; }
    public string? ClassAfter { get; init; }
    public List<string> ImportsAdded { get; init; } = [];
    public List<string> ImportsRemoved { get; init; } = [];
    public List<string> AnnotationsChanged { get; init; } = [];
    public List<string> QualifiersChanged { get; init; } = [];
}

/// <summary>
/// Kiểm tra và chuẩn hóa output của model (specs/04 Mục 3), giống nhau cho mọi quy trình prompting và mọi vòng sửa:
/// lấy mã, từ chối output không hợp lệ, chuẩn hóa tên class, sửa import theo test framework, thêm import JDK, kiểm tra package.
/// </summary>
public static partial class OutputNormalizer
{
    public static NormalizationResult Normalize(string response, string? finishReason, string expectedPackage, string expectedClass, string cutSimpleName, string framework)
    {
        var (code, hadFence) = ExtractCode(response);
        NormalizationResult Reject(string reason) => new() { Valid = false, RejectReason = reason, HadFence = hadFence };

        if (string.Equals(finishReason, "length", StringComparison.OrdinalIgnoreCase))
        {
            return Reject("truncated (finish_reason=length)");
        }

        if (string.IsNullOrWhiteSpace(code))
        {
            return Reject("empty");
        }

        if (DiffMarker().IsMatch(code))
        {
            return Reject("diff instead of a Java file");
        }

        foreach (var marker in new[] { "pom.xml", "build.gradle", "settings.gradle", "src/main/java" })
        {
            if (code.Contains(marker, StringComparison.Ordinal))
            {
                return Reject($"non-test content ({marker})");
            }
        }

        var balance = CheckBalance(code);
        if (balance is not null)
        {
            return Reject(balance);
        }

        // Tên class (bước 3).
        var classes = TopLevelClasses(code);
        var before = classes.Count == 1 ? classes[0] : string.Join(",", classes);
        var variants = new[] { "Test", "Tests", "GeneratedTest", "LLMGeneratedTest", "_ArrowTest" }.Select(s => cutSimpleName + s).ToHashSet();
        if (classes.Count == 1 && classes[0] != expectedClass && variants.Contains(classes[0]))
        {
            code = Regex.Replace(code, $@"\b{Regex.Escape(classes[0])}\b", expectedClass);
            classes = TopLevelClasses(code);
        }

        if (classes.Count != 1 || classes[0] != expectedClass)
        {
            return Reject($"wrong class (found {(classes.Count == 0 ? "none" : string.Join(", ", classes))}, expected {expectedClass})") with { ClassBefore = before };
        }

        // Import theo framework (bước 4), import JDK (bước 5).
        var added = new List<string>();
        var removed = new List<string>();
        var annotations = new List<string>();
        var qualifiers = new List<string>();
        code = framework.ToLowerInvariant() switch
        {
            "junit5" => ToJUnit5(code, added, removed, annotations, qualifiers),
            "junit4" => ToJUnit4(code, added, removed, annotations, qualifiers),
            _ => code,
        };
        code = AddJdkImports(code, added);

        // Package (bước 6).
        var package = PackageLine().Match(code);
        var actualPackage = package.Success ? package.Groups[1].Value : "";
        if (actualPackage != expectedPackage)
        {
            return Reject($"wrong package (found '{actualPackage}', expected '{expectedPackage}')") with { ClassBefore = before, ClassAfter = expectedClass };
        }

        return new NormalizationResult
        {
            Valid = true,
            Code = code.Replace("\r\n", "\n").TrimEnd() + "\n",
            HadFence = hadFence,
            ClassBefore = before,
            ClassAfter = expectedClass,
            ImportsAdded = added,
            ImportsRemoved = removed,
            AnnotationsChanged = annotations,
            QualifiersChanged = qualifiers,
        };
    }

    // ── bước 1: lấy mã ────────────────────────────────────────────────

    [GeneratedRegex(@"```[ \t]*([\w+\-]*)[ \t]*\r?\n(.*?)```", RegexOptions.Singleline)]
    private static partial Regex Fence();

    public static (string Code, bool HadFence) ExtractCode(string response)
    {
        var blocks = Fence().Matches(response);
        if (blocks.Count == 0)
        {
            return (response.Trim(), false);
        }

        var java = blocks.FirstOrDefault(b => b.Groups[1].Value.Equals("java", StringComparison.OrdinalIgnoreCase))
                   ?? blocks.FirstOrDefault(b => b.Groups[1].Value.Length == 0)
                   ?? blocks[0];
        return (java.Groups[2].Value.Trim(), true);
    }

    [GeneratedRegex(@"^(--- |\+\+\+ |@@)", RegexOptions.Multiline)]
    private static partial Regex DiffMarker();

    [GeneratedRegex(@"^\s*package\s+([\w.]+)\s*;", RegexOptions.Multiline)]
    private static partial Regex PackageLine();

    /// <summary>Cân bằng ngoặc, bỏ qua chuỗi, ký tự, text block và comment; null nếu hợp lệ.</summary>
    public static string? CheckBalance(string code)
    {
        var stack = new Stack<char>();
        var i = 0;
        while (i < code.Length)
        {
            var c = code[i];
            if (c == '"' && string.CompareOrdinal(code, i, "\"\"\"", 0, 3) == 0)
            {
                var end = code.IndexOf("\"\"\"", i + 3, StringComparison.Ordinal);
                if (end < 0)
                {
                    return "unclosed text block";
                }

                i = end + 3;
                continue;
            }

            if (c is '"' or '\'')
            {
                var j = i + 1;
                while (j < code.Length && code[j] != c)
                {
                    if (code[j] == '\n')
                    {
                        return "unclosed string literal";
                    }

                    j += code[j] == '\\' ? 2 : 1;
                }

                if (j >= code.Length)
                {
                    return "unclosed string literal";
                }

                i = j + 1;
                continue;
            }

            if (c == '/' && i + 1 < code.Length && code[i + 1] == '/')
            {
                var end = code.IndexOf('\n', i);
                i = end < 0 ? code.Length : end;
                continue;
            }

            if (c == '/' && i + 1 < code.Length && code[i + 1] == '*')
            {
                var end = code.IndexOf("*/", i + 2, StringComparison.Ordinal);
                if (end < 0)
                {
                    return "unclosed comment";
                }

                i = end + 2;
                continue;
            }

            switch (c)
            {
                case '{' or '(' or '[':
                    stack.Push(c);
                    break;
                case '}' or ')' or ']':
                    var open = c == '}' ? '{' : c == ')' ? '(' : '[';
                    if (stack.Count == 0 || stack.Pop() != open)
                    {
                        return "unbalanced brackets";
                    }

                    break;
            }

            i++;
        }

        return stack.Count == 0 ? null : "unbalanced brackets";
    }

    /// <summary>
    /// Class cấp cao nhất của file. Ưu tiên <c>public class</c>; không có thì lấy <c>class</c> ở độ sâu 0
    /// (test JUnit 5 thường không public; quyết định chờ xác nhận ghi ở specs/00-INDEX.md).
    /// </summary>
    public static List<string> TopLevelClasses(string code)
    {
        var masked = MaskLiteralsAndComments(code);
        var result = new List<(string Name, bool Public)>();
        var depth = 0;
        for (var i = 0; i < masked.Length; i++)
        {
            var c = masked[i];
            if (c == '{')
            {
                depth++;
            }
            else if (c == '}')
            {
                depth--;
            }
            else if (depth == 0 && (i == 0 || !char.IsLetterOrDigit(masked[i - 1]) && masked[i - 1] != '_'))
            {
                var match = ClassDeclaration().Match(masked, i);
                if (match.Success && match.Index == i)
                {
                    result.Add((match.Groups[2].Value, match.Groups[1].Value.Contains("public", StringComparison.Ordinal)));
                    i += match.Length - 1;
                }
            }
        }

        var publics = result.Where(r => r.Public).Select(r => r.Name).ToList();
        return publics.Count > 0 ? publics : result.Select(r => r.Name).ToList();
    }

    [GeneratedRegex(@"((?:(?:public|final|abstract|strictfp)\s+)*)class\s+([A-Za-z_$][\w$]*)")]
    private static partial Regex ClassDeclaration();

    private static string MaskLiteralsAndComments(string code)
    {
        var builder = new StringBuilder(code.Length);
        var i = 0;
        while (i < code.Length)
        {
            var c = code[i];
            int end;
            if (c == '"' && string.CompareOrdinal(code, i, "\"\"\"", 0, 3) == 0)
            {
                end = code.IndexOf("\"\"\"", i + 3, StringComparison.Ordinal);
                end = end < 0 ? code.Length : end + 3;
            }
            else if (c is '"' or '\'')
            {
                end = i + 1;
                while (end < code.Length && code[end] != c && code[end] != '\n')
                {
                    end += code[end] == '\\' ? 2 : 1;
                }

                end = Math.Min(code.Length, end + 1);
            }
            else if (c == '/' && i + 1 < code.Length && code[i + 1] == '/')
            {
                end = code.IndexOf('\n', i);
                end = end < 0 ? code.Length : end;
            }
            else if (c == '/' && i + 1 < code.Length && code[i + 1] == '*')
            {
                end = code.IndexOf("*/", i + 2, StringComparison.Ordinal);
                end = end < 0 ? code.Length : end + 2;
            }
            else
            {
                builder.Append(c);
                i++;
                continue;
            }

            builder.Append(' ', end - i);
            i = end;
        }

        return builder.ToString();
    }

    // ── bước 4: import theo framework ─────────────────────────────────

    private static readonly (string Junit4, string Junit5)[] AnnotationPairs =
    [
        ("Before", "BeforeEach"), ("After", "AfterEach"), ("BeforeClass", "BeforeAll"), ("AfterClass", "AfterAll"), ("Ignore", "Disabled"),
    ];

    private static readonly string[] JUnit5Annotations = ["Test", "BeforeEach", "AfterEach", "BeforeAll", "AfterAll", "Disabled", "DisplayName", "Nested", "Tag", "Timeout"];
    private static readonly string[] JUnit4Annotations = ["Test", "Before", "After", "BeforeClass", "AfterClass", "Ignore"];

    private static readonly string[] AssertMethods =
    [
        "assertEquals", "assertNotEquals", "assertTrue", "assertFalse", "assertNull", "assertNotNull", "assertSame", "assertNotSame",
        "assertArrayEquals", "assertThrows", "assertDoesNotThrow", "assertAll", "assertIterableEquals", "assertLinesMatch", "assertTimeout",
        "assertInstanceOf", "fail",
    ];

    [GeneratedRegex(@"^\s*import\s+(static\s+)?([\w.]+(?:\.\*)?)\s*;\s*$", RegexOptions.Multiline)]
    private static partial Regex ImportLine();

    private static List<(bool Static, string Name, Match Match)> Imports(string code) =>
        ImportLine().Matches(code).Select(m => (m.Groups[1].Success, m.Groups[2].Value, m)).ToList();

    private static string RemoveImports(string code, Func<bool, string, bool> predicate, List<string> removed)
    {
        foreach (var (isStatic, name, _) in Imports(code).Where(i => predicate(i.Static, i.Name)).ToList())
        {
            var pattern = $@"^\s*import\s+{(isStatic ? @"static\s+" : "")}{Regex.Escape(name)}\s*;[ \t]*\r?\n?";
            code = Regex.Replace(code, pattern, "", RegexOptions.Multiline);
            removed.Add((isStatic ? "static " : "") + name);
        }

        return code;
    }

    private static bool HasImport(string code, bool isStatic, string name)
    {
        var parent = name.Contains('.') ? name[..name.LastIndexOf('.')] : "";
        return Imports(code).Any(i => i.Static == isStatic && (i.Name == name || i.Name == parent + ".*"));
    }

    private static string AddImport(string code, bool isStatic, string name, List<string> added)
    {
        if (HasImport(code, isStatic, name))
        {
            return code;
        }

        var line = $"import {(isStatic ? "static " : "")}{name};";
        var imports = Imports(code);
        int at;
        if (imports.Count > 0)
        {
            var last = imports[^1].Match;
            at = last.Index + last.Length;
            code = code.Insert(at, (code[at - 1] == '\n' ? "" : "\n") + line + (code[at - 1] == '\n' ? "\n" : ""));
        }
        else
        {
            var package = PackageLine().Match(code);
            at = package.Success ? package.Index + package.Length : 0;
            code = at == 0 ? line + "\n\n" + code : code.Insert(at, "\n\n" + line);
        }

        added.Add((isStatic ? "static " : "") + name);
        return code;
    }

    private static bool UsesAnnotation(string code, string name) => Regex.IsMatch(MaskLiteralsAndComments(code), $@"@{name}\b(?!\.)");

    private static bool UsesUnqualifiedAsserts(string code)
    {
        var masked = MaskLiteralsAndComments(code);
        return AssertMethods.Any(m => Regex.IsMatch(masked, $@"(?<![\w.]){m}\s*\("));
    }

    private static string ToJUnit5(string code, List<string> added, List<string> removed, List<string> annotations, List<string> qualifiers)
    {
        code = RemoveImports(code, (isStatic, name) =>
            !isStatic && name is "org.junit.Test" or "org.junit.Before" or "org.junit.After" or "org.junit.BeforeClass" or "org.junit.AfterClass" or "org.junit.Ignore"
            || isStatic && (name == "org.junit.Assert.*" || name.StartsWith("org.junit.Assert.", StringComparison.Ordinal)), removed);
        foreach (var (junit4, junit5) in AnnotationPairs)
        {
            var pattern = $@"@{junit4}\b(?![\w.])";
            if (Regex.IsMatch(code, pattern))
            {
                code = Regex.Replace(code, pattern, "@" + junit5);
                annotations.Add($"@{junit4}→@{junit5}");
            }
        }

        if (Regex.IsMatch(code, @"(?<![\w.])Assert\.(assert\w+|fail)\s*\("))
        {
            code = Regex.Replace(code, @"(?<![\w.])Assert\.(assert\w+|fail)(\s*\()", "Assertions.$1$2");
            code = RemoveImports(code, (isStatic, name) => !isStatic && name == "org.junit.Assert", removed);
            code = AddImport(code, false, "org.junit.jupiter.api.Assertions", added);
            qualifiers.Add("Assert.→Assertions.");
        }

        foreach (var annotation in JUnit5Annotations.Where(a => UsesAnnotation(code, a)))
        {
            code = AddImport(code, false, "org.junit.jupiter.api." + annotation, added);
        }

        if (UsesUnqualifiedAsserts(code) && !Imports(code).Any(i => i.Static && i.Name.StartsWith("org.junit.jupiter.api.Assertions.", StringComparison.Ordinal)))
        {
            code = AddImport(code, true, "org.junit.jupiter.api.Assertions.*", added);
        }

        return code;
    }

    private static string ToJUnit4(string code, List<string> added, List<string> removed, List<string> annotations, List<string> qualifiers)
    {
        code = RemoveImports(code, (isStatic, name) => name.StartsWith("org.junit.jupiter.", StringComparison.Ordinal), removed);
        foreach (var (junit4, junit5) in AnnotationPairs)
        {
            var pattern = $@"@{junit5}\b(?![\w.])";
            if (Regex.IsMatch(code, pattern))
            {
                code = Regex.Replace(code, pattern, "@" + junit4);
                annotations.Add($"@{junit5}→@{junit4}");
            }
        }

        if (Regex.IsMatch(code, @"(?<![\w.])Assertions\.(assert\w+|fail)\s*\("))
        {
            code = Regex.Replace(code, @"(?<![\w.])Assertions\.(assert\w+|fail)(\s*\()", "Assert.$1$2");
            code = AddImport(code, false, "org.junit.Assert", added);
            qualifiers.Add("Assertions.→Assert.");
        }

        foreach (var annotation in JUnit4Annotations.Where(a => UsesAnnotation(code, a)))
        {
            code = AddImport(code, false, "org.junit." + annotation, added);
        }

        if (UsesUnqualifiedAsserts(code) && !Imports(code).Any(i => i.Static && i.Name.StartsWith("org.junit.Assert.", StringComparison.Ordinal)))
        {
            code = AddImport(code, true, "org.junit.Assert.*", added);
        }

        return code;
    }

    // ── bước 5: import JDK ────────────────────────────────────────────

    private static readonly string[] JdkTypes = ["java.util.Arrays", "java.util.Collections", "java.util.Comparator", "java.util.NoSuchElementException"];

    private static string AddJdkImports(string code, List<string> added)
    {
        var masked = MaskLiteralsAndComments(code);
        foreach (var type in JdkTypes)
        {
            var simple = type[(type.LastIndexOf('.') + 1)..];
            if (Regex.IsMatch(masked, $@"(?<![\w.]){simple}\b") && !Regex.IsMatch(masked, $@"\b(class|interface|enum)\s+{simple}\b"))
            {
                code = AddImport(code, false, type, added);
            }
        }

        return code;
    }
}
