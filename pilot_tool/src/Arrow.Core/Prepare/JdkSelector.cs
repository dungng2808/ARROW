using System.Globalization;
using System.Text.RegularExpressions;
using Arrow.Core.Manifest;

namespace Arrow.Core.Prepare;

/// <summary>JDK đã chọn. <see cref="Reason"/>: <c>declared_level</c> | <c>manifest</c> | <c>manifest_fallback</c>, thêm <c>+release_flag</c> khi đổi 8 → 11.</summary>
public sealed record JdkChoice(int Major, string Home, string Reason);

/// <summary>
/// Chọn JDK cho một task (specs/02 §3, chốt 13/10/2026). Preflight chỉ biên dịch, không chạy test, và chỉ đọc
/// <c>&lt;properties&gt;</c> của POM module, nên nhiều task khai Java 7/8 bị gán JDK 17; test cũ (Mockito, EqualsVerifier)
/// fail khi chạy trên JDK mới. Vì vậy ưu tiên mức Java khai trong file build; baseline biên dịch lỗi thì quay về JDK của manifest.
/// </summary>
public static partial class JdkSelector
{
    /// <summary>Dấu hiệu module biên dịch bằng <c>--release</c>, mà javac của JDK 8 không có.</summary>
    [GeneratedRegex(@"options\.release|\brelease\.set\(|--release|<release>|maven\.compiler\.release")]
    public static partial Regex ReleaseFlag();

    /// <summary>JDK major dùng cho một mức Java khai báo: ≤ 8 → 8, 9–11 → 11, 12–17 → 17, còn lại → 21.</summary>
    public static int JdkForLevel(int level) => level <= 8 ? 8 : level <= 11 ? 11 : level <= 17 ? 17 : 21;

    /// <summary>Mức Java khai trong file build (module trước, rồi thư mục cha): <c>release</c>, rồi <c>target</c>, rồi <c>source</c>.</summary>
    public static int? DeclaredLevel(BuildToolKind tool, IReadOnlyList<string> buildFileTexts)
    {
        var levels = DependencyParser.JavaLevels(0, tool, buildFileTexts);
        var value = levels.Release ?? levels.Target ?? levels.Source;
        return int.TryParse(value, NumberStyles.Integer, CultureInfo.InvariantCulture, out var level) ? level : null;
    }

    /// <param name="buildFileTexts">Nội dung file build của module và tối đa 3 thư mục cha, module trước.</param>
    public static JdkChoice Select(string manifestJavaVersion, BuildToolKind tool, IReadOnlyList<string> buildFileTexts, IReadOnlyDictionary<string, string> jdkHomes)
    {
        var declared = DeclaredLevel(tool, buildFileTexts);
        return declared is { } level
            ? Resolve(JdkForLevel(level), "declared_level", buildFileTexts, jdkHomes)
            : FromManifest(manifestJavaVersion, buildFileTexts, jdkHomes, "manifest");
    }

    /// <summary>
    /// JDK ứng viên theo thứ tự thử ở baseline: JDK chọn theo <see cref="Select"/>, JDK của manifest (<c>manifest_fallback</c>),
    /// rồi các JDK còn lại của máy từ thấp tới cao (<c>fallback</c>). Bỏ JDK trùng và JDK máy không có.
    /// </summary>
    public static List<JdkChoice> Candidates(string manifestJavaVersion, BuildToolKind tool, IReadOnlyList<string> buildFileTexts, IReadOnlyDictionary<string, string> jdkHomes)
    {
        var candidates = new List<JdkChoice>();
        void TryAdd(Func<JdkChoice> choose)
        {
            try
            {
                var choice = choose();
                if (candidates.All(c => c.Major != choice.Major))
                {
                    candidates.Add(choice);
                }
            }
            catch (PrepareException ex) when (ex.Reason == "JDK_MISSING")
            {
            }
        }

        TryAdd(() => Select(manifestJavaVersion, tool, buildFileTexts, jdkHomes));
        TryAdd(() => FromManifest(manifestJavaVersion, buildFileTexts, jdkHomes));
        foreach (var major in jdkHomes.Keys.Select(k => int.TryParse(k, out var m) ? m : 0).Where(m => m > 0).Order())
        {
            TryAdd(() => new JdkChoice(major, jdkHomes[major.ToString(CultureInfo.InvariantCulture)], "fallback"));
        }

        return candidates.Count > 0
            ? candidates
            : throw new PrepareException("JDK_MISSING", "Máy chưa khai báo JDK nào (chạy lại arrow setup, bước JDK)");
    }

    /// <summary>JDK preflight đã biên dịch thành công (bước dự phòng khi JDK khai báo không biên dịch được).</summary>
    public static JdkChoice FromManifest(string manifestJavaVersion, IReadOnlyList<string> buildFileTexts, IReadOnlyDictionary<string, string> jdkHomes, string reason = "manifest_fallback")
    {
        var major = ManifestRecord.ParseJdkMajor(manifestJavaVersion)
                    ?? throw new PrepareException("JDK_UNKNOWN", $"Không đọc được phiên bản Java từ manifest: {manifestJavaVersion}");
        return Resolve(major, reason, buildFileTexts, jdkHomes);
    }

    private static JdkChoice Resolve(int major, string reason, IReadOnlyList<string> buildFileTexts, IReadOnlyDictionary<string, string> jdkHomes)
    {
        if (major == 8 && buildFileTexts.Any(text => ReleaseFlag().IsMatch(text)))
        {
            major = 11;
            reason += "+release_flag";
        }

        if (!jdkHomes.TryGetValue(major.ToString(CultureInfo.InvariantCulture), out var home) || string.IsNullOrEmpty(home))
        {
            throw new PrepareException("JDK_MISSING", $"Máy chưa khai báo JDK {major} (chạy lại arrow setup, bước JDK)");
        }

        return new JdkChoice(major, home, reason);
    }
}

/// <summary>Lỗi hạ tầng của bước chuẩn bị, mang mã lý do ghi vào ledger (<c>reason</c>).</summary>
public sealed class PrepareException(string reason, string message) : Exception(message)
{
    public string Reason { get; } = reason;
}
