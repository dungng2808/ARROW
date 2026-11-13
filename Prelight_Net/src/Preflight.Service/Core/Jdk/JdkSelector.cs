using Preflight.Models;
using Preflight.Service.Core.PyCompat;
using Preflight.Service.External;

namespace Preflight.Service.Core.Jdk;

public sealed record JavaEnvironment(Dictionary<string, string> Environment, string Version, string? Error);

/// <summary>Port của runner._java_env; kiểm cả java/javac và target major.</summary>
public sealed class JdkSelector(IProcessRunner runner)
{
    public async Task<JavaEnvironment> SelectAsync(string? target, PreflightConfig config, string logDirectory, CancellationToken ct)
    {
        var env = Environment.GetEnvironmentVariables().Cast<System.Collections.DictionaryEntry>().ToDictionary(e => (string)e.Key, e => (string)e.Value!, StringComparer.OrdinalIgnoreCase);
        string selected = config.Java.Homes.GetValueOrDefault(target ?? "", config.Java.DefaultHome);
        if (selected.Length > 0 && !Directory.Exists(selected)) return new(env, "", "JDK_HOME_MISSING");
        if (selected.Length > 0) { env["JAVA_HOME"] = selected; env["PATH"] = Path.Combine(selected, "bin") + Path.PathSeparator + env.GetValueOrDefault("PATH", ""); }
        string? Find(string tool)
        {
            string name = tool + (OperatingSystem.IsWindows() ? ".exe" : "");
            if (selected.Length > 0) return File.Exists(Path.Combine(selected, "bin", name)) ? Path.Combine(selected, "bin", name) : null;
            return env.GetValueOrDefault("PATH", "").Split(Path.PathSeparator).Select(p => Path.Combine(p.Trim('"'), name)).FirstOrDefault(File.Exists);
        }
        string? java = Find("java"), javac = Find("javac");
        if (java is null) return new(env, "", "JDK_JAVA_MISSING");
        if (javac is null) return new(env, "", "JDK_JAVAC_MISSING");
        Directory.CreateDirectory(logDirectory);
        var a = await runner.RunAsync([java, "-version"], logDirectory, 60, Path.Combine(logDirectory, "java_version.log"), env, ct);
        var b = await runner.RunAsync([javac, "-version"], logDirectory, 60, Path.Combine(logDirectory, "javac_version.log"), env, ct);
        if (a.ExitCode != 0 || b.ExitCode != 0) return new(env, "", "JDK_VERSION_COMMAND_FAILED");
        string javaText = File.ReadAllText(a.LogPath), compilerText = File.ReadAllText(b.LogPath);
        var jm = PyRegex.Create("(?:openjdk|java)\\s+(?:version\\s+)?[\"']?(?:1\\.)?(\\d+)", System.Text.RegularExpressions.RegexOptions.IgnoreCase).Match(javaText);
        var cm = PyRegex.Create(@"\bjavac\s+(?:1\.)?(\d+)", System.Text.RegularExpressions.RegexOptions.IgnoreCase).Match(compilerText);
        if (!jm.Success || !cm.Success) return new(env, "", "JDK_VERSION_UNPARSEABLE");
        string version = javaText.Split('\n').Select(PyStr.Strip).First(l => l.Contains(jm.Value, StringComparison.Ordinal));
        string? error = jm.Groups[1].Value != cm.Groups[1].Value || target is not null && jm.Groups[1].Value != PyStr.RemovePrefix(target, "1.") ? "JDK_VERSION_MISMATCH" : null;
        return new(env, version, error);
    }
}
