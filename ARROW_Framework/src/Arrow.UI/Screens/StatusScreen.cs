using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>S6 (spec 06 Mục 8): bảng tổng đã lưu của một run và run còn sống không (heartbeat).</summary>
public sealed class StatusScreen(IRunQueryService runs, ConsoleOutput output)
{
    public int Run(string runId)
    {
        var view = runs.GetStatus(runId);
        output.Title($"Trạng thái · {runId}{(view.Lock is null ? "" : $" · {view.Lock.ExperimentId} · {view.Lock.MachineName}")}");
        if (view.Status is null)
        {
            output.Status(Mark.Warn, "Run chưa có status.json", "chưa chạy bằng arrow-fw run, hoặc chỉ là run sàng lọc");
            return 0;
        }

        output.Line($"Trạng thái: {Format.RunState(view.Status.State)} · bắt đầu {view.Status.StartedAt.ToLocalTime():dd/MM HH:mm}");
        Format.Summary(output, view.Status);
        output.Line(view.Alive ? $"Còn sống: có (heartbeat {view.HeartbeatAgeSeconds:0} s trước)" : "Còn sống: không");
        if (view.Alive)
        {
            output.Line($"Điều khiển từ terminal này: arrow-fw control --run-id {runId} pause|resume|stop");
        }

        return 0;
    }
}
