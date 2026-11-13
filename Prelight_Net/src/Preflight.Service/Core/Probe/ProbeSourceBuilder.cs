using Preflight.Models;
using Preflight.Service.Core.PyCompat;

namespace Preflight.Service.Core.Probe;

/// <summary>Port của runner._probe_source và rerun_fixed_probe._probe_source; không parse hay compile Java.</summary>
public static class ProbeSourceBuilder
{
    public static string Build(JavaClass java, JavaApi api, string className, string framework, ProbeProfile profile = ProbeProfile.v1, string source = "")
    {
        string package = java.Package.Length > 0 ? $"package {java.Package};\n\n" : "";
        string throws = profile == ProbeProfile.v2 ? " throws Throwable" : "";
        var header = framework switch
        {
            "junit5" => ("import org.junit.jupiter.api.Test;\n\n", "@Test\n    void probe()" + throws),
            "junit4" => ("import org.junit.Test;\n\n", "@Test\n    public void probe()" + throws),
            "testng" => ("import org.testng.annotations.Test;\n\n", "@Test\n    public void probe()" + throws),
            _ => ("", "void probe()" + throws)
        };
        bool overloaded = profile == ProbeProfile.v2 && java.Apis.Count(other => other.Kind == api.Kind && other.Name == api.Name && ProbeArguments.SplitParameters(other.Parameters).Length == ProbeArguments.SplitParameters(api.Parameters).Length) > 1;
        string arguments;
        if (overloaded)
        {
            string imports = string.Concat(SplitLines(source).Where(line => PyRegex.Create(@"^\s*import\s+[\w.*]+\s*;").IsMatch(line)).Select(line => PyStr.Strip(line) + "\n"));
            package += imports + (imports.Length > 0 ? "\n" : "");
            var nested = PyRegex.Create(@"\b(?:class|interface|enum|record)\s+(\w+)").Matches(source).Select(m => m.Groups[1].Value).Where(name => name != java.Name).ToHashSet(StringComparer.Ordinal);
            arguments = ProbeArguments.CastArguments(api.Parameters, java.Name, nested);
        }
        else arguments = ProbeArguments.PrimitiveArguments(api.Parameters);
        string call = api.Kind == "constructor" ? $"new {java.Name}({arguments});"
            : api.IsStatic ? $"{java.Name}.{api.Name}({arguments});"
            : $"{java.Name} value = null;\n        value.{api.Name}({arguments});";
        return $"{package}{header.Item1}final class {className} {{\n    {header.Item2} {{\n        {call}\n    }}\n}}\n";
    }
    private static string[] SplitLines(string source) => PyRegex.Create(@"\r\n|[\n\r\v\f\x1c-\x1e\x85\u2028\u2029]").Split(source);
}
