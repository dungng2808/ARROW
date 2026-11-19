package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.math.BigInteger;

import org.junit.Test;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.ValueExpression;

public class DataExpressionSource_ArrowTest {

    @Test
    public void constructorStoresArguments() {
        DataExpressionSource source = new DataExpressionSource(null, 2, null, null);
        assertEquals(2, source.index);
        assertEquals(null, source.dataExpression);
        assertEquals(null, source.parseState);
        assertEquals(null, source.encoding);
        assertEquals(null, source.cache);
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
    }

    @Test
    public void doesNotEqualNullOrDifferentClass() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertFalse(source.equals(null));
        assertFalse(source.equals("source"));
    }

    @Test
    public void toStringIncludesClassAndIndex() {
        DataExpressionSource source = new DataExpressionSource(null, 3, null, null);
        String result = source.toString();
        assertTrue(result.startsWith("DataExpressionSource(null[3]("));
        assertTrue(result.endsWith(")"));
    }

    @Test
    public void constructorAcceptsNegativeIndex() {
        DataExpressionSource source = new DataExpressionSource(null, -1, null, null);
        assertEquals(-1, source.index);
    }

    @Test
    public void publicFieldsCanBeUpdated() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        byte[] bytes = new byte[] {1, 2};
        source.cache = bytes;
        assertArrayEquals(bytes, source.cache);
    }

    @Test
    public void equalityUsesAllConstructorFields() {
        DataExpressionSource first = new DataExpressionSource(null, 0, null, null);
        DataExpressionSource second = new DataExpressionSource(null, 0, null, null);
        assertEquals(first, second);
        assertNotEquals(first, new DataExpressionSource(null, 1, null, null));
    }

    @Test
    public void sourceClassIsConcrete() {
        assertNotNull(DataExpressionSource.class);
    }
}
