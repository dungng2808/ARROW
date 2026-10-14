using System.Diagnostics;
using System.Text;
using System.Text.RegularExpressions;
using Arrow.DataAccess;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Common;
using Arrow.Models.Manifest;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block1;

/// <summary>
/// Khối 1 (spec 01): tải đúng commit → build root, module root → JDK, 6 thông tin cấu hình → biên dịch + snapshot → baseline ×N → tiêu chí B.
/// Lưu kết quả theo thứ tự spec 01 Mục 6.4; dòng ledger do nơi gọi ghi sau cùng.
/// </summary>
public sealed partial class RepositoryAnalyzer(
    IGitClient git,
    IWorkspaceManager workspace,
    IBuildRunner build,
    ITestReportReader reports,
    ITaskResultRepository results,
    ILogRepository log) : IRepositoryAnalyzer
{
    /// <summary>Trạng thái của một lần phân tích: task, run, đường dẫn workspace, số thứ tự log.</summary>
    private sealed class Job(TaskRecord task, RunContext run)
    {
        public TaskRecord Task { get; } = task;
        public RunContext Run { get; } = run;
        public TaskKey Key { get; } = new(task.Dataset, task.TaskId);
        public string? Mirror { get; set; }
        public int LogIndex { get; set; }
        public CancellationToken Token { get; init; }
    }

    /// <summary>Kết thúc sớm với EXCLUDED/INFRA.</summary>
    private sealed class Stop(BlockOutcome outcome) : Exception(outcome.Message)
    {
        public BlockOutcome Outcome { get; } = outcome;
    }

    private static Stop Excluded(string code, string message) => new(new BlockOutcome { Outcome = OutcomeKind.Excluded, Code = code, Message = message });

    private static Stop Infra(string code, string message) => new(new BlockOutcome { Outcome = OutcomeKind.Infra, Code = code, Message = message, NeedsRerun = true });

    public async Task<RepositoryAnalysis> AnalyzeAsync(TaskRecord task, RunContext run, CancellationToken cancellationToken)
    {
        var job = new Job(task, run) { Token = cancellationToken };
        results.Delete(run.RunId, job.Key, ResultPaths.Block1Dir);
        results.SaveText(run.RunId, job.Key, ResultPaths.Task, task.RawJson + "\n");
        try
        {
            var analysis = await AnalyzeCoreAsync(job).ConfigureAwait(false);
            if (!run.KeepWorkspace)
            {
                await workspace.DeleteTaskAsync(run.RunId, job.Key, job.Mirror, CancellationToken.None).ConfigureAwait(false);
            }

            return analysis;
        }
        catch (Stop stop)
        {
            return await FinishEarlyAsync(job, stop.Outcome).ConfigureAwait(false);
        }
        catch (OperationCanceledException)
        {
            throw;
        }
        catch (ArrowException)
        {
            throw;
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException or InvalidOperationException or System.ComponentModel.Win32Exception)
        {
            log.Error($"[K1] {task.TaskId}: lỗi workspace", ex);
            return await FinishEarlyAsync(job, new BlockOutcome
            {
                Outcome = OutcomeKind.Infra, Code = ErrorCodes.B1Workspace, Message = $"{ex.GetType().Name}: {ex.Message}", NeedsRerun = true,
            }).ConfigureAwait(false);
        }
    }

    private async Task<RepositoryAnalysis> FinishEarlyAsync(Job job, BlockOutcome outcome)
    {
        results.SaveJson(job.Run.RunId, job.Key, ResultPaths.Block1Outcome, outcome);
        await workspace.DeleteTaskAsync(job.Run.RunId, job.Key, job.Mirror, CancellationToken.None).ConfigureAwait(false);
        var metadata = results.LoadJson<RepositoryMetadata>(job.Run.RunId, job.Key, ResultPaths.Metadata);
        var baseline = results.LoadJson<Baseline>(job.Run.RunId, job.Key, ResultPaths.Baseline);
        return new RepositoryAnalysis(outcome, metadata, baseline, null);
    }

    private async Task<RepositoryAnalysis> AnalyzeCoreAsync(Job job)
    {
        var task = job.Task;
        var run = job.Run;
        var ct = job.Token;

        // ── Flow 1.1: tải đúng commit ─────────────────────────────────
        var missing = new[] { ("task_id", task.TaskId), ("repo_url", task.RepoUrl), ("checkout_sha", task.CheckoutSha), ("class_path", task.ClassPath), ("class_fqn", task.ClassFqn) }
            .Where(f => string.IsNullOrWhiteSpace(f.Item2)).Select(f => f.Item1).ToList();
        if (missing.Count > 0)
        {
            throw Excluded(ErrorCodes.B1ManifestInvalid, "Thiếu trường: " + string.Join(", ", missing));
        }

        if (run.Tools.JdkHomes.Count == 0)
        {
            throw new ArrowException(ErrorCodes.JdkMissing, "Không tìm thấy JDK nào (jdk_homes trong machine.local.json hoặc ARROW/Java-version/runtime/jdk-N)");
        }

        job.Mirror = workspace.MirrorPath(task.RepoUrl);
        await git.EnsureMirrorAsync(job.Mirror, task.RepoUrl, ct).ConfigureAwait(false);
        await FetchAsync(job).ConfigureAwait(false);

        var taskDir = workspace.TaskDir(run.RunId, job.Key);
        await workspace.DeleteTaskAsync(run.RunId, job.Key, job.Mirror, ct).ConfigureAwait(false);
        var baseDir = Path.Combine(taskDir, "base");
        var head = await git.AddWorktreeAsync(job.Mirror, task.CheckoutSha, baseDir, ct).ConfigureAwait(false);
        if (!string.Equals(head, task.CheckoutSha, StringComparison.OrdinalIgnoreCase))
        {
            throw Excluded(ErrorCodes.B1CommitMissing, $"HEAD của worktree là {head}, không phải {task.CheckoutSha}");
        }

        var classPath = BuildRootRule.NormalizeRelative(task.ClassPath);
        var cutFile = Abs(baseDir, classPath);
        if (!File.Exists(cutFile))
        {
            throw Excluded(ErrorCodes.B1CutMissing, $"Không có {classPath} ở commit {task.CheckoutSha}");
        }

        var cutSha = Hashing.Sha256TextFile(cutFile);
        var sourceRoot = PathCalculator.SourceRoot(classPath, task.ClassFqn);

        // ── Flow 1.2 + 6.1: thử từng cách build (manifest: một cách; tự dò: reactor rồi standalone) ──
        var plans = BuildRootRule.FromManifest(task) is { } fromManifest ? [fromManifest] : DetectPlans(baseDir, sourceRoot, task.BuildTool);
        if (plans.Count == 0)
        {
            throw Excluded(ErrorCodes.B1BuildRootNotFound, "Không có lần biên dịch test thành công trong manifest và không tìm được thư mục có file build");
        }

        Compiled? compiled = null;
        Stop? lastStop = null;
        foreach (var plan in plans)
        {
            try
            {
                compiled = await CompileAsync(job, plan, baseDir, sourceRoot).ConfigureAwait(false);
                break;
            }
            catch (Stop stop) when (plan != plans[^1] && stop.Outcome.Outcome == OutcomeKind.Excluded)
            {
                lastStop = stop;
            }
        }

        if (compiled is null)
        {
            throw lastStop ?? Excluded(ErrorCodes.B1BaselineBuildFailed, "Không biên dịch được");
        }

        // ── Flow 1.3: M ───────────────────────────────────────────────
        var metadata = BuildMetadata(job, compiled, baseDir, classPath, sourceRoot, cutSha);
        results.SaveJson(run.RunId, job.Key, ResultPaths.Metadata, metadata);
        results.SaveText(run.RunId, job.Key, ResultPaths.MetadataPrompt, MetadataPromptRenderer.Render(metadata) + "\n");
        results.SaveText(run.RunId, job.Key, ResultPaths.DependenciesResolved,
            string.Concat(compiled.Resolved.Select(d => $"{d.Group}:{d.Artifact}:{d.Version}:{d.Scope}\n")));
        results.SaveText(run.RunId, job.Key, ResultPaths.TestClasspath, string.Concat(compiled.Classpath.Select(c => c + "\n")));

        // ── snapshot ─────────────────────────────────────────────────
        var snapshot = Path.Combine(taskDir, "snapshot");
        workspace.DeleteDirectory(snapshot);
        Directory.Move(compiled.Context.RepoRoot, snapshot);
        var context = compiled.Context with { RepoRoot = snapshot };
        var info = new WorkspaceInfo
        {
            Mirror = Relative(workspace.WorkDir, job.Mirror),
            BaseCheckout = Relative(workspace.WorkDir, baseDir),
            CompiledSnapshot = Relative(workspace.WorkDir, snapshot),
            JdkMajor = compiled.JdkMajor,
            JdkHome = compiled.Context.JdkHome,
            BuildRoot = compiled.Plan.BuildRoot,
            BuildMode = compiled.Plan.Mode,
            BuildRootSource = compiled.Plan.Source,
            MavenModuleSelector = compiled.Plan.Tool == "maven" && compiled.Plan.Mode == BuildMode.Reactor ? $"-pl {compiled.Plan.Module} -am" : null,
            CommonFlags = context.CommonFlags.ToList(),
            Tool = compiled.Plan.Tool,
            Module = compiled.Plan.Module,
            Executable = context.Executable,
            ExecutableInRepo = context.ExecutableInRepo,
            InitScript = context.InitScript is null ? null : Relative(workspace.WorkDir, context.InitScript),
        };
        results.SaveJson(run.RunId, job.Key, ResultPaths.Workspace, info);

        // ── Flow 1.4: baseline ×N và tiêu chí B ───────────────────────
        var pairedClass = PairedTestClass(baseDir, task.TestClassPath);
        var baseline = await RunBaselineAsync(job, context, compiled, pairedClass).ConfigureAwait(false);
        results.SaveJson(run.RunId, job.Key, ResultPaths.Baseline, baseline);

        var outcome = run.Screening.RequirePairedTestPass && baseline.PairedTest.Status != PairedTestStatus.Pass
            ? new BlockOutcome
            {
                Outcome = OutcomeKind.Excluded,
                Code = ErrorCodes.B1PairedTestNotPassing,
                Message = $"Test người viết {baseline.PairedTest.Class ?? "(không có)"}: {Upper(baseline.PairedTest.Status)}",
            }
            : BlockOutcome.Ok();
        results.SaveJson(run.RunId, job.Key, ResultPaths.Block1Outcome, outcome);
        if (outcome.Outcome != OutcomeKind.Ok)
        {
            await workspace.DeleteTaskAsync(run.RunId, job.Key, job.Mirror, CancellationToken.None).ConfigureAwait(false);
            return new RepositoryAnalysis(outcome, metadata, baseline, null);
        }

        return new RepositoryAnalysis(outcome, metadata, baseline, info);
    }

    // ── Flow 1.1 ─────────────────────────────────────────────────────

    private async Task FetchAsync(Job job)
    {
        var attempts = Math.Max(1, job.Run.Verification.InfraAttempts);
        for (var attempt = 1; ; attempt++)
        {
            var result = await git.FetchCommitAsync(job.Mirror!, job.Task.CheckoutSha, workspace.LegacyMirrors(job.Task.RepoUrl), job.Token).ConfigureAwait(false);
            switch (result.Status)
            {
                case GitFetchStatus.Ok:
                    return;
                case GitFetchStatus.CommitMissing:
                    throw Excluded(ErrorCodes.B1CommitMissing, result.Detail);
                case GitFetchStatus.NetworkError when attempt >= attempts:
                    throw Infra(ErrorCodes.B1CloneFailed, $"git fetch lỗi sau {attempt} lần thử: {result.Detail}");
                default:
                    log.Warn($"[K1] {job.Task.TaskId}: git fetch lỗi mạng (lần {attempt}), thử lại");
                    await Task.Delay(Delay(job.Run, attempt), job.Token).ConfigureAwait(false);
                    break;
            }
        }
    }

    private static TimeSpan Delay(RunContext run, int attempt) =>
        run.RetryDelays.Count == 0 ? TimeSpan.Zero : run.RetryDelays[Math.Min(attempt - 1, run.RetryDelays.Count - 1)];

    // ── Flow 1.2: tự dò khi manifest không có lần biên dịch thành công ────

    /// <summary>Spec 01 Mục 4.1 bước 2: reactor ở thư mục cao nhất nhận module là con (nếu có), rồi standalone ở module root.</summary>
    private static List<BuildPlan> DetectPlans(string baseDir, string sourceRoot, string manifestTool)
    {
        var moduleRoot = PathCalculator.ModuleRootFromSourceRoot(sourceRoot);
        if (moduleRoot is null)
        {
            return [];
        }

        var tool = HasBuildFile(baseDir, moduleRoot, "maven") && manifestTool != "gradle" ? "maven"
            : HasBuildFile(baseDir, moduleRoot, "gradle") ? "gradle"
            : HasBuildFile(baseDir, moduleRoot, "maven") ? "maven" : null;
        if (tool is null)
        {
            return [];
        }

        var plans = new List<BuildPlan>();
        string? reactorRoot = null;
        var child = moduleRoot;
        while (child != ".")
        {
            var parent = Path.GetDirectoryName(child.Replace('/', Path.DirectorySeparatorChar))?.Replace('\\', '/') is { Length: > 0 } p ? p : ".";
            if (!DeclaresModule(baseDir, parent, child, tool))
            {
                break;
            }

            reactorRoot = parent;
            child = parent;
        }

        if (reactorRoot is not null)
        {
            var module = BuildRootRule.RelativeTo(Abs(baseDir, reactorRoot), Abs(baseDir, moduleRoot))!;
            plans.Add(new BuildPlan
            {
                Tool = tool,
                BuildRoot = reactorRoot,
                Mode = BuildMode.Reactor,
                Module = tool == "gradle" ? ":" + module.Replace('/', ':') : module,
                Source = "detected:reactor",
            });
        }

        plans.Add(new BuildPlan { Tool = tool, BuildRoot = moduleRoot, Mode = tool == "gradle" ? BuildMode.Reactor : BuildMode.Standalone, Module = tool == "gradle" ? ":" : null, Source = "detected:standalone" });
        return plans;
    }

    private static bool HasBuildFile(string baseDir, string dir, string tool) =>
        tool == "maven"
            ? File.Exists(Path.Combine(Abs(baseDir, dir), "pom.xml"))
            : new[] { "build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts" }.Any(f => File.Exists(Path.Combine(Abs(baseDir, dir), f)));

    [GeneratedRegex("""['"]:?([^'"]+)['"]""")]
    private static partial Regex QuotedName();

    private static bool DeclaresModule(string baseDir, string parent, string child, string tool)
    {
        var name = BuildRootRule.RelativeTo(Abs(baseDir, parent), Abs(baseDir, child));
        if (name is null)
        {
            return false;
        }

        if (tool == "maven")
        {
            return File.Exists(Path.Combine(Abs(baseDir, parent), "pom.xml")) && MavenModuleScanner.Modules(Abs(baseDir, parent)).Contains(name);
        }

        var settings = new[] { "settings.gradle", "settings.gradle.kts" }.Select(f => Path.Combine(Abs(baseDir, parent), f)).FirstOrDefault(File.Exists);
        return settings is not null && File.ReadAllLines(settings).Where(l => l.TrimStart().StartsWith("include", StringComparison.Ordinal))
            .SelectMany(l => QuotedName().Matches(l).Select(m => m.Groups[1].Value.Replace(':', '/')))
            .Contains(name);
    }

    // ── Flow 1.2 + 6.1: chọn công cụ, JDK, đọc cấu hình, biên dịch ────

    private sealed record Compiled(
        BuildPlan Plan,
        BuildContext Context,
        int JdkMajor,
        string JdkReason,
        string ToolVersion,
        bool Wrapper,
        string ModuleRoot,
        MavenProjectInfo? Pom,
        GradleInfo? Gradle,
        string? Declared,
        string? DeclaredSource,
        int? DeclaredMajor,
        IReadOnlyList<ResolvedDependency> Resolved,
        IReadOnlyList<string> Classpath,
        double CompileSeconds,
        Dictionary<string, string> MissingReasons);

    private async Task<Compiled> CompileAsync(Job job, BuildPlan plan, string baseDir, string sourceRoot)
    {
        var run = job.Run;
        if (!HasBuildFile(baseDir, plan.BuildRoot, plan.Tool))
        {
            throw Excluded(ErrorCodes.B1BuildRootNotFound, $"Build root {plan.BuildRoot} không có file build {plan.Tool}");
        }

        if (plan.Tool == "maven" && !PathCalculator.ModuleMatches(sourceRoot, BuildRootRule.ExpectedModuleRoot(plan)))
        {
            throw Excluded(ErrorCodes.B1ModuleMismatch, $"CUT ở {sourceRoot}, module root theo lệnh build là {BuildRootRule.ExpectedModuleRoot(plan)}");
        }

        var taskDir = workspace.TaskDir(run.RunId, job.Key);
        var building = Path.Combine(taskDir, "building");
        await PrepareBuildCopyAsync(baseDir, building, job.Token).ConfigureAwait(false);

        var (executable, wrapper) = ResolveExecutable(job, plan, building);
        var manifestMajor = ManifestJavaVersionParser.ParseMajor(job.Task.JavaVersion);
        var probeJdk = JdkSelector.Select(null, manifestMajor, run.Tools.JdkHomes.Keys.ToList()).Order[0];
        var missingReasons = new Dictionary<string, string>();
        var context = new BuildContext
        {
            RepoRoot = building,
            Plan = plan,
            Executable = wrapper ? Relative(building, executable) : executable,
            ExecutableInRepo = wrapper,
            JdkHome = run.Tools.JdkHomes[probeJdk],
            CommonFlags = plan.Tool == "gradle" ? BuildCommands.GradleCommonFlags : ["-B"],
            InitScript = plan.Tool == "gradle" ? WriteInitScript(taskDir) : null,
            Timeout = TimeSpan.FromSeconds(run.Verification.StepTimeoutSeconds),
        };

        // Phiên bản công cụ và mức Java khai báo (đọc bằng JDK của manifest).
        string toolVersion = "";
        MavenProjectInfo? pom = null;
        GradleInfo? gradle = null;
        string moduleRoot;
        if (plan.Tool == "maven")
        {
            var version = await RunStepAsync(job, context, ["-v"], "maven-version").ConfigureAwait(false);
            toolVersion = BuildFailureClassifier.ToolVersion(version.Result.Output) ?? "";
            context = context with { CommonFlags = BuildCommands.MavenCommonFlags(toolVersion.Length > 0 ? toolVersion : null, OperatingSystem.IsWindows()) };
            moduleRoot = BuildRootRule.ExpectedModuleRoot(plan);
            var pomFile = Path.Combine(taskDir, "effective-pom.xml");
            File.Delete(pomFile);
            var effective = await RunStepAsync(job, context, BuildCommands.MavenEffectivePom(NullIfEmpty(toolVersion), pomFile), "effective-pom").ConfigureAwait(false);
            if (effective.Failure == BuildFailureKind.None && File.Exists(pomFile))
            {
                try
                {
                    pom = EffectivePomReader.SelectTarget(EffectivePomReader.Parse(await File.ReadAllTextAsync(pomFile, job.Token).ConfigureAwait(false)), Abs(building, moduleRoot));
                }
                catch (System.Xml.XmlException ex)
                {
                    missingReasons["java"] = "effective POM không đọc được: " + ex.Message;
                }
            }
            else
            {
                missingReasons["java"] = "help:effective-pom lỗi: " + effective.Result.Tail(3);
            }
        }
        else
        {
            var probe = await RunStepAsync(job, context, BuildCommands.GradleInfo(plan.Module), "gradle-info").ConfigureAwait(false);
            if (probe.Failure == BuildFailureKind.None)
            {
                gradle = GradleProbe.Parse(probe.Result.Output);
                toolVersion = gradle.GradleVersion;
            }
            else
            {
                missingReasons["java"] = "init script Gradle lỗi: " + probe.Result.Tail(3);
            }

            moduleRoot = gradle is { ProjectDir.Length: > 0 } && BuildRootRule.RelativeTo(building, gradle.ProjectDir) is { } dir
                ? dir
                : BuildRootRule.ExpectedModuleRoot(plan);
            if (!PathCalculator.ModuleMatches(sourceRoot, moduleRoot))
            {
                throw Excluded(ErrorCodes.B1ModuleMismatch, $"CUT ở {sourceRoot}, thư mục project Gradle {plan.Module} là {moduleRoot}");
            }
        }

        var declared = pom?.Declared ?? gradle?.Declared;
        var declaredSource = pom?.DeclaredSource ?? gradle?.DeclaredSource;
        var declaredMajor = ManifestJavaVersionParser.ParseLevel(declared);
        if (declared is null && !missingReasons.ContainsKey("java"))
        {
            missingReasons["java"] = "file build không khai báo mức Java; dùng JDK của manifest";
        }

        // Biên dịch: JDK chọn theo mức khai báo, lỗi biên dịch thì thử JDK khác (spec 01 Mục 4.3 bước 4).
        var jdkPlan = JdkSelector.Select(declaredMajor, manifestMajor, run.Tools.JdkHomes.Keys.ToList());
        var tried = new List<int>();
        BuildRun? compile = null;
        foreach (var jdk in jdkPlan.Order)
        {
            if (tried.Count > 0)
            {
                await PrepareBuildCopyAsync(baseDir, building, job.Token).ConfigureAwait(false);
            }

            tried.Add(jdk);
            context = context with { JdkHome = run.Tools.JdkHomes[jdk] };
            var arguments = plan.Tool == "gradle" ? BuildCommands.GradleCompileWithClasspath(plan.Module) : BuildCommands.MavenCompileWithDependencies(NullIfEmpty(toolVersion));
            compile = await RunStepAsync(job, context, arguments, $"compile-jdk{jdk}").ConfigureAwait(false);
            switch (compile.Failure)
            {
                case BuildFailureKind.None:
                    break;
                case BuildFailureKind.Timeout:
                    throw Infra(ErrorCodes.B1BaselineTimeout, $"Biên dịch quá {run.Verification.StepTimeoutSeconds} s sau {run.Verification.InfraAttempts} lần thử");
                case BuildFailureKind.Network:
                    throw Infra(ErrorCodes.B1DependencyResolution, "Không tải được dependency/plugin: " + compile.Result.Tail(3));
                default:
                    log.Info($"[K1] {job.Task.TaskId}: biên dịch với JDK {jdk} lỗi");
                    continue;
            }

            break;
        }

        if (compile is null || compile.Failure != BuildFailureKind.None)
        {
            throw Excluded(ErrorCodes.B1BaselineBuildFailed, $"Không biên dịch được với JDK {string.Join(", ", tried)}: {compile?.Result.Tail(3)}");
        }

        var jdkMajor = tried[^1];
        var jdkReason = tried.Count == 1 ? jdkPlan.Reason : JdkSelector.FallbackReason(tried);

        // Dependency đã phân giải và classpath test (cùng lần gọi với biên dịch).
        IReadOnlyList<ResolvedDependency> resolved;
        IReadOnlyList<string> classpath;
        if (plan.Tool == "gradle")
        {
            var output = GradleProbe.Parse(compile.Result.Output);
            resolved = output.Resolved;
            classpath = output.Classpath;
        }
        else
        {
            var moduleDir = Abs(building, moduleRoot);
            var depsFile = Path.Combine(moduleDir, BuildCommands.MavenDependencyFile);
            var cpFile = Path.Combine(moduleDir, BuildCommands.MavenClasspathFile);
            resolved = File.Exists(depsFile) ? MavenDependencyListParser.Parse(await File.ReadAllTextAsync(depsFile, job.Token).ConfigureAwait(false)) : [];
            classpath = File.Exists(cpFile)
                ? (await File.ReadAllTextAsync(cpFile, job.Token).ConfigureAwait(false)).Trim().Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries)
                : [];
            if (!File.Exists(depsFile))
            {
                missingReasons["dependencies"] = "dependency:list không ghi file của module đích";
            }

            foreach (var generated in Directory.EnumerateFiles(Abs(building, plan.BuildRoot), "arrow-*.txt", new EnumerationOptions { RecurseSubdirectories = true, MaxRecursionDepth = 6 }))
            {
                File.Delete(generated);
            }
        }

        classpath = classpath.Select(entry => BuildRootRule.RelativeTo(building, entry) ?? entry.Replace('\\', '/')).ToList();
        return new Compiled(plan, context, jdkMajor, jdkReason, toolVersion, wrapper, moduleRoot, pom, gradle, declared, declaredSource, declaredMajor, resolved, classpath, Math.Round(compile.Result.Duration.TotalSeconds, 1), missingReasons);
    }

    private async Task PrepareBuildCopyAsync(string baseDir, string building, CancellationToken ct)
    {
        await workspace.CopyDirectoryAsync(baseDir, building, ct).ConfigureAwait(false);
        if (!OperatingSystem.IsWindows())
        {
            foreach (var wrapper in Directory.EnumerateFiles(building, "*", new EnumerationOptions { RecurseSubdirectories = true, MaxRecursionDepth = 4 })
                         .Where(f => Path.GetFileName(f) is "mvnw" or "gradlew"))
            {
                File.SetUnixFileMode(wrapper, File.GetUnixFileMode(wrapper) | UnixFileMode.UserExecute | UnixFileMode.GroupExecute | UnixFileMode.OtherExecute);
            }
        }
    }

    /// <summary>
    /// Chương trình build: theo đúng lệnh đã biên dịch thành công ở preflight (wrapper hay công cụ của máy);
    /// không có lệnh trong manifest thì dùng wrapper nếu có ở build root hoặc gốc repository (spec 01 Mục 4.2).
    /// </summary>
    private static (string Executable, bool Wrapper) ResolveExecutable(Job job, BuildPlan plan, string building)
    {
        var windows = OperatingSystem.IsWindows();
        var wrapperName = plan.Tool == "gradle" ? windows ? "gradlew.bat" : "gradlew" : windows ? "mvnw.cmd" : "mvnw";
        string? FindWrapper()
        {
            foreach (var dir in new[] { plan.BuildRoot, "." }.Distinct())
            {
                var candidate = Path.Combine(Abs(building, dir), wrapperName);
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }

            return null;
        }

        var stage = plan.Source.StartsWith("manifest:", StringComparison.Ordinal) ? plan.Source["manifest:".Length..] : null;
        var program = stage is null ? null : job.Task.BuildAttempts.FirstOrDefault(a => a.Stage == stage && a.ExitCode == 0)?.Command.FirstOrDefault();
        var usedWrapper = program is not null && Path.GetFileName(program.Replace('\\', '/')).StartsWith(plan.Tool == "gradle" ? "gradlew" : "mvnw", StringComparison.OrdinalIgnoreCase);
        if (program is null || usedWrapper)
        {
            if (FindWrapper() is { } wrapper)
            {
                return (wrapper, true);
            }
        }

        var system = plan.Tool == "gradle" ? job.Run.Tools.Gradle : job.Run.Tools.Maven;
        if (system is null)
        {
            throw Infra(plan.Tool == "gradle" ? ErrorCodes.GradleMissing : ErrorCodes.MavenMissing,
                $"Cần {(plan.Tool == "gradle" ? "Gradle" : "Maven")} (đặt {(plan.Tool == "gradle" ? "gradle_path" : "maven_path")} trong machine.local.json hoặc thêm vào PATH)");
        }

        return (system, false);
    }

    private static string WriteInitScript(string taskDir)
    {
        var path = Path.Combine(taskDir, GradleProbe.FileName);
        Directory.CreateDirectory(taskDir);
        File.WriteAllText(path, GradleProbe.InitScript.Replace("\r\n", "\n"), new UTF8Encoding(false));
        return path;
    }

    /// <summary>Chạy một lệnh build, ghi log, thử lại khi timeout hoặc lỗi mạng (tối đa <c>infra_attempts</c> lần).</summary>
    private async Task<BuildRun> RunStepAsync(Job job, BuildContext context, IReadOnlyList<string> arguments, string step)
    {
        var attempts = Math.Max(1, job.Run.Verification.InfraAttempts);
        for (var attempt = 1; ; attempt++)
        {
            var result = await build.RunAsync(context, arguments, job.Token).ConfigureAwait(false);
            SaveCommandLog(job, attempt == 1 ? step : $"{step}-try{attempt}", context, result);
            if (result.Failure is BuildFailureKind.Timeout or BuildFailureKind.Network && attempt < attempts)
            {
                log.Warn($"[K1] {job.Task.TaskId}: {step} {(result.Failure == BuildFailureKind.Timeout ? "quá thời gian" : "lỗi mạng")} (lần {attempt}), thử lại");
                await Task.Delay(Delay(job.Run, attempt), job.Token).ConfigureAwait(false);
                continue;
            }

            return result;
        }
    }

    private void SaveCommandLog(Job job, string step, BuildContext context, BuildRun run)
    {
        job.LogIndex++;
        var output = BuildCommands.TruncateLog(run.Result.Output);
        var text = BuildCommands.FormatLog(run, context.JdkHome, output);
        results.SaveText(job.Run.RunId, job.Key, $"{ResultPaths.Block1Dir}/logs/{job.LogIndex:00}-{step}.log", text);
    }

    // ── Flow 1.3 ─────────────────────────────────────────────────────

    private RepositoryMetadata BuildMetadata(Job job, Compiled compiled, string baseDir, string classPath, string sourceRoot, string cutSha)
    {
        var task = job.Task;
        var plan = compiled.Plan;
        var building = compiled.Context.RepoRoot;
        var (package, simple) = PathCalculator.SplitFqn(task.ClassFqn);

        // Cấu trúc: module trong build root.
        List<string> modules;
        if (plan.Tool == "gradle" && compiled.Gradle is { Projects.Count: > 0 } gradle)
        {
            var buildRootAbs = Abs(building, plan.BuildRoot);
            modules = gradle.Projects.Select(p => BuildRootRule.RelativeTo(buildRootAbs, p.Dir) ?? p.Path).Distinct().ToList();
        }
        else
        {
            modules = MavenModuleScanner.Modules(Abs(building, plan.BuildRoot)).ToList();
        }

        // Test root: khai báo trong file build, mặc định <module root>/src/test/java.
        var declaredTestRoot = compiled.Pom?.TestSourceDirectory ?? compiled.Gradle?.TestSourceDirs.FirstOrDefault();
        var testRoot = declaredTestRoot is not null && BuildRootRule.RelativeTo(building, declaredTestRoot) is { } t
            ? t
            : BuildRootRule.Join(compiled.ModuleRoot, "src/test/java");
        var existingNames = Directory.EnumerateFiles(Abs(building, plan.BuildRoot), simple + job.Run.GeneratedTestSuffix + "*.java", new EnumerationOptions { RecurseSubdirectories = true, IgnoreInaccessible = true })
            .Select(Path.GetFileNameWithoutExtension).ToHashSet(StringComparer.Ordinal);
        var (generatedPath, generatedFqn) = PathCalculator.GeneratedTest(testRoot, task.ClassFqn, job.Run.GeneratedTestSuffix,
            (path, name) => existingNames.Contains(name) || File.Exists(Abs(building, path)));

        // Package trong source root.
        var sourceAbs = Abs(baseDir, sourceRoot);
        var packages = Directory.Exists(sourceAbs)
            ? Directory.EnumerateFiles(sourceAbs, "*.java", new EnumerationOptions { RecurseSubdirectories = true, IgnoreInaccessible = true })
                .Select(f => Path.GetRelativePath(sourceAbs, Path.GetDirectoryName(f)!).Replace(Path.DirectorySeparatorChar, '.'))
                .Select(p => p == "." ? "" : p)
                .Where(p => p.Length > 0)
            : [];

        // Dependency trực tiếp và test framework.
        var direct = (compiled.Pom?.Dependencies ?? compiled.Gradle?.Dependencies ?? []).ToList();
        var pairedSource = task.TestClassPath is { Length: > 0 } pairedPath && File.Exists(Abs(baseDir, pairedPath)) ? File.ReadAllText(Abs(baseDir, pairedPath)) : null;
        var framework = TestFrameworkDetector.Detect(compiled.Resolved, pairedSource, task.TestingFramework, direct);
        var missing = new Dictionary<string, string>(compiled.MissingReasons);
        if (compiled.Resolved.Count == 0 && !missing.ContainsKey("dependencies"))
        {
            missing["dependencies"] = "không có dependency nào được phân giải";
        }

        var buildFile = plan.Tool == "maven"
            ? BuildRootRule.Join(plan.BuildRoot, "pom.xml")
            : new[] { "build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts" }
                .Select(f => BuildRootRule.Join(plan.BuildRoot, f)).FirstOrDefault(f => File.Exists(Abs(baseDir, f))) ?? BuildRootRule.Join(plan.BuildRoot, "build.gradle");

        return new RepositoryMetadata
        {
            TaskId = task.TaskId,
            Dataset = task.Dataset,
            Revision = new RevisionInfo
            {
                RepoUrl = task.RepoUrl, Commit = task.CheckoutSha, Provenance = task.RevisionProvenance, VerificationStatus = task.RevisionVerificationStatus,
            },
            BuildTool = new BuildToolInfo
            {
                Kind = plan.Tool,
                Version = compiled.ToolVersion,
                Wrapper = compiled.Wrapper,
                BuildRoot = plan.BuildRoot,
                BuildMode = plan.Mode,
                BuildFile = buildFile,
                ReactorModule = plan.Mode == BuildMode.Reactor ? plan.Module : null,
            },
            Java = new JavaInfo
            {
                Declared = compiled.Declared,
                DeclaredSource = compiled.DeclaredSource,
                Major = compiled.DeclaredMajor,
                JdkMajor = compiled.JdkMajor,
                JdkChoiceReason = compiled.JdkReason,
            },
            Dependencies = new DependencyInfo
            {
                Direct = direct.Where(d => d.Scope != "test").ToList(),
                Test = direct.Where(d => d.Scope == "test").ToList(),
                ResolvedCount = compiled.Resolved.Count,
            },
            TestFramework = framework,
            ProjectStructure = new ProjectStructureInfo
            {
                MultiModule = modules.Count > 1 || modules.Any(m => m != "."),
                Modules = modules,
                TargetModule = BuildRootRule.NormalizeRelative(task.ModulePath),
                ModuleRoot = compiled.ModuleRoot,
                Packages = PathCalculator.RankPackages(packages, package).ToList(),
            },
            Paths = new PathsInfo
            {
                SourceRoot = sourceRoot,
                TestRoot = testRoot,
                CutPath = classPath,
                GeneratedTestPath = generatedPath,
                GeneratedTestFqn = generatedFqn,
            },
            Cut = new CutInfo { Fqn = task.ClassFqn, SimpleName = simple, Package = package, SourceSha256 = cutSha },
            MissingReasons = missing,
        };
    }

    // ── Flow 1.4 ─────────────────────────────────────────────────────

    /// <summary>FQN của test người viết: package trong file + tên file; không đọc được file thì suy từ đường dẫn sau <c>src/test/java/</c>.</summary>
    private static string? PairedTestClass(string baseDir, string? testClassPath)
    {
        if (string.IsNullOrWhiteSpace(testClassPath))
        {
            return null;
        }

        var path = BuildRootRule.NormalizeRelative(testClassPath);
        var simple = Path.GetFileNameWithoutExtension(path);
        var file = Abs(baseDir, path);
        if (File.Exists(file))
        {
            var package = JavaPackage.Of(File.ReadAllText(file));
            return package.Length == 0 ? simple : $"{package}.{simple}";
        }

        var marker = path.IndexOf("src/test/java/", StringComparison.Ordinal);
        return marker < 0 ? simple : path[(marker + "src/test/java/".Length)..^".java".Length].Replace('/', '.');
    }

    private async Task<Baseline> RunBaselineAsync(Job job, BuildContext snapshotContext, Compiled compiled, string? pairedClass)
    {
        var run = job.Run;
        var arguments = compiled.Plan.Tool == "gradle" ? BuildCommands.GradleModuleTest(compiled.Plan.Module) : BuildCommands.MavenModuleTest;
        var runs = new List<IReadOnlyList<TestCaseResult>>();
        var durations = new List<double>();
        var attempts = Math.Max(1, run.Verification.InfraAttempts);
        for (var i = 1; i <= Math.Max(1, run.Verification.BaselineRuns); i++)
        {
            for (var attempt = 1; ; attempt++)
            {
                var copy = await workspace.NewCopyAsync(snapshotContext.RepoRoot, run.RunId, job.Key, $"baseline{i}", job.Token).ConfigureAwait(false);
                try
                {
                    var watch = Stopwatch.StartNew();
                    var context = snapshotContext with { RepoRoot = copy };
                    var result = await build.RunAsync(context, arguments, job.Token).ConfigureAwait(false);
                    SaveCommandLog(job, attempt == 1 ? $"baseline-{i}" : $"baseline-{i}-try{attempt}", context, result);
                    if (result.Failure is BuildFailureKind.Timeout or BuildFailureKind.Network)
                    {
                        if (attempt < attempts)
                        {
                            log.Warn($"[K1] {job.Task.TaskId}: baseline {i} {(result.Failure == BuildFailureKind.Timeout ? "quá thời gian" : "lỗi mạng")} (lần {attempt}), thử lại");
                            await Task.Delay(Delay(run, attempt), job.Token).ConfigureAwait(false);
                            continue;
                        }

                        throw result.Failure == BuildFailureKind.Timeout
                            ? Infra(ErrorCodes.B1BaselineTimeout, $"Chạy test sẵn có quá {run.Verification.StepTimeoutSeconds} s sau {attempt} lần thử")
                            : Infra(ErrorCodes.B1DependencyResolution, "Không tải được dependency khi chạy test: " + result.Result.Tail(3));
                    }

                    var tests = reports.Read(context.BuildRootPath);
                    if (result.Failure == BuildFailureKind.Build)
                    {
                        log.Warn($"[K1] {job.Task.TaskId}: baseline {i} mã thoát {result.Result.ExitCode}, đọc được {tests.Count} test");
                    }

                    runs.Add(tests);
                    durations.Add(Math.Round(watch.Elapsed.TotalSeconds, 1));
                    break;
                }
                finally
                {
                    workspace.DeleteDirectory(copy);
                }
            }
        }

        var (passing, flaky, failing) = BaselineEvaluator.Classify(runs);
        var compileCommand = BuildCommands.Command(compiled.Context, compiled.Plan.Tool == "gradle"
            ? BuildCommands.GradleCompileWithClasspath(compiled.Plan.Module)
            : BuildCommands.MavenCompileWithDependencies(NullIfEmpty(compiled.ToolVersion)));
        return new Baseline
        {
            Runs = runs.Count,
            Commands = new Dictionary<string, List<string>>
            {
                ["compile"] = compileCommand.ToList(),
                ["module_test"] = BuildCommands.Command(snapshotContext, arguments).ToList(),
            },
            WorkingDir = compiled.Plan.BuildRoot,
            PassingInAllRuns = passing,
            Flaky = flaky,
            FailingInAllRuns = failing,
            PairedTest = BaselineEvaluator.Paired(runs, pairedClass),
            DurationsSeconds = new BaselineDurations { Compile = compiled.CompileSeconds, ModuleTest = durations },
        };
    }

    // ── tiện ích ─────────────────────────────────────────────────────

    private static string Abs(string root, string relative) =>
        Path.GetFullPath(Path.Combine(root, relative == "." ? "" : relative.Replace('/', Path.DirectorySeparatorChar)));

    private static string Relative(string root, string path) => Path.GetRelativePath(root, path).Replace('\\', '/');

    private static string? NullIfEmpty(string value) => value.Length == 0 ? null : value;

    private static string Upper(PairedTestStatus status) => JsonFile.Serialize(status).Trim('"');
}
