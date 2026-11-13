using Preflight.Service.Core.PyCompat;

namespace Preflight.Service.Core.Probe;

/// <summary>Port của java_ast.primitive_arguments và các helper v2 trong rerun_fixed_probe.py.</summary>
public static class ProbeArguments
{
    private static readonly System.Text.RegularExpressions.Regex Annotation = PyRegex.Create(@"@[\w.]+(?:\([^)]*\))?");
    private static readonly Dictionary<string, string> Primitives = new(StringComparer.Ordinal)
    {
        ["boolean"] = "false", ["byte"] = "(byte) 0", ["short"] = "(short) 0", ["int"] = "0",
        ["long"] = "0L", ["float"] = "0F", ["double"] = "0D", ["char"] = "'\\0'"
    };
    private static string Content(string parameters) => PyStr.Strip(PyStr.RemoveSuffix(PyStr.RemovePrefix(PyStr.Strip(parameters), "("), ")"));
    public static string PrimitiveArguments(string parameters)
    {
        string content = Content(parameters);
        if (content.Length == 0) return "";
        return string.Join(", ", PyRegex.Create(@",(?![^<]*>)").Split(content).Select(chunk =>
        {
            string compact = PyStr.Strip(Annotation.Replace(PyStr.Strip(chunk), "").Replace("final ", "", StringComparison.Ordinal));
            string type = compact.Contains(' ') ? PyStr.RSplit(compact, " ", 1)[0] : compact;
            return Primitives.GetValueOrDefault(PyStr.Strip(type.Replace("...", "[]", StringComparison.Ordinal)), "null");
        }));
    }
    public static string[] SplitParameters(string parameters)
    {
        string content = Content(parameters);
        if (content.Length == 0) return [];
        var chunks = new List<string>();
        int depth = 0, start = 0;
        for (int i = 0; i < content.Length; i++)
        {
            depth += content[i] == '<' ? 1 : content[i] == '>' ? -1 : 0;
            if (content[i] == ',' && depth == 0) { chunks.Add(PyStr.Strip(content[start..i])); start = i + 1; }
        }
        chunks.Add(PyStr.Strip(content[start..]));
        return chunks.ToArray();
    }
    public static string ErasedType(string chunk)
    {
        string compact = PyStr.Strip(Annotation.Replace(chunk, "").Replace("final ", "", StringComparison.Ordinal));
        string type = compact.Contains(' ') ? PyStr.RSplit(compact, " ", 1)[0] : compact;
        type = type.Replace("...", "[]", StringComparison.Ordinal);
        var generic = PyRegex.Create(@"<[^<>]*>");
        while (type.Contains('<'))
        {
            type = generic.Replace(type, "");
        }
        return type.Replace(" ", "", StringComparison.Ordinal);
    }
    public static string CastArguments(string parameters, string outer, IReadOnlySet<string> nested)
    {
        return string.Join(", ", SplitParameters(parameters).Select(chunk =>
        {
            string erased = ErasedType(chunk);
            if (nested.Contains(PyStr.RemoveSuffix(PyStr.Split(erased, ".", 1)[0], "[]"))) erased = outer + "." + erased;
            string primitive = PrimitiveArguments($"({erased} x)");
            if (primitive != "null") return primitive;
            if (PyRegex.Create(@"\A[A-Z][A-Z0-9_]?\z").IsMatch(PyStr.RemoveSuffix(erased, "[]")))
                return erased.EndsWith("[]", StringComparison.Ordinal) ? "(Object[]) null" : "(Object) null";
            return $"({erased}) null";
        }));
    }
}
