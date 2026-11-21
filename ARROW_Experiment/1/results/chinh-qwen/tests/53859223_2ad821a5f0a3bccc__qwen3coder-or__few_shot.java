package com.nordea.oss.copybook.converters;

import com.nordea.oss.copybook.exceptions.TypeConverterException;
import com.nordea.oss.copybook.converters.IntegerToInteger;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntegerToInteger_ArrowTest {

    @Test
    public void testValidateValidInt() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(Integer.class, 4, 0);
    }

    @Test
    public void testValidateValidPrimitiveInt() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(int.class, 4, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void testValidateInvalidSize() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(Integer.class, 10, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void testValidateInvalidType() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(String.class, 4, 0);
    }

    @Test
    public void testToWithDefaultValueAndNullBytes() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.setDefaultValue("42");
        converter.setNullFillerByte((byte) 0x20);
        byte[] bytes = new byte[]{0x20, 0x20, 0x20};
        Object result = converter.to(bytes, 0, 3, 0, false);
        assertEquals(42, result);
    }

    @Test
    public void testToWithNormalValue() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = new byte[]{0x31, 0x32, 0x33}; // "123"
        Object result = converter.to(bytes, 0, 3, 0, false);
        assertEquals(123, result);
    }

    @Test(expected = TypeConverterException.class)
    public void testToWithInvalidIntegerString() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = new byte[]{0x2D, 0x31, 0x32}; // "-12"
        converter.to(bytes, 0, 3, 0, false);
    }

    @Test
    public void testFromWithValidValueAndPadding() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] result = converter.from(123, 5, 0, true);
        assertNotNull(result);
        assertEquals("123", new String(result));
    }

    @Test
    public void testFromWithValidValueNoPadding() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] result = converter.from(123, 5, 0, false);
        assertNotNull(result);
        assertEquals("123", new String(result));
    }

    @Test(expected = TypeConverterException.class)
    public void testFromWithNegativeValue() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.from(-1, 5, 0, true);
    }

    @Test(expected = TypeConverterException.class)
    public void testFromWithValueTooLarge() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.from(123456, 3, 0, true);
    }

    @Test
    public void testFromWithNullValueAndDefaultValue() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.setDefaultValue("999");
        byte[] result = converter.from(null, 5, 0, true);
        assertNotNull(result);
        assertEquals("999", new String(result));
    }

    @Test
    public void testFromWithNullValueAndNoDefaultValue() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] result = converter.from(null, 5, 0, true);
        assertNull(result);
    }

    @Test
    public void testGetIntegerStringWithNormalValue() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = new byte[]{0x31, 0x32, 0x33}; // "123"
        String result = converter.getIntegerString(bytes, 0, 3, false);
        assertEquals("123", result);
    }

    @Test(expected = TypeConverterException.class)
    public void testGetIntegerStringWithStartDash() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = new byte[]{0x2D, 0x31, 0x32}; // "-12"
        converter.getIntegerString(bytes, 0, 3, false);
    }

    @Test(expected = TypeConverterException.class)
    public void testGetIntegerStringWithEndDash() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = new byte[]{0x31, 0x32, 0x2D}; // "12-"
        converter.getIntegerString(bytes, 0, 3, false);
    }
}
