using System.Diagnostics;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Build;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Git;
using Preflight.Service.Core.JavaAst;
using Preflight.Service.Core.Jdk;
using Preflight.Service.Core.Policy;
using Preflight.Service.Core.Probe;
using Preflight.Service.Core.Revision;
using Preflight.Service.Datasets.CatLm;
using Preflight.Service.External;

namespace Preflight.Service.Core.Runner;

/// <summary>Port nguyên thứ tự runner.preflight_one; ba adapter dùng cùng lõi này.</summary>
public sealed class PreflightPipeline
{
    private readonly string runRoot, inputRoot;
    private readonly PreflightConfig config;
    private readonly GitCli git;
    private readonly MirrorManager mirrors;
    private readonly WorktreeManager worktrees;
    private readonly JavaSourceParser parser;
    private readonly IProcessRunner process;
    private readonly System.Collections.Concurrent.ConcurrentDictionary<string, bool> verifiedArchives = new(StringComparer.Ordinal);
    public MirrorManager Mirrors => mirrors;
    public PreflightPipeline(string runRoot, string inputRoot, PreflightConfig config, IProcessRunner? process = null)
    {
        this.runRoot = runRoot; this.inputRoot = inputRoot; this.config = config; this.process = process ?? new ProcessRunner();
        git = new(runRoot, this.process, config.Run.GitTimeoutSeconds); mirrors = new(runRoot, git); worktrees = new(runRoot, git, mirrors);
        parser = new(Path.IsPathRooted(config.Java.TreeSitterLibrary) ? config.Java.TreeSitterLibrary : Path.Combine(RuntimePaths.Root, config.Java.TreeSitterLibrary));
    }
    public async Task<PreflightResult> RunAsync(CandidateTask task, int attempt, CancellationToken ct)
    {
        var timer = Stopwatch.StartNew(); var c = task.Candidate;
        var result = new PreflightResult { TaskId = c.TaskId, SourceJsonPath = c.SourceJsonPath, SourceJsonSha256 = c.SourceJsonSha256, SourceJsonPaths = c.SourceJsonPaths, SourceJsonSha256s = c.SourceJsonSha256s, RepoUrl = c.RepoUrl, ClassPath = c.ClassPath, ClassFqn = c.ClassFqn, TestClassPath = c.TestClassPaths.FirstOrDefault() ?? "", TestClassPaths = c.TestClassPaths, Dataset = task.Dataset, PairingMethod = task.Pairing.Method, PairingEvidence = task.Pairing, ProbeProfile = config.Run.ProbeProfile.ToString(), Extra = task.Extra ?? new(StringComparer.Ordinal) };
        var reasons = new List<string>(); var attempts = new List<BuildAttempt>(); string? mirror = null, workspace = null;
        string logs = Path.Combine(runRoot, "logs", c.TaskId, $"attempt-{attempt:D3}");
        void Status(PreflightStatus value, string? reason = null) { result.PreflightStatus = value; if (reason is not null) reasons.Add(reason); }
        void AddAttempts(IEnumerable<BuildAttempt> values)
        {
            foreach (var value in values)
            {
                attempts.Add(value);
                if (File.Exists(value.LogPath) && InfrastructureFailure.LogIsDiskFull(File.ReadAllText(value.LogPath))) throw new InfrastructureFailure("INFRA_DISK_FULL");
            }
        }
        try
        {
            ct.ThrowIfCancellationRequested();
            if (task.Repo.Kind == "snapshot" && !Directory.Exists(task.Repo.SnapshotPath))
            {
                if (task.Extra?.TryGetValue("catlm.java_archive_sha256", out string? expected) == true && !verifiedArchives.ContainsKey(task.Repo.ArchivePath))
                {
                    long length = long.Parse(task.Extra["catlm.java_archive_bytes"], System.Globalization.CultureInfo.InvariantCulture);
                    if (TarIndexer.PrefixHash(task.Repo.ArchivePath, length) != expected) throw new InvalidDataException("CAT-LM Java archive changed"); verifiedArchives[task.Repo.ArchivePath] = true;
                }
                string key = task.Repo.ArchivePrefix["TestData/RawData/java/".Length..].TrimEnd('/');
                TarIndexer.Extract(task.Repo.ArchivePath, new Dictionary<string, string> { [key] = task.Repo.SnapshotPath }, ct);
            }
            mirror = await mirrors.EnsureAsync(task.Repo, config.Run.Offline, ct);
            string evidenceRoot = Path.GetDirectoryName(RuntimePaths.Evidence(task, c.SourceJsonPath, runRoot, inputRoot))!;
            if (!Path.IsPathRooted(c.SourceJsonPath))
            {
                string full = RuntimePaths.Evidence(task, c.SourceJsonPath, runRoot, inputRoot);
                evidenceRoot = full[..(full.Length - c.SourceJsonPath.Replace('/', Path.DirectorySeparatorChar).Length)].TrimEnd(Path.DirectorySeparatorChar);
            }
            var choice = await new RevisionResolver(git).ChooseAsync(mirror, task, evidenceRoot, config, ct);
            result.CheckoutSha = choice.CheckoutSha; result.RevisionProvenance = choice.Provenance; result.RevisionVerificationStatus = choice.Status;
            result.ContentMatch = choice.EvidenceRef.Length > 0 ? PythonJson.Element(new { EvidenceRef = choice.EvidenceRef }) : choice.ContentMatch;
            result.RevisionEvidenceRef = choice.EvidenceRef; result.RunMode = choice.Status == RevisionStatus.UPSTREAM_PINNED ? RunMode.STRICT : RunMode.DISCOVERY_ONLY;
            if (choice.Status != RevisionStatus.UPSTREAM_PINNED) reasons.Add("REVISION_" + choice.Status);
            workspace = await worktrees.CreateAsync(c.RepoUrl, mirror, c.TaskId, choice.CheckoutSha, ct); result.WorkingDirectory = workspace;
            string? classFile = ResolvePath.Resolve(workspace, c.ClassPath);
            if (classFile is null) { Status(PreflightStatus.SOURCE_INVALID, "SOURCE_PATH_NOT_FOUND"); return result; }
            string source = ReadJava(classFile); var java = parser.Parse(source, c.ClassPath, c.ClassName);
            if (java is null || java.Name != c.ClassName) { Status(PreflightStatus.SOURCE_INVALID, "SOURCE_FQN_MISMATCH"); return result; }
            result.ClassFqn = java.Fqn; var policy = ClassPolicy.Evaluate(java); result.Tags = policy.Tags; reasons.AddRange(policy.Reasons);
            var plan = BuildDetector.Detect(workspace, classFile);
            if (plan is null) { Status(PreflightStatus.BUILD_TOOL_UNSUPPORTED, "BUILD_TOOL_UNSUPPORTED"); return result; }
            result.BuildTool = plan.Tool; result.ModulePath = plan.ModulePath;
            var toolVersion = await process.RunAsync([plan.Executable, "--version"], plan.InvocationRoot, Math.Min(60, config.Run.BuildTimeoutSeconds), Path.Combine(logs, "tool_version.log"), CompileRunner.ToolEnvironment(config), ct);
            AddAttempts([toolVersion with { Stage = "tool_version" }]);
            string[] versionLog = File.ReadAllText(toolVersion.LogPath).ToLowerInvariant().Split('\n'); result.BuildToolVersion = versionLog.Length > 2 ? versionLog[2] : "unknown";
            result.BuildToolVersionDetail = File.ReadAllText(toolVersion.LogPath).Split('\n').FirstOrDefault(l => l.Contains("Maven", StringComparison.Ordinal) || l.StartsWith("Gradle ", StringComparison.Ordinal)) ?? "";
            var environment = await new JdkSelector(process).SelectAsync(JavaTargetReader.Read(plan.ModuleRoot, plan.Tool, config.Run.ProbeProfile), config, logs, ct);
            result.JavaVersion = environment.Version;
            if (environment.Error is not null) { Status(PreflightStatus.JDK_UNSUPPORTED, environment.Error); return result; }
            var env = CompileRunner.ToolEnvironment(config, environment.Environment); var compiler = new CompileRunner(process);
            foreach (string stage in new[] { "main_compile", "test_compile" })
            {
                var values = await compiler.CompileAsync(plan, stage, config, logs, env, ct); AddAttempts(values);
                if (values[^1].ExitCode != 0) { var status = FailureClassifier.Classify(values[^1], stage); Status(status, status.ToString()); return result; }
            }
            if (policy.Policy is not null) { Status(PreflightStatus.EXCLUDED); return result; }
            var apis = new ApiCounter(parser).Count(java, classFile, workspace);
            var methods = apis.Where(a => a.Kind == "method").ToArray(); var constructors = apis.Where(a => a.Kind == "constructor").ToArray();
            result.MethodCount = methods.Length; result.ConstructorCount = constructors.Length; result.ApiCount = apis.Count;
            if (apis.Count == 0) { Status(PreflightStatus.EXCLUDED, "EXCLUDE_NO_TESTABLE_API"); return result; }
            if (config.Run.FastMode) { Status(PreflightStatus.PRECHECKED); return result; }
            string testSource = string.Concat(c.TestClassPaths.Select(p => ResolvePath.Resolve(workspace, p)).Where(p => p is not null).Select(p => ReadJava(p!)));
            result.TestingFramework = FrameworkDetector.Detect(source, testSource);
            var api = methods.FirstOrDefault(a => a.IsStatic) ?? methods.FirstOrDefault(a => !a.IsStatic) ?? constructors[0]; result.ProbedApi = api.Signature;
            string probeName = "PreflightProbe_" + c.TaskId[^Math.Min(8, c.TaskId.Length)..], probeRoot = TestSourceRoot.Find(classFile, plan.ModuleRoot);
            string probe = Path.Combine(probeRoot, java.Package.Replace('.', Path.DirectorySeparatorChar), probeName + ".java");
            if (!ResolvePath.IsWithin(ResolvePath.ResolveLinks(probe), workspace)) { Status(PreflightStatus.SOURCE_INVALID, "PROBE_PATH_OUTSIDE_WORKSPACE"); return result; }
            Directory.CreateDirectory(Path.GetDirectoryName(probe)!); File.WriteAllText(probe, ProbeSourceBuilder.Build(java, api, probeName, result.TestingFramework, config.Run.ProbeProfile, source), new System.Text.UTF8Encoding(false));
            IReadOnlyList<BuildAttempt> probeAttempts;
            try { probeAttempts = await compiler.CompileAsync(plan, "probe_compile", config, logs, env, ct); AddAttempts(probeAttempts); }
            finally { File.Delete(probe); }
            if (probeAttempts[^1].ExitCode != 0) { result.ProbeStatus = "FAILED"; Status(PreflightStatus.PROBE_FAILED, "PROBE_FAILED"); return result; }
            result.ProbeStatus = "PASSED";
            if (result.Tags.Contains("DIRECT_CONNECTION", StringComparer.Ordinal)) { Status(PreflightStatus.NEEDS_REVIEW, "DIRECT_CONNECTION_NEEDS_REVIEW"); return result; }
            if (result.TestingFramework == "unknown") { Status(PreflightStatus.NEEDS_REVIEW, "TEST_FRAMEWORK_UNKNOWN"); return result; }
            Status(PreflightStatus.ELIGIBLE);
            return result;
        }
        catch (OperationCanceledException) { throw; }
        catch (InfrastructureFailure) { throw; }
        catch (IOException e) when (InfrastructureFailure.IsDiskFull(e)) { throw new InfrastructureFailure("INFRA_DISK_FULL: " + e.Message); }
        catch (GitCommandException) { var status = mirror is null ? PreflightStatus.CLONE_FAILED : PreflightStatus.CHECKOUT_FAILED; Status(status, status.ToString()); return result; }
        catch (KeyNotFoundException) { Status(PreflightStatus.COMMIT_MISSING, "COMMIT_MISSING"); return result; }
        catch (Exception e) { Status(PreflightStatus.SOURCE_INVALID, "UNHANDLED_" + e.GetType().Name); return result; }
        finally
        {
            result.DurationSeconds = Math.Round(timer.Elapsed.TotalSeconds, 3); result.ReasonCodes = reasons; result.BuildAttempts = attempts; result.LogPaths = attempts.Select(a => a.LogPath).ToArray();
            (result.TechnicalEligible, result.StrictEligible) = StatusPolicy.DeriveEligibility(result.PreflightStatus.ToString(), result.RunMode.ToString(), result.RevisionVerificationStatus.ToString());
            if (workspace is not null && mirror is not null && !config.Run.KeepWorkspaces)
            {
                try { await worktrees.RemoveAsync(c.RepoUrl, mirror, c.TaskId); }
                catch (Exception error) when (error is IOException or GitCommandException or UnauthorizedAccessException or InvalidDataException) { result.Extra["workspace_cleanup_error"] = error.Message; }
            }
        }
    }
    private static string ReadJava(string path) => System.Text.Encoding.UTF8.GetString(File.ReadAllBytes(path)).Replace("\r\n", "\n", StringComparison.Ordinal).Replace('\r', '\n');
}
