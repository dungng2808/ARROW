using System.Text;
using System.Text.Json;
using System.Text.RegularExpressions;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Git;
using Preflight.Service.Datasets.Classes2Test;

namespace Preflight.Service.Datasets.GitBug;

/// <summary>Port normalize_gitbug.changed_java_files và pair_test.</summary>
public static class GitBugRules
{
    public static IReadOnlyList<(string Path, bool Deleted)> ChangedJavaFiles(string patch) => Regex.Matches(patch, @"^diff --git a/(\S+) b/(\S+)\n((?:(?!diff --git ).*\n?)*)", RegexOptions.Multiline | RegexOptions.CultureInvariant).Where(m => m.Groups[2].Value.EndsWith(".java", StringComparison.Ordinal)).Select(m => (m.Groups[2].Value, m.Groups[3].Value.Contains("+++ /dev/null", StringComparison.Ordinal))).ToArray();
    public static (string Path, string Method) PairTest(string classPath, IReadOnlySet<string> paths)
    {
        int at = classPath.IndexOf("src/main/java/", StringComparison.Ordinal);
        string prefix = classPath[..at], relative = classPath[(at + 14)..];
        int slash = relative.LastIndexOf('/'); string package = slash < 0 ? "" : relative[..slash], name = relative[(slash + 1)..^5];
        string root = prefix + "src/test/java/"; string[] names = [name + "Test.java", "Test" + name + ".java", name + "Tests.java"];
        foreach (string file in names)
        {
            string path = root + (package.Length > 0 ? package + "/" : "") + file;
            if (paths.Contains(path)) return (path, "same_package");
        }
        var matches = paths.Where(p => p.StartsWith(root, StringComparison.Ordinal) && names.Contains(p.Split('/').Last(), StringComparer.Ordinal)).Order(PythonCodePointComparer.Instance).ToArray();
        return matches.Length == 1 ? (matches[0], "unique_name_in_module") : ("", matches.Length > 0 ? "ambiguous_name" : "none");
    }
}

/// <summary>Adapter GitBug fixed commit, ghép test theo tên hoặc manual label.</summary>
public sealed class GitBugAdapter : IDatasetAdapter
{
    public string Name => "gitbug-java";
    public async Task<NormalizationResult> NormalizeAsync(DatasetConfig config, NormalizeContext context, CancellationToken cancellationToken = default)
    {
        // Input đã normalize là đường vào tái lập mà không cần mạng.
        if (Directory.Exists(Path.Combine(config.Root, "dataset")) && File.Exists(Path.Combine(config.Root, "revision_map.csv")))
        {
            var result = await new Classes2TestAdapter().NormalizeAsync(config, context, cancellationToken);
            var map = RevisionMapReader.Load(Path.Combine(config.Root, "revision_map.csv"));
            var tasks = result.Tasks.Select(t =>
            {
                using var raw = JsonDocument.Parse(File.ReadAllBytes(Path.Combine(Classes2TestAdapter.DatasetRoot(config.Root), t.Candidate.SourceJsonPath)));
                string pairing = raw.RootElement.TryGetProperty("gitbug", out var metadata) && metadata.TryGetProperty("test_pairing", out var method) ? method.GetString() ?? "dataset_record" : "dataset_record";
                return t with { Dataset = Name, PinnedRevision = map.GetValueOrDefault(t.Candidate.TaskId) ?? throw new InvalidDataException("Missing GitBug pinned revision"), Pairing = new(pairing) };
            }).ToArray();
            using var index = new ClassIndexStore(Path.Combine(context.RunRoot, "state/class_index.sqlite")); index.SaveTasks(tasks);
            return result with { Tasks = tasks };
        }
        string[] bugFiles = Directory.GetFiles(Path.Combine(config.Root, "data/bugs"), "*.json").Order(PythonCodePointComparer.Instance).ToArray();
        if (bugFiles.Length == 0) throw new InvalidDataException("No GitBug JSONL bug files");
        var git = new GitCli(context.RunRoot); var mirrors = new MirrorManager(context.RunRoot, git);
        var labels = ReadLabels(config.ManualLabels); var usedLabels = new HashSet<string>(StringComparer.Ordinal); var rows = new List<Dictionary<string, object?>>(); var revisions = new List<Dictionary<string, object?>>();
        string dataset = Path.Combine(context.RunRoot, "inputs/gitbug/dataset"); Directory.CreateDirectory(dataset);
        string benchmark = (await git.RunAsync(config.Root, ["rev-parse", "HEAD"], false, cancellationToken)).Output.Trim();
        int bugs = 0; var sources = new Dictionary<string, RepoSource>(StringComparer.Ordinal);
        foreach (string bugFile in bugFiles)
        {
            foreach (string line in File.ReadLines(bugFile).Where(l => l.Trim().Length > 0))
            {
                cancellationToken.ThrowIfCancellationRequested(); if (context.Limit > 0 && bugs >= context.Limit) break;
                using var doc = JsonDocument.Parse(line); var bug = doc.RootElement; bugs++;
                string S(string key) => bug.TryGetProperty(key, out var v) ? PythonValue.StringOrEmpty(v) : "";
                string repository = S("repository"), sha = S("commit_hash"), project = repository.Replace('/', '-') + "-" + sha[..12], repoUrl = "https://github.com/" + repository;
                var source = new RepoSource(repoUrl, S("clone_url").Length > 0 ? S("clone_url") : repoUrl + ".git");
                sources[repoUrl] = source;
                string? mirror = null; HashSet<string>? paths = null;
                try
                {
                    mirror = await mirrors.EnsureAsync(source, context.Offline, cancellationToken);
                    if ((await git.RunAsync(mirror, ["cat-file", "-e", sha + "^{commit}"], false, cancellationToken)).ExitCode != 0 && !context.Offline) await git.RunAsync(mirror, ["fetch", "origin", sha], false, cancellationToken);
                    var tree = await git.RunAsync(mirror, ["ls-tree", "-r", "--name-only", sha], false, cancellationToken);
                    if (tree.ExitCode == 0) paths = tree.Output.Split('\n', StringSplitOptions.RemoveEmptyEntries).ToHashSet(StringComparer.Ordinal);
                }
                catch (GitCommandException) { }
                var files = GitBugRules.ChangedJavaFiles(S("bug_patch")); int written = 0;
                Dictionary<string, object?> Row(string path) => new() { ["project_id"] = project, ["repository"] = repository, ["checkout_sha"] = sha, ["bug_file"] = Path.GetFileName(bugFile), ["strategy"] = S("strategy"), ["change_type"] = S("change_type"), ["class_path"] = path };
                if (files.Count == 0) { var empty = Row(""); empty["status"] = "EXCLUDED"; empty["reason"] = "NO_JAVA_IN_BUG_PATCH"; rows.Add(empty); }
                foreach (var (path, deleted) in files)
                {
                    var row = Row(path); string fileName = path.Split('/').Last();
                    string reason = deleted ? "DELETED_IN_FIX" : !("/" + path).Contains("/src/main/java/", StringComparison.Ordinal) ? "NOT_UNDER_SRC_MAIN_JAVA" : fileName is "package-info.java" or "module-info.java" ? "NON_CLASS_FILE" : mirror is null ? "CLONE_FAILED" : paths is null ? "COMMIT_MISSING" : !paths.Contains(path) ? "FILE_MISSING_AT_COMMIT" : "";
                    string test = "", pairing = ""; Dictionary<string, string>? label = null;
                    if (reason.Length == 0)
                    {
                        (test, pairing) = GitBugRules.PairTest(path, paths!);
                        if (test.Length == 0 && labels.TryGetValue(project + "\0" + path, out label))
                        {
                            usedLabels.Add(project + "\0" + path);
                            if (label["decision"] == "PAIRED") { test = label["test_class_path"]; if (!paths!.Contains(test)) throw new InvalidDataException("Labeled test missing at fixed commit: " + test); pairing = "manual"; }
                            else pairing = "manual_none";
                        }
                        if (test.Length == 0) reason = "NO_PAIRED_TEST";
                    }
                    row["status"] = reason.Length == 0 ? "CANDIDATE" : "EXCLUDED"; row["reason"] = reason;
                    if (pairing.Length > 0) row["test_pairing"] = pairing;
                    if (reason.Length == 0)
                    {
                        string name = fileName[..^5], taskId = TaskIdFactory.Create(project, repoUrl, path, name); written++;
                        var metadata = new Dictionary<string, object?> { ["bug_id"] = project, ["bug_file"] = Path.GetFileName(bugFile), ["commit_hash"] = sha, ["previous_commit_hash"] = S("previous_commit_hash"), ["strategy"] = S("strategy"), ["change_type"] = S("change_type"), ["test_pairing"] = pairing };
                        if (pairing == "manual" && label is not null) metadata["manual_label"] = new[] { "labeler", "reviewer", "rationale" }.Where(label.ContainsKey).ToDictionary(k => k, k => label[k], StringComparer.Ordinal);
                        metadata["benchmark_commit"] = benchmark;
                        var record = new Dictionary<string, object?> { ["repository"] = new { Url = repoUrl, Language = "Java" }, ["focal_class"] = new { Identifier = name, File = path }, ["test_class"] = new { Identifier = test.Split('/').Last()[..^5], File = test }, ["gitbug"] = metadata };
                        string output = Path.Combine(dataset, project, written + ".json"); Directory.CreateDirectory(Path.GetDirectoryName(output)!);
                        var options = new JsonSerializerOptions(PythonJson.Options) { WriteIndented = true, Encoder = System.Text.Encodings.Web.JavaScriptEncoder.UnsafeRelaxedJsonEscaping };
                        File.WriteAllText(output, JsonSerializer.Serialize(record, options) + "\n", new UTF8Encoding(false));
                        row["task_id"] = taskId; row["test_class_path"] = test;
                        revisions.Add(new() { ["task_id"] = taskId, ["checkout_sha"] = sha, ["revision_verification_status"] = "UPSTREAM_PINNED", ["revision_provenance"] = "dataset_record", ["evidence_ref"] = $"gitbug-java@{benchmark[..Math.Min(12, benchmark.Length)]}:data/bugs/{Path.GetFileName(bugFile)}#commit_hash={sha}" });
                    }
                    rows.Add(row);
                }
            }
            if (context.Limit > 0 && bugs >= context.Limit) break;
        }
        ManifestWriter.WriteCsv(Path.Combine(context.RunRoot, "candidates.csv"), rows); ManifestWriter.WriteCsv(Path.Combine(context.RunRoot, "revision_map.csv"), revisions);
        ManifestWriter.WriteJson(Path.Combine(context.RunRoot, "normalize.meta.json"), new { SchemaVersion = 1, GitbugJavaCommit = benchmark, Bugs = bugs, Repositories = bugFiles.Length, CandidateClasses = revisions.Count, ByReason = rows.Where(r => r["status"] as string == "EXCLUDED").GroupBy(r => (string)r["reason"]!).ToDictionary(g => g.Key, g => g.Count(), StringComparer.Ordinal), BugFilesSha256 = bugFiles.ToDictionary(p => Path.GetFileName(p)!, p => Hash(p)), ManualLabels = config.ManualLabels, ManualLabelsSha256 = config.ManualLabels.Length > 0 ? Hash(config.ManualLabels) : "", LabelsUsed = usedLabels.Count, UnusedLabels = labels.Keys.Except(usedLabels).Order(StringComparer.Ordinal).ToArray() });
        var normalized = await new Classes2TestAdapter().NormalizeAsync(config with { Root = dataset }, context with { Limit = 0 }, cancellationToken);
        var revisionMap = RevisionMapReader.Load(Path.Combine(context.RunRoot, "revision_map.csv"));
        var candidates = normalized.Tasks.Select(t => t with { Dataset = Name, Repo = sources[t.Candidate.RepoUrl], PinnedRevision = revisionMap[t.Candidate.TaskId], Pairing = new(rows.First(r => r.GetValueOrDefault("task_id") as string == t.Candidate.TaskId).GetValueOrDefault("test_pairing") as string ?? "dataset_record") }).ToArray();
        using (var store = new ClassIndexStore(Path.Combine(context.RunRoot, "state/class_index.sqlite"))) store.SaveTasks(candidates);
        return normalized with { Tasks = candidates, RawCount = bugs, Rejections = rows.Where(r => r["status"] as string == "EXCLUDED").Select(r => r.ToDictionary(p => p.Key, p => p.Value?.ToString() ?? "", StringComparer.Ordinal)).ToArray() };
    }
    private static Dictionary<string, Dictionary<string, string>> ReadLabels(string path)
    {
        var result = new Dictionary<string, Dictionary<string, string>>(StringComparer.Ordinal);
        if (path.Length == 0) return result;
        foreach (var row in CsvTable.Read(path))
        {
            string key = row["project_id"].Trim() + "\0" + row["class_path"].Trim(), decision = row.GetValueOrDefault("decision", "").Trim().ToUpperInvariant();
            if (decision is not ("PAIRED" or "NONE") || decision == "PAIRED" && row.GetValueOrDefault("test_class_path", "").Length == 0 || result.ContainsKey(key)) throw new InvalidDataException("Invalid/duplicate manual label: " + key);
            row["decision"] = decision; result[key] = row;
        }
        return result;
    }
    private static string Hash(string path) { using var stream = File.OpenRead(path); return Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(stream)).ToLowerInvariant(); }
}
