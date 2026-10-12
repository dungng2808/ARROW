using System.Collections;
using System.Text;
using System.Text.RegularExpressions;
using Arrow.Core.Util;

namespace Arrow.Core.Prompts;

/// <summary>Template đã đọc: phần đầu YAML (<c>id</c>, <c>version</c>, <c>role</c>) và thân.</summary>
public sealed record PromptTemplate(string Id, string Version, string Role, string Body, string Sha256, string RelativePath);

/// <summary>
/// Template engine tự viết (specs/04 Mục 2): chỉ <c>{{bien}}</c>, <c>{{#if bien}}…{{/if}}</c>, <c>{{#each ds}}…{{/each}}</c>.
/// Biến thiếu → lỗi ngay, không thay bằng rỗng. Trong <c>#each</c>, phần tử là từ điển; có thêm biến <c>index</c> (từ 1).
/// </summary>
public static partial class TemplateEngine
{
    public static PromptTemplate Parse(string text, string relativePath)
    {
        var normalized = text.Replace("\r\n", "\n");
        var sha = Hashing.Sha256Text(normalized);
        if (!normalized.StartsWith("---\n", StringComparison.Ordinal))
        {
            throw new ArrowException("PROMPT_INVALID", $"{relativePath}: thiếu phần đầu YAML (---)");
        }

        var end = normalized.IndexOf("\n---\n", 4, StringComparison.Ordinal);
        if (end < 0)
        {
            throw new ArrowException("PROMPT_INVALID", $"{relativePath}: phần đầu YAML chưa đóng");
        }

        var header = normalized[4..end].Split('\n')
            .Select(l => l.Split(':', 2))
            .Where(p => p.Length == 2)
            .ToDictionary(p => p[0].Trim(), p => p[1].Trim().Trim('"'));
        var body = normalized[(end + 5)..];
        // Bỏ một dòng trống đầu và xuống dòng cuối để prompt không có dòng thừa.
        body = body.TrimStart('\n').TrimEnd('\n');
        return new PromptTemplate(header.GetValueOrDefault("id", ""), header.GetValueOrDefault("version", ""), header.GetValueOrDefault("role", "user"),
            body, sha, relativePath);
    }

    public static string Render(string template, IReadOnlyDictionary<string, object?> values) => RenderBlock(template, values, 0);

    [GeneratedRegex(@"\{\{(#if|#each)\s+([\w.]+)\s*\}\}|\{\{(/if|/each)\}\}|\{\{\s*([\w.]+)\s*\}\}")]
    private static partial Regex Token();

    private static string RenderBlock(string template, IReadOnlyDictionary<string, object?> values, int depth)
    {
        if (depth > 10)
        {
            throw new ArrowException("PROMPT_INVALID", "Template lồng quá sâu");
        }

        var output = new StringBuilder();
        var position = 0;
        while (position < template.Length)
        {
            var match = Token().Match(template, position);
            if (!match.Success)
            {
                output.Append(template, position, template.Length - position);
                break;
            }

            output.Append(template, position, match.Index - position);
            if (match.Groups[4].Success)
            {
                output.Append(Format(Lookup(values, match.Groups[4].Value)));
                position = match.Index + match.Length;
                continue;
            }

            if (match.Groups[3].Success)
            {
                throw new ArrowException("PROMPT_INVALID", $"Thẻ đóng {{{{{match.Groups[3].Value}}}}} thừa");
            }

            var kind = match.Groups[1].Value;
            var name = match.Groups[2].Value;
            var (inner, after) = FindBlock(template, match.Index + match.Length, kind == "#if" ? "if" : "each");
            var value = Lookup(values, name);
            if (kind == "#if")
            {
                if (IsTruthy(value))
                {
                    output.Append(RenderBlock(inner, values, depth + 1));
                }
            }
            else
            {
                if (value is not IEnumerable items || value is string)
                {
                    throw new ArrowException("PROMPT_INVALID", $"{{{{#each {name}}}}} cần một danh sách");
                }

                var index = 0;
                foreach (var item in items)
                {
                    index++;
                    var scope = new Dictionary<string, object?>(values);
                    if (item is IReadOnlyDictionary<string, object?> fields)
                    {
                        foreach (var (key, field) in fields)
                        {
                            scope[key] = field;
                        }
                    }
                    else
                    {
                        scope["item"] = item;
                    }

                    scope["index"] = index;
                    output.Append(RenderBlock(inner, scope, depth + 1));
                }
            }

            position = after;
        }

        return output.ToString();
    }

    /// <summary>Tìm thân khối và vị trí sau thẻ đóng tương ứng (cho phép lồng cùng loại).</summary>
    private static (string Inner, int After) FindBlock(string template, int start, string kind)
    {
        var open = "{{#" + kind;
        var close = "{{/" + kind + "}}";
        var level = 1;
        var position = start;
        while (true)
        {
            var nextClose = template.IndexOf(close, position, StringComparison.Ordinal);
            if (nextClose < 0)
            {
                throw new ArrowException("PROMPT_INVALID", $"Thiếu {close}");
            }

            var nextOpen = template.IndexOf(open, position, StringComparison.Ordinal);
            if (nextOpen >= 0 && nextOpen < nextClose)
            {
                level++;
                position = nextOpen + open.Length;
                continue;
            }

            level--;
            if (level == 0)
            {
                var inner = template[start..nextClose];
                var after = nextClose + close.Length;
                // Khối đứng riêng một dòng: bỏ xuống dòng ngay sau thẻ mở và thẻ đóng.
                if (inner.StartsWith('\n'))
                {
                    inner = inner[1..];
                }

                if (after < template.Length && template[after] == '\n' && (inner.Length == 0 || inner.EndsWith('\n')))
                {
                    after++;
                }

                return (inner, after);
            }

            position = nextClose + close.Length;
        }
    }

    private static object? Lookup(IReadOnlyDictionary<string, object?> values, string name)
    {
        if (!values.TryGetValue(name, out var value))
        {
            throw new ArrowException("PROMPT_VARIABLE_MISSING", $"Template cần biến '{name}' nhưng không có");
        }

        return value;
    }

    private static bool IsTruthy(object? value) => value switch
    {
        null => false,
        bool b => b,
        string s => s.Length > 0,
        ICollection c => c.Count > 0,
        _ => true,
    };

    private static string Format(object? value) => value switch
    {
        null => "",
        string s => s,
        IFormattable f => f.ToString(null, System.Globalization.CultureInfo.InvariantCulture),
        _ => value.ToString() ?? "",
    };
}
