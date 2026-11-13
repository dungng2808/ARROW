using System.Text;

namespace Preflight.DataAccess;

/// <summary>Reader CSV hỗ trợ BOM, escaped quote và trường nhiều dòng.</summary>
public static class CsvTable
{
    public static IReadOnlyList<Dictionary<string, string>> Read(string path)
    {
        using var reader = new StreamReader(path, new UTF8Encoding(false, true), true);
        string text = reader.ReadToEnd(); var rows = new List<List<string>>(); var row = new List<string>(); var value = new StringBuilder(); bool quoted = false;
        for (int i = 0; i < text.Length; i++)
        {
            char c = text[i];
            if (c == '"' && (quoted || value.Length == 0)) { if (quoted && i + 1 < text.Length && text[i + 1] == '"') { value.Append('"'); i++; } else quoted = !quoted; }
            else if (!quoted && c == ',') { row.Add(value.ToString()); value.Clear(); }
            else if (!quoted && c is '\r' or '\n') { if (c == '\r' && i + 1 < text.Length && text[i + 1] == '\n') i++; if (row.Count > 0 || value.Length > 0) { row.Add(value.ToString()); rows.Add(row); } row = []; value.Clear(); }
            else value.Append(c);
        }
        if (row.Count > 0 || value.Length > 0) { row.Add(value.ToString()); rows.Add(row); }
        if (rows.Count == 0) return [];
        return rows.Skip(1).Select(r => rows[0].Select((key, i) => (key, Value: i < r.Count ? r[i].Trim() : "")).GroupBy(x => x.key, StringComparer.Ordinal).ToDictionary(g => g.Key, g => g.Last().Value, StringComparer.Ordinal)).ToArray();
    }
}
