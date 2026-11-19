package com.jayway.jsonpath.matchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.equalTo;

public class WithJsonPath_ArrowTest {

    @Test
    public void matchesWhenPathValueSatisfiesMatcher() {
        JsonPath path = JsonPath.compile("$.name");
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(path, equalTo((Object) "Ada"));

        assertTrue(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathValueDoesNotSatisfyMatcher() {
        JsonPath path = JsonPath.compile("$.name");
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(path, equalTo((Object) "Grace"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathIsMissing() {
        JsonPath path = JsonPath.compile("$.missing");
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(path, equalTo((Object) "Ada"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void describesExpectedPathAndMatcher() {
        JsonPath path = JsonPath.compile("$.name");
        Matcher<Object> matcher = new WithJsonPath<Object>(path, equalTo((Object) "Ada"));
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("with json path \"$.name\" evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchWithEvaluatedValue() {
        JsonPath path = JsonPath.compile("$.name");
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(path, equalTo((Object) "Grace"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertEquals("json path \"$.name\" was evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchWhenPathIsMissing() {
        JsonPath path = JsonPath.compile("$.missing");
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(path, equalTo((Object) "Ada"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertEquals("json path \"$.missing\" was not found in {\"name\":\"Ada\"}", description.toString());
    }
}
