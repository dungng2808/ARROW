namespace Arrow.Service.Interfaces;

public sealed record PromptFileInfo(string RelativePath, string Id, string Version, string Role, string Sha256);

public sealed record PromptCheckReport(IReadOnlyList<PromptFileInfo> Files, IReadOnlyList<string> Problems)
{
    public bool Ok => Problems.Count == 0;
}

/// <summary>Dịch vụ cho UI: xem và kiểm tra bộ prompt (lệnh <c>arrow prompts check</c>).</summary>
public interface IPromptService
{
    PromptCheckReport Check();
}
