namespace Preflight.Tests;

internal sealed class TestWorkspace : IDisposable
{
    internal static string Root
    {
        get
        {
            var directory = new DirectoryInfo(AppContext.BaseDirectory);
            while (directory is not null && !System.IO.File.Exists(System.IO.Path.Combine(directory.FullName, "PreflightNet.sln"))) directory = directory.Parent;
            return directory?.FullName ?? throw new InvalidOperationException("Prelight_Net root not found");
        }
    }
    internal string Path { get; } = System.IO.Path.Combine(Root, ".work", "tests", Guid.NewGuid().ToString("N"));
    internal TestWorkspace() => Directory.CreateDirectory(Path);
    internal string File(string relative, string content)
    {
        string path = System.IO.Path.GetFullPath(System.IO.Path.Combine(Path, relative));
        Directory.CreateDirectory(System.IO.Path.GetDirectoryName(path)!);
        System.IO.File.WriteAllText(path, content, new System.Text.UTF8Encoding(false));
        return path;
    }
    public void Dispose()
    {
        string expected = System.IO.Path.GetFullPath(System.IO.Path.Combine(Root, ".work", "tests")) + System.IO.Path.DirectorySeparatorChar;
        if (!System.IO.Path.GetFullPath(Path).StartsWith(expected, StringComparison.Ordinal)) throw new InvalidOperationException("Unsafe test cleanup");
        foreach (string file in Directory.EnumerateFiles(Path, "*", new EnumerationOptions { RecurseSubdirectories = true, AttributesToSkip = FileAttributes.ReparsePoint }))
            System.IO.File.SetAttributes(file, System.IO.File.GetAttributes(file) & ~FileAttributes.ReadOnly);
        Directory.Delete(Path, recursive: true);
    }
}
