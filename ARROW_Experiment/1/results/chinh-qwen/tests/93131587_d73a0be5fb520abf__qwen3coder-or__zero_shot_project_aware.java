package com.btc.redg.runtime.defaultvalues.pluggable.buildermatchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.junit.Test;

public class Conditions_ArrowTest {

    @Test
    public void testEqTrue() {
        Predicate<String> predicate = Conditions.eq("test");
        assertTrue(predicate.test("test"));
    }

    @Test
    public void testEqFalse() {
        Predicate<String> predicate = Conditions.eq("test");
        assertFalse(predicate.test("other"));
    }

    @Test
    public void testNeqTrue() {
        Predicate<String> predicate = Conditions.neq("test");
        assertTrue(predicate.test("other"));
    }

    @Test
    public void testNeqFalse() {
        Predicate<String> predicate = Conditions.neq("test");
        assertFalse(predicate.test("test"));
    }

    @Test
    public void testContainsTrue() {
        Predicate<String> predicate = Conditions.contains("test");
        assertTrue(predicate.test("this is a test string"));
    }

    @Test
    public void testContainsFalse() {
        Predicate<String> predicate = Conditions.contains("test");
        assertFalse(predicate.test("this is a other string"));
    }

    @Test
    public void testMatchesRegexTrue() {
        Predicate<String> predicate = Conditions.matchesRegex(".*test.*");
        assertTrue(predicate.test("this is a test string"));
    }

    @Test
    public void testMatchesRegexFalse() {
        Predicate<String> predicate = Conditions.matchesRegex(".*test.*");
        assertFalse(predicate.test("this is a other string"));
    }
}
