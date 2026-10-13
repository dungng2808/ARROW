using System.Text.RegularExpressions;
using Arrow.Models.Block1;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>Chạy Maven/Gradle từ build root với bộ chọn module, cờ chung và JDK của task (spec 01 Mục 6).</summary>
public sealed class BuildRunner(IProcessRunner runner) : IBuildRunner
{
    /// <summary>JVM của mọi lệnh build: mã hóa, ngôn ngữ và múi giờ cố định để kết quả test không phụ thuộc máy.</summary>
    public const string JavaToolOptions = "-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=UTC";

    public async Task<BuildRun> RunAsync(BuildContext context, IReadOnlyList<string> arguments, CancellationToken cancellationToken)
    {
        var command = BuildCommands.Command(context, arguments);
        var workingDirectory = context.BuildRootPath;
        var environment = new Dictionary<string, string?>
        {
            ["JAVA_HOME"] = context.JdkHome,
            ["PATH"] = Path.Combine(context.JdkHome, "bin") + Path.PathSeparator + Environment.GetEnvironmentVariable("PATH"),
            ["JAVA_TOOL_OPTIONS"] = JavaToolOptions,
            ["JDK_JAVA_OPTIONS"] = null,
            ["MAVEN_ARGS"] = null,
            ["MAVEN_OPTS"] = null,
            ["GRADLE_OPTS"] = null,
            ["TZ"] = "UTC",
            ["LANG"] = "en_US.UTF-8",
            ["LC_ALL"] = "en_US.UTF-8",
        };
        var result = await runner.RunAsync(new ProcessSpec(command[0], command.Skip(1).ToList(), workingDirectory, environment, context.Timeout), cancellationToken).ConfigureAwait(false);
        return new BuildRun(command, workingDirectory, result, BuildFailureClassifier.Classify(result));
    }
}

/// <summary>Dựng lệnh Maven/Gradle (dùng chung cho khối 1, 3, 4).</summary>
public static class BuildCommands
{
    /// <summary>Plugin Maven ghim phiên bản; Maven cũ hơn 3.6.3 dùng bản cũ hơn (spec 01 Mục 4.4).</summary>
    public static string DependencyPlugin(string? mavenVersion) =>
        "org.apache.maven.plugins:maven-dependency-plugin:" + (IsModernMaven(mavenVersion) ? "3.8.1" : "3.1.2");

    public static string HelpPlugin(string? mavenVersion) =>
        "org.apache.maven.plugins:maven-help-plugin:" + (IsModernMaven(mavenVersion) ? "3.4.1" : "3.2.0");

    public static bool IsModernMaven(string? version) => version is null || CompareVersions(version, "3.6.3") >= 0;

    /// <summary>
    /// Cờ chung của Maven (spec 01 Mục 6): tắt các plugin kiểm tra mã/định dạng/ký không liên quan tới test.
    /// <c>-ntp</c> chỉ có từ Maven 3.6.1.
    /// </summary>
    public static IReadOnlyList<string> MavenCommonFlags(string? mavenVersion, bool windows)
    {
        var flags = new List<string> { "-B" };
        flags.Add(mavenVersion is null || CompareVersions(mavenVersion, "3.6.1") >= 0
            ? "-ntp"
            : "-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn");
        flags.AddRange([
            "-Dstyle.color=never",
            "-Dcheckstyle.skip=true", "-Dspotless.check.skip=true", "-Dspotless.apply.skip=true", "-Dspotless.skip=true",
            "-Dformatter.skip=true", "-Dimpsort.skip=true", "-Dlicense.skip=true", "-Drat.skip=true", "-Djacoco.skip=true",
            "-Dpmd.skip=true", "-Dcpd.skip=true", "-Dspotbugs.skip=true", "-Denforcer.skip=true", "-Dmaven.javadoc.skip=true",
            "-Dgpg.skip=true",
        ]);
        if (windows)
        {
            flags.Add("-Dsurefire.useManifestOnlyJar=false");
        }

        return flags;
    }

    public static IReadOnlyList<string> GradleCommonFlags => ["--no-daemon", "--console=plain"];

    /// <summary>Lệnh đầy đủ: chương trình, cờ chung, bộ chọn module, rồi <paramref name="arguments"/>.</summary>
    public static IReadOnlyList<string> Command(BuildContext context, IReadOnlyList<string> arguments)
    {
        var command = new List<string> { context.ExecutablePath };
        if (context.Plan.Tool == "gradle")
        {
            command.AddRange(context.CommonFlags);
            if (context.InitScript is not null)
            {
                command.AddRange(["--init-script", context.InitScript]);
            }
        }
        else
        {
            command.AddRange(["-f", "pom.xml"]);
            command.AddRange(context.CommonFlags);
            if (context.Plan.Mode == BuildMode.Reactor && context.Plan.Module is not null)
            {
                command.AddRange(["-pl", context.Plan.Module, "-am"]);
            }
        }

        command.AddRange(arguments);
        return command;
    }

    // ── Đối số của từng bước ──────────────────────────────────────────

    /// <summary>Biên dịch test và lấy dependency, classpath trong <b>một</b> lần gọi Maven (spec 01 Mục 4.4 bước 2).</summary>
    public static IReadOnlyList<string> MavenCompileWithDependencies(string? mavenVersion)
    {
        var plugin = DependencyPlugin(mavenVersion);
        return
        [
            "-DskipTests", "test-compile",
            $"{plugin}:list", "-DincludeScope=test", "-DoutputAbsoluteArtifactFilename=true", "-DoutputFile=" + MavenDependencyFile, "-DappendOutput=false",
            $"{plugin}:build-classpath", "-Dmdep.outputFile=" + MavenClasspathFile,
        ];
    }

    public const string MavenDependencyFile = "arrow-deps.txt";
    public const string MavenClasspathFile = "arrow-cp.txt";

    public static IReadOnlyList<string> MavenEffectivePom(string? mavenVersion, string outputFile) =>
        [$"{HelpPlugin(mavenVersion)}:effective-pom", "-Doutput=" + outputFile];

    /// <summary>Chạy test của module (baseline, bước 5 của khối 3): test fail không làm lệnh thất bại.</summary>
    public static IReadOnlyList<string> MavenModuleTest => ["-DfailIfNoTests=false", "-Dsurefire.failIfNoSpecifiedTests=false", "-Dmaven.test.failure.ignore=true", "test"];

    public static IReadOnlyList<string> GradleInfo(string? project) => ["-q", Block1.GradleProbe.Task(project, "arrowInfo")];

    public static IReadOnlyList<string> GradleCompileWithClasspath(string? project) =>
        [Block1.GradleProbe.Task(project, "testClasses"), Block1.GradleProbe.Task(project, "arrowClasspath")];

    public static IReadOnlyList<string> GradleModuleTest(string? project) => [Block1.GradleProbe.Task(project, "test"), "--continue"];

    /// <summary>So sánh phiên bản dạng <c>3.9.6</c>; phần không phải số bị bỏ.</summary>
    public static int CompareVersions(string left, string right)
    {
        static int[] Parts(string v) => v.Split('.', '-').Select(p => int.TryParse(new string(p.TakeWhile(char.IsDigit).ToArray()), out var n) ? n : 0).ToArray();
        var a = Parts(left);
        var b = Parts(right);
        for (var i = 0; i < Math.Max(a.Length, b.Length); i++)
        {
            var x = i < a.Length ? a[i] : 0;
            var y = i < b.Length ? b[i] : 0;
            if (x != y)
            {
                return x.CompareTo(y);
            }
        }

        return 0;
    }
}

/// <summary>Phân loại lệnh build thất bại: lỗi mạng (thử lại) hay lỗi build (đổi JDK hoặc kết luận).</summary>
public static partial class BuildFailureClassifier
{
    private static readonly string[] NetworkMarkers =
    [
        "connection reset", "connection refused", "connect timed out", "read timed out", "connection timed out", "unknownhostexception",
        "unknown host", "network is unreachable", "no route to host", "temporary failure in name resolution", "remote host terminated the handshake",
        "premature end of content-length", "socketexception", "sockettimeoutexception", "could not resolve host",
    ];

    [GeneratedRegex(@"status code:?\s*(5\d\d|429)\b|received status code (5\d\d|429)", RegexOptions.IgnoreCase)]
    private static partial Regex ServerError();

    public static BuildFailureKind Classify(ProcessResult result)
    {
        if (result.TimedOut)
        {
            return BuildFailureKind.Timeout;
        }

        if (result.ExitCode == 0)
        {
            return BuildFailureKind.None;
        }

        var text = result.Output.ToLowerInvariant();
        return NetworkMarkers.Any(text.Contains) || ServerError().IsMatch(result.Output) ? BuildFailureKind.Network : BuildFailureKind.Build;
    }

    [GeneratedRegex(@"Apache Maven (\d+(\.\d+)*)")]
    private static partial Regex MavenVersionLine();

    [GeneratedRegex(@"^Gradle (\d+(\.\d+)*)", RegexOptions.Multiline)]
    private static partial Regex GradleVersionLine();

    /// <summary>Phiên bản trong output của <c>mvn -v</c> / <c>gradle -v</c>.</summary>
    public static string? ToolVersion(string output) =>
        MavenVersionLine().Match(output) is { Success: true } maven ? maven.Groups[1].Value
        : GradleVersionLine().Match(output) is { Success: true } gradle ? gradle.Groups[1].Value
        : null;
}
