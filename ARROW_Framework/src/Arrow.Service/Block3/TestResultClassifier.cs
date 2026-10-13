using System.Text.RegularExpressions;
using Arrow.Models.Block3;
using Arrow.Models.Common;

namespace Arrow.Service.Block3;

/// <summary>Kết quả lần chạy riêng class test sinh ra (bước 2–4): trạng thái lỗi (null = cả 3 bước đạt), số test, các lỗi.</summary>
public sealed record TargetRunResult(VerificationState? FailedState, TestMethodCounts Counts, IReadOnlyList<FailureInfo> Failures, IReadOnlyList<string> FailingMethods);

/// <summary>Phân loại kết quả chạy test (spec 03 Mục 3.3) và module regression (Mục 3.4, 15.1).</summary>
public static partial class TestResultClassifier
{
    public const int TraceHeadLines = 8;

    private static readonly HashSet<string> AssertionTypes =
    [
        "java.lang.AssertionError", "org.opentest4j.AssertionFailedError", "org.opentest4j.MultipleFailuresError",
        "org.junit.ComparisonFailure", "org.junit.internal.ArrayComparisonFailure", "junit.framework.AssertionFailedError",
        "junit.framework.ComparisonFailure",
    ];

    private static readonly string[] AssertionPrefixes = ["org.assertj.core.error.", "org.mockito.exceptions.verification.", "org.hamcrest."];

    /// <summary>Tên method của lỗi cấp class (khởi tạo class, <c>@BeforeAll</c>…).</summary>
    private static readonly HashSet<string> ClassLevelNames = ["initializationError", "classMethod", "", "executionError"];

    /// <summary>Frame thuộc thư viện/công cụ, bỏ qua khi tìm frame đầu tiên thuộc mã dự án.</summary>
    private static readonly string[] LibraryPrefixes =
    [
        "java.", "javax.", "jdk.", "sun.", "com.sun.", "org.junit.", "junit.", "org.testng.", "org.mockito.", "net.bytebuddy.",
        "org.assertj.", "org.hamcrest.", "org.opentest4j.", "org.apache.maven.", "org.gradle.", "worker.org.gradle.", "kotlin.",
        "org.springframework.", "org.easymock.", "org.powermock.", "org.evosuite.",
    ];

    [GeneratedRegex(@"^[\p{L}_$][\p{L}\p{N}_$]*(\.[\p{L}_$][\p{L}\p{N}_$]*)+$")]
    private static partial Regex QualifiedName();

    [GeneratedRegex(@"^\s*(?:Caused by:\s*)?(?<name>[\p{L}_$][\p{L}\p{N}_$]*(?:\.[\p{L}_$][\p{L}\p{N}_$]*)+)(?::|\s*$)")]
    private static partial Regex ExceptionLine();

    [GeneratedRegex(@"^\s*at\s+(?:[\w.\-]+/+)?(?<cls>[\p{L}_$][\p{L}\p{N}_$.]*)\.(?<method>[\p{L}_$<][\p{L}\p{N}_$>]*)\(")]
    private static partial Regex FrameLine();

    /// <summary>Test thuộc class <paramref name="testFqn"/> (kể cả class lồng <c>Outer$Inner</c>).</summary>
    public static bool Belongs(TestCaseResult test, string testFqn) =>
        test.ClassName == testFqn || test.ClassName.StartsWith(testFqn + "$", StringComparison.Ordinal);

    /// <summary>
    /// Bước 2–4: không test nào chạy (hoặc JUnit 4 "No runnable methods") → DISCOVERY; có lỗi không phải assertion
    /// (hoặc lỗi cấp class) → RUNTIME; mọi lỗi là assertion → ASSERTION; không lỗi → null.
    /// </summary>
    public static TargetRunResult Classify(IReadOnlyList<TestCaseResult> allTests, string testFqn)
    {
        var tests = allTests.Where(t => Belongs(t, testFqn)).ToList();
        var classLevel = tests.Where(t => ClassLevelNames.Contains(t.Name) && t.Status is TestCaseStatus.Failed or TestCaseStatus.Error).ToList();
        if (classLevel.Any(t => (t.Message ?? "").Contains("No runnable methods", StringComparison.Ordinal)))
        {
            return new TargetRunResult(VerificationState.Discovery, new TestMethodCounts(), [], []);
        }

        var methods = tests.Where(t => !ClassLevelNames.Contains(t.Name) && t.Status != TestCaseStatus.Skipped).ToList();
        if (methods.Count == 0 && classLevel.Count == 0)
        {
            return new TargetRunResult(VerificationState.Discovery, new TestMethodCounts(), [], []);
        }

        var failed = classLevel.Concat(methods.Where(t => t.Status is TestCaseStatus.Failed or TestCaseStatus.Error)).ToList();
        var failures = failed.Select(ToFailure).ToList();
        var counts = new TestMethodCounts
        {
            Total = methods.Count,
            Passed = methods.Count(t => t.Status == TestCaseStatus.Passed),
            Failed = methods.Count(t => t.Status != TestCaseStatus.Passed),
        };
        var failingMethods = failed.Select(t => t.Name).Where(n => n.Length > 0).Distinct().ToList();
        VerificationState? state = failures.Count == 0
            ? null
            : classLevel.Count > 0 || failures.Any(f => !IsAssertion(f.Type, f.Message)) ? VerificationState.Runtime : VerificationState.Assertion;
        return new TargetRunResult(state, counts, failures, failingMethods);
    }

    public static FailureInfo ToFailure(TestCaseResult test)
    {
        var lines = (test.Trace ?? "").Replace("\r\n", "\n").Split('\n');
        return new FailureInfo
        {
            Method = test.Name,
            Type = ExceptionName(test.ExceptionType, test.Trace),
            Message = test.Message,
            TraceHead = test.Trace is null ? null : string.Join('\n', lines.Take(TraceHeadLines)),
            Frame = ProjectFrame(test.Trace),
        };
    }

    /// <summary>
    /// Tên exception: thuộc tính <c>type</c> nếu là tên class hợp lệ; nếu không (một số plugin ghi thông điệp vào <c>type</c>)
    /// thì tên exception ở dòng đầu stack trace, hoặc dòng <c>Caused by:</c> đầu tiên.
    /// </summary>
    public static string ExceptionName(string? type, string? trace)
    {
        if (type is not null && QualifiedName().IsMatch(type.Trim()))
        {
            return type.Trim();
        }

        foreach (var line in (trace ?? "").Replace("\r\n", "\n").Split('\n').Take(1)
                     .Concat((trace ?? "").Split('\n').Where(l => l.TrimStart().StartsWith("Caused by:", StringComparison.Ordinal)).Take(1)))
        {
            var match = ExceptionLine().Match(line);
            if (match.Success)
            {
                return match.Groups["name"].Value;
            }
        }

        return string.IsNullOrWhiteSpace(type) ? "unknown" : type.Trim();
    }

    /// <summary>Exception assertion (spec 03 Mục 3.3 bước 3), gồm lỗi "should have thrown" của TestNG <c>expectedExceptions</c>.</summary>
    public static bool IsAssertion(string? exception, string? message)
    {
        if (exception is null)
        {
            return false;
        }

        if (AssertionTypes.Contains(exception) || AssertionPrefixes.Any(p => exception.StartsWith(p, StringComparison.Ordinal)))
        {
            return true;
        }

        return exception == "org.testng.TestException"
               && message is not null
               && (message.Contains("should have thrown", StringComparison.OrdinalIgnoreCase) || message.Contains("Expected exception", StringComparison.OrdinalIgnoreCase));
    }

    /// <summary><c>Class.method</c> của frame đầu tiên thuộc mã dự án (không phải JDK, framework test, công cụ build); không có → null.</summary>
    public static string? ProjectFrame(string? trace)
    {
        foreach (var line in (trace ?? "").Replace("\r\n", "\n").Split('\n'))
        {
            var match = FrameLine().Match(line);
            if (!match.Success)
            {
                continue;
            }

            var cls = match.Groups["cls"].Value;
            if (LibraryPrefixes.Any(p => cls.StartsWith(p, StringComparison.Ordinal)))
            {
                continue;
            }

            return $"{ShortName(cls)}.{match.Groups["method"].Value}";
        }

        return null;
    }

    /// <summary><c>org.opentest4j.AssertionFailedError</c> → <c>AssertionFailedError</c>; class lồng giữ <c>$</c>.</summary>
    public static string ShortName(string name) => name[(name.LastIndexOf('.') + 1)..];

    /// <summary>
    /// Bước 5 (spec 03 Mục 3.4, 15.1): test trong <c>passing_in_all_runs</c> mà fail hoặc không còn chạy, và test sinh ra
    /// fail (hoặc không chạy) khi chạy cùng module. Test flaky và test hỏng sẵn không được xét.
    /// </summary>
    public static IReadOnlyList<RegressionInfo> FindRegressions(IReadOnlyList<string> baselinePassing, IReadOnlyList<TestCaseResult> moduleRun, string testFqn)
    {
        var byId = moduleRun.GroupBy(t => t.Id).ToDictionary(g => g.Key, g => g.OrderByDescending(t => Severity(t.Status)).First());
        var regressions = new List<RegressionInfo>();
        foreach (var id in baselinePassing)
        {
            if (!byId.TryGetValue(id, out var test))
            {
                regressions.Add(new RegressionInfo { TestId = id, Type = "NOT_RUN", Message = "không có trong báo cáo khi chạy cùng test sinh ra" });
            }
            else if (test.Status is TestCaseStatus.Failed or TestCaseStatus.Error)
            {
                regressions.Add(new RegressionInfo { TestId = id, Type = ExceptionName(test.ExceptionType, test.Trace), Message = test.Message });
            }
        }

        var generated = moduleRun.Where(t => Belongs(t, testFqn)).ToList();
        if (generated.Count == 0)
        {
            regressions.Add(new RegressionInfo { TestId = testFqn, Type = "NOT_RUN", Message = "test sinh ra không được chạy khi chạy cả module" });
        }

        regressions.AddRange(generated.Where(t => t.Status is TestCaseStatus.Failed or TestCaseStatus.Error)
            .Select(t => new RegressionInfo { TestId = t.Id, Type = ExceptionName(t.ExceptionType, t.Trace), Message = t.Message }));
        return regressions;
    }

    private static int Severity(TestCaseStatus status) => status switch
    {
        TestCaseStatus.Error => 3,
        TestCaseStatus.Failed => 2,
        TestCaseStatus.Passed => 1,
        _ => 0,
    };
}
