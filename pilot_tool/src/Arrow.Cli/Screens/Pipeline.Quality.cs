using Arrow.Core.Manifest;

namespace Arrow.Cli.Screens;

public sealed partial class Pipeline
{
    /// <summary>S3 EvoSuite, S4 đo chất lượng.</summary>
    private IEnumerable<Func<StageSpec?>> QualityStages(IReadOnlyList<ManifestRecord> tasks, ISet<string> stages, CancellationToken cancellationToken)
    {
        _ = tasks;
        _ = stages;
        _ = cancellationToken;
        return [];
    }
}
