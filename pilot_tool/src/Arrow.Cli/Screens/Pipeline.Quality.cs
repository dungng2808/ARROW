using Arrow.Core.Manifest;
using Arrow.Core.Repair;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Quality;
using Arrow.Infrastructure.Repair;

namespace Arrow.Cli.Screens;

public sealed partial class Pipeline
{
    /// <summary>S3 EvoSuite, S4 đo chất lượng.</summary>
    private IEnumerable<Func<StageSpec?>> QualityStages(IReadOnlyList<ManifestRecord> tasks, ISet<string> stages, CancellationToken cancellationToken)
    {
        yield return () => stages.Contains("evosuite") ? EvoSuiteStage(tasks) : null;
        yield return () => stages.Contains("quality") ? QualityStage(tasks) : null;
    }

    public StageSpec? EvoSuiteStage(IReadOnlyList<ManifestRecord> tasks)
    {
        if (!_experiment.ComparisonSystems.Any(s => s.Name == "evosuite"))
        {
            return null;
        }

        var service = new EvoSuiteService(_paths, _experiment, _machine, _git, _candidates, _workDir);
        var jobs = ReadyTasks(tasks).Select(t => new JobKey { Dataset = t.Dataset, TaskId = t.TaskId, Stage = "evosuite", Strategy = EvoSuiteService.SystemName }).ToList();
        return new StageSpec("evosuite", jobs, Math.Max(1, _machine.Workers.Build), "evo",
            async (job, context, ct) => ToJobResult(await service.RunAsync(_store, job, context.Step, ct).ConfigureAwait(false)),
            Label);
    }

    /// <summary>Mọi bộ test VALID của một task: ARROW sau sinh, ARROW sau sửa (mỗi cơ chế), EvoSuite.</summary>
    public List<SuiteToMeasure> ValidSuites(PreparedTaskData data)
    {
        var suites = new List<SuiteToMeasure>();
        foreach (var model in _experiment.Models)
        {
            foreach (var strategy in _experiment.GenerationStrategies)
            {
                var genDir = GenerateService.Dir(data.TaskDir, model.Name, strategy);
                var verifyPath = Path.Combine(genDir, "verify_0.json");
                if (File.Exists(verifyPath) && Json.Read<VerifyResult>(verifyPath).State == VerificationState.Valid)
                {
                    suites.Add(Arrow($"arrow__{model.Name}__{strategy}__gen", File.ReadAllText(Path.Combine(genDir, "cand_0.java"))));
                }

                foreach (var mechanism in _experiment.Repair.Mechanisms)
                {
                    var mechanismDir = Path.Combine(RepairService.CandidateDir(data.TaskDir, model.Name, strategy), mechanism);
                    var summaryPath = Path.Combine(mechanismDir, "summary.json");
                    if (!File.Exists(summaryPath))
                    {
                        continue;
                    }

                    var summary = Json.Read<RepairSummary>(summaryPath);
                    var candidate = Path.Combine(mechanismDir, summary.AttemptId, $"round_{summary.SuccessRound}", "cand.java");
                    if (summary.Success && File.Exists(candidate))
                    {
                        suites.Add(Arrow($"arrow__{model.Name}__{strategy}__{mechanism}", File.ReadAllText(candidate)));
                    }
                }
            }
        }

        if (EvoSuiteService.ValidSuite(_paths.ArrowRoot, data) is { } evosuite)
        {
            suites.Add(evosuite);
        }

        return suites;

        SuiteToMeasure Arrow(string system, string code) => new()
        {
            System = system,
            TestClassFqn = data.Task.GeneratedTestClassFqn,
            TestPath = data.Task.GeneratedTestPath,
            Code = code,
        };
    }

    public StageSpec? QualityStage(IReadOnlyList<ManifestRecord> tasks)
    {
        var service = new QualityService(_paths, _experiment, _machine, _git, _analyzer, _workDir);
        var jobs = new List<JobKey>();
        foreach (var task in ReadyTasks(tasks))
        {
            var data = PreparedTaskData.Load(_store, task.Dataset, task.TaskId);
            jobs.AddRange(ValidSuites(data).Select(s => new JobKey { Dataset = task.Dataset, TaskId = task.TaskId, Stage = "quality", Strategy = s.System }));
        }

        return new StageSpec("quality", jobs, Math.Max(1, _machine.Workers.Build), "quality",
            async (job, context, ct) =>
            {
                var data = PreparedTaskData.Load(_store, job.Dataset, job.TaskId);
                var suite = ValidSuites(data).First(s => s.System == job.Strategy);
                return ToJobResult(await service.MeasureAsync(_store, job, suite, context.Step, ct).ConfigureAwait(false));
            },
            job => $"{Label(job)} {job.Strategy}");
    }
}
