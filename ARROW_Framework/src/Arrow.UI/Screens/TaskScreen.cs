using System.Globalization;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>S7 (spec 06 Mục 9): kết quả khối 1, c0 và các chain theo model × quy trình, từng vòng của một chain, số đo khối 4.</summary>
public sealed class TaskScreen(IRunQueryService runs, ConsoleInput input, ConsoleOutput output, bool interactive)
{
    public int Run(string runId, string taskId)
    {
        var detail = runs.GetTaskDetail(runId, taskId);
        var data = detail.Data;
        var m = detail.Metadata;
        output.Title($"Task {data.Key.TaskId} · {m?.Cut.Fqn ?? "?"} · {data.Key.Dataset}");

        var block1 = data.Block1;
        if (block1 is null)
        {
            output.Line("Khối 1  chưa chạy");
            return 0;
        }

        if (block1.Outcome != OutcomeKind.Ok || m is null)
        {
            output.Line($"Khối 1  [X] {Format.Outcome(block1.Outcome)} {block1.Code} {block1.Message}");
            return 0;
        }

        var framework = m.TestFramework.Name switch { "junit5" => "JUnit 5", "testng" => "TestNG", _ => "JUnit 4" };
        var mocking = m.TestFramework.Mocking.Count > 0 ? " + " + string.Join(", ", m.TestFramework.Mocking.Select(x => x.Split(':').ElementAtOrDefault(1) ?? x)) : "";
        output.Line($"Khối 1  [OK] {(m.BuildTool.Kind == "gradle" ? "Gradle" : "Maven")} {m.BuildTool.Version} · build root {m.BuildTool.BuildRoot} ({m.BuildTool.BuildMode.ToString().ToLowerInvariant()}" +
                    $"{(m.BuildTool.ReactorModule is null ? "" : ", " + m.BuildTool.ReactorModule)}) · Java {m.Java.Major ?? m.Java.JdkMajor} (JDK {m.Java.JdkMajor}) · {framework}{mocking}");
        if (detail.Baseline is { } baseline)
        {
            output.Line($"        baseline: {baseline.PassingInAllRuns.Count} test pass cả {baseline.Runs} lần, {baseline.Flaky.Count} flaky · tiêu chí B: {baseline.PairedTest.Status.ToString().ToUpperInvariant()}");
        }

        var chains = new List<FinalTestSuite>();
        foreach (var model in data.Candidates.Select(c => c.Model).Distinct())
        {
            output.Blank();
            output.Line($"Khối 2 + 3 ({model})");
            var rows = new List<IReadOnlyList<string>>();
            foreach (var candidate in data.Candidates.Where(c => c.Model == model))
            {
                var c0 = candidate.Candidate is { ValidOutput: false } ? "INVALID" : candidate.VerifyC0 is { } v ? Format.State(v.State) : "-";
                string Chain(string mechanism)
                {
                    var chain = data.Chains.FirstOrDefault(c => c.Final.Model == model && c.Final.Strategy == candidate.Strategy && c.Final.Mechanism == mechanism)?.Final;
                    if (chain is null)
                    {
                        return "-";
                    }

                    chains.Add(chain);
                    return chain.Success
                        ? $"[OK] vòng {chain.SuccessRound} · {chain.RepairSeconds.ToString("0", CultureInfo.InvariantCulture)} s"
                        : $"[X] {chain.RoundsRun} vòng · {Format.State(chain.FinalState)}";
                }

                rows.Add([candidate.Strategy, c0, Chain("fixed"), Chain("adaptive")]);
            }

            output.Table(["Quy trình", "c0", "Fixed", "Adaptive"], rows);
        }

        if (interactive && chains.Count > 0)
        {
            output.Blank();
            var choice = input.Choose("Xem từng vòng của chain nào?", chains.Select(c => $"{c.Model} · {c.Strategy} · {c.Mechanism}").ToList(), "Bỏ qua");
            if (choice >= 0)
            {
                PrintRounds(data, chains[choice], detail.TaskDir);
            }
        }

        if (data.Suites.Count > 0)
        {
            output.Blank();
            output.Line("Khối 4");
            output.Table(["Bộ test", "line", "branch", "mutation", "smell"], data.Suites.Select(s => (IReadOnlyList<string>)
            [
                s.SuiteId, F(s.Coverage?.Line), F(s.Coverage?.Branch), F(s.Mutation?.Score), F(s.Smells?.Density),
            ]).ToList());
        }

        return 0;
    }

    private void PrintRounds(Models.Block4.TaskData data, FinalTestSuite chain, string taskDir)
    {
        var decisions = data.Chains.First(c => c.Final == chain).Decisions;
        var c0 = data.Candidates.First(c => c.Model == chain.Model && c.Strategy == chain.Strategy).VerifyC0;
        var rows = new List<IReadOnlyList<string>>();
        rows.Add(["c0", "-", Format.State(chain.C0State) + (c0?.ErrorSignature is null ? "" : " · " + c0.ErrorSignature), "-", "checkpoint = c0"]);
        foreach (var d in decisions)
        {
            var result = d.InvalidOutput ? "output không hợp lệ" : d.DuplicateCode ? "mã trùng" : RoundState(d);
            var comparison = d.Comparison switch { "BETTER" => "tốt hơn", "WORSE" => "tệ hơn", "EQUAL" => "ngang", _ => "-" };
            var decision = d.State == VerificationState.Valid ? "thành công"
                : chain.Mechanism == "fixed" ? "sửa tiếp từ bản này"
                : d.Improved ? $"checkpoint = {d.Round}, mức {d.LevelAfter}"
                : d.Rollback ? $"rollback, mức {d.LevelAfter}"
                : d.LevelAfter != d.Level ? $"lên mức {d.LevelAfter}" : "giữ mức";
            rows.Add([d.Round.ToString(CultureInfo.InvariantCulture), d.Level.ToString(CultureInfo.InvariantCulture), result, comparison, decision]);
        }

        output.Blank();
        output.Table(["Vòng", "Mức", "Kết quả", "So với checkpoint", "Quyết định"], rows);
        output.Line($"File từng vòng: {Path.Combine(taskDir, "block3", chain.Model, chain.Strategy, chain.Mechanism)}{Path.DirectorySeparatorChar}round_<k>{Path.DirectorySeparatorChar}");
    }

    private static string RoundState(RoundDecision d)
    {
        var text = d.State is { } state ? Format.State(state) : "";
        if (d.State is VerificationState.Assertion or VerificationState.Runtime)
        {
            text += $" {d.TestsPassed} pass";
        }

        return d.RepeatedSignature ? text + " (lặp lỗi)" : text;
    }

    private static string F(double? value) => value is null ? "-" : value.Value.ToString("0.00", CultureInfo.InvariantCulture);
}
