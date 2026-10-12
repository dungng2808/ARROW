namespace Arrow.Core.Prepare;

/// <summary>Các bước build có log riêng (specs/02 §4.4, §7).</summary>
public static class BuildStep
{
    public const string ToolVersion = "tool_version";
    public const string GradleTasks = "gradle_tasks";
    public const string MainCompile = "main_compile";
    public const string TestCompile = "test_compile";
    public const string TargetTest = "target_test";
    public const string ModuleTest = "module_test";
    public const string Dependencies = "dependencies";
}

/// <summary>Một lệnh build: tham số (không gồm chương trình) và thư mục chạy tương đối so với gốc repository.</summary>
public sealed record BuildCommand(IReadOnlyList<string> Arguments, string WorkingDir);

public sealed record BuildFlags
{
    public IReadOnlyList<string> Maven { get; init; } = [];
    public IReadOnlyList<string> Gradle { get; init; } = [];

    /// <summary>Task Gradle muốn loại; chỉ thêm <c>-x</c> cho task có trong <c>gradle_tasks</c>.</summary>
    public IReadOnlyList<string> GradleExcludedTasks { get; init; } = [];
    public bool Windows { get; init; }

    /// <summary>Phiên bản Maven thực sự chạy (wrapper có thể cũ); null nếu chưa biết.</summary>
    public string? MavenVersion { get; init; }
}

/// <summary>Dựng lệnh Maven/Gradle (specs/02 §4). Code thuần, kiểm thử được.</summary>
public static class BuildCommands
{
    public const string MavenDependencyPlugin = "org.apache.maven.plugins:maven-dependency-plugin:3.8.1";

    /// <summary>
    /// Tham số JVM cho mọi tiến trình Java của build và test (qua <c>JAVA_TOOL_OPTIONS</c>, specs/02 §3 bước 6):
    /// cố định encoding, locale và múi giờ để kết quả không phụ thuộc cài đặt của máy (máy tiếng Việt in "9:00SA").
    /// </summary>
    public static readonly IReadOnlyList<string> JvmOptions =
        ["-Dfile.encoding=UTF-8", "-Duser.language=en", "-Duser.country=US", "-Duser.timezone=UTC"];

    /// <summary>Bản cho Maven trước 3.6.3 (3.8.1 đòi Maven 3.6.3); cùng goal <c>list</c> và cùng tham số.</summary>
    public const string MavenDependencyPluginLegacy = "org.apache.maven.plugins:maven-dependency-plugin:3.1.2";

    public static string DependencyPluginFor(string? mavenVersion) =>
        IsOlderThan(mavenVersion, 3, 6, 3) ? MavenDependencyPluginLegacy : MavenDependencyPlugin;

    /// <summary>File danh sách dependency của Maven, tương đối so với module (Maven căn tham số File theo basedir).</summary>
    public const string MavenDependencyOutput = "target/arrow-dependencies.txt";

    /// <param name="testClass">FQCN class test (chỉ cho <see cref="BuildStep.TargetTest"/>).</param>
    /// <param name="gradleTasks">Tên task có trong module (không có tiền tố project); null nếu chưa đọc.</param>
    public static BuildCommand For(BuildPlan plan, string step, BuildFlags flags, string? testClass = null, IReadOnlyCollection<string>? gradleTasks = null) =>
        plan.Tool == BuildToolKind.Maven ? Maven(plan, step, flags, testClass) : Gradle(plan, step, flags, testClass, gradleTasks);

    private static BuildCommand Maven(BuildPlan plan, string step, BuildFlags flags, string? testClass)
    {
        if (step == BuildStep.ToolVersion)
        {
            return new BuildCommand(["-v"], plan.WorkingDir);
        }

        var args = new List<string> { "-f", "pom.xml" };
        if (plan.Mode == BuildMode.Reactor)
        {
            args.AddRange(["-pl", plan.ModulePath, "-am"]);
        }

        args.AddRange(flags.Maven.Select(f => AdaptMavenFlag(f, flags.MavenVersion)));
        if (flags.Windows)
        {
            args.Add("-Dsurefire.useManifestOnlyJar=false");
        }

        args.AddRange(step switch
        {
            BuildStep.MainCompile => ["-DskipTests", "compile"],
            BuildStep.TestCompile => ["-DskipTests", "test-compile"],
            BuildStep.TargetTest => ["-DfailIfNoTests=false", "-Dsurefire.failIfNoSpecifiedTests=false", $"-Dtest={SimpleName(Require(testClass, step))}", "test"],
            BuildStep.ModuleTest => ["-DfailIfNoTests=false", "-Dmaven.test.failure.ignore=true", "test"],
            // test-compile trước để module anh em trong reactor đã có class khi liệt kê dependency.
            BuildStep.Dependencies => ["-DskipTests", "test-compile", $"{DependencyPluginFor(flags.MavenVersion)}:list", "-DincludeScope=test", $"-DoutputFile={MavenDependencyOutput}", "-DappendOutput=false"],
            _ => throw new ArgumentOutOfRangeException(nameof(step), step, "Bước Maven không hỗ trợ"),
        });
        return new BuildCommand(args, plan.WorkingDir);
    }

    private static BuildCommand Gradle(BuildPlan plan, string step, BuildFlags flags, string? testClass, IReadOnlyCollection<string>? gradleTasks)
    {
        if (step == BuildStep.ToolVersion)
        {
            return new BuildCommand(["--version"], plan.WorkingDir);
        }

        var prefix = plan.Mode == BuildMode.Reactor ? plan.GradleProjectPath + ":" : "";
        var args = new List<string>(flags.Gradle);
        if (step != BuildStep.GradleTasks && gradleTasks is not null)
        {
            foreach (var task in flags.GradleExcludedTasks.Where(gradleTasks.Contains))
            {
                args.AddRange(["-x", prefix + task]);
            }
        }

        args.AddRange(step switch
        {
            BuildStep.GradleTasks => [prefix + "tasks", "--all"],
            BuildStep.MainCompile => [prefix + "classes"],
            BuildStep.TestCompile => [prefix + "testClasses"],
            BuildStep.TargetTest => [prefix + "test", "--tests", Require(testClass, step)],
            // Thoát mã khác 0 khi có test fail là bình thường.
            BuildStep.ModuleTest => [prefix + "test", "--continue"],
            BuildStep.Dependencies => [prefix + "dependencies", "--configuration", "testRuntimeClasspath"],
            _ => throw new ArgumentOutOfRangeException(nameof(step), step, "Bước Gradle không hỗ trợ"),
        });
        return new BuildCommand(args, plan.WorkingDir);
    }

    /// <summary>Đọc tên task từ output của <c>gradle tasks --all</c> (dòng "name - mô tả" hoặc chỉ "name").</summary>
    public static HashSet<string> ParseGradleTasks(string output)
    {
        var tasks = new HashSet<string>(StringComparer.Ordinal);
        foreach (var raw in output.Split('\n'))
        {
            var line = raw.TrimEnd('\r');
            var match = System.Text.RegularExpressions.Regex.Match(line, @"^([A-Za-z][\w:\-]*)(?: - .*)?$");
            if (match.Success && !line.EndsWith(" tasks", StringComparison.Ordinal))
            {
                var name = match.Groups[1].Value;
                tasks.Add(name.Contains(':') ? name[(name.LastIndexOf(':') + 1)..] : name);
            }
        }

        return tasks;
    }

    /// <summary>Thư mục báo cáo XML của test, tương đối so với module.</summary>
    public static string ReportDir(BuildToolKind tool) => tool == BuildToolKind.Maven ? "target/surefire-reports" : "build/test-results/test";

    /// <summary>Dấu hiệu module không thuộc reactor/settings → chạy riêng tại module (specs/02 §4.2).</summary>
    public static bool IsModuleNotInBuild(BuildToolKind tool, string output) =>
        tool == BuildToolKind.Maven
            ? output.Contains("Could not find the selected project in the reactor", StringComparison.OrdinalIgnoreCase)
            : System.Text.RegularExpressions.Regex.IsMatch(output, @"Project '[^']*' not found in root project", System.Text.RegularExpressions.RegexOptions.IgnoreCase)
              || output.Contains("not part of the build defined by settings file", StringComparison.OrdinalIgnoreCase);

    /// <summary>Dấu hiệu build cần lịch sử git đầy đủ (bản <c>--depth 1</c> không đủ, specs/02 §1).</summary>
    public static bool NeedsFullHistory(string output) =>
        output.Contains("fatal: No names found", StringComparison.Ordinal)
        || output.Contains("shallow", StringComparison.OrdinalIgnoreCase) && output.Contains("git", StringComparison.OrdinalIgnoreCase)
        || output.Contains("git-commit-id", StringComparison.OrdinalIgnoreCase) && output.Contains("ERROR", StringComparison.Ordinal);

    /// <summary>Cờ tắt log tải file cho Maven trước 3.6.1 (chưa có <c>-ntp</c>); cùng tác dụng.</summary>
    public const string LegacyNoTransferProgress = "-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn";

    /// <summary><c>-ntp</c> chỉ có từ Maven 3.6.1; wrapper cũ hơn thì đổi sang cờ tương đương (specs/02 §4.3).</summary>
    public static string AdaptMavenFlag(string flag, string? mavenVersion) =>
        flag is "-ntp" or "--no-transfer-progress" && IsOlderThan(mavenVersion, 3, 6, 1) ? LegacyNoTransferProgress : flag;

    private static bool IsOlderThan(string? version, params int[] reference)
    {
        if (version is null || !System.Text.RegularExpressions.Regex.IsMatch(version, @"^\d+(\.\d+)*$"))
        {
            return false;
        }

        var parts = version.Split('.').Select(int.Parse).ToArray();
        for (var i = 0; i < reference.Length; i++)
        {
            var value = i < parts.Length ? parts[i] : 0;
            if (value != reference[i])
            {
                return value < reference[i];
            }
        }

        return false;
    }

    private static string SimpleName(string fqcn) => fqcn.Contains('.') ? fqcn[(fqcn.LastIndexOf('.') + 1)..] : fqcn;

    private static string Require(string? value, string step) =>
        value ?? throw new ArgumentException($"Bước {step} cần tên class test");
}
