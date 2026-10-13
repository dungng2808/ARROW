using System.Text.RegularExpressions;
using Arrow.Core.Prepare;

namespace Arrow.Core.Verify;

/// <summary>
/// Phân loại lỗi của chuỗi xác minh từ log và báo cáo test (specs/03 Mục 2–4, 6). Code thuần.
/// </summary>
public static partial class FailureClassifier
{
    // ── Bước 1: biên dịch ────────────────────────────────────────────────

    /// <summary>Loại lỗi javac theo danh sách cố định (specs/03 Mục 6); thứ tự: mẫu cụ thể trước.</summary>
    private static readonly (string Kind, Regex Pattern)[] JavacKinds =
    [
        ("cannot find symbol", new Regex(@"cannot find symbol", RegexOptions.IgnoreCase)),
        ("incompatible types", new Regex(@"incompatible types|bad operand type|cannot be converted to", RegexOptions.IgnoreCase)),
        ("unreported exception", new Regex(@"unreported exception", RegexOptions.IgnoreCase)),
        ("constructor cannot be applied", new Regex(@"constructor .* (cannot be applied|in (class|enum) .* cannot be applied)|no suitable constructor", RegexOptions.IgnoreCase)),
        ("method cannot be applied", new Regex(@"method .* cannot be applied|no suitable method|cannot be applied to given types", RegexOptions.IgnoreCase)),
        ("package does not exist", new Regex(@"package .* does not exist", RegexOptions.IgnoreCase)),
        ("has private access", new Regex(@"has private access", RegexOptions.IgnoreCase)),
        ("is not public", new Regex(@"is not public in|not public in", RegexOptions.IgnoreCase)),
        ("cannot be referenced from a static context", new Regex(@"cannot be referenced from a static context", RegexOptions.IgnoreCase)),
        ("already defined", new Regex(@"is already defined", RegexOptions.IgnoreCase)),
        ("missing return statement", new Regex(@"missing return statement", RegexOptions.IgnoreCase)),
        ("unreachable statement", new Regex(@"unreachable statement", RegexOptions.IgnoreCase)),
        ("class is abstract", new Regex(@"is abstract; cannot be instantiated", RegexOptions.IgnoreCase)),
        ("does not override", new Regex(@"does not override|method does not override or implement", RegexOptions.IgnoreCase)),
        ("cannot be accessed", new Regex(@"cannot be accessed|has protected access", RegexOptions.IgnoreCase)),
    ];

    public static string JavacKind(string message)
    {
        foreach (var (kind, pattern) in JavacKinds)
        {
            if (pattern.IsMatch(message))
            {
                return kind;
            }
        }

        return "other";
    }

    /// <summary>Lỗi javac dạng <c>/p/File.java:42: error: msg</c> (javac, Gradle) hoặc <c>[ERROR] /p/File.java:[42,15] msg</c> (Maven).</summary>
    [GeneratedRegex(@"^(?:\[ERROR\]\s+)?(?<file>(?:[A-Za-z]:)?[^\s:\[\]]*?\.java):(?:\[(?<line>\d+),(?<col>\d+)\]\s*(?:error:\s*)?|(?<line2>\d+):\s*error:\s*)(?<msg>.*)$")]
    private static partial Regex JavacLine();

    [GeneratedRegex(@"^(?:\[ERROR\]\s+)?\s*symbol\s*:\s*(?<kind>method|variable|class|constructor|package|static)?\s*(?<symbol>.*)$")]
    private static partial Regex SymbolLine();

    [GeneratedRegex(@"^(?:\[ERROR\]\s+)?\s*location\s*:\s*(?<location>.*)$")]
    private static partial Regex LocationLine();

    /// <summary>Mọi lỗi javac trong log (kèm symbol, location ở các dòng sau). Lỗi trùng (Maven in hai lần) bị bỏ.</summary>
    public static List<CompileError> ParseCompileErrors(string log)
    {
        var errors = new List<CompileError>();
        var seen = new HashSet<string>();
        var lines = log.Replace("\r\n", "\n").Split('\n');
        for (var i = 0; i < lines.Length; i++)
        {
            var match = JavacLine().Match(lines[i].TrimEnd());
            if (!match.Success)
            {
                continue;
            }

            var lineText = match.Groups["line"].Success ? match.Groups["line"].Value : match.Groups["line2"].Value;
            var message = match.Groups["msg"].Value.Trim();
            if (message.StartsWith("warning:", StringComparison.OrdinalIgnoreCase) || lines[i].Contains("[WARNING]", StringComparison.Ordinal))
            {
                continue;
            }

            string? symbol = null, symbolKind = null, location = null;
            for (var j = i + 1; j < Math.Min(lines.Length, i + 6); j++)
            {
                if (JavacLine().IsMatch(lines[j].TrimEnd()))
                {
                    break;
                }

                var s = SymbolLine().Match(lines[j].TrimEnd());
                if (s.Success && symbol is null)
                {
                    symbolKind = s.Groups["kind"].Success && s.Groups["kind"].Value != "static" ? s.Groups["kind"].Value : null;
                    symbol = (symbolKind is null ? s.Groups["symbol"].Value : $"{symbolKind} {s.Groups["symbol"].Value}").Trim();
                    continue;
                }

                var l = LocationLine().Match(lines[j].TrimEnd());
                if (l.Success && location is null)
                {
                    location = l.Groups["location"].Value.Trim();
                }
            }

            var error = new CompileError
            {
                File = match.Groups["file"].Value,
                Line = int.Parse(lineText),
                Column = match.Groups["col"].Success ? int.Parse(match.Groups["col"].Value) : null,
                Message = message,
                Kind = JavacKind(message),
                Symbol = symbol,
                SymbolKind = symbolKind,
                Location = location,
            };
            if (seen.Add($"{error.File}:{error.Line}:{error.Column}:{error.Message}"))
            {
                errors.Add(error);
            }
        }

        return errors;
    }

    /// <summary>Log có dấu hiệu thật của lỗi biên dịch (AR 2.1); không dùng chuỗi con <c>error:</c> đơn thuần.</summary>
    public static bool HasCompilationFailure(string log) =>
        log.Contains("COMPILATION ERROR", StringComparison.Ordinal)
        || log.Contains("Compilation failure", StringComparison.Ordinal)
        || CompileTaskFailed().IsMatch(log)
        || ParseCompileErrors(log).Count > 0;

    [GeneratedRegex(@"Execution failed for task '[^']*:compile(Test)?(Java|Kotlin|Groovy)'")]
    private static partial Regex CompileTaskFailed();

    /// <summary>Lỗi biên dịch chỉ vào file test sinh ra (đường dẫn kết thúc bằng <c>&lt;T&gt;.java</c>).</summary>
    public static List<CompileError> ErrorsInFile(IEnumerable<CompileError> errors, string testSimpleName) =>
        errors.Where(e => e.File.Replace('\\', '/').EndsWith("/" + testSimpleName + ".java", StringComparison.Ordinal)
                          || e.File == testSimpleName + ".java").ToList();

    // ── Lỗi hạ tầng (specs/03 Mục 2, quy tắc bổ sung) ───────────────────

    private static readonly string[] DependencyDownloadMarkers =
    [
        "Could not resolve dependencies", "Could not transfer artifact", "Could not resolve all files",
        "Could not resolve all dependencies", "Could not download", "Could not GET", "Connection reset",
        "Could not resolve host", "Read timed out",
    ];

    public static bool IsDependencyDownloadError(string log) =>
        DependencyDownloadMarkers.Any(m => log.Contains(m, StringComparison.OrdinalIgnoreCase));

    /// <summary>Công cụ build tự lỗi: không khởi động được, JVM của Maven/Gradle crash.</summary>
    public static bool IsToolError(int? exitCode, bool timedOut, string log) =>
        !timedOut && exitCode is null
        || log.Contains("A fatal error has been detected by the Java Runtime Environment", StringComparison.Ordinal) && !log.Contains("surefire", StringComparison.OrdinalIgnoreCase)
        || log.Contains("Could not find or load main class org.codehaus.plexus.classworlds", StringComparison.Ordinal)
        || log.Contains("Could not find or load main class org.gradle", StringComparison.Ordinal);

    // ── Bước 4: RUNTIME hay ASSERTION (specs/03 Mục 3) ──────────────────

    private static readonly string[] AssertionTypes =
    [
        "java.lang.AssertionError", "org.opentest4j.AssertionFailedError", "org.opentest4j.MultipleFailuresError",
        "junit.framework.AssertionFailedError", "junit.framework.ComparisonFailure", "org.junit.ComparisonFailure",
        "org.junit.internal.ArrayComparisonFailure", "org.testng.TestException",
    ];

    private static readonly string[] AssertionPrefixes = ["org.assertj.core.error.", "org.mockito.exceptions.verification.", "org.hamcrest."];

    private static readonly string[] TimeoutTypes =
    [
        "org.junit.runners.model.TestTimedOutException", "java.util.concurrent.TimeoutException", "org.junit.jupiter.api.AssertTimeout",
    ];

    public static bool IsAssertionFailure(TestCaseResult result)
    {
        var type = ExceptionType(result) ?? "";
        var message = result.Message ?? "";
        if (TimeoutTypes.Any(t => type.StartsWith(t, StringComparison.Ordinal)) || message.Contains("timed out after", StringComparison.OrdinalIgnoreCase))
        {
            return false;
        }

        if (AssertionTypes.Contains(type) || AssertionPrefixes.Any(p => type.StartsWith(p, StringComparison.Ordinal)))
        {
            return true;
        }

        // assertThrows không ném; @Test(expected=…) không ném (JUnit 4 ném AssertionError với thông điệp này).
        return message.Contains("to be thrown, but nothing was thrown", StringComparison.Ordinal)
               || message.StartsWith("Expected exception:", StringComparison.Ordinal)
               || message.StartsWith("Expected test to throw", StringComparison.Ordinal);
    }

    private static string? FirstTraceType(string? detail)
    {
        var first = detail?.Split('\n').FirstOrDefault()?.Trim();
        if (string.IsNullOrEmpty(first))
        {
            return null;
        }

        var colon = first.IndexOf(':');
        return colon > 0 ? first[..colon] : first;
    }

    /// <summary>JUnit 4 báo class không có <c>@Test</c> bằng một test case <c>initializationError</c> "No runnable methods": là DISCOVERY.</summary>
    public static bool IsNoTestsMarker(TestCaseResult result) =>
        result.Id.EndsWith("#initializationError", StringComparison.Ordinal)
        && ((result.Message ?? "").Contains("No runnable methods", StringComparison.Ordinal)
            || (result.Detail ?? "").Contains("No runnable methods", StringComparison.Ordinal)
            || (result.Message ?? "").Contains("No tests found", StringComparison.Ordinal));

    /// <summary>Trạng thái của bước 4 khi có method lỗi: mọi method lỗi là assertion → ASSERTION, ngược lại RUNTIME.</summary>
    public static VerificationState TargetFailureState(IEnumerable<TestCaseResult> failed) =>
        failed.All(IsAssertionFailure) ? VerificationState.Assertion : VerificationState.Runtime;

    [GeneratedRegex(@"^[A-Za-z_$][\w$]*(\.[A-Za-z_$][\w$]*)+$")]
    private static partial Regex QualifiedClass();

    [GeneratedRegex(@"(?:^|\s|Caused by:\s*)([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+(?:Exception|Error|Throwable|Failure))\b")]
    private static partial Regex ThrowableName();

    /// <summary>
    /// Tên exception của một test case lỗi. Surefire đôi khi ghi thông điệp vào <c>type</c> (exception tự định nghĩa <c>toString</c>,
    /// ví dụ của snakeyaml): chỉ nhận <c>type</c> có dạng tên class, ngược lại tìm trong stack trace; không có thì null.
    /// </summary>
    public static string? ExceptionType(TestCaseResult result)
    {
        if (result.Type is { } type && QualifiedClass().IsMatch(type.Trim()))
        {
            return type.Trim();
        }

        if (FirstTraceType(result.Detail) is { } first && QualifiedClass().IsMatch(first))
        {
            return first;
        }

        var match = ThrowableName().Match(result.Detail ?? "");
        return match.Success ? match.Groups[1].Value : null;
    }

    public static TestFailure ToFailure(TestCaseResult result)
    {
        var trace = (result.Detail ?? "").Split('\n').Select(l => l.TrimEnd()).Where(l => l.Length > 0).Take(15).ToList();
        return new TestFailure
        {
            Method = TestReportParser.MethodName(result.Id[(result.Id.IndexOf('#') + 1)..]),
            Kind = IsAssertionFailure(result) ? "assertion" : "exception",
            Type = ExceptionType(result),
            Message = result.Message,
            TraceHead = trace,
        };
    }

    // ── Error signature (specs/03 Mục 6) ─────────────────────────────────

    public static string Signature(VerificationState state, string kind, string target) =>
        $"{States.Name(state)} | {kind} | {target}";

    public static string CompileSignature(IReadOnlyList<CompileError> errors)
    {
        var first = errors.FirstOrDefault();
        return first is null
            ? Signature(VerificationState.Compile, "other", "-")
            : Signature(VerificationState.Compile, first.Kind, first.SymbolKind ?? "-");
    }

    /// <summary>RUNTIME: exception + frame đầu tiên thuộc code dự án (class có trong src/main/java).</summary>
    public static string RuntimeSignature(TestFailure failure, ISet<string> projectClasses)
    {
        var exception = SimpleType(failure.Type ?? "Exception");
        foreach (var line in failure.TraceHead)
        {
            var match = FrameLine().Match(line);
            if (!match.Success)
            {
                continue;
            }

            var cls = match.Groups["cls"].Value;
            var outer = cls.Contains('$') ? cls[..cls.IndexOf('$')] : cls;
            if (projectClasses.Contains(outer))
            {
                return Signature(VerificationState.Runtime, exception, $"{SimpleType(outer)}.{match.Groups["method"].Value}");
            }
        }

        return Signature(VerificationState.Runtime, exception, "-");
    }

    [GeneratedRegex(@"^\s*at\s+(?<cls>[\w$.]+)\.(?<method>[\w$<>]+)\(")]
    private static partial Regex FrameLine();

    public static string AssertionSignature(TestFailure failure) =>
        Signature(VerificationState.Assertion, SimpleType(failure.Type ?? "AssertionError"), failure.Method);

    public static string RegressionSignature(IEnumerable<NewFailure> failures) =>
        Signature(VerificationState.Regression, "existing tests fail",
            string.Join(",", failures.Select(f => SimpleType(f.Id.Split('#')[0])).Distinct().Order(StringComparer.Ordinal)));

    public static string DiscoverySignature() => Signature(VerificationState.Discovery, "no tests executed", "-");

    /// <summary>[D11] UNKNOWN: plugin id trong "Failed to execute goal", hoặc dòng ERROR đầu tiên đã chuẩn hóa.</summary>
    public static string UnknownSignature(string log)
    {
        var goal = FailedGoal().Match(log);
        if (goal.Success)
        {
            return Signature(VerificationState.Unknown, goal.Groups["plugin"].Value, "-");
        }

        var task = GradleFailedTask().Match(log);
        if (task.Success)
        {
            return Signature(VerificationState.Unknown, "task " + task.Groups["task"].Value, "-");
        }

        var firstError = log.Replace("\r\n", "\n").Split('\n')
            .Select(l => l.Trim())
            .FirstOrDefault(l => l.StartsWith("[ERROR]", StringComparison.Ordinal) && l.Length > 8 || l.StartsWith("FAILURE:", StringComparison.Ordinal));
        return Signature(VerificationState.Unknown, firstError is null ? "build failed" : NormalizeLine(firstError), "-");
    }

    [GeneratedRegex(@"Failed to execute goal (?:[\w.\-]+:)?(?<plugin>[\w.\-]+):[\w.\-]+:[\w.\-]+")]
    private static partial Regex FailedGoal();

    [GeneratedRegex(@"Execution failed for task '(?:[\w\-]*:)*(?<task>[\w\-]+)'")]
    private static partial Regex GradleFailedTask();

    /// <summary>Bỏ đường dẫn, số, chuỗi trong ngoặc kép; giữ tối đa 80 ký tự.</summary>
    public static string NormalizeLine(string line)
    {
        var text = line.Replace("[ERROR]", "").Trim();
        text = PathLike().Replace(text, "<path>");
        text = QuotedText().Replace(text, "<str>");
        text = Numbers().Replace(text, "<n>");
        text = Spaces().Replace(text, " ").Trim();
        return text.Length > 80 ? text[..80] : text;
    }

    [GeneratedRegex(@"(?:[A-Za-z]:)?(?:[/\\][\w.\-@$]+){2,}")]
    private static partial Regex PathLike();

    [GeneratedRegex("\"[^\"]*\"|'[^']*'")]
    private static partial Regex QuotedText();

    [GeneratedRegex(@"\b\d+(\.\d+)*\b")]
    private static partial Regex Numbers();

    [GeneratedRegex(@"\s+")]
    private static partial Regex Spaces();

    public static string SimpleType(string type)
    {
        var name = type.Trim();
        var colon = name.IndexOf(':');
        if (colon > 0)
        {
            name = name[..colon];
        }

        return name.Contains('.') ? name[(name.LastIndexOf('.') + 1)..] : name;
    }

    // ── Regression (specs/03 Mục 4) ─────────────────────────────────────

    /// <summary>Test của tập so sánh (pass ở mọi lần baseline, trừ class sinh ra) bị fail hoặc biến mất lần này.</summary>
    public static List<NewFailure> NewFailures(Baseline baseline, IReadOnlyList<TestCaseResult> moduleRun, string generatedClassFqn)
    {
        var byId = moduleRun.GroupBy(r => r.Id).ToDictionary(g => g.Key, g => g.Last());
        var failures = new List<NewFailure>();
        foreach (var id in baseline.PassingInAllRuns)
        {
            var cls = id.Split('#')[0];
            if (cls == generatedClassFqn || cls.StartsWith(generatedClassFqn + "$", StringComparison.Ordinal))
            {
                continue;
            }

            if (!byId.TryGetValue(id, out var now))
            {
                failures.Add(new NewFailure { Id = id, Type = "MISSING", Message = "test có ở baseline nhưng không có trong lần chạy này" });
            }
            else if (now.Outcome is TestOutcome.Fail or TestOutcome.Error)
            {
                failures.Add(new NewFailure { Id = id, Type = now.Type, Message = now.Message });
            }
        }

        return failures;
    }

    /// <summary>Test case thuộc class sinh ra (kể cả class lồng <c>T$Inner</c>).</summary>
    public static List<TestCaseResult> OfClass(IEnumerable<TestCaseResult> results, string classFqn) =>
        results.Where(r =>
        {
            var cls = r.Id.Split('#')[0];
            return cls == classFqn || cls.StartsWith(classFqn + "$", StringComparison.Ordinal);
        }).ToList();

    /// <summary>Log lỗi đã rút gọn cho prompt sửa khi UNKNOWN: 40 dòng ERROR/FAILURE cuối.</summary>
    public static string ErrorExcerpt(string log, int maxLines = 40) =>
        string.Join('\n', log.Replace("\r\n", "\n").Split('\n')
            .Where(l => l.Contains("[ERROR]", StringComparison.Ordinal) || l.Contains("FAILURE", StringComparison.Ordinal)
                        || l.Contains("Caused by:", StringComparison.Ordinal) || l.StartsWith("> ", StringComparison.Ordinal)
                        || l.Contains("Execution failed", StringComparison.Ordinal))
            .TakeLast(maxLines));
}
