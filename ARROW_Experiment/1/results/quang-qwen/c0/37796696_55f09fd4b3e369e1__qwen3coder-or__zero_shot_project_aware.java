package io.cucumber.cucumberexpressions;

import org.junit.jupiter.api.Test;
import org.hamcrest.Matchers;
import java.util.List;
import java.util.regex.Pattern;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class CucumberExpression_ArrowTest {

    @Test
    public void testSimpleTextExpression() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("hello world", registry);
        assertThat(expr.getSource(), equalTo("hello world"));
        assertThat(expr.getRegexp().pattern(), equalTo("^hello world$"));
    }

    @Test
    public void testExpressionWithParameter() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int} oranges", registry);
        assertThat(expr.getSource(), equalTo("I have {int} oranges"));
        assertThat(expr.getRegexp().pattern(), equalTo("^I have (\\\\d+) oranges$"));
    }

    @Test
    public void testExpressionWithOptional() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have (no|some) oranges", registry);
        assertThat(expr.getSource(), equalTo("I have (no|some) oranges"));
        assertThat(expr.getRegexp().pattern(), equalTo("^I have (?:no|some) oranges$"));
    }

    @Test
    public void testExpressionWithAlternative() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int}/oranges", registry);
        assertThat(expr.getSource(), equalTo("I have {int}/oranges"));
        assertThat(expr.getRegexp().pattern(), equalTo("^I have (\\\\d+)/oranges$"));
    }

    @Test
    public void testExpressionWithEscapedCharacters() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have \\{int\\} oranges", registry);
        assertThat(expr.getSource(), equalTo("I have \\{int\\} oranges"));
        assertThat(expr.getRegexp().pattern(), equalTo("^I have \\{int\\} oranges$"));
    }

    @Test
    public void testMatchReturnsNullWhenNoMatch() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("hello world", registry);
        List<Argument<?>> result = expr.match("goodbye world");
        assertNull(result);
    }

    @Test
    public void testMatchReturnsArgumentsWhenMatch() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int} oranges", registry);
        List<Argument<?>> result = expr.match("I have 5 oranges");
        assertNotNull(result);
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0).getValue(), equalTo("5"));
    }

    @Test
    public void testMatchWithTypeHint() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int} oranges", registry);
        List<Argument<?>> result = expr.match("I have 5 oranges", Integer.class);
        assertNotNull(result);
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0).getValue(), equalTo(5));
    }

    @Test
    public void testMatchWithOptional() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have (no|some) oranges", registry);
        List<Argument<?>> result = expr.match("I have some oranges");
        assertNotNull(result);
        assertThat(result.size(), equalTo(0));
    }

    @Test
    public void testMatchWithAlternative() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int}/oranges", registry);
        List<Argument<?>> result = expr.match("I have 5/oranges");
        assertNotNull(result);
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0).getValue(), equalTo("5"));
    }

    @Test
    public void testParameterTypesCannotBeAlternative() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        assertThrows(CucumberExpressionException.class, () -> {
            new CucumberExpression("I have {int}/{string}", registry);
        });
    }

    @Test
    public void testParameterTypesCannotBeOptional() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        assertThrows(CucumberExpressionException.class, () -> {
            new CucumberExpression("I have ({int})", registry);
        });
    }

    @Test
    public void testGetSourceReturnsOriginalExpression() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int} oranges", registry);
        assertThat(expr.getSource(), equalTo("I have {int} oranges"));
    }

    @Test
    public void testGetRegexpReturnsCompiledPattern() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int} oranges", registry);
        Pattern pattern = expr.getRegexp();
        assertNotNull(pattern);
        assertThat(pattern.pattern(), equalTo("^I have (\\\\d+) oranges$"));
    }
}
