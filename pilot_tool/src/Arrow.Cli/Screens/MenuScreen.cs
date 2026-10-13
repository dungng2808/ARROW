using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Runs;
using Arrow.Infrastructure.Llm;
using Arrow.Infrastructure.Tooling;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>Màn hình đầu và menu chính (CLI_DASHBOARD.md, SETUP.md §2).</summary>
public sealed class MenuScreen(Session session)
{
    private const string RunMine = "Chạy shard của tôi (S0 chuẩn bị → S5 báo cáo)";
    private const string RunSmoke = "Chạy smoke với model thật (2 CUT, S0 → S2, tối đa 36 lượt gọi)";
    private const string RunDemo = "Chạy thử dashboard (job giả, không build, không gọi model)";
    private const string Status = "Xem trạng thái run";
    private const string Show = "Xem kết quả run (sinh test, vòng sửa, đường cộng dồn)";
    private const string Control = "Tạm dừng / tiếp tục run đang chạy";
    private const string Setup = "Cài đặt máy";
    private const string Models = "Model, provider, khóa API, giá";
    private const string Doctor = "Kiểm tra môi trường";
    private const string Export = "Xuất kết quả để gửi người điều phối";
    private const string ShardPlan = "Chia shard";
    private const string Merge = "Gom kết quả các máy";
    private const string Report = "Báo cáo pilot";
    private const string Quit = "Thoát";

    /// <summary>Smoke với model thật (README Mục 0): 2 CUT Classes2Test của pilot, cấu hình experiments/smoke-v1.json (n_max 2).</summary>
    private const string SmokeExperiment = "experiments/smoke-v1.json";
    private static readonly string[] SmokeTasks = ["107165228_0924785415323560", "159422409_9d630c4556038ca7"];

    public async Task<int> RunAsync(CancellationToken cancellationToken)
    {
        if (session.Machine is null)
        {
            AnsiConsole.Clear();
            Theme.Logo();
            AnsiConsole.Write(new Panel(new Markup(
                    "Không tìm thấy [bold]pilot_tool/machine.local.json[/].\nCần cài đặt một lần: chọn máy, shard, thư mục, JDK, khóa API (8 bước)."))
                .Header(" Chưa cài đặt máy này ").Border(BoxBorder.Rounded).Expand());
            if (AnsiConsole.Prompt(new SelectionPrompt<string>().AddChoices("Bắt đầu cài đặt", Quit)) == Quit)
            {
                return 0;
            }

            if (!await new SetupScreen(session).RunAsync(cancellationToken))
            {
                return 1;
            }

            await new DoctorScreen(session).RunAsync(cancellationToken);
            WaitKey();
        }

        while (true)
        {
            AnsiConsole.Clear();
            Theme.Logo();
            RenderHome();
            var choice = AnsiConsole.Prompt(new SelectionPrompt<string>()
                .Title("Chọn việc ([grey]↑/↓, Enter[/]):")
                .PageSize(16)
                .AddChoices(RunMine, RunSmoke, RunDemo, Status, Show, Control, Setup, Models, Doctor, Export)
                .AddChoiceGroup("── Người điều phối ──", ShardPlan, Merge, Report)
                .AddChoices(Quit));

            AnsiConsole.Clear();
            switch (choice)
            {
                case Quit:
                    return 0;
                case RunMine:
                    try
                    {
                        await new ShardRunScreen(session).RunAsync(new ShardRunScreen.Options(null, false, false, null), cancellationToken);
                    }
                    catch (Arrow.Core.Util.ArrowException ex)
                    {
                        AnsiConsole.MarkupLine($"[red]{Markup.Escape(ex.Code)}[/]: {Markup.Escape(ex.Message)}");
                    }

                    break;
                case RunSmoke:
                {
                    var runId = $"smoke-{DateTime.Now:yyyyMMdd-HHmm}";
                    AnsiConsole.MarkupLine($"[grey]Smoke gọi GPT-6 Luna thật qua LiteLLM: {SmokeTasks.Length} CUT × 3 quy trình, sửa tối đa 2 vòng × 2 cơ chế; run [bold]{runId}[/]. Lỗi khóa/ví/định tuyến của provider làm run tự tạm dừng: bấm S để dừng.[/]");
                    if (!AnsiConsole.Confirm("Bắt đầu?"))
                    {
                        break;
                    }

                    try
                    {
                        await new ShardRunScreen(session).RunAsync(new ShardRunScreen.Options(runId, false, false, SmokeTasks,
                            ExperimentPath: SmokeExperiment, Stages: ["prepare", "generate", "repair"], MaxLlmCalls: 36), cancellationToken);
                    }
                    catch (Arrow.Core.Util.ArrowException ex)
                    {
                        AnsiConsole.MarkupLine($"[red]{Markup.Escape(ex.Code)}[/]: {Markup.Escape(ex.Message)}");
                    }

                    break;
                }
                case Show:
                    await PickRunAsync(runId => Task.FromResult(Guard(() => new ReportScreens(session).Show(runId, null, null))));
                    break;
                case Export:
                    await PickRunAsync(runId => Task.FromResult(Guard(() => new ReportScreens(session).Export(runId, null))));
                    break;
                case Report:
                    await PickRunAsync(runId => Task.FromResult(Guard(() => new ReportScreens(session).Report(runId, null))));
                    break;
                case Merge:
                {
                    var planId = session.TryLoadShard()?.PlanId ?? "pilot-6";
                    var plan = AnsiConsole.Prompt(new TextPrompt<string>("File plan.json:").DefaultValue($"../shards/{planId}/plan.json"));
                    var bundles = AnsiConsole.Prompt(new TextPrompt<string>("Thư mục chứa các gói .zip:").DefaultValue($"../pilot_results/{planId}"));
                    var runId = AnsiConsole.Prompt(new TextPrompt<string>("Tên run gom:").DefaultValue($"{planId}-merged"));
                    Guard(() => new ReportScreens(session).Merge(session.Paths.Resolve(plan), session.Paths.Resolve(bundles), runId, null));
                    break;
                }
                case RunDemo:
                    await new DemoRunScreen(session).RunAsync(null, null, plain: false, cancellationToken);
                    break;
                case Status:
                    await PickRunAsync(runId => new RunScreens(session).ShowStatusAsync(runId, cancellationToken));
                    break;
                case Control:
                    await PickRunAsync(runId =>
                    {
                        var command = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("Lệnh:").AddChoices("Tạm dừng", "Tiếp tục", "Dừng hẳn"));
                        var control = command switch { "Tạm dừng" => ControlCommand.Pause, "Tiếp tục" => ControlCommand.Resume, _ => ControlCommand.Stop };
                        return Task.FromResult(new RunScreens(session).Control(runId, control, "menu"));
                    });
                    break;
                case Setup:
                    if (await new SetupScreen(session).RunAsync(cancellationToken))
                    {
                        await new DoctorScreen(session).RunAsync(cancellationToken);
                    }

                    break;
                case Doctor:
                    await new DoctorScreen(session).RunAsync(cancellationToken);
                    break;
                case Models:
                    await new ModelScreen(session).RunAsync(null, cancellationToken);
                    continue;
                case ShardPlan:
                    new ShardPlanScreen(session).Run(null, null, null, null, null, assumeYes: false);
                    break;
            }

            WaitKey();
        }
    }

    private void RenderHome()
    {
        var machine = session.Machine!;
        var shard = session.TryLoadShard();
        var litellm = LiteLlmConfigFile.TryLoad(session.Paths.LiteLlmConfigPath);
        var doctor = machine.LastDoctor is null
            ? $"{Theme.Warn} chưa kiểm tra"
            : machine.LastDoctor.Status == "READY"
                ? $"{Theme.Ok} READY ({machine.LastDoctor.At.ToLocalTime():dd/MM HH:mm})"
                : $"{Theme.Fail} {machine.LastDoctor.Status}";
        var workDir = session.Paths.Resolve(machine.WorkDir);
        var free = MachineInfo.FreeGb(workDir);
        var grid = new Grid().AddColumn().AddColumn().AddColumn().AddColumn();
        grid.AddRow("Máy", Markup.Escape(machine.MachineName), "Shard",
            shard is null ? $"{Theme.Warn} chưa có" : Markup.Escape($"{shard.PlanId}/{shard.ShardId} ({shard.Tasks.Count} CUT)"));
        grid.AddRow("Thực nghiệm", Markup.Escape(Path.GetFileNameWithoutExtension(machine.Experiment)), "Môi trường", doctor);
        grid.AddRow("Model", ModelStatus(machine, litellm),
            "Thư mục repo", Markup.Escape(workDir) + (free < 20 ? $" [yellow]{free} GB ⚠[/]" : $" [grey]{free} GB trống[/]"));
        AnsiConsole.Write(new Panel(grid).Header(" Máy này ").Border(BoxBorder.Rounded).Expand());

        var runs = RunStore.ListRuns(session.RunsDir).Select(r => (r.RunId, Status: r.ReadStatus())).Where(r => r.Status is not null).ToList();
        if (runs.Count > 0)
        {
            var now = DateTimeOffset.UtcNow;
            var lines = runs.TakeLast(3).Select(r =>
            {
                var stage = r.Status!.Stages.FirstOrDefault(s => s.Stage == r.Status.Stage);
                var alive = r.Status.IsAlive(now) ? "" : r.Status.State is RunState.Done or RunState.Stopped ? "" : " [grey](không còn chạy)[/]";
                return $"{Markup.Escape(r.RunId)}   {r.Status.State.ToString().ToUpperInvariant()}{alive}   {Markup.Escape(r.Status.Stage)} {stage?.Done}/{stage?.Total}   đã chạy {Theme.Duration(r.Status.ElapsedSeconds)}";
            });
            AnsiConsole.Write(new Panel(new Markup(string.Join("\n", lines))).Header(" Run gần đây ").Border(BoxBorder.Rounded).Expand());
        }
    }

    /// <summary>Mỗi model của thực nghiệm: đã có provider và khóa trên máy này chưa.</summary>
    private string ModelStatus(MachineConfig machine, LiteLlmConfigFile? litellm)
    {
        try
        {
            var models = Arrow.Core.Config.ExperimentLoader.Load(session.Paths.Resolve(machine.Experiment)).Config.Models;
            return string.Join("\n", models.Select(m => litellm?.Route(m.Name) is { IsComplete: true } r
                ? $"{Theme.Ok} {Markup.Escape(m.Name)} [grey]{Markup.Escape(r.EndpointHost)}[/]"
                : $"{Theme.Fail} {Markup.Escape(m.Name)} [grey]chưa có provider/khóa[/]"));
        }
        catch (Exception ex) when (ex is Arrow.Core.Util.ArrowException or IOException)
        {
            return $"{Theme.Warn} không đọc được cấu hình thực nghiệm";
        }
    }

    /// <summary>Lỗi có mã (run thiếu, gói sai…) in ra rồi về menu, không thoát chương trình.</summary>
    private static int Guard(Func<int> action)
    {
        try
        {
            return action();
        }
        catch (Arrow.Core.Util.ArrowException ex)
        {
            AnsiConsole.MarkupLine($"[red]{Markup.Escape(ex.Code)}[/]: {Markup.Escape(ex.Message)}");
            return 2;
        }
    }

    private async Task PickRunAsync(Func<string, Task<int>> action)
    {
        var runs = RunStore.ListRuns(session.RunsDir).Select(r => r.RunId).ToList();
        if (runs.Count == 0)
        {
            AnsiConsole.MarkupLine("[grey]Chưa có run nào.[/]");
            return;
        }

        var runId = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("Run:").AddChoices(runs.AsEnumerable().Reverse()));
        await action(runId);
    }

    private static void WaitKey()
    {
        if (Console.IsInputRedirected)
        {
            return;
        }

        AnsiConsole.MarkupLine("\n[grey]Nhấn phím bất kỳ để về menu…[/]");
        Console.ReadKey(intercept: true);
    }
}
