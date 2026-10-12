using Arrow.Core.Config;
using Arrow.Core.Generation;
using Arrow.Core.Llm;
using Arrow.Core.Prepare;
using Arrow.Core.Prompts;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Prepare;
using Arrow.Infrastructure.Verify;

namespace Arrow.Infrastructure.Generation;

/// <summary>Dữ liệu S0 của một task (task.json, baseline.json, repo_context.json) và mã CUT.</summary>
public sealed record PreparedTaskData(TaskInfo Task, Baseline Baseline, RepoContext Context, string TaskDir)
{
    public static PreparedTaskData Load(RunStore store, string dataset, string taskId)
    {
        var dir = store.TaskDir(dataset, taskId);
        return new PreparedTaskData(
            Json.Read<TaskInfo>(Path.Combine(dir, PrepareService.TaskFile)),
            Json.Read<Baseline>(Path.Combine(dir, PrepareService.BaselineFile)),
            Json.Read<RepoContext>(Path.Combine(dir, PrepareService.RepoContextFile)),
            dir);
    }

    public string TestPackage => Task.ClassFqn.Contains('.') ? Task.ClassFqn[..Task.ClassFqn.LastIndexOf('.')] : "";
    public string TestClassName => Task.GeneratedTestClassFqn[(Task.GeneratedTestClassFqn.LastIndexOf('.') + 1)..];
    public string CutSimpleName => Task.ClassFqn[(Task.ClassFqn.LastIndexOf('.') + 1)..];
}

/// <summary>Kết quả một job (sinh test hoặc một vòng sửa) cho RunHost.</summary>
public sealed record StageOutcome
{
    public JobStatus Status { get; init; } = JobStatus.Done;
    public string Outcome { get; init; } = "";
    public string? Reason { get; init; }
    public string Message { get; init; } = "";
    public double LlmSeconds { get; init; }
    public double BuildSeconds { get; init; }
    public long PromptTokens { get; init; }
    public long CompletionTokens { get; init; }
    public List<string> Outputs { get; init; } = [];

    /// <summary>Lỗi cấu hình model: runner tạm dừng cả run (specs/04 Mục 1).</summary>
    public bool PauseRun { get; init; }
}

/// <summary>Gọi model và ghi <c>prompt.json</c>, <c>response.json</c>, <c>usage.json</c>, <c>normalization.json</c>.</summary>
public static class LlmCall
{
    public static async Task<(LlmResponse Response, NormalizationResult Normalization)> RunAsync(ILlmClient llm, LlmRequest request, string dir,
        PreparedTaskData data, CancellationToken cancellationToken)
    {
        Directory.CreateDirectory(dir);
        Json.WriteAtomic(Path.Combine(dir, "prompt.json"), new { model = request.Model, @params = request.Params, messages = request.Messages });
        var response = await llm.CompleteAsync(request, cancellationToken).ConfigureAwait(false);
        AtomicFile.WriteAllText(Path.Combine(dir, "response.json"), ToolLog.Redact(response.RawJson) + "\n");
        Json.WriteAtomic(Path.Combine(dir, "usage.json"), response.Usage);
        var normalization = OutputNormalizer.Normalize(response.Text, response.Usage.FinishReason, data.TestPackage, data.TestClassName,
            data.CutSimpleName, data.Task.TestingFramework);
        Json.WriteAtomic(Path.Combine(dir, "normalization.json"), normalization with { Code = null });
        return (response, normalization);
    }

    /// <summary><c>verify.json</c> của output không hợp lệ: COMPILE, không build (specs/04 Mục 3).</summary>
    public static VerifyResult Invalid(NormalizationResult normalization) => new()
    {
        State = VerificationState.Compile,
        InvalidOutput = true,
        InvalidReason = normalization.RejectReason,
        Steps = new VerifySteps { Compile = StepStatus.Fail },
        Group = States.Group(VerificationState.Compile),
        ErrorSignature = "COMPILE | invalid output | -",
    };
}

/// <summary>
/// S1: sinh test cho một (task, model, quy trình) (specs/04 Mục 4, specs/03): dựng prompt, gọi model, kiểm tra và chuẩn hóa,
/// xác minh 5 bước. Output không hợp lệ được ghi là COMPILE, không build, không sinh lại.
/// </summary>
public sealed class GenerateService(ExperimentConfig experiment, PromptLibrary prompts, ILlmClient llm, GitRepositoryProvider git,
    CandidateRunner candidates)
{
    public static string Dir(string taskDir, string model, string strategy) => Path.Combine(taskDir, "gen", model, strategy);

    public async Task<StageOutcome> RunAsync(RunStore store, JobKey job, Action<string> reportStep, CancellationToken cancellationToken)
    {
        var data = PreparedTaskData.Load(store, job.Dataset, job.TaskId);
        var model = experiment.Models.First(m => m.Name == job.Model);
        var dir = Dir(data.TaskDir, model.Name, job.Strategy!);
        var relative = Path.GetRelativePath(data.TaskDir, dir).Replace('\\', '/');
        var taskRel = Path.GetRelativePath(store.Root, data.TaskDir).Replace('\\', '/');

        reportStep("prompt");
        var cutSource = await git.ReadFileAsync(data.Task.RepoUrl, data.Task.CheckoutSha, data.Task.ClassPath, cancellationToken).ConfigureAwait(false);
        var messages = prompts.GenerationMessages(job.Strategy!, new GenerationInput
        {
            TestClassName = data.TestClassName,
            TestPackage = data.TestPackage,
            CutPath = data.Task.ClassPath,
            CutSource = cutSource.TrimEnd(),
            TestFilePath = data.Task.GeneratedTestPath,
            RepoContext = data.Context,
        });
        var request = new LlmRequest { Model = model.Name, Messages = messages, Params = model.Params.ToDictionary(p => p.Key, p => (object?)p.Value) };

        reportStep("llm");
        LlmResponse response;
        NormalizationResult normalization;
        try
        {
            (response, normalization) = await LlmCall.RunAsync(llm, request, dir, data, cancellationToken).ConfigureAwait(false);
        }
        catch (LlmException ex)
        {
            return new StageOutcome { Status = JobStatus.Infra, Outcome = "INFRA", Reason = ex.Reason, Message = ex.Message, PauseRun = ex.PausesRun };
        }

        VerifyResult verify;
        if (!normalization.Valid)
        {
            verify = LlmCall.Invalid(normalization);
        }
        else
        {
            await File.WriteAllTextAsync(Path.Combine(dir, "cand_0.java"), normalization.Code, cancellationToken).ConfigureAwait(false);
            verify = await candidates.VerifyAsync(data.Task, data.Baseline, store.RunId, job.ShortHash, data.TaskDir, new CandidateSpec
            {
                Code = normalization.Code!,
                TestPath = data.Task.GeneratedTestPath,
                TestClassFqn = data.Task.GeneratedTestClassFqn,
                LogKey = $"gen-{model.Name}-{job.Strategy}",
            }, reportStep, cancellationToken).ConfigureAwait(false);
        }

        Json.WriteAtomic(Path.Combine(dir, "verify_0.json"), verify);
        var outputs = new[] { "prompt.json", "response.json", "usage.json", "normalization.json", "cand_0.java", "verify_0.json" }
            .Where(f => File.Exists(Path.Combine(dir, f)))
            .Select(f => $"{taskRel}/{relative}/{f}")
            .ToList();
        var infra = verify.State == VerificationState.Infra;
        return new StageOutcome
        {
            Status = infra ? JobStatus.Infra : JobStatus.Done,
            Outcome = States.Name(verify.State),
            Reason = infra ? verify.InfraReason : null,
            Message = verify.InvalidOutput ? $"output không hợp lệ: {verify.InvalidReason}" : verify.ErrorSignature ?? "VALID",
            LlmSeconds = response.Usage.LatencySeconds,
            BuildSeconds = Math.Round(verify.TotalBuildSeconds, 2),
            PromptTokens = response.Usage.CacheHit ? 0 : response.Usage.PromptTokens,
            CompletionTokens = response.Usage.CacheHit ? 0 : response.Usage.CompletionTokens,
            Outputs = outputs,
        };
    }
}
