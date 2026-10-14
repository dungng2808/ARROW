using Arrow.Models.Block1;
using Arrow.Models.Common;

namespace Arrow.Service.Interfaces;

/// <summary>Một lệnh cần chạy: chương trình, tham số, thư mục, biến môi trường (null = xóa biến đó), timeout, dữ liệu stdin.</summary>
public sealed record ProcessSpec(
    string FileName,
    IReadOnlyList<string> Arguments,
    string WorkingDirectory,
    IReadOnlyDictionary<string, string?>? Environment = null,
    TimeSpan? Timeout = null,
    string? StandardInput = null);

/// <summary>Kết quả một lệnh: mã thoát (null khi bị giết), có timeout không, stdout+stderr gộp, thời gian chạy.</summary>
public sealed record ProcessResult(int? ExitCode, bool TimedOut, string Output, TimeSpan Duration)
{
    public bool Succeeded => !TimedOut && ExitCode == 0;

    /// <summary>Vài dòng cuối của output (để đưa vào thông báo lỗi).</summary>
    public string Tail(int lines = 20) => string.Join('\n', Output.Replace("\r\n", "\n").Split('\n').Where(l => l.Trim().Length > 0).TakeLast(lines));
}

/// <summary>Chạy tiến trình ngoài (spec 07 Mục 4.3): timeout, giết cả cây tiến trình khi timeout hoặc bị hủy.</summary>
public interface IProcessRunner
{
    Task<ProcessResult> RunAsync(ProcessSpec spec, CancellationToken cancellationToken);
}

// ── git và workspace (spec 01 Mục 3, spec 03 Mục 3.0) ──────────────

[System.Text.Json.Serialization.JsonConverter(typeof(UpperSnakeEnumConverter<GitFetchStatus>))]
public enum GitFetchStatus
{
    Ok,

    /// <summary>Repository không còn, hoặc server không có commit (không thử lại).</summary>
    CommitMissing,

    /// <summary>Lỗi mạng (thử lại được).</summary>
    NetworkError,
}

public sealed record GitFetchResult(GitFetchStatus Status, string Detail);

/// <summary>Các lệnh git của khối 1: mirror bare, tải đúng một commit, worktree.</summary>
public interface IGitClient
{
    /// <summary>Tạo mirror bare (<c>git init --bare</c> + remote <c>origin</c>) nếu chưa có.</summary>
    Task EnsureMirrorAsync(string mirrorPath, string repoUrl, CancellationToken cancellationToken);

    Task<bool> HasCommitAsync(string mirrorPath, string sha, CancellationToken cancellationToken);

    /// <summary>
    /// Một lần tải commit: thử các mirror cục bộ có sẵn (<paramref name="localSources"/>), rồi <c>fetch --depth 1 origin &lt;sha&gt;</c>,
    /// server từ chối thì tải toàn bộ lịch sử. Không tự thử lại khi lỗi mạng.
    /// </summary>
    Task<GitFetchResult> FetchCommitAsync(string mirrorPath, string sha, IReadOnlyList<string> localSources, CancellationToken cancellationToken);

    /// <summary><c>git worktree add --detach</c>; trả commit HEAD của worktree.</summary>
    Task<string> AddWorktreeAsync(string mirrorPath, string sha, string targetDir, CancellationToken cancellationToken);

    /// <summary>Xóa worktree và dọn đăng ký của nó trong mirror.</summary>
    Task RemoveWorktreeAsync(string mirrorPath, string worktreeDir, CancellationToken cancellationToken);
}

/// <summary>Thư mục làm việc của công cụ build: mirror, checkout gốc, snapshot, bản sao cho từng lần chạy test.</summary>
public interface IWorkspaceManager
{
    string WorkDir { get; }

    /// <summary><c>work_dir/mirrors/&lt;host&gt;/&lt;owner&gt;/&lt;repo&gt;.git</c>.</summary>
    string MirrorPath(string repoUrl);

    /// <summary>Mirror cùng repository theo cách đặt tên cũ (<c>work_dir/mirrors/&lt;owner&gt;__&lt;repo&gt;.git</c>) để tải commit không qua mạng.</summary>
    IReadOnlyList<string> LegacyMirrors(string repoUrl);

    /// <summary><c>work_dir/ws/&lt;run&gt;/&lt;task&gt;</c>.</summary>
    string TaskDir(string runId, TaskKey task);

    /// <summary>Sao chép thư mục bằng cách nhanh nhất hệ điều hành hỗ trợ (clonefile trên macOS, reflink trên Linux), giữ quyền chạy, thời gian sửa và symlink.</summary>
    Task CopyDirectoryAsync(string source, string target, CancellationToken cancellationToken);

    /// <summary>Bản sao mới của <paramref name="snapshot"/> trong <c>&lt;task dir&gt;/tmp/&lt;label&gt;-&lt;id&gt;</c>.</summary>
    Task<string> NewCopyAsync(string snapshot, string runId, TaskKey task, string label, CancellationToken cancellationToken);

    void DeleteDirectory(string path);

    /// <summary>Xóa toàn bộ workspace của task (gỡ worktree khỏi mirror).</summary>
    Task DeleteTaskAsync(string runId, TaskKey task, string? mirrorPath, CancellationToken cancellationToken);
}

// ── Maven/Gradle ─────────────────────────────────────────────────────

/// <summary>Loại thất bại của một lệnh build, để quyết định thử lại (lỗi hạ tầng) hay đổi JDK / kết luận lỗi.</summary>
public enum BuildFailureKind
{
    None,
    Timeout,

    /// <summary>Lỗi mạng khi tải dependency/plugin.</summary>
    Network,

    /// <summary>Lỗi biên dịch hoặc lỗi build khác.</summary>
    Build,
}

/// <summary>Cách gọi Maven/Gradle cho một bản sao workspace (spec 01 Mục 4.1, 6).</summary>
public sealed record BuildContext
{
    /// <summary>Gốc repository của bản sao (tuyệt đối).</summary>
    public string RepoRoot { get; init; } = "";
    public BuildPlan Plan { get; init; } = new();
    /// <summary>Chương trình build: đường dẫn tuyệt đối của công cụ trên máy, hoặc wrapper tính từ gốc repository (<see cref="ExecutableInRepo"/>).</summary>
    public string Executable { get; init; } = "";

    /// <summary>Wrapper (<c>mvnw</c>, <c>gradlew</c>) nằm trong repository: đường dẫn tính từ <see cref="RepoRoot"/>, nên dùng được trên mọi bản sao.</summary>
    public bool ExecutableInRepo { get; init; }
    public string JdkHome { get; init; } = "";
    public IReadOnlyList<string> CommonFlags { get; init; } = [];

    /// <summary>Gradle: init script của framework (tuyệt đối).</summary>
    public string? InitScript { get; init; }
    public TimeSpan Timeout { get; init; } = TimeSpan.FromMinutes(15);

    /// <summary>Thêm vào <c>JAVA_TOOL_OPTIONS</c> chung (khối 4: agent JaCoCo).</summary>
    public string? ExtraJavaToolOptions { get; init; }

    public string BuildRootPath => Path.GetFullPath(Path.Combine(RepoRoot, Plan.BuildRoot.Replace('/', Path.DirectorySeparatorChar)));

    public string ExecutablePath => ExecutableInRepo ? Path.GetFullPath(Path.Combine(RepoRoot, Executable.Replace('/', Path.DirectorySeparatorChar))) : Executable;
}

public sealed record BuildRun(IReadOnlyList<string> Command, string WorkingDirectory, ProcessResult Result, BuildFailureKind Failure);

public interface IBuildRunner
{
    /// <summary>
    /// Chạy từ build root: Maven <c>&lt;exe&gt; -f pom.xml &lt;cờ chung&gt; [-pl m -am] &lt;args&gt;</c>;
    /// Gradle <c>&lt;exe&gt; --no-daemon --console=plain --init-script … &lt;args&gt;</c>. JDK, <c>JAVA_TOOL_OPTIONS</c> đặt qua biến môi trường.
    /// </summary>
    Task<BuildRun> RunAsync(BuildContext context, IReadOnlyList<string> arguments, CancellationToken cancellationToken);
}

/// <summary>Đọc báo cáo XML của Surefire (<c>target/surefire-reports</c>) và Gradle (<c>build/test-results</c>) trong một thư mục.</summary>
public interface ITestReportReader
{
    IReadOnlyList<TestCaseResult> Read(string directory);
}

// ── java-helper (spec 07 Mục 10) ─────────────────────────────────────

public sealed record JavaTypeName(string Name, bool Public);

public sealed record JavaParseResult(bool Parsed, string? Error, string Package, IReadOnlyList<JavaTypeName> TopLevelTypes, int TestMethodCount);

public sealed record JavaRenameResult(string Code, string OldName, IReadOnlyList<string> Actions);

public sealed record JavaHashResult(bool Parsed, string Normalized, string CodeHash);

public sealed record JavaAssertionCount(bool Parsed, int? Total, IReadOnlyDictionary<string, int> PerMethod);

public sealed record JavaTestMethods(bool Parsed, IReadOnlyList<string> Methods);

public sealed record JavaMethodsAtLines(bool Parsed, IReadOnlyDictionary<int, string?> Methods);

public sealed record JavaSpliceResult(
    string Code,
    IReadOnlyList<string> Replaced,
    IReadOnlyList<string> Dropped,
    IReadOnlyList<string> AddedImports,
    IReadOnlyList<string> AddedFields,
    IReadOnlyList<string> AddedMethods);

public sealed record JavaClassApiType(string Name, string Kind, string Origin, IReadOnlyList<string> Members);

public sealed record JavaMissingClass(string Name, string Reason);

public sealed record JavaClassApi(IReadOnlyList<JavaClassApiType> Types, IReadOnlyList<JavaMissingClass> Missing);

public sealed record JavaNamedSource(string Name, string Source);

public sealed record JavaMethodSources(bool Parsed, IReadOnlyList<JavaNamedSource> Methods);

/// <summary>Mã CUT sau khi rút gọn; <c>Truncation</c> ∈ <c>none</c>, <c>comments_removed</c>, <c>signatures_only</c>.</summary>
public sealed record JavaShrinkResult(string Code, string Truncation);

public sealed record JavaCalledMethods(
    bool Parsed,
    IReadOnlyList<string> Called,
    IReadOnlyList<JavaNamedSource> Methods,
    IReadOnlyList<string> Constructors,
    IReadOnlyList<string> Fields);

/// <summary>
/// Công cụ Java hiểu cấu trúc mã Java (JavaParser), chạy thường trú (spec 07 Mục 10). Lệnh lỗi (ví dụ ghép method khi câu trả lời
/// không parse được) ném <c>ArrowException</c> mã <c>JAVA_HELPER_ERROR</c>; quá thời gian ném <c>JAVA_HELPER_TIMEOUT</c>.
/// </summary>
public interface IJavaHelper : IAsyncDisposable
{
    Task<string> PingAsync(CancellationToken cancellationToken);

    /// <summary>Kiểm tra cú pháp, class top-level, package (spec 02 Mục 4.5 bước 2).</summary>
    Task<JavaParseResult> ParseAsync(string code, CancellationToken cancellationToken);

    /// <summary>Đặt package (null = giữ nguyên) và đổi tên class chính cùng mọi tham chiếu, giữ nguyên định dạng (spec 02 Mục 4.5 bước 3).</summary>
    Task<JavaRenameResult> RenameClassAsync(string code, string newName, string? package, CancellationToken cancellationToken);

    /// <summary>Mã bỏ comment, thu khoảng trắng, và hash <c>sha256:…</c> (AR Mục 2.3).</summary>
    Task<JavaHashResult> StripCommentsHashAsync(string code, CancellationToken cancellationToken);

    /// <summary>Đếm assertion theo AR Mục 5.2.</summary>
    Task<JavaAssertionCount> CountAssertionsAsync(string code, CancellationToken cancellationToken);

    Task<JavaTestMethods> ListTestMethodsAsync(string code, CancellationToken cancellationToken);

    /// <summary>Method nhỏ nhất chứa mỗi dòng (method chứa lỗi biên dịch).</summary>
    Task<JavaMethodsAtLines> MethodAtLineAsync(string code, IReadOnlyList<int> lines, CancellationToken cancellationToken);

    /// <summary>Ghép mức 3: thay các method lỗi của checkpoint bằng bản trong câu trả lời (spec 02 Mục 4.5 bước 4).</summary>
    Task<JavaSpliceResult> SpliceMethodsAsync(string baseCode, string response, IReadOnlyList<string> methods, CancellationToken cancellationToken);

    /// <summary>Chữ ký constructor/method/field không private: từ mã nguồn và từ classpath (thông tin bổ sung mức 2 COMPILE).</summary>
    Task<JavaClassApi> ClassApiAsync(IReadOnlyList<string> sources, IReadOnlyList<string> classes, IReadOnlyList<string> classpath, CancellationToken cancellationToken);

    /// <summary>Mã nguồn các method theo tên (<c>&lt;init&gt;</c> = constructor).</summary>
    Task<JavaMethodSources> MethodSourceAsync(string code, IReadOnlyList<string> names, CancellationToken cancellationToken);

    /// <summary>Method của CUT mà các test method gọi tới, kèm mã nguồn, constructor và field của CUT.</summary>
    Task<JavaCalledMethods> CalledMethodsAsync(string testCode, IReadOnlyList<string> testMethods, string cutCode, CancellationToken cancellationToken);

    /// <summary>Rút gọn mã CUT dài hơn <paramref name="maxChars"/>: bỏ comment, rồi chỉ giữ chữ ký (spec 02 Mục 2.1 bước 2).</summary>
    Task<JavaShrinkResult> ShrinkSourceAsync(string code, int maxChars, CancellationToken cancellationToken);
}
