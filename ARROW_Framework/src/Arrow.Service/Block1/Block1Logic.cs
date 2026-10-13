using System.Text;
using System.Text.RegularExpressions;
using Arrow.Models.Block1;
using Arrow.Models.Common;

namespace Arrow.Service.Block1;

/// <summary>Đường dẫn source/test và tên test sinh ra (spec 01 Mục 4.6–4.7). Mọi đường dẫn tính từ gốc repository, dùng <c>/</c>.</summary>
public static class PathCalculator
{
    public static (string Package, string SimpleName) SplitFqn(string fqn)
    {
        var dot = fqn.LastIndexOf('.');
        return dot < 0 ? ("", fqn) : (fqn[..dot], fqn[(dot + 1)..]);
    }

    /// <summary><c>class_path</c> bỏ phần <c>&lt;package path&gt;/&lt;File&gt;.java</c>; không khớp package thì lấy thư mục chứa file.</summary>
    public static string SourceRoot(string classPath, string fqn)
    {
        var path = BuildRootRule.NormalizeRelative(classPath);
        var (package, simple) = SplitFqn(fqn);
        var suffix = (package.Length == 0 ? "" : package.Replace('.', '/') + "/") + simple + ".java";
        if (path == suffix)
        {
            return ".";
        }

        if (path.EndsWith("/" + suffix, StringComparison.Ordinal))
        {
            return path[..^(suffix.Length + 1)];
        }

        var slash = path.LastIndexOf('/');
        return slash < 0 ? "." : path[..slash];
    }

    /// <summary>Module root suy từ source root chuẩn <c>…/src/main/java</c>; source root khác chuẩn → null.</summary>
    public static string? ModuleRootFromSourceRoot(string sourceRoot)
    {
        if (sourceRoot == "src/main/java")
        {
            return ".";
        }

        return sourceRoot.EndsWith("/src/main/java", StringComparison.Ordinal) ? sourceRoot[..^"/src/main/java".Length] : null;
    }

    /// <summary>
    /// CUT phải nằm trong module root dự kiến: source root chuẩn thì module root suy ra phải bằng module root dự kiến;
    /// source root khác chuẩn thì chỉ cần nằm bên trong module root dự kiến.
    /// </summary>
    public static bool ModuleMatches(string sourceRoot, string expectedModuleRoot)
    {
        var fromCut = ModuleRootFromSourceRoot(sourceRoot);
        if (fromCut is not null)
        {
            return fromCut == expectedModuleRoot;
        }

        return expectedModuleRoot == "." || sourceRoot == expectedModuleRoot || sourceRoot.StartsWith(expectedModuleRoot + "/", StringComparison.Ordinal);
    }

    /// <summary>
    /// <c>&lt;test_root&gt;/&lt;package path&gt;/&lt;CUT&gt;&lt;suffix&gt;.java</c>; tên đã có (<paramref name="taken"/> trả true) thì thêm số: <c>_ArrowTest2</c>, <c>_ArrowTest3</c>…
    /// </summary>
    public static (string Path, string Fqn) GeneratedTest(string testRoot, string cutFqn, string suffix, Func<string, string, bool> taken)
    {
        var (package, simple) = SplitFqn(cutFqn);
        for (var n = 1; ; n++)
        {
            var name = simple + suffix + (n == 1 ? "" : n.ToString(System.Globalization.CultureInfo.InvariantCulture));
            var path = BuildRootRule.Join(testRoot, (package.Length == 0 ? "" : package.Replace('.', '/') + "/") + name + ".java");
            if (!taken(path, name))
            {
                return (path, package.Length == 0 ? name : $"{package}.{name}");
            }
        }
    }

    /// <summary>Tối đa <paramref name="limit"/> package, ưu tiên package của CUT, rồi package cha/con của nó, rồi theo thứ tự chữ cái.</summary>
    public static IReadOnlyList<string> RankPackages(IEnumerable<string> packages, string cutPackage, int limit = 30)
    {
        static bool Related(string a, string b) => a == b || a.StartsWith(b + ".", StringComparison.Ordinal) || b.StartsWith(a + ".", StringComparison.Ordinal);
        return packages.Distinct()
            .OrderBy(p => p == cutPackage ? 0 : Related(p, cutPackage) ? 1 : 2)
            .ThenBy(p => p, StringComparer.Ordinal)
            .Take(limit)
            .ToList();
    }
}

/// <summary>Package của một file Java đọc bằng biểu thức chính quy (đủ cho test người viết; không cần java-helper).</summary>
public static partial class JavaPackage
{
    [GeneratedRegex(@"^\s*package\s+([\w.]+)\s*;", RegexOptions.Multiline)]
    private static partial Regex PackageLine();

    [GeneratedRegex(@"/\*.*?\*/", RegexOptions.Singleline)]
    private static partial Regex BlockComment();

    public static string Of(string source)
    {
        var match = PackageLine().Match(BlockComment().Replace(source, ""));
        return match.Success ? match.Groups[1].Value : "";
    }
}

/// <summary>Gộp kết quả các lần chạy test sẵn có (spec 01 Mục 6.2) và tính tiêu chí B (Mục 6.3).</summary>
public static class BaselineEvaluator
{
    /// <summary>
    /// Pass ở mọi lần → <c>passing_in_all_runs</c>; fail/error ở mọi lần → <c>failing_in_all_runs</c>;
    /// còn lại (lúc pass lúc fail, hoặc có lần không xuất hiện) → <c>flaky</c>. Test bị skip ở mọi lần không thuộc nhóm nào.
    /// </summary>
    public static (List<string> Passing, List<string> Flaky, List<string> Failing) Classify(IReadOnlyList<IReadOnlyList<TestCaseResult>> runs)
    {
        var perRun = runs.Select(run => run.GroupBy(t => t.Id).ToDictionary(g => g.Key, g => Worst(g.Select(t => t.Status)))).ToList();
        var ids = perRun.SelectMany(r => r.Keys).Distinct().OrderBy(id => id, StringComparer.Ordinal);
        List<string> passing = [], flaky = [], failing = [];
        foreach (var id in ids)
        {
            var statuses = perRun.Select(r => r.TryGetValue(id, out var s) ? s : (TestCaseStatus?)null).ToList();
            if (statuses.All(s => s == TestCaseStatus.Skipped || s is null) && statuses.Any(s => s == TestCaseStatus.Skipped))
            {
                continue;
            }

            if (statuses.All(s => s == TestCaseStatus.Passed))
            {
                passing.Add(id);
            }
            else if (statuses.All(s => s is TestCaseStatus.Failed or TestCaseStatus.Error))
            {
                failing.Add(id);
            }
            else
            {
                flaky.Add(id);
            }
        }

        return (passing, flaky, failing);
    }

    /// <summary>Tiêu chí B: các test của class <paramref name="pairedClass"/> (và class lồng của nó) ở từng lần chạy.</summary>
    public static PairedTestResult Paired(IReadOnlyList<IReadOnlyList<TestCaseResult>> runs, string? pairedClass)
    {
        if (pairedClass is null)
        {
            return new PairedTestResult { Class = null, TestsRun = [], Status = PairedTestStatus.NoPairedTest };
        }

        bool Belongs(TestCaseResult t) => t.ClassName == pairedClass || t.ClassName.StartsWith(pairedClass + "$", StringComparison.Ordinal);
        var paired = runs.Select(run => run.Where(Belongs).Where(t => t.Status != TestCaseStatus.Skipped).ToList()).ToList();
        var testsRun = paired.Select(r => r.Select(t => t.Id).Distinct().Count()).ToList();
        var (passing, flaky, failing) = Classify(paired.Select(r => (IReadOnlyList<TestCaseResult>)r).ToList());

        PairedTestStatus status;
        if (testsRun.All(n => n == 0))
        {
            status = PairedTestStatus.NoTestRun;
        }
        else if (failing.Count > 0)
        {
            status = PairedTestStatus.Failing;
        }
        else if (flaky.Count > 0 || testsRun.Any(n => n == 0))
        {
            status = PairedTestStatus.Flaky;
        }
        else
        {
            status = passing.Count > 0 ? PairedTestStatus.Pass : PairedTestStatus.NoTestRun;
        }

        return new PairedTestResult { Class = pairedClass, TestsRun = testsRun, Status = status };
    }

    private static TestCaseStatus Worst(IEnumerable<TestCaseStatus> statuses)
    {
        var list = statuses.ToList();
        return list.Contains(TestCaseStatus.Error) ? TestCaseStatus.Error
            : list.Contains(TestCaseStatus.Failed) ? TestCaseStatus.Failed
            : list.Contains(TestCaseStatus.Passed) ? TestCaseStatus.Passed
            : TestCaseStatus.Skipped;
    }
}

/// <summary>Khối M dạng chữ điền vào <c>{{repository_configuration}}</c> (spec 02 Mục 2.2), sinh từ <c>metadata.json</c>.</summary>
public static class MetadataPromptRenderer
{
    public const int MaxDependencies = 25;
    public const int MaxModules = 15;

    public static string Render(RepositoryMetadata m)
    {
        var text = new StringBuilder();
        var build = m.BuildTool;
        var structure = m.ProjectStructure;
        var tool = build.Kind == "gradle" ? "Gradle" : "Maven";
        var scope = structure.MultiModule
            ? build.Kind == "gradle"
                ? $"project {build.ReactorModule ?? ":"}, multi-project build"
                : $"module {structure.TargetModule}, multi-module project"
            : "single-module project";
        text.Append($"- Build tool: {tool}{(build.Version.Length > 0 ? " " + build.Version : "")} ({scope}, build root {build.BuildRoot})\n");

        var level = m.Java.Major ?? m.Java.JdkMajor;
        text.Append($"- Java version: {level} (compile with JDK {m.Java.JdkMajor}; do not use language features or APIs newer than Java {level})\n");

        var framework = m.TestFramework;
        var name = framework.Name switch { "junit5" => "JUnit 5", "testng" => "TestNG", _ => "JUnit 4" };
        var frameworkText = name + (framework.Version.Length > 0 ? $" ({framework.Version})" : "") + (framework.Vintage ? ", JUnit 4 tests also run through the vintage engine" : "");
        var mocking = framework.Mocking.Count == 0 ? "none" : string.Join(", ", framework.Mocking.Select(Library).Distinct());
        var assertions = framework.AssertionLibs.Count == 0 ? $"none beyond {(framework.Name == "testng" ? "TestNG" : "JUnit")}" : string.Join(", ", framework.AssertionLibs.Select(Library).Distinct());
        text.Append($"- Test framework: {frameworkText}; mocking: {mocking}; assertions: {assertions}\n");

        var dependencies = m.Dependencies.Test.Concat(m.Dependencies.Direct).Select(d => $"{d.Group}:{d.Artifact}{(d.Version.Length > 0 ? ":" + d.Version : "")}").Distinct().ToList();
        var shown = dependencies.Take(MaxDependencies).ToList();
        var more = dependencies.Count > shown.Count ? $", … (+{dependencies.Count - shown.Count} more)" : "";
        text.Append($"- Dependencies (direct): {(shown.Count == 0 ? "none" : string.Join(", ", shown) + more)}\n");

        var modules = structure.MultiModule
            ? "modules " + string.Join(", ", structure.Modules.Take(MaxModules)) + (structure.Modules.Count > MaxModules ? $", … (+{structure.Modules.Count - MaxModules} more)" : "")
            : "single module";
        text.Append($"- Project structure: {modules}; CUT package {(m.Cut.Package.Length == 0 ? "(default package)" : m.Cut.Package)}\n");
        text.Append($"- Source path: {m.Paths.SourceRoot}\n");
        text.Append($"- Test path: {m.Paths.GeneratedTestPath}");
        return text.ToString();
    }

    /// <summary><c>org.mockito:mockito-core:3.12.4</c> → <c>Mockito 3.12.4</c>.</summary>
    public static string Library(string coordinates)
    {
        var parts = coordinates.Split(':');
        var artifact = parts.Length > 1 ? parts[1] : coordinates;
        var version = parts.Length > 2 ? " " + parts[2] : "";
        var name = artifact switch
        {
            _ when artifact.StartsWith("mockito", StringComparison.Ordinal) => "Mockito",
            _ when artifact.StartsWith("powermock", StringComparison.Ordinal) => "PowerMock",
            "easymock" => "EasyMock",
            "jmockit" => "JMockit",
            "assertj-core" => "AssertJ",
            "truth" => "Truth",
            _ when artifact.StartsWith("hamcrest", StringComparison.Ordinal) => "Hamcrest",
            _ => artifact,
        };
        return name + version;
    }
}
