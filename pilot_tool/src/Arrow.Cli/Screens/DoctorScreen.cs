using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Util;
using Arrow.Infrastructure.Doctor;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>
/// Kiểm tra môi trường (SETUP.md §6); ghi kết quả vào machine.local.json. Mục ✘ nào sửa tự động được
/// (cài LiteLLM, nhập khóa API, chạy lại cài đặt) thì hỏi người dùng rồi sửa và kiểm tra lại.
/// </summary>
public sealed class DoctorScreen(Session session)
{
    public async Task<DoctorReport> RunAsync(CancellationToken cancellationToken, bool offerFixes = true)
    {
        var report = await CheckAsync(cancellationToken);
        if (!offerFixes || Console.IsInputRedirected)
        {
            return report;
        }

        var fixes = report.Checks.Where(c => c.Status == CheckStatus.Fail && c.Fix != DoctorFix.None)
            .Select(c => c.Fix).Distinct().ToList();
        if (fixes.Count == 0)
        {
            return report;
        }

        AnsiConsole.WriteLine();
        if (fixes.Contains(DoctorFix.RunSetup))
        {
            if (AnsiConsole.Confirm("Một số mục cần sửa trong phần cài đặt (cấu hình máy hoặc JDK). Mở [bold]cài đặt[/] ngay?"))
            {
                // Cài đặt xong sẽ tự kiểm tra lại (kèm đề nghị sửa các mục còn lại).
                return await new SetupScreen(session).RunAsync(cancellationToken)
                    ? await RunAsync(cancellationToken)
                    : report;
            }

            fixes.Remove(DoctorFix.RunSetup);
        }

        var changed = false;
        if (fixes.Contains(DoctorFix.InstallLiteLlm) && AnsiConsole.Confirm("Máy chưa có LiteLLM. [bold]Cài ngay[/] vào ARROW/litellm?"))
        {
            changed |= await FixActions.InstallLiteLlmAsync(session, cancellationToken);
        }

        if (fixes.Contains(DoctorFix.EnterApiKey) && AnsiConsole.Confirm("Máy chưa có khóa API. [bold]Nhập khóa ngay[/]?"))
        {
            FixActions.EnterApiKey(session);
            changed = true;
        }

        if (fixes.Contains(DoctorFix.InstallTools) && AnsiConsole.Confirm("Thiếu công cụ của S3/S4 (EvoSuite, JaCoCo, PIT, tsDetect). [bold]Tải ngay[/] vào ARROW/tools?"))
        {
            changed |= await FixActions.InstallToolsAsync(session, cancellationToken) == 0;
        }

        if (!changed)
        {
            return report;
        }

        AnsiConsole.WriteLine();
        AnsiConsole.MarkupLine("[grey]Kiểm tra lại sau khi sửa…[/]");
        return await CheckAsync(cancellationToken);
    }

    /// <summary>Khung "cần làm" cho một mục ✘/⚠: vấn đề, vì sao, từng bước sửa.</summary>
    public static void ShowSteps(DoctorCheck check)
    {
        var fail = check.Status == CheckStatus.Fail;
        var lines = new List<string> { $"[bold]{Markup.Escape(check.Detail)}[/]", "" };
        var number = 0;
        foreach (var step in check.Steps!)
        {
            lines.Add(step.StartsWith("Vì sao:", StringComparison.Ordinal)
                ? $"[grey]{Markup.Escape(step)}[/]"
                : $"[bold]{++number}.[/] {Markup.Escape(step)}");
        }

        AnsiConsole.Write(new Panel(new Markup(string.Join('\n', lines)))
            .Header(fail ? $"[red] ✘ {Markup.Escape(check.Name)}: cần làm [/]" : $"[yellow] ⚠ {Markup.Escape(check.Name)}: nên làm [/]")
            .Border(BoxBorder.Rounded)
            .BorderColor(fail ? Color.Red : Color.Yellow)
            .Expand());
        ToolLog.Warn($"doctor: {check.Name}: {check.Detail} | {string.Join(" | ", check.Steps!)}");
    }

    private async Task<DoctorReport> CheckAsync(CancellationToken cancellationToken)
    {
        AnsiConsole.Write(new Rule("[bold]Kiểm tra môi trường[/]").LeftJustified());
        var table = new Table().Border(TableBorder.Rounded).AddColumns("Kiểm tra", "", "Chi tiết");
        DoctorReport? report = null;
        if (!AnsiConsole.Profile.Capabilities.Interactive)
        {
            report = await new DoctorService(session.Paths).RunAsync(check =>
            {
                var icon = check.Status switch
                {
                    CheckStatus.Pass => Theme.Ok,
                    CheckStatus.Warn => Theme.Warn,
                    CheckStatus.Fail => Theme.Fail,
                    _ => Theme.Skip,
                };
                var detail = Markup.Escape(check.Detail) + (check.Hint is null || check.Status == CheckStatus.Pass ? "" : $"\n[grey]→ {Markup.Escape(check.Hint)}[/]");
                table.AddRow(Markup.Escape(check.Name), icon, detail);
                ToolLog.Info($"doctor: {check.Name}: {check.Status.ToString().ToUpperInvariant()} · {check.Detail}");
            }, cancellationToken);
            AnsiConsole.Write(table);
        }
        else
        {
            await AnsiConsole.Live(table).StartAsync(async context =>
            {
                report = await new DoctorService(session.Paths).RunAsync(check =>
                {
                    var icon = check.Status switch
                    {
                        CheckStatus.Pass => Theme.Ok,
                        CheckStatus.Warn => Theme.Warn,
                        CheckStatus.Fail => Theme.Fail,
                        _ => Theme.Skip,
                    };
                    var detail = Markup.Escape(check.Detail) + (check.Hint is null || check.Status == CheckStatus.Pass ? "" : $"\n[grey]→ {Markup.Escape(check.Hint)}[/]");
                    table.AddRow(Markup.Escape(check.Name), icon, detail);
                    ToolLog.Info($"doctor: {check.Name}: {check.Status.ToString().ToUpperInvariant()} · {check.Detail}");
                    context.Refresh();
                }, cancellationToken);
            });
        }

        var ready = report!.Status == "READY";
        ToolLog.Info($"doctor: kết quả {report.Status}");
        foreach (var check in report.Checks.Where(c => c.Steps is { Count: > 0 } && c.Status is CheckStatus.Fail or CheckStatus.Warn))
        {
            ShowSteps(check);
        }

        AnsiConsole.MarkupLine(ready ? "Trạng thái: [bold green]READY[/]" : "Trạng thái: [bold red]NOT_READY[/] – còn mục ✘.");
        var machine = session.Machine;
        if (machine is not null)
        {
            (machine with { LastDoctor = new DoctorStamp { At = DateTimeOffset.UtcNow, Status = report.Status } }).Save(session.Paths.MachineConfigPath);
        }

        return report;
    }
}
