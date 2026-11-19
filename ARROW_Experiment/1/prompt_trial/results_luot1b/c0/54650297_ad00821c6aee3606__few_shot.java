package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import org.junit.Test;

public class DataExpressionSource_ArrowTest {

    @Test
    public void constructorRejectsNullDataExpression() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("dataExpression", expected.getMessage());
        }
    }

    @Test
    public void constructorRejectsNullParseState() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("dataExpression", expected.getMessage());
        }
    }

    @Test
    public void constructorRejectsNullEncoding() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("dataExpression", expected.getMessage());
        }
    }

    @Test
    public void equalityAndHashCodeForSameInstance() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertEquals(source, source);
        assertEquals(source.hashCode(), source.hashCode());
    }

    @Test
    public void equalityRejectsNullAndDifferentClass() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertNotEquals(source, null);
        assertNotEquals(source, new Object());
    }

    @Test
    public void toStringIncludesClassNameAndIndex() {
        DataExpressionSource source = new DataExpressionSource(null, 3, null, null);
        assertTrue(source.toString().startsWith("DataExpressionSource(null[3]("));
    }

    @Test
    public void publicFieldsRetainConstructorArguments() {
        DataExpressionSource source = new DataExpressionSource(null, -2, null, null);
        assertEquals(-2, source.index);
        assertEquals(null, source.dataExpression);
        assertEquals(null, source.parseState);
        assertEquals(null, source.encoding);
        assertEquals(null, source.cache);
    }

    @Test
    public void cacheCanBeAssigned() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        byte[] bytes = new byte[] {1, 2};
        source.cache = bytes;
        assertTrue(source.cache == bytes);
    }

    @Test
    public void equalityIsFalseForDifferentIndex() {
        DataExpressionSource first = new DataExpressionSource(null, 0, null, null);
        DataExpressionSource second = new DataExpressionSource(null, 1, null, null);
        assertFalse(first.equals(second));
    }
}
