using System.Text;
using System.Text.RegularExpressions;
using Arrow.Core.Repair;
using Arrow.Core.Verify;
using Arrow.Infrastructure.Generation;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Java;

namespace Arrow.Infrastructure.Repair;

/// <summary>
/// Thông tin bổ sung của prompt sửa mức 2 theo trạng thái của checkpoint (specs/04 Mục 6.2, AR 3.1).
/// </summary>
public sealed partial class RepairContextBuilder(GitRepositoryProvider git, JavaAnalyzer analyzer)
{
    public async Task<string> BuildAsync(PreparedTaskData data, string cutSource, BuiltCandidate checkpoint, VerifyResult verify, CancellationToken cancellationToken)
    {
        var task = data.Task;
        switch (checkpoint.State)
        {
            case VerificationState.Compile:
            {
                var related = await RelatedSourcesAsync(data, verify, cancellationToken).ConfigureAwait(false);
                var api = await analyzer.CutApiAsync(cutSource, related.Values, cancellationToken).ConfigureAwait(false);
                var builder = new StringBuilder("Constructors and methods that really exist (class under test and related classes):\n");
                foreach (var type in api.Types)
                {
                    builder.AppendLine($"{type.Kind} {type.Name}:");
                    foreach (var member in type.Members)
                    {
                        builder.AppendLine("  " + member);
                    }
                }

                builder.Append(RepairPrompts.DependenciesAndJava(data.Context));
                return builder.ToString();
            }

            case VerificationState.Discovery:
                return RepairPrompts.DiscoveryBlock(task.TestingFramework, data.TestPackage, data.TestClassName, task.GeneratedTestPath);

            case VerificationState.Runtime:
            {
                var called = await analyzer.CalledMethodsAsync(checkpoint.Code, cutSource, verify.Failures.Select(f => f.Method), cancellationToken).ConfigureAwait(false);
                var builder = new StringBuilder();
                AppendCode(builder, "Code of the methods of the class under test that the failing tests call:", called.Methods);
                AppendCode(builder, "Constructors:", called.Constructors);
                AppendCode(builder, "Fields and how they are initialised:", called.Fields);
                builder.Append(MockHint(data));
                return builder.ToString().TrimEnd();
            }

            case VerificationState.Assertion:
            {
                var called = await analyzer.CalledMethodsAsync(checkpoint.Code, cutSource, verify.Failures.Select(f => f.Method), cancellationToken).ConfigureAwait(false);
                var builder = new StringBuilder("Expected and actual values of the failing assertions:\n");
                builder.AppendLine(RepairPrompts.ExpectedActualLines(verify));
                AppendCode(builder, "Code of the methods under test:", called.Methods);
                return builder.ToString().TrimEnd();
            }

            case VerificationState.Regression:
                return RepairPrompts.RegressionBlock(verify);

            case VerificationState.Unknown:
            {
                var log = ReadLastLog(data.TaskDir, verify);
                var buildFile = await ModuleBuildFileAsync(data, cancellationToken).ConfigureAwait(false);
                var parts = new[]
                {
                    "(a) Build failure:\n" + RepairPrompts.UnknownFailureBlock(log),
                    "(b) Test step configuration:\n" + RepairPrompts.TestConfigBlock(buildFile, task.Plan.Tool == Core.Prepare.BuildToolKind.Maven),
                    "(c) " + RepairPrompts.TestEnginesBlock(data.Context),
                    "(d) Signals in the log:\n" + (RepairPrompts.UnknownSignalsBlock(log) is { Length: > 0 } signals ? signals : "- none recognised"),
                };
                return string.Join("\n\n", parts);
            }

            default:
                return "";
        }
    }

    private static void AppendCode(StringBuilder builder, string title, IReadOnlyCollection<string> snippets)
    {
        if (snippets.Count == 0)
        {
            return;
        }

        builder.AppendLine(title).AppendLine("```java");
        foreach (var snippet in snippets.Take(10))
        {
            builder.AppendLine(snippet.TrimEnd()).AppendLine();
        }

        builder.AppendLine("```");
    }

    private static string MockHint(PreparedTaskData data)
    {
        var libraries = data.Context.TestFramework.Libraries;
        var mockito = libraries.FirstOrDefault(l => l.Contains("mockito", StringComparison.OrdinalIgnoreCase));
        if (mockito is null)
        {
            return "No mocking library is available in the test scope: create real objects or simple hand-written fakes.\n";
        }

        var setup = data.Task.TestingFramework switch
        {
            "junit5" => "@ExtendWith(MockitoExtension.class) with @Mock fields, or Mockito.mock(Type.class)",
            "junit4" => "@RunWith(MockitoJUnitRunner.class) with @Mock fields, or Mockito.mock(Type.class)",
            _ => "Mockito.mock(Type.class) in a @BeforeMethod method",
        };
        return $"Mocking library in the test scope: {mockito}. Create mocks with {setup}.\n";
    }

    [GeneratedRegex(@"\b(?:class|interface|enum|record)\s+([\w.$]+)|type\s+([\w.$]+)|symbol:\s+class\s+([\w.$]+)")]
    private static partial Regex TypeMention();

    /// <summary>Mã nguồn các class liên quan mà lỗi biên dịch nhắc tới (tối đa 5), tìm trong src/main/java của commit.</summary>
    private async Task<Dictionary<string, string>> RelatedSourcesAsync(PreparedTaskData data, VerifyResult verify, CancellationToken cancellationToken)
    {
        var names = new HashSet<string>(StringComparer.Ordinal);
        foreach (var error in verify.CompileErrors)
        {
            foreach (var text in new[] { error.Symbol, error.Location, error.Message })
            {
                foreach (Match match in TypeMention().Matches(text ?? ""))
                {
                    var name = match.Groups.Cast<Group>().Skip(1).First(g => g.Success).Value;
                    var simple = name[(name.LastIndexOf('.') + 1)..].Split('$')[0];
                    if (simple.Length > 0 && char.IsUpper(simple[0]) && simple != data.CutSimpleName && simple != data.TestClassName)
                    {
                        names.Add(simple);
                    }
                }
            }
        }

        var result = new Dictionary<string, string>(StringComparer.Ordinal);
        if (names.Count == 0)
        {
            return result;
        }

        var files = await git.ListFilesAsync(data.Task.RepoUrl, data.Task.CheckoutSha, cancellationToken).ConfigureAwait(false);
        foreach (var name in names.Take(5))
        {
            var path = files.FirstOrDefault(f => f.Contains("src/main/java/", StringComparison.Ordinal) && f.EndsWith("/" + name + ".java", StringComparison.Ordinal));
            if (path is not null)
            {
                result[name] = await git.ReadFileAsync(data.Task.RepoUrl, data.Task.CheckoutSha, path, cancellationToken).ConfigureAwait(false);
            }
        }

        return result;
    }

    private async Task<string> ModuleBuildFileAsync(PreparedTaskData data, CancellationToken cancellationToken)
    {
        var moduleDir = data.Task.Plan.ModuleDir == "." ? "" : data.Task.Plan.ModuleDir + "/";
        foreach (var name in new[] { "pom.xml", "build.gradle", "build.gradle.kts" })
        {
            try
            {
                return await git.ReadFileAsync(data.Task.RepoUrl, data.Task.CheckoutSha, moduleDir + name, cancellationToken).ConfigureAwait(false);
            }
            catch (Core.Prepare.PrepareException)
            {
            }
        }

        return "";
    }

    private static string ReadLastLog(string taskDir, VerifyResult verify)
    {
        var last = verify.Logs.LastOrDefault();
        var path = last is null ? null : Path.Combine(taskDir, last);
        return path is not null && File.Exists(path) ? File.ReadAllText(path) : verify.ErrorLog ?? "";
    }
}
