using System.Globalization;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Runner;

if (args.Length == 0 || args[0] is "--help" or "-h" or "help")
{
    Console.WriteLine("""
    preflight-net normalize --dataset classes2test|gitbug|catlm --config <json> --output-dir <run> [--input-root <path>] [--limit N]
    preflight-net run --output-dir <run> [--dataset <name> --config <json> --input-root <path>] [--workers N] [--revision-map <csv>] [--shard <json> | --max-classes N] [--fast]
    preflight-net resume --output-dir <run> [--workers N]
    preflight-net reopen --output-dir <run> --status <infrastructure-status> --since <utc> --reason <text>
    preflight-net sweep --output-dir <run>
    preflight-net summarize --run-dir <run> --output-dir <summary>
    preflight-net export-shards --index <sqlite> --output-dir <dir> [--parts 5]
    preflight-net split-dataset --source <dataset> --output-dir <dir> [--parts 5]
    preflight-net rerun --previous-run <run> --output-dir <new> [--status <S> ...] [--new-only] [--workers N]
    preflight-net merge --run <old> --run <new> --output-dir <dir>
    preflight-net convert-config <legacy.toml> [--output <json>]
    Relative paths resolve from Prelight_Net. Outputs must stay inside this workspace.
    """); return 0;
}
using var cancellation = new CancellationTokenSource();
Console.CancelKeyPress += (_, e) => { e.Cancel = true; cancellation.Cancel(); };
try
{
    Environment.CurrentDirectory = RuntimePaths.Root;
    var options = new Dictionary<string, List<string>>(StringComparer.Ordinal); var positional = new List<string>();
    string[] flags = ["fast", "new-only", "offline", "keep-workspaces", "keep-repo-cache"];
    for (int i = 1; i < args.Length; i++)
    {
        if (!args[i].StartsWith("--", StringComparison.Ordinal)) { positional.Add(args[i]); continue; }
        string key = args[i][2..]; string value = "true";
        if (!flags.Contains(key, StringComparer.Ordinal)) { if (++i >= args.Length || args[i].StartsWith("--", StringComparison.Ordinal)) throw new ArgumentException("Missing value for --" + key); value = args[i]; }
        if (!options.TryGetValue(key, out var values)) options[key] = values = []; values.Add(value);
    }
    string? O(string key) => options.TryGetValue(key, out var values) ? values.Last() : null;
    string R(string key) => O(key) ?? throw new ArgumentException("Missing --" + key);
    int N(string key, int fallback = 0) => O(key) is { } value ? int.Parse(value, CultureInfo.InvariantCulture) : fallback;
    bool B(string key) => O(key) == "true";
    string[] allowed = args[0] switch
    {
        "normalize" or "run" => ["dataset", "config", "output-dir", "input-root", "limit", "workers", "revision-map", "shard", "max-classes", "fast", "offline", "keep-workspaces", "keep-repo-cache", "probe-profile"],
        "resume" => ["output-dir", "workers"], "reopen" => ["output-dir", "status", "since", "reason"], "sweep" => ["output-dir"],
        "summarize" => ["run-dir", "output-dir"], "export-shards" => ["index", "output-dir", "parts"], "split-dataset" => ["source", "input-root", "output-dir", "parts"],
        "rerun" => ["previous-run", "output-dir", "status", "new-only", "workers", "input-root", "config", "revision-map"], "merge" => ["run", "output-dir"], "convert-config" => ["output"], _ => []
    };
    foreach (string key in options.Keys) if (!allowed.Contains(key, StringComparer.Ordinal)) throw new ArgumentException("Unknown option for " + args[0] + ": --" + key);
    string Output(string key = "output-dir")
    {
        string path = Path.GetFullPath(R(key));
        if (!ResolvePath.IsWithin(path, RuntimePaths.Root) || path.Equals(RuntimePaths.Root, StringComparison.OrdinalIgnoreCase)) throw new InvalidDataException("Output must be a child of Prelight_Net");
        return path;
    }
    var service = new PreflightService(); string command = args[0]; var ct = cancellation.Token;
    if (command == "convert-config")
    {
        string input = positional.Single(), output = Path.GetFullPath(O("output") ?? Path.GetFileNameWithoutExtension(input) + ".json");
        if (!ResolvePath.IsWithin(output, RuntimePaths.Root)) throw new InvalidDataException("Output must stay in Prelight_Net"); AuxiliaryCommands.ConvertConfig(input, output); return 0;
    }
    string run = Output(); int? workers = O("workers") is null ? null : N("workers");
    switch (command)
    {
        case "normalize":
        case "run":
            if (command == "normalize" || !File.Exists(Path.Combine(run, "state/normalize.json")))
            {
                var config = ConfigLoader.Load(R("config"), Console.Error.WriteLine);
                config = config with { Run = config.Run with { Offline = B("offline") || config.Run.Offline, KeepRepoCache = B("keep-repo-cache") || config.Run.KeepRepoCache, KeepWorkspaces = B("keep-workspaces") || config.Run.KeepWorkspaces, ProbeProfile = O("probe-profile") is { } profile ? Enum.Parse<ProbeProfile>(profile) : config.Run.ProbeProfile } };
                var result = await service.NormalizeAsync(R("dataset"), config, run, O("input-root"), N("limit"), ct); Console.WriteLine($"Normalized {result.RawCount} inputs -> {result.Tasks.Count} classes; {result.Rejections.Count} rejections");
            }
            if (command == "normalize") return 0;
            return await service.RunAsync(run, false, workers, B("fast"), O("revision-map"), O("shard"), N("max-classes"), ct);
        case "resume":
            if (options.Keys.Any(k => k is not ("output-dir" or "workers"))) throw new ArgumentException("resume only permits --output-dir and --workers");
            return await service.RunAsync(run, true, workers, false, null, null, 0, ct);
        case "export-shards": AuxiliaryCommands.ExportShards(R("index"), run, N("parts", 5)); return 0;
        case "split-dataset": await AuxiliaryCommands.SplitAsync(O("source") ?? R("input-root"), run, N("parts", 5), ct); return 0;
        case "summarize": AuxiliaryCommands.Summarize(R("run-dir"), run); return 0;
        case "merge": AuxiliaryCommands.Merge(options.GetValueOrDefault("run") ?? throw new ArgumentException("Missing --run"), run); return 0;
        case "rerun": return await AuxiliaryCommands.RerunAsync(R("previous-run"), run, options.GetValueOrDefault("status")?.ToArray() ?? [], B("new-only"), workers, ct, O("input-root"), O("config"), O("revision-map"));
        case "sweep":
        case "reopen":
            using (var runLock = new RunLock(Path.Combine(run, "state/run.lock")))
            using (var checkpoint = new CheckpointStore(Path.Combine(run, "state/run_state.sqlite")))
            {
                var contract = checkpoint.Contract(); checkpoint.Recover();
                if (command == "sweep") await RunMaintenance.SweepAsync(run, contract, checkpoint, ct);
                else
                {
                    string status = R("status");
                    if (status is not ("CLONE_FAILED" or "CHECKOUT_FAILED" or "INTERNAL_ERROR")) throw new ArgumentException("reopen is restricted to infrastructure statuses; use rerun for build failures");
                    int count = checkpoint.Reopen(status, R("since"), R("reason")); PreflightService.UpdateProvenance(run, checkpoint, contract, "REOPENED", R("reason")); Console.WriteLine($"Reopened {count} tasks; previous results retained in audit_events");
                }
            }
            return 0;
        default: throw new ArgumentException("Unknown command: " + command);
    }
}
catch (OperationCanceledException) { Console.Error.WriteLine("Interrupted; use resume with the same output directory."); return 130; }
catch (Exception error) { Console.Error.WriteLine(error.GetType().Name + ": " + error.Message); return 1; }
