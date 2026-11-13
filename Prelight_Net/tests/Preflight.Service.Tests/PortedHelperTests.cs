using System.Text;
using Preflight.Models;
using Preflight.Service.Core.Build;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Jdk;
using Preflight.Service.Core.Policy;
using Preflight.Service.Core.Probe;
using Preflight.Service.Core.PyCompat;
using Preflight.Service.Datasets.Classes2Test;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class PortedHelperTests
{
    [Theory]
    [InlineData("", "/etc/passwd")]
    [InlineData("", "../secret.java")]
    [InlineData("", "src/../secret.java")]
    [InlineData("", "C:/secret.java")]
    [InlineData("", "C:\\secret.java")]
    [InlineData("", "C:secret.java")]
    [InlineData("", "\\\\server\\share\\secret.java")]
    [InlineData("", "\\\\?\\C:\\secret.java")]
    [InlineData("", "src/\0.java")]
    public void UnsafePathsAreRejected(string _, string path)
    {
        using var work = new TestWorkspace();
        Assert.True(Classes2TestRecordReader.UnsafeSourcePath(path)); Assert.Empty(ResolvePath.PathCandidates(work.Path, path));
    }
    [Fact]
    public void EmptyAndNormalizedRelativePathsMatchPython()
    {
        using var work = new TestWorkspace();
        Assert.Empty(ResolvePath.PathCandidates(work.Path, ""));
        string inside = work.File("src/main/java/X.java", "class X {}");
        Assert.Equal(inside, ResolvePath.Resolve(work.Path, "src\\main\\java\\X.java"));
        Assert.Equal(inside, ResolvePath.Resolve(work.Path, "repo/src/main/java/X.java"));
        Assert.Null(ResolvePath.Resolve(work.Path, "missing.java"));
    }
    [Fact]
    public void ResolvePathContainsJunctionEscapes()
    {
        if (!OperatingSystem.IsWindows()) return;
        using var work = new TestWorkspace();
        string outside = work.File("outside/secret.java", "outside"), workspace = Path.Combine(work.Path, "workspace");
        Directory.CreateDirectory(workspace);
        string junction = Path.Combine(workspace, "linked");
        var start = new System.Diagnostics.ProcessStartInfo("cmd.exe") { UseShellExecute = false, CreateNoWindow = true, RedirectStandardOutput = true, RedirectStandardError = true };
        foreach (string arg in new[] { "/d", "/c", "mklink", "/J", junction, Path.GetDirectoryName(outside)! }) start.ArgumentList.Add(arg);
        using var process = System.Diagnostics.Process.Start(start)!; process.WaitForExit();
        Assert.Equal(0, process.ExitCode);
        try { Assert.Empty(ResolvePath.PathCandidates(workspace, "linked/secret.java")); }
        finally { Directory.Delete(junction); }
        Assert.True(File.Exists(outside));
    }
    [Fact]
    public void TaskIdIsStableAndSensitiveToIdentity()
    {
        string value = TaskIdFactory.Create("1", "url", "a/X.java", "X");
        Assert.Equal(value, TaskIdFactory.Create("1", "url", "a/X.java", "X"));
        Assert.NotEqual(value, TaskIdFactory.Create("1", "url", "b/X.java", "X"));
    }
    [Fact]
    public void NormalizerPreservesEscapesAndCommentMarkersInsideStrings()
    {
        Assert.Equal("Stringurl=\"https://example.test/a/*x*/\";intx=1;", JavaNormalizer.Normalize("String url = \"https://example.test/a/*x*/\"; // remove\nint x = 1; /* remove */"));
        Assert.Equal("Strings=\"\\\"//\";", JavaNormalizer.Normalize("String s = \"\\\"//\"; /* unfinished"));
        Assert.Equal("\"ab\"", JavaNormalizer.Normalize("\"a b\""));
    }
    [Theory]
    [InlineData("sourceCompatibility = 1.8", "8")]
    [InlineData("sourceCompatibility = '1.8'", "8")]
    [InlineData("sourceCompatibility = JavaVersion.VERSION_1_8", "8")]
    [InlineData("targetCompatibility = JavaVersion.VERSION_1_8", "8")]
    [InlineData("sourceCompatibility = JavaVersion.VERSION_17", "17")]
    [InlineData("sourceCompatibility = JavaVersion.VERSION_21", "21")]
    [InlineData("val version = JavaVersion.VERSION_1_8", "8")]
    public void GradleTargetNormalizesVersions(string content, string expected)
    {
        using var work = new TestWorkspace(); work.File("build.gradle", content);
        Assert.Equal(expected, JavaTargetReader.Read(work.Path, "gradle"));
    }
    [Fact]
    public void ReleaseV2OnlyChangesJava8AndScansAtMostThreeParents()
    {
        using var work = new TestWorkspace(); work.File("pom.xml", "<release>8</release>");
        string module = Path.Combine(work.Path, "a/b/c"); Directory.CreateDirectory(module);
        work.File("a/b/c/pom.xml", "<java.version>8</java.version>");
        Assert.Equal("8", JavaTargetReader.Read(module, "maven"));
        Assert.Equal("11", JavaTargetReader.Read(module, "maven", ProbeProfile.v2));
        work.File("a/b/c/d/pom.xml", "<java.version>8</java.version>");
        Assert.Equal("8", JavaTargetReader.Read(Path.Combine(module, "d"), "maven", ProbeProfile.v2));
    }
    [Fact]
    public void ProbeSourceCoversConstructorStaticAndInstance()
    {
        var java = new JavaClass { Name = "X", Package = "x" };
        Assert.Contains("new X();", ProbeSourceBuilder.Build(java, new("constructor", "X", "()", false, ""), "Probe", "junit4"));
        Assert.Contains("value.run(0);", ProbeSourceBuilder.Build(java, new("method", "run", "(int n)", false, ""), "Probe", "junit5"));
        Assert.Contains("X.all();", ProbeSourceBuilder.Build(java, new("method", "all", "()", true, ""), "Probe", "testng"));
        Assert.Contains("throws Throwable", ProbeSourceBuilder.Build(java, new("method", "all", "()", true, ""), "Probe", "testng", ProbeProfile.v2));
    }
    [Fact]
    public void ProbeArgumentsCoverAllPrimitiveTypes()
    {
        Assert.Equal("false, (byte) 0, (short) 0, 0, 0L, 0F, 0D, '\\0', null, null, null", ProbeArguments.PrimitiveArguments("(boolean b, byte by, short sh, int i, long l, float f, double d, char c, @NotNull final String s, int[] arr, boolean... varargs)"));
        Assert.Equal("", ProbeArguments.PrimitiveArguments("()"));
    }
    [Fact]
    public void FailureClassifierHandlesMissingLogAndPrecedence()
    {
        using var work = new TestWorkspace();
        var attempt = new BuildAttempt("", "", [], 1, false, 0, Path.Combine(work.Path, "missing.log"));
        Assert.Equal(PreflightStatus.MAIN_BUILD_FAILED, FailureClassifier.Classify(attempt, "main_compile"));
        Assert.Equal(PreflightStatus.BUILD_TOOL_UNSUPPORTED, FailureClassifier.Classify("no such file or directory could not resolve", false, "main_compile"));
        Assert.Equal(PreflightStatus.BUILD_TIMEOUT, FailureClassifier.Classify("no such file or directory", true, "test_compile"));
    }
    [Fact]
    public void BomAndRawByteHashArePreserved()
    {
        using var work = new TestWorkspace(); string path = work.File("42/a.json", "");
        byte[] bytes = Encoding.UTF8.GetPreamble().Concat(Encoding.UTF8.GetBytes("{\"repository\":{\"url\":\"url\"},\"focal_class\":{\"file\":\"src/main/java/X.java\",\"identifier\":\"X\"}}" )).ToArray();
        File.WriteAllBytes(path, bytes); var record = Classes2TestRecordReader.Read(path);
        Assert.Null(record.ReasonCode); Assert.Equal("42", record.Identity![0]);
        Assert.Equal(Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(bytes)).ToLowerInvariant(), record.ContentSha256);
    }
    [Fact]
    public void PythonWhitespaceAndStringOperationsKeepQuirks()
    {
        Assert.True(PyIsSpace.IsSpace('\x1c')); Assert.False(char.IsWhiteSpace('\x1c'));
        Assert.Equal("abc", PyStr.RStrip("abc.*.*", ".*"));
        Assert.Equal(new[] { "a a", "a" }, PyStr.RSplit("a a a", " ", 1));
        Assert.Equal("text", PyStr.Strip("\x1c\u00a0text\x1f"));
    }
}
