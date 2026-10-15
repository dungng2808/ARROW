using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>
/// <c>arrow rq2-subset --experiment F --run-id A [--run-id B …]</c>: chọn tập con RQ2 sau pha 1 (spec 03 Mục 6.1), in số ứng viên của
/// từng ô và đường dẫn file cần commit; còn vấn đề (task thiếu, trùng, c0 INFRA) thì in danh sách và không ghi file.
/// </summary>
public sealed class RepairSubsetScreen(IRepairSubsetService service, ConsoleOutput output)
{
    public int Run(string experiment, IReadOnlyList<string> runIds)
    {
        var result = service.Create(Path.GetFullPath(experiment), runIds);
        output.Title($"Tap con RQ2 · {Path.GetFileName(experiment)} · {runIds.Count} run");
        if (result.Subset is null)
        {
            foreach (var problem in result.Problems.Take(50))
            {
                output.Status(Mark.Fail, problem);
            }

            if (result.Problems.Count > 50)
            {
                output.Line($"… và {result.Problems.Count - 50} vấn đề khác");
            }

            output.Line("Chưa ghi tập con: chạy xong pha 1 trên mọi máy (và chạy lại các task INFRA) rồi chạy lại lệnh này.");
            return 1;
        }

        foreach (var (dataset, data) in result.Subset.Datasets)
        {
            output.Line($"{dataset}: {data.Candidates.Count}/{data.Size} ứng viên, {data.PerCell} mỗi ô");
            output.Table(["O (model|quy trinh)", "That bai", "Chon", "Thieu"],
                data.PoolByCell.Select(c => (IReadOnlyList<string>)[c.Key, c.Value.ToString(), Math.Min(c.Value, data.PerCell).ToString(), data.ShortfallByCell.GetValueOrDefault(c.Key).ToString()]));
            output.Blank();
        }

        output.Status(Mark.Ok, "Da ghi", result.Path!);
        output.Line("Commit file nay len Git; moi may pull roi chay lai dung lenh arrow run (pha 2) de sua theo tap con.");
        return 0;
    }
}
