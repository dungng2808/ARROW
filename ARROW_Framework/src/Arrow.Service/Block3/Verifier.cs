using System.Diagnostics;
using System.Text;
using System.Xml.Linq;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block3;

/// <summary>
/// Xác minh 5 bước (spec 03 Mục 3): mỗi lần xác minh dùng bản sao mới của snapshot; bước 1–4 chung một bản sao,
/// bước 5 trên bản sao mới khác. Lỗi hạ tầng (timeout, mạng, công cụ, workspace) được thử lại tối đa <c>infra_attempts</c> lần
/// trên bản sao mới; vẫn lỗi thì trả <c>INFRA</c> kèm mã <c>B3_*</c>.
/// </summary>
public sealed class Verifier(
    IWorkspaceManager workspace,
    IBuildRunner build,
    ITestReportReader reports,
    IJavaHelper javaHelper,
    ITaskResultRepository results,
    ILogRepository log) : IVerifier
{
    public const int ExcerptLines = 120;

    /// <summary>Lỗi hạ tầng của một bước, làm dừng lần xác minh.</summary>
    private sealed class InfraStop(string code, string reason) : Exception(reason)
    {
        public string Code { get; } = code;
    }

    private sealed class Run(SuiteFiles suite, TaskContext task, VerifyOptions options, CancellationToken token)
    {
        public SuiteFiles Suite { get; } = suite;
        public TaskContext Task { get; } = task;
        public VerifyOptions Options { get; } = options;
        public CancellationToken Token { get; } = token;
        public List<StepRecord> Steps { get; } = [];
        public List<string> Copies { get; } = [];

        /// <summary>Đối số thêm của lệnh build cho từng bản sao (Gradle: init script classpath phụ).</summary>
        public Dictionary<string, IReadOnlyList<string>> CopyArgs { get; } = [];
        public double Seconds { get; set; }
    }

    public async Task<VerifyResult> VerifyAsync(SuiteFiles suite, TaskContext task, VerifyOptions options, CancellationToken cancellationToken)
    {
        var run = new Run(suite, task, options, cancellationToken);
        var mainFile = MainTestFile(suite);
        var code = suite.Files.GetValueOrDefault(mainFile) ?? "";
        var (codeHash, assertions) = await MeasureAsync(code, cancellationToken).ConfigureAwait(false);
        VerifyResult? result = null;
        try
        {
            result = await VerifyCoreAsync(run, mainFile, code).ConfigureAwait(false);
        }
        catch (InfraStop stop)
        {
            result = new VerifyResult { State = VerificationState.Infra, InfraReason = $"{stop.Code}: {stop.Message}", NeedsRerun = true };
            log.Warn($"[K3] {task.Key.TaskId} {suite.SuiteId}: INFRA {stop.Code} {stop.Message}");
        }
        finally
        {
            // Bản sao luôn bị xóa, trừ khi bật keep_failed_workspaces và bộ test không VALID.
            if (!options.KeepFailedWorkspaces || result?.State == VerificationState.Valid)
            {
                foreach (var copy in run.Copies)
                {
                    workspace.DeleteDirectory(copy);
                }
            }
        }

        return result with
        {
            Group = StateGroups.GroupOf(result.State),
            Steps = run.Steps,
            TestFile = mainFile,
            CodeHash = codeHash,
            AssertionCount = assertions,
            BuildSeconds = Math.Round(run.Seconds, 1),
        };
    }

    private async Task<VerifyResult> VerifyCoreAsync(Run run, string mainFile, string code)
    {
        var plan = BuildCommands.FromWorkspace(run.Task.Workspace, workspace.WorkDir, "", TimeSpan.Zero).Plan;
        var generatedNames = GeneratedFileNames(run.Suite);

        // ── Bước 1: biên dịch ────────────────────────────────────────
        var (compile, copy) = await RunStepAsync(run, "compile", BuildCommands.CompileTests(plan)).ConfigureAwait(false);
        if (compile.Failure != BuildFailureKind.None)
        {
            var output = compile.Result.Output;
            var errors = JavacErrorParser.Parse(output, generatedNames);
            if (errors.Any(e => e.InGeneratedFile))
            {
                var lines = errors.Where(e => JavacErrorParser.FileName(e.File) == JavacErrorParser.FileName(mainFile)).Select(e => e.Line).Distinct().ToList();
                return new VerifyResult
                {
                    State = VerificationState.Compile,
                    CompileErrors = errors.Select(e => e with { File = BuildErrorExtractor.StripRoot(e.File, copy) }).ToList(),
                    ErrorSignature = SignatureBuilder.Compile(errors),
                    FailingMethods = await MethodsAtLinesAsync(code, lines, run.Token).ConfigureAwait(false),
                };
            }

            return new VerifyResult
            {
                State = VerificationState.Unknown,
                CompileErrors = errors.Select(e => e with { File = BuildErrorExtractor.StripRoot(e.File, copy) }).ToList(),
                BuildErrorExcerpt = BuildErrorExtractor.Excerpt(output, copy, ExcerptLines),
                ErrorSignature = SignatureBuilder.Unknown(output),
            };
        }

        // ── Bước 2–4: chạy riêng class test (cùng bản sao) ────────────
        var (single, singleCopy) = await RunStepAsync(run, "run_target", BuildCommands.RunSingleTest(plan, run.Suite.TestFqn), reuseCopy: copy).ConfigureAwait(false);
        var context = BuildCommands.FromWorkspace(run.Task.Workspace, workspace.WorkDir, singleCopy, TimeSpan.Zero);
        var targetTests = reports.Read(context.BuildRootPath);
        SaveReports(run, context.BuildRootPath, "run_target");
        var target = TestResultClassifier.Classify(targetTests, run.Suite.TestFqn);
        if (target.FailedState == VerificationState.Discovery && single.Failure == BuildFailureKind.Build && !NoTestsMarker(single.Result.Output))
        {
            // Lệnh chạy test hỏng mà không có báo cáo (JVM chạy test bị sập, System.exit…): lỗi khi chạy, không phải "không tìm thấy test".
            var step = BuildErrorExtractor.FailingStep(single.Result.Output);
            var failure = new FailureInfo
            {
                Method = "",
                Type = step,
                Message = BuildErrorExtractor.Excerpt(single.Result.Output, singleCopy, 8),
            };
            return new VerifyResult
            {
                State = VerificationState.Runtime,
                Failures = [failure],
                BuildErrorExcerpt = BuildErrorExtractor.Excerpt(single.Result.Output, singleCopy, ExcerptLines),
                ErrorSignature = $"RUNTIME | {step} | -",
            };
        }

        switch (target.FailedState)
        {
            case VerificationState.Discovery:
                return new VerifyResult { State = VerificationState.Discovery, ErrorSignature = SignatureBuilder.Discovery() };
            case VerificationState.Runtime:
                return new VerifyResult
                {
                    State = VerificationState.Runtime, TestMethods = target.Counts, Failures = [.. target.Failures],
                    FailingMethods = [.. target.FailingMethods], ErrorSignature = SignatureBuilder.Runtime(target.Failures),
                };
            case VerificationState.Assertion:
                return new VerifyResult
                {
                    State = VerificationState.Assertion, TestMethods = target.Counts, Failures = [.. target.Failures],
                    FailingMethods = [.. target.FailingMethods], ErrorSignature = SignatureBuilder.Assertion(target.Failures),
                };
        }

        // ── Bước 5: cả module, trên bản sao mới khác ─────────────────
        var (module, moduleCopy) = await RunStepAsync(run, "run_module", BuildCommands.RunModuleTests(plan)).ConfigureAwait(false);
        var moduleContext = BuildCommands.FromWorkspace(run.Task.Workspace, workspace.WorkDir, moduleCopy, TimeSpan.Zero);
        var moduleTests = reports.Read(moduleContext.BuildRootPath);
        if (module.Failure == BuildFailureKind.Build)
        {
            log.Warn($"[K3] {run.Task.Key.TaskId} {run.Suite.SuiteId}: chạy cả module mã thoát {module.Result.ExitCode}, đọc được {moduleTests.Count} test");
        }

        var regressions = TestResultClassifier.FindRegressions(run.Task.Baseline.PassingInAllRuns, moduleTests, run.Suite.TestFqn);
        if (regressions.Count > 0)
        {
            SaveReports(run, moduleContext.BuildRootPath, "run_module");
            return new VerifyResult
            {
                State = VerificationState.Regression, TestMethods = target.Counts, Regressions = [.. regressions],
                ErrorSignature = SignatureBuilder.Regression(regressions),
            };
        }

        return new VerifyResult { State = VerificationState.Valid, TestMethods = target.Counts };
    }

    private static bool NoTestsMarker(string output) =>
        output.Contains("No tests found for given includes", StringComparison.Ordinal)
        || output.Contains("No tests were executed", StringComparison.Ordinal)
        || output.Contains("No tests matching", StringComparison.Ordinal);

    /// <summary>
    /// Chạy một bước, ghi log, thử lại trên bản sao mới khi lỗi hạ tầng. <paramref name="reuseCopy"/>: lần thử đầu dùng bản sao có sẵn
    /// (bước 2 dùng bản sao đã biên dịch ở bước 1).
    /// </summary>
    private async Task<(BuildRun Run, string Copy)> RunStepAsync(Run run, string step, IReadOnlyList<string> arguments, string? reuseCopy = null)
    {
        var attempts = Math.Max(1, run.Options.InfraAttempts);
        var timeout = TimeSpan.FromSeconds(run.Options.StepTimeoutSeconds);
        var watch = Stopwatch.StartNew();
        string? lastReason = null;
        string lastCode = ErrorCodes.B3ToolError;
        for (var attempt = 1; attempt <= attempts; attempt++)
        {
            if (attempt > 1)
            {
                await Task.Delay(run.Options.RetryDelay, run.Token).ConfigureAwait(false);
            }

            string copy;
            try
            {
                copy = attempt == 1 && reuseCopy is not null ? reuseCopy : await PrepareCopyAsync(run).ConfigureAwait(false);
            }
            catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
            {
                (lastCode, lastReason) = (ErrorCodes.B3Workspace, $"{step}: không tạo được workspace ({ex.Message})");
                continue;
            }

            var context = BuildCommands.FromWorkspace(run.Task.Workspace, workspace.WorkDir, copy, timeout);
            var args = arguments.Concat(run.CopyArgs.GetValueOrDefault(copy) ?? []).ToList();
            BuildRun result;
            try
            {
                result = await build.RunAsync(context, args, run.Token).ConfigureAwait(false);
            }
            catch (Exception ex) when (ex is System.ComponentModel.Win32Exception or InvalidOperationException or IOException)
            {
                (lastCode, lastReason) = (ErrorCodes.B3ToolError, $"{step}: không chạy được công cụ build ({ex.Message})");
                continue;
            }

            var logPath = $"{run.Options.ResultDir}/logs/{step}{(attempt > 1 ? $"-try{attempt}" : "")}.log";
            results.SaveText(run.Task.RunId, run.Task.Key, logPath, BuildCommands.FormatLog(result, context.JdkHome));
            if (result.Failure is BuildFailureKind.Timeout or BuildFailureKind.Network)
            {
                (lastCode, lastReason) = result.Failure == BuildFailureKind.Timeout
                    ? (ErrorCodes.B3Timeout, $"{step} quá {run.Options.StepTimeoutSeconds} s")
                    : (ErrorCodes.B3Dependency, $"{step}: không tải được dependency ({result.Result.Tail(2)})");
                log.Warn($"[K3] {run.Task.Key.TaskId}: {lastReason} (lần {attempt})");
                continue;
            }

            run.Seconds += watch.Elapsed.TotalSeconds;
            run.Steps.Add(new StepRecord
            {
                Step = step, Ok = result.Failure == BuildFailureKind.None, Log = logPath, Seconds = Math.Round(result.Result.Duration.TotalSeconds, 1), Attempts = attempt,
            });
            return (result, copy);
        }

        run.Seconds += watch.Elapsed.TotalSeconds;
        run.Steps.Add(new StepRecord { Step = step, Ok = false, Attempts = attempts, TimedOut = lastCode == ErrorCodes.B3Timeout, Seconds = Math.Round(watch.Elapsed.TotalSeconds, 1) });
        throw new InfraStop(lastCode, $"{lastReason} sau {attempts} lần thử");
    }

    /// <summary>Bản sao mới của snapshot, ghi mọi file của bộ test và classpath phụ (spec 03 Mục 3.0, 9).</summary>
    private async Task<string> PrepareCopyAsync(Run run)
    {
        var snapshot = Path.Combine(workspace.WorkDir, run.Task.Workspace.CompiledSnapshot);
        var copy = await workspace.NewCopyAsync(snapshot, run.Task.RunId, run.Task.Key, "verify", run.Token).ConfigureAwait(false);
        run.Copies.Add(copy);
        run.CopyArgs[copy] = await SuiteWriter.WriteAsync(copy, run.Suite, run.Task, run.Token).ConfigureAwait(false);
        return copy;
    }

    /// <summary>Xem <see cref="SuiteWriter.AddMavenSystemDependencies"/>.</summary>
    public static void AddMavenSystemDependencies(string pomPath, IReadOnlyList<string> jars) => SuiteWriter.AddMavenSystemDependencies(pomPath, jars);

    /// <summary>Chép báo cáo XML của class test vào thư mục kết quả.</summary>
    private void SaveReports(Run run, string buildRoot, string step)
    {
        foreach (var file in Directory.EnumerateFiles(buildRoot, "TEST-*.xml", new EnumerationOptions { RecurseSubdirectories = true, IgnoreInaccessible = true })
                     .Where(TestReportReader.IsReport)
                     .Where(f => Path.GetFileName(f).StartsWith("TEST-" + run.Suite.TestFqn, StringComparison.Ordinal) || step == "run_module"))
        {
            results.SaveText(run.Task.RunId, run.Task.Key, $"{run.Options.ResultDir}/reports/{step}/{Path.GetFileName(file)}", File.ReadAllText(file));
        }
    }

    private async Task<(string? Hash, int Assertions)> MeasureAsync(string code, CancellationToken ct)
    {
        if (code.Length == 0)
        {
            return (null, 0);
        }

        try
        {
            var hash = await javaHelper.StripCommentsHashAsync(code, ct).ConfigureAwait(false);
            var count = await javaHelper.CountAssertionsAsync(code, ct).ConfigureAwait(false);
            return (hash.CodeHash, count.Total ?? 0);
        }
        catch (ArrowException ex)
        {
            log.Warn($"[K3] java-helper không đo được bộ test: {ex.Message}");
            return (null, 0);
        }
    }

    private async Task<List<string>> MethodsAtLinesAsync(string code, IReadOnlyList<int> lines, CancellationToken ct)
    {
        if (lines.Count == 0 || code.Length == 0)
        {
            return [];
        }

        try
        {
            var methods = await javaHelper.MethodAtLineAsync(code, lines, ct).ConfigureAwait(false);
            return lines.Select(l => methods.Methods.GetValueOrDefault(l)).OfType<string>().Distinct().ToList();
        }
        catch (ArrowException)
        {
            return [];
        }
    }

    /// <summary>File của class <c>test_fqn</c> trong bộ: tên file trùng tên ngắn của class.</summary>
    public static string MainTestFile(SuiteFiles suite)
    {
        var simple = suite.TestFqn[(suite.TestFqn.LastIndexOf('.') + 1)..] + ".java";
        return suite.Files.Keys.FirstOrDefault(p => JavacErrorParser.FileName(p) == simple) ?? suite.Files.Keys.FirstOrDefault() ?? simple;
    }

    /// <summary>File "sinh ra" để phân biệt COMPILE/UNKNOWN: file của test class và các file cùng tiền tố tên (scaffolding của EvoSuite).</summary>
    public static IReadOnlyCollection<string> GeneratedFileNames(SuiteFiles suite)
    {
        var simple = suite.TestFqn[(suite.TestFqn.LastIndexOf('.') + 1)..];
        return suite.Files.Keys.Select(JavacErrorParser.FileName).Where(n => n.StartsWith(simple, StringComparison.Ordinal)).ToHashSet(StringComparer.Ordinal);
    }
}
