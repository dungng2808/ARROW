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
    public void asStringLiteralUsesDoubleQuotesWhenValueHasNoDoubleQuote()
    {
        StringValue value = new StringValue("hello");

        assertEquals("\"hello\"", value.asStringLiteral());
    }

    @Test
    public void asStringLiteralUsesSingleQuotesWhenValueHasDoubleQuote()
    {
        StringValue value = new StringValue("say \"hello\"");

        assertEquals("'say \"hello\"'", value.asStringLiteral());
    }

    @Test
    public void getTypeReturnsString()
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
    public void applyWithArithmeticOperationReturnsAValue()
    {
        StringValue value = new StringValue("left");

        Value result = value.apply(new StringValue("right"), ArithmeticOp.ADD);

        assertNotNull(result);
    }

    @Test
    public void applyWithUnaryOperationReturnsAValue()
    {
        StringValue value = new StringValue("hello");

        Value result = value.apply(UnaryOp.values()[0]);

        assertNotNull(result);
    }

    @Test
    public void applyPredicateReturnsBooleanValue()
    {
        StringValue value = new StringValue("same");

        BooleanValue result = value.applyPredicate(new StringValue("same"), PredicateOp.EQUAL);

        assertEquals(Boolean.TRUE, result.booleanValue());
    }
}
