package com.jayway.jsonpath.matchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class WithJsonPath_ArrowTest {

    @Test
    public void matchesWhenPathValueSatisfiesMatcher() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));

        assertTrue(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathValueDoesNotSatisfyMatcher() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Grace"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.age"), equalTo("30"));

        assertFalse(matcher.matches(context));
    }

    @Test
    public void describesExpectedPathAndMatcher() {
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Ada"));
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("with json path \"$.name\" evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchForExistingPath() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.name"), equalTo("Grace"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertEquals("json path \"$.name\" was evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchForMissingPath() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.age"), equalTo("30"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertEquals("json path \"$.age\" was not found in {\"name\":\"Ada\"}", description.toString());
    }
}
