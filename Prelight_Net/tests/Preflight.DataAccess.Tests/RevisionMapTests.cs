using Preflight.Tests;
using Xunit;

namespace Preflight.DataAccess.Tests;

public sealed class RevisionMapTests
{
    [Fact]
    public void EvidenceContainingOnlyAlternatingWhitespaceIsRejected()
    {
        using var work = new TestWorkspace();
        string json = System.Text.Json.JsonSerializer.Serialize(new { task_id = "task", checkout_sha = new string('a', 40), revision_verification_status = "UPSTREAM_PINNED", revision_provenance = "dataset_record", evidence_ref = "\x1c \x1d \x1e \x1f " });
        Assert.Throws<ArgumentException>(() => RevisionMapReader.Load(work.File("map.jsonl", json)));
    }
    [Fact]
    public void InvalidUtf8IsRejectedWithoutReplacement()
    {
        using var work = new TestWorkspace(); string path = work.File("map.csv", "");
        File.WriteAllBytes(path, [0x80, 0x81]);
        Assert.Throws<System.Text.DecoderFallbackException>(() => RevisionMapReader.Load(path));
    }
    [Fact]
    public void CsvOnlyRemovesOneUtf8Bom()
    {
        using var work = new TestWorkspace();
        string path = work.File("map.csv", "\ufeff\ufefftask_id,checkout_sha,revision_verification_status,revision_provenance,evidence_ref\ntask," + new string('a', 40) + ",UPSTREAM_PINNED,dataset_record,ref\n");
        Assert.Throws<ArgumentException>(() => RevisionMapReader.Load(path));
    }
    [Fact]
    public void PythonValuePreservesFalsyValuesAndContainerRepr()
    {
        using var doc = System.Text.Json.JsonDocument.Parse("{\"empty\":[],\"value\":{\"x\":[true,null,\"a\"]}}");
        Assert.Equal("", PythonValue.StringOrEmpty(doc.RootElement.GetProperty("empty")));
        Assert.Equal("{'x': [True, None, 'a']}", PythonValue.StringOrEmpty(doc.RootElement.GetProperty("value")));
    }
    [Theory]
    [InlineData("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz")]
    [InlineData("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
    [InlineData("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
    [InlineData("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n")]
    [InlineData("ａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａａ")]
    public void ShaMustBeExactly40AsciiHex(string sha)
    {
        using var work = new TestWorkspace();
        string json = System.Text.Json.JsonSerializer.Serialize(new { task_id = "fixture", checkout_sha = sha, revision_verification_status = "UPSTREAM_PINNED", revision_provenance = "dataset_record", evidence_ref = "fixture.json" });
        Assert.Throws<ArgumentException>(() => RevisionMapReader.Load(work.File("map.jsonl", json)));
    }
    [Fact]
    public void CsvReadsBomQuotedCommaAndMultilineEvidence()
    {
        using var work = new TestWorkspace();
        string path = work.File("map.csv", "\ufefftask_id,checkout_sha,revision_verification_status,revision_provenance,evidence_ref\r\ntask," + new string('a', 40) + ",UPSTREAM_PINNED,dataset_record,\"one,\"\"two\"\"\nthree\"\r\n");
        Assert.Equal("one,\"two\"\nthree", RevisionMapReader.Load(path)["task"].EvidenceRef);
    }
    [Fact]
    public void NoneReturnsEmptyMap() => Assert.Empty(RevisionMapReader.Load(null));
}
