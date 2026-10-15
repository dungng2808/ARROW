using System.Security.Cryptography;
using System.Text;
using Arrow.Models.Block3;

namespace Arrow.Service.Block3;

/// <summary>
/// Chọn tập con RQ2 (spec 03 Mục 6.1; Report 2 Experiment 2; quyết định 15/10/2026): <c>size</c> ứng viên thất bại chia đều cho các ô
/// model × quy trình; trong mỗi ô xếp ứng viên theo <c>sha256("&lt;seed&gt;|&lt;dataset&gt;|&lt;task_id&gt;|&lt;model&gt;|&lt;strategy&gt;")</c> và lấy các
/// ứng viên đầu. Thứ tự này chỉ phụ thuộc seed và định danh ứng viên (không phụ thuộc máy, thứ tự gộp run hay kết quả sửa), nên tái lập được.
/// Ô có ít ứng viên hơn phần của ô thì lấy hết và ghi số thiếu. Code thuần.
/// </summary>
public static class RepairSubsetSelector
{
    public const string Rule =
        "repairable failed initial candidates (c0 has code, verified, not VALID, not INFRA); equal share per model x strategy cell; " +
        "within a cell rank by sha256(\"<seed>|<dataset>|<task_id>|<model>|<strategy>\") ascending and take the first; a short cell is taken whole";

    public static string Cell(string model, string strategy) => $"{model}|{strategy}";

    public static string Rank(int seed, string dataset, SubsetCandidate candidate) =>
        Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes($"{seed}|{dataset}|{candidate.TaskId}|{candidate.Model}|{candidate.Strategy}"))).ToLowerInvariant();

    public static SubsetDataset Select(string dataset, IReadOnlyList<SubsetCandidate> eligible, IReadOnlyList<string> models, IReadOnlyList<string> strategies, int size, int seed)
    {
        var cells = models.SelectMany(m => strategies.Select(s => (Model: m, Strategy: s))).ToList();
        if (cells.Count == 0 || size <= 0 || size % cells.Count != 0)
        {
            throw new ArgumentException($"repair_subset = {size} phải chia hết cho số ô model × quy trình ({cells.Count})");
        }

        var perCell = size / cells.Count;
        var pool = new Dictionary<string, int>(StringComparer.Ordinal);
        var shortfall = new Dictionary<string, int>(StringComparer.Ordinal);
        var chosen = new List<SubsetCandidate>();
        foreach (var (model, strategy) in cells)
        {
            var inCell = eligible.Where(c => c.Model == model && c.Strategy == strategy)
                .OrderBy(c => Rank(seed, dataset, c), StringComparer.Ordinal)
                .ToList();
            var key = Cell(model, strategy);
            pool[key] = inCell.Count;
            if (inCell.Count < perCell)
            {
                shortfall[key] = perCell - inCell.Count;
            }

            chosen.AddRange(inCell.Take(perCell));
        }

        return new SubsetDataset { Size = size, PerCell = perCell, PoolByCell = pool, ShortfallByCell = shortfall, Candidates = chosen };
    }
}
