using System.Text.Json;
using Arrow.Core.Prompts;
using Arrow.Core.Util;
using Arrow.Infrastructure.Git;
using Arrow.Infrastructure.Java;

namespace Arrow.Infrastructure.Generation;

/// <summary>
/// Chọn 2 ví dụ few-shot cố định cho mọi CUT (specs/04 Mục 5, D4): một JUnit 4, một JUnit 5, từ pool Classes2Test
/// technical_eligible + CONTENT_MATCHED, bỏ mọi repository của các manifest đã dùng (tập đánh giá, pilot, dự bị).
/// Thứ tự thử: danh sách đã sắp theo task_id, xáo bằng <c>Random(seed)</c>; lấy ứng viên đầu tiên đạt điều kiện.
/// </summary>
public sealed class FewShotSelector(GitRepositoryProvider git, JavaAnalyzer analyzer)
{
    public const string Rule =
        "pool: technical_eligible + CONTENT_MATCHED of Classes2Test preflight, minus every repository of the excluded manifests; " +
        "per framework (junit4, junit5): candidates sorted by task_id, shuffled with System.Random(seed) (Fisher-Yates), first candidate that " +
        "meets all conditions is chosen: CUT 30-150 lines, human test 20-120 lines, >= 2 test methods and >= 2 assertions (java-analyzer), " +
        "no Mockito, no external service or container markers in the test (Spring/Micronaut/Quarkus context, @ExtendWith, @RunWith, injection, " +
        "Testcontainers, JDBC, SQL, database, HTTP URL, sockets, temporary folders, environment variables, sleeps)";

    private static readonly string[] ExternalMarkers =
    [
        "mockito", "Mockito", "@SpringBootTest", "SpringRunner", "SpringJUnit4ClassRunner", "@Autowired", "@ExtendWith(SpringExtension", "Testcontainers",
        "@Container", "jdbc:", "DataSource", "http://", "https://", "Socket", "TemporaryFolder", "@TempDir", "@Rule", "System.getenv", "Thread.sleep",
        "@Inject", "@MicronautTest", "@QuarkusTest", "@ExtendWith", "@RunWith", "@DataJpaTest", "@WebMvcTest", "EntityManager", ".sql", "Database",
    ];

    public async Task<FewShotSet> SelectAsync(IEnumerable<string> poolFiles, IEnumerable<string> excludedManifests, int seed, Action<string> log, CancellationToken cancellationToken)
    {
        var excludedList = excludedManifests.ToList();
        var excludedRepos = new HashSet<string>(StringComparer.Ordinal);
        foreach (var manifest in excludedList)
        {
            foreach (var line in File.ReadLines(manifest).Where(l => l.Trim().Length > 0))
            {
                using var document = JsonDocument.Parse(line);
                excludedRepos.Add(document.RootElement.GetProperty("repo_url").GetString()!);
            }
        }

        var candidates = new List<Dictionary<string, string>>();
        foreach (var file in poolFiles.OrderBy(f => f, StringComparer.Ordinal))
        {
            foreach (var line in File.ReadLines(file).Where(l => l.Trim().Length > 0))
            {
                var record = Flatten(line);
                if (record.GetValueOrDefault("technical_eligible")?.ToLowerInvariant() == "true"
                    && record.GetValueOrDefault("revision_verification_status") == "CONTENT_MATCHED"
                    && !excludedRepos.Contains(record["repo_url"]))
                {
                    candidates.Add(record);
                }
            }
        }

        var examples = new List<FewShotExample>();
        foreach (var framework in new[] { "junit4", "junit5" })
        {
            var ordered = candidates.Where(c => c.GetValueOrDefault("testing_framework") == framework)
                .GroupBy(c => c["task_id"]).Select(g => g.First())
                .OrderBy(c => c["task_id"], StringComparer.Ordinal).ToList();
            var random = new Random(seed);
            for (var i = ordered.Count - 1; i > 0; i--)
            {
                var j = random.Next(i + 1);
                (ordered[i], ordered[j]) = (ordered[j], ordered[i]);
            }

            FewShotExample? chosen = null;
            foreach (var candidate in ordered.Take(300))
            {
                cancellationToken.ThrowIfCancellationRequested();
                var (example, reason) = await TryAsync(candidate, cancellationToken).ConfigureAwait(false);
                log($"{framework} {candidate["task_id"]}: {reason}");
                if (example is not null)
                {
                    chosen = example;
                    break;
                }
            }

            examples.Add(chosen ?? throw new ArrowException("FEWSHOT_NONE", $"Không có ứng viên {framework} đạt điều kiện"));
        }

        return new FewShotSet
        {
            Rule = Rule,
            Seed = seed,
            ExcludedManifests = excludedList.Select(Path.GetFileName).Select(n => n!).ToList(),
            Candidates = candidates.Count,
            Examples = examples,
        };
    }

    private async Task<(FewShotExample? Example, string Reason)> TryAsync(Dictionary<string, string> candidate, CancellationToken cancellationToken)
    {
        string cut, test;
        try
        {
            cut = await git.ReadFileAsync(candidate["repo_url"], candidate["checkout_sha"], candidate["class_path"], cancellationToken).ConfigureAwait(false);
            test = await git.ReadFileAsync(candidate["repo_url"], candidate["checkout_sha"], candidate["test_class_path"], cancellationToken).ConfigureAwait(false);
        }
        catch (Exception ex) when (ex is Arrow.Core.Prepare.PrepareException or ArrowException)
        {
            return (null, "không tải được: " + ex.Message);
        }

        var cutLines = cut.TrimEnd().Split('\n').Length;
        var testLines = test.TrimEnd().Split('\n').Length;
        if (cutLines is < 30 or > 150)
        {
            return (null, $"CUT {cutLines} dòng");
        }

        if (testLines is < 20 or > 120)
        {
            return (null, $"test {testLines} dòng");
        }

        var marker = ExternalMarkers.FirstOrDefault(m => test.Contains(m, StringComparison.Ordinal));
        if (marker is not null)
        {
            return (null, $"test có '{marker}'");
        }

        var analysis = await analyzer.AnalyzeTestAsync(test, [], cancellationToken).ConfigureAwait(false);
        if (!analysis.Parsed || analysis.TestMethods.Count < 2 || (analysis.AssertionCount ?? 0) < 2)
        {
            return (null, $"test có {analysis.TestMethods.Count} test method, {analysis.AssertionCount} assertion");
        }

        return (new FewShotExample
        {
            TaskId = candidate["task_id"],
            RepoUrl = candidate["repo_url"],
            CheckoutSha = candidate["checkout_sha"],
            TestingFramework = candidate["testing_framework"],
            CutPath = candidate["class_path"],
            TestPath = candidate["test_class_path"],
            CutSource = cut,
            TestSource = test,
            CutSha256 = Hashing.Sha256Text(cut),
            TestSha256 = Hashing.Sha256Text(test),
        }, $"chọn (CUT {cutLines} dòng, test {testLines} dòng, {analysis.TestMethods.Count} test method, {analysis.AssertionCount} assertion)");
    }

    private static Dictionary<string, string> Flatten(string line)
    {
        using var document = JsonDocument.Parse(line);
        var result = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var property in document.RootElement.EnumerateObject())
        {
            result[property.Name] = property.Value.ValueKind == JsonValueKind.String ? property.Value.GetString()! : property.Value.GetRawText();
        }

        return result;
    }
}
