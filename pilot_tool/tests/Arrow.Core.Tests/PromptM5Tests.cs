using Arrow.Core.Generation;
using Arrow.Core.Llm;
using Arrow.Core.Prepare;
using Arrow.Core.Prompts;
using Arrow.Core.Util;

namespace Arrow.Core.Tests;

public class TemplateEngineTests
{
    [Fact]
    public void RendersVariablesIfAndEach()
    {
        var values = new Dictionary<string, object?>
        {
            ["name"] = "Calc",
            ["flag"] = true,
            ["none"] = "",
            ["items"] = new List<IReadOnlyDictionary<string, object?>>
            {
                new Dictionary<string, object?> { ["round"] = 1, ["outcome"] = "COMPILE" },
                new Dictionary<string, object?> { ["round"] = 2, ["outcome"] = "duplicate code" },
            },
        };
        const string template = "Class {{name}}\n{{#if flag}}yes\n{{/if}}{{#if none}}no\n{{/if}}{{#each items}}\n- {{index}}/{{round}}: {{outcome}} ({{name}})\n{{/each}}\nend";
        Assert.Equal("Class Calc\nyes\n- 1/1: COMPILE (Calc)\n- 2/2: duplicate code (Calc)\nend", TemplateEngine.Render(template, values));
    }

    [Fact]
    public void MissingVariableIsAnError()
    {
        var ex = Assert.Throws<ArrowException>(() => TemplateEngine.Render("Hi {{who}}", new Dictionary<string, object?>()));
        Assert.Equal("PROMPT_VARIABLE_MISSING", ex.Code);
        Assert.Throws<ArrowException>(() => TemplateEngine.Render("{{#if x}}open", new Dictionary<string, object?> { ["x"] = true }));
    }

    [Fact]
    public void ParsesHeaderAndHashIsStable()
    {
        const string text = "---\nid: generation.zero_shot\nversion: v1\nrole: user\n---\nBody {{x}}\n";
        var a = TemplateEngine.Parse(text, "generation/zero_shot.md");
        var b = TemplateEngine.Parse(text.Replace("\n", "\r\n"), "generation/zero_shot.md");
        Assert.Equal("generation.zero_shot", a.Id);
        Assert.Equal("user", a.Role);
        Assert.Equal("Body {{x}}", a.Body);
        Assert.Equal(a.Sha256, b.Sha256);
    }

    [Fact]
    public void ShippedPromptsRenderForEveryStrategy()
    {
        var library = PromptLibrary.Load(Path.Combine(PilotToolDir(), "prompts"));
        Assert.Contains("generation/zero_shot.md", library.Hashes.Keys);
        Assert.Contains("fewshot/examples.json", library.Hashes.Keys);
        var input = new GenerationInput
        {
            TestClassName = "Calc_ArrowTest",
            TestPackage = "a",
            CutPath = "src/main/java/a/Calc.java",
            CutSource = "package a;\nimport org.apache.commons.lang3.StringUtils;\npublic class Calc {}",
            TestFilePath = "src/test/java/a/Calc_ArrowTest.java",
            RepoContext = new RepoContext
            {
                BuildTool = new BuildToolInfo { Name = "maven", Wrapper = true, Version = "3.9.6" },
                JavaVersion = new JavaVersionInfo { JdkMajor = 17, Release = "17" },
                TestFramework = new TestFrameworkInfo { Name = "junit5", Version = "5.10.2", Libraries = ["org.mockito:mockito-core:5.11.0"] },
                Dependencies =
                [
                    new Dependency("org.junit.jupiter", "junit-jupiter-api", "5.10.2", "test"),
                    new Dependency("org.apache.commons", "commons-lang3", "3.14.0", "compile"),
                    new Dependency("com.unrelated", "lib", "1.0", "compile"),
                ],
                SourcePath = "src/main/java/a/Calc.java",
                TestPath = "src/test/java/a/Calc_ArrowTest.java",
            },
        };

        var zero = library.GenerationMessages(PromptLibrary.ZeroShot, input);
        Assert.Equal("system", zero[0].Role);
        Assert.Contains("Test class name: Calc_ArrowTest (package a)", zero[1].Content);
        Assert.DoesNotContain("JUnit", zero[1].Content); // D3: không cho biết framework

        var few = library.GenerationMessages(PromptLibrary.FewShotStrategy, input);
        Assert.Contains("Example 1 – class under test:", few[1].Content);
        Assert.Contains("Example 2 – unit test:", few[1].Content);
        Assert.DoesNotContain("Repository configuration", few[1].Content);

        var aware = library.GenerationMessages(PromptLibrary.ProjectAware, input);
        Assert.Contains("- Build tool: Maven 3.9.6 (wrapper: yes)", aware[1].Content);
        Assert.Contains("- Java version: 17 (release=17)", aware[1].Content);
        Assert.Contains("- Test framework: JUnit 5 5.10.2 (org.junit.jupiter)", aware[1].Content);
        Assert.Contains("org.apache.commons:commons-lang3:3.14.0", aware[1].Content);
        Assert.DoesNotContain("com.unrelated", aware[1].Content);
        Assert.Contains("Write it to: src/test/java/a/Calc_ArrowTest.java (package a)", aware[1].Content);
    }

    public static string PilotToolDir()
    {
        for (var directory = new DirectoryInfo(AppContext.BaseDirectory); directory is not null; directory = directory.Parent)
        {
            if (File.Exists(Path.Combine(directory.FullName, "ArrowRunner.sln")))
            {
                return directory.FullName;
            }
        }

        throw new InvalidOperationException("pilot_tool không tìm thấy");
    }
}

public class OutputNormalizerTests
{
    private static NormalizationResult N(string response, string framework = "junit5", string? finish = "stop") =>
        OutputNormalizer.Normalize(response, finish, "com.example", "Calc_ArrowTest", "Calc", framework);

    [Fact]
    public void ExtractsFencedCodeAndRenamesVariant()
    {
        var result = N("Here is the test:\n```java\npackage com.example;\n\nimport org.junit.jupiter.api.Test;\n\nclass CalcTest {\n  @Test void a() { CalcTest t = null; }\n}\n```\nDone.");
        Assert.True(result.Valid, result.RejectReason);
        Assert.True(result.HadFence);
        Assert.Equal("CalcTest", result.ClassBefore);
        Assert.Contains("class Calc_ArrowTest {", result.Code);
        Assert.Contains("Calc_ArrowTest t = null", result.Code);
    }

    [Fact]
    public void AcceptsCodeWithoutFence()
    {
        var result = N("package com.example;\nimport org.junit.jupiter.api.Test;\npublic class Calc_ArrowTest { @Test void a() {} }");
        Assert.True(result.Valid, result.RejectReason);
        Assert.False(result.HadFence);
    }

    [Theory]
    [InlineData("", "empty")]
    [InlineData("--- a/Calc.java\n+++ b/Calc.java\n@@ -1 +1 @@", "diff instead of a Java file")]
    [InlineData("package com.example;\n// see pom.xml\nclass Calc_ArrowTest {}", "non-test content (pom.xml)")]
    [InlineData("package com.example;\nclass Calc_ArrowTest { void a() { String s = \"}\"; }", "unbalanced brackets")]
    [InlineData("package com.example;\nclass Calc_ArrowTest { String s = \"oops; }", "unclosed string literal")]
    [InlineData("package com.example;\nclass Other { }", "wrong class (found Other, expected Calc_ArrowTest)")]
    [InlineData("package com.other;\nclass Calc_ArrowTest { }", "wrong package (found 'com.other', expected 'com.example')")]
    public void Rejects(string response, string reason) => Assert.Equal(reason, N(response).RejectReason);

    [Fact]
    public void BracketsInsideStringsAndCommentsAreIgnored()
    {
        var result = N("package com.example;\nclass Calc_ArrowTest {\n  // )\n  /* { */\n  String s = \"(\"; char c = '}';\n}");
        Assert.True(result.Valid, result.RejectReason);
    }

    [Fact]
    public void TruncatedResponseIsRejected() =>
        Assert.Equal("truncated (finish_reason=length)", N("package com.example; class Calc_ArrowTest {}", finish: "length").RejectReason);

    [Fact]
    public void ConvertsJUnit4ToJUnit5()
    {
        var result = N("""
            package com.example;

            import static org.junit.Assert.*;

            import org.junit.Before;
            import org.junit.Test;

            public class CalcTest {
                @Before public void setUp() {}
                @Test public void adds() { assertEquals(2, 1 + 1); }
                @Test public void q() { Assert.assertTrue(true); }
            }
            """);
        Assert.True(result.Valid, result.RejectReason);
        var code = result.Code!;
        Assert.DoesNotContain("org.junit.Before", code);
        Assert.Contains("@BeforeEach public void setUp()", code);
        Assert.Contains("import org.junit.jupiter.api.Test;", code);
        Assert.Contains("import org.junit.jupiter.api.BeforeEach;", code);
        Assert.Contains("import static org.junit.jupiter.api.Assertions.*;", code);
        Assert.Contains("Assertions.assertTrue(true)", code);
        Assert.Contains("@Before→@BeforeEach", result.AnnotationsChanged);
        Assert.Contains("static org.junit.Assert.*", result.ImportsRemoved);
    }

    [Fact]
    public void ConvertsJUnit5ToJUnit4AndAddsJdkImports()
    {
        var result = N("""
            package com.example;

            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.Assertions;

            class Calc_ArrowTest {
                @BeforeEach void setUp() {}
                @Test void sorts() { Assertions.assertEquals(Arrays.asList(1), Collections.singletonList(1)); }
            }
            """, framework: "junit4");
        Assert.True(result.Valid, result.RejectReason);
        var code = result.Code!;
        Assert.DoesNotContain("jupiter", code);
        Assert.Contains("@Before void setUp()", code);
        Assert.Contains("import org.junit.Test;", code);
        Assert.Contains("import org.junit.Before;", code);
        Assert.Contains("Assert.assertEquals(", code);
        Assert.Contains("import java.util.Arrays;", code);
        Assert.Contains("import java.util.Collections;", code);
    }

    [Fact]
    public void TestNgIsUnchanged()
    {
        const string code = "package com.example;\nimport org.testng.annotations.Test;\npublic class Calc_ArrowTest { @Test public void a() {} }";
        Assert.Equal(code + "\n", N(code, "testng").Code);
    }
}

public class LlmRequestTests
{
    [Fact]
    public void CacheKeyIgnoresParamOrderButNotContent()
    {
        var a = new LlmRequest { Model = "m", Messages = [new ChatMessage("user", "hi")], Params = new Dictionary<string, object?> { ["temperature"] = 0, ["seed"] = 42 } };
        var b = a with { Params = new Dictionary<string, object?> { ["seed"] = 42, ["temperature"] = 0 } };
        var c = a with { Messages = [new ChatMessage("user", "hi!")] };
        Assert.Equal(a.CacheKey(), b.CacheKey());
        Assert.NotEqual(a.CacheKey(), c.CacheKey());
    }

    [Fact]
    public void CostSkipsCacheHits()
    {
        var usage = new LlmUsage { PromptTokens = 1_000_000, CompletionTokens = 500_000 };
        Assert.Equal(2.0, LlmCost.Usd(usage, 1.0, 2.0), 6);
        Assert.Equal(0, LlmCost.Usd(usage with { CacheHit = true }, 1.0, 2.0));
        Assert.Equal(0, LlmCost.Usd(usage, null, null));
    }
}
