using System.Text.RegularExpressions;
using System.Xml.Linq;
using Arrow.Models.Block1;

namespace Arrow.Service.Block1;

/// <summary>Một project trong effective POM: thư mục build và source (đường dẫn tuyệt đối của bản build), mức Java khai báo, dependency trực tiếp.</summary>
public sealed record MavenProjectInfo
{
    public string GroupId { get; init; } = "";
    public string ArtifactId { get; init; } = "";
    public string? SourceDirectory { get; init; }
    public string? TestSourceDirectory { get; init; }
    public string? BuildDirectory { get; init; }
    public string? Declared { get; init; }
    public string? DeclaredSource { get; init; }
    public IReadOnlyList<DependencyRef> Dependencies { get; init; } = [];
}

/// <summary>Đọc file của <c>help:effective-pom</c> (một <c>&lt;project&gt;</c> hoặc nhiều trong <c>&lt;projects&gt;</c>), spec 01 Mục 4.3, 4.4, 4.7.</summary>
public static class EffectivePomReader
{
    public static IReadOnlyList<MavenProjectInfo> Parse(string xml)
    {
        var root = XDocument.Parse(xml).Root ?? throw new FormatException("effective POM rỗng");
        var projects = root.Name.LocalName == "projects" ? Children(root, "project") : [root];
        return projects.Select(ReadProject).ToList();
    }

    /// <summary>Project của module đích: project có <c>target</c> hoặc <c>src/main/java</c> ngay trong <paramref name="moduleRootAbsolute"/>; không có thì project cuối (Maven xếp module đích sau các module nó phụ thuộc).</summary>
    public static MavenProjectInfo? SelectTarget(IReadOnlyList<MavenProjectInfo> projects, string moduleRootAbsolute)
    {
        var root = Normalize(moduleRootAbsolute);
        bool Owns(string? dir) => dir is not null && (Normalize(dir) == root + "/target" || Normalize(dir) == root + "/src/main/java");
        return projects.FirstOrDefault(p => Owns(p.BuildDirectory) || Owns(p.SourceDirectory)) ?? projects.LastOrDefault();
    }

    private static MavenProjectInfo ReadProject(XElement project)
    {
        var properties = Child(project, "properties");
        string? Property(string name) => Usable(properties is null ? null : Child(properties, name)?.Value);

        var build = Child(project, "build");
        var compiler = build is null
            ? null
            : Children(Child(build, "plugins"), "plugin").FirstOrDefault(p => Child(p, "artifactId")?.Value.Trim() == "maven-compiler-plugin");
        var compilerConfig = compiler is null ? null : Child(compiler, "configuration");

        (string? Value, string Source)[] candidates =
        [
            (Property("maven.compiler.release"), "maven.compiler.release"),
            (Property("maven.compiler.source"), "maven.compiler.source"),
            (Usable(compilerConfig is null ? null : Child(compilerConfig, "release")?.Value), "maven-compiler-plugin.release"),
            (Usable(compilerConfig is null ? null : Child(compilerConfig, "source")?.Value), "maven-compiler-plugin.source"),
            (Property("java.version"), "java.version"),
        ];
        var declared = candidates.FirstOrDefault(c => c.Value is not null && ManifestJavaVersionParser.ParseLevel(c.Value) is not null);

        var dependencies = Children(Child(project, "dependencies"), "dependency")
            .Select(d => new DependencyRef
            {
                Group = Child(d, "groupId")?.Value.Trim() ?? "",
                Artifact = Child(d, "artifactId")?.Value.Trim() ?? "",
                Version = Child(d, "version")?.Value.Trim() ?? "",
                Scope = Child(d, "scope")?.Value.Trim() is { Length: > 0 } scope ? scope : "compile",
            })
            .ToList();

        return new MavenProjectInfo
        {
            GroupId = Child(project, "groupId")?.Value.Trim() ?? "",
            ArtifactId = Child(project, "artifactId")?.Value.Trim() ?? "",
            SourceDirectory = build is null ? null : Usable(Child(build, "sourceDirectory")?.Value),
            TestSourceDirectory = build is null ? null : Usable(Child(build, "testSourceDirectory")?.Value),
            BuildDirectory = build is null ? null : Usable(Child(build, "directory")?.Value),
            Declared = declared.Value,
            DeclaredSource = declared.Value is null ? null : declared.Source,
            Dependencies = dependencies,
        };
    }

    private static string? Usable(string? value) => value is null || value.Contains("${", StringComparison.Ordinal) || value.Trim().Length == 0 ? null : value.Trim();

    private static XElement? Child(XElement? parent, string name) => parent?.Elements().FirstOrDefault(e => e.Name.LocalName == name);

    private static IEnumerable<XElement> Children(XElement? parent, string name) => parent is null ? [] : parent.Elements().Where(e => e.Name.LocalName == name);

    private static string Normalize(string path) => path.Replace('\\', '/').TrimEnd('/');
}

/// <summary>Danh sách module khai báo trong <c>&lt;modules&gt;</c> của <c>pom.xml</c>, đệ quy (spec 01 Mục 4.6), đọc trực tiếp file trong workspace.</summary>
public static class MavenModuleScanner
{
    /// <summary>Đường dẫn module tính từ <paramref name="buildRootAbsolute"/>; dự án một module → <c>["."]</c>.</summary>
    public static IReadOnlyList<string> Modules(string buildRootAbsolute)
    {
        var result = new List<string>();
        Visit(buildRootAbsolute, ".", result, depth: 0);
        return result.Count == 0 ? ["."] : result;
    }

    private static void Visit(string rootAbsolute, string relative, List<string> result, int depth)
    {
        var dir = Path.Combine(rootAbsolute, relative);
        var pom = File.Exists(dir) ? dir : Path.Combine(dir, "pom.xml");
        if (depth > 8 || !File.Exists(pom))
        {
            return;
        }

        IEnumerable<string> modules;
        try
        {
            var project = XDocument.Load(pom).Root;
            modules = project?.Elements().Where(e => e.Name.LocalName == "modules").SelectMany(m => m.Elements().Where(e => e.Name.LocalName == "module")).Select(m => m.Value.Trim()) ?? [];
        }
        catch (System.Xml.XmlException)
        {
            return;
        }

        var baseDir = File.Exists(dir) ? Path.GetDirectoryName(relative) ?? "." : relative;
        foreach (var module in modules.Where(m => m.Length > 0))
        {
            var child = BuildRootRule.Join(baseDir, module.EndsWith(".xml", StringComparison.OrdinalIgnoreCase) ? Path.GetDirectoryName(module) ?? "." : module);
            if (!result.Contains(child))
            {
                result.Add(child);
                Visit(rootAbsolute, child, result, depth + 1);
            }
        }
    }
}

/// <summary>Một dependency đã phân giải (dòng của <c>dependency:list</c> hoặc artifact Gradle).</summary>
public sealed record ResolvedDependency(string Group, string Artifact, string Version, string Scope, string? Path)
{
    public string Coordinates => $"{Group}:{Artifact}:{Version}";
}

/// <summary>Đọc <c>arrow-deps.txt</c> do <c>dependency:list -DoutputAbsoluteArtifactFilename=true</c> ghi (spec 01 Mục 4.4).</summary>
public static class MavenDependencyListParser
{
    private static readonly HashSet<string> Scopes = ["compile", "provided", "runtime", "test", "system", "import"];

    /// <summary>
    /// Dòng có dạng <c>group:artifact:type[:classifier]:version:scope[:đường dẫn]</c>, có thể kèm <c> -- module x</c> hoặc <c> (optional)</c>.
    /// Đường dẫn Windows (<c>C:\…</c>) có dấu <c>:</c> nên được ghép lại sau scope.
    /// </summary>
    public static IReadOnlyList<ResolvedDependency> Parse(string text)
    {
        var result = new List<ResolvedDependency>();
        foreach (var raw in text.Replace("\r\n", "\n").Split('\n'))
        {
            var line = raw.Trim();
            var module = line.IndexOf(" -- ", StringComparison.Ordinal);
            if (module >= 0)
            {
                line = line[..module];
            }

            line = line.Replace(" (optional)", "", StringComparison.Ordinal).Trim();
            var tokens = line.Split(':');
            if (tokens.Length < 5 || line.Contains(' '))
            {
                continue;
            }

            var scopeIndex = Enumerable.Range(4, tokens.Length - 4).FirstOrDefault(i => Scopes.Contains(tokens[i]), -1);
            if (scopeIndex < 0)
            {
                continue;
            }

            var path = scopeIndex + 1 < tokens.Length ? string.Join(':', tokens[(scopeIndex + 1)..]) : null;
            result.Add(new ResolvedDependency(tokens[0], tokens[1], tokens[scopeIndex - 1], tokens[scopeIndex], string.IsNullOrEmpty(path) ? null : path));
        }

        return result;
    }
}

/// <summary>Thông tin một project Gradle in ra bởi init script của framework.</summary>
public sealed record GradleInfo
{
    public string GradleVersion { get; init; } = "";
    public string ProjectDir { get; init; } = "";
    public IReadOnlyList<(string Path, string Dir)> Projects { get; init; } = [];
    public string? Declared { get; init; }
    public string? DeclaredSource { get; init; }
    public IReadOnlyList<string> MainSourceDirs { get; init; } = [];
    public IReadOnlyList<string> TestSourceDirs { get; init; } = [];
    public IReadOnlyList<DependencyRef> Dependencies { get; init; } = [];
    public IReadOnlyList<ResolvedDependency> Resolved { get; init; } = [];
    public IReadOnlyList<string> Classpath { get; init; } = [];
}

/// <summary>Init script Gradle của framework và cách đọc output của nó (spec 01 Mục 4.3–4.7, Mục 6).</summary>
public static class GradleProbe
{
    public const string FileName = "arrow-init.gradle";

    /// <summary>
    /// Tắt task checkstyle/spotless, cho task test không làm build thất bại khi test fail (baseline cần chạy hết),
    /// thêm task <c>arrowInfo</c> (cấu hình project) và <c>arrowClasspath</c> (classpath test sau khi build).
    /// Viết bằng API cũ để chạy được từ Gradle 4 tới 9.
    /// </summary>
    public const string InitScript = """
        def arrowOut = { String key, Object value -> println('ARROW|' + key + '|' + (value == null ? '' : value.toString().replace('\n', ' '))) }
        def arrowTestClasspath = { p -> p.configurations.findByName('testRuntimeClasspath') ?: p.configurations.findByName('testRuntime') }

        allprojects { p ->
            p.tasks.matching { t -> t.name.startsWith('checkstyle') || t.name.startsWith('spotless') }.all { t -> t.enabled = false }
            p.tasks.withType(Test).all { t -> t.ignoreFailures = true }

            p.tasks.create('arrowInfo') {
                doLast {
                    arrowOut('gradle_version', p.gradle.gradleVersion)
                    arrowOut('project_dir', p.projectDir.absolutePath)
                    p.rootProject.allprojects.each { q -> arrowOut('project', q.path + '|' + q.projectDir.absolutePath) }
                    def declared = null
                    def javaExt = p.extensions.findByName('java')
                    try {
                        if (javaExt != null && javaExt.hasProperty('toolchain') && javaExt.toolchain.languageVersion.isPresent()) {
                            declared = 'toolchain|' + javaExt.toolchain.languageVersion.get()
                        }
                    } catch (Throwable ignored) { }
                    if (declared == null) {
                        try {
                            if (javaExt != null && javaExt.hasProperty('sourceCompatibility')) { declared = 'sourceCompatibility|' + javaExt.sourceCompatibility }
                            else if (p.hasProperty('sourceCompatibility')) { declared = 'sourceCompatibility|' + p.sourceCompatibility }
                        } catch (Throwable ignored) { }
                    }
                    if (declared != null) { arrowOut('java_declared', declared) }
                    if (p.hasProperty('sourceSets')) {
                        def main = p.sourceSets.findByName('main')
                        def test = p.sourceSets.findByName('test')
                        if (main != null) { main.java.srcDirs.each { d -> arrowOut('main_src', d.absolutePath) } }
                        if (test != null) { test.java.srcDirs.each { d -> arrowOut('test_src', d.absolutePath) } }
                    }
                    ['api', 'implementation', 'compile', 'compileOnly', 'runtimeOnly', 'runtime',
                     'testImplementation', 'testCompile', 'testCompileOnly', 'testRuntimeOnly', 'testRuntime'].each { n ->
                        def c = p.configurations.findByName(n)
                        if (c != null) {
                            c.dependencies.each { d -> arrowOut('dep', n + '|' + (d.group ?: '') + '|' + d.name + '|' + (d.version ?: '')) }
                        }
                    }
                }
            }

            p.tasks.create('arrowClasspath') {
                dependsOn { def c = arrowTestClasspath(p); c != null ? c : [] }
                doLast {
                    def c = arrowTestClasspath(p)
                    if (c != null) {
                        c.resolvedConfiguration.resolvedArtifacts.each { a ->
                            def id = a.moduleVersion.id
                            arrowOut('resolved', id.group + '|' + id.name + '|' + id.version + '|' + a.file.absolutePath)
                        }
                        c.files.each { f -> arrowOut('cp', f.absolutePath) }
                    }
                }
            }
        }
        """;

    /// <summary>Tên task của một project: <c>(":core", "test")</c> → <c>:core:test</c>; project gốc → <c>:test</c>.</summary>
    public static string Task(string? project, string task) => project is null or ":" or "" ? ":" + task : project.TrimEnd(':') + ":" + task;

    public static GradleInfo Parse(string output)
    {
        var projects = new List<(string, string)>();
        var main = new List<string>();
        var test = new List<string>();
        var dependencies = new List<DependencyRef>();
        var resolved = new List<ResolvedDependency>();
        var classpath = new List<string>();
        string version = "", projectDir = "";
        string? declared = null, declaredSource = null;

        foreach (var raw in output.Replace("\r\n", "\n").Split('\n'))
        {
            var line = raw.Trim();
            if (!line.StartsWith("ARROW|", StringComparison.Ordinal))
            {
                continue;
            }

            var parts = line.Split('|');
            var value = parts.Length > 2 ? string.Join('|', parts[2..]) : "";
            switch (parts[1])
            {
                case "gradle_version":
                    version = value;
                    break;
                case "project_dir":
                    projectDir = value;
                    break;
                case "project" when parts.Length >= 4:
                    projects.Add((parts[2], string.Join('|', parts[3..])));
                    break;
                case "java_declared" when parts.Length >= 4:
                    declaredSource = parts[2];
                    declared = parts[3];
                    break;
                case "main_src":
                    main.Add(value);
                    break;
                case "test_src":
                    test.Add(value);
                    break;
                case "dep" when parts.Length >= 6:
                    dependencies.Add(new DependencyRef { Group = parts[3], Artifact = parts[4], Version = parts[5], Scope = ScopeOf(parts[2]) });
                    break;
                case "resolved" when parts.Length >= 6:
                    resolved.Add(new ResolvedDependency(parts[2], parts[3], parts[4], "test", string.Join('|', parts[5..])));
                    break;
                case "cp":
                    classpath.Add(value);
                    break;
            }
        }

        return new GradleInfo
        {
            GradleVersion = version,
            ProjectDir = projectDir,
            Projects = projects,
            Declared = declared,
            DeclaredSource = declaredSource,
            MainSourceDirs = main,
            TestSourceDirs = test,
            Dependencies = dependencies.Where(d => d.Artifact.Length > 0).ToList(),
            Resolved = resolved,
            Classpath = classpath,
        };
    }

    private static string ScopeOf(string configuration) => configuration switch
    {
        "compileOnly" => "provided",
        "runtimeOnly" or "runtime" => "runtime",
        _ when configuration.StartsWith("test", StringComparison.Ordinal) => "test",
        _ => "compile",
    };
}

/// <summary>Nhận dạng test framework, thư viện mock và assertion (spec 01 Mục 4.5).</summary>
public static partial class TestFrameworkDetector
{
    [GeneratedRegex(@"^\s*import\s+(static\s+)?org\.testng\.", RegexOptions.Multiline)]
    private static partial Regex TestNgImport();

    [GeneratedRegex(@"^\s*import\s+(static\s+)?org\.junit\.", RegexOptions.Multiline)]
    private static partial Regex JUnitImport();

    [GeneratedRegex(@"^\s*import\s+(static\s+)?org\.junit\.jupiter\.", RegexOptions.Multiline)]
    private static partial Regex JupiterImport();

    /// <param name="dependencies">Dependency đã phân giải trên classpath test (gồm cả gián tiếp).</param>
    /// <param name="pairedTestSource">Mã test người viết (chỉ để chọn giữa TestNG và JUnit khi có cả hai).</param>
    /// <param name="direct">Dependency khai báo trực tiếp: <c>hamcrest-core</c> chỉ tính là thư viện assertion khi được khai báo trực tiếp (JUnit 4 luôn kéo theo nó).</param>
    public static TestFrameworkInfo Detect(IReadOnlyList<ResolvedDependency> dependencies, string? pairedTestSource, string manifestFramework, IReadOnlyList<DependencyRef>? direct = null)
    {
        ResolvedDependency? Find(Func<ResolvedDependency, bool> match) => dependencies.FirstOrDefault(match);

        var jupiter = Find(d => d.Group == "org.junit.jupiter" && d.Artifact is "junit-jupiter-api" or "junit-jupiter" or "junit-jupiter-engine")
                      ?? Find(d => d.Group == "org.junit.jupiter");
        var vintage = Find(d => d.Group == "org.junit.vintage" && d.Artifact == "junit-vintage-engine");
        var junit4 = Find(d => d.Group == "junit" && d.Artifact == "junit");
        var testng = Find(d => d.Group == "org.testng" && d.Artifact == "testng");

        string name, version, evidence;
        var useTestNg = testng is not null && (jupiter is null && junit4 is null
                                               || pairedTestSource is not null && TestNgImport().IsMatch(pairedTestSource) && !JUnitImport().IsMatch(pairedTestSource));
        if (useTestNg)
        {
            (name, version, evidence) = ("testng", testng!.Version, $"dependency {testng.Group}:{testng.Artifact} ({testng.Scope})");
        }
        else if (jupiter is not null)
        {
            (name, version, evidence) = ("junit5", jupiter.Version, $"dependency {jupiter.Group}:{jupiter.Artifact} ({jupiter.Scope})");
        }
        else if (junit4 is not null)
        {
            (name, version, evidence) = ("junit4", junit4.Version, $"dependency {junit4.Group}:{junit4.Artifact} ({junit4.Scope})");
        }
        else
        {
            name = manifestFramework is "junit4" or "junit5" or "testng" ? manifestFramework
                : pairedTestSource is not null && JupiterImport().IsMatch(pairedTestSource) ? "junit5" : "junit4";
            (version, evidence) = ("", "manifest");
        }

        return new TestFrameworkInfo
        {
            Name = name,
            Version = version,
            Vintage = name == "junit5" && vintage is not null,
            Mocking = dependencies
                .Where(d => d.Artifact.StartsWith("mockito-", StringComparison.Ordinal) || d.Artifact is "easymock" or "jmockit" || d.Artifact.StartsWith("powermock-", StringComparison.Ordinal))
                .Select(d => d.Coordinates).Distinct().ToList(),
            AssertionLibs = dependencies
                .Where(d => d.Artifact is "assertj-core" or "truth" || d.Artifact.StartsWith("hamcrest", StringComparison.Ordinal))
                .Where(d => !(d.Artifact == "hamcrest-core" && junit4 is not null && direct?.Any(x => x.Artifact == "hamcrest-core") != true))
                .Select(d => d.Coordinates).Distinct().ToList(),
            Evidence = evidence,
        };
    }
}
