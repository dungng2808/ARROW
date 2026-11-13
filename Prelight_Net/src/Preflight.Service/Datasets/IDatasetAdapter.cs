using Preflight.Models;

namespace Preflight.Service.Datasets;

public sealed record NormalizeContext(string RunRoot, bool Offline = false, int Limit = 0);
public interface IDatasetAdapter
{
    string Name { get; }
    Task<NormalizationResult> NormalizeAsync(DatasetConfig config, NormalizeContext context, CancellationToken cancellationToken = default);
}
