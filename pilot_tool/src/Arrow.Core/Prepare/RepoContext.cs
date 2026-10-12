using System.Text.RegularExpressions;

namespace Arrow.Core.Prepare;

/// <summary>6 thông tin repository đưa vào prompt (<c>repo_context.json</c>, specs/02 §7).</summary>
public sealed record RepoContext
{
    public BuildToolInfo BuildTool { get; init; } = new();
    public JavaVersionInfo JavaVersion { get; init; } = new();
    public List<Dependency> Dependencies { get; init; } = [];
    public string DependenciesSource { get; init; } = "";
    public string? DependenciesError { get; init; }
    public TestFrameworkInfo TestFramework { get; init; } = new();
    public ProjectStructure ProjectStructure { get; init; } = new();
    public string SourcePath { get; init; } = "";
    public string TestPath { get; init; } = "";
}

public sealed record BuildToolInfo
{
    public string Name { get; init; } = "";
    public bool Wrapper { get; init; }
    public string Version { get; init; } = "unknown";
}

public sealed record JavaVersionInfo
{
    public int JdkMajor { get; init; }
    public string? Source { get; init; }
    public string? Target { get; init; }
    public string? Release { get; init; }
}

public sealed record Dependency(string Group, string Artifact, string Version, string Scope)
{
    public override string ToString() => $"{Group}:{Artifact}:{Version}:{Scope}";
}

public sealed record TestFrameworkInfo
{
    public string Name { get; init; } = "";
    public string? Version { get; init; }

    /// <summary>Thư viện mock/assert có trong scope test, ví dụ <c>org.mockito:mockito-core:4.11.0</c>.</summary>
    public List<string> Libraries { get; init; } = [];
}

public sealed record ProjectStructure
{
    /// <summary>Cây thư mục của module tới độ sâu 3, chỉ thư mục, mỗi phần tử một đường dẫn tương đối.</summary>
    public List<string> Directories { get; init; } = [];
    public int MainJavaFiles { get; init; }
    public int TestJavaFiles { get; init; }

    /// <summary>Các module của build (thư mục có file build, tương đối so với thư mục gọi build), nếu nhiều module.</summary>
    public List<string> Modules { get; init; } = [];
}

/// <summary>Đọc dependency từ output của Maven/Gradle và từ file build (code thuần).</summary>
public static partial class DependencyParser
{
    [GeneratedRegex(@"^\s*([\w.\-]+):([\w.\-]+):([\w.\-]+)(?::([\w.\-]+))?:([\w.\-]+):(compile|test|runtime|provided|system)\b")]
    private static partial Regex MavenListLine();

    [GeneratedRegex(@"^[|\\+ ]*[+\\]--- ([\w.\-]+):([\w.\-]+)(?::([\w.\-]+))?(?: -> ([\w.\-]+))?")]
    private static partial Regex GradleTreeLine();

    /// <summary>File của <c>dependency:list</c>: <c>group:artifact:type[:classifier]:version:scope</c>.</summary>
    public static List<Dependency> ParseMavenList(string text)
    {
        var result = new List<Dependency>();
        foreach (var line in text.Split('\n'))
        {
            var match = MavenListLine().Match(line);
            if (match.Success)
            {
                result.Add(new Dependency(match.Groups[1].Value, match.Groups[2].Value, match.Groups[5].Value, match.Groups[6].Value));
            }
        }

        return Distinct(result);
    }

    /// <summary>Output của <c>dependencies --configuration testRuntimeClasspath</c>; phiên bản sau <c>-&gt;</c> là bản đã chọn.</summary>
    public static List<Dependency> ParseGradleTree(string text, string scope = "testRuntimeClasspath")
    {
        var result = new List<Dependency>();
        foreach (var line in text.Split('\n'))
        {
            var match = GradleTreeLine().Match(line);
            if (!match.Success || line.Contains("project :", StringComparison.Ordinal))
            {
                continue;
            }

            var version = match.Groups[4].Success ? match.Groups[4].Value : match.Groups[3].Success ? match.Groups[3].Value : "";
            result.Add(new Dependency(match.Groups[1].Value, match.Groups[2].Value, version, scope));
        }

        return Distinct(result);
    }

    /// <summary>Dự phòng khi lệnh lấy dependency lỗi: dependency khai báo trực tiếp trong file build (không resolve).</summary>
    public static List<Dependency> ParseDeclared(BuildToolKind tool, string buildFileText)
    {
        var result = new List<Dependency>();
        if (tool == BuildToolKind.Maven)
        {
            foreach (Match block in Regex.Matches(buildFileText, @"<dependency>(.*?)</dependency>", RegexOptions.Singleline))
            {
                string Tag(string name) => Regex.Match(block.Groups[1].Value, $@"<{name}>\s*([^<]+?)\s*</{name}>").Groups[1].Value;
                var group = Tag("groupId");
                var artifact = Tag("artifactId");
                if (group.Length > 0 && artifact.Length > 0)
                {
                    var scope = Tag("scope");
                    result.Add(new Dependency(group, artifact, Tag("version"), scope.Length > 0 ? scope : "compile"));
                }
            }
        }
        else
        {
            foreach (Match match in Regex.Matches(buildFileText, @"\b(\w+)\s*\(?\s*['""]([\w.\-]+):([\w.\-]+)(?::([\w.\-${}]+))?['""]"))
            {
                result.Add(new Dependency(match.Groups[2].Value, match.Groups[3].Value, match.Groups[4].Value, match.Groups[1].Value));
            }
        }

        return Distinct(result);
    }

    private static List<Dependency> Distinct(IEnumerable<Dependency> dependencies) =>
        dependencies.DistinctBy(d => (d.Group, d.Artifact, d.Version, d.Scope)).OrderBy(d => d.Group, StringComparer.Ordinal).ThenBy(d => d.Artifact, StringComparer.Ordinal).ToList();

    private static readonly Dictionary<string, (string Group, string Artifact)[]> FrameworkArtifacts = new()
    {
        ["junit4"] = [("junit", "junit")],
        ["junit5"] = [("org.junit.jupiter", "junit-jupiter-api"), ("org.junit.jupiter", "junit-jupiter")],
        ["testng"] = [("org.testng", "testng")],
    };

    private static readonly string[] HelperGroups = ["org.mockito", "org.assertj", "org.hamcrest", "org.easymock", "org.powermock", "io.rest-assured", "com.google.truth"];

    /// <summary>Phiên bản test framework và thư viện mock/assert lấy từ danh sách dependency.</summary>
    public static TestFrameworkInfo Framework(string framework, IReadOnlyList<Dependency> dependencies)
    {
        var version = FrameworkArtifacts.TryGetValue(framework, out var artifacts)
            ? artifacts.Select(a => dependencies.FirstOrDefault(d => d.Group == a.Group && d.Artifact == a.Artifact)?.Version).FirstOrDefault(v => !string.IsNullOrEmpty(v))
            : null;
        var libraries = dependencies
            .Where(d => HelperGroups.Any(g => d.Group == g || d.Group.StartsWith(g + ".", StringComparison.Ordinal)))
            .Select(d => $"{d.Group}:{d.Artifact}:{d.Version}")
            .Distinct()
            .ToList();
        return new TestFrameworkInfo { Name = framework, Version = version, Libraries = libraries };
    }

    /// <summary>
    /// <c>source</c>/<c>target</c>/<c>release</c> khai trong file build. <paramref name="buildFileTexts"/>: file của module trước,
    /// rồi file của thư mục cha (POM cha thường khai thay module); mỗi trường lấy giá trị đầu tiên tìm thấy.
    /// </summary>
    public static JavaVersionInfo JavaLevels(int jdkMajor, BuildToolKind tool, IEnumerable<string> buildFileTexts)
    {
        var levels = buildFileTexts.Select(t => JavaLevels(jdkMajor, tool, t)).ToList();
        return new JavaVersionInfo
        {
            JdkMajor = jdkMajor,
            Source = levels.Select(l => l.Source).FirstOrDefault(v => v is not null),
            Target = levels.Select(l => l.Target).FirstOrDefault(v => v is not null),
            Release = levels.Select(l => l.Release).FirstOrDefault(v => v is not null),
        };
    }

    public static JavaVersionInfo JavaLevels(int jdkMajor, BuildToolKind tool, string buildFileText)
    {
        // Chỉ nhận giá trị dạng phiên bản (8, 1.8, 11); bỏ qua ví dụ <source>src/it/java</source> của build-helper.
        static string? Clean(string? v) =>
            string.IsNullOrWhiteSpace(v) || !Regex.IsMatch(v.Trim(), @"^(1[._])?\d+$") ? null : Regex.Replace(v.Trim(), @"^1[._](\d+)$", "$1");
        if (tool == BuildToolKind.Maven)
        {
            string? Find(params string[] keys) => keys
                .SelectMany(k => Regex.Matches(buildFileText, $@"<{Regex.Escape(k)}>\s*([^<]+?)\s*</{Regex.Escape(k)}>"))
                .Select(m => Clean(m.Groups[1].Value)).FirstOrDefault(v => v is not null);
            return new JavaVersionInfo
            {
                JdkMajor = jdkMajor,
                Source = Find("maven.compiler.source", "source", "java.version"),
                Target = Find("maven.compiler.target", "target", "java.version"),
                Release = Find("maven.compiler.release", "release"),
            };
        }

        string? Gradle(string key) => Clean(Regex.Match(buildFileText, $@"\b{key}\b\s*(?:=\s*)?(?:JavaVersion\.VERSION_)?[""']?(1[._]\d+|\d+)").Groups[1].Value);
        var release = Regex.Match(buildFileText, @"release(?:\.set\(|\s*=\s*)\s*(\d+)");
        return new JavaVersionInfo
        {
            JdkMajor = jdkMajor,
            Source = Gradle("sourceCompatibility"),
            Target = Gradle("targetCompatibility"),
            Release = release.Success ? release.Groups[1].Value : null,
        };
    }

    /// <summary>
    /// Test path của class test sinh ra: thay <c>src/main/java</c> trong <c>class_path</c> bằng <c>src/test/java</c>
    /// (giống preflight), tên file <c>&lt;CUT&gt;&lt;suffix&gt;.java</c>.
    /// </summary>
    public static string TestPathFor(string classPath, string moduleDir, string testClassSuffix)
    {
        var normalized = BuildPlanDetector.Normalize(classPath);
        var parts = normalized.Split('/');
        var fileName = Path.GetFileNameWithoutExtension(parts[^1]) + testClassSuffix + ".java";
        for (var i = 0; i + 2 < parts.Length; i++)
        {
            if (parts[i] == "src" && parts[i + 1] == "main" && parts[i + 2] == "java")
            {
                return string.Join('/', parts[..i].Concat(["src", "test", "java"]).Concat(parts[(i + 3)..^1]).Append(fileName));
            }
        }

        return BuildPlanDetector.Join(moduleDir, "src/test/java/" + fileName);
    }
}
