using Preflight.Models;

namespace Preflight.Service.Core.Build;

/// <summary>Port của runner._failure_status và các marker nguyên bản.</summary>
public static class FailureClassifier
{
    public static readonly IReadOnlyList<string> DependencyMarkers = Array.AsReadOnly(new[] { "could not resolve", "could not transfer", "could not find artifact", "unable to resolve dependency", "read timed out", "connection timed out", "unknown host" });
    public static readonly IReadOnlyList<string> JdkMarkers = Array.AsReadOnly(new[] { "release version", "source option", "target option", "invalid target release", "unsupported class file major version", "java_home" });
    public static PreflightStatus Classify(BuildAttempt attempt, string stage)
    {
        string output;
        try { output = File.ReadAllText(attempt.LogPath).ToLowerInvariant(); }
        catch (IOException) { output = ""; }
        catch (UnauthorizedAccessException) { output = ""; }
        return Classify(output, attempt.TimedOut, stage);
    }
    public static PreflightStatus Classify(string output, bool timedOut, string stage)
    {
        if (timedOut) return PreflightStatus.BUILD_TIMEOUT;
        output = output.ToLowerInvariant();
        if (new[] { "executable_not_found", "is not recognized", "no such file or directory" }.Any(x => output.Contains(x, StringComparison.Ordinal))) return PreflightStatus.BUILD_TOOL_UNSUPPORTED;
        if (DependencyMarkers.Any(x => output.Contains(x, StringComparison.Ordinal))) return PreflightStatus.DEPENDENCY_UNAVAILABLE;
        if (JdkMarkers.Any(x => output.Contains(x, StringComparison.Ordinal))) return PreflightStatus.JDK_UNSUPPORTED;
        return stage == "main_compile" ? PreflightStatus.MAIN_BUILD_FAILED : PreflightStatus.TEST_COMPILE_FAILED;
    }
}
