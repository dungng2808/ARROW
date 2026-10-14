using System.Text.RegularExpressions;
using Arrow.Models.Block3;
using Arrow.Models.Common;

namespace Arrow.Service.Block3;

/// <summary>
/// Error signature chuẩn hóa (spec 03 Mục 5): chỉ gồm loại lỗi và tên (không số dòng, đường dẫn, giá trị) để nhận ra lỗi lặp.
/// Lấy lỗi đầu tiên, ưu tiên lỗi nằm trong file sinh ra.
/// </summary>
public static class SignatureBuilder
{
    public static string Compile(IReadOnlyList<CompileError> errors)
    {
        var first = errors.FirstOrDefault(e => e.InGeneratedFile) ?? errors.FirstOrDefault();
        return first is null ? "COMPILE | other | -" : $"COMPILE | {first.Kind} | {first.SymbolKind ?? "-"}";
    }

    public static string Unknown(string? log) => $"UNKNOWN | {BuildErrorExtractor.FailingStep(log ?? "")} | -";

    public static string Discovery() => "DISCOVERY | no tests executed | -";

    /// <summary><c>RUNTIME | &lt;exception ngắn&gt; | &lt;Class.method của frame đầu thuộc mã dự án&gt;</c>.</summary>
    public static string Runtime(IReadOnlyList<FailureInfo> failures)
    {
        var first = failures.FirstOrDefault(f => !TestResultClassifier.IsAssertion(f.Type, f.Message)) ?? failures.FirstOrDefault();
        return first is null ? "RUNTIME | unknown | -" : $"RUNTIME | {TestResultClassifier.ShortName(first.Type ?? "unknown")} | {first.Frame ?? "-"}";
    }

    /// <summary><c>ASSERTION | &lt;exception ngắn&gt; | &lt;test method fail đầu tiên&gt;</c>.</summary>
    public static string Assertion(IReadOnlyList<FailureInfo> failures)
    {
        var first = failures.FirstOrDefault();
        return first is null ? "ASSERTION | unknown | -" : $"ASSERTION | {TestResultClassifier.ShortName(first.Type ?? "unknown")} | {first.Method}";
    }

    /// <summary><c>REGRESSION | existing tests fail | ATest,BTest</c>: tên ngắn các class bị hỏng (gồm cả class sinh ra khi nó fail chung), sắp xếp.</summary>
    public static string Regression(IReadOnlyList<RegressionInfo> regressions)
    {
        var classes = regressions
            .Select(r => r.TestId.Split('#')[0])
            .Select(c => TestResultClassifier.ShortName(c).Split('$')[0])
            .Distinct(StringComparer.Ordinal)
            .OrderBy(c => c, StringComparer.Ordinal);
        return $"REGRESSION | existing tests fail | {string.Join(',', classes)}";
    }
}

/// <summary>Khối lỗi build cho UNKNOWN (spec 03 Mục 5): <c>Failed to execute goal</c> (Maven) / <c>What went wrong</c> (Gradle) kèm <c>Caused by:</c>.</summary>
public static partial class BuildErrorExtractor
{
    [GeneratedRegex(@"Failed to execute goal (?<group>[\w.\-]+):(?<artifact>[\w.\-]+):(?<version>[\w.\-]+):(?<goal>[\w.\-]+)")]
    private static partial Regex MavenGoal();

    [GeneratedRegex(@"Execution failed for task '(?<task>[^']+)'")]
    private static partial Regex GradleTask();

    private static readonly string[] NoiseMarkers =
    [
        "-> [Help", "Re-run Maven", "For more information about the errors", "To see the full stack trace", "[Help 1]",
        "After correcting the problems", "mvn <args> -rf", "Run with --stacktrace", "Run with --info", "Run with --debug",
        "Run with --scan", "Get more help at", "* Try:",
    ];

    /// <summary>Plugin Maven (<c>maven-enforcer-plugin</c>) hoặc task Gradle (<c>compileTestJava</c>) gây lỗi; không xác định → <c>build</c>.</summary>
    public static string FailingStep(string log)
    {
        if (MavenGoal().Match(log) is { Success: true } goal)
        {
            return goal.Groups["artifact"].Value;
        }

        if (GradleTask().Match(log) is { Success: true } task)
        {
            var name = task.Groups["task"].Value;
            return name[(name.LastIndexOf(':') + 1)..];
        }

        return log.Contains("Failed to execute goal", StringComparison.Ordinal) ? "maven" : "build";
    }

    /// <summary>Tối đa <paramref name="maxLines"/> dòng lỗi build, bỏ dòng trùng, bỏ lời khuyên của Maven/Gradle, thay đường dẫn workspace bằng đường dẫn tương đối.</summary>
    public static string Excerpt(string log, string? workspaceRoot, int maxLines = 60)
    {
        var lines = log.Replace("\r\n", "\n").Split('\n');
        var picked = new List<string>();
        var inBlock = false;
        foreach (var raw in lines)
        {
            var line = raw.TrimEnd();
            if (line.Contains("Failed to execute goal", StringComparison.Ordinal) || line.StartsWith("* What went wrong", StringComparison.Ordinal))
            {
                inBlock = true;
            }
            else if (line.StartsWith("* Try:", StringComparison.Ordinal) || line.StartsWith("BUILD FAILED", StringComparison.Ordinal) || line.StartsWith("[INFO]", StringComparison.Ordinal))
            {
                inBlock = false;
            }

            var keep = (inBlock && (line.StartsWith("[ERROR]", StringComparison.Ordinal) || !line.StartsWith('[')))
                       || line.TrimStart().StartsWith("Caused by:", StringComparison.Ordinal);
            if (keep && line.Trim().Length > 0 && line.Trim() != "[ERROR]" && !NoiseMarkers.Any(m => line.Contains(m, StringComparison.Ordinal)))
            {
                picked.Add(line);
            }
        }

        var text = picked.Distinct(StringComparer.Ordinal).Take(maxLines);
        var joined = string.Join('\n', text);
        return workspaceRoot is null ? joined : StripRoot(joined, workspaceRoot);
    }

    /// <summary>Bỏ đường dẫn tuyệt đối của bản sao workspace (cả dạng <c>/</c>, <c>\</c> và <c>/D:/…</c> của Maven trên Windows).</summary>
    public static string StripRoot(string text, string workspaceRoot)
    {
        var root = workspaceRoot.TrimEnd('/', '\\');
        var forward = root.Replace('\\', '/');
        return text.Replace(root + "/", "", StringComparison.Ordinal)
            .Replace(root + "\\", "", StringComparison.Ordinal)
            .Replace("/" + forward + "/", "", StringComparison.Ordinal)
            .Replace(forward + "/", "", StringComparison.Ordinal)
            .Replace(root, ".", StringComparison.Ordinal);
    }
}

/// <summary>Nhóm lỗi để mô tả và báo cáo (không dùng để quyết định).</summary>
public static class StateGroups
{
    public static ErrorGroup? GroupOf(VerificationState state) => state switch
    {
        VerificationState.Compile or VerificationState.Unknown or VerificationState.Discovery => ErrorGroup.Build,
        VerificationState.Runtime or VerificationState.Regression => ErrorGroup.Runtime,
        VerificationState.Assertion => ErrorGroup.Assertion,
        _ => null,
    };
}
