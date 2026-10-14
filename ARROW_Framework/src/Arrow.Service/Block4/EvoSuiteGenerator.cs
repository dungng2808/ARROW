using System.Diagnostics;
using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block4;

/// <summary>
/// Bộ test EvoSuite cho RQ3 (spec 04 Mục 3): trên bản sao mới đã biên dịch, chạy EvoSuite 1.2.0 cấu hình mặc định, <c>-seed 42</c>,
/// <c>-class &lt;CUT&gt;</c>, <c>-projectCP</c> = class production của module + classpath test; timeout 20 phút. Task cần JDK &gt; 11 →
/// <c>UNSUPPORTED_JDK</c>. Bộ sinh ra (test class + scaffolding, classpath phụ là runtime của EvoSuite) được khối 3 xác minh, không sửa.
/// </summary>
public sealed class EvoSuiteGenerator(SuitePreparer preparer, IToolsCatalog tools, IProcessRunner runner, IWorkspaceManager workspace, ITaskResultRepository results) : IComparisonGenerator
{
    public const int MaxJdk = 11;
    public static readonly TimeSpan Timeout = TimeSpan.FromMinutes(20);

    public async Task<ComparisonSuite> GenerateAsync(TaskContext task, CancellationToken cancellationToken)
    {
        var watch = Stopwatch.StartNew();
        ComparisonSuite result;
        if (task.Workspace.JdkMajor > MaxJdk)
        {
            result = new ComparisonSuite
            {
                Status = "UNSUPPORTED_JDK", Code = ErrorCodes.B4EvoSuiteUnsupportedJdk,
                Reason = $"task cần JDK {task.Workspace.JdkMajor}; EvoSuite 1.2.0 chỉ chạy trên Java ≤ {MaxJdk}",
            };
        }
        else
        {
            result = await RunAsync(task, cancellationToken).ConfigureAwait(false);
        }

        result = result with { Seconds = Math.Round(watch.Elapsed.TotalSeconds, 1) };
        results.SaveJson(task.RunId, task.Key, ResultPaths.Join(ResultPaths.EvoSuiteDir, "comparison.json"), result);
        return result;
    }

    private async Task<ComparisonSuite> RunAsync(TaskContext task, CancellationToken cancellationToken)
    {
        var evosuite = await tools.EnsureAsync("evosuite", cancellationToken).ConfigureAwait(false);
        var runtime = await tools.EnsureAsync("evosuite-runtime", cancellationToken).ConfigureAwait(false);
        var options = new MeasureOptions { ResultDir = ResultPaths.EvoSuiteDir };
        PreparedSuite prepared;
        try
        {
            prepared = await preparer.PrepareAsync(new SuiteFiles { SuiteId = "evosuite" }, task, "evosuite", compile: true, options, cancellationToken).ConfigureAwait(false);
        }
        catch (ToolFailure failure)
        {
            return Failed($"không biên dịch được snapshot: {failure.Message}");
        }

        try
        {
            var projectCp = new List<string> { prepared.ProductionClasses };
            projectCp.AddRange(prepared.Classpath.Where(c => c != prepared.TestClasses && !projectCp.Contains(c)));
            var fqn = task.Metadata.Cut.Fqn;
            var spec = new ProcessSpec(
                ToolLocator.JavaExecutable(task.Workspace.JdkHome),
                ["-cp", evosuite, "org.evosuite.EvoSuite", "-class", fqn, "-projectCP", string.Join(Path.PathSeparator, projectCp), "-seed", "42"],
                prepared.Copy,
                new Dictionary<string, string?> { ["JAVA_HOME"] = task.Workspace.JdkHome, ["JAVA_TOOL_OPTIONS"] = BuildRunner.JavaToolOptions },
                Timeout);
            var run = await runner.RunAsync(spec, cancellationToken).ConfigureAwait(false);
            results.SaveText(task.RunId, task.Key, ResultPaths.Join(ResultPaths.EvoSuiteDir, "logs", "evosuite.log"),
                $"$ {spec.FileName} {string.Join(' ', spec.Arguments)}\n{BuildCommands.TruncateLog(run.Output)}\n# exit={run.ExitCode?.ToString() ?? "-"} timeout={(run.TimedOut ? "yes" : "no")}\n");

            var package = task.Metadata.Cut.Package;
            var generatedDir = Path.Combine(prepared.Copy, "evosuite-tests", package.Replace('.', Path.DirectorySeparatorChar));
            var name = task.Metadata.Cut.SimpleName + "_ESTest";
            var testFile = Path.Combine(generatedDir, name + ".java");
            if (run.TimedOut || !File.Exists(testFile))
            {
                return Failed(run.TimedOut ? "EvoSuite quá 20 phút" : run.ExitCode != 0 ? $"EvoSuite lỗi (mã {run.ExitCode}): {run.Tail(5)}" : "EvoSuite không sinh file test");
            }

            var testRoot = task.Metadata.Paths.TestRoot.TrimEnd('/');
            var packagePath = package.Length == 0 ? "" : package.Replace('.', '/') + "/";
            var files = new Dictionary<string, string>();
            foreach (var file in Directory.GetFiles(generatedDir, name + "*.java"))
            {
                files[$"{testRoot}/{packagePath}{Path.GetFileName(file)}"] = (await File.ReadAllTextAsync(file, cancellationToken).ConfigureAwait(false)).Replace("\r\n", "\n");
            }

            foreach (var (path, content) in files)
            {
                results.SaveText(task.RunId, task.Key, ResultPaths.Join(ResultPaths.EvoSuiteDir, "suite", Path.GetFileName(path)), content);
            }

            return new ComparisonSuite
            {
                Status = "OK",
                Suite = new SuiteFiles
                {
                    SuiteId = "evosuite",
                    TestFqn = (package.Length == 0 ? "" : package + ".") + name,
                    Files = files,
                    ExtraTestClasspath = [runtime],
                },
            };
        }
        finally
        {
            workspace.DeleteDirectory(prepared.Copy);
        }
    }

    private static ComparisonSuite Failed(string reason) => new() { Status = "FAILED", Code = ErrorCodes.B4EvoSuiteFailed, Reason = reason };
}
