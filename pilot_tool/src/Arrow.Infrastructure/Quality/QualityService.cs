using System.Diagnostics;
using Arrow.Core.Config;
using Arrow.Core.Quality;
using Arrow.Core.Runs;
using Arrow.Core.Util;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Java;
using Arrow.Infrastructure.Tooling;

namespace Arrow.Infrastructure.Quality;

/// <summary>Một bộ test hợp lệ cần đo (của ARROW sau sinh hoặc sau sửa, hoặc của EvoSuite).</summary>
public sealed record SuiteToMeasure
{
    /// <summary>Tên thư mục <c>quality/&lt;system&gt;/</c>, ví dụ <c>arrow__gpt-6-luna__few_shot__gen</c>, <c>evosuite</c>.</summary>
    public required string System { get; init; }
    public required string TestClassFqn { get; init; }
    public required string TestPath { get; init; }
    public required string Code { get; init; }
    public IReadOnlyDictionary<string, string> ExtraFiles { get; init; } = new Dictionary<string, string>();

    /// <summary>Jar thêm vào scope test (EvoSuite runtime).</summary>
    public IReadOnlyList<string> ExtraTestJars { get; init; } = [];
}

/// <summary>
/// S4 (specs/06 Mục 2): trên workspace sạch có đúng một file test sinh ra (cộng scaffolding với EvoSuite), đo JaCoCo 0.8.12
/// (chỉ chạy class test đó), PIT 1.17.4 (bản dòng lệnh, chỉ test đó, chỉ CUT), tsDetect 2.2. Thiếu số đo thì <c>status</c> khác OK
/// kèm lý do, không ghi 0.
/// </summary>
public sealed class QualityService(ToolPaths paths, ExperimentConfig experiment, MachineConfig machine, GitRepositoryProvider git, JavaAnalyzer analyzer, string workDir)
{
    public static string Dir(string taskDir, string system) => Path.Combine(taskDir, "quality", system);

    public async Task<StageOutcome> MeasureAsync(RunStore store, JobKey job, SuiteToMeasure suite, Action<string> reportStep, CancellationToken cancellationToken)
    {
        var data = PreparedTaskData.Load(store, job.Dataset, job.TaskId);
        var task = data.Task;
        var outDir = Dir(data.TaskDir, suite.System);
        Directory.CreateDirectory(outDir);
        var key = $"quality-{suite.System}";

        reportStep("git");
        await git.EnsureCommitAsync(task.RepoUrl, task.CheckoutSha, cancellationToken).ConfigureAwait(false);
        var workspace = Path.Combine(workDir, "ws", store.RunId, job.ShortHash);
        await git.AddWorktreeAsync(task.RepoUrl, task.CheckoutSha, workspace, cancellationToken).ConfigureAwait(false);
        try
        {
            var toolkit = new QualityToolkit(experiment, machine, task, workspace, data.TaskDir);
            var testFile = Path.Combine(workspace, suite.TestPath);
            Directory.CreateDirectory(Path.GetDirectoryName(testFile)!);
            await File.WriteAllTextAsync(testFile, suite.Code, cancellationToken).ConfigureAwait(false);
            foreach (var (path, content) in suite.ExtraFiles)
            {
                var full = Path.Combine(workspace, path);
                Directory.CreateDirectory(Path.GetDirectoryName(full)!);
                await File.WriteAllTextAsync(full, content, cancellationToken).ConfigureAwait(false);
            }

            var gradleArgs = new List<string>();
            if (toolkit.IsMaven)
            {
                foreach (var jar in suite.ExtraTestJars)
                {
                    QualityToolkit.AddSystemDependency(Path.Combine(toolkit.ModuleDir, "pom.xml"), "org.evosuite", "evosuite-standalone-runtime", "1.2.0", jar);
                }
            }
            else
            {
                gradleArgs.AddRange(["--init-script", toolkit.WriteGradleInitScript(suite.ExtraTestJars)]);
            }

            reportStep("jacoco");
            var jacoco = await JacocoAsync(toolkit, key, suite, gradleArgs, data, cancellationToken).ConfigureAwait(false);
            Json.WriteAtomic(Path.Combine(outDir, "jacoco.json"), jacoco);

            reportStep("pit");
            var pit = await PitAsync(toolkit, key, suite, gradleArgs, data, cancellationToken).ConfigureAwait(false);
            Json.WriteAtomic(Path.Combine(outDir, "pit.json"), pit);

            reportStep("tsdetect");
            var smells = await TsDetectAsync(toolkit, key, testFile, Path.Combine(workspace, task.ClassPath), suite.Code, cancellationToken).ConfigureAwait(false);
            Json.WriteAtomic(Path.Combine(outDir, "tsdetect.json"), smells);

            var rel = Path.GetRelativePath(store.Root, outDir).Replace('\\', '/');
            var ok = new[] { jacoco.Status, pit.Status, smells.Status }.Count(s => s == "OK");
            return new StageOutcome
            {
                Outcome = ok == 3 ? "OK" : "PARTIAL",
                Message = $"line {Fmt(jacoco.Line)} · branch {Fmt(jacoco.Branch)} · mutation {Fmt(pit.MutationScore)} · smells {smells.TotalSmells?.ToString() ?? "-"}"
                          + (ok == 3 ? "" : $" ({string.Join(", ", new[] { ("jacoco", jacoco.Status, jacoco.Reason), ("pit", pit.Status, pit.Reason), ("tsdetect", smells.Status, smells.Reason) }.Where(t => t.Item2 != "OK").Select(t => $"{t.Item1}: {t.Item3}"))})"),
                BuildSeconds = Math.Round(jacoco.Seconds + pit.Seconds + smells.Seconds, 2),
                Outputs = [$"{rel}/jacoco.json", $"{rel}/pit.json", $"{rel}/tsdetect.json"],
            };
        }
        finally
        {
            await git.RemoveWorktreeAsync(task.RepoUrl, workspace, CancellationToken.None).ConfigureAwait(false);
        }
    }

    private static string Fmt(double? value) => value is null ? "-" : value.Value.ToString("0.00", System.Globalization.CultureInfo.InvariantCulture);

    private async Task<CoverageResult> JacocoAsync(QualityToolkit toolkit, string key, SuiteToMeasure suite, List<string> gradleArgs, PreparedTaskData data,
        CancellationToken cancellationToken)
    {
        var watch = Stopwatch.StartNew();
        var simple = suite.TestClassFqn[(suite.TestClassFqn.LastIndexOf('.') + 1)..];
        var result = toolkit.IsMaven
            ? await toolkit.BuildAsync(key, "jacoco", ["-DfailIfNoTests=false", "-Dsurefire.failIfNoSpecifiedTests=false", "-Dmaven.test.failure.ignore=true", $"-Dtest={simple}",
                "org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent", "test", "org.jacoco:jacoco-maven-plugin:0.8.12:report"], ["-Djacoco.skip=true"], toolkit.StepTimeout, cancellationToken).ConfigureAwait(false)
            : await toolkit.BuildAsync(key, "jacoco", [.. gradleArgs, "test", "--tests", suite.TestClassFqn, "jacocoTestReport", "--continue"], null, toolkit.StepTimeout, cancellationToken).ConfigureAwait(false);
        var csv = toolkit.IsMaven
            ? Path.Combine(toolkit.ModuleDir, "target", "site", "jacoco", "jacoco.csv")
            : Path.Combine(toolkit.ModuleDir, "build", "reports", "jacoco", "test", "jacocoTestReport.csv");
        var log = toolkit.Relative(result.LogPath);
        if (!File.Exists(csv))
        {
            return new CoverageResult { Status = "FAILED", Reason = result.TimedOut ? "timeout" : $"không có báo cáo JaCoCo (exit {result.ExitCode})", Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log };
        }

        var coverage = QualityParsers.ParseJacocoCsv(await File.ReadAllTextAsync(csv, cancellationToken).ConfigureAwait(false), data.Task.ClassFqn);
        return coverage is null
            ? new CoverageResult { Status = "FAILED", Reason = "CUT không có trong báo cáo JaCoCo", Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log }
            : coverage with { Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log };
    }

    private async Task<MutationResult> PitAsync(QualityToolkit toolkit, string key, SuiteToMeasure suite, List<string> gradleArgs, PreparedTaskData data,
        CancellationToken cancellationToken)
    {
        var watch = Stopwatch.StartNew();
        var (classpath, cpLog, cpError) = await toolkit.TestClasspathAsync(key, gradleArgs, cancellationToken).ConfigureAwait(false);
        if (classpath is null)
        {
            return new MutationResult { Status = "FAILED", Reason = "không lấy được classpath test: " + cpError, Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = cpLog };
        }

        classpath.AddRange(suite.ExtraTestJars);
        var platform = classpath.Any(c => Path.GetFileName(c).StartsWith("junit-platform", StringComparison.Ordinal) || Path.GetFileName(c).StartsWith("junit-jupiter", StringComparison.Ordinal));
        if (platform && !classpath.Any(c => Path.GetFileName(c).StartsWith("junit-platform-launcher", StringComparison.Ordinal)))
        {
            // Maven: Surefire tự mang junit-platform-launcher nên classpath test không có; plugin JUnit 5 của PIT cần nó.
            var launcher = await ToolsCatalog.EnsureJunitLauncherAsync(paths.ArrowRoot, classpath, cancellationToken).ConfigureAwait(false);
            if (launcher is not null)
            {
                classpath.Add(launcher);
            }
        }

        var full = ToolsCatalog.PitClasspath(paths.ArrowRoot, platform).Concat(classpath);
        var reportDir = Path.Combine(toolkit.ModuleDir, ".arrow-pit");
        var (result, log) = await toolkit.RunJavaAsync(data.Task.JdkHome, key, "pit",
        [
            "-cp", string.Join(Path.PathSeparator, full),
            "org.pitest.mutationtest.commandline.MutationCoverageReport",
            "--reportDir", reportDir,
            "--targetClasses", data.Task.ClassFqn,
            "--targetTests", suite.TestClassFqn,
            "--sourceDirs", Path.Combine(toolkit.ModuleDir, "src", "main", "java"),
            "--outputFormats", "CSV,XML",
            "--timestampedReports=false",
            "--threads", "2",
            "--timeoutConst", "8000",
        ], toolkit.ModuleDir, TimeSpan.FromMinutes(30), cancellationToken).ConfigureAwait(false);
        var csv = Path.Combine(reportDir, "mutations.csv");
        if (!File.Exists(csv))
        {
            return new MutationResult { Status = "FAILED", Reason = result.TimedOut ? "timeout 30 phút" : $"PIT lỗi (exit {result.ExitCode})", Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log };
        }

        var mutation = QualityParsers.ParsePitCsv(await File.ReadAllTextAsync(csv, cancellationToken).ConfigureAwait(false), data.Task.ClassFqn);
        return mutation is null
            ? new MutationResult { Status = "FAILED", Reason = "PIT không sinh mutant nào cho CUT", Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log }
            : mutation with { Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log };
    }

    private async Task<SmellResult> TsDetectAsync(QualityToolkit toolkit, string key, string testFile, string cutFile, string code, CancellationToken cancellationToken)
    {
        var watch = Stopwatch.StartNew();
        var dir = Path.Combine(Path.GetDirectoryName(testFile)!, ".arrow-tsdetect");
        Directory.CreateDirectory(dir);
        var input = Path.Combine(dir, "input.csv");
        await File.WriteAllTextAsync(input, $"app,{testFile},{cutFile}\n", cancellationToken).ConfigureAwait(false);
        var javaHome = new[] { "17", "21", "11" }.Select(v => machine.JdkHomes.GetValueOrDefault(v)).First(h => !string.IsNullOrEmpty(h))!;
        var (result, log) = await toolkit.RunJavaAsync(javaHome, key, "tsdetect", ["-jar", ToolsCatalog.TsDetectJar(paths.ArrowRoot), input], dir, TimeSpan.FromMinutes(10), cancellationToken)
            .ConfigureAwait(false);
        var output = Directory.EnumerateFiles(dir, "Output_TestSmellDetection_*.csv").OrderBy(f => f, StringComparer.Ordinal).LastOrDefault();
        var parsed = output is null ? null : QualityParsers.ParseTsDetectCsv(await File.ReadAllTextAsync(output, cancellationToken).ConfigureAwait(false));
        if (parsed is null)
        {
            return new SmellResult { Status = "FAILED", Reason = $"tsDetect không ra kết quả (exit {result.ExitCode})", Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2), Log = log };
        }

        var analysis = await analyzer.AnalyzeTestAsync(code, [], cancellationToken).ConfigureAwait(false);
        return parsed with
        {
            TestMethods = analysis.TestMethods.Count,
            Lines = code.TrimEnd().Split('\n').Length,
            Seconds = Math.Round(watch.Elapsed.TotalSeconds, 2),
            Log = log,
        };
    }
}
