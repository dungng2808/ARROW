using System.Collections.Concurrent;
using System.Text.RegularExpressions;

namespace Arrow.DataAccess.Common;

/// <summary>
/// Che bí mật trước khi ghi log hay file kết quả (spec 00 Mục 6): khóa API, <c>master_key</c>, <c>api_base</c> đầy đủ.
/// Hai lớp: (1) giá trị đã đăng ký khi đọc cấu hình (khóa thật, api_base thật); (2) mẫu nhận dạng chung.
/// </summary>
public static partial class Redactor
{
    private static readonly ConcurrentDictionary<string, string> Registered = new(StringComparer.Ordinal);

    /// <summary>Đăng ký một bí mật; <paramref name="replacement"/> mặc định là <c>***</c> + 4 ký tự cuối.</summary>
    public static void Register(string secret, string? replacement = null)
    {
        if (string.IsNullOrEmpty(secret) || secret.Length < 6)
        {
            return;
        }

        Registered[secret] = replacement ?? Mask(secret);
    }

    /// <summary>Che, chỉ giữ 4 ký tự cuối.</summary>
    public static string Mask(string secret) => secret.Length <= 4 ? "***" : "***" + secret[^4..];

    public static string Redact(string text)
    {
        if (string.IsNullOrEmpty(text))
        {
            return text;
        }

        foreach (var (secret, replacement) in Registered.OrderByDescending(p => p.Key.Length))
        {
            text = text.Replace(secret, replacement, StringComparison.Ordinal);
        }

        text = SkKey().Replace(text, m => "sk-***" + m.Value[^4..]);
        text = KeyValue().Replace(text, m => m.Groups[1].Value + m.Groups[2].Value + "***");
        text = Bearer().Replace(text, "Bearer ***");
        return text;
    }

    [GeneratedRegex(@"sk-[A-Za-z0-9_\-]{12,}")]
    private static partial Regex SkKey();

    [GeneratedRegex(@"(?i)\b(api_key|master_key|apikey|x-api-key)(""?\s*[:=]\s*""?)(?!\*\*\*)[^\s"",}]+")]
    private static partial Regex KeyValue();

    [GeneratedRegex(@"(?i)Bearer\s+[A-Za-z0-9._\-]{8,}")]
    private static partial Regex Bearer();
}
