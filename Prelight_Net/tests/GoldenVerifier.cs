using System.Security.Cryptography;
using System.Text.Json;
using System.Text.Json.Nodes;
using Preflight.DataAccess;
using Xunit;

namespace Preflight.Tests;

internal static class GoldenVerifier
{
    internal static string Folder => Environment.GetEnvironmentVariable("PREFLIGHT_GOLDEN_ROOT") ?? Path.Combine(TestWorkspace.Root, "tests", "golden");
    internal static void Verify(string name, Func<JsonElement, object?> evaluate, Func<Exception, string>? errorName = null)
    {
        using var manifest = JsonDocument.Parse(File.ReadAllBytes(Path.Combine(Folder, "manifest.json")));
        string file = Path.Combine(Folder, name + ".jsonl");
        var metadata = manifest.RootElement.GetProperty("files").GetProperty(name + ".jsonl");
        using (var stream = File.OpenRead(file)) Assert.Equal(metadata.GetProperty("sha256").GetString(), Convert.ToHexString(SHA256.HashData(stream)).ToLowerInvariant());
        int count = 0;
        foreach (string line in File.ReadLines(file))
        {
            using var record = JsonDocument.Parse(line);
            JsonElement input = record.RootElement.GetProperty("input");
            object? actual = null; Exception? error = null;
            try { actual = evaluate(input); } catch (Exception caught) { error = caught; }
            if (record.RootElement.TryGetProperty("error", out var expectedError))
                Assert.True(error is not null && expectedError.GetString() == (errorName?.Invoke(error) ?? ErrorName(error)), $"{name} line {count + 1}; input={input}; expected error={expectedError}; actual={error}");
            else
            {
                Assert.True(error is null, $"{name} line {count + 1}; input={input}; unexpected={error}");
                JsonElement expected = record.RootElement.GetProperty("output"), actualElement = PythonJson.Element(actual);
                Assert.True(JsonNode.DeepEquals(JsonNode.Parse(expected.GetRawText()), JsonNode.Parse(actualElement.GetRawText())),
                    $"{name} line {count + 1}; input={input}; expected={expected}; actual={actualElement}");
            }
            count++;
        }
        Assert.Equal(metadata.GetProperty("count").GetInt32(), count);
        bool subset = manifest.RootElement.TryGetProperty("subset", out var subsetValue) && subsetValue.GetBoolean();
        ManifestWriter.WriteJson(Path.Combine(TestWorkspace.Root, ".work", "verification", subset ? "subset" : "full", name + ".json"), new { Function = name, Matched = count, Total = count, GoldenSha256 = metadata.GetProperty("sha256").GetString() });
    }
    internal static string ErrorName(Exception error) => error switch
    {
        System.Text.DecoderFallbackException => "UnicodeDecodeError", ArgumentException => "ValueError", JsonException => "JSONDecodeError",
        _ => error.GetType().Name
    };
}
