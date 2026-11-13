using System.Text;
using System.Text.Json;
using System.Text.RegularExpressions;
using Preflight.Models;

namespace Preflight.DataAccess;

/// <summary>Port của revision.load_revision_map (CSV/JSONL, duplicate và audit).</summary>
public static class RevisionMapReader
{
    public static IReadOnlyDictionary<string, RevisionChoice> Load(string? path)
    {
        var result = new Dictionary<string, RevisionChoice>(StringComparer.Ordinal);
        if (path is null) return result;
        IEnumerable<Dictionary<string, string>> entries;
        if (Path.GetExtension(path).Equals(".csv", StringComparison.OrdinalIgnoreCase))
        {
            string text = new UTF8Encoding(false, true).GetString(File.ReadAllBytes(path));
            if (text.StartsWith('\ufeff')) text = text[1..];
            var rows = CsvRows(text).Where(row => row.Count > 0).ToArray();
            entries = rows.Length == 0 ? [] : rows.Skip(1).Select(row =>
            {
                var entry = new Dictionary<string, string>(StringComparer.Ordinal);
                for (int i = 0; i < rows[0].Count; i++) entry[rows[0][i]] = i < row.Count ? row[i] : "";
                return entry;
            });
        }
        else
        {
            string text = new UTF8Encoding(false, true).GetString(File.ReadAllBytes(path));
            entries = Regex.Split(text.Replace("\r\n", "\n", StringComparison.Ordinal).Replace('\r', '\n'), @"[\n\v\f\x1c-\x1e\x85\u2028\u2029]").Where(line => PythonValue.Strip(line).Length > 0).Select(line =>
            {
                using var doc = JsonDocument.Parse(line, new JsonDocumentOptions { MaxDepth = 1000 });
                return doc.RootElement.EnumerateObject().GroupBy(p => p.Name, StringComparer.Ordinal).ToDictionary(g => g.Key, g => PythonValue.StringOrEmpty(g.Last().Value), StringComparer.Ordinal);
            });
        }
        foreach (var entry in entries)
        {
            string taskId = entry.GetValueOrDefault("task_id", ""), sha = entry.GetValueOrDefault("checkout_sha", "");
            string status = entry.GetValueOrDefault("revision_verification_status", ""), provenance = entry.GetValueOrDefault("revision_provenance", "");
            string evidence = PythonValue.Strip(entry.GetValueOrDefault("evidence_ref", ""));
            if (taskId.Length == 0 || !Regex.IsMatch(sha, @"\A[0-9a-fA-F]{40}\z", RegexOptions.CultureInvariant) || status != "UPSTREAM_PINNED")
                throw new ArgumentException("revision map requires task_id, full 40-character hexadecimal checkout_sha, and UPSTREAM_PINNED status");
            if (provenance is not ("upstream_metadata" or "dataset_record")) throw new ArgumentException($"{taskId}: UPSTREAM_PINNED requires upstream_metadata or dataset_record provenance");
            if (evidence.Length == 0) throw new ArgumentException($"{taskId}: UPSTREAM_PINNED requires a non-empty evidence_ref");
            var choice = new RevisionChoice(sha.ToLowerInvariant(), provenance, RevisionStatus.UPSTREAM_PINNED, EvidenceRef: evidence);
            if (result.TryGetValue(taskId, out var previous) && previous != choice) throw new ArgumentException($"{taskId}: conflicting duplicate revision map entry");
            result[taskId] = choice;
        }
        return result;
    }
    private static IEnumerable<List<string>> CsvRows(string text)
    {
        var row = new List<string>(); var field = new StringBuilder(); bool quoted = false, started = false;
        for (int i = 0; i < text.Length; i++)
        {
            char c = text[i];
            if (c == '"' && (quoted || field.Length == 0))
            {
                started = true;
                if (quoted && i + 1 < text.Length && text[i + 1] == '"') { field.Append('"'); i++; }
                else quoted = !quoted;
            }
            else if (c == ',' && !quoted) { row.Add(field.ToString()); field.Clear(); started = true; }
            else if (c is '\r' or '\n' && !quoted)
            {
                if (c == '\r' && i + 1 < text.Length && text[i + 1] == '\n') i++;
                if (started || field.Length > 0 || row.Count > 0) { row.Add(field.ToString()); yield return row; }
                row = []; field.Clear(); started = false;
            }
            else { field.Append(c); started = true; }
        }
        if (started || row.Count > 0) { row.Add(field.ToString()); yield return row; }
    }
}
