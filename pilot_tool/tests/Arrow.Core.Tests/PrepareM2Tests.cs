using Arrow.Core.Config;
using Arrow.Core.Prepare;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Prepare;

namespace Arrow.Core.Tests;

public class BuildPlanDetectorTests
{
    private static Func<string, bool> Files(params string[] files) => path => files.Contains(path);

    [Fact]
    public void MavenSingleModuleInSubdirectoryRunsThere()
    {
        // aion: POM duy nhất nằm trong 3rdParty/libsodium-jni-2.0.1 → module_path "." nhưng chạy tại thư mục đó.
        var plan = BuildPlanDetector.Detect("3rdParty/lib/src/main/java/org/x/Util.java", Files("3rdParty/lib/pom.xml"), windows: false)!;
        Assert.Equal(BuildToolKind.Maven, plan.Tool);
        Assert.Equal("3rdParty/lib", plan.ModuleDir);
        Assert.Equal(".", plan.ModulePath);
        Assert.Equal(BuildMode.Root, plan.Mode);
        Assert.Equal("3rdParty/lib", plan.WorkingDir);
        Assert.Null(plan.Wrapper);
    }

    [Fact]
    public void MavenReactorRootedBelowRepositoryRoot()
    {
        // jqf: reactor ở jqf/, module fuzz → -pl fuzz -am chạy tại jqf/.
        var plan = BuildPlanDetector.Detect("jqf/fuzz/src/main/java/a/B.java", Files("jqf/pom.xml", "jqf/fuzz/pom.xml"), windows: false)!;
        Assert.Equal("jqf/fuzz", plan.ModuleDir);
        Assert.Equal("jqf", plan.InvocationRoot);
        Assert.Equal("fuzz", plan.ModulePath);
        Assert.Equal(BuildMode.Reactor, plan.Mode);
        Assert.Equal("jqf", plan.WorkingDir);
    }

    [Fact]
    public void MavenWrapperNeedsWrapperFiles()
    {
        var with = BuildPlanDetector.Detect("src/main/java/A.java", Files("pom.xml", "mvnw", ".mvn/wrapper/maven-wrapper.properties"), windows: false)!;
        Assert.Equal("mvnw", with.Wrapper);
        var without = BuildPlanDetector.Detect("src/main/java/A.java", Files("pom.xml", "mvnw"), windows: false)!;
        Assert.Null(without.Wrapper);
        var windows = BuildPlanDetector.Detect("src/main/java/A.java", Files("pom.xml", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.jar"), windows: true)!;
        Assert.Equal("mvnw.cmd", windows.Wrapper);
    }

    [Fact]
    public void GradleNestedProjectPath()
    {
        var plan = BuildPlanDetector.Detect("agent/core/src/main/java/a/B.java",
            Files("settings.gradle", "build.gradle", "agent/core/build.gradle.kts", "gradlew", "gradle/wrapper/gradle-wrapper.jar"), windows: false)!;
        Assert.Equal(BuildToolKind.Gradle, plan.Tool);
        Assert.Equal("agent/core", plan.ModulePath);
        Assert.Equal(":agent:core", plan.GradleProjectPath);
        Assert.Equal(".", plan.WorkingDir);
        Assert.Equal("gradlew", plan.Wrapper);
    }

    [Fact]
    public void GradleWrapperWithoutJarFallsBackToMachineGradle()
    {
        var plan = BuildPlanDetector.Detect("src/main/java/A.java", Files("build.gradle", "gradlew"), windows: false)!;
        Assert.Null(plan.Wrapper);
        Assert.Equal(BuildMode.Root, plan.Mode);
    }

    [Fact]
    public void NoBuildFileReturnsNull() =>
        Assert.Null(BuildPlanDetector.Detect("src/main/java/A.java", Files(), windows: false));
}

public class BuildCommandsTests
{
    private static readonly BuildFlags Flags = new()
    {
        Maven = ["-B", "-ntp"],
        Gradle = ["--no-daemon", "--console=plain"],
        GradleExcludedTasks = ["checkstyleMain", "spotlessCheck"],
    };

    [Fact]
    public void BuildEnvironmentFixesLocaleAndTimezone()
    {
        // jitterted-ensembler: JVM theo locale tiếng Việt của máy in "9:00SA" thay vì "9:00AM" → test sẵn có fail.
        var environment = Arrow.Infrastructure.Build.BuildRunner.BuildEnvironment("/jdk");
        Assert.Equal("-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=UTC", environment["JAVA_TOOL_OPTIONS"]);
    }

    [Fact]
    public void MavenReactorUsesPlAm()
    {
        var plan = new BuildPlan { Tool = BuildToolKind.Maven, ModuleDir = "jqf/fuzz", InvocationRoot = "jqf", ModulePath = "fuzz", Mode = BuildMode.Reactor };
        var command = BuildCommands.For(plan, BuildStep.TestCompile, Flags);
        Assert.Equal("jqf", command.WorkingDir);
        Assert.Equal(["-f", "pom.xml", "-pl", "fuzz", "-am", "-B", "-ntp", "-DskipTests", "test-compile"], command.Arguments);

        var standalone = BuildCommands.For(plan.AsStandalone(), BuildStep.MainCompile, Flags);
        Assert.Equal("jqf/fuzz", standalone.WorkingDir);
        Assert.Equal(["-f", "pom.xml", "-B", "-ntp", "-DskipTests", "compile"], standalone.Arguments);
    }

    [Fact]
    public void MavenTargetTestUsesSimpleNameAndWindowsFlag()
    {
        var plan = new BuildPlan { Tool = BuildToolKind.Maven, Mode = BuildMode.Root };
        var command = BuildCommands.For(plan, BuildStep.TargetTest, Flags with { Windows = true }, "a.b.Foo_ArrowTest");
        Assert.Contains("-Dtest=Foo_ArrowTest", command.Arguments);
        Assert.Contains("-Dsurefire.useManifestOnlyJar=false", command.Arguments);
        Assert.Equal("test", command.Arguments[^1]);
    }

    [Fact]
    public void OldMavenWrapperGetsLegacyTransferFlag()
    {
        var plan = new BuildPlan { Tool = BuildToolKind.Maven, Mode = BuildMode.Root };
        Assert.Contains(BuildCommands.LegacyNoTransferProgress, BuildCommands.For(plan, BuildStep.MainCompile, Flags with { MavenVersion = "3.6.0" }).Arguments);
        Assert.Contains("-ntp", BuildCommands.For(plan, BuildStep.MainCompile, Flags with { MavenVersion = "3.6.1" }).Arguments);
        Assert.Contains("-ntp", BuildCommands.For(plan, BuildStep.MainCompile, Flags with { MavenVersion = "3.9.16" }).Arguments);
        Assert.Contains("-ntp", BuildCommands.For(plan, BuildStep.MainCompile, Flags).Arguments);
        Assert.Contains(BuildCommands.MavenDependencyPluginLegacy + ":list", BuildCommands.For(plan, BuildStep.Dependencies, Flags with { MavenVersion = "3.6.0" }).Arguments);
        Assert.Contains(BuildCommands.MavenDependencyPlugin + ":list", BuildCommands.For(plan, BuildStep.Dependencies, Flags with { MavenVersion = "3.6.3" }).Arguments);
    }

    [Fact]
    public void GradleExcludesOnlyExistingTasks()
    {
        var plan = new BuildPlan { Tool = BuildToolKind.Gradle, ModuleDir = "core", ModulePath = "core", Mode = BuildMode.Reactor };
        var command = BuildCommands.For(plan, BuildStep.ModuleTest, Flags, gradleTasks: new HashSet<string> { "checkstyleMain", "test" });
        Assert.Equal(["--no-daemon", "--console=plain", "-x", ":core:checkstyleMain", ":core:test", "--continue"], command.Arguments);
        Assert.Equal(".", command.WorkingDir);

        var root = BuildCommands.For(plan with { ModulePath = ".", ModuleDir = ".", Mode = BuildMode.Root }, BuildStep.TargetTest, Flags, "a.B_ArrowTest", new HashSet<string>());
        Assert.Equal(["--no-daemon", "--console=plain", "test", "--tests", "a.B_ArrowTest"], root.Arguments);
    }

    [Fact]
    public void ParsesGradleTaskList()
    {
        const string output = """
            Build tasks
            -----------
            classes - Assembles main classes.
            testClasses - Assembles test classes.

            Verification tasks
            ------------------
            checkstyleMain - Run Checkstyle analysis for main classes
            core:spotlessCheck - Checks formatting
            test
            BUILD SUCCESSFUL in 2s
            """;
        var tasks = BuildCommands.ParseGradleTasks(output);
        Assert.Contains("classes", tasks);
        Assert.Contains("checkstyleMain", tasks);
        Assert.Contains("spotlessCheck", tasks);
        Assert.Contains("test", tasks);
        Assert.DoesNotContain("Build", tasks);
    }

    [Fact]
    public void DetectsModuleOutsideBuild()
    {
        Assert.True(BuildCommands.IsModuleNotInBuild(BuildToolKind.Maven, "[ERROR] Could not find the selected project in the reactor: exercise4"));
        Assert.True(BuildCommands.IsModuleNotInBuild(BuildToolKind.Gradle, "Project 'kiqt-core' not found in root project 'kiqt'."));
        Assert.False(BuildCommands.IsModuleNotInBuild(BuildToolKind.Maven, "[ERROR] COMPILATION ERROR"));
    }

    [Fact]
    public void ParsesToolVersions()
    {
        Assert.Equal("3.9.16", PrepareService.ParseToolVersion(BuildToolKind.Maven, "Apache Maven 3.9.16 (abc)\nMaven home: /x"));
        Assert.Equal("8.5", PrepareService.ParseToolVersion(BuildToolKind.Gradle, "\n------\nGradle 8.5\n------"));
        Assert.Equal("unknown", PrepareService.ParseToolVersion(BuildToolKind.Gradle, "Downloading https://services.gradle.org"));
    }
}

public class JdkSelectorTests
{
    private static readonly Dictionary<string, string> Homes = new() { ["8"] = "/jdk8", ["11"] = "/jdk11", ["17"] = "/jdk17" };

    private const string Jdk17 = "openjdk version \"17.0.20.1\" 2026-08-18 LTS";

    [Fact]
    public void NoDeclarationUsesManifestMajor()
    {
        Assert.Equal(new JdkChoice(17, "/jdk17", "manifest"), JdkSelector.Select(Jdk17, BuildToolKind.Maven, ["<project/>"], Homes));
    }

    [Fact]
    public void DeclaredLevelWinsOverManifest()
    {
        // NetCom2: rmi/pom.xml khai <target>1.8</target> trong maven-compiler-plugin, preflight gán JDK 17.
        Assert.Equal(new JdkChoice(8, "/jdk8", "declared_level"),
            JdkSelector.Select(Jdk17, BuildToolKind.Maven, ["<plugin><configuration><source>1.8</source><target>1.8</target></configuration></plugin>"], Homes));
        // neo-starters: POM cha khai java.version 1.8, module không khai.
        Assert.Equal(8, JdkSelector.Select(Jdk17, BuildToolKind.Maven, ["<project/>", "<java.version>1.8</java.version>"], Homes).Major);
        Assert.Equal(8, JdkSelector.Select(Jdk17, BuildToolKind.Maven, ["<target>1.7</target>"], Homes).Major);
        Assert.Equal(11, JdkSelector.Select(Jdk17, BuildToolKind.Gradle, ["sourceCompatibility = '11'"], Homes).Major);
        Assert.Equal(17, JdkSelector.Select("openjdk version \"1.8.0_504\"", BuildToolKind.Maven, ["<maven.compiler.release>14</maven.compiler.release>"], Homes).Major);
    }

    [Fact]
    public void Java8WithReleaseFlagUsesJdk11()
    {
        var choice = JdkSelector.Select(Jdk17, BuildToolKind.Maven, ["<maven.compiler.release>8</maven.compiler.release>"], Homes);
        Assert.Equal(11, choice.Major);
        Assert.Equal("declared_level+release_flag", choice.Reason);
    }

    [Fact]
    public void ManifestFallbackAndMissingJdk()
    {
        Assert.Equal(new JdkChoice(17, "/jdk17", "manifest_fallback"), JdkSelector.FromManifest(Jdk17, [], Homes));
        var ex = Assert.Throws<PrepareException>(() => JdkSelector.Select("openjdk version \"21.0.12.1\"", BuildToolKind.Maven, [], Homes));
        Assert.Equal("JDK_MISSING", ex.Reason);
    }
}

public class BaselineTests
{
    private static TestCaseResult T(string id, TestOutcome outcome) => new() { Id = id, Outcome = outcome };

    [Fact]
    public void ClassifiesFailingAndFlaky()
    {
        var run1 = new[] { T("A#ok", TestOutcome.Pass), T("A#broken", TestOutcome.Fail), T("A#flip", TestOutcome.Pass), T("A#gone", TestOutcome.Pass), T("A#skip", TestOutcome.Skip) };
        var run2 = new[] { T("A#ok", TestOutcome.Pass), T("A#broken", TestOutcome.Error), T("A#flip", TestOutcome.Fail), T("A#skip", TestOutcome.Skip) };
        var baseline = Baseline.Compute([run1, run2], [61.234, 58.9], "surefire-xml");
        Assert.Equal(5, baseline.TestsTotal);
        Assert.Equal(["A#broken"], baseline.FailingInAllRuns);
        Assert.Equal(["A#flip", "A#gone"], baseline.Flaky);
        Assert.Equal(["A#ok"], baseline.PassingInAllRuns);
        Assert.Equal([61.23, 58.9], baseline.DurationsSeconds);
    }

    [Fact]
    public void EmptyModuleHasNoTests()
    {
        var baseline = Baseline.Compute([Array.Empty<TestCaseResult>(), Array.Empty<TestCaseResult>()], [1, 1], "none");
        Assert.Equal(0, baseline.TestsTotal);
    }

    [Fact]
    public void ClassNameFallsBackToSuiteName()
    {
        // amforeas (Surefire 3.0.2, JUnit 5): classname="testRead()"; cucable (JUnit 4): classname là tên method.
        Assert.Equal("amforeas.client.RequestParamsTest", TestReportParser.ClassNameOf("testRead()", "amforeas.client.RequestParamsTest"));
        Assert.Equal("com.trivago.PropertyManagerTest", TestReportParser.ClassNameOf("checkFoo", "com.trivago.PropertyManagerTest"));
        Assert.Equal("a.FooTest$Inner", TestReportParser.ClassNameOf("a.FooTest$Inner", "a.FooTest$Inner"));
        Assert.Equal("FooTest", TestReportParser.ClassNameOf("FooTest", "FooTest"));
        Assert.Equal("a.FooTest", TestReportParser.ClassNameOf("FooTest", "a.FooTest"));
        var results = TestReportParser.Parse("""<testsuite name="a.B"><testcase name="m" classname="m()"/></testsuite>""");
        Assert.Equal("a.B#m", results[0].Id);
    }

    [Fact]
    public void ParsesSurefireXml()
    {
        const string xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <testsuite name="a.FooTest" tests="3">
              <testcase name="ok" classname="a.FooTest" time="0.012"/>
              <testcase name="bad" classname="a.FooTest" time="0.5">
                <failure message="expected:&lt;1&gt; but was:&lt;2&gt;" type="java.lang.AssertionError">java.lang.AssertionError: expected
            at a.FooTest.bad(FooTest.java:10)</failure>
              </testcase>
              <testcase name="skipped" classname="a.FooTest"><skipped/></testcase>
            </testsuite>
            """;
        var results = TestReportParser.Parse(xml);
        Assert.Equal(3, results.Count);
        Assert.Equal(TestOutcome.Fail, results[1].Outcome);
        Assert.Equal("a.FooTest#bad", results[1].Id);
        Assert.Equal("java.lang.AssertionError", results[1].Type);
        Assert.Equal(TestOutcome.Skip, results[2].Outcome);
        Assert.Empty(TestReportParser.Parse("not xml"));
    }
}

public class RepoContextTests
{
    [Fact]
    public void ParsesMavenDependencyList()
    {
        const string text = """

            The following files have been resolved:
               junit:junit:jar:4.13.2:test -- module junit
               org.mockito:mockito-core:jar:4.11.0:test
               com.google.guava:guava:jar:32.1.2-jre:compile
               io.netty:netty-tcnative:jar:linux-x86_64:2.0.61.Final:runtime
            """;
        var dependencies = DependencyParser.ParseMavenList(text);
        Assert.Contains(new Dependency("junit", "junit", "4.13.2", "test"), dependencies);
        Assert.Contains(new Dependency("io.netty", "netty-tcnative", "2.0.61.Final", "runtime"), dependencies);
        Assert.Equal(4, dependencies.Count);

        var framework = DependencyParser.Framework("junit4", dependencies);
        Assert.Equal("4.13.2", framework.Version);
        Assert.Equal(["org.mockito:mockito-core:4.11.0"], framework.Libraries);
    }

    [Fact]
    public void ParsesGradleTree()
    {
        const string text = """
            testRuntimeClasspath - Runtime classpath of source set 'test'.
            +--- project :core
            +--- org.slf4j:slf4j-api:1.7.36 -> 2.0.9
            \--- org.junit.jupiter:junit-jupiter:5.10.0
                 +--- org.junit.jupiter:junit-jupiter-api:5.10.0
                 |    \--- org.opentest4j:opentest4j:1.3.0
                 \--- org.junit:junit-bom:5.10.0 (*)
            """;
        var dependencies = DependencyParser.ParseGradleTree(text);
        Assert.Contains(dependencies, d => d is { Artifact: "slf4j-api", Version: "2.0.9" });
        Assert.Contains(dependencies, d => d is { Artifact: "opentest4j", Version: "1.3.0" });
        Assert.DoesNotContain(dependencies, d => d.Artifact == "core");
        Assert.Equal("5.10.0", DependencyParser.Framework("junit5", dependencies).Version);
    }

    [Fact]
    public void ParsesDeclaredDependenciesAndJavaLevels()
    {
        const string pom = """
            <properties><maven.compiler.source>1.8</maven.compiler.source><maven.compiler.target>1.8</maven.compiler.target></properties>
            <dependencies>
              <dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.12</version><scope>test</scope></dependency>
              <dependency><groupId>com.x</groupId><artifactId>y</artifactId></dependency>
            </dependencies>
            """;
        var declared = DependencyParser.ParseDeclared(BuildToolKind.Maven, pom);
        Assert.Contains(new Dependency("junit", "junit", "4.12", "test"), declared);
        Assert.Contains(new Dependency("com.x", "y", "", "compile"), declared);
        var levels = DependencyParser.JavaLevels(8, BuildToolKind.Maven, pom);
        Assert.Equal("8", levels.Source);
        Assert.Equal("8", levels.Target);

        Assert.Null(DependencyParser.JavaLevels(8, BuildToolKind.Maven, "<source>src/it/java</source>").Source);
        var inherited = DependencyParser.JavaLevels(17, BuildToolKind.Maven, ["<project><source>src/it/java</source></project>", "<maven.compiler.source>1.8</maven.compiler.source>"]);
        Assert.Equal("8", inherited.Source);

        var gradle = DependencyParser.JavaLevels(11, BuildToolKind.Gradle, "sourceCompatibility = JavaVersion.VERSION_1_8\ntargetCompatibility = '11'");
        Assert.Equal("8", gradle.Source);
        Assert.Equal("11", gradle.Target);
        Assert.Contains(new Dependency("org.junit.jupiter", "junit-jupiter", "5.9.0", "testImplementation"),
            DependencyParser.ParseDeclared(BuildToolKind.Gradle, "testImplementation 'org.junit.jupiter:junit-jupiter:5.9.0'"));
    }

    [Fact]
    public void TestPathMirrorsSourcePath()
    {
        Assert.Equal("jqf/fuzz/src/test/java/edu/x/Foo_ArrowTest.java",
            DependencyParser.TestPathFor("jqf/fuzz/src/main/java/edu/x/Foo.java", "jqf/fuzz", "_ArrowTest"));
        Assert.Equal("src/test/java/A_ArrowTest.java", DependencyParser.TestPathFor("src/main/java/A.java", ".", "_ArrowTest"));
    }

    [Fact]
    public void MirrorNameFromUrl()
    {
        Assert.Equal("traccar__traccar.git", GitRepositoryProvider.MirrorName("https://github.com/traccar/traccar"));
        Assert.Equal("AuthMe__ConfigMe.git", GitRepositoryProvider.MirrorName("https://github.com/AuthMe/ConfigMe.git/"));
    }
}

public class WorkDirPolicyTests
{
    [Fact]
    public void RejectsSpacesAndDiacritics()
    {
        Assert.False(Arrow.Core.Config.WorkDirPolicy.IsClean("/Users/dungng/FPT/Ki 9/SET490/DoAnARROW/ARROW/repo"));
        Assert.False(Arrow.Core.Config.WorkDirPolicy.IsClean(@"C:\Users\Hán\arrow-repo"));
        Assert.True(Arrow.Core.Config.WorkDirPolicy.IsClean("/Users/dungng/arrow-repo"));
        Assert.True(Arrow.Core.Config.WorkDirPolicy.IsClean(@"C:\arrow-repo"));
        Assert.Equal(@"C:\arrow-repo", Arrow.Core.Config.WorkDirPolicy.Default(windows: true, home: @"C:\Users\Hán"));
        Assert.Equal(Path.Combine("/home/a", "x"), Arrow.Core.Config.WorkDirPolicy.ExpandHome("~/x", "/home/a"));
    }
}

public class PathDiagnosticsTests
{
    private const string SpacedArrow = "/Users/dungng/FPT/Ki 9/SET490/DoAnARROW/ARROW";

    [Fact]
    public void SpaceInsideArrowSuggestsRenameAndAlternative()
    {
        var advice = PathDiagnostics.Diagnose(SpacedArrow + "/repo", SpacedArrow, windows: false, home: "/Users/dungng");
        Assert.True(advice.Blocking);
        var issue = Assert.Single(advice.Issues);
        Assert.Equal("Ki 9", issue.Segment);
        Assert.Contains("\"Ki 9\" thành \"Ki9\"", advice.Steps[0]);
        Assert.Contains("install-litellm.sh", advice.Steps[1]);
        Assert.Contains("/Users/dungng/arrow-repo", advice.Steps[^1]);
        Assert.Contains("Cách sửa:", advice.Describe());
    }

    [Fact]
    public void DiacriticsAndSpecialCharsOutsideArrow()
    {
        var advice = PathDiagnostics.Diagnose(@"C:\Users\Hán\repo#1", @"D:\ARROW", windows: true, home: @"C:\Users\Hán");
        Assert.True(advice.Blocking);
        Assert.Contains(advice.Issues, i => i.Segment == "Hán");
        Assert.Contains(advice.Issues, i => i.Segment == "repo#1" && i.Problem.Contains("'#'"));
        Assert.Contains(@"C:\arrow-repo", advice.Steps[0]);
        Assert.Equal("Han", PathDiagnostics.SuggestName("Hán"));
        Assert.Equal("Dung", PathDiagnostics.SuggestName("Dũng"));
    }

    [Fact]
    public void LongWindowsPathOnlyWarns()
    {
        var advice = PathDiagnostics.Diagnose(@"C:\work\projects\semester9\set490\arrow-capstone-final\ARROW\repo", @"C:\work", windows: true, home: @"C:\Users\a");
        Assert.False(advice.Blocking);
        Assert.False(advice.Clean);
        Assert.Contains("LongPathsEnabled", advice.Steps[^1]);
    }

    [Fact]
    public void CleanPathHasNoIssues() =>
        Assert.True(PathDiagnostics.Diagnose("/Users/dungng/FPT/Ki9/SET490/DoAnARROW/ARROW/repo", "/x", windows: false, home: "/h").Clean);
}

public class JdkCandidateTests
{
    private static readonly Dictionary<string, string> Homes = new() { ["8"] = "/jdk8", ["11"] = "/jdk11", ["17"] = "/jdk17", ["21"] = "/jdk21" };

    [Fact]
    public void DeclaredThenManifestThenRest()
    {
        // jsoup: khai Java 8 nhưng test cần JDK mới; manifest ghi 17.
        var candidates = JdkSelector.Candidates("openjdk version \"17.0.20.1\"", BuildToolKind.Maven, ["<maven.compiler.source>1.8</maven.compiler.source>"], Homes);
        Assert.Equal([8, 17, 11, 21], candidates.Select(c => c.Major));
        Assert.Equal(["declared_level", "manifest_fallback", "fallback", "fallback"], candidates.Select(c => c.Reason));
    }

    [Fact]
    public void NoDeclarationStartsWithManifest()
    {
        // nifi: không khai mức Java, surefire 2.20.1 không chạy được trên JDK 17 → thử 8 tiếp theo.
        var candidates = JdkSelector.Candidates("openjdk version \"17.0.20.1\"", BuildToolKind.Maven, ["<project/>"], Homes);
        Assert.Equal([17, 8, 11, 21], candidates.Select(c => c.Major));
        Assert.Equal("manifest", candidates[0].Reason);
    }
}
