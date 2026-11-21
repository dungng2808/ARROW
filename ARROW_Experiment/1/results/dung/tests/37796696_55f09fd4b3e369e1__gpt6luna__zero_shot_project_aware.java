package io.cucumber.cucumberexpressions;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class CucumberExpression_ArrowTest {

    @Test
    public void returnsOriginalSource() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry(java.util.Locale.ENGLISH);
        CucumberExpression expression = new CucumberExpression("hello", registry);

        assertThat(expression.getSource(), is("hello"));
    }

    @Test
    public void buildsAnchoredRegexpForLiteralText() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry(java.util.Locale.ENGLISH);
        CucumberExpression expression = new CucumberExpression("hello", registry);

        assertThat(expression.getRegexp().pattern(), is("^hello$"));
    }

    @Test
    public void matchesLiteralText() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry(java.util.Locale.ENGLISH);
        CucumberExpression expression = new CucumberExpression("hello", registry);

        List<Argument<?>> arguments = expression.match("hello");

        assertThat(arguments.size(), is(0));
    }

    @Test
    public void doesNotMatchDifferentLiteralText() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry(java.util.Locale.ENGLISH);
        CucumberExpression expression = new CucumberExpression("hello", registry);

        assertThat(expression.match("goodbye"), is(nullValue()));
    }

    @Test
    public void matchesOptionalTextWhenPresentOrAbsent() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry(java.util.Locale.ENGLISH);
        CucumberExpression expression = new CucumberExpression("colour(s)", registry);

        assertThat(expression.getRegexp().pattern(), is("^colours?$"));
        assertThat(expression.match("colour").size(), is(0));
        assertThat(expression.match("colours").size(), is(0));
    }

    @Test
    public void matchesTextAlternatives() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry(java.util.Locale.ENGLISH);
        CucumberExpression expression = new CucumberExpression("red/blue", registry);

        assertThat(expression.getRegexp().pattern(), is("^(?:red|blue)$"));
        assertThat(expression.match("red").size(), is(0));
        assertThat(expression.match("blue").size(), is(0));
        assertThat(expression.match("green"), is(nullValue()));
    }

    @Test
    public void escapesRegexpMetacharactersInLiteralText() {
        ParameterTypeRegistry registry = new ParameterTypeRegistry(java.util.Locale.ENGLISH);
        CucumberExpression expression = new CucumberExpression("a.b", registry);

        assertThat(expression.getRegexp().pattern(), is("^a\\.b$"));
        assertThat(expression.match("a.b").size(), is(0));
        assertThat(expression.match("axb"), is(nullValue()));
    }
}
