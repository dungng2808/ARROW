using System.Text.Json;
using Preflight.Models;

namespace Preflight.DataAccess;

/// <summary>Đọc hợp đồng JSON tại plan 4.6; thay cho cli._load_config đọc TOML.</summary>
public static class ConfigLoader
{
    public static PreflightConfig Load(string path, Action<string>? warning = null)
    {
        using var document = JsonDocument.Parse(File.ReadAllBytes(path));
        if (document.RootElement.TryGetProperty("policy", out var policy))
            foreach (string key in new[] { "allow_content_matched_technical_manifest", "integration_rule_file" })
                if (policy.TryGetProperty(key, out _)) warning?.Invoke($"Khóa policy.{key} không có tác dụng trong Python và được bỏ qua.");
        var config = document.RootElement.Deserialize<PreflightConfig>(PythonJson.Options) ?? throw new InvalidDataException("Config must be an object");
        if (config.Run.Workers < 1 || config.Run.MaxRevisionCandidates < 1 || config.Run.MaxEvidenceChecks < 1 || config.Run.BuildTimeoutSeconds < 1 || config.Run.GitTimeoutSeconds < 1)
            throw new InvalidDataException("workers, limits and timeouts must be positive");
        if (!Enum.IsDefined(config.Run.ProbeProfile)) throw new InvalidDataException("probe_profile must be v1 or v2");
        if (string.IsNullOrWhiteSpace(config.Tools.MavenRepoLocal) || string.IsNullOrWhiteSpace(config.Tools.GradleUserHome)) throw new InvalidDataException("Build cache paths must be configured");
        if (config.Disk.MinFreeGb < 0 || config.Disk.ResumeFreeGb < config.Disk.MinFreeGb || config.Disk.CheckIntervalSeconds < 1 || config.Disk.BuildCacheMaxGb < 0)
            throw new InvalidDataException("Invalid disk thresholds");
        return config;
    }
}
