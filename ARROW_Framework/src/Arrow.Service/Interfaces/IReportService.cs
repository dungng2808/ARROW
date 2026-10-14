using Arrow.Models.Run;

namespace Arrow.Service.Interfaces;

/// <summary>Báo cáo đã ghi: các run, thư mục báo cáo, tên file, nội dung <c>report.md</c> và các bảng (để UI in tóm tắt).</summary>
public sealed record ReportSummary(
    IReadOnlyList<string> RunIds,
    string ReportsDir,
    IReadOnlyList<string> Files,
    string Markdown,
    IReadOnlyDictionary<string, CsvTable> Tables,
    int? PilotN = null);

/// <summary>Màn hình S8 (spec 07 Mục 4.1): tạo báo cáo cho một hoặc nhiều run.</summary>
public interface IReportService
{
    ReportSummary Build(IReadOnlyList<string> runIds);
}
