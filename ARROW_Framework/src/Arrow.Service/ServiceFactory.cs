using Arrow.DataAccess.Files;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Config;
using Arrow.Service.App;
using Arrow.Service.Block1;
using Arrow.Service.Block2;
using Arrow.Service.Block3;
using Arrow.Service.Block4;
using Arrow.Service.External;
using Arrow.Service.Interfaces;
using Arrow.Service.Pipeline;

namespace Arrow.Service;

/// <summary>Các dịch vụ tầng UI được gọi (spec 07 Mục 4.1).</summary>
public sealed record ArrowServices(
    FrameworkPaths Paths,
    IPromptService Prompts,
    IScreeningService Screening,
    IPipelineService Pipeline,
    IRunQueryService Runs,
    IReportService Reports,
    ISetupService Setup,
    IModelConfigService Models,
    IDoctorService Doctor,
    IRepairSubsetService RepairSubsets);

/// <summary>
/// Ghép các tầng (spec 07 Mục 9): tạo repository của Data Access, rồi các service, truyền phụ thuộc qua constructor.
/// UI chỉ nhận <see cref="ArrowServices"/>, không tham chiếu Data Access.
/// </summary>
public static class ServiceFactory
{
    /// <param name="fakeLlm">Model giả dùng khi run bật <c>--fake-llm</c> (null = <see cref="FakeLlmClient.ForPipeline"/>); để test thay câu trả lời.</param>
    public static ArrowServices Create(FrameworkPaths paths, bool verbose = false, Func<ILlmClient>? fakeLlm = null)
    {
        ILogRepository log = new FileLogRepository(paths, verbose);
        IPromptRepository prompts = new FilePromptRepository(paths);
        IConfigRepository config = new FileConfigRepository(paths);
        IRunRepository runs = new FileRunRepository(paths);
        ITaskResultRepository results = new FileTaskResultRepository(paths);
        IReportRepository reports = new FileReportRepository(paths);
        ILlmCacheRepository cache = new FileLlmCacheRepository(paths);

        IRepositoryAnalyzer CreateAnalyzer(ToolEnvironment tools, string workDir)
        {
            IProcessRunner runner = new ProcessRunner();
            IGitClient git = new GitClient(runner, tools.Git);
            return new RepositoryAnalyzer(git, new WorkspaceManager(git, runner, workDir), new BuildRunner(runner), new TestReportReader(), results, log);
        }

        RunComponents CreateComponents(RunSetup setup)
        {
            // Hàng đợi build: mọi tiến trình ngoài (Maven, Gradle, Java, git) của run đi qua cùng một giới hạn.
            IProcessRunner runner = new ThrottledProcessRunner(new ProcessRunner(), setup.Machine?.Workers.Build ?? 4);
            IGitClient git = new GitClient(runner, setup.Tools.Git);
            var workspace = new WorkspaceManager(git, runner, setup.WorkDir);
            var build = new BuildRunner(runner);
            var reader = new TestReportReader();
            var helper = new JavaHelperClient(JavaHelperOptions.Default(paths, setup.Machine), new ProcessRunner(), log);
            var generator = new TestGenerator(prompts, setup.Llm, helper, results);
            var verifier = new Verifier(workspace, build, reader, helper, results, log);
            var diagnoser = new FailureDiagnoser();
            var repair = new RepairRunner(generator, verifier, diagnoser, new ExtraContextBuilder(helper), helper, workspace, results, log);
            var toolJdk = setup.Tools.JdkHomes.GetValueOrDefault(17) ?? setup.Tools.JdkHomes.Values.LastOrDefault() ?? "";
            var tools = new ToolsCatalog(setup.ToolsDir, Path.Combine(paths.FrameworkRoot, "tools", "tools.lock.json"), setup.Tools.Maven, toolJdk, runner, log);
            var preparer = new SuitePreparer(workspace, build, results);
            var assessor = new QualityAssessor(
            [
                new JacocoTool(preparer, build, tools, runner, workspace, results),
                new PitTool(preparer, tools, runner, workspace, results, setup.Tools.JdkHomes),
                new TsDetectTool(tools, helper, runner, workspace, results),
            ], results, log);
            return new RunComponents(
                new RepositoryAnalyzer(git, workspace, build, reader, results, log),
                generator, verifier, diagnoser, repair,
                new EvoSuiteGenerator(preparer, tools, runner, workspace, results),
                assessor, helper, workspace);
        }

        var loader = new RunResultsLoader(runs, results);
        return new ArrowServices(
            paths,
            new PromptService(prompts, log),
            new ScreeningService(paths, config, new FileManifestRepository(), runs, results, reports, CreateAnalyzer, log),
            new PipelineService(paths, config, new FileManifestRepository(), prompts, runs, results, cache, log, CreateComponents, fakeLlm),
            new RunQueryService(runs, results, loader),
            new ReportService(loader, reports, log),
            new SetupService(paths, config, new ProcessRunner()),
            new ModelConfigService(paths, config, log),
            new DoctorService(paths, config, prompts, new ProcessRunner(), log),
            new RepairSubsetService(config, new FileManifestRepository(), runs, results, log));
    }

    /// <summary>Tìm thư mục <c>ARROW_Framework</c>: đi lên từ <paramref name="start"/> tới khi gặp <c>ArrowFramework.sln</c> hoặc cặp <c>prompts/</c> + <c>specs/</c>.</summary>
    public static FrameworkPaths? Locate(string start)
    {
        for (var dir = new DirectoryInfo(Path.GetFullPath(start)); dir is not null; dir = dir.Parent)
        {
            if (File.Exists(Path.Combine(dir.FullName, "ArrowFramework.sln"))
                || (Directory.Exists(Path.Combine(dir.FullName, "prompts")) && Directory.Exists(Path.Combine(dir.FullName, "specs"))))
            {
                return new FrameworkPaths(dir.FullName);
            }

            var nested = Path.Combine(dir.FullName, "ARROW_Framework");
            if (File.Exists(Path.Combine(nested, "ArrowFramework.sln")))
            {
                return new FrameworkPaths(nested);
            }
        }

        return null;
    }
}
