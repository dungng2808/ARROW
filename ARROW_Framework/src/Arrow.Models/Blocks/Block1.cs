using System.Text.Json.Serialization;
using Arrow.Models.Common;

namespace Arrow.Models.Block1;

/// <summary><c>block1/metadata.json</c>: Repository Metadata M, đủ 6 thông tin của Đóng góp 1 (spec 01 Mục 2.1).</summary>
public sealed record RepositoryMetadata
{
    public string TaskId { get; init; } = "";
    public string Dataset { get; init; } = "";
    public RevisionInfo Revision { get; init; } = new();
    public BuildToolInfo BuildTool { get; init; } = new();
    public JavaInfo Java { get; init; } = new();
    public DependencyInfo Dependencies { get; init; } = new();
    public TestFrameworkInfo TestFramework { get; init; } = new();
    public ProjectStructureInfo ProjectStructure { get; init; } = new();
    public PathsInfo Paths { get; init; } = new();
    public CutInfo Cut { get; init; } = new();

    /// <summary>Nhóm thông tin không lấy được và lý do (trường vẫn có, giá trị rỗng).</summary>
    public Dictionary<string, string> MissingReasons { get; init; } = [];
}

public sealed record RevisionInfo
{
    public string RepoUrl { get; init; } = "";
    public string Commit { get; init; } = "";
    public string Provenance { get; init; } = "";
    public string VerificationStatus { get; init; } = "";
}

[JsonConverter(typeof(LowerSnakeEnumConverter<BuildMode>))]
public enum BuildMode
{
    Reactor,
    Standalone,
}

public sealed record BuildToolInfo
{
    /// <summary><c>maven</c> hoặc <c>gradle</c>.</summary>
    public string Kind { get; init; } = "";
    public string Version { get; init; } = "";
    public bool Wrapper { get; init; }
    public string BuildRoot { get; init; } = ".";
    public BuildMode BuildMode { get; init; }
    public string BuildFile { get; init; } = "";
    public string? ReactorModule { get; init; }
}

public sealed record JavaInfo
{
    public string? Declared { get; init; }
    public string? DeclaredSource { get; init; }
    public int? Major { get; init; }
    public int JdkMajor { get; init; }
    public string JdkChoiceReason { get; init; } = "";
}

public sealed record DependencyRef
{
    public string Group { get; init; } = "";
    public string Artifact { get; init; } = "";
    public string Version { get; init; } = "";
    public string Scope { get; init; } = "";
}

public sealed record DependencyInfo
{
    public List<DependencyRef> Direct { get; init; } = [];
    public List<DependencyRef> Test { get; init; } = [];
    public int ResolvedCount { get; init; }
}

public sealed record TestFrameworkInfo
{
    /// <summary><c>junit4</c>, <c>junit5</c> hoặc <c>testng</c>.</summary>
    public string Name { get; init; } = "";
    public string Version { get; init; } = "";
    public bool Vintage { get; init; }
    public List<string> Mocking { get; init; } = [];
    public List<string> AssertionLibs { get; init; } = [];
    public string Evidence { get; init; } = "";
}

public sealed record ProjectStructureInfo
{
    public bool MultiModule { get; init; }
    public List<string> Modules { get; init; } = [];
    public string TargetModule { get; init; } = ".";
    public string ModuleRoot { get; init; } = ".";
    public List<string> Packages { get; init; } = [];
}

/// <summary>Mọi đường dẫn tính từ gốc repository, dùng <c>/</c>.</summary>
public sealed record PathsInfo
{
    public string SourceRoot { get; init; } = "";
    public string TestRoot { get; init; } = "";
    public string CutPath { get; init; } = "";
    public string GeneratedTestPath { get; init; } = "";
    public string GeneratedTestFqn { get; init; } = "";
}

public sealed record CutInfo
{
    public string Fqn { get; init; } = "";
    public string SimpleName { get; init; } = "";
    public string Package { get; init; } = "";
    public string SourceSha256 { get; init; } = "";
}

[JsonConverter(typeof(UpperSnakeEnumConverter<PairedTestStatus>))]
public enum PairedTestStatus
{
    Pass,
    NoTestRun,
    Failing,
    Flaky,
    NoPairedTest,
}

/// <summary><c>block1/baseline.json</c> (spec 01 Mục 2.2, 6).</summary>
public sealed record Baseline
{
    public int Runs { get; init; }
    public Dictionary<string, List<string>> Commands { get; init; } = [];
    public string WorkingDir { get; init; } = ".";
    public List<string> PassingInAllRuns { get; init; } = [];
    public List<string> Flaky { get; init; } = [];
    public List<string> FailingInAllRuns { get; init; } = [];
    public PairedTestResult PairedTest { get; init; } = new();
    public BaselineDurations DurationsSeconds { get; init; } = new();
}

public sealed record PairedTestResult
{
    public string? Class { get; init; }
    public List<int> TestsRun { get; init; } = [];
    public PairedTestStatus Status { get; init; }
}

public sealed record BaselineDurations
{
    public double Compile { get; init; }
    public List<double> ModuleTest { get; init; } = [];
}

/// <summary><c>block1/workspace.json</c> (spec 01 Mục 2.3).</summary>
public sealed record WorkspaceInfo
{
    public string Mirror { get; init; } = "";
    public string BaseCheckout { get; init; } = "";
    public string CompiledSnapshot { get; init; } = "";
    public int JdkMajor { get; init; }
    public string JdkHome { get; init; } = "";
    public string BuildRoot { get; init; } = ".";
    public BuildMode BuildMode { get; init; }
    public string BuildRootSource { get; init; } = "";
    public string? MavenModuleSelector { get; init; }
    public List<string> CommonFlags { get; init; } = [];

    /// <summary><c>maven</c> hoặc <c>gradle</c>.</summary>
    public string Tool { get; init; } = "maven";

    /// <summary>Maven: thư mục trong <c>-pl</c> (reactor); Gradle: đường dẫn project (<c>:core</c>).</summary>
    public string? Module { get; init; }

    /// <summary>Chương trình build: wrapper tính từ gốc repository (<see cref="ExecutableInRepo"/>) hoặc đường dẫn tuyệt đối của công cụ trên máy.</summary>
    public string Executable { get; init; } = "";
    public bool ExecutableInRepo { get; init; }

    /// <summary>Gradle: init script của framework (tương đối với <c>work_dir</c>).</summary>
    public string? InitScript { get; init; }
}

/// <summary>
/// Cách build một task (spec 01 Mục 4.1): build root (tính từ gốc repository), chế độ, bộ chọn module
/// (Maven: đường dẫn trong <c>-pl</c>; Gradle: đường dẫn project như <c>:core</c>), và nguồn của quyết định.
/// </summary>
public sealed record BuildPlan
{
    /// <summary><c>maven</c> hoặc <c>gradle</c>.</summary>
    public string Tool { get; init; } = "";
    public string BuildRoot { get; init; } = ".";
    public BuildMode Mode { get; init; }

    /// <summary>Maven: thư mục module trong <c>-pl</c> (chế độ reactor), null ở chế độ standalone. Gradle: đường dẫn project (<c>:</c> là project gốc).</summary>
    public string? Module { get; init; }

    /// <summary><c>manifest:&lt;stage&gt;</c>, <c>detected:reactor</c> hoặc <c>detected:standalone</c>.</summary>
    public string Source { get; init; } = "";
}

/// <summary>Kết quả khối 1 cho một task.</summary>
public sealed record RepositoryAnalysis(
    BlockOutcome Outcome,
    RepositoryMetadata? Metadata,
    Baseline? Baseline,
    WorkspaceInfo? Workspace);
