using System.Text;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Run;

namespace Arrow.DataAccess.Files;

public sealed class FileRunRepository(FrameworkPaths paths) : IRunRepository
{
    public string RunRoot(string runId) => Path.Combine(paths.RunsDir, runId);

    private string RunFile(string runId, string name) => Path.Combine(RunRoot(runId), name);

    public bool Exists(string runId) => Directory.Exists(RunRoot(runId));

    public void EnsureCreated(string runId) => Directory.CreateDirectory(RunRoot(runId));

    public IReadOnlyList<RunInfo> ListRuns() =>
        Directory.Exists(paths.RunsDir)
            ? Directory.GetDirectories(paths.RunsDir)
                .Select(d => Path.GetFileName(d))
                .OrderBy(id => id, StringComparer.Ordinal)
                .Select(id => new RunInfo { RunId = id, Lock = ReadLock(id), Status = ReadStatus(id) })
                .ToList()
            : [];

    public RunLock? ReadLock(string runId) => JsonFile.TryRead<RunLock>(RunFile(runId, "run.lock.json"));

    public void WriteLock(string runId, RunLock runLock) => JsonFile.Write(RunFile(runId, "run.lock.json"), runLock);

    public void AppendLedger(string runId, LedgerEntry entry) => JsonlFile.Append(RunFile(runId, "ledger.jsonl"), entry);

    public IReadOnlyList<LedgerEntry> ReadLedger(string runId) => JsonlFile.ReadAll<LedgerEntry>(RunFile(runId, "ledger.jsonl"));

    public void AppendEvent(string runId, RunEvent runEvent) =>
        JsonlFile.Append(RunFile(runId, "events.jsonl"), runEvent with { Message = Redactor.Redact(runEvent.Message) });

    public IReadOnlyList<RunEvent> ReadEvents(string runId, int last)
    {
        var all = JsonlFile.ReadAll<RunEvent>(RunFile(runId, "events.jsonl"));
        return all.Skip(Math.Max(0, all.Count - last)).ToList();
    }

    public void WriteStatus(string runId, RunStatus status) => JsonFile.Write(RunFile(runId, "status.json"), status);

    public RunStatus? ReadStatus(string runId) => JsonFile.TryRead<RunStatus>(RunFile(runId, "status.json"));

    public void WriteControl(string runId, ControlCommand command) => JsonFile.Write(RunFile(runId, "control.json"), command);

    public ControlCommand? ReadControl(string runId) => JsonFile.TryRead<ControlCommand>(RunFile(runId, "control.json"));

    public void ClearControl(string runId)
    {
        var path = RunFile(runId, "control.json");
        lock (FileLocks.For(path))
        {
            File.Delete(path);
        }
    }
}

public sealed class FileTaskResultRepository(FrameworkPaths paths) : ITaskResultRepository
{
    public string TaskDir(string runId, TaskKey task) => Path.Combine(paths.RunsDir, runId, "tasks", task.Dataset, task.TaskId);

    private string Full(string runId, TaskKey task, string relativePath)
    {
        if (Path.IsPathRooted(relativePath) || relativePath.Split('/', '\\').Contains(".."))
        {
            throw new ArgumentException($"Đường dẫn kết quả phải tương đối và không có '..': {relativePath}");
        }

        return Path.Combine(TaskDir(runId, task), relativePath.Replace('/', Path.DirectorySeparatorChar));
    }

    public bool Exists(string runId, TaskKey task, string relativePath)
    {
        var full = Full(runId, task, relativePath);
        return File.Exists(full) || Directory.Exists(full);
    }

    public void SaveJson<T>(string runId, TaskKey task, string relativePath, T value) => JsonFile.Write(Full(runId, task, relativePath), value);

    public T? LoadJson<T>(string runId, TaskKey task, string relativePath) where T : class => JsonFile.TryRead<T>(Full(runId, task, relativePath));

    public void SaveText(string runId, TaskKey task, string relativePath, string text) => AtomicFile.WriteAllText(Full(runId, task, relativePath), text);

    public string? LoadText(string runId, TaskKey task, string relativePath)
    {
        var full = Full(runId, task, relativePath);
        return File.Exists(full) ? File.ReadAllText(full, Encoding.UTF8) : null;
    }

    public void Delete(string runId, TaskKey task, string relativePath)
    {
        var full = Full(runId, task, relativePath);
        if (Directory.Exists(full))
        {
            Directory.Delete(full, recursive: true);
        }
        else if (File.Exists(full))
        {
            File.Delete(full);
        }
    }

    public IReadOnlyList<TaskKey> ListTasks(string runId)
    {
        var tasksDir = Path.Combine(paths.RunsDir, runId, "tasks");
        if (!Directory.Exists(tasksDir))
        {
            return [];
        }

        return Directory.GetDirectories(tasksDir)
            .SelectMany(d => Directory.GetDirectories(d).Select(t => new TaskKey(Path.GetFileName(d), Path.GetFileName(t))))
            .OrderBy(t => t.Dataset, StringComparer.Ordinal).ThenBy(t => t.TaskId, StringComparer.Ordinal)
            .ToList();
    }
}

public sealed class FileLlmCacheRepository(FrameworkPaths paths) : ILlmCacheRepository
{
    private string PathFor(string runId, string key)
    {
        if (key.Length < 8 || key.Any(c => !char.IsAsciiLetterOrDigit(c)))
        {
            throw new ArgumentException("Khóa cache phải là chuỗi hex (sha256)");
        }

        return Path.Combine(paths.RunsDir, runId, "llm_cache", key[..2], key + ".json");
    }

    public LlmCacheEntry? TryGet(string runId, string key) => JsonFile.TryRead<LlmCacheEntry>(PathFor(runId, key));

    public void Put(string runId, LlmCacheEntry entry) => JsonFile.Write(PathFor(runId, entry.Key), entry);
}

public sealed class FileReportRepository(FrameworkPaths paths) : IReportRepository
{
    public string ReportsDir(string runId) => Path.Combine(paths.RunsDir, runId, "reports");

    public void WriteCsv(string runId, string fileName, CsvTable table) =>
        AtomicFile.WriteAllText(Path.Combine(ReportsDir(runId), fileName), CsvWriter.Format(table.Headers, table.Rows));

    public void WriteText(string runId, string fileName, string text) =>
        AtomicFile.WriteAllText(Path.Combine(ReportsDir(runId), fileName), text);
}

/// <summary>Log của tool, một file mỗi ngày; che bí mật bằng <see cref="Redactor"/> trước khi ghi.</summary>
public sealed class FileLogRepository(FrameworkPaths paths, bool verbose = false, Func<DateTimeOffset>? clock = null) : ILogRepository
{
    private readonly Func<DateTimeOffset> _clock = clock ?? (() => DateTimeOffset.Now);

    public void Write(LogLevel level, string message, Exception? exception = null)
    {
        if (level == LogLevel.Debug && !verbose)
        {
            return;
        }

        var now = _clock();
        var tag = level switch { LogLevel.Debug => "DBG", LogLevel.Info => "INF", LogLevel.Warn => "WRN", _ => "ERR" };
        var text = $"{TimeFormat.Local(now)} [{tag}] {message}";
        if (exception is not null)
        {
            text += "\n" + exception;
        }

        var path = Path.Combine(paths.LogsDir, $"arrow-{now.ToLocalTime():yyyyMMdd}.log");
        Directory.CreateDirectory(paths.LogsDir);
        lock (FileLocks.For(path))
        {
            File.AppendAllText(path, Redactor.Redact(text) + "\n", new UTF8Encoding(false));
        }
    }
}
