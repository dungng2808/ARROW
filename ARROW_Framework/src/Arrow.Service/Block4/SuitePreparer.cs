using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block1;
using Arrow.Service.Block3;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block4;

/// <summary>Bản sao đã chuẩn bị để đo: thư mục, cách gọi build, đối số thêm, thư mục module, class production/test, classpath test.</summary>
public sealed record PreparedSuite(
    string Copy,
    BuildContext Context,
    IReadOnlyList<string> ExtraArgs,
    string ModuleDir,
    string ProductionClasses,
    string TestClasses,
    IReadOnlyList<string> Classpath);

/// <summary>
/// Chuẩn bị chung của khối 4 (spec 04 Mục 4.1): bản sao mới của snapshot → ghi mọi file của bộ test và classpath phụ → (nếu cần) biên dịch
/// test và lấy classpath trong <b>một</b> lần gọi build (Maven <c>test-compile … build-classpath</c>; Gradle <c>testClasses arrowClasspath</c>).
/// Biên dịch thất bại sau 3 lần thử → <c>B4_SUITE_COMPILE</c>.
/// </summary>
public sealed class SuitePreparer(IWorkspaceManager workspace, IBuildRunner build, ITaskResultRepository results)
{
    public async Task<PreparedSuite> PrepareAsync(SuiteFiles suite, TaskContext task, string label, bool compile, MeasureOptions options, CancellationToken cancellationToken)
    {
        var snapshot = Path.Combine(workspace.WorkDir, task.Workspace.CompiledSnapshot.Replace('/', Path.DirectorySeparatorChar));
        var timeout = TimeSpan.FromSeconds(options.StepTimeoutSeconds);
        string? lastError = null;
        for (var attempt = 1; attempt <= Math.Max(1, options.InfraAttempts); attempt++)
        {
            if (attempt > 1)
            {
                await Task.Delay(options.RetryDelay, cancellationToken).ConfigureAwait(false);
            }

            var copy = await workspace.NewCopyAsync(snapshot, task.RunId, task.Key, label, cancellationToken).ConfigureAwait(false);
            var extra = await SuiteWriter.WriteAsync(copy, suite, task, cancellationToken).ConfigureAwait(false);
            var context = BuildCommands.FromWorkspace(task.Workspace, workspace.WorkDir, copy, timeout);
            var moduleDir = Path.GetFullPath(Path.Combine(copy, task.Metadata.ProjectStructure.ModuleRoot.Replace('/', Path.DirectorySeparatorChar)));
            var gradle = task.Workspace.Tool == "gradle";
            var production = Path.Combine(moduleDir, gradle ? Path.Combine("build", "classes", "java", "main") : Path.Combine("target", "classes"));
            var testClasses = Path.Combine(moduleDir, gradle ? Path.Combine("build", "classes", "java", "test") : Path.Combine("target", "test-classes"));
            if (!compile)
            {
                return new PreparedSuite(copy, context, extra, moduleDir, production, testClasses, []);
            }

            var arguments = gradle
                ? BuildCommands.GradleCompileWithClasspath(task.Workspace.Module)
                : ["-DskipTests", "test-compile", BuildCommands.DependencyPlugin(task.Metadata.BuildTool.Version) + ":build-classpath", "-Dmdep.includeScope=test", "-Dmdep.outputFile=" + BuildCommands.MavenClasspathFile];
            var run = await build.RunAsync(context, arguments.Concat(extra).ToList(), cancellationToken).ConfigureAwait(false);
            results.SaveText(task.RunId, task.Key, ResultPaths.Join(options.ResultDir, "logs", $"{label}-prepare{(attempt > 1 ? $"-try{attempt}" : "")}.log"), BuildCommands.FormatLog(run, context.JdkHome));
            if (run.Failure == BuildFailureKind.None)
            {
                var classpath = gradle
                    ? GradleProbe.Parse(run.Result.Output).Classpath
                    : ReadMavenClasspath(Path.Combine(moduleDir, BuildCommands.MavenClasspathFile));
                return new PreparedSuite(copy, context, extra, moduleDir, production, testClasses, classpath);
            }

            lastError = run.Failure == BuildFailureKind.Timeout ? $"quá {options.StepTimeoutSeconds} s" : run.Result.Tail(5);
            workspace.DeleteDirectory(copy);
        }

        throw new ToolFailure(ErrorCodes.B4SuiteCompile, $"Không biên dịch được bộ test {suite.SuiteId} sau {options.InfraAttempts} lần thử: {lastError}");
    }

    private static List<string> ReadMavenClasspath(string file) =>
        File.Exists(file)
            ? File.ReadAllText(file).Trim().Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries).ToList()
            : [];

    /// <summary>Thư mục mã nguồn production của module (từ M), tuyệt đối trong bản sao.</summary>
    public static string SourceRoot(PreparedSuite prepared, RepositoryMetadata metadata) =>
        Path.GetFullPath(Path.Combine(prepared.Copy, metadata.Paths.SourceRoot.Replace('/', Path.DirectorySeparatorChar)));
}
