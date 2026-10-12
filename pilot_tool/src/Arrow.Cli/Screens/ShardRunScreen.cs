using Arrow.Cli.Logging;
using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Prepare;
using Arrow.Core.Runs;
using Arrow.Core.Shards;
using Arrow.Core.Util;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Llm;
using Arrow.Infrastructure.Prepare;
using Arrow.Infrastructure.Processes;
using Arrow.Infrastructure.Tooling;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>
/// Chạy thật shard của máy này. Mốc M2: S0 (chuẩn bị repository, baseline, 6 thông tin repository);
/// các bước sau thêm vào danh sách stage ở các mốc tiếp theo.
/// </summary>
public sealed class ShardRunScreen(Session session)
{
    /// <param name="AllTasks">Chạy mọi task của cấu hình thực nghiệm thay vì shard của máy (người điều phối thử trước khi chia).</param>
    public sealed record Options(string? RunId, bool Plain, bool RerunInfra, IReadOnlyCollection<string>? Tasks, bool AllTasks = false);

    public async Task<int> RunAsync(Options options, CancellationToken cancellationToken)
    {
        var paths = session.Paths;
        var machine = session.Machine ?? throw new ArrowException("NO_MACHINE_CONFIG", "Máy chưa cài đặt. Chạy arrow setup trước.");
        if (string.IsNullOrEmpty(machine.Experiment) || string.IsNullOrEmpty(machine.Shard))
        {
            throw new ArrowException("NO_SHARD", "Máy chưa chọn cấu hình thực nghiệm hoặc shard. Chạy arrow setup.");
        }

        if (!string.IsNullOrEmpty(machine.ArrowRoot) && Path.GetFullPath(machine.ArrowRoot).TrimEnd('/', '\\') != paths.ArrowRoot.TrimEnd('/', '\\'))
        {
            var steps = Arrow.Infrastructure.Doctor.DoctorService.MovedArrowSteps().Select((step, i) => $"{i + 1}. {step}");
            throw new ArrowException("ARROW_MOVED",
                $"Thư mục ARROW đã đổi tên hoặc chuyển chỗ (cấu hình ghi {machine.ArrowRoot}, hiện tại {paths.ArrowRoot}); đường dẫn JDK, LiteLLM còn trỏ chỗ cũ. Cách sửa:\n{string.Join('\n', steps)}");
        }

        var experiment = ExperimentLoader.Load(paths.Resolve(machine.Experiment));
        var shardPath = paths.Resolve(machine.Shard);
        var (shard, plan) = ShardPlanner.LoadShard(shardPath);
        if (plan is not null && !string.Equals(plan.ExperimentSha256, experiment.Sha256, StringComparison.OrdinalIgnoreCase))
        {
            throw new ArrowException("SHARD_EXPERIMENT_MISMATCH", "Shard được chia cho cấu hình thực nghiệm khác. git pull hoặc nhờ người điều phối chia lại.");
        }

        var records = experiment.Config.Manifests
            .SelectMany(m => ManifestReader.Read(experiment.ResolveManifestPath(m), m.Dataset))
            .ToDictionary(r => (r.Dataset, r.TaskId));
        var selected = options.AllTasks
            ? records.Values.ToList()
            : shard.Tasks.Select(t => records.GetValueOrDefault((t.Dataset, t.TaskId)) ?? throw new ArrowException("TASK_NOT_IN_MANIFEST", $"{t.TaskId} không có trong manifest")).ToList();
        var tasks = selected.Where(t => options.Tasks is null || options.Tasks.Contains(t.TaskId)).ToList();
        if (tasks.Count == 0)
        {
            AnsiConsole.MarkupLine("[yellow]Không có task nào để chạy (kiểm tra --tasks).[/]");
            return 1;
        }

        var runId = options.RunId ?? (options.AllTasks ? $"{experiment.Config.ExperimentId}-all-{machine.MachineName}" : session.DefaultRunId());
        if (options.AllTasks)
        {
            shard = shard with { ShardId = "all" };
        }

        var store = session.Store(runId);
        store.EnsureCreated();
        using var runLog = ToolLogSetup.AttachRun(store.ToolLogPath);
        var runLock = await BuildLockAsync(runId, experiment, shard, options.AllTasks ? experiment.Sha256 : Hashing.Sha256File(shardPath), machine, cancellationToken).ConfigureAwait(false);
        RunConfigLock.WriteOrVerify(store, runLock);

        var workDir = paths.Resolve(machine.WorkDir);
        var pathAdvice = PathDiagnostics.Diagnose(workDir, paths.ArrowRoot, OperatingSystem.IsWindows(), Environment.GetFolderPath(Environment.SpecialFolder.UserProfile));
        if (pathAdvice.Blocking)
        {
            throw new ArrowException("WORK_DIR_UNSAFE", $"Thư mục repository {workDir} không dùng được:\n{pathAdvice.Describe()}");
        }

        Directory.CreateDirectory(workDir);
        var git = new GitRepositoryProvider(machine.GitPath, workDir, machine.RepoPolicy);
        var prepare = new PrepareService(git, machine, experiment.Config, workDir);
        var byTask = tasks.ToDictionary(t => t.TaskId);

        var prepareJobs = tasks.Select(t => new JobKey { Dataset = t.Dataset, TaskId = t.TaskId, Stage = "prepare" }).ToList();
        var prepareStage = new StageSpec("prepare", prepareJobs, Math.Max(1, machine.Workers.Build), "build",
            async (job, context, ct) =>
            {
                var record = byTask[job.TaskId];
                try
                {
                    var result = await prepare.RunAsync(record, store, job, context.Step, ct).ConfigureAwait(false);
                    var b = result.Baseline;
                    var taskDir = Path.GetRelativePath(store.Root, store.TaskDir(record.Dataset, record.TaskId)).Replace('\\', '/');
                    return new JobResult
                    {
                        Outcome = "READY",
                        Message = $"JDK {result.Task.JdkMajor} · {result.Task.Plan.Tool.ToString().ToLowerInvariant()} {result.Task.Plan.Mode.ToString().ToLowerInvariant()} · {b.TestsTotal} test ({b.FailingInAllRuns.Count} fail sẵn, {b.Flaky.Count} flaky) · {result.Context.Dependencies.Count} dependency",
                        BuildSeconds = Math.Round(result.BuildSeconds, 2),
                        Outputs = [$"{taskDir}/{PrepareService.TaskFile}", $"{taskDir}/{PrepareService.BaselineFile}", $"{taskDir}/{PrepareService.RepoContextFile}"],
                    };
                }
                catch (PrepareException ex)
                {
                    return new JobResult { Status = JobStatus.Infra, Outcome = "INFRA", Reason = ex.Reason, Message = ex.Message };
                }
            },
            job => job.TaskId.Length > 24 ? job.TaskId[..24] + "…" : job.TaskId);

        var header = new DashboardView.Header(runId, $"S0 chuẩn bị · {tasks.Count} task", "chưa gọi model (M2)", machine.MachineName);
        var host = new RunHost(store, header, options.Plain, options.RerunInfra);
        var stopped = await host.RunAsync([prepareStage], cancellationToken).ConfigureAwait(false);

        PrintSummary(store, tasks);
        if (stopped)
        {
            var finished = store.LatestByJob().Values.Count(e => RunStore.IsFinished(e, rerunInfra: false));
            RunHost.PrintStopped(runId, $"arrow run --run-id {runId}", finished, prepareJobs.Count);
        }
        else
        {
            AnsiConsole.MarkupLine($"\n{Theme.Ok} S0 xong cho run [bold]{Markup.Escape(runId)}[/]. Task INFRA chạy lại bằng: [bold]arrow run --run-id {Markup.Escape(runId)} --rerun-infra[/]");
        }

        return 0;
    }

    private static void PrintSummary(RunStore store, IReadOnlyList<ManifestRecord> tasks)
    {
        var latest = store.LatestByJob();
        var table = new Table().Border(TableBorder.Rounded)
            .AddColumns("Task", "Kết quả", "JDK", "Build", "Test baseline", "Thời gian");
        foreach (var task in tasks)
        {
            var entry = latest.Values.FirstOrDefault(e => e.Job?.TaskId == task.TaskId && e.Job.Stage == "prepare");
            var taskFile = Path.Combine(store.TaskDir(task.Dataset, task.TaskId), PrepareService.TaskFile);
            var baselineFile = Path.Combine(store.TaskDir(task.Dataset, task.TaskId), PrepareService.BaselineFile);
            var info = entry?.Status == JobStatus.Done && File.Exists(taskFile) ? Json.Read<TaskInfo>(taskFile) : null;
            var baseline = info is not null && File.Exists(baselineFile) ? Json.Read<Baseline>(baselineFile) : null;
            var outcome = entry is null ? "[grey]chưa chạy[/]"
                : entry.Status == JobStatus.Infra ? $"[red]INFRA[/] {Markup.Escape(entry.Reason ?? "")}"
                : "[green]READY[/]";
            table.AddRow(
                Markup.Escape(task.TaskId),
                outcome,
                info?.JdkMajor.ToString() ?? "",
                info is null ? "" : $"{info.Plan.Tool.ToString().ToLowerInvariant()} {info.Plan.Mode.ToString().ToLowerInvariant()}",
                baseline is null ? "" : $"{baseline.TestsTotal} ({baseline.FailingInAllRuns.Count} fail, {baseline.Flaky.Count} flaky)",
                entry?.StartedAt is { } s && entry.EndedAt is { } e ? Theme.Duration((e - s).TotalSeconds) : "");
        }

        AnsiConsole.Write(table);
    }

    private async Task<RunConfigLock> BuildLockAsync(string runId, LoadedExperiment experiment, ShardFile shard, string shardSha, MachineConfig machine, CancellationToken cancellationToken)
    {
        var jdkVersions = new Dictionary<string, string>();
        foreach (var (major, home) in machine.JdkHomes)
        {
            var info = await ToolProbe.VerifyJdkAsync(home, int.Parse(major, System.Globalization.CultureInfo.InvariantCulture), cancellationToken).ConfigureAwait(false);
            jdkVersions[major] = info?.JavaVersion ?? "unknown";
        }

        var javaHome = machine.JdkHomes.GetValueOrDefault("17") ?? machine.JdkHomes.Values.FirstOrDefault();
        var maven = await ToolProbe.ProbeMavenAsync(machine.MavenPath, javaHome, cancellationToken).ConfigureAwait(false);
        var gradle = await ToolProbe.ProbeGradleAsync(machine.GradlePath, javaHome, cancellationToken).ConfigureAwait(false);
        var gitInfo = await ToolProbe.ProbeGitAsync(machine.GitPath, cancellationToken).ConfigureAwait(false);
        var (commit, dirty) = await GitStateAsync(machine.GitPath, cancellationToken).ConfigureAwait(false);
        var llm = LiteLlmConfigFile.TryLoad(session.Paths.LiteLlmConfigPath);
        var build = experiment.Config.Build;
        return new RunConfigLock
        {
            RunId = runId,
            CreatedAt = DateTimeOffset.UtcNow,
            ExperimentId = experiment.Config.ExperimentId,
            ExperimentSha256 = experiment.Sha256,
            ShardId = shard.ShardId,
            ShardSha256 = shardSha,
            ToolVersion = typeof(ShardRunScreen).Assembly.GetName().Version?.ToString(3) ?? "",
            ToolGitCommit = commit,
            ToolGitDirty = dirty,
            MachineName = machine.MachineName,
            Os = MachineInfo.RuntimeId,
            CpuCount = MachineInfo.CpuCount,
            RamGb = MachineInfo.RamGb,
            JdkVersions = jdkVersions,
            MavenVersion = maven?.Version,
            GradleVersion = gradle?.Version,
            GitVersion = gitInfo?.Version,
            LlmEndpointHost = llm?.EndpointHost,
            LlmKeyAlias = machine.Llm.KeyAlias,
            BuildFlags = new Dictionary<string, List<string>>
            {
                ["maven"] = build.MavenExtraFlags,
                ["gradle"] = build.GradleExtraFlags,
                ["gradle_excluded_tasks"] = build.GradleExcludedTasks,
                ["jvm"] = [.. BuildCommands.JvmOptions],
            },
            NMax = experiment.Config.Repair.NMax,
            NRule = experiment.Config.NRule,
        };
    }

    /// <summary>Commit của repository ARROW chứa pilot_tool và pilot_tool có thay đổi chưa commit hay không.</summary>
    private async Task<(string? Commit, bool? Dirty)> GitStateAsync(string gitPath, CancellationToken cancellationToken)
    {
        var git = string.IsNullOrEmpty(gitPath) ? "git" : gitPath;
        var root = session.Paths.ArrowRoot;
        var head = await ProcessRunner.RunAsync(git, ["-C", root, "rev-parse", "HEAD"], root, null, TimeSpan.FromSeconds(30), cancellationToken).ConfigureAwait(false);
        if (!head.Succeeded)
        {
            return (null, null);
        }

        var status = await ProcessRunner.RunAsync(git, ["-C", root, "status", "--porcelain", "--", session.Paths.PilotToolDir], root, null, TimeSpan.FromSeconds(30), cancellationToken).ConfigureAwait(false);
        return (head.StdOut.Trim(), status.Succeeded ? status.StdOut.Trim().Length > 0 : null);
    }
}
