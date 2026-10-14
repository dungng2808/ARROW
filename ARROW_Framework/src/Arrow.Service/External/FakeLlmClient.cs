using Arrow.Models.Block2;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>
/// Model giả cho test và <c>--fake-llm</c> (spec 07 Mục 4.3): chọn câu trả lời theo luật khớp nội dung prompt, luật thêm trước được
/// xét trước; không luật nào khớp thì trả <see cref="DefaultReply"/>. Không gọi mạng, không tốn tiền.
/// </summary>
public sealed class FakeLlmClient : ILlmClient
{
    private readonly List<(Func<LlmRequest, bool> Match, Func<LlmRequest, string> Reply)> _rules = [];
    private readonly List<LlmRequest> _calls = [];
    private readonly object _lock = new();

    /// <summary>Câu trả lời khi không luật nào khớp (mặc định: không có mã).</summary>
    public Func<LlmRequest, string> DefaultReply { get; set; } = _ => "I cannot help with that.";

    /// <summary>Model id ghi vào response (mặc định: đúng tên model của request).</summary>
    public Func<LlmRequest, string> ModelId { get; set; } = r => r.Model;

    public IReadOnlyList<LlmRequest> Calls
    {
        get
        {
            lock (_lock)
            {
                return _calls.ToList();
            }
        }
    }

    public FakeLlmClient When(Func<LlmRequest, bool> match, Func<LlmRequest, string> reply)
    {
        lock (_lock)
        {
            _rules.Add((match, reply));
        }

        return this;
    }

    /// <summary>
    /// Model giả của <c>--fake-llm</c>: lượt sinh trả một test class tối thiểu đúng package, tên class và framework trong prompt
    /// (JUnit 4 khi prompt không nêu framework); lượt sửa trả lại nguyên mã đang sửa.
    /// </summary>
    public static FakeLlmClient ForPipeline() => new() { DefaultReply = MinimalTest };

    private static string MinimalTest(LlmRequest request)
    {
        var user = request.Messages.LastOrDefault(m => m.Role == "user")?.Content ?? "";
        var current = System.Text.RegularExpressions.Regex.Match(user, "Current test class:\n```java\n(?<code>.*?)\n```", System.Text.RegularExpressions.RegexOptions.Singleline);
        if (current.Success)
        {
            return current.Groups["code"].Value + "\n";
        }

        string Field(string label) => System.Text.RegularExpressions.Regex.Match(user, $@"- {label}: (?<v>[^\n]*)") is { Success: true } m ? m.Groups["v"].Value.Trim() : "";
        var package = Field("Package");
        var name = Field("Class name");
        var framework = Field("Test framework");
        var (imports, method) = framework.StartsWith("JUnit 5", StringComparison.Ordinal)
            ? ("import static org.junit.jupiter.api.Assertions.assertNotNull;\n\nimport org.junit.jupiter.api.Test;", "@Test\n    public void createsObject()")
            : framework.StartsWith("TestNG", StringComparison.Ordinal)
                ? ("import static org.testng.Assert.assertNotNull;\n\nimport org.testng.annotations.Test;", "@Test\n    public void createsObject()")
                : ("import static org.junit.Assert.assertNotNull;\n\nimport org.junit.Test;", "@Test\n    public void createsObject()");
        return $"{(package.Length > 0 ? $"package {package};\n\n" : "")}{imports}\n\npublic class {(name.Length > 0 ? name : "GeneratedTest")} {{\n    {method} {{\n        assertNotNull(new Object());\n    }}\n}}\n";
    }

    /// <summary>Luật: message user chứa <paramref name="text"/> thì trả <paramref name="reply"/>.</summary>
    public FakeLlmClient WhenUserContains(string text, string reply) =>
        When(r => r.Messages.Any(m => m.Role == "user" && m.Content.Contains(text, StringComparison.Ordinal)), _ => reply);

    public Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken cancellationToken)
    {
        cancellationToken.ThrowIfCancellationRequested();
        Func<LlmRequest, string> reply;
        lock (_lock)
        {
            _calls.Add(request);
            reply = _rules.FirstOrDefault(r => r.Match(request)).Reply ?? DefaultReply;
        }

        var text = reply(request);
        var promptChars = request.Messages.Sum(m => m.Content.Length);
        return Task.FromResult(new LlmResponse
        {
            Text = text,
            RawJson = System.Text.Json.JsonSerializer.Serialize(new { model = ModelId(request), fake = true, choices = new[] { new { message = new { role = "assistant", content = text }, finish_reason = "stop" } } }),
            Usage = new LlmUsage
            {
                CallId = request.CallId,
                ModelId = ModelId(request),
                PromptTokens = promptChars / 4,
                CompletionTokens = text.Length / 4,
                FinishReason = "stop",
                LatencySeconds = 0.01,
                Attempts = 1,
            },
        });
    }
}
