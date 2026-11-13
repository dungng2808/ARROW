using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;
using Preflight.Models;
using Preflight.DataAccess;
using Preflight.Service.Core.PyCompat;

namespace Preflight.Service.Core.JavaAst;

/// <summary>Port của java_ast.py bằng đúng tree-sitter core/grammar Java.</summary>
public sealed class JavaSourceParser
{
    private static readonly HashSet<string> Declarations = new(["class_declaration", "interface_declaration", "enum_declaration", "record_declaration", "annotation_type_declaration"], StringComparer.Ordinal);
    public JavaSourceParser(string? library = null) => TreeSitterNative.Load(library);
    public JavaClass? Parse(string text, string sourcePath, string? expectedName = null)
    {
        byte[] source = Encoding.UTF8.GetBytes(text);
        IntPtr parser = TreeSitterNative.ts_parser_new(), tree = IntPtr.Zero;
        try
        {
            if (!TreeSitterNative.ts_parser_set_language(parser, TreeSitterNative.tree_sitter_java())) throw new InvalidDataException("tree-sitter ABI mismatch");
            tree = TreeSitterNative.ts_parser_parse_string(parser, IntPtr.Zero, source, (uint)source.Length);
            if (tree == IntPtr.Zero) throw new InvalidDataException("tree-sitter parse failed");
            var root = TreeSitterNative.ts_tree_root_node(tree);
            string Text(TsNode node) => node.Id == IntPtr.Zero ? "" : Encoding.UTF8.GetString(source.AsSpan((int)TreeSitterNative.ts_node_start_byte(node), (int)(TreeSitterNative.ts_node_end_byte(node) - TreeSitterNative.ts_node_start_byte(node))));
            string package = ""; var imports = new List<string>(); TsNode declaration = default;
            foreach (var child in Children(root))
            {
                string type = Type(child);
                if (type == "package_declaration") package = PyStr.Strip(PyStr.RStrip(PyStr.RemovePrefix(Text(child), "package"), ";"));
                else if (type == "import_declaration") imports.Add(PyStr.Strip(PyStr.RStrip(PyStr.RemovePrefix(Text(child), "import"), ";")));
                else if (Declarations.Contains(type) && expectedName is null) { declaration = child; break; }
            }
            if (expectedName is not null)
                declaration = Descendants(root).FirstOrDefault(n => Declarations.Contains(Type(n)) && Text(Field(n, "name")) == expectedName);
            if (declaration.Id == IntPtr.Zero) return null;
            string name = Text(Field(declaration, "name"));
            var modifiers = Tokens(Text(Children(declaration).FirstOrDefault(n => Type(n) == "modifiers")));
            var annotations = PyRegex.Create(@"@([A-Za-z_$][\w$.]*)").Matches(Text(Children(declaration).FirstOrDefault(n => Type(n) == "modifiers"))).Select(m => m.Groups[1].Value).ToHashSet(StringComparer.Ordinal);
            string kind = Type(declaration) switch { "class_declaration" => "class", "interface_declaration" => "interface", "enum_declaration" => "enum", "record_declaration" => "record", _ => "annotation" };
            var apis = new List<JavaApi>();
            foreach (var child in Children(Field(declaration, "body")))
            {
                string type = Type(child);
                if (type is not ("method_declaration" or "constructor_declaration")) continue;
                var apiModifiers = Tokens(Text(Children(child).FirstOrDefault(n => Type(n) == "modifiers")));
                if (apiModifiers.Contains("private")) continue;
                var parameters = Field(child, "parameters");
                apis.Add(new(type == "constructor_declaration" ? "constructor" : "method", Text(Field(child, "name")), parameters.Id == IntPtr.Zero ? "()" : Text(parameters), apiModifiers.Contains("static"), PyStr.Strip(PyStr.Split(Text(child), "{")[0])));
            }
            if (kind == "class" && !apis.Any(a => a.Kind == "constructor")) apis.Add(new("constructor", name, "()", false, $"{name}() [implicit default constructor]"));
            var java = new JavaClass { Package = package, Name = name, Kind = kind, Modifiers = modifiers, Annotations = annotations, Apis = apis, Imports = imports, Extends = PyStr.Strip(PyStr.RemovePrefix(Text(Field(declaration, "superclass")), "extends")), SourceSet = SourceSet(sourcePath), IsTopLevel = TreeSitterNative.ts_node_eq(TreeSitterNative.ts_node_parent(declaration), root) };
            return java with { Modifiers = modifiers.Order(PythonCodePointComparer.Instance).ToHashSet(StringComparer.Ordinal), Annotations = annotations.Order(PythonCodePointComparer.Instance).ToHashSet(StringComparer.Ordinal), Tags = TagRules.Evaluate(java, text, sourcePath).Order(PythonCodePointComparer.Instance).ToHashSet(StringComparer.Ordinal) };
        }
        finally { if (tree != IntPtr.Zero) TreeSitterNative.ts_tree_delete(tree); TreeSitterNative.ts_parser_delete(parser); }
    }
    private static HashSet<string> Tokens(string text) => PyRegex.Create(@"@[A-Za-z_$][\w$.]*|\b(?:public|private|protected|static|abstract|final)\b").Matches(text).Select(m => m.Value).ToHashSet(StringComparer.Ordinal);
    private static string SourceSet(string path)
    {
        string normal = "/" + path.Replace('\\', '/').ToLowerInvariant();
        return normal.Contains("/src/test/", StringComparison.Ordinal) ? "test" : normal.Contains("/src/main/java/", StringComparison.Ordinal) || normal.Contains("/src/main/kotlin/", StringComparison.Ordinal) ? "production" : "unknown";
    }
    private static string Type(TsNode node) => node.Id == IntPtr.Zero ? "" : Marshal.PtrToStringUTF8(TreeSitterNative.ts_node_type(node))!;
    private static TsNode Field(TsNode node, string name) => node.Id == IntPtr.Zero ? default : TreeSitterNative.ts_node_child_by_field_name(node, name, (uint)name.Length);
    private static IEnumerable<TsNode> Children(TsNode node)
    {
        if (node.Id == IntPtr.Zero) yield break;
        uint count = TreeSitterNative.ts_node_named_child_count(node);
        for (uint i = 0; i < count; i++) yield return TreeSitterNative.ts_node_named_child(node, i);
    }
    private static IEnumerable<TsNode> Descendants(TsNode node)
    {
        foreach (var child in Children(node)) { yield return child; foreach (var nested in Descendants(child)) yield return nested; }
    }
}

/// <summary>Port nguyên regex tag của java_ast.parse_java_source.</summary>
public static class TagRules
{
    public static HashSet<string> Evaluate(JavaClass java, string source, string path)
    {
        var tags = new HashSet<string>(StringComparer.Ordinal); string imports = string.Join('\n', java.Imports);
        bool Match(string pattern, string text, RegexOptions options = RegexOptions.None) => PyRegex.Create(pattern, options).IsMatch(text);
        if (Match(@"\b(?:DataSource|JdbcTemplate|EntityManager)\b", source) || Match(@"\b(?:java\.sql|javax\.persistence|jakarta\.persistence|springframework\.jdbc)", imports)) tags.Add("DB_DEPENDENT");
        if (Match(@"\bDriverManager\s*\.\s*getConnection\s*\(", source)) { tags.Add("DB_DEPENDENT"); tags.Add("DIRECT_CONNECTION"); }
        if (Match(@"\b(?:java\.net|okhttp|apache\.http|HttpClient|WebClient)\b", source) || Match(@"\b(?:java\.io|java\.nio\.file|FileInputStream|Files\.)", source)) tags.Add("EXTERNAL_DEPENDENT");
        if ((tags.Contains("DB_DEPENDENT") || tags.Contains("EXTERNAL_DEPENDENT")) && !tags.Contains("DIRECT_CONNECTION")) tags.Add("REQUIRES_MOCK");
        if (java.Annotations.Any(a => a.EndsWith("Generated", StringComparison.Ordinal)) || java.Imports.Any(a => a.EndsWith(".Generated", StringComparison.Ordinal)) || ("/" + path.ToLowerInvariant() + "/").Contains("/generated/", StringComparison.Ordinal) || Match(@"\b(?:generated by|auto-generated|do not edit)\b", source, RegexOptions.IgnoreCase)) tags.Add("GENERATED_SOURCE");
        if (Match(@"(?:Test|Tests|IT)$", java.Name)) tags.Add("SUSPECT_TEST_NAME");
        var methods = java.Apis.Where(a => a.Kind == "method").ToArray();
        if (methods.Length > 0 && methods.All(a => a.IsStatic)) tags.Add("STATIC_UTILITY");
        if (!java.Modifiers.Contains("public")) tags.Add("PACKAGE_PRIVATE");
        if (methods.Length == 0 && java.Apis.Any(a => a.Kind == "constructor")) tags.Add("CONSTRUCTOR_ONLY");
        if (!methods.Any(a => !Match(@"\A(?:(?:get|set|is)[A-Z]|(?:equals|hashCode|toString)$)", a.Name))) tags.Add("LOW_LOGIC");
        return tags;
    }
}

[StructLayout(LayoutKind.Sequential)]
internal struct TsNode { public uint C0, C1, C2, C3; public IntPtr Id, Tree; }
internal static class TreeSitterNative
{
    private const string Library = "preflight-tree-sitter";
    private static IntPtr handle;
    private static readonly object Gate = new();
    internal static void Load(string? path)
    {
        lock (Gate)
        {
            if (handle != IntPtr.Zero) return;
            if (path is null)
            {
                var directory = new DirectoryInfo(AppContext.BaseDirectory);
                while (directory is not null && !File.Exists(Path.Combine(directory.FullName, "PreflightNet.sln"))) directory = directory.Parent;
                path = Path.Combine(directory?.FullName ?? Environment.CurrentDirectory, "tools/tree-sitter-native/bin/tree_sitter_java_preflight.dll");
            }
            handle = NativeLibrary.Load(Path.GetFullPath(path));
            NativeLibrary.SetDllImportResolver(typeof(TreeSitterNative).Assembly, (name, _, _) => name == Library ? handle : IntPtr.Zero);
        }
    }
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr tree_sitter_java();
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr ts_parser_new();
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] [return: MarshalAs(UnmanagedType.I1)] internal static extern bool ts_parser_set_language(IntPtr parser, IntPtr language);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr ts_parser_parse_string(IntPtr parser, IntPtr oldTree, byte[] source, uint length);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern TsNode ts_tree_root_node(IntPtr tree);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr ts_node_type(TsNode node);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern uint ts_node_start_byte(TsNode node);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern uint ts_node_end_byte(TsNode node);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern uint ts_node_named_child_count(TsNode node);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern TsNode ts_node_named_child(TsNode node, uint index);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Ansi)] internal static extern TsNode ts_node_child_by_field_name(TsNode node, string name, uint length);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern TsNode ts_node_parent(TsNode node);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] [return: MarshalAs(UnmanagedType.I1)] internal static extern bool ts_node_eq(TsNode a, TsNode b);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern void ts_tree_delete(IntPtr tree);
    [DllImport(Library, CallingConvention = CallingConvention.Cdecl)] internal static extern void ts_parser_delete(IntPtr parser);
}
