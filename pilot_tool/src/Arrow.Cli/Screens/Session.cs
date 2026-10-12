using Arrow.Core.Config;
using Arrow.Core.Runs;
using Arrow.Core.Shards;
using Arrow.Core.Util;
using Arrow.Infrastructure;

namespace Arrow.Cli.Screens;

/// <summary>Những gì màn hình nào cũng cần: thư mục, cấu hình máy, shard, run mặc định của máy.</summary>
public sealed class Session(ToolPaths paths)
{
    public ToolPaths Paths { get; } = paths;

    public MachineConfig? Machine => MachineConfig.TryLoad(Paths.MachineConfigPath);

    public string RunsDir => Paths.Resolve(Machine?.RunsDir ?? "runs");

    public ShardFile? TryLoadShard()
    {
        var machine = Machine;
        if (machine is null || string.IsNullOrEmpty(machine.Shard))
        {
            return null;
        }

        try
        {
            return ShardPlanner.LoadShard(Paths.Resolve(machine.Shard)).Shard;
        }
        catch (Exception ex) when (ex is ArrowException or IOException or System.Text.Json.JsonException)
        {
            return null;
        }
    }

    /// <summary>run_id mặc định của máy: &lt;plan&gt;-&lt;shard&gt;-&lt;máy&gt; (specs/01 §4).</summary>
    public string DefaultRunId(bool demo = false)
    {
        var machine = Machine?.MachineName ?? "may";
        if (demo)
        {
            return $"demo-{machine}";
        }

        var shard = TryLoadShard();
        return shard is null ? $"run-{machine}" : $"{shard.PlanId}-{shard.ShardId}-{machine}";
    }

    public RunStore Store(string runId) => new(RunsDir, runId);
}
