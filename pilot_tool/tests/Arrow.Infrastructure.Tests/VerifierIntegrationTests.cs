using Arrow.Core.Verify;
using Arrow.Infrastructure.Verify;

namespace Arrow.Infrastructure.Tests;

/// <summary>Chuỗi xác minh 5 bước trên repository mẫu (specs/03 Mục 9): mỗi fixture phải ra đúng trạng thái, signature, số assertion.</summary>
public class VerifierIntegrationTests
{
    private static async Task<VerifyResult> VerifyAsync(PreparedTask prepared, string code, string key)
    {
        var task = prepared.Task;
        var result = await Env.Candidates.VerifyAsync(task, prepared.Baseline, prepared.Store.RunId, key, prepared.TaskDir, new CandidateSpec
        {
            Code = code,
            TestPath = task.GeneratedTestPath,
            TestClassFqn = task.GeneratedTestClassFqn,
            LogKey = key,
        }, _ => { }, CancellationToken.None);
        return result;
    }

    private static string Junit4(string body, string imports = "") => $$"""
        package com.example.calc;

        import static org.junit.Assert.*;

        import org.junit.Test;
        {{imports}}

        public class Calculator_ArrowTest {
        {{body}}
        }
        """;

    [Fact]
    public async Task CompileErrorInsideMethod()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Test
                public void addsTwo() {
                    assertEquals(3, new Calculator("c").addd(1, 2));
                }
            """), "compile-method");
        Assert.Equal(VerificationState.Compile, r.State);
        Assert.Equal("COMPILE | cannot find symbol | method", r.ErrorSignature);
        Assert.Equal("addsTwo", Assert.Single(r.CompileErrors).Method);
        Assert.Equal(StepStatus.Fail, r.Steps.Compile);
        Assert.Equal("BUILD", r.Group);
        Assert.Equal(1, r.AssertionCount);
    }

    [Fact]
    public async Task CompileErrorInImport()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Test
                public void adds() {
                    assertEquals(3, new Calculator("c").add(1, 2));
                }
            """, "import org.nothing.Missing;"), "compile-import");
        Assert.Equal(VerificationState.Compile, r.State);
        Assert.Equal("COMPILE | package does not exist | -", r.ErrorSignature);
        Assert.Null(r.CompileErrors[0].Method);
    }

    [Fact]
    public async Task NoTestAnnotationIsDiscovery()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                public void adds() {
                    assertEquals(3, new Calculator("c").add(1, 2));
                }
            """), "no-test");
        Assert.Equal(VerificationState.Discovery, r.State);
        Assert.Equal("DISCOVERY | no tests executed | -", r.ErrorSignature);
    }

    [Fact]
    public async Task AllIgnoredIsDiscovery()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Ignore
                @Test
                public void adds() {
                    assertEquals(3, new Calculator("c").add(1, 2));
                }
            """, "import org.junit.Ignore;"), "all-ignored");
        Assert.Equal(VerificationState.Discovery, r.State);
        Assert.Equal(StepStatus.Pass, r.Steps.Discovery);
        Assert.Equal(StepStatus.Fail, r.Steps.Execution);
    }

    [Fact]
    public async Task NullPointerFromCutIsRuntime()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Test
                public void measuresLabel() {
                    assertEquals(0, new Calculator("c").labelLength(null));
                }
            """), "npe");
        Assert.Equal(VerificationState.Runtime, r.State);
        Assert.Equal("RUNTIME | NullPointerException | Calculator.labelLength", r.ErrorSignature);
        Assert.Equal("exception", r.Failures[0].Kind);
    }

    [Fact]
    public async Task WrongValueIsAssertionNotCompile()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Test
                public void addsWrong() {
                    assertEquals(6, new Calculator("c").add(2, 3));
                }

                @Test
                public void adds() {
                    assertEquals(5, new Calculator("c").add(2, 3));
                    assertTrue(true);
                }
            """), "assert-value");
        Assert.Equal(VerificationState.Assertion, r.State);
        Assert.Equal("ASSERTION | AssertionError | addsWrong", r.ErrorSignature);
        Assert.Equal(1, r.TestMethods.Passed);
        Assert.Equal(2, r.AssertionCount);
    }

    [Fact]
    public async Task AssertThrowsWithoutThrowIsAssertion()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Test
                public void dividesByZero() {
                    assertThrows(IllegalArgumentException.class, () -> new Calculator("c").divide(4, 2));
                }
            """), "assert-throws");
        Assert.Equal(VerificationState.Assertion, r.State);
        Assert.Equal("ASSERTION | AssertionError | dividesByZero", r.ErrorSignature);
    }

    [Fact]
    public async Task BreakingSharedStateIsRegression()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Test
                public void writesStore() throws Exception {
                    SharedStore.write("broken");
                    assertEquals("broken", SharedStore.read());
                }
            """), "regression");
        Assert.Equal(VerificationState.Regression, r.State);
        Assert.Equal("REGRESSION | existing tests fail | SharedStoreTest", r.ErrorSignature);
        Assert.Equal("com.example.calc.SharedStoreTest#storeIsUntouched", Assert.Single(r.NewFailures).Id);
        Assert.Equal("RUNTIME", r.Group);
    }

    [Fact]
    public async Task GoodTestIsValid()
    {
        var p = await Env.PreparedAsync(Samples.MavenJunit4);
        var r = await VerifyAsync(p, Junit4("""
                @Test
                public void adds() {
                    assertEquals(5, new Calculator("c").add(2, 3));
                }

                @Test(expected = IllegalArgumentException.class)
                public void rejectsZero() {
                    new Calculator("c").divide(1, 0);
                }
            """), "valid");
        Assert.Equal(VerificationState.Valid, r.State);
        Assert.Null(r.ErrorSignature);
        Assert.Equal(2, r.TestMethods.Passed);
        Assert.Equal(2, r.AssertionCount);
        Assert.True(r.ModuleRunIncludesTarget);
        Assert.StartsWith("sha256:", r.CodeHash);
    }

    [Fact]
    public async Task ModuleWithOwnIncludesStillRunsGeneratedTest()
    {
        var p = await Env.PreparedAsync(Samples.MavenMultiJunit5);
        var r = await VerifyAsync(p, """
            package com.example.app;

            import static org.junit.jupiter.api.Assertions.assertEquals;

            import org.junit.jupiter.api.Test;

            class Greeter_ArrowTest {

                @Test
                void greets() {
                    assertEquals("Hi, Ann!", new Greeter("Hi").greet("ann"));
                }
            }
            """, "includes");
        Assert.Equal(VerificationState.Valid, r.State);
        Assert.False(r.ModuleRunIncludesTarget);
        Assert.Contains(r.Logs, l => l.Contains("module_test_with_target", StringComparison.Ordinal));
    }

    [Fact]
    public async Task GradleValidAndCompile()
    {
        var p = await Env.PreparedAsync(Samples.GradleJunit5);
        var valid = await VerifyAsync(p, """
            package com.example.shop;

            import static org.junit.jupiter.api.Assertions.*;

            import org.junit.jupiter.api.Test;

            class Cart_ArrowTest {

                @Test
                void rejectsNegative() {
                    assertThrows(IllegalArgumentException.class, () -> new Cart().add(-1));
                }

                @Test
                void counts() {
                    Cart cart = new Cart();
                    cart.add(1);
                    assertEquals(1, cart.size());
                }
            }
            """, "gradle-valid");
        Assert.Equal(VerificationState.Valid, valid.State);
        Assert.Equal(2, valid.TestMethods.Passed);

        var compile = await VerifyAsync(p, """
            package com.example.shop;

            import org.junit.jupiter.api.Test;

            class Cart_ArrowTest {

                @Test
                void total() {
                    int value = new Cart().sum();
                }
            }
            """, "gradle-compile");
        Assert.Equal(VerificationState.Compile, compile.State);
        Assert.Equal("COMPILE | cannot find symbol | method", compile.ErrorSignature);
        Assert.Equal("total", compile.CompileErrors[0].Method);
    }
}
