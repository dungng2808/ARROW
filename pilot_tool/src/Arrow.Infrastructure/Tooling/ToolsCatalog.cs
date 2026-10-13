using Arrow.Core.Util;

namespace Arrow.Infrastructure.Tooling;

/// <summary>Một file công cụ trong <c>ARROW/tools/</c>: nguồn tải và SHA-256 (SETUP.md, specs/06).</summary>
public sealed record ToolFile(string Tool, string RelativePath, string Url, string Sha256);

/// <summary>
/// Công cụ của bước S3, S4 (specs/06): EvoSuite 1.2.0, JaCoCo 0.8.12, PIT 1.17.4 (bản dòng lệnh và thư viện phụ thuộc),
/// tsDetect 2.2. Tải vào <c>ARROW/tools/</c> (không commit), kiểm tra SHA-256.
/// </summary>
public static class ToolsCatalog
{
    private const string Central = "https://repo1.maven.org/maven2";

    public static readonly IReadOnlyList<ToolFile> Files =
    [
        new("EvoSuite 1.2.0", "evosuite/evosuite-1.2.0.jar", "https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-1.2.0.jar",
            "0bc0062fdec70c35089ad736c9b88ccf87308c2f0aaaafe43176f3c4a89ddeba"),
        new("EvoSuite 1.2.0", "evosuite/evosuite-standalone-runtime-1.2.0.jar", "https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-standalone-runtime-1.2.0.jar",
            "cc856a6d02b391367d2a4516346c699c67e4b4a370203471df1f3d78b46b74dc"),
        new("JaCoCo 0.8.12", "jacoco/jacococli-0.8.12.jar", $"{Central}/org/jacoco/org.jacoco.cli/0.8.12/org.jacoco.cli-0.8.12-nodeps.jar",
            "594c01125b84864a7fc5a9efab19b71b7de36b27346863f6d7cd0cd45400ceb0"),
        new("JaCoCo 0.8.12", "jacoco/jacocoagent-0.8.12.jar", $"{Central}/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar",
            "115e8e6e6593ca3a9892dfef695df4d487c706e59e71e64dc0ab95716ee02622"),
        new("PIT 1.17.4", "pit/pitest-1.17.4.jar", $"{Central}/org/pitest/pitest/1.17.4/pitest-1.17.4.jar",
            "97082da0ae5283f564b3f51652c39f4f1d235eb3b019384d11eb5d4fc0e4219e"),
        new("PIT 1.17.4", "pit/pitest-entry-1.17.4.jar", $"{Central}/org/pitest/pitest-entry/1.17.4/pitest-entry-1.17.4.jar",
            "809c1cf0b83c14e5c5cdb8d70c2c231db24499243263e18474436e30c23657eb"),
        new("PIT 1.17.4", "pit/pitest-command-line-1.17.4.jar", $"{Central}/org/pitest/pitest-command-line/1.17.4/pitest-command-line-1.17.4.jar",
            "a1785b9ec46daf34685b63b9702c1b94f052fa90d74cc1a84f20944894e34ccc"),
        new("PIT 1.17.4", "pit/pitest-junit5-plugin-1.2.1.jar", $"{Central}/org/pitest/pitest-junit5-plugin/1.2.1/pitest-junit5-plugin-1.2.1.jar",
            "9d944887a583ebd4adc33d2d26b8b56639fd53fee93b4d068a90596705c072b5"),
        new("PIT 1.17.4", "pit/lib/asm-9.7.1.jar", $"{Central}/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar",
            "8cadd43ac5eb6d09de05faecca38b917a040bb9139c7edeb4cc81c740b713281"),
        new("PIT 1.17.4", "pit/lib/asm-analysis-9.7.1.jar", $"{Central}/org/ow2/asm/asm-analysis/9.7.1/asm-analysis-9.7.1.jar",
            "85b29371884ba31bb76edf22323c2c24e172c3267a67152eba3d1ccc2e041ef2"),
        new("PIT 1.17.4", "pit/lib/asm-commons-9.7.1.jar", $"{Central}/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar",
            "9a579b54d292ad9be171d4313fd4739c635592c2b5ac3a459bbd1049cddec6a0"),
        new("PIT 1.17.4", "pit/lib/asm-tree-9.7.1.jar", $"{Central}/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar",
            "9929881f59eb6b840e86d54570c77b59ce721d104e6dfd7a40978991c2d3b41f"),
        new("PIT 1.17.4", "pit/lib/asm-util-9.7.1.jar", $"{Central}/org/ow2/asm/asm-util/9.7.1/asm-util-9.7.1.jar",
            "f885be71b5c90556f5f1ad1c4f9276b29b96057c497d46666fe4ddbec3cb43c6"),
        new("PIT 1.17.4", "pit/lib/commons-lang3-3.14.0.jar", $"{Central}/org/apache/commons/commons-lang3/3.14.0/commons-lang3-3.14.0.jar",
            "7b96bf3ee68949abb5bc465559ac270e0551596fa34523fddf890ec418dde13c"),
        new("PIT 1.17.4", "pit/lib/commons-text-1.12.0.jar", $"{Central}/org/apache/commons/commons-text/1.12.0/commons-text-1.12.0.jar",
            "de023257ff166044a56bd1aa9124e843cd05dac5806cc705a9311f3556d5a15f"),
        new("tsDetect 2.2", "tsdetect/TestSmellDetector.jar", "https://github.com/TestSmells/TestSmellDetector/releases/download/v2.2/TestSmellDetector.jar",
            "6a12de6d1613c7ee9e845a2b69d876834f8ee98379e85d38c0e6027f7110f7bc"),
    ];

    public static string ToolsDir(string arrowRoot) => Path.Combine(arrowRoot, "tools");

    public static string PathOf(string arrowRoot, string relativePath) => Path.Combine(ToolsDir(arrowRoot), relativePath);

    /// <summary>File thiếu hoặc sai SHA-256.</summary>
    public static List<(ToolFile File, string Problem)> Verify(string arrowRoot)
    {
        var problems = new List<(ToolFile, string)>();
        foreach (var file in Files)
        {
            var path = PathOf(arrowRoot, file.RelativePath);
            if (!File.Exists(path))
            {
                problems.Add((file, "thiếu"));
            }
            else if (Hashing.Sha256File(path) != file.Sha256)
            {
                problems.Add((file, "sai SHA-256"));
            }
        }

        return problems;
    }

    /// <summary>Tải các file thiếu hoặc sai, kiểm tra SHA-256 trước khi giữ.</summary>
    public static async Task<List<string>> InstallAsync(string arrowRoot, Action<string> log, CancellationToken cancellationToken)
    {
        using var http = new HttpClient { Timeout = TimeSpan.FromMinutes(10) };
        var errors = new List<string>();
        foreach (var (file, problem) in Verify(arrowRoot))
        {
            var path = PathOf(arrowRoot, file.RelativePath);
            log($"tải {file.RelativePath} ({problem}) từ {file.Url}");
            try
            {
                var bytes = await http.GetByteArrayAsync(file.Url, cancellationToken).ConfigureAwait(false);
                var sha = Hashing.Sha256Bytes(bytes);
                if (sha != file.Sha256)
                {
                    errors.Add($"{file.RelativePath}: SHA-256 {sha} khác {file.Sha256}");
                    continue;
                }

                Directory.CreateDirectory(Path.GetDirectoryName(path)!);
                await File.WriteAllBytesAsync(path, bytes, cancellationToken).ConfigureAwait(false);
            }
            catch (HttpRequestException ex)
            {
                errors.Add($"{file.RelativePath}: {ex.Message}");
            }
        }

        return errors;
    }

    public static string EvoSuiteJar(string arrowRoot) => PathOf(arrowRoot, "evosuite/evosuite-1.2.0.jar");
    public static string EvoSuiteRuntimeJar(string arrowRoot) => PathOf(arrowRoot, "evosuite/evosuite-standalone-runtime-1.2.0.jar");
    public static string TsDetectJar(string arrowRoot) => PathOf(arrowRoot, "tsdetect/TestSmellDetector.jar");

    /// <summary>Classpath của PIT bản dòng lệnh; plugin JUnit 5 chỉ thêm khi classpath test có JUnit Platform.</summary>
    public static List<string> PitClasspath(string arrowRoot, bool junitPlatform) =>
        Files.Where(f => f.RelativePath.StartsWith("pit/", StringComparison.Ordinal) && (junitPlatform || !f.RelativePath.Contains("junit5", StringComparison.Ordinal)))
            .Select(f => PathOf(arrowRoot, f.RelativePath)).ToList();

    /// <summary>
    /// <c>junit-platform-launcher</c> cùng phiên bản với <c>junit-platform-engine</c> trên classpath test (PIT + plugin JUnit 5 cần nó;
    /// Surefire tự mang nên classpath Maven không có). Tải từ Maven Central vào <c>tools/pit/launcher/</c> một lần.
    /// </summary>
    public static async Task<string?> EnsureJunitLauncherAsync(string arrowRoot, IEnumerable<string> classpath, CancellationToken cancellationToken)
    {
        var engine = classpath.Select(Path.GetFileName)
            .Select(n => System.Text.RegularExpressions.Regex.Match(n ?? "", @"^junit-platform-engine-(\d[\w.\-]*)\.jar$"))
            .FirstOrDefault(m => m.Success);
        if (engine is null)
        {
            return null;
        }

        var version = engine.Groups[1].Value;
        var path = PathOf(arrowRoot, $"pit/launcher/junit-platform-launcher-{version}.jar");
        if (File.Exists(path))
        {
            return path;
        }

        using var http = new HttpClient { Timeout = TimeSpan.FromMinutes(2) };
        try
        {
            var bytes = await http.GetByteArrayAsync($"{Central}/org/junit/platform/junit-platform-launcher/{version}/junit-platform-launcher-{version}.jar", cancellationToken).ConfigureAwait(false);
            Directory.CreateDirectory(Path.GetDirectoryName(path)!);
            await File.WriteAllBytesAsync(path, bytes, cancellationToken).ConfigureAwait(false);
            ToolLog.Info($"tools: tải junit-platform-launcher {version}");
            return path;
        }
        catch (HttpRequestException ex)
        {
            ToolLog.Warn($"tools: không tải được junit-platform-launcher {version}: {ex.Message}");
            return null;
        }
    }
}
