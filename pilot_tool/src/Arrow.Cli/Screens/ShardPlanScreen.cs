using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Shards;
using Arrow.Core.Util;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>Chia shard cho các máy (người điều phối, SETUP.md §3).</summary>
public sealed class ShardPlanScreen(Session session)
{
    public const string DefaultOwners = "Dung-mac=Dũng-mac,Dung-win=Dũng-win,KieuAnh=Kiều Anh,Quang,Chinh,Han=Hán";

    public int Run(string? experimentPath, string? owners, string? planId, string? outputDir, string? coordinator, bool assumeYes)
    {
        var paths = session.Paths;
        experimentPath ??= session.Machine?.Experiment is { Length: > 0 } fromMachine
            ? paths.Resolve(fromMachine)
            : Directory.GetFiles(paths.ExperimentsDir, "*.json").OrderBy(f => f).FirstOrDefault();
        if (experimentPath is null)
        {
            AnsiConsole.MarkupLine("[red]Không có cấu hình thực nghiệm. Dùng --experiment.[/]");
            return 1;
        }

        var experiment = ExperimentLoader.Load(paths.Resolve(experimentPath, System.Environment.CurrentDirectory));
        var ownerList = (owners ?? DefaultOwners).Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
            .Select(ShardOwner.Parse).ToList();
        planId ??= $"pilot-{ownerList.Count}";
        outputDir = outputDir is null ? Path.Combine(paths.ShardsDir, planId) : paths.Resolve(outputDir, System.Environment.CurrentDirectory);
        coordinator ??= ownerList[0].Id;

        var records = experiment.Config.Manifests
            .SelectMany(m => ManifestReader.Read(experiment.ResolveManifestPath(m), m.Dataset)).ToList();
        var result = ShardPlanner.Plan(experiment, records, ownerList, planId, coordinator);

        var datasets = experiment.Config.Manifests.Select(m => m.Dataset).ToList();
        var table = new Table().Border(TableBorder.Rounded).AddColumn("Shard").AddColumn("Người").AddColumn(new TableColumn("CUT").RightAligned());
        foreach (var dataset in datasets)
        {
            table.AddColumn(new TableColumn(dataset).RightAligned());
        }

        table.AddColumn(new TableColumn("Repository").RightAligned()).AddColumn(new TableColumn("Build ước tính").RightAligned());
        foreach (var shard in result.Plan.Shards)
        {
            var cells = new List<string> { shard.ShardId, Markup.Escape(shard.OwnerDisplay), shard.TaskCount.ToString() };
            cells.AddRange(datasets.Select(d => shard.TasksByDataset.GetValueOrDefault(d).ToString()));
            cells.Add(shard.RepositoryCount.ToString());
            cells.Add(Theme.Duration(shard.EstimatedBuildSeconds));
            table.AddRow(cells.ToArray());
        }

        var totals = new List<string> { "[bold]Tổng[/]", "", result.Plan.Shards.Sum(s => s.TaskCount).ToString() };
        totals.AddRange(datasets.Select(d => result.Plan.Shards.Sum(s => s.TasksByDataset.GetValueOrDefault(d)).ToString()));
        totals.Add(result.Plan.Shards.Sum(s => s.RepositoryCount).ToString());
        totals.Add("");
        table.AddRow(totals.ToArray());

        AnsiConsole.Write(new Rule($"[bold]Chia shard · {Markup.Escape(planId)}[/] · {Markup.Escape(experiment.Config.ExperimentId)}").LeftJustified());
        AnsiConsole.Write(table);
        if (!assumeYes && !AnsiConsole.Confirm($"Ghi {Markup.Escape(paths.Relative(outputDir))}/ ?"))
        {
            return 1;
        }

        ShardPlanner.Write(result, outputDir);
        ToolLog.Info($"shard plan: ghi {result.Shards.Count} shard ({string.Join(", ", result.Plan.Shards.Select(s => $"{s.ShardId}={s.Owner}:{s.TaskCount}"))}) vào {outputDir}, experiment {experiment.Sha256[..12]}");
        AnsiConsole.MarkupLine($"{Theme.Ok} Đã ghi {result.Shards.Count} shard và plan.json vào [bold]{Markup.Escape(outputDir)}[/]. Commit và push để các máy dùng.");
        return 0;
    }
}
