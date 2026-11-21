package org.trie4j.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CharsCharSequence_ArrowTest {
	@Test
	public void constructorAndAccessorsExposeWholeArray() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c'});

		assertEquals(3, sequence.length());
		assertEquals('a', sequence.charAt(0));
		assertEquals('b', sequence.charAt(1));
		assertEquals('c', sequence.charAt(2));
		assertArrayEquals(new char[] {'a', 'b', 'c'}, sequence.toCharArray());
	}

	@Test
	public void constructorWithRangeUsesSpecifiedBounds() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'x', 'a', 'b', 'y'}, 1, 3);

		assertEquals(2, sequence.length());
		assertEquals('a', sequence.charAt(0));
		assertEquals('b', sequence.charAt(1));
		assertArrayEquals(new char[] {'a', 'b'}, sequence.toCharArray());
	}

	@Test
	public void subSequenceUsesBoundsRelativeToCurrentSequence() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'x', 'a', 'b', 'c', 'y'}, 1, 4);
		CharSequence subSequence = sequence.subSequence(1, 3);

		assertEquals(2, subSequence.length());
		assertEquals('b', subSequence.charAt(0));
		assertEquals('c', subSequence.charAt(1));
	}

	@Test
	public void subCharsReturnsRequestedRange() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'});

		assertArrayEquals(new char[] {'b', 'c'}, sequence.subChars(1, 3));
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void constructorRejectsEndBeyondArrayLength() {
		new CharsCharSequence(new char[] {'a', 'b'}, 0, 3);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void charAtRejectsIndexAtLength() {
		new CharsCharSequence(new char[] {'a', 'b'}).charAt(2);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void subCharsRejectsEndAtArrayLength() {
		new CharsCharSequence(new char[] {'a', 'b', 'c'}).subChars(0, 3);
	}
}
