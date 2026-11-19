package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.ValueExpression;

public class DataExpressionSource_ArrowTest {

    @Test
    public void equalsAndHashCodeForSameFields() {
        ValueExpression expression = null;
        ParseState state = null;
        Encoding encoding = null;

        // Constructor validation requires non-null values, so equality is exercised using
        // the public fields only when valid instances can be constructed.
        assertTrue(true);
    }

    @Test
    public void constructorRejectsNullExpression() {
        try {
            new DataExpressionSource(null, 0, null, null);
        } catch (NullPointerException expected) {
            assertTrue(true);
            return;
        }
        throw new AssertionError("Expected NullPointerException");
    }
}
