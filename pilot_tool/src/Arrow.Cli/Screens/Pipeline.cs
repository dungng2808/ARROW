using Arrow.Core.Config;
using Arrow.Core.Llm;
using Arrow.Core.Manifest;
using Arrow.Core.Prompts;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Infrastructure;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Java;
using Arrow.Infrastructure.Llm;
using Arrow.Infrastructure.Prepare;
using Arrow.Infrastructure.Verify;

namespace Arrow.Cli.Screens;

/// <summary>
/// Các service của một lần chạy và cách dựng từng stage (S0–S4). Model chỉ được tạo khi có job cần gọi model:
/// model giả (<c>--fake-llm</c>) hoặc LiteLLM proxy của máy (tự bật nếu <c>proxy_mode=auto_start</c>).
/// </summary>
public sealed partial class Pipeline : IAsyncDisposable
{
    private readonly ToolPaths _paths;
    private readonly MachineConfig _machine;
    private readonly ExperimentConfig _experiment;
    private readonly RunStore _store;
    private readonly PromptLibrary _prompts;
    private readonly ShardRunScreen.Options _options;
    private readonly GitRepositoryProvider _git;
    private readonly PrepareService _prepare;
    private readonly JavaAnalyzer _analyzer;
    private readonly CandidateRunner _candidates;
    private readonly string _workDir;
    private readonly SemaphoreSlim _llmLock = new(1, 1);
    private ILlmClient? _llm;
    private LlmGateway? _gateway;
    private LiteLlmProxy? _proxy;

    public Pipeline(ToolPaths paths, MachineConfig machine, ExperimentConfig experiment, RunStore store, string workDir, PromptLibrary prompts, ShardRunScreen.Options options)
    {
        _paths = paths;
        _machine = machine;
        _experiment = experiment;
        _store = store;
        _prompts = prompts;
        _options = options;
        _workDir = workDir;
        _git = new GitRepositoryProvider(machine.GitPath, workDir, machine.RepoPolicy);
        _prepare = new PrepareService(_git, machine, experiment, workDir);
        _analyzer = new JavaAnalyzer(paths.PilotToolDir, machine);
        _candidates = new CandidateRunner(_git, workDir, machine, new FiveStepVerifier(experiment, machine, _analyzer));
    }

    /// <summary>Số lượt gọi API thật trong lần chạy này.</summary>
    public int LlmCalls => _gateway?.Calls ?? 0;

    private static string Label(JobKey job) => job.TaskId.Length > 24 ? job.TaskId[..24] + "…" : job.TaskId;

    /// <summary>Task đã qua S0 (job prepare DONE).</summary>
    private List<ManifestRecord> ReadyTasks(IEnumerable<ManifestRecord> tasks)
    {
        var latest = _store.LatestByJob();
        return tasks.Where(t => latest.GetValueOrDefault(new JobKey { Dataset = t.Dataset, TaskId = t.TaskId, Stage = "prepare" }.Canonical)?.Status == JobStatus.Done).ToList();
    }

    // ── S0 ────────────────────────────────────────────────────────────

    public StageSpec PrepareStage(IReadOnlyList<ManifestRecord> tasks)
    {
        var byTask = tasks.ToDictionary(t => (t.Dataset, t.TaskId));
        var jobs = tasks.Select(t => new JobKey { Dataset = t.Dataset, TaskId = t.TaskId, Stage = "prepare" }).ToList();
        return new StageSpec("prepare", jobs, Math.Max(1, _machine.Workers.Build), "build",
            async (job, context, ct) =>
            {
                var record = byTask[(job.Dataset, job.TaskId)];
                try
                {
                    var result = await _prepare.RunAsync(record, _store, job, context.Step, ct).ConfigureAwait(false);
                    var b = result.Baseline;
                    var taskDir = Path.GetRelativePath(_store.Root, _store.TaskDir(record.Dataset, record.TaskId)).Replace('\\', '/');
                    return new JobResult
                    {
                        Outcome = "READY",
                        Message = $"JDK {result.Task.JdkMajor} · {result.Task.Plan.Tool.ToString().ToLowerInvariant()} {result.Task.Plan.Mode.ToString().ToLowerInvariant()} · {b.TestsTotal} test ({b.FailingInAllRuns.Count} fail sẵn, {b.Flaky.Count} flaky) · {result.Context.Dependencies.Count} dependency",
                        BuildSeconds = Math.Round(result.BuildSeconds, 2),
                        Outputs = [$"{taskDir}/{PrepareService.TaskFile}", $"{taskDir}/{PrepareService.BaselineFile}", $"{taskDir}/{PrepareService.RepoContextFile}"],
                    };
                }
                catch (Arrow.Core.Prepare.PrepareException ex)
                {
                    return new JobResult { Status = JobStatus.Infra, Outcome = "INFRA", Reason = ex.Reason, Message = ex.Message };
                }
            },
            Label);
    }

    // ── S1 ────────────────────────────────────────────────────────────

    public StageSpec? GenerateStage(IReadOnlyList<ManifestRecord> tasks, CancellationToken cancellationToken)
    {
        if (_experiment.Models.Count == 0 || _experiment.GenerationStrategies.Count == 0)
        {
            return null;
        }

        var jobs = (from task in ReadyTasks(tasks)
                    from model in _experiment.Models
                    from strategy in _experiment.GenerationStrategies
                    select new JobKey { Dataset = task.Dataset, TaskId = task.TaskId, Stage = "generate", Model = model.Name, Strategy = strategy }).ToList();
        return new StageSpec("generate", jobs, Math.Max(1, _machine.Workers.Build), "gen",
            async (job, context, ct) =>
            {
                var service = new GenerateService(_experiment, _prompts, await LlmAsync(ct).ConfigureAwait(false), _git, _candidates);
                return ToJobResult(await service.RunAsync(_store, job, context.Step, ct).ConfigureAwait(false));
            },
            job => $"{Label(job)} {job.Strategy}");
    }

    public static JobResult ToJobResult(StageOutcome outcome) => new()
    {
        Status = outcome.Status,
        Outcome = outcome.Outcome,
        Reason = outcome.Reason,
        Message = outcome.Message,
        LlmSeconds = Math.Round(outcome.LlmSeconds, 2),
        BuildSeconds = Math.Round(outcome.BuildSeconds, 2),
        Outputs = outcome.Outputs,
        PromptTokens = outcome.PromptTokens,
        CompletionTokens = outcome.CompletionTokens,
        PauseRun = outcome.PauseRun,
    };

    // ── model ─────────────────────────────────────────────────────────

    /// <summary>Client model của run: cache (llm_cache/) bọc ngoài giới hạn tốc độ và kiểm tra model id.</summary>
    public async Task<ILlmClient> LlmAsync(CancellationToken cancellationToken)
    {
        if (_llm is not null)
        {
            return _llm;
        }

        await _llmLock.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            if (_llm is not null)
            {
                return _llm;
            }

            ILlmClient inner;
            if (_options.FakeLlmPath is not null)
            {
                inner = FakeLlmClient.FromFile(_paths.Resolve(_options.FakeLlmPath, Environment.CurrentDirectory));
                ToolLog.Info($"run {_store.RunId}: dùng model giả {_options.FakeLlmPath}");
            }
            else
            {
                var config = LiteLlmConfigFile.TryLoad(_paths.LiteLlmConfigPath)
                             ?? throw new ArrowException("LLM_CONFIG_MISSING", "Chưa có litellm.config.local.yaml: chạy arrow setup");
                _proxy = await LiteLlmProxy.EnsureRunningAsync(_paths.ArrowRoot, _paths.PilotToolDir, _machine, cancellationToken).ConfigureAwait(false);
                inner = new LiteLlmClient(new SocketsHttpHandler(), _machine.Llm.ProxyUrl, config.MasterKey);
            }

            _gateway = new LlmGateway(inner, _machine.Workers.Llm, _options.FakeLlmPath is null ? _machine.Llm.RpmLimit : 0, _options.MaxLlmCalls)
            {
                ExpectedPrefixes = _options.FakeLlmPath is null
                    ? _experiment.Models.ToDictionary(m => m.Name, m => m.ExpectedModelIdPrefix)
                    : new Dictionary<string, string>(),
            };
            _llm = new CachingLlmClient(_gateway, Path.Combine(_store.Root, "llm_cache"));
            return _llm;
        }
        finally
        {
            _llmLock.Release();
        }
    }

    public async ValueTask DisposeAsync()
    {
        if (_proxy is not null)
        {
            await _proxy.DisposeAsync().ConfigureAwait(false);
        }
    }
}
