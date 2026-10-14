using Arrow.DataAccess.Interfaces;
using Arrow.Models.Config;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

/// <summary>Cài đặt máy (S2, spec 06 Mục 4): dò công cụ theo <c>machine.local.json</c> và các vị trí mặc định, lưu cấu hình máy.</summary>
public sealed class SetupService(FrameworkPaths paths, IConfigRepository config, IProcessRunner runner) : ISetupService
{
    public const double MinFreeGb = 20;

    public MachineConfig? CurrentMachine() => config.LoadMachine();

    public async Task<SetupState> LoadAsync(MachineConfig? editing, CancellationToken cancellationToken)
    {
        var saved = config.LoadMachine();
        var machine = editing ?? saved ?? new MachineConfig { MachineName = Environment.MachineName };
        var tools = new List<ToolInfo>();
        foreach (var major in new[] { 8, 11, 17, 21 })
        {
            tools.Add(await ToolProbe.JdkAsync(runner, major, ToolLocator.FindJdkHome(major, machine, paths.ArrowRoot), cancellationToken).ConfigureAwait(false));
        }

        tools.Add(await ToolProbe.ProgramAsync(runner, "Maven", ToolLocator.FindMaven(machine), ["-v"], cancellationToken).ConfigureAwait(false));
        tools.Add(await ToolProbe.ProgramAsync(runner, "Gradle", ToolLocator.FindGradle(machine), ["-v"], cancellationToken, "không tìm thấy (chỉ cần khi repository Gradle không có gradlew)").ConfigureAwait(false));
        tools.Add(await ToolProbe.ProgramAsync(runner, "Git", ToolLocator.FindGit(machine), ["--version"], cancellationToken).ConfigureAwait(false));
        return new SetupState(machine, saved is not null, tools, config.ListExperiments(), paths.FrameworkRoot);
    }

    public Task<ToolInfo> CheckJdkAsync(int major, string home, CancellationToken cancellationToken) =>
        ToolProbe.JdkAsync(runner, major, Path.GetFullPath(home), cancellationToken);

    public string? ValidateWorkDir(string path)
    {
        var full = paths.Resolve(path);
        if (full.Contains(' '))
        {
            return "đường dẫn có dấu cách (một số công cụ build không chạy được)";
        }

        if (full.Any(c => c > 127))
        {
            return "đường dẫn có ký tự ngoài ASCII (dấu tiếng Việt)";
        }

        return FreeSpaceGb(full) is { } free && free < MinFreeGb ? $"chỉ còn {free:0} GB trống, cần ít nhất {MinFreeGb:0} GB" : null;
    }

    public double? FreeSpaceGb(string path)
    {
        try
        {
            var root = Path.GetPathRoot(paths.Resolve(path));
            return root is null ? null : new DriveInfo(root).AvailableFreeSpace / 1024d / 1024 / 1024;
        }
        catch (Exception ex) when (ex is IOException or ArgumentException or UnauthorizedAccessException)
        {
            return null;
        }
    }

    public void Save(MachineConfig machine) => config.SaveMachine(machine);
}
