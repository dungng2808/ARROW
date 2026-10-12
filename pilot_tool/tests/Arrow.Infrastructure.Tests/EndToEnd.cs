using System.Text.Json;
using System.Text.Json.Nodes;
using Arrow.Core.Manifest;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Infrastructure.Llm;
using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Tests;

/// <summary>
/// Chạy lệnh <c>arrow run</c> thật (tiến trình riêng) trên các fixture với model giả: cấu hình thực nghiệm tạm gồm
/// Classes2Test (fixture Maven JUnit 4, Gradle JUnit 5; có sửa) và GitBug-Java (fixture Maven nhiều module; không sửa).
/// </summary>
public static class EndToEnd
{
    public sealed record Setup(string ExperimentPath, string RulesPath, string RunId)
    {
        public RunStore Store => new(Path.Combine(Env.Paths.PilotToolDir, Env.Machine.RunsDir), RunId);
    }

    public static Setup Create(string name, IEnumerable<FakeLlmClient.Rule> rules, int nMax = 3, Action<JsonObject>? edit = null)
    {
        var dir = Path.Combine(Env.Root, "e2e", name);
        Directory.CreateDirectory(dir);
        string WriteManifest(string file, params ManifestRecord[] records)
        {
            var path = Path.Combine(dir, file);
            File.WriteAllText(path, string.Concat(records.Select(r => Json.Serialize(r with { DurationSeconds = 10 }, indented: false) + "\n")));
            return path;
        }

        var c2t = WriteManifest("classes2test.jsonl", Samples.MavenJunit4, Samples.GradleJunit5);
        var gitbug = WriteManifest("gitbug.jsonl", Samples.MavenMultiJunit5);
        var experiment = JsonNode.Parse(File.ReadAllText(Path.Combine(Env.Paths.ExperimentsDir, "pilot-v1.json")))!.AsObject();
        experiment["experiment_id"] = name;
        experiment["kind"] = "e2e";
        experiment["manifests"] = new JsonArray(
            new JsonObject { ["dataset"] = "classes2test", ["path"] = c2t, ["sha256"] = Hashing.Sha256File(c2t), ["repair"] = true },
            new JsonObject { ["dataset"] = "gitbug-java", ["path"] = gitbug, ["sha256"] = Hashing.Sha256File(gitbug), ["repair"] = false });
        experiment["repair"]!["n_max"] = nMax;
        edit?.Invoke(experiment);
        var experimentPath = Path.Combine(dir, "experiment.json");
        File.WriteAllText(experimentPath, experiment.ToJsonString(new JsonSerializerOptions { WriteIndented = true }));
        var rulesPath = Path.Combine(dir, "fake-llm.json");
        File.WriteAllText(rulesPath, Json.Serialize(rules.ToList()));
        return new Setup(experimentPath, rulesPath, $"{name}-{Guid.NewGuid():N}"[..Math.Min(40, name.Length + 13)]);
    }

    /// <summary>Chạy <c>arrow run</c> với tham số thêm; trả mã thoát và output.</summary>
    public static async Task<(int ExitCode, string Output)> RunAsync(Setup setup, params string[] extra)
    {
        var cli = Path.Combine(AppContext.BaseDirectory, "Arrow.Cli.dll");
        var args = new List<string> { cli, "run", "--root", Env.Paths.PilotToolDir, "--experiment", setup.ExperimentPath, "--fake-llm", setup.RulesPath, "--run-id", setup.RunId, "--plain" };
        args.AddRange(extra);
        var result = await ProcessRunner.RunAsync("dotnet", args, Env.Paths.PilotToolDir, new Dictionary<string, string?> { ["ARROW_PILOT_ROOT"] = Env.Paths.PilotToolDir },
            TimeSpan.FromMinutes(40), CancellationToken.None);
        return (result.ExitCode ?? -1, result.Output);
    }

    public static Dictionary<string, LedgerEntry> Latest(Setup setup) =>
        setup.Store.LatestByJob().Values.Where(e => e.Job is not null).ToDictionary(e => e.Job!.Canonical);

    public static string Outcome(Setup setup, string stage, string taskId, string? strategy = null, string? mechanism = null, int round = 0) =>
        Latest(setup).Values.Single(e => e.Job!.Stage == stage && e.Job.TaskId == taskId && e.Job.Strategy == strategy && e.Job.Mechanism == mechanism && e.Job.Round == round).Outcome ?? "";

    // ── Response mẫu ────────────────────────────────────────────────────

    public const string CalcValid = """
        Here you go:
        ```java
        package com.example.calc;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class CalculatorTest {
            @Test
            public void adds() {
                assertEquals(5, new Calculator("c").add(2, 3));
            }
        }
        ```
        """;

    public const string CalcAssertion = """
        ```java
        package com.example.calc;

        import org.junit.jupiter.api.Test;

        import static org.junit.jupiter.api.Assertions.assertEquals;

        public class Calculator_ArrowTest {
            @Test
            public void adds() {
                assertEquals(6, new Calculator("c").add(2, 3));
            }

            @Test
            public void divides() {
                assertEquals(2, new Calculator("c").divide(4, 2));
            }
        }
        ```
        """;

    public const string CalcRuntime = """
        ```java
        package com.example.calc;

        import static org.junit.Assert.assertEquals;

        import org.junit.Test;

        public class Calculator_ArrowTest {
            @Test
            public void measures() {
                assertEquals(0, new Calculator("c").labelLength(null));
            }
        }
        ```
        """;

    public const string Invalid = "I cannot write this test without more context.";

    public const string GreeterCompile = """
        ```java
        package com.example.app;

        import org.junit.jupiter.api.Test;

        class Greeter_ArrowTest {
            @Test
            void greets() {
                new Greeter("Hi").hello("x");
            }
        }
        ```
        """;

    public const string CartValid = """
        ```java
        package com.example.shop;

        import static org.junit.jupiter.api.Assertions.assertEquals;

        import org.junit.jupiter.api.Test;

        class Cart_ArrowTest {
            @Test
            void sums() {
                Cart cart = new Cart();
                cart.add(4);
                cart.add(5);
                assertEquals(9, cart.total());
            }
        }
        ```
        """;
}
