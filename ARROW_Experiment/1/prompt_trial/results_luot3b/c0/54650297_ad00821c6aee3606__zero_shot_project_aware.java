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
    public void equalityAndHashCodeUseConstructorFields() {
        ValueExpression expression = (ValueExpression) java.lang.reflect.Proxy.newProxyInstance(
            ValueExpression.class.getClassLoader(), new Class<?>[] { ValueExpression.class },
            (proxy, method, args) -> null);
        ParseState state = (ParseState) java.lang.reflect.Proxy.newProxyInstance(
            ParseState.class.getClassLoader(), new Class<?>[] { ParseState.class },
            (proxy, method, args) -> null);
        Encoding encoding = (Encoding) java.lang.reflect.Proxy.newProxyInstance(
            Encoding.class.getClassLoader(), new Class<?>[] { Encoding.class },
            (proxy, method, args) -> null);

        DataExpressionSource first = new DataExpressionSource(expression, 1, state, encoding);
        DataExpressionSource same = new DataExpressionSource(expression, 1, state, encoding);
        DataExpressionSource different = new DataExpressionSource(expression, 2, state, encoding);

        assertTrue(first.equals(same));
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(different));
        assertFalse(first.equals(null));
        assertFalse(first.equals(new Object()));
    }

    @Test
    public void toStringIncludesExpressionIndexStateAndEncoding() {
        ValueExpression expression = (ValueExpression) java.lang.reflect.Proxy.newProxyInstance(
            ValueExpression.class.getClassLoader(), new Class<?>[] { ValueExpression.class },
            (proxy, method, args) -> "expression");
        ParseState state = (ParseState) java.lang.reflect.Proxy.newProxyInstance(
            ParseState.class.getClassLoader(), new Class<?>[] { ParseState.class },
            (proxy, method, args) -> "state");
        Encoding encoding = (Encoding) java.lang.reflect.Proxy.newProxyInstance(
            Encoding.class.getClassLoader(), new Class<?>[] { Encoding.class },
            (proxy, method, args) -> "encoding");

        DataExpressionSource source = new DataExpressionSource(expression, 3, state, encoding);

        assertEquals("DataExpressionSource(expression[3](state,encoding))", source.toString());
    }

    @Test(expected = RuntimeException.class)
    public void constructorRejectsNullExpression() {
        ParseState state = (ParseState) java.lang.reflect.Proxy.newProxyInstance(
            ParseState.class.getClassLoader(), new Class<?>[] { ParseState.class },
            (proxy, method, args) -> null);
        Encoding encoding = (Encoding) java.lang.reflect.Proxy.newProxyInstance(
            Encoding.class.getClassLoader(), new Class<?>[] { Encoding.class },
            (proxy, method, args) -> null);

        new DataExpressionSource(null, 0, state, encoding);
    }

    @Test(expected = RuntimeException.class)
    public void constructorRejectsNullParseState() {
        ValueExpression expression = (ValueExpression) java.lang.reflect.Proxy.newProxyInstance(
            ValueExpression.class.getClassLoader(), new Class<?>[] { ValueExpression.class },
            (proxy, method, args) -> null);
        Encoding encoding = (Encoding) java.lang.reflect.Proxy.newProxyInstance(
            Encoding.class.getClassLoader(), new Class<?>[] { Encoding.class },
            (proxy, method, args) -> null);

        new DataExpressionSource(expression, 0, null, encoding);
    }

    @Test(expected = RuntimeException.class)
    public void constructorRejectsNullEncoding() {
        ValueExpression expression = (ValueExpression) java.lang.reflect.Proxy.newProxyInstance(
            ValueExpression.class.getClassLoader(), new Class<?>[] { ValueExpression.class },
            (proxy, method, args) -> null);
        ParseState state = (ParseState) java.lang.reflect.Proxy.newProxyInstance(
            ParseState.class.getClassLoader(), new Class<?>[] { ParseState.class },
            (proxy, method, args) -> null);

        new DataExpressionSource(expression, 0, state, null);
    }
}
