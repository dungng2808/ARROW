using Arrow.Models.Run;
using Arrow.Service;
using Arrow.UI.Screens;

namespace Arrow.UI;

/// <summary>Chọn màn hình theo lệnh (spec 00 Mục 5.1, spec 06 Mục 2). Không chứa quy tắc nghiệp vụ.</summary>
public sealed class Commands(ArrowServices services, ConsoleInput input, ConsoleOutput output, string version, bool interactive)
{
    /// <summary>Lệnh nhận từ đứng sau tùy chọn.</summary>
    private static readonly HashSet<string> TakesTrailing = ["control"];

    public async Task<int> RunAsync(CommandLine command)
    {
        if (command.Trailing.Count > 0 && (command.Command.Count == 0 || !TakesTrailing.Contains(command.Command[0])))
        {
            throw new ArgumentException($"Tham số không hợp lệ: {string.Join(' ', command.Trailing)}");
        }

        var name = string.Join(' ', command.Command);
        switch (name)
        {
            case "":
                return await new MenuScreen(services, input, output, version, interactive).RunAsync();
            case "prompts check":
                return new PromptCheckScreen(services.Prompts, output).Run();
            case "screen":
                return await new ScreeningScreen(services.Screening, input, output).RunAsync(command);
            case "run":
                return await Run(command, null);
            case "analyze":
                return await Run(command, [1]);
            case "generate":
                return await Run(command, [2]);
            case "repair":
                return await Run(command, [3]);
            case "assess":
                return await Run(command, [4]);
            case "report":
                return new ReportScreen(services.Reports, output).Run(command.Options("run-id") is { Count: > 0 } ids ? ids : [Require(command, "run-id")]);
            case "status":
                return new StatusScreen(services.Runs, output).Run(Require(command, "run-id"));
            case "show":
                return new TaskScreen(services.Runs, input, output, interactive).Run(Require(command, "run-id"), Require(command, "task"));
            case "rerun-list":
            {
                var runId = Require(command, "run-id");
                return new RerunScreen(services.Runs, input, output, interactive).Run(runId) is { } rerun
                    ? await new RunScreen(services.Pipeline, input, output, interactive).RunAsync(rerun with { ExperimentPath = command.Option("experiment") })
                    : 0;
            }

            case "control":
                return Control(command);
            case "doctor":
                return await new DoctorScreen(services.Doctor, input, output, interactive).RunAsync(command.Option("experiment"));
            case "tools install":
                return await new DoctorScreen(services.Doctor, input, output, interactive).RunAsync(command.Option("experiment"), fixWithoutAsking: true);
            case "setup":
                return await new SetupScreen(services.Setup, new ModelScreen(services.Models, input, output), new DoctorScreen(services.Doctor, input, output, interactive), input, output).RunAsync();
            case "models":
                return await new ModelScreen(services.Models, input, output).RunAsync(command.Option("experiment"));
            default:
                output.Status(Mark.Fail, $"Lệnh không hỗ trợ: {name}", "xem arrow-fw --help");
                return 2;
        }
    }

    private async Task<int> Run(CommandLine command, List<int>? blocks)
    {
        var request = new RunRequest
        {
            ExperimentPath = command.Option("experiment"),
            RunId = command.Option("run-id"),
            TaskSelection = command.Option("tasks"),
            Blocks = blocks ?? (command.Option("blocks") is { } b ? ParseBlocks(b) : [1, 2, 3, 4]),
            RerunInfra = command.Flag("rerun-infra"),
            MaxLlmCalls = command.Option("max-llm-calls") is { } max ? int.TryParse(max, out var n) && n > 0 ? n : throw new ArgumentException("--max-llm-calls phải là số nguyên dương") : null,
            FakeLlm = command.Flag("fake-llm"),
        };
        return await new RunScreen(services.Pipeline, input, output, interactive).RunAsync(request);
    }

    private int Control(CommandLine command)
    {
        var runId = Require(command, "run-id");
        var word = command.Trailing.FirstOrDefault() ?? command.Option("action") ?? throw new ArgumentException("Cần pause, resume hoặc stop: arrow-fw control --run-id R pause");
        var action = word switch
        {
            "pause" => ControlAction.Pause,
            "resume" => ControlAction.Resume,
            "stop" => ControlAction.Stop,
            _ => throw new ArgumentException($"Lệnh điều khiển không hợp lệ: {word} (pause|resume|stop)"),
        };
        services.Runs.SendControl(runId, action);
        output.Status(Mark.Ok, $"Đã gửi {word} cho run {runId}", "run đang chạy nhận lệnh trong vòng 2 giây");
        return 0;
    }

    private static string Require(CommandLine command, string option) =>
        command.Option(option) ?? throw new ArgumentException($"Thiếu --{option}");

    /// <summary><c>"1,2,3,4"</c> → [1, 2, 3, 4]; chỉ nhận số 1–4.</summary>
    public static List<int> ParseBlocks(string text)
    {
        var blocks = text.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
            .Select(b => int.TryParse(b, out var n) && n is >= 1 and <= 4 ? n : throw new ArgumentException($"Khối không hợp lệ: {b} (1–4)"))
            .Distinct().Order().ToList();
        return blocks.Count == 0 ? throw new ArgumentException("Cần ít nhất một khối") : blocks;
    }

    public void PrintHelp()
    {
        output.Title($"ARROW Framework v{version} · .NET {Environment.Version.ToString(2)}");
        output.Line($"Thư mục: {services.Paths.FrameworkRoot}");
        output.Blank();
        output.Table(["Lệnh", "Việc"],
        [
            ["arrow-fw", "Menu chính"],
            ["arrow-fw run [--experiment F] [--run-id R] [--tasks FILE|ID,…] [--blocks 1,2,3,4] [--rerun-infra] [--max-llm-calls N] [--fake-llm]", "Chạy các khối (mặc định cả 4)"],
            ["arrow-fw analyze | generate | repair | assess …", "Chỉ khối 1 | 2 | 3 | 4 (cùng tùy chọn với run)"],
            ["arrow-fw screen --manifest F --run-id R [--dataset D] [--tasks …] [--workers N] [--rerun-infra] [--keep-workspace]", "Sàng lọc CUT (khối 1, tiêu chí B)"],
            ["arrow-fw report --run-id R [--run-id R2 …]", "Báo cáo RQ1–RQ3 (gộp nhiều run)"],
            ["arrow-fw status --run-id R", "Trạng thái run"],
            ["arrow-fw show --run-id R --task T", "Chi tiết một task"],
            ["arrow-fw rerun-list --run-id R", "Job cần chạy lại riêng"],
            ["arrow-fw control --run-id R pause|resume|stop", "Điều khiển run đang chạy từ terminal khác"],
            ["arrow-fw doctor [--experiment F]", "Kiểm tra môi trường"],
            ["arrow-fw tools install", "Tải công cụ khối 4 còn thiếu"],
            ["arrow-fw setup", "Cài đặt máy"],
            ["arrow-fw models [--experiment F]", "Model, provider, khóa API, giá"],
            ["arrow-fw prompts check", "Kiểm tra bộ prompt"],
            ["arrow-fw --version", "In phiên bản"],
        ]);
    }
}
