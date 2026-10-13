using Arrow.DataAccess.Files;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Config;
using Arrow.Service.App;
using Arrow.Service.Interfaces;

namespace Arrow.Service;

/// <summary>Các dịch vụ tầng UI được gọi (spec 07 Mục 4.1). Bước C1 mới có <see cref="Prompts"/>; các dịch vụ khác thêm ở bước sau.</summary>
public sealed record ArrowServices(FrameworkPaths Paths, IPromptService Prompts);

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
        return new ArrowServices(paths, new PromptService(prompts, log));
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
