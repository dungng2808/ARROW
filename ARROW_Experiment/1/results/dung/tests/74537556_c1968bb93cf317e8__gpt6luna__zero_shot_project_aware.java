package org.onlab.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Identifier_ArrowTest {

    private static class TestIdentifier extends Identifier<String> {
        TestIdentifier(String value) {
            super(value);
        }

        TestIdentifier() {
            super();
        }
    }

    @Test
    public void returnsBackingIdentifier() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertEquals("alpha", identifier.id());
    }

    @Test
    public void computesHashCodeFromBackingIdentifier() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertEquals("alpha".hashCode(), identifier.hashCode());
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
    public void identifierIsEqualToItself() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertTrue(identifier.equals(identifier));
    }

    @Test
    public void identifierIsNotEqualToNullOrOtherTypes() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertFalse(identifier.equals(null));
        assertFalse(identifier.equals("alpha"));
    }

    @Test
    public void identifiersOfDifferentClassesAreNotEqual() {
        TestIdentifier identifier = new TestIdentifier("alpha");
        OtherIdentifier other = new OtherIdentifier("alpha");

        assertFalse(identifier.equals(other));
    }

    @Test
    public void returnsBackingValueAsString() {
        TestIdentifier identifier = new TestIdentifier("alpha");

        assertEquals("alpha", identifier.toString());
    }

    @Test
    public void serializationConstructorLeavesIdentifierNull() {
        TestIdentifier identifier = new TestIdentifier();

        assertNull(identifier.id());
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullBackingValue() {
        new TestIdentifier(null);
    }

    private static class OtherIdentifier extends Identifier<String> {
        OtherIdentifier(String value) {
            super(value);
        }
    }
}
