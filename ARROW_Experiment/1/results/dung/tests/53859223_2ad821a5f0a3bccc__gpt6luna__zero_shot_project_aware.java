package com.nordea.oss.copybook.converters;

import com.nordea.oss.copybook.exceptions.TypeConverterException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IntegerToInteger_ArrowTest {

    @Test
    public void validateAcceptsIntegerAndPrimitiveInt() {
        IntegerToInteger converter = new IntegerToInteger();

        converter.validate(Integer.class, 9, 0);
        converter.validate(Integer.TYPE, 9, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void validateRejectsSizeGreaterThanNine() {
        new IntegerToInteger().validate(Integer.class, 10, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void validateRejectsUnsupportedType() {
        new IntegerToInteger().validate(Long.class, 9, 0);
    }

    @Test
    public void fromConvertsIntegerToBytes() {
        byte[] result = new IntegerToInteger().from(123, 3, 0, false);

        assertArrayEquals("123".getBytes(), result);
    }

    @Test
    public void fromAddsPaddingWhenRequested() {
        byte[] result = new IntegerToInteger().from(12, 4, 0, true);

        assertArrayEquals("0012".getBytes(), result);
    }

    @Test
    public void fromReturnsNullForNullWithoutDefault() {
        assertNull(new IntegerToInteger().from(null, 4, 0, false));
    }

    @Test(expected = TypeConverterException.class)
    public void fromRejectsNegativeValue() {
        new IntegerToInteger().from(-1, 2, 0, false);
    }

    @Test(expected = TypeConverterException.class)
    public void fromRejectsValueTooLargeForField() {
        new IntegerToInteger().from(123, 2, 0, false);
    }

    @Test
    public void toConvertsBytesToInteger() {
        Object result = new IntegerToInteger().to("123".getBytes(), 0, 3, 0, false);

        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void toConvertsZero() {
        Object result = new IntegerToInteger().to("0".getBytes(), 0, 1, 0, false);

        assertEquals(Integer.valueOf(0), result);
    }

    @Test(expected = TypeConverterException.class)
    public void toRejectsLeadingMinus() {
        new IntegerToInteger().to("-12".getBytes(), 0, 3, 0, false);
    }

    @Test(expected = TypeConverterException.class)
    public void toRejectsTrailingMinus() {
        new IntegerToInteger().to("12-".getBytes(), 0, 3, 0, false);
    }
}
