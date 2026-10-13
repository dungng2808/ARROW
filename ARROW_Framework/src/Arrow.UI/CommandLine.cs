namespace Arrow.UI;

/// <summary>
/// Tách tham số dòng lệnh: các từ đầu là lệnh (<c>prompts check</c>), sau đó là tùy chọn <c>--tên giá trị</c> hoặc cờ <c>--tên</c>.
/// Một tùy chọn có thể lặp lại (<c>--run-id A --run-id B</c>). Không dùng thư viện.
/// </summary>
public sealed class CommandLine
{
    private readonly Dictionary<string, List<string>> _options = new(StringComparer.Ordinal);
    private readonly HashSet<string> _flags = new(StringComparer.Ordinal);

    public IReadOnlyList<string> Command { get; }

    private CommandLine(IReadOnlyList<string> command) => Command = command;

    /// <summary>Các tùy chọn không nhận giá trị.</summary>
    public static readonly IReadOnlySet<string> KnownFlags = new HashSet<string>(StringComparer.Ordinal)
    {
        "version", "help", "verbose", "plain", "rerun-infra", "keep-workspace",
    };

    public static CommandLine Parse(IReadOnlyList<string> args)
    {
        var command = new List<string>();
        var i = 0;
        while (i < args.Count && !args[i].StartsWith("--", StringComparison.Ordinal))
        {
            command.Add(args[i]);
            i++;
        }

        var result = new CommandLine(command);
        while (i < args.Count)
        {
            var arg = args[i];
            if (!arg.StartsWith("--", StringComparison.Ordinal) || arg.Length == 2)
            {
                throw new ArgumentException($"Tham số không hợp lệ: {arg}");
            }

            var name = arg[2..];
            var eq = name.IndexOf('=');
            if (eq > 0)
            {
                result.Add(name[..eq], name[(eq + 1)..]);
                i++;
            }
            else if (KnownFlags.Contains(name) || i + 1 >= args.Count || args[i + 1].StartsWith("--", StringComparison.Ordinal))
            {
                result._flags.Add(name);
                i++;
            }
            else
            {
                result.Add(name, args[i + 1]);
                i += 2;
            }
        }

        return result;
    }

    private void Add(string name, string value)
    {
        if (!_options.TryGetValue(name, out var values))
        {
            _options[name] = values = [];
        }

        values.Add(value);
    }

    public bool Is(params string[] words) => Command.Count == words.Length && Command.Zip(words).All(p => p.First == p.Second);

    public bool Flag(string name) => _flags.Contains(name);

    public string? Option(string name) => _options.TryGetValue(name, out var values) ? values[^1] : null;

    public IReadOnlyList<string> Options(string name) => _options.TryGetValue(name, out var values) ? values : [];
}
