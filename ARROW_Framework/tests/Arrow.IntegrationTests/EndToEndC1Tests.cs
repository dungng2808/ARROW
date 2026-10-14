using System.Diagnostics;
using Arrow.Service;

namespace Arrow.IntegrationTests;

/// <summary>Bước C1: lệnh <c>arrow prompts check</c> chạy qua cả 3 tầng (UI → Service → Data Access) trên bộ prompt thật.</summary>
public class EndToEndC1Tests
{
    private static (int ExitCode, string Output) RunArrow(params string[] args)
    {
        var dll = Path.Combine(AppContext.BaseDirectory, "arrow.dll");
        var start = new ProcessStartInfo("dotnet")
        {
            RedirectStandardOutput = true, RedirectStandardError = true, RedirectStandardInput = true, UseShellExecute = false,
            StandardOutputEncoding = System.Text.Encoding.UTF8, StandardErrorEncoding = System.Text.Encoding.UTF8,
        };
        start.ArgumentList.Add(dll);
        foreach (var arg in args)
        {
            start.ArgumentList.Add(arg);
        }

        using var process = Process.Start(start)!;
        process.StandardInput.Close();
        var output = process.StandardOutput.ReadToEnd() + process.StandardError.ReadToEnd();
        process.WaitForExit(60_000);
        return (process.ExitCode, output);
    }

    [Fact]
    public void LocatesFrameworkRootFromTestDirectory()
    {
        var paths = ServiceFactory.Locate(AppContext.BaseDirectory);
        Assert.NotNull(paths);
        Assert.True(File.Exists(Path.Combine(paths!.FrameworkRoot, "ArrowFramework.sln")));
        Assert.True(Directory.Exists(paths.PromptsDir));
    }

    [Fact]
    public void PromptsCheckPassesOnRealPrompts()
    {
        var (exitCode, output) = RunArrow("prompts", "check");
        Assert.Equal(0, exitCode);
        Assert.Contains("[OK] Bộ prompt nhất quán", output);
        Assert.Contains("generation/system/few_shot.md", output);
    }

    [Fact]
    public void UnknownCommandFailsWithCode2()
    {
        var (exitCode, output) = RunArrow("nope");
        Assert.Equal(2, exitCode);
        Assert.Contains("[X]", output);
    }

    [Fact]
    public void ServicesWriteToolLog()
    {
        var paths = ServiceFactory.Locate(AppContext.BaseDirectory)!;
        var services = ServiceFactory.Create(paths);
        Assert.True(services.Prompts.Check().Ok);
        Assert.NotEmpty(Directory.GetFiles(paths.LogsDir, "arrow-*.log"));
    }
}
