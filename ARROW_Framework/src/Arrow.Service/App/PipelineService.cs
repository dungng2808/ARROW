using System.Globalization;
using System.Reflection;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Run;
using Arrow.Service.Block2;
using Arrow.Service.Block4;
using Arrow.Service.External;
using Arrow.Service.Interfaces;
using Arrow.Service.Pipeline;

namespace Arrow.Service.App;

/// <summary>
/// <c>arrow run</c> (spec 00 Mục 5, spec 07 Mục 7.1, 8): đọc cấu hình, kiểm tra bộ prompt, tạo hoặc mở run (khóa cấu hình trong
/// <c>run.lock.json</c>), dựng lớp gọi model (Replaying → Gateway → LiteLLM/giả), chạy các job ở luồng nền, ghi <c>status.json</c> mỗi
/// 2 giây và nhận lệnh điều khiển (<c>control.json</c>) từ terminal khác.
/// </summary>
public sealed class PipelineService(
    FrameworkPaths paths,
    IConfigRepository config,
    IManifestRepository manifests,
    IPromptRepository prompts,
    IRunRepository runs,
    ITaskResultRepository results,
    ILlmCacheRepository cache,
    ILogRepository log,
    Func<RunSetup, RunComponents> componentsFactory,
    Func<ILlmClient>? fakeLlmFactory = null) : IPipelineService
{
    public static readonly TimeSpan Heartbeat = TimeSpan.FromSeconds(2);

    private ControlGate? _gate;
    private RunCounters _counters = new();
    private string _runId = "";
    private DateTimeOffset _startedAt;
    private volatile RunState _state = RunState.Done;

    private sealed record Prepared(string RunId, LoadedExperiment Experiment, string ExperimentText, IReadOnlyList<PlannedTask> Tasks, MachineConfig? Machine, ToolEnvironment Tools, string WorkDir, string ToolsDir, Models.Block3.RepairSubset? Subset);

    public RunHandle Start(RunRequest request, IProgress<RunEvent> progress)
    {
        if (_state is RunState.Running or RunState.Paused or RunState.Stopping)
        {
            throw new InvalidOperationException("Đang có một run chạy trong tiến trình này");
        }

        var prepared = Prepare(request);
        _gate?.Dispose();
        _gate = new ControlGate();
        _counters = new RunCounters();
        _runId = prepared.RunId;
        _startedAt = DateTimeOffset.Now;
        _state = RunState.Running;
        var gate = _gate;
        return new RunHandle(prepared.RunId, Task.Run(() => RunAsync(prepared, request, gate, progress)));
    }

    public void Pause() => _gate?.Pause();

    public void Resume() => _gate?.Resume();

    public void Stop()
    {
        if (_gate is { } gate)
        {
            _state = RunState.Stopping;
            gate.Stop();
        }
    }

    public RunStatus GetSummary() => _counters.Snapshot(_runId, CurrentState(), _startedAt, _gate?.PausedReason);

    private RunState CurrentState() => _state == RunState.Running && _gate?.IsPaused == true ? RunState.Paused : _state;

    // ── chuẩn bị (đồng bộ, lỗi báo ngay cho UI) ────────────────────────

    private Prepared Prepare(RunRequest request)
    {
        var experimentPath = request.ExperimentPath ?? config.ListExperiments() switch
        {
            [var only] => only,
            [] => throw new ArrowException(ErrorCodes.ConfigMissing, $"Không có file cấu hình thực nghiệm trong {paths.ExperimentsDir}"),
            var many => throw new ArgumentException("Có nhiều cấu hình thực nghiệm, chọn bằng --experiment: " + string.Join(", ", many.Select(Path.GetFileName))),
        };
        var experiment = config.LoadExperiment(experimentPath);
        var problems = PromptConsistencyChecker.Check(prompts.LoadAll().ToDictionary(t => t.RelativePath));
        if (problems.Count > 0)
        {
            throw new ArrowException(ErrorCodes.PromptInvalid, "Bộ prompt không nhất quán: " + string.Join("; ", problems));
        }

        var runId = request.RunId ?? $"run-{DateTime.Now:yyyyMMdd-HHmm}";
        if (runId.Length == 0 || runId.Any(c => !(char.IsAsciiLetterOrDigit(c) || c is '-' or '_' or '.')))
        {
            throw new ArgumentException($"Run id chỉ gồm chữ, số, '-', '_', '.': '{runId}'");
        }

        var all = manifests.ReadTasks(experiment);
        var wanted = ScreeningService.ParseSelection(request.TaskSelection).Concat(ShardTasks(request.ShardPath, experiment)).Distinct().ToList();
        var unknown = wanted.Except(all.Select(t => t.TaskId)).ToList();
        if (unknown.Count > 0)
        {
            throw new ArgumentException("Task không có trong cấu hình thực nghiệm: " + string.Join(", ", unknown));
        }

        // Số thứ tự task (quyết định Fixed hay Adaptive chạy trước) là vị trí trong toàn bộ thực nghiệm, không phụ thuộc
        // --tasks/--shard, để cùng một task chạy theo cùng thứ tự trên mọi máy.
        var planned = all.Select((t, i) => new PlannedTask(t, experiment.Config.Manifests.First(m => m.Dataset == t.Dataset), i + 1))
            .Where(p => wanted.Count == 0 || wanted.Contains(p.Record.TaskId))
            .ToList();
        var machine = config.LoadMachine();
        var tools = ToolLocator.Environment(machine, paths.ArrowRoot);
        var experimentText = File.ReadAllText(experiment.Path).Replace("\r\n", "\n");
        // Tập con RQ2 (pha 2): chỉ dùng khi được chọn cho đúng cấu hình này; chưa có thì chain của dataset đặt repair_subset chờ.
        var subset = experiment.Config.RepairSubsetFile is { } subsetFile ? config.LoadRepairSubset(experiment.ResolvePath(subsetFile)) : null;
        if (subset is not null && subset.ExperimentSha256 != experiment.Sha256)
        {
            throw new ArrowException(ErrorCodes.RunConfigMismatch,
                $"{experiment.Config.RepairSubsetFile} được chọn cho cấu hình sha256 {subset.ExperimentSha256[..12]}…, cấu hình đang dùng là {experiment.Sha256[..12]}…");
        }

        var prepared = new Prepared(runId, experiment, experimentText, planned, machine, tools,
            paths.Resolve(machine?.WorkDir ?? "../repo"), paths.Resolve(machine?.ToolsDir ?? "../tools"), subset);
        LockRun(prepared);
        return prepared;
    }

    /// <summary>
    /// <c>--shard</c>: file phân công (<c>shards/&lt;plan&gt;/shard-NN.json</c>, mục <c>tasks[].task_id</c>). File ghi sha256 của cấu hình
    /// thực nghiệm (và của manifest); lệch với cấu hình đang dùng thì từ chối, để mọi máy chạy cùng một cấu hình.
    /// </summary>
    private static IReadOnlyList<string> ShardTasks(string? shardPath, Models.Config.LoadedExperiment experiment)
    {
        if (string.IsNullOrWhiteSpace(shardPath))
        {
            return [];
        }

        if (!File.Exists(shardPath))
        {
            throw new ArrowException(ErrorCodes.ConfigMissing, $"Không có file shard {shardPath}");
        }

        var shard = System.Text.Json.Nodes.JsonNode.Parse(File.ReadAllText(shardPath))
                    ?? throw new ArrowException(ErrorCodes.ConfigInvalid, $"{shardPath}: không phải JSON");
        var expected = shard["experiment_sha256"]?.GetValue<string>();
        if (expected is not null && expected != experiment.Sha256)
        {
            throw new ArrowException(ErrorCodes.RunConfigMismatch,
                $"{Path.GetFileName(shardPath)} được chia cho cấu hình thực nghiệm sha256 {expected[..12]}…, cấu hình đang dùng là {experiment.Sha256[..12]}… (pull bản mới hoặc chia lại shard)");
        }

        var tasks = shard["tasks"]?.AsArray().Select(t => t?["task_id"]?.GetValue<string>()).OfType<string>().ToList()
                    ?? throw new ArrowException(ErrorCodes.ConfigInvalid, $"{shardPath}: thiếu mục tasks");
        return tasks;
    }

    /// <summary>Run mới: ghi <c>run.lock.json</c> và bản sao cấu hình; run cũ: cấu hình và prompt phải khớp (<c>RUN_CONFIG_MISMATCH</c>).</summary>
    private void LockRun(Prepared prepared)
    {
        var promptSha = prompts.LoadAll().ToDictionary(t => t.RelativePath, t => t.Sha256);
        var existing = runs.ReadLock(prepared.RunId);
        if (existing is not null)
        {
            var samePrompts = existing.PromptSha256.Count == promptSha.Count && existing.PromptSha256.All(p => promptSha.GetValueOrDefault(p.Key) == p.Value);
            if (existing.ExperimentSha256 != prepared.Experiment.Sha256 || !samePrompts)
            {
                throw new ArrowException(ErrorCodes.RunConfigMismatch,
                    $"Run {prepared.RunId} được tạo với {(existing.ExperimentSha256 != prepared.Experiment.Sha256 ? "cấu hình thực nghiệm" : "bộ prompt")} khác; dùng run id khác hoặc khôi phục cấu hình cũ");
            }

            return;
        }

        var liteLlm = config.LoadLiteLlm();
        runs.EnsureCreated(prepared.RunId);
        runs.WriteText(prepared.RunId, RunResultsLoader.ExperimentCopy, prepared.ExperimentText);
        runs.WriteLock(prepared.RunId, new RunLock
        {
            RunId = prepared.RunId,
            CreatedAt = DateTimeOffset.Now,
            ExperimentId = prepared.Experiment.Config.ExperimentId,
            ExperimentSha256 = prepared.Experiment.Sha256,
            ManifestSha256 = prepared.Experiment.Config.Manifests.ToDictionary(m => m.Dataset, m => m.Sha256),
            PromptSha256 = promptSha,
            ToolVersion = Assembly.GetExecutingAssembly().GetName().Version?.ToString(3) ?? "",
            ToolGitCommit = GitCommit(),
            MachineName = prepared.Machine?.MachineName is { Length: > 0 } name ? name : Environment.MachineName,
            Os = System.Runtime.InteropServices.RuntimeInformation.RuntimeIdentifier,
            JdkVersions = prepared.Tools.JdkHomes.ToDictionary(j => j.Key.ToString(CultureInfo.InvariantCulture), j => j.Value),
            BuildFlags = BuildCommands.MavenCommonFlags(null, OperatingSystem.IsWindows()).ToList(),
            LlmEndpointHosts = string.Join(", ", prepared.Experiment.Config.Models.Select(m => $"{m.Name}={liteLlm?.Route(m.Name)?.EndpointHost ?? "-"}")),
        });
    }

    /// <summary>Commit của tool (đọc <c>.git/HEAD</c>, không chạy git); không có thì null.</summary>
    private string? GitCommit()
    {
        for (var dir = new DirectoryInfo(paths.FrameworkRoot); dir is not null; dir = dir.Parent)
        {
            var head = Path.Combine(dir.FullName, ".git", "HEAD");
            if (!File.Exists(head))
            {
                continue;
            }

            var text = File.ReadAllText(head).Trim();
            if (!text.StartsWith("ref: ", StringComparison.Ordinal))
            {
                return text;
            }

            var reference = Path.Combine(dir.FullName, ".git", text[5..].Replace('/', Path.DirectorySeparatorChar));
            return File.Exists(reference) ? File.ReadAllText(reference).Trim() : null;
        }

        return null;
    }

    // ── chạy (luồng nền) ───────────────────────────────────────────────

    private async Task<RunResult> RunAsync(Prepared prepared, RunRequest request, ControlGate gate, IProgress<RunEvent> progress)
    {
        var runId = prepared.RunId;
        string? error = null;
        LiteLlmProxy? proxy = null;
        RunComponents? components = null;
        using var heartbeatStop = new CancellationTokenSource();
        var heartbeat = HeartbeatAsync(runId, gate, heartbeatStop.Token);
        try
        {
            Event(runId, progress, EventLevel.Info,
                $"Run {runId} · {prepared.Experiment.Config.ExperimentId} · {prepared.Tasks.Count} task · khối {string.Join(',', request.Blocks)}{(request.FakeLlm ? " · model giả" : "")}");
            var needsModel = request.Blocks.Contains(2) || request.Blocks.Contains(3);
            ILlmClient baseLlm;
            if (request.FakeLlm)
            {
                baseLlm = fakeLlmFactory?.Invoke() ?? FakeLlmClient.ForPipeline();
            }
            else
            {
                var liteLlm = config.LoadLiteLlm();
                var machineLlm = prepared.Machine?.Llm ?? new LlmMachineConfig();
                if (needsModel)
                {
                    var missing = prepared.Experiment.Config.Models.Where(m => liteLlm?.Route(m.Name) is not { IsComplete: true }).Select(m => m.Name).ToList();
                    if (missing.Count > 0)
                    {
                        throw new ArrowException(ErrorCodes.B2LlmConfig, $"Chưa khai báo provider/khóa cho model {string.Join(", ", missing)} (arrow models)");
                    }

                    proxy = new LiteLlmProxy(machineLlm, paths, log);
                    await proxy.EnsureRunningAsync(gate.Token).ConfigureAwait(false);
                }

                baseLlm = new LiteLlmClient(new HttpClient { Timeout = Timeout.InfiniteTimeSpan }, machineLlm.ProxyUrl, liteLlm?.MasterKey, log);
            }

            var machine = prepared.Machine;
            // Model giả không tốn tiền: không áp giá của model thật lên số token giả.
            var models = request.FakeLlm ? prepared.Experiment.Config.Models.Select(m => m with { PriceUsdPerMillion = null }) : prepared.Experiment.Config.Models;
            using var gateway = new LlmGateway(baseLlm, models, machine?.Workers.Llm ?? 2, request.FakeLlm ? 0 : machine?.Llm.RpmLimit ?? 60, request.MaxLlmCalls);
            var llm = new ReplayingLlmClient(gateway, cache, runId);
            var setup = new RunSetup(runId, prepared.Tools, prepared.WorkDir, prepared.ToolsDir, machine, prepared.Experiment, llm, prepared.Subset);
            components = componentsFactory(setup);
            var planner = new ResumePlanner(runs.ReadLedger(runId), request.RerunInfra);
            var executor = new RunExecutor(setup, components, runs, results, planner, gate, _counters, request, progress, log);
            await executor.RunAsync(prepared.Tasks).ConfigureAwait(false);
            _state = RunState.Done;
        }
        catch (OperationCanceledException) when (gate.IsStopped)
        {
            _state = RunState.Stopped;
        }
        catch (Exception ex) when (ex is ArrowException or IOException or InvalidOperationException or HttpRequestException)
        {
            error = ex is ArrowException arrow ? $"{arrow.Code}: {arrow.Message}" : ex.Message;
            log.Error($"[RUN] {runId}: {error}", ex);
            Event(runId, progress, EventLevel.Error, error);
            _state = RunState.Failed;
        }
        finally
        {
            await heartbeatStop.CancelAsync().ConfigureAwait(false);
            await heartbeat.ConfigureAwait(false);
            if (components is not null)
            {
                await components.DisposeAsync().ConfigureAwait(false);
            }

            if (proxy is not null)
            {
                await proxy.DisposeAsync().ConfigureAwait(false);
            }
        }

        var status = GetSummary();
        runs.WriteStatus(runId, status);
        Event(runId, progress, _state == RunState.Done ? EventLevel.Info : EventLevel.Warn, $"Run {runId} kết thúc: {_state.ToString().ToUpperInvariant()}");
        return new RunResult { RunId = runId, State = _state, Error = error, Status = status };
    }

    /// <summary>Mỗi 2 giây: ghi <c>status.json</c>, đọc lệnh điều khiển từ terminal khác (<c>control.json</c>).</summary>
    private async Task HeartbeatAsync(string runId, ControlGate gate, CancellationToken stop)
    {
        while (!stop.IsCancellationRequested)
        {
            try
            {
                if (runs.ReadControl(runId) is { } command)
                {
                    runs.ClearControl(runId);
                    switch (command.Action)
                    {
                        case ControlAction.Pause:
                            gate.Pause();
                            break;
                        case ControlAction.Resume:
                            gate.Resume();
                            break;
                        case ControlAction.Stop:
                            Stop();
                            break;
                    }

                    log.Info($"[RUN] {runId}: lệnh điều khiển {command.Action} từ {command.Source}");
                }

                runs.WriteStatus(runId, GetSummary());
            }
            catch (IOException ex)
            {
                log.Warn($"[RUN] heartbeat: {ex.Message}");
            }

            try
            {
                await Task.Delay(Heartbeat, stop).ConfigureAwait(false);
            }
            catch (OperationCanceledException)
            {
                return;
            }
        }
    }

    private void Event(string runId, IProgress<RunEvent> progress, EventLevel level, string message)
    {
        var runEvent = new RunEvent { At = DateTimeOffset.Now, Level = level, Message = message };
        runs.AppendEvent(runId, runEvent);
        progress.Report(runEvent);
    }
}
