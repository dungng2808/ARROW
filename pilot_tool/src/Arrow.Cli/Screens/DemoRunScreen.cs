using Arrow.Cli.Logging;
using Arrow.Cli.Ui;
using Arrow.Core.Runs;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>
/// Lần chạy demo của mốc M1: job giả (không gọi model, không build) để kiểm tra dashboard, ledger,
/// chạy tiếp sau khi dừng, và tạm dừng/tiếp tục/dừng hẳn trên macOS và Windows.
/// </summary>
public sealed class DemoRunScreen(Session session)
{
    private static readonly string[] Strategies = ["zero_shot", "few_shot", "zero_shot_project_aware"];
    private static readonly string[] Outcomes = ["VALID", "VALID", "COMPILE", "COMPILE", "ASSERTION", "RUNTIME", "DISCOVERY", "REGRESSION", "UNKNOWN"];

    public async Task<int> RunAsync(string? runId, int? jobCount, bool plain, CancellationToken cancellationToken)
    {
        runId ??= session.DefaultRunId(demo: true);
        var store = session.Store(runId);
        store.EnsureCreated();
        using var runLog = ToolLogSetup.AttachRun(store.ToolLogPath);
        var jobs = BuildJobs(jobCount);

        var header = new DashboardView.Header(runId, "demo (job giả)", "không gọi model", session.Machine?.MachineName ?? "");
        var host = new RunHost(store, header, plain, rerunInfra: false);
        var stopped = await host.RunAsync([new StageSpec("demo", jobs, 2, "LLM", RunFakeJobAsync)], cancellationToken).ConfigureAwait(false);
        var finished = store.LatestByJob().Values.Count(e => RunStore.IsFinished(e, rerunInfra: false));
        if (stopped)
        {
            RunHost.PrintStopped(runId, $"arrow run --demo --run-id {runId}", finished, jobs.Count);
        }
        else
        {
            AnsiConsole.MarkupLine($"\n{Theme.Ok} Run [bold]{Markup.Escape(runId)}[/] xong: {jobs.Count} job, tạm dừng {Theme.Duration(store.ReadStatus()?.PausedSeconds ?? 0)}.");
        }

        return 0;
    }

    private static async Task<JobResult> RunFakeJobAsync(JobKey job, JobContext context, CancellationToken cancellationToken)
    {
        var seed = Convert.ToInt32(job.ShortHash[..6], 16);
        var random = new Random(seed);
        context.Step("llm");
        var llmSeconds = 0.4 + random.NextDouble() * 1.2;
        await Task.Delay(TimeSpan.FromSeconds(llmSeconds), cancellationToken).ConfigureAwait(false);
        context.Step("build");
        var buildSeconds = 0.6 + random.NextDouble() * 1.6;
        await Task.Delay(TimeSpan.FromSeconds(buildSeconds), cancellationToken).ConfigureAwait(false);

        var infra = random.NextDouble() < 0.03;
        return new JobResult
        {
            Status = infra ? JobStatus.Infra : JobStatus.Done,
            Outcome = infra ? "INFRA" : Outcomes[random.Next(Outcomes.Length)],
            Reason = infra ? "BUILD_TIMEOUT" : null,
            Message = infra ? "build timeout (job giả)" : "job giả",
            LlmSeconds = Math.Round(llmSeconds, 2),
            BuildSeconds = Math.Round(buildSeconds, 2),
            PromptTokens = 2000 + random.Next(4000),
            CompletionTokens = 500 + random.Next(1500),
        };
    }

    private List<JobKey> BuildJobs(int? jobCount)
    {
        var tasks = session.TryLoadShard()?.Tasks.Select(t => (t.Dataset, t.TaskId)).ToList()
                    ?? Enumerable.Range(1, 10).Select(i => ("classes2test", $"demo_task_{i:00}")).ToList();
        var jobs = tasks.SelectMany(t => Strategies.Select(s => new JobKey
        {
            Dataset = t.Item1, TaskId = t.Item2, Stage = "demo", Model = "gpt-6-luna", Strategy = s,
        })).ToList();
        return jobCount is > 0 ? jobs.Take(jobCount.Value).ToList() : jobs;
    }
}
