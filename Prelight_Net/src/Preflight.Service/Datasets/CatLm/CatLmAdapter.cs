using System.Formats.Tar;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Git;

namespace Preflight.Service.Datasets.CatLm;

public sealed record CatLmPair(string Repository, string ClassPath, string TestPath, bool EvalSubset, string SourceEntry);

/// <summary>Đọc phần Java của tar theo tuần tự; không đọc phần Python bị cụt.</summary>
public static class TarIndexer
{
    public static IReadOnlyList<CatLmPair> ReadPairs(string archive, string cohort = "all_filepairs", Action<long>? consumed = null)
    {
        var metadata = new Dictionary<string, JsonElement>(StringComparer.Ordinal);
        using var file = File.OpenRead(archive); using var reader = new TarReader(file);
        while (reader.GetNextEntry() is { } entry)
        {
            string name = entry.Name;
            if (name.StartsWith("./", StringComparison.Ordinal)) name = name[2..];
            if (name.StartsWith("TestData/RawData/python/", StringComparison.Ordinal)) break;
            ValidateArchivePath(name);
            if (entry.DataStream is null || !(name == "TestData/TestFramework/java_filepairs.json" || name.StartsWith("TestData/RawDataOutput/java/FilePairs/", StringComparison.Ordinal) && name.EndsWith(".json", StringComparison.Ordinal))) continue;
            using var memory = new MemoryStream(); entry.DataStream.CopyTo(memory);
            using var doc = JsonDocument.Parse(memory.ToArray()); metadata[name] = doc.RootElement.Clone();
        }
        consumed?.Invoke(file.Position);
        var eval = new HashSet<string>(StringComparer.Ordinal); var result = new List<CatLmPair>();
        if (metadata.TryGetValue("TestData/TestFramework/java_filepairs.json", out var table))
            foreach (var org in table.EnumerateObject()) foreach (var project in org.Value.EnumerateObject()) foreach (var pair in project.Value.EnumerateArray())
            {
                string repo = org.Name + "/" + project.Name, code = Relative(pair[0].GetString()!, repo), test = Relative(pair[1].GetString()!, repo);
                eval.Add(repo + "\0" + code + "\0" + test); result.Add(new(repo, code, test, true, "TestData/TestFramework/java_filepairs.json"));
            }
        if (cohort == "eval_filepairs") return result;
        if (cohort != "all_filepairs") throw new ArgumentException("Unknown CAT-LM cohort");
        result.Clear();
        foreach (var (entry, value) in metadata.OrderBy(p => p.Key, PythonCodePointComparer.Instance))
        {
            if (!entry.Contains("/FilePairs/", StringComparison.Ordinal)) continue;
            string name = Path.GetFileNameWithoutExtension(entry)["filepairs_".Length..]; int split = name.IndexOf("__", StringComparison.Ordinal);
            if (split < 0) throw new InvalidDataException("Invalid CAT-LM filepairs name: " + entry);
            string repo = name[..split] + "/" + name[(split + 2)..];
            var codes = value.GetProperty("code_filename").EnumerateArray().ToArray(); var tests = value.GetProperty("test_filename").EnumerateArray().ToArray();
            if (codes.Length != tests.Length) throw new InvalidDataException("CAT-LM pair arrays differ: " + entry);
            for (int i = 0; i < codes.Length; i++)
            {
                string code = Relative(codes[i].GetString()!, repo), test = Relative(tests[i].GetString()!, repo);
                result.Add(new(repo, code, test, eval.Contains(repo + "\0" + code + "\0" + test), entry));
            }
        }
        return result;
    }
    private static string Relative(string path, string repo)
    {
        string prefix = "/data/GitHubMining/RawDataSample/java/" + repo + "/";
        if (!path.StartsWith(prefix, StringComparison.Ordinal)) throw new InvalidDataException("Unexpected CAT-LM source path: " + path);
        string result = path[prefix.Length..]; ValidateArchivePath(result); return result;
    }
    public static void ValidateArchivePath(string path)
    {
        if (path.StartsWith('/') || path.Contains('\0') || path.Contains('\\') || path.Split('/').Any(p => p == ".." || p.Contains(':'))) throw new InvalidDataException("UNSAFE_ARCHIVE_PATH: " + path);
    }
    public static string PrefixHash(string archive, long length)
    {
        using var input = File.OpenRead(archive); using var hash = IncrementalHash.CreateHash(HashAlgorithmName.SHA256); byte[] buffer = new byte[1048576];
        while (length > 0) { int read = input.Read(buffer, 0, (int)Math.Min(length, buffer.Length)); if (read == 0) throw new EndOfStreamException("Archive changed/truncated"); hash.AppendData(buffer.AsSpan(0, read)); length -= read; }
        return Convert.ToHexString(hash.GetHashAndReset()).ToLowerInvariant();
    }
    public static void Extract(string archive, IReadOnlyDictionary<string, string> selected, CancellationToken ct)
    {
        using var file = File.OpenRead(archive); using var reader = new TarReader(file);
        while (reader.GetNextEntry() is { } entry)
        {
            ct.ThrowIfCancellationRequested(); string name = entry.Name;
            if (name.StartsWith("./", StringComparison.Ordinal)) name = name[2..];
            if (name.StartsWith("TestData/RawData/python/", StringComparison.Ordinal)) break;
            ValidateArchivePath(name);
            const string root = "TestData/RawData/java/";
            if (!name.StartsWith(root, StringComparison.Ordinal)) continue;
            string[] parts = name[root.Length..].Split('/');
            if (parts.Length < 3 || !selected.TryGetValue(parts[0] + "/" + parts[1], out var targetRoot)) continue;
            string relative = string.Join('/', parts.Skip(2)); if (relative.Length == 0) continue;
            string target = Path.GetFullPath(Path.Combine(targetRoot, relative));
            if (!ResolvePath.IsWithin(target, targetRoot)) throw new InvalidDataException("ARCHIVE_PATH_ESCAPE");
            if (entry.EntryType == TarEntryType.Directory) Directory.CreateDirectory(target);
            else if (entry.EntryType is TarEntryType.RegularFile or TarEntryType.V7RegularFile)
            {
                Directory.CreateDirectory(Path.GetDirectoryName(target)!);
                using var output = new FileStream(target, FileMode.Create, FileAccess.Write); entry.DataStream?.CopyTo(output);
            }
            else if (entry.EntryType is TarEntryType.SymbolicLink or TarEntryType.HardLink)
            {
                // Không giải nén link; không ghi source ra ngoài thư mục snapshot.
                ValidateArchivePath(entry.LinkName);
            }
        }
    }
}

/// <summary>Adapter CAT-LM Java; build từ Git HEAD, ghi rõ dirty snapshot và provenance.</summary>
public sealed class CatLmAdapter : IDatasetAdapter
{
    public string Name => "catlm";
    public async Task<NormalizationResult> NormalizeAsync(DatasetConfig config, NormalizeContext context, CancellationToken cancellationToken = default)
    {
        string archive = Archive(config.Archive);
        if (config.RevisionPolicy is not ("snapshot_unverified" or "snapshot_dataset_record")) throw new InvalidDataException("Unknown CAT-LM revision policy");
        long javaBytes = 0; var pairs = TarIndexer.ReadPairs(archive, config.Cohort, n => javaBytes = n); string javaHash = TarIndexer.PrefixHash(archive, javaBytes);
        if (context.Limit > 0) pairs = pairs.Take(context.Limit).ToArray();
        var repositories = pairs.Select(p => p.Repository).Distinct(StringComparer.Ordinal).ToDictionary(p => p, p => Path.Combine(context.RunRoot, "cache/catlm_snapshots", p.Replace("/", "__", StringComparison.Ordinal)), StringComparer.Ordinal);
        TarIndexer.Extract(archive, repositories, cancellationToken);
        var git = new GitCli(context.RunRoot); var tasks = new List<CandidateTask>(); var rejected = new List<Dictionary<string, string>>();
        foreach (var group in pairs.GroupBy(p => p.Repository + "\0" + p.ClassPath).OrderBy(g => g.Key, PythonCodePointComparer.Instance))
        {
            var first = group.First(); string snapshot = repositories[first.Repository];
            string sha = (await git.RunAsync(snapshot, ["rev-parse", "HEAD"], true, cancellationToken)).Output.Trim();
            if (!System.Text.RegularExpressions.Regex.IsMatch(sha, "\\A[0-9a-f]{40}\\z")) throw new InvalidDataException("Invalid snapshot SHA");
            await git.RunAsync(snapshot, ["cat-file", "-e", sha + "^{commit}"], true, cancellationToken);
            string shallow = Path.Combine(snapshot, ".git/shallow");
            if (File.Exists(shallow) && !File.ReadLines(shallow).Contains(sha, StringComparer.Ordinal)) throw new InvalidDataException("Snapshot HEAD is absent from shallow evidence");
            var remote = await git.RunAsync(snapshot, ["remote", "get-url", "origin"], true, cancellationToken);
            string repo = remote.Output.Trim(), project = "catlm-" + first.Repository.Replace("/", "__", StringComparison.Ordinal), name = Path.GetFileNameWithoutExtension(first.ClassPath);
            if (config.VerifyUpstream)
            {
                if (context.Offline) throw new InvalidDataException("verify_upstream requires network access");
                await git.RunAsync(snapshot, ["fetch", "--no-tags", "origin", sha], true, cancellationToken);
                if ((await git.RunAsync(snapshot, ["rev-parse", "FETCH_HEAD"], true, cancellationToken)).Output.Trim() != sha) throw new InvalidDataException("Upstream SHA differs from snapshot");
            }
            var paths = (await git.RunAsync(snapshot, ["ls-tree", "-r", "--name-only", "HEAD"], true, cancellationToken)).Output.Split('\n', StringSplitOptions.RemoveEmptyEntries).ToHashSet(StringComparer.Ordinal);
            string[] tests = group.Select(p => p.TestPath).Distinct(StringComparer.Ordinal).Order(PythonCodePointComparer.Instance).ToArray();
            string reason = !first.ClassPath.EndsWith(".java", StringComparison.Ordinal) || tests.Any(p => !p.EndsWith(".java", StringComparison.Ordinal)) ? "CATLM_NOT_JAVA" : !paths.Contains(first.ClassPath) || tests.Any(p => !paths.Contains(p)) ? "CATLM_PAIR_FILE_MISSING_AT_HEAD" : "";
            if (reason.Length > 0) { rejected.Add(new() { ["source_json_path"] = first.SourceEntry, ["reason_code"] = reason, ["class_path"] = first.ClassPath }); continue; }
            string dirty = (await git.RunAsync(snapshot, ["diff", "--ignore-cr-at-eol", "--name-status", "HEAD"], true, cancellationToken)).Output;
            string status = (await git.RunAsync(snapshot, ["status", "--porcelain"], true, cancellationToken)).Output;
            string id = TaskIdFactory.Create(project, repo, first.ClassPath, name), sourceFile = Path.Combine(context.RunRoot, "inputs/catlm", id + ".json");
            ManifestWriter.WriteJson(sourceFile, new { Archive = archive, Repository = first.Repository, ClassPath = first.ClassPath, TestClassPaths = tests, CheckoutSha = sha, PairSources = group.Select(p => p.SourceEntry).ToArray() });
            string hash = Convert.ToHexString(SHA256.HashData(File.ReadAllBytes(sourceFile))).ToLowerInvariant();
            string evidence = $"zenodo:7909299/cat_lm_artifacts.zip!TestData/RawData/java/{first.Repository}/.git/HEAD={sha}";
            // Quyết định nghiên cứu chưa chốt: hỗ trợ cả hai chính sách qua config.
            var revisionStatus = config.RevisionPolicy == "snapshot_dataset_record" ? RevisionStatus.UPSTREAM_PINNED : RevisionStatus.UNVERIFIED;
            var extra = new Dictionary<string, string> { ["catlm_eval_subset"] = group.Any(p => p.EvalSubset) ? "true" : "false", ["catlm.snapshot_worktree_diff"] = dirty, ["catlm.snapshot_status"] = status, ["revision_source"] = "snapshot_head", ["source_path_original"] = "/data/GitHubMining/RawDataSample/java/" + first.Repository + "/" + first.ClassPath };
            extra["catlm.java_archive_bytes"] = javaBytes.ToString(System.Globalization.CultureInfo.InvariantCulture); extra["catlm.java_archive_sha256"] = javaHash;
            if (dirty.Split('\n').Any(l => new[] { first.ClassPath }.Concat(tests).Any(p => l.EndsWith("\t" + p, StringComparison.Ordinal)))) extra["revision_warning"] = "CATLM_PAIR_FILE_MODIFIED_IN_SNAPSHOT";
            var candidate = new ClassCandidate(id, project, repo, first.ClassPath, name, name, [sourceFile], [hash], tests);
            tasks.Add(new(candidate, Name, new(repo, repo, "snapshot", snapshot, archive, "TestData/RawData/java/" + first.Repository + "/"), new(sha, "dataset_record", revisionStatus, EvidenceRef: evidence), new("catlm_filepairs"), extra));
        }
        using var store = new ClassIndexStore(Path.Combine(context.RunRoot, "state/class_index.sqlite")); store.SaveTasks(tasks);
        ManifestWriter.WriteJson(Path.Combine(context.RunRoot, "normalize.meta.json"), new { Dataset = Name, PairCount = pairs.Count, Repositories = repositories.Count, CutCount = tasks.Count, EvalPairCount = pairs.Count(p => p.EvalSubset), Archive = archive, RevisionPolicy = config.RevisionPolicy });
        return new(tasks, pairs.Count, rejected);
    }
    public static string Archive(string path)
    {
        if (File.Exists(path)) return path;
        string original = Path.Combine(Path.GetDirectoryName(Path.GetFullPath(path))!, "testdata.tgz");
        return File.Exists(original) ? original : throw new FileNotFoundException("CAT-LM archive missing", path);
    }
}
