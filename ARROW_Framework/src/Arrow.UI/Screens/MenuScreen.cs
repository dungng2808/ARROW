using Arrow.Models.Run;
using Arrow.Service;

namespace Arrow.UI.Screens;

/// <summary>
/// S1 (spec 06 Mục 3): thông tin máy, thực nghiệm, môi trường, model, thư mục repository, run gần đây; chọn việc bằng số. Máy chưa có
/// <c>machine.local.json</c> thì vào thẳng S2. Sau mỗi màn hình con chờ Enter rồi về menu.
/// </summary>
public sealed class MenuScreen(ArrowServices services, ConsoleInput input, ConsoleOutput output, string version, bool interactive)
{
    public async Task<int> RunAsync()
    {
        if (services.Setup.CurrentMachine() is null)
        {
            output.Status(Mark.Warn, "Máy này chưa cài đặt (chưa có machine.local.json)", "bắt đầu cài đặt");
            await Setup().RunAsync();
        }

        while (true)
        {
            PrintHeader();
            var choice = input.Choose("Chọn việc:",
            [
                "Chạy thực nghiệm (khối 1 -> 4)",
                "Chạy tiếp run đang dở",
                "Sàng lọc CUT (chỉ khối 1, tiêu chí B)",
                "Xem trạng thái run",
                "Xem chi tiết một task",
                "Báo cáo",
                "Cần chạy lại riêng",
                "Cài đặt máy",
                "Model, provider, khóa API, giá",
                "Kiểm tra môi trường",
            ], "Thoát");
            if (choice < 0)
            {
                return 0;
            }

            try
            {
                await RunChoiceAsync(choice);
            }
            catch (Exception ex) when (ex is Arrow.Models.Common.ArrowException or ArgumentException or InvalidOperationException)
            {
                output.Status(Mark.Fail, ex is Arrow.Models.Common.ArrowException a ? a.Code : "Lỗi", ex.Message);
            }

            input.Pause();
        }
    }

    private SetupScreen Setup() => new(services.Setup, new ModelScreen(services.Models, input, output), new DoctorScreen(services.Doctor, input, output, interactive), input, output);

    private RunScreen Runner() => new(services.Pipeline, input, output, interactive);

    private async Task RunChoiceAsync(int choice)
    {
        switch (choice)
        {
            case 0:
                await NewRunAsync();
                break;
            case 1 when ChooseRun(experimentRuns: true) is { } runId:
                await Runner().RunAsync(new RunRequest { RunId = runId });
                break;
            case 2:
                await new ScreeningScreen(services.Screening, input, output).RunAsync(CommandLine.Parse(["screen"]));
                break;
            case 3 when ChooseRun(experimentRuns: false) is { } runId:
                new StatusScreen(services.Runs, output).Run(runId);
                break;
            case 4 when ChooseRun(experimentRuns: false) is { } runId:
                new TaskScreen(services.Runs, input, output, interactive).Run(runId, input.Ask("task_id"));
                break;
            case 5 when ChooseRun(experimentRuns: true) is { } runId:
                new ReportScreen(services.Reports, output).Run([runId]);
                break;
            case 6 when ChooseRun(experimentRuns: true) is { } runId:
                if (new RerunScreen(services.Runs, input, output, interactive).Run(runId) is { } rerun)
                {
                    await Runner().RunAsync(rerun);
                }

                break;
            case 7:
                await Setup().RunAsync();
                break;
            case 8:
                await new ModelScreen(services.Models, input, output).RunAsync(null);
                break;
            case 9:
                await new DoctorScreen(services.Doctor, input, output, interactive).RunAsync(null);
                break;
        }
    }

    private async Task NewRunAsync()
    {
        // Cấu hình thực nghiệm: một file thì dùng luôn, nhiều file thì hỏi (pilot-v1, smoke-real, eval-v1…).
        var experiments = services.Models.Experiments();
        var experiment = experiments.Count switch
        {
            0 => null,
            1 => experiments[0],
            _ => experiments[input.Choose("Cấu hình thực nghiệm:", experiments.Select(Path.GetFileName).Select(n => n!).ToList())],
        };
        var runId = input.Ask("Run id", $"run-{DateTime.Now:yyyyMMdd-HHmm}");
        var set = input.Choose("Tập task:",
        [
            "Tất cả task của thực nghiệm",
            "File phân công máy (shards/<plan>/shard-NN.json)",
            "Đọc từ file danh sách task_id",
            "Nhập task_id, cách nhau dấu phẩy",
        ]);
        string? shard = null;
        string? selection = null;
        switch (set)
        {
            case 1:
                var shards = services.Pipeline.ListShards();
                if (shards.Count == 0)
                {
                    output.Status(Mark.Warn, "Không có file shard trong ARROW/shards");
                    return;
                }

                shard = shards[input.Choose("Shard của máy này:", shards.Select(s => $"{Path.GetFileName(Path.GetDirectoryName(s))}/{Path.GetFileName(s)}").ToList())];
                break;
            case 2:
                selection = input.Ask("File danh sách task_id");
                break;
            case 3:
                selection = input.Ask("task_id");
                break;
        }

        var blocks = input.Ask("Các khối", "1,2,3,4");
        if (!input.Confirm("Bắt đầu?", true))
        {
            return;
        }

        await Runner().RunAsync(new RunRequest { ExperimentPath = experiment, RunId = runId, TaskSelection = selection, ShardPath = shard, Blocks = Commands.ParseBlocks(blocks) });
    }

    /// <summary>Chọn một run trong <c>runs/</c> (mới nhất trước); <paramref name="experimentRuns"/> = bỏ run sàng lọc.</summary>
    private string? ChooseRun(bool experimentRuns)
    {
        var runs = services.Runs.ListRuns()
            .Where(r => !experimentRuns || r.Lock?.ExperimentId.StartsWith("screen:", StringComparison.Ordinal) != true)
            .OrderByDescending(r => r.Lock?.CreatedAt ?? DateTimeOffset.MinValue)
            .ToList();
        if (runs.Count == 0)
        {
            output.Status(Mark.Warn, "Chưa có run nào");
            return null;
        }

        var choice = input.Choose("Run:", runs.Select(Describe).ToList(), "Quay lại");
        return choice < 0 ? null : runs[choice].RunId;
    }

    private static string Describe(RunInfo run) =>
        $"{run.RunId,-20} {(run.Status is null ? (run.Lock?.ExperimentId.StartsWith("screen:", StringComparison.Ordinal) == true ? "SÀNG LỌC" : "-") : Format.RunState(run.Status.State)),-10} "
        + (run.Status is null ? "" : string.Join(" ", run.Status.Blocks.Select(b => $"K{b.Block} {b.Done}/{b.Total}")) + $"  INFRA {run.Status.Blocks.Sum(b => b.Infra)}");

    private void PrintHeader()
    {
        output.Blank();
        output.Title($"ARROW Framework v{version} · .NET {Environment.Version.ToString(2)} · {System.Runtime.InteropServices.RuntimeInformation.RuntimeIdentifier}");
        var machine = services.Setup.CurrentMachine();
        output.Line($"Máy:          {machine?.MachineName ?? "(chưa cài đặt)"}");
        var experiments = services.Models.Experiments();
        try
        {
            if (experiments.Count > 0)
            {
                var view = services.Models.Get(experiments[0]);
                output.Line($"Thực nghiệm:  {Path.GetFileNameWithoutExtension(view.ExperimentPath)} (sha {view.ExperimentSha256[..8]}...){(experiments.Count > 1 ? $" + {experiments.Count - 1} file khác" : "")}");
                foreach (var model in view.Models)
                {
                    output.Line($"Model:        {(model.Route is { IsComplete: true } ? "[OK]" : "[X]")} {model.Model.Name}  {model.Route?.EndpointHost}");
                }
            }
            else
            {
                output.Line("Thực nghiệm:  (chưa có file trong experiments/)");
            }
        }
        catch (Arrow.Models.Common.ArrowException ex)
        {
            output.Line($"Thực nghiệm:  [X] {ex.Code}: {ex.Message}");
        }

        output.Line(machine?.LastDoctor is { } doctor
            ? $"Môi trường:   {(doctor.Status == "READY" ? "[OK]" : "[X]")} {doctor.Status} ({doctor.At.ToLocalTime():dd/MM HH:mm})"
            : "Môi trường:   [!] chưa kiểm tra (chọn 10)");
        if (machine is not null)
        {
            output.Line($"Thư mục repo: {machine.WorkDir} ({services.Setup.FreeSpaceGb(machine.WorkDir):0} GB trống)");
        }

        var recent = services.Runs.ListRuns().OrderByDescending(r => r.Lock?.CreatedAt ?? DateTimeOffset.MinValue).Take(5).ToList();
        if (recent.Count > 0)
        {
            output.Blank();
            output.Line("Run gần đây:");
            foreach (var run in recent)
            {
                output.Line("  " + Describe(run));
            }
        }

        output.Blank();
    }
}
