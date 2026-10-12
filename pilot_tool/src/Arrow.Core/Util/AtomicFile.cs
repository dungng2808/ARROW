using System.Text;

namespace Arrow.Core.Util;

/// <summary>Ghi file nguyên tử và ghi thêm dòng có flush (specs/08 §3).</summary>
public static class AtomicFile
{
    private static readonly UTF8Encoding Utf8NoBom = new(encoderShouldEmitUTF8Identifier: false);

    public static void WriteAllText(string path, string content)
    {
        var directory = Path.GetDirectoryName(Path.GetFullPath(path));
        if (!string.IsNullOrEmpty(directory))
        {
            Directory.CreateDirectory(directory);
        }

        var temp = path + ".tmp";
        File.WriteAllText(temp, content.Replace("\r\n", "\n"), Utf8NoBom);
        File.Move(temp, path, overwrite: true);
    }

    private static readonly object AppendLock = new();

    /// <summary>Ghi thêm một dòng (không chứa xuống dòng) rồi flush xuống đĩa.</summary>
    public static void AppendLine(string path, string line)
    {
        if (line.Contains('\n'))
        {
            throw new ArgumentException("Một dòng JSONL không được chứa ký tự xuống dòng.", nameof(line));
        }

        var directory = Path.GetDirectoryName(Path.GetFullPath(path));
        if (!string.IsNullOrEmpty(directory))
        {
            Directory.CreateDirectory(directory);
        }

        lock (AppendLock)
        {
            using var stream = new FileStream(path, FileMode.Append, FileAccess.Write, FileShare.Read);
            var bytes = Utf8NoBom.GetBytes(line + "\n");
            stream.Write(bytes, 0, bytes.Length);
            stream.Flush(flushToDisk: true);
        }
    }

    /// <summary>Đọc các dòng JSONL; bỏ dòng cuối nếu bị ghi dở (không phải JSON hoàn chỉnh).</summary>
    public static IEnumerable<string> ReadLines(string path)
    {
        if (!File.Exists(path))
        {
            yield break;
        }

        using var stream = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
        using var reader = new StreamReader(stream, Utf8NoBom);
        while (reader.ReadLine() is { } line)
        {
            if (!string.IsNullOrWhiteSpace(line))
            {
                yield return line;
            }
        }
    }
}
