using Arrow.Models.Common;
using Arrow.Models.Run;

namespace Arrow.Service.Pipeline;

/// <summary>Định danh job trong một task (dòng ledger, spec 00 Mục 5.2).</summary>
public static class JobIds
{
    public const string Analyze = "analyze";
    public const string EvoSuite = "evosuite";

    public static string Generate(string model, string strategy) => $"generate/{model}/{strategy}";
    public static string VerifyC0(string model, string strategy) => $"verify/{model}/{strategy}";
    public static string Chain(string model, string strategy, string mechanism) => $"chain/{model}/{strategy}/{mechanism}";
    public static string Measure(string suiteId) => $"measure/{suiteId}";
}

/// <summary>
/// Chạy tiếp (spec 00 Mục 5.3, spec 07 Mục 8): job có dòng ledger thì bỏ qua; job chưa có thì làm lại từ đầu job; job mà dòng ledger
/// mới nhất là INFRA chỉ chạy lại khi có <c>--rerun-infra</c>.
/// </summary>
public sealed class ResumePlanner
{
    private readonly Dictionary<(string Dataset, string TaskId, string Job), LedgerEntry> _latest = [];
    private readonly bool _rerunInfra;
    private readonly object _lock = new();

    public ResumePlanner(IEnumerable<LedgerEntry> ledger, bool rerunInfra)
    {
        _rerunInfra = rerunInfra;
        foreach (var entry in ledger)
        {
            Record(entry);
        }
    }

    public void Record(LedgerEntry entry)
    {
        lock (_lock)
        {
            _latest[(entry.Dataset, entry.TaskId, entry.Job)] = entry;
        }
    }

    public LedgerEntry? Latest(TaskKey task, string job)
    {
        lock (_lock)
        {
            return _latest.GetValueOrDefault((task.Dataset, task.TaskId, job));
        }
    }

    /// <summary>Job cần chạy: chưa có dòng ledger, hoặc dòng mới nhất là INFRA và có <c>--rerun-infra</c>.</summary>
    public bool ShouldRun(TaskKey task, string job) => Latest(task, job) is not { } entry || entry.Outcome == OutcomeKind.Infra && _rerunInfra;
}
