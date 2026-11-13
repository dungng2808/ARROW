using System.Text.Json;
using Preflight.DataAccess;
using Preflight.Models;
using Preflight.Service.Core.Build;
using Preflight.Service.Core.Common;
using Preflight.Service.Core.Jdk;
using Preflight.Service.Core.Policy;
using Preflight.Service.Core.Probe;
using Preflight.Service.Core.PyCompat;
using Preflight.Service.Datasets.Classes2Test;
using Preflight.Tests;
using Xunit;

namespace Preflight.Service.Tests;

public sealed class GoldenTests
{
    [Theory]
    [InlineData("task_id")]
    [InlineData("normalize_java")]
    [InlineData("primitive_arguments")]
    [InlineData("split_parameters")]
    [InlineData("erased_type")]
    [InlineData("cast_arguments")]
    [InlineData("primary_status")]
    [InlineData("eligibility")]
    [InlineData("framework")]
    [InlineData("class_policy")]
    [InlineData("probe_source")]
    [InlineData("java_target")]
    [InlineData("uses_release_flag")]
    [InlineData("failure_status")]
    [InlineData("record_identity")]
    [InlineData("path_candidates")]
    [InlineData("resolve_path")]
    [InlineData("test_source_root")]
    [InlineData("isspace")]
    [InlineData("pystr")]
    [InlineData("regex")]
    [InlineData("regex_unicode")]
    public void MatchesPythonExactly(string function)
    {
        using var work = new TestWorkspace();
        int record = 0;
        GoldenVerifier.Verify(function, input => Evaluate(function, input, work, record++), error => error is PythonDataException python ? python.PythonType : GoldenVerifier.ErrorName(error));
    }
    private static string S(JsonElement value, string name) => value.GetProperty(name).GetString()!;
    private static string[] Strings(JsonElement value) => value.EnumerateArray().Select(x => x.GetString()!).ToArray();
    private static object? Evaluate(string function, JsonElement input, TestWorkspace work, int record)
    {
        switch (function)
        {
            case "task_id": var a = Strings(input); return TaskIdFactory.Create(a[0], a[1], a[2], a[3]);
            case "normalize_java": return JavaNormalizer.Normalize(input.GetString()!);
            case "primitive_arguments": return ProbeArguments.PrimitiveArguments(input.GetString()!);
            case "split_parameters": return ProbeArguments.SplitParameters(input.GetString()!);
            case "erased_type": return ProbeArguments.ErasedType(input.GetString()!);
            case "cast_arguments": return ProbeArguments.CastArguments(S(input, "parameters"), S(input, "outer"), Strings(input.GetProperty("nested")).ToHashSet(StringComparer.Ordinal));
            case "primary_status": return StatusPolicy.SelectPrimaryStatus(Strings(input)).ToString();
            case "eligibility": var e = Strings(input); var eligibility = StatusPolicy.DeriveEligibility(e[0], e[1], e[2]); return new[] { eligibility.Technical, eligibility.Strict };
            case "framework": var f = Strings(input); return FrameworkDetector.Detect(f[0], f[1]);
            case "class_policy": var decision = ClassPolicy.Evaluate(input.Deserialize<JavaClass>(PythonJson.Options)!); return new object?[] { decision.Policy, decision.Tags, decision.Reasons };
            case "probe_source": return ProbeSourceBuilder.Build(input.GetProperty("java").Deserialize<JavaClass>(PythonJson.Options)!, input.GetProperty("api").Deserialize<JavaApi>(PythonJson.Options)!, S(input, "class_name"), S(input, "framework"), Enum.Parse<ProbeProfile>(S(input, "profile")), S(input, "source"));
            case "java_target":
            case "uses_release_flag":
                string root = Path.Combine(work.Path, "target");
                if (Directory.Exists(root)) Directory.Delete(root, true);
                Directory.CreateDirectory(root);
                foreach (var file in input.GetProperty("files").EnumerateObject()) work.File("target/" + file.Name, file.Value.GetString()!);
                string module = Path.GetFullPath(Path.Combine(root, S(input, "module")));
                Directory.CreateDirectory(module);
                return function == "java_target" ? JavaTargetReader.Read(module, S(input, "tool"), Enum.Parse<ProbeProfile>(S(input, "profile"))) : JavaTargetReader.UsesReleaseFlag(module);
            case "failure_status": return FailureClassifier.Classify(S(input, "text"), input.GetProperty("timed_out").GetBoolean(), S(input, "stage")).ToString();
            case "record_identity":
                string path = Path.Combine(work.Path, S(input, "project_id"), S(input, "name"));
                Directory.CreateDirectory(Path.GetDirectoryName(path)!);
                File.WriteAllBytes(path, Convert.FromBase64String(S(input, "bytes_base64")));
                var identity = Classes2TestRecordReader.Read(path);
                return new object?[] { identity.Identity, identity.Raw, identity.ReasonCode, identity.ContentSha256 };
            case "path_candidates":
            case "resolve_path":
                foreach (string file in Strings(input.GetProperty("files"))) work.File(file, "class X {}");
                string Relative(string value) => Path.GetRelativePath(work.Path, value).Replace('\\', '/');
                if (function == "path_candidates") return ResolvePath.PathCandidates(work.Path, S(input, "relative")).Select(Relative).ToArray();
                string? resolved = ResolvePath.Resolve(work.Path, S(input, "relative")); return resolved is null ? null : Relative(resolved);
            case "test_source_root": return Path.GetRelativePath(work.Path, TestSourceRoot.Find(Path.Combine(work.Path, S(input, "class_file")), Path.Combine(work.Path, S(input, "module_root")))).Replace('\\', '/');
            case "isspace": return PyIsSpace.IsSpace((char)input.GetInt32());
            case "pystr":
                string text = S(input, "text"), chars = S(input, "chars"); int max = input.GetProperty("max_split").GetInt32();
                return S(input, "operation") switch { "strip" => PyStr.Strip(text), "removeprefix" => PyStr.RemovePrefix(text, chars), "rstrip" => PyStr.RStrip(text, chars), "split" => PyStr.Split(text, chars, max), "rsplit" => PyStr.RSplit(text, chars, max), _ => throw new InvalidOperationException() };
            case "regex":
            case "regex_unicode": return PyRegex.Create(S(input, "pattern")).Matches(S(input, "text")).Select(m => m.Groups.Count > 1 ? m.Groups[1].Value : m.Value).ToArray();
            default: throw new InvalidOperationException($"Unknown golden function {function} at {record}");
        }
    }
}
