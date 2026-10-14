using System.Reflection;
using System.Text;
using Arrow.Models.Common;
using Arrow.Service;
using Arrow.UI;

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
    // S5 chỉ chờ lệnh gõ khi có bàn phím và màn hình (spec 06 Mục 7).
    var interactive = !Console.IsInputRedirected && !Console.IsOutputRedirected;
    var commands = new Commands(services, ConsoleInput.FromConsole(), output, version, interactive);
    if (command.Flag("help"))
    {
        commands.PrintHelp();
        return 0;
    }

    return await commands.RunAsync(command);
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
