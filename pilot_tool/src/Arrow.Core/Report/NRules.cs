namespace Arrow.Core.Report;

/// <summary>Quy tắc chọn N từ đường cộng dồn chung của hai cơ chế (specs/07 Mục 2). Code thuần.</summary>
public static class NRules
{
    /// <summary><c>S(k)</c> = số chuỗi đạt VALID ở vòng ≤ k, k = 1…nMax, từ vòng thành công của từng chuỗi (null = không thành công).</summary>
    public static int[] Cumulative(IEnumerable<int?> successRounds, int nMax = 10)
    {
        var rounds = successRounds.ToList();
        return Enumerable.Range(1, nMax).Select(k => rounds.Count(r => r is not null && r <= k)).ToArray();
    }

    /// <summary>
    /// [D8] share: <c>S(10) = 0</c> → 10; ngược lại <c>max(3, min{k : S(k) ≥ threshold × S(10)})</c>.
    /// </summary>
    public static int Share(IReadOnlyList<int> s, double threshold = 0.95, int minimum = 3)
    {
        var last = s[^1];
        if (last == 0)
        {
            return s.Count;
        }

        for (var k = 1; k <= s.Count; k++)
        {
            if (s[k - 1] >= threshold * last)
            {
                return Math.Max(minimum, k);
            }
        }

        return s.Count;
    }

    /// <summary>
    /// <c>report2</c>: vòng k nhỏ nhất (5 ≤ k ≤ 10) mà vòng k−1 và vòng k không thêm ca nào
    /// (<c>S(k−1) = S(k−2)</c> và <c>S(k) = S(k−1)</c>, <c>S(0) = 0</c>); không có thì 10.
    /// </summary>
    public static int Report2(IReadOnlyList<int> s, int minimum = 5)
    {
        int At(int k) => k <= 0 ? 0 : s[k - 1];
        for (var k = minimum; k <= s.Count; k++)
        {
            if (At(k - 1) == At(k - 2) && At(k) == At(k - 1))
            {
                return k;
            }
        }

        return s.Count;
    }

    public static int Apply(string rule, IReadOnlyList<int> s) => rule switch
    {
        "share95" => Share(s, 0.95),
        "report2" => Report2(s),
        _ => throw new ArgumentException($"Quy tắc N không biết: {rule}"),
    };
}
