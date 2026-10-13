using System.Text.RegularExpressions;
using Arrow.Models.Block2;
using Arrow.Models.Common;

namespace Arrow.Service.Block2;

/// <summary>Đường dẫn các template trong <c>prompts/</c> (spec 02 Mục 2.3).</summary>
public static class PromptFiles
{
    public const string SystemZeroShot = "generation/system/zero_shot.md";
    public const string SystemZeroShotProjectAware = "generation/system/zero_shot_project_aware.md";
    public const string SystemFewShot = "generation/system/few_shot.md";
    public const string UserDefault = "generation/user/default.md";
    public const string UserProjectAware = "generation/user/project_aware.md";
    public const string RepairSystem = "repair/system.md";
    public const string RepairLevel1 = "repair/user/level1.md";
    public const string RepairLevel2 = "repair/user/level2.md";
    public const string RepairLevel3 = "repair/user/level3.md";
    public const string FailureSummaries = "repair/failure_summaries.md";
    public const string FewShotProvenance = "generation/fewshot_provenance.json";

    public static readonly IReadOnlyList<string> GenerationSystems = [SystemZeroShot, SystemZeroShotProjectAware, SystemFewShot];

    /// <summary>Mọi template framework cần để chạy; thiếu file nào cũng là lỗi.</summary>
    public static readonly IReadOnlyList<string> Required =
    [
        SystemZeroShot, SystemZeroShotProjectAware, SystemFewShot, UserDefault, UserProjectAware,
        RepairSystem, RepairLevel1, RepairLevel2, RepairLevel3, FailureSummaries,
    ];

    /// <summary>System và user template của một quy trình sinh test.</summary>
    public static (string System, string User) ForStrategy(string strategy) => strategy switch
    {
        "zero_shot" => (SystemZeroShot, UserDefault),
        "zero_shot_project_aware" => (SystemZeroShotProjectAware, UserProjectAware),
        "few_shot" => (SystemFewShot, UserDefault),
        _ => throw new ArrowException(ErrorCodes.ConfigInvalid, $"Quy trình prompting không hỗ trợ: {strategy}"),
    };
}

/// <summary>Điền chỗ trống <c>{{tên}}</c> của template. Thiếu giá trị, thừa giá trị hay chỗ trống không khai báo đều là lỗi, không gửi.</summary>
public static partial class PromptTemplateRenderer
{
    [GeneratedRegex(@"\{\{([a-z_]+)\}\}")]
    private static partial Regex Placeholder();

    /// <summary>Tên các chỗ trống xuất hiện trong nội dung template.</summary>
    public static IReadOnlySet<string> UsedPlaceholders(string body) =>
        Placeholder().Matches(body).Select(m => m.Groups[1].Value).ToHashSet(StringComparer.Ordinal);

    public static string Render(PromptTemplate template, IReadOnlyDictionary<string, string> values)
    {
        var used = UsedPlaceholders(template.Body);
        var declared = template.Placeholders.ToHashSet(StringComparer.Ordinal);
        var undeclared = used.Except(declared).ToList();
        if (undeclared.Count > 0)
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, $"{template.RelativePath}: chỗ trống không khai báo trong placeholders: {string.Join(", ", undeclared)}");
        }

        var missing = used.Where(p => !values.ContainsKey(p)).ToList();
        if (missing.Count > 0)
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, $"{template.RelativePath}: thiếu giá trị cho {string.Join(", ", missing)}");
        }

        var extra = values.Keys.Where(k => !used.Contains(k)).ToList();
        if (extra.Count > 0)
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, $"{template.RelativePath}: có giá trị cho chỗ trống không có trong template: {string.Join(", ", extra)}");
        }

        // Thay một lượt bằng regex: giá trị đã điền (ví dụ mã nguồn chứa "{{x}}") không bị thay tiếp.
        return Placeholder().Replace(template.Body, m => values[m.Groups[1].Value]);
    }
}
