using Arrow.Core.Prepare;
using Arrow.Core.Verify;

namespace Arrow.Core.Tests;

public class FailureClassifierTests
{
    [Fact]
    public void ParsesMavenCompileErrorsWithSymbol()
    {
        const string log = """
            [ERROR] COMPILATION ERROR :
            [ERROR] /ws/app/src/test/java/a/PairService_ArrowTest.java:[42,15] cannot find symbol
              symbol:   method getTotal(long)
              location: class a.PairService
            [ERROR] /ws/app/src/test/java/a/PairService_ArrowTest.java:[3,1] package org.x does not exist
            [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:testCompile (default-testCompile) on project app: Compilation failure
            [ERROR] /ws/app/src/test/java/a/PairService_ArrowTest.java:[42,15] cannot find symbol
            """;
        var errors = FailureClassifier.ParseCompileErrors(log);
        Assert.Equal(2, errors.Count);
        Assert.Equal(42, errors[0].Line);
        Assert.Equal(15, errors[0].Column);
        Assert.Equal("cannot find symbol", errors[0].Kind);
        Assert.Equal("method", errors[0].SymbolKind);
        Assert.Equal("method getTotal(long)", errors[0].Symbol);
        Assert.Equal("class a.PairService", errors[0].Location);
        Assert.Equal("package does not exist", errors[1].Kind);
        Assert.Equal("COMPILE | cannot find symbol | method", FailureClassifier.CompileSignature(errors));
        Assert.True(FailureClassifier.HasCompilationFailure(log));
        Assert.Equal(2, FailureClassifier.ErrorsInFile(errors, "PairService_ArrowTest").Count);
        Assert.Empty(FailureClassifier.ErrorsInFile(errors, "Other_ArrowTest"));
    }

    [Fact]
    public void ParsesGradleJavacErrors()
    {
        const string log = """
            > Task :compileTestJava FAILED
            /ws/src/test/java/a/Cart_ArrowTest.java:10: error: incompatible types: String cannot be converted to int
                    int x = "a";
                            ^
            1 error
            FAILURE: Build failed with an exception.
            * What went wrong:
            Execution failed for task ':compileTestJava'.
            """;
        var error = Assert.Single(FailureClassifier.ParseCompileErrors(log));
        Assert.Equal(10, error.Line);
        Assert.Null(error.Column);
        Assert.Equal("incompatible types", error.Kind);
        Assert.Null(error.SymbolKind);
        Assert.Equal("COMPILE | incompatible types | -", FailureClassifier.CompileSignature([error]));
    }

    [Fact]
    public void AssertionErrorLineIsNotCompileError()
    {
        const string log = """
            [INFO] Running a.FooTest
            java.lang.AssertionError: expected:<6> but was:<5>
            [ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
            """;
        Assert.False(FailureClassifier.HasCompilationFailure(log));
        Assert.Empty(FailureClassifier.ParseCompileErrors(log));
    }

    [Theory]
    [InlineData("cannot find symbol", "cannot find symbol")]
    [InlineData("incompatible types: possible lossy conversion from long to int", "incompatible types")]
    [InlineData("unreported exception java.io.IOException; must be caught or declared to be thrown", "unreported exception")]
    [InlineData("method add in class Calc cannot be applied to given types;", "method cannot be applied")]
    [InlineData("constructor Calc in class Calc cannot be applied to given types;", "constructor cannot be applied")]
    [InlineData("package org.mockito does not exist", "package does not exist")]
    [InlineData("value has private access in a.B", "has private access")]
    [InlineData("a.B is not public in a; cannot be accessed from outside package", "is not public")]
    [InlineData("non-static method f() cannot be referenced from a static context", "cannot be referenced from a static context")]
    [InlineData("variable x is already defined in method m()", "already defined")]
    [InlineData("missing return statement", "missing return statement")]
    [InlineData("unreachable statement", "unreachable statement")]
    [InlineData("a.Shape is abstract; cannot be instantiated", "class is abstract")]
    [InlineData("method does not override or implement a method from a supertype", "does not override")]
    [InlineData("';' expected", "other")]
    public void NormalizesJavacKinds(string message, string kind) => Assert.Equal(kind, FailureClassifier.JavacKind(message));

    private static TestCaseResult Failed(string id, string type, string message = "m", string? detail = null) =>
        new() { Id = id, Outcome = TestOutcome.Fail, Type = type, Message = message, Detail = detail };

    [Fact]
    public void SeparatesAssertionFromRuntime()
    {
        Assert.True(FailureClassifier.IsAssertionFailure(Failed("a.T#x", "org.opentest4j.AssertionFailedError")));
        Assert.True(FailureClassifier.IsAssertionFailure(Failed("a.T#x", "org.assertj.core.error.AssertJMultipleFailuresError")));
        Assert.True(FailureClassifier.IsAssertionFailure(Failed("a.T#x", "org.mockito.exceptions.verification.WantedButNotInvoked")));
        Assert.True(FailureClassifier.IsAssertionFailure(Failed("a.T#x", "org.opentest4j.AssertionFailedError", "Expected java.lang.IllegalStateException to be thrown, but nothing was thrown.")));
        Assert.True(FailureClassifier.IsAssertionFailure(Failed("a.T#x", "java.lang.AssertionError", "Expected exception: java.lang.IllegalStateException")));
        Assert.False(FailureClassifier.IsAssertionFailure(Failed("a.T#x", "java.lang.NullPointerException")));
        Assert.False(FailureClassifier.IsAssertionFailure(Failed("a.T#x", "org.junit.runners.model.TestTimedOutException", "test timed out after 100 milliseconds")));

        Assert.Equal(VerificationState.Assertion, FailureClassifier.TargetFailureState([Failed("a.T#x", "java.lang.AssertionError")]));
        Assert.Equal(VerificationState.Runtime, FailureClassifier.TargetFailureState([Failed("a.T#x", "java.lang.AssertionError"), Failed("a.T#y", "java.lang.IllegalStateException")]));
    }

    [Fact]
    public void RuntimeSignatureUsesFirstProjectFrame()
    {
        var failure = FailureClassifier.ToFailure(Failed("a.OrderService_ArrowTest#total", "java.lang.NullPointerException", "boom", """
            java.lang.NullPointerException: boom
                at java.base/java.util.Objects.requireNonNull(Objects.java:209)
                at a.OrderService.totalPrice(OrderService.java:31)
                at a.OrderService_ArrowTest.total(OrderService_ArrowTest.java:12)
            """));
        Assert.Equal("total", failure.Method);
        Assert.Equal("RUNTIME | NullPointerException | OrderService.totalPrice",
            FailureClassifier.RuntimeSignature(failure, new HashSet<string> { "a.OrderService" }));
        Assert.Equal("RUNTIME | NullPointerException | -", FailureClassifier.RuntimeSignature(failure, new HashSet<string>()));
    }

    [Fact]
    public void SignaturesForOtherStates()
    {
        var assertion = FailureClassifier.ToFailure(Failed("a.T#testGetOneNotFound", "org.opentest4j.AssertionFailedError"));
        Assert.Equal("ASSERTION | AssertionFailedError | testGetOneNotFound", FailureClassifier.AssertionSignature(assertion));
        Assert.Equal("REGRESSION | existing tests fail | ATest,BTest",
            FailureClassifier.RegressionSignature([new NewFailure { Id = "x.BTest#m" }, new NewFailure { Id = "x.ATest#n" }, new NewFailure { Id = "x.BTest#k" }]));
        Assert.Equal("DISCOVERY | no tests executed | -", FailureClassifier.DiscoverySignature());
        Assert.Equal("UNKNOWN | maven-surefire-plugin | -",
            FailureClassifier.UnknownSignature("[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test (default-test) on project x"));
        Assert.Equal("UNKNOWN | task compileTestJava | -", FailureClassifier.UnknownSignature("Execution failed for task ':core:compileTestJava'."));
        Assert.Equal("UNKNOWN | Something broke at <path> line <n> | -", FailureClassifier.UnknownSignature("[ERROR] Something broke at /a/b/c.txt line 12"));
    }

    [Fact]
    public void RegressionComparesWithPassingBaseline()
    {
        var baseline = new Baseline { PassingInAllRuns = ["a.ATest#ok", "a.ATest#gone", "a.BTest#breaks", "a.X_ArrowTest#self"] };
        var run = new List<TestCaseResult>
        {
            new() { Id = "a.ATest#ok", Outcome = TestOutcome.Pass },
            new() { Id = "a.BTest#breaks", Outcome = TestOutcome.Error, Type = "java.lang.IllegalStateException" },
            new() { Id = "a.Flaky#f", Outcome = TestOutcome.Fail },
        };
        var failures = FailureClassifier.NewFailures(baseline, run, "a.X_ArrowTest");
        Assert.Equal(["a.ATest#gone", "a.BTest#breaks"], failures.Select(f => f.Id));
        Assert.Equal("MISSING", failures[0].Type);
    }

    [Fact]
    public void JUnit4NoRunnableMethodsIsDiscoveryMarker()
    {
        Assert.True(FailureClassifier.IsNoTestsMarker(Failed("a.T#initializationError", "java.lang.Exception", "No runnable methods")));
        Assert.False(FailureClassifier.IsNoTestsMarker(Failed("a.T#x", "java.lang.Exception", "No runnable methods")));
    }

    [Fact]
    public void InfraDetection()
    {
        Assert.True(FailureClassifier.IsDependencyDownloadError("[ERROR] Failed to execute goal on project x: Could not resolve dependencies for project"));
        Assert.True(FailureClassifier.IsToolError(null, false, ""));
        Assert.False(FailureClassifier.IsToolError(1, false, "[ERROR] BUILD FAILURE"));
    }
}

public class CodeHasherTests
{
    [Fact]
    public void HashIgnoresWhitespaceAndLineEndings()
    {
        var a = CodeHasher.Hash("class A {\r\n  int x;   }\n");
        var b = CodeHasher.Hash("class A { int x; }");
        Assert.Equal(a, b);
        Assert.StartsWith("sha256:", a);
        Assert.NotEqual(a, CodeHasher.Hash("class A { int y; }"));
    }

    [Fact]
    public void StateOrderAndGroups()
    {
        Assert.True(States.Rank(VerificationState.Compile) < States.Rank(VerificationState.Unknown));
        Assert.True(States.Rank(VerificationState.Regression) < States.Rank(VerificationState.Valid));
        Assert.Equal("BUILD", States.Group(VerificationState.Discovery));
        Assert.Equal("RUNTIME", States.Group(VerificationState.Regression));
        Assert.Null(States.Group(VerificationState.Valid));
        Assert.Throws<ArgumentException>(() => States.Rank(VerificationState.Infra));
    }
}
