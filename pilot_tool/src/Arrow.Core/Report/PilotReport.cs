using System.Globalization;
using System.Text;

namespace Arrow.Core.Report;

/// <summary>Một lần đo thời gian của một bước (dòng nguồn của <c>part1_cost.csv</c>).</summary>
public sealed record StepSample(string Step, string Dataset, double Seconds, long PromptTokens = 0, long CompletionTokens = 0, double CostUsd = 0);

/// <summary>Một dòng của <c>part1_cost.csv</c> (specs/07 Mục 1.1).</summary>
public sealed record CostRow(string Step, string Dataset, int Count, double MedianSeconds, double P90Seconds, double MaxSeconds, double TotalSeconds,
    long PromptTokens, long CompletionTokens, double CostUsd);

/// <summary>Một dòng của <c>part1_infra_errors.csv</c> (specs/07 Mục 1.2).</summary>
public sealed record InfraRow(string TaskId, string Dataset, string Stage, string Reason, int Attempts, string LastMessage);

/// <summary>Một chuỗi sửa của phần 2 (ứng viên × cơ chế).</summary>
public sealed record RepairChain(string TaskId, string Strategy, string Mechanism, bool Finished, int? SuccessRound, bool InfraUnresolved);

/// <summary>Kết quả phần 2 (specs/07 Mục 2).</summary>
public sealed record NSelection
{
    public int NMax { get; init; }
    public int Chains { get; init; }
    public int ExcludedInfra { get; init; }
    public int Unfinished { get; init; }
    public int[] Cumulative { get; init; } = [];
    public bool Computable { get; init; }
    public string Rule { get; init; } = "";
    public int? N { get; init; }
    public Dictionary<string, int> Sensitivity { get; init; } = [];
}

/// <summary>Tổng hợp số liệu cho báo cáo pilot (specs/07). Code thuần, không đọc file.</summary>
public static class PilotReport
{
    public static readonly string[] CostSteps =
        ["clone", "baseline", "llm_generate", "build_verify", "llm_repair", "build_repair", "evosuite", "jacoco", "pit", "tsdetect"];

    public static double Percentile(IReadOnlyList<double> values, double p)
    {
        if (values.Count == 0)
        {
            return 0;
        }

        var sorted = values.Order().ToList();
        var rank = p * (sorted.Count - 1);
        var low = (int)Math.Floor(rank);
        var high = (int)Math.Ceiling(rank);
        return Math.Round(sorted[low] + (sorted[high] - sorted[low]) * (rank - low), 2);
    }

    public static List<CostRow> Costs(IEnumerable<StepSample> samples) =>
        samples.GroupBy(s => (s.Step, s.Dataset))
            .OrderBy(g => Array.IndexOf(CostSteps, g.Key.Step)).ThenBy(g => g.Key.Dataset, StringComparer.Ordinal)
            .Select(g =>
            {
                var seconds = g.Select(s => s.Seconds).ToList();
                return new CostRow(g.Key.Step, g.Key.Dataset, seconds.Count, Percentile(seconds, 0.5), Percentile(seconds, 0.9), Math.Round(seconds.Max(), 2),
                    Math.Round(seconds.Sum(), 2), g.Sum(s => s.PromptTokens), g.Sum(s => s.CompletionTokens), Math.Round(g.Sum(s => s.CostUsd), 4));
            })
            .ToList();

    /// <summary>Phần 2: S(k) chung hai cơ chế, N theo quy tắc, phân tích độ nhạy. Chỉ tính khi mọi chuỗi còn lại đã xong.</summary>
    public static NSelection SelectN(IReadOnlyList<RepairChain> chains, int nMax, string rule)
    {
        var usable = chains.Where(c => !c.InfraUnresolved).ToList();
        var unfinished = usable.Count(c => !c.Finished);
        var cumulative = NRules.Cumulative(usable.Where(c => c.Finished).Select(c => c.SuccessRound), nMax);
        var computable = unfinished == 0 && usable.Count > 0;
        return new NSelection
        {
            NMax = nMax,
            Chains = usable.Count,
            ExcludedInfra = chains.Count - usable.Count,
            Unfinished = unfinished,
            Cumulative = cumulative,
            Computable = computable,
            Rule = rule,
            N = computable ? NRules.Apply(rule, cumulative) : null,
            Sensitivity = computable
                ? new Dictionary<string, int>
                {
                    ["report2"] = NRules.Report2(cumulative),
                    ["share90"] = NRules.Share(cumulative, 0.90),
                    ["share95"] = NRules.Share(cumulative, 0.95),
                    ["share99"] = NRules.Share(cumulative, 0.99),
                }
                : [],
        };
    }

    public static string Csv<T>(IEnumerable<T> rows, params (string Header, Func<T, object?> Value)[] columns)
    {
        var builder = new StringBuilder(string.Join(',', columns.Select(c => c.Header))).Append('\n');
        foreach (var row in rows)
        {
            builder.Append(string.Join(',', columns.Select(c => Cell(c.Value(row))))).Append('\n');
        }

        return builder.ToString();
    }

    private static string Cell(object? value)
    {
        var text = value switch
        {
            null => "",
            double d => d.ToString("0.####", CultureInfo.InvariantCulture),
            IFormattable f => f.ToString(null, CultureInfo.InvariantCulture),
            _ => value.ToString() ?? "",
        };
        return text.Contains(',') || text.Contains('"') || text.Contains('\n') ? "\"" + text.Replace("\"", "\"\"").Replace('\n', ' ') + "\"" : text;
    }

    public static string CumulativeCsv(NSelection selection)
    {
        var last = selection.Cumulative.Length == 0 ? 0 : selection.Cumulative[^1];
        var builder = new StringBuilder("round,successes_cumulative,share_of_round10\n");
        for (var k = 1; k <= selection.Cumulative.Length; k++)
        {
            var share = last == 0 ? "" : ((double)selection.Cumulative[k - 1] / last).ToString("0.####", CultureInfo.InvariantCulture);
            builder.Append(k).Append(',').Append(selection.Cumulative[k - 1]).Append(',').Append(share).Append('\n');
        }

        return builder.ToString();
    }
}
