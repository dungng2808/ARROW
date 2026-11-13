using System.Globalization;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Preflight.DataAccess;

/// <summary>Port của json.dumps trong cli.py/checkpoint.py/util.py; định dạng JSON Python.</summary>
public static class PythonJson
{
    public static JsonSerializerOptions Options { get; } = CreateOptions();
    private static JsonSerializerOptions CreateOptions()
    {
        var result = new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower, PropertyNameCaseInsensitive = false, MaxDepth = 1000 };
        result.Converters.Add(new JsonStringEnumConverter());
        result.Converters.Add(new PythonDoubleConverter());
        return result;
    }
    public static JsonElement Element<T>(T value) => JsonSerializer.SerializeToElement(value, Options);
    public static string Serialize<T>(T value, bool compact = false, bool indented = false) => Render(Element(value), compact, indented);
    public static string Render(JsonElement value, bool compact = false, bool indented = false, bool sortKeys = true)
    {
        var text = new StringBuilder();
        Write(text, value, compact, indented, 0, sortKeys);
        return text.ToString();
    }
    private static void Write(StringBuilder text, JsonElement value, bool compact, bool indented, int depth, bool sortKeys)
    {
        string separator = compact ? ":" : ": ";
        void Break(int level) { if (indented) text.Append('\n').Append(' ', level * 2); }
        switch (value.ValueKind)
        {
            case JsonValueKind.Object:
                text.Append('{');
                var unique = value.EnumerateObject().GroupBy(p => p.Name, StringComparer.Ordinal).Select(g => g.Last());
                var properties = (sortKeys ? unique.OrderBy(p => p.Name, PythonCodePointComparer.Instance) : unique).ToArray();
                for (int i = 0; i < properties.Length; i++)
                {
                    if (i > 0) text.Append(indented || compact ? "," : ", ");
                    Break(depth + 1);
                    text.Append(Quote(properties[i].Name)).Append(separator);
                    Write(text, properties[i].Value, compact, indented, depth + 1, sortKeys);
                }
                if (properties.Length > 0) Break(depth);
                text.Append('}'); break;
            case JsonValueKind.Array:
                text.Append('['); int index = 0;
                foreach (var item in value.EnumerateArray())
                {
                    if (index++ > 0) text.Append(indented || compact ? "," : ", ");
                    Break(depth + 1); Write(text, item, compact, indented, depth + 1, sortKeys);
                }
                if (index > 0) Break(depth);
                text.Append(']'); break;
            case JsonValueKind.String: text.Append(Quote(value.GetString()!)); break;
            case JsonValueKind.Number:
                string raw = value.GetRawText();
                text.Append(raw.IndexOfAny(['.', 'e', 'E']) >= 0 ? FloatRepr(value.GetDouble()) : raw); break;
            case JsonValueKind.True: text.Append("true"); break;
            case JsonValueKind.False: text.Append("false"); break;
            default: text.Append("null"); break;
        }
    }
    public static string Quote(string value)
    {
        var text = new StringBuilder("\"");
        foreach (char c in value)
            text.Append(c switch
            {
                '"' => "\\\"", '\\' => "\\\\", '\b' => "\\b", '\f' => "\\f", '\n' => "\\n", '\r' => "\\r", '\t' => "\\t",
                < ' ' => "\\u" + ((int)c).ToString("x4", CultureInfo.InvariantCulture), _ => c.ToString()
            });
        return text.Append('"').ToString();
    }
    public static string FloatRepr(double value)
    {
        if (!double.IsFinite(value)) throw new ArgumentOutOfRangeException(nameof(value));
        string raw = value.ToString("R", CultureInfo.InvariantCulture).ToLowerInvariant();
        int e = raw.IndexOf('e');
        if (e >= 0)
        {
            int power = int.Parse(raw[(e + 1)..], CultureInfo.InvariantCulture);
            string mantissa = raw[..e];
            if (power is >= -4 and < 16)
                return value.ToString("0.0###############################", CultureInfo.InvariantCulture);
            return mantissa + "e" + (power < 0 ? "-" : "+") + Math.Abs(power).ToString("D2", CultureInfo.InvariantCulture);
        }
        // Python chuyển số lớn sang dạng khoa học ở exponent 16.
        string digits = raw.TrimStart('-').Replace(".", "", StringComparison.Ordinal);
        int point = raw.TrimStart('-').IndexOf('.');
        int exponent = (point < 0 ? raw.TrimStart('-').Length : point) - 1;
        if (exponent >= 16)
        {
            digits = digits.TrimEnd('0');
            return (value < 0 ? "-" : "") + digits[0] + (digits.Length > 1 ? "." + digits[1..] : "") + "e+" + exponent;
        }
        return raw.Contains('.') ? raw : raw + ".0";
    }
    private sealed class PythonDoubleConverter : JsonConverter<double>
    {
        public override double Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options) => reader.GetDouble();
        public override void Write(Utf8JsonWriter writer, double value, JsonSerializerOptions options) => writer.WriteRawValue(FloatRepr(value));
    }
}
