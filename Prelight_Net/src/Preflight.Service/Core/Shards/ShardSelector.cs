using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;

namespace Preflight.Service.Core.Shards;

/// <summary>Port shards.assignment_record/select_shard và repo_key của split_dataset.</summary>
public static class ShardSelector
{
    public static object Assignment(ClassCandidate c)
    {
        var evidence = c.SourceJsonPaths.Zip(c.SourceJsonSha256s).OrderBy(p => p.First, PythonCodePointComparer.Instance).Select(p => new[] { p.First, p.Second }).ToArray();
        string json = PythonJson.Serialize(evidence, compact: true); var ascii = new StringBuilder();
        foreach (char ch in json) ascii.Append(ch >= 127 ? "\\u" + ((int)ch).ToString("x4", System.Globalization.CultureInfo.InvariantCulture) : ch.ToString());
        string hash = Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(ascii.ToString()))).ToLowerInvariant();
        return new { c.TaskId, c.RepoUrl, c.ClassPath, c.ClassName, EvidenceCount = evidence.Length, EvidenceSha256 = hash };
    }
    public static CandidateTask[] Select(IReadOnlyList<CandidateTask> tasks, string shard)
    {
        using var doc = JsonDocument.Parse(File.ReadAllBytes(shard)); var root = doc.RootElement;
        if (root.GetProperty("schema_version").GetInt32() != 1 || root.GetProperty("shard_id").GetString() is not { Length: > 0 }) throw new InvalidDataException("Invalid shard schema");
        var rows = root.GetProperty("classes").EnumerateArray().ToArray();
        if (rows.Length == 0 || rows.Length != root.GetProperty("class_count").GetInt32()) throw new InvalidDataException("Invalid shard class_count");
        var map = tasks.ToDictionary(t => t.Candidate.TaskId, StringComparer.Ordinal); var seen = new HashSet<string>(StringComparer.Ordinal); var selected = new List<CandidateTask>();
        foreach (var row in rows)
        {
            string id = row.GetProperty("task_id").GetString()!;
            if (!seen.Add(id) || !map.TryGetValue(id, out var task) || PythonJson.Render(row, compact: true) != PythonJson.Serialize(Assignment(task.Candidate), compact: true)) throw new InvalidDataException("Shard metadata/evidence mismatch: " + id);
            selected.Add(task);
        }
        return selected.ToArray();
    }
    public static string RepoKey(string url)
    {
        string value = url.TrimEnd('/'); if (value.EndsWith(".git", StringComparison.Ordinal)) value = value[..^4];
        return Uri.TryCreate(value, UriKind.Absolute, out var uri) && uri.Host.Equals("github.com", StringComparison.OrdinalIgnoreCase) ? "github.com/" + uri.AbsolutePath.Trim('/').ToLowerInvariant() : value;
    }
    public static IReadOnlyList<CandidateTask[]> Partition(IReadOnlyList<CandidateTask> tasks, int parts)
    {
        if (parts < 1) throw new ArgumentOutOfRangeException(nameof(parts));
        var buckets = Enumerable.Range(0, parts).Select(_ => new List<CandidateTask>()).ToArray(); var repos = new int[parts];
        foreach (var group in tasks.GroupBy(t => RepoKey(t.Repo.RepoUrl)).OrderByDescending(g => g.Count()).ThenBy(g => g.Key, PythonCodePointComparer.Instance))
        {
            int index = Enumerable.Range(0, parts).MinBy(i => (buckets[i].Count, repos[i], i)); buckets[index].AddRange(group); repos[index]++;
        }
        return buckets.Select(b => b.OrderBy(t => t.Candidate.TaskId, PythonCodePointComparer.Instance).ToArray()).ToArray();
    }
}
