package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

public class DataExpressionSource_ArrowTest {

    @Test
    public void exposesConstructorArguments() {
        DataExpressionSource source = new DataExpressionSource(null, 2, null, null);
        assertEquals(2, source.index);
    }

    @Test
    public void equalObjectsHaveEqualHashCodes() {
        DataExpressionSource first = new DataExpressionSource(null, 1, null, null);
        DataExpressionSource second = new DataExpressionSource(null, 1, null, null);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentIndicesAreNotEqual() {
        DataExpressionSource first = new DataExpressionSource(null, 0, null, null);
        DataExpressionSource second = new DataExpressionSource(null, 1, null, null);

        assertFalse(first.equals(second));
        assertNotEquals(first, second);
    }

    @Test
    public void isNotEqualToNull() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);

        assertFalse(source.equals(null));
    }

    @Test
    public void toStringIncludesClassNameAndIndex() {
        DataExpressionSource source = new DataExpressionSource(null, 3, null, null);

        assertEquals("DataExpressionSource(null[3](null,null))", source.toString());
    }
}
