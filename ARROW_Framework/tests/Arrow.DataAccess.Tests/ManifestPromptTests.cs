using Arrow.DataAccess.Common;
using Arrow.DataAccess.Files;
using Arrow.Models.Common;
using Arrow.Models.Config;

namespace Arrow.DataAccess.Tests;

public class ManifestPromptTests
{
    [Fact]
    public void ReadsRealPilotManifestFields()
    {
        var manifest = Path.Combine(RepoFiles.ArrowRoot, "evaluation_manifest", "pilot", "classes2test_pilot_50_seed42_v2.jsonl");
        var tasks = new FileManifestRepository().ReadTasks(manifest, "classes2test");
        Assert.Equal(50, tasks.Count);
        Assert.All(tasks, t =>
        {
            Assert.Equal("classes2test", t.Dataset);
            Assert.Equal("CONTENT_MATCHED", t.RevisionVerificationStatus);
            Assert.Equal("git_history", t.RevisionProvenance);
            Assert.Equal(40, t.CheckoutSha.Length);
            Assert.StartsWith("openjdk version", t.JavaVersion, StringComparison.Ordinal);
            Assert.NotEmpty(t.BuildAttempts);
            Assert.NotEmpty(t.RawJson);
        });

        // Mẫu có build root là thư mục con (spec 01 Mục 10.1).
        var libsodium = tasks.Single(t => t.TaskId == "118477931_429246acc7725217");
        Assert.Equal(".", libsodium.ModulePath);
        var testCompile = libsodium.BuildAttempts.Single(a => a.Stage == "test_compile");
        Assert.Equal(0, testCompile.ExitCode);
        Assert.Equal("3rdParty/libsodium-jni-2.0.1", Path.GetRelativePath(libsodium.WorkingDirectory, testCompile.WorkingDirectory).Replace('\\', '/'));
    }

    [Fact]
    public void ReadsEvaluationManifestFallbackAttempts()
    {
        var manifest = Path.Combine(RepoFiles.ArrowRoot, "evaluation_manifest", "classes2test_200_seed42.jsonl");
        var task = new FileManifestRepository().ReadTasks(manifest, "classes2test").Single(t => t.TaskId == "103728471_1928343239506019");
        var fallback = task.BuildAttempts.Single(a => a.Stage == "test_compile_fallback");
        Assert.Equal(0, fallback.ExitCode);
        Assert.Equal(1, task.BuildAttempts.Single(a => a.Stage == "test_compile").ExitCode);
        Assert.Equal(["mvn", "-q", "-DskipTests", "test-compile"], fallback.Command);
    }

    [Fact]
    public void LoadsAllRealPromptTemplates()
    {
        var repo = new FilePromptRepository(new FrameworkPaths(RepoFiles.FrameworkRoot));
        var templates = repo.LoadAll();
        Assert.Equal(10, templates.Count);
        Assert.DoesNotContain(templates, t => t.RelativePath.EndsWith("README.md", StringComparison.Ordinal));
        Assert.All(templates, t =>
        {
            Assert.NotEmpty(t.Id);
            Assert.NotEmpty(t.Version);
            Assert.DoesNotContain("---\nid:", t.Body);
            Assert.Equal(64, t.Sha256.Length);
        });

        var fewShot = templates.Single(t => t.RelativePath == "generation/system/few_shot.md");
        Assert.Equal("system", fewShot.Role);
        Assert.Contains("166116543_f6b6c10d343d5bb1", fewShot.Extra["fewshot_examples"]);
        var level3 = templates.Single(t => t.RelativePath == "repair/user/level3.md");
        Assert.Contains("extra_context", level3.Placeholders);
        Assert.Contains("{{extra_context}}", level3.Body);
        Assert.Contains("\"166116543_f6b6c10d343d5bb1\"", repo.ReadData("generation/fewshot_provenance.json"));
    }

    [Fact]
    public void TemplateShaIgnoresLineEndingsAndFrontMatterIsRequired()
    {
        const string lf = "---\nid: x\nversion: v1\nrole: user\nplaceholders: a, b\n---\nHello {{a}} {{b}}\n";
        var a = PromptTemplateParser.Parse("x.md", lf);
        var b = PromptTemplateParser.Parse("x.md", lf.Replace("\n", "\r\n"));
        Assert.Equal(a.Sha256, b.Sha256);
        Assert.Equal("Hello {{a}} {{b}}", a.Body);
        Assert.Equal(["a", "b"], a.Placeholders);

        Assert.Equal(ErrorCodes.PromptInvalid, Assert.Throws<ArrowException>(() => PromptTemplateParser.Parse("y.md", "no front matter")).Code);
        Assert.Equal(ErrorCodes.PromptInvalid, Assert.Throws<ArrowException>(() => PromptTemplateParser.Parse("z.md", "---\nid: x\n---\nbody")).Code);
    }
}
