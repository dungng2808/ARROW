using System.Diagnostics;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.IntegrationTests;

public class ProcessStragglerTests
{
    /// <summary>Hết thời gian thì dừng cả tiến trình đã tách khỏi cây (như JVM chạy test của daemon Gradle) nếu dòng lệnh chứa thư mục làm việc.</summary>
    [Fact]
    public async Task TimeoutAlsoStopsDetachedProcessesOfTheWorkingDirectory()
    {
        if (OperatingSystem.IsWindows())
        {
            return;
        }

        var dir = Path.Combine(Path.GetTempPath(), "arrow-it", "ws", "run", "task", "tmp", "copy-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(dir);
        try
        {
            // perl bỏ qua đối số thừa: dòng lệnh của tiến trình tách rời chứa đường dẫn thư mục làm việc.
            var spec = new ProcessSpec("/bin/sh", ["-c", $"nohup perl -e 'sleep 300' '{dir}' >/dev/null 2>&1 & sleep 300"], dir, null, TimeSpan.FromSeconds(2));
            var result = await new ProcessRunner().RunAsync(spec, CancellationToken.None);
            Assert.True(result.TimedOut);
            await Task.Delay(500);
            using var check = Process.Start(new ProcessStartInfo("pgrep", ["-f", "--", dir]) { RedirectStandardOutput = true, UseShellExecute = false })!;
            var remaining = (await check.StandardOutput.ReadToEndAsync()).Trim();
            await check.WaitForExitAsync();
            Assert.Equal("", remaining);
        }
        finally
        {
            Directory.Delete(dir, recursive: true);
        }
    }
}
