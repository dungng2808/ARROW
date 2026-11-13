using System.Diagnostics;
using System.Globalization;
using System.Text;
using Preflight.Service.External;

namespace Preflight.Service.Core.Git;

/// <summary>One cat-file process per revision search; consumes blob lengths exactly, including empty/Unicode files.</summary>
public sealed class GitObjectReader : IDisposable
{
    private readonly Process process;
    private readonly WindowsProcessJob? job;
    private readonly Stream output;
    private readonly Task<string> error;
    private readonly int timeout;
    public GitObjectReader(string mirror, int timeoutSeconds)
    {
        timeout = timeoutSeconds;
        var start = new ProcessStartInfo("git") { UseShellExecute = false, CreateNoWindow = true, RedirectStandardInput = true, RedirectStandardOutput = true, RedirectStandardError = true, StandardInputEncoding = new UTF8Encoding(false), StandardErrorEncoding = new UTF8Encoding(false) };
        foreach (string arg in new[] { "-c", "safe.directory=" + Path.GetFullPath(mirror).Replace('\\', '/'), "-C", mirror, "cat-file", "--batch" }) start.ArgumentList.Add(arg);
        start.Environment["GIT_TERMINAL_PROMPT"] = "0"; start.Environment["GCM_INTERACTIVE"] = "never";
        process = Process.Start(start) ?? throw new IOException("Cannot start git cat-file");
        if (OperatingSystem.IsWindows()) { job = new WindowsProcessJob(); job.Attach(process); }
        output = process.StandardOutput.BaseStream; error = process.StandardError.ReadToEndAsync();
    }
    public async Task<string?> ReadAsync(string sha, string path, CancellationToken ct)
    {
        if (path.IndexOfAny(['\n', '\r', '\0']) >= 0) throw new InvalidDataException("Unsafe batch object path");
        using var limit = new CancellationTokenSource(TimeSpan.FromSeconds(timeout)); using var linked = CancellationTokenSource.CreateLinkedTokenSource(ct, limit.Token);
        using var registration = linked.Token.Register(() => { job?.Terminate(); try { if (!process.HasExited) process.Kill(true); } catch (InvalidOperationException) { } });
        try
        {
            await process.StandardInput.WriteLineAsync((sha + ":" + path).AsMemory(), linked.Token); await process.StandardInput.FlushAsync(linked.Token);
            var header = new StringBuilder(); var one = new byte[1];
            while (true) { await output.ReadExactlyAsync(one, linked.Token); if (one[0] == 10) break; header.Append((char)one[0]); if (header.Length > 32768) throw new InvalidDataException("Invalid cat-file header"); }
            string[] parts = header.ToString().Split(' ');
            if (parts[^1] == "missing") return null;
            if (parts.Length != 3 || !int.TryParse(parts[2], NumberStyles.None, CultureInfo.InvariantCulture, out int length) || length < 0) throw new InvalidDataException("Invalid cat-file object length");
            byte[] data = new byte[length]; await output.ReadExactlyAsync(data, linked.Token); await output.ReadExactlyAsync(one, linked.Token);
            if (one[0] != 10) throw new InvalidDataException("Invalid cat-file separator");
            return parts[1] == "blob" ? Encoding.UTF8.GetString(data).Replace("\r\n", "\n", StringComparison.Ordinal).Replace('\r', '\n') : null;
        }
        catch (Exception) when (linked.IsCancellationRequested) { ct.ThrowIfCancellationRequested(); throw new GitCommandException("git cat-file timeout"); }
    }
    public void Dispose()
    {
        process.StandardInput.Close(); if (!process.WaitForExit(1000)) { job?.Terminate(); try { process.Kill(true); } catch (InvalidOperationException) { } process.WaitForExit(); }
        _ = error.GetAwaiter().GetResult(); output.Dispose(); job?.Dispose(); process.Dispose();
    }
}
