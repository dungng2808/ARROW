using Arrow.Core.Verify;
using Arrow.Infrastructure.Java;

namespace Arrow.Infrastructure.Tests;

/// <summary>java-analyzer trên bộ test mẫu (mốc M4): đếm assertion (specs/03 Mục 8), bỏ comment (Mục 7), ghép method (specs/05 Mục 5.3).</summary>
public class AnalyzerTests
{
    private static JavaAnalyzer Analyzer => Env.Analyzer;
    private static readonly CancellationToken None = CancellationToken.None;

    [Fact]
    public async Task CountsAssertionsByRule()
    {
        const string source = """
            package a;

            import static org.junit.jupiter.api.Assertions.*;
            import static org.assertj.core.api.Assertions.assertThat;
            import static org.mockito.Mockito.*;
            import static io.restassured.RestAssured.given;

            import org.junit.jupiter.api.Test;

            class FooTest {
                @Test
                void junit() {
                    assertEquals(2, calc());          // 1
                    assertTrue(flag());               // 1
                    assertNotNull(service(), "msg");  // 1
                    assertThrows(IllegalStateException.class, () -> boom()); // 1
                    assertDoesNotThrow(() -> calc()); // 1
                }

                @Test
                void trivial() {
                    assertTrue(true);
                    assertFalse(false);
                    assertEquals(x, x);
                    assertSame(calc(), calc());
                    assertNotNull(new Object());
                    assertNull(null);
                }

                @Test
                void assertjAndMockito() {
                    assertThat(list()).hasSize(2);    // 1
                    verify(repo).save(any());         // 1
                    Mockito.verify(repo, times(2)).flush(); // 1
                }

                @Test
                void restAssured() {
                    given().when().get("/hi").then().statusCode(200).body(is("hi")); // 2
                    given().get("/x").then().assertThat().statusCode(404);          // 1
                    String id = given().get("/y").then().extract().path("id");      // 0
                }

                @org.junit.Test(expected = IllegalStateException.class)
                public void expected() {                                  // 1
                    boom();
                }

                @Test
                void failInTry() {
                    try {
                        boom();
                        fail("should throw");             // 1
                    } catch (IllegalStateException e) {
                        // ok
                    }
                    fail("outside try");                  // 0
                }

                private void helper() {
                    assertEquals(1, calc());              // 1, tính vào tổng, không vào test method
                }
            }
            """;
        var analysis = await Analyzer.AnalyzeTestAsync(source, [14, 3], None);
        Assert.True(analysis.Parsed);
        Assert.Equal(5, analysis.AssertionsPerMethod["junit"]);
        Assert.Equal(0, analysis.AssertionsPerMethod["trivial"]);
        Assert.Equal(3, analysis.AssertionsPerMethod["assertjAndMockito"]);
        Assert.Equal(3, analysis.AssertionsPerMethod["restAssured"]);
        Assert.Equal(1, analysis.AssertionsPerMethod["expected"]);
        Assert.Equal(1, analysis.AssertionsPerMethod["failInTry"]);
        Assert.Equal(14, analysis.AssertionCount); // 13 trong test method + 1 trong helper
        Assert.Equal(["junit", "trivial", "assertjAndMockito", "restAssured", "expected", "failInTry"], analysis.TestMethods);
        Assert.Equal("junit", analysis.MethodsAtLines["14"]);
        Assert.Null(analysis.MethodsAtLines["3"]);
    }

    [Fact]
    public async Task StripsCommentsButNotStrings()
    {
        const string q = "\"\"\"";
        var a = "package a;\n/** doc */\nclass A {\n    String url = \"http://example.com/*not a comment*/\"; // trailing\n"
                + "    /* block */ int x = 1;\n    String block = " + q + "\n        // inside text block\n        " + q + ";\n}\n";
        var b = "package a;\nclass A {\n    String url = \"http://example.com/*not a comment*/\";\n"
                + "    int x = 1;\n    String block = " + q + "\n        // inside text block\n        " + q + ";\n}\n";
        var first = await Analyzer.AnalyzeTestAsync(a, [], None);
        var second = await Analyzer.AnalyzeTestAsync(b, [], None);
        Assert.Contains("http://example.com/*not a comment*/", first.Stripped);
        Assert.Contains("// inside text block", first.Stripped);
        Assert.DoesNotContain("trailing", first.Stripped);
        Assert.Equal(CodeHasher.Hash(second.Stripped), CodeHasher.Hash(first.Stripped));
    }

    [Fact]
    public async Task StripsCommentsWhenCodeDoesNotParse()
    {
        var analysis = await Analyzer.AnalyzeTestAsync("class A { int x = ; // c\n String s = \"//keep\"; }", [], None);
        Assert.False(analysis.Parsed);
        Assert.Contains("\"//keep\"", analysis.Stripped);
        Assert.DoesNotContain("// c", analysis.Stripped);
        Assert.Null(analysis.AssertionCount);
    }

    private const string Checkpoint = """
        package a;

        import static org.junit.Assert.*;

        import org.junit.Test;

        public class CalcTest {

            private final Calc calc = new Calc();

            @Test
            public void adds() {
                assertEquals(3, calc.add(1, 2));
            }

            /** Divides. */
            @Test
            public void divides() {
                assertEquals(2, calc.divide(4, 3));
            }

            @Test
            public void broken() {
                assertEquals(1, calc.bad());
            }

            @Test
            public void keepsSpacing()   {   assertTrue( calc.ok() );   }
        }
        """;

    [Fact]
    public async Task SplicesOnlyFailingMethods()
    {
        const string response = """
            package a;

            import static org.junit.Assert.*;

            import java.util.List;
            import org.junit.Test;

            public class CalcTest {

                private final Calc calc = new Calc();
                private int counter;

                @Test
                public void adds() {
                    assertEquals(99, calc.add(1, 2)); // must NOT replace: not failing
                }

                /** Divides (fixed). */
                @Test
                public void divides() {
                    assertEquals(1, calc.divide(4, 3));
                }

                @Test
                public void newTest() {
                }

                private int twice(int value) {
                    return value * 2;
                }
            }
            """;
        var result = await Analyzer.SpliceAsync(Checkpoint, response, ["divides", "broken"], None);
        Assert.Null(result.Error);
        Assert.Equal(["divides"], result.Replaced);
        Assert.Equal(["broken"], result.Dropped);
        Assert.Equal(["import java.util.List;"], result.AddedImports);
        Assert.Equal(["counter"], result.AddedFields);
        Assert.Equal(["twice"], result.AddedMethods);
        Assert.Contains("assertEquals(3, calc.add(1, 2));", result.Code);
        Assert.DoesNotContain("99", result.Code);
        Assert.Contains("/** Divides (fixed). */", result.Code);
        Assert.Contains("assertEquals(1, calc.divide(4, 3));", result.Code);
        Assert.DoesNotContain("calc.bad()", result.Code);
        Assert.DoesNotContain("newTest", result.Code);
        Assert.Contains("public void keepsSpacing()   {   assertTrue( calc.ok() );   }", result.Code);

        // Kết quả ghép phải parse được và giữ đúng thứ tự method.
        var analysis = await Analyzer.AnalyzeTestAsync(result.Code, [], None);
        Assert.True(analysis.Parsed);
        Assert.Equal(["adds", "divides", "keepsSpacing"], analysis.TestMethods);
    }

    [Fact]
    public async Task SpliceReportsUnparsableResponse()
    {
        var result = await Analyzer.SpliceAsync(Checkpoint, "public class CalcTest { void x( }", ["broken"], None);
        Assert.Equal("response does not parse", result.Error);
    }

    private const string Cut = """
        package a;

        public class Calc {
            private final int base;
            public static final int MAX = 10;

            public Calc() {
                this(0);
            }

            Calc(int base) {
                this.base = base;
            }

            public int add(int a, int b) {
                return base + a + b;
            }

            public int divide(int a, int b) throws ArithmeticException {
                return a / b;
            }

            private int secret() {
                return 42;
            }
        }
        """;

    [Fact]
    public async Task ListsCutApiAndCalledMethods()
    {
        var api = await Analyzer.CutApiAsync(Cut, ["package a; public interface Shape { double area(); }"], None);
        var calc = Assert.Single(api.Types, t => t.Name == "a.Calc");
        Assert.Contains("public Calc()", calc.Members);
        Assert.Contains("Calc(int base)", calc.Members);
        Assert.Contains("public int add(int a, int b)", calc.Members);
        Assert.Contains("public int divide(int a, int b) throws ArithmeticException", calc.Members);
        Assert.Contains("public static final int MAX", calc.Members);
        Assert.DoesNotContain(calc.Members, m => m.Contains("secret"));
        Assert.Contains(api.Types, t => t.Name == "a.Shape" && t.Members.Contains("double area()"));

        var called = await Analyzer.CalledMethodsAsync(Checkpoint, Cut, ["divides"], None);
        Assert.Equal(["divide"], called.Called);
        Assert.Contains("return a / b;", Assert.Single(called.Methods));
        Assert.Equal(2, called.Constructors.Count);
        Assert.Equal(2, called.Fields.Count);

        var sources = await Analyzer.MethodSourceAsync(Cut, ["add", "<init>"], None);
        Assert.Equal(3, sources.Count);
    }
}
