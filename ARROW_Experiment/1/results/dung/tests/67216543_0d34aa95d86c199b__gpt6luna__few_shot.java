package com.j256.simplemagic.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BigEndianString16Type_ArrowTest {

    @Test
    public void extractsBigEndianCharacters() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] {0, 'A', 0, 'B', 0, 0};

        char[] result = (char[]) type.extractValueFromBytes(0, bytes, false);

        assertArrayEquals(new char[] {'A', 'B'}, result);
    }

    @Test
    public void extractsCharactersFromOffset() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] {9, 9, 0, 'C', 0, 0};

        char[] result = (char[]) type.extractValueFromBytes(2, bytes, true);

        assertArrayEquals(new char[] {'C'}, result);
    }

    @Test
    public void extractsCharactersWhenNoTerminatorIsPresent() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] {0, 'X', 0, 'Y'};

        char[] result = (char[]) type.extractValueFromBytes(0, bytes, false);

        assertArrayEquals(new char[] {'X'}, result);
    }

    @Test
    public void extractsEmptyValueWhenTerminatorIsAtStart() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = new byte[] {0, 0, 0, 'Z'};

        char[] result = (char[]) type.extractValueFromBytes(0, bytes, false);

        assertEquals(0, result.length);
    }
}
