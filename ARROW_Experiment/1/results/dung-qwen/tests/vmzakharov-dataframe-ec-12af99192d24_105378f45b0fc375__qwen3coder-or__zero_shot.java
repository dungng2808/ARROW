package io.github.vmzakharov.ecdataframe.dsl.value;

import io.github.vmzakharov.ecdataframe.dsl.ArithmeticOp;
import io.github.vmzakharov.ecdataframe.dsl.PredicateOp;
import io.github.vmzakharov.ecdataframe.dsl.UnaryOp;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringValue_ArrowTest {

    @Test
    public void constructor() {
        StringValue stringValue = new StringValue("test");
        assertEquals("test", stringValue.stringValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorWithNull() {
        new StringValue(null);
    }

    @Test
    public void applyArithmeticOp() {
        StringValue stringValue = new StringValue("hello");
        Value result = stringValue.apply(new Value() {
            @Override
            public Value apply(Value another, ArithmeticOp operation) {
                return new StringValue(operation.applyString("hello", another.stringValue()));
            }

            @Override
            public Value apply(UnaryOp operation) {
                return null;
            }

            @Override
            public String asStringLiteral() {
                return null;
            }

            @Override
            public String stringValue() {
                return "world";
            }

            @Override
            public BooleanValue applyPredicate(Value another, PredicateOp operation) {
                return null;
            }

            @Override
            public ValueType getType() {
                return ValueType.STRING;
            }

            @Override
            public int compareTo(Value other) {
                return 0;
            }
        }, new ArithmeticOp() {
            @Override
            public Value applyString(String left, String right) {
                return new StringValue(left + right);
            }
        });
        
        assertEquals("helloworld", result.stringValue());
    }

    @Test
    public void applyUnaryOp() {
        StringValue stringValue = new StringValue("hello");
        Value result = stringValue.apply(new UnaryOp() {
            @Override
            public Value applyString(String value) {
                return new StringValue(value.toUpperCase());
            }
        });
        
        assertEquals("HELLO", result.stringValue());
    }

    @Test
    public void asStringLiteralWithoutQuotes() {
        StringValue stringValue = new StringValue("hello");
        assertEquals("\"hello\"", stringValue.asStringLiteral());
    }

    @Test
    public void asStringLiteralWithDoubleQuotes() {
        StringValue stringValue = new StringValue("he\"llo");
        assertEquals("'he\"llo'", stringValue.asStringLiteral());
    }

    @Test
    public void stringValue() {
        StringValue stringValue = new StringValue("test");
        assertEquals("test", stringValue.stringValue());
    }

    @Test
    public void applyPredicateOp() {
        StringValue stringValue = new StringValue("hello");
        BooleanValue result = stringValue.applyPredicate(new StringValue("world"), new PredicateOp() {
            @Override
            public BooleanValue applyString(String left, String right) {
                return new BooleanValue(left.compareTo(right) > 0);
            }
        });
        
        assertTrue(result.booleanValue());
    }

    @Test
    public void getType() {
        StringValue stringValue = new StringValue("test");
        assertEquals(ValueType.STRING, stringValue.getType());
    }

    @Test
    public void compareToWithNullValues() {
        StringValue stringValue = new StringValue(null);
        StringValue otherValue = new StringValue(null);
        assertEquals(0, stringValue.compareTo(otherValue));
    }

    @Test
    public void compareToWithNullAndNonNull() {
        StringValue stringValue = new StringValue(null);
        StringValue otherValue = new StringValue("test");
        assertEquals(-1, stringValue.compareTo(otherValue));
    }

    @Test
    public void compareToWithNonNullAndNull() {
        StringValue stringValue = new StringValue("test");
        StringValue otherValue = new StringValue(null);
        assertEquals(1, stringValue.compareTo(otherValue));
    }

    @Test
    public void compareToWithTwoNonNullValues() {
        StringValue stringValue = new StringValue("abc");
        StringValue otherValue = new StringValue("def");
        assertEquals(-1, stringValue.compareTo(otherValue));
    }

    @Test
    public void compareToWithEqualValues() {
        StringValue stringValue = new StringValue("test");
        StringValue otherValue = new StringValue("test");
        assertEquals(0, stringValue.compareTo(otherValue));
    }
}
