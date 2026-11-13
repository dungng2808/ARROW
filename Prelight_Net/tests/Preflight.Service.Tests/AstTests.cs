using System.Text.Json;
using System.Text.Json.Nodes;
using System.IO.Compression;
using Preflight.DataAccess;
using Preflight.Service.Core.JavaAst;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class AstTests
{
    [Fact]
    public void NativeParserMatchesPythonOnFiveThousandRealJavaFiles()
    {
        string path = Path.Combine(TestWorkspace.Root, "tests/golden/java_ast.jsonl.gz");
        using var metadata = JsonDocument.Parse(File.ReadAllText(Path.Combine(TestWorkspace.Root, "tests/golden/java_ast.meta.json")));
        Assert.Equal(metadata.RootElement.GetProperty("sha256").GetString(), Core.Runner.RuntimePaths.Hash(path));
        var parser = new JavaSourceParser(); int count = 0;
        using var fileStream = File.OpenRead(path); using var gzip = new GZipStream(fileStream, CompressionMode.Decompress); using var reader = new StreamReader(gzip);
        while (reader.ReadLine() is { } line)
        {
            using var doc = JsonDocument.Parse(line); var input = doc.RootElement.GetProperty("input");
            string source = input.GetProperty("source").GetString()!, file = input.GetProperty("path").GetString()!;
            var actual = PythonJson.Element(parser.Parse(source, file, input.GetProperty("expected_name").GetString())); var expected = doc.RootElement.GetProperty("output");
            Assert.True(JsonNode.DeepEquals(JsonNode.Parse(expected.GetRawText()), JsonNode.Parse(actual.GetRawText())), $"AST {count + 1}: {file}\nexpected={expected}\nactual={actual}"); count++;
        }
        Assert.True(count >= 5000); Assert.Equal(metadata.RootElement.GetProperty("count").GetInt32(), count);
        ManifestWriter.WriteJson(Path.Combine(TestWorkspace.Root, ".work/verification/java_ast.json"), new { Matched = count, Total = count });
    }
    [Fact]
    public void InheritanceHidesMethodsByNameAndTerminatesCycles()
    {
        using var work = new TestWorkspace();
        work.File("src/main/java/p/Parent.java", "package p; public class Parent extends Child { public void x(int a){} public void y(){} }");
        string file = work.File("src/main/java/p/Child.java", "package p; public class Child extends Parent { public void x(){} }");
        var parser = new JavaSourceParser(); var java = parser.Parse(File.ReadAllText(file), "src/main/java/p/Child.java", "Child")!;
        var apis = new ApiCounter(parser).Count(java, file, work.Path);
        Assert.Single(apis, a => a.Name == "x"); Assert.Single(apis, a => a.Name == "y");
    }
}
