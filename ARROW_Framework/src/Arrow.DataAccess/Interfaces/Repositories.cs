using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Manifest;
using Arrow.Models.Run;

namespace Arrow.DataAccess.Interfaces;

/// <summary>Cấu hình: <c>experiments/*.json</c>, <c>machine.local.json</c>, <c>litellm.config.local.yaml</c> (spec 07 Mục 5.1).</summary>
public interface IConfigRepository
{
    IReadOnlyList<string> ListExperiments();

    /// <summary>Đọc cấu hình thực nghiệm, kiểm tra sha256 từng manifest; lệch thì ném <see cref="ErrorCodes.ManifestMismatch"/>.</summary>
    LoadedExperiment LoadExperiment(string path);

    void SetModelPrice(string experimentPath, string modelName, PriceConfig? price);
    void SetExpectedModelIdPrefix(string experimentPath, string modelName, string prefix);
    void AddModel(string experimentPath, ModelConfig model);
    void RemoveModel(string experimentPath, string modelName);

    MachineConfig? LoadMachine();
    void SaveMachine(MachineConfig machine);

    /// <summary>Đọc cấu hình LiteLLM và đăng ký khóa, <c>master_key</c>, <c>api_base</c> vào <c>Redactor</c>.</summary>
    LiteLlmConfig? LoadLiteLlm();
    void SaveLiteLlm(LiteLlmConfig config);
}

/// <summary>Manifest <c>.jsonl</c> (spec 01 Mục 1.1).</summary>
public interface IManifestRepository
{
    IReadOnlyList<TaskRecord> ReadTasks(string manifestPath, string dataset);

    /// <summary>Mọi task của mọi manifest trong cấu hình thực nghiệm, theo thứ tự manifest rồi thứ tự dòng.</summary>
    IReadOnlyList<TaskRecord> ReadTasks(LoadedExperiment experiment);
}

/// <summary>Template và dữ liệu trong <c>prompts/</c> (spec 02 Mục 2.3).</summary>
public interface IPromptRepository
{
    PromptTemplate Load(string relativePath);

    /// <summary>Mọi template <c>.md</c> (trừ README) trong <c>prompts/</c>.</summary>
    IReadOnlyList<PromptTemplate> LoadAll();

    /// <summary>Đọc file dữ liệu khác (ví dụ <c>generation/fewshot_provenance.json</c>).</summary>
    string ReadData(string relativePath);
}

/// <summary>Các file cấp run: khóa cấu hình, ledger, sự kiện, trạng thái, lệnh điều khiển.</summary>
public interface IRunRepository
{
    string RunRoot(string runId);
    bool Exists(string runId);
    void EnsureCreated(string runId);
    IReadOnlyList<RunInfo> ListRuns();

    RunLock? ReadLock(string runId);
    void WriteLock(string runId, RunLock runLock);

    /// <summary>Thêm một dòng ledger. Phải là việc ghi cuối cùng của một job.</summary>
    void AppendLedger(string runId, LedgerEntry entry);
    IReadOnlyList<LedgerEntry> ReadLedger(string runId);

    void AppendEvent(string runId, RunEvent runEvent);
    IReadOnlyList<RunEvent> ReadEvents(string runId, int last);

    void WriteStatus(string runId, RunStatus status);
    RunStatus? ReadStatus(string runId);

    void WriteControl(string runId, ControlCommand command);
    ControlCommand? ReadControl(string runId);
    void ClearControl(string runId);
}

/// <summary>Kết quả từng khối của từng task trong <c>runs/&lt;run&gt;/tasks/&lt;dataset&gt;/&lt;task&gt;/</c>. Đường dẫn con lấy từ <see cref="ResultPaths"/>.</summary>
public interface ITaskResultRepository
{
    string TaskDir(string runId, TaskKey task);
    bool Exists(string runId, TaskKey task, string relativePath);
    void SaveJson<T>(string runId, TaskKey task, string relativePath, T value);
    T? LoadJson<T>(string runId, TaskKey task, string relativePath) where T : class;
    void SaveText(string runId, TaskKey task, string relativePath, string text);
    string? LoadText(string runId, TaskKey task, string relativePath);

    /// <summary>Xóa file hoặc thư mục con (dùng khi bỏ đầu ra dở dang của job chưa có dòng ledger).</summary>
    void Delete(string runId, TaskKey task, string relativePath);

    /// <summary>Các task đã có thư mục kết quả trong run.</summary>
    IReadOnlyList<TaskKey> ListTasks(string runId);
}

/// <summary>Cache lượt gọi model để phát lại khi chạy tiếp (spec 02 Mục 4.4).</summary>
public interface ILlmCacheRepository
{
    LlmCacheEntry? TryGet(string runId, string key);
    void Put(string runId, LlmCacheEntry entry);
}

/// <summary>Báo cáo trong <c>runs/&lt;run&gt;/reports/</c>.</summary>
public interface IReportRepository
{
    string ReportsDir(string runId);
    void WriteCsv(string runId, string fileName, CsvTable table);
    void WriteText(string runId, string fileName, string text);
}

public enum LogLevel
{
    Debug,
    Info,
    Warn,
    Error,
}

/// <summary>Log của tool: <c>logs/arrow-&lt;yyyyMMdd&gt;.log</c>; che bí mật trước khi ghi.</summary>
public interface ILogRepository
{
    void Write(LogLevel level, string message, Exception? exception = null);
}

public static class LogRepositoryExtensions
{
    public static void Debug(this ILogRepository log, string message) => log.Write(LogLevel.Debug, message);
    public static void Info(this ILogRepository log, string message) => log.Write(LogLevel.Info, message);
    public static void Warn(this ILogRepository log, string message) => log.Write(LogLevel.Warn, message);
    public static void Error(this ILogRepository log, string message, Exception? exception = null) => log.Write(LogLevel.Error, message, exception);
}
