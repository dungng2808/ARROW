package com.jayway.jsonpath.matchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WithJsonPath_ArrowTest {

    @Test
    public void matchesWhenPathValueSatisfiesMatcher() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));

        assertTrue(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathValueDoesNotSatisfyMatcher() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Grace"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void describesExpectedPathAndMatcher() {
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("with json path \"$.name\" evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchWithEvaluatedValue() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Grace"));
        Description description = new StringDescription();

        try {
            matcher.matches(context);
            matcher.describeMismatch(context, description);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        assertEquals("json path \"$.name\" was evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{}");
        Matcher<String> matcher = new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));
        Description description = new StringDescription();

        matcher.matches(context);
        matcher.describeMismatch(context, description);

        assertEquals("json path \"$.name\" was not found in {}", description.toString());
    }
}
