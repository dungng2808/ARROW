using Arrow.Models.Block4;

namespace Arrow.Service.Interfaces;

/// <summary>Tùy chọn khi đo một bộ test: thư mục kết quả (<c>block4/&lt;suite&gt;</c>), timeout build, số lần thử khi lỗi môi trường.</summary>
public sealed record MeasureOptions
{
    public string ResultDir { get; init; } = "";
    public int StepTimeoutSeconds { get; init; } = 900;
    public int InfraAttempts { get; init; } = 3;
    public TimeSpan RetryDelay { get; init; } = TimeSpan.FromSeconds(30);
}

/// <summary>JaCoCo, PIT, tsDetect (spec 04 Mục 4.2–4.4): mỗi lần đo trên một bản sao mới của snapshot.</summary>
public interface IQualityTool
{
    /// <summary><c>jacoco</c>, <c>pit</c> hoặc <c>tsdetect</c> (tên trong <c>assessment.tools</c>).</summary>
    string Name { get; }

    Task<ToolResult> MeasureAsync(SuiteToMeasure suite, TaskContext task, MeasureOptions options, CancellationToken cancellationToken);
}

/// <summary>Sinh bộ test của hệ thống so sánh (EvoSuite, spec 04 Mục 3).</summary>
public interface IComparisonGenerator
{
    Task<ComparisonSuite> GenerateAsync(TaskContext task, CancellationToken cancellationToken);
}

/// <summary>Chạy lần lượt các công cụ trên một bộ test VALID, ghi <c>jacoco.json</c>, <c>pit.json</c>, <c>tsdetect.json</c>, <c>outcome.json</c>.</summary>
public interface IQualityAssessor
{
    Task<QualityOutcome> MeasureAsync(SuiteToMeasure suite, TaskContext task, IReadOnlyList<string> tools, MeasureOptions options, CancellationToken cancellationToken);
}

/// <summary>Lỗi của một công cụ đo: mã <c>B4_*</c>; <see cref="Retryable"/> khi do môi trường (timeout, công cụ không chạy).</summary>
public sealed class ToolFailure(string code, string message, bool retryable = false) : Exception(message)
{
    public string Code { get; } = code;
    public bool Retryable { get; } = retryable;
}
