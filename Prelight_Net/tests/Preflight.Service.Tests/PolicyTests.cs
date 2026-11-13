using Preflight.Models;
using Preflight.Service.Core.Policy;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class PolicyTests
{
    public static IEnumerable<object[]> Statuses => Enum.GetValues<PreflightStatus>().Select(s => new object[] { s });
    [Theory]
    [MemberData(nameof(Statuses))]
    public void EachPreflightStatusIsSelectableOnItsOwn(PreflightStatus status) => Assert.Equal(status, StatusPolicy.SelectPrimaryStatus([status.ToString()]));
    [Fact]
    public void UnknownStatusIsRejected() => Assert.Throws<ArgumentException>(() => StatusPolicy.SelectPrimaryStatus(["NOT_A_STATUS"]));
    [Theory]
    [InlineData("test", "class", true, false, false, "EXCLUDE_TEST_SOURCE")]
    [InlineData("unknown", "class", true, false, false, "EXCLUDE_NON_PRODUCTION_SOURCE")]
    [InlineData("production", "class", false, false, false, "EXCLUDE_UNSUPPORTED_DECLARATION")]
    [InlineData("production", "interface", true, false, false, "EXCLUDE_UNSUPPORTED_DECLARATION")]
    [InlineData("production", "annotation", true, false, false, "EXCLUDE_UNSUPPORTED_DECLARATION")]
    [InlineData("production", "enum", true, false, false, "EXCLUDE_STRICT_COHORT")]
    [InlineData("production", "record", true, false, false, "EXCLUDE_STRICT_COHORT")]
    [InlineData("production", "class", true, true, false, "EXCLUDE_ABSTRACT_CLASS")]
    [InlineData("production", "class", true, false, true, "EXCLUDE_GENERATED_SOURCE")]
    public void ExcludedDeclarationsReceiveReasonCodes(string sourceSet, string kind, bool top, bool abstractClass, bool generated, string expected)
    {
        var java = new JavaClass { SourceSet = sourceSet, Kind = kind, IsTopLevel = top, Modifiers = abstractClass ? new(["abstract"], StringComparer.Ordinal) : new(StringComparer.Ordinal), Tags = generated ? new(["GENERATED_SOURCE"], StringComparer.Ordinal) : new(StringComparer.Ordinal) };
        var result = ClassPolicy.Evaluate(java);
        Assert.Equal(expected, result.Policy); Assert.Contains(expected, result.Reasons);
    }
    [Theory]
    [InlineData("final")]
    [InlineData("STATIC_UTILITY")]
    [InlineData("PACKAGE_PRIVATE")]
    [InlineData("LOW_LOGIC")]
    [InlineData("CONSTRUCTOR_ONLY")]
    [InlineData("DB_DEPENDENT")]
    [InlineData("DIRECT_CONNECTION")]
    [InlineData("EXTERNAL_DEPENDENT")]
    [InlineData("SUSPECT_TEST_NAME")]
    public void AcceptedTagsAreNotAutomaticExclusions(string tag)
    {
        var result = ClassPolicy.Evaluate(new JavaClass { SourceSet = "production", Tags = new([tag], StringComparer.Ordinal), Modifiers = new(["final"], StringComparer.Ordinal) });
        Assert.Null(result.Policy); Assert.Empty(result.Reasons); Assert.Contains(tag, result.Tags);
    }
    [Fact]
    public void EligibleDiscoveryIsTechnicalButNeverStrict()
    {
        Assert.Equal((true, false), StatusPolicy.DeriveEligibility("ELIGIBLE", "DISCOVERY_ONLY", "CONTENT_MATCHED"));
        Assert.Equal((true, true), StatusPolicy.DeriveEligibility("ELIGIBLE", "STRICT", "UPSTREAM_PINNED"));
        Assert.Equal((false, false), StatusPolicy.DeriveEligibility("PRECHECKED", "STRICT", "UPSTREAM_PINNED"));
    }
}
