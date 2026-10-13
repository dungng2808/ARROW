using System.Globalization;
using System.Text;
using Arrow.Core.Config;
using Arrow.Core.Llm;
using Arrow.Core.Prepare;
using Arrow.Core.Quality;
using Arrow.Core.Repair;
using Arrow.Core.Report;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Repair;

namespace Arrow.Infrastructure.Report;

/// <summary>Đường dẫn các file báo cáo đã ghi.</summary>
public sealed record ReportFiles(string Dir, NSelection Part2, List<string> Files);

/// <summary>
/// S5 (specs/07): chỉ đọc dữ liệu trong <c>runs/&lt;id&gt;/</c> (run một máy hoặc run đã gom bằng <c>merge</c>), không gọi model
/// hay build. Ghi <c>reports/part1_cost.csv</c>, <c>part1_infra_errors.csv</c>, <c>part1_generation.csv</c>,
/// <c>part2_cumulative.csv</c>, <c>pilot_report.md</c>.
/// </summary>
public sealed class ReportService(ExperimentConfig experiment)
{
    private sealed record GenResult(string Dataset, string TaskId, string Model, string Strategy, VerifyResult Verify, LlmUsage? Usage);

    public ReportFiles Write(RunStore store)
    {
        var reportsDir = Path.Combine(store.Root, "reports");
        Directory.CreateDirectory(reportsDir);
        var ledger = store.ReadLedger().Where(e => e.Job is not null).ToList();
        var latest = store.LatestByJob();
        var events = File.Exists(store.EventsPath) ? store.ReadRecentEvents(int.MaxValue) : [];
        var prices = experiment.Models.ToDictionary(m => m.Name, m => m.PriceUsdPerMillion);
        var samples = new List<StepSample>();
        var generations = new List<GenResult>();
        var chains = new List<RepairChain>();
        var excluded = new List<(string Task, string Strategy, string Reason)>();
        var repairDatasets = experiment.Manifests.Where(m => m.Repair).Select(m => m.Dataset).ToHashSet();
        var modelIds = new SortedSet<string>(StringComparer.Ordinal);

        // S0: thời gian tải repository (thời lượng job trừ thời gian build) và baseline (các lần chạy test của module).
        foreach (var entry in latest.Values.Where(e => e.Job!.Stage == "prepare" && e.Status == JobStatus.Done && e.StartedAt is not null && e.EndedAt is not null))
        {
            var total = (entry.EndedAt!.Value - entry.StartedAt!.Value).TotalSeconds;
            samples.Add(new StepSample("clone", entry.Job!.Dataset, Math.Max(0, Math.Round(total - (entry.BuildSeconds ?? 0), 2))));
            var baselinePath = Path.Combine(store.TaskDir(entry.Job.Dataset, entry.Job.TaskId), "baseline.json");
            if (File.Exists(baselinePath))
            {
                samples.Add(new StepSample("baseline", entry.Job.Dataset, Math.Round(Json.Read<Baseline>(baselinePath).DurationsSeconds.Sum(), 2)));
            }
        }

        foreach (var taskDir in TaskDirs(store))
        {
            var dataset = Path.GetFileName(Path.GetDirectoryName(taskDir))!;
            var taskId = Path.GetFileName(taskDir);
            foreach (var model in experiment.Models)
            {
                var price = prices.GetValueOrDefault(model.Name);
                foreach (var strategy in experiment.GenerationStrategies)
                {
                    // S1
                    var genDir = Path.Combine(taskDir, "gen", model.Name, strategy);
                    var verifyPath = Path.Combine(genDir, "verify_0.json");
                    var usagePath = Path.Combine(genDir, "usage.json");
                    var usage = File.Exists(usagePath) ? Json.Read<LlmUsage>(usagePath) : null;
                    if (usage is not null)
                    {
                        modelIds.Add(usage.ModelId);
                        samples.Add(new StepSample("llm_generate", dataset, usage.LatencySeconds, usage.CacheHit ? 0 : usage.PromptTokens, usage.CacheHit ? 0 : usage.CompletionTokens,
                            LlmCost.Usd(usage, price?.Input, price?.Output)));
                    }

                    if (File.Exists(verifyPath))
                    {
                        var verify = Json.Read<VerifyResult>(verifyPath);
                        generations.Add(new GenResult(dataset, taskId, model.Name, strategy, verify, usage));
                        if (!verify.InvalidOutput && verify.State != VerificationState.Infra)
                        {
                            samples.Add(new StepSample("build_verify", dataset, Math.Round(verify.TotalBuildSeconds, 2)));
                        }
                    }

                    // S2
                    var candidateDir = RepairService.CandidateDir(taskDir, model.Name, strategy);
                    if (File.Exists(Path.Combine(candidateDir, "excluded.json")))
                    {
                        excluded.Add((taskId, strategy, Json.Read<RepairExclusion>(Path.Combine(candidateDir, "excluded.json")).Reason));
                    }

                    foreach (var mechanism in experiment.Repair.Mechanisms)
                    {
                        var mechanismDir = Path.Combine(candidateDir, mechanism);
                        if (!Directory.Exists(mechanismDir))
                        {
                            continue;
                        }

                        foreach (var decisionPath in Directory.EnumerateFiles(mechanismDir, "decision.json", SearchOption.AllDirectories))
                        {
                            var decision = Json.Read<RepairDecision>(decisionPath);
                            var roundUsagePath = Path.Combine(Path.GetDirectoryName(decisionPath)!, "usage.json");
                            var roundUsage = File.Exists(roundUsagePath) ? Json.Read<LlmUsage>(roundUsagePath) : null;
                            if (roundUsage is not null)
                            {
                                modelIds.Add(roundUsage.ModelId);
                            }

                            samples.Add(new StepSample("llm_repair", dataset, decision.LlmSeconds, decision.LlmTokens.GetValueOrDefault("prompt"), decision.LlmTokens.GetValueOrDefault("completion"),
                                roundUsage is null ? 0 : LlmCost.Usd(roundUsage, price?.Input, price?.Output)));
                            if (decision.BuildSeconds > 0)
                            {
                                samples.Add(new StepSample("build_repair", dataset, decision.BuildSeconds));
                            }
                        }

                        if (!repairDatasets.Contains(dataset))
                        {
                            continue;
                        }

                        var branchKey = new JobKey { Dataset = dataset, TaskId = taskId, Stage = "repair", Model = model.Name, Strategy = strategy, Mechanism = mechanism };
                        var branch = latest.GetValueOrDefault(branchKey.Canonical);
                        var summaryPath = Path.Combine(mechanismDir, "summary.json");
                        var summary = File.Exists(summaryPath) ? Json.Read<RepairSummary>(summaryPath) : null;
                        chains.Add(new RepairChain(taskId, strategy, mechanism,
                            summary is not null && (summary.Success || summary.RoundsUsed >= experiment.Repair.NMax),
                            summary?.SuccessRound, branch?.Status == JobStatus.Infra));
                    }
                }
            }

            // S3, S4
            var stats = Path.Combine(taskDir, "evosuite", "evosuite_stats.json");
            if (File.Exists(stats))
            {
                samples.Add(new StepSample("evosuite", dataset, Json.Read<EvoSuiteStats>(stats).GenerationSeconds));
            }

            var qualityDir = Path.Combine(taskDir, "quality");
            if (Directory.Exists(qualityDir))
            {
                foreach (var system in Directory.EnumerateDirectories(qualityDir))
                {
                    AddQuality<CoverageResult>(system, "jacoco.json", "jacoco", dataset, r => r.Seconds, samples);
                    AddQuality<MutationResult>(system, "pit.json", "pit", dataset, r => r.Seconds, samples);
                    AddQuality<SmellResult>(system, "tsdetect.json", "tsdetect", dataset, r => r.Seconds, samples);
                }
            }
        }

        // Lỗi hạ tầng (job INFRA còn lại).
        var infra = latest.Values.Where(e => e.Status == JobStatus.Infra)
            .Select(e => new InfraRow(e.Job!.TaskId, e.Job.Dataset, e.Job.Stage, e.Reason ?? "",
                ledger.Count(l => l.Job!.Canonical == e.Job.Canonical),
                events.LastOrDefault(v => v.TaskId == e.Job.TaskId && v.Stage == e.Job.Stage && v.Strategy == e.Job.Strategy)?.Message ?? ""))
            .OrderBy(r => r.Stage, StringComparer.Ordinal).ThenBy(r => r.TaskId, StringComparer.Ordinal).ToList();

        var costs = PilotReport.Costs(samples);
        var part2 = PilotReport.SelectN(chains.Where(c => repairDatasets.Count > 0).ToList(), experiment.Repair.NMax, experiment.NRule);
        var files = new List<string>();
        void Write(string name, string content)
        {
            AtomicFile.WriteAllText(Path.Combine(reportsDir, name), content);
            files.Add(Path.Combine(reportsDir, name));
        }

        Write("part1_cost.csv", PilotReport.Csv(costs,
            ("step", r => r.Step), ("dataset", r => r.Dataset), ("count", r => r.Count), ("median_seconds", r => r.MedianSeconds), ("p90_seconds", r => r.P90Seconds),
            ("max_seconds", r => r.MaxSeconds), ("total_seconds", r => r.TotalSeconds), ("prompt_tokens", r => r.PromptTokens), ("completion_tokens", r => r.CompletionTokens),
            ("cost_usd", r => r.CostUsd)));
        Write("part1_infra_errors.csv", PilotReport.Csv(infra,
            ("task_id", r => r.TaskId), ("dataset", r => r.Dataset), ("stage", r => r.Stage), ("reason", r => r.Reason), ("attempts", r => r.Attempts), ("last_message", r => r.LastMessage)));
        Write("part1_generation.csv", PilotReport.Csv(generations,
            ("dataset", g => g.Dataset), ("task_id", g => g.TaskId), ("model", g => g.Model), ("strategy", g => g.Strategy), ("state", g => States.Name(g.Verify.State)),
            ("invalid_output", g => g.Verify.InvalidOutput), ("error_signature", g => g.Verify.ErrorSignature), ("assertion_count", g => g.Verify.AssertionCount),
            ("tests_passed", g => g.Verify.TestMethods.Passed), ("model_id", g => g.Usage?.ModelId)));
        Write("part2_cumulative.csv", PilotReport.CumulativeCsv(part2));
        Write("pilot_report.md", Markdown(store, costs, infra, generations, part2, excluded, modelIds));
        return new ReportFiles(reportsDir, part2, files);
    }

    private static void AddQuality<T>(string systemDir, string file, string step, string dataset, Func<T, double> seconds, List<StepSample> samples)
    {
        var path = Path.Combine(systemDir, file);
        if (File.Exists(path))
        {
            samples.Add(new StepSample(step, dataset, seconds(Json.Read<T>(path))));
        }
    }

    private static IEnumerable<string> TaskDirs(RunStore store)
    {
        var tasks = Path.Combine(store.Root, "tasks");
        return Directory.Exists(tasks)
            ? Directory.EnumerateDirectories(tasks).SelectMany(Directory.EnumerateDirectories).OrderBy(d => d, StringComparer.Ordinal)
            : [];
    }

    /// <summary>Cấu hình đã khóa của từng máy: run một máy có một file, run đã gom có <c>machines/*.json</c>.</summary>
    public static List<RunConfigLock> Locks(RunStore store)
    {
        var machines = Path.Combine(store.Root, "machines");
        if (Directory.Exists(machines))
        {
            return Directory.EnumerateFiles(machines, "*.json").OrderBy(f => f, StringComparer.Ordinal).Select(Json.Read<RunConfigLock>).ToList();
        }

        return File.Exists(store.RunConfigLockPath) ? [Json.Read<RunConfigLock>(store.RunConfigLockPath)] : [];
    }

    private string Markdown(RunStore store, List<CostRow> costs, List<InfraRow> infra, List<GenResult> generations, NSelection part2,
        List<(string Task, string Strategy, string Reason)> excluded, SortedSet<string> modelIds)
    {
        var inv = CultureInfo.InvariantCulture;
        var md = new StringBuilder();
        var locks = Locks(store);
        md.AppendLine($"# Báo cáo pilot: {store.RunId}").AppendLine();
        md.AppendLine("Pilot kiểm tra môi trường, công cụ, chi phí (phần 1) và chọn số vòng sửa tối đa N (phần 2). Pilot **không** trả lời câu hỏi nghiên cứu và không gộp vào kết quả chính.").AppendLine();

        md.AppendLine("## Thông tin run").AppendLine();
        md.AppendLine($"- Cấu hình thực nghiệm: `{experiment.ExperimentId}` (sha256 `{locks.FirstOrDefault()?.ExperimentSha256 ?? "?"}`), `n_max` = {experiment.Repair.NMax}, quy tắc N = `{experiment.NRule}`");
        md.AppendLine($"- Model id trả về: {(modelIds.Count == 0 ? "(chưa gọi model)" : string.Join(", ", modelIds.Select(m => $"`{m}`")))}");
        md.AppendLine($"- Thời điểm tạo báo cáo: {DateTimeOffset.UtcNow:yyyy-MM-dd HH:mm} UTC").AppendLine();
        md.AppendLine("| Máy | Hệ điều hành | CPU | RAM (GB) | Endpoint | Commit tool | Shard |");
        md.AppendLine("| --- | --- | --- | --- | --- | --- | --- |");
        foreach (var l in locks)
        {
            md.AppendLine($"| {l.MachineName} | {l.Os} | {l.CpuCount} | {l.RamGb.ToString("0", inv)} | {l.LlmEndpointHost ?? "-"} | `{(l.ToolGitCommit ?? "?")[..Math.Min(8, (l.ToolGitCommit ?? "?").Length)]}`{(l.ToolGitDirty == true ? " (dirty)" : "")} | {l.ShardId ?? "-"} |");
        }

        md.AppendLine().AppendLine("## Phần 1: môi trường, công cụ, chi phí").AppendLine();
        md.AppendLine("### 1.1. Thời gian và chi phí theo bước (`part1_cost.csv`)").AppendLine();
        md.AppendLine("| Bước | Dataset | Số lần | Trung vị (s) | p90 (s) | Max (s) | Tổng (s) | Token vào | Token ra | Chi phí (USD) |");
        md.AppendLine("| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |");
        foreach (var r in costs)
        {
            md.AppendLine($"| {r.Step} | {r.Dataset} | {r.Count} | {r.MedianSeconds.ToString("0.0", inv)} | {r.P90Seconds.ToString("0.0", inv)} | {r.MaxSeconds.ToString("0.0", inv)} | {r.TotalSeconds.ToString("0", inv)} | {r.PromptTokens} | {r.CompletionTokens} | {r.CostUsd.ToString("0.0000", inv)} |");
        }

        if (experiment.Models.Any(m => m.PriceUsdPerMillion is null))
        {
            md.AppendLine().AppendLine("Giá token chưa điền trong cấu hình thực nghiệm (`price_usd_per_million`): cột chi phí là 0; token vẫn đầy đủ.");
        }

        md.AppendLine().AppendLine("### 1.2. Lỗi hạ tầng (`part1_infra_errors.csv`)").AppendLine();
        if (infra.Count == 0)
        {
            md.AppendLine("Không có job lỗi hạ tầng.");
        }
        else
        {
            md.AppendLine("| Lý do | Số job |").AppendLine("| --- | ---: |");
            foreach (var group in infra.GroupBy(r => $"{r.Stage} · {r.Reason}").OrderByDescending(g => g.Count()))
            {
                md.AppendLine($"| {group.Key} | {group.Count()} |");
            }

            md.AppendLine().AppendLine("| Task | Dataset | Bước | Lý do | Số lần | Thông điệp cuối |").AppendLine("| --- | --- | --- | --- | ---: | --- |");
            foreach (var r in infra)
            {
                md.AppendLine($"| {r.TaskId} | {r.Dataset} | {r.Stage} | {r.Reason} | {r.Attempts} | {Escape(r.LastMessage)} |");
            }
        }

        md.AppendLine().AppendLine("### 1.3. Kết quả bước sinh test (chỉ để kiểm tra pipeline, không phải kết quả RQ1)").AppendLine();
        var states = Enum.GetValues<VerificationState>();
        md.AppendLine("| Dataset | Quy trình | " + string.Join(" | ", states.Select(States.Name)) + " | Output không hợp lệ |");
        md.AppendLine("| --- | --- | " + string.Join(" | ", states.Select(_ => "---:")) + " | ---: |");
        foreach (var group in generations.GroupBy(g => (g.Dataset, g.Strategy)).OrderBy(g => g.Key.Dataset, StringComparer.Ordinal).ThenBy(g => g.Key.Strategy, StringComparer.Ordinal))
        {
            md.AppendLine($"| {group.Key.Dataset} | {group.Key.Strategy} | " + string.Join(" | ", states.Select(s => group.Count(g => g.Verify.State == s))) + $" | {group.Count(g => g.Verify.InvalidOutput)} |");
        }

        md.AppendLine().AppendLine("Bước kiểm tra và chuẩn hóa output (specs/04 Mục 3) là một phần của pipeline, áp dụng giống nhau cho mọi quy trình prompting.");

        md.AppendLine().AppendLine("### 1.4. Ước tính cho phần GPT-6 Luna của thực nghiệm chính").AppendLine();
        var perCandidate = PerCandidate(costs, "llm_generate", "build_verify");
        var perRound = PerCandidate(costs, "llm_repair", "build_repair");
        var genCost = Mean(costs, "llm_generate");
        var repairCost = Mean(costs, "llm_repair");
        var quality = new[] { "evosuite", "jacoco", "pit", "tsdetect" }.Sum(s => costs.Where(c => c.Step == s).Select(c => c.MedianSeconds).DefaultIfEmpty(0).Max());
        var n = part2.N ?? experiment.Repair.NMax;
        md.AppendLine("| Hạng mục | Số lượng | Thời gian ước tính (giờ, một máy) | Chi phí ước tính (USD) |");
        md.AppendLine("| --- | --- | ---: | ---: |");
        md.AppendLine($"| RQ1 | 200 × 3 + 32 × 3 = 696 ứng viên | {(696 * perCandidate / 3600).ToString("0.0", inv)} | {(696 * genCost).ToString("0.00", inv)} |");
        md.AppendLine($"| RQ2 | 120 ứng viên Classes2Test + ứng viên GitBug-Java thất bại, × 2 cơ chế × tối đa N = {n} vòng (cận trên, chưa gồm GitBug-Java) | {(120 * 2 * n * perRound / 3600).ToString("0.0", inv)} | {(120 * 2 * n * repairCost).ToString("0.00", inv)} |");
        md.AppendLine($"| RQ3 | EvoSuite, JaCoCo, PIT, tsDetect cho 232 CUT | {(232 * quality / 3600).ToString("0.0", inv)} | 0 |");
        md.AppendLine().AppendLine("Ước tính dựa trên thời gian trung vị và chi phí trung bình mỗi lượt của pilot (chỉ GPT-6 Luna). Phần Qwen3-Coder-30B **không** ước tính được từ pilot.");

        md.AppendLine().AppendLine("## Phần 2: chọn N (chỉ Classes2Test)").AppendLine();
        md.AppendLine($"- Chuỗi sửa (ứng viên × cơ chế) dùng để tính: {part2.Chains}; loại vì lỗi hạ tầng chưa giải quyết: {part2.ExcludedInfra}; chưa chạy xong: {part2.Unfinished}.");
        md.AppendLine($"- Ứng viên không vào bước sửa: {excluded.Count}{(excluded.Count == 0 ? "" : " (" + string.Join(", ", excluded.GroupBy(e => e.Reason).Select(g => $"{g.Key}: {g.Count()}")) + ")")}.");
        md.AppendLine("- Số liệu chung hai cơ chế; báo cáo pilot không tách Fixed và Adaptive.").AppendLine();
        md.AppendLine("| Vòng k | S(k) cộng dồn | S(k) / S(n_max) |").AppendLine("| ---: | ---: | ---: |");
        var last = part2.Cumulative.Length == 0 ? 0 : part2.Cumulative[^1];
        for (var k = 1; k <= part2.Cumulative.Length; k++)
        {
            md.AppendLine($"| {k} | {part2.Cumulative[k - 1]} | {(last == 0 ? "-" : ((double)part2.Cumulative[k - 1] / last).ToString("0.00", inv))} |");
        }

        md.AppendLine();
        if (part2.Computable)
        {
            md.AppendLine($"**N theo quy tắc `{part2.Rule}`: {part2.N}.**").AppendLine();
            md.AppendLine("Phân tích độ nhạy: " + string.Join(", ", part2.Sensitivity.Select(s => $"`{s.Key}` → {s.Value}")) + ".");
            if (part2.NMax < 10)
            {
                md.AppendLine().AppendLine($"Lưu ý: run này chạy `n_max` = {part2.NMax} (nhỏ hơn 10 của pilot); N chỉ có ý nghĩa kiểm tra công cụ.");
            }
        }
        else
        {
            md.AppendLine($"N chưa tính được: còn {part2.Unfinished} chuỗi chưa VALID và chưa đủ {part2.NMax} vòng{(part2.Chains == 0 ? " (chưa có chuỗi sửa nào)" : "")}.");
        }

        md.AppendLine().AppendLine("## Ngoại lệ đã khai báo").AppendLine();
        md.AppendLine("- Cờ build chung bỏ qua plugin định dạng, giấy phép, coverage tối thiểu của repository (specs/02 Mục 4.3), như nhau cho mọi hệ thống; ghi trong `run_config.lock.json`.");
        md.AppendLine("- JVM của mọi bước build chạy với locale `en_US` và múi giờ UTC (specs/02 Mục 3 bước 6).");
        md.AppendLine("- EvoSuite: `evosuite-standalone-runtime-1.2.0.jar` thêm vào scope test chỉ trong workspace của job EvoSuite và job đo bộ test EvoSuite (specs/06 Mục 1).");
        md.AppendLine("- PIT chạy bản dòng lệnh 1.17.4 cho mọi build tool; module JUnit 5 thêm `pitest-junit5-plugin` 1.2.1 và `junit-platform-launcher` cùng phiên bản platform (specs/06 Mục 2.2).");
        md.AppendLine("- Mẫu Classes2Test của pilot sàng lọc bằng S0 (tiêu chí B, `evaluation_manifest/pilot/README.md`); revision là content-matched.");

        md.AppendLine().AppendLine("## Hạn chế").AppendLine();
        md.AppendLine("- Pilot chỉ chạy GPT-6 Luna; N chọn trên GPT-6 Luna được áp dụng cho cả Qwen3-Coder-30B, có thể chưa tối ưu cho Qwen.");
        md.AppendLine("- Thực nghiệm chạy một lần cho mỗi cấu hình.");
        return md.ToString();
    }

    private static double PerCandidate(List<CostRow> costs, params string[] steps) =>
        steps.Sum(s => costs.Where(c => c.Step == s).Select(c => c.MedianSeconds).DefaultIfEmpty(0).Max());

    private static double Mean(List<CostRow> costs, string step)
    {
        var rows = costs.Where(c => c.Step == step).ToList();
        var count = rows.Sum(r => r.Count);
        return count == 0 ? 0 : rows.Sum(r => r.CostUsd) / count;
    }

    private static string Escape(string text) => text.Replace("|", "\\|").Replace("\n", " ");
}
