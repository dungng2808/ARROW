using Arrow.Models.Run;
using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>S8 (spec 06 Mục 10): tạo báo cáo cho một hoặc nhiều run rồi in tóm tắt, mỗi bảng tách theo dataset và model.</summary>
public sealed class ReportScreen(IReportService reports, ConsoleOutput output)
{
    public int Run(IReadOnlyList<string> runIds)
    {
        var report = reports.Build(runIds);
        output.Title($"Báo cáo · {string.Join(", ", runIds)} · đã ghi {report.ReportsDir}");

        var rq1 = report.Tables["rq1_verification.csv"];
        foreach (var group in rq1.Rows.GroupBy(r => (Dataset: r[0], Model: r[1])))
        {
            output.Line($"RQ1 · Verification ({group.Key.Dataset} · {group.Key.Model})");
            output.Table(["Quy trình", "n", "Compile", "Discovery", "Execution", "Target", "VALID", "Không hợp lệ", "INFRA"],
                group.Select(r => (IReadOnlyList<string>)[r[2], r[3], Format.Percent(r[4], r[3]), Format.Percent(r[5], r[3]), Format.Percent(r[6], r[3]), Format.Percent(r[7], r[3]), Format.Percent(r[8], r[3]), r[9], r[10]]).ToList());
            output.Blank();
        }

        var rq2 = report.Tables["rq2_summary.csv"];
        foreach (var group in rq2.Rows.GroupBy(r => (Dataset: r[0], Model: r[1])))
        {
            output.Line($"RQ2 · Repair ({group.Key.Dataset} · {group.Key.Model}) · {group.First()[3]} candidate");
            foreach (var r in group)
            {
                output.Line($"{(r[2] == "fixed" ? "Fixed" : r[2] == "adaptive" ? "Adaptive" : r[2]),-9} thành công {Format.Percent(r[4], r[3])} · vòng TB {r[6]} · thời gian TB {r[8]} s");
            }

            output.Blank();
        }

        if (report.Tables.TryGetValue("pilot_n.csv", out var pilot) && report.PilotN is not null)
        {
            output.Line($"Pilot · chọn N: S(k) = {string.Join(' ', pilot.Rows.Select(r => r[1]))} -> N = {report.PilotN}");
            output.Blank();
        }

        var rq3 = report.Tables["rq3_summary.csv"];
        foreach (var group in rq3.Rows.GroupBy(r => (Dataset: r[0], Model: r[1])))
        {
            output.Line($"RQ3 · Quality ({group.Key.Dataset} · {group.Key.Model}) · ARROW vs EvoSuite, ghép cặp theo từng chỉ số");
            output.Table(["Chỉ số", "n", "ARROW", "EvoSuite"], group.Select(r => (IReadOnlyList<string>)[r[2], r[3], r[4], r[5]]).ToList());
            output.Blank();
        }

        var rerunTable = report.Tables["needs_rerun.csv"];
        var rerun = rerunTable.Rows.Count;
        // Báo cáo gộp: gợi ý đúng run có job cần chạy lại (cột rerun_command chứa "--run-id <run>").
        var rerunRuns = rerunTable.Rows
            .Select(r => r[^1].Split(' ').SkipWhile(w => w != "--run-id").Skip(1).FirstOrDefault())
            .OfType<string>()
            .Distinct(StringComparer.Ordinal)
            .DefaultIfEmpty(runIds[0]);
        output.Line(rerun == 0
            ? "Cần chạy lại riêng: 0 job (run hoàn chỉnh)"
            : $"Cần chạy lại riêng: {rerun} job -> {string.Join("  ·  ", rerunRuns.Select(r => $"arrow rerun-list --run-id {r}"))}");
        output.Line($"File: {string.Join(", ", report.Files)}");
        return 0;
    }
}
