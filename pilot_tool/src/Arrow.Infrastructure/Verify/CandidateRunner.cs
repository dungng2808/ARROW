using Arrow.Core.Config;
using Arrow.Core.Prepare;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Git;

namespace Arrow.Infrastructure.Verify;

/// <summary>Một ứng viên cần build: mã, tên class, nơi ghi log.</summary>
public sealed record CandidateSpec
{
    public required string Code { get; init; }
    public required string TestPath { get; init; }
    public required string TestClassFqn { get; init; }
    public required string LogKey { get; init; }
    public IReadOnlyDictionary<string, string> ExtraFiles { get; init; } = new Dictionary<string, string>();
    public IReadOnlyList<string> ExtraBuildArgs { get; init; } = [];

    /// <summary>Sửa workspace trước khi xác minh (ngoại lệ đã khai báo của EvoSuite, specs/06 Mục 1 bước 5).</summary>
    public Func<string, Task>? PrepareWorkspace { get; init; }
}

/// <summary>
/// Xác minh một ứng viên trên workspace sạch riêng (git worktree tại <c>checkout_sha</c>), xóa workspace khi xong
/// (giữ lại nếu <c>keep_failed_workspaces</c> và kết quả là INFRA) (specs/02 Mục 2, specs/03).
/// </summary>
public sealed class CandidateRunner(GitRepositoryProvider git, string workDir, MachineConfig machine, FiveStepVerifier verifier)
{
    public FiveStepVerifier Verifier => verifier;

    public async Task<VerifyResult> VerifyAsync(TaskInfo task, Baseline baseline, string runId, string jobHash, string taskDir,
        CandidateSpec candidate, Action<string> reportStep, CancellationToken cancellationToken)
    {
        reportStep("git");
        await git.EnsureCommitAsync(task.RepoUrl, task.CheckoutSha, cancellationToken).ConfigureAwait(false);
        var workspace = Path.Combine(workDir, "ws", runId, jobHash);
        await git.AddWorktreeAsync(task.RepoUrl, task.CheckoutSha, workspace, cancellationToken).ConfigureAwait(false);
        var keep = false;
        try
        {
            if (candidate.PrepareWorkspace is not null)
            {
                await candidate.PrepareWorkspace(workspace).ConfigureAwait(false);
            }

            var result = await verifier.VerifyAsync(new VerifyRequest
            {
                Task = task,
                Baseline = baseline,
                Workspace = workspace,
                TestPath = candidate.TestPath,
                TestClassFqn = candidate.TestClassFqn,
                Code = candidate.Code,
                ExtraFiles = candidate.ExtraFiles,
                ExtraBuildArgs = candidate.ExtraBuildArgs,
                LogsDir = Path.Combine(taskDir, "logs"),
                LogKey = candidate.LogKey,
                TaskDir = taskDir,
            }, reportStep, cancellationToken).ConfigureAwait(false);
            keep = machine.KeepFailedWorkspaces && result.State == VerificationState.Infra;
            return result;
        }
        finally
        {
            if (!keep)
            {
                await git.RemoveWorktreeAsync(task.RepoUrl, workspace, CancellationToken.None).ConfigureAwait(false);
            }
        }
    }
}
