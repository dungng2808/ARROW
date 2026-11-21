package io.cucumber.cucumberexpressions;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CucumberExpression_ArrowTest {

    @Test
    public void matchesLiteralText() {
        CucumberExpression expression = new CucumberExpression("hello", new ParameterTypeRegistry());
        List<Argument<?>> arguments = expression.match("hello");
        assertNotNull(arguments);
        assertEquals(0, arguments.size());
    }

    @Test
    public void doesNotMatchDifferentLiteralText() {
        CucumberExpression expression = new CucumberExpression("hello", new ParameterTypeRegistry());
        assertNull(expression.match("goodbye"));
    }

    @Test
    public void matchesAndCapturesParameter() {
        CucumberExpression expression = new CucumberExpression("hello {word}", new ParameterTypeRegistry());
        List<Argument<?>> arguments = expression.match("hello world");
        assertNotNull(arguments);
        assertEquals(1, arguments.size());
        assertEquals("world", arguments.get(0).getGroup().getValue());
    }

    @Test
    public void matchesOptionalTextWithAndWithoutOptionalPart() {
        CucumberExpression expression = new CucumberExpression("colour(s)", new ParameterTypeRegistry());
        assertNotNull(expression.match("colour"));
        assertNotNull(expression.match("colours"));
    }

    @Test
    public void exposesOriginalSource() {
        CucumberExpression expression = new CucumberExpression("hello {word}", new ParameterTypeRegistry());
        assertEquals("hello {word}", expression.getSource());
    }

    @Test
    public void exposesAnchoredRegexp() {
        CucumberExpression expression = new CucumberExpression("hello", new ParameterTypeRegistry());
        Pattern pattern = expression.getRegexp();
        assertEquals("^hello$", pattern.pattern());
    }
}
