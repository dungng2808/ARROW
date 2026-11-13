using System.Text;

namespace Preflight.DataAccess;

/// <summary>So sánh code point như sorted của Python, kể cả ngoài BMP.</summary>
public sealed class PythonCodePointComparer : IComparer<string>
{
    public static PythonCodePointComparer Instance { get; } = new();
    public int Compare(string? x, string? y)
    {
        if (ReferenceEquals(x, y)) return 0;
        if (x is null) return -1; if (y is null) return 1;
        var a = x.EnumerateRunes().GetEnumerator(); var b = y.EnumerateRunes().GetEnumerator();
        while (true)
        {
            bool left = a.MoveNext(), right = b.MoveNext();
            if (!left || !right) return left.CompareTo(right);
            int result = a.Current.Value.CompareTo(b.Current.Value); if (result != 0) return result;
        }
    }
}
