using System.Text.Json;
using Preflight.Models;

namespace Preflight.DataAccess;

/// <summary>Port schema classes/evidence/input_rejections của ingest.py.</summary>
public sealed class ClassIndexStore : SqliteStore
{
    public ClassIndexStore(string path) : base(path) => Execute("""
        CREATE TABLE IF NOT EXISTS classes(task_id TEXT PRIMARY KEY,project_id TEXT NOT NULL,repo_url TEXT NOT NULL,class_path TEXT NOT NULL,class_fqn TEXT NOT NULL,class_name TEXT NOT NULL);
        CREATE TABLE IF NOT EXISTS evidence(task_id TEXT NOT NULL,source_json_path TEXT NOT NULL,source_json_sha256 TEXT NOT NULL,test_class_path TEXT NOT NULL,PRIMARY KEY(task_id,source_json_path));
        CREATE INDEX IF NOT EXISTS evidence_task_idx ON evidence(task_id);
        CREATE TABLE IF NOT EXISTS input_rejections(source_json_path TEXT PRIMARY KEY,reason_code TEXT NOT NULL);
        CREATE TABLE IF NOT EXISTS normalized_tasks(task_id TEXT PRIMARY KEY,task_json TEXT NOT NULL);
        """);
    public void Begin() => Execute("BEGIN TRANSACTION");
    public void Commit() => Execute("COMMIT");
    public void Add(string taskId, string[] identity, string relative, string hash, string test)
    {
        Execute("INSERT OR IGNORE INTO classes VALUES($0,$1,$2,$3,$4,$5)", taskId, identity[0], identity[1], identity[2], identity[4], identity[3]);
        Execute("INSERT OR IGNORE INTO evidence VALUES($0,$1,$2,$3)", taskId, relative, hash, test);
    }
    public void Reject(string path, string reason) => Execute("INSERT OR REPLACE INTO input_rejections VALUES($0,$1)", path, reason);
    public IReadOnlyList<Dictionary<string, string>> Rejections() => Query("SELECT source_json_path,reason_code FROM input_rejections ORDER BY source_json_path").Select(r => new Dictionary<string, string> { ["source_json_path"] = (string)r[0]!, ["reason_code"] = (string)r[1]! }).ToArray();
    public IReadOnlyList<ClassCandidate> Candidates()
    {
        return Query("SELECT * FROM classes ORDER BY project_id,task_id").Select(r =>
        {
            var e = Query("SELECT source_json_path,source_json_sha256,test_class_path FROM evidence WHERE task_id=$0 ORDER BY source_json_path", r[0]);
            return new ClassCandidate((string)r[0]!, (string)r[1]!, (string)r[2]!, (string)r[3]!, (string)r[4]!, (string)r[5]!, e.Select(x => (string)x[0]!).ToArray(), e.Select(x => (string)x[1]!).ToArray(), e.Select(x => (string)x[2]!).Where(s => s.Length > 0).Distinct(StringComparer.Ordinal).Order(PythonCodePointComparer.Instance).ToArray());
        }).ToArray();
    }
    public void SaveTasks(IEnumerable<CandidateTask> tasks)
    {
        Begin(); try { foreach (var task in tasks) Execute("INSERT OR REPLACE INTO normalized_tasks VALUES($0,$1)", task.Candidate.TaskId, PythonJson.Serialize(task, compact: true)); Commit(); } catch { Execute("ROLLBACK"); throw; }
    }
    public IReadOnlyList<CandidateTask> LoadTasks() => Query("SELECT task_json FROM normalized_tasks ORDER BY task_id").Select(r => JsonSerializer.Deserialize<CandidateTask>((string)r[0]!, PythonJson.Options)!).ToArray();
}
