using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Common;

namespace Preflight.Service.Datasets.Classes2Test;

/// <summary>Port streaming ingest.build_index/candidates; dataset gốc chỉ đọc.</summary>
public sealed class Classes2TestAdapter : IDatasetAdapter
{
    public string Name => "classes2test";
    public async Task<NormalizationResult> NormalizeAsync(DatasetConfig config, NormalizeContext context, CancellationToken cancellationToken = default)
    {
        string root = DatasetRoot(config.Root);
        using var store = new ClassIndexStore(Path.Combine(context.RunRoot, "state/class_index.sqlite"));
        int count = 0; store.Begin(); var gate = new object();
        try
        {
            IEnumerable<string> paths = Directory.EnumerateDirectories(root).OrderBy(p => Path.GetFileName(p)!, PythonCodePointComparer.Instance).SelectMany(project => Directory.EnumerateFiles(project, "*.json").OrderBy(p => Path.GetFileName(p)!, PythonCodePointComparer.Instance));
            if (context.Limit > 0) paths = paths.Take(context.Limit);
            await Parallel.ForEachAsync(paths, new ParallelOptions { MaxDegreeOfParallelism = 8, CancellationToken = cancellationToken }, (path, ct) =>
            {
                ct.ThrowIfCancellationRequested(); var record = Classes2TestRecordReader.Read(path);
                string relative = Path.GetRelativePath(root, path).Replace('\\', '/');
                lock (gate)
                {
                    count++;
                    if (record.Identity is null) store.Reject(relative, record.ReasonCode ?? "INPUT_SCHEMA_INVALID");
                    else
                    {
                        string test = record.Raw!.Value.TryGetProperty("test_class", out var t) && t.ValueKind == JsonValueKind.Object && t.TryGetProperty("file", out var f) ? PythonValue.StringOrEmpty(f).Replace('\\', '/').Trim('/') : "";
                        var i = record.Identity; store.Add(TaskIdFactory.Create(i[0], i[1], i[2], i[4]), i, relative, record.ContentSha256!, test);
                    }
                    if (count % 1000 == 0) { store.Commit(); store.Begin(); }
                }
                return ValueTask.CompletedTask;
            });
            store.Commit();
        }
        catch { throw; }
        var tasks = store.Candidates().Select(c => new CandidateTask(c, Name, new(c.RepoUrl, c.RepoUrl), null, new("dataset_record"))).ToArray();
        store.SaveTasks(tasks);
        return new NormalizationResult(tasks, count, store.Rejections());
    }
    public static string DatasetRoot(string root) => Directory.Exists(Path.Combine(root, "dataset")) ? Path.Combine(root, "dataset") : root;
}
