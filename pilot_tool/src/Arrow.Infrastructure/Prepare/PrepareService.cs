using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Prepare;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Infrastructure.Build;
using Arrow.Infrastructure.Git;

namespace Arrow.Infrastructure.Prepare;

public sealed record PrepareResult(TaskInfo Task, Baseline Baseline, RepoContext Context, double BuildSeconds);

/// <summary>
/// S0 cho một task (specs/02): lấy commit, tạo workspace, dò module, chọn JDK, build baseline,
/// chạy test của module <c>baseline_runs</c> lần, trích 6 thông tin repository, ghi
/// <c>task.json</c>, <c>baseline.json</c>, <c>repo_context.json</c>. Lỗi hạ tầng ném <see cref="PrepareException"/>.
/// </summary>
public sealed class PrepareService(GitRepositoryProvider git, MachineConfig machine, ExperimentConfig experiment, string workDir)
{
    public const string TaskFile = "task.json";
    public const string BaselineFile = "baseline.json";
    public const string RepoContextFile = "repo_context.json";

    private static readonly string[] SkippedDirectories = ["target", "build", "out", "bin", ".git", ".gradle", ".idea", ".mvn", ".settings", "node_modules"];

    public async Task<PrepareResult> RunAsync(ManifestRecord record, RunStore store, JobKey job, Action<string> reportStep, CancellationToken cancellationToken)
    {
        var taskDir = store.TaskDir(record.Dataset, record.TaskId);
        var logsDir = Path.Combine(taskDir, "logs");
        var steps = new List<StepRecord>();
        var build = experiment.Build;
        var timeout = TimeSpan.FromSeconds(build.StepTimeoutSeconds);

        reportStep("git");
        await git.EnsureCommitAsync(record.RepoUrl, record.CheckoutSha, cancellationToken).ConfigureAwait(false);
        var workspace = Path.Combine(workDir, "ws", store.RunId, job.ShortHash);
        await git.AddWorktreeAsync(record.RepoUrl, record.CheckoutSha, workspace, cancellationToken).ConfigureAwait(false);
        var failed = true;
        try
        {
            var plan = BuildPlanDetector.Detect(record.ClassPath, rel => File.Exists(Path.Combine(workspace, rel)), OperatingSystem.IsWindows())
                       ?? throw new PrepareException("BUILD_FILE_MISSING", $"Không tìm thấy pom.xml/build.gradle phía trên {record.ClassPath}");
            if (!string.Equals(plan.Tool.ToString(), record.BuildTool, StringComparison.OrdinalIgnoreCase))
            {
                ToolLog.Warn($"{record.TaskId}: manifest ghi {record.BuildTool} nhưng dò được {plan.Tool}; dùng {plan.Tool}");
            }

            if (plan.ModulePath != BuildPlanDetector.Normalize(record.ModulePath))
            {
                ToolLog.Warn($"{record.TaskId}: module_path manifest '{record.ModulePath}' khác module dò được '{plan.ModulePath}'");
            }

            var buildFiles = BuildFileTexts(workspace, plan.ModuleDir);
            JdkChoice jdk = null!;
            BuildRunner runner = null!;
            var attempts = new List<JdkAttempt>();
            var flags = new BuildFlags
            {
                Maven = build.MavenExtraFlags,
                Gradle = build.GradleExtraFlags,
                GradleExcludedTasks = build.GradleExcludedTasks,
                Windows = OperatingSystem.IsWindows(),
            };
            var repoHistory = "shallow";
            HashSet<string>? gradleTasks = null;
            var logSuffix = "";
            var toolVersion = "unknown";

            string LogPath(string name) => Path.Combine(logsDir, $"prepare__{name}{logSuffix}.log");

            async Task<StepResult> Run(string step, string logName, TimeSpan stepTimeout)
            {
                reportStep(step);
                var result = await runner.RunAsync(BuildCommands.For(plan, step, flags, gradleTasks: gradleTasks), step, LogPath(logName), stepTimeout, cancellationToken).ConfigureAwait(false);
                steps.Add(result.ToRecord(taskDir));
                if (result.Succeeded || result.TimedOut)
                {
                    return result;
                }

                if (plan.Mode == BuildMode.Reactor && BuildCommands.IsModuleNotInBuild(plan.Tool, result.Output))
                {
                    plan = plan.AsStandalone();
                    ToolLog.Info($"{record.TaskId}: module {plan.ModulePath} không thuộc reactor/settings, chạy riêng tại {plan.ModuleDir}");
                    result = await runner.RunAsync(BuildCommands.For(plan, step, flags, gradleTasks: gradleTasks), step, LogPath(logName + "_standalone"), stepTimeout, cancellationToken).ConfigureAwait(false);
                    steps.Add(result.ToRecord(taskDir));
                }

                if (!result.Succeeded && !result.TimedOut && repoHistory == "shallow" && BuildCommands.NeedsFullHistory(result.Output))
                {
                    await git.UnshallowAsync(record.RepoUrl, cancellationToken).ConfigureAwait(false);
                    repoHistory = "full";
                    result = await runner.RunAsync(BuildCommands.For(plan, step, flags, gradleTasks: gradleTasks), step, LogPath(logName + "_full_history"), stepTimeout, cancellationToken).ConfigureAwait(false);
                    steps.Add(result.ToRecord(taskDir));
                }

                return result;
            }

            // Baseline: biên dịch rồi chạy test của module, thử lần lượt các JDK ứng viên (specs/02 §3):
            // JDK khai báo → JDK của manifest → các JDK còn lại. Chọn JDK đầu tiên mà test sẵn có biên dịch và chạy được.
            var candidates = JdkSelector.Candidates(record.JavaVersion, plan.Tool, buildFiles, machine.JdkHomes);
            var moduleAbsolute = Path.Combine(workspace, plan.ModuleDir == "." ? "" : plan.ModuleDir);
            Baseline? baseline = null;
            for (var index = 0; ; index++)
            {
                jdk = candidates[index];
                runner = new BuildRunner(workspace, plan, jdk, machine.MavenPath, machine.GradlePath);
                logSuffix = index == 0 ? "" : $"_jdk{jdk.Major}";
                var failure = await TryBaselineAsync().ConfigureAwait(false);
                attempts.Add(new JdkAttempt(jdk.Major, jdk.Reason, failure?.Reason ?? "OK"));
                if (failure is null)
                {
                    break;
                }

                if (index + 1 >= candidates.Count)
                {
                    throw new PrepareException(failure.Reason, $"{failure.Message} (đã thử JDK {string.Join(", ", attempts.Select(a => a.Major))})");
                }

                ToolLog.Info($"{record.TaskId}: {failure.Reason} với JDK {jdk.Major} ({jdk.Reason}); thử JDK {candidates[index + 1].Major}");
            }

            async Task<PrepareException?> TryBaselineAsync()
            {
                // Phiên bản build tool (không lấy được thì ghi unknown, không phải lỗi).
                var versionResult = await Run(BuildStep.ToolVersion, BuildStep.ToolVersion, TimeSpan.FromSeconds(build.ToolVersionTimeoutSeconds)).ConfigureAwait(false);
                toolVersion = ParseToolVersion(plan.Tool, versionResult.Output);
                flags = flags with { MavenVersion = plan.Tool == BuildToolKind.Maven && toolVersion != "unknown" ? toolVersion : null };
                if (plan.Tool == BuildToolKind.Gradle)
                {
                    var tasksResult = await Run(BuildStep.GradleTasks, BuildStep.GradleTasks, timeout).ConfigureAwait(false);
                    gradleTasks = tasksResult.Succeeded ? BuildCommands.ParseGradleTasks(tasksResult.Output) : [];
                }

                // 1. Biên dịch trên workspace sạch.
                foreach (var step in new[] { BuildStep.MainCompile, BuildStep.TestCompile })
                {
                    var result = await Run(step, step, timeout).ConfigureAwait(false);
                    if (result.TimedOut)
                    {
                        throw new PrepareException("BASELINE_TIMEOUT", $"{step} quá {build.StepTimeoutSeconds}s");
                    }

                    if (!result.Succeeded)
                    {
                        return new PrepareException("BASELINE_BUILD_FAILED", $"{step} lỗi với JDK {jdk.Major} (mã thoát {result.ExitCode}); xem logs/{Path.GetFileName(result.LogPath)}");
                    }
                }

                // 2. Chạy test của module nhiều lần, đọc báo cáo XML.
                var runs = new List<IReadOnlyList<TestCaseResult>>();
                var durations = new List<double>();
                var reportSource = "none";
                for (var n = 1; n <= Math.Max(1, build.BaselineRuns); n++)
                {
                    BuildRunner.ClearReports(moduleAbsolute, plan.Tool);
                    var name = $"{BuildStep.ModuleTest}_{n}";
                    var result = await Run(BuildStep.ModuleTest, name, timeout).ConfigureAwait(false);
                    if (result.TimedOut)
                    {
                        throw new PrepareException("BASELINE_TIMEOUT", $"module_test lần {n} quá {build.StepTimeoutSeconds}s");
                    }

                    var (results, source) = BuildRunner.ReadReports(moduleAbsolute, plan.Tool, Path.Combine(logsDir, "reports", $"prepare__{name}{logSuffix}"));
                    if (!result.Succeeded && !results.Any(r => r.Outcome is TestOutcome.Fail or TestOutcome.Error))
                    {
                        // Thoát mã khác 0 mà không có test nào fail: plugin test hoặc JVM không chạy được với JDK này.
                        return new PrepareException("BASELINE_TEST_RUN_FAILED", $"module_test lần {n} lỗi với JDK {jdk.Major} (mã thoát {result.ExitCode}) mà không có test fail; xem logs/{Path.GetFileName(result.LogPath)}");
                    }

                    runs.Add(results);
                    durations.Add(result.Seconds);
                    reportSource = source == "none" ? reportSource : source;
                }

                baseline = Baseline.Compute(runs, durations, reportSource);
                return null;
            }


            // 6 thông tin repository.
            var context = await ExtractContextAsync(record, plan, jdk, toolVersion, workspace, moduleAbsolute, Run, cancellationToken).ConfigureAwait(false);

            // Không ghi đè file sẵn có: tên trùng (hiếm) thì thêm hậu tố số _ArrowTest2… (specs/03 Mục 1).
            var testClassFqn = record.ClassFqn + experiment.TestClassSuffix;
            var suffixFile = experiment.TestClassSuffix + ".java";
            var testPath = context.TestPath;
            for (var n = 2; File.Exists(Path.Combine(workspace, testPath)); n++)
            {
                testPath = context.TestPath[..^suffixFile.Length] + experiment.TestClassSuffix + n + ".java";
                testClassFqn = record.ClassFqn + experiment.TestClassSuffix + n;
                ToolLog.Warn($"{record.TaskId}: {context.TestPath} đã có trong repository, dùng {Path.GetFileName(testPath)}");
            }

            context = context with { TestPath = testPath };

            var info = new TaskInfo
            {
                Dataset = record.Dataset,
                TaskId = record.TaskId,
                RepoUrl = record.RepoUrl,
                CheckoutSha = record.CheckoutSha,
                ClassFqn = record.ClassFqn,
                ClassPath = record.ClassPath,
                TestClassPath = record.TestClassPath,
                ManifestModulePath = record.ModulePath,
                BuildTool = record.BuildTool,
                TestingFramework = record.TestingFramework,
                ManifestJavaVersion = record.JavaVersion,
                Plan = plan,
                JdkMajor = jdk.Major,
                JdkHome = jdk.Home,
                JdkReason = jdk.Reason,
                JdkAttempts = attempts,
                BuildToolVersion = toolVersion,
                GradleTasks = gradleTasks?.Order(StringComparer.Ordinal).ToList(),
                RepoHistory = repoHistory,
                GeneratedTestClassFqn = testClassFqn,
                GeneratedTestPath = context.TestPath,
                Steps = steps,
                PreparedAt = DateTimeOffset.UtcNow,
            };

            // Ghi output trước; ledger do runner ghi sau.
            Json.WriteAtomic(Path.Combine(taskDir, BaselineFile), baseline);
            Json.WriteAtomic(Path.Combine(taskDir, RepoContextFile), context);
            Json.WriteAtomic(Path.Combine(taskDir, TaskFile), info);
            failed = false;
            return new PrepareResult(info, baseline!, context, steps.Sum(s => s.Seconds));
        }
        finally
        {
            Json.WriteAtomic(Path.Combine(taskDir, "prepare_steps.json"), steps);
            if (!(failed && machine.KeepFailedWorkspaces))
            {
                await git.RemoveWorktreeAsync(record.RepoUrl, workspace, CancellationToken.None).ConfigureAwait(false);
            }
        }
    }

    private async Task<RepoContext> ExtractContextAsync(ManifestRecord record, BuildPlan plan, JdkChoice jdk, string toolVersion, string workspace,
        string moduleAbsolute, Func<string, string, TimeSpan, Task<StepResult>> run, CancellationToken cancellationToken)
    {
        var buildFile = new[] { "pom.xml", "build.gradle", "build.gradle.kts" }
            .Select(n => Path.Combine(moduleAbsolute, n)).FirstOrDefault(File.Exists);
        var buildText = buildFile is null ? "" : await File.ReadAllTextAsync(buildFile, cancellationToken).ConfigureAwait(false);

        List<Dependency> dependencies;
        string source;
        string? error = null;
        var result = await run(BuildStep.Dependencies, BuildStep.Dependencies, TimeSpan.FromSeconds(experiment.Build.StepTimeoutSeconds)).ConfigureAwait(false);
        if (plan.Tool == BuildToolKind.Maven)
        {
            var listFile = Path.Combine(moduleAbsolute, BuildCommands.MavenDependencyOutput);
            dependencies = result.Succeeded && File.Exists(listFile)
                ? DependencyParser.ParseMavenList(await File.ReadAllTextAsync(listFile, cancellationToken).ConfigureAwait(false))
                : [];
            source = "maven-dependency-plugin";
        }
        else
        {
            dependencies = result.Succeeded ? DependencyParser.ParseGradleTree(result.Output) : [];
            source = "gradle-dependencies";
        }

        if (dependencies.Count == 0)
        {
            error = result.TimedOut ? "timeout" : result.Succeeded ? "empty" : $"exit {result.ExitCode}";
            dependencies = DependencyParser.ParseDeclared(plan.Tool, buildText);
            source = "declared";
            ToolLog.Warn($"{record.TaskId}: không lấy được dependency đã resolve ({error}); dùng {dependencies.Count} dependency khai báo trong file build");
        }

        var invocationAbsolute = Path.Combine(workspace, plan.InvocationRoot == "." ? "" : plan.InvocationRoot);
        return new RepoContext
        {
            BuildTool = new BuildToolInfo { Name = plan.Tool.ToString().ToLowerInvariant(), Wrapper = plan.Wrapper is not null, Version = toolVersion },
            JavaVersion = DependencyParser.JavaLevels(jdk.Major, plan.Tool, BuildFileTexts(workspace, plan.ModuleDir)),
            Dependencies = dependencies,
            DependenciesSource = source,
            DependenciesError = error,
            TestFramework = DependencyParser.Framework(record.TestingFramework, dependencies),
            ProjectStructure = new ProjectStructure
            {
                Directories = ListDirectories(moduleAbsolute, 3),
                MainJavaFiles = CountJava(Path.Combine(moduleAbsolute, "src", "main", "java")),
                TestJavaFiles = CountJava(Path.Combine(moduleAbsolute, "src", "test", "java")),
                Modules = ListModules(invocationAbsolute),
            },
            SourcePath = BuildPlanDetector.Normalize(record.ClassPath),
            TestPath = DependencyParser.TestPathFor(record.ClassPath, plan.ModuleDir, experiment.TestClassSuffix),
        };
    }

    /// <summary>File build của module và tối đa 3 thư mục cha (để xét cờ <c>--release</c>).</summary>
    private static List<string> BuildFileTexts(string workspace, string moduleDir)
    {
        var texts = new List<string>();
        var directory = new DirectoryInfo(Path.Combine(workspace, moduleDir == "." ? "" : moduleDir));
        var root = Path.GetFullPath(workspace).TrimEnd(Path.DirectorySeparatorChar);
        for (var level = 0; level <= 3 && directory is not null; level++, directory = directory.Parent)
        {
            if (!directory.FullName.StartsWith(root, StringComparison.Ordinal))
            {
                break;
            }

            foreach (var name in new[] { "pom.xml", "build.gradle", "build.gradle.kts" })
            {
                var path = Path.Combine(directory.FullName, name);
                if (File.Exists(path))
                {
                    texts.Add(File.ReadAllText(path));
                }
            }
        }

        return texts;
    }

    public static string ParseToolVersion(BuildToolKind tool, string output)
    {
        var match = tool == BuildToolKind.Maven
            ? System.Text.RegularExpressions.Regex.Match(output, @"Apache Maven (\d+(?:\.\d+)+)")
            : System.Text.RegularExpressions.Regex.Match(output, @"Gradle (\d+(?:\.\d+)+)");
        return match.Success ? match.Groups[1].Value : "unknown";
    }

    private static List<string> ListDirectories(string root, int depth)
    {
        var result = new List<string>();
        void Walk(string directory, int level)
        {
            if (level > depth || result.Count >= 300)
            {
                return;
            }

            IEnumerable<string> children;
            try
            {
                children = Directory.EnumerateDirectories(directory).OrderBy(d => d, StringComparer.Ordinal).ToList();
            }
            catch (IOException)
            {
                return;
            }

            foreach (var child in children)
            {
                var name = Path.GetFileName(child);
                if (SkippedDirectories.Contains(name) || name.StartsWith('.'))
                {
                    continue;
                }

                result.Add(Path.GetRelativePath(root, child).Replace('\\', '/'));
                Walk(child, level + 1);
            }
        }

        Walk(root, 1);
        return result;
    }

    private static int CountJava(string directory) =>
        Directory.Exists(directory) ? Directory.EnumerateFiles(directory, "*.java", SearchOption.AllDirectories).Count() : 0;

    private static List<string> ListModules(string invocationRoot)
    {
        var modules = new List<string>();
        void Walk(string directory, int level)
        {
            if (level > 4)
            {
                return;
            }

            foreach (var child in Directory.EnumerateDirectories(directory).OrderBy(d => d, StringComparer.Ordinal))
            {
                var name = Path.GetFileName(child);
                if (SkippedDirectories.Contains(name) || name.StartsWith('.') || name == "src")
                {
                    continue;
                }

                if (new[] { "pom.xml", "build.gradle", "build.gradle.kts" }.Any(f => File.Exists(Path.Combine(child, f))))
                {
                    modules.Add(Path.GetRelativePath(invocationRoot, child).Replace('\\', '/'));
                }

                Walk(child, level + 1);
            }
        }

        Walk(invocationRoot, 1);
        return modules;
    }
}
