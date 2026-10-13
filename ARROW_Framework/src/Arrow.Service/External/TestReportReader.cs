using System.Globalization;
using System.Xml;
using System.Xml.Linq;
using Arrow.Models.Common;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>
/// Đọc <c>TEST-*.xml</c> trong <c>target/surefire-reports</c> (Maven) và <c>build/test-results/**</c> (Gradle) của mọi module dưới một thư mục.
/// File hỏng (tiến trình bị giết giữa chừng) bị bỏ qua.
/// </summary>
public sealed partial class TestReportReader : ITestReportReader
{
    [System.Text.RegularExpressions.GeneratedRegex(@"^[\p{L}_$][\p{L}\p{N}_$]*(\.[\p{L}_$][\p{L}\p{N}_$]*)*$")]
    private static partial System.Text.RegularExpressions.Regex QualifiedName();

    /// <summary>
    /// Tên class của một testcase. Surefire 3.x với JUnit 5 có thể ghi <c>classname</c> là tên method hoặc display name
    /// (<c>testInsert()</c>), nên chỉ tin <c>classname</c> khi nó là tên class Java hợp lệ và có package nếu testsuite có package;
    /// ngược lại dùng tên testsuite (luôn là FQN của class test).
    /// </summary>
    public static string ClassNameOf(string? testCaseClass, string suiteName)
    {
        if (string.IsNullOrEmpty(testCaseClass) || !QualifiedName().IsMatch(testCaseClass))
        {
            return suiteName;
        }

        return suiteName.Contains('.') && !testCaseClass.Contains('.') ? suiteName : testCaseClass;
    }

    private static readonly EnumerationOptions Options = new()
    {
        RecurseSubdirectories = true,
        IgnoreInaccessible = true,
        AttributesToSkip = FileAttributes.ReparsePoint,
    };

    public IReadOnlyList<TestCaseResult> Read(string directory)
    {
        if (!Directory.Exists(directory))
        {
            return [];
        }

        var results = new List<TestCaseResult>();
        foreach (var file in Directory.EnumerateFiles(directory, "TEST-*.xml", Options).Where(IsReport).OrderBy(f => f, StringComparer.Ordinal))
        {
            results.AddRange(ReadFile(file));
        }

        return results;
    }

    /// <summary>
    /// Chỉ file nằm ngay trong <c>surefire-reports/</c> (Maven) hoặc <c>test-results/&lt;task&gt;/</c> (Gradle).
    /// TestNG ghi thêm bản trùng vào <c>surefire-reports/junitreports/</c>; đọc cả hai thì mỗi test bị đếm hai lần
    /// (mẫu pilot 618492, 43258821).
    /// </summary>
    public static bool IsReport(string file)
    {
        var parent = Path.GetDirectoryName(file);
        var grandParent = parent is null ? null : Path.GetDirectoryName(parent);
        return Path.GetFileName(parent) == "surefire-reports" || (grandParent is not null && Path.GetFileName(grandParent) == "test-results");
    }

    public static IReadOnlyList<TestCaseResult> ReadFile(string file)
    {
        XDocument document;
        try
        {
            document = XDocument.Load(file);
        }
        catch (XmlException)
        {
            return [];
        }

        return Parse(document);
    }

    /// <summary>Bỏ <c>()</c> ở cuối tên method (Gradle với JUnit 5 ghi <c>adds()</c>), spec 03 Mục 3.3.</summary>
    public static string MethodName(string? name) =>
        name is null ? "" : name.EndsWith("()", StringComparison.Ordinal) ? name[..^2] : name;

    public static IReadOnlyList<TestCaseResult> Parse(XDocument document)
    {
        var results = new List<TestCaseResult>();
        foreach (var suite in document.Descendants().Where(e => e.Name.LocalName == "testsuite"))
        {
            var suiteName = (string?)suite.Attribute("name") ?? "";
            foreach (var testCase in suite.Elements().Where(e => e.Name.LocalName == "testcase"))
            {
                var className = ClassNameOf((string?)testCase.Attribute("classname"), suiteName);
                var failure = testCase.Elements().FirstOrDefault(e => e.Name.LocalName == "failure");
                var error = testCase.Elements().FirstOrDefault(e => e.Name.LocalName == "error");
                var skipped = testCase.Elements().Any(e => e.Name.LocalName == "skipped");
                var problem = error ?? failure;
                results.Add(new TestCaseResult
                {
                    ClassName = className,
                    Name = MethodName((string?)testCase.Attribute("name")),
                    Status = error is not null ? TestCaseStatus.Error : failure is not null ? TestCaseStatus.Failed : skipped ? TestCaseStatus.Skipped : TestCaseStatus.Passed,
                    Message = problem is null ? null : (string?)problem.Attribute("message"),
                    ExceptionType = problem is null ? null : (string?)problem.Attribute("type"),
                    Trace = problem is null || problem.Value.Trim().Length == 0 ? null : problem.Value.Trim(),
                    TimeSeconds = double.TryParse((string?)testCase.Attribute("time"), NumberStyles.Float, CultureInfo.InvariantCulture, out var t) ? t : 0,
                });
            }
        }

        return results;
    }
}
