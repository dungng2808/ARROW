using System.Collections.Concurrent;
using System.Security.Cryptography;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Arrow.DataAccess.Common;

/// <summary>JSON dùng chung: snake_case, UTF-8 không BOM, xuống dòng LF, khóa của dictionary giữ nguyên (spec 00 Mục 4).</summary>
public static class JsonFile
{
    public static readonly JsonSerializerOptions Options = Create(indented: true);
    public static readonly JsonSerializerOptions Compact = Create(indented: false);

    private static JsonSerializerOptions Create(bool indented) => new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower,
        DictionaryKeyPolicy = null,
        WriteIndented = indented,
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
        ReadCommentHandling = JsonCommentHandling.Skip,
        AllowTrailingCommas = true,
        DefaultIgnoreCondition = JsonIgnoreCondition.Never,
    };

    public static string Serialize<T>(T value, bool indented = true) =>
        JsonSerializer.Serialize(value, indented ? Options : Compact).Replace("\r\n", "\n");

    public static T Deserialize<T>(string text) =>
        JsonSerializer.Deserialize<T>(text, Options) ?? throw new InvalidDataException("JSON rỗng");

    public static T Read<T>(string path) => Deserialize<T>(File.ReadAllText(path, Encoding.UTF8));

    public static T? TryRead<T>(string path) where T : class => File.Exists(path) ? Read<T>(path) : null;

    public static void Write<T>(string path, T value) => AtomicFile.WriteAllText(path, Serialize(value) + "\n");
}

/// <summary>Khóa theo đường dẫn file, để nhiều luồng không ghi chen nhau vào cùng một file.</summary>
public static class FileLocks
{
    private static readonly ConcurrentDictionary<string, object> Locks = new(StringComparer.Ordinal);

    public static object For(string path) => Locks.GetOrAdd(Path.GetFullPath(path), _ => new object());
}

/// <summary>Ghi nguyên tử: ghi <c>&lt;file&gt;.&lt;guid&gt;.tmp</c> rồi đổi tên. Tên tạm có guid nên hai luồng không đụng nhau; có khóa theo file.</summary>
public static class AtomicFile
{
    private static readonly UTF8Encoding Utf8NoBom = new(encoderShouldEmitUTF8Identifier: false);

    public static void WriteAllText(string path, string text)
    {
        var full = Path.GetFullPath(path);
        Directory.CreateDirectory(Path.GetDirectoryName(full)!);
        lock (FileLocks.For(full))
        {
            var temp = $"{full}.{Guid.NewGuid():N}.tmp";
            File.WriteAllText(temp, text.Replace("\r\n", "\n"), Utf8NoBom);
            for (var attempt = 1; ; attempt++)
            {
                try
                {
                    File.Move(temp, full, overwrite: true);
                    return;
                }
                catch (Exception ex) when (ex is IOException or UnauthorizedAccessException && attempt < 5)
                {
                    // Windows có thể tạm từ chối khi một tiến trình khác đang đọc file đích.
                    Thread.Sleep(50 * attempt);
                }
                catch
                {
                    TryDelete(temp);
                    throw;
                }
            }
        }
    }

    private static void TryDelete(string path)
    {
        try
        {
            File.Delete(path);
        }
        catch (IOException)
        {
        }
    }
}

/// <summary>File JSONL: thêm từng dòng (có khóa), đọc lại bỏ qua dòng cuối dở dang (ghi bị ngắt giữa chừng).</summary>
public static class JsonlFile
{
    public static void Append<T>(string path, T value)
    {
        var full = Path.GetFullPath(path);
        Directory.CreateDirectory(Path.GetDirectoryName(full)!);
        var line = JsonFile.Serialize(value, indented: false) + "\n";
        lock (FileLocks.For(full))
        {
            File.AppendAllText(full, line, new UTF8Encoding(false));
        }
    }

    public static List<T> ReadAll<T>(string path)
    {
        var result = new List<T>();
        if (!File.Exists(path))
        {
            return result;
        }

        string[] lines;
        lock (FileLocks.For(path))
        {
            lines = File.ReadAllLines(path, Encoding.UTF8);
        }

        for (var i = 0; i < lines.Length; i++)
        {
            if (string.IsNullOrWhiteSpace(lines[i]))
            {
                continue;
            }

            try
            {
                result.Add(JsonFile.Deserialize<T>(lines[i]));
            }
            catch (JsonException) when (i == lines.Length - 1)
            {
                // Dòng cuối ghi dở khi tiến trình bị dừng: bỏ qua.
            }
        }

        return result;
    }
}

public static class Hashing
{
    public static string Sha256Bytes(byte[] bytes) => Convert.ToHexString(SHA256.HashData(bytes)).ToLowerInvariant();

    public static string Sha256Text(string text) => Sha256Bytes(Encoding.UTF8.GetBytes(text));

    /// <summary>sha256 của file văn bản sau khi đổi CRLF thành LF, để cùng một file cho cùng hash trên macOS và Windows.</summary>
    public static string Sha256TextFile(string path) => Sha256Text(File.ReadAllText(path, Encoding.UTF8).Replace("\r\n", "\n"));
}

/// <summary>Ghi CSV theo RFC 4180 (bọc ngoặc kép khi có dấu phẩy, ngoặc kép, xuống dòng).</summary>
public static class CsvWriter
{
    public static string Format(IReadOnlyList<string> headers, IEnumerable<IReadOnlyList<string>> rows)
    {
        var text = new StringBuilder();
        AppendRow(text, headers);
        foreach (var row in rows)
        {
            if (row.Count != headers.Count)
            {
                throw new ArgumentException($"Dòng có {row.Count} cột, tiêu đề có {headers.Count} cột");
            }

            AppendRow(text, row);
        }

        return text.ToString();
    }

    private static void AppendRow(StringBuilder text, IReadOnlyList<string> cells)
    {
        for (var i = 0; i < cells.Count; i++)
        {
            if (i > 0)
            {
                text.Append(',');
            }

            var cell = cells[i] ?? "";
            text.Append(cell.IndexOfAny([',', '"', '\n', '\r']) >= 0 ? "\"" + cell.Replace("\"", "\"\"") + "\"" : cell);
        }

        text.Append('\n');
    }
}
