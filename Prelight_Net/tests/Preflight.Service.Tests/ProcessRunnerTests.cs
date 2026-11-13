using System.Diagnostics;
using Preflight.Service.External;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class ProcessRunnerTests
{
    private static string Python => Path.GetFullPath(Path.Combine(TestWorkspace.Root, "..", "preflight_tool", ".venv", OperatingSystem.IsWindows() ? "Scripts/python.exe" : "bin/python"));
    private static string[] Command(string mode, params string[] arguments) => [Python, "-B", Path.Combine(TestWorkspace.Root, "tools", "process_fixture.py"), mode, .. arguments];

    [Fact]
    public async Task SuccessKeepsPythonLogFormatAndOriginalCommand()
    {
        using var work = new TestWorkspace();
        string log = Path.Combine(work.Path, "dữ liệu có khoảng trắng", "success.log");
        var command = Command("success");
        var result = await new ProcessRunner().RunAsync(command, work.Path, 5, log);
        Assert.Equal(0, result.ExitCode); Assert.False(result.TimedOut); Assert.Equal(command, result.Command);
        Assert.Equal("$ " + string.Join(' ', command) + "\n\nSTDOUT\ndữ liệu\n\n\nSTDERR\nlỗi thử\n", File.ReadAllText(log));
    }
    [Fact]
    public async Task MissingExecutableHas127AndMarker()
    {
        using var work = new TestWorkspace(); string log = Path.Combine(work.Path, "missing.log");
        var result = await new ProcessRunner().RunAsync(["preflight-definitely-missing-tool"], work.Path, 5, log);
        Assert.Equal(127, result.ExitCode); Assert.Contains("\n\nEXECUTABLE_NOT_FOUND\n", File.ReadAllText(log));
    }
    [Fact]
    public async Task TimeoutAlsoStopsDescendantAfterParentAlreadyExited()
    {
        using var work = new TestWorkspace(); string pid = Path.Combine(work.Path, "orphan.pid"), log = Path.Combine(work.Path, "orphan.log");
        var elapsed = Stopwatch.StartNew();
        var result = await new ProcessRunner().RunAsync(Command("orphan", pid), work.Path, 1, log);
        Assert.True(result.TimedOut); Assert.True(elapsed.Elapsed < TimeSpan.FromSeconds(8));
        Assert.False(IsRunning(int.Parse(File.ReadAllText(pid), System.Globalization.CultureInfo.InvariantCulture)));
    }
    [Fact]
    public async Task NonzeroExitIsPreserved()
    {
        using var work = new TestWorkspace();
        Assert.Equal(7, (await new ProcessRunner().RunAsync(Command("exit"), work.Path, 5, Path.Combine(work.Path, "exit.log"))).ExitCode);
    }
    [Fact]
    public async Task TimeoutStopsChildProcessAndPreservesPartialOutput()
    {
        using var work = new TestWorkspace(); string pidPath = Path.Combine(work.Path, "child.pid"), log = Path.Combine(work.Path, "timeout.log");
        var elapsed = Stopwatch.StartNew();
        var result = await new ProcessRunner().RunAsync(Command("tree", pidPath), work.Path, 1, log);
        Assert.True(elapsed.Elapsed < TimeSpan.FromSeconds(8), $"Kill tree took {elapsed.Elapsed}");
        Assert.True(result.TimedOut); Assert.Null(result.ExitCode);
        Assert.Contains("TIMEOUT after 1s\n", File.ReadAllText(log));
        Assert.Contains("ready", File.ReadAllText(log));
        Assert.False(IsRunning(int.Parse(File.ReadAllText(pidPath), System.Globalization.CultureInfo.InvariantCulture)));
    }
    [Fact]
    public async Task CancellationStopsTreeAndKeepsLog()
    {
        using var work = new TestWorkspace(); string log = Path.Combine(work.Path, "cancel.log"), pidPath = Path.Combine(work.Path, "child.pid");
        using var cancel = new CancellationTokenSource();
        var running = new ProcessRunner().RunAsync(Command("tree", pidPath), work.Path, 60, log, cancellationToken: cancel.Token);
        var ready = Stopwatch.StartNew();
        while ((!File.Exists(pidPath) || new FileInfo(pidPath).Length == 0) && !running.IsCompleted && ready.Elapsed < TimeSpan.FromSeconds(10)) await Task.Delay(20);
        var elapsed = Stopwatch.StartNew(); cancel.Cancel();
        await Assert.ThrowsAnyAsync<OperationCanceledException>(() => running);
        Assert.True(elapsed.Elapsed < TimeSpan.FromSeconds(8), $"Kill tree took {elapsed.Elapsed}");
        Assert.Contains("\n\nCANCELLED\n", File.ReadAllText(log));
        Assert.False(IsRunning(int.Parse(File.ReadAllText(pidPath), System.Globalization.CultureInfo.InvariantCulture)));
    }
    [Fact]
    public async Task PreCancelledWorkDoesNotLaunchProcess()
    {
        using var work = new TestWorkspace(); using var cancel = new CancellationTokenSource(); cancel.Cancel();
        string log = Path.Combine(work.Path, "none.log");
        await Assert.ThrowsAnyAsync<OperationCanceledException>(() => new ProcessRunner().RunAsync(Command("sleep"), work.Path, 5, log, cancellationToken: cancel.Token));
        Assert.False(File.Exists(log));
    }
    [Fact]
    public async Task EnvironmentIsExplicitAndStandardInputIsClosed()
    {
        using var work = new TestWorkspace(); string log = Path.Combine(work.Path, "env.log");
        var env = Environment.GetEnvironmentVariables().Cast<System.Collections.DictionaryEntry>().ToDictionary(e => (string)e.Key, e => (string)e.Value!, StringComparer.Ordinal);
        env["PREFLIGHT_FIXTURE"] = "expected";
        var result = await new ProcessRunner().RunAsync(Command("environment"), work.Path, 5, log, env);
        Assert.Equal(0, result.ExitCode); Assert.Contains("expected\nclosed\n", File.ReadAllText(log));
    }
    [Fact]
    public async Task WindowsCmdWrapperHandlesSpacesAndMetacharacters()
    {
        if (!OperatingSystem.IsWindows()) return;
        using var work = new TestWorkspace();
        string wrapper = work.File("thư mục/wrapper.cmd", "@echo off\r\nset \"value=%~1\"\r\nset value\r\n");
        string log = Path.Combine(work.Path, "wrapper.log");
        var result = await new ProcessRunner().RunAsync([wrapper, "hello & data"], work.Path, 5, log);
        Assert.Equal(0, result.ExitCode); Assert.Contains("hello & data\n", File.ReadAllText(log));
    }
    private static bool IsRunning(int pid)
    {
        try { using var process = Process.GetProcessById(pid); return !process.HasExited; }
        catch (ArgumentException) { return false; }
    }
}
