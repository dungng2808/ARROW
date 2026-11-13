using System.Text;
using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Git;

namespace Preflight.Service.Core.Revision;

/// <summary>Port revision.choose_revision: score evidence, tie-break SHA và fallback cặp/HEAD.</summary>
public sealed class RevisionResolver(GitCli git)
{
    public async Task<RevisionChoice> ChooseAsync(string mirror, CandidateTask task, string datasetRoot, PreflightConfig config, CancellationToken ct)
    {
        if (task.PinnedRevision is { } pinned)
        {
            if ((await git.RunAsync(mirror, ["rev-parse", "--verify", pinned.CheckoutSha + "^{commit}"], false, ct)).ExitCode != 0) throw new KeyNotFoundException("COMMIT_MISSING");
            return pinned;
        }
        var candidate = task.Candidate;
        using var objects = new GitObjectReader(mirror, config.Run.GitTimeoutSeconds);
        Task<string?> Read(string sha, string path) => path.IndexOfAny(['\n', '\r']) >= 0 ? git.ShowAsync(mirror, sha, path, ct) : objects.ReadAsync(sha, path, ct);
        var log = await git.RunAsync(mirror, new[] { "log", "--all", "--format=%H", "--", candidate.ClassPath }.Concat(candidate.TestClassPaths), false, ct);
        var commits = log.Output.Split('\n', StringSplitOptions.TrimEntries | StringSplitOptions.RemoveEmptyEntries).Distinct(StringComparer.Ordinal).Take(config.Run.MaxRevisionCandidates);
        string first = "", bestSha = ""; List<Dictionary<string, string>> best = [];
        foreach (string sha in commits)
        {
            string? source = await Read(sha, candidate.ClassPath); if (source is null) continue;
            var tests = new Dictionary<string, string?>(StringComparer.Ordinal);
            foreach (string path in candidate.TestClassPaths) tests[path] = await Read(sha, path);
            if (!tests.Values.Any(v => v is not null)) continue;
            if (first.Length == 0) first = sha;
            var matches = new List<Dictionary<string, string>>(); string normalized = JavaNormalizer.Normalize(source);
            foreach (string relative in candidate.SourceJsonPaths.Take(config.Run.MaxEvidenceChecks))
            {
                ct.ThrowIfCancellationRequested(); JsonElement raw;
                try
                {
                    // utf-8, không bỏ BOM: giữ quirk content match của Python.
                    string text = new UTF8Encoding(false, true).GetString(File.ReadAllBytes(Path.Combine(datasetRoot, relative)));
                    using var doc = JsonDocument.Parse(text, new JsonDocumentOptions { MaxDepth = 1000 }); raw = doc.RootElement.Clone();
                }
                catch (IOException) { continue; } catch (JsonException) { continue; }
                string Get(string group, string key) => raw.TryGetProperty(group, out var g) && g.ValueKind == JsonValueKind.Object && g.TryGetProperty(key, out var v) ? PythonValue.StringOrEmpty(v) : "";
                string testPath = Get("test_class", "file").Replace('\\', '/').Trim('/'), focal = Get("focal_method", "body"), test = Get("test_case", "body");
                if (!tests.TryGetValue(testPath, out string? testSource)) tests[testPath] = testSource = await Read(sha, testPath);
                if (focal.Length > 0 && test.Length > 0 && normalized.Contains(JavaNormalizer.Normalize(focal), StringComparison.Ordinal) && !string.IsNullOrEmpty(testSource) && JavaNormalizer.Normalize(testSource).Contains(JavaNormalizer.Normalize(test), StringComparison.Ordinal))
                    matches.Add(new() { ["source_json_path"] = relative, ["focal_method"] = Get("focal_method", "signature"), ["test_case"] = Get("test_case", "signature") });
            }
            if (matches.Count > best.Count || matches.Count > 0 && matches.Count == best.Count && string.CompareOrdinal(sha, bestSha) < 0) { best = matches; bestSha = sha; }
        }
        if (best.Count > 0) return new(bestSha, "git_history", RevisionStatus.CONTENT_MATCHED, PythonJson.Element(new { Normalization = "strip Java comments then collapse whitespace", MatchedEvidence = best, MatchedEvidenceCount = best.Count }));
        if (first.Length > 0) return new(first, "git_history", RevisionStatus.UNVERIFIED, PythonJson.Element(new { SelectionMethod = "latest_commit_with_focal_and_test_paths" }));
        var head = await git.RunAsync(mirror, ["rev-parse", "HEAD"], false, ct);
        if (head.ExitCode != 0 || head.Output.Trim().Length == 0) throw new KeyNotFoundException("COMMIT_MISSING");
        return new(head.Output.Trim(), "git_history", RevisionStatus.UNVERIFIED, PythonJson.Element(new { SelectionMethod = "mirror_HEAD_no_path_pair" }));
    }
}
