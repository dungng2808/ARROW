package com.jayway.jsonpath.matchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WithJsonPath_ArrowTest {

    @Test
    public void matchesWhenPathValueMatches() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> resultMatcher = IsEqual.equalTo("Ada");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.name"), resultMatcher);

        assertTrue(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathValueDoesNotMatch() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<String> resultMatcher = IsEqual.equalTo("Grace");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.name"), resultMatcher);

        assertFalse(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{}");
        Matcher<String> resultMatcher = IsEqual.equalTo("Ada");
        WithJsonPath<String> matcher =
                new WithJsonPath<String>(JsonPath.compile("$.name"), resultMatcher);

        assertFalse(matcher.matches(context));
    }

    @Test
    public void describesPathAndResultMatcher() {
        WithJsonPath<String> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.name"), IsEqual.equalTo("Ada"));
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("with json path \"$.name\" evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchWithEvaluatedValue() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<String> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.name"), IsEqual.equalTo("Grace"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertEquals("json path \"$.name\" was evaluated to \"Ada\"", description.toString());
    }

    @Test
    public void describesMismatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{}");
        WithJsonPath<String> matcher = new WithJsonPath<String>(
                JsonPath.compile("$.name"), IsEqual.equalTo("Ada"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertEquals("json path \"$.name\" was not found in {}", description.toString());
    }
}
