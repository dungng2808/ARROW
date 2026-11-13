using System.Globalization;
using System.Text;
using System.Text.Json;

namespace Preflight.DataAccess;

/// <summary>Port của truthiness, str(value or "") và strip khi đọc JSON Python.</summary>
public static class PythonValue
{
    public static bool IsSpace(char value) => char.IsWhiteSpace(value) || value is >= '\x1c' and <= '\x1f';
    public static string Strip(string value)
    {
        int start = 0, end = value.Length;
        while (start < end && IsSpace(value[start])) start++;
        while (end > start && IsSpace(value[end - 1])) end--;
        return value[start..end];
    }
    public static bool Truthy(JsonElement value) => value.ValueKind switch
    {
        JsonValueKind.Undefined or JsonValueKind.Null or JsonValueKind.False => false,
        JsonValueKind.String => value.GetString()!.Length > 0,
        JsonValueKind.Number => value.GetDouble() != 0,
        JsonValueKind.Array => value.GetArrayLength() > 0,
        JsonValueKind.Object => value.EnumerateObject().Any(), _ => true
    };
    public static string StringOrEmpty(JsonElement value) => Truthy(value) ? Str(value) : "";
    public static string Str(JsonElement value) => value.ValueKind switch
    {
        JsonValueKind.String => value.GetString()!,
        JsonValueKind.True => "True", JsonValueKind.False => "False",
        JsonValueKind.Null or JsonValueKind.Undefined => "None",
        JsonValueKind.Array => "[" + string.Join(", ", value.EnumerateArray().Select(Repr)) + "]",
        JsonValueKind.Object => "{" + string.Join(", ", value.EnumerateObject().Select(p => ReprString(p.Name) + ": " + Repr(p.Value))) + "}",
        _ => value.GetRawText().IndexOfAny(['.', 'e', 'E']) >= 0 ? PythonJson.FloatRepr(value.GetDouble()) : value.GetRawText()
    };
    private static string Repr(JsonElement value) => value.ValueKind == JsonValueKind.String ? ReprString(value.GetString()!) : Str(value);
    private static string ReprString(string value)
    {
        char quote = value.Contains('\'') && !value.Contains('"') ? '"' : '\'';
        var text = new StringBuilder().Append(quote);
        foreach (var rune in value.EnumerateRunes())
        {
            int code = rune.Value;
            if (code == quote || code == '\\') text.Append('\\').Append(rune.ToString());
            else if (code is '\n' or '\r' or '\t') text.Append(code == '\n' ? "\\n" : code == '\r' ? "\\r" : "\\t");
            else if (code != ' ' && Rune.GetUnicodeCategory(rune) is UnicodeCategory.Control or UnicodeCategory.Format or UnicodeCategory.LineSeparator or UnicodeCategory.ParagraphSeparator or UnicodeCategory.SpaceSeparator or UnicodeCategory.OtherNotAssigned)
                text.Append(code < 256 ? "\\x" + code.ToString("x2", CultureInfo.InvariantCulture) : code < 65536 ? "\\u" + code.ToString("x4", CultureInfo.InvariantCulture) : "\\U" + code.ToString("x8", CultureInfo.InvariantCulture));
            else text.Append(rune.ToString());
        }
        return text.Append(quote).ToString();
    }
}
