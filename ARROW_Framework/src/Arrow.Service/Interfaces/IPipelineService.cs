using Arrow.Models.Block1;
using Arrow.Models.Block4;
using Arrow.Models.Run;

namespace Arrow.Service.Interfaces;

/// <summary>Một run đang chạy ở luồng nền: run id và việc chờ kết thúc.</summary>
public sealed record RunHandle(string RunId, Task<RunResult> Completion);

/// <summary>Màn hình S5 (spec 07 Mục 4.1, 6): chạy run ở luồng nền, báo sự kiện qua <see cref="IProgress{T}"/>, điều khiển khi đang chạy.</summary>
public interface IPipelineService
{
    RunHandle Start(RunRequest request, IProgress<RunEvent> progress);

    /// <summary>Không bắt đầu bước mới; bước đang chạy chạy nốt.</summary>
    void Pause();
    void Resume();

    /// <summary>Dừng an toàn: hủy bước đang chạy (giết tiến trình); job đã ghi ledger được giữ.</summary>
    void Stop();

    /// <summary>Bảng tổng hiện tại (lệnh <c>t</c>).</summary>
    RunStatus GetSummary();
}

/// <summary>Chi tiết một task cho màn hình S7.</summary>
public sealed record TaskDetail(TaskData Data, RepositoryMetadata? Metadata, Baseline? Baseline, string TaskDir);

/// <summary>Màn hình S1, S6, S7, S9 (spec 07 Mục 4.1): đọc trạng thái đã lưu của các run; gửi lệnh điều khiển cho run đang chạy ở terminal khác.</summary>
public interface IRunQueryService
{
    IReadOnlyList<RunInfo> ListRuns();
    RunStatusView GetStatus(string runId);
    TaskDetail GetTaskDetail(string runId, string taskId);
    IReadOnlyList<RerunItem> GetRerunList(string runId);
    void SendControl(string runId, ControlAction action);
}
