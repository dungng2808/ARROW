namespace Arrow.Infrastructure;

/// <summary>Vị trí thư mục <c>pilot_tool</c> và thư mục gốc ARROW, dò từ thư mục hiện tại hoặc thư mục chương trình.</summary>
public sealed record ToolPaths(string PilotToolDir, string ArrowRoot)
{
    public string MachineConfigPath => Path.Combine(PilotToolDir, "machine.local.json");
    public string LiteLlmConfigPath => Path.Combine(PilotToolDir, "litellm.config.local.yaml");
    public string ExperimentsDir => Path.Combine(PilotToolDir, "experiments");
    public string ShardsDir => Path.Combine(ArrowRoot, "shards");
    public string ToolLogDir => Path.Combine(PilotToolDir, "logs");

    public string Resolve(string relativeOrAbsolute, string? baseDir = null) =>
        Path.IsPathRooted(relativeOrAbsolute)
            ? relativeOrAbsolute
            : Path.GetFullPath(Path.Combine(baseDir ?? PilotToolDir, relativeOrAbsolute));

    /// <summary>Đường dẫn tương đối so với pilot_tool (ghi vào machine.local.json để dùng được trên máy khác).</summary>
    public string Relative(string path) => Path.GetRelativePath(PilotToolDir, path).Replace('\\', '/');

    public static ToolPaths Detect(string? overrideDir = null)
    {
        // Thứ tự: --root, biến môi trường ARROW_PILOT_ROOT (do lệnh arrow cài sẵn), thư mục hiện tại, thư mục chương trình.
        var envRoot = System.Environment.GetEnvironmentVariable("ARROW_PILOT_ROOT");
        foreach (var start in new[] { overrideDir, envRoot, System.Environment.CurrentDirectory, AppContext.BaseDirectory }.Where(s => !string.IsNullOrEmpty(s)))
        {
            for (var directory = new DirectoryInfo(start!); directory is not null; directory = directory.Parent)
            {
                if (File.Exists(Path.Combine(directory.FullName, "ArrowRunner.sln")))
                {
                    return new ToolPaths(directory.FullName, directory.Parent?.FullName ?? directory.FullName);
                }
            }
        }

        throw new InvalidOperationException("Không tìm thấy thư mục pilot_tool (chứa ArrowRunner.sln). Chạy lệnh trong thư mục ARROW/pilot_tool hoặc dùng --root.");
    }
}
