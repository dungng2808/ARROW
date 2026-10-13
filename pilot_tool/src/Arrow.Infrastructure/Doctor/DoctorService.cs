using Arrow.Core.Config;
using Arrow.Core.Manifest;
using Arrow.Core.Shards;
using Arrow.Core.Util;
using Arrow.Infrastructure.Llm;
using Arrow.Infrastructure.Processes;
using Arrow.Infrastructure.Tooling;

namespace Arrow.Infrastructure.Doctor;

public enum CheckStatus
{
    Pass,
    Warn,
    Fail,
    Skip,
}

/// <summary>Cách sửa tự động cho một mục ✘ (DoctorScreen hỏi người dùng rồi thực hiện).</summary>
public enum DoctorFix
{
    None,
    RunSetup,
    InstallLiteLlm,
    EnterApiKey,
    InstallTools,
}

/// <param name="Steps">Các bước làm tiếp khi Hint một dòng không đủ; DoctorScreen in thành khung riêng dưới bảng.</param>
public sealed record DoctorCheck(string Name, CheckStatus Status, string Detail, string? Hint = null, DoctorFix Fix = DoctorFix.None, IReadOnlyList<string>? Steps = null);

public sealed record DoctorReport(IReadOnlyList<DoctorCheck> Checks)
{
    /// <summary>READY khi không có mục nào Fail (SETUP.md §6).</summary>
    public string Status => Checks.Any(c => c.Status == CheckStatus.Fail) ? "NOT_READY" : "READY";
}

/// <summary>Kiểm tra môi trường của máy (SETUP.md §6). Chỉ kiểm tra; việc sửa do DoctorScreen làm khi người dùng đồng ý.</summary>
public sealed class DoctorService(ToolPaths paths)
{
    public const double MinFreeGb = 20;

    public async Task<DoctorReport> RunAsync(Action<DoctorCheck>? onCheck = null, CancellationToken cancellationToken = default)
    {
        var checks = new List<DoctorCheck>();
        void Add(DoctorCheck check)
        {
            checks.Add(check);
            onCheck?.Invoke(check);
        }

        // 1. Thư mục
        Add(CheckWritable("Thư mục pilot_tool ghi được", paths.PilotToolDir));

        // 2. Cấu hình máy
        var machine = MachineConfig.TryLoad(paths.MachineConfigPath);
        if (machine is null)
        {
            Add(new DoctorCheck("Cấu hình máy machine.local.json", CheckStatus.Fail, "Chưa có", "Chạy: arrow setup", DoctorFix.RunSetup));
            return new DoctorReport(checks);
        }

        // Thư mục ARROW đã đổi tên hoặc chuyển chỗ: đường dẫn JDK, LiteLLM, lệnh arrow còn trỏ chỗ cũ.
        var configuredRoot = string.IsNullOrEmpty(machine.ArrowRoot) ? paths.ArrowRoot : Path.GetFullPath(machine.ArrowRoot);
        if (!string.Equals(configuredRoot.TrimEnd('/', '\\'), paths.ArrowRoot.TrimEnd('/', '\\'), OperatingSystem.IsWindows() ? StringComparison.OrdinalIgnoreCase : StringComparison.Ordinal))
        {
            Add(new DoctorCheck("Cấu hình máy machine.local.json", CheckStatus.Fail,
                $"Thư mục ARROW đã đổi tên hoặc chuyển chỗ: cấu hình ghi {configuredRoot}, hiện tại {paths.ArrowRoot}",
                "Xem các bước bên dưới", DoctorFix.RunSetup, MovedArrowSteps()));
        }
        else
        {
            Add(new DoctorCheck("Cấu hình máy machine.local.json", CheckStatus.Pass, $"máy {machine.MachineName}"));
        }

        // 3. Cấu hình thực nghiệm và manifest
        LoadedExperiment? experiment = null;
        try
        {
            experiment = ExperimentLoader.Load(paths.Resolve(machine.Experiment));
            Add(new DoctorCheck("Cấu hình thực nghiệm, hash manifest", CheckStatus.Pass,
                $"{experiment.Config.ExperimentId} · {experiment.Config.Manifests.Count} manifest khớp hash"));
        }
        catch (Exception ex) when (ex is ArrowException or IOException or System.Text.Json.JsonException)
        {
            Add(new DoctorCheck("Cấu hình thực nghiệm, hash manifest", CheckStatus.Fail, ex.Message, "git pull rồi chạy lại arrow setup"));
        }

        // 4. Shard
        ShardFile? shard = null;
        if (experiment is not null)
        {
            try
            {
                var (loadedShard, plan) = ShardPlanner.LoadShard(paths.Resolve(machine.Shard));
                shard = loadedShard;
                if (plan is null)
                {
                    Add(new DoctorCheck("Shard", CheckStatus.Fail, "Không có plan.json cạnh file shard"));
                }
                else if (!string.Equals(plan.ExperimentSha256, experiment.Sha256, StringComparison.OrdinalIgnoreCase))
                {
                    Add(new DoctorCheck("Shard", CheckStatus.Fail, "Phân công được tạo cho cấu hình thực nghiệm khác", "git pull; người điều phối cần chia lại shard"));
                }
                else
                {
                    var known = experiment.Config.Manifests
                        .SelectMany(m => ManifestReader.Read(experiment.ResolveManifestPath(m), m.Dataset))
                        .Select(r => (r.Dataset, r.TaskId)).ToHashSet();
                    var missing = shard.Tasks.Count(t => !known.Contains((t.Dataset, t.TaskId)));
                    Add(missing == 0
                        ? new DoctorCheck("Shard", CheckStatus.Pass, $"{shard.ShardId} · {shard.Tasks.Count} task, hash khớp plan.json")
                        : new DoctorCheck("Shard", CheckStatus.Fail, $"{missing} task không có trong manifest"));
                }
            }
            catch (Exception ex) when (ex is ArrowException or IOException or System.Text.Json.JsonException)
            {
                Add(new DoctorCheck("Shard", CheckStatus.Fail, ex.Message, "Chọn lại shard trong arrow setup"));
            }
        }

        // 5. JDK
        foreach (var major in ToolProbe.RequiredJdkMajors)
        {
            machine.JdkHomes.TryGetValue(major.ToString(), out var home);
            var info = string.IsNullOrEmpty(home) ? null : await ToolProbe.VerifyJdkAsync(home, major, cancellationToken);
            Add(info is null
                ? new DoctorCheck($"JDK {major}", CheckStatus.Fail, string.IsNullOrEmpty(home) ? "Chưa khai báo" : $"Không chạy được java/javac {major} ở {home}",
                    $"Cài JDK {major} vào ARROW/Java-version/runtime/jdk-{major} theo Java-version/README.md, rồi chạy lại arrow setup (bước 5 tự tìm)", DoctorFix.RunSetup)
                : new DoctorCheck($"JDK {major}", CheckStatus.Pass, $"{info.JavaVersion}"));
        }

        // 6. Maven, Gradle, Git
        machine.JdkHomes.TryGetValue("17", out var jdk17);
        var maven = await ToolProbe.ProbeMavenAsync(machine.MavenPath, jdk17, cancellationToken);
        Add(maven is null
            ? new DoctorCheck("Maven", CheckStatus.Warn, "Không tìm thấy", "Chỉ cần khi repository không có mvnw")
            : new DoctorCheck("Maven", CheckStatus.Pass, $"{maven.Version} · {maven.Path}"));
        var gradle = await ToolProbe.ProbeGradleAsync(machine.GradlePath, jdk17, cancellationToken);
        Add(gradle is null
            ? new DoctorCheck("Gradle", CheckStatus.Warn, "Không tìm thấy", "Chỉ cần khi repository không có gradlew")
            : new DoctorCheck("Gradle", CheckStatus.Pass, $"{gradle.Version} · {gradle.Path}"));
        var git = await ToolProbe.ProbeGitAsync(machine.GitPath, cancellationToken);
        Add(git is null
            ? new DoctorCheck("Git", CheckStatus.Fail, "Không tìm thấy", "Cài Git")
            : new DoctorCheck("Git", CheckStatus.Pass, $"{git.Version}"));

        // 7. Dung lượng
        var workDir = paths.Resolve(machine.WorkDir);
        var free = MachineInfo.FreeGb(workDir);
        Add(free >= MinFreeGb
            ? new DoctorCheck("Dung lượng trống thư mục repository", CheckStatus.Pass, $"{free} GB · {workDir}")
            : new DoctorCheck("Dung lượng trống thư mục repository", CheckStatus.Fail, $"{free} GB (< {MinFreeGb} GB) · {workDir}", "Dọn ổ hoặc đổi thư mục trong arrow setup"));

        // Đường dẫn workspace có dấu cách hoặc ký tự ngoài ASCII làm một số test fail (URL mã hóa thành %20…),
        // khiến baseline và kết quả khác nhau giữa các máy.
        var pathAdvice = PathDiagnostics.Diagnose(workDir, paths.ArrowRoot, OperatingSystem.IsWindows(), System.Environment.GetFolderPath(System.Environment.SpecialFolder.UserProfile));
        Add(pathAdvice.Clean
            ? new DoctorCheck("Đường dẫn thư mục repository", CheckStatus.Pass, "Không có dấu cách, chữ có dấu hay ký tự đặc biệt")
            : new DoctorCheck("Đường dẫn thư mục repository", pathAdvice.Blocking ? CheckStatus.Fail : CheckStatus.Warn,
                string.Join("; ", pathAdvice.Issues.Select(i => i.Problem)) + $" ({workDir})",
                "Xem các bước bên dưới", pathAdvice.Blocking ? DoctorFix.RunSetup : DoctorFix.None,
                pathAdvice.Issues.Select(i => $"Vì sao: {i.Effect}.").Concat(pathAdvice.Steps).ToList()));

        // 8. GitHub
        if (git is not null && shard is { Tasks.Count: > 0 })
        {
            var repo = shard.Tasks[0].RepoUrl;
            var lsRemote = await ProcessRunner.RunAsync(git.Path, ["ls-remote", "--heads", repo], timeout: TimeSpan.FromSeconds(60), cancellationToken: cancellationToken);
            Add(lsRemote.Succeeded
                ? new DoctorCheck("Kết nối GitHub", CheckStatus.Pass, $"git ls-remote {repo}")
                : new DoctorCheck("Kết nối GitHub", CheckStatus.Fail, lsRemote.TimedOut ? "Quá thời gian" : lsRemote.StdErr.Trim(), "Kiểm tra mạng, proxy"));
        }

        // 9. LiteLLM
        var litellmConfig = LiteLlmConfigFile.TryLoad(paths.LiteLlmConfigPath);
        if (litellmConfig is null || !litellmConfig.HasApiKey)
        {
            Add(new DoctorCheck("Khóa API và endpoint", CheckStatus.Fail, "Chưa có khóa API của máy này", "Nhập khóa ngay, hoặc arrow setup bước 6", DoctorFix.EnterApiKey));
        }
        else
        {
            Add(new DoctorCheck("Khóa API và endpoint", CheckStatus.Pass, $"{LiteLlmConfigFile.Mask(litellmConfig.ApiKey)} · {litellmConfig.EndpointHost}"));
        }

        if (machine.Llm.ProxyMode == "auto_start")
        {
            var litellm = await ToolProbe.ProbeLiteLlmAsync(paths.ArrowRoot, cancellationToken);
            var installHint = OperatingSystem.IsWindows() ? @"Cài ngay, hoặc chạy: powershell -ExecutionPolicy Bypass -File install\install-litellm.ps1" : "Cài ngay, hoặc chạy: sh install/install-litellm.sh";
            Add(litellm is null
                ? new DoctorCheck("LiteLLM", CheckStatus.Fail, "Chưa cài LiteLLM (chương trình trung gian để gọi model)", installHint, DoctorFix.InstallLiteLlm)
                : litellm.Version == ToolProbe.Broken
                    ? new DoctorCheck("LiteLLM", CheckStatus.Fail,
                        $"Có LiteLLM ở {litellm.Path} nhưng không chạy được. Thường do thư mục ARROW đã đổi tên hoặc chuyển chỗ: môi trường Python ghi cứng đường dẫn cũ",
                        "Cài lại (tạo lại môi trường Python): " + installHint, DoctorFix.InstallLiteLlm)
                    : new DoctorCheck("LiteLLM", CheckStatus.Pass, $"{litellm.Version} · {litellm.Path}"));
        }

        Add(new DoctorCheck("Gọi thử model", CheckStatus.Skip, "Gọi thử bằng lệnh smoke: arrow run --experiment experiments/smoke-v1.json --max-llm-calls 6"));
        if (litellmConfig is not null && litellmConfig.HasApiKey && !File.ReadAllText(paths.LiteLlmConfigPath).Contains("allowed_openai_params", StringComparison.Ordinal))
        {
            Add(new DoctorCheck("LiteLLM nhận reasoning_effort", CheckStatus.Fail, "litellm.config.local.yaml thiếu allowed_openai_params: [\"reasoning_effort\"]",
                "Nhập lại khóa (tool ghi lại file đúng mẫu), hoặc thêm dòng đó dưới api_key (SETUP.md, Ghi chú LiteLLM)", DoctorFix.EnterApiKey));
        }

        // 9b. Công cụ của S3, S4 (EvoSuite, JaCoCo, PIT, tsDetect) trong ARROW/tools
        var toolProblems = ToolsCatalog.Verify(paths.ArrowRoot);
        foreach (var group in ToolsCatalog.Files.GroupBy(f => f.Tool))
        {
            var problems = toolProblems.Where(p => p.File.Tool == group.Key).ToList();
            Add(problems.Count == 0
                ? new DoctorCheck(group.Key, CheckStatus.Pass, $"{group.Count()} file trong ARROW/tools, đúng SHA-256")
                : new DoctorCheck(group.Key, CheckStatus.Fail, string.Join("; ", problems.Select(p => $"{p.File.RelativePath} {p.Problem}")),
                    "Tải ngay, hoặc chạy: arrow tools install", DoctorFix.InstallTools));
        }

        // 10. Windows: đường dẫn dài
        if (OperatingSystem.IsWindows())
        {
            var reg = await ProcessRunner.RunAsync("reg", ["query", @"HKLM\SYSTEM\CurrentControlSet\Control\FileSystem", "/v", "LongPathsEnabled"], timeout: TimeSpan.FromSeconds(20), cancellationToken: cancellationToken);
            Add(reg.Output.Contains("0x1")
                ? new DoctorCheck("Windows LongPathsEnabled", CheckStatus.Pass, "Đã bật")
                : new DoctorCheck("Windows LongPathsEnabled", CheckStatus.Fail, "Chưa bật",
                    "Mở PowerShell bằng quyền Administrator, chạy: New-ItemProperty -Path HKLM:\\SYSTEM\\CurrentControlSet\\Control\\FileSystem -Name LongPathsEnabled -Value 1 -PropertyType DWORD -Force, rồi khởi động lại máy"));
        }
        else
        {
            Add(new DoctorCheck("Windows LongPathsEnabled", CheckStatus.Skip, "Không cần trên " + MachineInfo.RuntimeId));
        }

        return new DoctorReport(checks);
    }

    /// <summary>Các bước sau khi đổi tên hoặc chuyển thư mục ARROW.</summary>
    public static IReadOnlyList<string> MovedArrowSteps() => OperatingSystem.IsWindows()
        ?
        [
            @"Mở terminal mới trong ARROW\pilot_tool, chạy: powershell -ExecutionPolicy Bypass -File install\install-arrow.ps1 (lệnh arrow trỏ chỗ mới).",
            @"Chạy: powershell -ExecutionPolicy Bypass -File install\install-litellm.ps1 (tạo lại môi trường Python của LiteLLM).",
            "Chạy arrow setup: tool tự cập nhật đường dẫn ARROW và JDK; kiểm tra lại bước 3 (thư mục repository).",
            "Chạy arrow doctor, phải ra READY. Run đang dở ở chỗ cũ: chạy tiếp bằng arrow run --run-id <id> sau khi doctor READY.",
        ]
        :
        [
            "Mở terminal mới trong ARROW/pilot_tool, chạy: sh install/install-arrow.sh (lệnh arrow trỏ chỗ mới).",
            "Chạy: sh install/install-litellm.sh (tạo lại môi trường Python của LiteLLM).",
            "Chạy arrow setup: tool tự cập nhật đường dẫn ARROW và JDK; kiểm tra lại bước 3 (thư mục repository).",
            "Chạy arrow doctor, phải ra READY. Run đang dở ở chỗ cũ: chạy tiếp bằng arrow run --run-id <id> sau khi doctor READY.",
        ];

    private static DoctorCheck CheckWritable(string name, string directory)
    {
        try
        {
            Directory.CreateDirectory(directory);
            var probe = Path.Combine(directory, ".write-test");
            File.WriteAllText(probe, "ok");
            File.Delete(probe);
            return new DoctorCheck(name, CheckStatus.Pass, directory);
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
        {
            return new DoctorCheck(name, CheckStatus.Fail, ex.Message);
        }
    }
}
