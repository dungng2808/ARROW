using Preflight.Models;

namespace Preflight.Service.Core.Policy;

/// <summary>Port của policy.select_primary_status và derive_eligibility.</summary>
public static class StatusPolicy
{
    private static readonly PreflightStatus[] Priority =
    [PreflightStatus.CLONE_FAILED, PreflightStatus.COMMIT_MISSING, PreflightStatus.CHECKOUT_FAILED,
     PreflightStatus.BUILD_TOOL_UNSUPPORTED, PreflightStatus.JDK_UNSUPPORTED, PreflightStatus.BUILD_TIMEOUT,
     PreflightStatus.DEPENDENCY_UNAVAILABLE, PreflightStatus.MAIN_BUILD_FAILED, PreflightStatus.TEST_COMPILE_FAILED,
     PreflightStatus.SOURCE_INVALID, PreflightStatus.EXCLUDED, PreflightStatus.PROBE_FAILED,
     PreflightStatus.INTEGRATION_ONLY, PreflightStatus.NEEDS_REVIEW, PreflightStatus.PRECHECKED, PreflightStatus.ELIGIBLE];
    public static PreflightStatus SelectPrimaryStatus(IEnumerable<string> observed)
    {
        var values = observed.ToHashSet(StringComparer.Ordinal);
        foreach (var status in Priority) if (values.Contains(status.ToString())) return status;
        throw new ArgumentException("No recognised preflight status", nameof(observed));
    }
    public static (bool Technical, bool Strict) DeriveEligibility(string status, string mode, string revision)
    {
        bool technical = status == "ELIGIBLE";
        return (technical, mode == "STRICT" && revision == "UPSTREAM_PINNED" && technical);
    }
}

/// <summary>Port của runner._class_policy. Chưa tính AST hay sửa quirk.</summary>
public static class ClassPolicy
{
    public static (string? Policy, string[] Tags, string[] Reasons) Evaluate(JavaClass java)
    {
        var tags = java.Tags.Order(StringComparer.Ordinal).ToArray();
        string? reason = java.SourceSet == "test" ? "EXCLUDE_TEST_SOURCE"
            : java.SourceSet != "production" ? "EXCLUDE_NON_PRODUCTION_SOURCE"
            : !java.IsTopLevel || java.Kind is "interface" or "annotation" ? "EXCLUDE_UNSUPPORTED_DECLARATION"
            : java.Kind is "enum" or "record" ? "EXCLUDE_STRICT_COHORT"
            : java.Modifiers.Contains("abstract") ? "EXCLUDE_ABSTRACT_CLASS"
            : java.Tags.Contains("GENERATED_SOURCE") ? "EXCLUDE_GENERATED_SOURCE" : null;
        return (reason, tags, reason is null ? [] : [reason]);
    }
}
