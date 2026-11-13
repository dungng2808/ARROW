using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Service.Core.PyCompat;
using Preflight.Service.Datasets.Classes2Test;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class AuditRegressionTests
{
    [Fact]
    public void JsonLineMapSupportsCarriageReturnsAndLastDuplicateKey()
    {
        using var w = new TestWorkspace(); string sha = new('a', 40);
        string row = "{\"task_id\":\"discard\",\"task_id\":\"task\",\"checkout_sha\":\"" + sha + "\",\"revision_verification_status\":\"UPSTREAM_PINNED\",\"revision_provenance\":\"dataset_record\",\"evidence_ref\":\"fixture\"}";
        string path = w.File("map.jsonl", row + "\r" + row.Replace("\"task\"", "\"task2\"", StringComparison.Ordinal));
        var map = RevisionMapReader.Load(path); Assert.Equal(2, map.Count); Assert.DoesNotContain("discard", map.Keys);
    }
    [Fact]
    public void DeepMetadataDoesNotRejectOtherwiseValidIdentity()
    {
        using var w = new TestWorkspace(); string nested = "null";
        for (int i = 0; i < 70; i++) nested = "{\"extra\":" + nested + "}";
        string json = "{\"repository\":{\"url\":\"repo\"},\"focal_class\":{\"file\":\"X.java\",\"identifier\":\"X\"},\"metadata\":" + nested + "}";
        var result = Classes2TestRecordReader.Read(w.File("42/1.json", json)); Assert.NotNull(result.Identity); Assert.Null(result.ReasonCode);
    }
    [Fact]
    public void UnicodeCodePointSortAndRstripMatchPython()
    {
        using var doc = JsonDocument.Parse("{\"😀\":\"supplementary\",\"\ue000\":\"bmp\"}");
        Assert.Equal("{\"\ue000\": \"bmp\", \"😀\": \"supplementary\"}", PythonJson.Render(doc.RootElement));
        Assert.Equal("𐐀", PyStr.RStrip("𐐀", "𠀀"));
    }
}
