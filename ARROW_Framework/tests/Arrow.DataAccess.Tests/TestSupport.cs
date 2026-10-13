using Arrow.Models.Config;

namespace Arrow.DataAccess.Tests;

/// <summary>Thư mục <c>ARROW_Framework</c> tạm cho một test; tự xóa khi xong.</summary>
public sealed class TempFramework : IDisposable
{
    public string Root { get; } = Path.Combine(Path.GetTempPath(), "arrow-fw-test-" + Guid.NewGuid().ToString("N"));

    public TempFramework()
    {
        Directory.CreateDirectory(Paths.ExperimentsDir);
        Directory.CreateDirectory(Paths.PromptsDir);
    }

    public FrameworkPaths Paths => new(Root);

    public string Write(string relativePath, string text)
    {
        var full = Path.Combine(Root, relativePath);
        Directory.CreateDirectory(Path.GetDirectoryName(full)!);
        File.WriteAllText(full, text);
        return full;
    }

    public void Dispose()
    {
        try
        {
            Directory.Delete(Root, recursive: true);
        }
        catch (IOException)
        {
        }
    }
}

/// <summary>Đường dẫn trong repository thật (để test trên prompt và manifest thật).</summary>
public static class RepoFiles
{
    /// <summary>Thư mục <c>ARROW_Framework</c> chứa file test đang chạy.</summary>
    public static string FrameworkRoot
    {
        get
        {
            for (var dir = new DirectoryInfo(AppContext.BaseDirectory); dir is not null; dir = dir.Parent)
            {
                if (File.Exists(Path.Combine(dir.FullName, "ArrowFramework.sln")))
                {
                    return dir.FullName;
                }
            }

            throw new InvalidOperationException("Không tìm thấy ArrowFramework.sln");
        }
    }

    public static string ArrowRoot => Path.GetFullPath(Path.Combine(FrameworkRoot, ".."));
}
