package com.j256.simplemagic.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class BigEndianString16Type_ArrowTest {

	private BigEndianString16Type bigEndianString16Type;

	@Before
	public void setUp() {
		bigEndianString16Type = new BigEndianString16Type();
	}

	@Test
	public void testExtractValueFromBytes_NormalCase() {
		byte[] bytes = { 0x00, 0x48, 0x00, 0x65, 0x00, 0x6C, 0x00, 0x6C, 0x00, 0x6F, 0x00, 0x00 };
		Object result = bigEndianString16Type.extractValueFromBytes(0, bytes, true);
		char[] expected = { 'H', 'e', 'l', 'l', 'o' };
		assertArrayEquals(expected, (char[]) result);
	}

	@Test
	public void testExtractValueFromBytes_EmptyString() {
		byte[] bytes = { 0x00, 0x00 };
		Object result = bigEndianString16Type.extractValueFromBytes(0, bytes, true);
		char[] expected = {};
		assertArrayEquals(expected, (char[]) result);
	}

	@Test
	public void testExtractValueFromBytes_SingleCharacter() {
		byte[] bytes = { 0x00, 0x41, 0x00, 0x00 };
		Object result = bigEndianString16Type.extractValueFromBytes(0, bytes, true);
		char[] expected = { 'A' };
		assertArrayEquals(expected, (char[]) result);
	}

	@Test
	public void testExtractValueFromBytes_WithTrailingBytes() {
		byte[] bytes = { 0x00, 0x48, 0x00, 0x65, 0x00, 0x6C, 0x00, 0x6C, 0x00, 0x6F, 0x00, 0x00, 0x00, 0x42 };
		Object result = bigEndianString16Type.extractValueFromBytes(0, bytes, true);
		char[] expected = { 'H', 'e', 'l', 'l', 'o' };
		assertArrayEquals(expected, (char[]) result);
	}

	@Test
	public void testBytesToChar() {
		char result = bigEndianString16Type.bytesToChar(0x00, 0x41);
		assertEquals('A', result);
	}

	@Test
	public void testBytesToChar_WithHigherValue() {
		char result = bigEndianString16Type.bytesToChar(0x01, 0x00);
		assertEquals((char) 256, result);
	}

	@Test
	public void testIsMatch_NormalCase() {
		char[] chars = { 'H', 'e', 'l', 'l', 'o' };
		TestInfo testInfo = new TestInfo("Hello");
		MutableOffset mutableOffset = new MutableOffset(0);
		Object result = bigEndianString16Type.isMatch(testInfo, null, false, chars, mutableOffset, null);
		assertTrue((Boolean) result);
	}

	@Test
	public void testIsMatch_Mismatch() {
		char[] chars = { 'H', 'e', 'l', 'l', 'o' };
		TestInfo testInfo = new TestInfo("World");
		MutableOffset mutableOffset = new MutableOffset(0);
		Object result = bigEndianString16Type.isMatch(testInfo, null, false, chars, mutableOffset, null);
		assertTrue(!(Boolean) result);
	}

	// Helper class to simulate TestInfo
	private static class TestInfo {
		private final String value;

		public TestInfo(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	// Helper class to simulate MutableOffset
	private static class MutableOffset {
		private int offset;

		public MutableOffset(int offset) {
			this.offset = offset;
		}

		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}
	}
}
