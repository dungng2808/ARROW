using Arrow.DataAccess.Interfaces;
using Arrow.Service.Block4;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

/// <summary>
/// Khối 4, phần báo cáo (spec 04 Mục 6, lệnh <c>arrow-fw report</c>): đọc một hoặc nhiều run, dựng báo cáo, ghi CSV và <c>report.md</c>.
/// Một run: ghi vào <c>runs/&lt;run&gt;/reports/</c>; nhiều run: ghi vào <c>runs/merged-&lt;run1&gt;-&lt;run2&gt;…/reports/</c>.
/// </summary>
public sealed class ReportService(RunResultsLoader loader, IReportRepository reports, ILogRepository log) : IReportService
{
    public ReportSummary Build(IReadOnlyList<string> runIds)
    {
        if (runIds.Count == 0)
        {
            throw new ArgumentException("Cần ít nhất một --run-id");
        }

        var experiment = loader.LoadExperiment(runIds[0]);
        var runs = runIds.Select(id => loader.Load(id, experiment)).ToList();
        var report = ReportBuilder.Build(runs, experiment);
        var target = runIds.Count == 1 ? runIds[0] : "merged-" + string.Join('-', runIds);
        foreach (var (name, table) in report.Csv)
        {
            reports.WriteCsv(target, name, table);
        }

        reports.WriteText(target, "report.md", report.Markdown);
        log.Info($"[K4] báo cáo {string.Join(", ", runIds)} → {reports.ReportsDir(target)}");
        return new ReportSummary(runIds, reports.ReportsDir(target), [.. report.Csv.Keys.Order(StringComparer.Ordinal), "report.md"], report.Markdown, report.Csv, report.PilotN);
    }
}
