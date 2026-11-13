namespace Preflight.Models;

/// <summary>Hợp đồng config tại plan mục 4.6; không tự khởi chạy dataset.</summary>
public sealed record PreflightConfig
{
    public RunConfig Run { get; init; } = new();
    public JavaConfig Java { get; init; } = new();
    public Dictionary<string, DatasetConfig> Datasets { get; init; } = new(StringComparer.Ordinal);
    public ToolsConfig Tools { get; init; } = new();
    public DiskConfig Disk { get; init; } = new();
}
public sealed record RunConfig
{
    public int Workers { get; init; } = 5;
    public int MaxRevisionCandidates { get; init; } = 500;
    public int MaxEvidenceChecks { get; init; } = 50;
    public int BuildTimeoutSeconds { get; init; } = 900;
    public int GitTimeoutSeconds { get; init; } = 1800;
    public bool KeepWorkspaces { get; init; }
    public bool KeepRepoCache { get; init; }
    public ProbeProfile ProbeProfile { get; init; } = ProbeProfile.v2;
    public bool FastMode { get; init; }
    public bool Offline { get; init; }
}
public sealed record JavaConfig
{
    public string DefaultHome { get; init; } = "";
    public Dictionary<string, string> Homes { get; init; } = new(StringComparer.Ordinal);
    public string TreeSitterLibrary { get; init; } = "tools/tree-sitter-native/bin/tree_sitter_java_preflight.dll";
}
public sealed record DatasetConfig
{
    public string Root { get; init; } = "";
    public string ManualLabels { get; init; } = "";
    public string Archive { get; init; } = "";
    public string Cohort { get; init; } = "all_filepairs";
    public bool VerifyUpstream { get; init; }
    public string RevisionPolicy { get; init; } = "snapshot_unverified";
}
public sealed record ToolsConfig
{
    public string MavenHome { get; init; } = "";
    public string GradleHome { get; init; } = "";
    public string MavenRepoLocal { get; init; } = ".cache/m2";
    public string GradleUserHome { get; init; } = ".cache/gradle";
}
public sealed record DiskConfig
{
    public double MinFreeGb { get; init; } = 20;
    public double ResumeFreeGb { get; init; } = 30;
    public int CheckIntervalSeconds { get; init; } = 30;
    public double BuildCacheMaxGb { get; init; } = 40;
    public bool SweepOrphansOnStart { get; init; } = true;
}
