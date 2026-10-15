using System.Diagnostics;
using System.Text;
using Arrow.DataAccess;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Service.Block3;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block4;

/// <summary>Phần chung của ba công cụ: chạy <c>java</c> của một JDK, ghi log, dọn bản sao.</summary>
public abstract class QualityToolBase(IProcessRunner runner, IWorkspaceManager workspace, ITaskResultRepository results)
{
    protected IWorkspaceManager Workspace => workspace;

    protected async Task<ProcessResult> JavaAsync(string jdkHome, IReadOnlyList<string> arguments, string workingDirectory, TimeSpan timeout, TaskContext task, string logPath, CancellationToken ct)
    {
        var spec = new ProcessSpec(ToolLocator.JavaExecutable(jdkHome), arguments, workingDirectory,
            new Dictionary<string, string?> { ["JAVA_HOME"] = jdkHome, ["JAVA_TOOL_OPTIONS"] = BuildRunner.JavaToolOptions, ["JDK_JAVA_OPTIONS"] = null }, timeout);
        var result = await runner.RunAsync(spec, ct).ConfigureAwait(false);
        var log = new StringBuilder()
            .Append("$ ").AppendJoin(' ', new[] { spec.FileName }.Concat(arguments).Select(a => a.Contains(' ') ? $"\"{a}\"" : a)).Append('\n')
            .Append("cwd: ").Append(workingDirectory).Append('\n')
            .Append(BuildCommands.TruncateLog(result.Output))
            .Append(FormattableString.Invariant($"\n# exit={result.ExitCode?.ToString() ?? "-"} seconds={result.Duration.TotalSeconds:0.0} timeout={(result.TimedOut ? "yes" : "no")}\n"));
        results.SaveText(task.RunId, task.Key, logPath, log.ToString());
        return result;
    }

    protected void SaveText(TaskContext task, string path, string text) => results.SaveText(task.RunId, task.Key, path, text);

    protected static string Log(MeasureOptions options, string name) => ResultPaths.Join(options.ResultDir, "logs", name + ".log");
}

/// <summary>
/// JaCoCo (spec 04 Mục 4.2, cập nhật 15/10/2026): <b>offline instrumentation</b>, cùng một cách cho bộ ARROW và bộ EvoSuite, Maven và Gradle.
/// Sau bước chuẩn bị, file <c>.class</c> của CUT và class lồng được <c>jacococli instrument</c> ngay trong thư mục class production của
/// bản sao; runtime JaCoCo (<c>jacocoagent.jar</c>) vào classpath test như classpath phụ; chạy riêng bộ test mà không biên dịch lại
/// production (Maven <c>-Dmaven.main.skip=true</c>, Gradle <c>-x :p:compileJava</c>); báo cáo dùng bản <c>.class</c> gốc.
/// Không gắn agent vào JVM nào: bộ EvoSuite giữ nguyên <c>separateClassLoader = true</c> (classloader của EvoSuite đọc bytecode đã có
/// probe nên vẫn ghi được coverage), không còn agent trùng với JaCoCo của dự án (plugin <c>jacoco</c> của Gradle bị tắt qua init script).
/// Bộ test không pass khi đo, hoặc CUT bị biên dịch lại → <c>B4_JACOCO_FAILED</c>, không ghi coverage 0.
/// </summary>
public sealed class JacocoTool(SuitePreparer preparer, IBuildRunner build, IToolsCatalog tools, ITestReportReader reports, IProcessRunner runner, IWorkspaceManager workspace, ITaskResultRepository results)
    : QualityToolBase(runner, workspace, results), IQualityTool
{
    public const string GradleInitScriptName = "arrow-jacoco.gradle";

    /// <summary>Tắt JaCoCo riêng của dự án (plugin <c>jacoco</c> gắn agent vào mọi task test; agent thứ hai làm JVM chạy test sập).</summary>
    public const string GradleInitScript = """
        allprojects { p ->
            p.afterEvaluate {
                p.tasks.withType(Test).each { t ->
                    def projectJacoco = t.extensions.findByName('jacoco')
                    if (projectJacoco != null) { projectJacoco.enabled = false }
                }
            }
        }
        """;

    public string Name => "jacoco";

    /// <summary>File <c>.class</c> của CUT và class lồng (<c>Tên.class</c>, <c>Tên$*.class</c>) trong thư mục class production.</summary>
    public static IReadOnlyList<string> CutClassFiles(string productionClasses, string fqn)
    {
        var lastDot = fqn.LastIndexOf('.');
        var directory = Path.Combine(productionClasses, lastDot < 0 ? "" : fqn[..lastDot].Replace('.', Path.DirectorySeparatorChar));
        var name = fqn[(lastDot + 1)..];
        return Directory.Exists(directory)
            ? Directory.EnumerateFiles(directory, "*.class")
                .Where(f => Path.GetFileName(f) == name + ".class" || Path.GetFileName(f).StartsWith(name + "$", StringComparison.Ordinal))
                .Order(StringComparer.Ordinal)
                .ToList()
            : [];
    }

    /// <summary>Đối số để lệnh chạy test không biên dịch lại class production (bản đã instrument).</summary>
    public static IReadOnlyList<string> KeepProductionClasses(string tool, string? module, string initScript) =>
        tool == "gradle"
            ? ["-x", Block1.GradleProbe.Task(module, "compileJava"), "--init-script", initScript]
            : ["-Dmaven.main.skip=true"];

    public async Task<ToolResult> MeasureAsync(SuiteToMeasure suite, TaskContext task, MeasureOptions options, CancellationToken cancellationToken)
    {
        var runtime = await tools.EnsureAsync("jacoco-agent", cancellationToken).ConfigureAwait(false);
        var cli = await tools.EnsureAsync("jacoco-cli", cancellationToken).ConfigureAwait(false);
        var files = suite.Files with { ExtraTestClasspath = [.. suite.Files.ExtraTestClasspath, runtime] };
        var prepared = await preparer.PrepareAsync(files, task, "jacoco", compile: true, options, cancellationToken).ConfigureAwait(false);
        try
        {
            var fqn = task.Metadata.Cut.Fqn;
            var cutClasses = CutClassFiles(prepared.ProductionClasses, fqn);
            if (cutClasses.Count == 0)
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, $"không thấy file .class của {fqn} trong thư mục class production");
            }

            var original = Path.Combine(prepared.Copy, "arrow-jacoco", "original");
            var instrumented = Path.Combine(prepared.Copy, "arrow-jacoco", "instrumented");
            foreach (var file in cutClasses)
            {
                var copyTo = Path.Combine(original, Path.GetRelativePath(prepared.ProductionClasses, file));
                Directory.CreateDirectory(Path.GetDirectoryName(copyTo)!);
                File.Copy(file, copyTo);
            }

            var instrument = await JavaAsync(prepared.Context.JdkHome, ["-jar", cli, "instrument", original, "--dest", instrumented],
                prepared.Copy, TimeSpan.FromMinutes(5), task, Log(options, "jacoco-instrument"), cancellationToken).ConfigureAwait(false);
            if (!instrument.Succeeded)
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, $"jacococli instrument lỗi: {instrument.Tail(5)}", retryable: instrument.TimedOut);
            }

            var hashes = new Dictionary<string, string>(StringComparer.Ordinal);
            foreach (var file in cutClasses)
            {
                File.Copy(Path.Combine(instrumented, Path.GetRelativePath(prepared.ProductionClasses, file)), file, overwrite: true);
                hashes[file] = Hashing.Sha256Bytes(File.ReadAllBytes(file));
            }

            var initScript = Path.Combine(prepared.Copy, GradleInitScriptName);
            await File.WriteAllTextAsync(initScript, GradleInitScript.Replace("\r\n", "\n"), new UTF8Encoding(false), cancellationToken).ConfigureAwait(false);
            var exec = Path.Combine(prepared.Copy, "arrow-jacoco.exec");
            var context = prepared.Context with { ExtraJavaToolOptions = $"-Djacoco-agent.destfile={exec}" };
            var arguments = BuildCommands.RunSingleTest(context.Plan, suite.Files.TestFqn)
                .Concat(prepared.ExtraArgs)
                .Concat(KeepProductionClasses(task.Workspace.Tool, task.Workspace.Module, initScript))
                .ToList();
            var run = await build.RunAsync(context, arguments, cancellationToken).ConfigureAwait(false);
            SaveText(task, Log(options, "jacoco-test"), BuildCommands.FormatLog(run, context.JdkHome));
            if (run.Failure is BuildFailureKind.Timeout or BuildFailureKind.Network)
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, run.Failure == BuildFailureKind.Timeout ? $"chạy test quá {options.StepTimeoutSeconds} s" : "lỗi mạng khi chạy test", retryable: true);
            }

            if (hashes.Any(h => !File.Exists(h.Key) || Hashing.Sha256Bytes(File.ReadAllBytes(h.Key)) != h.Value))
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, "CUT bị biên dịch lại khi chạy test nên mất bản đã instrument");
            }

            var target = TestResultClassifier.Classify(reports.Read(context.BuildRootPath), suite.Files.TestFqn);
            if (target.FailedState is { } state)
            {
                var first = target.Failures.FirstOrDefault();
                throw new ToolFailure(ErrorCodes.B4JacocoFailed,
                    $"bộ test không pass khi đo JaCoCo ({state.ToString().ToUpperInvariant()}{(first is null ? "" : $": {first.Method} {first.Type} {first.Message}")})");
            }

            if (!File.Exists(exec) || new FileInfo(exec).Length == 0)
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, "không có jacoco.exec (runtime JaCoCo không ghi dữ liệu)");
            }

            var csv = Path.Combine(prepared.Copy, "arrow-jacoco.csv");
            var report = await JavaAsync(context.JdkHome,
                ["-jar", cli, "report", exec, "--classfiles", original, "--sourcefiles", SuitePreparer.SourceRoot(prepared, task.Metadata), "--csv", csv],
                prepared.Copy, TimeSpan.FromMinutes(5), task, Log(options, "jacoco-report"), cancellationToken).ConfigureAwait(false);
            if (!report.Succeeded || !File.Exists(csv))
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, $"jacococli report lỗi: {report.Tail(5)}", retryable: report.TimedOut);
            }

            var text = await File.ReadAllTextAsync(csv, cancellationToken).ConfigureAwait(false);
            SaveText(task, ResultPaths.Join(options.ResultDir, "jacoco.csv"), text);
            var coverage = JacocoCsvParser.Parse(text, fqn) ?? throw new ToolFailure(ErrorCodes.B4JacocoFailed, $"báo cáo JaCoCo không có {fqn}");
            return new ToolResult { Tool = Name, Coverage = coverage };
        }
        finally
        {
            Workspace.DeleteDirectory(prepared.Copy);
        }
    }
}

/// <summary>
/// PIT (spec 04 Mục 4.3): chuẩn bị (biên dịch test, classpath) → <c>MutationCoverageReport</c> với <c>--targetClasses &lt;FQN&gt;,&lt;FQN&gt;$*</c>,
/// <c>--targetTests &lt;test FQN&gt;</c>, <c>DEFAULTS</c>, 1 luồng, XML; plugin JUnit 5 / TestNG theo framework của module; kiểm tra phạm vi mutant.
/// Minion chạy test bằng JDK của task (<c>--jvmPath</c>), tiến trình chính bằng JDK 11+.
/// </summary>
public sealed class PitTool(SuitePreparer preparer, IToolsCatalog tools, IProcessRunner runner, IWorkspaceManager workspace, ITaskResultRepository results, IReadOnlyDictionary<int, string> jdkHomes)
    : QualityToolBase(runner, workspace, results), IQualityTool
{
    public static readonly TimeSpan Timeout = TimeSpan.FromMinutes(30);

    /// <summary>PIT 1.17 cần Java 11 trở lên để chạy.</summary>
    public const int MinimumJdk = 11;

    public string Name => "pit";

    private static readonly string[] FrameworkJarPrefixes = ["junit-platform-", "junit-jupiter-", "opentest4j-", "apiguardian-", "testng-", "jcommander-"];

    public async Task<ToolResult> MeasureAsync(SuiteToMeasure suite, TaskContext task, MeasureOptions options, CancellationToken cancellationToken)
    {
        var launch = new List<string>();
        foreach (var id in new[] { "pitest", "pitest-entry", "pitest-command-line", "pitest-html-report", "commons-text", "commons-lang3", "jopt-simple" })
        {
            launch.Add(await tools.EnsureAsync(id, cancellationToken).ConfigureAwait(false));
        }

        var prepared = await preparer.PrepareAsync(suite.Files, task, "pit", compile: true, options, cancellationToken).ConfigureAwait(false);
        try
        {
            var classpath = new List<string> { prepared.ProductionClasses, prepared.TestClasses };
            classpath.AddRange(prepared.Classpath.Where(c => !classpath.Contains(c)));
            var framework = task.Metadata.TestFramework.Name;
            if (framework == "junit5")
            {
                launch.Add(await tools.EnsureAsync("pitest-junit5-plugin", cancellationToken).ConfigureAwait(false));
                if (NeedsNewerPlatform(PlatformVersion(prepared.Classpath)))
                {
                    // pitest-junit5-plugin cần JUnit Platform 1.5+ (org.junit.platform.commons.PreconditionViolationException): chỉ khi chạy PIT,
                    // bộ jar JUnit 5 của module được thay bằng 5.5.2 / 1.5.2 (tương thích ngược với test viết cho 5.0–5.4).
                    var hadParams = classpath.Any(c => Path.GetFileName(c).StartsWith("junit-jupiter-params-", StringComparison.Ordinal));
                    classpath.RemoveAll(IsJUnit5Jar);
                    foreach (var (coordinate, file) in ReplacementJUnit5.Where(r => hadParams || !r.Coordinate.Contains("junit-jupiter-params", StringComparison.Ordinal)))
                    {
                        classpath.Add(await tools.EnsureMavenAsync(coordinate, file, cancellationToken).ConfigureAwait(false));
                    }
                }
                else if (!classpath.Any(c => Path.GetFileName(c).StartsWith("junit-platform-launcher", StringComparison.Ordinal)))
                {
                    var version = PlatformVersion(prepared.Classpath) ?? "1.10.2";
                    classpath.Add(await tools.EnsureMavenAsync($"org.junit.platform:junit-platform-launcher:{version}", $"junit-platform/junit-platform-launcher-{version}.jar", cancellationToken).ConfigureAwait(false));
                }
            }
            else if (framework == "testng")
            {
                launch.Add(await tools.EnsureAsync("pitest-testng-plugin", cancellationToken).ConfigureAwait(false));
            }

            var jdk = task.Workspace.JdkMajor >= MinimumJdk ? prepared.Context.JdkHome
                : jdkHomes.Where(j => j.Key >= MinimumJdk).OrderBy(j => j.Key).Select(j => j.Value).FirstOrDefault()
                  ?? throw new ToolFailure(ErrorCodes.B4PitFailed, $"PIT cần JDK {MinimumJdk}+ mà máy không có");
            var classPathFile = Path.Combine(prepared.Copy, "arrow-pit-classpath.txt");
            await File.WriteAllLinesAsync(classPathFile, classpath, cancellationToken).ConfigureAwait(false);
            var reportDir = Path.Combine(prepared.Copy, "arrow-pit-report");
            var fqn = task.Metadata.Cut.Fqn;
            // Lệnh khởi chạy chỉ cần PIT, plugin và jar của test framework; classpath đầy đủ đi qua --classPathFile
            // (dòng lệnh Windows giới hạn 32.767 ký tự).
            var frameworkJars = classpath.Where(c => FrameworkJarPrefixes.Any(p => Path.GetFileName(c).StartsWith(p, StringComparison.Ordinal)));
            var pit = await JavaAsync(jdk,
            [
                "-cp", string.Join(Path.PathSeparator, launch.Concat(frameworkJars)),
                "org.pitest.mutationtest.commandline.MutationCoverageReport",
                "--reportDir", reportDir,
                "--targetClasses", $"{fqn},{fqn}$*",
                "--targetTests", suite.Files.TestFqn,
                "--classPathFile", classPathFile,
                "--mutators", "DEFAULTS",
                "--threads", "1",
                "--timestampedReports=false",
                "--outputFormats", "XML",
                "--failWhenNoMutations=false",
                "--sourceDirs", SuitePreparer.SourceRoot(prepared, task.Metadata),
                // Tiến trình chính của PIT cần JDK 11+, nhưng test chạy trong minion bằng đúng JDK của task (như khi xác minh): thư viện
                // cũ (Mockito 1–2, ByteBuddy cũ) không chạy trên JDK 11 nên bộ test VALID trên JDK 8 sẽ "không xanh" dưới PIT.
                // Lớp minion của PIT biên dịch cho Java 8.
                "--jvmPath", ToolLocator.JavaExecutable(prepared.Context.JdkHome),
            ], prepared.ModuleDir, Timeout, task, Log(options, "pit"), cancellationToken).ConfigureAwait(false);

            var xmlFile = Path.Combine(reportDir, "mutations.xml");
            if (!pit.Succeeded || !File.Exists(xmlFile))
            {
                throw new ToolFailure(ErrorCodes.B4PitFailed, pit.TimedOut ? $"PIT quá {Timeout.TotalMinutes:0} phút" : $"PIT lỗi (mã {pit.ExitCode}):\n{pit.Tail(20)}", retryable: pit.TimedOut);
            }

            var xml = await File.ReadAllTextAsync(xmlFile, cancellationToken).ConfigureAwait(false);
            SaveText(task, ResultPaths.Join(options.ResultDir, "mutations.xml"), xml);
            var (mutation, outOfScope) = PitXmlParser.Parse(xml, fqn);
            if (outOfScope.Count > 0)
            {
                throw new ToolFailure(ErrorCodes.B4PitScope, "PIT tạo mutant cho class ngoài CUT: " + string.Join(", ", outOfScope.Take(5)));
            }

            return new ToolResult { Tool = Name, Mutation = mutation };
        }
        finally
        {
            Workspace.DeleteDirectory(prepared.Copy);
        }
    }

    /// <summary>Bộ JUnit 5 thay thế khi chạy PIT trên module có JUnit Platform cũ hơn 1.5.</summary>
    public static readonly IReadOnlyList<(string Coordinate, string File)> ReplacementJUnit5 =
    [
        ("org.junit.jupiter:junit-jupiter-api:5.5.2", "junit-platform/junit-jupiter-api-5.5.2.jar"),
        ("org.junit.jupiter:junit-jupiter-engine:5.5.2", "junit-platform/junit-jupiter-engine-5.5.2.jar"),
        ("org.junit.jupiter:junit-jupiter-params:5.5.2", "junit-platform/junit-jupiter-params-5.5.2.jar"),
        ("org.junit.platform:junit-platform-commons:1.5.2", "junit-platform/junit-platform-commons-1.5.2.jar"),
        ("org.junit.platform:junit-platform-engine:1.5.2", "junit-platform/junit-platform-engine-1.5.2.jar"),
        ("org.junit.platform:junit-platform-launcher:1.5.2", "junit-platform/junit-platform-launcher-1.5.2.jar"),
        ("org.opentest4j:opentest4j:1.2.0", "junit-platform/opentest4j-1.2.0.jar"),
    ];

    private static bool IsJUnit5Jar(string path)
    {
        var name = Path.GetFileName(path);
        return name.StartsWith("junit-jupiter-", StringComparison.Ordinal) || name.StartsWith("junit-platform-", StringComparison.Ordinal)
               || name.StartsWith("opentest4j-", StringComparison.Ordinal);
    }

    /// <summary>JUnit Platform cũ hơn 1.5 (JUnit Jupiter 5.0–5.4): pitest-junit5-plugin không chạy được.</summary>
    public static bool NeedsNewerPlatform(string? platformVersion) =>
        platformVersion is not null && Version.TryParse(platformVersion.Split('-')[0], out var version) && version < new Version(1, 5);

    /// <summary>Phiên bản JUnit Platform của module, đọc từ tên jar <c>junit-platform-commons-&lt;v&gt;.jar</c> / <c>junit-platform-engine-…</c>.</summary>
    public static string? PlatformVersion(IReadOnlyList<string> classpath)
    {
        foreach (var prefix in new[] { "junit-platform-engine-", "junit-platform-commons-" })
        {
            var jar = classpath.Select(Path.GetFileNameWithoutExtension).FirstOrDefault(n => n is not null && n.StartsWith(prefix, StringComparison.Ordinal));
            if (jar is not null)
            {
                return jar[prefix.Length..];
            }
        }

        return null;
    }
}

/// <summary>
/// tsDetect (spec 04 Mục 4.4): chỉ đọc mã nguồn, nên ghi file test và CUT vào thư mục tạm rồi chạy <c>TestSmellDetector.jar &lt;csv&gt;</c>;
/// mật độ = tổng smell / số test method (đếm bằng java-helper).
/// </summary>
public sealed class TsDetectTool(IToolsCatalog tools, IJavaHelper javaHelper, IProcessRunner runner, IWorkspaceManager workspace, ITaskResultRepository results)
    : QualityToolBase(runner, workspace, results), IQualityTool
{
    public static readonly TimeSpan Timeout = TimeSpan.FromMinutes(5);

    public string Name => "tsdetect";

    public async Task<ToolResult> MeasureAsync(SuiteToMeasure suite, TaskContext task, MeasureOptions options, CancellationToken cancellationToken)
    {
        var jar = await tools.EnsureAsync("tsdetect", cancellationToken).ConfigureAwait(false);
        var dir = Path.Combine(Workspace.TaskDir(task.RunId, task.Key), "tmp", "tsdetect-" + Guid.NewGuid().ToString("N")[..8]);
        Directory.CreateDirectory(dir);
        try
        {
            var testSource = suite.Files.Files.TryGetValue(MainFile(suite), out var code) ? code : "";
            var testFile = Path.Combine(dir, "test", Path.GetFileName(MainFile(suite)));
            var cutFile = Path.Combine(dir, "main", Path.GetFileName(task.Metadata.Paths.CutPath));
            Directory.CreateDirectory(Path.GetDirectoryName(testFile)!);
            Directory.CreateDirectory(Path.GetDirectoryName(cutFile)!);
            await File.WriteAllTextAsync(testFile, testSource, cancellationToken).ConfigureAwait(false);
            var cutSource = Path.Combine(Workspace.WorkDir, task.Workspace.CompiledSnapshot.Replace('/', Path.DirectorySeparatorChar), task.Metadata.Paths.CutPath.Replace('/', Path.DirectorySeparatorChar));
            File.Copy(cutSource, cutFile, overwrite: true);

            var input = Path.Combine(dir, "input.csv");
            await File.WriteAllTextAsync(input, $"app,{testFile},{cutFile}\n", cancellationToken).ConfigureAwait(false);
            var run = await JavaAsync(task.Workspace.JdkHome, ["-jar", jar, input], dir, Timeout, task, Log(options, "tsdetect"), cancellationToken).ConfigureAwait(false);
            var output = Directory.GetFiles(dir, "*.csv").Where(f => !f.EndsWith("input.csv", StringComparison.Ordinal)).OrderByDescending(File.GetLastWriteTimeUtc).FirstOrDefault();
            if (!run.Succeeded || output is null)
            {
                throw new ToolFailure(ErrorCodes.B4TsDetectFailed, run.TimedOut ? "tsDetect quá 5 phút" : $"tsDetect lỗi: {run.Tail(5)}", retryable: run.TimedOut);
            }

            var csv = await File.ReadAllTextAsync(output, cancellationToken).ConfigureAwait(false);
            SaveText(task, ResultPaths.Join(options.ResultDir, "tsdetect.csv"), csv);
            var methods = (await javaHelper.ListTestMethodsAsync(testSource, cancellationToken).ConfigureAwait(false)).Methods.Count;
            var smells = TsDetectCsvParser.Parse(csv, methods) ?? throw new ToolFailure(ErrorCodes.B4TsDetectFailed, "CSV của tsDetect không có dòng kết quả");
            return new ToolResult { Tool = Name, Smells = smells };
        }
        finally
        {
            Workspace.DeleteDirectory(dir);
        }
    }

    private static string MainFile(SuiteToMeasure suite) => Block3.Verifier.MainTestFile(suite.Files);
}

/// <summary>
/// Chạy lần lượt các công cụ trên một bộ test (spec 04 Mục 5): một công cụ lỗi không chặn công cụ khác; lỗi môi trường thử lại tối đa
/// <c>infra_attempts</c> lần, vẫn lỗi thì <c>needs_rerun</c>. Ghi <c>&lt;tool&gt;.json</c> và <c>outcome.json</c> vào thư mục của bộ.
/// </summary>
public sealed class QualityAssessor(IEnumerable<IQualityTool> tools, ITaskResultRepository results, ILogRepository log) : IQualityAssessor
{
    private readonly IReadOnlyList<IQualityTool> _tools = tools.ToList();

    public async Task<QualityOutcome> MeasureAsync(SuiteToMeasure suite, TaskContext task, IReadOnlyList<string> toolNames, MeasureOptions options, CancellationToken cancellationToken)
    {
        var outcome = new QualityOutcome { SuiteId = suite.SuiteId };
        foreach (var tool in _tools.Where(t => toolNames.Contains(t.Name)))
        {
            var watch = Stopwatch.StartNew();
            ToolResult? result = null;
            for (var attempt = 1; attempt <= Math.Max(1, options.InfraAttempts); attempt++)
            {
                if (attempt > 1)
                {
                    await Task.Delay(options.RetryDelay, cancellationToken).ConfigureAwait(false);
                }

                try
                {
                    result = await tool.MeasureAsync(suite, task, options, cancellationToken).ConfigureAwait(false) with { Attempts = attempt };
                    break;
                }
                catch (ToolFailure failure)
                {
                    log.Warn($"[K4] {task.Key.TaskId} {suite.SuiteId} {tool.Name}: {failure.Code} {failure.Message} (lần {attempt})");
                    var lastAttempt = !failure.Retryable || attempt == options.InfraAttempts;
                    result = new ToolResult
                    {
                        Tool = tool.Name,
                        Attempts = attempt,
                        Outcome = new BlockOutcome
                        {
                            Outcome = failure.Retryable ? OutcomeKind.Infra : OutcomeKind.Failed, Code = failure.Code, Message = failure.Message, NeedsRerun = failure.Retryable,
                        },
                    };
                    if (lastAttempt)
                    {
                        break;
                    }
                }
                catch (ArrowException ex)
                {
                    // Công cụ không có hoặc không tải được: lỗi cấu hình của máy, cần chạy lại sau khi sửa.
                    result = new ToolResult { Tool = tool.Name, Attempts = attempt, Outcome = new BlockOutcome { Outcome = OutcomeKind.Infra, Code = ex.Code, Message = ex.Message, NeedsRerun = true } };
                    break;
                }
            }

            result = result! with { Seconds = Math.Round(watch.Elapsed.TotalSeconds, 1) };
            var file = ResultPaths.Join(options.ResultDir, tool.Name + ".json");
            if (result.Coverage is not null)
            {
                results.SaveJson(task.RunId, task.Key, file, result.Coverage);
            }
            else if (result.Mutation is not null)
            {
                results.SaveJson(task.RunId, task.Key, file, result.Mutation);
            }
            else if (result.Smells is not null)
            {
                results.SaveJson(task.RunId, task.Key, file, result.Smells);
            }

            outcome.Tools[tool.Name] = result.Outcome with { Log = $"logs/{tool.Name}.log", Seconds = result.Seconds };
        }

        results.SaveJson(task.RunId, task.Key, ResultPaths.Join(options.ResultDir, "outcome.json"), outcome);
        return outcome;
    }
}
