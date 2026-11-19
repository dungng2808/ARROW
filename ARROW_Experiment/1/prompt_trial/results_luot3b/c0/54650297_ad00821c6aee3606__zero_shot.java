package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.math.BigInteger;

import org.junit.Test;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.ValueExpression;

public class DataExpressionSource_ArrowTest {

    @Test
    public void constructorStoresArgumentsAndFormatsToString() {
        ValueExpression expression = null;
        ParseState state = null;
        Encoding encoding = null;
        try {
            expression = (ValueExpression) java.lang.reflect.Proxy.newProxyInstance(
                ValueExpression.class.getClassLoader(),
                new Class<?>[] { ValueExpression.class },
                (proxy, method, args) -> method.getName().equals("toString") ? "expression" : null);
            state = (ParseState) java.lang.reflect.Proxy.newProxyInstance(
                ParseState.class.getClassLoader(),
                new Class<?>[] { ParseState.class },
                (proxy, method, args) -> method.getName().equals("toString") ? "state" : null);
            encoding = (Encoding) java.lang.reflect.Proxy.newProxyInstance(
                Encoding.class.getClassLoader(),
                new Class<?>[] { Encoding.class },
                (proxy, method, args) -> method.getName().equals("toString") ? "encoding" : null);
        } catch (IllegalArgumentException e) {
            throw e;
        }

        DataExpressionSource source = new DataExpressionSource(expression, 2, state, encoding);

        assertEquals(expression, source.dataExpression);
        assertEquals(2, source.index);
        assertEquals(state, source.parseState);
        assertEquals(encoding, source.encoding);
        assertEquals("DataExpressionSource(expression[2](state,encoding))", source.toString());
    }

    @Test
    public void equalSourcesHaveEqualHashCodes() {
        ValueExpression expression = proxy(ValueExpression.class, "expression");
        ParseState state = proxy(ParseState.class, "state");
        Encoding encoding = proxy(Encoding.class, "encoding");

        DataExpressionSource first = new DataExpressionSource(expression, 0, state, encoding);
        DataExpressionSource second = new DataExpressionSource(expression, 0, state, encoding);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void sourcesWithDifferentIndicesAreNotEqual() {
        ValueExpression expression = proxy(ValueExpression.class, "expression");
        ParseState state = proxy(ParseState.class, "state");
        Encoding encoding = proxy(Encoding.class, "encoding");

        DataExpressionSource first = new DataExpressionSource(expression, 0, state, encoding);
        DataExpressionSource second = new DataExpressionSource(expression, 1, state, encoding);

        assertNotEquals(first, second);
    }

    @Test
    public void sourceIsNotEqualToNullOrAnotherClass() {
        ValueExpression expression = proxy(ValueExpression.class, "expression");
        ParseState state = proxy(ParseState.class, "state");
        Encoding encoding = proxy(Encoding.class, "encoding");
        DataExpressionSource source = new DataExpressionSource(expression, 0, state, encoding);

        assertFalse(source.equals(null));
        assertFalse(source.equals("source"));
    }

    @Test
    public void cacheStartsNull() {
        ValueExpression expression = proxy(ValueExpression.class, "expression");
        ParseState state = proxy(ParseState.class, "state");
        Encoding encoding = proxy(Encoding.class, "encoding");

        DataExpressionSource source = new DataExpressionSource(expression, 0, state, encoding);

        assertTrue(source.cache == null);
    }

    private static <T> T proxy(Class<T> type, String text) {
        return type.cast(java.lang.reflect.Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] { type },
            (proxy, method, args) -> {
                if (method.getName().equals("toString")) {
                    return text;
                }
                if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (method.getName().equals("equals")) {
                    return proxy == args[0];
                }
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                }
                if (returnType == int.class) {
                    return 0;
                }
                if (returnType == long.class) {
                    return 0L;
                }
                return null;
            }));
    }
}
