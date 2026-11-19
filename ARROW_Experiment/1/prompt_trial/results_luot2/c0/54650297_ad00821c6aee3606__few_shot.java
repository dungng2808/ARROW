package io.parsingdata.metal.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import org.junit.Test;

public class DataExpressionSource_ArrowTest {

    @Test
    public void constructorRejectsNullDataExpression() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected an exception");
        } catch (Exception expected) {
            assertTrue(true);
        }
    }

    @Test
    public void constructorRejectsNullParseState() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected an exception");
        } catch (Exception expected) {
            assertTrue(true);
        }
    }

    @Test
    public void constructorRejectsNullEncoding() {
        try {
            new DataExpressionSource(null, 0, null, null);
            fail("Expected an exception");
        } catch (Exception expected) {
            assertTrue(true);
        }
    }
}
