using Arrow.DataAccess.Files;
using Arrow.Models.Block2;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.Block2;

namespace Arrow.Service.Tests;

public class PromptTests
{
    private static string FrameworkRoot
    {
        get
        {
            for (var dir = new DirectoryInfo(AppContext.BaseDirectory); dir is not null; dir = dir.Parent)
            {
                if (File.Exists(Path.Combine(dir.FullName, "ArrowFramework.sln")))
                {
                    return dir.FullName;
                }
            }

            throw new InvalidOperationException("Không tìm thấy ArrowFramework.sln");
        }
    }

    private static Dictionary<string, PromptTemplate> RealTemplates() =>
        new FilePromptRepository(new FrameworkPaths(FrameworkRoot)).LoadAll().ToDictionary(t => t.RelativePath);

    private static PromptTemplate Template(string body, params string[] placeholders) => new()
    {
        RelativePath = "t.md", Id = "t", Version = "v1", Role = "user", Body = body, Placeholders = [.. placeholders],
    };

    [Fact]
    public void RenderFillsEveryPlaceholderOnce()
    {
        var text = PromptTemplateRenderer.Render(Template("A {{x}} B {{y}} C {{x}}", "x", "y"), new Dictionary<string, string> { ["x"] = "1", ["y"] = "{{x}}" });
        // Giá trị đã điền (ở đây là "{{x}}") không bị thay tiếp.
        Assert.Equal("A 1 B {{x}} C 1", text);
    }

    [Fact]
    public void RenderRejectsMissingExtraOrUndeclaredValues()
    {
        var t = Template("{{x}} {{y}}", "x", "y");
        Assert.Equal(ErrorCodes.PromptInvalid, Assert.Throws<ArrowException>(() => PromptTemplateRenderer.Render(t, new Dictionary<string, string> { ["x"] = "1" })).Code);
        Assert.Throws<ArrowException>(() => PromptTemplateRenderer.Render(t, new Dictionary<string, string> { ["x"] = "1", ["y"] = "2", ["z"] = "3" }));
        Assert.Throws<ArrowException>(() => PromptTemplateRenderer.Render(Template("{{x}}"), new Dictionary<string, string> { ["x"] = "1" }));
    }

    [Fact]
    public void RealProjectAwareUserPromptRenders()
    {
        var template = RealTemplates()[PromptFiles.UserProjectAware];
        var text = PromptTemplateRenderer.Render(template, new Dictionary<string, string>
        {
            ["cut_fqn"] = "com.acme.shop.OrderService",
            ["cut_path"] = "shop-core/src/main/java/com/acme/shop/OrderService.java",
            ["test_package"] = "com.acme.shop",
            ["test_class_name"] = "OrderService_ArrowTest",
            ["repository_configuration"] = "- Build tool: Maven 3.9.6",
            ["cut_source"] = "package com.acme.shop;\npublic class OrderService {}",
        });
        Assert.Contains("- Class name: OrderService_ArrowTest", text);
        Assert.Contains("Repository configuration (compatibility constraints):\n- Build tool: Maven 3.9.6", text);
        Assert.DoesNotContain("{{", text);
        Assert.DoesNotContain("---", text);
    }

    [Fact]
    public void StrategyMapsToTemplates()
    {
        Assert.Equal((PromptFiles.SystemFewShot, PromptFiles.UserDefault), PromptFiles.ForStrategy("few_shot"));
        Assert.Equal((PromptFiles.SystemZeroShotProjectAware, PromptFiles.UserProjectAware), PromptFiles.ForStrategy("zero_shot_project_aware"));
        Assert.Throws<ArrowException>(() => PromptFiles.ForStrategy("zero_shot_repository_aware"));
    }

    [Fact]
    public void RealPromptSetIsConsistent()
    {
        Assert.Empty(PromptConsistencyChecker.Check(RealTemplates()));
    }

    [Fact]
    public void CheckerDetectsUnfairChanges()
    {
        var templates = RealTemplates();

        var zeroShot = templates[PromptFiles.SystemZeroShot];
        templates[PromptFiles.SystemZeroShot] = zeroShot with { Body = zeroShot.Body.Replace("Do not mock the CUT itself.", "Mock the CUT if useful.") };
        var userDefault = templates[PromptFiles.UserDefault];
        templates[PromptFiles.UserDefault] = userDefault with { Body = userDefault.Body.Replace("Generate a unit test class", "Generate thorough unit tests") };
        var repair = templates[PromptFiles.RepairSystem];
        templates[PromptFiles.RepairSystem] = repair with { Body = repair.Body.Replace("Do not output Markdown fences", "You may output Markdown fences") };
        var level2 = templates[PromptFiles.RepairLevel2];
        templates[PromptFiles.RepairLevel2] = level2 with { Placeholders = [.. level2.Placeholders, "unused_value"] };
        templates.Remove(PromptFiles.RepairLevel3);

        var problems = PromptConsistencyChecker.Check(templates);
        Assert.Contains(problems, p => p.Contains("zero_shot_project_aware.md") && p.Contains("Shared requirements"));
        Assert.Contains(problems, p => p.Contains("project_aware.md") && p.Contains("ngoài khối cấu hình"));
        Assert.Contains(problems, p => p.Contains("repair/system.md") && p.Contains("Output contract"));
        Assert.Contains(problems, p => p.Contains("unused_value"));
        Assert.Contains(problems, p => p.Contains("Thiếu template repair/user/level3.md"));
    }
}
