package com.j256.simplemagic.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BigEndianString16Type_ArrowTest {

    @Test
    public void extractsBigEndianCharactersUntilTerminator() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] { 0, 'A', 0, 'B', 0, 0, 0, 'C' };

        char[] result = (char[]) type.extractValueFromBytes(0, bytes, false);

        assertArrayEquals(new char[] { 'A', 'B' }, result);
    }

    @Test
    public void extractsCharactersWhenNoTerminatorIsPresent() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] { 0, 'A', 0, 'B' };

        char[] result = (char[]) type.extractValueFromBytes(0, bytes, false);

        assertArrayEquals(new char[] { 'A', 'B' }, result);
    }

    @Test
    public void oddTrailingByteIsIgnored() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] { 0, 'A', 0, 'B', 0 };

        char[] result = (char[]) type.extractValueFromBytes(0, bytes, false);

        assertArrayEquals(new char[] { 'A', 'B' }, result);
    }

    @Test
    public void extractsFromNonzeroOffsetUsingAbsoluteBytePositions() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] { 9, 9, 0, 'A', 0, 0 };

        char[] result = (char[]) type.extractValueFromBytes(2, bytes, false);

        assertArrayEquals(new char[] { 'A' }, result);
    }

    @Test
    public void emptyInputProducesEmptyCharacters() {
        BigEndianString16Type type = new BigEndianString16Type();

        char[] result = (char[]) type.extractValueFromBytes(0, new byte[0], false);

        assertArrayEquals(new char[0], result);
    }

    @Test
    public void convertsBytesWithHighBitSet() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] { (byte) 0x80, 0x01, 0, 0 };

        char[] result = (char[]) type.extractValueFromBytes(0, bytes, false);

        assertArrayEquals(new char[] { '\u8001' }, result);
    }
}
