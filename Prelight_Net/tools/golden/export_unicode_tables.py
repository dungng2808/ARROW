"""Khóa tập ký tự regex theo Python 3.11 thay vì Unicode của runtime .NET."""
import re
from pathlib import Path


def ranges(values):
    result = []
    for value in values:
        if result and value == result[-1][1] + 1:
            result[-1][1] = value
        else:
            result.append([value, value])
    return result


def char_class(values):
    return "[" + "".join(f"\\u{start:04x}" + (f"-\\u{end:04x}" if start != end else "") for start, end in ranges(values)) + "]"


def patterns(pattern):
    regex = re.compile(pattern)
    bmp, extra = [], {}
    for code in range(0x110000):
        if regex.fullmatch(chr(code)):
            if code <= 0xffff:
                bmp.append(code)
            else:
                high, low = 0xd800 + ((code - 0x10000) >> 10), 0xdc00 + ((code - 0x10000) & 1023)
                extra.setdefault(high, []).append(low)
    return char_class(bmp), "(?:" + "|".join(f"\\u{high:04x}" + char_class(lows) for high, lows in sorted(extra.items())) + ")"


def unicode_cases():
    values = ["𐐀", "𝔘", "𝟘", "𠀀", "\u0301", "\u200c", "²", "\u1c89", "\U00011f02", "\U0001e4d0"]
    for value in values:
        for pattern in (r"\w+", r"\d+", r"@[\w.]+(?:\([^)]*\))?", r"\b(?:class|interface)\s+(\w+)", r"\bpublic\b"):
            text = f"@{value} public class {value} {{}}"
            yield {"pattern": pattern, "text": text}, re.findall(pattern, text)


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[2]
    lines = ["// Sinh bằng Python 3.11 re; không sửa bằng tay.", "namespace Preflight.Service.Core.PyCompat;", "internal static class PythonUnicodeData", "{"]
    for name, pattern in (("Word", r"\w"), ("Digit", r"\d")):
        bmp, extra = patterns(pattern)
        lines += [f'    internal const string {name}Bmp = @"{bmp}";', f'    internal const string {name}Extra = @"{extra}";']
    lines.append("}")
    (root / "src/Preflight.Service/Core/PyCompat/PythonUnicodeData.Generated.cs").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
