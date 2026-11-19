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
        assertNotNull(source);
    }

    @Test
    public void equalityAndHashCodeUseConstructorArguments() {
        DataExpressionSource first = new DataExpressionSource(null, 1, null, null);
        DataExpressionSource second = new DataExpressionSource(null, 1, null, null);
        DataExpressionSource differentIndex = new DataExpressionSource(null, 2, null, null);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, differentIndex);
        assertFalse(first.equals(null));
    }

    @Test
    public void toStringIncludesIndexAndExpression() {
        DataExpressionSource source = new DataExpressionSource(null, 3, null, null);
        assertEquals("DataExpressionSource(null[3](null,null))", source.toString());
    }

    @Test
    public void getDataRejectsNegativeOffset() throws Exception {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        Method method = Source.class.getDeclaredMethod("getData", BigInteger.class, BigInteger.class);
        method.setAccessible(true);
        try {
            method.invoke(source, BigInteger.valueOf(-1), BigInteger.ZERO);
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            return;
        }
        throw new AssertionError("Expected an exception");
    }

    @Test
    public void isAvailableRejectsNegativeLength() throws Exception {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        Method method = Source.class.getDeclaredMethod("isAvailable", BigInteger.class, BigInteger.class);
        method.setAccessible(true);
        try {
            method.invoke(source, BigInteger.ZERO, BigInteger.valueOf(-1));
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            return;
        }
        throw new AssertionError("Expected an exception");
    }
}
