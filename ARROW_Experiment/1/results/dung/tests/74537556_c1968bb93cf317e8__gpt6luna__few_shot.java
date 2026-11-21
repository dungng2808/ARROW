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
    }

    private static class OtherIdentifier extends Identifier<String> {
        OtherIdentifier(String value) {
            super(value);
        }
    }

    @Test
    public void idReturnsBackingValue() {
        Identifier<String> identifier = new TestIdentifier("device-1");
        assertEquals("device-1", identifier.id());
    }

    @Test
    public void equalIdentifiersHaveEqualHashCodes() {
        Identifier<String> first = new TestIdentifier("device-1");
        Identifier<String> second = new TestIdentifier("device-1");
        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void identifiersWithDifferentValuesAreNotEqual() {
        Identifier<String> first = new TestIdentifier("device-1");
        Identifier<String> second = new TestIdentifier("device-2");
        assertFalse(first.equals(second));
    }

    @Test
    public void identifiersOfDifferentClassesAreNotEqual() {
        Identifier<String> first = new TestIdentifier("device-1");
        Identifier<String> second = new OtherIdentifier("device-1");
        assertFalse(first.equals(second));
    }

    @Test
    public void identifierIsNotEqualToNullOrOtherObject() {
        Identifier<String> identifier = new TestIdentifier("device-1");
        assertFalse(identifier.equals(null));
        assertFalse(identifier.equals("device-1"));
    }

    @Test
    public void identifierIsEqualToItself() {
        Identifier<String> identifier = new TestIdentifier("device-1");
        assertTrue(identifier.equals(identifier));
    }

    @Test
    public void toStringReturnsBackingValueString() {
        Identifier<Integer> identifier = new Identifier<Integer>(42) { };
        assertEquals("42", identifier.toString());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullValue() {
        new TestIdentifier(null);
    }

    @Test
    public void protectedNoArgumentConstructorLeavesIdentifierNull() {
        Identifier<String> identifier = new Identifier<String>() { };
        assertNull(identifier.id());
    }
}
