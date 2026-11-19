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

    private DataExpressionSource createSource() throws Exception {
        ValueExpression expression = null;
        for (Method method : ValueExpression.class.getMethods()) {
            if (method.getName().equals("constant") && method.getParameterTypes().length == 1
                    && java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                expression = (ValueExpression) method.invoke(null, new Object[] { null });
                break;
            }
        }
        if (expression == null) {
            throw new IllegalStateException("No usable ValueExpression factory");
        }
        return new DataExpressionSource(expression, 0, null, null);
    }

    @Test
    public void constructorStoresItsArguments() throws Exception {
        DataExpressionSource source = createSource();
        assertNotNull(source.dataExpression);
        assertEquals(0, source.index);
        assertEquals(null, source.parseState);
        assertEquals(null, source.encoding);
        assertEquals(null, source.cache);
    }

    @Test
    public void equalSourcesHaveEqualHashCodes() throws Exception {
        DataExpressionSource first = createSource();
        DataExpressionSource second = new DataExpressionSource(first.dataExpression, 0, null, null);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void sourcesWithDifferentIndicesAreNotEqual() throws Exception {
        DataExpressionSource first = createSource();
        DataExpressionSource second = new DataExpressionSource(first.dataExpression, 1, null, null);
        assertNotEquals(first, second);
    }

    @Test
    public void sourceIsNotEqualToNullOrAnotherClass() throws Exception {
        DataExpressionSource source = createSource();
        assertFalse(source.equals(null));
        assertFalse(source.equals("source"));
    }

    @Test
    public void toStringIncludesExpressionAndIndex() throws Exception {
        DataExpressionSource source = createSource();
        assertTrue(source.toString().startsWith("DataExpressionSource("));
        assertTrue(source.toString().contains("[0]"));
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullExpression() {
        new DataExpressionSource(null, 0, null, null);
    }

    @Test
    public void cacheCanBeAssignedDirectly() throws Exception {
        DataExpressionSource source = createSource();
        byte[] bytes = new byte[] { 1, 2, 3 };
        source.cache = bytes;
        assertArrayEquals(new byte[] { 1, 2, 3 }, source.cache);
    }
}
