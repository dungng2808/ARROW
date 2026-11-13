using System.Text;
using System.Text.Json;
using Preflight.Models;
using Preflight.Tests;
using Xunit;

namespace Preflight.DataAccess.Tests;

public sealed class OutputTests
{
    [Fact]
    public void FailedWriteKeepsPreviouslyPublishedFile()
    {
        using var work = new TestWorkspace(); string path = work.File("result.jsonl", "previous\n");
        static IEnumerable<object> BrokenRecords()
        {
            yield return new { A = 1 };
            throw new IOException("fixture write failure");
        }
        Assert.Throws<IOException>(() => ManifestWriter.WriteJsonl(path, BrokenRecords()));
        Assert.Equal("previous\n", File.ReadAllText(path));
    }
    [Fact]
    public void OutputIsStablyKeySortedAndByteIdenticalToPython()
    {
        using var work = new TestWorkspace(); string path = Path.Combine(work.Path, "ordered.jsonl");
        var records = new[] { new Dictionary<string, object> { ["z"] = 1, ["a"] = 2 } };
        ManifestWriter.WriteJsonl(path, records); byte[] first = File.ReadAllBytes(path);
        ManifestWriter.WriteJsonl(path, records);
        Assert.Equal(Encoding.UTF8.GetBytes("{\"a\": 2, \"z\": 1}\n"), first);
        Assert.Equal(first, File.ReadAllBytes(path)); Assert.False(File.Exists(path + ".tmp"));
    }
    [Fact]
    public void JsonlCsvRoundTripAndNestedData()
    {
        using var work = new TestWorkspace(); string jsonl = Path.Combine(work.Path, "result.jsonl"), csv = Path.Combine(work.Path, "result.csv");
        var value = Result() with { BuildAttempts = [new("main_compile", "work", ["mvn", "compile"], 0, false, 0.1, "log.txt")] };
        ManifestWriter.WriteJsonl(jsonl, new[] { value }); ManifestWriter.WriteCsv(csv, new[] { value });
        using var parsed = JsonDocument.Parse(File.ReadAllText(jsonl));
        Assert.Equal("SOURCE_INVALID", parsed.RootElement.GetProperty("preflight_status").GetString());
        Assert.Equal("mvn", parsed.RootElement.GetProperty("build_attempts")[0].GetProperty("command")[0].GetString());
        Assert.Contains("\"\"command\"\":[\"\"mvn\"\",\"\"compile\"\"]", File.ReadAllText(csv));
        Assert.Contains("False", File.ReadAllText(csv));
        Assert.False(File.Exists(csv + ".tmp"));
    }
    [Fact]
    public void CsvPreservesQuotesNewlinesNullAndPythonBooleans()
    {
        using var work = new TestWorkspace(); string path = Path.Combine(work.Path, "output.csv");
        ManifestWriter.WriteCsv(path, new[] { new { Z = (string?)null, A = "a,\"b\"\nđ", B = true, C = new[] { "x", "y" } } });
        Assert.Equal("a,b,c,z\r\n\"a,\"\"b\"\"\nđ\",True,\"[\"\"x\"\",\"\"y\"\"]\",\r\n", File.ReadAllText(path));
    }
    [Fact]
    public void JsonIsIndentedUtf8WithoutBomAndSurvivesReplacement()
    {
        using var work = new TestWorkspace(); string path = Path.Combine(work.Path, "output.json");
        ManifestWriter.WriteJson(path, new { Z = "đ", A = 0.0 });
        Assert.Equal("{\n  \"a\": 0.0,\n  \"z\": \"đ\"\n}\n", File.ReadAllText(path));
        Assert.False(File.ReadAllBytes(path).Take(3).SequenceEqual(new byte[] { 0xef, 0xbb, 0xbf }));
        Assert.False(File.Exists(path + ".tmp"));
    }
    [Fact]
    public void DefaultResultKeepsEveryPythonFieldAndDocumentedAdditions()
    {
        string[] fields = ["task_id", "source_json_path", "source_json_sha256", "source_json_paths", "source_json_sha256s", "repo_url", "checkout_sha", "revision_provenance", "revision_verification_status", "run_mode", "class_path", "class_fqn", "test_class_path", "module_path", "working_directory", "build_tool", "build_tool_version", "java_version", "testing_framework", "constructor_count", "method_count", "api_count", "probed_api", "probe_status", "preflight_status", "technical_eligible", "strict_eligible", "tags", "reason_codes", "content_match", "build_attempts", "duration_seconds", "log_paths"];
        var value = PythonJson.Element(Result());
        Assert.All(fields, field => Assert.True(value.TryGetProperty(field, out _), field));
        string[] additions = ["dataset", "pairing_method", "pairing_evidence", "revision_evidence_ref", "probe_profile", "build_tool_version_detail", "test_class_paths", "extra"];
        Assert.Equal(fields.Concat(additions).Order(StringComparer.Ordinal), value.EnumerateObject().Select(p => p.Name).Order(StringComparer.Ordinal));
        Assert.Equal("UNVERIFIED", value.GetProperty("revision_verification_status").GetString());
        Assert.Equal("DISCOVERY_ONLY", value.GetProperty("run_mode").GetString());
    }
    [Fact]
    public void ConfigDefaultsAndDeprecatedKeysAreExplicit()
    {
        using var work = new TestWorkspace(); var warnings = new List<string>();
        var config = ConfigLoader.Load(work.File("config.json", "{\"run\":{\"workers\":3,\"probe_profile\":\"v1\"},\"policy\":{\"integration_rule_file\":\"ignored\"}}"), warnings.Add);
        Assert.Equal(3, config.Run.Workers); Assert.Equal(ProbeProfile.v1, config.Run.ProbeProfile);
        Assert.Equal("all_filepairs", new DatasetConfig().Cohort); Assert.Single(warnings);
    }
    [Theory]
    [InlineData("{\"run\":{\"workers\":0}}")]
    [InlineData("{\"run\":{\"build_timeout_seconds\":0}}")]
    [InlineData("{\"run\":{\"max_evidence_checks\":0}}")]
    [InlineData("{\"disk\":{\"min_free_gb\":100,\"resume_free_gb\":1}}")]
    public void InvalidConfigIsRejected(string json)
    {
        using var work = new TestWorkspace(); Assert.Throws<InvalidDataException>(() => ConfigLoader.Load(work.File("config.json", json)));
    }
    private static PreflightResult Result() => new()
    {
        TaskId = "task", SourceJsonPath = "42/a.json", SourceJsonSha256 = new('a', 64),
        SourceJsonPaths = ["42/a.json"], SourceJsonSha256s = [new('a', 64)], RepoUrl = "https://example.test/repo"
    };
}
