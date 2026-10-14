using System.Globalization;
using System.Text;
using System.Text.Json;
using System.Text.Json.Nodes;
using System.Text.RegularExpressions;
using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;

namespace Arrow.DataAccess.Common;

/// <summary>Đọc/ghi <c>litellm.config.local.yaml</c> theo định dạng cố định (mỗi model một mục trong <c>model_list</c>).</summary>
public static partial class LiteLlmYaml
{
    public static string Format(LiteLlmConfig config)
    {
        var yaml = new StringBuilder()
            .Append("# Cấu hình LiteLLM proxy của máy này. KHÔNG commit file này (chứa khóa API).\n")
            .Append("# Sửa bằng arrow models.\n")
            .Append("model_list:\n");
        foreach (var route in config.Routes)
        {
            yaml.Append("  - model_name: ").Append(route.ModelName).Append('\n')
                .Append("    litellm_params:\n")
                .Append("      model: ").Append(route.ProviderModel).Append('\n')
                .Append("      api_base: ").Append(route.ApiBase).Append('\n');
            if (route.HasApiKey)
            {
                yaml.Append("      api_key: ").Append(route.ApiKey).Append('\n');
            }

            // Cho LiteLLM chuyển reasoning_effort tới provider; không dùng drop_params (spec 02 Mục 4.2).
            yaml.Append("      allowed_openai_params: [\"reasoning_effort\"]\n");
        }

        return yaml.Append("general_settings:\n").Append("  master_key: ").Append(config.MasterKey).Append('\n').ToString();
    }

    public static LiteLlmConfig Parse(string text)
    {
        text = text.Replace("\r\n", "\n");
        var settingsAt = text.IndexOf("\ngeneral_settings:", StringComparison.Ordinal);
        var models = settingsAt >= 0 ? text[..settingsAt] : text;
        var routes = Regex.Split(models, @"(?m)^(?=\s*-\s*model_name:)").Skip(1)
            .Select(block => new LiteLlmRoute
            {
                ModelName = Value(block, "model_name"),
                ProviderModel = Value(block, "model"),
                ApiBase = Value(block, "api_base"),
                ApiKey = Value(block, "api_key"),
            })
            .Where(r => r.ModelName.Length > 0)
            .ToList();
        return new LiteLlmConfig { Routes = routes, MasterKey = settingsAt >= 0 ? Value(text[settingsAt..], "master_key") : "" };
    }

    private static string Value(string block, string key) =>
        Regex.Match(block, $@"(?m)^\s*-?\s*{key}:\s*(\S+)\s*$") is { Success: true } m ? m.Groups[1].Value : "";
}

/// <summary>Đọc file template trong <c>prompts/</c>: phần đầu file giữa hai dòng <c>---</c>, rồi nội dung gửi model.</summary>
public static class PromptTemplateParser
{
    public static PromptTemplate Parse(string relativePath, string fileText)
    {
        var text = fileText.Replace("\r\n", "\n");
        if (!text.StartsWith("---\n", StringComparison.Ordinal))
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, $"{relativePath}: thiếu phần đầu file (dòng đầu phải là ---)");
        }

        var end = text.IndexOf("\n---\n", 3, StringComparison.Ordinal);
        if (end < 0)
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, $"{relativePath}: phần đầu file không có dòng --- đóng");
        }

        var fields = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var line in text[4..end].Split('\n', StringSplitOptions.RemoveEmptyEntries))
        {
            var colon = line.IndexOf(':');
            if (colon <= 0)
            {
                throw new ArrowException(ErrorCodes.PromptInvalid, $"{relativePath}: dòng không đúng dạng 'khóa: giá trị': {line}");
            }

            fields[line[..colon].Trim()] = line[(colon + 1)..].Trim();
        }

        string Take(string key) => fields.Remove(key, out var value) ? value : "";
        var template = new PromptTemplate
        {
            RelativePath = relativePath.Replace('\\', '/'),
            Id = Take("id"),
            Version = Take("version"),
            Role = Take("role"),
            UsedBy = Take("used_by"),
            Placeholders = Take("placeholders").Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries).ToList(),
            Body = text[(end + 5)..].TrimEnd('\n'),
            Sha256 = Hashing.Sha256Text(text),
        };
        if (template.Id.Length == 0 || template.Version.Length == 0 || template.Role.Length == 0)
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, $"{relativePath}: phần đầu file phải có id, version, role");
        }

        return template with { Extra = fields };
    }
}

/// <summary>Sửa mục <c>models</c> của file cấu hình thực nghiệm, giữ nguyên các trường khác và thứ tự khóa.</summary>
public static class ExperimentJsonEditor
{
    private static readonly JsonSerializerOptions WriteOptions = new()
    {
        WriteIndented = true,
        Encoder = System.Text.Encodings.Web.JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
    };

    public static string SetPrice(string json, string modelName, PriceConfig? price) =>
        Edit(json, models => Find(models, modelName)["price_usd_per_million"] = price is null
            ? null
            : new JsonObject { ["input"] = price.Input, ["output"] = price.Output });

    public static string SetExpectedPrefix(string json, string modelName, string prefix) =>
        Edit(json, models => Find(models, modelName)["expected_model_id_prefix"] = prefix);

    public static string AddModel(string json, ModelConfig model) =>
        Edit(json, models =>
        {
            if (models.OfType<JsonObject>().Any(m => (string?)m["name"] == model.Name))
            {
                throw new ArrowException(ErrorCodes.ModelExists, $"Cấu hình đã có model {model.Name}");
            }

            models.Add(JsonNode.Parse(JsonFile.Serialize(model, indented: false)));
        });

    public static string RemoveModel(string json, string modelName) =>
        Edit(json, models =>
        {
            models.Remove(Find(models, modelName));
            if (models.Count == 0)
            {
                throw new ArrowException(ErrorCodes.ModelRequired, "Cấu hình thực nghiệm phải còn ít nhất một model");
            }
        });

    private static JsonObject Find(JsonArray models, string modelName) =>
        models.OfType<JsonObject>().FirstOrDefault(m => (string?)m["name"] == modelName)
        ?? throw new ArrowException(ErrorCodes.ModelMissing, $"Cấu hình không có model {modelName}");

    private static string Edit(string json, Action<JsonArray> change)
    {
        var root = JsonNode.Parse(json, documentOptions: new JsonDocumentOptions { CommentHandling = JsonCommentHandling.Skip, AllowTrailingCommas = true }) as JsonObject
                   ?? throw new ArrowException(ErrorCodes.ConfigInvalid, "File cấu hình thực nghiệm không phải object JSON");
        if (root["models"] is not JsonArray models)
        {
            models = [];
            root["models"] = models;
        }

        change(models);
        return root.ToJsonString(WriteOptions).Replace("\r\n", "\n") + "\n";
    }
}

/// <summary>Định dạng thời điểm trong log: giờ địa phương, có độ lệch múi giờ.</summary>
internal static class TimeFormat
{
    public static string Local(DateTimeOffset at) => at.ToLocalTime().ToString("yyyy-MM-dd'T'HH:mm:ss.fffzzz", CultureInfo.InvariantCulture);
}
