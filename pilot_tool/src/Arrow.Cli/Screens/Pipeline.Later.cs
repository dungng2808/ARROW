using Arrow.Core.Manifest;

namespace Arrow.Cli.Screens;

public sealed partial class Pipeline
{
    /// <summary>S2 sửa test, S3 EvoSuite, S4 đo chất lượng.</summary>
    public IEnumerable<Func<StageSpec?>> LaterStages(IReadOnlyList<ManifestRecord> tasks, ISet<string> stages, CancellationToken cancellationToken)
    {
        _ = tasks;
        _ = stages;
        _ = cancellationToken;
        return [];
    }
}
