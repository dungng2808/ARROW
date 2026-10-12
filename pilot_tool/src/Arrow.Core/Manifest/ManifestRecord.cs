using System.Text.Json;
using System.Text.Json.Serialization;
using Arrow.Core.Util;

namespace Arrow.Core.Manifest;

/// <summary>Một bản ghi của manifest preflight (evaluation_manifest/…jsonl). Trường khác được giữ nguyên.</summary>
public sealed record ManifestRecord
{
    public string TaskId { get; init; } = "";
    public string RepoUrl { get; init; } = "";
    public string CheckoutSha { get; init; } = "";
    public string ClassFqn { get; init; } = "";
    public string ClassPath { get; init; } = "";
    public string ModulePath { get; init; } = ".";
    public string TestClassPath { get; init; } = "";
    public string BuildTool { get; init; } = "";
    public string TestingFramework { get; init; } = "";
    public string JavaVersion { get; init; } = "";
    public double? DurationSeconds { get; init; }

    [JsonExtensionData]
    public Dictionary<string, JsonElement>? Extra { get; init; }

    /// <summary>Dataset do cấu hình thực nghiệm gán, không có trong file manifest.</summary>
    [JsonIgnore]
    public string Dataset { get; init; } = "";

    [JsonIgnore]
    public string ClassName => ClassFqn.Contains('.') ? ClassFqn[(ClassFqn.LastIndexOf('.') + 1)..] : ClassFqn;

    [JsonIgnore]
    public string PackageName => ClassFqn.Contains('.') ? ClassFqn[..ClassFqn.LastIndexOf('.')] : "";

    /// <summary>JDK major theo JDK preflight đã build thành công (specs/02 §3, bước 1).</summary>
    [JsonIgnore]
    public int? JdkMajor => ParseJdkMajor(JavaVersion);

    public static int? ParseJdkMajor(string javaVersion)
    {
        var match = System.Text.RegularExpressions.Regex.Match(javaVersion ?? "", "\"(\\d+)(?:\\.(\\d+))?");
        if (!match.Success)
        {
            return null;
        }

        var first = int.Parse(match.Groups[1].Value);
        return first == 1 && match.Groups[2].Success ? int.Parse(match.Groups[2].Value) : first;
    }
}

public static class ManifestReader
{
    public static List<ManifestRecord> Read(string path, string dataset)
    {
        var records = new List<ManifestRecord>();
        var seen = new HashSet<string>();
        var lineNumber = 0;
        foreach (var line in AtomicFile.ReadLines(path))
        {
            lineNumber++;
            var record = Json.Deserialize<ManifestRecord>(line) with { Dataset = dataset };
            if (string.IsNullOrEmpty(record.TaskId) || string.IsNullOrEmpty(record.RepoUrl) ||
                string.IsNullOrEmpty(record.CheckoutSha) || string.IsNullOrEmpty(record.ClassPath))
            {
                throw new ArrowException("MANIFEST_INVALID", $"{path}:{lineNumber} thiếu task_id, repo_url, checkout_sha hoặc class_path");
            }

            if (!seen.Add(record.TaskId))
            {
                throw new ArrowException("MANIFEST_DUPLICATE_TASK", $"{path}: task_id lặp {record.TaskId}");
            }

            records.Add(record);
        }

        return records;
    }
}
