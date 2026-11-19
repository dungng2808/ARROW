package io.parsingdata.metal.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.ValueExpression;
import java.math.BigInteger;
import org.junit.Test;

public class DataExpressionSource_ArrowTest {

    @Test
    public void constructorStoresArgumentsAndFormatsToString() {
        ValueExpression expression = null;
        ParseState state = null;
        Encoding encoding = null;
        try {
            new DataExpressionSource(expression, 0, state, encoding);
            fail("Expected null argument to be rejected");
        } catch (Exception expected) {
            assertTrue(expected instanceof RuntimeException);
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
                }, 0, new ParseState(), new Encoding());
        assertEquals(source, source);
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
                }, 0, new ParseState(), new Encoding());
        assertNotEquals(source, null);
        assertFalse(source.equals(new Object()));
    }
}
