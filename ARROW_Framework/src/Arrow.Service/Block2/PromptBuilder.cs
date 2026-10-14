using System.Text;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block1;

namespace Arrow.Service.Block2;

/// <summary>Mã CUT đưa vào prompt (đã rút gọn nếu quá dài) và cách rút gọn (spec 02 Mục 2.1).</summary>
public sealed record CutSource(string Code, string Truncation = "none");

/// <summary>Hai message đã điền và các template đã dùng.</summary>
public sealed record BuiltPrompt(IReadOnlyList<ChatMessage> Messages, IReadOnlyList<TemplateRef> Templates, string UserTemplate);

/// <summary>
/// Dựng prompt (spec 02 Mục 2–3): chọn template theo quy trình (sinh) hoặc theo mức (sửa), điền chỗ trống. Code thuần:
/// nhận template đã đọc, M, mã CUT; không đọc file, không gọi model.
/// </summary>
public static class PromptBuilder
{
    /// <summary>Số lần thử gần nhất hiện đủ chi tiết trong <c>{{failed_attempts}}</c> (spec 03 Mục 6.4).</summary>
    public const int DetailedAttempts = 3;

    public const string None = "(none)";

    /// <summary>Chế độ GENERATE: system và user theo quy trình; khối M chỉ có ở <c>zero_shot_project_aware</c>.</summary>
    public static BuiltPrompt Generation(string strategy, PromptTemplate system, PromptTemplate user, RepositoryMetadata metadata, CutSource cut)
    {
        var values = new Dictionary<string, string>(StringComparer.Ordinal)
        {
            ["cut_fqn"] = metadata.Cut.Fqn,
            ["cut_path"] = metadata.Paths.CutPath,
            ["test_package"] = metadata.Cut.Package,
            ["test_class_name"] = TestClassName(metadata),
            ["cut_source"] = cut.Code.TrimEnd('\n'),
        };
        if (strategy == "zero_shot_project_aware")
        {
            values["repository_configuration"] = MetadataPromptRenderer.Render(metadata);
        }

        return Build(system, user, values);
    }

    /// <summary>
    /// Chế độ REPAIR (spec 02 Mục 1.2, spec 03 Mục 6.5): system <c>repair/system.md</c>, user theo <see cref="RepairRequest.Level"/>;
    /// khối M đầy đủ ở mọi mức; mã gửi đi là <see cref="RepairRequest.BaseCode"/>.
    /// </summary>
    public static BuiltPrompt Repair(RepairRequest request, PromptTemplate system, PromptTemplate user, PromptTemplate failureSummaries, RepositoryMetadata metadata, CutSource cut)
    {
        var values = new Dictionary<string, string>(StringComparer.Ordinal)
        {
            ["failure_summary"] = FailureSummary(failureSummaries, request.State),
            ["test_package"] = metadata.Cut.Package,
            ["test_class_name"] = TestClassName(metadata),
            ["repository_configuration"] = MetadataPromptRenderer.Render(metadata),
            ["error_block"] = OrNone(request.ErrorBlock),
            ["test_source"] = request.BaseCode.Replace("\r\n", "\n").TrimEnd('\n'),
            ["cut_fqn"] = metadata.Cut.Fqn,
            ["cut_path"] = metadata.Paths.CutPath,
            ["cut_source"] = cut.Code.TrimEnd('\n'),
        };
        if (request.Level >= 2)
        {
            values["extra_context"] = OrNone(request.ExtraContext);
            values["failed_attempts"] = FormatAttempts(request.FailedAttempts);
        }

        if (request.Level == 3)
        {
            values["failing_methods"] = string.Join(", ", request.RewriteMethods.Select(m => $"`{m}`"));
        }

        return Build(system, user, values);
    }

    /// <summary>Template user của một mức sửa (Fixed luôn mức 1).</summary>
    public static string RepairUserTemplate(int level) => level switch
    {
        1 => PromptFiles.RepairLevel1,
        2 => PromptFiles.RepairLevel2,
        3 => PromptFiles.RepairLevel3,
        _ => throw new ArgumentOutOfRangeException(nameof(level), level, "Mức sửa phải là 1, 2 hoặc 3"),
    };

    /// <summary>Tên ngắn của class test sinh ra (<c>OrderService_ArrowTest</c>).</summary>
    public static string TestClassName(RepositoryMetadata metadata)
    {
        var fqn = metadata.Paths.GeneratedTestFqn;
        return fqn[(fqn.LastIndexOf('.') + 1)..];
    }

    /// <summary>
    /// <c>{{failed_attempts}}</c>: 3 lần thử gần nhất có đủ signature, lỗi đầu tiên, dòng mã đã đổi; lần cũ hơn chỉ một dòng
    /// <c>Round r · signature</c> (spec 03 Mục 6.4).
    /// </summary>
    public static string FormatAttempts(IReadOnlyList<Attempt> attempts)
    {
        if (attempts.Count == 0)
        {
            return "- " + None;
        }

        var text = new StringBuilder();
        for (var i = 0; i < attempts.Count; i++)
        {
            var attempt = attempts[i];
            text.Append($"- Round {attempt.Round} · {attempt.Signature}\n");
            if (i < attempts.Count - DetailedAttempts)
            {
                continue;
            }

            if (!string.IsNullOrWhiteSpace(attempt.FirstError))
            {
                text.Append($"  First error: {attempt.FirstError}\n");
            }

            if (!string.IsNullOrWhiteSpace(attempt.Change))
            {
                text.Append("  Changed code:\n");
                foreach (var line in attempt.Change.Replace("\r\n", "\n").TrimEnd('\n').Split('\n'))
                {
                    text.Append("    ").Append(line).Append('\n');
                }
            }
        }

        return text.ToString().TrimEnd('\n');
    }

    /// <summary>Câu mô tả lỗi của một trạng thái trong <c>repair/failure_summaries.md</c> (dòng <c>STATE: câu</c>).</summary>
    public static string FailureSummary(PromptTemplate summaries, VerificationState state)
    {
        var key = state.ToString().ToUpperInvariant() + ":";
        var line = summaries.Body.Split('\n').FirstOrDefault(l => l.StartsWith(key, StringComparison.Ordinal))
                   ?? throw new ArrowException(ErrorCodes.PromptInvalid, $"{summaries.RelativePath}: không có câu cho trạng thái {state.ToString().ToUpperInvariant()}");
        return line[key.Length..].Trim();
    }

    private static string OrNone(string? value) => string.IsNullOrWhiteSpace(value) ? None : value.Replace("\r\n", "\n").TrimEnd('\n');

    private static BuiltPrompt Build(PromptTemplate system, PromptTemplate user, Dictionary<string, string> values)
    {
        // Mỗi template chỉ nhận đúng các chỗ trống nó có (system prompt không có chỗ trống).
        var userValues = values.Where(v => user.Placeholders.Contains(v.Key)).ToDictionary(v => v.Key, v => v.Value, StringComparer.Ordinal);
        var systemValues = values.Where(v => system.Placeholders.Contains(v.Key)).ToDictionary(v => v.Key, v => v.Value, StringComparer.Ordinal);
        var messages = new List<ChatMessage>
        {
            new("system", PromptTemplateRenderer.Render(system, systemValues)),
            new("user", PromptTemplateRenderer.Render(user, userValues)),
        };
        return new BuiltPrompt(messages, [Ref(system), Ref(user)], user.RelativePath);
    }

    private static TemplateRef Ref(PromptTemplate template) => new(template.Id, template.Version, template.Sha256);
}
