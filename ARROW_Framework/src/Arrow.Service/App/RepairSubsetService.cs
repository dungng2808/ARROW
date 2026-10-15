using Arrow.DataAccess;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block3;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

/// <summary>
/// Gom kết quả xác minh c0 của các run cùng cấu hình thực nghiệm. Chỉ chọn khi mọi task của dataset có <c>repair_subset</c> đã xong pha 1
/// ở đúng một run và không còn c0 INFRA (spec 03 Mục 6.1: ứng viên INFRA chưa có kết quả, phải chạy lại trước).
/// </summary>
public sealed class RepairSubsetService(
    IConfigRepository config,
    IManifestRepository manifests,
    IRunRepository runs,
    ITaskResultRepository results,
    ILogRepository log) : IRepairSubsetService
{
    public RepairSubsetResult Create(string experimentPath, IReadOnlyList<string> runIds)
    {
        var experiment = config.LoadExperiment(experimentPath);
        var cfg = experiment.Config;
        if (cfg.RepairSubsetFile is null)
        {
            throw new ArrowException(ErrorCodes.ConfigInvalid, $"{Path.GetFileName(experimentPath)} không khai báo repair_subset_file");
        }

        var subsetManifests = cfg.Manifests.Where(m => m.Repair && m.RepairSubset is not null).ToList();
        if (subsetManifests.Count == 0)
        {
            throw new ArrowException(ErrorCodes.ConfigInvalid, $"{Path.GetFileName(experimentPath)}: không manifest nào đặt repair_subset");
        }

        var problems = new List<string>();
        foreach (var runId in runIds)
        {
            var runLock = runs.ReadLock(runId);
            if (runLock is null)
            {
                problems.Add($"run {runId}: không có run.lock.json");
            }
            else if (runLock.ExperimentSha256 != experiment.Sha256)
            {
                problems.Add($"run {runId}: chạy với cấu hình khác (sha256 {runLock.ExperimentSha256[..12]}…)");
            }
        }

        var subset = new RepairSubset
        {
            ExperimentId = cfg.ExperimentId,
            ExperimentSha256 = experiment.Sha256,
            Seed = 42,
            Rule = RepairSubsetSelector.Rule,
            Runs = [.. runIds],
        };
        var models = cfg.Models.Select(m => m.Name).ToList();
        foreach (var manifest in subsetManifests)
        {
            var eligible = new List<SubsetCandidate>();
            foreach (var task in manifests.ReadTasks(experiment.ResolveManifestPath(manifest), manifest.Dataset))
            {
                var key = new TaskKey(manifest.Dataset, task.TaskId);
                var owners = runIds.Where(r => results.LoadJson<BlockOutcome>(r, key, ResultPaths.Block1Outcome) is not null).ToList();
                if (owners.Count != 1)
                {
                    problems.Add(owners.Count == 0 ? $"{task.TaskId}: chưa chạy pha 1 ở run nào" : $"{task.TaskId}: có ở nhiều run ({string.Join(", ", owners)})");
                    continue;
                }

                var run = owners[0];
                if (results.LoadJson<BlockOutcome>(run, key, ResultPaths.Block1Outcome)!.Outcome != OutcomeKind.Ok)
                {
                    continue; // task bị loại ở khối 1: không có ứng viên
                }

                foreach (var model in models)
                {
                    foreach (var strategy in cfg.Strategies)
                    {
                        var candidate = results.LoadJson<CandidateTest>(run, key, ResultPaths.Join(ResultPaths.GenerateDir(model, strategy), ResultPaths.Candidate));
                        if (candidate is null)
                        {
                            problems.Add($"{task.TaskId} {model}/{strategy}: chưa sinh c0 (run {run})");
                            continue;
                        }

                        if (!candidate.ValidOutput)
                        {
                            continue; // không có mã để sửa (spec 03 Mục 6.1)
                        }

                        var verify = results.LoadJson<VerifyResult>(run, key, ResultPaths.VerifyC0(model, strategy));
                        if (verify is null || verify.State == VerificationState.Infra)
                        {
                            problems.Add($"{task.TaskId} {model}/{strategy}: c0 {(verify is null ? "chưa xác minh" : "INFRA")} (run {run}; chạy lại với --rerun-infra)");
                        }
                        else if (verify.State != VerificationState.Valid)
                        {
                            eligible.Add(new SubsetCandidate { TaskId = task.TaskId, Model = model, Strategy = strategy });
                        }
                    }
                }
            }

            subset.Datasets[manifest.Dataset] = RepairSubsetSelector.Select(manifest.Dataset, eligible, models, cfg.Strategies, manifest.RepairSubset!.Value, subset.Seed);
        }

        if (problems.Count > 0)
        {
            return new RepairSubsetResult(null, null, problems);
        }

        var path = experiment.ResolvePath(cfg.RepairSubsetFile);
        config.SaveRepairSubset(path, subset);
        log.Info($"rq2-subset: ghi {path} ({string.Join(", ", subset.Datasets.Select(d => $"{d.Key}: {d.Value.Candidates.Count}"))})");
        return new RepairSubsetResult(path, subset, []);
    }
}
