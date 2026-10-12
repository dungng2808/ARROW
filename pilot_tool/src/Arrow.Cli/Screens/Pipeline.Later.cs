using Arrow.Core.Manifest;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Repair;

namespace Arrow.Cli.Screens;

public sealed partial class Pipeline
{
    /// <summary>S2 sửa test, S3 EvoSuite, S4 đo chất lượng.</summary>
    public IEnumerable<Func<StageSpec?>> LaterStages(IReadOnlyList<ManifestRecord> tasks, ISet<string> stages, CancellationToken cancellationToken)
    {
        yield return () => stages.Contains("repair") ? RepairStage(tasks, cancellationToken) : null;
        foreach (var stage in QualityStages(tasks, stages, cancellationToken))
        {
            yield return stage;
        }
    }

    // ── S2 ────────────────────────────────────────────────────────────

    /// <summary>Ứng viên của bước sinh chưa VALID (của dataset có <c>repair: true</c>) × cơ chế, theo thứ tự xen kẽ (specs/05 Mục 2.2).</summary>
    public StageSpec? RepairStage(IReadOnlyList<ManifestRecord> tasks, CancellationToken cancellationToken)
    {
        var repairDatasets = _experiment.Manifests.Where(m => m.Repair).Select(m => m.Dataset).ToHashSet();
        if (repairDatasets.Count == 0 || _experiment.Repair.Mechanisms.Count == 0)
        {
            return null;
        }

        var candidates = new List<JobKey>();
        foreach (var task in ReadyTasks(tasks).Where(t => repairDatasets.Contains(t.Dataset)))
        {
            foreach (var model in _experiment.Models)
            {
                foreach (var strategy in _experiment.GenerationStrategies)
                {
                    var verifyPath = Path.Combine(GenerateService.Dir(_store.TaskDir(task.Dataset, task.TaskId), model.Name, strategy), "verify_0.json");
                    if (!File.Exists(verifyPath))
                    {
                        continue;
                    }

                    var state = Json.Read<VerifyResult>(verifyPath).State;
                    if (state is not (VerificationState.Valid or VerificationState.Infra))
                    {
                        candidates.Add(new JobKey { Dataset = task.Dataset, TaskId = task.TaskId, Stage = "repair", Model = model.Name, Strategy = strategy });
                    }
                }
            }
        }

        var ordered = candidates.OrderBy(c => c.TaskId, StringComparer.Ordinal).ThenBy(c => c.Model, StringComparer.Ordinal).ThenBy(c => c.Strategy, StringComparer.Ordinal).ToList();
        var jobs = new List<JobKey>();
        var runOrder = new Dictionary<string, string>();
        var mechanisms = _experiment.Repair.Mechanisms;
        for (var i = 0; i < ordered.Count; i++)
        {
            // Thứ lẻ (1, 3, …): Fixed trước; thứ chẵn: Adaptive trước.
            var order = (i + 1) % 2 == 1 ? mechanisms.OrderBy(m => m == "fixed" ? 0 : 1) : mechanisms.OrderBy(m => m == "adaptive" ? 0 : 1);
            var position = 0;
            foreach (var mechanism in order)
            {
                var job = ordered[i] with { Mechanism = mechanism };
                jobs.Add(job);
                runOrder[job.Canonical] = position++ == 0 ? "first" : "second";
            }
        }

        return new StageSpec("repair", jobs, Math.Max(1, _machine.Workers.Build), "repair",
            async (job, context, ct) =>
            {
                var service = new RepairService(_experiment, _prompts, await LlmAsync(ct).ConfigureAwait(false), _git, _candidates, _analyzer);
                return ToJobResult(await service.RunBranchAsync(_store, job, runOrder[job.Canonical], context.Step, ct).ConfigureAwait(false));
            },
            job => $"{Label(job)} {job.Strategy} {job.Mechanism}");
    }
}
