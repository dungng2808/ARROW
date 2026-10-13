using Arrow.Cli.Logging;
using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Prepare;
using Arrow.Core.Prompts;
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
/// Chạy thật: shard của máy này, hoặc mọi task của một cấu hình thực nghiệm (<c>--all-tasks</c>, <c>--experiment</c>).
/// Các bước S0 chuẩn bị → S1 sinh test → S2 sửa → S3 EvoSuite → S4 đo chất lượng chạy lần lượt; danh sách job của bước sau
/// dựng từ kết quả của bước trước (README Mục 3).
/// </summary>
public sealed class ShardRunScreen(Session session)
{
    public static readonly string[] AllStages = ["prepare", "generate", "repair", "evosuite", "quality", "report"];

    /// <param name="AllTasks">Chạy mọi task của cấu hình thực nghiệm thay vì shard của máy (người điều phối thử trước khi chia).</param>
    /// <param name="ExperimentPath">Cấu hình thực nghiệm khác cấu hình của máy (ví dụ smoke); chạy mọi task của nó, không theo shard.</param>
    /// <param name="Stages">Chỉ chạy các bước này (mặc định: mọi bước).</param>
    /// <param name="FakeLlmPath">File luật của model giả (không gọi API, không bật proxy).</param>
    /// <param name="MaxLlmCalls">Giới hạn số lượt gọi API thật của lần chạy (smoke).</param>
    public sealed record Options(string? RunId, bool Plain, bool RerunInfra, IReadOnlyCollection<string>? Tasks, bool AllTasks = false,
        string? ExperimentPath = null, IReadOnlyCollection<string>? Stages = null, string? FakeLlmPath = null, int? MaxLlmCalls = null);

    public async Task<int> RunAsync(Options options, CancellationToken cancellationToken)
    {
        var paths = session.Paths;
        var machine = session.Machine ?? throw new ArrowException("NO_MACHINE_CONFIG", "Máy chưa cài đặt. Chạy arrow setup trước.");
        var useShard = options.ExperimentPath is null && !options.AllTasks;
        if (string.IsNullOrEmpty(options.ExperimentPath ?? machine.Experiment) || useShard && string.IsNullOrEmpty(machine.Shard))
        {
            throw new ArrowException("NO_SHARD", "Máy chưa chọn cấu hình thực nghiệm hoặc shard. Chạy arrow setup.");
        }

        if (!string.IsNullOrEmpty(machine.ArrowRoot) && Path.GetFullPath(machine.ArrowRoot).TrimEnd('/', '\\') != paths.ArrowRoot.TrimEnd('/', '\\'))
        {
            var steps = Arrow.Infrastructure.Doctor.DoctorService.MovedArrowSteps().Select((step, i) => $"{i + 1}. {step}");
            throw new ArrowException("ARROW_MOVED",
                $"Thư mục ARROW đã đổi tên hoặc chuyển chỗ (cấu hình ghi {machine.ArrowRoot}, hiện tại {paths.ArrowRoot}); đường dẫn JDK, LiteLLM còn trỏ chỗ cũ. Cách sửa:\n{string.Join('\n', steps)}");
        }

        var experiment = ExperimentLoader.Load(paths.Resolve(options.ExperimentPath ?? machine.Experiment));
        ShardFile shard;
        string shardSha;
        if (useShard)
        {
            var shardPath = paths.Resolve(machine.Shard);
            (shard, var plan) = ShardPlanner.LoadShard(shardPath);
            if (plan is not null && !string.Equals(plan.ExperimentSha256, experiment.Sha256, StringComparison.OrdinalIgnoreCase))
            {
                throw new ArrowException("SHARD_EXPERIMENT_MISMATCH", "Shard được chia cho cấu hình thực nghiệm khác. git pull hoặc nhờ người điều phối chia lại.");
            }

            shardSha = Hashing.Sha256File(shardPath);
        }
        else
        {
            shard = new ShardFile { ShardId = "all" };
            shardSha = experiment.Sha256;
        }

        var records = experiment.Config.Manifests
            .SelectMany(m => ManifestReader.Read(experiment.ResolveManifestPath(m), m.Dataset))
            .ToDictionary(r => (r.Dataset, r.TaskId));
        var selected = useShard
            ? shard.Tasks.Select(t => records.GetValueOrDefault((t.Dataset, t.TaskId)) ?? throw new ArrowException("TASK_NOT_IN_MANIFEST", $"{t.TaskId} không có trong manifest")).ToList()
            : records.Values.ToList();
        var tasks = selected.Where(t => options.Tasks is null || options.Tasks.Contains(t.TaskId)).ToList();
        if (tasks.Count == 0)
        {
            AnsiConsole.MarkupLine("[yellow]Không có task nào để chạy (kiểm tra --tasks).[/]");
            return 1;
        }

        var runId = options.RunId ?? (useShard ? session.DefaultRunId() : $"{experiment.Config.ExperimentId}-all-{machine.MachineName}");
        var stages = (options.Stages ?? AllStages).ToHashSet(StringComparer.Ordinal);
        var store = session.Store(runId);
        store.EnsureCreated();
        using var runLog = ToolLogSetup.AttachRun(store.ToolLogPath);
        var prompts = PromptLibrary.Load(Path.Combine(paths.PilotToolDir, experiment.Config.PromptSet.Dir));
        var runLock = await BuildLockAsync(runId, experiment, shard, shardSha, machine, prompts, cancellationToken).ConfigureAwait(false);
        RunConfigLock.WriteOrVerify(store, runLock);

        var workDir = paths.Resolve(machine.WorkDir);
        var pathAdvice = PathDiagnostics.Diagnose(workDir, paths.ArrowRoot, OperatingSystem.IsWindows(), Environment.GetFolderPath(Environment.SpecialFolder.UserProfile));
        if (pathAdvice.Blocking)
        {
            throw new ArrowException("WORK_DIR_UNSAFE", $"Thư mục repository {workDir} không dùng được:\n{pathAdvice.Describe()}");
        }

        Directory.CreateDirectory(workDir);
        await using var pipeline = new Pipeline(paths, machine, experiment.Config, store, workDir, prompts, options);
        var byTask = tasks.ToDictionary(t => (t.Dataset, t.TaskId));
        var factories = new List<Func<StageSpec?>>
        {
            () => stages.Contains("prepare") ? pipeline.PrepareStage(tasks) : null,
            () => stages.Contains("generate") ? pipeline.GenerateStage(tasks, cancellationToken) : null,
        };
        factories.AddRange(pipeline.LaterStages(tasks, stages, cancellationToken));

        var header = new DashboardView.Header(runId, $"{experiment.Config.ExperimentId} · {tasks.Count} task · {string.Join(",", AllStages.Where(stages.Contains))}",
            options.FakeLlmPath is not null ? "model giả (--fake-llm)" : string.Join(", ", experiment.Config.Models.Select(m => m.Name)), machine.MachineName);
        var host = new RunHost(store, header, options.Plain, options.RerunInfra);
        var stopped = await host.RunAsync(factories, cancellationToken).ConfigureAwait(false);

        if (stages.Contains("prepare"))
        {
            PrintSummary(store, tasks);
        }

        PrintOutcomes(store);
        if (pipeline.LlmCalls > 0)
        {
            AnsiConsole.MarkupLine($"Lượt gọi API thật: [bold]{pipeline.LlmCalls}[/]");
        }

        if (!stopped && stages.Contains("report"))
        {
            var report = new Arrow.Infrastructure.Report.ReportService(experiment.Config).Write(store);
            AnsiConsole.MarkupLine($"{Theme.Ok} S5 báo cáo: [bold]{Markup.Escape(paths.Relative(Path.Combine(report.Dir, "pilot_report.md")))}[/] · "
                                   + (report.Part2.Computable ? $"N theo {Markup.Escape(report.Part2.Rule)} = [bold]{report.Part2.N}[/]" : "N chưa tính được"));
        }

        var finished = store.LatestByJob().Values.Count(e => RunStore.IsFinished(e, rerunInfra: false));
        if (stopped)
        {
            RunHost.PrintStopped(runId, $"arrow run --run-id {runId}", finished, store.LatestByJob().Count);
        }
        else
        {
            AnsiConsole.MarkupLine($"\n{Theme.Ok} Xong run [bold]{Markup.Escape(runId)}[/]. Job INFRA chạy lại bằng: [bold]arrow run --run-id {Markup.Escape(runId)} --rerun-infra[/]");
        }

        return 0;
    }

    /// <summary>Số job theo stage và kết quả của run.</summary>
    private static void PrintOutcomes(RunStore store)
    {
        var table = new Table().Border(TableBorder.Rounded).AddColumns("Bước", "Kết quả", "Số job");
        foreach (var group in store.LatestByJob().Values.Where(e => e.Job is not null && e.Job.Stage != "prepare")
                     .GroupBy(e => (e.Job!.Stage, e.Outcome ?? e.Status?.ToString() ?? "")).OrderBy(g => g.Key.Stage, StringComparer.Ordinal).ThenBy(g => g.Key.Item2, StringComparer.Ordinal))
        {
            table.AddRow(group.Key.Stage, Markup.Escape(group.Key.Item2), group.Count().ToString(System.Globalization.CultureInfo.InvariantCulture));
        }

        if (table.Rows.Count > 0)
        {
            AnsiConsole.Write(table);
        }
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

    private async Task<RunConfigLock> BuildLockAsync(string runId, LoadedExperiment experiment, ShardFile shard, string shardSha, MachineConfig machine,
        PromptLibrary prompts, CancellationToken cancellationToken)
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
            PromptHashes = prompts.Hashes,
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
