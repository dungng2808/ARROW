using Arrow.Core.Util;

namespace Arrow.Core.Config;

/// <summary>Cấu hình thực nghiệm dùng chung, commit lên Git (specs/01 §1).</summary>
public sealed record ExperimentConfig
{
    public int SchemaVersion { get; init; } = 1;
    public string ExperimentId { get; init; } = "";
    public string Kind { get; init; } = "pilot";
    public List<ManifestRef> Manifests { get; init; } = [];
    public List<ModelConfig> Models { get; init; } = [];
    public List<string> GenerationStrategies { get; init; } = [];
    public PromptSetConfig PromptSet { get; init; } = new();
    public RepairConfig Repair { get; init; } = new();
    public string NRule { get; init; } = "share95";
    public BuildConfig Build { get; init; } = new();
    public List<ComparisonSystemConfig> ComparisonSystems { get; init; } = [];
    public Dictionary<string, string> Quality { get; init; } = [];
    public string TestClassSuffix { get; init; } = "_ArrowTest";
}

public sealed record ManifestRef
{
    public string Dataset { get; init; } = "";
    public string Path { get; init; } = "";
    public string Sha256 { get; init; } = "";
    public bool Repair { get; init; }
}

public sealed record ModelConfig
{
    public string Name { get; init; } = "";
    public string ExpectedModelIdPrefix { get; init; } = "";
    public Dictionary<string, object> Params { get; init; } = [];
    public PriceConfig? PriceUsdPerMillion { get; init; }
}

public sealed record PriceConfig
{
    public double Input { get; init; }
    public double Output { get; init; }
}

public sealed record PromptSetConfig
{
    public string Dir { get; init; } = "prompts";
    public string Version { get; init; } = "v1";
}

public sealed record RepairConfig
{
    public List<string> Mechanisms { get; init; } = ["fixed", "adaptive"];
    public int NMax { get; init; } = 10;
    public bool EarlyStop { get; init; }
}

public sealed record BuildConfig
{
    public int StepTimeoutSeconds { get; init; } = 900;
    public int ToolVersionTimeoutSeconds { get; init; } = 120;
    public List<string> MavenExtraFlags { get; init; } = [];
    public List<string> GradleExtraFlags { get; init; } = [];

    /// <summary>Task Gradle được loại bằng <c>-x</c>, chỉ khi task đó có thật trong module (specs/02 §4.3).</summary>
    public List<string> GradleExcludedTasks { get; init; } = [];
    public int BaselineRuns { get; init; } = 2;
}

public sealed record ComparisonSystemConfig
{
    public string Name { get; init; } = "";
    public string Version { get; init; } = "";
    public int Seed { get; init; } = 42;
    public string Config { get; init; } = "default";
}

/// <summary>Cấu hình thực nghiệm đã đọc kèm hash và đường dẫn tuyệt đối.</summary>
public sealed record LoadedExperiment(ExperimentConfig Config, string Path, string Sha256)
{
    public string ResolveManifestPath(ManifestRef manifest) =>
        System.IO.Path.GetFullPath(System.IO.Path.Combine(System.IO.Path.GetDirectoryName(Path)!, manifest.Path));
}

public static class ExperimentLoader
{
    /// <summary>Đọc cấu hình thực nghiệm; kiểm tra hash từng manifest (MANIFEST_HASH_MISMATCH).</summary>
    public static LoadedExperiment Load(string path, bool verifyManifests = true)
    {
        var fullPath = System.IO.Path.GetFullPath(path);
        var config = Json.Read<ExperimentConfig>(fullPath);
        var loaded = new LoadedExperiment(config, fullPath, Hashing.Sha256File(fullPath));
        if (verifyManifests)
        {
            foreach (var manifest in config.Manifests)
            {
                var manifestPath = loaded.ResolveManifestPath(manifest);
                if (!File.Exists(manifestPath))
                {
                    throw new ArrowException("MANIFEST_MISSING", $"Không tìm thấy manifest {manifestPath}");
                }

                var actual = Hashing.Sha256File(manifestPath);
                if (!string.Equals(actual, manifest.Sha256, StringComparison.OrdinalIgnoreCase))
                {
                    throw new ArrowException("MANIFEST_HASH_MISMATCH",
                        $"Manifest {manifest.Dataset} có hash {actual}, cấu hình ghi {manifest.Sha256}");
                }
            }
        }

        return loaded;
    }
}
