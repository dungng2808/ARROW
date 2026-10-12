using Arrow.Core.Config;
using Arrow.Core.Prepare;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Build;
using Arrow.Infrastructure.Java;

namespace Arrow.Infrastructure.Verify;

/// <summary>Một ứng viên cần xác minh trên workspace sạch của task.</summary>
public sealed record VerifyRequest
{
    public required TaskInfo Task { get; init; }
    public required Baseline Baseline { get; init; }

    /// <summary>Gốc repository đã checkout đúng commit (workspace riêng của job).</summary>
    public required string Workspace { get; init; }

    /// <summary>Đường dẫn file test, tương đối so với gốc repository.</summary>
    public required string TestPath { get; init; }
    public required string TestClassFqn { get; init; }
    public required string Code { get; init; }

    /// <summary>File phụ ghi kèm (scaffolding của EvoSuite), đường dẫn tương đối → nội dung.</summary>
    public IReadOnlyDictionary<string, string> ExtraFiles { get; init; } = new Dictionary<string, string>();

    /// <summary>Thư mục <c>logs/</c> của task và tiền tố tên log (<c>gen-gpt-6-luna-few_shot</c>).</summary>
    public required string LogsDir { get; init; }
    public required string LogKey { get; init; }

    /// <summary>Gốc để ghi đường dẫn log tương đối trong verify.json.</summary>
    public required string TaskDir { get; init; }

    /// <summary>Tham số build thêm (EvoSuite runtime qua init script Gradle).</summary>
    public IReadOnlyList<string> ExtraBuildArgs { get; init; } = [];
}

/// <summary>
/// Bộ xác minh 5 bước dùng chung cho mọi ứng viên (specs/03): compile → discovery → execution → target pass →
/// module regression. Timeout, lỗi công cụ, lỗi tải dependency: build lại 1 lần, vẫn lỗi thì INFRA.
/// </summary>
public sealed class FiveStepVerifier(ExperimentConfig experiment, MachineConfig machine, JavaAnalyzer analyzer)
{
    public async Task<VerifyResult> VerifyAsync(VerifyRequest request, Action<string> reportStep, CancellationToken cancellationToken)
    {
        var task = request.Task;
        var plan = task.Plan;
        var simpleName = request.TestClassFqn[(request.TestClassFqn.LastIndexOf('.') + 1)..];
        var moduleDir = Path.Combine(request.Workspace, plan.ModuleDir == "." ? "" : plan.ModuleDir);
        var jdk = new JdkChoice(task.JdkMajor, task.JdkHome, task.JdkReason);
        var runner = new BuildRunner(request.Workspace, plan, jdk, machine.MavenPath, machine.GradlePath);
        var flags = new BuildFlags
        {
            Maven = experiment.Build.MavenExtraFlags,
            Gradle = experiment.Build.GradleExtraFlags,
            GradleExcludedTasks = experiment.Build.GradleExcludedTasks,
            Windows = OperatingSystem.IsWindows(),
            MavenVersion = plan.Tool == BuildToolKind.Maven && task.BuildToolVersion != "unknown" ? task.BuildToolVersion : null,
        };
        var gradleTasks = task.GradleTasks?.ToHashSet(StringComparer.Ordinal);
        var timeout = TimeSpan.FromSeconds(experiment.Build.StepTimeoutSeconds);
        var logs = new List<string>();
        var seconds = new Dictionary<string, double?> { ["test_compile"] = null, ["target_test"] = null, ["module_test"] = null };
        var rebuilds = 0;

        // Ghi test (specs/03 Mục 1): đúng một file, UTF-8, LF; không ghi đè file sẵn có của repository.
        var testFile = Path.Combine(request.Workspace, request.TestPath);
        if (File.Exists(testFile))
        {
            throw new ArrowException("TEST_FILE_EXISTS", $"{request.TestPath} đã có trong repository");
        }

        Directory.CreateDirectory(Path.GetDirectoryName(testFile)!);
        await File.WriteAllTextAsync(testFile, request.Code.Replace("\r\n", "\n"), cancellationToken).ConfigureAwait(false);
        foreach (var (path, content) in request.ExtraFiles)
        {
            var full = Path.Combine(request.Workspace, path);
            Directory.CreateDirectory(Path.GetDirectoryName(full)!);
            await File.WriteAllTextAsync(full, content.Replace("\r\n", "\n"), cancellationToken).ConfigureAwait(false);
        }

        string LogPath(string step, int attempt) => Path.Combine(request.LogsDir, $"{request.LogKey}__{step}{(attempt > 1 ? "_" + attempt : "")}.log");

        // Chạy một bước; lỗi hạ tầng thì build lại 1 lần (AR 2.1).
        async Task<(StepResult Result, string? Infra)> Run(string step, IReadOnlyList<string> extra, string logStep)
        {
            reportStep(logStep);
            StepResult result = null!;
            for (var attempt = 1; attempt <= 2; attempt++)
            {
                var command = BuildCommands.For(plan, step, flags, request.TestClassFqn, gradleTasks);
                command = command with { Arguments = [.. command.Arguments.Take(command.Arguments.Count - (plan.Tool == BuildToolKind.Maven ? 1 : 0)), .. request.ExtraBuildArgs, .. extra, .. command.Arguments.Skip(command.Arguments.Count - (plan.Tool == BuildToolKind.Maven ? 1 : 0))] };
                if (step is BuildStep.TargetTest or BuildStep.ModuleTest)
                {
                    BuildRunner.ClearReports(moduleDir, plan.Tool);
                }

                result = await runner.RunAsync(command, step, LogPath(logStep, attempt), timeout, cancellationToken).ConfigureAwait(false);
                logs.Add(Path.GetRelativePath(request.TaskDir, result.LogPath).Replace('\\', '/'));
                seconds[step] = Math.Round((seconds[step] ?? 0) + result.Seconds, 2);
                var infra = InfraReason(result);
                if (infra is null)
                {
                    return (result, null);
                }

                if (attempt == 1)
                {
                    rebuilds++;
                    ToolLog.Warn($"{task.TaskId}: {step} {infra}, build lại 1 lần");
                }
                else
                {
                    return (result, infra);
                }
            }

            return (result, null);
        }

        VerifyResult Infra(string reason, VerifySteps steps) => new()
        {
            State = VerificationState.Infra,
            InfraReason = reason,
            Steps = steps,
            BuildSeconds = seconds,
            Logs = logs,
            Rebuilds = rebuilds,
        };

        // ── Bước 1: compile ─────────────────────────────────────────────
        var (compile, compileInfra) = await Run(BuildStep.TestCompile, [], "test_compile").ConfigureAwait(false);
        if (compileInfra is not null)
        {
            return Infra(compileInfra, new VerifySteps());
        }

        if (!compile.Succeeded)
        {
            var allErrors = FailureClassifier.ParseCompileErrors(compile.Output);
            var inFile = FailureClassifier.ErrorsInFile(allErrors, simpleName);
            var analysis = await analyzer.AnalyzeTestAsync(request.Code, inFile.Select(e => e.Line), cancellationToken).ConfigureAwait(false);
            if (inFile.Count > 0)
            {
                var located = inFile.Select(e => e with { Method = analysis.MethodsAtLines.GetValueOrDefault(e.Line.ToString(System.Globalization.CultureInfo.InvariantCulture)) }).ToList();
                return Finish(VerificationState.Compile, new VerifySteps { Compile = StepStatus.Fail }, analysis,
                    FailureClassifier.CompileSignature(located), compileErrors: located);
            }

            return Finish(VerificationState.Unknown, new VerifySteps { Compile = StepStatus.Fail }, analysis,
                FailureClassifier.UnknownSignature(compile.Output), errorLog: FailureClassifier.ErrorExcerpt(compile.Output));
        }

        var analysisOk = await analyzer.AnalyzeTestAsync(request.Code, [], cancellationToken).ConfigureAwait(false);

        // ── Bước 2–4: chạy riêng test sinh ra ───────────────────────────
        var (target, targetInfra) = await Run(BuildStep.TargetTest, [], "target_test").ConfigureAwait(false);
        if (targetInfra is not null)
        {
            return Infra(targetInfra, new VerifySteps { Compile = StepStatus.Pass });
        }

        var (targetResults, _) = BuildRunner.ReadReports(moduleDir, plan.Tool, Path.Combine(request.LogsDir, "reports", $"{request.LogKey}__target_test"));
        var mine = FailureClassifier.OfClass(targetResults, request.TestClassFqn).Where(r => !FailureClassifier.IsNoTestsMarker(r)).ToList();
        var counts = new TestMethodCounts
        {
            Total = mine.Count,
            Passed = mine.Count(r => r.Outcome == TestOutcome.Pass),
            Failed = mine.Count(r => r.Outcome is TestOutcome.Fail or TestOutcome.Error),
            Skipped = mine.Count(r => r.Outcome == TestOutcome.Skip),
        };
        if (mine.Count == 0 || counts.Skipped == counts.Total)
        {
            var steps = new VerifySteps { Compile = StepStatus.Pass, Discovery = mine.Count == 0 ? StepStatus.Fail : StepStatus.Pass, Execution = mine.Count == 0 ? StepStatus.NotRun : StepStatus.Fail };
            return Finish(VerificationState.Discovery, steps, analysisOk, FailureClassifier.DiscoverySignature(), counts: counts,
                errorLog: target.Succeeded ? null : FailureClassifier.ErrorExcerpt(target.Output));
        }

        var failed = mine.Where(r => r.Outcome is TestOutcome.Fail or TestOutcome.Error).ToList();
        if (failed.Count > 0)
        {
            var state = FailureClassifier.TargetFailureState(failed);
            var failures = failed.Select(FailureClassifier.ToFailure).ToList();
            var signature = state == VerificationState.Assertion
                ? FailureClassifier.AssertionSignature(failures.First(f => f.Kind == "assertion"))
                : FailureClassifier.RuntimeSignature(failures.First(f => f.Kind == "exception"), ProjectClasses(request.Workspace));
            return Finish(state, new VerifySteps { Compile = StepStatus.Pass, Discovery = StepStatus.Pass, Execution = StepStatus.Pass, TargetPass = StepStatus.Fail },
                analysisOk, signature, counts: counts, failures: failures);
        }

        // ── Bước 5: chạy toàn module, so với baseline ──────────────────
        var passedTarget = new VerifySteps { Compile = StepStatus.Pass, Discovery = StepStatus.Pass, Execution = StepStatus.Pass, TargetPass = StepStatus.Pass };
        var (module, moduleInfra) = await Run(BuildStep.ModuleTest, [], "module_test").ConfigureAwait(false);
        if (moduleInfra is not null)
        {
            return Infra(moduleInfra, passedTarget);
        }

        var (moduleResults, _) = BuildRunner.ReadReports(moduleDir, plan.Tool, Path.Combine(request.LogsDir, "reports", $"{request.LogKey}__module_test"));
        bool? includesTarget = true;
        if (FailureClassifier.OfClass(moduleResults, request.TestClassFqn).Count == 0)
        {
            // Module tự cấu hình includes/excludes: thêm test sinh ra mà không sửa file build (specs/03 Mục 4).
            includesTarget = false;
            var extra = IncludeTargetArgs(request, plan.Tool);
            var (rerun, rerunInfra) = await Run(BuildStep.ModuleTest, extra, "module_test_with_target").ConfigureAwait(false);
            if (rerunInfra is not null)
            {
                return Infra(rerunInfra, passedTarget);
            }

            module = rerun;
            (moduleResults, _) = BuildRunner.ReadReports(moduleDir, plan.Tool, Path.Combine(request.LogsDir, "reports", $"{request.LogKey}__module_test_with_target"));
            if (FailureClassifier.OfClass(moduleResults, request.TestClassFqn).Count == 0)
            {
                ToolLog.Warn($"{task.TaskId}: lần chạy module vẫn không chạy {simpleName}");
            }
        }

        var newFailures = FailureClassifier.NewFailures(request.Baseline, moduleResults, request.TestClassFqn);
        if (moduleResults.Count == 0 && !module.Succeeded)
        {
            // Không có báo cáo nào: lần chạy module hỏng (ví dụ test sinh ra làm JVM fork thoát).
            newFailures = request.Baseline.PassingInAllRuns.Select(id => new NewFailure { Id = id, Type = "MISSING", Message = "module run produced no report" }).ToList();
        }

        if (newFailures.Count > 0)
        {
            return Finish(VerificationState.Regression, passedTarget with { Regression = StepStatus.Fail }, analysisOk,
                FailureClassifier.RegressionSignature(newFailures), counts: counts, newFailures: newFailures, includesTarget: includesTarget);
        }

        return Finish(VerificationState.Valid, passedTarget with { Regression = StepStatus.Pass }, analysisOk, null, counts: counts, includesTarget: includesTarget);

        VerifyResult Finish(VerificationState state, VerifySteps steps, TestAnalysis analysis, string? signature,
            TestMethodCounts? counts = null, List<TestFailure>? failures = null, List<CompileError>? compileErrors = null,
            List<NewFailure>? newFailures = null, bool? includesTarget = null, string? errorLog = null) => new()
        {
            State = state,
            Steps = steps,
            TestMethods = counts ?? new TestMethodCounts(),
            Failures = failures ?? [],
            CompileErrors = compileErrors ?? [],
            NewFailures = newFailures ?? [],
            ModuleRunIncludesTarget = includesTarget,
            Group = States.Group(state),
            ErrorSignature = signature,
            AssertionCount = analysis.AssertionCount,
            AssertionsPerMethod = analysis.AssertionsPerMethod,
            TestMethodNames = analysis.TestMethods,
            CodeHash = CodeHasher.Hash(analysis.Stripped),
            BuildSeconds = seconds,
            Logs = logs,
            ErrorLog = errorLog,
            Rebuilds = rebuilds,
        };
    }

    /// <summary>Lỗi hạ tầng của một lệnh: timeout, công cụ lỗi, lỗi tải dependency (specs/03 Mục 2).</summary>
    public static string? InfraReason(StepResult result)
    {
        if (result.TimedOut)
        {
            return "BUILD_TIMEOUT";
        }

        if (FailureClassifier.IsToolError(result.ExitCode, result.TimedOut, result.Output))
        {
            return "TOOL_ERROR";
        }

        return !result.Succeeded && FailureClassifier.IsDependencyDownloadError(result.Output) ? "DEPENDENCY_DOWNLOAD" : null;
    }

    /// <summary>Maven: <c>-Dsurefire.includesFile</c> gồm mẫu mặc định và test sinh ra; Gradle: init script thêm include.</summary>
    private static List<string> IncludeTargetArgs(VerifyRequest request, BuildToolKind tool)
    {
        var simple = request.TestClassFqn[(request.TestClassFqn.LastIndexOf('.') + 1)..];
        var packagePath = request.TestClassFqn.Contains('.') ? request.TestClassFqn[..request.TestClassFqn.LastIndexOf('.')].Replace('.', '/') + "/" : "";
        if (tool == BuildToolKind.Maven)
        {
            var file = Path.Combine(request.Workspace, ".arrow-surefire-includes.txt");
            File.WriteAllText(file, string.Join('\n', "**/Test*.java", "**/*Test.java", "**/*Tests.java", "**/*TestCase.java", $"{packagePath}{simple}.java") + "\n");
            return [$"-Dsurefire.includesFile={file}"];
        }

        var script = Path.Combine(request.Workspace, ".arrow-include-target.gradle");
        File.WriteAllText(script, $$"""
            gradle.projectsEvaluated {
                gradle.rootProject.allprojects { p ->
                    p.tasks.withType(Test).configureEach { t ->
                        if (!t.includes.isEmpty()) { t.include('{{packagePath}}{{simple}}*') }
                    }
                }
            }
            """);
        return ["--init-script", script];
    }

    /// <summary>Tên class (FQCN) trong mọi <c>src/main/java</c> của repository: "code dự án" của RUNTIME signature.</summary>
    public static HashSet<string> ProjectClasses(string workspace)
    {
        var classes = new HashSet<string>(StringComparer.Ordinal);
        foreach (var file in Directory.EnumerateFiles(workspace, "*.java", SearchOption.AllDirectories))
        {
            var relative = Path.GetRelativePath(workspace, file).Replace('\\', '/');
            var index = relative.IndexOf("src/main/java/", StringComparison.Ordinal);
            if (index >= 0)
            {
                classes.Add(relative[(index + "src/main/java/".Length)..^".java".Length].Replace('/', '.'));
            }
        }

        return classes;
    }
}
