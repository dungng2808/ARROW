using Arrow.Core.Runs;
using Spectre.Console;
using Spectre.Console.Rendering;

namespace Arrow.Cli.Ui;

/// <summary>Màn hình dashboard trong terminal (CLI_DASHBOARD.md §2–§3). Chỉ vẽ từ dữ liệu có sẵn.</summary>
public static class DashboardView
{
    public sealed record Header(string RunId, string Dataset, string Model, string Machine);

    private static readonly Dictionary<string, string> StageNames = new()
    {
        ["prepare"] = "S0 Chuẩn bị",
        ["generate"] = "S1 Sinh test",
        ["repair"] = "S2 Sửa test",
        ["evosuite"] = "S3 EvoSuite",
        ["quality"] = "S4 Chất lượng",
        ["demo"] = "Demo",
    };

    public static IRenderable Render(Header header, RunStatus status, IReadOnlyList<RunEvent> events, bool interactive, bool alive = true)
    {
        var width = Math.Max(60, Console.IsOutputRedirected ? 120 : Console.WindowWidth);
        var compact = width < 100;
        var rows = new List<IRenderable>
        {
            HeaderPanel(header, status, alive),
            ProgressPanel(status, compact ? 18 : 30),
        };

        if (compact)
        {
            rows.Add(OutcomesPanel(status));
        }
        else
        {
            var grid = new Grid().AddColumn().AddColumn();
            grid.AddRow(OutcomesPanel(status), new Rows(RunningPanel(status), CostPanel(status)));
            rows.Add(grid);
            rows.Add(EventsPanel(events));
        }

        rows.Add(new Markup(KeyHints(status.State, interactive)));
        return new Rows(rows);
    }

    private static IRenderable HeaderPanel(Header header, RunStatus status, bool alive)
    {
        var state = status.State switch
        {
            RunState.Running => "[green]● RUNNING[/]",
            RunState.Pausing => "[yellow]⏸ PAUSING – đang chờ bước đang chạy xong[/]",
            RunState.Paused => "[yellow]⏸ PAUSED[/]",
            RunState.Stopping => "[red]■ STOPPING – đang chờ bước đang chạy xong[/]",
            RunState.Stopped => "[red]■ STOPPED[/]",
            RunState.Done => "[green]✔ DONE[/]",
            _ => "[red]✘ FAILED[/]",
        };
        if (!alive && status.State is RunState.Running or RunState.Pausing or RunState.Paused or RunState.Stopping)
        {
            state += " [grey](không còn chạy)[/]";
        }

        var text = new Markup(
            $"Run: [bold]{Markup.Escape(header.RunId)}[/]    Dataset: {Markup.Escape(header.Dataset)}    Model: {Markup.Escape(header.Model)}\n" +
            $"Trạng thái: {state}    Đã chạy: {Theme.Duration(status.ElapsedSeconds)}    Tạm dừng: {Theme.Duration(status.PausedSeconds)}    Máy: {Markup.Escape(header.Machine)}");
        return new Panel(text).Header(" ARROW pilot ").Border(BoxBorder.Rounded).Expand();
    }

    private static IRenderable ProgressPanel(RunStatus status, int barWidth)
    {
        var lines = new List<string>();
        foreach (var stage in status.Stages)
        {
            var name = StageNames.GetValueOrDefault(stage.Stage, stage.Stage);
            var percent = stage.Total == 0 ? 0 : (int)(100.0 * stage.Done / stage.Total);
            var tail = stage.Done >= stage.Total && stage.Total > 0 ? "[green]✔[/]" : $"{percent}%";
            var infra = stage.Infra > 0 ? $"  [grey](lỗi hạ tầng: {stage.Infra})[/]" : "";
            lines.Add($"{Markup.Escape(name),-14} {Theme.Bar(stage.Done, stage.Total, barWidth)}  {stage.Done}/{stage.Total}  {tail}{infra}");
        }

        if (lines.Count == 0)
        {
            lines.Add("[grey]Chưa có bước nào chạy[/]");
        }

        return new Panel(new Markup(string.Join("\n", lines))).Header(" Tiến độ ").Border(BoxBorder.Rounded).Expand();
    }

    private static IRenderable OutcomesPanel(RunStatus status)
    {
        var total = status.Outcomes.Values.Sum();
        var chart = new BarChart().Width(46);
        foreach (var outcome in Theme.OutcomeOrder)
        {
            chart.AddItem(outcome, status.Outcomes.GetValueOrDefault(outcome), Theme.OutcomeColor(outcome));
        }

        return new Panel(chart).Header($" Kết quả xác minh ({total} ứng viên) ").Border(BoxBorder.Rounded);
    }

    private static IRenderable RunningPanel(RunStatus status)
    {
        var now = DateTimeOffset.UtcNow;
        var lines = status.RunningJobs.Count == 0
            ? ["[grey]Không có bước nào đang chạy[/]"]
            : status.RunningJobs.Select(j =>
                $"{Markup.Escape(j.Worker),-6} {Markup.Escape(Short(j.TaskId)),-16} {Markup.Escape(j.Label),-12} [[{Markup.Escape(j.Step)}]] {(int)(now - j.Since).TotalSeconds}s").ToList();
        return new Panel(new Markup(string.Join("\n", lines))).Header(" Đang chạy ").Border(BoxBorder.Rounded).Expand();
    }

    private static IRenderable CostPanel(RunStatus status) =>
        new Panel(new Markup(
                $"Token: {status.PromptTokens / 1000.0:0.#}k vào / {status.CompletionTokens / 1000.0:0.#}k ra   ${status.CostUsd:0.00}"))
            .Header(" Chi phí ").Border(BoxBorder.Rounded).Expand();

    private static IRenderable EventsPanel(IReadOnlyList<RunEvent> events)
    {
        var lines = events.Count == 0
            ? ["[grey]Chưa có sự kiện[/]"]
            : events.TakeLast(5).Select(e =>
            {
                var color = e.Outcome is null ? "grey" : Theme.OutcomeColor(e.Outcome).ToMarkup();
                return $"{e.At.ToLocalTime():HH:mm:ss}  {Markup.Escape(Short(e.TaskId ?? "")),-16} {Markup.Escape(e.Strategy ?? ""),-12} → [{color}]{Markup.Escape(e.Outcome ?? "")}[/]  {Markup.Escape(e.Message)}";
            }).ToList();
        return new Panel(new Markup(string.Join("\n", lines))).Header(" Sự kiện gần nhất ").Border(BoxBorder.Rounded).Expand();
    }

    private static string KeyHints(RunState state, bool interactive)
    {
        if (!interactive)
        {
            return " [grey][[Q]] Thoát màn hình xem (không ảnh hưởng run)[/]";
        }

        return state switch
        {
            RunState.Pausing or RunState.Paused => " [bold][[R]][/] Tiếp tục   [bold][[S]][/] Dừng hẳn",
            RunState.Stopping => " [grey]Đang dừng…[/]",
            _ => " [bold][[P]][/] Tạm dừng   [bold][[R]][/] Tiếp tục   [bold][[S]][/] Dừng hẳn   [grey](Ctrl+C = S)[/]",
        };
    }

    private static string Short(string taskId) => taskId.Length > 15 ? taskId[..14] + "…" : taskId;
}
