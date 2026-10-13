namespace Arrow.Models.Block2;

/// <summary>Một message gửi model: chỉ có <c>system</c> và <c>user</c> (spec 02 Mục 4.1).</summary>
public sealed record ChatMessage(string Role, string Content);

/// <summary>Một lượt gọi model. <see cref="CallId"/> xác định duy nhất lượt gọi trong run (spec 02 Mục 4.4).</summary>
public sealed record LlmRequest
{
    public string CallId { get; init; } = "";
    public string Model { get; init; } = "";
    public List<ChatMessage> Messages { get; init; } = [];
    public Dictionary<string, object?> Params { get; init; } = [];
}

/// <summary><c>usage.json</c>: số liệu của lượt gọi thật; khi phát lại khi chạy tiếp vẫn là số của lần gọi thật.</summary>
public sealed record LlmUsage
{
    public string CallId { get; init; } = "";
    public string ModelId { get; init; } = "";
    public long PromptTokens { get; init; }
    public long CompletionTokens { get; init; }
    public string? FinishReason { get; init; }
    public double LatencySeconds { get; init; }
    public int Attempts { get; init; } = 1;
    public double CostUsd { get; init; }
    public bool Replayed { get; init; }
}

public sealed record LlmResponse
{
    public string Text { get; init; } = "";
    public LlmUsage Usage { get; init; } = new();

    /// <summary>Body gốc của API (<c>response.json</c>).</summary>
    public string RawJson { get; init; } = "{}";
}

/// <summary>Một mục của <c>llm_cache/</c>: response và số liệu gốc để phát lại khi chạy tiếp.</summary>
public sealed record LlmCacheEntry
{
    public string Key { get; init; } = "";
    public string CallId { get; init; } = "";
    public LlmResponse Response { get; init; } = new();
    public DateTimeOffset StoredAt { get; init; }
}

/// <summary>Một file template trong <c>prompts/</c> đã tách phần đầu file (spec 02 Mục 2.3).</summary>
public sealed record PromptTemplate
{
    /// <summary>Đường dẫn tương đối trong <c>prompts/</c>, dùng <c>/</c>.</summary>
    public string RelativePath { get; init; } = "";
    public string Id { get; init; } = "";
    public string Version { get; init; } = "";
    public string Role { get; init; } = "";
    public string UsedBy { get; init; } = "";
    public List<string> Placeholders { get; init; } = [];

    /// <summary>Các khóa khác trong phần đầu file (ví dụ <c>fewshot_examples</c>, <c>provenance</c>).</summary>
    public Dictionary<string, string> Extra { get; init; } = [];

    /// <summary>Nội dung gửi cho model (đã bỏ phần đầu file), xuống dòng LF.</summary>
    public string Body { get; init; } = "";

    /// <summary>sha256 của cả file (kể cả phần đầu), ghi vào <c>run.lock.json</c>.</summary>
    public string Sha256 { get; init; } = "";
}

public sealed record TemplateRef(string Id, string Version, string Sha256);

/// <summary><c>prompt.json</c>: đúng hai message đã gửi, tham số và template đã dùng.</summary>
public sealed record PromptRecord
{
    public string CallId { get; init; } = "";
    public List<ChatMessage> Messages { get; init; } = [];
    public Dictionary<string, object?> Params { get; init; } = [];
    public List<TemplateRef> Templates { get; init; } = [];
    public string SourceTruncation { get; init; } = "none";
}

/// <summary><c>candidate.json</c> (spec 02 Mục 5).</summary>
public sealed record CandidateTest
{
    public string TaskId { get; init; } = "";
    public string Model { get; init; } = "";
    public string Strategy { get; init; } = "";
    public string Mode { get; init; } = "GENERATE";
    public bool ValidOutput { get; init; }
    public string? InvalidReason { get; init; }
    public string TestFqn { get; init; } = "";
    public string TestPath { get; init; } = "";
    public string? Code { get; init; }
    public string? CodeHash { get; init; }
    public int TestMethodCount { get; init; }
    public int AssertionCount { get; init; }
    public List<string> Normalization { get; init; } = [];
    public List<string> DroppedMethods { get; init; } = [];
    public string? FinishReason { get; init; }
}
