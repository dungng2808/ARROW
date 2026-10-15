using System.Xml.Linq;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block3;
using Arrow.Service.External;

namespace Arrow.Service.Tests;

/// <summary>Phân loại lỗi và signature của khối 3 theo spec 03 Mục 3.2–3.4, 5, 14.3, 15.1.</summary>
public class Block3ClassificationTests
{
    // ── Mục 3.2: lỗi javac ───────────────────────────────────────────

    [Fact]
    public void ParsesJavacErrorsOnEveryPathStyle()
    {
        const string log = """
            [INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ shop-core ---
            [WARNING] /Users/a/ws/shop-core/src/test/java/com/acme/shop/OldTest.java:[3,8] [deprecation] Foo in bar has been deprecated
            [ERROR] COMPILATION ERROR :
            [ERROR] /Users/a/ws/shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java:[14,30] cannot find symbol
              symbol:   method getTotal(long)
              location: variable service of type com.acme.shop.OrderService
            [ERROR] /D:/ws/shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java:[20,12] incompatible types: java.lang.String cannot be converted to long
            [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:testCompile (default-testCompile) on project shop-core: Compilation failure: Compilation failure:
            [ERROR] /Users/a/ws/shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java:[14,30] cannot find symbol
            [ERROR]   symbol:   method getTotal(long)
            [ERROR]   location: variable service of type com.acme.shop.OrderService
            D:\ws\core\src\test\java\com\acme\Helper.java:7: error: package org.junit.jupiter.api does not exist
            import org.junit.jupiter.api.Test;
                                        ^
            /home/u/ws/core/src/test/java/com/acme/OrderService_ArrowTest.java:31: error: constructor OrderService in class OrderService cannot be applied to given types;
                    new OrderService();
                    ^
              required: TaxPolicy
              found:    no arguments
            /home/u/ws/core/src/test/java/com/acme/OrderService_ArrowTest.java:40: warning: [unchecked] unchecked call
                at com.acme.OrderService.totalPrice(OrderService.java:31)
            """;
        var errors = JavacErrorParser.Parse(log, "OrderService_ArrowTest.java");
        Assert.Equal(4, errors.Count); // lỗi Maven in hai lần chỉ tính một lần; cảnh báo và stack frame bị bỏ

        Assert.Equal((14, 30, "cannot find symbol", "method", "getTotal(long)"), (errors[0].Line, errors[0].Column!.Value, errors[0].Kind, errors[0].SymbolKind!, errors[0].Symbol!));
        Assert.Equal("variable service of type com.acme.shop.OrderService", errors[0].Location);
        Assert.True(errors[0].InGeneratedFile);
        Assert.Equal(("/D:/ws/shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java", "incompatible types"), (errors[1].File, errors[1].Kind));
        Assert.Equal((7, (int?)null, "package does not exist", false), (errors[2].Line, errors[2].Column, errors[2].Kind, errors[2].InGeneratedFile));
        Assert.Equal(("constructor cannot be applied", 31), (errors[3].Kind, errors[3].Line));
    }

    [Fact]
    public void ParsesTruncatedOutputOfMavenCompilerPlugin2()
    {
        // maven-compiler-plugin 2.3.2 (task 149270308): tiền tố "error:" trong thông điệp, mất dòng symbol, location không có nhãn.
        const string log = """
            [ERROR] /Users/a/ws/flexbox/src/test/java/com/d/FlexboxLayout_ArrowTest.java:[64,43] error: x has private access in Bounds
            [ERROR] /Users/a/ws/flexbox/src/test/java/com/d/FlexboxLayout_ArrowTest.java:[65,44] error: cannot find symbol
            [ERROR]  class Bounds
            [ERROR] /Users/a/ws/flexbox/src/test/java/com/d/FlexboxLayout_ArrowTest.java:[70,9] error: com.d.Shape is abstract; cannot be instantiated
            """;
        var errors = JavacErrorParser.Parse(log, "FlexboxLayout_ArrowTest.java");
        Assert.Equal(3, errors.Count);
        Assert.Equal(("x has private access in Bounds", "has private access", (string?)null), (errors[0].Message, errors[0].Kind, errors[0].Location));
        Assert.Equal(("cannot find symbol", "cannot find symbol", "class Bounds"), (errors[1].Message, errors[1].Kind, errors[1].Location));
        Assert.Equal(["Bounds"], JavacErrorParser.MentionedTypes(errors[0]));
        Assert.Equal(["Bounds"], JavacErrorParser.MentionedTypes(errors[1]));
        Assert.Equal(["com.d.Shape"], JavacErrorParser.MentionedTypes(errors[2]));
    }

    [Theory]
    [InlineData("cannot find symbol", "cannot find symbol")]
    [InlineData("incompatible types: String cannot be converted to long", "incompatible types")]
    [InlineData("unreported exception java.io.IOException; must be caught or declared to be thrown", "unreported exception")]
    [InlineData("method apply in class TaxPolicy cannot be applied to given types;", "method cannot be applied")]
    [InlineData("no suitable method found for of(int)", "method cannot be applied")]
    [InlineData("constructor OrderService in class OrderService cannot be applied to given types;", "constructor cannot be applied")]
    [InlineData("no suitable constructor found for OrderService(int)", "constructor cannot be applied")]
    [InlineData("package org.assertj.core.api does not exist", "package does not exist")]
    [InlineData("tax has private access in com.acme.shop.OrderService", "has private access")]
    [InlineData("com.acme.Internal is not public in com.acme; cannot be accessed from outside package", "is not public")]
    [InlineData("non-static method total() cannot be referenced from a static context", "cannot be referenced from a static context")]
    [InlineData("variable service is already defined in method setUp()", "already defined")]
    [InlineData("missing return statement", "missing return statement")]
    [InlineData("unreachable statement", "unreachable statement")]
    [InlineData("com.acme.Shape is abstract; cannot be instantiated", "class is abstract")]
    [InlineData("method does not override or implement a method from a supertype", "does not override")]
    [InlineData("cannot access com.acme.Hidden", "cannot be accessed")]
    [InlineData("';' expected", "other")]
    public void NormalizesJavacKinds(string message, string kind) => Assert.Equal(kind, JavacErrorParser.Kind(message));

    // ── Mục 3.3: kết quả chạy riêng test sinh ra ─────────────────────

    private const string Fqn = "com.acme.shop.OrderService_ArrowTest";

    private static TestCaseResult T(string name, TestCaseStatus status, string? type = null, string? message = null, string? trace = null, string cls = Fqn) =>
        new() { ClassName = cls, Name = name, Status = status, ExceptionType = type, Message = message, Trace = trace };

    private const string NpeTrace = """
        java.lang.NullPointerException: Cannot invoke "com.acme.shop.api.TaxPolicy.apply(long)" because "this.tax" is null
        	at com.acme.shop.OrderService.totalPrice(OrderService.java:31)
        	at com.acme.shop.OrderService_ArrowTest.addsTax(OrderService_ArrowTest.java:14)
        	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        """;

    [Fact]
    public void ClassifiesDiscoveryRuntimeAssertionAndPass()
    {
        Assert.Equal(VerificationState.Discovery, TestResultClassifier.Classify([T("other", TestCaseStatus.Passed, cls: "com.acme.OtherTest")], Fqn).FailedState);
        Assert.Equal(VerificationState.Discovery, TestResultClassifier.Classify([T("initializationError", TestCaseStatus.Error, "java.lang.Exception", "No runnable methods")], Fqn).FailedState);
        Assert.Equal(VerificationState.Discovery, TestResultClassifier.Classify([T("skipped", TestCaseStatus.Skipped)], Fqn).FailedState);

        var runtime = TestResultClassifier.Classify([T("addsTax", TestCaseStatus.Error, "java.lang.NullPointerException", trace: NpeTrace), T("rejects", TestCaseStatus.Failed, "java.lang.AssertionError"), T("ok", TestCaseStatus.Passed)], Fqn);
        Assert.Equal(VerificationState.Runtime, runtime.FailedState);
        Assert.Equal((3, 1, 2), (runtime.Counts.Total, runtime.Counts.Passed, runtime.Counts.Failed));
        Assert.Equal(["addsTax", "rejects"], runtime.FailingMethods);
        Assert.Equal("OrderService.totalPrice", runtime.Failures[0].Frame);
        Assert.Equal(4, runtime.Failures[0].TraceHead!.Split('\n').Length);

        var assertion = TestResultClassifier.Classify([T("totalPriceAddsTax", TestCaseStatus.Failed, "org.opentest4j.AssertionFailedError", "expected: <110> but was: <100>"), T("ok", TestCaseStatus.Passed)], Fqn);
        Assert.Equal(VerificationState.Assertion, assertion.FailedState);
        Assert.Equal(["totalPriceAddsTax"], assertion.FailingMethods);

        // Lỗi khởi tạo class (không phải "No runnable methods") là RUNTIME.
        Assert.Equal(VerificationState.Runtime, TestResultClassifier.Classify([T("initializationError", TestCaseStatus.Error, "java.lang.ExceptionInInitializerError")], Fqn).FailedState);

        var pass = TestResultClassifier.Classify([T("a", TestCaseStatus.Passed), T("b", TestCaseStatus.Passed, cls: Fqn + "$Nested")], Fqn);
        Assert.Null(pass.FailedState);
        Assert.Equal(2, pass.Counts.Passed);
    }

    [Theory]
    [InlineData("java.lang.AssertionError", null, true)]
    [InlineData("org.opentest4j.AssertionFailedError", null, true)]
    [InlineData("org.junit.ComparisonFailure", null, true)]
    [InlineData("junit.framework.AssertionFailedError", null, true)]
    [InlineData("org.opentest4j.MultipleFailuresError", null, true)]
    [InlineData("org.assertj.core.error.AssertJMultipleFailuresError", null, true)]
    [InlineData("org.mockito.exceptions.verification.WantedButNotInvoked", null, true)]
    [InlineData("org.testng.TestException", "Method OrderServiceTest.rejects() should have thrown an exception of class IllegalStateException", true)]
    [InlineData("org.testng.TestException", "Cannot instantiate class", false)]
    [InlineData("java.lang.NullPointerException", null, false)]
    [InlineData("org.mockito.exceptions.misusing.UnfinishedStubbingException", null, false)]
    public void RecognizesAssertionExceptions(string type, string? message, bool expected) => Assert.Equal(expected, TestResultClassifier.IsAssertion(type, message));

    [Fact]
    public void ExceptionNameFallsBackToTraceWhenTypeIsAMessage()
    {
        Assert.Equal("java.lang.IllegalStateException", TestResultClassifier.ExceptionName("java.lang.IllegalStateException", null));
        Assert.Equal("java.lang.IllegalArgumentException", TestResultClassifier.ExceptionName("amount < 0", "java.lang.IllegalArgumentException: amount < 0\n\tat x.Y.z(Y.java:1)"));
        Assert.Equal("java.io.IOException", TestResultClassifier.ExceptionName("wrapped", "something odd\n\tat a.B.c(B.java:1)\nCaused by: java.io.IOException: disk"));
        Assert.Equal("unknown", TestResultClassifier.ExceptionName(null, null));
        Assert.Null(TestResultClassifier.ProjectFrame("java.lang.X\n\tat java.base/java.util.Objects.requireNonNull(Objects.java:208)\n\tat org.junit.Assert.fail(Assert.java:1)"));
        Assert.Equal("Parser$Inner.lambda$parse$0", TestResultClassifier.ProjectFrame("e\n\tat app//com.acme.Parser$Inner.lambda$parse$0(Parser.java:9)"));
    }

    // ── Mục 3.4, 15.1: module regression ─────────────────────────────

    [Fact]
    public void FindsRegressions()
    {
        string[] baseline = ["com.acme.InvoiceTest#appliesDefaultTax", "com.acme.CartTest#adds", "com.acme.CartTest#removes"];
        IReadOnlyList<TestCaseResult> module =
        [
            T("appliesDefaultTax", TestCaseStatus.Failed, "java.lang.AssertionError", "expected:<10> but was:<20>", cls: "com.acme.InvoiceTest"),
            T("adds", TestCaseStatus.Passed, cls: "com.acme.CartTest"),
            T("now", TestCaseStatus.Failed, "java.lang.AssertionError", cls: "com.acme.ClockTest"), // flaky ở baseline: không xét
            T("addsTax", TestCaseStatus.Passed),
        ];
        var regressions = TestResultClassifier.FindRegressions(baseline, module, Fqn);
        Assert.Equal(["com.acme.InvoiceTest#appliesDefaultTax", "com.acme.CartTest#removes"], regressions.Select(r => r.TestId));
        Assert.Equal(["java.lang.AssertionError", "NOT_RUN"], regressions.Select(r => r.Type));
        Assert.Equal("REGRESSION | existing tests fail | CartTest,InvoiceTest", SignatureBuilder.Regression(regressions));

        // R5: test sinh ra pass khi chạy riêng nhưng fail khi chạy chung.
        var own = TestResultClassifier.FindRegressions([], [T("addsTax", TestCaseStatus.Failed, "java.lang.AssertionError")], Fqn);
        Assert.Equal("REGRESSION | existing tests fail | OrderService_ArrowTest", SignatureBuilder.Regression(own));
        Assert.Empty(TestResultClassifier.FindRegressions(["com.acme.CartTest#adds"], [T("adds", TestCaseStatus.Passed, cls: "com.acme.CartTest"), T("x", TestCaseStatus.Passed)], Fqn));
    }

    // ── Mục 5: signature ─────────────────────────────────────────────

    [Fact]
    public void BuildsSignaturesForEveryState()
    {
        List<CompileError> compile =
        [
            new() { File = "Helper.java", Kind = "package does not exist" },
            new() { File = "OrderService_ArrowTest.java", InGeneratedFile = true, Kind = "cannot find symbol", SymbolKind = "method" },
        ];
        Assert.Equal("COMPILE | cannot find symbol | method", SignatureBuilder.Compile(compile));
        Assert.Equal("COMPILE | incompatible types | -", SignatureBuilder.Compile([new CompileError { Kind = "incompatible types" }]));
        Assert.Equal("UNKNOWN | maven-enforcer-plugin | -", SignatureBuilder.Unknown(MavenEnforcerLog));
        Assert.Equal("UNKNOWN | compileTestJava | -", SignatureBuilder.Unknown(GradleLog));
        Assert.Equal("DISCOVERY | no tests executed | -", SignatureBuilder.Discovery());

        var npe = TestResultClassifier.ToFailure(T("addsTax", TestCaseStatus.Error, "java.lang.NullPointerException", trace: NpeTrace));
        Assert.Equal("RUNTIME | NullPointerException | OrderService.totalPrice", SignatureBuilder.Runtime([npe]));
        var failed = TestResultClassifier.ToFailure(T("testGetOneNotFound", TestCaseStatus.Failed, "org.opentest4j.AssertionFailedError"));
        Assert.Equal("ASSERTION | AssertionFailedError | testGetOneNotFound", SignatureBuilder.Assertion([failed]));
    }

    private const string MavenEnforcerLog = """
        [INFO] BUILD FAILURE
        [INFO] ------------------------------------------------------------------------
        [ERROR] Failed to execute goal org.apache.maven.plugins:maven-enforcer-plugin:3.0.0:enforce (enforce-versions) on project shop-core:
        [ERROR] Rule 0: org.apache.maven.plugins.enforcer.RequireJavaVersion failed with message:
        [ERROR] Detected JDK /work/ws/copy-1/jdk is version 1.8.0 which is not in the allowed range [11,).
        [ERROR] Rule 0: org.apache.maven.plugins.enforcer.RequireJavaVersion failed with message:
        [ERROR] -> [Help 1]
        [ERROR]
        [ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
        """;

    private const string GradleLog = """
        > Task :core:compileTestJava FAILED

        FAILURE: Build failed with an exception.

        * What went wrong:
        Execution failed for task ':core:compileTestJava'.
        > error: annotation processor 'lombok' threw /work/ws/copy-1/core/Gen.java
          Caused by: java.lang.IllegalStateException: boom

        * Try:
        > Run with --stacktrace option to get the stack trace.

        BUILD FAILED in 3s
        """;

    [Fact]
    public void ExtractsBuildErrorBlocks()
    {
        var maven = BuildErrorExtractor.Excerpt(MavenEnforcerLog, "/work/ws/copy-1");
        Assert.Equal("""
            [ERROR] Failed to execute goal org.apache.maven.plugins:maven-enforcer-plugin:3.0.0:enforce (enforce-versions) on project shop-core:
            [ERROR] Rule 0: org.apache.maven.plugins.enforcer.RequireJavaVersion failed with message:
            [ERROR] Detected JDK jdk is version 1.8.0 which is not in the allowed range [11,).
            """.Replace("\r\n", "\n"), maven);

        var gradle = BuildErrorExtractor.Excerpt(GradleLog, "/work/ws/copy-1");
        Assert.Contains("Execution failed for task ':core:compileTestJava'.", gradle);
        Assert.Contains("threw core/Gen.java", gradle);
        Assert.Contains("Caused by: java.lang.IllegalStateException: boom", gradle);
        Assert.DoesNotContain("--stacktrace", gradle);
        Assert.Equal("build", BuildErrorExtractor.FailingStep("something else"));
        Assert.Equal(2, BuildErrorExtractor.Excerpt(MavenEnforcerLog, null, maxLines: 2).Split('\n').Length);
    }

    // ── Báo cáo XML: stack trace và tên method Gradle JUnit 5 ──────────

    [Fact]
    public void ReaderKeepsTraceAndStripsGradleParentheses()
    {
        var tests = TestReportReader.Parse(XDocument.Parse("""
            <testsuite name="com.acme.calc.CalculatorTest">
              <testcase name="adds()" classname="com.acme.calc.CalculatorTest"/>
              <testcase name="divides()" classname="com.acme.calc.CalculatorTest">
                <failure message="/ by zero" type="java.lang.ArithmeticException">java.lang.ArithmeticException: / by zero
            	at com.acme.calc.Calculator.divide(Calculator.java:9)</failure>
              </testcase>
              <testcase name="sum(int)[1]" classname="com.acme.calc.CalculatorTest"/>
            </testsuite>
            """));
        Assert.Equal(["adds", "divides", "sum(int)[1]"], tests.Select(t => t.Name));
        Assert.StartsWith("java.lang.ArithmeticException: / by zero", tests[1].Trace, StringComparison.Ordinal);
        Assert.Null(tests[0].Trace);
        Assert.Equal("Calculator.divide", TestResultClassifier.ProjectFrame(tests[1].Trace));
    }
}
