namespace Arrow.Core.Util;

/// <summary>Lỗi có mã, để in ra người dùng và ghi vào log (ví dụ MANIFEST_HASH_MISMATCH).</summary>
public sealed class ArrowException(string code, string message) : Exception(message)
{
    public string Code { get; } = code;

    public override string ToString() => $"{Code}: {Message}";
}
