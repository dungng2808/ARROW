using Arrow.DataAccess.Files;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Config;
using Arrow.Service.App;
using Arrow.Service.Block1;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service;

/// <summary>Các dịch vụ tầng UI được gọi (spec 07 Mục 4.1). Tới bước C3 có <see cref="Prompts"/> và <see cref="Screening"/>; các dịch vụ khác thêm ở bước sau.</summary>
public sealed record ArrowServices(FrameworkPaths Paths, IPromptService Prompts, IScreeningService Screening);

/// <summary>
/// Ghép các tầng (spec 07 Mục 9): tạo repository của Data Access, rồi các service, truyền phụ thuộc qua constructor.
/// UI chỉ nhận <see cref="ArrowServices"/>, không tham chiếu Data Access.
/// </summary>
public static class ServiceFactory
{
    public static ArrowServices Create(FrameworkPaths paths, bool verbose = false)
    {
        ILogRepository log = new FileLogRepository(paths, verbose);
        IPromptRepository prompts = new FilePromptRepository(paths);
        IConfigRepository config = new FileConfigRepository(paths);
        IRunRepository runs = new FileRunRepository(paths);
        ITaskResultRepository results = new FileTaskResultRepository(paths);
        IReportRepository reports = new FileReportRepository(paths);

        IRepositoryAnalyzer CreateAnalyzer(ToolEnvironment tools, string workDir)
        {
            IProcessRunner runner = new ProcessRunner();
            IGitClient git = new GitClient(runner, tools.Git);
            return new RepositoryAnalyzer(git, new WorkspaceManager(git, runner, workDir), new BuildRunner(runner), new TestReportReader(), results, log);
        }

        var screening = new ScreeningService(paths, config, new FileManifestRepository(), runs, results, reports, CreateAnalyzer, log);
        return new ArrowServices(paths, new PromptService(prompts, log), screening);
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
