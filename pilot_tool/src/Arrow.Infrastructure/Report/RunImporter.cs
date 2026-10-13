using Arrow.Core.Runs;
using Arrow.Core.Util;

namespace Arrow.Infrastructure.Report;

/// <summary><c>imported.json</c>: run mới lấy kết quả các bước đầu từ run cũ.</summary>
public sealed record ImportRecord
{
    public string FromRun { get; init; } = "";
    public string? FromToolCommit { get; init; }
    public string FromExperimentSha256 { get; init; } = "";
    public List<string> Stages { get; init; } = [];
    public int LedgerLines { get; init; }
    public DateTimeOffset ImportedAt { get; init; }
}

/// <summary>
/// Tạo run mới từ kết quả S0–S2 của một run cũ (cùng cấu hình thực nghiệm) để chạy lại các bước sau bằng tool đã sửa, không gọi
/// model lại: chép <c>tasks/</c> (trừ <c>evosuite/</c>, <c>quality/</c>), <c>llm_cache/</c>, các dòng ledger và sự kiện của các bước
/// được chép; ghi nguồn và commit tool cũ vào <c>imported.json</c>.
/// </summary>
public static class RunImporter
{
    public static readonly string[] ImportableStages = ["prepare", "generate", "repair"];

    public static ImportRecord Import(RunStore source, RunStore target, string experimentSha256)
    {
        if (!source.Exists)
        {
            throw new ArrowException("RUN_MISSING", $"Không có runs/{source.RunId}");
        }

        if (target.Exists && File.Exists(target.LedgerPath))
        {
            throw new ArrowException("RUN_EXISTS", $"runs/{target.RunId} đã có dữ liệu; chọn --run-id khác");
        }

        var sourceLock = Json.Read<RunConfigLock>(source.RunConfigLockPath);
        if (sourceLock.ExperimentSha256 != experimentSha256)
        {
            throw new ArrowException("EXPERIMENT_MISMATCH", $"runs/{source.RunId} dùng cấu hình thực nghiệm khác (sha {sourceLock.ExperimentSha256[..12]})");
        }

        target.EnsureCreated();
        foreach (var file in Directory.EnumerateFiles(source.Root, "*", SearchOption.AllDirectories))
        {
            var relative = Path.GetRelativePath(source.Root, file).Replace('\\', '/');
            var parts = relative.Split('/');
            var keep = parts[0] == "llm_cache"
                       || parts[0] == "tasks" && parts.Length > 4 && parts[3] is not ("evosuite" or "quality")
                       || parts[0] == "tasks" && parts.Length == 4;
            if (!keep || relative.EndsWith(".tmp", StringComparison.Ordinal))
            {
                continue;
            }

            var destination = Path.Combine(target.Root, relative);
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            File.Copy(file, destination, overwrite: true);
        }

        var lines = 0;
        foreach (var entry in source.ReadLedger().Where(e => e.Job is not null && ImportableStages.Contains(e.Job.Stage)))
        {
            target.AppendLedger(entry);
            lines++;
        }

        foreach (var runEvent in source.ReadRecentEvents(int.MaxValue).Where(e => ImportableStages.Contains(e.Stage)))
        {
            target.AppendEvent(runEvent);
        }

        var record = new ImportRecord
        {
            FromRun = source.RunId,
            FromToolCommit = sourceLock.ToolGitCommit,
            FromExperimentSha256 = sourceLock.ExperimentSha256,
            Stages = [.. ImportableStages],
            LedgerLines = lines,
            ImportedAt = DateTimeOffset.UtcNow,
        };
        Json.WriteAtomic(Path.Combine(target.Root, "imported.json"), record);
        return record;
    }
}
