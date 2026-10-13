using System.Diagnostics;
using Arrow.DataAccess.Files;
using Arrow.Models.Common;
using Arrow.Models.Config;
using Arrow.Service;
using Arrow.Service.External;
using Arrow.Service.Interfaces;

namespace Arrow.IntegrationTests;

/// <summary>Một tiến trình java-helper dùng chung cho cả lớp test (như khi chạy thật: tiến trình thường trú).</summary>
public sealed class JavaHelperFixture : IAsyncLifetime
{
    public FrameworkPaths Paths { get; } = ServiceFactory.Locate(AppContext.BaseDirectory)!;
    public JavaHelperClient Client { get; private set; } = null!;

    public Task InitializeAsync()
    {
        var options = JavaHelperOptions.Default(Paths, machine: null);
        Client = new JavaHelperClient(options, new ProcessRunner(), new FileLogRepository(Paths));
        return Task.CompletedTask;
    }

    public async Task DisposeAsync() => await Client.DisposeAsync();
}

public class JavaHelperTests(JavaHelperFixture fixture) : IClassFixture<JavaHelperFixture>
{
    private IJavaHelper Helper => fixture.Client;
    private static CancellationToken None => CancellationToken.None;

    private const string Cut = """
        package com.acme.shop;

        public class OrderService {
            private final TaxPolicy tax;
            public OrderService(TaxPolicy tax) { this.tax = tax; }
            public long totalPrice(long orderId) { return tax.apply(100); }
            public void cancel(long orderId) {}
            private void secret() {}
        }
        """;

    private const string Test = """
        import org.junit.Test;
        import static org.junit.Assert.*;

        public class OrderServiceTest {
            private OrderServiceTest self;

            @Test
            public void totalPriceAddsTax() {
                OrderService service = new OrderService(new TaxPolicy(10));
                assertEquals(110, service.getTotal(1L));
                assertTrue(true);
            }

            @Test(expected = IllegalStateException.class)
            public void cancelTwiceFails() { new OrderService(null).cancel(1L); }
        }
        """;

    [Fact]
    public async Task PingParseAndNormalizeNames()
    {
        Assert.Equal("1.0.0", await Helper.PingAsync(None));

        var parsed = await Helper.ParseAsync(Test, None);
        Assert.True(parsed.Parsed);
        Assert.Equal("", parsed.Package);
        Assert.Equal("OrderServiceTest", Assert.Single(parsed.TopLevelTypes).Name);
        Assert.Equal(2, parsed.TestMethodCount);
        Assert.False((await Helper.ParseAsync("class A { void x( }", None)).Parsed);

        var renamed = await Helper.RenameClassAsync(Test, "OrderService_ArrowTest", "com.acme.shop", None);
        Assert.StartsWith("package com.acme.shop;", renamed.Code);
        Assert.Contains("public class OrderService_ArrowTest {", renamed.Code);
        Assert.Contains("private OrderService_ArrowTest self;", renamed.Code);
        Assert.DoesNotContain("OrderServiceTest", renamed.Code);
        Assert.Equal(["package: added com.acme.shop", "class: OrderServiceTest → OrderService_ArrowTest"], renamed.Actions);
    }

    [Fact]
    public async Task HashCountsTestsAndLines()
    {
        var a = await Helper.StripCommentsHashAsync("class A { /* x */ int f() { return 1; } }", None);
        var b = await Helper.StripCommentsHashAsync("class A {\n  // y\n  int f() {\n    return 1;\n  }\n}\n", None);
        Assert.Equal(a.CodeHash, b.CodeHash);
        Assert.StartsWith("sha256:", a.CodeHash);

        var counts = await Helper.CountAssertionsAsync(Test, None);
        Assert.Equal(2, counts.Total); // assertEquals + @Test(expected); assertTrue(true) không đếm
        Assert.Equal(1, counts.PerMethod["totalPriceAddsTax"]);
        Assert.Equal(1, counts.PerMethod["cancelTwiceFails"]);

        Assert.Equal(["totalPriceAddsTax", "cancelTwiceFails"], (await Helper.ListTestMethodsAsync(Test, None)).Methods);

        var lines = await Helper.MethodAtLineAsync(Test, [10, 1], None);
        Assert.Equal("totalPriceAddsTax", lines.Methods[10]);
        Assert.Null(lines.Methods[1]);
    }

    [Fact]
    public async Task SpliceKeepsOtherMethodsAndReportsChanges()
    {
        const string response = """
            import org.junit.Test;
            import java.util.List;
            import static org.junit.Assert.*;

            public class OrderServiceTest {
                @Test
                public void totalPriceAddsTax() {
                    assertEquals(110, new OrderService(new TaxPolicy(10)).totalPrice(1L));
                }
            }
            """;
        var result = await Helper.SpliceMethodsAsync(Test, response, ["totalPriceAddsTax"], None);
        Assert.Equal(["totalPriceAddsTax"], result.Replaced);
        Assert.Empty(result.Dropped);
        Assert.Equal(["import java.util.List;"], result.AddedImports);
        Assert.Contains(".totalPrice(1L)", result.Code);
        Assert.DoesNotContain("getTotal", result.Code);
        Assert.Contains("public void cancelTwiceFails() { new OrderService(null).cancel(1L); }", result.Code);

        // Câu trả lời không parse được: lỗi có mã, và java-helper vẫn dùng tiếp được.
        var error = await Assert.ThrowsAsync<ArrowException>(() => Helper.SpliceMethodsAsync(Test, "class {", ["totalPriceAddsTax"], None));
        Assert.Equal(ErrorCodes.JavaHelperError, error.Code);
        Assert.True((await Helper.ParseAsync(Test, None)).Parsed);
    }

    [Fact]
    public async Task ClassApiFromSourceAndFromJarOnClasspath()
    {
        var api = await Helper.ClassApiAsync([Cut], ["com.google.gson.Gson", "com.example.Missing"], [fixture.Client.JarPath], None);
        var order = api.Types.Single(t => t.Name == "com.acme.shop.OrderService");
        Assert.Equal("source", order.Origin);
        Assert.Contains("public OrderService(TaxPolicy tax)", order.Members);
        Assert.Contains("public long totalPrice(long orderId)", order.Members);
        Assert.DoesNotContain(order.Members, m => m.Contains("secret"));

        var gson = api.Types.Single(t => t.Name == "com.google.gson.Gson");
        Assert.Equal("classpath", gson.Origin);
        Assert.Contains(gson.Members, m => m.StartsWith("public String toJson(Object)", StringComparison.Ordinal));
        Assert.Equal("com.example.Missing", Assert.Single(api.Missing).Name);
    }

    [Fact]
    public async Task MethodSourcesAndCalledMethods()
    {
        var sources = await Helper.MethodSourceAsync(Cut, ["totalPrice", "<init>"], None);
        Assert.Equal(["<init>", "totalPrice"], sources.Methods.Select(m => m.Name));
        Assert.StartsWith("public long totalPrice(long orderId)", sources.Methods[1].Source);

        const string test = "class T { @Test void a() { s.totalPrice(1L); s.getTotal(1L); } @Test void b() { s.cancel(1L); } }";
        var called = await Helper.CalledMethodsAsync(test, ["a"], Cut, None);
        Assert.Equal(["totalPrice"], called.Called);
        Assert.Single(called.Methods);
        Assert.Single(called.Constructors);
        Assert.Equal(["private final TaxPolicy tax;"], called.Fields);
    }

    [Fact]
    public async Task ParallelCallsAreServedOneByOne()
    {
        var results = await Task.WhenAll(Enumerable.Range(0, 30).Select(i =>
            Helper.ParseAsync($"class C{i} {{ @Test void t{i}() {{}} }}", None)));
        Assert.All(results, r => Assert.True(r.Parsed));
        Assert.Equal(Enumerable.Range(0, 30).Select(i => $"C{i}"), results.Select(r => r.TopLevelTypes[0].Name));
    }

    [Fact]
    public async Task RestartsAfterProcessIsKilled()
    {
        await Helper.PingAsync(None);
        var pid = fixture.Client.ProcessId;
        Assert.NotNull(pid);
        using (var process = Process.GetProcessById(pid!.Value))
        {
            process.Kill();
            process.WaitForExit(5_000);
        }

        Assert.Equal("1.0.0", await Helper.PingAsync(None));
        Assert.NotEqual(pid, fixture.Client.ProcessId);
    }
}

public class JavaHelperBuildTests
{
    [Fact]
    public async Task BuildsJarFromSourcesWhenMissingOrOutdated()
    {
        var paths = ServiceFactory.Locate(AppContext.BaseDirectory)!;
        var source = Path.Combine(paths.FrameworkRoot, "tools", "java-helper");
        var copy = Path.Combine(Path.GetTempPath(), "java-helper-build-" + Guid.NewGuid().ToString("N"));
        try
        {
            File.Copy(Path.Combine(source, "pom.xml"), Path.Combine(Directory.CreateDirectory(copy).FullName, "pom.xml"));
            foreach (var file in Directory.GetFiles(Path.Combine(source, "src", "main"), "*", SearchOption.AllDirectories))
            {
                var target = Path.Combine(copy, Path.GetRelativePath(source, file));
                Directory.CreateDirectory(Path.GetDirectoryName(target)!);
                File.Copy(file, target);
            }

            var options = JavaHelperOptions.Default(paths, machine: null) with { ProjectDir = copy };
            await using var client = new JavaHelperClient(options, new ProcessRunner(), new FileLogRepository(paths));
            Assert.True(client.NeedsBuild());
            Assert.Equal("1.0.0", await client.PingAsync(CancellationToken.None)); // tự build rồi chạy
            Assert.True(File.Exists(client.JarPath));
            Assert.False(client.NeedsBuild());

            File.SetLastWriteTimeUtc(Path.Combine(copy, "pom.xml"), DateTime.UtcNow.AddMinutes(1));
            Assert.True(client.NeedsBuild());
        }
        finally
        {
            Directory.Delete(copy, recursive: true);
        }
    }
}

public class ProcessRunnerTests
{
    private static string Java
    {
        get
        {
            var paths = ServiceFactory.Locate(AppContext.BaseDirectory)!;
            return ToolLocator.JavaExecutable(ToolLocator.FindJdkHome(17, null, paths.ArrowRoot)!);
        }
    }

    [Fact]
    public async Task CapturesOutputExitCodeAndEnvironment()
    {
        var result = await new ProcessRunner().RunAsync(
            new ProcessSpec(Java, ["-version"], Path.GetTempPath(), new Dictionary<string, string?> { ["JAVA_TOOL_OPTIONS"] = null }),
            CancellationToken.None);
        Assert.True(result.Succeeded);
        Assert.Contains("version \"17", result.Output);
        Assert.DoesNotContain("Picked up JAVA_TOOL_OPTIONS", result.Output);

        var failed = await new ProcessRunner().RunAsync(new ProcessSpec(Java, ["-no-such-flag"], Path.GetTempPath()), CancellationToken.None);
        Assert.False(failed.Succeeded);
        Assert.NotEqual(0, failed.ExitCode);
        Assert.NotEmpty(failed.Tail(3));
    }

    [Fact]
    public async Task TimeoutKillsProcess()
    {
        // Lệnh ngủ 30 s của hệ điều hành, timeout 1 s: phải bị giết, không có mã thoát.
        var spec = OperatingSystem.IsWindows()
            ? new ProcessSpec("ping", ["-n", "30", "127.0.0.1"], Path.GetTempPath(), Timeout: TimeSpan.FromSeconds(1))
            : new ProcessSpec("/bin/sleep", ["30"], Path.GetTempPath(), Timeout: TimeSpan.FromSeconds(1));
        var watch = Stopwatch.StartNew();
        var result = await new ProcessRunner().RunAsync(spec, CancellationToken.None);
        Assert.True(result.TimedOut);
        Assert.Null(result.ExitCode);
        Assert.False(result.Succeeded);
        Assert.True(watch.Elapsed < TimeSpan.FromSeconds(15));
    }

    [Fact]
    public async Task CancellationKillsProcessAndThrows()
    {
        var spec = OperatingSystem.IsWindows()
            ? new ProcessSpec("ping", ["-n", "30", "127.0.0.1"], Path.GetTempPath())
            : new ProcessSpec("/bin/sleep", ["30"], Path.GetTempPath());
        using var cancel = new CancellationTokenSource(TimeSpan.FromSeconds(1));
        await Assert.ThrowsAnyAsync<OperationCanceledException>(() => new ProcessRunner().RunAsync(spec, cancel.Token));
    }
}
