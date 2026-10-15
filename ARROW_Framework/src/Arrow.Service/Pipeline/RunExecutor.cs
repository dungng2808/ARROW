using System.Globalization;
using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Models.Manifest;
using Arrow.Models.Run;
using Arrow.Service.Block3;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Pipeline;

/// <summary>Bộ đếm của run cho <c>status.json</c> và bảng tổng (spec 06 S5, S6).</summary>
public sealed class RunCounters
{
    private readonly object _lock = new();
    private readonly Dictionary<int, BlockProgress> _blocks = Enumerable.Range(1, 4).ToDictionary(b => b, b => new BlockProgress { Block = b });
    private readonly HashSet<string> _running = [];
    private readonly Dictionary<string, int> _c0 = [];
    private readonly Dictionary<string, int> _chains = [];
    private int _llmCalls;
    private double _cost;
    private int _invalid;

    public void Planned(int block) => Update(block, p => p with { Total = p.Total + 1 });

    public void Finished(int block, OutcomeKind outcome) => Update(block, p => p with
    {
        Done = p.Done + 1,
        Infra = p.Infra + (outcome == OutcomeKind.Infra ? 1 : 0),
        Excluded = p.Excluded + (outcome == OutcomeKind.Excluded ? 1 : 0),
    });

    public void Start(string job)
    {
        lock (_lock)
        {
            _running.Add(job);
        }
    }

    public void End(string job)
    {
        lock (_lock)
        {
            _running.Remove(job);
        }
    }

    public void C0(VerificationState state) => Count(_c0, state.ToString().ToUpperInvariant());

    public void Chain(string mechanism, bool success)
    {
        Count(_chains, mechanism);
        if (success)
        {
            Count(_chains, mechanism + "_success");
        }
    }

    public void Llm(LlmUsage usage, bool validOutput)
    {
        lock (_lock)
        {
            _llmCalls++;
            _cost += usage.CostUsd;
            _invalid += validOutput ? 0 : 1;
        }
    }

    public RunStatus Snapshot(string runId, RunState state, DateTimeOffset startedAt, string? pausedReason)
    {
        lock (_lock)
        {
            return new RunStatus
            {
                RunId = runId,
                State = state,
                StartedAt = startedAt,
                UpdatedAt = DateTimeOffset.Now,
                Blocks = [.. _blocks.Values.OrderBy(b => b.Block)],
                Running = [.. _running.Order(StringComparer.Ordinal)],
                C0States = new Dictionary<string, int>(_c0),
                LlmCalls = _llmCalls,
                CostUsd = Math.Round(_cost, 6),
                PausedReason = pausedReason,
                ChainSuccess = new Dictionary<string, int>(_chains),
                InvalidOutputs = _invalid,
            };
        }
    }

    private void Update(int block, Func<BlockProgress, BlockProgress> change)
    {
        lock (_lock)
        {
            _blocks[block] = change(_blocks[block]);
        }
    }

    private void Count(Dictionary<string, int> counts, string key)
    {
        lock (_lock)
        {
            counts[key] = counts.GetValueOrDefault(key) + 1;
        }
    }
}

/// <summary>Một task của run cần chạy: bản ghi manifest, cấu hình manifest của dataset, thứ tự (để xen kẽ Fixed/Adaptive).</summary>
public sealed record PlannedTask(TaskRecord Record, ManifestRef Manifest, int Index);

/// <summary>
/// Chạy các job của từng task theo thứ tự phụ thuộc (spec 00 Mục 5.2, spec 07 Mục 7–8): khối 1 → sinh c0 (mỗi model × quy trình)
/// → xác minh c0 → chain sửa (Fixed/Adaptive xen kẽ theo thứ tự task) → EvoSuite → đo mọi bộ VALID. Mỗi job: kiểm tra
/// <see cref="ControlGate"/>, xóa đầu ra dở dang, chạy, lưu kết quả, rồi ghi dòng ledger (việc ghi cuối cùng).
/// </summary>
public sealed class RunExecutor(
    RunSetup setup,
    RunComponents blocks,
    IRunRepository runs,
    ITaskResultRepository results,
    ResumePlanner planner,
    ControlGate gate,
    RunCounters counters,
    RunRequest request,
    IProgress<RunEvent> progress,
    ILogRepository log)
{
    private ExperimentConfig Experiment => setup.Experiment.Config;
    private string RunId => setup.RunId;
    private bool Runs(int block) => request.Blocks.Contains(block);

    public async Task RunAsync(IReadOnlyList<PlannedTask> tasks)
    {
        var workers = (setup.Machine?.Workers.Build ?? 4) + (setup.Machine?.Workers.Llm ?? 2);
        await Parallel.ForEachAsync(tasks, new ParallelOptions { MaxDegreeOfParallelism = Math.Max(1, workers), CancellationToken = gate.Token }, async (task, _) =>
        {
            try
            {
                await RunTaskAsync(task).ConfigureAwait(false);
            }
            catch (OperationCanceledException)
            {
                throw;
            }
            catch (Exception ex) when (ex is not ArrowException { Code: ErrorCodes.B2CallLimit })
            {
                // Lỗi không lường trước của một task không làm dừng cả run; job đang chạy không có dòng ledger nên sẽ được làm lại.
                log.Error($"[RUN] {task.Record.TaskId}: lỗi không lường trước", ex);
                Event(0, task.Record.TaskId, null, EventLevel.Error, $"lỗi không lường trước: {ex.Message}");
            }
        }).ConfigureAwait(false);
    }

    private async Task RunTaskAsync(PlannedTask planned)
    {
        var record = planned.Record;
        var key = new TaskKey(record.Dataset, record.TaskId);

        // ── Khối 1 ─────────────────────────────────────────────────────
        counters.Planned(1);
        if (Runs(1) && planner.ShouldRun(key, JobIds.Analyze))
        {
            await gate.WaitAsync().ConfigureAwait(false);
            var analysis = await Track(JobIds.Analyze, key, () => blocks.Analyzer.AnalyzeAsync(record, RunContext(), gate.Token)).ConfigureAwait(false);
            Ledger(key, 1, JobIds.Analyze, analysis.Outcome);
            Event(1, key.TaskId, JobIds.Analyze, analysis.Outcome.Outcome == OutcomeKind.Ok ? EventLevel.Info : EventLevel.Warn, DescribeBlock1(analysis));
        }
        else
        {
            Skipped(1, key, JobIds.Analyze);
        }

        var outcome = results.LoadJson<BlockOutcome>(RunId, key, ResultPaths.Block1Outcome);
        var metadata = results.LoadJson<RepositoryMetadata>(RunId, key, ResultPaths.Metadata);
        var baseline = results.LoadJson<Baseline>(RunId, key, ResultPaths.Baseline);
        var workspace = results.LoadJson<WorkspaceInfo>(RunId, key, ResultPaths.Workspace);
        if (outcome?.Outcome != OutcomeKind.Ok || metadata is null || baseline is null || workspace is null)
        {
            return;
        }

        var task = new TaskContext { RunId = RunId, Key = key, Metadata = metadata, Baseline = baseline, Workspace = workspace };
        var cutSource = ReadCut(task);

        // ── Khối 2 và 3 ────────────────────────────────────────────────
        foreach (var model in Experiment.Models)
        {
            foreach (var strategy in Experiment.Strategies)
            {
                var generation = new GenerationContext
                {
                    RunId = RunId, Key = key, Metadata = metadata, CutSource = cutSource, Model = model, Strategy = strategy,
                    ResultDir = ResultPaths.GenerateDir(model.Name, strategy),
                };
                await GenerateAsync(generation).ConfigureAwait(false);
                if (results.LoadJson<CandidateTest>(RunId, key, ResultPaths.Join(generation.ResultDir, ResultPaths.Candidate)) is not { ValidOutput: true } c0)
                {
                    continue;
                }

                var c0Verify = await VerifyC0Async(task, model.Name, strategy, c0).ConfigureAwait(false);
                if (c0Verify is null || c0Verify.State is VerificationState.Infra or VerificationState.Valid || !planned.Manifest.Repair || !Runs(3))
                {
                    continue;
                }

                // AR Mục 8: task lẻ chạy Fixed trước, task chẵn chạy Adaptive trước.
                var mechanisms = Experiment.Repair.Mechanisms.OrderBy(m => m == (planned.Index % 2 == 1 ? "fixed" : "adaptive") ? 0 : 1).ToList();
                var rq3Only = false;
                if (planned.Manifest.RepairSubset is not null)
                {
                    // Tập con RQ2 (spec 03 Mục 6.1): chọn sau RQ1 bằng arrow rq2-subset; chưa có thì chain chờ tới pha 2.
                    if (setup.Subset is null)
                    {
                        Event(3, key.TaskId, JobIds.Chain(model.Name, strategy, "*"), EventLevel.Info, $"{strategy}: chờ tập con RQ2 (arrow rq2-subset) để sửa");
                        continue;
                    }

                    if (!setup.Subset.Contains(planned.Manifest.Dataset, key.TaskId, model.Name, strategy))
                    {
                        // Ngoài tập con: chỉ quy trình đại diện của RQ3 được sửa, bằng Adaptive, không tính vào RQ2.
                        if (strategy != Experiment.Repair.Rq3Strategy || !mechanisms.Contains("adaptive"))
                        {
                            continue;
                        }

                        mechanisms = ["adaptive"];
                        rq3Only = true;
                    }
                }

                for (var i = 0; i < mechanisms.Count; i++)
                {
                    await ChainAsync(task, generation, c0, c0Verify, mechanisms[i], rq3Only ? "rq3_only" : i == 0 ? "first" : "second", rq3Only).ConfigureAwait(false);
                }
            }
        }

        // ── Khối 4 ─────────────────────────────────────────────────────
        if (!Runs(4))
        {
            return;
        }

        var suites = ValidSuites(task);
        if (Experiment.Assessment.ComparisonSystems.Contains("evosuite") && blocks.EvoSuite is not null)
        {
            if (await EvoSuiteAsync(task).ConfigureAwait(false) is { } evosuite)
            {
                suites.Add(evosuite);
            }
        }

        foreach (var suite in suites)
        {
            await MeasureAsync(task, suite).ConfigureAwait(false);
        }
    }

    // ── job của khối 2, 3 ────────────────────────────────────────────

    private async Task GenerateAsync(GenerationContext generation)
    {
        var key = generation.Key;
        var job = JobIds.Generate(generation.Model.Name, generation.Strategy);
        counters.Planned(2);
        if (!Runs(2) || !planner.ShouldRun(key, job))
        {
            Skipped(2, key, job);
            return;
        }

        results.Delete(RunId, key, generation.ResultDir);
        try
        {
            var generated = await Track(job, key, () => WithModelAsync(() => blocks.Generator.GenerateAsync(generation, gate.Token))).ConfigureAwait(false);
            counters.Llm(generated.Usage, generated.Candidate.ValidOutput);
            var candidate = generated.Candidate;
            Ledger(key, 2, job, new BlockOutcome
            {
                Outcome = OutcomeKind.Ok,
                Code = candidate.ValidOutput ? null : ErrorCodes.B2InvalidOutput,
                Message = candidate.ValidOutput ? $"{candidate.TestMethodCount} test method" : candidate.InvalidReason,
            });
            Event(2, key.TaskId, job, candidate.ValidOutput ? EventLevel.Info : EventLevel.Warn,
                $"{generation.Model.Name}/{generation.Strategy} c0 → {(candidate.ValidOutput ? $"{candidate.TestMethodCount} test method, {candidate.AssertionCount} assertion" : "INVALID_OUTPUT " + candidate.InvalidReason)}{(generated.Usage.Replayed ? " (phát lại)" : "")}");
        }
        catch (ArrowException ex) when (ex.Code == ErrorCodes.B2LlmUnavailable)
        {
            Ledger(key, 2, job, new BlockOutcome { Outcome = OutcomeKind.Infra, Code = ex.Code, Message = ex.Message, NeedsRerun = true });
            Event(2, key.TaskId, job, EventLevel.Warn, $"INFRA {ex.Code}: {ex.Message}");
        }
    }

    private async Task<VerifyResult?> VerifyC0Async(TaskContext task, string model, string strategy, CandidateTest c0)
    {
        var job = JobIds.VerifyC0(model, strategy);
        counters.Planned(3);
        if (Runs(3) && planner.ShouldRun(task.Key, job))
        {
            await gate.WaitAsync().ConfigureAwait(false);
            var dir = $"block3/{model}/{strategy}/c0";
            results.Delete(RunId, task.Key, dir);
            var verify = await Track(job, task.Key, () => blocks.Verifier.VerifyAsync(RepairRunner.Suite(c0, 0), task, VerifyOptions(dir), gate.Token)).ConfigureAwait(false);
            results.SaveJson(RunId, task.Key, ResultPaths.VerifyC0(model, strategy), verify);
            counters.C0(verify.State);
            Ledger(task.Key, 3, job, verify.State == VerificationState.Infra
                ? new BlockOutcome { Outcome = OutcomeKind.Infra, Code = verify.InfraReason?.Split(':')[0], Message = verify.InfraReason, NeedsRerun = true }
                : new BlockOutcome { Outcome = OutcomeKind.Ok, Message = verify.State.ToString().ToUpperInvariant() });
            Event(3, task.Key.TaskId, job, verify.State == VerificationState.Infra ? EventLevel.Warn : EventLevel.Info,
                $"{model}/{strategy} c0 → {verify.State.ToString().ToUpperInvariant()}{(verify.ErrorSignature is null ? "" : " · " + verify.ErrorSignature)}");
        }
        else
        {
            Skipped(3, task.Key, job);
        }

        return results.LoadJson<VerifyResult>(RunId, task.Key, ResultPaths.VerifyC0(model, strategy));
    }

    private async Task ChainAsync(TaskContext task, GenerationContext generation, CandidateTest c0, VerifyResult c0Verify, string mechanism, string order, bool rq3Only = false)
    {
        var job = JobIds.Chain(generation.Model.Name, generation.Strategy, mechanism);
        counters.Planned(3);
        if (!planner.ShouldRun(task.Key, job))
        {
            Skipped(3, task.Key, job);
            return;
        }

        var chain = new ChainRequest
        {
            Task = task, Generation = generation, C0 = c0, C0Verify = c0Verify, Mechanism = mechanism,
            N = Experiment.Repair.N, EarlyStopP = mechanism == "adaptive" ? Experiment.Repair.EarlyStopP : null,
            Verify = VerifyOptions(""), RunOrder = order, Rq3Only = rq3Only,
            OnRound = decision => Event(3, task.Key.TaskId, job, EventLevel.Info,
                $"{generation.Strategy} {mechanism} vòng {decision.Round} → {RoundText(decision)} · mức {decision.Level}→{decision.LevelAfter}"),
        };
        var result = await Track(job, task.Key, () => WithModelAsync(() => blocks.Repair.RunChainAsync(chain, gate.Token))).ConfigureAwait(false);
        if (result.Outcome.Outcome == OutcomeKind.Ok)
        {
            counters.Chain(mechanism, result.Final.Success);
        }

        Ledger(task.Key, 3, job, result.Outcome);
        var final = result.Final;
        Event(3, task.Key.TaskId, job, result.Outcome.Outcome == OutcomeKind.Ok ? EventLevel.Info : EventLevel.Warn,
            result.Outcome.Outcome != OutcomeKind.Ok ? $"{generation.Strategy} {mechanism}: INFRA {result.Outcome.Message}"
            : final.Success ? $"{generation.Strategy} {mechanism}: thành công vòng {final.SuccessRound} · {final.RepairSeconds:0} s"
            : $"{generation.Strategy} {mechanism}: không thành công sau {final.RoundsRun} vòng ({final.FinalState.ToString().ToUpperInvariant()})");
    }

    // ── job của khối 4 ─────────────────────────────────────────────────

    /// <summary>Bộ test VALID của ARROW: c0 VALID (<c>…__initial</c>) và chain đạt VALID (<c>…__&lt;mechanism&gt;</c>).</summary>
    private List<SuiteToMeasure> ValidSuites(TaskContext task)
    {
        var suites = new List<SuiteToMeasure>();
        foreach (var model in Experiment.Models.Select(m => m.Name))
        {
            foreach (var strategy in Experiment.Strategies)
            {
                var candidate = results.LoadJson<CandidateTest>(RunId, task.Key, ResultPaths.Join(ResultPaths.GenerateDir(model, strategy), ResultPaths.Candidate));
                if (candidate?.Code is null)
                {
                    continue;
                }

                if (results.LoadJson<VerifyResult>(RunId, task.Key, ResultPaths.VerifyC0(model, strategy))?.State == VerificationState.Valid)
                {
                    suites.Add(Suite($"{model}__{strategy}__initial", candidate, candidate.Code));
                }

                foreach (var mechanism in Experiment.Repair.Mechanisms)
                {
                    if (results.LoadJson<FinalTestSuite>(RunId, task.Key, ResultPaths.Final(model, strategy, mechanism)) is { Success: true }
                        && results.LoadText(RunId, task.Key, ResultPaths.FinalCode(model, strategy, mechanism)) is { } code)
                    {
                        suites.Add(Suite($"{model}__{strategy}__{mechanism}", candidate, code));
                    }
                }
            }
        }

        return suites;
    }

    private static SuiteToMeasure Suite(string id, CandidateTest candidate, string code) => new()
    {
        SuiteId = id,
        System = "ARROW",
        Files = new SuiteFiles { SuiteId = id, TestFqn = candidate.TestFqn, Files = new() { [candidate.TestPath] = code } },
    };

    private async Task<SuiteToMeasure?> EvoSuiteAsync(TaskContext task)
    {
        counters.Planned(4);
        if (planner.ShouldRun(task.Key, JobIds.EvoSuite))
        {
            await gate.WaitAsync().ConfigureAwait(false);
            results.Delete(RunId, task.Key, ResultPaths.EvoSuiteDir);
            BlockOutcome outcome;
            string message;
            try
            {
                var comparison = await Track(JobIds.EvoSuite, task.Key, () => blocks.EvoSuite!.GenerateAsync(task, gate.Token)).ConfigureAwait(false);
                outcome = new BlockOutcome { Outcome = OutcomeKind.Ok, Code = comparison.Code, Message = comparison.Status + (comparison.Reason is null ? "" : ": " + comparison.Reason) };
                message = outcome.Message!;
                if (comparison is { Status: "OK", Suite: { } suite })
                {
                    var verify = await Track(JobIds.EvoSuite, task.Key, () => blocks.Verifier.VerifyAsync(suite, task, VerifyOptions(ResultPaths.EvoSuiteDir), gate.Token)).ConfigureAwait(false);
                    results.SaveJson(RunId, task.Key, ResultPaths.Join(ResultPaths.EvoSuiteDir, ResultPaths.Verify), verify);
                    message = $"bộ EvoSuite {suite.Files.Count} file → {verify.State.ToString().ToUpperInvariant()}";
                    outcome = verify.State == VerificationState.Infra
                        ? new BlockOutcome { Outcome = OutcomeKind.Infra, Code = verify.InfraReason?.Split(':')[0], Message = verify.InfraReason, NeedsRerun = true }
                        : outcome with { Message = message };
                }
            }
            catch (ArrowException ex)
            {
                outcome = new BlockOutcome { Outcome = OutcomeKind.Infra, Code = ex.Code, Message = ex.Message, NeedsRerun = true };
                message = $"INFRA {ex.Code}: {ex.Message}";
            }

            Ledger(task.Key, 4, JobIds.EvoSuite, outcome);
            Event(4, task.Key.TaskId, JobIds.EvoSuite, outcome.Outcome == OutcomeKind.Ok ? EventLevel.Info : EventLevel.Warn, "EvoSuite: " + message);
        }
        else
        {
            Skipped(4, task.Key, JobIds.EvoSuite);
        }

        var generated = results.LoadJson<ComparisonSuite>(RunId, task.Key, ResultPaths.Join(ResultPaths.EvoSuiteDir, "comparison.json"));
        var verified = results.LoadJson<VerifyResult>(RunId, task.Key, ResultPaths.Join(ResultPaths.EvoSuiteDir, ResultPaths.Verify));
        return generated?.Suite is { } files && verified?.State == VerificationState.Valid
            ? new SuiteToMeasure { SuiteId = "evosuite", System = "EvoSuite", Files = files with { SuiteId = "evosuite" } }
            : null;
    }

    private async Task MeasureAsync(TaskContext task, SuiteToMeasure suite)
    {
        var job = JobIds.Measure(suite.SuiteId);
        counters.Planned(4);
        if (!planner.ShouldRun(task.Key, job))
        {
            Skipped(4, task.Key, job);
            return;
        }

        await gate.WaitAsync().ConfigureAwait(false);
        var dir = ResultPaths.SuiteDir(suite.SuiteId);
        results.Delete(RunId, task.Key, dir);
        var options = new MeasureOptions
        {
            ResultDir = dir, StepTimeoutSeconds = Experiment.Verification.StepTimeoutSeconds, InfraAttempts = Experiment.Verification.InfraAttempts,
        };
        var quality = await Track(job, task.Key, () => blocks.Assessor.MeasureAsync(suite, task, Experiment.Assessment.Tools, options, gate.Token)).ConfigureAwait(false);
        var infra = quality.Tools.Where(t => t.Value.NeedsRerun).ToList();
        Ledger(task.Key, 4, job, infra.Count > 0
            ? new BlockOutcome { Outcome = OutcomeKind.Infra, Code = infra[0].Value.Code, Message = string.Join("; ", infra.Select(t => $"{t.Key}: {t.Value.Message}")), NeedsRerun = true }
            : new BlockOutcome { Outcome = OutcomeKind.Ok, Message = string.Join(", ", quality.Tools.Select(t => $"{t.Key} {t.Value.Outcome.ToString().ToUpperInvariant()}")) });
        Event(4, task.Key.TaskId, job, infra.Count > 0 ? EventLevel.Warn : EventLevel.Info,
            $"{suite.SuiteId}: " + string.Join(" · ", quality.Tools.Select(t => $"{t.Key} {(t.Value.Outcome == OutcomeKind.Ok ? "OK" : t.Value.Code)}")));
    }

    // ── tiện ích ───────────────────────────────────────────────────────

    private RunContext RunContext() => new()
    {
        RunId = RunId,
        Verification = Experiment.Verification,
        Screening = Experiment.Screening,
        GeneratedTestSuffix = Experiment.GeneratedTestSuffix,
        Tools = setup.Tools,
        KeepWorkspace = true,
    };

    private VerifyOptions VerifyOptions(string resultDir) => new()
    {
        ResultDir = resultDir,
        StepTimeoutSeconds = Experiment.Verification.StepTimeoutSeconds,
        InfraAttempts = Experiment.Verification.InfraAttempts,
    };

    /// <summary>Mã CUT nguyên văn: từ checkout gốc, không có thì từ snapshot.</summary>
    private string ReadCut(TaskContext task)
    {
        foreach (var root in new[] { task.Workspace.BaseCheckout, task.Workspace.CompiledSnapshot })
        {
            var path = Path.Combine(setup.WorkDir, root.Replace('/', Path.DirectorySeparatorChar), task.Metadata.Paths.CutPath.Replace('/', Path.DirectorySeparatorChar));
            if (File.Exists(path))
            {
                return File.ReadAllText(path);
            }
        }

        throw new ArrowException(ErrorCodes.B1Workspace, $"Không đọc được mã CUT {task.Metadata.Paths.CutPath} (workspace của task đã bị xóa?)");
    }

    /// <summary>
    /// Lỗi cấu hình model (<c>B2_LLM_CONFIG</c>, <c>B2_WRONG_MODEL</c>) → tạm dừng run, chờ người dùng sửa rồi gõ <c>r</c>, gọi lại;
    /// vượt <c>--max-llm-calls</c> → dừng run (spec 02 Mục 6).
    /// </summary>
    private async Task<T> WithModelAsync<T>(Func<Task<T>> call)
    {
        while (true)
        {
            await gate.WaitAsync().ConfigureAwait(false);
            try
            {
                return await call().ConfigureAwait(false);
            }
            catch (ArrowException ex) when (ex.Code is ErrorCodes.B2LlmConfig or ErrorCodes.B2WrongModel)
            {
                gate.Pause($"{ex.Code}: {ex.Message}");
                Event(2, null, null, EventLevel.Error, $"TẠM DỪNG: {ex.Code}: {ex.Message}. Sửa bằng arrow models ở terminal khác rồi gõ r");
            }
            catch (ArrowException ex) when (ex.Code == ErrorCodes.B2CallLimit)
            {
                Event(2, null, null, EventLevel.Error, $"DỪNG: {ex.Message}");
                gate.Stop();
                gate.Token.ThrowIfCancellationRequested();
                throw;
            }
        }
    }

    private async Task<T> Track<T>(string job, TaskKey key, Func<Task<T>> work)
    {
        var name = $"{key.TaskId}:{job}";
        counters.Start(name);
        try
        {
            return await work().ConfigureAwait(false);
        }
        finally
        {
            counters.End(name);
        }
    }

    /// <summary>Job không chạy lần này: đã xong từ trước (có dòng ledger) thì tính là xong; chưa có thì vẫn là chưa xong.</summary>
    private void Skipped(int block, TaskKey key, string job)
    {
        if (planner.Latest(key, job) is { } entry)
        {
            counters.Finished(block, entry.Outcome);
        }
    }

    private void Ledger(TaskKey key, int block, string job, BlockOutcome outcome)
    {
        var entry = new LedgerEntry
        {
            At = DateTimeOffset.Now, Block = block, Dataset = key.Dataset, TaskId = key.TaskId, Job = job,
            Outcome = outcome.Outcome, Code = outcome.Code, Message = outcome.Message, NeedsRerun = outcome.NeedsRerun,
        };
        runs.AppendLedger(RunId, entry);
        planner.Record(entry);
        counters.Finished(block, outcome.Outcome);
    }

    private void Event(int block, string? taskId, string? job, EventLevel level, string message)
    {
        var runEvent = new RunEvent { At = DateTimeOffset.Now, Level = level, Block = block, TaskId = taskId, Job = job, Message = message };
        runs.AppendEvent(RunId, runEvent);
        progress.Report(runEvent);
    }

    private static string DescribeBlock1(RepositoryAnalysis analysis)
    {
        if (analysis.Outcome.Outcome != OutcomeKind.Ok || analysis.Metadata is not { } m)
        {
            return $"{analysis.Outcome.Outcome.ToString().ToUpperInvariant()} {analysis.Outcome.Code} {analysis.Outcome.Message}";
        }

        var framework = m.TestFramework.Name switch { "junit5" => "JUnit 5", "testng" => "TestNG", _ => "JUnit 4" };
        var passing = analysis.Baseline?.PassingInAllRuns.Count ?? 0;
        var paired = analysis.Baseline?.PairedTest.Status.ToString().ToUpperInvariant() ?? "-";
        return string.Create(CultureInfo.InvariantCulture,
            $"OK · {(m.BuildTool.Kind == "gradle" ? "Gradle" : "Maven")} · Java {m.Java.Major ?? m.Java.JdkMajor} (JDK {m.Java.JdkMajor}) · {framework} · baseline {passing} test · tiêu chí B {paired}");
    }

    private static string RoundText(RoundDecision decision) =>
        decision.InvalidOutput ? "output không hợp lệ"
        : decision.DuplicateCode ? "mã trùng"
        : $"{decision.State?.ToString().ToUpperInvariant()}{(decision.Comparison is null ? "" : " · " + decision.Comparison switch { "BETTER" => "tốt hơn", "WORSE" => "tệ hơn", _ => "ngang" })}{(decision.Rollback ? " · rollback" : "")} · checkpoint={decision.CheckpointRound}";
}
