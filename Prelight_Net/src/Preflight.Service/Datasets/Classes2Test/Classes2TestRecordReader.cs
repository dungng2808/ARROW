using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using Preflight.Service.Core.PyCompat;
using Preflight.DataAccess;

namespace Preflight.Service.Datasets.Classes2Test;

public sealed record RecordIdentityResult(string[]? Identity, JsonElement? Raw, string? ReasonCode, string? ContentSha256);
public sealed class PythonDataException(string pythonType) : Exception(pythonType)
{
    public string PythonType { get; } = pythonType;
}

/// <summary>Port của ingest._unsafe_source_path và _record_identity; chưa xây SQLite index.</summary>
public static class Classes2TestRecordReader
{
    public static bool UnsafeSourcePath(string value)
    {
        string normal = value.Replace('\\', '/');
        return normal.StartsWith('/') || PyRegex.Create(@"^[A-Za-z]:").IsMatch(normal) || normal.Contains('\0') || normal.Split('/').Contains("..", StringComparer.Ordinal);
    }
    public static RecordIdentityResult Read(string path)
    {
        byte[] content = File.ReadAllBytes(path);
        string text;
        try { text = new UTF8Encoding(false, true).GetString(content); }
        catch (DecoderFallbackException) { return new(null, null, "INPUT_ENCODING_INVALID", null); }
        text = PyStr.RemovePrefix(text, "\ufeff");
        JsonElement raw;
        try { using var doc = JsonDocument.Parse(text, new JsonDocumentOptions { MaxDepth = 1000 }); raw = doc.RootElement.Clone(); }
        catch (JsonException) { return new(null, null, "INPUT_JSON_INVALID", null); }
        if (raw.ValueKind != JsonValueKind.Object) throw new PythonDataException("AttributeError");
        JsonElement repo = Get(raw, "repository"), focal = Get(raw, "focal_class"), test = Get(raw, "test_class");
        string repoUrl = PyStr.Strip(PythonValue.StringOrEmpty(Get(repo, "url")));
        string classPath = PythonValue.StringOrEmpty(Get(focal, "file")), testPath = PythonValue.StringOrEmpty(Get(test, "file"));
        if (UnsafeSourcePath(classPath) || UnsafeSourcePath(testPath)) return new(null, raw, "INPUT_PATH_INVALID", null);
        classPath = classPath.Replace('\\', '/').Trim('/');
        string className = PyStr.Strip(PythonValue.StringOrEmpty(Get(focal, "identifier")));
        if (repoUrl.Length == 0 || classPath.Length == 0 || className.Length == 0) return new(null, raw, "INPUT_SCHEMA_INVALID", null);
        string projectId = new DirectoryInfo(Path.GetDirectoryName(Path.GetFullPath(path))!).Name;
        return new([projectId, repoUrl, classPath, className, className], raw, null, Convert.ToHexString(SHA256.HashData(content)).ToLowerInvariant());
    }
    private static JsonElement Get(JsonElement value, string key)
    {
        if (!PythonValue.Truthy(value)) return default;
        if (value.ValueKind != JsonValueKind.Object) throw new PythonDataException("AttributeError");
        return value.TryGetProperty(key, out var result) ? result : default;
    }
}
