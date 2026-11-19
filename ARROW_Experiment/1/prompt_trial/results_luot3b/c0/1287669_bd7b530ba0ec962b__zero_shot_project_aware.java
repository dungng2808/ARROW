package com.jayway.jsonpath.matchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WithJsonPath_ArrowTest {

    @Test
    public void matchesWhenPathValueMatches() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<ReadContext> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.name"), equalTo("Ada"));

        assertTrue(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathValueDoesNotMatch() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<ReadContext> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.name"), equalTo("Grace"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<ReadContext> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.age"), equalTo("30"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void describesExpectedPathAndMatcher() {
        Matcher<ReadContext> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.name"), equalTo("Ada"));
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertThat(description.toString(),
                is("with json path \"$.name\" evaluated to \"Ada\""));
    }

    @Test
    public void describesMismatchWithEvaluatedValue() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<ReadContext> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.name"), equalTo("Grace"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertThat(description.toString(), is("json path \"$.name\" was evaluated to \"Ada\""));
    }

    @Test
    public void describesMismatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<ReadContext> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.age"), equalTo("30"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertThat(description.toString(),
                is("json path \"$.age\" was not found in {\"name\":\"Ada\"}"));
    }
}
