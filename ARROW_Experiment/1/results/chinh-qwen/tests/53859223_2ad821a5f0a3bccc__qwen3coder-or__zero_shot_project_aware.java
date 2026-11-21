package com.nordea.oss.copybook.converters;

import com.nordea.oss.copybook.exceptions.TypeConverterException;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntegerToInteger_ArrowTest {

    @Test
    public void testValidateWithValidIntTypeAndSize() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(Integer.class, 5, 0);
        converter.validate(int.class, 9, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void testValidateWithInvalidSize() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(Integer.class, 10, 0);
    }

    @Test(expected = TypeConverterException.class)
    public void testValidateWithInvalidType() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.validate(String.class, 5, 0);
    }

    @Test
    public void testToWithValidData() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = "12345".getBytes();
        Object result = converter.to(bytes, 0, 5, 0, false);
        assertEquals(12345, result);
    }

    @Test
    public void testToWithNegativeValue() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = "-12345".getBytes();
        try {
            converter.to(bytes, 0, 6, 0, false);
            fail("Expected TypeConverterException");
        } catch (TypeConverterException e) {
            assertEquals("Integer value can not start or end with -", e.getMessage());
        }
    }

    @Test
    public void testToWithTrailingDash() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = "12345-".getBytes();
        try {
            converter.to(bytes, 0, 6, 0, false);
            fail("Expected TypeConverterException");
        } catch (TypeConverterException e) {
            assertEquals("Integer value can not start or end with -", e.getMessage());
        }
    }

    @Test
    public void testToWithDefaultValue() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.setDefaultValue("999");
        byte[] bytes = new byte[5];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ' ';
        }
        Object result = converter.to(bytes, 0, 5, 0, false);
        assertEquals(999, result);
    }

    @Test
    public void testFromWithValidInteger() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] result = converter.from(12345, 5, 0, true);
        assertNotNull(result);
        assertEquals("12345", new String(result));
    }

    @Test(expected = TypeConverterException.class)
    public void testFromWithNegativeValue() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.from(-123, 5, 0, true);
    }

    @Test(expected = TypeConverterException.class)
    public void testFromWithValueTooLarge() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.from(123456, 5, 0, true);
    }

    @Test
    public void testFromWithNullValueAndNoDefaultValue() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] result = converter.from(null, 5, 0, true);
        assertNull(result);
    }

    @Test
    public void testFromWithNullValueAndDefaultValue() {
        IntegerToInteger converter = new IntegerToInteger();
        converter.setDefaultValue("42");
        byte[] result = converter.from(null, 5, 0, true);
        assertNotNull(result);
        assertEquals("42", new String(result));
    }

    @Test
    public void testGetIntegerStringWithValidData() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = "12345".getBytes();
        String result = converter.getIntegerString(bytes, 0, 5, false);
        assertEquals("12345", result);
    }

    @Test(expected = TypeConverterException.class)
    public void testGetIntegerStringWithLeadingDash() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = "-12345".getBytes();
        converter.getIntegerString(bytes, 0, 6, false);
    }

    @Test(expected = TypeConverterException.class)
    public void testGetIntegerStringWithTrailingDash() {
        IntegerToInteger converter = new IntegerToInteger();
        byte[] bytes = "12345-".getBytes();
        converter.getIntegerString(bytes, 0, 6, false);
    }
}
