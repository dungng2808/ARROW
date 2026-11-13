namespace Preflight.Service.Core.Probe;

/// <summary>Port của runner._test_source_root.</summary>
public static class TestSourceRoot
{
    public static string Find(string classFile, string moduleRoot)
    {
        string full = Path.GetFullPath(classFile), root = Path.GetPathRoot(full)!;
        string[] parts = full[root.Length..].Split(Path.DirectorySeparatorChar, StringSplitOptions.RemoveEmptyEntries);
        for (int i = 0; i < parts.Length - 2; i++)
            if (parts[i].ToLowerInvariant() == "src" && parts[i + 1].ToLowerInvariant() == "main" && parts[i + 2].ToLowerInvariant() == "java")
                return Path.Combine([root, .. parts[..i], "src", "test", "java"]);
        return Path.Combine(moduleRoot, "src", "test", "java");
    }
}
