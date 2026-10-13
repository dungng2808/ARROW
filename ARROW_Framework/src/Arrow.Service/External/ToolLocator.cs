using Arrow.Models.Config;

namespace Arrow.Service.External;

/// <summary>Tìm JDK và Maven trên máy (dùng trước khi có màn hình cài đặt; spec 01 Mục 1.2, spec 07 Mục 10).</summary>
public static class ToolLocator
{
    private static bool IsWindows => OperatingSystem.IsWindows();

    /// <summary>Đường dẫn <c>bin/java</c> của một JDK home.</summary>
    public static string JavaExecutable(string jdkHome) => Path.Combine(jdkHome, "bin", IsWindows ? "java.exe" : "java");

    /// <summary>
    /// JDK home theo thứ tự: <c>machine.local.json</c> (<c>jdk_homes</c>), rồi <c>ARROW/Java-version/runtime/jdk-&lt;major&gt;</c>
    /// (trên macOS có thêm <c>Contents/Home</c>). Không có thì null.
    /// </summary>
    public static string? FindJdkHome(int major, MachineConfig? machine, string arrowRoot)
    {
        var candidates = new List<string>();
        if (machine is not null && machine.JdkHomes.TryGetValue(major.ToString(), out var configured))
        {
            candidates.Add(configured);
        }

        var runtime = Path.Combine(arrowRoot, "Java-version", "runtime", $"jdk-{major}");
        candidates.Add(Path.Combine(runtime, "Contents", "Home"));
        candidates.Add(runtime);
        return candidates.FirstOrDefault(home => File.Exists(JavaExecutable(home)));
    }

    /// <summary>Maven theo thứ tự: <c>maven_path</c> của máy, rồi <c>mvn</c> (<c>mvn.cmd</c> trên Windows) trong PATH.</summary>
    public static string? FindMaven(MachineConfig? machine)
    {
        if (machine is { MavenPath.Length: > 0 } && File.Exists(machine.MavenPath))
        {
            return machine.MavenPath;
        }

        return FindOnPath(IsWindows ? "mvn.cmd" : "mvn");
    }

    public static string? FindOnPath(string program) =>
        (Environment.GetEnvironmentVariable("PATH") ?? "")
        .Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries)
        .Select(dir => Path.Combine(dir, program))
        .FirstOrDefault(File.Exists);
}
