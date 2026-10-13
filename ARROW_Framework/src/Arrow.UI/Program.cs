using System.Reflection;
using System.Text;
using Arrow.Models.Common;
using Arrow.Service;
using Arrow.UI;
using Arrow.UI.Screens;

Console.OutputEncoding = Encoding.UTF8;
if (!Console.IsInputRedirected)
{
    Console.InputEncoding = Encoding.UTF8;
}

var output = ConsoleOutput.FromConsole();
var version = Assembly.GetExecutingAssembly().GetName().Version?.ToString(3) ?? "0.0.0";

try
{
    var command = CommandLine.Parse(args);
    if (command.Flag("version"))
    {
        output.Line($"arrow-fw {version}");
        return 0;
    }

    var paths = command.Option("root") is { } root
        ? new Arrow.Models.Config.FrameworkPaths(Path.GetFullPath(root))
        : ServiceFactory.Locate(AppContext.BaseDirectory) ?? ServiceFactory.Locate(Environment.CurrentDirectory);
    if (paths is null)
    {
        output.Status(Mark.Fail, "Không tìm thấy thư mục ARROW_Framework", "chạy trong thư mục đó hoặc dùng --root <thư mục>");
        return 2;
    }

    var services = ServiceFactory.Create(paths, command.Flag("verbose"));

    if (command.Is("prompts", "check"))
    {
        return new PromptCheckScreen(services.Prompts, output).Run();
    }

    if (command.Command.Count == 0 || command.Flag("help"))
    {
        output.Title($"ARROW Framework v{version} · .NET {Environment.Version.ToString(2)}");
        output.Line($"Thư mục: {paths.FrameworkRoot}");
        output.Blank();
        output.Line("Lệnh hiện có (bước C1):");
        output.Line("  arrow-fw prompts check   Kiểm tra bộ prompt trong prompts/");
        output.Line("  arrow-fw --version       In phiên bản");
        output.Line("Menu và các lệnh run, report, setup… có từ các bước sau (spec 07 Mục 11).");
        return 0;
    }

    output.Status(Mark.Fail, $"Lệnh không hỗ trợ: {string.Join(' ', command.Command)}", "xem arrow-fw --help");
    return 2;
}
catch (ArrowException ex)
{
    output.Status(Mark.Fail, ex.Code, ex.Message);
    return 2;
}
catch (ArgumentException ex)
{
    output.Status(Mark.Fail, "Tham số", ex.Message);
    return 2;
}
catch (InputClosedException)
{
    return 3;
}
