using System.Diagnostics;
using System.Text;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Files;
using Arrow.Models.Config;
using Arrow.Models.Run;
using Arrow.Service.External;

namespace Arrow.IntegrationTests;

/// <summary>
/// Bước C9 (spec 07 Mục 10): chạy <c>arrow-fw</c> thật với input/output chuyển hướng trên một thư mục framework tạm (<c>--root</c>):
/// trợ giúp, menu, kiểm tra môi trường, run với model giả, trạng thái, chi tiết task, báo cáo, cần chạy lại, điều khiển, model.
/// </summary>
public class CliIntegrationTests
{
    private static (int ExitCode, string Output) Arrow(string root, string stdin, params string[] args)
    {
        var dll = Path.Combine(AppContext.BaseDirectory, "arrow-fw.dll");
        var start = new ProcessStartInfo("dotnet")
        {
            RedirectStandardOutput = true, RedirectStandardError = true, RedirectStandardInput = true, UseShellExecute = false,
            StandardOutputEncoding = Encoding.UTF8, StandardErrorEncoding = Encoding.UTF8, StandardInputEncoding = new UTF8Encoding(false),
        };
        start.ArgumentList.Add(dll);
        foreach (var arg in args.Append("--root").Append(root))
        {
            start.ArgumentList.Add(arg);
        }

        using var process = Process.Start(start)!;
        process.StandardInput.Write(stdin);
        process.StandardInput.Close();
        var error = process.StandardError.ReadToEndAsync();
        var output = process.StandardOutput.ReadToEnd();
        Assert.True(process.WaitForExit(600_000), "arrow-fw không kết thúc");
        return (process.ExitCode, output + error.Result);
    }

    private static void CopyTree(string source, string target)
    {
        foreach (var file in Directory.GetFiles(source, "*", SearchOption.AllDirectories))
        {
            var destination = Path.Combine(target, Path.GetRelativePath(source, file));
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            File.Copy(file, destination, overwrite: true);
        }
    }

    [Fact]
    public void HelpListsEveryCommand()
    {
        var (code, output) = Arrow(FixtureRepo.Framework.FrameworkRoot, "", "--help");
        Assert.Equal(0, code);
        foreach (var command in new[] { "arrow-fw run", "arrow-fw report", "arrow-fw status", "arrow-fw show", "arrow-fw rerun-list", "arrow-fw control", "arrow-fw doctor", "arrow-fw setup", "arrow-fw models", "arrow-fw screen" })
        {
            Assert.Contains(command, output);
        }

        Assert.Equal(2, Arrow(FixtureRepo.Framework.FrameworkRoot, "", "run", "--run-id", "x", "stray").ExitCode);
    }

    [Fact]
    public void ScreensWorkWithRedirectedInput()
    {
        using var repo = new FixtureRepo("maven-verify-junit4");
        var real = FixtureRepo.Framework;
        var paths = repo.ResultPaths;
        var root = paths.FrameworkRoot;
        CopyTree(real.PromptsDir, paths.PromptsDir);
        CopyTree(Path.Combine(real.FrameworkRoot, "tools", "java-helper"), Path.Combine(root, "tools", "java-helper"));
        File.Copy(Path.Combine(real.FrameworkRoot, "tools", "tools.lock.json"), Path.Combine(root, "tools", "tools.lock.json"));
        var tools = ToolLocator.Environment(null, real.ArrowRoot);
        new FileConfigRepository(paths).SaveMachine(new MachineConfig
        {
            MachineName = "cli-it",
            JdkHomes = tools.JdkHomes.ToDictionary(j => j.Key.ToString(), j => j.Value),
            WorkDir = repo.WorkDir,
            ToolsDir = Path.Combine(real.ArrowRoot, "tools"),
            Workers = new WorkersConfig { Build = 2, Llm = 1 },
        });
        var record = repo.Task("900_cli", "src/main/java/com/acme/billing/PriceCalculator.java", "com.acme.billing.PriceCalculator", "src/test/java/com/acme/billing/PriceCalculatorTest.java",
            "openjdk version \"1.8.0_504\"", "junit4", ("test_compile", ["mvn", "-q", "-DskipTests", "test-compile"], ".", 0));
        var manifest = Path.Combine(repo.Root, "cli.jsonl");
        File.WriteAllText(manifest, JsonFile.Serialize(record with { Dataset = "", RawJson = "" }, indented: false) + "\n");
        Directory.CreateDirectory(paths.ExperimentsDir);
        File.WriteAllText(Path.Combine(paths.ExperimentsDir, "cli.json"), JsonFile.Serialize(new ExperimentConfig
        {
            ExperimentId = "cli",
            Manifests = [new ManifestRef { Dataset = "classes2test", Path = manifest, Sha256 = Hashing.Sha256TextFile(manifest), Repair = true }],
            Models = [new ModelConfig { Name = "fake-model", Params = new() { ["temperature"] = 0 } }],
            Strategies = ["zero_shot"],
            Repair = new RepairConfig { N = 2 },
        }));

        // S1: menu với input chuyển hướng, chọn 0 để thoát.
        var (menuCode, menu) = Arrow(root, "0\n");
        Assert.Equal(0, menuCode);
        Assert.Contains("Máy:          cli-it", menu);
        Assert.Contains("Thực nghiệm:  cli", menu);
        Assert.Contains("Chọn việc:", menu);

        // S5: run với model giả, output chuyển hướng nên không chờ lệnh gõ.
        var (runCode, run) = Arrow(root, "", "run", "--run-id", "cli", "--blocks", "1,2,3", "--fake-llm");
        Assert.True(runCode == 0, run);
        Assert.Contains("[K1] 900_cli OK · Maven", run);
        Assert.Contains("[K2] 900_cli fake-model/zero_shot c0 →", run);
        Assert.Contains("[K3] 900_cli fake-model/zero_shot c0 → VALID", run);
        Assert.Contains("Khối  Xong / Tổng", run);
        Assert.Contains("Run cli xong", run);

        // S6, S7, S8, S9.
        var (_, status) = Arrow(root, "", "status", "--run-id", "cli");
        Assert.Contains("Trạng thái: XONG", status);
        Assert.Contains("Còn sống: không", status);
        var (_, show) = Arrow(root, "", "show", "--run-id", "cli", "--task", "900_cli");
        Assert.Contains("Task 900_cli · com.acme.billing.PriceCalculator · classes2test", show);
        Assert.Contains("zero_shot  VALID", show);
        var (reportCode, report) = Arrow(root, "", "report", "--run-id", "cli");
        Assert.True(reportCode == 0, report);
        Assert.Contains("RQ1 · Verification (classes2test · fake-model)", report);
        Assert.Contains("Cần chạy lại riêng: 0 job", report);
        Assert.True(File.Exists(Path.Combine(root, "runs", "cli", "reports", "report.md")));
        Assert.Contains("Không có job nào cần chạy lại", Arrow(root, "", "rerun-list", "--run-id", "cli").Output);

        // Điều khiển từ terminal khác: ghi control.json cho run đang chạy đọc.
        Assert.Equal(0, Arrow(root, "", "control", "--run-id", "cli", "pause").ExitCode);
        Assert.Equal(ControlAction.Pause, new FileRunRepository(paths).ReadControl("cli")!.Action);

        // S3: bảng model, chọn 0 (xong).
        var (_, models) = Arrow(root, "0\n", "models");
        Assert.Contains("fake-model", models);
        Assert.Contains("(chưa khai báo)", models);

        // Chạy lại cùng run id: mọi job đã có ledger nên không chạy lại gì.
        var (_, again) = Arrow(root, "", "run", "--run-id", "cli", "--blocks", "1,2,3", "--fake-llm");
        Assert.DoesNotContain("[K1] 900_cli", again);
        Assert.Contains("Run cli xong", again);
    }
}
