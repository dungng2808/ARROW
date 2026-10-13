using Arrow.DataAccess.Common;
using Arrow.DataAccess.Files;
using Arrow.Models.Common;
using Arrow.Models.Config;

namespace Arrow.DataAccess.Tests;

public class ConfigRepositoryTests
{
    private static string Experiment(string manifestSha) => $$"""
        {
          "experiment_id": "pilot-test",
          "kind": "pilot",
          "manifests": [ { "dataset": "classes2test", "path": "m.jsonl", "sha256": "{{manifestSha}}", "repair": true } ],
          "models": [
            {
              "name": "gpt-6-luna",
              "expected_model_id_prefix": "gpt-6-luna",
              "params": { "reasoning_effort": "none", "temperature": 0, "max_tokens": 8192, "seed": 42 },
              "price_usd_per_million": null
            }
          ],
          "strategies": ["zero_shot", "zero_shot_project_aware", "few_shot"],
          "repair": { "mechanisms": ["fixed", "adaptive"], "n": 10 },
          "verification": { "baseline_runs": 2, "step_timeout_seconds": 900, "infra_attempts": 3 }
        }
        """;

    [Fact]
    public void LoadsExperimentAndChecksManifestSha()
    {
        using var temp = new TempFramework();
        var manifest = temp.Write("experiments/m.jsonl", "{\"task_id\":\"a\"}\n");
        temp.Write("experiments/pilot.json", Experiment(Hashing.Sha256TextFile(manifest)));
        var repo = new FileConfigRepository(temp.Paths);

        var loaded = repo.LoadExperiment("experiments/pilot.json");
        Assert.Equal("pilot-test", loaded.Config.ExperimentId);
        Assert.Equal(8192, ((System.Text.Json.JsonElement)loaded.Config.Models[0].Params["max_tokens"]!).GetInt32());
        Assert.Equal(10, loaded.Config.Repair.N);
        Assert.Equal(3, loaded.Config.Verification.InfraAttempts);
        Assert.Equal(64, loaded.Sha256.Length);
        Assert.Single(repo.ListExperiments(), p => p.EndsWith("pilot.json", StringComparison.Ordinal));

        // Manifest đổi line ending (Windows checkout) vẫn khớp.
        File.WriteAllText(manifest, "{\"task_id\":\"a\"}\r\n");
        repo.LoadExperiment("experiments/pilot.json");

        // Manifest đổi nội dung → lệch sha.
        File.WriteAllText(manifest, "{\"task_id\":\"b\"}\n");
        var ex = Assert.Throws<ArrowException>(() => repo.LoadExperiment("experiments/pilot.json"));
        Assert.Equal(ErrorCodes.ManifestMismatch, ex.Code);
    }

    [Fact]
    public void MissingOrInvalidExperimentGivesCodedError()
    {
        using var temp = new TempFramework();
        var repo = new FileConfigRepository(temp.Paths);
        Assert.Equal(ErrorCodes.ConfigMissing, Assert.Throws<ArrowException>(() => repo.LoadExperiment("experiments/none.json")).Code);
        temp.Write("experiments/bad.json", "{ not json");
        Assert.Equal(ErrorCodes.ConfigInvalid, Assert.Throws<ArrowException>(() => repo.LoadExperiment("experiments/bad.json")).Code);
    }

    [Fact]
    public void ModelEditsKeepOtherFieldsAndKeyOrder()
    {
        using var temp = new TempFramework();
        var manifest = temp.Write("experiments/m.jsonl", "{}\n");
        var path = temp.Write("experiments/pilot.json", Experiment(Hashing.Sha256TextFile(manifest)));
        var repo = new FileConfigRepository(temp.Paths);

        repo.SetModelPrice(path, "gpt-6-luna", new PriceConfig { Input = 0.05, Output = 0.25 });
        repo.AddModel(path, new ModelConfig { Name = "qwen3-coder-30b", ExpectedModelIdPrefix = "qwen3-coder", Params = new() { ["temperature"] = 0 } });
        var text = File.ReadAllText(path);
        Assert.True(text.IndexOf("\"experiment_id\"", StringComparison.Ordinal) < text.IndexOf("\"models\"", StringComparison.Ordinal));
        Assert.True(text.IndexOf("\"models\"", StringComparison.Ordinal) < text.IndexOf("\"strategies\"", StringComparison.Ordinal));

        var loaded = repo.LoadExperiment(path).Config;
        Assert.Equal(0.25, loaded.Models[0].PriceUsdPerMillion!.Output);
        Assert.Equal(["gpt-6-luna", "qwen3-coder-30b"], loaded.Models.Select(m => m.Name));
        Assert.Equal(ErrorCodes.ModelExists, Assert.Throws<ArrowException>(() => repo.AddModel(path, new ModelConfig { Name = "gpt-6-luna" })).Code);

        repo.RemoveModel(path, "qwen3-coder-30b");
        Assert.Equal(ErrorCodes.ModelRequired, Assert.Throws<ArrowException>(() => repo.RemoveModel(path, "gpt-6-luna")).Code);
        Assert.Equal(ErrorCodes.ModelMissing, Assert.Throws<ArrowException>(() => repo.SetModelPrice(path, "nope", null)).Code);
    }

    [Fact]
    public void MachineConfigRoundTrip()
    {
        using var temp = new TempFramework();
        var repo = new FileConfigRepository(temp.Paths);
        Assert.Null(repo.LoadMachine());
        repo.SaveMachine(new MachineConfig { MachineName = "Dung-mac", JdkHomes = new() { ["8"] = "/jdk8" }, Workers = new() { Build = 4, Llm = 2 } });
        var machine = repo.LoadMachine()!;
        Assert.Equal("Dung-mac", machine.MachineName);
        Assert.Equal("/jdk8", machine.JdkHomes["8"]);
        Assert.Contains("\"machine_name\"", File.ReadAllText(temp.Paths.MachineConfigPath));
    }

    [Fact]
    public void LiteLlmRoundTripReadsOldSingleModelFileAndRegistersSecrets()
    {
        using var temp = new TempFramework();
        File.WriteAllText(temp.Paths.LiteLlmConfigPath, """
            model_list:
              - model_name: gpt-6-luna
                litellm_params:
                  model: openai/gpt-6-luna
                  api_base: https://api.example-router.test/v1/proxy-path
                  api_key: sk-test-abcdefghijklmnop1234
                  allowed_openai_params: ["reasoning_effort"]
            general_settings:
              master_key: sk-local-0123456789abcdef
            """);
        var repo = new FileConfigRepository(temp.Paths);
        var config = repo.LoadLiteLlm()!;
        var route = Assert.Single(config.Routes);
        Assert.Equal("api.example-router.test", route.EndpointHost);
        Assert.True(route.IsComplete);

        var withQwen = config with
        {
            Routes = [route, new LiteLlmRoute { ModelName = "qwen3-coder-30b", ProviderModel = "ollama_chat/qwen3-coder:30b", ApiBase = "http://localhost:11434" }],
        };
        repo.SaveLiteLlm(withQwen);
        var back = repo.LoadLiteLlm()!;
        Assert.Equal(["gpt-6-luna", "qwen3-coder-30b"], back.Routes.Select(r => r.ModelName));
        Assert.True(back.Route("qwen3-coder-30b")!.IsComplete);
        Assert.False(back.Route("qwen3-coder-30b")!.NeedsApiKey);
        Assert.Equal("sk-local-0123456789abcdef", back.MasterKey);
        Assert.DoesNotContain("api_key: \n", File.ReadAllText(temp.Paths.LiteLlmConfigPath));

        // Khóa, master_key và api_base đầy đủ đã được đăng ký để che.
        var redacted = Redactor.Redact("key sk-test-abcdefghijklmnop1234 master sk-local-0123456789abcdef base https://api.example-router.test/v1/proxy-path");
        Assert.DoesNotContain("abcdefghijklmnop", redacted);
        Assert.DoesNotContain("0123456789abcdef", redacted);
        Assert.DoesNotContain("/v1/proxy-path", redacted);
        Assert.Contains("api.example-router.test", redacted);
    }
}
