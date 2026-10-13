using System.Text.Json;
using Arrow.Core.Util;

namespace Arrow.Core.Runs;

/// <summary>Đường dẫn và đọc/ghi trong <c>runs/&lt;run_id&gt;/</c> (specs/01 §4, specs/08).</summary>
public sealed class RunStore
{
    public RunStore(string runsDir, string runId)
    {
        RunId = runId;
        Root = Path.GetFullPath(Path.Combine(runsDir, runId));
    }

    public string RunId { get; }
    public string Root { get; }
    public string RunConfigLockPath => Path.Combine(Root, "run_config.lock.json");
    public string LedgerPath => Path.Combine(Root, "ledger.jsonl");
    public string EventsPath => Path.Combine(Root, "events.jsonl");
    public string StatusPath => Path.Combine(Root, "status.json");
    public string ControlPath => Path.Combine(Root, "control.json");
    public string ToolLogPath => Path.Combine(Root, "tool.log");

    public string TaskDir(string dataset, string taskId) => Path.Combine(Root, "tasks", dataset, taskId);

    public bool Exists => Directory.Exists(Root);

    public void EnsureCreated() => Directory.CreateDirectory(Root);

    public static IEnumerable<RunStore> ListRuns(string runsDir) =>
        Directory.Exists(runsDir)
            ? Directory.EnumerateDirectories(runsDir).OrderBy(d => d, StringComparer.Ordinal)
                .Select(d => new RunStore(runsDir, Path.GetFileName(d)))
            : [];

    // ── ledger ───────────────────────────────────────────────────────────

    public void AppendLedger(LedgerEntry entry) => AtomicFile.AppendLine(LedgerPath, Json.Serialize(entry, indented: false));

    /// <summary>Đọc ledger; bỏ qua dòng hỏng (ví dụ dòng cuối bị ghi dở khi mất điện).</summary>
    public IReadOnlyList<LedgerEntry> ReadLedger()
    {
        var entries = new List<LedgerEntry>();
        foreach (var line in AtomicFile.ReadLines(LedgerPath))
        {
            try
            {
                entries.Add(Json.Deserialize<LedgerEntry>(line));
            }
            catch (JsonException)
            {
                // Dòng ghi dở: job tương ứng sẽ được chạy lại.
            }
        }

        return entries;
    }

    /// <summary>Trạng thái mới nhất của mỗi job (dòng sau thắng dòng trước).</summary>
    public IReadOnlyDictionary<string, LedgerEntry> LatestByJob() =>
        ReadLedger()
            .Where(e => e.Job is not null)
            .GroupBy(e => e.Job!.Canonical)
            .ToDictionary(g => g.Key, g => g.Last());

    /// <summary>Job đã xong: dòng cuối là DONE hoặc SKIPPED. Job INFRA chỉ chạy lại khi được yêu cầu.</summary>
    public static bool IsFinished(LedgerEntry? entry, bool rerunInfra) =>
        entry?.Status switch
        {
            JobStatus.Done or JobStatus.Skipped => true,
            JobStatus.Infra => !rerunInfra,
            _ => false,
        };

    // ── events ───────────────────────────────────────────────────────────

    public void AppendEvent(RunEvent runEvent) => AtomicFile.AppendLine(EventsPath, Json.Serialize(runEvent, indented: false));

    public IReadOnlyList<RunEvent> ReadRecentEvents(int count)
    {
        var lines = AtomicFile.ReadLines(EventsPath).TakeLast(count);
        var events = new List<RunEvent>();
        foreach (var line in lines)
        {
            try
            {
                events.Add(Json.Deserialize<RunEvent>(line));
            }
            catch (JsonException)
            {
            }
        }

        return events;
    }

    // ── status, control ──────────────────────────────────────────────────

    public void WriteStatus(RunStatus status) => Json.WriteAtomic(StatusPath, status);

    public RunStatus? ReadStatus() => TryRead<RunStatus>(StatusPath);

    public void WriteControl(ControlFile control) => Json.WriteAtomic(ControlPath, control);

    public ControlFile? ReadControl() => TryRead<ControlFile>(ControlPath);

    private static T? TryRead<T>(string path) where T : class
    {
        try
        {
            return File.Exists(path) ? Json.Read<T>(path) : null;
        }
        catch (Exception ex) when (ex is JsonException or IOException or InvalidDataException)
        {
            return null;
        }
    }
}
