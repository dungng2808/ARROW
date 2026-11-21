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
        
        List<Argument<?>> arguments = expression.match("Hello John");
        assertNotNull(arguments);
        assertEquals(1, arguments.size());
        assertEquals("John", arguments.get(0).getValue());
    }

    @Test
    public void testExpressionWithOptional() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("I have (a cat)", registry);
        
        assertEquals("I have (a cat)", expression.getSource());
        assertNotNull(expression.getRegexp());
        
        List<Argument<?>> arguments = expression.match("I have a cat");
        assertNotNull(arguments);
        assertEquals(0, arguments.size());
        
        arguments = expression.match("I have");
        assertNotNull(arguments);
        assertEquals(0, arguments.size());
    }

    @Test
    public void testExpressionWithAlternative() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("I like {food} or {food}", registry);
        
        assertEquals("I like {food} or {food}", expression.getSource());
        assertNotNull(expression.getRegexp());
        
        List<Argument<?>> arguments = expression.match("I like pizza or pasta");
        assertNotNull(arguments);
        assertEquals(2, arguments.size());
        assertEquals("pizza", arguments.get(0).getValue());
        assertEquals("pasta", arguments.get(1).getValue());
    }

    @Test
    public void testExpressionWithEscapedCharacters() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("I have \\{escaped\\}", registry);
        
        assertEquals("I have \\{escaped\\}", expression.getSource());
        assertNotNull(expression.getRegexp());
        
        List<Argument<?>> arguments = expression.match("I have {escaped}");
        assertNotNull(arguments);
        assertEquals(0, arguments.size());
    }

    @Test
    public void testMatchReturnsNullWhenNoMatch() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello {name}", registry);
        
        List<Argument<?>> arguments = expression.match("Goodbye John");
        assertNull(arguments);
    }

    @Test
    public void testGetRegexp() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry();
        CucumberExpression expression = new CucumberExpression("Hello {name}", registry);
        
        Pattern pattern = expression.getRegexp();
        assertNotNull(pattern);
        assertTrue(pattern.matcher("Hello John").matches());
        assertFalse(pattern.matcher("Goodbye John").matches());
    }
}
