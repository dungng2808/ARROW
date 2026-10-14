namespace Arrow.UI;

/// <summary>Hết dữ liệu nhập (ví dụ input được chuyển hướng từ file và đã đọc hết).</summary>
public sealed class InputClosedException() : Exception("Hết dữ liệu nhập");

/// <summary>
/// Nhập liệu của tầng UI (spec 06 Mục 1): mọi câu hỏi đều đọc bằng <see cref="TextReader.ReadLine"/>.
/// Enter để trống = giữ giá trị mặc định trong ngoặc vuông.
/// </summary>
public sealed class ConsoleInput(TextReader input, TextWriter output)
{
    public static ConsoleInput FromConsole() => new(Console.In, Console.Out);

    private string ReadLine() => input.ReadLine() ?? throw new InputClosedException();

    /// <summary>Một dòng nhập thô (null khi hết dữ liệu), dùng cho lệnh gõ trong lúc run đang chạy (S5).</summary>
    public string? TryReadLine() => input.ReadLine();

    /// <summary>In <c>Nhấn Enter để về menu</c> và chờ một dòng (spec 06 Mục 3).</summary>
    public void Pause()
    {
        output.Write("Nhấn Enter để về menu");
        output.WriteLine();
        input.ReadLine();
    }

    /// <summary>In danh sách đánh số từ 1 và hỏi <c>Chọn số</c>. Trả chỉ số (từ 0) của mục được chọn; chọn <c>0</c> (nếu có <paramref name="zeroLabel"/>) trả -1.</summary>
    public int Choose(string title, IReadOnlyList<string> items, string? zeroLabel = null, int? defaultIndex = null)
    {
        if (items.Count == 0)
        {
            throw new ArgumentException("Danh sách lựa chọn rỗng", nameof(items));
        }

        output.WriteLine(title);
        var width = items.Count.ToString().Length;
        for (var i = 0; i < items.Count; i++)
        {
            output.WriteLine($"  {(i + 1).ToString().PadLeft(width)}. {items[i]}");
        }

        if (zeroLabel is not null)
        {
            output.WriteLine($"  {"0".PadLeft(width)}. {zeroLabel}");
        }

        var min = zeroLabel is null ? 1 : 0;
        while (true)
        {
            output.Write(defaultIndex is { } d ? $"Chọn số [{d + 1}]: " : "Chọn số: ");
            var text = ReadLine().Trim();
            if (text.Length == 0 && defaultIndex is { } chosen)
            {
                return chosen;
            }

            if (int.TryParse(text, out var number) && number >= min && number <= items.Count)
            {
                return number - 1;
            }

            output.WriteLine($"[!] Nhập số từ {min} đến {items.Count}");
        }
    }

    /// <summary>Hỏi một chuỗi; trống thì trả <paramref name="defaultValue"/> (nếu có), không có mặc định thì hỏi lại.</summary>
    public string Ask(string label, string? defaultValue = null, Func<string, string?>? validate = null)
    {
        while (true)
        {
            output.Write(defaultValue is null ? $"{label}: " : $"{label} [{defaultValue}]: ");
            var text = ReadLine().Trim();
            if (text.Length == 0)
            {
                if (defaultValue is not null)
                {
                    return defaultValue;
                }

                output.WriteLine("[!] Không được để trống");
                continue;
            }

            if (validate?.Invoke(text) is { } error)
            {
                output.WriteLine($"[!] {error}");
                continue;
            }

            return text;
        }
    }

    public int AskInt(string label, int defaultValue, int min, int max)
    {
        while (true)
        {
            var text = Ask(label, defaultValue.ToString());
            if (int.TryParse(text, out var value) && value >= min && value <= max)
            {
                return value;
            }

            output.WriteLine($"[!] Nhập số nguyên từ {min} đến {max}");
        }
    }

    public bool Confirm(string label, bool defaultYes)
    {
        while (true)
        {
            output.Write($"{label} (y/n) [{(defaultYes ? "y" : "n")}]: ");
            var text = ReadLine().Trim().ToLowerInvariant();
            switch (text)
            {
                case "":
                    return defaultYes;
                case "y" or "yes" or "c" or "có" or "co":
                    return true;
                case "n" or "no" or "k" or "không" or "khong":
                    return false;
                default:
                    output.WriteLine("[!] Gõ y hoặc n");
                    break;
            }
        }
    }

    /// <summary>
    /// Hỏi khóa API bằng <see cref="TextReader.ReadLine"/> (chốt 14/10/2026): khóa hiện trên màn hình khi gõ, nên in nhắc trước.
    /// Trống thì trả chuỗi rỗng (người gọi giữ khóa cũ nếu có).
    /// </summary>
    public string AskSecret(string label)
    {
        output.WriteLine("[!] Khóa sẽ hiện trên màn hình khi gõ");
        output.Write($"{label}: ");
        return ReadLine().Trim();
    }
}

public enum Mark
{
    Ok,
    Fail,
    Warn,
    Skip,
}

/// <summary>Xuất của tầng UI (spec 06 Mục 1): chỉ <see cref="TextWriter.WriteLine()"/>, chữ thuần, bảng căn cột bằng khoảng trắng. Có khóa để luồng nền và luồng chính không in chen nhau.</summary>
public sealed class ConsoleOutput(TextWriter writer)
{
    private readonly object _gate = new();

    public static ConsoleOutput FromConsole() => new(Console.Out);

    public static string MarkText(Mark mark) => mark switch
    {
        Mark.Ok => "[OK]",
        Mark.Fail => "[X]",
        Mark.Warn => "[!]",
        _ => "[-]",
    };

    public void Line(string text = "")
    {
        lock (_gate)
        {
            writer.WriteLine(text);
        }
    }

    public void Blank() => Line();

    public void Title(string text)
    {
        lock (_gate)
        {
            writer.WriteLine(text);
            writer.WriteLine(new string('=', Math.Max(3, text.Length)));
        }
    }

    public void Status(Mark mark, string label, string detail = "") =>
        Line($"{MarkText(mark),-4} {label}{(detail.Length > 0 ? "  " + detail : "")}");

    /// <summary>Bảng căn cột theo độ dài lớn nhất của mỗi cột; hai khoảng trắng giữa các cột.</summary>
    public void Table(IReadOnlyList<string> headers, IEnumerable<IReadOnlyList<string>> rows)
    {
        var all = new List<IReadOnlyList<string>> { headers };
        all.AddRange(rows);
        var widths = Enumerable.Range(0, headers.Count)
            .Select(c => all.Max(r => c < r.Count ? (r[c] ?? "").Length : 0))
            .ToArray();
        lock (_gate)
        {
            foreach (var row in all)
            {
                var cells = Enumerable.Range(0, headers.Count).Select(c => c < row.Count ? row[c] ?? "" : "");
                writer.WriteLine(string.Join("  ", cells.Select((cell, c) => c == headers.Count - 1 ? cell : cell.PadRight(widths[c]))).TrimEnd());
            }
        }
    }
}
