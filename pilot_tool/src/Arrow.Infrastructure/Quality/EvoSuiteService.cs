using System.Diagnostics;
using Arrow.Core.Config;
using Arrow.Core.Prepare;
using Arrow.Core.Quality;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Tooling;
using Arrow.Infrastructure.Verify;

namespace Arrow.Infrastructure.Quality;

/// <summary>
/// S3 (specs/06 Mục 1): EvoSuite 1.2.0 cấu hình mặc định, seed 42, chạy bằng JDK 11 trên classpath đã biên dịch của module;
/// test sinh ra (<c>&lt;CUT&gt;_ESTest</c> + scaffolding) đi qua cùng bộ xác minh 5 bước, với ngoại lệ đã khai báo: thêm
/// <c>evosuite-standalone-runtime</c> vào scope test chỉ trong workspace của job EvoSuite.
/// </summary>
public sealed class EvoSuiteService(ToolPaths paths, ExperimentConfig experiment, MachineConfig machine, GitRepositoryProvider git, CandidateRunner candidates, string workDir)
{
    public const string SystemName = "evosuite";
    public static readonly string[] OutputVariables = ["TARGET_CLASS", "criterion", "Coverage", "Total_Goals", "Covered_Goals", "Size", "Length", "Total_Time"];

    public static string Dir(string taskDir) => Path.Combine(taskDir, "evosuite");

    /// <summary>Đường dẫn file test EvoSuite trong repository: thư mục test của module, cùng package với CUT.</summary>
    public static string TestPath(TaskInfo task, string suffix) =>
        task.GeneratedTestPath[..task.GeneratedTestPath.LastIndexOf('/')] + "/" + task.ClassFqn[(task.ClassFqn.LastIndexOf('.') + 1)..] + suffix + ".java";

    /// <summary>Bộ test EvoSuite đã sinh (để S4 đo), null nếu không có hoặc chưa VALID.</summary>
    public static SuiteToMeasure? ValidSuite(string arrowRoot, PreparedTaskData data)
    {
        var dir = Dir(data.TaskDir);
        var verifyPath = Path.Combine(dir, "verify.json");
        if (!File.Exists(verifyPath) || Json.Read<VerifyResult>(verifyPath).State != VerificationState.Valid)
        {
            return null;
        }

        var simple = data.CutSimpleName;
        return new SuiteToMeasure
        {
            System = SystemName,
            TestClassFqn = data.Task.ClassFqn + "_ESTest",
            TestPath = TestPath(data.Task, "_ESTest"),
            Code = File.ReadAllText(Path.Combine(dir, "tests", simple + "_ESTest.java")),
            ExtraFiles = new Dictionary<string, string> { [TestPath(data.Task, "_ESTest_scaffolding")] = File.ReadAllText(Path.Combine(dir, "tests", simple + "_ESTest_scaffolding.java")) },
            ExtraTestJars = [ToolsCatalog.EvoSuiteRuntimeJar(arrowRoot)],
        };
    }

    public async Task<StageOutcome> RunAsync(RunStore store, JobKey job, Action<string> reportStep, CancellationToken cancellationToken)
    {
        var data = PreparedTaskData.Load(store, job.Dataset, job.TaskId);
        var task = data.Task;
        var dir = Dir(data.TaskDir);
        Directory.CreateDirectory(dir);
        var rel = Path.GetRelativePath(store.Root, dir).Replace('\\', '/');
        var jdk11 = machine.JdkHomes.GetValueOrDefault("11");
        if (string.IsNullOrEmpty(jdk11))
        {
            return new StageOutcome { Status = JobStatus.Infra, Outcome = "INFRA", Reason = "JDK_MISSING", Message = "EvoSuite 1.2.0 cần JDK 11 (jdk_homes.11)" };
        }

        var stats = new EvoSuiteStats
        {
            JdkMajor = 11,
            DeclaredDeviations =
            [
                "evosuite-standalone-runtime-1.2.0.jar thêm vào scope test chỉ trong workspace của job EvoSuite (Maven: dependency scope system; Gradle: testImplementation files(...) qua init script)",
            ],
        };

        // 1–3. Workspace sạch, biên dịch, classpath, sinh test.
        reportStep("git");
        await git.EnsureCommitAsync(task.RepoUrl, task.CheckoutSha, cancellationToken).ConfigureAwait(false);
        var workspace = Path.Combine(workDir, "ws", store.RunId, job.ShortHash);
        await git.AddWorktreeAsync(task.RepoUrl, task.CheckoutSha, workspace, cancellationToken).ConfigureAwait(false);
        string? code = null, scaffolding = null;
        try
        {
            var toolkit = new QualityToolkit(experiment, machine, task, workspace, data.TaskDir);
            var gradleArgs = toolkit.IsMaven ? new List<string>() : ["--init-script", toolkit.WriteGradleInitScript([])];
            reportStep("classpath");
            var (classpath, cpLog, cpError) = await toolkit.TestClasspathAsync("evosuite", gradleArgs, cancellationToken).ConfigureAwait(false);
            if (classpath is null)
            {
                return Finish(stats with { Status = "FAILED", Reason = "không lấy được classpath: " + cpError, Log = cpLog }, null, "FAILED");
            }

            // projectCP: class main và dependency (bỏ thư mục class test).
            var projectCp = classpath.Where(c => !c.Replace('\\', '/').EndsWith("/test-classes", StringComparison.Ordinal)
                                                 && !c.Replace('\\', '/').EndsWith("/classes/java/test", StringComparison.Ordinal)
                                                 && !c.Replace('\\', '/').EndsWith("/resources/test", StringComparison.Ordinal)
                                                 && (File.Exists(c) || Directory.Exists(c))).ToList();
            var outDir = Path.Combine(workspace, ".arrow-evosuite");
            Directory.CreateDirectory(outDir);
            reportStep("evosuite");
            var watch = Stopwatch.StartNew();
            var (result, log) = await toolkit.RunJavaAsync(jdk11, "evosuite", "generate",
            [
                // Classpath của project cũng đưa vào JVM chính của EvoSuite: chạy "-jar" thì tiến trình chính không nạp được
                // class của project khi nhận test từ tiến trình con qua RMI, nên sinh xong mà không ghi được file test
                // (ClassNotFoundException "RMI class loader disabled"). Tham số của EvoSuite không đổi.
                "-cp", string.Join(Path.PathSeparator, new[] { ToolsCatalog.EvoSuiteJar(paths.ArrowRoot) }.Concat(projectCp)),
                "org.evosuite.EvoSuite",
                "-class", task.ClassFqn,
                "-projectCP", string.Join(Path.PathSeparator, projectCp),
                "-seed", "42",
                $"-Dtest_dir={Path.Combine(outDir, "tests")}",
                $"-Dreport_dir={Path.Combine(outDir, "report")}",
                "-Doutput_variables=" + string.Join(",", OutputVariables),
            ], outDir, TimeSpan.FromMinutes(20), cancellationToken).ConfigureAwait(false);
            stats = stats with { GenerationSeconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log, EffectiveDefaults = EffectiveDefaults(result.Output) };

            var packagePath = task.ClassFqn.Contains('.') ? task.ClassFqn[..task.ClassFqn.LastIndexOf('.')].Replace('.', Path.DirectorySeparatorChar) : "";
            var generated = Path.Combine(outDir, "tests", packagePath, data.CutSimpleName + "_ESTest.java");
            var generatedScaffolding = Path.Combine(outDir, "tests", packagePath, data.CutSimpleName + "_ESTest_scaffolding.java");
            var statistics = Path.Combine(outDir, "report", "statistics.csv");
            if (File.Exists(statistics))
            {
                stats = stats with { Statistics = ParseStatistics(await File.ReadAllTextAsync(statistics, cancellationToken).ConfigureAwait(false)) };
            }

            if (!File.Exists(generated) || !File.Exists(generatedScaffolding))
            {
                var unsupported = result.Output.Contains("Unsupported class file major version", StringComparison.Ordinal)
                                  || result.Output.Contains("UnsupportedClassVersionError", StringComparison.Ordinal);
                var reason = result.TimedOut ? "timeout 20 phút" : unsupported ? "UNSUPPORTED_JDK" : $"EvoSuite không sinh test (exit {result.ExitCode})";
                return Finish(stats with { Status = unsupported ? "UNSUPPORTED_JDK" : "FAILED", Reason = reason }, null, unsupported ? "UNSUPPORTED_JDK" : "FAILED");
            }

            code = await File.ReadAllTextAsync(generated, cancellationToken).ConfigureAwait(false);
            scaffolding = await File.ReadAllTextAsync(generatedScaffolding, cancellationToken).ConfigureAwait(false);
        }
        finally
        {
            await git.RemoveWorktreeAsync(task.RepoUrl, workspace, CancellationToken.None).ConfigureAwait(false);
        }

        Directory.CreateDirectory(Path.Combine(dir, "tests"));
        await File.WriteAllTextAsync(Path.Combine(dir, "tests", data.CutSimpleName + "_ESTest.java"), code, cancellationToken).ConfigureAwait(false);
        await File.WriteAllTextAsync(Path.Combine(dir, "tests", data.CutSimpleName + "_ESTest_scaffolding.java"), scaffolding, cancellationToken).ConfigureAwait(false);

        // 6. Xác minh 5 bước như ARROW.
        var runtimeJar = ToolsCatalog.EvoSuiteRuntimeJar(paths.ArrowRoot);
        var gradleInit = new List<string>();
        var verify = await candidates.VerifyAsync(task, data.Baseline, store.RunId, job.ShortHash + "v", data.TaskDir, new CandidateSpec
        {
            Code = code,
            TestPath = TestPath(task, "_ESTest"),
            TestClassFqn = task.ClassFqn + "_ESTest",
            LogKey = "evosuite",
            ExtraFiles = new Dictionary<string, string> { [TestPath(task, "_ESTest_scaffolding")] = scaffolding },
            ExtraBuildArgs = gradleInit,
            PrepareWorkspace = workspace =>
            {
                var toolkit = new QualityToolkit(experiment, machine, task, workspace, data.TaskDir);
                if (toolkit.IsMaven)
                {
                    QualityToolkit.AddSystemDependency(Path.Combine(toolkit.ModuleDir, "pom.xml"), "org.evosuite", "evosuite-standalone-runtime", "1.2.0", runtimeJar);
                }
                else
                {
                    gradleInit.AddRange(["--init-script", toolkit.WriteGradleInitScript([runtimeJar])]);
                }

                return Task.CompletedTask;
            },
        }, reportStep, cancellationToken).ConfigureAwait(false);
        Json.WriteAtomic(Path.Combine(dir, "verify.json"), verify);
        var outcome = Finish(stats, verify, States.Name(verify.State));
        return outcome;

        StageOutcome Finish(EvoSuiteStats final, VerifyResult? verified, string outcomeName)
        {
            Json.WriteAtomic(Path.Combine(dir, "evosuite_stats.json"), final);
            var infra = verified?.State == VerificationState.Infra;
            return new StageOutcome
            {
                Status = infra ? JobStatus.Infra : JobStatus.Done,
                Outcome = outcomeName,
                Reason = infra ? verified!.InfraReason : final.Status == "OK" ? null : final.Reason,
                Message = verified is null ? final.Reason ?? "" : $"sinh {final.GenerationSeconds:0}s · {verified.ErrorSignature ?? "VALID"}",
                BuildSeconds = Math.Round(final.GenerationSeconds + (verified?.TotalBuildSeconds ?? 0), 2),
                Outputs = new[] { "evosuite_stats.json", "verify.json" }.Where(f => File.Exists(Path.Combine(dir, f))).Select(f => $"{rel}/{f}").ToList(),
            };
        }
    }

    private static Dictionary<string, string> ParseStatistics(string csv)
    {
        var lines = csv.Replace("\r\n", "\n").Split('\n', StringSplitOptions.RemoveEmptyEntries);
        if (lines.Length < 2)
        {
            return [];
        }

        var header = lines[0].Split(',');
        var values = lines[^1].Split(',');
        return header.Zip(values).ToDictionary(p => p.First, p => p.Second);
    }

    /// <summary>Giá trị mặc định thực tế ghi trong log (specs/06 Mục 1 bước 3).</summary>
    private static Dictionary<string, string> EffectiveDefaults(string log)
    {
        var defaults = new Dictionary<string, string>();
        foreach (var line in log.Split('\n'))
        {
            var match = System.Text.RegularExpressions.Regex.Match(line, @"\*\s*(Search budget|Total number of goals|Generated \d+ tests|Using seed|Starting evolution|Criterion|Criteria)[:\s]*(.*)$", System.Text.RegularExpressions.RegexOptions.IgnoreCase);
            if (match.Success)
            {
                defaults[match.Groups[1].Value.Trim()] = match.Groups[2].Value.Trim();
            }
        }

        defaults["search_budget"] = defaults.GetValueOrDefault("Search budget", "60 (mặc định của EvoSuite 1.2.0, không truyền tham số)");
        return defaults;
    }
}
