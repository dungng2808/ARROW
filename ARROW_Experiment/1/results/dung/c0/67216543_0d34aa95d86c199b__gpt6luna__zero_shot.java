package com.j256.simplemagic.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BigEndianString16Type_ArrowTest {

    @Test
    public void extractBigEndianCharacters() {
        BigEndianString16Type type = new BigEndianString16Type();
        char[] result = (char[]) type.extractValueFromBytes(0,
                new byte[] { 0, 'A', 0, 'B', 0, 0 }, false);

        assertArrayEquals(new char[] { 'A', 'B' }, result);
    }

    @Test
    public void extractFromNonzeroOffset() {
        BigEndianString16Type type = new BigEndianString16Type();
        char[] result = (char[]) type.extractValueFromBytes(2,
                new byte[] { 9, 9, 0, 'X', 0, 0 }, true);

        assertArrayEquals(new char[] { 'X' }, result);
    }

    @Test
    public void extractWithoutTerminator() {
        BigEndianString16Type type = new BigEndianString16Type();
        char[] result = (char[]) type.extractValueFromBytes(0, new byte[] { 0, 'A', 0, 'B' }, false);

        assertArrayEquals(new char[] { 'A' }, result);
    }

    @Test
    public void extractEmptyString() {
        BigEndianString16Type type = new BigEndianString16Type();
        char[] result = (char[]) type.extractValueFromBytes(0, new byte[] { 0, 0 }, false);

        assertEquals(0, result.length);
    }

    @Test
    public void extractValueIsNotNull() {
        BigEndianString16Type type = new BigEndianString16Type();
        Object result = type.extractValueFromBytes(0, new byte[] { 0, 'A', 0, 0 }, false);

        assertNotNull(result);
    }

    @Test
    public void bytesToCharUsesBigEndianOrder() {
        BigEndianString16Type type = new BigEndianString16Type();

        assertEquals('A', type.bytesToChar(0, 'A'));
    }
}
