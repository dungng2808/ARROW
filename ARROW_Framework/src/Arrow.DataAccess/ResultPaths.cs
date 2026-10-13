namespace Arrow.DataAccess;

/// <summary>Đường dẫn con trong thư mục kết quả của một task (spec 00 Mục 4). Luôn dùng <c>/</c>.</summary>
public static class ResultPaths
{
    // Khối 1
    public const string Block1Dir = "block1";
    public const string Task = "block1/task.json";
    public const string Metadata = "block1/metadata.json";
    public const string MetadataPrompt = "block1/metadata_prompt.txt";
    public const string Baseline = "block1/baseline.json";
    public const string Workspace = "block1/workspace.json";
    public const string Block1Outcome = "block1/outcome.json";
    public const string DependenciesResolved = "block1/dependencies_resolved.txt";
    public const string TestClasspath = "block1/test_classpath.txt";

    // Khối 2: candidate ban đầu
    public static string GenerateDir(string model, string strategy) => $"block2/{model}/{strategy}";

    // Khối 3: xác minh c0, chain sửa
    public static string VerifyC0(string model, string strategy) => $"block3/{model}/{strategy}/verify_c0.json";
    public static string ChainDir(string model, string strategy, string mechanism) => $"block3/{model}/{strategy}/{mechanism}";
    public static string RoundDir(string model, string strategy, string mechanism, int round) => $"{ChainDir(model, strategy, mechanism)}/round_{round}";
    public static string Final(string model, string strategy, string mechanism) => $"{ChainDir(model, strategy, mechanism)}/final.json";
    public static string FinalCode(string model, string strategy, string mechanism) => $"{ChainDir(model, strategy, mechanism)}/final.java";
    public const string EvoSuiteDir = "block3/evosuite";

    // Khối 4
    public static string SuiteDir(string suiteId) => $"block4/{suiteId}";

    // Tên file trong thư mục của một lượt gọi model / một vòng
    public const string Prompt = "prompt.json";
    public const string Response = "response.json";
    public const string Usage = "usage.json";
    public const string CandidateCode = "candidate.java";
    public const string Candidate = "candidate.json";
    public const string Verify = "verify.json";
    public const string Decision = "decision.json";
    public const string Request = "request.json";

    /// <summary>Ghép đường dẫn con bằng <c>/</c>.</summary>
    public static string Join(params string[] parts) => string.Join('/', parts.Select(p => p.Trim('/')));
}
