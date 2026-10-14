using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Models.Config;

namespace Arrow.Service.Interfaces;

/// <summary>
/// Cách duy nhất framework gọi model (spec 02 Mục 4, spec 07 Mục 4.3). Lỗi ném <see cref="ArrowException"/> với mã:
/// <c>B2_LLM_CONFIG</c>, <c>B2_WRONG_MODEL</c>, <c>B2_CALL_LIMIT</c> (dừng run) hoặc <c>B2_LLM_UNAVAILABLE</c> (job INFRA).
/// </summary>
public interface ILlmClient
{
    Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken);
}

/// <summary>Những gì khối 2 cần cho một lượt sinh hoặc sửa của một task.</summary>
public sealed record GenerationContext
{
    public string RunId { get; init; } = "";
    public TaskKey Key { get; init; } = new("", "");
    public RepositoryMetadata Metadata { get; init; } = new();

    /// <summary>Mã nguồn CUT nguyên văn (đọc từ checkout gốc).</summary>
    public string CutSource { get; init; } = "";
    public ModelConfig Model { get; init; } = new();

    /// <summary>Quy trình prompting của candidate (chế độ REPAIR: chỉ là một phần định danh chain).</summary>
    public string Strategy { get; init; } = "";

    /// <summary>Thư mục ghi <c>prompt.json</c>, <c>response.json</c>, <c>usage.json</c>, <c>candidate.*</c>, tính từ thư mục task.</summary>
    public string ResultDir { get; init; } = "";
}

/// <summary>Kết quả một lượt của khối 2: candidate, số liệu lượt gọi, prompt đã gửi.</summary>
public sealed record GenerationResult(CandidateTest Candidate, LlmUsage Usage, PromptRecord Prompt);

/// <summary>Khối 2 (spec 02 Mục 7): chế độ GENERATE và REPAIR, dùng chung lớp gọi model.</summary>
public interface ITestGenerator
{
    Task<GenerationResult> GenerateAsync(GenerationContext context, CancellationToken cancellationToken);

    Task<GenerationResult> RepairAsync(GenerationContext context, RepairRequest request, CancellationToken cancellationToken);
}
