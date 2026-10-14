using System.Globalization;
using Arrow.Models.Config;
using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>
/// S3 (spec 06 Mục 5): xem model của cấu hình thực nghiệm; khai báo provider, endpoint, khóa API (máy này); sửa giá, thêm/bỏ model
/// (dùng chung, đổi sha256 nên hỏi lại); gọi thử một lượt.
/// </summary>
public sealed class ModelScreen(IModelConfigService models, ConsoleInput input, ConsoleOutput output)
{
    public async Task<int> RunAsync(string? experimentPath)
    {
        var path = experimentPath ?? ChooseExperiment();
        if (path is null)
        {
            return 1;
        }

        while (true)
        {
            var view = models.Get(path);
            output.Blank();
            output.Title($"Model · {Path.GetFileName(view.ExperimentPath)} · sha {view.ExperimentSha256[..8]}");
            output.Line("Model chạy, tham số, giá: cấu hình thực nghiệm (dùng chung, commit).");
            output.Line("Provider, endpoint, khóa API: litellm.config.local.yaml của máy này.");
            output.Blank();
            output.Table(["Model", "Tham số", "Giá USD/1M", "Provider (máy này)", "Khóa"], view.Models.Select(m => (IReadOnlyList<string>)
            [
                m.Model.Name,
                string.Join(", ", m.Model.Params.Select(p => $"{p.Key}={p.Value}")),
                m.Model.PriceUsdPerMillion is { } price ? $"vào {price.Input.ToString(CultureInfo.InvariantCulture)}, ra {price.Output.ToString(CultureInfo.InvariantCulture)}" : "-",
                m.Route is null ? "(chưa khai báo)" : $"{m.Route.ProviderModel} · {m.Route.EndpointHost}",
                (m.Route is { IsComplete: true } ? "[OK] " : "[X] ") + m.KeyHint,
            ]).ToList());
            output.Blank();

            var actions = new List<(string Label, Func<Task> Run)>();
            foreach (var m in view.Models)
            {
                actions.Add(($"Provider và khóa API của {m.Model.Name}", () => { EditRoute(m); return Task.CompletedTask; }));
                actions.Add(($"Giá của {m.Model.Name}", () => { EditPrice(path, m.Model); return Task.CompletedTask; }));
                actions.Add(($"Gọi thử {m.Model.Name} (1 lượt)", () => TestAsync(path, m.Model.Name)));
            }

            actions.Add(("Thêm model vào cấu hình thực nghiệm", () => { AddModel(path); return Task.CompletedTask; }));
            if (view.Models.Count > 1)
            {
                actions.Add(("Bỏ một model khỏi cấu hình thực nghiệm", () => { RemoveModel(path, view); return Task.CompletedTask; }));
            }

            var choice = input.Choose("", actions.Select(a => a.Label).ToList(), "Xong");
            if (choice < 0)
            {
                return 0;
            }

            await actions[choice].Run();
        }
    }

    private string? ChooseExperiment()
    {
        var experiments = models.Experiments();
        if (experiments.Count == 0)
        {
            output.Status(Mark.Fail, "Không có cấu hình thực nghiệm trong experiments/");
            return null;
        }

        return experiments.Count == 1 ? experiments[0] : experiments[input.Choose("Cấu hình thực nghiệm:", experiments.Select(Path.GetFileName).ToList()!)];
    }

    private void EditRoute(ModelView model)
    {
        var kind = input.Choose("Provider:", ["Endpoint tương thích OpenAI (commercial API)", "Ollama tự host (open-weight model)", "Khác (chuỗi model LiteLLM)"], defaultIndex: 0);
        var current = model.Route;
        var (providerModel, apiBase) = kind switch
        {
            0 => ("openai/" + input.Ask("Tên model ở provider", current?.ProviderModel.Split('/').Last() ?? model.Model.Name),
                input.Ask("Endpoint (api_base)", current?.ApiBase is { Length: > 0 } b ? b : "https://api.openai.com/v1")),
            1 => ("ollama_chat/" + input.Ask("Tên model trong Ollama", current?.ProviderModel.Split('/').Last() ?? "qwen3-coder:30b"),
                input.Ask("Endpoint Ollama", current?.ApiBase is { Length: > 0 } o ? o : "http://localhost:11434")),
            _ => (input.Ask("Chuỗi model LiteLLM (provider/model)", current?.ProviderModel), input.Ask("Endpoint (api_base)", current?.ApiBase)),
        };
        var key = kind == 1 ? "" : input.AskSecret($"Khóa API{(current?.HasApiKey == true ? $" (Enter để giữ {model.KeyHint})" : "")}");
        var route = models.SaveRoute(model.Model.Name, providerModel, apiBase, key);
        output.Status(Mark.Ok, "Đã lưu litellm.config.local.yaml", $"{model.Model.Name} · {route.EndpointHost} · khóa {(route.HasApiKey ? "..." + route.ApiKey[^Math.Min(4, route.ApiKey.Length)..] : "(không)")}");
    }

    private void EditPrice(string path, ModelConfig model)
    {
        var current = model.PriceUsdPerMillion;
        var price = new PriceConfig
        {
            Input = double.Parse(input.Ask("Giá token vào (USD / 1 triệu)", current?.Input.ToString(CultureInfo.InvariantCulture) ?? "0", Number), CultureInfo.InvariantCulture),
            Output = double.Parse(input.Ask("Giá token ra (USD / 1 triệu)", current?.Output.ToString(CultureInfo.InvariantCulture) ?? "0", Number), CultureInfo.InvariantCulture),
        };
        output.Status(Mark.Warn, "Sửa giá làm đổi sha256 của cấu hình thực nghiệm", "run đã tạo với cấu hình cũ sẽ không chạy tiếp được");
        if (input.Confirm("Ghi?", false))
        {
            models.SetPrice(path, model.Name, price);
            output.Status(Mark.Ok, "Đã ghi giá");
        }
    }

    private void AddModel(string path)
    {
        var name = input.Ask("Tên model (dùng trong cấu hình và LiteLLM)");
        var prefix = input.Ask("Tiền tố model id trả về", name);
        output.Status(Mark.Warn, "Thêm model làm đổi sha256 của cấu hình thực nghiệm");
        if (input.Confirm("Ghi?", false))
        {
            models.AddModel(path, new ModelConfig
            {
                Name = name, ExpectedModelIdPrefix = prefix,
                Params = new() { ["temperature"] = 0, ["max_tokens"] = 8192, ["seed"] = 42 },
            });
            output.Status(Mark.Ok, $"Đã thêm {name}", "khai báo provider và khóa ở mục tiếp theo");
        }
    }

    private void RemoveModel(string path, ModelsView view)
    {
        var choice = input.Choose("Bỏ model nào?", view.Models.Select(m => m.Model.Name).ToList(), "Thôi");
        if (choice >= 0 && input.Confirm($"Bỏ {view.Models[choice].Model.Name}? (đổi sha256)", false))
        {
            models.RemoveModel(path, view.Models[choice].Model.Name);
            output.Status(Mark.Ok, "Đã bỏ");
        }
    }

    private async Task TestAsync(string path, string model)
    {
        output.Line($"Gọi thử {model}…");
        var result = await models.TestAsync(path, model, CancellationToken.None);
        if (!result.Ok)
        {
            output.Status(Mark.Fail, $"Gọi thử {model}", result.Error ?? "");
            return;
        }

        output.Status(Mark.Ok, "Câu trả lời", result.Text ?? "");
        output.Status(result.ModelMatches ? Mark.Ok : Mark.Fail, "Model id", result.ModelId ?? "");
        output.Line($"     token {result.TokensIn} vào / {result.TokensOut} ra · {result.Seconds} s · {result.CostUsd.ToString("0.######", CultureInfo.InvariantCulture)} USD");
    }

    private static string? Number(string text) =>
        double.TryParse(text, NumberStyles.Float, CultureInfo.InvariantCulture, out var value) && value >= 0 ? null : "nhập số không âm, dấu chấm thập phân";
}
