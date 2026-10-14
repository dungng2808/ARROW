using Arrow.Models.Block1;
using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block3;

namespace Arrow.Service.Tests;

/// <summary>Flow 3.3 (spec 03 Mục 5): Diagnosis và error block cho từng trạng thái.</summary>
public class FailureDiagnoserTests
{
    private static readonly RepositoryMetadata M = new()
    {
        TestFramework = new TestFrameworkInfo { Name = "junit4" },
        Paths = new PathsInfo { GeneratedTestFqn = "com.acme.shop.OrderService_ArrowTest" },
    };

    private static CandidateTest Candidate(string code = "") => new()
    {
        TestFqn = "com.acme.shop.OrderService_ArrowTest",
        TestPath = "shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java",
        Code = code,
        CodeHash = "sha256:from-block2",
    };

    private readonly FailureDiagnoser _diagnoser = new();

    [Fact]
    public void CompileBlockShowsErrorsSymbolsAndCode()
    {
        const string code = "package com.acme.shop;\nclass OrderService_ArrowTest {\n  @Test void totalPriceAddsTax() {\n    long t = service.getTotal(1L);\n  }\n}\n";
        var result = new VerifyResult
        {
            State = VerificationState.Compile,
            ErrorSignature = "COMPILE | cannot find symbol | method",
            FailingMethods = ["totalPriceAddsTax"],
            CodeHash = "sha256:verify",
            CompileErrors =
            [
                new CompileError { File = "shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java", InGeneratedFile = true, Line = 4, Column = 22, Message = "cannot find symbol", Kind = "cannot find symbol", SymbolKind = "method", Symbol = "getTotal(long)", Location = "variable service of type com.acme.shop.OrderService" },
                new CompileError { File = "shop-core/src/main/java/com/acme/shop/Other.java", Line = 9, Message = "cannot find symbol", Kind = "cannot find symbol" },
            ],
        };
        var diagnosis = _diagnoser.Diagnose(result, Candidate(code), M);
        Assert.Equal((VerificationState.Compile, ErrorGroup.Build), (diagnosis.State, diagnosis.Group!.Value));
        Assert.Equal("COMPILE | cannot find symbol | method", diagnosis.ErrorSignature);
        Assert.Equal("sha256:from-block2", diagnosis.CodeHash);
        Assert.Equal(["totalPriceAddsTax"], diagnosis.FailingMethods);
        Assert.Equal("""
            shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java:4:22: error: cannot find symbol
              symbol: method getTotal(long)
              location: variable service of type com.acme.shop.OrderService
              code: long t = service.getTotal(1L);
            """.Replace("\r\n", "\n").TrimEnd(), diagnosis.ErrorBlock);
        Assert.Equal("shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java:4: cannot find symbol", diagnosis.FirstError);
    }

    [Fact]
    public void UnknownAndDiscoveryBlocks()
    {
        var unknown = _diagnoser.Diagnose(new VerifyResult
        {
            State = VerificationState.Unknown,
            ErrorSignature = "UNKNOWN | maven-enforcer-plugin | -",
            BuildErrorExcerpt = "[ERROR] Failed to execute goal org.apache.maven.plugins:maven-enforcer-plugin:3.0.0:enforce\n[ERROR] Rule 0 failed",
            FailingMethods = ["ignored"],
        }, Candidate(), M);
        Assert.Equal(ErrorGroup.Build, unknown.Group);
        Assert.Empty(unknown.FailingMethods);
        Assert.StartsWith("[ERROR] Failed to execute goal", unknown.ErrorBlock, StringComparison.Ordinal);
        Assert.Equal("[ERROR] Failed to execute goal org.apache.maven.plugins:maven-enforcer-plugin:3.0.0:enforce", unknown.FirstError);

        var discovery = _diagnoser.Diagnose(new VerifyResult { State = VerificationState.Discovery, ErrorSignature = "DISCOVERY | no tests executed | -", TestFile = "shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java" }, Candidate(), M);
        Assert.Equal("The test runner executed 0 test methods of com.acme.shop.OrderService_ArrowTest (file shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java). The project runs tests with JUnit 4.", discovery.ErrorBlock);
        Assert.Equal("0 tests executed", discovery.FirstError);
    }

    [Fact]
    public void RuntimeAndAssertionBlocksListFailingMethods()
    {
        var failures = Enumerable.Range(1, 7).Select(i => new FailureInfo
        {
            Method = $"test{i}",
            Type = "java.lang.NullPointerException",
            Message = "Cannot invoke \"TaxPolicy.apply(long)\" because \"this.tax\" is null\nsecond line",
            TraceHead = "java.lang.NullPointerException: x\n\tat com.acme.shop.OrderService.totalPrice(OrderService.java:31)\n\tat com.acme.shop.OrderService_ArrowTest.test1(OrderService_ArrowTest.java:14)",
        }).ToList();
        var runtime = _diagnoser.Diagnose(new VerifyResult { State = VerificationState.Runtime, Failures = failures, FailingMethods = failures.Select(f => f.Method).ToList(), ErrorSignature = "RUNTIME | NullPointerException | OrderService.totalPrice" }, Candidate(), M);
        Assert.Equal(ErrorGroup.Runtime, runtime.Group);
        Assert.Equal(7, runtime.FailingMethods.Count);
        var lines = runtime.ErrorBlock.Split('\n');
        Assert.Equal("Test method: test1", lines[0]);
        Assert.Equal("Exception: java.lang.NullPointerException: Cannot invoke \"TaxPolicy.apply(long)\" because \"this.tax\" is null", lines[1]);
        Assert.Equal("  at com.acme.shop.OrderService.totalPrice(OrderService.java:31)", lines[2]);
        Assert.Equal(5, lines.Count(l => l.StartsWith("Test method:", StringComparison.Ordinal)));
        Assert.Equal("… and 2 more failing test methods", lines[^1]);

        var assertion = _diagnoser.Diagnose(new VerifyResult
        {
            State = VerificationState.Assertion,
            Failures = [new FailureInfo { Method = "totalPriceAddsTax", Type = "java.lang.AssertionError", Message = "expected:<110> but was:<100>" }],
            FailingMethods = ["totalPriceAddsTax"],
        }, Candidate(), M);
        Assert.Equal(ErrorGroup.Assertion, assertion.Group);
        Assert.Equal("Test method: totalPriceAddsTax\nException: java.lang.AssertionError: expected:<110> but was:<100>", assertion.ErrorBlock);
        Assert.Equal("java.lang.AssertionError: expected:<110> but was:<100>", assertion.FirstError);
    }

    [Fact]
    public void RegressionBlockAndLimits()
    {
        var regression = _diagnoser.Diagnose(new VerifyResult
        {
            State = VerificationState.Regression,
            ErrorSignature = "REGRESSION | existing tests fail | CartTest,InvoiceTest",
            Regressions =
            [
                new RegressionInfo { TestId = "com.acme.InvoiceTest#appliesDefaultTax", Type = "java.lang.AssertionError", Message = "expected:<110> but was:<120>" },
                new RegressionInfo { TestId = "com.acme.CartTest#adds", Type = "NOT_RUN" },
            ],
        }, Candidate(), M);
        Assert.Equal(ErrorGroup.Runtime, regression.Group);
        Assert.Equal("""
            The generated test passes alone, but running the whole module together with it breaks these tests:
            - com.acme.InvoiceTest#appliesDefaultTax: java.lang.AssertionError: expected:<110> but was:<120>
            - com.acme.CartTest#adds: was not run (missing from the report)
            """.Replace("\r\n", "\n").TrimEnd(), regression.ErrorBlock);
        Assert.Equal("com.acme.InvoiceTest#appliesDefaultTax: java.lang.AssertionError", regression.FirstError);

        var valid = _diagnoser.Diagnose(new VerifyResult { State = VerificationState.Valid }, Candidate(), M);
        Assert.Equal(("", (ErrorGroup?)null), (valid.ErrorBlock, valid.Group));

        var long70 = string.Join('\n', Enumerable.Range(1, 70).Select(i => $"line {i}"));
        var limited = FailureDiagnoser.Limit(long70);
        Assert.Equal(61, limited.Split('\n').Length);
        Assert.EndsWith("… (10 more lines)", limited, StringComparison.Ordinal);
    }
}
