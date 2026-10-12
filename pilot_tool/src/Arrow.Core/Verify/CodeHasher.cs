using System.Text.RegularExpressions;
using Arrow.Core.Util;

namespace Arrow.Core.Verify;

/// <summary>
/// Code hash (specs/03 Mục 7): SHA-256 của mã đã bỏ comment (java-analyzer, theo AST), xuống dòng LF,
/// mọi khoảng trắng liên tiếp gộp thành một dấu cách, bỏ khoảng trắng đầu/cuối.
/// </summary>
public static partial class CodeHasher
{
    public static string Normalize(string codeWithoutComments) =>
        Whitespace().Replace(codeWithoutComments.Replace("\r\n", "\n"), " ").Trim();

    public static string Hash(string codeWithoutComments) => "sha256:" + Hashing.Sha256Text(Normalize(codeWithoutComments));

    [GeneratedRegex(@"\s+")]
    private static partial Regex Whitespace();
}
