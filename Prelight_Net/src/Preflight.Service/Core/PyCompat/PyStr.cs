using System.Text.RegularExpressions;

namespace Preflight.Service.Core.PyCompat;

/// <summary>Port của str.isspace: Unicode White_Space cộng U+001C..U+001F.</summary>
public static class PyIsSpace
{
    public static bool IsSpace(char value) => Preflight.DataAccess.PythonValue.IsSpace(value);
}

/// <summary>Port của str.strip, removeprefix, removesuffix, rstrip, split và rsplit.</summary>
public static class PyStr
{
    public static string Strip(string value)
    {
        return Preflight.DataAccess.PythonValue.Strip(value);
    }
    public static string RemovePrefix(string value, string prefix) => value.StartsWith(prefix, StringComparison.Ordinal) ? value[prefix.Length..] : value;
    public static string RemoveSuffix(string value, string suffix) => suffix.Length > 0 && value.EndsWith(suffix, StringComparison.Ordinal) ? value[..^suffix.Length] : value;
    public static string RStrip(string value, string chars)
    {
        int end = value.Length;
        var trim = chars.EnumerateRunes().Select(r => r.Value).ToHashSet();
        while (end > 0)
        {
            System.Text.Rune.DecodeLastFromUtf16(value.AsSpan(0, end), out var rune, out int consumed);
            if (!trim.Contains(rune.Value)) break;
            end -= consumed;
        }
        return value[..end];
    }
    public static string[] Split(string value, string separator, int maxSplit = -1) => SplitCore(value, separator, maxSplit, false);
    public static string[] RSplit(string value, string separator, int maxSplit = -1) => SplitCore(value, separator, maxSplit, true);
    private static string[] SplitCore(string value, string separator, int maxSplit, bool right)
    {
        if (separator.Length == 0) throw new ArgumentException("empty separator", nameof(separator));
        var result = new List<string>();
        while (maxSplit != 0)
        {
            int at = right ? value.LastIndexOf(separator, StringComparison.Ordinal) : value.IndexOf(separator, StringComparison.Ordinal);
            if (at < 0) break;
            result.Add(right ? value[(at + separator.Length)..] : value[..at]);
            value = right ? value[..at] : value[(at + separator.Length)..];
            if (maxSplit > 0) maxSplit--;
        }
        result.Add(value);
        if (right) result.Reverse();
        return result.ToArray();
    }
}

/// <summary>Port các pattern re của Python; điều chỉnh \w, \b, \s về tập ký tự Python.</summary>
public static class PyRegex
{
    private static readonly string Word = "(?:" + PythonUnicodeData.WordBmp + "|" + PythonUnicodeData.WordExtra + ")";
    private static readonly string Digit = "(?:" + PythonUnicodeData.DigitBmp + "|" + PythonUnicodeData.DigitExtra + ")";
    private static readonly System.Collections.Concurrent.ConcurrentDictionary<(string, RegexOptions), Regex> Cache = new();
    public static string Translate(string pattern)
    {
        var result = new System.Text.StringBuilder();
        for (int i = 0; i < pattern.Length; i++)
        {
            char c = pattern[i];
            if (c == '[')
            {
                int end = i + 1;
                while (end < pattern.Length)
                {
                    if (pattern[end] == '\\') { end += 2; continue; }
                    if (pattern[end] == ']') break;
                    end++;
                }
                string content = pattern[(i + 1)..end];
                bool word = content.Contains(@"\w", StringComparison.Ordinal), digit = content.Contains(@"\d", StringComparison.Ordinal);
                content = content.Replace(@"\w", PythonUnicodeData.WordBmp[1..^1], StringComparison.Ordinal)
                    .Replace(@"\d", PythonUnicodeData.DigitBmp[1..^1], StringComparison.Ordinal)
                    .Replace(@"\s", @"\s\x1c-\x1f", StringComparison.Ordinal);
                if ((word || digit) && content.StartsWith('^')) throw new NotSupportedException("Negated Unicode word/digit classes are not used by P0/P1 Python patterns");
                string translated = "[" + content + "]";
                if (word || digit) translated = "(?:" + translated + (word ? "|" + PythonUnicodeData.WordExtra : "") + (digit ? "|" + PythonUnicodeData.DigitExtra : "") + ")";
                result.Append(translated); i = end; continue;
            }
            if (c == '\\' && i + 1 < pattern.Length)
            {
                char escape = pattern[++i];
                result.Append(escape switch
                {
                    'w' => Word,
                    'd' => Digit,
                    'b' => $@"(?:(?<={Word})(?!{Word})|(?<!{Word})(?={Word}))",
                    's' => @"[\s\x1c-\x1f]",
                    _ => "\\" + escape
                });
            }
            else
            {
                result.Append(c);
            }
        }
        return result.ToString();
    }
    public static Regex Create(string pattern, RegexOptions options = RegexOptions.None) => Cache.GetOrAdd((pattern, options), key => new(Translate(key.Item1), key.Item2 | RegexOptions.CultureInvariant, TimeSpan.FromSeconds(10)));
}
