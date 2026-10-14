using System.Text.RegularExpressions;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>Dò phiên bản công cụ trên máy (S2, S4): <c>java -version</c>, <c>mvn -v</c>, <c>gradle -v</c>, <c>git --version</c>.</summary>
public static partial class ToolProbe
{
    [GeneratedRegex(@"version ""(?<v>[^""]+)""")]
    private static partial Regex JavaVersionLine();

    [GeneratedRegex(@"git version (?<v>\S+)")]
    private static partial Regex GitVersionLine();

    /// <summary>Major của chuỗi phiên bản Java: <c>1.8.0_422</c> → 8, <c>17.0.12</c> → 17.</summary>
    public static int? JavaMajor(string version)
    {
        var parts = version.Split('.', '_', '-', '+');
        if (!int.TryParse(parts[0], out var first))
        {
            return null;
        }

        return first == 1 && parts.Length > 1 && int.TryParse(parts[1], out var second) ? second : first;
    }

    public static async Task<ToolInfo> JdkAsync(IProcessRunner runner, int major, string? home, CancellationToken ct)
    {
        var name = $"JDK {major}";
        if (home is null)
        {
            return new ToolInfo(name, false, null, null, "không tìm thấy - xem Java-version/README.md");
        }

        var javac = Path.Combine(home, "bin", OperatingSystem.IsWindows() ? "javac.exe" : "javac");
        if (!File.Exists(ToolLocator.JavaExecutable(home)) || !File.Exists(javac))
        {
            return new ToolInfo(name, false, null, home, "thiếu bin/java hoặc bin/javac (cần JDK, không phải JRE)");
        }

        var result = await Run(runner, ToolLocator.JavaExecutable(home), ["-version"], ct).ConfigureAwait(false);
        var version = JavaVersionLine().Match(result?.Output ?? "") is { Success: true } m ? m.Groups["v"].Value : null;
        if (version is null)
        {
            return new ToolInfo(name, false, null, home, "java -version không chạy được");
        }

        return JavaMajor(version) == major
            ? new ToolInfo(name, true, version, home)
            : new ToolInfo(name, false, version, home, $"đây là Java {JavaMajor(version)}, không phải {major}");
    }

    public static async Task<ToolInfo> ProgramAsync(IProcessRunner runner, string name, string? path, IReadOnlyList<string> versionArgs, CancellationToken ct, string? missingHint = null)
    {
        if (path is null)
        {
            return new ToolInfo(name, false, null, null, missingHint ?? "không tìm thấy trong PATH");
        }

        var result = await Run(runner, path, versionArgs, ct).ConfigureAwait(false);
        var output = result?.Output ?? "";
        var version = name == "Git" ? GitVersionLine().Match(output) is { Success: true } g ? g.Groups["v"].Value : null : External.BuildFailureClassifier.ToolVersion(output);
        return result is { Succeeded: true } ? new ToolInfo(name, true, version, path) : new ToolInfo(name, false, version, path, "không chạy được: " + result?.Tail(1));
    }

    private static async Task<ProcessResult?> Run(IProcessRunner runner, string program, IReadOnlyList<string> args, CancellationToken ct)
    {
        try
        {
            return await runner.RunAsync(new ProcessSpec(program, args, Path.GetTempPath(),
                new Dictionary<string, string?> { ["JAVA_TOOL_OPTIONS"] = null, ["MAVEN_ARGS"] = null }, TimeSpan.FromSeconds(60)), ct).ConfigureAwait(false);
        }
        catch (Exception ex) when (ex is System.ComponentModel.Win32Exception or InvalidOperationException or IOException)
        {
            return null;
        }
    }
}
