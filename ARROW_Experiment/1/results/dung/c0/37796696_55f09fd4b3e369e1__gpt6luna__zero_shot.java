package io.cucumber.cucumberexpressions;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CucumberExpression_ArrowTest {

    @Test
    public void returnsOriginalSource() {
        CucumberExpression expression = new CucumberExpression("hello", new ParameterTypeRegistry());
        assertEquals("hello", expression.getSource());
    }

    @Test
    public void createsAnchoredRegexpForPlainText() {
        CucumberExpression expression = new CucumberExpression("hello", new ParameterTypeRegistry());
        assertEquals("^hello$", expression.getRegexp().pattern());
    }

    @Test
    public void matchesPlainText() {
        CucumberExpression expression = new CucumberExpression("hello", new ParameterTypeRegistry());
        List<Argument<?>> arguments = expression.match("hello");
        assertNotNull(arguments);
        assertTrue(arguments.isEmpty());
    }

    @Test
    public void doesNotMatchDifferentText() {
        CucumberExpression expression = new CucumberExpression("hello", new ParameterTypeRegistry());
        assertNull(expression.match("goodbye"));
    }

    @Test
    public void matchesOptionalTextWhenPresentOrAbsent() {
        CucumberExpression expression = new CucumberExpression("hello (world)", new ParameterTypeRegistry());
        assertNotNull(expression.match("hello world"));
        assertNotNull(expression.match("hello "));
    }

    @Test
    public void matchesAlternatives() {
        CucumberExpression expression = new CucumberExpression("red/blue", new ParameterTypeRegistry());
        assertNotNull(expression.match("red"));
        assertNotNull(expression.match("blue"));
    }

    @Test
    public void escapesRegexMetacharactersInText() {
        CucumberExpression expression = new CucumberExpression("a.b", new ParameterTypeRegistry());
        assertEquals("^a\\.b$", expression.getRegexp().pattern());
        assertNotNull(expression.match("a.b"));
        assertNull(expression.match("axb"));
    }

    @Test
    public void rejectsUnknownParameterType() {
        try {
            new CucumberExpression("{unknown}", new ParameterTypeRegistry());
        } catch (UndefinedParameterTypeException expected) {
            return;
        }
        throw new AssertionError("Expected an undefined parameter type exception");
    }

    @Test
    public void rejectsParameterTypesInsideOptionalText() {
        try {
            new CucumberExpression("hello ({int})", new ParameterTypeRegistry());
        } catch (CucumberExpressionException expected) {
            return;
        }
        throw new AssertionError("Expected a cucumber expression exception");
    }

    @Test
    public void rejectsParameterTypesInsideAlternatives() {
        try {
            new CucumberExpression("{int}/blue", new ParameterTypeRegistry());
        } catch (CucumberExpressionException expected) {
            return;
        }
        throw new AssertionError("Expected a cucumber expression exception");
    }
}
