using System.Text.Encodings.Web;
using System.Text.Json;
using System.Text.Json.Nodes;
using Arrow.Core.Util;

namespace Arrow.Core.Config;

/// <summary>
/// Sửa danh sách <c>models</c> của file cấu hình thực nghiệm (model chạy, tham số, giá) mà giữ nguyên các trường khác và thứ tự khóa.
/// File này commit lên Git và dùng chung cả nhóm: sửa xong thì sha256 đổi, phân công shard phải chia lại và run cũ không chạy tiếp được.
/// </summary>
public static class ExperimentModelEditor
{
    private static readonly JsonSerializerOptions WriteOptions = new()
    {
        WriteIndented = true,
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
    };

    /// <summary>Đặt giá USD cho 1 triệu token; <paramref name="price"/> null thì xóa giá (báo cáo ghi "chưa có giá").</summary>
    public static string SetPrice(string json, string modelName, PriceConfig? price) =>
        Edit(json, models =>
        {
            var model = Find(models, modelName);
            model["price_usd_per_million"] = price is null ? null : new JsonObject { ["input"] = price.Input, ["output"] = price.Output };
        });

    /// <summary>Thêm model vào cuối danh sách; đã có tên này thì báo lỗi.</summary>
    public static string AddModel(string json, ModelConfig model) =>
        Edit(json, models =>
        {
            if (models.OfType<JsonObject>().Any(m => (string?)m["name"] == model.Name))
            {
                throw new ArrowException("MODEL_EXISTS", $"Cấu hình đã có model {model.Name}");
            }

            models.Add(JsonNode.Parse(Json.Serialize(model, indented: false)));
        });

    public static string RemoveModel(string json, string modelName) =>
        Edit(json, models =>
        {
            models.Remove(Find(models, modelName));
            if (models.Count == 0)
            {
                throw new ArrowException("MODEL_REQUIRED", "Cấu hình thực nghiệm phải còn ít nhất một model");
            }
        });

    /// <summary>Đổi tiền tố model id mà provider phải trả về (kiểm tra đúng model, specs/04 Mục 1).</summary>
    public static string SetExpectedPrefix(string json, string modelName, string prefix) =>
        Edit(json, models => Find(models, modelName)["expected_model_id_prefix"] = prefix);

    private static JsonObject Find(JsonArray models, string modelName) =>
        models.OfType<JsonObject>().FirstOrDefault(m => (string?)m["name"] == modelName)
        ?? throw new ArrowException("MODEL_MISSING", $"Cấu hình không có model {modelName}");

    private static string Edit(string json, Action<JsonArray> change)
    {
        var root = JsonNode.Parse(json, documentOptions: new JsonDocumentOptions { CommentHandling = JsonCommentHandling.Skip, AllowTrailingCommas = true })
                   as JsonObject ?? throw new ArrowException("EXPERIMENT_INVALID", "File cấu hình thực nghiệm không phải object JSON");
        if (root["models"] is not JsonArray models)
        {
            models = [];
            root["models"] = models;
        }

        change(models);
        return root.ToJsonString(WriteOptions).Replace("\r\n", "\n") + "\n";
    }
}
