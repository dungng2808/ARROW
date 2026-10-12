using System.Text.Json.Serialization;
using System.Xml;
using System.Xml.Linq;
using Arrow.Core.Util;

namespace Arrow.Core.Prepare;

[JsonConverter(typeof(UpperSnakeEnumConverter<TestOutcome>))]
public enum TestOutcome
{
    Pass,
    Fail,
    Error,
    Skip,
}

/// <summary>Kết quả một test method trong báo cáo XML (specs/02 §6).</summary>
public sealed record TestCaseResult
{
    /// <summary><c>classname#name</c>.</summary>
    public string Id { get; init; } = "";
    public TestOutcome Outcome { get; init; }
    public string? Type { get; init; }
    public string? Message { get; init; }

    /// <summary>30 dòng đầu của nội dung lỗi.</summary>
    public string? Detail { get; init; }
    public double Seconds { get; init; }
}

/// <summary>Đọc báo cáo JUnit XML của Surefire và Gradle (<c>TEST-*.xml</c>).</summary>
public static class TestReportParser
{
    public static List<TestCaseResult> Parse(string xml)
    {
        XDocument document;
        try
        {
            document = XDocument.Parse(xml);
        }
        catch (XmlException)
        {
            return [];
        }

        var results = new List<TestCaseResult>();
        foreach (var testcase in document.Descendants("testcase"))
        {
            var suiteName = (string?)testcase.Ancestors("testsuite").FirstOrDefault()?.Attribute("name");
            var className = ClassNameOf((string?)testcase.Attribute("classname"), suiteName);
            var name = MethodLabel((string?)testcase.Attribute("name") ?? "");
            var failure = testcase.Element("failure");
            var error = testcase.Element("error");
            var skipped = testcase.Element("skipped");
            var problem = failure ?? error;
            results.Add(new TestCaseResult
            {
                Id = $"{className}#{name}",
                Outcome = failure is not null ? TestOutcome.Fail : error is not null ? TestOutcome.Error : skipped is not null ? TestOutcome.Skip : TestOutcome.Pass,
                Type = (string?)problem?.Attribute("type"),
                Message = (string?)problem?.Attribute("message"),
                Detail = problem is null ? null : string.Join('\n', problem.Value.Replace("\r\n", "\n").Split('\n').Take(30)),
                Seconds = double.TryParse((string?)testcase.Attribute("time"), System.Globalization.NumberStyles.Float, System.Globalization.CultureInfo.InvariantCulture, out var t) ? t : 0,
            });
        }

        return results;
    }

    /// <summary>Gradle ghi tên test JUnit 5 kèm <c>()</c> (<c>alwaysFails()</c>); bỏ để ID giống Surefire.</summary>
    public static string MethodLabel(string name) => name.EndsWith("()", StringComparison.Ordinal) ? name[..^2] : name;

    /// <summary>Tên method Java của một test case (bỏ tham số và chỉ số của test có tham số: <c>add(int)[1]</c> → <c>add</c>).</summary>
    public static string MethodName(string testCaseName)
    {
        var end = testCaseName.IndexOfAny(['(', '[', ' ']);
        return end > 0 ? testCaseName[..end] : testCaseName;
    }

    private static readonly System.Text.RegularExpressions.Regex QualifiedName =
        new(@"^[A-Za-z_$][\w$]*(\.[A-Za-z_$][\w$]*)*$", System.Text.RegularExpressions.RegexOptions.Compiled);

    /// <summary>
    /// Tên class của test case. Một số bản Surefire (JUnit 5 với display name, JUnit 4 với runner riêng) ghi
    /// <c>classname</c> là tên method (<c>testRead()</c>, <c>checkFoo</c>); khi đó lấy tên của <c>testsuite</c>.
    /// </summary>
    public static string ClassNameOf(string? className, string? suiteName)
    {
        className ??= "";
        var suiteValid = suiteName is not null && QualifiedName.IsMatch(suiteName);
        if (!QualifiedName.IsMatch(className))
        {
            return suiteValid ? suiteName! : className;
        }

        return !className.Contains('.') && suiteValid && suiteName!.Contains('.') ? suiteName : className;
    }

    /// <summary>Gộp nhiều file; một test xuất hiện nhiều lần (rerun của Surefire) thì lấy lần cuối.</summary>
    public static List<TestCaseResult> ParseMany(IEnumerable<string> xmls) =>
        xmls.SelectMany(Parse).GroupBy(r => r.Id).Select(g => g.Last()).OrderBy(r => r.Id, StringComparer.Ordinal).ToList();
}

/// <summary>Kết quả baseline của module (<c>baseline.json</c>, specs/02 §5).</summary>
public sealed record Baseline
{
    public int Runs { get; init; }
    public int TestsTotal { get; init; }
    public List<string> FailingInAllRuns { get; init; } = [];
    public List<string> Flaky { get; init; } = [];
    public List<double> DurationsSeconds { get; init; } = [];
    public string ReportSource { get; init; } = "none";

    /// <summary>Test pass ở mọi lần chạy: dùng để so sánh regression.</summary>
    public List<string> PassingInAllRuns { get; init; } = [];

    public static Baseline Compute(IReadOnlyList<IReadOnlyList<TestCaseResult>> runs, IReadOnlyList<double> durations, string reportSource)
    {
        var ids = runs.SelectMany(r => r.Select(t => t.Id)).Distinct().OrderBy(i => i, StringComparer.Ordinal).ToList();
        var failing = new List<string>();
        var flaky = new List<string>();
        var passing = new List<string>();
        foreach (var id in ids)
        {
            // Test không có trong một lần chạy coi như kết quả khác → flaky.
            var outcomes = runs.Select(r => r.FirstOrDefault(t => t.Id == id)?.Outcome).ToList();
            var failed = outcomes.Select(o => o is TestOutcome.Fail or TestOutcome.Error).ToList();
            if (outcomes.Any(o => o is null) || failed.Distinct().Count() > 1)
            {
                flaky.Add(id);
            }
            else if (failed.All(f => f))
            {
                failing.Add(id);
            }
            else if (outcomes.All(o => o == TestOutcome.Pass))
            {
                passing.Add(id);
            }
        }

        return new Baseline
        {
            Runs = runs.Count,
            TestsTotal = ids.Count,
            FailingInAllRuns = failing,
            Flaky = flaky,
            PassingInAllRuns = passing,
            DurationsSeconds = durations.Select(d => Math.Round(d, 2)).ToList(),
            ReportSource = reportSource,
        };
    }
}
