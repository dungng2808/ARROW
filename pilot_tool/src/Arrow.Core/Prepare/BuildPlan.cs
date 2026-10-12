using System.Text.Json.Serialization;
using Arrow.Core.Util;

namespace Arrow.Core.Prepare;

[JsonConverter(typeof(LowerSnakeEnumConverter<BuildToolKind>))]
public enum BuildToolKind
{
    Maven,
    Gradle,
}

/// <summary>
/// Cách gọi Maven/Gradle cho module chứa CUT. <c>root</c>: một module, chạy tại module;
/// <c>reactor</c>: chạy tại thư mục gốc với <c>-pl … -am</c> (Maven) hoặc <c>:a:b:task</c> (Gradle);
/// <c>standalone</c>: module không thuộc reactor/settings, chạy riêng tại module.
/// </summary>
[JsonConverter(typeof(LowerSnakeEnumConverter<BuildMode>))]
public enum BuildMode
{
    Root,
    Reactor,
    Standalone,
}

/// <summary>
/// Kế hoạch build của một task, đường dẫn tương đối so với gốc repository, dùng '/'.
/// <see cref="ModulePath"/> tương đối so với <see cref="InvocationRoot"/>, cùng nghĩa với <c>module_path</c> của manifest preflight.
/// </summary>
public sealed record BuildPlan
{
    public BuildToolKind Tool { get; init; }
    public string ModuleDir { get; init; } = ".";
    public string InvocationRoot { get; init; } = ".";
    public string ModulePath { get; init; } = ".";
    public BuildMode Mode { get; init; }

    /// <summary>Wrapper tương đối so với gốc repository (<c>mvnw</c>, <c>gradlew</c>…), null nếu dùng chương trình của máy.</summary>
    public string? Wrapper { get; init; }

    /// <summary>Thư mục chạy lệnh (tương đối so với gốc repository).</summary>
    [JsonIgnore]
    public string WorkingDir => Mode == BuildMode.Reactor ? InvocationRoot : ModuleDir;

    /// <summary>Gradle project path, ví dụ <c>:a:b</c>; module gốc là <c>""</c>.</summary>
    [JsonIgnore]
    public string GradleProjectPath => ModulePath == "." ? "" : ":" + string.Join(':', ModulePath.Split('/', StringSplitOptions.RemoveEmptyEntries));

    public BuildPlan AsStandalone() => this with { Mode = BuildMode.Standalone };
}

/// <summary>
/// Dò module và thư mục gọi build từ <c>class_path</c>, theo đúng cách preflight đã làm
/// (<c>preflight_tool/preflight/runner.py</c>, <c>detect_build</c>), để lệnh build giống lệnh preflight đã chạy thành công.
/// </summary>
public static class BuildPlanDetector
{
    /// <param name="fileExists">Kiểm tra file theo đường dẫn tương đối so với gốc repository (dùng '/').</param>
    public static BuildPlan? Detect(string classPath, Func<string, bool> fileExists, bool windows)
    {
        var start = Parent(Normalize(classPath));
        var mavenModule = FindUp(start, fileExists, "pom.xml");
        if (mavenModule is not null)
        {
            // POM cao nhất là ứng viên reactor; nếu Maven không nhận module thì chuyển sang standalone.
            var root = mavenModule;
            foreach (var ancestor in Ancestors(mavenModule))
            {
                if (fileExists(Join(ancestor, "pom.xml")))
                {
                    root = ancestor;
                }
            }

            var modulePath = Relative(root, mavenModule);
            return new BuildPlan
            {
                Tool = BuildToolKind.Maven,
                ModuleDir = mavenModule,
                InvocationRoot = root,
                ModulePath = modulePath,
                Mode = modulePath == "." ? BuildMode.Root : BuildMode.Reactor,
                Wrapper = FindWrapper(root, fileExists, windows ? "mvnw.cmd" : "mvnw", [".mvn/wrapper/maven-wrapper.jar", ".mvn/wrapper/maven-wrapper.properties"], requireAll: false),
            };
        }

        var gradleModule = FindUp(start, fileExists, "build.gradle", "build.gradle.kts");
        if (gradleModule is null)
        {
            return null;
        }

        var settingsRoot = FindUp(gradleModule, fileExists, "settings.gradle", "settings.gradle.kts") ?? gradleModule;
        var path = Relative(settingsRoot, gradleModule);
        return new BuildPlan
        {
            Tool = BuildToolKind.Gradle,
            ModuleDir = gradleModule,
            InvocationRoot = settingsRoot,
            ModulePath = path,
            Mode = path == "." ? BuildMode.Root : BuildMode.Reactor,
            // Script wrapper thiếu JAR thì không chạy được Gradle; dùng Gradle của máy.
            Wrapper = FindWrapper(settingsRoot, fileExists, windows ? "gradlew.bat" : "gradlew", ["gradle/wrapper/gradle-wrapper.jar"], requireAll: true),
        };
    }

    private static string? FindWrapper(string root, Func<string, bool> fileExists, string name, string[] support, bool requireAll)
    {
        foreach (var directory in new[] { root }.Concat(Ancestors(root)))
        {
            var script = Join(directory, name);
            if (!fileExists(script))
            {
                continue;
            }

            var present = support.Select(s => fileExists(Join(directory, s)));
            if (requireAll ? present.All(p => p) : present.Any(p => p))
            {
                return script;
            }
        }

        return null;
    }

    private static string? FindUp(string start, Func<string, bool> fileExists, params string[] names)
    {
        foreach (var directory in new[] { start }.Concat(Ancestors(start)))
        {
            if (names.Any(n => fileExists(Join(directory, n))))
            {
                return directory;
            }
        }

        return null;
    }

    /// <summary>Thư mục cha từ gần tới xa, kết thúc ở gốc repository ".".</summary>
    private static IEnumerable<string> Ancestors(string directory)
    {
        var current = directory;
        while (current != ".")
        {
            current = Parent(current);
            yield return current;
        }
    }

    public static string Normalize(string path)
    {
        var parts = path.Replace('\\', '/').Split('/', StringSplitOptions.RemoveEmptyEntries).Where(p => p != ".").ToArray();
        return parts.Length == 0 ? "." : string.Join('/', parts);
    }

    private static string Parent(string path)
    {
        var index = path.LastIndexOf('/');
        return index < 0 ? "." : path[..index];
    }

    public static string Join(string directory, string name) => directory == "." ? name : directory + "/" + name;

    private static string Relative(string root, string path) =>
        root == path ? "." : root == "." ? path : path[(root.Length + 1)..];
}
