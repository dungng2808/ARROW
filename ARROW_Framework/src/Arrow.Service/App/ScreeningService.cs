using System.Diagnostics;
using System.Globalization;
using System.Reflection;
using Arrow.DataAccess;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Manifest;
using Arrow.Models.Run;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

/// <summary>
/// <c>arrow screen</c> (spec 01 Mục 6.3, spec 06 S10): chạy chỉ khối 1 cho mọi CUT của một manifest, song song theo <c>workers.build</c>,
/// ghi ledger sau mỗi task (chạy lại cùng run id thì bỏ qua task đã xong), rồi ghi <c>reports/screening.csv</c>.
/// Sàng lọc chỉ <b>đo</b> tiêu chí B: task không đạt vẫn có <c>outcome = OK</c>, trạng thái nằm ở cột <c>paired_test_status</c>.
/// </summary>
public sealed class ScreeningService(
    FrameworkPaths paths,
    IConfigRepository config,
    IManifestRepository manifests,
    IRunRepository runs,
    ITaskResultRepository results,
    IReportRepository reports,
    Func<ToolEnvironment, string, IRepositoryAnalyzer> analyzerFactory,
    ILogRepository log) : IScreeningService
{
    public const string Job = "analyze";
    public const string CsvName = "screening.csv";

    public static readonly IReadOnlyList<string> CsvHeaders = ["task_id", "outcome", "code", "paired_test_status", "tests_run", "jdk_major", "message"];

    public async Task<ScreeningSummary> RunAsync(ScreeningRequest request, IProgress<RunEvent> progress, CancellationToken cancellationToken)
    {
        var manifestPath = Path.GetFullPath(request.ManifestPath);
        if (!File.Exists(manifestPath))
        {
            throw new ArrowException(ErrorCodes.ConfigMissing, $"Không có manifest {manifestPath}");
        }

        if (string.IsNullOrWhiteSpace(request.RunId) || request.RunId.Any(c => !(char.IsAsciiLetterOrDigit(c) || c is '-' or '_' or '.')))
        {
            throw new ArgumentException($"Run id chỉ gồm chữ, số, '-', '_', '.': '{request.RunId}'");
        }

        var dataset = request.Dataset ?? GuessDataset(manifestPath);
        var all = manifests.ReadTasks(manifestPath, dataset);
        var wanted = request.TaskIds.Concat(ParseSelection(request.TaskSelection)).Distinct().ToList();
        var unknown = wanted.Except(all.Select(t => t.TaskId)).ToList();
        if (unknown.Count > 0)
        {
            throw new ArgumentException("Task không có trong manifest: " + string.Join(", ", unknown));
        }

        var selected = wanted.Count == 0 ? all : all.Where(t => wanted.Contains(t.TaskId)).ToList();
        var machine = config.LoadMachine();
        var tools = ToolLocator.Environment(machine, paths.ArrowRoot);
        var workDir = paths.Resolve(machine?.WorkDir ?? "../repo");
        var analyzer = analyzerFactory(tools, workDir);
        PrepareRun(request.RunId, dataset, manifestPath, machine, tools);

        var context = new RunContext
        {
            RunId = request.RunId,
            Verification = new VerificationConfig(),
            Screening = new ScreeningConfig { RequirePairedTestPass = false },
            Tools = tools,
            KeepWorkspace = request.KeepWorkspace,
        };

        var done = LastEntries(request.RunId);
        var pending = selected.Where(t => !done.TryGetValue(t.TaskId, out var entry) || entry.Outcome == OutcomeKind.Infra && request.RerunInfra).ToList();
        var workers = Math.Max(1, request.Workers ?? machine?.Workers.Build ?? 4);
        Report(progress, request.RunId, null, EventLevel.Info,
            $"Sàng lọc {selected.Count} CUT ({dataset}): {selected.Count - pending.Count} đã xong trong run, chạy {pending.Count}, song song {workers}; work_dir {workDir}");

        var finished = 0;
        await Parallel.ForEachAsync(pending, new ParallelOptions { MaxDegreeOfParallelism = workers, CancellationToken = cancellationToken }, async (task, ct) =>
        {
            Report(progress, request.RunId, task.TaskId, EventLevel.Info, "bắt đầu");
            var watch = Stopwatch.StartNew();
            var analysis = await analyzer.AnalyzeAsync(task, context, ct).ConfigureAwait(false);
            runs.AppendLedger(request.RunId, new LedgerEntry
            {
                At = DateTimeOffset.Now,
                Block = 1,
                Dataset = dataset,
                TaskId = task.TaskId,
                Job = Job,
                Outcome = analysis.Outcome.Outcome,
                Code = analysis.Outcome.Code,
                Message = analysis.Outcome.Message,
                NeedsRerun = analysis.Outcome.NeedsRerun,
            });
            var count = Interlocked.Increment(ref finished);
            var row = Row(dataset, task.TaskId, analysis.Outcome, analysis.Baseline, analysis.Metadata);
            Report(progress, request.RunId, task.TaskId, analysis.Outcome.Outcome == OutcomeKind.Ok ? EventLevel.Info : EventLevel.Warn,
                $"{Describe(row)} ({watch.Elapsed.TotalSeconds:0} s) [{count}/{pending.Count}]");
        }).ConfigureAwait(false);

        var rows = BuildRows(request.RunId, dataset, selected);
        reports.WriteCsv(request.RunId, CsvName, new CsvTable(CsvHeaders, rows.Select(ToCsv).ToList()));
        return new ScreeningSummary(request.RunId, manifestPath, rows, Path.Combine(reports.ReportsDir(request.RunId), CsvName));
    }

    /// <summary><c>--tasks</c>: file có sẵn thì đọc từng dòng (bỏ dòng trống và dòng <c>#</c>), không thì tách theo dấu phẩy.</summary>
    public static IReadOnlyList<string> ParseSelection(string? selection)
    {
        if (string.IsNullOrWhiteSpace(selection))
        {
            return [];
        }

        var items = File.Exists(selection) ? File.ReadAllLines(selection) : selection.Split(',');
        return items.Select(t => t.Trim()).Where(t => t.Length > 0 && !t.StartsWith('#')).Distinct().ToList();
    }

    /// <summary><c>gitbug</c> trong tên file → <c>gitbug-java</c>, còn lại <c>classes2test</c>.</summary>
    public static string GuessDataset(string manifestPath) =>
        Path.GetFileName(manifestPath).Contains("gitbug", StringComparison.OrdinalIgnoreCase) ? "gitbug-java" : "classes2test";

    /// <summary>Tạo run hoặc kiểm tra run cũ cùng manifest (chạy tiếp với manifest khác thì từ chối).</summary>
    private void PrepareRun(string runId, string dataset, string manifestPath, MachineConfig? machine, ToolEnvironment tools)
    {
        var sha = Hashing.Sha256TextFile(manifestPath);
        var existing = runs.ReadLock(runId);
        if (existing is not null)
        {
            if (!existing.ExperimentId.StartsWith("screen:", StringComparison.Ordinal) || !existing.ManifestSha256.TryGetValue(dataset, out var old) || old != sha)
            {
                throw new ArrowException(ErrorCodes.RunConfigMismatch, $"Run {runId} đã có với cấu hình khác ({existing.ExperimentId}); chọn run id khác");
            }

            return;
        }

        runs.EnsureCreated(runId);
        runs.WriteLock(runId, new RunLock
        {
            RunId = runId,
            CreatedAt = DateTimeOffset.Now,
            ExperimentId = "screen:" + Path.GetFileName(manifestPath),
            ManifestSha256 = new Dictionary<string, string> { [dataset] = sha },
            ToolVersion = Assembly.GetExecutingAssembly().GetName().Version?.ToString(3) ?? "",
            MachineName = machine?.MachineName is { Length: > 0 } name ? name : Environment.MachineName,
            Os = System.Runtime.InteropServices.RuntimeInformation.RuntimeIdentifier,
            JdkVersions = tools.JdkHomes.ToDictionary(j => j.Key.ToString(CultureInfo.InvariantCulture), j => j.Value),
            BuildFlags = BuildCommands.MavenCommonFlags(null, OperatingSystem.IsWindows()).ToList(),
        });
    }

    private Dictionary<string, LedgerEntry> LastEntries(string runId) =>
        runs.ReadLedger(runId).Where(e => e.Block == 1 && e.Job == Job)
            .GroupBy(e => e.TaskId).ToDictionary(g => g.Key, g => g.Last());

    private List<ScreeningRow> BuildRows(string runId, string dataset, IReadOnlyList<TaskRecord> tasks)
    {
        var done = LastEntries(runId);
        var rows = new List<ScreeningRow>();
        foreach (var task in tasks)
        {
            if (!done.TryGetValue(task.TaskId, out var entry))
            {
                continue;
            }

            var key = new TaskKey(dataset, task.TaskId);
            var outcome = new BlockOutcome { Outcome = entry.Outcome, Code = entry.Code, Message = entry.Message, NeedsRerun = entry.NeedsRerun };
            rows.Add(Row(dataset, task.TaskId, outcome,
                results.LoadJson<Baseline>(runId, key, ResultPaths.Baseline),
                results.LoadJson<RepositoryMetadata>(runId, key, ResultPaths.Metadata)));
        }

        return rows;
    }

    private static ScreeningRow Row(string dataset, string taskId, BlockOutcome outcome, Baseline? baseline, RepositoryMetadata? metadata) => new()
    {
        Dataset = dataset,
        TaskId = taskId,
        Outcome = outcome.Outcome,
        Code = outcome.Code,
        PairedTestStatus = baseline is null ? "" : JsonFile.Serialize(baseline.PairedTest.Status).Trim('"'),
        TestsRun = baseline?.PairedTest.TestsRun ?? [],
        JdkMajor = metadata?.Java.JdkMajor,
        Message = outcome.Message,
        NeedsRerun = outcome.NeedsRerun,
    };

    private static IReadOnlyList<string> ToCsv(ScreeningRow row) =>
    [
        row.TaskId,
        JsonFile.Serialize(row.Outcome).Trim('"'),
        row.Code ?? "",
        row.PairedTestStatus,
        string.Join(';', row.TestsRun),
        row.JdkMajor?.ToString(CultureInfo.InvariantCulture) ?? "",
        row.Message ?? "",
    ];

    private static string Describe(ScreeningRow row) =>
        row.Outcome == OutcomeKind.Ok
            ? $"OK  tiêu chí B {row.PairedTestStatus}  test {string.Join('/', row.TestsRun)}  JDK {row.JdkMajor}"
            : $"{JsonFile.Serialize(row.Outcome).Trim('"')}  {row.Code}  {row.Message}";

    private void Report(IProgress<RunEvent> progress, string runId, string? taskId, EventLevel level, string message)
    {
        var runEvent = new RunEvent { At = DateTimeOffset.Now, Level = level, Block = 1, TaskId = taskId, Job = Job, Message = message };
        runs.AppendEvent(runId, runEvent);
        if (level != EventLevel.Info)
        {
            log.Warn($"[K1] {taskId}: {message}");
        }

        progress.Report(runEvent);
    }
}
