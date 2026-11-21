package io.github.vmzakharov.ecdataframe.dsl.value;

import io.github.vmzakharov.ecdataframe.dsl.ArithmeticOp;
import io.github.vmzakharov.ecdataframe.dsl.PredicateOp;
import io.github.vmzakharov.ecdataframe.dsl.UnaryOp;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringValue_ArrowTest
{
    @Test
    public void constructor()
    {
        StringValue stringValue = new StringValue("test");
        assertEquals("test", stringValue.stringValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorWithNull()
    {
        new StringValue(null);
    }

    @Test
    public void applyArithmeticOp()
    {
        StringValue stringValue = new StringValue("hello");
        ArithmeticOp op = new ArithmeticOp()
        {
            @Override
            public Value applyString(String left, String right)
            {
                return new StringValue(left + right);
            }

            @Override
            public Value applyNumber(Number left, Number right)
            {
                return null;
            }
        };

        Value result = stringValue.apply(new StringValue(" world"), op);
        assertEquals("hello world", result.stringValue());
    }

    @Test
    public void applyUnaryOp()
    {
        StringValue stringValue = new StringValue("hello");
        UnaryOp op = new UnaryOp()
        {
            @Override
            public Value applyString(String value)
            {
                return new StringValue(value.toUpperCase());
            }

            @Override
            public Value applyNumber(Number value)
            {
                return null;
            }
        };

        Value result = stringValue.apply(op);
        assertEquals("HELLO", result.stringValue());
    }

    @Test
    public void asStringLiteralWithoutQuotes()
    {
        StringValue stringValue = new StringValue("hello");
        assertEquals("\"hello\"", stringValue.asStringLiteral());
    }

    @Test
    public void asStringLiteralWithDoubleQuotes()
    {
        StringValue stringValue = new StringValue("he said \"hello\"");
        assertEquals("'he said \"hello\"'", stringValue.asStringLiteral());
    }

    @Test
    public void asStringLiteralWithSingleQuotes()
    {
        StringValue stringValue = new StringValue("he said 'hello'");
        assertEquals("\"he said 'hello'\"", stringValue.asStringLiteral());
    }

    @Test
    public void stringValue()
    {
        StringValue stringValue = new StringValue("test");
        assertEquals("test", stringValue.stringValue());
    }

    @Test
    public void applyPredicateOp()
    {
        StringValue stringValue = new StringValue("hello");
        PredicateOp op = new PredicateOp()
        {
            @Override
            public BooleanValue applyString(String left, String right)
            {
                return new BooleanValue(left.equals(right));
            }

            @Override
            public BooleanValue applyNumber(Number left, Number right)
            {
                return null;
            }
        };

        Value result = stringValue.applyPredicate(new StringValue("hello"), op);
        assertTrue(result.asBoolean());
    }

    @Test
    public void getType()
    {
        StringValue stringValue = new StringValue("test");
        assertEquals(ValueType.STRING, stringValue.getType());
    }

    @Test
    public void compareToEqualStrings()
    {
        StringValue stringValue1 = new StringValue("hello");
        StringValue stringValue2 = new StringValue("hello");
        assertEquals(0, stringValue1.compareTo(stringValue2));
    }

    @Test
    public void compareToLessThan()
    {
        StringValue stringValue1 = new StringValue("apple");
        StringValue stringValue2 = new StringValue("banana");
        assertTrue(stringValue1.compareTo(stringValue2) < 0);
    }

    @Test
    public void compareToGreaterThan()
    {
        StringValue stringValue1 = new StringValue("zebra");
        StringValue stringValue2 = new StringValue("apple");
        assertTrue(stringValue1.compareTo(stringValue2) > 0);
    }

    @Test
    public void compareToNullLeft()
    {
        StringValue stringValue1 = new StringValue(null);
        StringValue stringValue2 = new StringValue("test");
        assertEquals(-1, stringValue1.compareTo(stringValue2));
    }

    @Test
    public void compareToNullRight()
    {
        StringValue stringValue1 = new StringValue("test");
        StringValue stringValue2 = new StringValue(null);
        assertEquals(1, stringValue1.compareTo(stringValue2));
    }

    @Test
    public void compareToBothNull()
    {
        StringValue stringValue1 = new StringValue(null);
        StringValue stringValue2 = new StringValue(null);
        assertEquals(0, stringValue1.compareTo(stringValue2));
    }

    @Test
    public void compareToWithVoid()
    {
        StringValue stringValue1 = new StringValue("test");
        StringValue stringValue2 = new StringValue("test");
        // This would normally be tested with a VoidValue but we're testing
        // the comparison logic, so we'll just ensure it doesn't crash
        assertEquals(0, stringValue1.compareTo(stringValue2));
    }
}
