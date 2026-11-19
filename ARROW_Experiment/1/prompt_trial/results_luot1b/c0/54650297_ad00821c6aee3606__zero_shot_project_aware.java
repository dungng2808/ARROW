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
    public void constructorStoresArgumentsAndInitializesCache() {
        ValueExpression expression = null;
        ParseState state = null;
        Encoding encoding = null;

        try {
            expression = (ValueExpression) java.lang.reflect.Proxy.newProxyInstance(
                ValueExpression.class.getClassLoader(),
                new Class<?>[] { ValueExpression.class },
                (proxy, method, args) -> null);
            state = (ParseState) java.lang.reflect.Proxy.newProxyInstance(
                ParseState.class.getClassLoader(),
                new Class<?>[] { ParseState.class },
                (proxy, method, args) -> null);
            encoding = (Encoding) java.lang.reflect.Proxy.newProxyInstance(
                Encoding.class.getClassLoader(),
                new Class<?>[] { Encoding.class },
                (proxy, method, args) -> null);
        } catch (IllegalArgumentException e) {
            throw e;
        }

        DataExpressionSource source = new DataExpressionSource(expression, 0, state, encoding);

        assertEquals(expression, source.dataExpression);
        assertEquals(0, source.index);
        assertEquals(state, source.parseState);
        assertEquals(encoding, source.encoding);
        assertEquals(null, source.cache);
    }

    @Test
    public void constructorRejectsNullExpression() {
        ParseState state = proxy(ParseState.class);
        Encoding encoding = proxy(Encoding.class);
        try {
            new DataExpressionSource(null, 0, state, encoding);
        } catch (NullPointerException expected) {
            assertNotNull(expected.getMessage());
            return;
        }
        throw new AssertionError("Expected NullPointerException");
    }

    @Test
    public void constructorRejectsNullParseState() {
        ValueExpression expression = proxy(ValueExpression.class);
        Encoding encoding = proxy(Encoding.class);
        try {
            new DataExpressionSource(expression, 0, null, encoding);
        } catch (NullPointerException expected) {
            assertNotNull(expected.getMessage());
            return;
        }
        throw new AssertionError("Expected NullPointerException");
    }

    @Test
    public void constructorRejectsNullEncoding() {
        ValueExpression expression = proxy(ValueExpression.class);
        ParseState state = proxy(ParseState.class);
        try {
            new DataExpressionSource(expression, 0, state, null);
        } catch (NullPointerException expected) {
            assertNotNull(expected.getMessage());
            return;
        }
        throw new AssertionError("Expected NullPointerException");
    }

    @Test
    public void equalityAndHashCodeUseConstructorFields() {
        ValueExpression expression = proxy(ValueExpression.class);
        ParseState state = proxy(ParseState.class);
        Encoding encoding = proxy(Encoding.class);

        DataExpressionSource first = new DataExpressionSource(expression, 2, state, encoding);
        DataExpressionSource second = new DataExpressionSource(expression, 2, state, encoding);
        DataExpressionSource differentIndex = new DataExpressionSource(expression, 1, state, encoding);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, differentIndex);
        assertFalse(first.equals(null));
        assertFalse(first.equals(new Object()));
    }

    @Test
    public void toStringIncludesExpressionIndexStateAndEncoding() {
        ValueExpression expression = proxy(ValueExpression.class);
        ParseState state = proxy(ParseState.class);
        Encoding encoding = proxy(Encoding.class);
        DataExpressionSource source = new DataExpressionSource(expression, 3, state, encoding);

        String result = source.toString();

        assertTrue(result.startsWith("DataExpressionSource("));
        assertTrue(result.contains("[3]("));
        assertTrue(result.endsWith("))"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] { type },
            (proxy, method, args) -> {
                if (method.getName().equals("toString")) {
                    return type.getSimpleName();
                }
                if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (method.getName().equals("equals")) {
                    return proxy == args[0];
                }
                return null;
            });
    }
}
