using System.Security.Cryptography;
using System.Text;

namespace Arrow.Core.Util;

/// <summary>SHA-256 hex chữ thường trên byte (specs/01).</summary>
public static class Hashing
{
    public static string Sha256Bytes(ReadOnlySpan<byte> bytes) =>
        Convert.ToHexString(SHA256.HashData(bytes)).ToLowerInvariant();

    public static string Sha256Text(string text) => Sha256Bytes(Encoding.UTF8.GetBytes(text));

    public static string Sha256File(string path)
    {
        using var stream = File.OpenRead(path);
        return Convert.ToHexString(SHA256.HashData(stream)).ToLowerInvariant();
    }
}
