using Preflight.Service.Core.PyCompat;

namespace Preflight.Service.Core.Common;

/// <summary>Port của runner._path_candidates/_resolve_path; chặn symlink thoát workspace.</summary>
public static class ResolvePath
{
    public static IReadOnlyList<string> PathCandidates(string workspace, string relative)
    {
        string raw = PyStr.Strip(relative).Replace('\\', '/');
        if (raw.Length == 0 || raw.StartsWith('/') || PyRegex.Create(@"^[A-Za-z]:").IsMatch(raw) || raw.Contains('\0')) return [];
        string[] parts = raw.Split('/').Where(p => p.Length > 0 && p != ".").ToArray();
        if (parts.Contains("..", StringComparer.Ordinal)) return [];
        string root = ResolveLinks(workspace);
        string primary = ResolveLinks(Path.Combine([root, .. parts]));
        if (!IsWithin(primary, root)) return [];
        var variants = parts.Length > 1 ? new[] { parts, parts[1..] } : new[] { parts };
        return variants.Select(p => ResolveLinks(Path.Combine([root, .. p]))).Where(p => IsWithin(p, root)).ToArray();
    }
    public static string? Resolve(string workspace, string relative) => PathCandidates(workspace, relative).FirstOrDefault(File.Exists);
    public static bool IsWithin(string path, string root)
    {
        var comparison = OperatingSystem.IsWindows() ? StringComparison.OrdinalIgnoreCase : StringComparison.Ordinal;
        root = Path.TrimEndingDirectorySeparator(Path.GetFullPath(root));
        path = Path.GetFullPath(path);
        return path.Equals(root, comparison) || path.StartsWith(root + Path.DirectorySeparatorChar, comparison);
    }
    public static string ResolveLinks(string path)
    {
        string full = Path.GetFullPath(path), current = Path.GetPathRoot(full)!;
        foreach (string part in full[current.Length..].Split(Path.DirectorySeparatorChar, StringSplitOptions.RemoveEmptyEntries))
        {
            current = Path.Combine(current, part);
            FileSystemInfo info = Directory.Exists(current) ? new DirectoryInfo(current) : new FileInfo(current);
            if (info.LinkTarget is not null) current = info.ResolveLinkTarget(true)?.FullName ?? current;
        }
        return Path.GetFullPath(current);
    }
}
