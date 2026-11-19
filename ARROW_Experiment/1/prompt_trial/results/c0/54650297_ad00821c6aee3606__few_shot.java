package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.math.BigInteger;
import org.junit.Test;

public class DataExpressionSource_ArrowTest {

    private byte[] readData(DataExpressionSource source, BigInteger offset, BigInteger length) throws Exception {
        Method method = Source.class.getDeclaredMethod("getData", BigInteger.class, BigInteger.class);
        method.setAccessible(true);
        return (byte[]) method.invoke(source, offset, length);
    }

    private boolean available(DataExpressionSource source, BigInteger offset, BigInteger length) throws Exception {
        Method method = Source.class.getDeclaredMethod("isAvailable", BigInteger.class, BigInteger.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(source, offset, length);
    }

    @Test
    public void constructorRejectsNullDataExpression() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected a null data expression to be rejected");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void constructorRejectsNullParseState() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected a null parse state to be rejected");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void constructorRejectsNullEncoding() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected a null encoding to be rejected");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void equalityAndHashCodeForSameObject() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertTrue(source.equals(source));
        assertEquals(source.hashCode(), source.hashCode());
    }

    @Test
    public void equalityRejectsNullAndDifferentClass() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertFalse(source.equals(null));
        assertFalse(source.equals(new Object()));
    }

    @Test
    public void toStringIncludesClassNameAndIndex() {
        DataExpressionSource source = new DataExpressionSource(null, 3, null, null);
        assertTrue(source.toString().startsWith("DataExpressionSource(null[3]("));
    }

    @Test
    public void cacheIsInitiallyNull() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertEquals(null, source.cache);
    }
}
