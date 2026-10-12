using System.Text;
using System.Text.RegularExpressions;
using Arrow.Core.Llm;
using Arrow.Core.Prepare;
using Arrow.Core.Prompts;
using Arrow.Core.Verify;

namespace Arrow.Core.Repair;

/// <summary>Dữ liệu để dựng prompt sửa của một vòng.</summary>
public sealed record RepairPromptInput
{
    public required PromptPlan Plan { get; init; }
    public required VerifyResult BaseVerify { get; init; }
    public required string TestClassName { get; init; }
    public required string TestPackage { get; init; }
    public required string CutPath { get; init; }
    public required string CutSource { get; init; }

    /// <summary>Thông tin bổ sung mức 2 (đã dựng theo trạng thái).</summary>
    public string Level2Block { get; init; } = "";
}

/// <summary>Prompt sửa (specs/04 Mục 6): mức 1 (cũng là prompt của Fixed), mức 2, mức 3.</summary>
public static partial class RepairPrompts
{
    public static string StateDescription(VerificationState state) => state switch
    {
        VerificationState.Compile => "The test class does not compile.",
        VerificationState.Unknown => "The build failed for a reason other than a compilation error in the test class.",
        VerificationState.Discovery => "The test class compiles, but the test runner does not find or run any of its tests.",
        VerificationState.Runtime => "Some tests throw an exception when they run.",
        VerificationState.Assertion => "Some tests fail their assertions.",
        VerificationState.Regression => "The new tests pass, but existing tests in the module now fail.",
        _ => throw new ArgumentException($"Không có prompt sửa cho {state}"),
    };

    private static readonly string[] SkippedFramePrefixes =
    [
        "java.", "javax.", "jdk.", "sun.", "com.sun.", "org.junit.", "junit.", "org.testng.", "org.apache.maven.", "org.gradle.",
        "worker.org.gradle.", "org.opentest4j.", "org.assertj.", "org.hamcrest.", "org.mockito.", "net.bytebuddy.",
    ];

    /// <summary>Khối lỗi rút gọn của mức 1 (tối đa 60 dòng), theo trạng thái (specs/04 Mục 6.1).</summary>
    public static string ErrorBlock(VerifyResult verify)
    {
        var lines = new List<string>();
        switch (verify.State)
        {
            case VerificationState.Compile:
                if (verify.InvalidOutput)
                {
                    lines.Add($"The previous output was not a valid test class: {verify.InvalidReason}");
                    break;
                }

                foreach (var error in verify.CompileErrors)
                {
                    lines.Add($"line {error.Line}: {error.Message}");
                    if (error.Symbol is not null)
                    {
                        lines.Add($"  symbol: {error.Symbol}");
                    }

                    if (error.Location is not null)
                    {
                        lines.Add($"  location: {error.Location}");
                    }
                }

                break;
            case VerificationState.Unknown:
                lines.AddRange((verify.ErrorLog ?? "").Split('\n').Where(l => l.Trim().Length > 0).TakeLast(40));
                break;
            case VerificationState.Discovery:
                lines.Add("No tests were executed.");
                break;
            case VerificationState.Runtime:
                foreach (var failure in verify.Failures.Take(5))
                {
                    lines.Add($"Test method {failure.Method}: {failure.Type}: {failure.Message}");
                    lines.AddRange(failure.TraceHead.Where(IsUsefulFrame).Take(15).Select(f => "    " + f.Trim()));
                }

                break;
            case VerificationState.Assertion:
                foreach (var failure in verify.Failures.Take(5))
                {
                    lines.Add($"Test method {failure.Method}: {failure.Message}");
                }

                break;
            case VerificationState.Regression:
                lines.Add("Existing tests that now fail:");
                lines.AddRange(verify.NewFailures.Take(10).Select(f => $"- {f.Id.Replace('#', '.')}"));
                break;
        }

        return string.Join('\n', lines.Take(60));
    }

    private static bool IsUsefulFrame(string line)
    {
        var trimmed = line.Trim();
        if (!trimmed.StartsWith("at ", StringComparison.Ordinal))
        {
            return false;
        }

        var frame = trimmed[3..];
        var slash = frame.IndexOf('/');
        if (slash > 0 && slash < frame.IndexOf('(') && !frame[..slash].Contains('.'))
        {
            frame = frame[(slash + 1)..]; // java.base/java.util… → java.util…
        }

        return !SkippedFramePrefixes.Any(p => frame.StartsWith(p, StringComparison.Ordinal));
    }

    /// <summary>Messages của vòng sửa: system + mức 1/2/3.</summary>
    public static List<ChatMessage> Messages(PromptLibrary library, RepairPromptInput input)
    {
        var plan = input.Plan;
        var values = new Dictionary<string, object?>
        {
            ["state_description"] = StateDescription(plan.Base.State),
            ["error_block"] = ErrorBlock(input.BaseVerify),
            ["test_class_name"] = input.TestClassName,
            ["test_package"] = input.TestPackage,
            ["test_source"] = plan.Base.Code.TrimEnd(),
            ["cut_path"] = input.CutPath,
            ["cut_source"] = input.CutSource.TrimEnd(),
        };
        var template = "repair/level1";
        if (plan.Level == 2)
        {
            template = "repair/level2";
            values["level2_block"] = input.Level2Block.Length == 0 ? "(none)" : input.Level2Block;
            values["attempts"] = plan.Attempts
                .Select(a => (IReadOnlyDictionary<string, object?>)new Dictionary<string, object?> { ["round"] = a.Round, ["outcome"] = a.Outcome })
                .ToList();
        }
        else if (plan.Level == 3)
        {
            template = "repair/level3";
            values["failing_methods"] = string.Join(" ", plan.FailingMethods.Select(m => $"`{m}`"));
        }

        return library.Messages("repair/system", template, values);
    }

    // ── Thông tin bổ sung mức 2 phần thuần (specs/04 Mục 6.2) ─────────

    /// <summary>DISCOVERY: khung test class mẫu đúng framework, package và tên file theo test path.</summary>
    public static string DiscoveryBlock(string framework, string package, string className, string testPath)
    {
        var builder = new StringBuilder();
        builder.AppendLine($"The test file must be {testPath}, with package {package} and class {className}.");
        builder.AppendLine($"A minimal test class that the {FrameworkName(framework)} runner of this repository discovers:");
        builder.AppendLine("```java");
        if (package.Length > 0)
        {
            builder.AppendLine($"package {package};").AppendLine();
        }

        switch (framework)
        {
            case "junit4":
                builder.AppendLine("import static org.junit.Assert.*;").AppendLine().AppendLine("import org.junit.Test;").AppendLine();
                builder.AppendLine($"public class {className} {{").AppendLine("    @Test").AppendLine("    public void example() {").AppendLine("        assertTrue(1 + 1 == 2);").AppendLine("    }").AppendLine("}");
                break;
            case "testng":
                builder.AppendLine("import static org.testng.Assert.*;").AppendLine().AppendLine("import org.testng.annotations.Test;").AppendLine();
                builder.AppendLine($"public class {className} {{").AppendLine("    @Test").AppendLine("    public void example() {").AppendLine("        assertTrue(1 + 1 == 2);").AppendLine("    }").AppendLine("}");
                break;
            default:
                builder.AppendLine("import static org.junit.jupiter.api.Assertions.*;").AppendLine().AppendLine("import org.junit.jupiter.api.Test;").AppendLine();
                builder.AppendLine($"class {className} {{").AppendLine("    @Test").AppendLine("    void example() {").AppendLine("        assertTrue(1 + 1 == 2);").AppendLine("    }").AppendLine("}");
                break;
        }

        builder.Append("```");
        return builder.ToString();
    }

    public static string FrameworkName(string framework) => framework switch { "junit4" => "JUnit 4", "junit5" => "JUnit 5", "testng" => "TestNG", _ => framework };

    /// <summary>REGRESSION: tên và lỗi của test sẵn có bị fail; gợi ý trạng thái dùng chung.</summary>
    public static string RegressionBlock(VerifyResult verify)
    {
        var lines = new List<string> { "Existing tests that fail only when the new test class runs in the same module run:" };
        lines.AddRange(verify.NewFailures.Take(10).Select(f => $"- {f.Id.Replace('#', '.')}: {f.Type} {Short(f.Message)}".TrimEnd()));
        lines.Add("These tests passed before the new test class was added. The new tests probably change shared state that the existing tests rely on:");
        lines.Add("static fields or singletons, files or directories, system properties, environment, locale or time zone, or global configuration.");
        lines.Add("Do not leave such state changed after a test: avoid it, or restore it in a cleanup method.");
        return string.Join('\n', lines);
    }

    [GeneratedRegex(@"expected:?\s*<(?<expected>.*?)>\s*but was:?\s*<(?<actual>.*?)>", RegexOptions.Singleline)]
    private static partial Regex ExpectedActual();

    /// <summary>ASSERTION: giá trị expected và actual của từng assertion lỗi.</summary>
    public static string ExpectedActualLines(VerifyResult verify)
    {
        var lines = new List<string>();
        foreach (var failure in verify.Failures.Where(f => f.Kind == "assertion").Take(5))
        {
            var match = ExpectedActual().Match(failure.Message ?? "");
            lines.Add(match.Success
                ? $"- {failure.Method}: expected <{match.Groups["expected"].Value}> but was <{match.Groups["actual"].Value}>"
                : $"- {failure.Method}: {Short(failure.Message)}");
        }

        return string.Join('\n', lines);
    }

    /// <summary>COMPILE: dependencies của module và Java version.</summary>
    public static string DependenciesAndJava(RepoContext context, int max = 40)
    {
        var deps = context.Dependencies.Select(d => $"{d.Group}:{d.Artifact}:{d.Version} ({d.Scope})").ToList();
        var lines = new List<string> { $"Java version: {context.JavaVersion.JdkMajor}", "Dependencies of the module:" };
        lines.AddRange(deps.Take(max).Select(d => "- " + d));
        if (deps.Count > max)
        {
            lines.Add($"- … and {deps.Count - max} more");
        }

        return string.Join('\n', lines);
    }

    /// <summary>[D6] UNKNOWN phần (a): khối "Failed to execute goal"/"What went wrong" kèm "Caused by:" đã bỏ trùng, tối đa 60 dòng.</summary>
    public static string UnknownFailureBlock(string log)
    {
        var lines = log.Replace("\r\n", "\n").Split('\n');
        var start = Array.FindIndex(lines, l => l.Contains("Failed to execute goal", StringComparison.Ordinal) || l.Contains("* What went wrong", StringComparison.Ordinal));
        if (start < 0)
        {
            return string.Join('\n', lines.Where(l => l.Contains("[ERROR]", StringComparison.Ordinal)).Take(60));
        }

        var block = new List<string>();
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var line in lines.Skip(start))
        {
            var trimmed = line.TrimEnd();
            if (trimmed.StartsWith("[ERROR] -> [Help", StringComparison.Ordinal) || trimmed.StartsWith("* Try:", StringComparison.Ordinal))
            {
                break;
            }

            if (trimmed.Contains("Caused by:", StringComparison.Ordinal) && !seen.Add(trimmed.Trim()))
            {
                continue;
            }

            block.Add(trimmed);
            if (block.Count >= 60)
            {
                break;
            }
        }

        return string.Join('\n', block).Trim();
    }

    private static readonly (string Marker, string Fact)[] UnknownSignals =
    [
        ("The forked VM terminated", "the forked test JVM terminated"),
        ("ExecutionException The forked VM", "the forked test JVM terminated"),
        ("OutOfMemoryError", "OutOfMemoryError"),
        ("annotation processing", "annotation processing failed"),
        ("Unsupported class file major version", "unsupported class file major version"),
        ("UnsupportedClassVersionError", "UnsupportedClassVersionError"),
        ("No tests to run", "no tests to run"),
        ("TestEngine with ID", "test engine failed to discover tests"),
        ("ClassNotFoundException", "ClassNotFoundException"),
        ("NoClassDefFoundError", "NoClassDefFoundError"),
        ("There was an error in the forked process", "error in the forked process"),
        ("Could not resolve", "dependency resolution failed"),
    ];

    /// <summary>[D6] UNKNOWN phần (d): mỗi dấu hiệu nhận ra trong log một dòng, chỉ nêu sự kiện.</summary>
    public static string UnknownSignalsBlock(string log) =>
        string.Join('\n', UnknownSignals.Where(s => log.Contains(s.Marker, StringComparison.OrdinalIgnoreCase)).Select(s => "- " + s.Fact).Distinct());

    /// <summary>[D6] UNKNOWN phần (c): test engine có trên classpath test và Java version.</summary>
    public static string TestEnginesBlock(RepoContext context)
    {
        var engines = context.Dependencies
            .Where(d => d.Artifact is "junit-jupiter-engine" or "junit-vintage-engine" or "junit" or "testng" or "junit-platform-launcher" or "junit-jupiter")
            .Select(d => $"{d.Group}:{d.Artifact}:{d.Version}").Distinct().ToList();
        return $"Test engines on the test classpath: {(engines.Count == 0 ? "none found" : string.Join(", ", engines))}\nJava version: {context.JavaVersion.JdkMajor}";
    }

    /// <summary>[D6] UNKNOWN phần (b): cấu hình bước test lấy từ file build (Surefire/compiler hoặc task test của Gradle).</summary>
    public static string TestConfigBlock(string buildFileText, bool maven)
    {
        if (maven)
        {
            var blocks = new List<string>();
            foreach (var plugin in new[] { "maven-surefire-plugin", "maven-compiler-plugin" })
            {
                var match = Regex.Match(buildFileText, $@"<plugin>\s*(?:<groupId>[^<]*</groupId>\s*)?<artifactId>{plugin}</artifactId>.*?</plugin>", RegexOptions.Singleline);
                blocks.Add(match.Success ? match.Value : $"<!-- {plugin}: not configured in the module POM (Maven default) -->");
            }

            return string.Join('\n', blocks);
        }

        var test = Regex.Match(buildFileText, @"(?:tasks\.)?test\s*(?:\(|\{)[^{}]*(?:\{[^{}]*\}[^{}]*)*\}", RegexOptions.Singleline);
        return test.Success ? test.Value : "// test task: not configured in the build file (Gradle default)";
    }

    private static string Short(string? text) => text is null ? "" : text.Length > 200 ? text[..200] + "…" : text;
}
