using System.Text.Json.Serialization;

namespace Arrow.Models.Common;

[JsonConverter(typeof(UpperSnakeEnumConverter<TestCaseStatus>))]
public enum TestCaseStatus
{
    Passed,

    /// <summary>Assertion thất bại (<c>&lt;failure&gt;</c> trong báo cáo XML).</summary>
    Failed,

    /// <summary>Exception không phải assertion (<c>&lt;error&gt;</c>).</summary>
    Error,
    Skipped,
}

/// <summary>Một test method trong báo cáo XML của Surefire/Gradle.</summary>
public sealed record TestCaseResult
{
    /// <summary>FQN của class test (class lồng dùng <c>$</c>).</summary>
    public string ClassName { get; init; } = "";
    public string Name { get; init; } = "";
    public TestCaseStatus Status { get; init; }
    public string? Message { get; init; }
    public string? ExceptionType { get; init; }
    public double TimeSeconds { get; init; }

    /// <summary>Định danh dùng trong baseline: <c>Class#method</c>.</summary>
    [JsonIgnore]
    public string Id => $"{ClassName}#{Name}";
}
