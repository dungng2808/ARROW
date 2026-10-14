using System.Diagnostics;
using System.Text;
using System.Text.Json;
using Arrow.DataAccess.Common;
using Arrow.DataAccess.Interfaces;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service.Interfaces;

namespace Arrow.Service.External;

/// <summary>Cấu hình của <see cref="JavaHelperClient"/>.</summary>
public sealed record JavaHelperOptions(
    string ProjectDir,
    string JdkHome,
    string? MavenExecutable,
    TimeSpan RequestTimeout,
    TimeSpan BuildTimeout)
{
    /// <summary>Mặc định: <c>ARROW_Framework/tools/java-helper</c>, JDK 17, Maven của máy; mỗi lệnh tối đa 60 s, build tối đa 10 phút.</summary>
    public static JavaHelperOptions Default(FrameworkPaths paths, MachineConfig? machine)
    {
        var jdk = ToolLocator.FindJdkHome(17, machine, paths.ArrowRoot)
                  ?? throw new ArrowException(ErrorCodes.JdkMissing, "java-helper cần JDK 17: không thấy trong machine.local.json hay ARROW/Java-version/runtime/jdk-17");
        return new JavaHelperOptions(
            Path.Combine(paths.FrameworkRoot, "tools", "java-helper"),
            jdk,
            ToolLocator.FindMaven(machine),
            TimeSpan.FromSeconds(60),
            TimeSpan.FromMinutes(10));
    }
}

/// <summary>
/// Gọi java-helper (spec 07 Mục 10): build jar bằng Maven khi chưa có hoặc mã nguồn mới hơn jar; giữ một tiến trình chạy thường trú,
/// gửi mỗi lệnh một dòng JSON và đọc một dòng JSON. Các lệnh được gửi lần lượt (một tiến trình); tiến trình chết thì tự khởi động lại
/// một lần; quá thời gian thì giết tiến trình và báo lỗi.
/// </summary>
public sealed class JavaHelperClient(JavaHelperOptions options, IProcessRunner runner, ILogRepository log) : IJavaHelper
{
    private readonly SemaphoreSlim _gate = new(1, 1);
    private Process? _process;
    private long _nextId;

    public string JarPath => Path.Combine(options.ProjectDir, "target", "java-helper.jar");

    /// <summary>pid của tiến trình java-helper đang chạy (null nếu chưa chạy); để chẩn đoán và test khả năng tự khởi động lại.</summary>
    public int? ProcessId => _process is { HasExited: false } p ? p.Id : null;

    // ── build jar ──────────────────────────────────────────────────────

    /// <summary>Jar cần build lại khi chưa có, hoặc <c>pom.xml</c> / mã nguồn mới hơn jar.</summary>
    public bool NeedsBuild()
    {
        if (!File.Exists(JarPath))
        {
            return true;
        }

        var jarTime = File.GetLastWriteTimeUtc(JarPath);
        var sources = Directory.EnumerateFiles(Path.Combine(options.ProjectDir, "src", "main"), "*", SearchOption.AllDirectories)
            .Append(Path.Combine(options.ProjectDir, "pom.xml"));
        return sources.Any(f => File.GetLastWriteTimeUtc(f) > jarTime);
    }

    private static readonly System.Collections.Concurrent.ConcurrentDictionary<string, SemaphoreSlim> BuildLocks = new(StringComparer.Ordinal);

    /// <summary>
    /// Build jar khi cần. Hai client (trong cùng tiến trình hoặc hai tiến trình) không build cùng lúc: khóa trong tiến trình theo thư mục
    /// project, và khóa file trong thư mục tạm của hệ thống (theo đường dẫn project) giữa các tiến trình; lấy được khóa thì kiểm tra lại vì bên kia có thể vừa build xong.
    /// </summary>
    public async Task<string> EnsureJarAsync(CancellationToken cancellationToken)
    {
        if (!NeedsBuild())
        {
            return JarPath;
        }

        var gate = BuildLocks.GetOrAdd(Path.GetFullPath(options.ProjectDir), _ => new SemaphoreSlim(1, 1));
        await gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            // File khóa nằm ngoài thư mục project (thư mục tạm của hệ thống) để việc chép thư mục java-helper không đụng vào nó.
            var projectKey = Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(Encoding.UTF8.GetBytes(Path.GetFullPath(options.ProjectDir))))[..16];
            var lockPath = Path.Combine(Path.GetTempPath(), $"arrow-java-helper-{projectKey}.lock");
            var waited = System.Diagnostics.Stopwatch.StartNew();
            FileStream? fileLock = null;
            while (fileLock is null)
            {
                try
                {
                    fileLock = new FileStream(lockPath, FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.None);
                }
                catch (IOException) when (waited.Elapsed < options.BuildTimeout)
                {
                    await Task.Delay(500, cancellationToken).ConfigureAwait(false);
                }
            }

            using (fileLock)
            {
                return NeedsBuild() ? await BuildJarAsync(cancellationToken).ConfigureAwait(false) : JarPath;
            }
        }
        finally
        {
            gate.Release();
        }
    }

    private async Task<string> BuildJarAsync(CancellationToken cancellationToken)
    {

        var maven = options.MavenExecutable
                    ?? throw new ArrowException(ErrorCodes.MavenMissing, "Cần Maven để build java-helper (đặt maven_path trong machine.local.json hoặc thêm mvn vào PATH)");
        log.Info($"java-helper: build jar bằng {maven} (JDK {options.JdkHome})");
        var result = await runner.RunAsync(new ProcessSpec(
            maven,
            ["-q", "-B", "-ntp", "-DskipTests", "package"],
            options.ProjectDir,
            new Dictionary<string, string?>
            {
                ["JAVA_HOME"] = options.JdkHome,
                ["PATH"] = Path.Combine(options.JdkHome, "bin") + Path.PathSeparator + Environment.GetEnvironmentVariable("PATH"),
                ["MAVEN_ARGS"] = null,
                ["JDK_JAVA_OPTIONS"] = null,
                ["JAVA_TOOL_OPTIONS"] = null,
            },
            options.BuildTimeout), cancellationToken).ConfigureAwait(false);
        if (!result.Succeeded || !File.Exists(JarPath))
        {
            throw new ArrowException(ErrorCodes.JavaHelperBuild,
                result.TimedOut ? "Build java-helper quá thời gian" : $"Build java-helper lỗi (mã {result.ExitCode}):\n{result.Tail()}");
        }

        return JarPath;
    }

    // ── tiến trình thường trú ───────────────────────────────────────────

    private async Task<Process> EnsureStartedAsync(CancellationToken cancellationToken)
    {
        if (_process is { HasExited: false })
        {
            return _process;
        }

        var jar = await EnsureJarAsync(cancellationToken).ConfigureAwait(false);
        var start = new ProcessStartInfo(ToolLocator.JavaExecutable(options.JdkHome))
        {
            RedirectStandardInput = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true,
            StandardInputEncoding = new UTF8Encoding(false),
            StandardOutputEncoding = Encoding.UTF8,
            StandardErrorEncoding = Encoding.UTF8,
            WorkingDirectory = options.ProjectDir,
        };
        // Stack lớn cho cây cú pháp sâu; tắt biến môi trường của người dùng có thể làm JVM in thêm dòng.
        foreach (var argument in new[] { "-Xss8m", "-jar", jar })
        {
            start.ArgumentList.Add(argument);
        }

        start.Environment.Remove("JAVA_TOOL_OPTIONS");
        start.Environment.Remove("JDK_JAVA_OPTIONS");
        var process = Process.Start(start) ?? throw new ArrowException(ErrorCodes.JavaHelperError, "Không chạy được java-helper");
        process.ErrorDataReceived += (_, e) =>
        {
            if (!string.IsNullOrWhiteSpace(e.Data))
            {
                log.Warn("java-helper stderr: " + e.Data);
            }
        };
        process.BeginErrorReadLine();
        log.Info($"java-helper: khởi động (pid {process.Id})");
        _process = process;
        return process;
    }

    private void KillProcess()
    {
        var process = _process;
        _process = null;
        if (process is null)
        {
            return;
        }

        try
        {
            if (!process.HasExited)
            {
                process.Kill(entireProcessTree: true);
                process.WaitForExit(5_000);
            }
        }
        catch (InvalidOperationException)
        {
        }

        process.Dispose();
    }

    /// <summary>Gửi một lệnh, trả <c>result</c>. Tiến trình chết giữa chừng thì khởi động lại và gửi lại đúng một lần.</summary>
    private async Task<JsonElement> SendAsync(string command, object args, CancellationToken cancellationToken)
    {
        await _gate.WaitAsync(cancellationToken).ConfigureAwait(false);
        try
        {
            for (var attempt = 1; ; attempt++)
            {
                var process = await EnsureStartedAsync(cancellationToken).ConfigureAwait(false);
                var id = Interlocked.Increment(ref _nextId);
                var line = JsonSerializer.Serialize(new { id, command, args });
                string? reply;
                using var timeout = new CancellationTokenSource(options.RequestTimeout);
                using var linked = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken, timeout.Token);
                try
                {
                    await process.StandardInput.WriteLineAsync(line.AsMemory(), linked.Token).ConfigureAwait(false);
                    await process.StandardInput.FlushAsync(linked.Token).ConfigureAwait(false);
                    reply = await process.StandardOutput.ReadLineAsync(linked.Token).ConfigureAwait(false);
                }
                catch (OperationCanceledException) when (timeout.IsCancellationRequested && !cancellationToken.IsCancellationRequested)
                {
                    KillProcess();
                    throw new ArrowException(ErrorCodes.JavaHelperTimeout, $"java-helper không trả lời lệnh {command} sau {options.RequestTimeout.TotalSeconds:0} s");
                }
                catch (IOException) when (attempt == 1)
                {
                    reply = null;
                }

                if (reply is null)
                {
                    KillProcess();
                    if (attempt == 1)
                    {
                        log.Warn($"java-helper: tiến trình dừng khi chạy {command}, khởi động lại");
                        continue;
                    }

                    throw new ArrowException(ErrorCodes.JavaHelperError, $"java-helper dừng khi chạy {command}");
                }

                using var document = JsonDocument.Parse(reply);
                var root = document.RootElement;
                if (root.TryGetProperty("id", out var replyId) && replyId.ValueKind == JsonValueKind.Number && replyId.GetInt64() != id)
                {
                    KillProcess();
                    throw new ArrowException(ErrorCodes.JavaHelperError, $"java-helper trả lời sai thứ tự (chờ id {id}, nhận {replyId.GetInt64()})");
                }

                if (!root.GetProperty("ok").GetBoolean())
                {
                    throw new ArrowException(ErrorCodes.JavaHelperError, $"java-helper {command}: {root.GetProperty("error").GetString()}");
                }

                return root.GetProperty("result").Clone();
            }
        }
        finally
        {
            _gate.Release();
        }
    }

    private async Task<T> CallAsync<T>(string command, object args, CancellationToken cancellationToken) =>
        JsonFile.Deserialize<T>((await SendAsync(command, args, cancellationToken).ConfigureAwait(false)).GetRawText());

    // ── các lệnh ───────────────────────────────────────────────────────

    public async Task<string> PingAsync(CancellationToken cancellationToken) =>
        (await SendAsync("ping", new { }, cancellationToken).ConfigureAwait(false)).GetProperty("version").GetString() ?? "";

    public Task<JavaParseResult> ParseAsync(string code, CancellationToken cancellationToken) =>
        CallAsync<JavaParseResult>("parse", new { code }, cancellationToken);

    public Task<JavaRenameResult> RenameClassAsync(string code, string newName, string? package, CancellationToken cancellationToken) =>
        package is null
            ? CallAsync<JavaRenameResult>("rename_class", new { code, new_name = newName }, cancellationToken)
            : CallAsync<JavaRenameResult>("rename_class", new { code, new_name = newName, package }, cancellationToken);

    public Task<JavaHashResult> StripCommentsHashAsync(string code, CancellationToken cancellationToken) =>
        CallAsync<JavaHashResult>("strip_comments_hash", new { code }, cancellationToken);

    public Task<JavaAssertionCount> CountAssertionsAsync(string code, CancellationToken cancellationToken) =>
        CallAsync<JavaAssertionCount>("count_assertions", new { code }, cancellationToken);

    public Task<JavaTestMethods> ListTestMethodsAsync(string code, CancellationToken cancellationToken) =>
        CallAsync<JavaTestMethods>("list_test_methods", new { code }, cancellationToken);

    public Task<JavaMethodsAtLines> MethodAtLineAsync(string code, IReadOnlyList<int> lines, CancellationToken cancellationToken) =>
        CallAsync<JavaMethodsAtLines>("method_at_line", new { code, lines }, cancellationToken);

    public Task<JavaSpliceResult> SpliceMethodsAsync(string baseCode, string response, IReadOnlyList<string> methods, CancellationToken cancellationToken) =>
        CallAsync<JavaSpliceResult>("splice_methods", new { @base = baseCode, response, methods }, cancellationToken);

    public Task<JavaClassApi> ClassApiAsync(IReadOnlyList<string> sources, IReadOnlyList<string> classes, IReadOnlyList<string> classpath, CancellationToken cancellationToken) =>
        CallAsync<JavaClassApi>("class_api", new { sources, classes, classpath }, cancellationToken);

    public Task<JavaMethodSources> MethodSourceAsync(string code, IReadOnlyList<string> names, CancellationToken cancellationToken) =>
        CallAsync<JavaMethodSources>("method_source", new { code, names }, cancellationToken);

    public Task<JavaCalledMethods> CalledMethodsAsync(string testCode, IReadOnlyList<string> testMethods, string cutCode, CancellationToken cancellationToken) =>
        CallAsync<JavaCalledMethods>("called_methods", new { test_code = testCode, test_methods = testMethods, cut_code = cutCode }, cancellationToken);

    public Task<JavaShrinkResult> ShrinkSourceAsync(string code, int maxChars, CancellationToken cancellationToken) =>
        CallAsync<JavaShrinkResult>("shrink_source", new { code, max_chars = maxChars }, cancellationToken);

    public async ValueTask DisposeAsync()
    {
        await _gate.WaitAsync().ConfigureAwait(false);
        try
        {
            if (_process is { HasExited: false } process)
            {
                try
                {
                    await process.StandardInput.WriteLineAsync("{\"id\":0,\"command\":\"shutdown\"}").ConfigureAwait(false);
                    await process.StandardInput.FlushAsync().ConfigureAwait(false);
                    process.WaitForExit(3_000);
                }
                catch (IOException)
                {
                }
            }

            KillProcess();
        }
        finally
        {
            _gate.Release();
            _gate.Dispose();
        }
    }
}
