using System.Text.RegularExpressions;
using Arrow.Models.Block1;
using Arrow.Models.Manifest;

namespace Arrow.Service.Block1;

/// <summary>Tách số major từ <c>java_version</c> của manifest (spec 01 Mục 4.3 bước 2).</summary>
public static partial class ManifestJavaVersionParser
{
    [GeneratedRegex("version\\s+\"([^\"]+)\"")]
    private static partial Regex Quoted();

    /// <summary><c>openjdk version "1.8.0_504"</c> → 8; <c>"17.0.20.1" 2026-08-18 LTS</c> → 17; <c>"21"</c> → 21; không tách được → null.</summary>
    public static int? ParseMajor(string? javaVersion)
    {
        if (string.IsNullOrWhiteSpace(javaVersion))
        {
            return null;
        }

        var match = Quoted().Match(javaVersion);
        var text = match.Success ? match.Groups[1].Value : javaVersion.Trim();
        return ParseLevel(text);
    }

    /// <summary>Mức Java dạng <c>1.7</c>, <c>1.8</c>, <c>11</c>, <c>17.0.2</c>, <c>JavaVersion.VERSION_1_8</c>, <c>VERSION_17</c> → số major.</summary>
    public static int? ParseLevel(string? level)
    {
        if (string.IsNullOrWhiteSpace(level))
        {
            return null;
        }

        var text = level.Trim();
        var version = text.LastIndexOf("VERSION_", StringComparison.Ordinal);
        if (version >= 0)
        {
            text = text[(version + "VERSION_".Length)..].Replace('_', '.');
        }

        var parts = text.Split('.', '_', '-', '+', ' ');
        if (!int.TryParse(parts[0], out var first))
        {
            return null;
        }

        if (first == 1 && parts.Length > 1 && int.TryParse(parts[1], out var second))
        {
            return second;
        }

        return first;
    }
}

/// <summary>
/// Build root, chế độ và bộ chọn module lấy từ <b>lần biên dịch test thành công</b> trong <c>build_attempts</c> (spec 01 Mục 4.1 bước 1).
/// </summary>
public static class BuildRootRule
{
    /// <summary>Thứ tự ưu tiên các lần thử biên dịch test.</summary>
    public static readonly IReadOnlyList<string> StagePriority = ["test_compile", "test_compile_fallback", "probe_compile", "probe_compile_fallback"];

    public static BuildPlan? FromManifest(TaskRecord task)
    {
        foreach (var stage in StagePriority)
        {
            var attempt = task.BuildAttempts.FirstOrDefault(a => a.Stage == stage && a.ExitCode == 0 && a.TimedOut != true);
            if (attempt is null)
            {
                continue;
            }

            var buildRoot = RelativeTo(task.WorkingDirectory, attempt.WorkingDirectory);
            if (buildRoot is null)
            {
                continue;
            }

            var tool = ToolOf(attempt.Command, task.BuildTool);
            return tool == "gradle"
                ? new BuildPlan { Tool = tool, BuildRoot = buildRoot, Mode = BuildMode.Reactor, Module = GradleProject(attempt.Command), Source = $"manifest:{stage}" }
                : MavenPlan(attempt.Command, buildRoot, $"manifest:{stage}");
        }

        return null;
    }

    private static BuildPlan MavenPlan(IReadOnlyList<string> command, string buildRoot, string source)
    {
        var index = command.ToList().FindIndex(c => c is "-pl" or "--projects");
        var module = index >= 0 && index + 1 < command.Count ? NormalizeRelative(command[index + 1]) : null;
        return new BuildPlan
        {
            Tool = "maven",
            BuildRoot = buildRoot,
            Mode = module is null ? BuildMode.Standalone : BuildMode.Reactor,
            Module = module,
            Source = source,
        };
    }

    /// <summary>Tên công cụ theo chương trình trong lệnh (<c>mvn</c>, <c>mvnw.cmd</c>, <c>…/gradlew</c>); không rõ thì theo manifest.</summary>
    public static string ToolOf(IReadOnlyList<string> command, string manifestTool)
    {
        var program = command.Count > 0 ? FileName(command[0]).ToLowerInvariant() : "";
        if (program.StartsWith("gradle", StringComparison.Ordinal))
        {
            return "gradle";
        }

        if (program.StartsWith("mvn", StringComparison.Ordinal))
        {
            return "maven";
        }

        return manifestTool == "gradle" ? "gradle" : "maven";
    }

    /// <summary>Đường dẫn project Gradle từ task trong lệnh: <c>:a:b:testClasses</c> → <c>:a:b</c>; task không có đường dẫn → project gốc <c>:</c>.</summary>
    public static string GradleProject(IReadOnlyList<string> command)
    {
        var task = command.Skip(1).LastOrDefault(c => !c.StartsWith('-') && c.EndsWith("Classes", StringComparison.Ordinal))
                   ?? command.Skip(1).LastOrDefault(c => !c.StartsWith('-'));
        if (task is null)
        {
            return ":";
        }

        var last = task.LastIndexOf(':');
        return last <= 0 ? ":" : task[..last];
    }

    /// <summary>
    /// <paramref name="child"/> tính từ <paramref name="root"/>, dùng <c>/</c>; chấp nhận đường dẫn Windows (<c>D:\…</c>) của máy chạy preflight.
    /// Không nằm trong <paramref name="root"/> → null.
    /// </summary>
    public static string? RelativeTo(string root, string child)
    {
        var r = Normalize(root);
        var c = Normalize(child);
        if (string.Equals(r, c, StringComparison.OrdinalIgnoreCase))
        {
            return ".";
        }

        return c.StartsWith(r + "/", StringComparison.OrdinalIgnoreCase) ? c[(r.Length + 1)..] : null;
    }

    /// <summary>Đường dẫn tương đối dùng <c>/</c>, bỏ <c>./</c> và dấu <c>/</c> thừa; rỗng → <c>.</c>.</summary>
    public static string NormalizeRelative(string path)
    {
        var parts = path.Replace('\\', '/').Split('/', StringSplitOptions.RemoveEmptyEntries).Where(p => p != ".").ToList();
        return parts.Count == 0 ? "." : string.Join('/', parts);
    }

    /// <summary>Ghép đường dẫn tương đối: <c>Join(".", "core")</c> → <c>core</c>.</summary>
    public static string Join(string left, string right) => NormalizeRelative($"{left}/{right}");

    private static string Normalize(string path) => path.Replace('\\', '/').TrimEnd('/');

    private static string FileName(string path)
    {
        var normalized = path.Replace('\\', '/');
        return normalized[(normalized.LastIndexOf('/') + 1)..];
    }

    /// <summary>
    /// Module root dự kiến theo kế hoạch build: standalone = build root; Maven reactor = build root + <c>-pl</c>;
    /// Gradle = thư mục project đọc được từ Gradle (truyền vào), nếu không có thì đoán <c>:a:b</c> → <c>a/b</c>.
    /// </summary>
    public static string ExpectedModuleRoot(BuildPlan plan, string? gradleProjectDir = null)
    {
        if (plan.Tool == "gradle")
        {
            return gradleProjectDir ?? Join(plan.BuildRoot, (plan.Module ?? ":").Trim(':').Replace(':', '/'));
        }

        return plan.Mode == BuildMode.Reactor && plan.Module is not null ? Join(plan.BuildRoot, plan.Module) : NormalizeRelative(plan.BuildRoot);
    }
}

/// <summary>Kết quả chọn JDK: thứ tự thử (JDK đầu là lựa chọn chính) và lý do.</summary>
public sealed record JdkPlan(IReadOnlyList<int> Order, string Reason);

/// <summary>Chọn JDK (spec 01 Mục 4.3 bước 3–4).</summary>
public static class JdkSelector
{
    public static readonly IReadOnlyList<int> Supported = [8, 11, 17, 21];

    /// <summary>
    /// Lựa chọn chính: JDK nhỏ nhất có major ≥ mức khai báo (khai báo &lt; 8 thì 8); không có mức khai báo → JDK của manifest.
    /// Dự phòng khi biên dịch lỗi: JDK của manifest, rồi các JDK còn lại theo thứ tự tăng dần.
    /// </summary>
    public static JdkPlan Select(int? declaredMajor, int? manifestMajor, IReadOnlyCollection<int> available)
    {
        var sorted = available.Distinct().OrderBy(m => m).ToList();
        if (sorted.Count == 0)
        {
            return new JdkPlan([], "none");
        }

        int first;
        string reason;
        if (declaredMajor is { } declared)
        {
            var wanted = Math.Max(declared, 8);
            first = sorted.FirstOrDefault(m => m >= wanted, sorted[^1]);
            reason = "declared";
        }
        else if (manifestMajor is { } manifest && sorted.Contains(manifest))
        {
            first = manifest;
            reason = "manifest";
        }
        else
        {
            first = manifestMajor is { } m ? sorted.FirstOrDefault(j => j >= m, sorted[^1]) : sorted.Contains(17) ? 17 : sorted[^1];
            reason = "manifest";
        }

        var order = new List<int> { first };
        if (manifestMajor is { } fromManifest && sorted.Contains(fromManifest) && !order.Contains(fromManifest))
        {
            order.Add(fromManifest);
        }

        order.AddRange(sorted.Where(m => !order.Contains(m)));
        return new JdkPlan(order, reason);
    }

    /// <summary>Lý do ghi vào M khi JDK dùng được không phải lựa chọn chính: <c>fallback:17,8</c> (các JDK đã thử, theo thứ tự).</summary>
    public static string FallbackReason(IEnumerable<int> tried) => "fallback:" + string.Join(',', tried);
}
