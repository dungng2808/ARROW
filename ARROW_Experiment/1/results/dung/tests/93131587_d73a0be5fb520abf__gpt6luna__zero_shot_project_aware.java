package com.btc.redg.runtime.defaultvalues.pluggable.buildermatchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.junit.Test;

public class Conditions_ArrowTest {

    @Test
    public void eqMatchesEqualValue() {
        Predicate<String> condition = Conditions.eq("value");

        assertTrue(condition.test("value"));
        assertFalse(condition.test("other"));
    }

    @Test
    public void neqMatchesDifferentValue() {
        Predicate<String> condition = Conditions.neq("value");

        assertTrue(condition.test("other"));
        assertFalse(condition.test("value"));
    }

    @Test
    public void containsMatchesSubstring() {
        Predicate<String> condition = Conditions.contains("red");

        assertTrue(condition.test("red-green"));
        assertFalse(condition.test("blue-green"));
    }

    @Test
    public void matchesRegexMatchesWholeString() {
        Predicate<String> condition = Conditions.matchesRegex("[a-z]+");

        assertTrue(condition.test("hello"));
        assertFalse(condition.test("hello1"));
    }
}
