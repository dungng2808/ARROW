package com.nordea.oss.copybook.converters;

import com.nordea.oss.copybook.exceptions.TypeConverterException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IntegerToInteger_ArrowTest {

    @Test
    public void convertsBytesToInteger() {
        IntegerToInteger converter = new IntegerToInteger();
        assertEquals(Integer.valueOf(123), converter.to(new byte[] {'1', '2', '3'}, 0, 3, 0, false));
    }

    @Test
    public void convertsIntegerToBytes() {
        IntegerToInteger converter = new IntegerToInteger();
        assertArrayEquals(new byte[] {'1', '2', '3'}, converter.from(Integer.valueOf(123), 3, 0, false));
    }

    @Test
    public void returnsNullForNullValueWithoutDefault() {
        IntegerToInteger converter = new IntegerToInteger();
        assertNull(converter.from(null, 3, 0, false));
    }

    @Test(expected = TypeConverterException.class)
    public void rejectsNegativeValue() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.from(Integer.valueOf(-1), 3, 0, false);
    }

    @Test(expected = TypeConverterException.class)
    public void rejectsValueThatDoesNotFit() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.from(Integer.valueOf(1234), 3, 0, false);
    }

    @Test
    public void validatesIntegerTypesWithinSupportedSize() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(Integer.class, 9, 0);
        converter.validate(Integer.TYPE, 9, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void rejectsUnsupportedSize() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(Integer.class, 10, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void rejectsUnsupportedType() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(Long.class, 3, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void rejectsLeadingMinusSign() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.to(new byte[] {'-', '1'}, 0, 2, 0, false);
    }

    @Test(expected = TypeConverterException.class)
    public void rejectsTrailingMinusSign() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.to(new byte[] {'1', '-'}, 0, 2, 0, false);
    }
}
