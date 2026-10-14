using System.Diagnostics;
using System.Text;
using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block4;
using Arrow.Models.Common;
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
/// JaCoCo (spec 04 Mục 4.2): chạy riêng bộ test với agent gắn qua <c>JAVA_TOOL_OPTIONS</c> (<c>includes=&lt;FQN&gt;:&lt;FQN&gt;$*</c>),
/// rồi <c>jacococli report --csv</c> trên class production của module; chỉ cộng dòng của CUT và class lồng.
/// </summary>
public sealed class JacocoTool(SuitePreparer preparer, IBuildRunner build, IToolsCatalog tools, IProcessRunner runner, IWorkspaceManager workspace, ITaskResultRepository results)
    : QualityToolBase(runner, workspace, results), IQualityTool
{
    public string Name => "jacoco";

    public async Task<ToolResult> MeasureAsync(SuiteToMeasure suite, TaskContext task, MeasureOptions options, CancellationToken cancellationToken)
    {
        var agent = await tools.EnsureAsync("jacoco-agent", cancellationToken).ConfigureAwait(false);
        var cli = await tools.EnsureAsync("jacoco-cli", cancellationToken).ConfigureAwait(false);
        var prepared = await preparer.PrepareAsync(suite.Files, task, "jacoco", compile: true, options, cancellationToken).ConfigureAwait(false);
        try
        {
            var fqn = task.Metadata.Cut.Fqn;
            var exec = Path.Combine(prepared.Copy, "arrow-jacoco.exec");
            var context = prepared.Context with { ExtraJavaToolOptions = $"-javaagent:{agent}=destfile={exec},append=true,includes={fqn}:{fqn}$*" };
            var run = await build.RunAsync(context, BuildCommands.RunSingleTest(context.Plan, suite.Files.TestFqn).Concat(prepared.ExtraArgs).ToList(), cancellationToken).ConfigureAwait(false);
            SaveText(task, Log(options, "jacoco-test"), BuildCommands.FormatLog(run, context.JdkHome));
            if (run.Failure is BuildFailureKind.Timeout or BuildFailureKind.Network)
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, run.Failure == BuildFailureKind.Timeout ? $"chạy test quá {options.StepTimeoutSeconds} s" : "lỗi mạng khi chạy test", retryable: true);
            }

            if (!File.Exists(exec))
            {
                throw new ToolFailure(ErrorCodes.B4JacocoFailed, "không có jacoco.exec (test không chạy dưới agent)");
            }

            var csv = Path.Combine(prepared.Copy, "arrow-jacoco.csv");
            var report = await JavaAsync(context.JdkHome,
                ["-jar", cli, "report", exec, "--classfiles", prepared.ProductionClasses, "--sourcefiles", SuitePreparer.SourceRoot(prepared, task.Metadata), "--csv", csv],
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
                if (!classpath.Any(c => Path.GetFileName(c).StartsWith("junit-platform-launcher", StringComparison.Ordinal)))
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
