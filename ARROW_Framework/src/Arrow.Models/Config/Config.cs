namespace Arrow.Models.Config;

/// <summary>Các thư mục của framework. <see cref="FrameworkRoot"/> là <c>ARROW_Framework</c>, <see cref="ArrowRoot"/> là thư mục cha <c>ARROW</c>.</summary>
public sealed record FrameworkPaths(string FrameworkRoot)
{
    public string ArrowRoot => Path.GetFullPath(Path.Combine(FrameworkRoot, ".."));
    public string ExperimentsDir => Path.Combine(FrameworkRoot, "experiments");
    public string PromptsDir => Path.Combine(FrameworkRoot, "prompts");
    public string RunsDir => Path.Combine(FrameworkRoot, "runs");
    public string LogsDir => Path.Combine(FrameworkRoot, "logs");
    public string MachineConfigPath => Path.Combine(FrameworkRoot, "machine.local.json");
    public string LiteLlmConfigPath => Path.Combine(FrameworkRoot, "litellm.config.local.yaml");

    /// <summary>Đường dẫn tương đối trong cấu hình được tính từ <see cref="FrameworkRoot"/>.</summary>
    public string Resolve(string path) => Path.IsPathRooted(path) ? Path.GetFullPath(path) : Path.GetFullPath(Path.Combine(FrameworkRoot, path));
}

/// <summary><c>experiments/&lt;tên&gt;.json</c> (spec 00 Mục 3.1): dùng chung, commit.</summary>
public sealed record ExperimentConfig
{
    public string ExperimentId { get; init; } = "";
    public string Kind { get; init; } = "pilot";
    public List<ManifestRef> Manifests { get; init; } = [];
    public List<ModelConfig> Models { get; init; } = [];
    public List<string> Strategies { get; init; } = [];
    public RepairConfig Repair { get; init; } = new();
    public VerificationConfig Verification { get; init; } = new();
    public ScreeningConfig Screening { get; init; } = new();
    public AssessmentConfig Assessment { get; init; } = new();
    public PromptsConfig Prompts { get; init; } = new();
    public string GeneratedTestSuffix { get; init; } = "_ArrowTest";

    /// <summary>File tập con RQ2 (tính từ thư mục của file cấu hình), tạo bằng <c>arrow rq2-subset</c>; cần khi có manifest đặt <c>repair_subset</c>.</summary>
    public string? RepairSubsetFile { get; init; }
}

public sealed record ManifestRef
{
    public string Dataset { get; init; } = "";
    public string Path { get; init; } = "";
    public string Sha256 { get; init; } = "";
    public bool Repair { get; init; }

    /// <summary>
    /// RQ2 chỉ sửa một tập con cố định gồm chừng này ứng viên thất bại của dataset, rút đều theo ô model × quy trình (Report 2 Experiment 2;
    /// spec 03 Mục 6.1). Null = sửa mọi ứng viên thất bại. Tập con được chọn sau RQ1 bằng <c>arrow rq2-subset</c>.
    /// </summary>
    public int? RepairSubset { get; init; }
}

public sealed record ModelConfig
{
    public string Name { get; init; } = "";
    public string ExpectedModelIdPrefix { get; init; } = "";
    public Dictionary<string, object?> Params { get; init; } = [];
    public PriceConfig? PriceUsdPerMillion { get; init; }
}

public sealed record PriceConfig
{
    public double Input { get; init; }
    public double Output { get; init; }
}

public sealed record RepairConfig
{
    public List<string> Mechanisms { get; init; } = ["fixed", "adaptive"];
    public int N { get; init; } = 10;
    public int? EarlyStopP { get; init; }

    /// <summary>
    /// Quy trình mà mọi ứng viên thất bại đều được sửa bằng Adaptive (kể cả ngoài tập con RQ2), chỉ để RQ3 có bộ test ARROW đại diện
    /// (quyết định 15/10/2026, phương án E). Các chain này có <c>rq3_only = true</c> và không tính vào RQ2.
    /// </summary>
    public string? Rq3Strategy { get; init; }
}

public sealed record VerificationConfig
{
    public int BaselineRuns { get; init; } = 2;
    public int StepTimeoutSeconds { get; init; } = 900;
    public int InfraAttempts { get; init; } = 3;
}

public sealed record ScreeningConfig
{
    public bool RequirePairedTestPass { get; init; } = true;
}

public sealed record AssessmentConfig
{
    public List<string> Tools { get; init; } = ["jacoco", "pit", "tsdetect"];
    public List<string> ComparisonSystems { get; init; } = ["evosuite"];
}

public sealed record PromptsConfig
{
    public string Dir { get; init; } = "prompts";
}

/// <summary>Cấu hình thực nghiệm đã đọc, kèm đường dẫn tuyệt đối và sha256 của file.</summary>
public sealed record LoadedExperiment(ExperimentConfig Config, string Path, string Sha256)
{
    public string ResolveManifestPath(ManifestRef manifest) => ResolvePath(manifest.Path);

    /// <summary>Đường dẫn trong cấu hình tính từ thư mục của file cấu hình.</summary>
    public string ResolvePath(string relative) =>
        System.IO.Path.GetFullPath(System.IO.Path.Combine(System.IO.Path.GetDirectoryName(Path)!, relative));
}

/// <summary><c>machine.local.json</c> (spec 00 Mục 3.2): riêng từng máy, không commit.</summary>
public sealed record MachineConfig
{
    public string MachineName { get; init; } = "";
    public Dictionary<string, string> JdkHomes { get; init; } = [];
    public string MavenPath { get; init; } = "";
    public string GradlePath { get; init; } = "";
    public string GitPath { get; init; } = "";
    public string WorkDir { get; init; } = "../repo";
    public string ToolsDir { get; init; } = "../tools";
    public LlmMachineConfig Llm { get; init; } = new();
    public WorkersConfig Workers { get; init; } = new();
    public DoctorStamp? LastDoctor { get; init; }
}

public sealed record LlmMachineConfig
{
    public string ProxyUrl { get; init; } = "http://127.0.0.1:4000";
    public string ProxyMode { get; init; } = "auto_start";
    public string ProxyConfigFile { get; init; } = "litellm.config.local.yaml";
    public int RpmLimit { get; init; } = 60;
}

public sealed record WorkersConfig
{
    public int Build { get; init; } = 4;
    public int Llm { get; init; } = 2;
}

public sealed record DoctorStamp
{
    public DateTimeOffset At { get; init; }
    public string Status { get; init; } = "";
}

/// <summary>Một model trong <c>model_list</c> của LiteLLM: tên model của thực nghiệm → model thật ở provider.</summary>
public sealed record LiteLlmRoute
{
    public string ModelName { get; init; } = "";
    public string ProviderModel { get; init; } = "";
    public string ApiBase { get; init; } = "";
    public string ApiKey { get; init; } = "";

    public bool HasApiKey => ApiKey.Length > 0;

    /// <summary>Ollama tự host không cần khóa.</summary>
    public bool NeedsApiKey => !ProviderModel.StartsWith("ollama", StringComparison.Ordinal);

    public bool IsComplete => ProviderModel.Length > 0 && ApiBase.Length > 0 && (HasApiKey || !NeedsApiKey);

    /// <summary>Tên máy chủ của endpoint: thứ duy nhất của endpoint được ghi vào log và kết quả.</summary>
    public string EndpointHost => Uri.TryCreate(ApiBase, UriKind.Absolute, out var uri) ? uri.Host : "";
}

/// <summary><c>litellm.config.local.yaml</c> (spec 02 Mục 4.3): riêng từng máy, không commit.</summary>
public sealed record LiteLlmConfig
{
    public List<LiteLlmRoute> Routes { get; init; } = [];
    public string MasterKey { get; init; } = "";

    public LiteLlmRoute? Route(string modelName) => Routes.FirstOrDefault(r => r.ModelName == modelName);
}

/// <summary><c>runs/&lt;run&gt;/run.lock.json</c>: cấu hình đã khóa khi tạo run; chạy tiếp phải khớp các trường khóa.</summary>
public sealed record RunLock
{
    public string RunId { get; init; } = "";
    public DateTimeOffset CreatedAt { get; init; }
    public string ExperimentId { get; init; } = "";
    public string ExperimentSha256 { get; init; } = "";
    public Dictionary<string, string> ManifestSha256 { get; init; } = [];
    public Dictionary<string, string> PromptSha256 { get; init; } = [];
    public string ToolVersion { get; init; } = "";
    public string? ToolGitCommit { get; init; }
    public string MachineName { get; init; } = "";
    public string Os { get; init; } = "";
    public Dictionary<string, string> JdkVersions { get; init; } = [];
    public string? MavenVersion { get; init; }
    public string? GradleVersion { get; init; }
    public List<string> BuildFlags { get; init; } = [];
    public string? LlmEndpointHosts { get; init; }
}
