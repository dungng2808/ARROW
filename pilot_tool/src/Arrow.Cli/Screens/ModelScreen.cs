using System.Globalization;
using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Llm;
using Arrow.Core.Runs;
using Arrow.Core.Shards;
using Arrow.Core.Util;
using Arrow.Infrastructure.Llm;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>
/// Model, provider, khóa API, giá: chọn model nào chạy (danh sách <c>models</c> của cấu hình thực nghiệm, dùng chung, commit),
/// mỗi model gọi provider nào bằng khóa nào (<c>litellm.config.local.yaml</c>, riêng máy này, không commit), và giá mỗi triệu token.
/// </summary>
public sealed class ModelScreen(Session session)
{
    private const string Done = "Xong";
    private const string AddModel = "Thêm model vào cấu hình thực nghiệm";
    private const string RemoveModel = "Bỏ model khỏi cấu hình thực nghiệm";
    private const string SwitchExperiment = "Đổi file cấu hình thực nghiệm";
    private const string RemoveUnused = "Xóa provider của model không còn dùng";

    private static readonly CultureInfo Inv = CultureInfo.InvariantCulture;

    public async Task<int> RunAsync(string? experimentPath, CancellationToken cancellationToken)
    {
        var paths = session.Paths;
        experimentPath = experimentPath is not null
            ? paths.Resolve(experimentPath, Environment.CurrentDirectory)
            : session.Machine?.Experiment is { Length: > 0 } fromMachine
                ? paths.Resolve(fromMachine)
                : Directory.GetFiles(paths.ExperimentsDir, "*.json").OrderBy(f => f).FirstOrDefault();
        if (experimentPath is null || !File.Exists(experimentPath))
        {
            AnsiConsole.MarkupLine("[red]Không có cấu hình thực nghiệm trong pilot_tool/experiments/.[/]");
            return 1;
        }

        while (true)
        {
            var experiment = ExperimentLoader.Load(experimentPath);
            var llm = LoadLlm();
            AnsiConsole.Clear();
            Theme.Logo();
            Render(experiment, llm);

            var models = experiment.Config.Models;
            var choices = new List<string>();
            choices.AddRange(models.Select(m => ProviderChoice(m.Name)));
            choices.AddRange(models.Select(m => PriceChoice(m.Name)));
            choices.AddRange(models.Select(m => TestChoice(m.Name)));
            choices.Add(AddModel);
            if (models.Count > 1)
            {
                choices.Add(RemoveModel);
            }

            if (llm.Routes.Any(r => models.All(m => m.Name != r.ModelName)))
            {
                choices.Add(RemoveUnused);
            }

            if (Directory.GetFiles(paths.ExperimentsDir, "*.json").Length > 1)
            {
                choices.Add(SwitchExperiment);
            }

            choices.Add(Done);
            var choice = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("Chọn việc:").PageSize(16).AddChoices(choices));
            AnsiConsole.WriteLine();
            try
            {
                switch (choice)
                {
                    case Done:
                        return 0;
                    case SwitchExperiment:
                    {
                        var files = Directory.GetFiles(paths.ExperimentsDir, "*.json").OrderBy(f => f).ToList();
                        var names = files.Select(f => Path.GetFileName(f)!).ToList();
                        experimentPath = files[names.IndexOf(AnsiConsole.Prompt(new SelectionPrompt<string>().Title("Cấu hình thực nghiệm:").AddChoices(names)))];
                        continue;
                    }
                    case AddModel:
                        AddNewModel(experiment);
                        break;
                    case RemoveModel:
                    {
                        var name = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("Bỏ model nào?").AddChoices(models.Select(m => m.Name)));
                        if (AnsiConsole.Confirm($"Bỏ [bold]{Markup.Escape(name)}[/] khỏi {Markup.Escape(Path.GetFileName(experiment.Path))}? Model này sẽ không chạy nữa.", false))
                        {
                            EditExperiment(experiment, json => ExperimentModelEditor.RemoveModel(json, name), $"bỏ model {name}");
                        }

                        break;
                    }
                    case RemoveUnused:
                    {
                        var unused = llm.Routes.Where(r => models.All(m => m.Name != r.ModelName)).Select(r => r.ModelName).ToList();
                        if (AnsiConsole.Confirm($"Xóa provider và khóa của {Markup.Escape(string.Join(", ", unused))} khỏi litellm.config.local.yaml?", false))
                        {
                            SaveLlm(unused.Aggregate(llm, (c, name) => c.WithoutRoute(name)), $"xóa provider của {string.Join(", ", unused)}");
                        }

                        break;
                    }
                    default:
                    {
                        var model = models.First(m => choice == ProviderChoice(m.Name) || choice == PriceChoice(m.Name) || choice == TestChoice(m.Name));
                        if (choice == ProviderChoice(model.Name))
                        {
                            ConfigureRoute(model.Name);
                        }
                        else if (choice == PriceChoice(model.Name))
                        {
                            SetPrice(experiment, model);
                        }
                        else
                        {
                            await TestCallAsync(experiment, model, cancellationToken);
                        }

                        break;
                    }
                }
            }
            catch (ArrowException ex)
            {
                AnsiConsole.MarkupLine($"[red]{Markup.Escape(ex.Code)}[/]: {Markup.Escape(ex.Message)}");
            }

            WaitKey();
        }
    }

    private static string ProviderChoice(string model) => $"Provider và khóa API của {model}";

    private static string PriceChoice(string model) => $"Giá của {model}";

    private static string TestChoice(string model) => $"Gọi thử {model} (1 lượt)";

    // ── Bảng tổng quan ──────────────────────────────────────────────────
    private void Render(LoadedExperiment experiment, LiteLlmConfigFile llm)
    {
        AnsiConsole.Write(new Panel(new Markup(
                "[bold]Model chạy, tham số và giá[/] nằm trong cấu hình thực nghiệm (dùng chung cả nhóm, commit lên Git): sửa thì sha256 đổi, phải chia lại shard và các máy git pull.\n" +
                "[bold]Provider, endpoint, khóa API[/] nằm trong litellm.config.local.yaml của máy này (không commit); sửa lúc nào cũng được, áp dụng từ lần chạy sau."))
            .Header($" Model · {Markup.Escape(Path.GetFileName(experiment.Path))} · sha {experiment.Sha256[..12]} ").Border(BoxBorder.Rounded).Expand());

        var table = new Table().Border(TableBorder.Rounded)
            .AddColumns("Model (tên trong thực nghiệm) · tham số", "Giá USD / 1M token", "Provider (máy này)", "Khóa API");
        foreach (var model in experiment.Config.Models)
        {
            var route = llm.Route(model.Name);
            var parameters = string.Join(", ", model.Params.Select(p => $"{p.Key}={p.Value}"));
            var price = model.PriceUsdPerMillion is { } p
                ? $"vào {p.Input.ToString("0.####", Inv)}\nra {p.Output.ToString("0.####", Inv)}"
                : "[yellow]chưa có giá[/]";
            table.AddRow(
                $"[bold]{Markup.Escape(model.Name)}[/]\n[grey]{Markup.Escape(parameters)}\nmodel id phải bắt đầu: {Markup.Escape(model.ExpectedModelIdPrefix)}[/]",
                price,
                route is null ? $"{Theme.Fail} chưa cài" : Markup.Escape($"{KindLabel(route.Kind)}\n{route.ProviderModel}\n{route.EndpointHost}"),
                route is null ? "" : route.HasApiKey ? $"{Theme.Ok} …{Markup.Escape(route.ApiKey.Length > 4 ? route.ApiKey[^4..] : "")}" : route.NeedsApiKey ? $"{Theme.Fail} chưa có" : "[grey]không cần[/]");
        }

        AnsiConsole.Write(table);
        var unused = llm.Routes.Where(r => experiment.Config.Models.All(m => m.Name != r.ModelName)).ToList();
        if (unused.Count > 0)
        {
            AnsiConsole.MarkupLine($"[grey]Có trong litellm.config.local.yaml nhưng không chạy trong cấu hình này: {Markup.Escape(string.Join(", ", unused.Select(r => $"{r.ModelName} ({r.EndpointHost})")))}[/]");
        }

        if (experiment.Config.Kind == "pilot" && experiment.Config.Models.Count > 1)
        {
            AnsiConsole.MarkupLine("[yellow]Pilot đã chốt chỉ chạy GPT-6 Luna (12/10/2026); thêm model khác vào cấu hình pilot là đổi quyết định đó.[/]");
        }
    }

    private static string KindLabel(LlmProviderKind kind) => kind switch
    {
        LlmProviderKind.OpenAiCompatible => "tương thích OpenAI",
        LlmProviderKind.Ollama => "Ollama",
        _ => "khác",
    };

    // ── Provider và khóa (máy này) ──────────────────────────────────────
    /// <summary>Hỏi provider, endpoint, khóa của một model rồi lưu ngay vào litellm.config.local.yaml (dùng cả ở arrow setup bước 6).</summary>
    public void ConfigureRoute(string modelName)
    {
        var llm = LoadLlm();
        var current = llm.Route(modelName);
        const string openAi = "Endpoint tương thích OpenAI (TeamoRouter, OpenRouter, OpenAI…), cần khóa API";
        const string ollama = "Ollama tự host (ví dụ Qwen3-Coder-30B), không cần khóa";
        const string other = "Khác: nhập chuỗi model của LiteLLM (anthropic/…, hosted_vllm/…)";
        var kinds = new Dictionary<string, LlmProviderKind> { [openAi] = LlmProviderKind.OpenAiCompatible, [ollama] = LlmProviderKind.Ollama, [other] = LlmProviderKind.Other };
        var defaultKind = current?.Kind ?? (modelName.Contains("qwen", StringComparison.OrdinalIgnoreCase) ? LlmProviderKind.Ollama : LlmProviderKind.OpenAiCompatible);
        var kind = kinds[AnsiConsole.Prompt(new SelectionPrompt<string>().Title($"Provider của [bold]{Markup.Escape(modelName)}[/]:")
            .AddChoices(kinds.Keys).DefaultValue(kinds.First(k => k.Value == defaultKind).Key))];

        var sameKind = current is not null && current.Kind == kind;
        string providerModel;
        switch (kind)
        {
            case LlmProviderKind.OpenAiCompatible:
            {
                var name = AnsiConsole.Prompt(new TextPrompt<string>("Tên model ở provider:")
                    .DefaultValue(sameKind ? current!.ProviderModel["openai/".Length..] : modelName)).Trim();
                providerModel = "openai/" + name;
                break;
            }
            case LlmProviderKind.Ollama:
            {
                var name = AnsiConsole.Prompt(new TextPrompt<string>("Tên model trong Ollama (ollama list):")
                    .DefaultValue(sameKind ? current!.ProviderModel[(current.ProviderModel.IndexOf('/') + 1)..] : "qwen3-coder:30b")).Trim();
                providerModel = "ollama_chat/" + name;
                break;
            }
            default:
                providerModel = AnsiConsole.Prompt(new TextPrompt<string>("Chuỗi model LiteLLM (provider/model):")
                    .DefaultValue(sameKind ? current!.ProviderModel : "")
                    .Validate(v => v.Contains('/') ? ValidationResult.Success() : ValidationResult.Error("Dạng provider/model, ví dụ anthropic/claude-…"))).Trim();
                break;
        }

        var defaultBase = sameKind && current!.ApiBase.Length > 0
            ? current.ApiBase
            : kind == LlmProviderKind.Ollama ? "http://localhost:11434" : llm.Routes.FirstOrDefault(r => r.Kind == kind)?.ApiBase ?? "https://api.openai.com/v1";
        var apiBase = AnsiConsole.Prompt(new TextPrompt<string>("Endpoint của provider (api_base):").DefaultValue(defaultBase)
            .Validate(v => Uri.TryCreate(v, UriKind.Absolute, out _) ? ValidationResult.Success() : ValidationResult.Error("Địa chỉ không hợp lệ"))).Trim();

        var route = new LiteLlmRoute { ModelName = modelName, ProviderModel = providerModel, ApiBase = apiBase };
        if (route.NeedsApiKey)
        {
            // Model khác cùng endpoint đã có khóa: gợi ý dùng chung.
            var sharing = llm.Routes.FirstOrDefault(r => r.ModelName != modelName && r.HasApiKey && r.EndpointHost == route.EndpointHost);
            var keep = current is { HasApiKey: true } && current.EndpointHost == route.EndpointHost ? current.ApiKey : "";
            var label = keep.Length > 0 ? $"Khóa API ({LiteLlmConfigFile.Mask(keep)}; Enter để giữ):" : "Khóa API:";
            if (keep.Length == 0 && sharing is not null && AnsiConsole.Confirm($"Dùng chung khóa của {Markup.Escape(sharing.ModelName)} ({Markup.Escape(LiteLlmConfigFile.Mask(sharing.ApiKey))}, cùng {Markup.Escape(route.EndpointHost)})?"))
            {
                keep = sharing.ApiKey;
                label = "";
            }

            var key = keep;
            if (label.Length > 0)
            {
                var prompt = new TextPrompt<string>(label).Secret();
                if (keep.Length > 0 || kind == LlmProviderKind.Other)
                {
                    prompt.AllowEmpty();
                }

                var typed = AnsiConsole.Prompt(prompt).Trim();
                key = typed.Length > 0 ? typed : keep;
            }

            route = route with { ApiKey = key };
        }

        SaveLlm(llm.WithRoute(route), $"provider của {modelName}: {providerModel} · {route.EndpointHost} · khóa {(route.HasApiKey ? LiteLlmConfigFile.Mask(route.ApiKey) : "không")}");
        AnsiConsole.MarkupLine("[grey]Chọn \"Gọi thử\" để kiểm tra khóa và endpoint (tốn 1 lượt gọi rất nhỏ).[/]");
    }

    public LiteLlmConfigFile LoadLlm()
    {
        var llm = LiteLlmConfigFile.TryLoad(session.Paths.LiteLlmConfigPath) ?? new LiteLlmConfigFile();
        return llm.MasterKey.Length > 0 ? llm : llm with { MasterKey = LiteLlmConfigFile.NewMasterKey() };
    }

    private void SaveLlm(LiteLlmConfigFile llm, string what)
    {
        llm.Save(session.Paths.LiteLlmConfigPath);
        ToolLog.Info($"models: {what}");
        AnsiConsole.MarkupLine($"{Theme.Ok} Đã lưu litellm.config.local.yaml ({Markup.Escape(what)}).");
        if (session.Machine?.Llm.ProxyMode == "external")
        {
            AnsiConsole.MarkupLine("[yellow]Proxy đang ở chế độ external: khởi động lại proxy của bạn để nhận cấu hình mới.[/]");
        }
    }

    // ── Giá và danh sách model (cấu hình thực nghiệm, dùng chung) ───────
    private void SetPrice(LoadedExperiment experiment, ModelConfig model)
    {
        AnsiConsole.MarkupLine("[grey]Giá USD cho 1 triệu token, lấy theo bảng giá của provider. Giá chỉ dùng để tính chi phí trong báo cáo, không ảnh hưởng kết quả. Model tự host (Ollama) để 0.[/]");
        var current = model.PriceUsdPerMillion;
        var input = AskPrice("Giá token vào (input):", current?.Input);
        var output = AskPrice("Giá token ra (output):", current?.Output);
        if (current is not null && current.Input == input && current.Output == output)
        {
            AnsiConsole.MarkupLine("[grey]Giá không đổi.[/]");
            return;
        }

        EditExperiment(experiment, json => ExperimentModelEditor.SetPrice(json, model.Name, new PriceConfig { Input = input, Output = output }),
            $"giá {model.Name} = {input.ToString(Inv)} / {output.ToString(Inv)} USD mỗi 1M token");
    }

    private static double AskPrice(string label, double? current)
    {
        var prompt = new TextPrompt<string>(label)
            .Validate(v => double.TryParse(v.Replace(',', '.'), NumberStyles.Float, Inv, out var d) && d >= 0 ? ValidationResult.Success() : ValidationResult.Error("Nhập số ≥ 0, ví dụ 0.05"));
        if (current is not null)
        {
            prompt.DefaultValue(current.Value.ToString(Inv));
        }

        return double.Parse(AnsiConsole.Prompt(prompt).Replace(',', '.'), NumberStyles.Float, Inv);
    }

    private void AddNewModel(LoadedExperiment experiment)
    {
        AnsiConsole.MarkupLine("[grey]Tên model là tên tool gửi tới LiteLLM và ghi vào kết quả (ví dụ qwen3-coder-30b). Mỗi model sẽ chạy đủ mọi quy trình sinh và cơ chế sửa.[/]");
        var name = AnsiConsole.Prompt(new TextPrompt<string>("Tên model:")
            .Validate(v => System.Text.RegularExpressions.Regex.IsMatch(v, "^[A-Za-z0-9._:-]+$") && experiment.Config.Models.All(m => m.Name != v)
                ? ValidationResult.Success()
                : ValidationResult.Error("Chỉ chữ không dấu, số, . _ : -; không trùng model đã có"))).Trim();
        var prefix = AnsiConsole.Prompt(new TextPrompt<string>("Model id provider trả về phải bắt đầu bằng (kiểm tra gọi đúng model):").DefaultValue(name)).Trim();

        var template = experiment.Config.Models.FirstOrDefault()?.Params ?? new Dictionary<string, object> { ["temperature"] = 0, ["max_tokens"] = 8192, ["seed"] = 42 };
        var parameters = template.ToDictionary(p => p.Key, p => p.Value);
        AnsiConsole.MarkupLine($"[grey]Tham số chép từ model đầu tiên: {Markup.Escape(string.Join(", ", parameters.Select(p => $"{p.Key}={p.Value}")))}[/]");
        if (parameters.ContainsKey("reasoning_effort")
            && AnsiConsole.Confirm("Bỏ reasoning_effort (model không có tham số này, ví dụ Qwen3-Coder qua Ollama)?", name.Contains("qwen", StringComparison.OrdinalIgnoreCase)))
        {
            parameters.Remove("reasoning_effort");
        }

        PriceConfig? price = null;
        if (AnsiConsole.Confirm("Đặt giá ngay?"))
        {
            price = new PriceConfig { Input = AskPrice("Giá token vào (input), USD / 1M:", null), Output = AskPrice("Giá token ra (output), USD / 1M:", null) };
        }

        var model = new ModelConfig { Name = name, ExpectedModelIdPrefix = prefix, Params = parameters, PriceUsdPerMillion = price };
        if (EditExperiment(experiment, json => ExperimentModelEditor.AddModel(json, model), $"thêm model {name}"))
        {
            AnsiConsole.MarkupLine("Giờ cài provider và khóa cho model này trên máy này:");
            ConfigureRoute(name);
        }
    }

    /// <summary>
    /// Ghi file cấu hình thực nghiệm sau khi người dùng đồng ý; báo các hệ quả của việc đổi sha256
    /// (shard chia cho sha cũ, run cũ không chạy tiếp được) và đề xuất chia lại shard.
    /// </summary>
    private bool EditExperiment(LoadedExperiment experiment, Func<string, string> change, string what)
    {
        var updated = change(File.ReadAllText(experiment.Path));
        var oldSha = experiment.Sha256;
        var plans = FindPlans(oldSha).ToList();
        var openRuns = RunStore.ListRuns(session.RunsDir)
            .Where(r => File.Exists(r.RunConfigLockPath) && Json.Read<RunConfigLock>(r.RunConfigLockPath).ExperimentSha256 == oldSha)
            .Where(r => r.ReadStatus() is not { State: RunState.Done })
            .Select(r => r.RunId).ToList();

        var notes = new List<string>
        {
            $"Ghi {Markup.Escape(session.Paths.Relative(experiment.Path))}: {Markup.Escape(what)}.",
            "File này dùng chung cả nhóm: sau khi sửa, commit và push; các máy khác git pull.",
        };
        if (plans.Count > 0)
        {
            notes.Add($"Phân công shard {Markup.Escape(string.Join(", ", plans.Select(p => p.Plan.PlanId)))} đang gắn với sha cũ: phải chia lại (tool đề xuất ngay sau khi ghi), nếu không \"Chạy shard\" sẽ báo SHARD_EXPERIMENT_MISMATCH.");
        }

        if (openRuns.Count > 0)
        {
            notes.Add($"Run chưa xong dùng sha cũ sẽ không chạy tiếp được: {Markup.Escape(string.Join(", ", openRuns))}.");
        }

        AnsiConsole.Write(new Panel(new Markup(string.Join('\n', notes))).Header("[yellow] Sửa cấu hình thực nghiệm [/]").BorderColor(Color.Yellow).Border(BoxBorder.Rounded).Expand());
        if (!AnsiConsole.Confirm("Ghi?", false))
        {
            AnsiConsole.MarkupLine("[grey]Không ghi.[/]");
            return false;
        }

        AtomicFile.WriteAllText(experiment.Path, updated);
        var reloaded = ExperimentLoader.Load(experiment.Path);
        ToolLog.Info($"models: {what} trong {experiment.Path}; sha {oldSha[..12]} → {reloaded.Sha256[..12]}");
        AnsiConsole.MarkupLine($"{Theme.Ok} Đã ghi. sha256 mới [bold]{reloaded.Sha256[..12]}[/] (cũ {oldSha[..12]}).");

        foreach (var (dir, plan) in plans)
        {
            if (AnsiConsole.Confirm($"Chia lại phân công [bold]{Markup.Escape(plan.PlanId)}[/] theo cấu hình mới (cùng người, cùng cách chia)?"))
            {
                var owners = string.Join(',', plan.Shards.Select(s => $"{s.Owner}={s.OwnerDisplay}"));
                new ShardPlanScreen(session).Run(experiment.Path, owners, plan.PlanId, dir, plan.Coordinator, assumeYes: true);
            }
        }

        return true;
    }

    private IEnumerable<(string Dir, ShardPlanFile Plan)> FindPlans(string experimentSha)
    {
        if (!Directory.Exists(session.Paths.ShardsDir))
        {
            yield break;
        }

        foreach (var planPath in Directory.EnumerateFiles(session.Paths.ShardsDir, "plan.json", SearchOption.AllDirectories).OrderBy(p => p))
        {
            ShardPlanFile? plan = null;
            try
            {
                plan = Json.Read<ShardPlanFile>(planPath);
            }
            catch (Exception ex) when (ex is IOException or System.Text.Json.JsonException)
            {
            }

            if (plan is not null && string.Equals(plan.ExperimentSha256, experimentSha, StringComparison.OrdinalIgnoreCase))
            {
                yield return (Path.GetDirectoryName(planPath)!, plan);
            }
        }
    }

    // ── Gọi thử ─────────────────────────────────────────────────────────
    private async Task TestCallAsync(LoadedExperiment experiment, ModelConfig model, CancellationToken cancellationToken)
    {
        var machine = session.Machine ?? throw new ArrowException("MACHINE_MISSING", "Chưa cài đặt máy: chạy arrow setup");
        var llm = LoadLlm();
        if (llm.Route(model.Name) is not { IsComplete: true })
        {
            AnsiConsole.MarkupLine($"{Theme.Fail} {Markup.Escape(model.Name)} chưa có provider hoặc khóa trên máy này.");
            return;
        }

        if (await LiteLlmProxy.IsAliveAsync(machine.Llm.ProxyUrl, cancellationToken))
        {
            AnsiConsole.MarkupLine($"[yellow]Đang có proxy chạy sẵn ở {Markup.Escape(machine.Llm.ProxyUrl)} (có thể của run khác, dùng cấu hình lúc nó bật). Kết quả gọi thử theo proxy đó.[/]");
        }

        var request = new LlmRequest
        {
            Model = model.Name,
            Messages = [new ChatMessage("user", "Reply with the single word OK.")],
            Params = model.Params.Where(p => p.Key != "max_tokens").ToDictionary(p => p.Key, p => (object?)p.Value).Append(new("max_tokens", 16)).ToDictionary(),
        };
        LlmResponse? response = null;
        Exception? error = null;
        await AnsiConsole.Status().StartAsync($"Gọi thử {model.Name} qua LiteLLM…", async _ =>
        {
            try
            {
                await using var proxy = await LiteLlmProxy.EnsureRunningAsync(session.Paths.ArrowRoot, session.Paths.PilotToolDir, machine, cancellationToken);
                var client = new LiteLlmClient(new SocketsHttpHandler(), machine.Llm.ProxyUrl, llm.MasterKey) { RetryDelays = [] };
                response = await client.CompleteAsync(request, cancellationToken);
            }
            catch (Exception ex) when (ex is LlmException or ArrowException or HttpRequestException)
            {
                error = ex;
            }
        });

        if (response is null)
        {
            var code = error is LlmException le ? le.Reason : error is ArrowException ae ? ae.Code : "LLM_ERROR";
            ToolLog.Warn($"models: gọi thử {model.Name} lỗi {code}: {ToolLog.Redact(error?.Message ?? "")}");
            AnsiConsole.MarkupLine($"{Theme.Fail} [red]{Markup.Escape(code)}[/]: {Markup.Escape(ToolLog.Redact(error?.Message ?? ""))}");
            AnsiConsole.MarkupLine("[grey]401/403: khóa sai hoặc hết tiền. 404: tên model ở provider sai. Ollama: kiểm tra ollama serve và ollama list. Log proxy: pilot_tool/logs/litellm-*.log[/]");
            return;
        }

        var usage = response.Usage;
        var prefixOk = usage.ModelId.StartsWith(model.ExpectedModelIdPrefix, StringComparison.Ordinal) || usage.ModelId.Contains("/" + model.ExpectedModelIdPrefix, StringComparison.Ordinal);
        var cost = LlmCost.Usd(usage, model.PriceUsdPerMillion?.Input, model.PriceUsdPerMillion?.Output);
        ToolLog.Info($"models: gọi thử {model.Name} OK, model id {usage.ModelId}, {usage.PromptTokens}/{usage.CompletionTokens} token, {usage.LatencySeconds:0.0}s");
        var grid = new Grid().AddColumn().AddColumn();
        grid.AddRow("Trả lời", Markup.Escape(response.Text.Trim().Length > 60 ? response.Text.Trim()[..60] + "…" : response.Text.Trim()));
        grid.AddRow("Model id", (prefixOk ? Theme.Ok : Theme.Fail) + " " + Markup.Escape(usage.ModelId) + (prefixOk ? "" : $" [red](cần bắt đầu bằng {Markup.Escape(model.ExpectedModelIdPrefix)}; run sẽ tạm dừng với LLM_WRONG_MODEL)[/]"));
        grid.AddRow("Token vào / ra", $"{usage.PromptTokens} / {usage.CompletionTokens}");
        grid.AddRow("Thời gian", $"{usage.LatencySeconds:0.0} s");
        grid.AddRow("Chi phí theo giá đã đặt", model.PriceUsdPerMillion is null ? "[yellow]chưa có giá[/]" : $"{cost:0.000000} USD");
        AnsiConsole.Write(new Panel(grid).Header($" {Theme.Ok} Gọi được {Markup.Escape(model.Name)} ").Border(BoxBorder.Rounded).Expand());
    }

    private static void WaitKey()
    {
        if (Console.IsInputRedirected)
        {
            return;
        }

        AnsiConsole.MarkupLine("\n[grey]Nhấn phím bất kỳ để tiếp tục…[/]");
        Console.ReadKey(intercept: true);
    }
}
