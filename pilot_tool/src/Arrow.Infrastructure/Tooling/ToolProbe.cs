using System.Text.RegularExpressions;
using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Tooling;

public sealed record JdkInfo(int Major, string Home, string JavaVersion, string JavacVersion);

public sealed record ToolInfo(string Name, string Path, string Version);

/// <summary>Tìm và kiểm tra JDK, Maven, Gradle, Git, LiteLLM trên máy (SETUP.md bước 5).</summary>
public static partial class ToolProbe
{
    public static readonly int[] RequiredJdkMajors = [8, 11, 17, 21];

    /// <summary>Các thư mục JDK có thể có, theo thứ tự ưu tiên.</summary>
    public static IEnumerable<string> CandidateJdkHomes(int major, string arrowRoot)
    {
        var runtime = System.IO.Path.Combine(arrowRoot, "Java-version", "runtime", $"jdk-{major}");
        yield return System.IO.Path.Combine(runtime, "Contents", "Home");
        yield return runtime;

        foreach (var variable in new[] { $"JAVA_{major}_HOME", $"JAVA{major}_HOME", $"JDK_{major}_HOME" })
        {
            var value = System.Environment.GetEnvironmentVariable(variable);
            if (!string.IsNullOrEmpty(value))
            {
                yield return value;
            }
        }

        var roots = new List<string>();
        if (OperatingSystem.IsMacOS())
        {
            roots.Add("/Library/Java/JavaVirtualMachines");
            roots.Add("/opt/homebrew/opt");
        }
        else if (OperatingSystem.IsWindows())
        {
            foreach (var programFiles in new[] { System.Environment.GetFolderPath(System.Environment.SpecialFolder.ProgramFiles) })
            {
                roots.Add(System.IO.Path.Combine(programFiles, "Java"));
                roots.Add(System.IO.Path.Combine(programFiles, "Eclipse Adoptium"));
                roots.Add(System.IO.Path.Combine(programFiles, "Microsoft"));
                roots.Add(System.IO.Path.Combine(programFiles, "Zulu"));
            }
        }
        else
        {
            roots.Add("/usr/lib/jvm");
        }

        foreach (var root in roots.Where(Directory.Exists))
        {
            foreach (var directory in Directory.EnumerateDirectories(root).OrderBy(d => d, StringComparer.Ordinal))
            {
                var name = System.IO.Path.GetFileName(directory);
                if (!Regex.IsMatch(name, $@"(^|[^0-9]){(major == 8 ? "(1\\.)?8" : major.ToString())}([^0-9]|$)"))
                {
                    continue;
                }

                yield return System.IO.Path.Combine(directory, "Contents", "Home");
                yield return System.IO.Path.Combine(directory, "libexec", "openjdk.jdk", "Contents", "Home");
                yield return directory;
            }
        }
    }

    /// <summary>Kiểm tra một thư mục là JDK đúng major: java và javac chạy được, đúng phiên bản.</summary>
    public static async Task<JdkInfo?> VerifyJdkAsync(string home, int major, CancellationToken cancellationToken = default)
    {
        var suffix = OperatingSystem.IsWindows() ? ".exe" : "";
        var java = System.IO.Path.Combine(home, "bin", "java" + suffix);
        var javac = System.IO.Path.Combine(home, "bin", "javac" + suffix);
        if (!File.Exists(java) || !File.Exists(javac))
        {
            return null;
        }

        var env = new Dictionary<string, string?> { ["JAVA_HOME"] = home, ["JAVA_TOOL_OPTIONS"] = null };
        var javaResult = await ProcessRunner.RunAsync(java, ["-version"], environment: env, timeout: TimeSpan.FromSeconds(30), cancellationToken: cancellationToken);
        var javacResult = await ProcessRunner.RunAsync(javac, ["-version"], environment: env, timeout: TimeSpan.FromSeconds(30), cancellationToken: cancellationToken);
        var javaMatch = JavaVersionRegex().Match(javaResult.Output);
        var javacMatch = JavacVersionRegex().Match(javacResult.Output);
        if (!javaMatch.Success || !javacMatch.Success)
        {
            return null;
        }

        int Major(Match m) => m.Groups[1].Value == "1" && m.Groups[2].Success ? int.Parse(m.Groups[2].Value) : int.Parse(m.Groups[1].Value);
        if (Major(javaMatch) != major || Major(javacMatch) != major)
        {
            return null;
        }

        var javaLine = javaResult.Output.Split('\n').FirstOrDefault(l => l.Contains("version", StringComparison.OrdinalIgnoreCase))?.Trim() ?? "";
        return new JdkInfo(major, home, javaLine, javacMatch.Value.Trim());
    }

    public static async Task<JdkInfo?> FindJdkAsync(int major, string arrowRoot, string? preferredHome = null, CancellationToken cancellationToken = default)
    {
        var candidates = new List<string>();
        if (!string.IsNullOrEmpty(preferredHome))
        {
            candidates.Add(preferredHome);
        }

        candidates.AddRange(CandidateJdkHomes(major, arrowRoot));
        foreach (var candidate in candidates.Distinct())
        {
            if (Directory.Exists(candidate) && await VerifyJdkAsync(candidate, major, cancellationToken) is { } info)
            {
                return info;
            }
        }

        return null;
    }

    /// <summary>Phiên bản Maven, đo sau khi đặt JAVA_HOME (specs/02 §4.1).</summary>
    public static async Task<ToolInfo?> ProbeMavenAsync(string? path, string? javaHome, CancellationToken cancellationToken = default)
    {
        path = string.IsNullOrEmpty(path) ? ProcessRunner.FindOnPath("mvn") : path;
        if (path is null)
        {
            return null;
        }

        var result = await ProcessRunner.RunAsync(path, ["-v"], environment: JavaEnv(javaHome), timeout: TimeSpan.FromSeconds(120), cancellationToken: cancellationToken);
        var match = Regex.Match(result.Output, @"Apache Maven (\S+)");
        return match.Success ? new ToolInfo("Maven", path, match.Groups[1].Value) : null;
    }

    public static async Task<ToolInfo?> ProbeGradleAsync(string? path, string? javaHome, CancellationToken cancellationToken = default)
    {
        path = string.IsNullOrEmpty(path) ? ProcessRunner.FindOnPath("gradle") : path;
        if (path is null)
        {
            return null;
        }

        var result = await ProcessRunner.RunAsync(path, ["--version"], environment: JavaEnv(javaHome), timeout: TimeSpan.FromSeconds(180), cancellationToken: cancellationToken);
        var match = Regex.Match(result.Output, @"Gradle (\d\S*)");
        return match.Success ? new ToolInfo("Gradle", path, match.Groups[1].Value) : null;
    }

    public static async Task<ToolInfo?> ProbeGitAsync(string? path, CancellationToken cancellationToken = default)
    {
        path = string.IsNullOrEmpty(path) ? ProcessRunner.FindOnPath("git") : path;
        if (path is null)
        {
            return null;
        }

        var result = await ProcessRunner.RunAsync(path, ["--version"], timeout: TimeSpan.FromSeconds(30), cancellationToken: cancellationToken);
        var match = Regex.Match(result.Output, @"git version (\S+)");
        return match.Success ? new ToolInfo("Git", path, match.Groups[1].Value) : null;
    }

    /// <summary>LiteLLM cài bằng install/install-litellm.* nằm trong ARROW/litellm (môi trường Python riêng).</summary>
    public static string LocalLiteLlmPath(string arrowRoot) =>
        OperatingSystem.IsWindows()
            ? System.IO.Path.Combine(arrowRoot, "litellm", "Scripts", "litellm.exe")
            : System.IO.Path.Combine(arrowRoot, "litellm", "bin", "litellm");

    /// <summary>Phiên bản trả về khi có file chương trình nhưng không chạy được (ví dụ môi trường Python còn trỏ đường dẫn cũ).</summary>
    public const string Broken = "broken";

    public static async Task<ToolInfo?> ProbeLiteLlmAsync(string arrowRoot, CancellationToken cancellationToken = default)
    {
        var local = LocalLiteLlmPath(arrowRoot);
        var path = File.Exists(local) ? local : ProcessRunner.FindOnPath("litellm");
        if (path is null)
        {
            return null;
        }

        var result = await ProcessRunner.RunAsync(path, ["--version"], timeout: TimeSpan.FromSeconds(60), cancellationToken: cancellationToken);
        if (!result.Succeeded)
        {
            return new ToolInfo("LiteLLM", path, Broken);
        }

        var match = Regex.Match(result.Output, @"(\d+\.\d+\.\d+)");
        return new ToolInfo("LiteLLM", path, match.Success ? match.Groups[1].Value : "unknown");
    }

    public static Dictionary<string, string?> JavaEnv(string? javaHome)
    {
        var env = new Dictionary<string, string?>();
        if (!string.IsNullOrEmpty(javaHome))
        {
            env["JAVA_HOME"] = javaHome;
            env["PATH"] = System.IO.Path.Combine(javaHome, "bin") + System.IO.Path.PathSeparator + System.Environment.GetEnvironmentVariable("PATH");
        }

        return env;
    }

    [GeneratedRegex(@"version ""(\d+)(?:\.(\d+))?")]
    private static partial Regex JavaVersionRegex();

    [GeneratedRegex(@"javac (\d+)(?:\.(\d+))?")]
    private static partial Regex JavacVersionRegex();
}
