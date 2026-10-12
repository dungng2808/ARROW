using System.Runtime.InteropServices;

namespace Arrow.Infrastructure.Tooling;

/// <summary>Thông tin máy hiển thị ở setup và ghi vào run_config.lock.json.</summary>
public static class MachineInfo
{
    public static string RuntimeId =>
        (OperatingSystem.IsMacOS() ? "osx" : OperatingSystem.IsWindows() ? "win" : "linux") + "-" +
        RuntimeInformation.OSArchitecture.ToString().ToLowerInvariant();

    public static string OsDescription => RuntimeInformation.OSDescription.Trim();

    public static int CpuCount => System.Environment.ProcessorCount;

    public static double RamGb => Math.Round(GC.GetGCMemoryInfo().TotalAvailableMemoryBytes / 1024d / 1024d / 1024d, 1);

    /// <summary>Dung lượng trống (GB) của ổ chứa thư mục; thư mục chưa có thì xét thư mục cha gần nhất.</summary>
    public static double FreeGb(string path)
    {
        var directory = new DirectoryInfo(Path.GetFullPath(path));
        while (directory is not null && !directory.Exists)
        {
            directory = directory.Parent;
        }

        if (directory is null)
        {
            return 0;
        }

        var drive = new DriveInfo(directory.Root.FullName);
        return Math.Round(drive.AvailableFreeSpace / 1024d / 1024d / 1024d, 1);
    }

    /// <summary>Gợi ý số luồng build: theo nhân CPU và RAM (mỗi build Maven/Gradle khoảng 3 GB).</summary>
    public static int SuggestedBuildWorkers() => Math.Clamp(Math.Min(CpuCount / 4, (int)(RamGb / 6)), 1, 4);
}
