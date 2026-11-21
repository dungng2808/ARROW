package io.github.vmzakharov.ecdataframe.dsl.value;

import io.github.vmzakharov.ecdataframe.dsl.ArithmeticOp;
import io.github.vmzakharov.ecdataframe.dsl.PredicateOp;
import io.github.vmzakharov.ecdataframe.dsl.UnaryOp;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StringValue_ArrowTest
{
    @Test
    public void stringValueReturnsOriginalString()
    {
        StringValue value = new StringValue("hello");
        assertEquals("hello", value.stringValue());
    }

    @Test
    public void asStringLiteralUsesDoubleQuotesWhenNoDoubleQuoteIsPresent()
    {
        StringValue value = new StringValue("hello");
        assertEquals("\"hello\"", value.asStringLiteral());
    }

    @Test
    public void asStringLiteralUsesSingleQuotesWhenDoubleQuoteIsPresent()
    {
        StringValue value = new StringValue("say \"hello\"");
        assertEquals("'say \"hello\"'", value.asStringLiteral());
    }

    @Test
    public void getTypeReturnsStringType()
    {
        StringValue value = new StringValue("hello");
        assertEquals(ValueType.STRING, value.getType());
    }

    @Test
    public void compareToOrdersStringsLexicographically()
    {
        StringValue value = new StringValue("apple");
        assertEquals(-1, value.compareTo(new StringValue("banana")));
    }

    @Test
    public void compareToReturnsZeroForEqualStrings()
    {
        StringValue value = new StringValue("same");
        assertEquals(0, value.compareTo(new StringValue("same")));
    }

    @Test
    public void compareToOrdersStringsByLengthWhenOneIsPrefix()
    {
        StringValue value = new StringValue("cat");
        assertEquals(-2, value.compareTo(new StringValue("cats")));
    }

    @Test
    public void applyArithmeticOperationReturnsAValue()
    {
        StringValue value = new StringValue("left");
        ArithmeticOp operation = new ArithmeticOp()
        {
            @Override
            public Value applyString(String left, String right)
            {
                return new StringValue(left + right);
            }
        };

        assertEquals("leftright", value.apply(new StringValue("right"), operation).stringValue());
    }

    @Test
    public void applyUnaryOperationReturnsAValue()
    {
        StringValue value = new StringValue("hello");
        UnaryOp operation = new UnaryOp()
        {
            @Override
            public Value applyString(String input)
            {
                return new StringValue(input.toUpperCase());
            }
        };

        assertEquals("HELLO", value.apply(operation).stringValue());
    }

    @Test
    public void applyPredicateReturnsBooleanValue()
    {
        StringValue value = new StringValue("same");
        PredicateOp operation = new PredicateOp()
        {
            @Override
            public BooleanValue applyString(String left, String right)
            {
                return new BooleanValue(left.equals(right));
            }
        };

        assertEquals(Boolean.TRUE, value.applyPredicate(new StringValue("same"), operation).booleanValue());
    }

    @Test(expected = RuntimeException.class)
    public void constructorRejectsNull()
    {
        new StringValue(null);
    }
}
