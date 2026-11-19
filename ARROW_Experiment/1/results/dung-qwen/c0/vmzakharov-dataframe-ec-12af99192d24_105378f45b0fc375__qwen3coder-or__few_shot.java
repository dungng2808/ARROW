package io.github.vmzakharov.ecdataframe.dsl.value;

import io.github.vmzakharov.ecdataframe.dsl.ArithmeticOp;
import io.github.vmzakharov.ecdataframe.dsl.PredicateOp;
import io.github.vmzakharov.ecdataframe.dsl.UnaryOp;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringValue_ArrowTest {

    @Test
    public void testStringValue() {
        StringValue stringValue = new StringValue("test");
        assertEquals("test", stringValue.stringValue());
    }

    @Test
    public void testAsStringLiteralWithoutQuotes() {
        StringValue stringValue = new StringValue("hello world");
        assertEquals("\"hello world\"", stringValue.asStringLiteral());
    }

    @Test
    public void testAsStringLiteralWithDoubleQuotes() {
        StringValue stringValue = new StringValue("hello \"world\"");
        assertEquals("'hello \"world\"'", stringValue.asStringLiteral());
    }

    @Test
    public void testGetType() {
        StringValue stringValue = new StringValue("test");
        assertEquals(ValueType.STRING, stringValue.getType());
    }

    @Test
    public void testApplyArithmeticOp() {
        StringValue stringValue = new StringValue("hello");
        ArithmeticOp op = new ArithmeticOp() {
            @Override
            public Value applyString(String left, String right) {
                return new StringValue(left + right);
            }

            @Override
            public Value applyNumber(Number left, Number right) {
                return null;
            }

            @Override
            public Value applyBoolean(Boolean left, Boolean right) {
                return null;
            }
        };
        
        Value result = stringValue.apply(new StringValue(" world"), op);
        assertEquals("hello world", result.stringValue());
    }

    @Test
    public void testApplyUnaryOp() {
        StringValue stringValue = new StringValue("hello");
        UnaryOp op = new UnaryOp() {
            @Override
            public Value applyString(String value) {
                return new StringValue(value.toUpperCase());
            }

            @Override
            public Value applyNumber(Number value) {
                return null;
            }

            @Override
            public Value applyBoolean(Boolean value) {
                return null;
            }
        };
        
        Value result = stringValue.apply(op);
        assertEquals("HELLO", result.stringValue());
    }

    @Test
    public void testApplyPredicateOp() {
        StringValue stringValue = new StringValue("hello");
        PredicateOp op = new PredicateOp() {
            @Override
            public BooleanValue applyString(String left, String right) {
                return new BooleanValue(left.equals(right));
            }

            @Override
            public BooleanValue applyNumber(Number left, Number right) {
                return null;
            }

            @Override
            public BooleanValue applyBoolean(Boolean left, Boolean right) {
                return null;
            }
        };
        
        BooleanValue result = stringValue.applyPredicate(new StringValue("hello"), op);
        assertTrue(result.booleanValue());
    }

    @Test
    public void testCompareToEqualStrings() {
        StringValue stringValue1 = new StringValue("hello");
        StringValue stringValue2 = new StringValue("hello");
        assertEquals(0, stringValue1.compareTo(stringValue2));
    }

    @Test
    public void testCompareToLessThan() {
        StringValue stringValue1 = new StringValue("apple");
        StringValue stringValue2 = new StringValue("banana");
        assertTrue(stringValue1.compareTo(stringValue2) < 0);
    }

    @Test
    public void testCompareToGreaterThan() {
        StringValue stringValue1 = new StringValue("zebra");
        StringValue stringValue2 = new StringValue("apple");
        assertTrue(stringValue1.compareTo(stringValue2) > 0);
    }

    @Test
    public void testCompareToNullValues() {
        StringValue stringValue1 = new StringValue(null);
        StringValue stringValue2 = new StringValue(null);
        assertEquals(0, stringValue1.compareTo(stringValue2));
    }

    @Test
    public void testCompareToNullFirst() {
        StringValue stringValue1 = new StringValue(null);
        StringValue stringValue2 = new StringValue("test");
        assertTrue(stringValue1.compareTo(stringValue2) < 0);
    }

    @Test
    public void testCompareToNullSecond() {
        StringValue stringValue1 = new StringValue("test");
        StringValue stringValue2 = new StringValue(null);
        assertTrue(stringValue1.compareTo(stringValue2) > 0);
    }
}
