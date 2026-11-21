package org.onlab.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class Identifier_ArrowTest {

    @Test
    public void testConstructorWithNullValueShouldThrowException() {
        try {
            new TestIdentifier(null);
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Identifier cannot be null."));
        }
    }

    @Test
    public void testConstructorWithValue() {
        String value = "testId";
        TestIdentifier identifier = new TestIdentifier(value);
        assertThat(identifier.id(), is(value));
    }

    @Test
    public void testHashCode() {
        String value = "testId";
        TestIdentifier identifier = new TestIdentifier(value);
        assertThat(identifier.hashCode(), is(value.hashCode()));
    }

    @Test
    public void testEqualsWithSameObject() {
        String value = "testId";
        TestIdentifier identifier = new TestIdentifier(value);
        assertTrue(identifier.equals(identifier));
    }

    @Test
    public void testEqualsWithNull() {
        String value = "testId";
        TestIdentifier identifier = new TestIdentifier(value);
        assertFalse(identifier.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        String value = "testId";
        TestIdentifier identifier = new TestIdentifier(value);
        assertFalse(identifier.equals("not an identifier"));
    }

    @Test
    public void testEqualsWithDifferentIdentifiers() {
        TestIdentifier identifier1 = new TestIdentifier("id1");
        TestIdentifier identifier2 = new TestIdentifier("id2");
        assertFalse(identifier1.equals(identifier2));
    }

    @Test
    public void testEqualsWithSameIdentifiers() {
        TestIdentifier identifier1 = new TestIdentifier("sameId");
        TestIdentifier identifier2 = new TestIdentifier("sameId");
        assertTrue(identifier1.equals(identifier2));
    }

    @Test
    public void testToString() {
        String value = "testId";
        TestIdentifier identifier = new TestIdentifier(value);
        assertThat(identifier.toString(), is(value));
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
