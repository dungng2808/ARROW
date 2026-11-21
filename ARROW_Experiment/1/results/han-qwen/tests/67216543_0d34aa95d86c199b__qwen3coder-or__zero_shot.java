package com.j256.simplemagic.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BigEndianString16Type_ArrowTest {

    @Test
    public void testExtractValueFromBytes_NormalCase() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = {(byte) 0x00, (byte) 0x48, (byte) 0x00, (byte) 0x65, (byte) 0x00, (byte) 0x6C, (byte) 0x00,
                (byte) 0x6C, (byte) 0x00, (byte) 0x6F, (byte) 0x00, (byte) 0x00};
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
    public void testExtractValueFromBytes_Offset() {
        BigEndianString16Type type = new BigEndianString16Type();
        byte[] bytes = {(byte) 0x00, (byte) 0x41, (byte) 0x00, (byte) 0x42, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Object result = type.extractValueFromBytes(2, bytes, true);
        char[] expected = {'B'};
        assertArrayEquals(expected, (char[]) result);
    }

    @Test
    public void testBytesToChar() {
        BigEndianString16Type type = new BigEndianString16Type();
        char result = type.bytesToChar(0x00, 0x41);
        assertEquals('A', result);
    }

    @Test
    public void testIsMatch_NormalCase() {
        BigEndianString16Type type = new BigEndianString16Type();
        char[] chars = {'H', 'e', 'l', 'l', 'o'};
        TestInfo testInfo = new TestInfo("Hello");
        MutableOffset mutableOffset = new MutableOffset(0);
        byte[] bytes = {(byte) 0x00, (byte) 0x48, (byte) 0x00, (byte) 0x65, (byte) 0x00, (byte) 0x6C, (byte) 0x00,
                (byte) 0x6C, (byte) 0x00, (byte) 0x6F, (byte) 0x00, (byte) 0x00};

        Object result = type.isMatch(testInfo, null, false, chars, mutableOffset, bytes);
        assertTrue((Boolean) result);
    }

    @Test
    public void testIsMatch_Mismatch() {
        BigEndianString16Type type = new BigEndianString16Type();
        char[] chars = {'H', 'e', 'l', 'l', 'o'};
        TestInfo testInfo = new TestInfo("World");
        MutableOffset mutableOffset = new MutableOffset(0);
        byte[] bytes = {(byte) 0x00, (byte) 0x57, (byte) 0x00, (byte) 0x6F, (byte) 0x00, (byte) 0x72, (byte) 0x00,
                (byte) 0x6C, (byte) 0x00, (byte) 0x64, (byte) 0x00, (byte) 0x00};

        Object result = type.isMatch(testInfo, null, false, chars, mutableOffset, bytes);
        assertNull(result);
    }

    // Helper class for testing
    private static class TestInfo {
        private final String value;

        public TestInfo(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Helper class for testing
    private static class MutableOffset {
        private int offset;

        public MutableOffset(int offset) {
            this.offset = offset;
        }
    }
}
