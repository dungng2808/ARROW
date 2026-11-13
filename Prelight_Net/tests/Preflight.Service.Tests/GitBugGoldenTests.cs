using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Service.Datasets.GitBug;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class GitBugGoldenTests
{
    [Fact]
    public void EveryRawGitBugPatchAndPairingRuleMatchesPython()
    {
        int bugs = 0, pairs = 0;
        using var metadata = JsonDocument.Parse(File.ReadAllText(Path.Combine(TestWorkspace.Root, "tests/golden/gitbug_rules.meta.json")));
        Assert.Equal(metadata.RootElement.GetProperty("sha256").GetString(), Core.Runner.RuntimePaths.Hash(Path.Combine(TestWorkspace.Root, "tests/golden/gitbug_rules.jsonl")));
        foreach (string line in File.ReadLines(Path.Combine(TestWorkspace.Root, "tests/golden/gitbug_rules.jsonl")))
        {
            using var doc = JsonDocument.Parse(line); var row = doc.RootElement; var input = row.GetProperty("input"); object actual;
            if (row.GetProperty("operation").GetString() == "changed_java") { actual = GitBugRules.ChangedJavaFiles(input.GetString()!).Select(t => new object[] { t.Path, t.Deleted }).ToArray(); bugs++; }
            else { var result = GitBugRules.PairTest(input.GetProperty("path").GetString()!, input.GetProperty("paths").EnumerateArray().Select(p => p.GetString()!).ToHashSet(StringComparer.Ordinal)); actual = new[] { result.Path, result.Method }; pairs++; }
            Assert.Equal(PythonJson.Render(row.GetProperty("output")), PythonJson.Serialize(actual));
        }
        Assert.Equal(199, bugs); Assert.Equal(606, pairs);
    }
}
