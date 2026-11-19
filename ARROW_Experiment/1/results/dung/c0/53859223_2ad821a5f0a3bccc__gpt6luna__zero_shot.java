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
        converter.validate(Integer.TYPE, 0, 0);
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
    public void toParsesInteger() {
        Integer result = (Integer) new IntegerToInteger().to(
                new byte[]{'1', '2', '3'}, 0, 3, 0, false);
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void fromFormatsIntegerWithoutPadding() {
        byte[] result = new IntegerToInteger().from(Integer.valueOf(123), 5, 0, false);
        assertArrayEquals(new byte[]{'1', '2', '3'}, result);
    }

    @Test
    public void fromFormatsIntegerWithPadding() {
        byte[] result = new IntegerToInteger().from(Integer.valueOf(123), 5, 0, true);
        assertArrayEquals(new byte[]{'0', '0', '1', '2', '3'}, result);
    }

    @Test
    public void fromReturnsNullForNullValueWithoutDefault() {
        assertNull(new IntegerToInteger().from(null, 5, 0, false));
    }

    @Test(expected = TypeConverterException.class)
    public void fromRejectsNegativeValue() {
        new IntegerToInteger().from(Integer.valueOf(-1), 3, 0, false);
    }

    @Test(expected = TypeConverterException.class)
    public void fromRejectsValueTooLargeForField() {
        new IntegerToInteger().from(Integer.valueOf(1234), 3, 0, false);
    }

    @Test(expected = TypeConverterException.class)
    public void toRejectsNegativeSignAtStart() {
        new IntegerToInteger().to(new byte[]{'-', '1'}, 0, 2, 0, false);
    }

    @Test(expected = TypeConverterException.class)
    public void toRejectsNegativeSignAtEnd() {
        new IntegerToInteger().to(new byte[]{'1', '-'}, 0, 2, 0, false);
    }
}
