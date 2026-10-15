using System.IO.Compression;
using System.Text;
using System.Text.RegularExpressions;
using Arrow.Models.Block1;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block1;
using Arrow.Service.Block2;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block3;

/// <summary>Dữ liệu của task để dựng thông tin bổ sung: M, mã CUT, snapshot (đọc mã nguồn dự án), classpath test và dependency đã phân giải.</summary>
public sealed record ExtraContextInput(
    RepositoryMetadata Metadata,
    string CutSource,
    string SnapshotDir,
    IReadOnlyList<string> TestClasspath,
    IReadOnlyList<string> ResolvedDependencies);

/// <summary>
/// <c>extra_context</c> của prompt sửa mức 2 và 3 theo trạng thái của checkpoint (spec 03 Mục 6.5), tối đa 200 dòng:
/// COMPILE: API thật của CUT và các class trong lỗi (package thật của jar test khi <c>package does not exist</c>);
/// UNKNOWN: khối lỗi build đầy đủ và cấu hình build; DISCOVERY: khung test class đúng framework; RUNTIME: mã method trong stack trace,
/// constructor và field của CUT, nhắc mock; ASSERTION: expected/actual, mã method CUT được gọi; REGRESSION: test bị hỏng, field static,
/// tài nguyên dùng chung test chạm tới.
/// </summary>
public sealed partial class ExtraContextBuilder(IJavaHelper javaHelper)
{
    public const int MaxLines = 200;
    public const int MaxPackages = 60;

    [GeneratedRegex(@"^\s*at\s+(?:[\w.\-]+/+)?(?<cls>[\p{L}_$][\p{L}\p{N}_$.]*)\.(?<method>[\p{L}_$<][\p{L}\p{N}_$>]*)\(")]
    private static partial Regex Frame();

    [GeneratedRegex(@"expected:?\s*<(?<e>.*?)>\s*but was:?\s*<(?<a>.*?)>", RegexOptions.Singleline)]
    private static partial Regex ExpectedAngle();

    [GeneratedRegex(@"expected:?\s*(?<e>.+?)\s+but was:?\s*(?<a>.+)", RegexOptions.Singleline)]
    private static partial Regex ExpectedPlain();

    [GeneratedRegex(@"System\.(setProperty|clearProperty|setOut|setErr|setIn|exit)\b|Locale\.setDefault|TimeZone\.setDefault|new\s+File\s*\(|Paths\.get\s*\(|Path\.of\s*\(|Files\.\w+\s*\(|FileWriter|FileOutputStream")]
    private static partial Regex SharedResource();

    public async Task<string> BuildAsync(ChainVersion best, ExtraContextInput input, CancellationToken cancellationToken)
    {
        string text;
        try
        {
            text = best.State switch
            {
                VerificationState.Compile => await CompileAsync(best, input, cancellationToken).ConfigureAwait(false),
                VerificationState.Unknown => Unknown(best, input.Metadata),
                VerificationState.Discovery => Discovery(input.Metadata),
                VerificationState.Runtime => await RuntimeAsync(best, input, cancellationToken).ConfigureAwait(false),
                VerificationState.Assertion => await AssertionAsync(best, input, cancellationToken).ConfigureAwait(false),
                VerificationState.Regression => await RegressionAsync(best, input, cancellationToken).ConfigureAwait(false),
                _ => "",
            };
        }
        catch (ArrowException ex)
        {
            text = $"(additional information could not be extracted: {ex.Message})";
        }

        return FailureDiagnoser.Limit(text.TrimEnd('\n'), MaxLines);
    }

    // ── COMPILE ────────────────────────────────────────────────────────

    private async Task<string> CompileAsync(ChainVersion best, ExtraContextInput input, CancellationToken ct)
    {
        var m = input.Metadata;
        var errors = best.Verify.CompileErrors.Where(e => e.InGeneratedFile).ToList();
        // Class nêu trong lỗi (JavacErrorParser.MentionedTypes): "location: class X", "variable s of type X", "constructor A in class X",
        // "x has private access in X", "X is abstract; cannot be instantiated".
        var mentioned = errors
            .SelectMany(JavacErrorParser.MentionedTypes)
            .Select(StripGenerics)
            .Where(n => n != m.Cut.Fqn && !n.EndsWith("_ArrowTest", StringComparison.Ordinal))
            .Distinct(StringComparer.Ordinal)
            .ToList();

        var sources = new List<string> { input.CutSource };
        var classes = new List<string>();
        foreach (var name in mentioned)
        {
            if (FindProjectSource(name, input) is { } source)
            {
                sources.Add(source);
            }
            else if (name.Contains('.'))
            {
                classes.Add(name);
            }
        }

        var api = await javaHelper.ClassApiAsync(sources, classes, input.TestClasspath, ct).ConfigureAwait(false);
        var text = new StringBuilder();
        var wanted = new HashSet<string>(mentioned.Append(m.Cut.Fqn), StringComparer.Ordinal);
        foreach (var type in api.Types.Where(t => wanted.Contains(t.Name) || t.Name == m.Cut.Fqn || wanted.Any(w => !w.Contains('.') && t.Name.EndsWith("." + w, StringComparison.Ordinal))))
        {
            text.Append($"Constructors, methods and fields of {type.Name}:\n");
            foreach (var member in type.Members)
            {
                text.Append("  ").Append(member).Append('\n');
            }

            if (type.Members.Count == 0)
            {
                text.Append("  (no accessible members)\n");
            }
        }

        if (errors.Any(e => e.Kind == "package does not exist"))
        {
            var packages = TestPackages(input);
            text.Append("Packages that exist in the project's test dependencies:\n");
            foreach (var package in packages)
            {
                text.Append("  ").Append(package).Append('\n');
            }

            if (packages.Count == 0)
            {
                text.Append("  (none found)\n");
            }
        }

        return text.ToString();
    }

    /// <summary>Package (tối đa 3 phần) có class trong các jar dependency scope <c>test</c> của dự án.</summary>
    public static IReadOnlyList<string> TestPackages(ExtraContextInput input)
    {
        var testJars = input.ResolvedDependencies
            .Select(d => d.Split(':'))
            .Where(p => p.Length >= 4 && p[^1] == "test")
            .Select(p => $"{p[1]}-{p[2]}")
            .ToHashSet(StringComparer.Ordinal);
        var packages = new SortedSet<string>(StringComparer.Ordinal);
        foreach (var jar in input.TestClasspath.Where(j => j.EndsWith(".jar", StringComparison.OrdinalIgnoreCase) && File.Exists(j)))
        {
            var name = Path.GetFileNameWithoutExtension(jar);
            if (testJars.Count > 0 && !testJars.Any(t => name.StartsWith(t, StringComparison.Ordinal)))
            {
                continue;
            }

            try
            {
                using var archive = ZipFile.OpenRead(jar);
                foreach (var entry in archive.Entries.Where(e => e.FullName.EndsWith(".class", StringComparison.Ordinal) && !e.FullName.StartsWith("META-INF/", StringComparison.Ordinal)))
                {
                    var slash = entry.FullName.LastIndexOf('/');
                    if (slash > 0)
                    {
                        packages.Add(string.Join('.', entry.FullName[..slash].Split('/').Take(3)));
                    }
                }
            }
            catch (InvalidDataException)
            {
            }
        }

        return packages.Take(MaxPackages).ToList();
    }

    // ── UNKNOWN, DISCOVERY ─────────────────────────────────────────────

    private static string Unknown(ChainVersion best, RepositoryMetadata m)
    {
        var lines = MetadataPromptRenderer.Render(m).Split('\n');
        return "Full build error:\n" + (best.Verify.BuildErrorExcerpt ?? "(no build error block)") + "\n\nBuild configuration:\n"
               + string.Join('\n', lines.Where(l => l.StartsWith("- Build tool", StringComparison.Ordinal) || l.StartsWith("- Java version", StringComparison.Ordinal) || l.StartsWith("- Test framework", StringComparison.Ordinal)));
    }

    /// <summary>Khung test class đúng framework, package và đường dẫn của test sinh ra.</summary>
    public static string Discovery(RepositoryMetadata m)
    {
        var className = PromptBuilder.TestClassName(m);
        var package = m.Cut.Package.Length > 0 ? $"package {m.Cut.Package};\n\n" : "";
        var (name, imports, method) = m.TestFramework.Name switch
        {
            "junit5" => ("JUnit 5", "import static org.junit.jupiter.api.Assertions.assertEquals;\n\nimport org.junit.jupiter.api.Test;", "    @Test\n    void exampleTest() {\n        assertEquals(2, 1 + 1);\n    }"),
            "testng" => ("TestNG", "import static org.testng.Assert.assertEquals;\n\nimport org.testng.annotations.Test;", "    @Test\n    public void exampleTest() {\n        assertEquals(1 + 1, 2);\n    }"),
            _ => ("JUnit 4", "import static org.junit.Assert.assertEquals;\n\nimport org.junit.Test;", "    @Test\n    public void exampleTest() {\n        assertEquals(2, 1 + 1);\n    }"),
        };
        return $"The project runs tests with {name}. A test class that its test runner discovers looks like this (file {m.Paths.GeneratedTestPath}):\n"
               + $"```java\n{package}{imports}\n\npublic class {className} {{\n{method}\n}}\n```";
    }

    // ── RUNTIME, ASSERTION ─────────────────────────────────────────────

    private async Task<string> RuntimeAsync(ChainVersion best, ExtraContextInput input, CancellationToken ct)
    {
        var m = input.Metadata;
        var text = new StringBuilder();
        var frames = best.Verify.Failures
            .SelectMany(f => (f.TraceHead ?? "").Split('\n'))
            .Select(l => Frame().Match(l))
            .Where(x => x.Success)
            .Select(x => (Class: x.Groups["cls"].Value.Split('$')[0], Method: x.Groups["method"].Value))
            .Where(f => !f.Class.EndsWith("_ArrowTest", StringComparison.Ordinal))
            .Distinct()
            .ToList();

        var methodSources = new List<string>();
        foreach (var group in frames.GroupBy(f => f.Class))
        {
            var source = group.Key == m.Cut.Fqn ? input.CutSource : FindProjectSource(group.Key, input);
            if (source is null)
            {
                continue;
            }

            var names = group.Select(f => f.Method).Distinct().ToList();
            var found = await javaHelper.MethodSourceAsync(source, names, ct).ConfigureAwait(false);
            methodSources.AddRange(found.Methods.Select(x => $"// {group.Key}\n{x.Source}"));
        }

        if (methodSources.Count > 0)
        {
            text.Append("Source of the project methods in the stack trace:\n```java\n").Append(string.Join("\n\n", methodSources)).Append("\n```\n");
        }

        var called = await javaHelper.CalledMethodsAsync(best.Code, best.Diagnosis.FailingMethods, input.CutSource, ct).ConfigureAwait(false);
        AppendConstruction(text, m, called);
        if (m.TestFramework.Mocking.Any(x => x.Contains("mockito", StringComparison.OrdinalIgnoreCase)))
        {
            text.Append("The project has Mockito: mock the CUT's dependencies that are hard to create or that fail at runtime, instead of constructing them.\n");
        }

        return text.ToString();
    }

    private async Task<string> AssertionAsync(ChainVersion best, ExtraContextInput input, CancellationToken ct)
    {
        var text = new StringBuilder("Expected and actual values of the failing assertions:\n");
        foreach (var failure in best.Verify.Failures.Take(FailureDiagnoser.MaxFailures))
        {
            var message = failure.Message ?? "";
            var match = ExpectedAngle().Match(message);
            if (!match.Success)
            {
                match = ExpectedPlain().Match(message);
            }

            var detail = match.Success
                ? $"expected {match.Groups["e"].Value.Trim()}, actual {match.Groups["a"].Value.Trim().Split('\n')[0]}"
                : (message.Length == 0 ? TestResultClassifier.ShortName(failure.Type ?? "assertion failed") : message.Replace("\r\n", "\n").Split('\n')[0]);
            text.Append($"- {(failure.Method.Length > 0 ? failure.Method : "(class)")}: {detail}\n");
        }

        var called = await javaHelper.CalledMethodsAsync(best.Code, best.Diagnosis.FailingMethods, input.CutSource, ct).ConfigureAwait(false);
        if (called.Methods.Count > 0)
        {
            text.Append($"Source of the {input.Metadata.Cut.SimpleName} methods called by the failing test methods:\n```java\n")
                .Append(string.Join("\n\n", called.Methods.Select(x => x.Source))).Append("\n```\n");
        }

        return text.ToString();
    }

    private static void AppendConstruction(StringBuilder text, RepositoryMetadata m, JavaCalledMethods called)
    {
        if (called.Constructors.Count > 0)
        {
            text.Append($"Constructors of {m.Cut.Fqn}:\n```java\n").Append(string.Join("\n\n", called.Constructors)).Append("\n```\n");
        }

        if (called.Fields.Count > 0)
        {
            text.Append($"Fields of {m.Cut.Fqn} and how they are initialized:\n");
            foreach (var field in called.Fields)
            {
                text.Append("  ").Append(field).Append('\n');
            }
        }
    }

    // ── REGRESSION ─────────────────────────────────────────────────────

    private async Task<string> RegressionAsync(ChainVersion best, ExtraContextInput input, CancellationToken ct)
    {
        var m = input.Metadata;
        var text = new StringBuilder("Existing tests that fail or stop running when the generated test runs with the module:\n");
        foreach (var regression in best.Verify.Regressions.Take(10))
        {
            var reason = regression.Type == "NOT_RUN" ? "was not run" : $"{regression.Type}{(string.IsNullOrWhiteSpace(regression.Message) ? "" : ": " + regression.Message.Replace("\r\n", "\n").Split('\n')[0])}";
            text.Append($"- {regression.TestId}: {reason}\n");
        }

        var api = await javaHelper.ClassApiAsync([input.CutSource], [], [], ct).ConfigureAwait(false);
        var statics = api.Types.Where(t => t.Name == m.Cut.Fqn).SelectMany(t => t.Members).Where(x => x.Contains("static ", StringComparison.Ordinal) && !x.Contains('(')).ToList();
        text.Append($"Static fields of {m.Cut.Fqn} (shared by every test in the JVM):\n");
        text.Append(statics.Count == 0 ? "  (none)\n" : string.Concat(statics.Select(s => $"  {s}\n")));

        var touched = best.Code.Replace("\r\n", "\n").Split('\n').Select(l => l.Trim()).Where(l => SharedResource().IsMatch(l)).Distinct().Take(20).ToList();
        var calledStatics = best.Code.Replace("\r\n", "\n").Split('\n').Select(l => l.Trim())
            .Where(l => l.Contains(m.Cut.SimpleName + ".", StringComparison.Ordinal) && !l.StartsWith("import", StringComparison.Ordinal)).Distinct().Take(10);
        text.Append("Code of the generated test that changes shared state (system properties, default locale/time zone, files, static members of the CUT):\n");
        var lines = touched.Concat(calledStatics).Distinct().ToList();
        text.Append(lines.Count == 0 ? "  (none found)\n" : string.Concat(lines.Select(l => $"  {l}\n")));
        return text.ToString();
    }

    // ── tiện ích ───────────────────────────────────────────────────────

    /// <summary>Mã nguồn của một class dự án trong snapshot (theo FQN, hoặc tên ngắn trong package của CUT); class lồng lấy file của class ngoài.</summary>
    private static string? FindProjectSource(string name, ExtraContextInput input)
    {
        var m = input.Metadata;
        var fqn = name.Contains('.') ? name : (m.Cut.Package.Length > 0 ? $"{m.Cut.Package}.{name}" : name);
        var outer = fqn.Split('$')[0];
        foreach (var root in new[] { m.Paths.SourceRoot, m.Paths.TestRoot }.Where(r => r.Length > 0))
        {
            var path = Path.Combine(input.SnapshotDir, root.Replace('/', Path.DirectorySeparatorChar), outer.Replace('.', Path.DirectorySeparatorChar) + ".java");
            if (File.Exists(path))
            {
                return File.ReadAllText(path).Replace("\r\n", "\n");
            }
        }

        return null;
    }

    private static string StripGenerics(string name) => name.Split('<')[0].TrimEnd('.');
}
