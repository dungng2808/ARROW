using Arrow.Models.Common;
using Arrow.Models.Run;
using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>
/// S10 (spec 06 Mục 12): <c>arrow-fw screen --manifest F --run-id R [--dataset D] [--tasks a,b|FILE] [--workers N] [--rerun-infra] [--keep-workspace]</c>.
/// In mỗi sự kiện của khối 1 một dòng, cuối cùng in bảng tổng theo tiêu chí B.
/// </summary>
public sealed class ScreeningScreen(IScreeningService screening, ConsoleInput input, ConsoleOutput output)
{
    /// <summary>In sự kiện ngay trên luồng gọi (không qua SynchronizationContext); <see cref="ConsoleOutput"/> có khóa.</summary>
    private sealed class LineProgress(ConsoleOutput output) : IProgress<RunEvent>
    {
        public void Report(RunEvent value)
        {
            var mark = value.Level == EventLevel.Info ? "" : value.Level == EventLevel.Warn ? "[!] " : "[X] ";
            output.Line($"{value.At.ToLocalTime():HH:mm:ss} [K1] {(value.TaskId is null ? "" : value.TaskId + "  ")}{mark}{value.Message}");
        }
    }

    public async Task<int> RunAsync(CommandLine command)
    {
        var manifest = command.Option("manifest") ?? input.Ask("Manifest (.jsonl)");
        var runId = command.Option("run-id") ?? input.Ask("Run id", $"screen-{DateTime.Now:yyyyMMdd-HHmm}");
        var request = new ScreeningRequest
        {
            ManifestPath = Path.GetFullPath(manifest),
            Dataset = command.Option("dataset"),
            RunId = runId,
            TaskSelection = command.Option("tasks"),
            Workers = command.Option("workers") is { } w ? int.TryParse(w, out var n) && n > 0 ? n : throw new ArgumentException("--workers phải là số nguyên dương") : null,
            RerunInfra = command.Flag("rerun-infra"),
            KeepWorkspace = command.Flag("keep-workspace"),
        };

        using var cancel = new CancellationTokenSource();
        void OnCancel(object? sender, ConsoleCancelEventArgs e)
        {
            e.Cancel = true;
            output.Line("Đang dừng: hủy các task đang chạy; task đã ghi ledger được giữ, chạy lại cùng lệnh để tiếp tục.");
            cancel.Cancel();
        }

        Console.CancelKeyPress += OnCancel;
        ScreeningSummary summary;
        try
        {
            summary = await screening.RunAsync(request, new LineProgress(output), cancel.Token);
        }
        catch (OperationCanceledException)
        {
            output.Status(Mark.Warn, "Đã dừng", $"chạy lại: arrow-fw screen --manifest {manifest} --run-id {runId}");
            return 130;
        }
        finally
        {
            Console.CancelKeyPress -= OnCancel;
        }

        PrintSummary(summary);
        return 0;
    }

    private void PrintSummary(ScreeningSummary summary)
    {
        output.Blank();
        output.Title($"Sang loc · {Path.GetFileName(summary.ManifestPath)} · run {summary.RunId}");
        var ok = summary.Rows.Where(r => r.Outcome == OutcomeKind.Ok).ToList();
        string[] statuses = ["PASS", "NO_TEST_RUN", "FAILING", "FLAKY", "NO_PAIRED_TEST"];
        var table = statuses
            .Select(s => (IReadOnlyList<string>)[s == "PASS" ? "PASS (dat tieu chi B)" : s, ok.Count(r => r.PairedTestStatus == s).ToString()])
            .Where(r => r[0].StartsWith("PASS", StringComparison.Ordinal) || r[1] != "0")
            .ToList();
        var excluded = summary.Rows.Where(r => r.Outcome == OutcomeKind.Excluded).ToList();
        var infra = summary.Rows.Where(r => r.Outcome == OutcomeKind.Infra).ToList();
        table.Add(["EXCLUDED (build...)", excluded.Count.ToString()]);
        table.Add(["INFRA (can chay lai)", infra.Count.ToString()]);
        output.Table(["Ket qua", "CUT"], table);

        foreach (var row in summary.Rows.Where(r => r.Outcome != OutcomeKind.Ok || r.PairedTestStatus != "PASS"))
        {
            output.Line($"  {row.TaskId}  {(row.Outcome == OutcomeKind.Ok ? row.PairedTestStatus : row.Code)}  {row.Message}");
        }

        output.Line($"Da ghi {summary.CsvPath}");
        if (infra.Count > 0)
        {
            output.Line($"Chay lai task INFRA: arrow-fw screen --manifest {summary.ManifestPath} --run-id {summary.RunId} --rerun-infra");
        }

        output.Line("Buoc tiep theo (ngoai framework): preflight_tool/scripts/select_pilot_after_s0.py ... de thay CUT khong dat");
    }
}
