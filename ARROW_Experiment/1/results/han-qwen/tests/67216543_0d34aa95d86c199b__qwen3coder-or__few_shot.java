package com.j256.simplemagic.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BigEndianString16Type_ArrowTest {

    @Test
    public void testExtractValueFromBytes_NormalCase() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = {(byte) 0x00, (byte) 0x48, (byte) 0x00, (byte) 0x65, (byte) 0x00, (byte) 0x6C, (byte) 0x00, (byte) 0x6C, (byte) 0x00, (byte) 0x6F, (byte) 0x00, (byte) 0x00};
        Object result = type.extractValueFromBytes(0, bytes, true);
        char[] expected = {'H', 'e', 'l', 'l', 'o'};
        assertArrayEquals(expected, (char[]) result);
    }

    @Test
    public void testExtractValueFromBytes_EmptyString() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = {(byte) 0x00, (byte) 0x00};
        Object result = type.extractValueFromBytes(0, bytes, true);
        char[] expected = {};
        assertArrayEquals(expected, (char[]) result);
    }

    @Test
    public void testExtractValueFromBytes_SingleCharacter() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = {(byte) 0x00, (byte) 0x41, (byte) 0x00, (byte) 0x00};
        Object result = type.extractValueFromBytes(0, bytes, true);
        char[] expected = {'A'};
        assertArrayEquals(expected, (char[]) result);
    }

    @Test
    public void testBytesToChar() {
        BigEndianString16Type type = new BigEndianString16Type();
        char result = type.bytesToChar(0x00, 0x41);
        assertEquals('A', result);
    }

    @Test
    public void testBytesToChar_Unicode() {
        BigEndianString16Type type = new BigEndianString16Type();
        char result = type.bytesToChar(0x00, 0x80);
        assertEquals(0x80, result);
    }
}
