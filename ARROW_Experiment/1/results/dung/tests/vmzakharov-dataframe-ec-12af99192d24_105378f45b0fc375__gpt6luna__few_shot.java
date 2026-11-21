package io.github.vmzakharov.ecdataframe.dsl.value;

import io.github.vmzakharov.ecdataframe.dsl.ArithmeticOp;
import io.github.vmzakharov.ecdataframe.dsl.PredicateOp;
import io.github.vmzakharov.ecdataframe.dsl.UnaryOp;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
    public void getTypeReturnsStringType()
    {
        StringValue value = new StringValue("hello");
        assertEquals(ValueType.STRING, value.getType());
    }

    @Test
    public void compareToOrdersStringsLexicographically()
    {
        StringValue first = new StringValue("apple");
        StringValue second = new StringValue("banana");
        assertTrue(first.compareTo(second) < 0);
        assertTrue(second.compareTo(first) > 0);
    }

    @Test
    public void compareToReturnsZeroForEqualStrings()
    {
        StringValue first = new StringValue("same");
        StringValue second = new StringValue("same");
        assertEquals(0, first.compareTo(second));
    }

    @Test
    public void applyArithmeticOperationDelegatesStringValues()
    {
        StringValue first = new StringValue("left");
        StringValue second = new StringValue("right");
        Value result = first.apply(second, ArithmeticOp.ADD);
        assertEquals("leftright", result.stringValue());
    }

    @Test
    public void applyUnaryOperationDelegatesStringValue()
    {
        StringValue value = new StringValue("hello");
        Value result = value.apply(UnaryOp.UPPER);
        assertEquals("HELLO", result.stringValue());
    }

    @Test
    public void applyPredicateDelegatesStringValues()
    {
        StringValue first = new StringValue("same");
        StringValue second = new StringValue("same");
        BooleanValue result = first.applyPredicate(second, PredicateOp.EQUAL);
        assertTrue(result.booleanValue());
    }

    @Test(expected = Exception.class)
    public void constructorRejectsNull()
    {
        new StringValue(null);
    }
}
