package io.cucumber.cucumberexpressions;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class CucumberExpression_ArrowTest {

    @Test
    public void testSimpleExpression() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello World", registry);
        assertEquals("Hello World", expression.getSource());
        assertNotNull(expression.getRegexp());
    }

    @Test
    public void testExpressionWithParameter() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello {name}", registry);
        assertEquals("Hello {name}", expression.getSource());
        assertNotNull(expression.getRegexp());
    }

    @Test
    public void testExpressionWithOptional() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello (World)", registry);
        assertEquals("Hello (World)", expression.getSource());
        assertNotNull(expression.getRegexp());
    }

    @Test
    public void testExpressionWithAlternative() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello /World/Universe", registry);
        assertEquals("Hello /World/Universe", expression.getSource());
        assertNotNull(expression.getRegexp());
    }

    @Test
    public void testMatchReturnsNullWhenNoMatch() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello {name}", registry);
        List<Argument<?>> result = expression.match("Goodbye World");
        assertNull(result);
    }

    @Test
    public void testMatchReturnsArgumentsWhenMatch() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello {name}", registry);
        List<Argument<?>> result = expression.match("Hello World");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetSource() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Given I have {int} cukes", registry);
        assertEquals("Given I have {int} cukes", expression.getSource());
    }

    @Test
    public void testGetRegexp() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Given I have {int} cukes", registry);
        Pattern pattern = expression.getRegexp();
        assertNotNull(pattern);
        assertTrue(pattern.matcher("Given I have 5 cukes").matches());
    }
}
