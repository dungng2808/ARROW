using Spectre.Console;

namespace Arrow.Cli.Ui;

/// <summary>Màu và ký hiệu dùng chung (CLI_DASHBOARD.md §2).</summary>
public static class Theme
{
    public static readonly string[] OutcomeOrder = ["VALID", "REGRESSION", "ASSERTION", "RUNTIME", "DISCOVERY", "UNKNOWN", "COMPILE", "INFRA"];

    public static Color OutcomeColor(string outcome) => outcome switch
    {
        "VALID" => Color.Green,
        "REGRESSION" or "ASSERTION" or "RUNTIME" => Color.Yellow,
        "DISCOVERY" or "UNKNOWN" or "COMPILE" => Color.Red,
        _ => Color.Grey,
    };

    public static string Ok => "[green]✔[/]";
    public static string Fail => "[red]✘[/]";
    public static string Warn => "[yellow]⚠[/]";
    public static string Skip => "[grey]–[/]";

    public static string Bar(int done, int total, int width)
    {
        if (total <= 0)
        {
            return new string('░', width);
        }

        var filled = (int)Math.Round(width * Math.Clamp(done / (double)total, 0, 1));
        return new string('█', filled) + new string('░', width - filled);
    }

    public static string Duration(double seconds)
    {
        var span = TimeSpan.FromSeconds(Math.Max(0, seconds));
        return $"{(int)span.TotalHours:00}:{span.Minutes:00}:{span.Seconds:00}";
    }

    public static void Logo()
    {
        AnsiConsole.Write(new FigletText("ARROW").Color(Color.Teal));
        AnsiConsole.MarkupLine($"[grey]Adaptive Repository-Aware Repair Workflow · pilot_tool v{typeof(Theme).Assembly.GetName().Version?.ToString(3)} · .NET {Environment.Version.Major} · {Arrow.Infrastructure.Tooling.MachineInfo.RuntimeId}[/]");
        AnsiConsole.WriteLine();
    }
}
