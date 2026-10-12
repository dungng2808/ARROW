using System.Text.Json;
using Arrow.Core.Config;
using Arrow.Core.Util;
using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Java;

public sealed record TestAnalysis
{
    public bool Parsed { get; init; }
    public string Stripped { get; init; } = "";
    public int? AssertionCount { get; init; }
    public Dictionary<string, int> AssertionsPerMethod { get; init; } = [];
    public List<string> TestMethods { get; init; } = [];
    public Dictionary<string, string?> MethodsAtLines { get; init; } = [];
}

public sealed record SpliceResult
{
    public string? Error { get; init; }
    public string Code { get; init; } = "";
    public List<string> Replaced { get; init; } = [];
    public List<string> Dropped { get; init; } = [];
    public List<string> AddedImports { get; init; } = [];
    public List<string> AddedFields { get; init; } = [];
    public List<string> AddedMethods { get; init; } = [];
}

public sealed record ApiType
{
    public string Name { get; init; } = "";
    public string Kind { get; init; } = "";
    public List<string> Members { get; init; } = [];
}

public sealed record CutApi
{
    public List<ApiType> Types { get; init; } = [];
}

public sealed record CalledMethods
{
    public List<string> Called { get; init; } = [];
    public List<string> Methods { get; init; } = [];
    public List<string> Constructors { get; init; } = [];
    public List<string> Fields { get; init; } = [];
}

/// <summary>
/// Gọi <c>tools/java-analyzer</c> (JavaParser) qua tiến trình: mỗi lệnh một file JSON yêu cầu, kết quả JSON trên stdout
/// (ARCHITECTURE Mục 10). Jar chưa có thì build bằng Maven một lần.
/// </summary>
public sealed class JavaAnalyzer(string pilotToolDir, MachineConfig machine)
{
    private static readonly SemaphoreSlim BuildLock = new(1, 1);
    private static readonly TimeSpan CallTimeout = TimeSpan.FromMinutes(2);
    private bool _checked;

    public string ProjectDir => Path.Combine(pilotToolDir, "tools", "java-analyzer");
    public string JarPath => Path.Combine(ProjectDir, "target", "java-analyzer.jar");

    /// <summary>JDK chạy analyzer: 17, rồi 21, rồi 11.</summary>
    public string JavaHome =>
        new[] { "17", "21", "11" }.Select(v => machine.JdkHomes.GetValueOrDefault(v)).FirstOrDefault(h => !string.IsNullOrEmpty(h))
        ?? throw new ArrowException("JDK_MISSING", "java-analyzer cần JDK 11, 17 hoặc 21 trong jdk_homes");

    /// <summary>Jar có và mới hơn mọi file nguồn (sau <c>git pull</c> mã nguồn mới thì build lại).</summary>
    public bool IsUpToDate()
    {
        if (!File.Exists(JarPath))
        {
            return false;
        }

        var jarTime = File.GetLastWriteTimeUtc(JarPath);
        var sources = Directory.EnumerateFiles(Path.Combine(ProjectDir, "src"), "*.java", SearchOption.AllDirectories).Append(Path.Combine(ProjectDir, "pom.xml"));
        return sources.All(f => File.GetLastWriteTimeUtc(f) <= jarTime);
    }

    public async Task EnsureBuiltAsync(CancellationToken cancellationToken)
    {
        if (_checked || IsUpToDate())
        {
            _checked = true;
            return;
        }

        await BuildLock.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            if (IsUpToDate())
            {
                _checked = true;
                return;
            }

            var env = new Dictionary<string, string?> { ["JAVA_HOME"] = JavaHome, ["JAVA_TOOL_OPTIONS"] = null, ["MAVEN_ARGS"] = null };
            var result = await ProcessRunner.RunAsync(machine.MavenPath, ["-B", "-ntp", "-q", "-f", Path.Combine(ProjectDir, "pom.xml"), "clean", "package"],
                ProjectDir, env, TimeSpan.FromMinutes(10), cancellationToken).ConfigureAwait(false);
            if (!result.Succeeded || !File.Exists(JarPath))
            {
                throw new ArrowException("ANALYZER_BUILD_FAILED", $"Không build được java-analyzer: {ToolLog.Redact(result.Output.Trim().Split('\n').LastOrDefault() ?? "")}");
            }

            ToolLog.Info("java-analyzer: đã build " + JarPath);
            _checked = true;
        }
        finally
        {
            BuildLock.Release();
        }
    }

    private async Task<T> CallAsync<T>(object request, CancellationToken cancellationToken)
    {
        await EnsureBuiltAsync(cancellationToken).ConfigureAwait(false);
        var file = Path.Combine(Path.GetTempPath(), $"arrow-analyzer-{Guid.NewGuid():N}.json");
        try
        {
            await File.WriteAllTextAsync(file, Json.Serialize(request, indented: false), cancellationToken).ConfigureAwait(false);
            var java = Path.Combine(JavaHome, "bin", OperatingSystem.IsWindows() ? "java.exe" : "java");
            var env = new Dictionary<string, string?> { ["JAVA_TOOL_OPTIONS"] = null, ["JDK_JAVA_OPTIONS"] = null };
            var result = await ProcessRunner.RunAsync(java, ["-Dfile.encoding=UTF-8", "-Xss8m", "-jar", JarPath, file], Path.GetTempPath(), env, CallTimeout, cancellationToken)
                .ConfigureAwait(false);
            if (!result.Succeeded)
            {
                throw new ArrowException("ANALYZER_FAILED", $"java-analyzer lỗi (mã {result.ExitCode}): {result.StdErr.Trim()}");
            }

            using var document = JsonDocument.Parse(result.StdOut);
            if (document.RootElement.TryGetProperty("error", out var error) && error.ValueKind == JsonValueKind.String && typeof(T) != typeof(SpliceResult))
            {
                throw new ArrowException("ANALYZER_FAILED", "java-analyzer: " + error.GetString());
            }

            return Json.Deserialize<T>(result.StdOut);
        }
        finally
        {
            File.Delete(file);
        }
    }

    public Task<TestAnalysis> AnalyzeTestAsync(string source, IEnumerable<int> lines, CancellationToken cancellationToken) =>
        CallAsync<TestAnalysis>(new { command = "analyze_test", source, lines = lines.Distinct().ToList() }, cancellationToken);

    public Task<SpliceResult> SpliceAsync(string checkpoint, string response, IEnumerable<string> methods, CancellationToken cancellationToken) =>
        CallAsync<SpliceResult>(new { command = "splice_methods", checkpoint, response, methods = methods.ToList() }, cancellationToken);

    public Task<CutApi> CutApiAsync(string source, IEnumerable<string> relatedSources, CancellationToken cancellationToken) =>
        CallAsync<CutApi>(new { command = "cut_api", source, related_sources = relatedSources.ToList() }, cancellationToken);

    public Task<CalledMethods> CalledMethodsAsync(string testSource, string cutSource, IEnumerable<string> testMethods, CancellationToken cancellationToken) =>
        CallAsync<CalledMethods>(new { command = "called_methods", test_source = testSource, cut_source = cutSource, test_methods = testMethods.ToList() }, cancellationToken);

    public async Task<List<string>> MethodSourceAsync(string source, IEnumerable<string> names, CancellationToken cancellationToken) =>
        (await CallAsync<MethodSources>(new { command = "method_source", source, names = names.ToList() }, cancellationToken).ConfigureAwait(false)).Methods;

    private sealed record MethodSources
    {
        public List<string> Methods { get; init; } = [];
    }
}
