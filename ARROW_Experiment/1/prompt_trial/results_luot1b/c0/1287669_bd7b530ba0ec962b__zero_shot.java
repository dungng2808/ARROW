package com.jayway.jsonpath.matchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class WithJsonPath_ArrowTest {

    @Test
    public void matchesWhenThePathValueMatches() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));

        assertThat(matcher.matches(context), is(true));
    }

    @Test
    public void doesNotMatchWhenThePathValueDoesNotMatch() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Grace"));

        assertThat(matcher.matches(context), is(false));
    }

    @Test
    public void doesNotMatchWhenThePathIsMissing() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.age"), equalTo(42));

        assertThat(matcher.matches(context), is(false));
    }

    @Test
    public void describesThePathAndResultMatcher() {
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertThat(description.toString(), is("with json path \"$.name\" evaluated to \"Ada\""));
    }

    @Test
    public void describesMismatchWithTheEvaluatedValue() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Grace"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertThat(description.toString(), is("json path \"$.name\" was evaluated to \"Ada\""));
    }

    @Test
    public void describesMismatchWhenThePathIsMissing() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.age"), equalTo(42));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertThat(description.toString(), is("json path \"$.age\" was not found in {\"name\":\"Ada\"}"));
    }
}
