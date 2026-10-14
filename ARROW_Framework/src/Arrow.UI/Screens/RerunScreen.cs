using Arrow.Models.Run;
using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>S9 (spec 06 Mục 11): danh sách job INFRA cần chạy lại riêng, kèm lệnh; chọn chạy lại một task hoặc tất cả.</summary>
public sealed class RerunScreen(IRunQueryService runs, ConsoleInput input, ConsoleOutput output, bool interactive)
{
    /// <summary>Trả yêu cầu chạy lại người dùng chọn (null = không chạy).</summary>
    public RunRequest? Run(string runId)
    {
        var items = runs.GetRerunList(runId);
        output.Title($"Cần chạy lại riêng · {runId}");
        if (items.Count == 0)
        {
            output.Status(Mark.Ok, "Không có job nào cần chạy lại");
            return null;
        }

        output.Table(["#", "Task", "Khối", "Job", "Lỗi", "Lần thử", "Lặp lại?"], items.Select((x, i) => (IReadOnlyList<string>)
        [
            (i + 1).ToString(), Format.Short(x.TaskId), x.Block.ToString(), x.Job, x.Code ?? "", x.Attempts.ToString(), x.TimeoutRepeated ? "có" : "không",
        ]).ToList());
        output.Blank();
        foreach (var item in items.DistinctBy(x => x.TaskId))
        {
            output.Line($"Lệnh: {item.Command}");
        }

        if (!interactive)
        {
            return null;
        }

        var tasks = items.Select(x => x.TaskId).Distinct().ToList();
        var numbers = items.Select(x => x.TaskId).ToList();
        var options = tasks.Select(t => $"Chạy lại task số {numbers.IndexOf(t) + 1} ({Format.Short(t)})").Append("Chạy lại tất cả").ToList();
        var choice = input.Choose("", options, "Quay lại");
        return choice < 0 ? null
            : new RunRequest { RunId = runId, RerunInfra = true, TaskSelection = choice == tasks.Count ? string.Join(',', tasks) : tasks[choice] };
    }
}
