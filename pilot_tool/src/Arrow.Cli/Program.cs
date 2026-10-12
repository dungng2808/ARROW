using System.ComponentModel;
using System.Text;
using Arrow.Cli.Logging;
using Arrow.Cli.Screens;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Infrastructure;
using Spectre.Console;
using Spectre.Console.Cli;

Console.OutputEncoding = Encoding.UTF8;
// Số trong log và màn hình luôn dùng dấu chấm thập phân, không phụ thuộc locale của máy.
System.Globalization.CultureInfo.DefaultThreadCurrentCulture = System.Globalization.CultureInfo.InvariantCulture;
System.Globalization.CultureInfo.CurrentCulture = System.Globalization.CultureInfo.InvariantCulture;
Console.InputEncoding = Encoding.UTF8;

// Log của tool: pilot_tool/logs/arrow-<ngày>.log (specs/08 §2). Không dò được pilot_tool thì bỏ qua log.
var verbose = args.Contains("--verbose");
try
{
    ToolLogSetup.Start(ToolPaths.Detect(GetRootArg(args)).ToolLogDir, verbose);
}
catch (InvalidOperationException)
{
}

ToolLog.Info($"arrow {typeof(GlobalSettings).Assembly.GetName().Version?.ToString(3)} · {Arrow.Infrastructure.Tooling.MachineInfo.RuntimeId} · .NET {Environment.Version} · lệnh: {string.Join(' ', args)}");

var app = new CommandApp<MenuCommand>();
app.Configure(config =>
{
    config.SetApplicationName("arrow");
    config.AddCommand<SetupCommand>("setup").WithDescription("Cài đặt máy (8 bước)");
    config.AddCommand<DoctorCommand>("doctor").WithDescription("Kiểm tra môi trường");
    config.AddCommand<RunCommand>("run").WithDescription("Chạy shard của máy này: S0 chuẩn bị → S1 sinh test → S2 sửa → S3 EvoSuite → S4 chất lượng (--demo: job giả)");
    config.AddCommand<StatusCommand>("status").WithDescription("Xem trạng thái một run");
    config.AddCommand<RunsCommand>("runs").WithDescription("Danh sách run trên máy");
    config.AddCommand<ControlCmd>("control").WithDescription("Tạm dừng / tiếp tục / dừng run đang chạy: control <pause|resume|stop>");
    config.AddBranch("shard", shard =>
    {
        shard.SetDescription("Chia shard (người điều phối)");
        shard.AddCommand<ShardPlanCommand>("plan").WithDescription("Chia task cho các máy");
    });
    config.AddBranch("fewshot", fewshot =>
    {
        fewshot.SetDescription("Ví dụ few-shot (người điều phối)");
        fewshot.AddCommand<FewShotSelectCommand>("select").WithDescription("Chọn 2 ví dụ few-shot cố định, ghi prompts/fewshot/examples.json");
    });
});

try
{
    var exitCode = await app.RunAsync(args);
    if (exitCode == 0)
    {
        ToolLog.Info("kết thúc, mã thoát 0");
    }
    else
    {
        ToolLog.Warn($"kết thúc, mã thoát {exitCode}");
    }
    return exitCode;
}
catch (ArrowException ex)
{
    ToolLog.Error($"{ex.Code}: {ex.Message}", ex);
    AnsiConsole.MarkupLine($"[red]{Markup.Escape(ex.Code)}[/]: {Markup.Escape(ex.Message)}");
    return 2;
}
catch (Exception ex)
{
    ToolLog.Error("lỗi không xử lý được", ex);
    AnsiConsole.MarkupLine($"[red]Lỗi:[/] {Markup.Escape(ex.Message)}");
    AnsiConsole.MarkupLine("[grey]Chi tiết trong pilot_tool/logs/. Gửi file log này khi báo lỗi.[/]");
    return 3;
}
finally
{
    ToolLogSetup.Stop();
}

static string? GetRootArg(string[] arguments)
{
    var index = Array.IndexOf(arguments, "--root");
    return index >= 0 && index + 1 < arguments.Length ? arguments[index + 1] : null;
}

/// <summary>Tùy chọn chung của mọi lệnh.</summary>
public class GlobalSettings : CommandSettings
{
    [CommandOption("--root <DIR>")]
    [Description("Thư mục pilot_tool (mặc định: tự dò)")]
    public string? Root { get; init; }

    [CommandOption("--verbose")]
    [Description("Ghi log chi tiết hơn (mức Debug) vào pilot_tool/logs/")]
    public bool Verbose { get; init; }

    public Session CreateSession() => new(ToolPaths.Detect(Root));
}

public sealed class MenuCommand : AsyncCommand<GlobalSettings>
{
    public override Task<int> ExecuteAsync(CommandContext context, GlobalSettings settings, CancellationToken cancellationToken) =>
        new MenuScreen(settings.CreateSession()).RunAsync(cancellationToken);
}

public sealed class SetupCommand : AsyncCommand<GlobalSettings>
{
    public override async Task<int> ExecuteAsync(CommandContext context, GlobalSettings settings, CancellationToken cancellationToken)
    {
        var session = settings.CreateSession();
        if (!await new SetupScreen(session).RunAsync(cancellationToken))
        {
            return 1;
        }

        var report = await new DoctorScreen(session).RunAsync(cancellationToken);
        return report.Status == "READY" ? 0 : 1;
    }
}

public sealed class DoctorCommand : AsyncCommand<GlobalSettings>
{
    public override async Task<int> ExecuteAsync(CommandContext context, GlobalSettings settings, CancellationToken cancellationToken) =>
        (await new DoctorScreen(settings.CreateSession()).RunAsync(cancellationToken)).Status == "READY" ? 0 : 1;
}

public sealed class RunCommand : AsyncCommand<RunCommand.Settings>
{
    public sealed class Settings : GlobalSettings
    {
        [CommandOption("--demo")]
        [Description("Chạy job giả để thử dashboard và tạm dừng/tiếp tục")]
        public bool Demo { get; init; }

        [CommandOption("--run-id <ID>")]
        public string? RunId { get; init; }

        [CommandOption("--jobs <N>")]
        [Description("Số job giả (mặc định: số task của shard × 3)")]
        public int? Jobs { get; init; }

        [CommandOption("--tasks <IDS>")]
        [Description("Chỉ chạy các task này (task_id cách nhau bởi dấu phẩy)")]
        public string? Tasks { get; init; }

        [CommandOption("--all-tasks")]
        [Description("Chạy mọi task của cấu hình thực nghiệm (người điều phối thử trước), không theo shard")]
        public bool AllTasks { get; init; }

        [CommandOption("--rerun-infra")]
        [Description("Chạy lại các job INFRA (lỗi hạ tầng) của run")]
        public bool RerunInfra { get; init; }

        [CommandOption("--plain")]
        [Description("Không vẽ màn hình, in từng dòng sự kiện")]
        public bool Plain { get; init; }

        [CommandOption("--experiment <FILE>")]
        [Description("Cấu hình thực nghiệm khác cấu hình của máy (ví dụ experiments/smoke-v1.json); chạy mọi task của nó")]
        public string? Experiment { get; init; }

        [CommandOption("--stages <LIST>")]
        [Description("Chỉ chạy các bước: prepare,generate,repair,evosuite,quality (mặc định: tất cả)")]
        public string? Stages { get; init; }

        [CommandOption("--fake-llm <FILE>")]
        [Description("Dùng model giả theo file luật (không gọi API, không tốn tiền)")]
        public string? FakeLlm { get; init; }

        [CommandOption("--max-llm-calls <N>")]
        [Description("Giới hạn số lượt gọi API thật; vượt thì tạm dừng run")]
        public int? MaxLlmCalls { get; init; }
    }

    public override async Task<int> ExecuteAsync(CommandContext context, Settings settings, CancellationToken cancellationToken)
    {
        if (settings.Demo)
        {
            return await new DemoRunScreen(settings.CreateSession()).RunAsync(settings.RunId, settings.Jobs, settings.Plain, cancellationToken);
        }

        var tasks = settings.Tasks?.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries).ToHashSet();
        var stages = settings.Stages?.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries).ToHashSet();
        var unknown = stages?.Where(s => !ShardRunScreen.AllStages.Contains(s)).ToList();
        if (unknown is { Count: > 0 })
        {
            throw new ArrowException("BAD_STAGE", $"Bước không có: {string.Join(", ", unknown)} (có: {string.Join(", ", ShardRunScreen.AllStages)})");
        }

        return await new ShardRunScreen(settings.CreateSession())
            .RunAsync(new ShardRunScreen.Options(settings.RunId, settings.Plain, settings.RerunInfra, tasks, settings.AllTasks,
                settings.Experiment, stages, settings.FakeLlm, settings.MaxLlmCalls), cancellationToken);
    }
}

public sealed class StatusCommand : AsyncCommand<StatusCommand.Settings>
{
    public sealed class Settings : GlobalSettings
    {
        [CommandOption("--run-id <ID>")]
        public string? RunId { get; init; }
    }

    public override Task<int> ExecuteAsync(CommandContext context, Settings settings, CancellationToken cancellationToken)
    {
        var session = settings.CreateSession();
        return new RunScreens(session).ShowStatusAsync(settings.RunId ?? session.DefaultRunId(), cancellationToken);
    }
}

public sealed class RunsCommand : Command<GlobalSettings>
{
    public override int Execute(CommandContext context, GlobalSettings settings, CancellationToken cancellationToken) =>
        new RunScreens(settings.CreateSession()).ListRuns();
}

public sealed class ControlCmd : Command<ControlCmd.Settings>
{
    public sealed class Settings : GlobalSettings
    {
        [CommandArgument(0, "<ACTION>")]
        [Description("pause | resume | stop")]
        public string Action { get; init; } = "";

        [CommandOption("--run-id <ID>")]
        public string? RunId { get; init; }
    }

    public override int Execute(CommandContext context, Settings settings, CancellationToken cancellationToken)
    {
        var command = settings.Action.ToLowerInvariant() switch
        {
            "pause" => ControlCommand.Pause,
            "resume" => ControlCommand.Resume,
            "stop" => ControlCommand.Stop,
            _ => throw new ArrowException("BAD_ACTION", "ACTION phải là pause, resume hoặc stop"),
        };
        var session = settings.CreateSession();
        return new RunScreens(session).Control(settings.RunId ?? session.DefaultRunId(), command, "cli");
    }
}

public sealed class ShardPlanCommand : Command<ShardPlanCommand.Settings>
{
    public sealed class Settings : GlobalSettings
    {
        [CommandOption("--experiment <FILE>")]
        public string? Experiment { get; init; }

        [CommandOption("--owners <LIST>")]
        [Description("Danh sách máy, ví dụ \"Dung-mac=Dũng-mac,Quang\"")]
        public string? Owners { get; init; }

        [CommandOption("--plan-id <ID>")]
        public string? PlanId { get; init; }

        [CommandOption("--out <DIR>")]
        public string? Out { get; init; }

        [CommandOption("--coordinator <ID>")]
        public string? Coordinator { get; init; }

        [CommandOption("-y|--yes")]
        public bool Yes { get; init; }
    }

    public override int Execute(CommandContext context, Settings settings, CancellationToken cancellationToken) =>
        new ShardPlanScreen(settings.CreateSession()).Run(settings.Experiment, settings.Owners, settings.PlanId, settings.Out, settings.Coordinator, settings.Yes);
}

public sealed class FewShotSelectCommand : AsyncCommand<FewShotSelectCommand.Settings>
{
    public sealed class Settings : GlobalSettings
    {
        [CommandOption("--pool-glob <GLOB>")]
        [Description("File pool preflight, tương đối so với ARROW (mặc định preflight_manifest/0*/technical_eligible_manifest.jsonl)")]
        public string PoolGlob { get; init; } = "preflight_manifest/0*/technical_eligible_manifest.jsonl";

        [CommandOption("--exclude <FILES>")]
        [Description("Manifest có repository bị loại, tương đối so với ARROW, cách nhau bởi dấu phẩy")]
        public string Exclude { get; init; } =
            "evaluation_manifest/classes2test_200_seed42.jsonl,evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl,"
            + "evaluation_manifest/pilot/classes2test_pilot_50_seed42_v2.jsonl,evaluation_manifest/pilot/classes2test_pilot_reserve_50_seed42.jsonl";

        [CommandOption("--seed <N>")]
        public int Seed { get; init; } = 42;
    }

    public override Task<int> ExecuteAsync(CommandContext context, Settings settings, CancellationToken cancellationToken) =>
        new FewShotScreen(settings.CreateSession()).RunAsync(settings.PoolGlob, settings.Exclude, settings.Seed, cancellationToken);
}
