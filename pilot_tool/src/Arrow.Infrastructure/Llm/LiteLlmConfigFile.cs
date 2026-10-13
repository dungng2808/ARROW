using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using Arrow.Core.Util;

namespace Arrow.Infrastructure.Llm;

/// <summary>Kiểu provider của một model trong LiteLLM; quyết định tiền tố <c>model</c> và có cần khóa API không.</summary>
public enum LlmProviderKind
{
    /// <summary>Endpoint tương thích OpenAI (OpenAI, TeamoRouter, OpenRouter…): <c>openai/&lt;model&gt;</c>, cần khóa.</summary>
    OpenAiCompatible,

    /// <summary>Ollama tự host: <c>ollama_chat/&lt;model&gt;</c>, không cần khóa.</summary>
    Ollama,

    /// <summary>Chuỗi model LiteLLM nhập tay (ví dụ <c>anthropic/…</c>, <c>hosted_vllm/…</c>).</summary>
    Other,
}

/// <summary>
/// Một model trong <c>model_list</c> của LiteLLM: <see cref="ModelName"/> là tên model trong cấu hình thực nghiệm
/// (tool gửi tên này tới proxy), <see cref="ProviderModel"/> là model thật ở provider.
/// </summary>
public sealed record LiteLlmRoute
{
    public string ModelName { get; init; } = "";
    public string ProviderModel { get; init; } = "";
    public string ApiBase { get; init; } = "";
    public string ApiKey { get; init; } = "";

    public bool HasApiKey => ApiKey.Length > 0;

    public LlmProviderKind Kind => ProviderModel switch
    {
        _ when ProviderModel.StartsWith("ollama", StringComparison.Ordinal) => LlmProviderKind.Ollama,
        _ when ProviderModel.StartsWith("openai/", StringComparison.Ordinal) => LlmProviderKind.OpenAiCompatible,
        _ => LlmProviderKind.Other,
    };

    /// <summary>Ollama tự host không cần khóa; các provider khác cần.</summary>
    public bool NeedsApiKey => Kind != LlmProviderKind.Ollama;

    /// <summary>Route dùng được: có model, endpoint, và khóa nếu provider cần.</summary>
    public bool IsComplete => ProviderModel.Length > 0 && ApiBase.Length > 0 && (HasApiKey || !NeedsApiKey);

    /// <summary>Tên máy chủ của endpoint (ghi vào kết quả thay cho địa chỉ đầy đủ).</summary>
    public string EndpointHost => Uri.TryCreate(ApiBase, UriKind.Absolute, out var uri) ? uri.Host : "";
}

/// <summary>
/// File cấu hình LiteLLM proxy của máy (<c>litellm.config.local.yaml</c>, không commit): mỗi model của thực nghiệm một route
/// (endpoint của provider, khóa API) và khóa bảo vệ proxy (specs/01 §2, SETUP.md bước 6).
/// </summary>
public sealed record LiteLlmConfigFile
{
    public List<LiteLlmRoute> Routes { get; init; } = [];
    public string MasterKey { get; init; } = "";

    public LiteLlmRoute? Route(string modelName) => Routes.FirstOrDefault(r => r.ModelName == modelName);

    /// <summary>Thêm mới hoặc thay route cùng tên model, giữ thứ tự.</summary>
    public LiteLlmConfigFile WithRoute(LiteLlmRoute route)
    {
        var routes = Routes.ToList();
        var index = routes.FindIndex(r => r.ModelName == route.ModelName);
        if (index >= 0)
        {
            routes[index] = route;
        }
        else
        {
            routes.Add(route);
        }

        return this with { Routes = routes };
    }

    public LiteLlmConfigFile WithoutRoute(string modelName) => this with { Routes = Routes.Where(r => r.ModelName != modelName).ToList() };

    /// <summary>Tên máy chủ endpoint của các model, theo thứ tự, không lặp (ghi vào run_config.lock.json).</summary>
    public string EndpointHosts(IEnumerable<string>? modelNames = null)
    {
        var routes = modelNames is null ? Routes : modelNames.Select(Route).OfType<LiteLlmRoute>();
        return string.Join(", ", routes.Select(r => r.EndpointHost).Where(h => h.Length > 0).Distinct());
    }

    public static string NewMasterKey() => "sk-local-" + Convert.ToHexString(RandomNumberGenerator.GetBytes(16)).ToLowerInvariant();

    public string ToYaml()
    {
        var yaml = new StringBuilder()
            .Append("# Cấu hình LiteLLM proxy của máy này. KHÔNG commit file này (chứa khóa API).\n")
            .Append("# Sửa bằng arrow models (hoặc menu \"Model, provider, khóa API, giá\").\n")
            .Append("model_list:\n");
        foreach (var route in Routes)
        {
            yaml.Append("  - model_name: ").Append(route.ModelName).Append('\n')
                .Append("    litellm_params:\n")
                .Append("      model: ").Append(route.ProviderModel).Append('\n')
                .Append("      api_base: ").Append(route.ApiBase).Append('\n');
            if (route.HasApiKey)
            {
                yaml.Append("      api_key: ").Append(route.ApiKey).Append('\n');
            }

            yaml.Append("      # Cho LiteLLM chuyển reasoning_effort tới provider (LiteLLM chưa biết model này hỗ trợ tham số đó);\n")
                .Append("      # không dùng drop_params vì sẽ âm thầm bỏ tham số của cấu hình thực nghiệm (specs/04 Mục 1).\n")
                .Append("      allowed_openai_params: [\"reasoning_effort\"]\n");
        }

        return yaml.Append("general_settings:\n")
            .Append("  master_key: ").Append(MasterKey).Append('\n')
            .Append('\n').ToString();
    }

    public void Save(string path) => AtomicFile.WriteAllText(path, ToYaml());

    /// <summary>Đọc lại file do tool ghi (định dạng cố định ở <see cref="ToYaml"/>; đọc được cả bản cũ chỉ có một model).</summary>
    public static LiteLlmConfigFile? TryLoad(string path) => File.Exists(path) ? Parse(File.ReadAllText(path)) : null;

    public static LiteLlmConfigFile Parse(string text)
    {
        static string Value(string block, string key) =>
            Regex.Match(block, $@"(?m)^\s*-?\s*{key}:\s*(\S+)\s*$") is { Success: true } m ? m.Groups[1].Value : "";

        text = text.Replace("\r\n", "\n");
        var settingsAt = text.IndexOf("\ngeneral_settings:", StringComparison.Ordinal);
        var modelPart = settingsAt >= 0 ? text[..settingsAt] : text;
        var routes = Regex.Split(modelPart, @"(?m)^(?=\s*-\s*model_name:)").Skip(1)
            .Select(block => new LiteLlmRoute
            {
                ModelName = Value(block, "model_name"),
                ProviderModel = Value(block, "model"),
                ApiBase = Value(block, "api_base"),
                ApiKey = Value(block, "api_key"),
            })
            .Where(r => r.ModelName.Length > 0)
            .ToList();
        return new LiteLlmConfigFile
        {
            Routes = routes,
            MasterKey = settingsAt >= 0 ? Value(text[settingsAt..], "master_key") : "",
        };
    }

    /// <summary>Che khóa: chỉ giữ 4 ký tự cuối.</summary>
    public static string Mask(string key) => key.Length <= 4 ? "****" : new string('*', Math.Min(28, key.Length - 4)) + key[^4..];
}
