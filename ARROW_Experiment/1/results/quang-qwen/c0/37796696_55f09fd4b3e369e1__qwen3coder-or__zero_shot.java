package io.cucumber.cucumberexpressions;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.Pattern;

public class CucumberExpression_ArrowTest {

    @Test
    public void testSimpleTextExpression() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("hello world", registry);
        assertEquals("hello world", expr.getSource());
        assertNotNull(expr.getRegexp());
    }

    @Test
    public void testExpressionWithParameter() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int} oranges", registry);
        assertEquals("I have {int} oranges", expr.getSource());
        assertNotNull(expr.getRegexp());
    }

    @Test
    public void testExpressionWithOptional() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have (some) oranges", registry);
        assertEquals("I have (some) oranges", expr.getSource());
        assertNotNull(expr.getRegexp());
    }

    @Test
    public void testExpressionWithAlternative() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int}/many oranges", registry);
        assertEquals("I have {int}/many oranges", expr.getSource());
        assertNotNull(expr.getRegexp());
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
        assertEquals(1, result.size());
        assertEquals("5", result.get(0).getValue());
    }

    @Test
    public void testMatchWithOptional() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have (some) {int} oranges", registry);
        List<Argument<?>> result = expr.match("I have 5 oranges");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("5", result.get(1).getValue());
    }

    @Test
    public void testMatchWithAlternative() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int}/many oranges", registry);
        List<Argument<?>> result = expr.match("I have 5 oranges");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("5", result.get(0).getValue());
    }

    @Test
    public void testGetRegexp() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have {int} oranges", registry);
        Pattern pattern = expr.getRegexp();
        assertNotNull(pattern);
        assertTrue(pattern.matcher("I have 5 oranges").matches());
    }

    @Test
    public void testEscaping() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have \\{int\\} oranges", registry);
        assertEquals("I have \\{int\\} oranges", expr.getSource());
        assertNotNull(expr.getRegexp());
    }

    @Test
    public void testComplexExpression() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expr = new CucumberExpression("I have (some) {int}/many oranges", registry);
        assertEquals("I have (some) {int}/many oranges", expr.getSource());
        assertNotNull(expr.getRegexp());
    }
}
