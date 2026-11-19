package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;

import org.junit.Test;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.ValueExpression;

public class DataExpressionSource_ArrowTest {

    @Test
    public void constructorRejectsNullDataExpression() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected a null data expression to be rejected");
        } catch (NullPointerException expected) {
            assertNotEquals(null, expected.getMessage());
        }
    }

    @Test
    public void constructorRejectsNullParseState() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected a null data expression to be rejected first");
        } catch (NullPointerException expected) {
            assertTrue(expected.getMessage().contains("dataExpression"));
        }
    }

    @Test
    public void constructorRejectsNullEncoding() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected a null data expression to be rejected first");
        } catch (NullPointerException expected) {
            assertTrue(expected.getMessage().contains("dataExpression"));
        }
    }

    @Test
    public void equalityAndHashCodeForSameInstance() {
        DataExpressionSource source = new DataExpressionSource(
            new ValueExpression() {
                @Override
                public ImmutableList<io.parsingdata.metal.expression.value.Value> eval(ParseState state, Encoding encoding) {
                    return null;
                }
            },
            0,
            null,
            null
        );
        assertTrue(source.equals(source));
        assertEquals(source.hashCode(), source.hashCode());
    }

    @Test
    public void equalityRejectsNullAndDifferentClass() {
        DataExpressionSource source = new DataExpressionSource(
            new ValueExpression() {
                @Override
                public ImmutableList<io.parsingdata.metal.expression.value.Value> eval(ParseState state, Encoding encoding) {
                    return null;
                }
            },
            0,
            null,
            null
        );
        assertFalse(source.equals(null));
        assertFalse(source.equals("other"));
    }

    @Test
    public void toStringIncludesIndex() {
        DataExpressionSource source = new DataExpressionSource(
            new ValueExpression() {
                @Override
                public ImmutableList<io.parsingdata.metal.expression.value.Value> eval(ParseState state, Encoding encoding) {
                    return null;
                }
            },
            2,
            null,
            null
        );
        assertTrue(source.toString().contains("[2]"));
    }
}
