using Preflight.Models;
using Preflight.Service.External;
using Preflight.Service.Core.Runner;

namespace Preflight.Service.Core.Build;

/// <summary>Port của runner._compile; fallback chỉ khi reactor/project không tìm thấy.</summary>
public sealed class CompileRunner(IProcessRunner runner)
{
    private static readonly string[] SkipFlags = ["-Dcheckstyle.skip=true", "-Dspring-javaformat.skip=true", "-Dspotless.check.skip=true", "-Dspotless.apply.skip=true", "-Dformatter.skip=true", "-Djacoco.skip=true"];
    public async Task<IReadOnlyList<BuildAttempt>> CompileAsync(BuildPlan plan, string stage, PreflightConfig config, string logs, Dictionary<string, string> env, CancellationToken ct)
    {
        var (primary, fallback) = BuildDetector.Commands(plan, stage);
        BuildCommand Configure(BuildCommand value)
        {
            var args = new List<string> { value.Command[0] };
            if (plan.Tool == "maven")
            {
                if (stage == "probe_compile" && config.Run.ProbeProfile == ProbeProfile.v2) args.AddRange(SkipFlags);
                if (config.Tools.MavenRepoLocal.Length > 0) args.Add("-Dmaven.repo.local=" + CachePath(config.Tools.MavenRepoLocal));
            }
            args.AddRange(value.Command.Skip(1)); return value with { Command = args };
        }
        primary = Configure(primary); fallback = Configure(fallback);
        var first = await runner.RunAsync(primary.Command, primary.WorkingDirectory, config.Run.BuildTimeoutSeconds, Path.Combine(logs, stage + ".log"), env, ct);
        var attempts = new List<BuildAttempt> { first with { Stage = stage } };
        if (first.ExitCode is not (0 or null) && !primary.Command.SequenceEqual(fallback.Command))
        {
            string text = File.ReadAllText(first.LogPath).ToLowerInvariant();
            if (text.Contains("could not find the selected project", StringComparison.Ordinal) || text.Contains("project", StringComparison.Ordinal) && text.Contains("not found", StringComparison.Ordinal))
            {
                var last = await runner.RunAsync(fallback.Command, fallback.WorkingDirectory, config.Run.BuildTimeoutSeconds, Path.Combine(logs, stage + "_fallback.log"), env, ct);
                attempts.Add(last with { Stage = stage + "_fallback" });
            }
        }
        return attempts;
    }
    public static Dictionary<string, string> ToolEnvironment(PreflightConfig config, Dictionary<string, string>? original = null)
    {
        var env = original ?? Environment.GetEnvironmentVariables().Cast<System.Collections.DictionaryEntry>().ToDictionary(e => (string)e.Key, e => (string)e.Value!, StringComparer.OrdinalIgnoreCase);
        var bins = new[] { config.Tools.MavenHome, config.Tools.GradleHome }.Where(s => s.Length > 0).Select(s => Path.Combine(s, "bin"));
        env["PATH"] = string.Join(Path.PathSeparator, bins.Concat([env.GetValueOrDefault("PATH", "")]));
        if (config.Tools.GradleUserHome.Length > 0) env["GRADLE_USER_HOME"] = CachePath(config.Tools.GradleUserHome);
        string temp = Path.Combine(RuntimePaths.Root, ".work/tmp"); Directory.CreateDirectory(temp); env["TMP"] = temp; env["TEMP"] = temp;
        return env;
    }
    private static string CachePath(string path)
    {
        string full = Path.GetFullPath(Path.IsPathRooted(path) ? path : Path.Combine(RuntimePaths.Root, path));
        if (!Common.ResolvePath.IsWithin(Common.ResolvePath.ResolveLinks(full), RuntimePaths.Root)) throw new InvalidDataException("Build caches must stay inside Prelight_Net");
        return full;
    }
}
