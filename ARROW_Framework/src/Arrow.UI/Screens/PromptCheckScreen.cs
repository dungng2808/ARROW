using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary><c>arrow prompts check</c>: liệt kê template và kết quả kiểm tra bộ prompt (spec 02 Mục 2.3).</summary>
public sealed class PromptCheckScreen(IPromptService prompts, ConsoleOutput output)
{
    public int Run()
    {
        var report = prompts.Check();
        output.Title("Kiểm tra bộ prompt");
        output.Table(
            ["File", "Id", "Version", "Vai trò", "sha256"],
            report.Files.Select(f => (IReadOnlyList<string>)[f.RelativePath, f.Id, f.Version, f.Role, f.Sha256[..12]]));
        output.Blank();
        if (report.Ok)
        {
            output.Status(Mark.Ok, "Bộ prompt nhất quán", $"{report.Files.Count} template; 3 prompt sinh chung phần yêu cầu và output contract; 2 user message chỉ khác khối cấu hình; prompt sửa dùng nguyên văn phần chung");
            return 0;
        }

        foreach (var problem in report.Problems)
        {
            output.Status(Mark.Fail, problem);
        }

        output.Line($"Trạng thái: {report.Problems.Count} vấn đề");
        return 1;
    }
}
