using Arrow.Models.Config;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Pipeline;

/// <summary>Những gì cần biết để dựng các khối cho một run: công cụ của máy, thư mục, cấu hình, lớp gọi model đã bọc.</summary>
public sealed record RunSetup(
    string RunId,
    ToolEnvironment Tools,
    string WorkDir,
    string ToolsDir,
    MachineConfig? Machine,
    LoadedExperiment Experiment,
    ILlmClient Llm);

/// <summary>Các khối của một run (spec 07 Mục 4.2), dùng chung một tiến trình java-helper và một hàng đợi build.</summary>
public sealed class RunComponents(
    IRepositoryAnalyzer analyzer,
    ITestGenerator generator,
    IVerifier verifier,
    IFailureDiagnoser diagnoser,
    IRepairRunner repair,
    IComparisonGenerator? evoSuite,
    IQualityAssessor assessor,
    IJavaHelper javaHelper,
    IWorkspaceManager workspace,
    IAsyncDisposable? extra = null) : IAsyncDisposable
{
    public IRepositoryAnalyzer Analyzer => analyzer;
    public ITestGenerator Generator => generator;
    public IVerifier Verifier => verifier;
    public IFailureDiagnoser Diagnoser => diagnoser;
    public IRepairRunner Repair => repair;
    public IComparisonGenerator? EvoSuite => evoSuite;
    public IQualityAssessor Assessor => assessor;
    public IJavaHelper JavaHelper => javaHelper;
    public IWorkspaceManager Workspace => workspace;

    public async ValueTask DisposeAsync()
    {
        await javaHelper.DisposeAsync().ConfigureAwait(false);
        if (extra is not null)
        {
            await extra.DisposeAsync().ConfigureAwait(false);
        }
    }
}
