using Arrow.Models.Block3;

namespace Arrow.Service.Interfaces;

/// <summary>Kết quả <c>arrow rq2-subset</c>: file đã ghi (null khi còn vấn đề), tập con, và các vấn đề làm chưa chọn được.</summary>
public sealed record RepairSubsetResult(string? Path, RepairSubset? Subset, IReadOnlyList<string> Problems);

/// <summary>Chọn tập con RQ2 sau pha 1 (RQ1) từ kết quả của mọi run (mọi máy), ghi <c>repair_subset_file</c> của cấu hình thực nghiệm.</summary>
public interface IRepairSubsetService
{
    RepairSubsetResult Create(string experimentPath, IReadOnlyList<string> runIds);
}
