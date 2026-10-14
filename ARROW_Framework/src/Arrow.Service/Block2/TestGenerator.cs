using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block2;

/// <summary>
/// Khối 2 (spec 02): dựng prompt → gọi model qua <see cref="ILlmClient"/> → tách mã, kiểm tra cú pháp, chuẩn hóa tên, ghép method (mức 3),
/// tính hash → ghi <c>prompt.json</c>, <c>response.json</c>, <c>usage.json</c>, <c>candidate.java</c>, <c>candidate.json</c> vào
/// <see cref="GenerationContext.ResultDir"/>. Lỗi gọi model (<c>B2_*</c>) ném ra cho nơi gọi; output không dùng được thì trả candidate
/// <c>valid_output = false</c>.
/// </summary>
public sealed class TestGenerator(IPromptRepository prompts, ILlmClient llm, IJavaHelper javaHelper, ITaskResultRepository results) : ITestGenerator
{
    /// <summary>CUT dài hơn số ký tự này thì bị rút gọn (spec 02 Mục 2.1).</summary>
    public const int MaxCutChars = 48_000;

    public const string NoCode = "no_code";
    public const string ParseError = "parse_error";
    public const string MultipleClasses = "multiple_classes";

    public static string GenerateCallId(string taskId, string model, string strategy) => $"gen/{taskId}/{model}/{strategy}";

    public static string RepairCallId(string taskId, string model, string strategy, string mechanism, int round) =>
        $"repair/{taskId}/{model}/{strategy}/{mechanism}/round_{round}";

    public async Task<GenerationResult> GenerateAsync(GenerationContext context, CancellationToken cancellationToken)
    {
        var (systemPath, userPath) = PromptFiles.ForStrategy(context.Strategy);
        var cut = await PrepareCutAsync(context.CutSource, cancellationToken).ConfigureAwait(false);
        var built = PromptBuilder.Generation(context.Strategy, prompts.Load(systemPath), prompts.Load(userPath), context.Metadata, cut);
        var callId = GenerateCallId(context.Key.TaskId, context.Model.Name, context.Strategy);
        return await CallAndNormalizeAsync(context, callId, built, cut, "GENERATE", null, cancellationToken).ConfigureAwait(false);
    }

    public async Task<GenerationResult> RepairAsync(GenerationContext context, RepairRequest request, CancellationToken cancellationToken)
    {
        var cut = await PrepareCutAsync(context.CutSource, cancellationToken).ConfigureAwait(false);
        var built = PromptBuilder.Repair(
            request,
            prompts.Load(PromptFiles.RepairSystem),
            prompts.Load(PromptBuilder.RepairUserTemplate(request.Level)),
            prompts.Load(PromptFiles.FailureSummaries),
            context.Metadata,
            cut);
        var callId = RepairCallId(context.Key.TaskId, context.Model.Name, context.Strategy, request.Mechanism, request.Round);
        return await CallAndNormalizeAsync(context, callId, built, cut, "REPAIR", request, cancellationToken).ConfigureAwait(false);
    }

    /// <summary>Mã CUT cho prompt: nguyên văn, hoặc rút gọn bằng java-helper khi dài hơn <see cref="MaxCutChars"/>.</summary>
    private async Task<CutSource> PrepareCutAsync(string source, CancellationToken cancellationToken)
    {
        var text = source.Replace("\r\n", "\n");
        if (text.Length <= MaxCutChars)
        {
            return new CutSource(text);
        }

        var shrunk = await javaHelper.ShrinkSourceAsync(text, MaxCutChars, cancellationToken).ConfigureAwait(false);
        return new CutSource(shrunk.Code, shrunk.Truncation);
    }

    private async Task<GenerationResult> CallAndNormalizeAsync(
        GenerationContext context, string callId, BuiltPrompt built, CutSource cut, string mode, RepairRequest? repair, CancellationToken cancellationToken)
    {
        var request = new LlmRequest { CallId = callId, Model = context.Model.Name, Messages = [.. built.Messages], Params = context.Model.Params };
        var record = new PromptRecord
        {
            CallId = callId, Messages = request.Messages, Params = request.Params, Templates = [.. built.Templates], SourceTruncation = cut.Truncation,
        };
        Save(context, ResultPaths.Prompt, record);

        var response = await llm.CompleteAsync(request, cancellationToken).ConfigureAwait(false);
        results.SaveText(context.RunId, context.Key, Path(context, ResultPaths.Response), response.RawJson.TrimEnd('\n') + "\n");
        Save(context, ResultPaths.Usage, response.Usage);

        var candidate = await NormalizeAsync(response.Text, context.Metadata, repair, cancellationToken).ConfigureAwait(false) with
        {
            TaskId = context.Key.TaskId,
            Model = context.Model.Name,
            Strategy = context.Strategy,
            Mode = mode,
            FinishReason = response.Usage.FinishReason,
        };
        if (candidate.Code is not null)
        {
            results.SaveText(context.RunId, context.Key, Path(context, ResultPaths.CandidateCode), candidate.Code);
        }

        Save(context, ResultPaths.Candidate, candidate);
        return new GenerationResult(candidate, response.Usage, record);
    }

    /// <summary>
    /// Spec 02 Mục 4.5: tách mã (bước 1), kiểm tra cú pháp và đúng một class top-level (bước 2), ghép method khi sửa mức 3 (bước 4),
    /// bỏ import lặp, đặt package và tên class (bước 3), tính hash, số test method, số assertion (bước 5).
    /// </summary>
    public async Task<CandidateTest> NormalizeAsync(string responseText, RepositoryMetadata metadata, RepairRequest? repair, CancellationToken cancellationToken)
    {
        var testFqn = metadata.Paths.GeneratedTestFqn;
        var invalid = new CandidateTest { TestFqn = testFqn, TestPath = metadata.Paths.GeneratedTestPath, ValidOutput = false };
        var extracted = CodeExtractor.Extract(responseText);
        var actions = extracted.Normalization.ToList();
        if (extracted.Code is null)
        {
            return invalid with { InvalidReason = NoCode, Normalization = actions };
        }

        var parsed = await javaHelper.ParseAsync(extracted.Code, cancellationToken).ConfigureAwait(false);
        if (!parsed.Parsed)
        {
            return invalid with { InvalidReason = ParseError, Normalization = actions };
        }

        if (parsed.TopLevelTypes.Count != 1)
        {
            return invalid with { InvalidReason = parsed.TopLevelTypes.Count == 0 ? NoCode : MultipleClasses, Normalization = actions };
        }

        var code = extracted.Code;
        var dropped = new List<string>();
        if (repair is { RewriteMethods.Count: > 0 })
        {
            var splice = await javaHelper.SpliceMethodsAsync(repair.BaseCode, code, repair.RewriteMethods, cancellationToken).ConfigureAwait(false);
            code = splice.Code;
            dropped.AddRange(splice.Dropped);
            actions.Add($"splice: replaced {Names(splice.Replaced)}; dropped {Names(splice.Dropped)}");
        }

        var (deduplicated, importActions) = CodeExtractor.RemoveDuplicateImports(code);
        actions.AddRange(importActions);
        var renamed = await javaHelper.RenameClassAsync(deduplicated, PromptBuilder.TestClassName(metadata), metadata.Cut.Package, cancellationToken).ConfigureAwait(false);
        actions.AddRange(renamed.Actions);
        code = renamed.Code.Replace("\r\n", "\n").TrimEnd('\n') + "\n";

        var hash = await javaHelper.StripCommentsHashAsync(code, cancellationToken).ConfigureAwait(false);
        var tests = await javaHelper.ListTestMethodsAsync(code, cancellationToken).ConfigureAwait(false);
        var assertions = await javaHelper.CountAssertionsAsync(code, cancellationToken).ConfigureAwait(false);
        return new CandidateTest
        {
            ValidOutput = true,
            TestFqn = testFqn,
            TestPath = metadata.Paths.GeneratedTestPath,
            Code = code,
            CodeHash = hash.CodeHash,
            TestMethodCount = tests.Methods.Count,
            AssertionCount = assertions.Total ?? 0,
            Normalization = actions,
            DroppedMethods = dropped,
        };
    }

    private static string Names(IReadOnlyList<string> names) => names.Count == 0 ? "-" : string.Join(", ", names);

    private static string Path(GenerationContext context, string file) => ResultPaths.Join(context.ResultDir, file);

    private void Save<T>(GenerationContext context, string file, T value) => results.SaveJson(context.RunId, context.Key, Path(context, file), value);
}
