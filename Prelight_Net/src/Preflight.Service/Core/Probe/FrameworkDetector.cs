namespace Preflight.Service.Core.Probe;

/// <summary>Port của runner._framework: ưu tiên junit5, junit4, testng.</summary>
public static class FrameworkDetector
{
    public static string Detect(string source, string testSource)
    {
        string joined = source + "\n" + testSource;
        return joined.Contains("org.junit.jupiter", StringComparison.Ordinal) ? "junit5"
            : joined.Contains("org.junit.Test", StringComparison.Ordinal) || joined.Contains("junit.framework", StringComparison.Ordinal) ? "junit4"
            : joined.Contains("org.testng", StringComparison.Ordinal) ? "testng" : "unknown";
    }
}
