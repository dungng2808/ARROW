using System.Text;
using System.Text.Json;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Manifest;

namespace Arrow.DataAccess.Files;

public sealed class FileConfigRepository(FrameworkPaths paths) : IConfigRepository
{
    public IReadOnlyList<string> ListExperiments() =>
        Directory.Exists(paths.ExperimentsDir)
            ? Directory.GetFiles(paths.ExperimentsDir, "*.json").OrderBy(f => f, StringComparer.Ordinal).ToList()
            : [];

    public LoadedExperiment LoadExperiment(string path)
    {
        var full = paths.Resolve(path);
        if (!File.Exists(full))
        {
            throw new ArrowException(ErrorCodes.ConfigMissing, $"Không có file cấu hình thực nghiệm {full}");
        }

        ExperimentConfig config;
        try
        {
            config = JsonFile.Read<ExperimentConfig>(full);
        }
        catch (JsonException ex)
        {
            throw new ArrowException(ErrorCodes.ConfigInvalid, $"{full}: JSON không hợp lệ ({ex.Message})", ex);
        }

        var loaded = new LoadedExperiment(config, full, Hashing.Sha256TextFile(full));
        foreach (var manifest in config.Manifests)
        {
            var manifestPath = loaded.ResolveManifestPath(manifest);
            if (!File.Exists(manifestPath))
            {
                throw new ArrowException(ErrorCodes.ManifestMismatch, $"Không có manifest {manifestPath} ({manifest.Dataset})");
            }

            var actual = Hashing.Sha256TextFile(manifestPath);
            if (!string.Equals(actual, manifest.Sha256, StringComparison.OrdinalIgnoreCase))
            {
                throw new ArrowException(ErrorCodes.ManifestMismatch, $"sha256 của {manifest.Path} là {actual[..12]}…, cấu hình ghi {manifest.Sha256[..Math.Min(12, manifest.Sha256.Length)]}…");
            }
        }

        return loaded;
    }

    public void SetModelPrice(string experimentPath, string modelName, PriceConfig? price) =>
        EditExperiment(experimentPath, json => ExperimentJsonEditor.SetPrice(json, modelName, price));

    public void SetExpectedModelIdPrefix(string experimentPath, string modelName, string prefix) =>
        EditExperiment(experimentPath, json => ExperimentJsonEditor.SetExpectedPrefix(json, modelName, prefix));

    public void AddModel(string experimentPath, ModelConfig model) =>
        EditExperiment(experimentPath, json => ExperimentJsonEditor.AddModel(json, model));

    public void RemoveModel(string experimentPath, string modelName) =>
        EditExperiment(experimentPath, json => ExperimentJsonEditor.RemoveModel(json, modelName));

    private void EditExperiment(string experimentPath, Func<string, string> change)
    {
        var full = paths.Resolve(experimentPath);
        AtomicFile.WriteAllText(full, change(File.ReadAllText(full, Encoding.UTF8)));
    }

    public MachineConfig? LoadMachine() => JsonFile.TryRead<MachineConfig>(paths.MachineConfigPath);

    public void SaveMachine(MachineConfig machine) => JsonFile.Write(paths.MachineConfigPath, machine);

    public LiteLlmConfig? LoadLiteLlm()
    {
        if (!File.Exists(paths.LiteLlmConfigPath))
        {
            return null;
        }

        var config = LiteLlmYaml.Parse(File.ReadAllText(paths.LiteLlmConfigPath, Encoding.UTF8));
        RegisterSecrets(config);
        return config;
    }

    public void SaveLiteLlm(LiteLlmConfig config)
    {
        RegisterSecrets(config);
        AtomicFile.WriteAllText(paths.LiteLlmConfigPath, LiteLlmYaml.Format(config));
    }

    private static void RegisterSecrets(LiteLlmConfig config)
    {
        Redactor.Register(config.MasterKey);
        foreach (var route in config.Routes)
        {
            Redactor.Register(route.ApiKey);
            if (route.EndpointHost.Length > 0 && route.ApiBase != route.EndpointHost)
            {
                Redactor.Register(route.ApiBase, route.EndpointHost);
            }
        }
    }
}

public sealed class FileManifestRepository : IManifestRepository
{
    public IReadOnlyList<TaskRecord> ReadTasks(string manifestPath, string dataset)
    {
        var tasks = new List<TaskRecord>();
        var lineNumber = 0;
        foreach (var line in File.ReadLines(manifestPath, Encoding.UTF8))
        {
            lineNumber++;
            if (string.IsNullOrWhiteSpace(line))
            {
                continue;
            }

            try
            {
                var record = JsonFile.Deserialize<TaskRecord>(line);
                tasks.Add(record with { Dataset = dataset, RawJson = line.Trim() });
            }
            catch (JsonException ex)
            {
                throw new ArrowException(ErrorCodes.ConfigInvalid, $"{manifestPath}:{lineNumber}: dòng manifest không phải JSON hợp lệ ({ex.Message})", ex);
            }
        }

        return tasks;
    }

    public IReadOnlyList<TaskRecord> ReadTasks(LoadedExperiment experiment) =>
        experiment.Config.Manifests.SelectMany(m => ReadTasks(experiment.ResolveManifestPath(m), m.Dataset)).ToList();
}

public sealed class FilePromptRepository(FrameworkPaths paths) : IPromptRepository
{
    public PromptTemplate Load(string relativePath)
    {
        var full = Path.Combine(paths.PromptsDir, relativePath);
        if (!File.Exists(full))
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, $"Không có template prompts/{relativePath}");
        }

        return PromptTemplateParser.Parse(relativePath, File.ReadAllText(full, Encoding.UTF8));
    }

    public IReadOnlyList<PromptTemplate> LoadAll() =>
        Directory.Exists(paths.PromptsDir)
            ? Directory.GetFiles(paths.PromptsDir, "*.md", SearchOption.AllDirectories)
                .Where(f => !string.Equals(Path.GetFileName(f), "README.md", StringComparison.OrdinalIgnoreCase))
                .Select(f => Path.GetRelativePath(paths.PromptsDir, f).Replace('\\', '/'))
                .OrderBy(f => f, StringComparer.Ordinal)
                .Select(Load)
                .ToList()
            : [];

    public string ReadData(string relativePath) =>
        File.ReadAllText(Path.Combine(paths.PromptsDir, relativePath), Encoding.UTF8).Replace("\r\n", "\n");
}
