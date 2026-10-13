using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Repair;
using Arrow.Core.Report;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Report;
using Arrow.Infrastructure.Repair;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary><c>arrow report</c>, <c>export</c>, <c>merge</c>, <c>show</c> (mốc M8, specs/07, specs/01 Mục 5).</summary>
public sealed class ReportScreens(Session session)
{
    private LoadedExperiment Experiment(string? path, RunStore store)
    {
        var locks = ReportService.Locks(store);
        if (path is null && locks.Count > 0)
        {
            // Tự tìm cấu hình thực nghiệm của run (ví dụ smoke-v1) theo sha256 đã khóa.
            var match = Directory.EnumerateFiles(session.Paths.ExperimentsDir, "*.json")
                .FirstOrDefault(f => Hashing.Sha256File(f) == locks[0].ExperimentSha256);
            if (match is not null)
            {
                return ExperimentLoader.Load(match, verifyManifests: false);
            }
        }

        var machine = session.Machine;
        var file = path ?? machine?.Experiment ?? throw new ArrowException("NO_EXPERIMENT", "Cần --experiment hoặc cấu hình máy");
        var experiment = ExperimentLoader.Load(session.Paths.Resolve(file), verifyManifests: false);
        if (locks.Count > 0 && locks.Any(l => l.ExperimentSha256 != experiment.Sha256))
        {
            throw new ArrowException("EXPERIMENT_MISMATCH", $"Run {store.RunId} được tạo với cấu hình thực nghiệm khác {file}; dùng --experiment đúng file");
        }

        return experiment;
    }

    private RunStore Existing(string runId)
    {
        var store = session.Store(runId);
        return store.Exists ? store : throw new ArrowException("RUN_MISSING", $"Không có runs/{runId}");
    }

    public int Report(string runId, string? experimentPath)
    {
        var store = Existing(runId);
        var result = new ReportService(Experiment(experimentPath, store).Config).Write(store);
        foreach (var file in result.Files)
        {
            AnsiConsole.MarkupLine($"{Theme.Ok} {Markup.Escape(session.Paths.Relative(file))}");
        }

        AnsiConsole.MarkupLine(result.Part2.Computable
            ? $"N theo quy tắc [bold]{Markup.Escape(result.Part2.Rule)}[/] = [bold]{result.Part2.N}[/] · độ nhạy: {Markup.Escape(string.Join(", ", result.Part2.Sensitivity.Select(p => $"{p.Key}={p.Value}")))}"
            : $"N chưa tính được ({result.Part2.Unfinished} chuỗi chưa xong, {result.Part2.Chains} chuỗi)");
        return 0;
    }

    public int Export(string runId, string? output)
    {
        var store = Existing(runId);
        var outDir = output is null ? Path.Combine(session.Paths.ArrowRoot, "pilot_results") : session.Paths.Resolve(output, Environment.CurrentDirectory);
        var result = BundleService.Export(store, outDir);
        AnsiConsole.MarkupLine($"{Theme.Ok} Đã đóng gói [bold]{Markup.Escape(result.ZipPath)}[/] ({result.Bytes / 1024.0 / 1024.0:0.0} MB, {result.Manifest.Files.Count} file)");
        if (result.Warning)
        {
            AnsiConsole.MarkupLine("[yellow]Gói lớn hơn 50 MB: GitHub có thể chậm; cân nhắc bỏ log build không cần.[/]");
        }

        AnsiConsole.MarkupLine("Gửi file này cho người điều phối (hoặc commit vào pilot_results/ nếu nhóm thống nhất). Gói không chứa khóa API.");
        return 0;
    }

    public int Merge(string planPath, string bundlesDir, string mergedRunId, string? experimentPath)
    {
        var bundles = Directory.EnumerateFiles(session.Paths.Resolve(bundlesDir, Environment.CurrentDirectory), "*.zip").OrderBy(f => f, StringComparer.Ordinal).ToList();
        var store = BundleService.Merge(session.Paths.Resolve(planPath, Environment.CurrentDirectory), bundles, session.RunsDir, mergedRunId);
        AnsiConsole.MarkupLine($"{Theme.Ok} Đã gom {bundles.Count} gói vào [bold]runs/{Markup.Escape(mergedRunId)}[/]");
        return Report(mergedRunId, experimentPath);
    }

    /// <summary>Kết quả sinh test, các vòng sửa và đường cộng dồn của một run (chỉ đọc).</summary>
    public int Show(string runId, string? taskFilter, string? experimentPath)
    {
        var store = Existing(runId);
        var experiment = Experiment(experimentPath, store).Config;
        var tasksRoot = Path.Combine(store.Root, "tasks");
        var chains = new List<RepairChain>();
        var taskDirs = Directory.Exists(tasksRoot) ? Directory.EnumerateDirectories(tasksRoot).SelectMany(Directory.EnumerateDirectories).OrderBy(d => d, StringComparer.Ordinal).ToList() : new List<string>();
        foreach (var taskDir in taskDirs)
        {
            var taskId = Path.GetFileName(taskDir);
            var dataset = Path.GetFileName(Path.GetDirectoryName(taskDir))!;
            var show = taskFilter is null || taskId.StartsWith(taskFilter, StringComparison.Ordinal);
            var table = new Table().Border(TableBorder.Rounded).Title($"[bold]{Markup.Escape(dataset)}/{Markup.Escape(taskId)}[/]")
                .AddColumns("Model / quy trình", "Sinh test", "Fixed", "Adaptive");
            foreach (var model in experiment.Models)
            {
                foreach (var strategy in experiment.GenerationStrategies)
                {
                    var verifyPath = Path.Combine(GenerateService.Dir(taskDir, model.Name, strategy), "verify_0.json");
                    if (!File.Exists(verifyPath))
                    {
                        continue;
                    }

                    var verify = Json.Read<VerifyResult>(verifyPath);
                    var cells = new List<string> { $"{model.Name} / {strategy}", Markup.Escape($"{States.Name(verify.State)}{(verify.InvalidOutput ? " (không hợp lệ)" : "")} {verify.ErrorSignature ?? ""}") };
                    foreach (var mechanism in new[] { "fixed", "adaptive" })
                    {
                        var mechanismDir = Path.Combine(RepairService.CandidateDir(taskDir, model.Name, strategy), mechanism);
                        var summaryPath = Path.Combine(mechanismDir, "summary.json");
                        if (!File.Exists(summaryPath))
                        {
                            cells.Add(File.Exists(Path.Combine(RepairService.CandidateDir(taskDir, model.Name, strategy), "excluded.json")) ? "[grey]loại[/]" : "");
                            continue;
                        }

                        var summary = Json.Read<RepairSummary>(summaryPath);
                        chains.Add(new RepairChain(taskId, strategy, mechanism, summary.Success || summary.RoundsUsed >= experiment.Repair.NMax, summary.SuccessRound, false));
                        var rounds = Directory.EnumerateDirectories(Path.Combine(mechanismDir, summary.AttemptId), "round_*")
                            .Select(d => Path.Combine(d, "decision.json")).Where(File.Exists).Select(Json.Read<RepairDecision>).OrderBy(d => d.Round)
                            .Select(d => $"r{d.Round} L{d.Level} {(d.InvalidOutput ? "không hợp lệ" : d.DuplicateCode ? "mã trùng" : States.Name(d.State!.Value))}{(d.Rollback ? " ↩" : "")}");
                        cells.Add(Markup.Escape(string.Join("\n", rounds)) + (summary.Success ? $"\n[green]VALID ở vòng {summary.SuccessRound}[/]" : ""));
                    }

                    table.AddRow(cells.ToArray());
                }
            }

            if (show && table.Rows.Count > 0)
            {
                AnsiConsole.Write(table);
            }
        }

        var selection = PilotReport.SelectN(chains, experiment.Repair.NMax, experiment.NRule);
        if (selection.Chains > 0)
        {
            var chart = new BarChart().Label($"Đường cộng dồn chung hai cơ chế (S(k), {selection.Chains} chuỗi)").Width(60);
            for (var k = 1; k <= selection.Cumulative.Length; k++)
            {
                chart.AddItem($"vòng {k}", selection.Cumulative[k - 1], Color.Green);
            }

            AnsiConsole.Write(chart);
            AnsiConsole.MarkupLine(selection.Computable
                ? $"N theo quy tắc [bold]{Markup.Escape(selection.Rule)}[/]: [bold]{selection.N}[/]"
                : $"N theo quy tắc: chưa tính ({selection.Unfinished} chuỗi chưa xong)");
        }

        return 0;
    }
}
