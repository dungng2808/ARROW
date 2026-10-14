using System.Text;
using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block3;

/// <summary>
/// Flow 3.3 (spec 03 Mục 5): từ <c>verify.json</c> dựng <see cref="Diagnosis"/> cho quyết định sửa và prompt.
/// Code thuần: không chạy công cụ (method chứa lỗi biên dịch đã được Verifier tính bằng java-helper).
/// </summary>
public sealed class FailureDiagnoser : IFailureDiagnoser
{
    public const int MaxBlockLines = 60;
    public const int MaxFailures = 5;

    public Diagnosis Diagnose(VerifyResult result, CandidateTest candidate, RepositoryMetadata metadata)
    {
        var (block, first) = result.State switch
        {
            VerificationState.Compile => CompileBlock(result, candidate.Code ?? ""),
            VerificationState.Unknown => UnknownBlock(result),
            VerificationState.Discovery => DiscoveryBlock(result, candidate, metadata),
            VerificationState.Runtime or VerificationState.Assertion => FailureBlock(result),
            VerificationState.Regression => RegressionBlock(result),
            _ => ("", null),
        };

        return new Diagnosis
        {
            State = result.State,
            Group = StateGroups.GroupOf(result.State),
            ErrorSignature = result.ErrorSignature,
            CodeHash = candidate.CodeHash ?? result.CodeHash,
            FailingMethods = result.State is VerificationState.Compile or VerificationState.Runtime or VerificationState.Assertion ? [.. result.FailingMethods] : [],
            ErrorBlock = Limit(block),
            FirstError = first,
        };
    }

    /// <summary>COMPILE: lỗi javac trong file sinh ra (không có thì mọi lỗi), kèm <c>symbol</c>/<c>location</c> và dòng mã tương ứng.</summary>
    private static (string, string?) CompileBlock(VerifyResult result, string code)
    {
        var errors = result.CompileErrors.Where(e => e.InGeneratedFile).ToList();
        if (errors.Count == 0)
        {
            errors = [.. result.CompileErrors];
        }

        var lines = code.Replace("\r\n", "\n").Split('\n');
        var text = new StringBuilder();
        foreach (var error in errors)
        {
            text.Append($"{error.File}:{error.Line}{(error.Column is { } c ? $":{c}" : "")}: error: {error.Message}\n");
            if (error.Symbol is not null)
            {
                text.Append($"  symbol: {(error.SymbolKind is null ? "" : error.SymbolKind + " ")}{error.Symbol}\n");
            }

            if (error.Location is not null)
            {
                text.Append($"  location: {error.Location}\n");
            }

            if (error.InGeneratedFile && error.Line >= 1 && error.Line <= lines.Length)
            {
                text.Append($"  code: {lines[error.Line - 1].Trim()}\n");
            }
        }

        var first = errors.FirstOrDefault() is { } e ? $"{e.File}:{e.Line}: {e.Message}" : null;
        return (text.ToString(), first);
    }

    /// <summary>UNKNOWN: khối <c>Failed to execute goal</c> / <c>What went wrong</c> kèm <c>Caused by:</c> (Verifier đã trích).</summary>
    private static (string, string?) UnknownBlock(VerifyResult result)
    {
        var block = result.BuildErrorExcerpt ?? "";
        return (block, block.Split('\n').FirstOrDefault(l => l.Trim().Length > 0)?.Trim());
    }

    /// <summary>DISCOVERY: số test được chạy (0) và tên class, file đã ghi.</summary>
    private static (string, string?) DiscoveryBlock(VerifyResult result, CandidateTest candidate, RepositoryMetadata metadata)
    {
        var framework = metadata.TestFramework.Name switch { "junit5" => "JUnit 5", "testng" => "TestNG", _ => "JUnit 4" };
        var testClass = candidate.TestFqn.Length > 0 ? candidate.TestFqn : metadata.Paths.GeneratedTestFqn;
        var file = result.TestFile ?? candidate.TestPath;
        return ($"The test runner executed 0 test methods of {testClass} (file {file}). The project runs tests with {framework}.\n", "0 tests executed");
    }

    /// <summary>RUNTIME/ASSERTION: mỗi method fail (tối đa 5): tên, exception, thông điệp, 8 dòng đầu stack trace.</summary>
    private static (string, string?) FailureBlock(VerifyResult result)
    {
        var text = new StringBuilder();
        foreach (var failure in result.Failures.Take(MaxFailures))
        {
            text.Append($"Test method: {(failure.Method.Length > 0 ? failure.Method : "(class)")}\n");
            text.Append($"Exception: {failure.Type}{(string.IsNullOrWhiteSpace(failure.Message) ? "" : ": " + OneLine(failure.Message))}\n");
            if (failure.TraceHead is { Length: > 0 } trace)
            {
                foreach (var line in trace.Split('\n').Skip(1))
                {
                    text.Append("  ").Append(line.Trim()).Append('\n');
                }
            }
        }

        if (result.Failures.Count > MaxFailures)
        {
            text.Append($"… and {result.Failures.Count - MaxFailures} more failing test methods\n");
        }

        var first = result.Failures.FirstOrDefault() is { } f ? $"{f.Type}{(string.IsNullOrWhiteSpace(f.Message) ? "" : ": " + OneLine(f.Message))}" : null;
        return (text.ToString(), first);
    }

    /// <summary>REGRESSION: test sẵn có bị hỏng (và test sinh ra fail khi chạy chung) kèm exception và thông điệp (tối đa 5).</summary>
    private static (string, string?) RegressionBlock(VerifyResult result)
    {
        var text = new StringBuilder("The generated test passes alone, but running the whole module together with it breaks these tests:\n");
        foreach (var regression in result.Regressions.Take(MaxFailures))
        {
            var reason = regression.Type == "NOT_RUN" ? "was not run (missing from the report)" : $"{regression.Type}{(string.IsNullOrWhiteSpace(regression.Message) ? "" : ": " + OneLine(regression.Message))}";
            text.Append($"- {regression.TestId}: {reason}\n");
        }

        if (result.Regressions.Count > MaxFailures)
        {
            text.Append($"… and {result.Regressions.Count - MaxFailures} more\n");
        }

        var first = result.Regressions.FirstOrDefault() is { } r ? $"{r.TestId}: {r.Type}" : null;
        return (text.ToString(), first);
    }

    private static string OneLine(string text)
    {
        var line = text.Replace("\r\n", "\n").Split('\n')[0].Trim();
        return line.Length > 300 ? line[..300] + "…" : line;
    }

    /// <summary>Tối đa 60 dòng (spec 03 Mục 5).</summary>
    public static string Limit(string block, int maxLines = MaxBlockLines)
    {
        var lines = block.TrimEnd('\n').Split('\n');
        return lines.Length <= maxLines ? string.Join('\n', lines) : string.Join('\n', lines.Take(maxLines)) + $"\n… ({lines.Length - maxLines} more lines)";
    }
}
