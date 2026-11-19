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
    public void matchesWhenPathValueSatisfiesMatcher() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.name"), equalTo((Object) "Ada"));

        assertThat(context, matcher);
    }

    @Test
    public void doesNotMatchWhenPathValueDoesNotSatisfyMatcher() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.name"), equalTo((Object) "Grace"));

        assertThat(context, not(matcher));
    }

    @Test
    public void doesNotMatchWhenPathIsMissing() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        Matcher<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.age"), equalTo((Object) 42));

        assertThat(context, not(matcher));
    }

    @Test
    public void describesExpectedPathAndMatcher() {
        WithJsonPath<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.name"), equalTo((Object) "Ada"));
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertThat(description.toString(), is("with json path \"$.name\" evaluated to \"Ada\""));
    }

    @Test
    public void describesMismatchWithEvaluatedValue() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.name"), equalTo((Object) "Grace"));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertThat(description.toString(), is("json path \"$.name\" was evaluated to \"Ada\""));
    }

    @Test
    public void describesMismatchWhenPathIsNotFound() {
        ReadContext context = JsonPath.parse("{\"name\":\"Ada\"}");
        WithJsonPath<Object> matcher = new WithJsonPath<Object>(JsonPath.compile("$.age"), equalTo((Object) 42));
        Description description = new StringDescription();

        matcher.describeMismatch(context, description);

        assertThat(description.toString(), is("json path \"$.age\" was not found in {\"name\":\"Ada\"}"));
    }
}
