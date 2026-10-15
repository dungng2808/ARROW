using System.Text.RegularExpressions;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.Block2;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

/// <summary>
/// Kiểm tra môi trường (S4, spec 06 Mục 6, spec 00 Mục 5.1 <c>doctor</c>): cấu hình thực nghiệm và sha256 manifest, bộ prompt, JDK,
/// Maven/Gradle/Git, thư mục repository, kết nối GitHub, LiteLLM, từng model (Ollama: <c>num_ctx</c> ≥ 32.768), java-helper, công cụ khối 4.
/// Báo từng dòng ngay khi kiểm xong; lưu dấu kết quả vào <c>machine.local.json</c>.
/// </summary>
public sealed partial class DoctorService(FrameworkPaths paths, IConfigRepository config, IPromptRepository prompts, IProcessRunner runner, ILogRepository log) : IDoctorService
{
    public const int MinOllamaContext = 32_768;

    [GeneratedRegex(@"num_ctx\s+(?<n>\d+)")]
    private static partial Regex NumCtx();

    public async Task<DoctorReport> RunAsync(string? experimentPath, Action<DoctorLine> onLine, CancellationToken cancellationToken)
    {
        var lines = new List<DoctorLine>();
        void Add(DoctorLine line)
        {
            lines.Add(line);
            onLine(line);
        }

        var machine = config.LoadMachine();

        // Cấu hình thực nghiệm và bộ prompt.
        LoadedExperiment? experiment = null;
        try
        {
            var path = experimentPath ?? config.ListExperiments() switch
            {
                [var only] => only,
                [] => throw new ArrowException(ErrorCodes.ConfigMissing, $"không có file trong {paths.ExperimentsDir}"),
                var many => many[0],
            };
            experiment = config.LoadExperiment(path);
            Add(new DoctorLine("Cấu hình thực nghiệm", DoctorMark.Ok, $"{experiment.Config.ExperimentId} · {experiment.Config.Manifests.Count} manifest khớp sha256"));
        }
        catch (ArrowException ex)
        {
            Add(new DoctorLine("Cấu hình thực nghiệm", DoctorMark.Fail, $"{ex.Code}: {ex.Message}", "sửa experiments/*.json"));
        }

        var problems = PromptConsistencyChecker.Check(prompts.LoadAll().ToDictionary(t => t.RelativePath));
        Add(problems.Count == 0
            ? new DoctorLine("Bộ prompt", DoctorMark.Ok, "nhất quán (RQ1 công bằng, prompt sửa dùng nguyên văn phần chung)")
            : new DoctorLine("Bộ prompt", DoctorMark.Fail, string.Join("; ", problems), "arrow prompts check"));

        // JDK và công cụ build.
        var jdks = new List<ToolInfo>();
        foreach (var major in new[] { 8, 11, 17, 21 })
        {
            jdks.Add(await ToolProbe.JdkAsync(runner, major, ToolLocator.FindJdkHome(major, machine, paths.ArrowRoot), cancellationToken).ConfigureAwait(false));
        }

        var missingJdk = jdks.Where(j => !j.Ok).ToList();
        Add(new DoctorLine("JDK 8 / 11 / 17 / 21",
            jdks.Single(j => j.Name == "JDK 17").Ok ? (missingJdk.Count == 0 ? DoctorMark.Ok : DoctorMark.Warn) : DoctorMark.Fail,
            string.Join(" / ", jdks.Select(j => j.Ok ? j.Version : "thiếu")),
            missingJdk.Count == 0 ? null : $"thiếu {string.Join(", ", missingJdk.Select(j => j.Name))}: task cần JDK đó sẽ bị loại; xem Java-version/README.md hoặc arrow setup"));

        var maven = await ToolProbe.ProgramAsync(runner, "Maven", ToolLocator.FindMaven(machine), ["-v"], cancellationToken).ConfigureAwait(false);
        var gradle = await ToolProbe.ProgramAsync(runner, "Gradle", ToolLocator.FindGradle(machine), ["-v"], cancellationToken).ConfigureAwait(false);
        var git = await ToolProbe.ProgramAsync(runner, "Git", ToolLocator.FindGit(machine), ["--version"], cancellationToken).ConfigureAwait(false);
        Add(new DoctorLine("Maven, Gradle, Git", maven.Ok && git.Ok ? (gradle.Ok ? DoctorMark.Ok : DoctorMark.Warn) : DoctorMark.Fail,
            $"{maven.Version ?? "thiếu"} · {gradle.Version ?? "thiếu"} · {git.Version ?? "thiếu"}",
            maven.Ok && git.Ok ? (gradle.Ok ? null : "Gradle chỉ cần khi repository không có gradlew") : "cài Maven/Git hoặc đặt maven_path/git_path bằng arrow setup"));

        // Thư mục repository.
        var workDir = paths.Resolve(machine?.WorkDir ?? "../repo");
        var setup = new SetupService(paths, config, runner);
        var workError = setup.ValidateWorkDir(workDir);
        Add(workError is null
            ? new DoctorLine("Thư mục repository", DoctorMark.Ok, $"{workDir} · {setup.FreeSpaceGb(workDir):0} GB trống")
            : new DoctorLine("Thư mục repository", DoctorMark.Fail, $"{workDir}: {workError}", "đổi work_dir bằng arrow setup"));

        // Kết nối GitHub.
        if (git.Ok)
        {
            var remote = await Run(git.Path!, ["ls-remote", "https://github.com/git/git", "HEAD"], TimeSpan.FromSeconds(30), cancellationToken).ConfigureAwait(false);
            Add(remote is { Succeeded: true }
                ? new DoctorLine("Kết nối GitHub", DoctorMark.Ok, "git ls-remote")
                : new DoctorLine("Kết nối GitHub", DoctorMark.Fail, remote?.Tail(1) ?? "git không chạy được", "kiểm tra mạng/proxy"));
        }

        // LiteLLM và từng model.
        var liteLlm = config.LoadLiteLlm();
        var proxyMode = machine?.Llm.ProxyMode ?? "auto_start";
        var litellmExe = LiteLlmProxy.FindExecutable(paths.ArrowRoot) ?? "";
        Add(proxyMode == "external" || File.Exists(litellmExe)
            ? new DoctorLine("LiteLLM", DoctorMark.Ok, proxyMode == "external" ? $"proxy ngoài ở {machine?.Llm.ProxyUrl}" : litellmExe)
            : new DoctorLine("LiteLLM", DoctorMark.Warn, "không tìm thấy lệnh litellm", "cài LiteLLM (pip install 'litellm[proxy]') hoặc đặt proxy_mode = external; chỉ cần khi gọi model thật"));
        foreach (var model in experiment?.Config.Models ?? [])
        {
            var route = liteLlm?.Route(model.Name);
            if (route is not { IsComplete: true })
            {
                Add(new DoctorLine($"Model {model.Name}", DoctorMark.Fail, "chưa khai báo provider/endpoint/khóa", "arrow models"));
                continue;
            }

            if (route.ProviderModel.StartsWith("ollama", StringComparison.Ordinal))
            {
                Add(await OllamaContextAsync(model.Name, route, cancellationToken).ConfigureAwait(false));
            }
            else
            {
                Add(new DoctorLine($"Model {model.Name}", DoctorMark.Ok, $"{route.ProviderModel} · {route.EndpointHost} · khóa {ModelConfigService.KeyHint(route)}"));
            }
        }

        // java-helper.
        var jdk17 = ToolLocator.FindJdkHome(17, machine, paths.ArrowRoot);
        if (jdk17 is not null)
        {
            try
            {
                await using var helper = new JavaHelperClient(JavaHelperOptions.Default(paths, machine), runner, log);
                var version = await helper.PingAsync(cancellationToken).ConfigureAwait(false);
                Add(new DoctorLine("java-helper", DoctorMark.Ok, $"{version} · {helper.JarPath}"));
            }
            catch (ArrowException ex)
            {
                Add(new DoctorLine("java-helper", DoctorMark.Fail, $"{ex.Code}: {ex.Message}"));
            }
        }
        else
        {
            Add(new DoctorLine("java-helper", DoctorMark.Fail, "cần JDK 17"));
        }

        // Công cụ khối 4.
        foreach (var tool in Catalog(machine).Check())
        {
            Add(!tool.Present
                ? new DoctorLine($"Công cụ {tool.Id}", DoctorMark.Fail, "chưa có trong tools_dir", "tự sửa: tải về (Maven Central / GitHub Releases)", AutoFixable: true)
                : !tool.Pinned
                    ? new DoctorLine($"Công cụ {tool.Id}", DoctorMark.Warn, $"sha256 {tool.Sha256![..12]}… chưa khóa", "chép sha256 vào tools/tools.lock.json để các máy dùng cùng file")
                    : tool.Matches
                        ? new DoctorLine($"Công cụ {tool.Id}", DoctorMark.Ok, "sha256 khớp")
                        : new DoctorLine($"Công cụ {tool.Id}", DoctorMark.Fail, "sha256 không khớp tools.lock.json", "xóa file rồi tải lại"));
        }

        var report = new DoctorReport(lines);
        if (machine is not null)
        {
            config.SaveMachine(machine with { LastDoctor = new DoctorStamp { At = DateTimeOffset.Now, Status = report.Ready ? "READY" : "NOT_READY" } });
        }

        return report;
    }

    public async Task<IReadOnlyList<DoctorLine>> FixAsync(Action<DoctorLine> onLine, CancellationToken cancellationToken)
    {
        var lines = new List<DoctorLine>();
        var catalog = Catalog(config.LoadMachine());
        foreach (var tool in catalog.Check().Where(t => !t.Present))
        {
            DoctorLine line;
            try
            {
                var path = await catalog.EnsureAsync(tool.Id, cancellationToken).ConfigureAwait(false);
                line = new DoctorLine($"Công cụ {tool.Id}", DoctorMark.Ok, $"đã tải {path}");
            }
            catch (ArrowException ex)
            {
                line = new DoctorLine($"Công cụ {tool.Id}", DoctorMark.Fail, ex.Message);
            }

            lines.Add(line);
            onLine(line);
        }

        return lines;
    }

    private ToolsCatalog Catalog(MachineConfig? machine)
    {
        var jdk = ToolLocator.FindJdkHome(17, machine, paths.ArrowRoot) ?? ToolLocator.FindJdkHome(11, machine, paths.ArrowRoot) ?? "";
        return new ToolsCatalog(paths.Resolve(machine?.ToolsDir ?? "../tools"), Path.Combine(paths.FrameworkRoot, "tools", "tools.lock.json"),
            ToolLocator.FindMaven(machine), jdk, runner, log);
    }

    /// <summary>Spec 02 Mục 4.2: Ollama cắt prompt không báo lỗi khi <c>num_ctx</c> nhỏ, nên phải ≥ 32.768.</summary>
    private async Task<DoctorLine> OllamaContextAsync(string name, LiteLlmRoute route, CancellationToken ct)
    {
        var ollama = ToolLocator.FindOnPath(OperatingSystem.IsWindows() ? "ollama.exe" : "ollama");
        if (ollama is null)
        {
            return new DoctorLine($"Model {name}", DoctorMark.Warn, $"{route.ProviderModel}: không có lệnh ollama để kiểm tra num_ctx");
        }

        var tag = route.ProviderModel[(route.ProviderModel.IndexOf('/') + 1)..];
        var show = await Run(ollama, ["show", tag], TimeSpan.FromSeconds(30), ct).ConfigureAwait(false);
        var numCtx = NumCtx().Match(show?.Output ?? "") is { Success: true } m ? int.Parse(m.Groups["n"].Value, System.Globalization.CultureInfo.InvariantCulture) : (int?)null;
        return numCtx >= MinOllamaContext
            ? new DoctorLine($"Model {name}", DoctorMark.Ok, $"{route.ProviderModel} · num_ctx {numCtx}")
            : new DoctorLine($"Model {name}", DoctorMark.Fail, $"{route.ProviderModel} · num_ctx {numCtx?.ToString() ?? "mặc định"} < {MinOllamaContext}",
                $"tạo model Ollama với PARAMETER num_ctx {MinOllamaContext} (Modelfile) rồi trỏ provider tới model đó");
    }

    private async Task<ProcessResult?> Run(string program, IReadOnlyList<string> args, TimeSpan timeout, CancellationToken ct)
    {
        try
        {
            return await runner.RunAsync(new ProcessSpec(program, args, Path.GetTempPath(), Timeout: timeout), ct).ConfigureAwait(false);
        }
        catch (Exception ex) when (ex is System.ComponentModel.Win32Exception or InvalidOperationException or IOException)
        {
            return null;
        }
    }
}
