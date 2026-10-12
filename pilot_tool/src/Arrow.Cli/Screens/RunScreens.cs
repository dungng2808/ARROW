using Arrow.Cli.Ui;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>Danh sách run, xem trạng thái, điều khiển (CLI_DASHBOARD.md §1, §5).</summary>
public sealed class RunScreens(Session session)
{
    public int ListRuns()
    {
        var runs = RunStore.ListRuns(session.RunsDir).ToList();
        if (runs.Count == 0)
        {
            AnsiConsole.MarkupLine($"[grey]Chưa có run nào trong {Markup.Escape(session.RunsDir)}[/]");
            return 0;
        }

        var now = DateTimeOffset.UtcNow;
        var table = new Table().Border(TableBorder.Rounded)
            .AddColumns("Run", "Bước hiện tại", "Trạng thái", "Bắt đầu", "Đã chạy");
        foreach (var run in runs)
        {
            var status = run.ReadStatus();
            if (status is null)
            {
                table.AddRow(Markup.Escape(run.RunId), "–", "[grey]chưa có status[/]", "", "");
                continue;
            }

            var stage = status.Stages.FirstOrDefault(s => s.Stage == status.Stage);
            var progress = stage is null ? status.Stage : $"{status.Stage} {stage.Done}/{stage.Total}";
            var state = status.State.ToString().ToUpperInvariant() + (status.IsAlive(now) || status.State is RunState.Done or RunState.Stopped or RunState.Failed ? "" : " [grey](không còn chạy)[/]");
            table.AddRow(Markup.Escape(run.RunId), Markup.Escape(progress), state,
                status.Since.ToLocalTime().ToString("dd/MM HH:mm"), Theme.Duration(status.ElapsedSeconds));
        }

        AnsiConsole.Write(table);
        return 0;
    }

    /// <summary>Màn hình chỉ xem; cập nhật trực tiếp nếu run đang chạy; Q để thoát.</summary>
    public async Task<int> ShowStatusAsync(string runId, CancellationToken cancellationToken)
    {
        var store = session.Store(runId);
        if (store.ReadStatus() is null)
        {
            AnsiConsole.MarkupLine($"[red]Không có run {Markup.Escape(runId)} trong {Markup.Escape(session.RunsDir)}[/]");
            return 1;
        }

        var header = new DashboardView.Header(runId, "", "", session.Machine?.MachineName ?? "");
        IRenderableFactory render = () =>
        {
            var status = store.ReadStatus()!;
            return DashboardView.Render(header, status, store.ReadRecentEvents(5), interactive: false, alive: status.IsAlive(DateTimeOffset.UtcNow));
        };

        if (Console.IsInputRedirected || Console.IsOutputRedirected)
        {
            AnsiConsole.Write(render());
            return 0;
        }

        await AnsiConsole.Live(render()).StartAsync(async context =>
        {
            while (!cancellationToken.IsCancellationRequested)
            {
                if (Console.KeyAvailable && char.ToLowerInvariant(Console.ReadKey(intercept: true).KeyChar) == 'q')
                {
                    break;
                }

                context.UpdateTarget(render());
                await Task.Delay(500, cancellationToken).ConfigureAwait(false);
            }
        });
        return 0;
    }

    public int Control(string runId, ControlCommand command, string requestedBy)
    {
        var store = session.Store(runId);
        var status = store.ReadStatus();
        if (status is null)
        {
            AnsiConsole.MarkupLine($"[red]Không có run {Markup.Escape(runId)}[/]");
            return 1;
        }

        if (!status.IsAlive(DateTimeOffset.UtcNow))
        {
            AnsiConsole.MarkupLine($"[yellow]Run {Markup.Escape(runId)} không còn chạy ({status.State}). Lệnh vẫn được ghi nhưng chỉ có tác dụng khi run chạy.[/]");
        }

        ControlGate.Request(store, command, requestedBy);
        ToolLog.Info($"gửi lệnh {command.ToString().ToLowerInvariant()} tới run {runId} (từ {requestedBy})");
        var text = command switch
        {
            ControlCommand.Pause => "Đã gửi lệnh tạm dừng: run làm nốt bước đang chạy rồi chờ.",
            ControlCommand.Resume => "Đã gửi lệnh tiếp tục.",
            _ => "Đã gửi lệnh dừng hẳn: run làm nốt bước đang chạy rồi thoát.",
        };
        AnsiConsole.MarkupLine($"{Theme.Ok} {text}");
        return 0;
    }

    private delegate Spectre.Console.Rendering.IRenderable IRenderableFactory();
}
