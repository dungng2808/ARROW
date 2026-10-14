using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block3;

/// <summary>
/// Một chain sửa (spec 03 Mục 6–8, spec 07 Mục 7.2): mỗi vòng policy tạo <see cref="RepairRequest"/> (mức ≥ 2 thì điền thông tin bổ sung)
/// → khối 2 sửa → Adaptive bỏ qua build khi mã trùng → xác minh trên bản sao mới → chẩn đoán → policy quyết định; ghi
/// <c>request.json</c>, <c>verify.json</c>, <c>decision.json</c> từng vòng; VALID hoặc hết N thì ghi <c>final.java</c>, <c>final.json</c>.
/// Lỗi hạ tầng (xác minh INFRA, <c>B2_LLM_UNAVAILABLE</c>) dừng chain với outcome INFRA.
/// </summary>
public sealed class RepairRunner(
    ITestGenerator generator,
    IVerifier verifier,
    IFailureDiagnoser diagnoser,
    ExtraContextBuilder extraContext,
    IJavaHelper javaHelper,
    IWorkspaceManager workspace,
    ITaskResultRepository results,
    ILogRepository log) : IRepairRunner
{
    public async Task<ChainResult> RunChainAsync(ChainRequest request, CancellationToken cancellationToken)
    {
        var task = request.Task;
        var generation = request.Generation;
        var model = generation.Model.Name;
        var strategy = generation.Strategy;
        var chainDir = ResultPaths.ChainDir(model, strategy, request.Mechanism);
        results.Delete(task.RunId, task.Key, chainDir);

        var c0 = new ChainVersion(0, request.C0, request.C0Verify, diagnoser.Diagnose(request.C0Verify, request.C0, task.Metadata));
        IRepairPolicy policy = request.Mechanism switch
        {
            "adaptive" => new AdaptiveRepairPolicy(c0, request.EarlyStopP),
            "fixed" => new FixedRepairPolicy(c0),
            _ => throw new ArrowException(ErrorCodes.ConfigInvalid, $"Cơ chế sửa không hỗ trợ: {request.Mechanism}"),
        };
        var snapshot = Path.Combine(workspace.WorkDir, task.Workspace.CompiledSnapshot.Replace('/', Path.DirectorySeparatorChar));
        var input = new ExtraContextInput(
            task.Metadata,
            generation.CutSource,
            snapshot,
            ResolveClasspath(snapshot, Lines(results.LoadText(task.RunId, task.Key, ResultPaths.TestClasspath))),
            Lines(results.LoadText(task.RunId, task.Key, ResultPaths.DependenciesResolved)));

        var totals = new Totals();
        ChainVersion? success = null;
        string stopReason;
        var round = 0;
        BlockOutcome outcome = BlockOutcome.Ok();
        while (true)
        {
            if (!policy.CanContinue(round, request.N))
            {
                stopReason = policy.StopReason(round, request.N);
                break;
            }

            round++;
            var roundDir = ResultPaths.RoundDir(model, strategy, request.Mechanism, round);
            var repair = policy.NextRequest(round) with { TaskId = task.Key.TaskId, Model = model, Strategy = strategy };
            if (repair.Level >= 2)
            {
                repair = repair with { ExtraContext = await extraContext.BuildAsync(policy.Current, input, cancellationToken).ConfigureAwait(false) };
            }

            results.SaveJson(task.RunId, task.Key, ResultPaths.Join(roundDir, ResultPaths.Request), repair);

            GenerationResult generated;
            try
            {
                generated = await generator.RepairAsync(generation with { ResultDir = roundDir }, repair, cancellationToken).ConfigureAwait(false);
            }
            catch (ArrowException ex) when (ex.Code == ErrorCodes.B2LlmUnavailable)
            {
                outcome = new BlockOutcome { Outcome = OutcomeKind.Infra, Code = ex.Code, Message = $"vòng {round}: {ex.Message}", NeedsRerun = true };
                stopReason = "infra";
                break;
            }

            totals.Add(generated.Usage);
            var candidate = generated.Candidate;
            var duplicate = candidate.ValidOutput ? policy.DuplicateOf(candidate.CodeHash) : null;
            VerifyResult? verify = null;
            Diagnosis? diagnosis = null;
            if (candidate.ValidOutput && duplicate is null)
            {
                verify = await verifier.VerifyAsync(Suite(candidate, round), task, request.Verify with { ResultDir = roundDir }, cancellationToken).ConfigureAwait(false);
                results.SaveJson(task.RunId, task.Key, ResultPaths.Join(roundDir, ResultPaths.Verify), verify);
                totals.BuildSeconds += verify.BuildSeconds;
                if (verify.State == VerificationState.Infra)
                {
                    outcome = new BlockOutcome { Outcome = OutcomeKind.Infra, Code = verify.InfraReason?.Split(':')[0], Message = $"vòng {round}: {verify.InfraReason}", NeedsRerun = true };
                    stopReason = "infra";
                    break;
                }

                diagnosis = diagnoser.Diagnose(verify, candidate, task.Metadata);
            }

            var decision = policy.Apply(new RoundOutcome { Round = round, Request = repair, Candidate = candidate, DuplicateOf = duplicate, Verify = verify, Diagnosis = diagnosis }) with
            {
                LlmSeconds = generated.Usage.LatencySeconds,
                BuildSeconds = verify?.BuildSeconds ?? 0,
                LlmTokensIn = generated.Usage.PromptTokens,
                LlmTokensOut = generated.Usage.CompletionTokens,
                LlmRetries = Math.Max(0, generated.Usage.Attempts - 1),
                Replayed = generated.Usage.Replayed,
            };
            results.SaveJson(task.RunId, task.Key, ResultPaths.Join(roundDir, ResultPaths.Decision), decision);
            request.OnRound?.Invoke(decision);
            log.Debug($"[K3] {task.Key.TaskId} {model}/{strategy}/{request.Mechanism} vòng {round}: {decision.State?.ToString() ?? (decision.DuplicateCode ? "mã trùng" : "output không hợp lệ")} mức {decision.Level}→{decision.LevelAfter}");

            if (verify?.State == VerificationState.Valid)
            {
                success = new ChainVersion(round, candidate, verify, diagnosis!);
                stopReason = "valid";
                break;
            }
        }

        var final = success ?? policy.Current;
        var droppedMethods = await DroppedMethodsAsync(request.C0.Code, final.Code, cancellationToken).ConfigureAwait(false);
        var suite = new FinalTestSuite
        {
            TaskId = task.Key.TaskId,
            Model = model,
            Strategy = strategy,
            Mechanism = request.Mechanism,
            C0State = request.C0Verify.State,
            Success = success is not null,
            SuccessRound = success?.Round ?? request.N,
            RoundsRun = round,
            StopReason = stopReason,
            FinalState = outcome.Outcome == OutcomeKind.Infra ? VerificationState.Infra : final.State,
            FinalRound = final.Round,
            RepairSeconds = Math.Round(totals.LlmSeconds + totals.BuildSeconds, 1),
            LlmSeconds = Math.Round(totals.LlmSeconds, 1),
            BuildSeconds = Math.Round(totals.BuildSeconds, 1),
            TokensIn = totals.TokensIn,
            TokensOut = totals.TokensOut,
            CostUsd = Math.Round(totals.CostUsd, 6),
            Counters = policy.Counters,
            DroppedMethods = droppedMethods,
            RunOrder = request.RunOrder,
        };
        if (outcome.Outcome == OutcomeKind.Ok)
        {
            results.SaveText(task.RunId, task.Key, ResultPaths.FinalCode(model, strategy, request.Mechanism), final.Code);
        }

        results.SaveJson(task.RunId, task.Key, ResultPaths.Final(model, strategy, request.Mechanism), suite);
        return new ChainResult(outcome, suite, outcome.Outcome == OutcomeKind.Ok ? final.Code : null);
    }

    /// <summary>Bộ test của một candidate: một file ở test path, tên class <c>test_fqn</c>.</summary>
    public static SuiteFiles Suite(CandidateTest candidate, int round) => new()
    {
        SuiteId = round == 0 ? "c0" : $"round_{round}",
        TestFqn = candidate.TestFqn,
        Files = new() { [candidate.TestPath] = candidate.Code ?? "" },
    };

    /// <summary>Test method có trong c0 mà không còn trong bản cuối (spec 03 Mục 8).</summary>
    private async Task<List<string>> DroppedMethodsAsync(string? c0Code, string finalCode, CancellationToken ct)
    {
        if (string.IsNullOrEmpty(c0Code) || finalCode.Length == 0 || c0Code == finalCode)
        {
            return [];
        }

        try
        {
            var before = await javaHelper.ListTestMethodsAsync(c0Code, ct).ConfigureAwait(false);
            var after = await javaHelper.ListTestMethodsAsync(finalCode, ct).ConfigureAwait(false);
            return before.Methods.Except(after.Methods, StringComparer.Ordinal).ToList();
        }
        catch (ArrowException ex)
        {
            log.Warn($"[K3] không đếm được method bị bỏ: {ex.Message}");
            return [];
        }
    }

    /// <summary>
    /// <c>test_classpath.txt</c> ghi thư mục class của module anh em tương đối với gốc workspace (spec 01 Mục 4.4) → đường dẫn tuyệt đối
    /// trong snapshot, để java-helper đọc được API của các class đó; jar ngoài (đường dẫn tuyệt đối) giữ nguyên.
    /// </summary>
    public static List<string> ResolveClasspath(string snapshot, IEnumerable<string> entries) =>
        entries.Select(e => Path.IsPathRooted(e) ? e : Path.GetFullPath(Path.Combine(snapshot, e.Replace('/', Path.DirectorySeparatorChar)))).ToList();

    private static List<string> Lines(string? text) =>
        (text ?? "").Replace("\r\n", "\n").Split('\n', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries).ToList();

    private sealed class Totals
    {
        public double LlmSeconds { get; set; }
        public double BuildSeconds { get; set; }
        public long TokensIn { get; set; }
        public long TokensOut { get; set; }
        public double CostUsd { get; set; }

        public void Add(LlmUsage usage)
        {
            LlmSeconds += usage.LatencySeconds;
            TokensIn += usage.PromptTokens;
            TokensOut += usage.CompletionTokens;
            CostUsd += usage.CostUsd;
        }
    }
}
