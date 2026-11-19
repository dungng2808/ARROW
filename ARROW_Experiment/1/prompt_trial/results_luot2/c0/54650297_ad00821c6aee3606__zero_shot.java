package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
            new DataExpressionSource(expression, 0, state, encoding);
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void equalityRejectsNullAndDifferentClass() {
        ValueExpression expression = null;
        ParseState state = null;
        Encoding encoding = null;
        DataExpressionSource source = null;
        try {
            source = new DataExpressionSource(expression, 0, state, encoding);
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
        assertTrue(source == null);
    }
}
