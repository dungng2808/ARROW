package com.jayway.jsonpath.matchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WithJsonPath_ArrowTest {

    @Test
    public void matchesWhenPathValueMatches() {
        ReadContext context = JsonPath.parse("{\"value\":5}");
        Matcher<Integer> resultMatcher = IsEqual.equalTo(5);
        WithJsonPath<Integer> matcher = new WithJsonPath<>(JsonPath.compile("$.value"), resultMatcher);

        assertTrue(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathValueDoesNotMatch() {
        ReadContext context = JsonPath.parse("{\"value\":5}");
        Matcher<Integer> resultMatcher = IsEqual.equalTo(6);
        WithJsonPath<Integer> matcher = new WithJsonPath<>(JsonPath.compile("$.value"), resultMatcher);

        assertFalse(matcher.matches(context));
    }

    @Test
    public void doesNotMatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{\"other\":5}");
        Matcher<Integer> resultMatcher = IsEqual.equalTo(5);
        WithJsonPath<Integer> matcher = new WithJsonPath<>(JsonPath.compile("$.value"), resultMatcher);

        assertFalse(matcher.matches(context));
    }

    @Test
    public void describesExpectedPathAndMatcher() {
        WithJsonPath<Integer> matcher = new WithJsonPath<>(JsonPath.compile("$.value"), IsEqual.equalTo(5));
        Description description = new StringDescription();

        matcher.describeTo(description);

        org.junit.jupiter.api.Assertions.assertEquals("with json path \"$.value\" evaluated to <5>", description.toString());
    }

    @Test
    public void describesMismatchForExistingPath() {
        ReadContext context = JsonPath.parse("{\"value\":5}");
        WithJsonPath<Integer> matcher = new WithJsonPath<>(JsonPath.compile("$.value"), IsEqual.equalTo(6));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        org.junit.jupiter.api.Assertions.assertEquals("json path \"$.value\" was evaluated to <5>", description.toString());
    }

    @Test
    public void describesMismatchForMissingPath() {
        ReadContext context = JsonPath.parse("{\"other\":5}");
        WithJsonPath<Integer> matcher = new WithJsonPath<>(JsonPath.compile("$.value"), IsEqual.equalTo(6));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        org.junit.jupiter.api.Assertions.assertEquals("json path \"$.value\" was not found in {\"other\":5}", description.toString());
    }
}
