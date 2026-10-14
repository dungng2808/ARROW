using System.Diagnostics;
using System.Security.Cryptography;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

/// <summary>
/// Model, provider, khóa API, giá (S3, spec 06 Mục 5). Tham số và giá nằm ở cấu hình thực nghiệm (dùng chung); provider, endpoint, khóa
/// nằm ở <c>litellm.config.local.yaml</c> của máy (không commit). Khóa không bao giờ trả ra ngoài, chỉ 4 ký tự cuối.
/// </summary>
public sealed class ModelConfigService(FrameworkPaths paths, IConfigRepository config, ILogRepository log) : IModelConfigService
{
    public IReadOnlyList<string> Experiments() => config.ListExperiments();

    public ModelsView Get(string experimentPath)
    {
        var experiment = config.LoadExperiment(experimentPath);
        var liteLlm = config.LoadLiteLlm();
        return new ModelsView(experiment.Path, experiment.Sha256,
            experiment.Config.Models.Select(m => new ModelView(m, liteLlm?.Route(m.Name), KeyHint(liteLlm?.Route(m.Name)))).ToList());
    }

    /// <summary>Gợi ý khóa: <c>...2a4b</c>, <c>(chưa có)</c> hoặc <c>(không cần)</c> với Ollama.</summary>
    public static string KeyHint(LiteLlmRoute? route) =>
        route is null ? "(chưa có)"
        : route.HasApiKey ? "..." + route.ApiKey[^Math.Min(4, route.ApiKey.Length)..]
        : route.NeedsApiKey ? "(chưa có)" : "(không cần)";

    public LiteLlmRoute SaveRoute(string modelName, string providerModel, string apiBase, string apiKey)
    {
        var current = config.LoadLiteLlm() ?? new LiteLlmConfig { MasterKey = "sk-arrow-" + Convert.ToHexString(RandomNumberGenerator.GetBytes(16)).ToLowerInvariant() };
        var old = current.Route(modelName);
        var route = new LiteLlmRoute
        {
            ModelName = modelName,
            ProviderModel = providerModel,
            ApiBase = apiBase,
            ApiKey = apiKey.Length > 0 ? apiKey : old?.ApiKey ?? "",
        };
        var routes = current.Routes.Where(r => r.ModelName != modelName).Append(route).ToList();
        config.SaveLiteLlm(current with { Routes = routes });
        log.Info($"models: lưu provider của {modelName} ({providerModel} · {route.EndpointHost})");
        return route;
    }

    public void SetPrice(string experimentPath, string modelName, PriceConfig? price) => config.SetModelPrice(experimentPath, modelName, price);

    public void SetExpectedPrefix(string experimentPath, string modelName, string prefix) => config.SetExpectedModelIdPrefix(experimentPath, modelName, prefix);

    public void AddModel(string experimentPath, ModelConfig model) => config.AddModel(experimentPath, model);

    public void RemoveModel(string experimentPath, string modelName) => config.RemoveModel(experimentPath, modelName);

    public async Task<ModelTestResult> TestAsync(string experimentPath, string modelName, CancellationToken cancellationToken)
    {
        var model = config.LoadExperiment(experimentPath).Config.Models.FirstOrDefault(m => m.Name == modelName)
                    ?? throw new ArrowException(ErrorCodes.ModelMissing, $"Cấu hình không có model {modelName}");
        var liteLlm = config.LoadLiteLlm();
        if (liteLlm?.Route(modelName) is not { IsComplete: true })
        {
            return new ModelTestResult(false, null, null, false, 0, 0, 0, 0, "chưa khai báo provider/endpoint/khóa");
        }

        var machine = config.LoadMachine()?.Llm ?? new LlmMachineConfig();
        await using var proxy = new LiteLlmProxy(machine, paths, log);
        var watch = Stopwatch.StartNew();
        try
        {
            await proxy.EnsureRunningAsync(cancellationToken).ConfigureAwait(false);
            using var http = new HttpClient { Timeout = Timeout.InfiniteTimeSpan };
            var client = new LiteLlmClient(http, machine.ProxyUrl, liteLlm.MasterKey, log, new LlmRetryOptions { Delays = [], RequestTimeout = TimeSpan.FromSeconds(120) });
            var request = new LlmRequest
            {
                CallId = $"test/{modelName}",
                Model = modelName,
                Messages = [new ChatMessage("system", "You are a connectivity check."), new ChatMessage("user", "Reply with the single word OK.")],
                Params = model.Params,
            };
            var response = await client.CompleteAsync(request, cancellationToken).ConfigureAwait(false);
            var usage = response.Usage;
            return new ModelTestResult(true, response.Text.Trim(), usage.ModelId, LlmGateway.ModelMatches(model.ExpectedModelIdPrefix, usage.ModelId),
                usage.PromptTokens, usage.CompletionTokens, Math.Round(watch.Elapsed.TotalSeconds, 2), LlmGateway.Cost(model.PriceUsdPerMillion, usage), null);
        }
        catch (ArrowException ex)
        {
            return new ModelTestResult(false, null, null, false, 0, 0, Math.Round(watch.Elapsed.TotalSeconds, 2), 0, $"{ex.Code}: {ex.Message}");
        }
    }
}
