package org.onlab.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Identifier_ArrowTest {

    private static class TestIdentifier extends Identifier<String> {
        TestIdentifier(String value) {
            super(value);
        }
    }

    private static class OtherIdentifier extends Identifier<String> {
        OtherIdentifier(String value) {
            super(value);
        }
    }

    @Test
    public void returnsBackingValue() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertEquals("alpha", identifier.id());
    }

    @Test
    public void equalIdentifiersHaveEqualHashCodes() {
        TestIdentifier first = new TestIdentifier("alpha");
        TestIdentifier second = new TestIdentifier("alpha");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void identifiersWithDifferentValuesAreNotEqual() {
        TestIdentifier first = new TestIdentifier("alpha");
        TestIdentifier second = new TestIdentifier("beta");

        assertFalse(first.equals(second));
    }

    @Test
    public void identifiersOfDifferentClassesAreNotEqual() {
        TestIdentifier first = new TestIdentifier("alpha");
        OtherIdentifier second = new OtherIdentifier("alpha");

        assertFalse(first.equals(second));
    }

    @Test
    public void identifierIsNotEqualToNullOrUnrelatedObject() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertFalse(identifier.equals(null));
        assertFalse(identifier.equals("alpha"));
    }

    @Test
    public void identifierIsEqualToItself() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertTrue(identifier.equals(identifier));
    }

    @Test
    public void returnsBackingValueAsString() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertEquals("alpha", identifier.toString());
    }

    @Test
    public void hashCodeMatchesBackingValue() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertEquals("alpha".hashCode(), identifier.hashCode());
    }
}
