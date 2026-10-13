using Arrow.DataAccess.Interfaces;
using Arrow.Service.Block2;
using Arrow.Service.Interfaces;

namespace Arrow.Service.App;

public sealed class PromptService(IPromptRepository prompts, ILogRepository log) : IPromptService
{
    public PromptCheckReport Check()
    {
        var templates = prompts.LoadAll().ToDictionary(t => t.RelativePath, StringComparer.Ordinal);
        var problems = PromptConsistencyChecker.Check(templates);
        log.Info($"prompts: kiểm tra {templates.Count} template, {problems.Count} vấn đề");
        var files = templates.Values
            .OrderBy(t => t.RelativePath, StringComparer.Ordinal)
            .Select(t => new PromptFileInfo(t.RelativePath, t.Id, t.Version, t.Role, t.Sha256))
            .ToList();
        return new PromptCheckReport(files, problems);
    }
}
