using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Tooling;

/// <summary>Chạy các script cài trong pilot_tool/install/ (doctor và setup gọi khi người dùng đồng ý).</summary>
public static class Installers
{
    /// <summary>Cài LiteLLM 1.91.0 vào ARROW/litellm bằng install/install-litellm.*.</summary>
    public static Task<ProcessResult> InstallLiteLlmAsync(ToolPaths paths, CancellationToken cancellationToken = default)
    {
        var installDir = Path.Combine(paths.PilotToolDir, "install");
        return OperatingSystem.IsWindows()
            ? ProcessRunner.RunAsync("powershell", ["-NoProfile", "-ExecutionPolicy", "Bypass", "-File", Path.Combine(installDir, "install-litellm.ps1")],
                workingDirectory: paths.PilotToolDir, timeout: TimeSpan.FromMinutes(20), cancellationToken: cancellationToken)
            : ProcessRunner.RunAsync("/bin/sh", [Path.Combine(installDir, "install-litellm.sh")],
                workingDirectory: paths.PilotToolDir, timeout: TimeSpan.FromMinutes(20), cancellationToken: cancellationToken);
    }
}
