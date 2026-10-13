using System.Globalization;
using System.Text;
using System.Xml.Linq;
using Arrow.Core.Config;
using Arrow.Core.Prepare;
using Arrow.Core.Util;
using Arrow.Infrastructure.Build;
using Arrow.Infrastructure.Processes;

namespace Arrow.Infrastructure.Quality;

/// <summary>Thao tác dùng chung của S3 (EvoSuite) và S4 (JaCoCo, PIT, tsDetect) trên workspace của một task.</summary>
public sealed class QualityToolkit
{
    private readonly ExperimentConfig _experiment;
    private readonly TaskInfo _task;
    private readonly string _workspace;
    private readonly string _logsDir;
    private readonly string _taskDir;
    private readonly BuildRunner _runner;
    private readonly BuildFlags _flags;

    public QualityToolkit(ExperimentConfig experiment, MachineConfig machine, TaskInfo task, string workspace, string taskDir)
    {
        _experiment = experiment;
        _task = task;
        _workspace = workspace;
        _taskDir = taskDir;
        _logsDir = Path.Combine(taskDir, "logs");
        _runner = new BuildRunner(workspace, task.Plan, new JdkChoice(task.JdkMajor, task.JdkHome, task.JdkReason), machine.MavenPath, machine.GradlePath);
        _flags = new BuildFlags
        {
            Maven = experiment.Build.MavenExtraFlags,
            Gradle = experiment.Build.GradleExtraFlags,
            GradleExcludedTasks = experiment.Build.GradleExcludedTasks,
            Windows = OperatingSystem.IsWindows(),
            MavenVersion = task.Plan.Tool == BuildToolKind.Maven && task.BuildToolVersion != "unknown" ? task.BuildToolVersion : null,
        };
    }

    public bool IsMaven => _task.Plan.Tool == BuildToolKind.Maven;
    public string Workspace => _workspace;

    /// <summary>Thư mục class main của module (nơi JaCoCo đọc class để lập báo cáo).</summary>
    public string MainClassesDir => IsMaven ? Path.Combine(ModuleDir, "target", "classes") : Path.Combine(ModuleDir, "build", "classes", "java", "main");

    public void AddJvmOptions(string options) => _runner.AddJvmOptions(options);
    public string ModuleDir => Path.Combine(_workspace, _task.Plan.ModuleDir == "." ? "" : _task.Plan.ModuleDir);
    public HashSet<string>? GradleTasks => _task.GradleTasks?.ToHashSet(StringComparer.Ordinal);
    public TimeSpan StepTimeout => TimeSpan.FromSeconds(_experiment.Build.StepTimeoutSeconds);

    public string Relative(string path) => Path.GetRelativePath(_taskDir, path).Replace('\\', '/');

    /// <summary>Chạy lệnh build với goal/task tùy ý, ghi log <c>logs/&lt;key&gt;__&lt;step&gt;.log</c>.</summary>
    public Task<StepResult> BuildAsync(string key, string step, IEnumerable<string> goals, IReadOnlyCollection<string>? dropFlags, TimeSpan timeout, CancellationToken cancellationToken) =>
        _runner.RunAsync(BuildCommands.Custom(_task.Plan, _flags, goals, GradleTasks, dropFlags), step, Path.Combine(_logsDir, $"{key}__{step}.log"), timeout, cancellationToken);

    /// <summary>
    /// Init script Gradle của bước đo: task <c>arrowClasspath</c> ghi
    /// classpath test và main vào <c>build/arrow-classpath.txt</c>; <paramref name="extraTestJars"/> thêm vào testImplementation.
    /// </summary>
    public string WriteGradleInitScript(IEnumerable<string> extraTestJars)
    {
        var jars = string.Join(", ", extraTestJars.Select(j => $"'{j.Replace("\\", "/")}'"));
        var script = Path.Combine(_workspace, ".arrow-quality.gradle");
        File.WriteAllText(script, $$"""
            allprojects { p ->
                p.pluginManager.withPlugin('java') {
                    {{(jars.Length == 0 ? "" : $"p.dependencies.add('testImplementation', p.files({jars}))")}}
                    p.tasks.register('arrowClasspath') {
                        def out = p.layout.buildDirectory.file('arrow-classpath.txt')
                        def test = p.sourceSets.test.runtimeClasspath
                        def main = p.sourceSets.main.runtimeClasspath
                        dependsOn p.tasks.named('testClasses')
                        doLast { out.get().asFile.text = 'test=' + test.asPath + '\n' + 'main=' + main.asPath + '\n' }
                    }
                }
            }
            """);
        return script;
    }

    /// <summary>Classpath test (đã biên dịch) của module: thư mục class test, class main, mọi dependency scope test.</summary>
    public async Task<(List<string>? Classpath, string Log, string? Error)> TestClasspathAsync(string key, IReadOnlyList<string> gradleArgs, CancellationToken cancellationToken)
    {
        if (IsMaven)
        {
            var plugin = BuildCommands.DependencyPluginFor(_flags.MavenVersion) + ":build-classpath";
            var result = await BuildAsync(key, "classpath", ["-DskipTests", "test-compile", plugin, "-Dmdep.outputFile=target/arrow-cp-test.txt", "-Dmdep.includeScope=test"],
                null, StepTimeout, cancellationToken).ConfigureAwait(false);
            var file = Path.Combine(ModuleDir, "target", "arrow-cp-test.txt");
            if (!result.Succeeded || !File.Exists(file))
            {
                return (null, Relative(result.LogPath), result.TimedOut ? "BUILD_TIMEOUT" : $"classpath exit {result.ExitCode}");
            }

            var entries = new List<string> { Path.Combine(ModuleDir, "target", "test-classes"), Path.Combine(ModuleDir, "target", "classes") };
            entries.AddRange((await File.ReadAllTextAsync(file, cancellationToken).ConfigureAwait(false)).Trim().Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries));
            return (entries, Relative(result.LogPath), null);
        }

        var gradle = await BuildAsync(key, "classpath", [.. gradleArgs, "arrowClasspath"], null, StepTimeout, cancellationToken).ConfigureAwait(false);
        var output = Path.Combine(ModuleDir, "build", "arrow-classpath.txt");
        if (!gradle.Succeeded || !File.Exists(output))
        {
            return (null, Relative(gradle.LogPath), gradle.TimedOut ? "BUILD_TIMEOUT" : $"classpath exit {gradle.ExitCode}");
        }

        var line = File.ReadAllLines(output).First(l => l.StartsWith("test=", StringComparison.Ordinal))[5..];
        return (line.Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries).ToList(), Relative(gradle.LogPath), null);
    }

    /// <summary>Chạy một chương trình Java (PIT, EvoSuite, tsDetect), ghi log theo định dạng của log build.</summary>
    public async Task<(ProcessResult Result, string Log)> RunJavaAsync(string javaHome, string key, string step, IReadOnlyList<string> args, string cwd, TimeSpan timeout,
        CancellationToken cancellationToken)
    {
        var java = Path.Combine(javaHome, "bin", OperatingSystem.IsWindows() ? "java.exe" : "java");
        var env = BuildRunner.BuildEnvironment(javaHome);
        var started = DateTimeOffset.UtcNow;
        var result = await ProcessRunner.RunAsync(java, args, cwd, env, timeout, cancellationToken).ConfigureAwait(false);
        var logPath = Path.Combine(_logsDir, $"{key}__{step}.log");
        var text = new StringBuilder()
            .Append("$ ").Append(java).Append(' ').AppendLine(string.Join(' ', args.Select(a => a.Contains(' ') ? $"\"{a}\"" : a)))
            .Append("cwd: ").AppendLine(cwd)
            .Append("started: ").AppendLine(started.ToString("yyyy-MM-ddTHH:mm:ssZ", CultureInfo.InvariantCulture))
            .AppendLine("--- stdout+stderr ---")
            .AppendLine(ToolLog.Redact(result.Output))
            .AppendLine("--- end ---")
            .Append("exit: ").Append(result.ExitCode?.ToString(CultureInfo.InvariantCulture) ?? "none")
            .Append(" · duration: ").Append(result.Duration.TotalSeconds.ToString("0.00", CultureInfo.InvariantCulture)).Append('s')
            .Append(" · timeout: ").AppendLine(result.TimedOut ? "true" : "false");
        AtomicFile.WriteAllText(logPath, text.ToString());
        return (result, Relative(logPath));
    }

    /// <summary>Thêm một dependency scope <c>system</c> vào POM của module (ngoại lệ đã khai báo của EvoSuite, specs/06 Mục 1 bước 5).</summary>
    public static void AddSystemDependency(string pomPath, string groupId, string artifactId, string version, string jarPath)
    {
        var document = XDocument.Load(pomPath, LoadOptions.PreserveWhitespace);
        var root = document.Root!;
        var ns = root.Name.Namespace;
        var dependencies = root.Element(ns + "dependencies");
        if (dependencies is null)
        {
            dependencies = new XElement(ns + "dependencies");
            root.Add(dependencies);
        }

        dependencies.Add(new XElement(ns + "dependency",
            new XElement(ns + "groupId", groupId),
            new XElement(ns + "artifactId", artifactId),
            new XElement(ns + "version", version),
            new XElement(ns + "scope", "system"),
            new XElement(ns + "systemPath", jarPath)));
        document.Save(pomPath);
    }
}
