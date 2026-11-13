using System.Text;
using System.Text.Json;
using Preflight.Models;

namespace Preflight.DataAccess;

/// <summary>Port checkpoint.py: transaction theo task, session, attempt và recovery.</summary>
public sealed class CheckpointStore : SqliteStore
{
    public CheckpointStore(string path) : base(path) => Execute("""
        CREATE TABLE IF NOT EXISTS metadata(key TEXT PRIMARY KEY,value TEXT NOT NULL);
        CREATE TABLE IF NOT EXISTS sessions(session_id INTEGER PRIMARY KEY AUTOINCREMENT,started_at_utc TEXT NOT NULL,ended_at_utc TEXT,workers INTEGER NOT NULL,status TEXT NOT NULL,completed_before INTEGER NOT NULL,completed_after INTEGER,reason TEXT);
        CREATE TABLE IF NOT EXISTS tasks(task_id TEXT PRIMARY KEY,ordinal INTEGER NOT NULL UNIQUE,repo_url TEXT NOT NULL,status TEXT NOT NULL,attempt_count INTEGER NOT NULL DEFAULT 0,started_at_utc TEXT,completed_at_utc TEXT,preflight_status TEXT,result_json TEXT);
        CREATE TABLE IF NOT EXISTS task_attempts(task_id TEXT NOT NULL,attempt_number INTEGER NOT NULL,session_id INTEGER NOT NULL,started_at_utc TEXT NOT NULL,ended_at_utc TEXT,status TEXT NOT NULL,PRIMARY KEY(task_id,attempt_number));
        CREATE TABLE IF NOT EXISTS repo_cleanup(repo_url TEXT PRIMARY KEY,event_json TEXT NOT NULL,updated_at_utc TEXT NOT NULL);
        CREATE TABLE IF NOT EXISTS audit_events(id INTEGER PRIMARY KEY AUTOINCREMENT,at_utc TEXT NOT NULL,event_json TEXT NOT NULL);
        """);
    public static string Now() => DateTimeOffset.UtcNow.ToString("O");
    public void Initialize(RunContract contract)
    {
        lock (Gate)
        {
            if (Query("SELECT key FROM metadata").Count > 0) throw new InvalidDataException("Checkpoint already initialized");
            Execute("BEGIN");
            try
            {
                string payload = PythonJson.Serialize(contract, compact: true);
                Execute("INSERT INTO metadata VALUES('schema_version','3')");
                Execute("INSERT INTO metadata VALUES('contract',$0)", payload);
                Execute("INSERT INTO metadata VALUES('contract_sha256',$0)", HashText(payload));
                int ordinal = 0;
                foreach (var task in contract.Tasks) Execute("INSERT INTO tasks(task_id,ordinal,repo_url,status) VALUES($0,$1,$2,'PENDING')", task.Candidate.TaskId, ordinal++, task.Repo.RepoUrl);
                Execute("COMMIT");
            }
            catch { Execute("ROLLBACK"); throw; }
        }
    }
    public RunContract Contract()
    {
        string version = (string)Query("SELECT value FROM metadata WHERE key='schema_version'").Single()[0]!;
        if (version != "3") throw new InvalidDataException("Unsupported .NET checkpoint schema");
        string payload = (string)Query("SELECT value FROM metadata WHERE key='contract'").Single()[0]!;
        if (HashText(payload) != (string)Query("SELECT value FROM metadata WHERE key='contract_sha256'").Single()[0]!) throw new InvalidDataException("Contract digest mismatch");
        return JsonSerializer.Deserialize<RunContract>(payload, PythonJson.Options)!;
    }
    public void Recover()
    {
        lock (Gate)
        {
            Execute("BEGIN");
            try { Execute("UPDATE task_attempts SET status='ABANDONED',ended_at_utc=$0 WHERE status='RUNNING'", Now()); Execute("UPDATE tasks SET status='PENDING',started_at_utc=NULL WHERE status='RUNNING'"); Execute("UPDATE sessions SET status='ABANDONED',ended_at_utc=$0 WHERE status='RUNNING'", Now()); Execute("COMMIT"); }
            catch { Execute("ROLLBACK"); throw; }
        }
    }
    public long BeginSession(int workers)
    {
        lock (Gate) { Execute("INSERT INTO sessions(started_at_utc,workers,status,completed_before) VALUES($0,$1,'RUNNING',$2)", Now(), workers, Counts()["completed"]); return (long)Query("SELECT last_insert_rowid()")[0][0]!; }
    }
    public void FinishSession(long session, string status) { lock (Gate) Execute("UPDATE sessions SET ended_at_utc=$0,status=$1,completed_after=$2 WHERE session_id=$3", Now(), status, Counts()["completed"], session); }
    public int Start(string task, long session)
    {
        lock (Gate)
        {
            var row = Query("SELECT status,attempt_count FROM tasks WHERE task_id=$0", task).Single();
            if ((string)row[0]! != "PENDING") throw new InvalidDataException("Task is not pending: " + task);
            int attempt = checked((int)(long)row[1]! + 1); Execute("BEGIN");
            try { Execute("UPDATE tasks SET status='RUNNING',attempt_count=$0,started_at_utc=$1 WHERE task_id=$2", attempt, Now(), task); Execute("INSERT INTO task_attempts VALUES($0,$1,$2,$3,NULL,'RUNNING')", task, attempt, session, Now()); Execute("COMMIT"); }
            catch { Execute("ROLLBACK"); throw; }
            return attempt;
        }
    }
    public void Complete(string task, int attempt, PreflightResult result)
    {
        lock (Gate)
        {
            if (result.TaskId != task) throw new InvalidDataException("Result task_id mismatch");
            Execute("BEGIN");
            try
            {
                if (Execute("UPDATE tasks SET status='COMPLETED',completed_at_utc=$0,preflight_status=$1,result_json=$2 WHERE task_id=$3 AND attempt_count=$4 AND status='RUNNING'", Now(), result.PreflightStatus.ToString(), PythonJson.Serialize(result, compact: true), task, attempt) != 1) throw new InvalidDataException("Inactive task attempt");
                Execute("UPDATE task_attempts SET status='COMPLETED',ended_at_utc=$0 WHERE task_id=$1 AND attempt_number=$2", Now(), task, attempt); Execute("COMMIT");
            }
            catch { Execute("ROLLBACK"); throw; }
        }
    }
    public void Interrupt(string task, int attempt, string reason = "INTERRUPTED")
    {
        lock (Gate)
        {
            Execute("BEGIN");
            try { Execute("UPDATE task_attempts SET status=$0,ended_at_utc=$1 WHERE task_id=$2 AND attempt_number=$3 AND status='RUNNING'", reason, Now(), task, attempt); Execute("UPDATE tasks SET status='PENDING',started_at_utc=NULL WHERE task_id=$0 AND attempt_count=$1 AND status='RUNNING'", task, attempt); Execute("COMMIT"); } catch { Execute("ROLLBACK"); throw; }
        }
    }
    public Dictionary<string, int> Counts()
    {
        lock (Gate)
        {
            var result = new Dictionary<string, int> { ["total"] = 0, ["completed"] = 0, ["running"] = 0, ["pending"] = 0 };
            foreach (var row in Query("SELECT status,COUNT(*) FROM tasks GROUP BY status")) { int count = checked((int)(long)row[1]!); result[((string)row[0]!).ToLowerInvariant()] = count; result["total"] += count; }
            return result;
        }
    }
    public HashSet<string> Pending() { lock (Gate) return Query("SELECT task_id FROM tasks WHERE status!='COMPLETED' ORDER BY ordinal").Select(r => (string)r[0]!).ToHashSet(StringComparer.Ordinal); }
    public IReadOnlyList<PreflightResult> Results() { lock (Gate) return Query("SELECT result_json FROM tasks WHERE status='COMPLETED' ORDER BY task_id").Select(r => JsonSerializer.Deserialize<PreflightResult>((string)r[0]!, PythonJson.Options)!).ToArray(); }
    public Dictionary<string, int> StatusCounts() { lock (Gate) return Query("SELECT preflight_status,COUNT(*) FROM tasks WHERE status='COMPLETED' GROUP BY preflight_status").ToDictionary(r => (string)r[0]!, r => checked((int)(long)r[1]!), StringComparer.Ordinal); }
    public IReadOnlyList<Dictionary<string, object?>> Sessions() { lock (Gate) return Query("SELECT session_id,started_at_utc,ended_at_utc,workers,status,completed_before,completed_after,reason FROM sessions ORDER BY session_id").Select(r => new[] { "session_id", "started_at_utc", "ended_at_utc", "workers", "status", "completed_before", "completed_after", "reason" }.Select((key, i) => (key, r[i])).ToDictionary(x => x.key, x => x.Item2, StringComparer.Ordinal)).ToArray(); }
    public void PutCleanup(Dictionary<string, object?> value) { lock (Gate) Execute("INSERT INTO repo_cleanup VALUES($0,$1,$2) ON CONFLICT(repo_url) DO UPDATE SET event_json=excluded.event_json,updated_at_utc=excluded.updated_at_utc", value["repo_url"], PythonJson.Serialize(value, compact: true), Now()); }
    public IReadOnlyList<JsonElement> CleanupEvents() { lock (Gate) return Query("SELECT event_json FROM repo_cleanup ORDER BY repo_url").Select(r => JsonDocument.Parse((string)r[0]!).RootElement.Clone()).ToArray(); }
    public HashSet<string> CompletedRepos() { lock (Gate) return Query("SELECT repo_url FROM tasks GROUP BY repo_url HAVING SUM(CASE WHEN status='COMPLETED' THEN 0 ELSE 1 END)=0").Select(r => (string)r[0]!).ToHashSet(StringComparer.Ordinal); }
    public int Reopen(string status, string since, string reason)
    {
        if (!DateTimeOffset.TryParse(since, System.Globalization.CultureInfo.InvariantCulture, System.Globalization.DateTimeStyles.AssumeUniversal, out var timestamp) || string.IsNullOrWhiteSpace(reason)) throw new ArgumentException("reopen requires UTC --since and --reason");
        since = timestamp.ToUniversalTime().ToString("O");
        lock (Gate)
        {
            Execute("BEGIN");
            try
            {
                var rows = Query("SELECT task_id,result_json FROM tasks WHERE status='COMPLETED' AND preflight_status=$0 AND completed_at_utc >= $1", status, since);
                var ids = rows.Select(r => (string)r[0]!).ToArray();
                Execute("INSERT INTO audit_events(at_utc,event_json) VALUES($0,$1)", Now(), PythonJson.Serialize(new { Action = "reopen", Reason = reason, Status = status, Since = since, PreviousResults = rows.Select(r => new { TaskId = r[0], ResultJson = r[1] }) }));
                foreach (string id in ids) Execute("UPDATE tasks SET status='PENDING',result_json=NULL,preflight_status=NULL,completed_at_utc=NULL WHERE task_id=$0", id);
                Execute("COMMIT"); return ids.Length;
            }
            catch { Execute("ROLLBACK"); throw; }
        }
    }
    private static string HashText(string value) => Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(Encoding.UTF8.GetBytes(value))).ToLowerInvariant();
}

/// <summary>Khóa run độc quyền, hệ điều hành tự nhả sau hard kill.</summary>
public sealed class RunLock : IDisposable
{
    private readonly FileStream stream;
    public RunLock(string path)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(path))!);
        try { stream = new FileStream(path, FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.None); }
        catch (IOException error) { throw new IOException("Run is already active: " + path, error); }
        byte[] metadata = Encoding.UTF8.GetBytes(PythonJson.Serialize(new { Pid = Environment.ProcessId, Host = Environment.MachineName, LockedAtUtc = CheckpointStore.Now() }));
        stream.SetLength(0); stream.Write(metadata); stream.Flush(true);
    }
    public void Dispose() => stream.Dispose();
}
