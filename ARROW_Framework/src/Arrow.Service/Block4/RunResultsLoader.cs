using Arrow.DataAccess;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Block4;
using Arrow.Models.Common;
using Arrow.Models.Config;

namespace Arrow.Service.Block4;

/// <summary>Đọc kết quả mọi khối của một run (qua Data Access) thành <see cref="RunData"/> cho báo cáo.</summary>
public sealed class RunResultsLoader(IRunRepository runs, ITaskResultRepository results)
{
    public const string ExperimentCopy = "experiment.json";

    /// <summary>Cấu hình thực nghiệm đã chép vào run lúc tạo (<c>runs/&lt;run&gt;/experiment.json</c>).</summary>
    public ExperimentConfig LoadExperiment(string runId)
    {
        var text = runs.ReadText(runId, ExperimentCopy)
                   ?? throw new ArrowException(ErrorCodes.RunMissing, $"Run {runId} không có {ExperimentCopy} (chưa chạy bằng arrow run?)");
        return JsonFile.Deserialize<ExperimentConfig>(text);
    }

    public RunData Load(string runId, ExperimentConfig experiment)
    {
        if (!runs.Exists(runId))
        {
            throw new ArrowException(ErrorCodes.RunMissing, $"Không có run {runId}");
        }

        return new RunData
        {
            RunId = runId,
            Lock = runs.ReadLock(runId),
            Ledger = [.. runs.ReadLedger(runId)],
            Tasks = results.ListTasks(runId).Select(key => LoadTask(runId, key, experiment)).ToList(),
        };
    }

    public TaskData LoadTask(string runId, TaskKey key, ExperimentConfig experiment)
    {
        T? Json<T>(string path) where T : class => results.Exists(runId, key, path) ? results.LoadJson<T>(runId, key, path) : null;

        SuiteData? Suite(string suiteId)
        {
            var dir = ResultPaths.SuiteDir(suiteId);
            var outcome = Json<QualityOutcome>(ResultPaths.Join(dir, "outcome.json"));
            return outcome is null
                ? null
                : new SuiteData
                {
                    SuiteId = suiteId,
                    Outcome = outcome,
                    Coverage = Json<CoverageResult>(ResultPaths.Join(dir, "jacoco.json")),
                    Mutation = Json<MutationResult>(ResultPaths.Join(dir, "pit.json")),
                    Smells = Json<SmellResult>(ResultPaths.Join(dir, "tsdetect.json")),
                };
        }

        var candidates = new List<CandidateData>();
        var chains = new List<ChainData>();
        var suiteIds = new List<string>();
        foreach (var model in experiment.Models.Select(m => m.Name))
        {
            foreach (var strategy in experiment.Strategies)
            {
                var dir = ResultPaths.GenerateDir(model, strategy);
                if (Json<CandidateTest>(ResultPaths.Join(dir, ResultPaths.Candidate)) is { } candidate)
                {
                    candidates.Add(new CandidateData
                    {
                        Model = model, Strategy = strategy, Candidate = candidate,
                        Usage = Json<LlmUsage>(ResultPaths.Join(dir, ResultPaths.Usage)),
                        VerifyC0 = Json<VerifyResult>(ResultPaths.VerifyC0(model, strategy)),
                    });
                }

                suiteIds.Add($"{model}__{strategy}__initial");
                foreach (var mechanism in experiment.Repair.Mechanisms)
                {
                    suiteIds.Add($"{model}__{strategy}__{mechanism}");
                    if (Json<FinalTestSuite>(ResultPaths.Final(model, strategy, mechanism)) is not { } final)
                    {
                        continue;
                    }

                    var decisions = new List<RoundDecision>();
                    var usages = new List<LlmUsage>();
                    for (var round = 1; round <= final.RoundsRun; round++)
                    {
                        var roundDir = ResultPaths.RoundDir(model, strategy, mechanism, round);
                        if (Json<RoundDecision>(ResultPaths.Join(roundDir, ResultPaths.Decision)) is { } decision)
                        {
                            decisions.Add(decision);
                        }

                        if (Json<LlmUsage>(ResultPaths.Join(roundDir, ResultPaths.Usage)) is { } usage)
                        {
                            usages.Add(usage);
                        }
                    }

                    chains.Add(new ChainData { Final = final, Decisions = decisions, Usages = usages });
                }
            }
        }

        suiteIds.Add("evosuite");
        return new TaskData
        {
            Key = key,
            Block1 = Json<BlockOutcome>(ResultPaths.Block1Outcome),
            Candidates = candidates,
            Chains = chains,
            Suites = suiteIds.Select(Suite).OfType<SuiteData>().ToList(),
            EvoSuite = Json<ComparisonSuite>(ResultPaths.Join(ResultPaths.EvoSuiteDir, "comparison.json")),
            EvoSuiteVerify = Json<VerifyResult>(ResultPaths.Join(ResultPaths.EvoSuiteDir, ResultPaths.Verify)),
        };
    }
}
