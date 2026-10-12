using System.Security.Cryptography;
using System.Text.RegularExpressions;
using Arrow.Core.Util;

namespace Arrow.Infrastructure.Llm;

/// <summary>
/// File cấu hình LiteLLM proxy của máy (<c>litellm.config.local.yaml</c>, không commit): chứa khóa API,
/// endpoint của provider và khóa bảo vệ proxy (specs/01 §2, SETUP.md bước 6).
/// </summary>
public sealed record LiteLlmConfigFile
{
    public string ModelName { get; init; } = "gpt-6-luna";
    public string ProviderModel { get; init; } = "openai/gpt-6-luna";
    public string ApiBase { get; init; } = "https://api.openai.com/v1";
    public string ApiKey { get; init; } = "";
    public string MasterKey { get; init; } = "";

    public bool HasApiKey => ApiKey.Length > 0;

    /// <summary>Tên máy chủ của endpoint (ghi vào kết quả thay cho địa chỉ đầy đủ).</summary>
    public string EndpointHost => Uri.TryCreate(ApiBase, UriKind.Absolute, out var uri) ? uri.Host : "";

    public static string NewMasterKey() => "sk-local-" + Convert.ToHexString(RandomNumberGenerator.GetBytes(16)).ToLowerInvariant();

    public string ToYaml() =>
        $"""
        # Cấu hình LiteLLM proxy của máy này. KHÔNG commit file này (chứa khóa API).
        model_list:
          - model_name: {ModelName}
            litellm_params:
              model: {ProviderModel}
              api_base: {ApiBase}
              api_key: {ApiKey}
              # Cho LiteLLM chuyển reasoning_effort tới provider (LiteLLM chưa biết model này hỗ trợ tham số đó);
              # không dùng drop_params vì sẽ âm thầm bỏ tham số của cấu hình thực nghiệm (specs/04 Mục 1).
              allowed_openai_params: ["reasoning_effort"]
        general_settings:
          master_key: {MasterKey}

        """;

    public void Save(string path) => AtomicFile.WriteAllText(path, ToYaml());

    /// <summary>Đọc lại file do tool ghi (định dạng cố định ở <see cref="ToYaml"/>).</summary>
    public static LiteLlmConfigFile? TryLoad(string path)
    {
        if (!File.Exists(path))
        {
            return null;
        }

        var text = File.ReadAllText(path);
        string Value(string key) => Regex.Match(text, $@"(?m)^\s*-?\s*{key}:\s*(\S+)\s*$") is { Success: true } m ? m.Groups[1].Value : "";
        return new LiteLlmConfigFile
        {
            ModelName = Value("model_name"),
            ProviderModel = Value("model"),
            ApiBase = Value("api_base"),
            ApiKey = Value("api_key"),
            MasterKey = Value("master_key"),
        };
    }

    /// <summary>Che khóa: chỉ giữ 4 ký tự cuối.</summary>
    public static string Mask(string key) => key.Length <= 4 ? "****" : new string('*', Math.Min(28, key.Length - 4)) + key[^4..];
}
