using System.Globalization;

namespace Arrow.Core.Quality;

/// <summary><c>quality/&lt;system&gt;/jacoco.json</c> (specs/06 Mục 2.1, 3). Mẫu số 0 → null, không ghi 0.</summary>
public sealed record CoverageResult
{
    public string Status { get; init; } = "OK";
    public string? Reason { get; init; }
    public string ToolVersion { get; init; } = "0.8.12";
    public double? Line { get; init; }
    public double? Branch { get; init; }
    public double? Method { get; init; }
    public Dictionary<string, int> Counters { get; init; } = [];
    public double Seconds { get; init; }
    public string? Log { get; init; }
}

/// <summary><c>pit.json</c> (specs/06 Mục 2.2).</summary>
public sealed record MutationResult
{
    public string Status { get; init; } = "OK";
    public string? Reason { get; init; }
    public string ToolVersion { get; init; } = "1.17.4";
    public double? MutationScore { get; init; }
    public double? TestStrength { get; init; }
    public int Total { get; init; }
    public int Killed { get; init; }
    public Dictionary<string, int> ByStatus { get; init; } = [];
    public double Seconds { get; init; }
    public string? Log { get; init; }
}

/// <summary><c>tsdetect.json</c> (specs/06 Mục 2.3): mật độ smell = tổng smell / 1 bộ test.</summary>
public sealed record SmellResult
{
    public string Status { get; init; } = "OK";
    public string? Reason { get; init; }
    public string ToolVersion { get; init; } = "2.2";
    public int? TotalSmells { get; init; }
    public double? SmellDensity { get; init; }
    public Dictionary<string, int> BySmell { get; init; } = [];
    public int? TestMethods { get; init; }
    public int? Lines { get; init; }
    public double Seconds { get; init; }
    public string? Log { get; init; }
}

/// <summary><c>evosuite/evosuite_stats.json</c> (specs/06 Mục 1).</summary>
public sealed record EvoSuiteStats
{
    public string Status { get; init; } = "OK";
    public string? Reason { get; init; }
    public string Version { get; init; } = "1.2.0";
    public int JdkMajor { get; init; }
    public double GenerationSeconds { get; init; }
    public Dictionary<string, string> Statistics { get; init; } = [];
    public Dictionary<string, string> EffectiveDefaults { get; init; } = [];
    public List<string> DeclaredDeviations { get; init; } = [];
    public string? Log { get; init; }
}

/// <summary>Đọc báo cáo của JaCoCo, PIT, tsDetect. Code thuần.</summary>
public static class QualityParsers
{
    private static double? Ratio(int covered, int missed) => covered + missed == 0 ? null : Math.Round((double)covered / (covered + missed), 4);

    /// <summary>
    /// <c>jacoco.csv</c>: dòng của CUT (khớp package và class, gồm inner class <c>CUT.Inner</c>); cộng dồn các dòng.
    /// </summary>
    public static CoverageResult? ParseJacocoCsv(string csv, string cutFqn)
    {
        var package = cutFqn.Contains('.') ? cutFqn[..cutFqn.LastIndexOf('.')] : "";
        var simple = cutFqn[(cutFqn.LastIndexOf('.') + 1)..];
        var lines = csv.Replace("\r\n", "\n").Split('\n', StringSplitOptions.RemoveEmptyEntries);
        if (lines.Length < 2)
        {
            return null;
        }

        var header = lines[0].Split(',');
        int Column(string name) => Array.IndexOf(header, name);
        var counters = new Dictionary<string, int>();
        var found = false;
        foreach (var line in lines.Skip(1))
        {
            var cells = line.Split(',');
            if (cells.Length < header.Length || cells[Column("PACKAGE")] != package)
            {
                continue;
            }

            var cls = cells[Column("CLASS")];
            if (cls != simple && !cls.StartsWith(simple + ".", StringComparison.Ordinal) && !cls.StartsWith(simple + "$", StringComparison.Ordinal))
            {
                continue;
            }

            found = true;
            foreach (var name in new[] { "LINE_MISSED", "LINE_COVERED", "BRANCH_MISSED", "BRANCH_COVERED", "METHOD_MISSED", "METHOD_COVERED", "INSTRUCTION_MISSED", "INSTRUCTION_COVERED" })
            {
                counters[name] = counters.GetValueOrDefault(name) + int.Parse(cells[Column(name)], CultureInfo.InvariantCulture);
            }
        }

        if (!found)
        {
            return null;
        }

        return new CoverageResult
        {
            Line = Ratio(counters["LINE_COVERED"], counters["LINE_MISSED"]),
            Branch = Ratio(counters["BRANCH_COVERED"], counters["BRANCH_MISSED"]),
            Method = Ratio(counters["METHOD_COVERED"], counters["METHOD_MISSED"]),
            Counters = counters,
        };
    }

    private static readonly HashSet<string> Detected = ["KILLED", "TIMED_OUT", "MEMORY_ERROR", "RUN_ERROR"];

    /// <summary>
    /// <c>mutations.csv</c> của PIT (file, class, mutator, method, line, status, killing test): mutation score = bị diệt
    /// (KILLED, TIMED_OUT, MEMORY_ERROR, RUN_ERROR như PIT tính) / tổng mutant của CUT; test strength bỏ NO_COVERAGE.
    /// </summary>
    public static MutationResult? ParsePitCsv(string csv, string cutFqn)
    {
        var statuses = new Dictionary<string, int>();
        foreach (var line in csv.Replace("\r\n", "\n").Split('\n', StringSplitOptions.RemoveEmptyEntries))
        {
            var cells = line.Split(',');
            if (cells.Length < 6)
            {
                continue;
            }

            var cls = cells[1];
            if (cls != cutFqn && !cls.StartsWith(cutFqn + "$", StringComparison.Ordinal))
            {
                continue;
            }

            statuses[cells[5]] = statuses.GetValueOrDefault(cells[5]) + 1;
        }

        statuses.Remove("NON_VIABLE");
        var total = statuses.Values.Sum();
        if (total == 0)
        {
            return null;
        }

        var killed = statuses.Where(s => Detected.Contains(s.Key)).Sum(s => s.Value);
        var covered = total - statuses.GetValueOrDefault("NO_COVERAGE");
        return new MutationResult
        {
            Total = total,
            Killed = killed,
            MutationScore = Math.Round((double)killed / total, 4),
            TestStrength = covered == 0 ? null : Math.Round((double)killed / covered, 4),
            ByStatus = statuses,
        };
    }

    private static readonly HashSet<string> NonSmellColumns = new(StringComparer.OrdinalIgnoreCase)
    {
        "App", "TestClass", "TestFilePath", "ProductionFilePath", "RelativeTestFilePath", "RelativeProductionFilePath", "NumberOfMethods",
    };

    /// <summary>CSV kết quả của tsDetect: mỗi cột smell là số lần phát hiện trong class test.</summary>
    public static SmellResult? ParseTsDetectCsv(string csv)
    {
        var lines = csv.Replace("\r\n", "\n").Split('\n', StringSplitOptions.RemoveEmptyEntries);
        if (lines.Length < 2)
        {
            return null;
        }

        var header = lines[0].Split(',').Select(h => h.Trim()).ToArray();
        var cells = lines[1].Split(',').Select(c => c.Trim()).ToArray();
        var bySmell = new Dictionary<string, int>();
        int? methods = null;
        for (var i = 0; i < Math.Min(header.Length, cells.Length); i++)
        {
            if (header[i].Equals("NumberOfMethods", StringComparison.OrdinalIgnoreCase) && int.TryParse(cells[i], out var m))
            {
                methods = m;
            }

            if (NonSmellColumns.Contains(header[i]))
            {
                continue;
            }

            if (int.TryParse(cells[i], NumberStyles.Integer, CultureInfo.InvariantCulture, out var value))
            {
                bySmell[header[i]] = value;
            }
            else if (bool.TryParse(cells[i], out var flag))
            {
                bySmell[header[i]] = flag ? 1 : 0;
            }
        }

        var total = bySmell.Values.Sum();
        return new SmellResult { TotalSmells = total, SmellDensity = total, BySmell = bySmell, TestMethods = methods };
    }
}
