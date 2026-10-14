using System.Xml.Linq;
using Arrow.DataAccess.Files;
using Arrow.Models.Block1;
using Arrow.Models.Common;
using Arrow.Models.Manifest;
using Arrow.Service;
using Arrow.Service.Block1;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Tests;

/// <summary>Logic thuần của khối 1 theo các ví dụ của spec 01 (Mục 4, 6, 9, 10).</summary>
public class Block1Tests
{
    private static readonly string ArrowRoot = ServiceFactory.Locate(AppContext.BaseDirectory)!.ArrowRoot;

    private static TaskRecord RealTask(string manifest, string taskId) =>
        new FileManifestRepository().ReadTasks(Path.Combine(ArrowRoot, "evaluation_manifest", manifest), "classes2test").Single(t => t.TaskId == taskId);

    // ── Mục 4.3: java_version của manifest ────────────────────────────

    [Theory]
    [InlineData("openjdk version \"1.8.0_504\"", 8)]
    [InlineData("openjdk version \"17.0.20.1\" 2026-08-18 LTS", 17)]
    [InlineData("openjdk version \"11.0.32.1\" 2026-07-21", 11)]
    [InlineData("openjdk version \"21\" 2023-09-19", 21)]
    [InlineData("", null)]
    [InlineData("không rõ", null)]
    public void ParsesManifestJavaMajor(string text, int? expected) => Assert.Equal(expected, ManifestJavaVersionParser.ParseMajor(text));

    [Theory]
    [InlineData("1.7", 7)]
    [InlineData("1.8", 8)]
    [InlineData("11", 11)]
    [InlineData("17.0.2", 17)]
    [InlineData("JavaVersion.VERSION_1_8", 8)]
    [InlineData("VERSION_17", 17)]
    [InlineData("${java.version}", null)]
    public void ParsesDeclaredLevels(string text, int? expected) => Assert.Equal(expected, ManifestJavaVersionParser.ParseLevel(text));

    // ── Mục 4.1: build root từ lần biên dịch test thành công ─────────

    [Fact]
    public void BuildRootIsSubdirectoryForLibsodium()
    {
        var plan = BuildRootRule.FromManifest(RealTask("pilot/classes2test_pilot_50_seed42_v2.jsonl", "118477931_429246acc7725217"))!;
        Assert.Equal("maven", plan.Tool);
        Assert.Equal("3rdParty/libsodium-jni-2.0.1", plan.BuildRoot);
        Assert.Equal(BuildMode.Standalone, plan.Mode);
        Assert.Null(plan.Module);
        Assert.Equal("manifest:test_compile", plan.Source);
        Assert.Equal("3rdParty/libsodium-jni-2.0.1", BuildRootRule.ExpectedModuleRoot(plan));
    }

    [Fact]
    public void FailedReactorFallsBackToStandaloneModule()
    {
        var plan = BuildRootRule.FromManifest(RealTask("classes2test_200_seed42.jsonl", "103728471_1928343239506019"))!;
        Assert.Equal("integrating/spring", plan.BuildRoot);
        Assert.Equal(BuildMode.Standalone, plan.Mode);
        Assert.Equal("manifest:test_compile_fallback", plan.Source);

        var outside = BuildRootRule.FromManifest(RealTask("classes2test_200_seed42.jsonl", "9580168_f12b2e65e420fb2d"))!;
        Assert.Equal("qulice-maven-plugin/src/it/multi-module/mod-alpha", outside.BuildRoot);
        Assert.Equal(BuildMode.Standalone, outside.Mode);
    }

    [Fact]
    public void ReactorPlanAndWindowsPaths()
    {
        var task = new TaskRecord
        {
            WorkingDirectory = @"D:\Ky9\ARROW\preflight_tool\runs\x\workspaces\555001_ab",
            BuildTool = "maven",
            BuildAttempts =
            [
                new BuildAttempt { Stage = "test_compile", Command = ["mvn.cmd", "-q", "-pl", @"modules\shop-core", "-am", "-DskipTests", "test-compile"], WorkingDirectory = @"D:\Ky9\ARROW\preflight_tool\runs\x\workspaces\555001_ab", ExitCode = 0 },
            ],
        };
        var plan = BuildRootRule.FromManifest(task)!;
        Assert.Equal(".", plan.BuildRoot);
        Assert.Equal(BuildMode.Reactor, plan.Mode);
        Assert.Equal("modules/shop-core", plan.Module);
        Assert.Equal("modules/shop-core", BuildRootRule.ExpectedModuleRoot(plan));
    }

    [Fact]
    public void GradleProjectFromTask()
    {
        Assert.Equal(":core", BuildRootRule.GradleProject(["/x/gradlew", "--no-daemon", ":core:testClasses"]));
        Assert.Equal(":disco-java-agent:disco-java-agent-core", BuildRootRule.GradleProject(["gradlew", "--no-daemon", ":disco-java-agent:disco-java-agent-core:testClasses"]));
        Assert.Equal(":", BuildRootRule.GradleProject(["gradlew.bat", "--no-daemon", "testClasses"]));
        Assert.Equal("gradle", BuildRootRule.ToolOf([@"G:\x\gradlew.bat"], "maven"));
        Assert.Equal("maven", BuildRootRule.ToolOf(["/x/mvnw"], "gradle"));
        Assert.Equal(":core:test", GradleProbe.Task(":core", "test"));
        Assert.Equal(":test", GradleProbe.Task(":", "test"));
    }

    [Fact]
    public void NoSuccessfulAttemptGivesNull()
    {
        var task = new TaskRecord
        {
            WorkingDirectory = "/w",
            BuildAttempts = [new BuildAttempt { Stage = "test_compile", Command = ["mvn"], WorkingDirectory = "/w", ExitCode = 1 }],
        };
        Assert.Null(BuildRootRule.FromManifest(task));
        Assert.Null(BuildRootRule.RelativeTo("/w", "/other"));
        Assert.Equal("a/b", BuildRootRule.RelativeTo(@"C:\w", @"C:\w\a\b"));
    }

    // ── Mục 4.3: chọn JDK ────────────────────────────────────────────

    [Fact]
    public void SelectsSmallestJdkAtLeastDeclared()
    {
        int[] all = [8, 11, 17, 21];
        Assert.Equal([8, 17, 11, 21], JdkSelector.Select(7, 17, all).Order);
        Assert.Equal("declared", JdkSelector.Select(7, 17, all).Reason);
        Assert.Equal([11, 8, 17, 21], JdkSelector.Select(11, 8, all).Order);
        Assert.Equal([17, 8, 11, 21], JdkSelector.Select(null, 17, all).Order);
        Assert.Equal("manifest", JdkSelector.Select(null, 17, all).Reason);
        Assert.Equal([21, 17, 8, 11], JdkSelector.Select(19, 17, all).Order);
        Assert.Equal([17, 8], JdkSelector.Select(11, 8, [8, 17]).Order);
        Assert.Equal("fallback:17,8", JdkSelector.FallbackReason([17, 8]));
    }

    // ── Mục 4.3–4.7: effective POM, dependency:list, Gradle ──────────

    private const string EffectivePom = """
        <?xml version="1.0" encoding="UTF-8"?>
        <projects>
          <project xmlns="http://maven.apache.org/POM/4.0.0">
            <groupId>com.acme</groupId><artifactId>shop-api</artifactId>
            <properties><maven.compiler.source>1.8</maven.compiler.source></properties>
            <build><sourceDirectory>/ws/b/shop-api/src/main/java</sourceDirectory><directory>/ws/b/shop-api/target</directory></build>
          </project>
          <project xmlns="http://maven.apache.org/POM/4.0.0">
            <groupId>com.acme</groupId><artifactId>shop-core</artifactId>
            <properties><java.version>11</java.version><maven.compiler.source>${java.version}</maven.compiler.source></properties>
            <dependencies>
              <dependency><groupId>com.acme</groupId><artifactId>shop-api</artifactId><version>1.0</version></dependency>
              <dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.13.2</version><scope>test</scope></dependency>
            </dependencies>
            <build>
              <sourceDirectory>/ws/b/shop-core/src/main/java</sourceDirectory>
              <testSourceDirectory>/ws/b/shop-core/src/test/java</testSourceDirectory>
              <directory>/ws/b/shop-core/target</directory>
              <plugins><plugin><artifactId>maven-compiler-plugin</artifactId><configuration><release>17</release></configuration></plugin></plugins>
            </build>
          </project>
        </projects>
        """;

    [Fact]
    public void ReadsEffectivePomOfTargetModule()
    {
        var projects = EffectivePomReader.Parse(EffectivePom);
        Assert.Equal(2, projects.Count);
        var core = EffectivePomReader.SelectTarget(projects, "/ws/b/shop-core")!;
        Assert.Equal("shop-core", core.ArtifactId);
        // maven.compiler.source chưa nội suy (${…}) bị bỏ, nên lấy release của plugin.
        Assert.Equal("17", core.Declared);
        Assert.Equal("maven-compiler-plugin.release", core.DeclaredSource);
        Assert.Equal("/ws/b/shop-core/src/test/java", core.TestSourceDirectory);
        Assert.Equal(["compile", "test"], core.Dependencies.Select(d => d.Scope));
        Assert.Equal("shop-api", EffectivePomReader.SelectTarget(projects, "/ws/b/shop-api")!.ArtifactId);
        Assert.Equal("1.8", EffectivePomReader.SelectTarget(projects, "/ws/b/shop-api")!.Declared);
    }

    [Fact]
    public void ParsesDependencyListLines()
    {
        const string text = """

            The following files have been resolved:
               org.slf4j:slf4j-api:jar:1.7.30:compile:/Users/a/.m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar -- module org.slf4j [auto]
               junit:junit:jar:4.13.2:test:C:\Users\a\.m2\repository\junit\junit\4.13.2\junit-4.13.2.jar
               io.netty:netty-tcnative:jar:linux-x86_64:2.0.0:runtime (optional)
               com.acme:shop-api:jar:1.0:compile
               none
            """;
        var deps = MavenDependencyListParser.Parse(text);
        Assert.Equal(4, deps.Count);
        Assert.Equal(new ResolvedDependency("org.slf4j", "slf4j-api", "1.7.30", "compile", "/Users/a/.m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar"), deps[0]);
        Assert.Equal(@"C:\Users\a\.m2\repository\junit\junit\4.13.2\junit-4.13.2.jar", deps[1].Path);
        Assert.Equal(("2.0.0", "runtime"), (deps[2].Version, deps[2].Scope));
        Assert.Null(deps[3].Path);
    }

    [Fact]
    public void ParsesGradleProbeOutput()
    {
        const string output = """
            > Task :core:arrowInfo
            ARROW|gradle_version|8.5
            ARROW|project_dir|/ws/b/core
            ARROW|project|:|/ws/b
            ARROW|project|:core|/ws/b/core
            ARROW|java_declared|sourceCompatibility|1.8
            ARROW|test_src|/ws/b/core/src/test/java
            ARROW|dep|testImplementation|org.junit.jupiter|junit-jupiter|5.10.0
            ARROW|dep|compileOnly|org.projectlombok|lombok|1.18.30
            ARROW|resolved|org.junit.jupiter|junit-jupiter-api|5.10.0|/g/junit-jupiter-api-5.10.0.jar
            ARROW|cp|/ws/b/api/build/libs/api.jar
            BUILD SUCCESSFUL
            """;
        var info = GradleProbe.Parse(output);
        Assert.Equal("8.5", info.GradleVersion);
        Assert.Equal("/ws/b/core", info.ProjectDir);
        Assert.Equal(2, info.Projects.Count);
        Assert.Equal(("1.8", "sourceCompatibility"), (info.Declared, info.DeclaredSource));
        Assert.Equal(["test", "provided"], info.Dependencies.Select(d => d.Scope));
        Assert.Equal("junit-jupiter-api", Assert.Single(info.Resolved).Artifact);
        Assert.Equal(["/ws/b/api/build/libs/api.jar"], info.Classpath);
    }

    // ── Mục 4.5: test framework ─────────────────────────────────────

    private static ResolvedDependency Dep(string group, string artifact, string version = "1.0", string scope = "test") => new(group, artifact, version, scope, null);

    [Fact]
    public void DetectsTestFrameworks()
    {
        var junit5 = TestFrameworkDetector.Detect([Dep("org.junit.jupiter", "junit-jupiter-api", "5.9.2"), Dep("org.junit.vintage", "junit-vintage-engine"), Dep("org.mockito", "mockito-core", "4.0.0"), Dep("org.assertj", "assertj-core", "3.24.2")], null, "junit4");
        Assert.Equal(("junit5", "5.9.2", true), (junit5.Name, junit5.Version, junit5.Vintage));
        Assert.Equal(["org.mockito:mockito-core:4.0.0"], junit5.Mocking);
        Assert.Equal(["org.assertj:assertj-core:3.24.2"], junit5.AssertionLibs);

        var junit4 = TestFrameworkDetector.Detect([Dep("junit", "junit", "4.11"), Dep("org.hamcrest", "hamcrest-core", "1.3")], null, "junit4");
        Assert.Equal(("junit4", "dependency junit:junit (test)"), (junit4.Name, junit4.Evidence));
        Assert.Empty(junit4.AssertionLibs);

        // Có cả TestNG và JUnit: theo import của test người viết.
        List<ResolvedDependency> both = [Dep("org.testng", "testng", "7.4.0"), Dep("junit", "junit", "4.12")];
        Assert.Equal("testng", TestFrameworkDetector.Detect(both, "import org.testng.annotations.Test;\nclass A {}", "junit4").Name);
        Assert.Equal("junit4", TestFrameworkDetector.Detect(both, "import org.junit.Test;\nclass A {}", "testng").Name);
        Assert.Equal("testng", TestFrameworkDetector.Detect([Dep("org.testng", "testng")], null, "junit4").Name);

        var fromManifest = TestFrameworkDetector.Detect([], null, "junit5");
        Assert.Equal(("junit5", "manifest"), (fromManifest.Name, fromManifest.Evidence));
    }

    // ── Mục 4.6–4.7: đường dẫn ──────────────────────────────────────

    [Fact]
    public void ComputesPaths()
    {
        const string cut = "3rdParty/libsodium-jni-2.0.1/src/main/java/org/libsodium/jni/crypto/Util.java";
        var sourceRoot = PathCalculator.SourceRoot(cut, "org.libsodium.jni.crypto.Util");
        Assert.Equal("3rdParty/libsodium-jni-2.0.1/src/main/java", sourceRoot);
        Assert.Equal("3rdParty/libsodium-jni-2.0.1", PathCalculator.ModuleRootFromSourceRoot(sourceRoot));
        Assert.True(PathCalculator.ModuleMatches(sourceRoot, "3rdParty/libsodium-jni-2.0.1"));
        Assert.False(PathCalculator.ModuleMatches(sourceRoot, "."));
        Assert.True(PathCalculator.ModuleMatches("src/java", "."));
        Assert.Equal(".", PathCalculator.SourceRoot("A.java", "A"));

        var (path, fqn) = PathCalculator.GeneratedTest("shop-core/src/test/java", "com.acme.shop.OrderService", "_ArrowTest", (_, _) => false);
        Assert.Equal("shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java", path);
        Assert.Equal("com.acme.shop.OrderService_ArrowTest", fqn);
        var second = PathCalculator.GeneratedTest("src/test/java", "a.B", "_ArrowTest", (_, name) => name == "B_ArrowTest");
        Assert.Equal(("src/test/java/a/B_ArrowTest2.java", "a.B_ArrowTest2"), second);

        Assert.Equal(["com.acme.shop", "com.acme", "com.acme.shop.repo", "org.other"],
            PathCalculator.RankPackages(["org.other", "com.acme.shop.repo", "com.acme", "com.acme.shop"], "com.acme.shop"));
        Assert.Equal("org.x", JavaPackage.Of("/* package wrong; */\npackage org.x;\nclass A {}"));
    }

    // ── Mục 6.2–6.3: baseline và tiêu chí B ─────────────────────────

    private static TestCaseResult T(string cls, string name, TestCaseStatus status) => new() { ClassName = cls, Name = name, Status = status };

    [Fact]
    public void ClassifiesBaselineRuns()
    {
        IReadOnlyList<TestCaseResult> run1 = [T("a.ATest", "ok", TestCaseStatus.Passed), T("a.ATest", "bad", TestCaseStatus.Failed), T("a.ClockTest", "now", TestCaseStatus.Passed), T("a.ATest", "gone", TestCaseStatus.Passed), T("a.ATest", "ignored", TestCaseStatus.Skipped)];
        IReadOnlyList<TestCaseResult> run2 = [T("a.ATest", "ok", TestCaseStatus.Passed), T("a.ATest", "bad", TestCaseStatus.Error), T("a.ClockTest", "now", TestCaseStatus.Failed), T("a.ATest", "ignored", TestCaseStatus.Skipped)];
        var (passing, flaky, failing) = BaselineEvaluator.Classify([run1, run2]);
        Assert.Equal(["a.ATest#ok"], passing);
        Assert.Equal(["a.ATest#gone", "a.ClockTest#now"], flaky);
        Assert.Equal(["a.ATest#bad"], failing);
    }

    [Fact]
    public void PairedTestStatuses()
    {
        IReadOnlyList<TestCaseResult> pass = [T("a.UtilTest", "x", TestCaseStatus.Passed), T("a.UtilTest$Inner", "y", TestCaseStatus.Passed), T("a.OtherTest", "z", TestCaseStatus.Failed)];
        var ok = BaselineEvaluator.Paired([pass, pass], "a.UtilTest");
        Assert.Equal(PairedTestStatus.Pass, ok.Status);
        Assert.Equal([2, 2], ok.TestsRun);

        Assert.Equal(PairedTestStatus.NoPairedTest, BaselineEvaluator.Paired([pass], null).Status);
        IReadOnlyList<TestCaseResult> duplicated = [T("a.UtilTest", "x", TestCaseStatus.Passed), T("a.UtilTest", "x", TestCaseStatus.Passed)];
        Assert.Equal([1, 1], BaselineEvaluator.Paired([duplicated, duplicated], "a.UtilTest").TestsRun);
        Assert.Equal(PairedTestStatus.NoTestRun, BaselineEvaluator.Paired([pass, pass], "a.MissingTest").Status);
        IReadOnlyList<TestCaseResult> skipped = [T("a.UtilTest", "x", TestCaseStatus.Skipped)];
        Assert.Equal(PairedTestStatus.NoTestRun, BaselineEvaluator.Paired([skipped, skipped], "a.UtilTest").Status);

        IReadOnlyList<TestCaseResult> failing = [T("a.UtilTest", "x", TestCaseStatus.Failed), T("a.UtilTest", "y", TestCaseStatus.Passed)];
        Assert.Equal(PairedTestStatus.Failing, BaselineEvaluator.Paired([failing, failing], "a.UtilTest").Status);
        IReadOnlyList<TestCaseResult> once = [T("a.UtilTest", "x", TestCaseStatus.Passed), T("a.UtilTest", "y", TestCaseStatus.Passed)];
        Assert.Equal(PairedTestStatus.Flaky, BaselineEvaluator.Paired([once, failing], "a.UtilTest").Status);
    }

    // ── spec 02 Mục 2.2: khối M dạng chữ ────────────────────────────

    [Fact]
    public void RendersMetadataPromptLikeSpecExample()
    {
        var m = new RepositoryMetadata
        {
            BuildTool = new BuildToolInfo { Kind = "maven", Version = "3.9.6", BuildRoot = ".", BuildMode = BuildMode.Reactor, ReactorModule = "shop-core" },
            Java = new JavaInfo { Declared = "1.8", Major = 8, JdkMajor = 8 },
            Dependencies = new DependencyInfo
            {
                Direct = [new DependencyRef { Group = "com.google.guava", Artifact = "guava", Version = "30.1-jre", Scope = "compile" }],
                Test = [new DependencyRef { Group = "junit", Artifact = "junit", Version = "4.13.2", Scope = "test" }],
            },
            TestFramework = new TestFrameworkInfo { Name = "junit4", Version = "4.13.2", Mocking = ["org.mockito:mockito-core:3.12.4"] },
            ProjectStructure = new ProjectStructureInfo { MultiModule = true, Modules = ["shop-api", "shop-core"], TargetModule = "shop-core", ModuleRoot = "shop-core" },
            Paths = new PathsInfo { SourceRoot = "shop-core/src/main/java", GeneratedTestPath = "shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java" },
            Cut = new CutInfo { Package = "com.acme.shop" },
        };
        Assert.Equal("""
            - Build tool: Maven 3.9.6 (module shop-core, multi-module project, build root .)
            - Java version: 8 (compile with JDK 8; do not use language features or APIs newer than Java 8)
            - Test framework: JUnit 4 (4.13.2); mocking: Mockito 3.12.4; assertions: none beyond JUnit
            - Dependencies (direct): junit:junit:4.13.2, com.google.guava:guava:30.1-jre
            - Project structure: modules shop-api, shop-core; CUT package com.acme.shop
            - Source path: shop-core/src/main/java
            - Test path: shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java
            """.Replace("\r\n", "\n"), MetadataPromptRenderer.Render(m));
    }

    // ── Lệnh screen: chọn task, đoán dataset ─────────────────────────

    [Fact]
    public void ParsesTaskSelectionAndDataset()
    {
        Assert.Equal(["a_1", "b_2"], Arrow.Service.App.ScreeningService.ParseSelection(" a_1, b_2 ,,a_1"));
        var file = Path.Combine(Path.GetTempPath(), $"tasks-{Guid.NewGuid():N}.txt");
        File.WriteAllText(file, "# pilot\nx_1\n\ny_2\n");
        try
        {
            Assert.Equal(["x_1", "y_2"], Arrow.Service.App.ScreeningService.ParseSelection(file));
        }
        finally
        {
            File.Delete(file);
        }

        Assert.Empty(Arrow.Service.App.ScreeningService.ParseSelection(null));
        Assert.Equal("gitbug-java", Arrow.Service.App.ScreeningService.GuessDataset("/m/gitbug_java_pilot_9_seed42.jsonl"));
        Assert.Equal("classes2test", Arrow.Service.App.ScreeningService.GuessDataset("/m/classes2test_pilot_50_seed42_v2.jsonl"));
    }

    // ── Công cụ ngoài: phân loại lỗi, lệnh, báo cáo XML ──────────────

    [Fact]
    public void ClassifiesBuildFailures()
    {
        static ProcessResult R(int? exit, string output, bool timedOut = false) => new(exit, timedOut, output, TimeSpan.Zero);
        Assert.Equal(BuildFailureKind.None, BuildFailureClassifier.Classify(R(0, "BUILD SUCCESS")));
        Assert.Equal(BuildFailureKind.Timeout, BuildFailureClassifier.Classify(R(null, "", timedOut: true)));
        Assert.Equal(BuildFailureKind.Network, BuildFailureClassifier.Classify(R(1, "Could not transfer artifact x: Connection reset")));
        Assert.Equal(BuildFailureKind.Network, BuildFailureClassifier.Classify(R(1, "Could not GET 'https://repo/x.pom'. Received status code 502 from server")));
        Assert.Equal(BuildFailureKind.Build, BuildFailureClassifier.Classify(R(1, "[ERROR] COMPILATION ERROR : cannot find symbol")));
        Assert.Equal(BuildFailureKind.Build, BuildFailureClassifier.Classify(R(1, "Could not transfer artifact x: status code: 404")));
        Assert.Equal("3.9.6", BuildFailureClassifier.ToolVersion("Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)"));
        Assert.Equal("8.5", BuildFailureClassifier.ToolVersion("\n------\nGradle 8.5\n------"));
        Assert.Equal(GitFetchStatus.CommitMissing, GitClient.Classify(R(128, "remote: Repository not found.\nfatal: repository 'https://github.com/x/y/' not found")));
        Assert.Equal(GitFetchStatus.NetworkError, GitClient.Classify(R(128, "fatal: unable to access 'https://github.com/x/y/': Could not resolve host: github.com")));
        Assert.Equal(GitFetchStatus.Ok, GitClient.Classify(R(128, "fatal: remote error: upload-pack: not our ref 0123")));
    }

    [Fact]
    public void BuildsMavenCommandsWithSelector()
    {
        var plan = new BuildPlan { Tool = "maven", BuildRoot = ".", Mode = BuildMode.Reactor, Module = "shop-core" };
        var context = new BuildContext { RepoRoot = "/r", Plan = plan, Executable = "mvn", CommonFlags = BuildCommands.MavenCommonFlags("3.9.6", windows: false) };
        var command = BuildCommands.Command(context, BuildCommands.MavenModuleTest);
        Assert.Equal(["mvn", "-f", "pom.xml", "-B", "-ntp"], command.Take(5));
        Assert.Equal(["-pl", "shop-core", "-am", "-DfailIfNoTests=false"], command.SkipWhile(c => c != "-pl").Take(4));
        Assert.Equal("test", command[^1]);
        Assert.DoesNotContain("-ntp", BuildCommands.MavenCommonFlags("3.5.4", windows: false));
        Assert.Contains("-Dsurefire.useManifestOnlyJar=false", BuildCommands.MavenCommonFlags("3.9.6", windows: true));
        Assert.EndsWith("3.1.2:list", BuildCommands.MavenCompileWithDependencies("3.5.4")[2], StringComparison.Ordinal);
        Assert.EndsWith("3.8.1:list", BuildCommands.MavenCompileWithDependencies("3.9.6")[2], StringComparison.Ordinal);

        var standalone = BuildCommands.Command(context with { Plan = plan with { Mode = BuildMode.Standalone, Module = null } }, ["-v"]);
        Assert.DoesNotContain("-pl", standalone);

        // Wrapper tính từ gốc repository của từng bản sao.
        var wrapper = context with { RepoRoot = Path.Combine(Path.GetTempPath(), "copy1"), Executable = "sub/mvnw", ExecutableInRepo = true };
        Assert.Equal(Path.Combine(Path.GetTempPath(), "copy1", "sub", "mvnw"), BuildCommands.Command(wrapper, ["-v"])[0]);

        var gradle = BuildCommands.Command(new BuildContext { Plan = new BuildPlan { Tool = "gradle", Module = ":core" }, Executable = "gradlew", CommonFlags = BuildCommands.GradleCommonFlags, InitScript = "/t/arrow-init.gradle" }, BuildCommands.GradleModuleTest(":core"));
        Assert.Equal(["gradlew", "--no-daemon", "--console=plain", "--init-script", "/t/arrow-init.gradle", ":core:test", "--continue"], gradle);
    }

    [Fact]
    public void ReadsSurefireXml()
    {
        var xml = XDocument.Parse("""
            <testsuite name="a.UtilTest" tests="4">
              <testcase name="ok" classname="a.UtilTest" time="0.01"/>
              <testcase name="fails" classname="a.UtilTest" time="1,234.5"><failure message="expected:&lt;1&gt;" type="java.lang.AssertionError"/></testcase>
              <testcase name="throws" classname="a.UtilTest$Inner"><error message="boom" type="java.lang.IllegalStateException"/></testcase>
              <testcase name="skip"><skipped/></testcase>
            </testsuite>
            """);
        var tests = TestReportReader.Parse(xml);
        Assert.Equal([TestCaseStatus.Passed, TestCaseStatus.Failed, TestCaseStatus.Error, TestCaseStatus.Skipped], tests.Select(t => t.Status));
        Assert.Equal("a.UtilTest$Inner#throws", tests[2].Id);
        Assert.Equal("a.UtilTest", tests[3].ClassName);
        Assert.Equal("java.lang.AssertionError", tests[1].ExceptionType);

        // Surefire 3.5 + JUnit 5: classname là tên method (mẫu pilot 9577793, 93861612).
        var junit5 = TestReportReader.Parse(XDocument.Parse("""
            <testsuite name="amforeas.client.RequestParamsTest">
              <testcase name="testInsert" classname="testInsert()"/>
              <testcase name="checkMode" classname="checkMode"/>
              <testcase name="nested" classname="amforeas.client.RequestParamsTest$Inner"/>
              <testcase name="shows" classname="Request params work"/>
            </testsuite>
            """));
        Assert.Equal(["amforeas.client.RequestParamsTest", "amforeas.client.RequestParamsTest", "amforeas.client.RequestParamsTest$Inner", "amforeas.client.RequestParamsTest"], junit5.Select(t => t.ClassName));
        Assert.Equal("DefaultTest", TestReportReader.ClassNameOf("DefaultTest", "DefaultTest"));

        // TestNG ghi bản trùng vào junitreports/: chỉ đọc file ngay trong surefire-reports/ hoặc test-results/<task>/.
        var sep = Path.DirectorySeparatorChar;
        Assert.True(TestReportReader.IsReport($"{sep}r{sep}m{sep}target{sep}surefire-reports{sep}TEST-a.ATest.xml"));
        Assert.False(TestReportReader.IsReport($"{sep}r{sep}m{sep}target{sep}surefire-reports{sep}junitreports{sep}TEST-a.ATest.xml"));
        Assert.True(TestReportReader.IsReport($"{sep}r{sep}core{sep}build{sep}test-results{sep}test{sep}TEST-a.ATest.xml"));
        Assert.False(TestReportReader.IsReport($"{sep}r{sep}src{sep}test{sep}resources{sep}TEST-a.ATest.xml"));
    }

    [Fact]
    public void GradleOwnerIsDeepestProjectContainingCut()
    {
        var info = new GradleInfo { Projects = [(":", "/ws/b"), (":dbfit-java", "/ws/b/dbfit-java"), (":dbfit-java:core", "/ws/b/dbfit-java/core"), (":api", "/ws/b/api")] };
        Assert.Equal(":dbfit-java:core", RepositoryAnalyzer.GradleOwner(info, "/ws/b", "dbfit-java/core/src/main/java"));
        Assert.Equal(":api", RepositoryAnalyzer.GradleOwner(info, "/ws/b", "api/src/main/java"));
        Assert.Equal(":", RepositoryAnalyzer.GradleOwner(info, "/ws/b", "src/main/java"));
        Assert.Equal(":", RepositoryAnalyzer.GradleOwner(info, "/ws/b", "apix/src/main/java"));
    }

    [Fact]
    public void RemovesOnlyCorruptJarsInsideBuildCaches()
    {
        var home = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        var inCache = Path.Combine(home, ".m2", "repository", "arrow-test-" + Guid.NewGuid().ToString("N")[..8], "x-1.0.jar");
        var outside = Path.Combine(Path.GetTempPath(), "arrow-test-" + Guid.NewGuid().ToString("N")[..8] + ".jar");
        Directory.CreateDirectory(Path.GetDirectoryName(inCache)!);
        File.WriteAllText(inCache, "<!doctype html><html></html>");
        File.WriteAllText(outside, "<!doctype html><html></html>");
        try
        {
            var log = $"[ERROR] error reading {inCache}; zip END header not found\n[ERROR] error reading {outside}; error in opening zip file";
            Assert.Equal(1, BuildFailureClassifier.RemoveCorruptCachedJars(log));
            Assert.False(File.Exists(inCache));
            Assert.True(File.Exists(outside));
            Assert.Equal(0, BuildFailureClassifier.RemoveCorruptCachedJars("BUILD SUCCESS"));
        }
        finally
        {
            Directory.Delete(Path.GetDirectoryName(inCache)!, recursive: true);
            File.Delete(outside);
        }
    }
}
