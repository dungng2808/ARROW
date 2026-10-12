using System.Text;
using System.Text.RegularExpressions;
using Arrow.Core.Llm;
using Arrow.Core.Prepare;
using Arrow.Core.Util;

namespace Arrow.Core.Prompts;

/// <summary>Một cặp ví dụ few-shot (specs/04 Mục 5).</summary>
public sealed record FewShotExample
{
    public string TaskId { get; init; } = "";
    public string RepoUrl { get; init; } = "";
    public string CheckoutSha { get; init; } = "";
    public string TestingFramework { get; init; } = "";
    public string CutPath { get; init; } = "";
    public string TestPath { get; init; } = "";
    public string CutSource { get; init; } = "";
    public string TestSource { get; init; } = "";
    public string CutSha256 { get; init; } = "";
    public string TestSha256 { get; init; } = "";
}

/// <summary><c>prompts/fewshot/examples.json</c>.</summary>
public sealed record FewShotSet
{
    public int SchemaVersion { get; init; } = 1;
    public string Rule { get; init; } = "";
    public int Seed { get; init; } = 42;
    public List<string> ExcludedManifests { get; init; } = [];
    public int Candidates { get; init; }
    public List<FewShotExample> Examples { get; init; } = [];
}

/// <summary>Thông tin của một CUT để dựng prompt sinh test.</summary>
public sealed record GenerationInput
{
    public required string TestClassName { get; init; }
    public required string TestPackage { get; init; }
    public required string CutPath { get; init; }
    public required string CutSource { get; init; }
    public required string TestFilePath { get; init; }
    public RepoContext? RepoContext { get; init; }
}

/// <summary>
/// Bộ template của <c>prompts/</c> (specs/04 Mục 2): đọc mọi file <c>.md</c>, tính hash từng file để ghi vào
/// <c>run_config.lock.json</c>, dựng messages cho bước sinh test và bước sửa.
/// </summary>
public sealed class PromptLibrary
{
    public const string ZeroShot = "zero_shot";
    public const string FewShotStrategy = "few_shot";
    public const string ProjectAware = "zero_shot_project_aware";

    private readonly Dictionary<string, PromptTemplate> _templates;

    private PromptLibrary(Dictionary<string, PromptTemplate> templates, FewShotSet? fewShot)
    {
        _templates = templates;
        Examples = fewShot;
    }

    public FewShotSet? Examples { get; }

    /// <summary>Hash của từng file template (và examples.json) theo đường dẫn tương đối, cho run_config.lock.json.</summary>
    public Dictionary<string, string> Hashes { get; private init; } = [];

    public static PromptLibrary Load(string promptsDir)
    {
        var templates = new Dictionary<string, PromptTemplate>(StringComparer.Ordinal);
        var hashes = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var file in Directory.EnumerateFiles(promptsDir, "*.md", SearchOption.AllDirectories).OrderBy(f => f, StringComparer.Ordinal))
        {
            var relative = Path.GetRelativePath(promptsDir, file).Replace('\\', '/');
            var template = TemplateEngine.Parse(File.ReadAllText(file, Encoding.UTF8), relative);
            templates[relative[..^3]] = template;
            hashes[relative] = template.Sha256;
        }

        var examplesPath = Path.Combine(promptsDir, "fewshot", "examples.json");
        FewShotSet? fewShot = null;
        if (File.Exists(examplesPath))
        {
            fewShot = Json.Read<FewShotSet>(examplesPath);
            hashes["fewshot/examples.json"] = Hashing.Sha256File(examplesPath);
        }

        return new PromptLibrary(templates, fewShot) { Hashes = hashes };
    }

    public PromptTemplate Template(string id) =>
        _templates.GetValueOrDefault(id) ?? throw new ArrowException("PROMPT_MISSING", $"Không có template prompts/{id}.md");

    public List<ChatMessage> Messages(string systemId, string userId, IReadOnlyDictionary<string, object?> values)
    {
        var system = Template(systemId);
        var user = Template(userId);
        return
        [
            new ChatMessage("system", TemplateEngine.Render(system.Body, values)),
            new ChatMessage("user", TemplateEngine.Render(user.Body, values)),
        ];
    }

    /// <summary>Prompt sinh test cho một quy trình (specs/04 Mục 4, D3: zero_shot và few_shot không có thông tin repository).</summary>
    public List<ChatMessage> GenerationMessages(string strategy, GenerationInput input)
    {
        var values = new Dictionary<string, object?>
        {
            ["test_class_name"] = input.TestClassName,
            ["test_package"] = input.TestPackage,
            ["cut_path"] = input.CutPath,
            ["cut_source"] = input.CutSource,
        };
        switch (strategy)
        {
            case ZeroShot:
                break;
            case FewShotStrategy:
                if (Examples is null || Examples.Examples.Count == 0)
                {
                    throw new ArrowException("FEWSHOT_MISSING", "Chưa có prompts/fewshot/examples.json: chạy arrow fewshot select");
                }

                values["examples"] = Examples.Examples
                    .Select(e => (IReadOnlyDictionary<string, object?>)new Dictionary<string, object?> { ["cut_source"] = e.CutSource.TrimEnd(), ["test_source"] = e.TestSource.TrimEnd() })
                    .ToList();
                break;
            case ProjectAware:
                values["repo_context"] = RepoContextFormatter.Format(input.RepoContext ?? throw new ArgumentException("project-aware cần repo_context"), input.CutSource);
                values["test_file_path"] = input.TestFilePath;
                break;
            default:
                throw new ArrowException("STRATEGY_UNKNOWN", $"Quy trình prompting không biết: {strategy}");
        }

        return Messages("generation/system", "generation/" + strategy, values);
    }
}

/// <summary>Khối <c>{{repo_context}}</c> (specs/04 Mục 4.5).</summary>
public static partial class RepoContextFormatter
{
    private static readonly string[] TestLibraryGroups =
    [
        "junit", "org.junit", "org.junit.jupiter", "org.junit.platform", "org.junit.vintage", "org.testng", "org.mockito", "org.assertj",
        "org.hamcrest", "io.rest-assured", "org.powermock", "org.easymock", "com.google.truth", "org.springframework.boot:spring-boot-starter-test",
    ];

    [GeneratedRegex(@"^\s*import\s+(?:static\s+)?([\w.]+)\s*;", RegexOptions.Multiline)]
    private static partial Regex ImportLine();

    public static string Format(RepoContext context, string cutSource, int maxDependencyLines = 40)
    {
        var lines = new List<string>();
        var build = context.BuildTool;
        var buildName = build.Name == "maven" ? "Maven" : build.Name == "gradle" ? "Gradle" : build.Name;
        var moduleName = ModuleOf(context.SourcePath);
        lines.Add($"- Build tool: {buildName} {build.Version} (wrapper: {(build.Wrapper ? "yes" : "no")}){(moduleName == "." ? "" : $", module: {moduleName}")}");

        var java = context.JavaVersion;
        var declared = java.Release is not null ? $"release={java.Release}" : java.Target is not null ? $"target={java.Target}" : java.Source is not null ? $"source={java.Source}" : null;
        lines.Add($"- Java version: {java.JdkMajor}{(declared is null ? "" : $" ({declared})")}");

        var framework = context.TestFramework;
        var frameworkName = framework.Name switch { "junit5" => "JUnit 5", "junit4" => "JUnit 4", "testng" => "TestNG", _ => framework.Name };
        var package = framework.Name switch { "junit5" => "org.junit.jupiter", "junit4" => "org.junit", "testng" => "org.testng", _ => "" };
        lines.Add($"- Test framework: {frameworkName}{(framework.Version is null ? "" : " " + framework.Version)}{(package.Length == 0 ? "" : $" ({package})")}");
        if (framework.Libraries.Count > 0)
        {
            lines.Add("- Other test libraries available: " + string.Join(", ", framework.Libraries.Select(Pretty)));
        }

        // Dependencies: thư viện scope test liên quan tới test, và thư viện mà CUT import (khớp theo package).
        var imports = ImportLine().Matches(cutSource).Select(m => m.Groups[1].Value).ToList();
        var relevant = context.Dependencies
            .Where(d => d.Scope == "test" && TestLibraryGroups.Any(g => g.Contains(':') ? $"{d.Group}:{d.Artifact}" == g : d.Group == g || d.Group.StartsWith(g + ".", StringComparison.Ordinal))
                        || imports.Any(i => i.StartsWith(d.Group + ".", StringComparison.Ordinal) || i == d.Group))
            .Select(d => $"{d.Group}:{d.Artifact}:{d.Version}")
            .Distinct()
            .ToList();
        if (relevant.Count > 0)
        {
            var shown = relevant.Take(maxDependencyLines).ToList();
            lines.Add("- Dependencies used by the class under test and the tests: " + string.Join(", ", shown)
                      + (relevant.Count > shown.Count ? $", … and {relevant.Count - shown.Count} more" : ""));
        }

        var structure = context.ProjectStructure;
        var kind = structure.Modules.Count > 0 ? $"multi-module {buildName} project (modules: {string.Join(", ", structure.Modules.Take(8))}{(structure.Modules.Count > 8 ? ", …" : "")})" : $"single-module {buildName} project";
        var testRoot = TestRoot(context.TestPath);
        var mainRoot = MainRoot(context.SourcePath);
        lines.Add($"- Project structure: {kind};");
        lines.Add($"  main sources in {mainRoot}, tests in {testRoot}");
        lines.Add($"- Source path of the class under test: {context.SourcePath}");
        lines.Add($"- Test path for the new test: {context.TestPath}");
        return string.Join('\n', lines);
    }

    private static string Pretty(string library)
    {
        var parts = library.Split(':');
        return parts.Length >= 3 ? $"{parts[1]} {parts[2]}" : library;
    }

    private static string ModuleOf(string sourcePath)
    {
        var index = sourcePath.IndexOf("src/main/java", StringComparison.Ordinal);
        return index <= 0 ? "." : sourcePath[..(index - 1)];
    }

    private static string MainRoot(string sourcePath)
    {
        var index = sourcePath.IndexOf("src/main/java", StringComparison.Ordinal);
        return index < 0 ? Path.GetDirectoryName(sourcePath)?.Replace('\\', '/') ?? "" : sourcePath[..(index + "src/main/java".Length)];
    }

    private static string TestRoot(string testPath)
    {
        var index = testPath.IndexOf("src/test/java", StringComparison.Ordinal);
        return index < 0 ? Path.GetDirectoryName(testPath)?.Replace('\\', '/') ?? "" : testPath[..(index + "src/test/java".Length)];
    }
}
