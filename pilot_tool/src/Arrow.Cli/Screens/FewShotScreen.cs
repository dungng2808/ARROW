using Arrow.Core.Util;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Java;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary><c>arrow fewshot select</c> (specs/04 Mục 5): chọn và ghi <c>prompts/fewshot/examples.json</c>.</summary>
public sealed class FewShotScreen(Session session)
{
    public async Task<int> RunAsync(string poolGlob, string exclude, int seed, CancellationToken cancellationToken)
    {
        var paths = session.Paths;
        var machine = session.Machine ?? throw new ArrowException("NO_MACHINE_CONFIG", "Máy chưa cài đặt. Chạy arrow setup trước.");
        var directory = Path.GetDirectoryName(poolGlob.Replace('\\', '/')) ?? "";
        var pattern = Path.GetFileName(poolGlob);
        var parentPattern = Path.GetFileName(directory);
        var grandParent = Path.Combine(paths.ArrowRoot, Path.GetDirectoryName(directory) ?? "");
        var pool = Directory.EnumerateDirectories(grandParent, parentPattern)
            .SelectMany(d => Directory.EnumerateFiles(d, pattern))
            .OrderBy(f => f, StringComparer.Ordinal).ToList();
        if (pool.Count == 0)
        {
            throw new ArrowException("POOL_MISSING", $"Không có file pool khớp {poolGlob}");
        }

        var excluded = exclude.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries).Select(e => Path.Combine(paths.ArrowRoot, e)).ToList();
        var git = new GitRepositoryProvider(machine.GitPath, paths.Resolve(machine.WorkDir), machine.RepoPolicy);
        var selector = new FewShotSelector(git, new JavaAnalyzer(paths.PilotToolDir, machine));
        var set = await selector.SelectAsync(pool, excluded, seed, line =>
        {
            ToolLog.Info("fewshot: " + line);
            AnsiConsole.MarkupLine("[grey]" + Markup.Escape(line) + "[/]");
        }, cancellationToken).ConfigureAwait(false);
        var output = Path.Combine(paths.PilotToolDir, "prompts", "fewshot", "examples.json");
        Json.WriteAtomic(output, set);
        foreach (var example in set.Examples)
        {
            AnsiConsole.MarkupLine($"{Ui.Theme.Ok} {example.TestingFramework}: [bold]{Markup.Escape(example.TaskId)}[/] {Markup.Escape(example.RepoUrl)}");
        }

        AnsiConsole.MarkupLine($"Đã ghi {Markup.Escape(paths.Relative(output))} ({set.Candidates} ứng viên trong pool). Commit file này.");
        return 0;
    }
}
