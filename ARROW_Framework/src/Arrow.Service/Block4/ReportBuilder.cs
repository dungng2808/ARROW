using System.Globalization;
using System.Text;
using System.Text.RegularExpressions;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Run;

namespace Arrow.Service.Block4;

/// <summary>Thống kê mô tả dùng trong báo cáo.</summary>
public static class Stats
{
    public static double? Mean(IReadOnlyCollection<double> values) => values.Count == 0 ? null : values.Average();

    public static double? Median(IReadOnlyCollection<double> values) => Percentile(values, 0.5);

    /// <summary>Phân vị theo nội suy tuyến tính giữa hai phần tử (trung vị của số phần tử chẵn là trung bình hai phần tử giữa).</summary>
    public static double? Percentile(IReadOnlyCollection<double> values, double p)
    {
        if (values.Count == 0)
        {
            return null;
        }

        var sorted = values.OrderBy(v => v).ToArray();
        var position = (sorted.Length - 1) * p;
        var lower = (int)Math.Floor(position);
        var upper = (int)Math.Ceiling(position);
        return sorted[lower] + (sorted[upper] - sorted[lower]) * (position - lower);
    }

    /// <summary>Độ lệch chuẩn mẫu (chia n − 1); một giá trị thì null.</summary>
    public static double? StdDev(IReadOnlyCollection<double> values)
    {
        if (values.Count < 2)
        {
            return null;
        }

        var mean = values.Average();
        return Math.Sqrt(values.Sum(v => (v - mean) * (v - mean)) / (values.Count - 1));
    }

    public static string F(double? value, int digits = 4) =>
        value is null ? "" : Math.Round(value.Value, digits).ToString("0.####", CultureInfo.InvariantCulture);

    public static string I(int value) => value.ToString(CultureInfo.InvariantCulture);
}

/// <summary>Quy tắc chọn N của pilot (spec 04 Mục 6.3, CLAUDE.md chốt 09/10/2026).</summary>
public static class NRule
{
    /// <summary>
    /// <paramref name="cumulative"/>[k−1] = S(k), số chain đạt VALID ở vòng ≤ k (gộp hai cơ chế). N = k nhỏ nhất, 5 ≤ k ≤ 10, mà
    /// S(k−1) = S(k−2) và S(k) = S(k−1); không có k nào thì N = 10.
    /// </summary>
    public static int Choose(IReadOnlyList<int> cumulative)
    {
        int S(int k) => k <= 0 ? 0 : cumulative[Math.Min(k, cumulative.Count) - 1];
        for (var k = 5; k <= Math.Min(10, cumulative.Count); k++)
        {
            if (S(k - 1) == S(k - 2) && S(k) == S(k - 1))
            {
                return k;
            }
        }

        return 10;
    }
}

/// <summary>Độ nhạy P (spec 04 Mục 6.4): tính lại một chain Adaptive như thể dừng sau P vòng liên tiếp không có bản tốt hơn checkpoint.</summary>
public static class PSensitivity
{
    public sealed record Outcome(bool Success, int Rounds, double Seconds);

    public static Outcome Simulate(FinalTestSuite final, IReadOnlyList<RoundDecision> decisions, int p)
    {
        var withoutImprovement = 0;
        var seconds = 0.0;
        foreach (var decision in decisions.OrderBy(d => d.Round))
        {
            seconds += decision.LlmSeconds + decision.BuildSeconds;
            if (final.Success && decision.Round == final.SuccessRound)
            {
                return new Outcome(true, decision.Round, seconds);
            }

            withoutImprovement = decision.Improved ? 0 : withoutImprovement + 1;
            if (withoutImprovement >= p)
            {
                return new Outcome(false, decision.Round, seconds);
            }
        }

        return new Outcome(final.Success, final.Success ? final.SuccessRound : final.RoundsRun, seconds);
    }
}

/// <summary>
/// Flow 4.5 Unified Metrics &amp; Reporting (spec 04 Mục 6): từ kết quả đã đọc của một hoặc nhiều run dựng mọi CSV (RQ1, RQ2, chọn N,
/// độ nhạy P, RQ3, chi phí, cần chạy lại) và <c>report.md</c>. Code thuần.
/// </summary>
public static partial class ReportBuilder
{
    public const string Representative = "zero_shot_project_aware";

    [GeneratedRegex(@"\b(compile|run_target|run_module|baseline|test_compile|prepare|evosuite|jacoco|pit|tsdetect)\b")]
    private static partial Regex StepName();

    private static readonly string[] Metrics = ["line", "branch", "method", "mutation_score", "smell_density"];

    public static UnifiedReport Build(IReadOnlyList<RunData> runs, ExperimentConfig experiment)
    {
        CheckMergeable(runs);
        var csv = new Dictionary<string, CsvTable>(StringComparer.Ordinal);
        var tasks = runs.SelectMany(r => r.Tasks.Select(t => (Run: r, Task: t))).ToList();
        var usable = tasks.Where(t => t.Task.Block1?.Outcome == OutcomeKind.Ok).Select(t => t.Task).ToList();

        csv["rq1_verification.csv"] = Rq1(usable);
        csv["rq1_signatures.csv"] = Rq1Signatures(usable);

        var chains = Rq2Chains(usable, experiment);
        csv["rq2_chains.csv"] = Rq2ChainRows(chains);
        csv["rq2_summary.csv"] = Rq2Summary(chains);
        csv["rq2_mechanisms.csv"] = Rq2Mechanisms(chains);
        csv["rq2_by_c0_state.csv"] = Rq2ByC0State(chains);
        csv["rq2_cumulative.csv"] = Rq2Cumulative(chains, experiment.Repair.N);
        csv["rq2_p_sensitivity.csv"] = Rq2Sensitivity(chains);
        int? pilotN = null;
        if (experiment.Kind == "pilot")
        {
            var (table, n) = PilotN(chains, experiment.Repair.N);
            csv["pilot_n.csv"] = table;
            pilotN = n;
        }

        var quality = Rq3Rows(usable, experiment);
        csv["rq3_quality.csv"] = Rq3Quality(quality);
        csv["rq3_summary.csv"] = Rq3Summary(quality);
        csv["rq3_availability.csv"] = Rq3Availability(quality);
        csv["rq3_arrow_variants.csv"] = Rq3Variants(quality);
        csv["rq3_smells.csv"] = Rq3Smells(quality);

        csv["cost.csv"] = Cost(usable);
        csv["timing.csv"] = Timing(usable);
        csv["excluded.csv"] = Excluded(tasks.Select(t => t.Task));
        csv["needs_rerun.csv"] = NeedsRerun(runs, experiment.Verification.InfraAttempts);

        return new UnifiedReport { Csv = csv, Markdown = Markdown(runs, experiment, tasks.Select(t => t.Task).ToList(), csv, pilotN), PilotN = pilotN };
    }

    /// <summary>Spec 04 Mục 6.8: cùng sha256 experiment và prompt, không task trùng giữa các run.</summary>
    public static void CheckMergeable(IReadOnlyList<RunData> runs)
    {
        if (runs.Count <= 1)
        {
            return;
        }

        var first = runs[0].Lock;
        foreach (var run in runs.Skip(1))
        {
            if (run.Lock?.ExperimentSha256 != first?.ExperimentSha256)
            {
                throw new ArrowException(ErrorCodes.B4ReportMismatch, $"Run {run.RunId} dùng cấu hình thực nghiệm khác run {runs[0].RunId}");
            }

            var prompts = run.Lock?.PromptSha256 ?? [];
            var firstPrompts = first?.PromptSha256 ?? [];
            if (prompts.Count != firstPrompts.Count || prompts.Any(p => !firstPrompts.TryGetValue(p.Key, out var sha) || sha != p.Value))
            {
                throw new ArrowException(ErrorCodes.B4ReportMismatch, $"Run {run.RunId} dùng bộ prompt khác run {runs[0].RunId}");
            }
        }

        var duplicate = runs.SelectMany(r => r.Tasks.Where(t => t.Block1 is not null).Select(t => t.Key)).GroupBy(k => k).FirstOrDefault(g => g.Count() > 1);
        if (duplicate is not null)
        {
            throw new ArrowException(ErrorCodes.B4ReportMismatch, $"Task {duplicate.Key} có trong nhiều run");
        }

        var modelIds = runs.Select(r => string.Join(",", r.Tasks.SelectMany(t => t.Candidates).Select(c => c.Usage?.ModelId).OfType<string>().Where(x => x.Length > 0).Distinct().Order()))
            .Where(ids => ids.Length > 0).Distinct().ToList();
        if (modelIds.Count > 1)
        {
            throw new ArrowException(ErrorCodes.B4ReportMismatch, "Các run có model id trả về khác nhau: " + string.Join(" | ", modelIds));
        }
    }

    // ── RQ1 ────────────────────────────────────────────────────────────

    private static CsvTable Rq1(IReadOnlyList<TaskData> tasks)
    {
        var rows = new List<IReadOnlyList<string>>();
        foreach (var group in tasks.SelectMany(t => t.Candidates.Select(c => (t.Key.Dataset, Candidate: c)))
                     .GroupBy(x => (x.Dataset, x.Candidate.Model, x.Candidate.Strategy))
                     .OrderBy(g => g.Key.Dataset, StringComparer.Ordinal).ThenBy(g => g.Key.Model, StringComparer.Ordinal).ThenBy(g => g.Key.Strategy, StringComparer.Ordinal))
        {
            int candidates = 0, compile = 0, discovery = 0, execution = 0, target = 0, valid = 0, invalid = 0, infra = 0;
            foreach (var (_, data) in group)
            {
                if (data.Candidate is null)
                {
                    continue;
                }

                if (!data.Candidate.ValidOutput)
                {
                    candidates++;
                    invalid++;
                    continue;
                }

                if (data.VerifyC0 is null)
                {
                    continue;
                }

                if (data.VerifyC0.State == VerificationState.Infra)
                {
                    infra++;
                    continue;
                }

                candidates++;
                var state = data.VerifyC0.State;
                compile += state >= VerificationState.Discovery ? 1 : 0;
                discovery += state >= VerificationState.Runtime ? 1 : 0;
                execution += state >= VerificationState.Assertion ? 1 : 0;
                target += state >= VerificationState.Regression ? 1 : 0;
                valid += state == VerificationState.Valid ? 1 : 0;
            }

            rows.Add([group.Key.Dataset, group.Key.Model, group.Key.Strategy, Stats.I(candidates), Stats.I(compile), Stats.I(discovery), Stats.I(execution), Stats.I(target), Stats.I(valid), Stats.I(invalid), Stats.I(infra)]);
        }

        return new CsvTable(["dataset", "model", "strategy", "candidates", "compile", "discovery", "execution", "target_pass", "valid", "invalid_output", "infra"], rows);
    }

    private static CsvTable Rq1Signatures(IReadOnlyList<TaskData> tasks)
    {
        var rows = tasks.SelectMany(t => t.Candidates.Select(c => (t.Key.Dataset, c)))
            .Where(x => x.c.VerifyC0?.ErrorSignature is not null)
            .GroupBy(x => (x.Dataset, x.c.Model, x.c.Strategy, Signature: x.c.VerifyC0!.ErrorSignature!))
            .OrderBy(g => g.Key.Dataset, StringComparer.Ordinal).ThenBy(g => g.Key.Model, StringComparer.Ordinal).ThenBy(g => g.Key.Strategy, StringComparer.Ordinal).ThenByDescending(g => g.Count())
            .Select(g => (IReadOnlyList<string>)[g.Key.Dataset, g.Key.Model, g.Key.Strategy, g.Key.Signature, Stats.I(g.Count())])
            .ToList();
        return new CsvTable(["dataset", "model", "strategy", "signature", "count"], rows);
    }

    // ── RQ2 ────────────────────────────────────────────────────────────

    private sealed record Chain(string Dataset, ChainData Data)
    {
        public FinalTestSuite Final => Data.Final;
    }

    /// <summary>
    /// Chỉ candidate mà mọi cơ chế đều chạy xong (không INFRA) (spec 04 Mục 6.2); bỏ chain chỉ dùng cho RQ3 (ngoài tập con RQ2).
    /// </summary>
    private static List<Chain> Rq2Chains(IReadOnlyList<TaskData> tasks, ExperimentConfig experiment) =>
        tasks.SelectMany(t => t.Chains.Where(c => !c.Final.Rq3Only).Select(c => new Chain(t.Key.Dataset, c)))
            .GroupBy(c => (c.Dataset, c.Final.TaskId, c.Final.Model, c.Final.Strategy))
            .Where(g => experiment.Repair.Mechanisms.All(m => g.Any(c => c.Final.Mechanism == m && c.Final.StopReason != "infra")))
            .SelectMany(g => g)
            .ToList();

    private static CsvTable Rq2ChainRows(IReadOnlyList<Chain> chains) => new(
        ["dataset", "task_id", "model", "strategy", "mechanism", "c0_state", "success", "success_round", "rounds_run", "stop_reason", "final_state", "final_round",
         "repair_seconds", "llm_seconds", "build_seconds", "tokens_in", "tokens_out", "cost_usd", "rollback", "duplicate_code", "invalid_output",
         "repeated_signature", "level_up", "state_change", "level3_rounds", "dropped_methods", "run_order"],
        chains.OrderBy(c => c.Dataset, StringComparer.Ordinal).ThenBy(c => c.Final.TaskId, StringComparer.Ordinal).ThenBy(c => c.Final.Model, StringComparer.Ordinal)
            .ThenBy(c => c.Final.Strategy, StringComparer.Ordinal).ThenBy(c => c.Final.Mechanism, StringComparer.Ordinal)
            .Select(c =>
            {
                var f = c.Final;
                return (IReadOnlyList<string>)
                [
                    c.Dataset, f.TaskId, f.Model, f.Strategy, f.Mechanism, State(f.C0State), f.Success ? "true" : "false", Stats.I(f.SuccessRound), Stats.I(f.RoundsRun),
                    f.StopReason, State(f.FinalState), Stats.I(f.FinalRound), Stats.F(f.RepairSeconds, 1), Stats.F(f.LlmSeconds, 1), Stats.F(f.BuildSeconds, 1),
                    f.TokensIn.ToString(CultureInfo.InvariantCulture), f.TokensOut.ToString(CultureInfo.InvariantCulture), Stats.F(f.CostUsd, 6),
                    Stats.I(f.Counters.Rollback), Stats.I(f.Counters.DuplicateCode), Stats.I(f.Counters.InvalidOutput), Stats.I(f.Counters.RepeatedSignature),
                    Stats.I(f.Counters.LevelUp), Stats.I(f.Counters.StateChange), Stats.I(f.Counters.Level3Rounds), Stats.I(f.DroppedMethods.Count), f.RunOrder,
                ];
            }).ToList());

    private static IEnumerable<IGrouping<(string Dataset, string Model, string Mechanism), Chain>> ByMechanism(IReadOnlyList<Chain> chains) =>
        chains.GroupBy(c => (c.Dataset, c.Final.Model, c.Final.Mechanism))
            .OrderBy(g => g.Key.Dataset, StringComparer.Ordinal).ThenBy(g => g.Key.Model, StringComparer.Ordinal).ThenBy(g => g.Key.Mechanism, StringComparer.Ordinal);

    private static CsvTable Rq2Summary(IReadOnlyList<Chain> chains) => new(
        ["dataset", "model", "mechanism", "chains", "success", "success_rate", "attempts_mean", "attempts_median", "time_mean_s", "time_median_s"],
        ByMechanism(chains).Select(g =>
        {
            var attempts = g.Select(c => (double)c.Final.SuccessRound).ToList();
            var times = g.Select(c => c.Final.RepairSeconds).ToList();
            var success = g.Count(c => c.Final.Success);
            return (IReadOnlyList<string>)
            [
                g.Key.Dataset, g.Key.Model, g.Key.Mechanism, Stats.I(g.Count()), Stats.I(success), Stats.F((double)success / g.Count(), 4),
                Stats.F(Stats.Mean(attempts), 2), Stats.F(Stats.Median(attempts), 2), Stats.F(Stats.Mean(times), 1), Stats.F(Stats.Median(times), 1),
            ];
        }).ToList());

    private static CsvTable Rq2Mechanisms(IReadOnlyList<Chain> chains) => new(
        ["dataset", "model", "mechanism", "rollback", "duplicate_code", "invalid_output", "repeated_signature", "level_up", "state_change", "level3_rounds", "dropped_methods"],
        ByMechanism(chains).Select(g => (IReadOnlyList<string>)
        [
            g.Key.Dataset, g.Key.Model, g.Key.Mechanism,
            Stats.I(g.Sum(c => c.Final.Counters.Rollback)), Stats.I(g.Sum(c => c.Final.Counters.DuplicateCode)), Stats.I(g.Sum(c => c.Final.Counters.InvalidOutput)),
            Stats.I(g.Sum(c => c.Final.Counters.RepeatedSignature)), Stats.I(g.Sum(c => c.Final.Counters.LevelUp)), Stats.I(g.Sum(c => c.Final.Counters.StateChange)),
            Stats.I(g.Sum(c => c.Final.Counters.Level3Rounds)), Stats.I(g.Sum(c => c.Final.DroppedMethods.Count)),
        ]).ToList());

    private static CsvTable Rq2ByC0State(IReadOnlyList<Chain> chains) => new(
        ["dataset", "model", "mechanism", "c0_state", "chains", "success"],
        chains.GroupBy(c => (c.Dataset, c.Final.Model, c.Final.Mechanism, c.Final.C0State))
            .OrderBy(g => g.Key.Dataset, StringComparer.Ordinal).ThenBy(g => g.Key.Model, StringComparer.Ordinal).ThenBy(g => g.Key.Mechanism, StringComparer.Ordinal).ThenBy(g => g.Key.C0State)
            .Select(g => (IReadOnlyList<string>)[g.Key.Dataset, g.Key.Model, g.Key.Mechanism, State(g.Key.C0State), Stats.I(g.Count()), Stats.I(g.Count(c => c.Final.Success))])
            .ToList());

    private static CsvTable Rq2Cumulative(IReadOnlyList<Chain> chains, int n) => new(
        ["dataset", "model", "mechanism", "round", "successes_cumulative", "rate"],
        ByMechanism(chains).SelectMany(g => Enumerable.Range(1, n).Select(k =>
        {
            var successes = g.Count(c => c.Final.Success && c.Final.SuccessRound <= k);
            return (IReadOnlyList<string>)[g.Key.Dataset, g.Key.Model, g.Key.Mechanism, Stats.I(k), Stats.I(successes), Stats.F((double)successes / g.Count(), 4)];
        })).ToList());

    private static CsvTable Rq2Sensitivity(IReadOnlyList<Chain> chains) => new(
        ["dataset", "model", "p", "chains", "success", "success_rate", "rounds_mean", "time_mean_s"],
        ByMechanism(chains).Where(g => g.Key.Mechanism == "adaptive").SelectMany(g =>
            new int?[] { 1, 2, 3, null }.Select(p =>
            {
                var outcomes = g.Select(c => p is null
                    ? new PSensitivity.Outcome(c.Final.Success, c.Final.Success ? c.Final.SuccessRound : c.Final.RoundsRun, c.Data.Decisions.Sum(d => d.LlmSeconds + d.BuildSeconds))
                    : PSensitivity.Simulate(c.Final, c.Data.Decisions, p.Value)).ToList();
                var success = outcomes.Count(o => o.Success);
                return (IReadOnlyList<string>)
                [
                    g.Key.Dataset, g.Key.Model, p?.ToString(CultureInfo.InvariantCulture) ?? "none", Stats.I(outcomes.Count), Stats.I(success), Stats.F((double)success / outcomes.Count, 4),
                    Stats.F(Stats.Mean(outcomes.Select(o => (double)o.Rounds).ToList()), 2), Stats.F(Stats.Mean(outcomes.Select(o => o.Seconds).ToList()), 1),
                ];
            })).ToList());

    /// <summary>Spec 04 Mục 6.3: S(k) trên Classes2Test, gộp hai cơ chế; trả bảng và N chọn được (null khi chưa có chain nào).</summary>
    private static (CsvTable Table, int? N) PilotN(IReadOnlyList<Chain> chains, int n)
    {
        var pool = chains.Where(c => c.Dataset == "classes2test").ToList();
        var cumulative = Enumerable.Range(1, n).Select(k => pool.Count(c => c.Final.Success && c.Final.SuccessRound <= k)).ToList();
        var last = cumulative.Count == 0 ? 0 : cumulative[^1];
        var rows = cumulative.Select((s, i) => (IReadOnlyList<string>)[Stats.I(i + 1), Stats.I(s), Stats.F(last == 0 ? null : (double)s / last, 4)]).ToList();
        return (new CsvTable(["round", "successes_cumulative", "share_of_round_n"], rows), pool.Count == 0 ? null : NRule.Choose(cumulative));
    }

    // ── RQ3 ────────────────────────────────────────────────────────────

    private sealed record QualityRow(string Dataset, string Model, string TaskId, string System, string SuiteId, string Strategy, string Mechanism, IReadOnlyDictionary<string, double?> Values, SmellResult? Smells);

    private static IReadOnlyDictionary<string, double?> Values(SuiteData suite) => new Dictionary<string, double?>(StringComparer.Ordinal)
    {
        ["line"] = suite.Coverage?.Line,
        ["branch"] = suite.Coverage?.Branch,
        ["method"] = suite.Coverage?.Method,
        ["mutation_score"] = suite.Mutation?.Score,
        ["smell_density"] = suite.Smells?.Density,
    };

    private static List<QualityRow> Rq3Rows(IReadOnlyList<TaskData> tasks, ExperimentConfig experiment)
    {
        var rows = new List<QualityRow>();
        foreach (var task in tasks)
        {
            foreach (var suite in task.Suites)
            {
                if (suite.SuiteId == "evosuite")
                {
                    rows.Add(new QualityRow(task.Key.Dataset, "", task.Key.TaskId, "EvoSuite", suite.SuiteId, "", "", Values(suite), suite.Smells));
                    continue;
                }

                var parts = suite.SuiteId.Split("__");
                if (parts.Length != 3)
                {
                    continue;
                }

                var representative = RepresentativeSuite(task, parts[0]);
                rows.Add(new QualityRow(task.Key.Dataset, parts[0], task.Key.TaskId, suite.SuiteId == representative ? "ARROW" : "ARROW-other", suite.SuiteId, parts[1], parts[2], Values(suite), suite.Smells));
            }
        }

        return rows;
    }

    /// <summary>Q3: bộ <c>zero_shot_project_aware</c> VALID ngay khi sinh; nếu không thì bộ sau Adaptive (nếu đạt VALID).</summary>
    public static string? RepresentativeSuite(TaskData task, string model)
    {
        var initial = $"{model}__{Representative}__initial";
        var adaptive = $"{model}__{Representative}__adaptive";
        return task.Suites.Any(s => s.SuiteId == initial) ? initial : task.Suites.Any(s => s.SuiteId == adaptive) ? adaptive : null;
    }

    private static CsvTable Rq3Quality(IReadOnlyList<QualityRow> rows) => new(
        ["dataset", "model", "task_id", "system", "suite_id", "line", "branch", "method", "mutation_score", "smell_density"],
        rows.OrderBy(r => r.Dataset, StringComparer.Ordinal).ThenBy(r => r.TaskId, StringComparer.Ordinal).ThenBy(r => r.SuiteId, StringComparer.Ordinal)
            .Select(r => (IReadOnlyList<string>)[r.Dataset, r.Model, r.TaskId, r.System, r.SuiteId, .. Metrics.Select(m => Stats.F(r.Values[m]))]).ToList());

    /// <summary>Cặp (ARROW, EvoSuite) theo task cho mỗi (dataset, model).</summary>
    private static IEnumerable<((string Dataset, string Model) Key, List<(QualityRow? Arrow, QualityRow? Evo)> Pairs)> Pairs(IReadOnlyList<QualityRow> rows)
    {
        var evo = rows.Where(r => r.System == "EvoSuite").ToDictionary(r => (r.Dataset, r.TaskId));
        var arrow = rows.Where(r => r.System == "ARROW").ToList();
        foreach (var group in arrow.Select(r => (r.Dataset, r.Model)).Distinct().OrderBy(k => k.Dataset, StringComparer.Ordinal).ThenBy(k => k.Model, StringComparer.Ordinal))
        {
            var mine = arrow.Where(r => r.Dataset == group.Dataset && r.Model == group.Model).ToDictionary(r => r.TaskId);
            var taskIds = mine.Keys.Concat(evo.Keys.Where(k => k.Dataset == group.Dataset).Select(k => k.TaskId)).Distinct(StringComparer.Ordinal);
            yield return (group, taskIds.Select(id => (mine.GetValueOrDefault(id), evo.GetValueOrDefault((group.Dataset, id)))).ToList());
        }
    }

    private static CsvTable Rq3Summary(IReadOnlyList<QualityRow> rows)
    {
        var result = new List<IReadOnlyList<string>>();
        foreach (var (key, pairs) in Pairs(rows))
        {
            foreach (var metric in Metrics)
            {
                var complete = pairs.Where(p => p.Arrow?.Values[metric] is not null && p.Evo?.Values[metric] is not null)
                    .Select(p => (Arrow: p.Arrow!.Values[metric]!.Value, Evo: p.Evo!.Values[metric]!.Value)).ToList();
                var a = complete.Select(p => p.Arrow).ToList();
                var e = complete.Select(p => p.Evo).ToList();
                result.Add([key.Dataset, key.Model, metric, Stats.I(complete.Count), Stats.F(Stats.Mean(a)), Stats.F(Stats.Mean(e)), Stats.F(Stats.Median(a)), Stats.F(Stats.Median(e)),
                    Stats.F(Stats.Mean(complete.Select(p => p.Arrow - p.Evo).ToList())), Stats.F(Stats.StdDev(a)), Stats.F(Stats.StdDev(e))]);
            }
        }

        return new CsvTable(["dataset", "model", "metric", "n_pairs", "arrow_mean", "evosuite_mean", "arrow_median", "evosuite_median", "mean_diff", "arrow_sd", "evosuite_sd"], result);
    }

    private static CsvTable Rq3Availability(IReadOnlyList<QualityRow> rows) => new(
        ["dataset", "model", "both", "only_arrow", "only_evosuite", .. Metrics.Select(m => "dropped_" + m)],
        Pairs(rows).Select(x =>
        {
            var both = x.Pairs.Where(p => p.Arrow is not null && p.Evo is not null).ToList();
            return (IReadOnlyList<string>)
            [
                x.Key.Dataset, x.Key.Model, Stats.I(both.Count), Stats.I(x.Pairs.Count(p => p.Arrow is not null && p.Evo is null)), Stats.I(x.Pairs.Count(p => p.Arrow is null && p.Evo is not null)),
                .. Metrics.Select(m => Stats.I(both.Count(p => p.Arrow!.Values[m] is null || p.Evo!.Values[m] is null))),
            ];
        }).ToList());

    private static CsvTable Rq3Variants(IReadOnlyList<QualityRow> rows) => new(
        ["dataset", "model", "strategy", "mechanism", "suites", .. Metrics.Select(m => m + "_mean")],
        rows.Where(r => r.System != "EvoSuite").GroupBy(r => (r.Dataset, r.Model, r.Strategy, r.Mechanism))
            .OrderBy(g => g.Key.Dataset, StringComparer.Ordinal).ThenBy(g => g.Key.Model, StringComparer.Ordinal).ThenBy(g => g.Key.Strategy, StringComparer.Ordinal).ThenBy(g => g.Key.Mechanism, StringComparer.Ordinal)
            .Select(g => (IReadOnlyList<string>)
            [
                g.Key.Dataset, g.Key.Model, g.Key.Strategy, g.Key.Mechanism, Stats.I(g.Count()),
                .. Metrics.Select(m => Stats.F(Stats.Mean(g.Select(r => r.Values[m]).OfType<double>().ToList()))),
            ]).ToList());

    private static CsvTable Rq3Smells(IReadOnlyList<QualityRow> rows) => new(
        ["dataset", "model", "system", "smell", "suites_with_smell", "suites", "share"],
        rows.Where(r => r.System != "ARROW-other" && r.Smells is not null).GroupBy(r => (r.Dataset, r.Model, r.System))
            .OrderBy(g => g.Key.Dataset, StringComparer.Ordinal).ThenBy(g => g.Key.Model, StringComparer.Ordinal).ThenBy(g => g.Key.System, StringComparer.Ordinal)
            .SelectMany(g => g.SelectMany(r => r.Smells!.Smells.Keys).Distinct().Order(StringComparer.Ordinal).Select(smell =>
            {
                var with = g.Count(r => r.Smells!.Smells.GetValueOrDefault(smell) > 0);
                return (IReadOnlyList<string>)[g.Key.Dataset, g.Key.Model, g.Key.System, smell, Stats.I(with), Stats.I(g.Count()), Stats.F((double)with / g.Count(), 4)];
            })).ToList());

    // ── Chi phí, tài nguyên, cần chạy lại ──────────────────────────────

    private static CsvTable Cost(IReadOnlyList<TaskData> tasks)
    {
        var rows = new List<IReadOnlyList<string>>();
        foreach (var dataset in tasks.Select(t => t.Key.Dataset).Distinct().Order(StringComparer.Ordinal))
        {
            var inDataset = tasks.Where(t => t.Key.Dataset == dataset).ToList();
            foreach (var (block, usages) in new[]
                     {
                         (2, inDataset.SelectMany(t => t.Candidates).Select(c => c.Usage).OfType<Models.Block2.LlmUsage>().ToList()),
                         (3, inDataset.SelectMany(t => t.Chains).SelectMany(c => c.Usages).ToList()),
                     })
            {
                rows.Add([dataset, Stats.I(block), Stats.I(usages.Count), usages.Sum(u => u.PromptTokens).ToString(CultureInfo.InvariantCulture),
                    usages.Sum(u => u.CompletionTokens).ToString(CultureInfo.InvariantCulture), Stats.F(usages.Sum(u => u.CostUsd), 6),
                    Stats.I(usages.Count(u => u.FinishReason == "length"))]);
            }
        }

        return new CsvTable(["dataset", "block", "llm_calls", "tokens_in", "tokens_out", "cost_usd", "finish_length"], rows);
    }

    private static CsvTable Timing(IReadOnlyList<TaskData> tasks)
    {
        var steps = new List<(string Step, List<double> Seconds)>
        {
            ("llm", tasks.SelectMany(t => t.Candidates.Select(c => c.Usage).OfType<Models.Block2.LlmUsage>().Concat(t.Chains.SelectMany(c => c.Usages))).Select(u => u.LatencySeconds).ToList()),
            ("verify_build", tasks.SelectMany(t => t.Candidates.Select(c => c.VerifyC0?.BuildSeconds).OfType<double>()
                .Concat(t.Chains.SelectMany(c => c.Decisions.Where(d => d.BuildSeconds > 0).Select(d => d.BuildSeconds)))).ToList()),
            ("evosuite", tasks.Select(t => t.EvoSuite?.Seconds).OfType<double>().ToList()),
        };
        foreach (var tool in new[] { "jacoco", "pit", "tsdetect" })
        {
            steps.Add((tool, tasks.SelectMany(t => t.Suites).Select(s => s.Outcome?.Tools.GetValueOrDefault(tool)?.Seconds).OfType<double>().ToList()));
        }

        return new CsvTable(["step", "count", "median_s", "p90_s", "max_s"],
            steps.Select(s => (IReadOnlyList<string>)[s.Step, Stats.I(s.Seconds.Count), Stats.F(Stats.Median(s.Seconds), 1), Stats.F(Stats.Percentile(s.Seconds, 0.9), 1), Stats.F(s.Seconds.Count == 0 ? null : s.Seconds.Max(), 1)]).ToList());
    }

    private static CsvTable Excluded(IEnumerable<TaskData> tasks) => new(
        ["dataset", "task_id", "code", "message"],
        tasks.Where(t => t.Block1?.Outcome == OutcomeKind.Excluded).OrderBy(t => t.Key.Dataset, StringComparer.Ordinal).ThenBy(t => t.Key.TaskId, StringComparer.Ordinal)
            .Select(t => (IReadOnlyList<string>)[t.Key.Dataset, t.Key.TaskId, t.Block1!.Code ?? "", OneLine(t.Block1.Message)]).ToList());

    /// <summary>Spec 04 Mục 6.6: job mà dòng ledger mới nhất là INFRA (cần chạy lại riêng), kèm lệnh chạy lại.</summary>
    private static CsvTable NeedsRerun(IReadOnlyList<RunData> runs, int attempts)
    {
        var rows = new List<IReadOnlyList<string>>();
        foreach (var run in runs)
        {
            foreach (var job in run.Ledger.GroupBy(e => (e.Dataset, e.TaskId, e.Job)))
            {
                var latest = job.OrderBy(e => e.At).Last();
                if (!latest.NeedsRerun)
                {
                    continue;
                }

                var timeouts = job.Count(e => e.Code?.Contains("TIMEOUT", StringComparison.Ordinal) == true);
                var step = StepName().Match(latest.Message ?? "") is { Success: true } m ? m.Value : "";
                rows.Add([latest.TaskId, latest.Dataset, Stats.I(latest.Block), latest.Job, step, latest.Code ?? "", Stats.I(attempts), "", timeouts >= 2 ? "true" : "false",
                    $"arrow run --run-id {run.RunId} --tasks {latest.TaskId} --rerun-infra"]);
            }
        }

        return new CsvTable(["task_id", "dataset", "block", "job", "step", "error_code", "attempts", "seconds_each", "timeout_repeated", "rerun_command"],
            rows.OrderBy(r => r[1], StringComparer.Ordinal).ThenBy(r => r[0], StringComparer.Ordinal).ToList());
    }

    // ── report.md ──────────────────────────────────────────────────────

    private static string Markdown(IReadOnlyList<RunData> runs, ExperimentConfig experiment, IReadOnlyList<TaskData> tasks, IReadOnlyDictionary<string, CsvTable> csv, int? pilotN)
    {
        var md = new StringBuilder();
        md.Append($"# Báo cáo ARROW: {experiment.ExperimentId}\n\n");
        md.Append("## Thông tin run\n\n");
        foreach (var run in runs)
        {
            var l = run.Lock;
            md.Append($"- Run `{run.RunId}`: experiment sha256 `{Short(l?.ExperimentSha256)}`, máy `{l?.MachineName}` ({l?.Os}), tool {l?.ToolVersion} commit `{l?.ToolGitCommit ?? "-"}`, tạo lúc {l?.CreatedAt:yyyy-MM-dd HH:mm}\n");
        }

        var modelIds = tasks.SelectMany(t => t.Candidates).Where(c => c.Usage is not null).GroupBy(c => c.Model)
            .Select(g => $"{g.Key} → {string.Join(", ", g.Select(c => c.Usage!.ModelId).Where(x => x.Length > 0).Distinct())}");
        md.Append($"- Model id trả về: {string.Join("; ", modelIds)}\n\n");

        md.Append("## Khối 1: task dùng được và bị loại\n\n");
        foreach (var group in tasks.GroupBy(t => t.Key.Dataset).OrderBy(g => g.Key, StringComparer.Ordinal))
        {
            md.Append($"- {group.Key}: {group.Count(t => t.Block1?.Outcome == OutcomeKind.Ok)} dùng được, {group.Count(t => t.Block1?.Outcome == OutcomeKind.Excluded)} bị loại, {group.Count(t => t.Block1?.Outcome == OutcomeKind.Infra)} INFRA\n");
        }

        Section(md, "RQ1: tính hợp lệ của candidate ban đầu (6.1)", csv["rq1_verification.csv"],
            "Classes2Test (content-matched) và GitBug-Java (upstream-pinned) báo cáo tách riêng. Mẫu số gồm cả output không hợp lệ, không gồm INFRA.");
        Section(md, "RQ2: hiệu quả sửa (6.2)", csv["rq2_summary.csv"], "Chỉ tính candidate mà mọi cơ chế đều chạy xong. Chain không thành công tính bằng N.");
        Section(md, "RQ2: số lần kích hoạt các cơ chế (mô tả, không chứng minh nhân quả)", csv["rq2_mechanisms.csv"], null);
        if (experiment.Kind == "pilot")
        {
            Section(md, pilotN is null ? "Chọn N (pilot, 6.3): chưa có chain sửa nào" : $"Chọn N (pilot, 6.3): N = {pilotN}", csv["pilot_n.csv"],
                "S(k) gộp chain của cả hai cơ chế trên Classes2Test; pilot không xem kết quả so sánh hai cơ chế.");
        }

        Section(md, "Độ nhạy P của dừng sớm (6.4)", csv["rq2_p_sensitivity.csv"], "Tính lại từ decision.json, không gọi thêm model.");
        Section(md, "RQ3: chất lượng bộ test VALID (6.5)", csv["rq3_summary.csv"], "Ghép cặp theo từng chỉ số; n_pairs riêng cho mỗi chỉ số.");
        Section(md, "RQ3: tập so sánh", csv["rq3_availability.csv"], null);
        Section(md, "Chi phí (6.6)", csv["cost.csv"], null);
        Section(md, "Thời gian từng bước (6.6)", csv["timing.csv"], null);
        Section(md, "Task bị loại", csv["excluded.csv"], null);
        var rerun = csv["needs_rerun.csv"];
        Section(md, "Cần chạy lại riêng", rerun, rerun.Rows.Count == 0 ? "Danh sách rỗng: run hoàn chỉnh." : "Các job này **chưa có kết quả** và chưa được tính vào RQ1–RQ3.");

        md.Append("## Ngoại lệ đã khai báo\n\n");
        md.Append("- Cờ build chung tắt checkstyle, spotless, enforcer, JaCoCo của dự án…; JVM chạy với `-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=UTC`.\n");
        md.Append("- Bộ EvoSuite cần `evosuite-standalone-runtime` trong classpath test (Maven: dependency scope `system` chỉ trong bản sao; Gradle: init script).\n");
        md.Append("- JaCoCo gắn qua `JAVA_TOOL_OPTIONS` với `includes=<FQN>:<FQN>$*`; PIT dùng `--targetClasses <FQN>,<FQN>$*`.\n\n");
        md.Append("## Hạn chế\n\n");
        md.Append("- Thực nghiệm chạy một lần cho mỗi cấu hình; các con số cơ chế là thống kê mô tả.\n");
        md.Append("- Commit của Classes2Test là content-matched: tái lập được nhưng không bảo đảm là commit tác giả dataset đã dùng.\n");
        return md.ToString();
    }

    private static void Section(StringBuilder md, string title, CsvTable table, string? note)
    {
        md.Append($"\n## {title}\n\n");
        if (note is not null)
        {
            md.Append(note).Append("\n\n");
        }

        if (table.Rows.Count == 0)
        {
            md.Append("(không có dữ liệu)\n");
            return;
        }

        md.Append("| ").AppendJoin(" | ", table.Headers).Append(" |\n");
        md.Append("| ").AppendJoin(" | ", table.Headers.Select(_ => "---")).Append(" |\n");
        foreach (var row in table.Rows)
        {
            md.Append("| ").AppendJoin(" | ", row.Select(c => c.Replace("|", "\\|", StringComparison.Ordinal))).Append(" |\n");
        }
    }

    private static string State(VerificationState state) => state.ToString().ToUpperInvariant();

    private static string Short(string? sha) => sha is { Length: > 12 } ? sha[..12] : sha ?? "";

    private static string OneLine(string? text) => (text ?? "").Replace("\r\n", " ").Replace('\n', ' ');
}
