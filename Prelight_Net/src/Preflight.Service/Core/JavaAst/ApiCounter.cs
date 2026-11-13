using Preflight.Models;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Probe;
using Preflight.Service.Core.PyCompat;

namespace Preflight.Service.Core.JavaAst;

/// <summary>Port của runner._resolve_parent_apis và phép gộp API trong preflight_one.</summary>
public sealed class ApiCounter(JavaSourceParser parser)
{
    public IReadOnlyList<JavaApi> Count(JavaClass java, string classFile, string workspace)
    {
        var own = java.Apis.Select(a => a.Name).ToHashSet(StringComparer.Ordinal);
        return java.Apis.Concat(Parents(java, classFile, workspace, new(StringComparer.Ordinal)).Where(a => !own.Contains(a.Name))).ToArray();
    }
    private IEnumerable<JavaApi> Parents(JavaClass java, string classFile, string workspace, HashSet<string> seen)
    {
        if (java.Extends.Length == 0) return [];
        string name = PyStr.Strip(PyRegex.Create("<.*").Replace(java.Extends, "").Split('.').Last());
        if (name.Length == 0 || name == "Object" || !seen.Add(name)) return [];
        string root = TestSourceRoot.Find(classFile, workspace).Replace(Path.Combine("src", "test", "java"), Path.Combine("src", "main", "java"), StringComparison.Ordinal);
        var paths = new List<string> { Path.Combine(root, java.Package.Replace('.', '/'), name + ".java") };
        foreach (string imported in java.Imports)
            if (PyStr.RStrip(imported, ".*").EndsWith(name, StringComparison.Ordinal)) paths.Add(Path.Combine(root, PyStr.RSplit(imported, ".", 1)[0].Replace('.', '/'), name + ".java"));
        foreach (string path in paths)
        {
            if (!ResolvePath.IsWithin(ResolvePath.ResolveLinks(path), workspace) || !File.Exists(path)) continue;
            string source = System.Text.Encoding.UTF8.GetString(File.ReadAllBytes(path)).Replace("\r\n", "\n", StringComparison.Ordinal).Replace('\r', '\n');
            var parent = parser.Parse(source, path.Replace('\\', '/'));
            if (parent is null) continue;
            return parent.Apis.Where(a => a.Kind == "method" && (parent.Modifiers.Contains("public") || parent.Package == java.Package)).Concat(Parents(parent, path, workspace, seen)).ToArray();
        }
        return [];
    }
}
