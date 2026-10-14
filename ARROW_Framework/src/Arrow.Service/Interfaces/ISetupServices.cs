using Arrow.Models.Config;

namespace Arrow.Service.Interfaces;

/// <summary>Một công cụ tìm được trên máy: tên, có dùng được không, phiên bản, đường dẫn, gợi ý khi thiếu.</summary>
public sealed record ToolInfo(string Name, bool Ok, string? Version, string? Path, string? Hint = null);

/// <summary>Trạng thái cài đặt máy (S2): cấu hình đã lưu (hoặc mặc định), công cụ dò được, cấu hình thực nghiệm có sẵn.</summary>
public sealed record SetupState(MachineConfig Machine, bool Saved, IReadOnlyList<ToolInfo> Tools, IReadOnlyList<string> Experiments, string FrameworkRoot);

/// <summary>Màn hình S2 (spec 06 Mục 4): dò JDK/Maven/Gradle/Git, kiểm tra thư mục repository, đọc/lưu <c>machine.local.json</c>.</summary>
public interface ISetupService
{
    /// <summary>Cấu hình máy đã lưu (null khi chưa cài đặt), không dò công cụ.</summary>
    MachineConfig? CurrentMachine();

    /// <summary>Dò công cụ theo <paramref name="editing"/> (cấu hình đang sửa, chưa lưu) hoặc theo <c>machine.local.json</c> khi null.</summary>
    Task<SetupState> LoadAsync(MachineConfig? editing, CancellationToken cancellationToken);

    /// <summary>Kiểm tra một JAVA_HOME có <c>java</c> và <c>javac</c> đúng major.</summary>
    Task<ToolInfo> CheckJdkAsync(int major, string home, CancellationToken cancellationToken);

    /// <summary>Lỗi của thư mục repository (dấu cách, dấu tiếng Việt, &lt; 20 GB trống) hoặc null nếu dùng được.</summary>
    string? ValidateWorkDir(string path);

    /// <summary>Dung lượng trống (GB) của ổ chứa thư mục.</summary>
    double? FreeSpaceGb(string path);

    void Save(MachineConfig machine);
}

/// <summary>Một model của cấu hình thực nghiệm kèm cách gọi trên máy này (S3).</summary>
public sealed record ModelView(ModelConfig Model, LiteLlmRoute? Route, string KeyHint);

public sealed record ModelsView(string ExperimentPath, string ExperimentSha256, IReadOnlyList<ModelView> Models);

/// <summary>Kết quả gọi thử một model (một lượt).</summary>
public sealed record ModelTestResult(bool Ok, string? Text, string? ModelId, bool ModelMatches, long TokensIn, long TokensOut, double Seconds, double CostUsd, string? Error);

/// <summary>Màn hình S3 (spec 06 Mục 5): model, provider, khóa API, giá; gọi thử.</summary>
public interface IModelConfigService
{
    IReadOnlyList<string> Experiments();
    ModelsView Get(string experimentPath);

    /// <summary>Lưu provider, endpoint, khóa của một model vào <c>litellm.config.local.yaml</c>; khóa rỗng = giữ khóa cũ.</summary>
    LiteLlmRoute SaveRoute(string modelName, string providerModel, string apiBase, string apiKey);

    void SetPrice(string experimentPath, string modelName, PriceConfig? price);
    void SetExpectedPrefix(string experimentPath, string modelName, string prefix);
    void AddModel(string experimentPath, ModelConfig model);
    void RemoveModel(string experimentPath, string modelName);
    Task<ModelTestResult> TestAsync(string experimentPath, string modelName, CancellationToken cancellationToken);
}

/// <summary>Một dòng của kiểm tra môi trường: trạng thái, mục, chi tiết, cách sửa, có tự sửa được không.</summary>
public sealed record DoctorLine(string Item, DoctorMark Mark, string Detail, string? Fix = null, bool AutoFixable = false);

public enum DoctorMark
{
    Ok,
    Warn,
    Fail,
}

public sealed record DoctorReport(IReadOnlyList<DoctorLine> Lines)
{
    public int Errors => Lines.Count(l => l.Mark == DoctorMark.Fail);
    public bool Ready => Errors == 0;
}

/// <summary>Màn hình S4 (spec 06 Mục 6): kiểm tra môi trường từng mục, báo từng dòng; sửa các lỗi tự sửa được (tải công cụ khối 4).</summary>
public interface IDoctorService
{
    Task<DoctorReport> RunAsync(string? experimentPath, Action<DoctorLine> onLine, CancellationToken cancellationToken);

    Task<IReadOnlyList<DoctorLine>> FixAsync(Action<DoctorLine> onLine, CancellationToken cancellationToken);
}
