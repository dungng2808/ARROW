using Preflight.Service.Core.Common;

namespace Preflight.Service.Core.Build;

public sealed record BuildPlan(string Tool, string ModuleRoot, string InvocationRoot, string ModulePath, string Executable);
public sealed record BuildCommand(IReadOnlyList<string> Command, string WorkingDirectory);

/// <summary>Port của runner.detect_build, _find_up, _wrapper và command builders.</summary>
public static class BuildDetector
{
    public static BuildPlan? Detect(string workspace, string classFile)
    {
        workspace = Path.GetFullPath(workspace);
        string? module = FindUp(Path.GetDirectoryName(classFile)!, workspace, "pom.xml");
        if (module is not null)
        {
            string root = module;
            for (var parent = Directory.GetParent(module); parent is not null && ResolvePath.IsWithin(parent.FullName, workspace); parent = parent.Parent)
                if (File.Exists(Path.Combine(parent.FullName, "pom.xml"))) root = parent.FullName;
            return new("maven", module, root, Relative(root, module), Wrapper(root, workspace, OperatingSystem.IsWindows() ? "mvnw.cmd" : "mvnw", OperatingSystem.IsWindows() ? "mvn.cmd" : "mvn", []));
        }
        module = FindUp(Path.GetDirectoryName(classFile)!, workspace, "build.gradle", "build.gradle.kts");
        if (module is null) return null;
        string gradleRoot = FindUp(module, workspace, "settings.gradle", "settings.gradle.kts") ?? module;
        return new("gradle", module, gradleRoot, Relative(gradleRoot, module), Wrapper(gradleRoot, workspace, OperatingSystem.IsWindows() ? "gradlew.bat" : "gradlew", OperatingSystem.IsWindows() ? "gradle.bat" : "gradle", ["gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties"]));
    }
    public static string? FindUp(string start, string stop, params string[] names)
    {
        for (var directory = new DirectoryInfo(start); directory is not null && ResolvePath.IsWithin(directory.FullName, stop); directory = directory.Parent)
            if (names.Any(name => File.Exists(Path.Combine(directory.FullName, name)))) return directory.FullName;
        return null;
    }
    private static string Relative(string root, string module) => Path.GetRelativePath(root, module).Replace('\\', '/');
    private static string Wrapper(string root, string workspace, string name, string fallback, string[] required)
    {
        for (var directory = new DirectoryInfo(root); directory is not null && ResolvePath.IsWithin(directory.FullName, workspace); directory = directory.Parent)
        {
            string path = Path.Combine(directory.FullName, name);
            if (File.Exists(path) && ResolvePath.IsWithin(ResolvePath.ResolveLinks(path), workspace) && required.All(r => File.Exists(Path.Combine(directory.FullName, r)) && ResolvePath.IsWithin(ResolvePath.ResolveLinks(Path.Combine(directory.FullName, r)), workspace))) return path;
        }
        return fallback;
    }
    public static (BuildCommand Primary, BuildCommand Fallback) Commands(BuildPlan plan, string stage)
    {
        if (plan.Tool == "maven")
        {
            string phase = stage == "main_compile" ? "compile" : "test-compile";
            var fallback = new BuildCommand([plan.Executable, "-q", "-DskipTests", phase], plan.ModuleRoot);
            return plan.ModulePath == "." ? (fallback, fallback) : (new([plan.Executable, "-q", "-pl", plan.ModulePath, "-am", "-DskipTests", phase], plan.InvocationRoot), fallback);
        }
        string task = stage == "main_compile" ? "classes" : "testClasses";
        var gradleFallback = new BuildCommand([plan.Executable, "--no-daemon", task], plan.ModuleRoot);
        return plan.ModulePath == "." ? (gradleFallback, gradleFallback) : (new([plan.Executable, "--no-daemon", ":" + plan.ModulePath.Replace('/', ':') + ":" + task], plan.InvocationRoot), gradleFallback);
    }
}
