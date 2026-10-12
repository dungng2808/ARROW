using System.Collections.Concurrent;
using Arrow.Core.Config;
using Arrow.Core.Llm;
using Arrow.Core.Prompts;
using Arrow.Core.Repair;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Java;
using Arrow.Infrastructure.Verify;

namespace Arrow.Infrastructure.Repair;

/// <summary><c>repair/…/excluded.json</c>: lý do c0 không vào bước sửa (specs/05 Mục 2.1).</summary>
public sealed record RepairExclusion(string Reason, string Detail);

/// <summary>Trạng thái một lần chạy nhánh (<c>&lt;attempt_id&gt;/attempt.json</c>).</summary>
public sealed record RepairAttempt
{
    public string AttemptId { get; init; } = "";
    public string Status { get; init; } = "running";
    public string? Reason { get; init; }
    public DateTimeOffset StartedAt { get; init; }
}

/// <summary>
/// S2: một nhánh (ứng viên × cơ chế) chạy tới <c>n_max</c> vòng, không dừng sớm (specs/05). Mỗi vòng là một dòng ledger
/// (<c>stage=repair</c>, <c>round=t</c>); trạng thái nhánh dựng lại từ <c>decision.json</c> nên chạy tiếp được. Lỗi hạ tầng
/// dừng nhánh, lượt sau chạy lại từ đầu với <c>attempt_id</c> mới.
/// </summary>
public sealed class RepairService(ExperimentConfig experiment, PromptLibrary prompts, ILlmClient llm, GitRepositoryProvider git,
    CandidateRunner candidates, JavaAnalyzer analyzer)
{
    private static readonly ConcurrentDictionary<string, SemaphoreSlim> C0Locks = new();

    public static string CandidateDir(string taskDir, string model, string strategy) => Path.Combine(taskDir, "repair", model, strategy);

    /// <summary>c0 dựng lại và build lại (dùng chung hai cơ chế); null nếu bị loại (đã ghi excluded.json).</summary>
    private async Task<(BuiltCandidate? C0, VerifyResult? Verify, StageOutcome? Stop)> PrepareC0Async(RunStore store, PreparedTaskData data, JobKey job,
        Action<string> reportStep, CancellationToken cancellationToken)
    {
        var candidateDir = CandidateDir(data.TaskDir, job.Model!, job.Strategy!);
        Directory.CreateDirectory(candidateDir);
        var excludedPath = Path.Combine(candidateDir, "excluded.json");
        StageOutcome Excluded(string reason, string detail)
        {
            Json.WriteAtomic(excludedPath, new RepairExclusion(reason, detail));
            return new StageOutcome { Status = JobStatus.Skipped, Outcome = "EXCLUDED", Reason = reason, Message = detail };
        }

        if (File.Exists(excludedPath))
        {
            var exclusion = Json.Read<RepairExclusion>(excludedPath);
            return (null, null, new StageOutcome { Status = JobStatus.Skipped, Outcome = "EXCLUDED", Reason = exclusion.Reason, Message = exclusion.Detail });
        }

        var genDir = GenerateService.Dir(data.TaskDir, job.Model!, job.Strategy!);
        var genVerifyPath = Path.Combine(genDir, "verify_0.json");
        if (!File.Exists(genVerifyPath))
        {
            return (null, null, Excluded("NO_GENERATION", "chưa có kết quả bước sinh test"));
        }

        var genVerify = Json.Read<VerifyResult>(genVerifyPath);
        if (genVerify.InvalidOutput)
        {
            return (null, null, Excluded("INVALID_OUTPUT", $"output không hợp lệ ở bước sinh: {genVerify.InvalidReason}"));
        }

        if (genVerify.State == VerificationState.Infra)
        {
            return (null, null, Excluded("PIPELINE_ERROR", $"lỗi hạ tầng ở bước sinh chưa giải quyết: {genVerify.InfraReason}"));
        }

        var code = await File.ReadAllTextAsync(Path.Combine(genDir, "cand_0.java"), cancellationToken).ConfigureAwait(false);
        var c0Dir = Path.Combine(candidateDir, "c0");
        var c0VerifyPath = Path.Combine(c0Dir, "verify.json");
        var gate = C0Locks.GetOrAdd(c0Dir, _ => new SemaphoreSlim(1, 1));
        await gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            if (!File.Exists(c0VerifyPath))
            {
                reportStep("c0");
                var rebuilt = await candidates.VerifyAsync(data.Task, data.Baseline, store.RunId, job.ShortHash + "c0", data.TaskDir, new CandidateSpec
                {
                    Code = code,
                    TestPath = data.Task.GeneratedTestPath,
                    TestClassFqn = data.Task.GeneratedTestClassFqn,
                    LogKey = $"repair-c0-{job.Model}-{job.Strategy}",
                }, reportStep, cancellationToken).ConfigureAwait(false);
                if (rebuilt.State == VerificationState.Infra)
                {
                    return (null, null, new StageOutcome { Status = JobStatus.Infra, Outcome = "INFRA", Reason = rebuilt.InfraReason, Message = "build lại c0 lỗi hạ tầng" });
                }

                Directory.CreateDirectory(c0Dir);
                await File.WriteAllTextAsync(Path.Combine(c0Dir, "cand.java"), code, cancellationToken).ConfigureAwait(false);
                Json.WriteAtomic(c0VerifyPath, rebuilt);
            }
        }
        finally
        {
            gate.Release();
        }

        var c0Verify = Json.Read<VerifyResult>(c0VerifyPath);
        if (c0Verify.State == VerificationState.Valid)
        {
            return (null, null, Excluded("C0_VALID", "c0 build lại đạt VALID"));
        }

        return (BuiltCandidate.From(0, code, c0Verify), c0Verify, null);
    }

    public async Task<StageOutcome> RunBranchAsync(RunStore store, JobKey job, string runOrder, Action<string> reportStep, CancellationToken cancellationToken)
    {
        var data = PreparedTaskData.Load(store, job.Dataset, job.TaskId);
        var (c0, c0Verify, stop) = await PrepareC0Async(store, data, job, reportStep, cancellationToken).ConfigureAwait(false);
        if (stop is not null)
        {
            return stop;
        }

        var mechanism = job.Mechanism!;
        var mechanismDir = Path.Combine(CandidateDir(data.TaskDir, job.Model!, job.Strategy!), mechanism);
        var (attemptId, attemptNumber) = ChooseAttempt(mechanismDir);
        var attemptDir = Path.Combine(mechanismDir, attemptId);
        Directory.CreateDirectory(attemptDir);
        var attemptPath = Path.Combine(attemptDir, "attempt.json");
        if (!File.Exists(attemptPath))
        {
            Json.WriteAtomic(attemptPath, new RepairAttempt { AttemptId = attemptId, StartedAt = DateTimeOffset.UtcNow });
        }

        // Dựng lại nhánh từ các vòng đã ghi.
        var decisions = new List<RepairDecision>();
        var verifies = new Dictionary<int, VerifyResult> { [0] = c0Verify! };
        for (var t = 1; File.Exists(Path.Combine(attemptDir, $"round_{t}", "decision.json")); t++)
        {
            var decision = Json.Read<RepairDecision>(Path.Combine(attemptDir, $"round_{t}", "decision.json"));
            decisions.Add(decision);
            var verifyPath = Path.Combine(attemptDir, $"round_{t}", "verify.json");
            if (File.Exists(verifyPath))
            {
                verifies[t] = Json.Read<VerifyResult>(verifyPath);
            }
        }

        var policy = RepairReplay.Create(mechanism, c0!, decisions.Select(d => d.Event!));
        var model = experiment.Models.First(m => m.Name == job.Model);
        var cutSource = (await git.ReadFileAsync(data.Task.RepoUrl, data.Task.CheckoutSha, data.Task.ClassPath, cancellationToken).ConfigureAwait(false)).TrimEnd();
        var contextBuilder = new RepairContextBuilder(git, analyzer);
        var taskRel = Path.GetRelativePath(store.Root, data.TaskDir).Replace('\\', '/');
        var nMax = experiment.Repair.NMax;

        for (var t = decisions.Count + 1; t <= nMax && !policy.Succeeded; t++)
        {
            reportStep($"vòng {t}");
            var plan = policy.NextPrompt();
            var roundDir = Path.Combine(attemptDir, $"round_{t}");
            var level2 = plan.Level == 2
                ? await contextBuilder.BuildAsync(data, cutSource, plan.Base, verifies[plan.Base.Round], cancellationToken).ConfigureAwait(false)
                : "";
            var messages = RepairPrompts.Messages(prompts, new RepairPromptInput
            {
                Plan = plan,
                BaseVerify = verifies[plan.Base.Round],
                TestClassName = data.TestClassName,
                TestPackage = data.TestPackage,
                CutPath = data.Task.ClassPath,
                CutSource = cutSource,
                Level2Block = level2,
            });
            var request = new LlmRequest { Model = model.Name, Messages = messages, Params = model.Params.ToDictionary(p => p.Key, p => (object?)p.Value) };

            LlmResponse response;
            Core.Generation.NormalizationResult normalization;
            try
            {
                (response, normalization) = await LlmCall.RunAsync(llm, request, roundDir, data, cancellationToken).ConfigureAwait(false);
            }
            catch (LlmException ex)
            {
                Json.WriteAtomic(attemptPath, new RepairAttempt { AttemptId = attemptId, Status = "infra", Reason = ex.Reason, StartedAt = DateTimeOffset.UtcNow });
                return new StageOutcome { Status = JobStatus.Infra, Outcome = "INFRA", Reason = ex.Reason, Message = $"vòng {t}: {ex.Message}", PauseRun = ex.PausesRun };
            }

            RoundEvent roundEvent;
            VerifyResult? verify = null;
            var replaced = new List<string>();
            var dropped = new List<string>();
            string? code = normalization.Valid ? normalization.Code : null;
            if (code is not null && plan.Level == 3)
            {
                var splice = await analyzer.SpliceAsync(plan.Base.Code, code, plan.FailingMethods, cancellationToken).ConfigureAwait(false);
                if (splice.Error is not null)
                {
                    Json.WriteAtomic(Path.Combine(roundDir, "splice.json"), splice);
                    code = null;
                }
                else
                {
                    code = splice.Code;
                    replaced = splice.Replaced;
                    dropped = splice.Dropped;
                    Json.WriteAtomic(Path.Combine(roundDir, "splice.json"), splice with { Code = "" });
                }
            }

            if (code is null)
            {
                roundEvent = new RoundEvent { Round = t, InvalidOutput = true };
            }
            else
            {
                var analysis = await analyzer.AnalyzeTestAsync(code, [], cancellationToken).ConfigureAwait(false);
                var hash = CodeHasher.Hash(analysis.Stripped);
                await File.WriteAllTextAsync(Path.Combine(roundDir, "cand.java"), code, cancellationToken).ConfigureAwait(false);
                if (policy.SkipsDuplicates && policy.IsDuplicate(hash))
                {
                    File.Delete(Path.Combine(roundDir, "cand.java"));
                    roundEvent = new RoundEvent { Round = t, DuplicateCode = true, CodeHash = hash };
                }
                else
                {
                    verify = await candidates.VerifyAsync(data.Task, data.Baseline, store.RunId, $"{job.ShortHash}{attemptId}r{t}", data.TaskDir, new CandidateSpec
                    {
                        Code = code,
                        TestPath = data.Task.GeneratedTestPath,
                        TestClassFqn = data.Task.GeneratedTestClassFqn,
                        LogKey = $"repair-{mechanism}-{job.Model}-{job.Strategy}-{attemptId}-r{t}",
                    }, reportStep, cancellationToken).ConfigureAwait(false);
                    if (verify.State == VerificationState.Infra)
                    {
                        Json.WriteAtomic(Path.Combine(roundDir, "verify.json"), verify);
                        Json.WriteAtomic(attemptPath, new RepairAttempt { AttemptId = attemptId, Status = "infra", Reason = verify.InfraReason, StartedAt = DateTimeOffset.UtcNow });
                        return new StageOutcome { Status = JobStatus.Infra, Outcome = "INFRA", Reason = verify.InfraReason, Message = $"vòng {t}: lỗi hạ tầng khi build" };
                    }

                    Json.WriteAtomic(Path.Combine(roundDir, "verify.json"), verify);
                    verifies[t] = verify;
                    roundEvent = new RoundEvent { Round = t, Built = BuiltCandidate.From(t, code, verify), CodeHash = verify.CodeHash };
                }
            }

            var decision = policy.Apply(roundEvent, plan) with
            {
                AttemptId = attemptId,
                RunOrder = runOrder,
                Level3Replaced = replaced,
                Level3Dropped = dropped,
                LlmSeconds = response.Usage.LatencySeconds,
                BuildSeconds = Math.Round(verify?.TotalBuildSeconds ?? 0, 2),
                LlmRetries = response.Retries,
                LlmTokens = new Dictionary<string, long>
                {
                    ["prompt"] = response.Usage.CacheHit ? 0 : response.Usage.PromptTokens,
                    ["completion"] = response.Usage.CacheHit ? 0 : response.Usage.CompletionTokens,
                },
            };
            Json.WriteAtomic(Path.Combine(roundDir, "decision.json"), decision);
            decisions.Add(decision);

            var outcome = decision.InvalidOutput ? "INVALID_OUTPUT" : decision.DuplicateCode ? "DUPLICATE_CODE" : States.Name(decision.State!.Value);
            var roundJob = job with { Round = t };
            var relative = Path.GetRelativePath(store.Root, roundDir).Replace('\\', '/');
            store.AppendLedger(new LedgerEntry
            {
                Job = roundJob,
                Status = JobStatus.Done,
                Outcome = outcome,
                StartedAt = DateTimeOffset.UtcNow,
                EndedAt = DateTimeOffset.UtcNow,
                LlmSeconds = decision.LlmSeconds,
                BuildSeconds = decision.BuildSeconds,
                Attempt = attemptNumber,
                Outputs = new[] { "prompt.json", "response.json", "usage.json", "normalization.json", "cand.java", "verify.json", "decision.json" }
                    .Where(f => File.Exists(Path.Combine(roundDir, f))).Select(f => $"{relative}/{f}").ToList(),
            });
            store.AppendEvent(new RunEvent
            {
                At = DateTimeOffset.UtcNow,
                Stage = "repair",
                TaskId = job.TaskId,
                Strategy = job.Strategy,
                Outcome = $"{mechanism} r{t} {outcome}",
                Message = $"mức {decision.Level}{(decision.Rollback ? ", rollback" : "")}{(decision.Improved ? ", checkpoint mới" : "")} · {decision.ErrorSignature ?? ""}",
                LlmSeconds = decision.LlmSeconds,
                BuildSeconds = decision.BuildSeconds,
            });
        }

        var c0Methods = (await analyzer.AnalyzeTestAsync(c0!.Code, [], cancellationToken).ConfigureAwait(false)).TestMethods;
        var finalMethods = (await analyzer.AnalyzeTestAsync(policy.Result.Code, [], cancellationToken).ConfigureAwait(false)).TestMethods;
        var summary = RepairSummary.From(attemptId, mechanism, runOrder, c0, decisions, nMax, policy.Result.State, c0Methods, finalMethods);
        Json.WriteAtomic(Path.Combine(mechanismDir, "summary.json"), summary);
        Json.WriteAtomic(attemptPath, new RepairAttempt { AttemptId = attemptId, Status = "done", StartedAt = DateTimeOffset.UtcNow });
        var mechanismRel = Path.GetRelativePath(store.Root, mechanismDir).Replace('\\', '/');
        return new StageOutcome
        {
            Outcome = summary.Success ? "VALID" : States.Name(summary.FinalState ?? c0.State),
            Message = summary.Success ? $"VALID ở vòng {summary.SuccessRound}" : $"chưa VALID sau {summary.RoundsUsed} vòng",
            LlmSeconds = decisions.Sum(d => d.LlmSeconds),
            BuildSeconds = decisions.Sum(d => d.BuildSeconds),
            PromptTokens = summary.PromptTokens,
            CompletionTokens = summary.CompletionTokens,
            Outputs = [$"{mechanismRel}/summary.json"],
        };
    }

    /// <summary>Lần chạy dở (không có lỗi hạ tầng) thì chạy tiếp; lần trước dừng vì lỗi hạ tầng thì mở attempt mới.</summary>
    private static (string Id, int Number) ChooseAttempt(string mechanismDir)
    {
        var existing = Directory.Exists(mechanismDir)
            ? Directory.EnumerateDirectories(mechanismDir, "a*").Select(Path.GetFileName)
                .Select(n => int.TryParse(n![1..], out var number) ? number : 0).Where(n => n > 0).OrderBy(n => n).ToList()
            : [];
        if (existing.Count == 0)
        {
            return ("a1", 1);
        }

        var last = existing[^1];
        var attemptPath = Path.Combine(mechanismDir, $"a{last}", "attempt.json");
        var status = File.Exists(attemptPath) ? Json.Read<RepairAttempt>(attemptPath).Status : "running";
        return status == "infra" ? ($"a{last + 1}", last + 1) : ($"a{last}", last);
    }
}
