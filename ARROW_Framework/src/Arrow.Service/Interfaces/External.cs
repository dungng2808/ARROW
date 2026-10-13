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
}
