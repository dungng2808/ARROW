using System.Text.Json;
using Preflight.Tests;
using Xunit;

namespace Preflight.DataAccess.Tests;

public sealed class GoldenTests
{
    [Theory]
    [InlineData("json_format")]
    [InlineData("revision_map")]
    public void MatchesPythonExactly(string function)
    {
        using var work = new TestWorkspace();
        GoldenVerifier.Verify(function, input =>
        {
            if (function == "json_format") return PythonJson.Render(input.GetProperty("value"), input.GetProperty("compact").GetBoolean(), input.GetProperty("indented").GetBoolean());
            if (input.ValueKind == JsonValueKind.Null) return RevisionMapReader.Load(null);
            string path = work.File("map." + input.GetProperty("extension").GetString(), input.GetProperty("text").GetString()!);
            return RevisionMapReader.Load(path);
        });
    }
}
