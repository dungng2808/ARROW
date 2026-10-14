using System.Globalization;
using System.Xml.Linq;
using Arrow.Models.Block4;

namespace Arrow.Service.Block4;

/// <summary>Tách một dòng CSV đơn giản (có ngoặc kép) thành các ô.</summary>
public static class Csv
{
    public static List<string> Split(string line)
    {
        var cells = new List<string>();
        var cell = new System.Text.StringBuilder();
        var quoted = false;
        for (var i = 0; i < line.Length; i++)
        {
            var c = line[i];
            if (quoted)
            {
                if (c == '"' && i + 1 < line.Length && line[i + 1] == '"')
                {
                    cell.Append('"');
                    i++;
                }
                else if (c == '"')
                {
                    quoted = false;
                }
                else
                {
                    cell.Append(c);
                }
            }
            else if (c == '"')
            {
                quoted = true;
            }
            else if (c == ',')
            {
                cells.Add(cell.ToString());
                cell.Clear();
            }
            else
            {
                cell.Append(c);
            }
        }

        cells.Add(cell.ToString());
        return cells;
    }

    public static List<string> Lines(string text) =>
        text.Replace("\r\n", "\n").Split('\n').Where(l => l.Trim().Length > 0).ToList();
}

/// <summary>
/// CSV của <c>jacococli report --csv</c> (spec 04 Mục 4.2 bước 3): chỉ lấy dòng có <c>PACKAGE</c> đúng package của CUT và <c>CLASS</c>
/// bằng đúng tên CUT hoặc bắt đầu bằng <c>&lt;tên CUT&gt;.</c> (class lồng); không lấy class tên gần giống (<c>OrderService</c> khi CUT là <c>Order</c>).
/// </summary>
public static class JacocoCsvParser
{
    private static readonly string[] Kinds = ["LINE", "BRANCH", "METHOD", "INSTRUCTION"];

    /// <summary>Null khi CUT không có trong báo cáo.</summary>
    public static CoverageResult? Parse(string csv, string cutFqn)
    {
        var lines = Csv.Lines(csv);
        if (lines.Count == 0)
        {
            return null;
        }

        var header = Csv.Split(lines[0]);
        int Column(string name) => header.FindIndex(h => h.Trim() == name);
        var package = cutFqn.Contains('.') ? cutFqn[..cutFqn.LastIndexOf('.')] : "";
        var simple = cutFqn[(cutFqn.LastIndexOf('.') + 1)..];
        var counters = new Dictionary<string, int>(StringComparer.Ordinal);
        var found = false;
        foreach (var row in lines.Skip(1).Select(Csv.Split))
        {
            var rowPackage = row[Column("PACKAGE")].Trim();
            var rowClass = row[Column("CLASS")].Trim();
            if (rowPackage != package || !(rowClass == simple || rowClass.StartsWith(simple + ".", StringComparison.Ordinal)))
            {
                continue;
            }

            found = true;
            foreach (var kind in Kinds)
            {
                foreach (var suffix in new[] { "_COVERED", "_MISSED" })
                {
                    var index = Column(kind + suffix);
                    if (index >= 0 && int.TryParse(row[index], NumberStyles.Integer, CultureInfo.InvariantCulture, out var value))
                    {
                        counters[kind + suffix] = counters.GetValueOrDefault(kind + suffix) + value;
                    }
                }
            }
        }

        if (!found)
        {
            return null;
        }

        double? Ratio(string kind)
        {
            var covered = counters.GetValueOrDefault(kind + "_COVERED");
            var total = covered + counters.GetValueOrDefault(kind + "_MISSED");
            return total == 0 ? null : Math.Round((double)covered / total, 4);
        }

        return new CoverageResult
        {
            Line = Ratio("LINE"),
            Branch = Ratio("BRANCH"),
            Method = Ratio("METHOD"),
            Counters = counters.Where(c => !c.Key.StartsWith("INSTRUCTION", StringComparison.Ordinal)).ToDictionary(c => c.Key, c => c.Value),
        };
    }
}

/// <summary><c>mutations.xml</c> của PIT (spec 04 Mục 4.3): đếm theo trạng thái, kiểm tra phạm vi mutant.</summary>
public static class PitXmlParser
{
    /// <summary>Kết quả và các class có mutant nằm ngoài <c>&lt;FQN&gt;</c>, <c>&lt;FQN&gt;$…</c>.</summary>
    public static (MutationResult Result, IReadOnlyList<string> OutOfScope) Parse(string xml, string cutFqn)
    {
        var document = XDocument.Parse(xml);
        var mutations = document.Descendants("mutation").ToList();
        int Count(string status) => mutations.Count(m => (string?)m.Attribute("status") == status);
        var outOfScope = mutations
            .Select(m => ((string?)m.Element("mutatedClass") ?? "").Trim())
            .Where(c => c != cutFqn && !c.StartsWith(cutFqn + "$", StringComparison.Ordinal))
            .Distinct(StringComparer.Ordinal)
            .ToList();

        var total = mutations.Count;
        var killed = Count("KILLED");
        var timedOut = Count("TIMED_OUT");
        var memory = Count("MEMORY_ERROR");
        var noCoverage = Count("NO_COVERAGE");
        var result = new MutationResult
        {
            Total = total,
            Killed = killed,
            Survived = Count("SURVIVED"),
            NoCoverage = noCoverage,
            TimedOut = timedOut,
            MemoryError = memory,
            RunError = Count("RUN_ERROR"),
            Score = total == 0 ? null : Math.Round((double)(killed + timedOut + memory) / total, 4),
            TestStrength = total - noCoverage == 0 ? null : Math.Round((double)killed / (total - noCoverage), 4),
        };
        return (result, outOfScope);
    }
}

/// <summary>
/// CSV kết quả của tsDetect (spec 04 Mục 4.4): một dòng cho file test, mỗi cột sau các cột thông tin là một loại smell
/// (giá trị số lần, hoặc <c>true</c>/<c>false</c> ở bản cũ).
/// </summary>
public static class TsDetectCsvParser
{
    private static readonly HashSet<string> InfoColumns = new(StringComparer.OrdinalIgnoreCase)
    {
        "App", "TestClass", "TestFilePath", "ProductionFilePath", "RelativeTestFilePath", "RelativeProductionFilePath", "NumberOfMethods",
    };

    public static SmellResult? Parse(string csv, int testMethods)
    {
        var lines = Csv.Lines(csv);
        if (lines.Count < 2)
        {
            return null;
        }

        var header = Csv.Split(lines[0]).Select(h => h.Trim()).ToList();
        var row = Csv.Split(lines[1]);
        var smells = new Dictionary<string, int>(StringComparer.Ordinal);
        for (var i = 0; i < header.Count && i < row.Count; i++)
        {
            if (InfoColumns.Contains(header[i]) || header[i].Length == 0)
            {
                continue;
            }

            var value = row[i].Trim();
            smells[header[i]] = value.Equals("true", StringComparison.OrdinalIgnoreCase) ? 1
                : int.TryParse(value, NumberStyles.Integer, CultureInfo.InvariantCulture, out var n) ? n
                : 0;
        }

        var total = smells.Values.Sum();
        return new SmellResult
        {
            TestMethods = testMethods,
            Smells = smells.Where(s => s.Value > 0).ToDictionary(s => s.Key, s => s.Value),
            TotalSmells = total,
            Density = testMethods == 0 ? null : Math.Round((double)total / testMethods, 4),
        };
    }
}
