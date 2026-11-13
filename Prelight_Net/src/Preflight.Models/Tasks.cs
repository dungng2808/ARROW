namespace Preflight.Models;

public sealed record RepoSource(string RepoUrl, string CloneUrl, string Kind = "remote", string SnapshotPath = "", string ArchivePath = "", string ArchivePrefix = "");
public sealed record PairingEvidence(string Method, IReadOnlyDictionary<string, string>? Details = null);
public sealed record CandidateTask(ClassCandidate Candidate, string Dataset, RepoSource Repo, RevisionChoice? PinnedRevision, PairingEvidence Pairing, Dictionary<string, string>? Extra = null);
public sealed record NormalizationResult(IReadOnlyList<CandidateTask> Tasks, int RawCount, IReadOnlyList<Dictionary<string, string>> Rejections);
public sealed record RunContract(PreflightConfig Config, string Dataset, string InputRoot, IReadOnlyList<CandidateTask> Tasks, string ToolFingerprint, string CandidateManifestSha256);
