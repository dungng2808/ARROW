using Arrow.Models.Block2;

namespace Arrow.Service.Block2;

/// <summary>
/// Kiểm tra bộ prompt trước khi chạy (spec 02 Mục 2.3): công bằng RQ1, prompt sửa dùng nguyên văn phần chung,
/// chỗ trống khai báo khớp chỗ trống thật. Trả về danh sách vấn đề; rỗng là đạt.
/// </summary>
public static class PromptConsistencyChecker
{
    private const string Shared = "Shared requirements";
    private const string Contract = "Output contract";
    private const string Technique = "Technique:";
    private const string RepairRequirements = "Repair requirements";
    private const string ConfigLabel = "Repository configuration (compatibility constraints):";

    public static IReadOnlyList<string> Check(IReadOnlyDictionary<string, PromptTemplate> templates)
    {
        var problems = new List<string>();
        PromptTemplate? Get(string path)
        {
            return templates.TryGetValue(path, out var template) ? template : null;
        }

        // 0. Có đủ mọi template bắt buộc.
        foreach (var path in PromptFiles.Required.Where(p => !templates.ContainsKey(p)))
        {
            problems.Add($"Thiếu template {path}");
        }

        // 1. Ba system prompt sinh test: "Shared requirements" và "Output contract" giống nhau từng chữ.
        var systems = PromptFiles.GenerationSystems.Select(Get).ToList();
        var parts = new List<(string Path, string Shared, string Contract)>();
        foreach (var system in systems.OfType<PromptTemplate>())
        {
            var shared = Section(system.Body, Shared, Contract);
            var contract = Section(system.Body, Contract, Technique);
            if (shared is null || contract is null)
            {
                problems.Add($"{system.RelativePath}: thiếu mục '{Shared}', '{Contract}' hoặc '{Technique}'");
                continue;
            }

            parts.Add((system.RelativePath, shared, contract));
        }

        for (var i = 1; i < parts.Count; i++)
        {
            if (parts[i].Shared != parts[0].Shared)
            {
                problems.Add($"{parts[i].Path}: '{Shared}' khác {parts[0].Path}");
            }

            if (parts[i].Contract != parts[0].Contract)
            {
                problems.Add($"{parts[i].Path}: '{Contract}' khác {parts[0].Path}");
            }
        }

        // 2. Hai user message sinh test chỉ khác khối cấu hình repository.
        var userDefault = Get(PromptFiles.UserDefault);
        var userProject = Get(PromptFiles.UserProjectAware);
        if (userDefault is not null && userProject is not null)
        {
            var withoutConfig = userProject.Body.Replace($"{ConfigLabel}\n{{{{repository_configuration}}}}\n\n", "", StringComparison.Ordinal);
            if (withoutConfig == userProject.Body)
            {
                problems.Add($"{PromptFiles.UserProjectAware}: không có khối '{ConfigLabel}' + {{{{repository_configuration}}}}");
            }
            else if (withoutConfig != userDefault.Body)
            {
                problems.Add($"{PromptFiles.UserProjectAware}: ngoài khối cấu hình repository còn khác {PromptFiles.UserDefault}");
            }
        }

        // 3. Prompt sửa dùng nguyên văn phần chung và output contract của prompt sinh.
        var repair = Get(PromptFiles.RepairSystem);
        if (repair is not null && parts.Count > 0)
        {
            var shared = Section(repair.Body, Shared, RepairRequirements);
            var contractStart = repair.Body.IndexOf(Contract, StringComparison.Ordinal);
            var contract = contractStart >= 0 ? repair.Body[contractStart..].Trim() : null;
            if (shared != parts[0].Shared)
            {
                problems.Add($"{PromptFiles.RepairSystem}: '{Shared}' không giống nguyên văn prompt sinh test");
            }

            if (contract != parts[0].Contract)
            {
                problems.Add($"{PromptFiles.RepairSystem}: '{Contract}' không giống nguyên văn prompt sinh test");
            }
        }

        // 4. Chỗ trống khai báo khớp chỗ trống thật trong nội dung.
        foreach (var template in templates.Values.OrderBy(t => t.RelativePath, StringComparer.Ordinal))
        {
            var used = PromptTemplateRenderer.UsedPlaceholders(template.Body);
            var declared = template.Placeholders.ToHashSet(StringComparer.Ordinal);
            foreach (var name in used.Except(declared))
            {
                problems.Add($"{template.RelativePath}: dùng {{{{{name}}}}} nhưng không khai báo trong placeholders");
            }

            foreach (var name in declared.Except(used))
            {
                problems.Add($"{template.RelativePath}: khai báo {name} nhưng nội dung không dùng");
            }
        }

        return problems;
    }

    /// <summary>Đoạn từ dòng bắt đầu bằng <paramref name="start"/> tới trước dòng bắt đầu bằng <paramref name="end"/>, đã bỏ khoảng trắng hai đầu.</summary>
    private static string? Section(string body, string start, string end)
    {
        var from = body.IndexOf(start, StringComparison.Ordinal);
        if (from < 0)
        {
            return null;
        }

        var to = body.IndexOf(end, from + start.Length, StringComparison.Ordinal);
        return to < 0 ? null : body[from..to].Trim();
    }
}
