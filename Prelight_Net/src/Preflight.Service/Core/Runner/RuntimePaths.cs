using System.Security.Cryptography;
using Preflight.Models;

namespace Preflight.Service.Core.Runner;

public static class RuntimePaths
{
    public static string Root
    {
        get
        {
            var current = new DirectoryInfo(AppContext.BaseDirectory);
            while (current is not null && !File.Exists(Path.Combine(current.FullName, "PreflightNet.sln"))) current = current.Parent;
            return current?.FullName ?? Environment.CurrentDirectory;
        }
    }
    public static string Evidence(CandidateTask task, string relative, string runRoot, string inputRoot)
    {
        if (Path.IsPathRooted(relative)) return relative;
        if (task.Extra?.TryGetValue("source_root", out var source) == true) return Path.Combine(source, relative);
        string copied = Path.Combine(runRoot, "inputs", relative);
        if (File.Exists(copied)) return copied;
        string generated = Path.Combine(runRoot, "inputs/gitbug/dataset", relative);
        if (task.Dataset == "gitbug-java" && File.Exists(generated)) return generated;
        string dataset = Directory.Exists(Path.Combine(inputRoot, "dataset")) ? Path.Combine(inputRoot, "dataset") : inputRoot;
        return Path.Combine(dataset, relative);
    }
    public static string Hash(string path) { using var file = File.OpenRead(path); return Convert.ToHexString(SHA256.HashData(file)).ToLowerInvariant(); }
    public static string Fingerprint(string nativePath)
    {
        using var hash = IncrementalHash.CreateHash(HashAlgorithmName.SHA256);
        foreach (string path in Directory.GetFiles(AppContext.BaseDirectory, "*.dll").Where(p => (Path.GetFileName(p).StartsWith("Preflight.", StringComparison.Ordinal) && !p.Contains(".Tests", StringComparison.Ordinal)) || Path.GetFileName(p) == "preflight-net.dll").Order(StringComparer.Ordinal).Concat([nativePath]))
        { hash.AppendData(System.Text.Encoding.UTF8.GetBytes(Path.GetFileName(path))); hash.AppendData(File.ReadAllBytes(path)); }
        return Convert.ToHexString(hash.GetHashAndReset()).ToLowerInvariant();
    }
}

public sealed class InfrastructureFailure(string message) : IOException(message)
{
    public static bool IsDiskFull(Exception error) => error is IOException && (error.HResult is unchecked((int)0x80070070) or unchecked((int)0x80070027));
    public static bool LogIsDiskFull(string text) => new[] { "no space left on device", "there is not enough space on the disk", "disk full" }.Any(m => text.Contains(m, StringComparison.OrdinalIgnoreCase));
}
