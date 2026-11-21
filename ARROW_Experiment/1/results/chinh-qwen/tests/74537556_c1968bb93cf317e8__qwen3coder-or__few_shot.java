package org.onlab.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class Identifier_ArrowTest {

    @Test
    public void testConstructorWithNullValue() {
        try {
            new TestIdentifier(null);
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testIdMethod() {
        String testValue = "test-id";
        TestIdentifier identifier = new TestIdentifier(testValue);
        assertEquals(testValue, identifier.id());
    }

    @Test
    public void testHashCode() {
        String testValue = "test-id";
        TestIdentifier identifier = new TestIdentifier(testValue);
        assertEquals(testValue.hashCode(), identifier.hashCode());
    }

    @Test
    public void testEqualsWithSameObject() {
        String testValue = "test-id";
        TestIdentifier identifier = new TestIdentifier(testValue);
        assertTrue(identifier.equals(identifier));
    }

    @Test
    public void testEqualsWithEqualIdentifiers() {
        String testValue = "test-id";
        TestIdentifier identifier1 = new TestIdentifier(testValue);
        TestIdentifier identifier2 = new TestIdentifier(testValue);
        assertTrue(identifier1.equals(identifier2));
    }

    @Test
    public void testEqualsWithDifferentClasses() {
        String testValue = "test-id";
        TestIdentifier identifier = new TestIdentifier(testValue);
        assertFalse(identifier.equals(new Object()));
    }

    @Test
    public void testEqualsWithNull() {
        String testValue = "test-id";
        TestIdentifier identifier = new TestIdentifier(testValue);
        assertFalse(identifier.equals(null));
    }

    @Test
    public void testEqualsWithDifferentValues() {
        TestIdentifier identifier1 = new TestIdentifier("id1");
        TestIdentifier identifier2 = new TestIdentifier("id2");
        assertFalse(identifier1.equals(identifier2));
    }

    @Test
    public void testToString() {
        String testValue = "test-id";
        TestIdentifier identifier = new TestIdentifier(testValue);
        assertEquals(testValue, identifier.toString());
    }

    @Test
    public void testDefaultConstructor() {
        Identifier<String> identifier = new Identifier<String>() {
            @Override
            public String id() {
                return super.id();
            }
        };
        assertNull(identifier.id());
    }

    // Helper class for testing
    private static class TestIdentifier extends Identifier<String> {
        TestIdentifier(String value) {
            super(value);
        }
    }
}
