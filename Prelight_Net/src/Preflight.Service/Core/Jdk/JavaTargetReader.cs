using Preflight.Models;
using Preflight.Service.Core.PyCompat;

namespace Preflight.Service.Core.Jdk;

/// <summary>Port của runner._java_target và rerun_fixed_probe._uses_release_flag/_java_target.</summary>
public static class JavaTargetReader
{
    public static string? Read(string moduleRoot, string tool, ProbeProfile profile = ProbeProfile.v1)
    {
        string? target = null;
        if (tool == "maven")
        {
            string path = Path.Combine(moduleRoot, "pom.xml");
            if (!File.Exists(path)) return null;
            string text = ReadText(path);
            foreach (string key in new[] { "maven.compiler.release", "maven.compiler.target", "java.version" })
            {
                string escaped = System.Text.RegularExpressions.Regex.Escape(key);
                var match = PyRegex.Create($"<{escaped}>([^<]+)</{escaped}>").Match(text);
                if (match.Success) { target = PyStr.RemovePrefix(PyStr.Strip(match.Groups[1].Value), "1."); break; }
            }
        }
        else
        {
            string path = Path.Combine(moduleRoot, "build.gradle");
            if (!File.Exists(path)) path = Path.Combine(moduleRoot, "build.gradle.kts");
            if (File.Exists(path))
            {
                string text = ReadText(path);
                var match = PyRegex.Create("\\b(?:sourceCompatibility|targetCompatibility)\\b\\s*(?:=\\s*)?(?:JavaVersion\\.VERSION_)?[\"']?(1[._]\\d+|\\d+)").Match(text);
                if (!match.Success) match = PyRegex.Create(@"\bJavaVersion\.VERSION_(1_\d+|\d+)\b").Match(text);
                if (match.Success) target = PyRegex.Create(@"^1[._]").Replace(match.Groups[1].Value, "");
            }
        }
        return profile == ProbeProfile.v2 && target == "8" && UsesReleaseFlag(moduleRoot) ? "11" : target;
    }
    public static bool UsesReleaseFlag(string moduleRoot)
    {
        var directory = new DirectoryInfo(Path.GetFullPath(moduleRoot));
        for (int i = 0; directory is not null && i < 4; i++, directory = directory.Parent)
            foreach (string name in new[] { "pom.xml", "build.gradle", "build.gradle.kts" })
            {
                string path = Path.Combine(directory.FullName, name);
                if (File.Exists(path) && PyRegex.Create(@"options\.release|\brelease\.set\(|--release|<release>|maven\.compiler\.release").IsMatch(ReadText(path))) return true;
            }
        return false;
    }
    private static string ReadText(string path) => System.Text.Encoding.UTF8.GetString(File.ReadAllBytes(path)).Replace("\r\n", "\n", StringComparison.Ordinal).Replace('\r', '\n');
}
