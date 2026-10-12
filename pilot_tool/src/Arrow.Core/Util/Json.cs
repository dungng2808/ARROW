using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Arrow.Core.Util;

/// <summary>JSON dùng chung: snake_case, UTF-8 không BOM, xuống dòng LF (specs/01).</summary>
public static class Json
{
    public static readonly JsonSerializerOptions Options = Create(indented: true);
    public static readonly JsonSerializerOptions Compact = Create(indented: false);

    private static JsonSerializerOptions Create(bool indented)
    {
        var options = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower,
            DictionaryKeyPolicy = null,
            WriteIndented = indented,
            DefaultIgnoreCondition = JsonIgnoreCondition.Never,
            Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
            ReadCommentHandling = JsonCommentHandling.Skip,
            AllowTrailingCommas = true,
        };
        return options;
    }

    public static T Read<T>(string path)
    {
        var text = File.ReadAllText(path, Encoding.UTF8);
        return JsonSerializer.Deserialize<T>(text, Options)
               ?? throw new InvalidDataException($"File JSON rỗng: {path}");
    }

    public static string Serialize<T>(T value, bool indented = true) =>
        JsonSerializer.Serialize(value, indented ? Options : Compact).Replace("\r\n", "\n");

    public static T Deserialize<T>(string text) =>
        JsonSerializer.Deserialize<T>(text, Options) ?? throw new InvalidDataException("JSON rỗng");

    /// <summary>Ghi file JSON nguyên tử (ghi .tmp rồi đổi tên).</summary>
    public static void WriteAtomic<T>(string path, T value) =>
        AtomicFile.WriteAllText(path, Serialize(value) + "\n");
}
