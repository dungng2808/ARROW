using System.Text;
using System.Text.Json;

namespace Preflight.DataAccess;

/// <summary>Port của cli._write_jsonl/_write_csv và checkpoint.atomic_write_json.</summary>
public static class ManifestWriter
{
    public static void WriteJson<T>(string path, T value) => AtomicWrite(path, writer => writer.Write(PythonJson.Serialize(value, indented: true) + "\n"));
    public static void WriteJsonl<T>(string path, IEnumerable<T> records) => AtomicWrite(path, writer =>
    {
        foreach (T record in records) writer.Write(PythonJson.Serialize(record) + "\n");
    });
    public static void WriteCsv<T>(string path, IEnumerable<T> records)
    {
        var rows = records.Select(PythonJson.Element).ToArray();
        string[] fields = rows.SelectMany(r => r.EnumerateObject().Select(p => p.Name)).Distinct(StringComparer.Ordinal).Order(StringComparer.Ordinal).ToArray();
        AtomicWrite(path, writer =>
        {
            void Row(IEnumerable<string> values) => writer.Write(string.Join(',', values.Select(CsvCell)) + "\r\n");
            Row(fields);
            foreach (var row in rows) Row(fields.Select(field => row.TryGetProperty(field, out var value) ? Flat(value) : ""));
        });
    }
    private static string Flat(JsonElement value) => value.ValueKind switch
    {
        JsonValueKind.Array or JsonValueKind.Object => PythonJson.Render(value, compact: true),
        JsonValueKind.String => value.GetString()!, JsonValueKind.True => "True", JsonValueKind.False => "False",
        JsonValueKind.Null or JsonValueKind.Undefined => "", _ => PythonJson.Render(value)
    };
    private static string CsvCell(string value) => value.IndexOfAny([',', '"', '\r', '\n']) >= 0 ? "\"" + value.Replace("\"", "\"\"", StringComparison.Ordinal) + "\"" : value;
    private static void AtomicWrite(string path, Action<StreamWriter> write)
    {
        path = Path.GetFullPath(path);
        Directory.CreateDirectory(Path.GetDirectoryName(path)!);
        string temporary = path + ".tmp";
        using (var stream = new FileStream(temporary, FileMode.Create, FileAccess.Write, FileShare.None))
        {
            using var writer = new StreamWriter(stream, new UTF8Encoding(false), leaveOpen: true);
            write(writer); writer.Flush(); stream.Flush(flushToDisk: true);
        }
        File.Move(temporary, path, overwrite: true);
    }
}
