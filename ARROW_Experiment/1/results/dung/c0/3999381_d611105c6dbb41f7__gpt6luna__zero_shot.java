package org.trie4j.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CharsCharSequence_ArrowTest {
	@Test
	public void constructorWithArrayUsesEntireArray() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c'});

		assertEquals(3, sequence.length());
		assertEquals('a', sequence.charAt(0));
		assertEquals('c', sequence.charAt(2));
	}

	@Test
	public void constructorWithRangeUsesSpecifiedBounds() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'}, 1, 3);

		assertEquals(2, sequence.length());
		assertEquals('b', sequence.charAt(0));
		assertEquals('c', sequence.charAt(1));
	}

	@Test
	public void subSequenceUsesBoundsRelativeToCurrentSequence() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'}, 1, 4);

		CharSequence subSequence = sequence.subSequence(1, 3);

		assertEquals(2, subSequence.length());
		assertEquals('c', subSequence.charAt(0));
		assertEquals('d', subSequence.charAt(1));
	}

	@Test
	public void toCharArrayCopiesTheSequenceRange() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'}, 1, 3);

		assertArrayEquals(new char[] {'b', 'c'}, sequence.toCharArray());
	}

	@Test
	public void subCharsCopiesRequestedRange() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'}, 1, 4);

		assertArrayEquals(new char[] {'c', 'd'}, sequence.subChars(1, 3));
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void constructorRejectsEndBeyondArrayLength() {
		new CharsCharSequence(new char[] {'a', 'b'}, 0, 3);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void charAtRejectsIndexAtSequenceEnd() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b'}, 0, 2);

		sequence.charAt(2);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void subCharsRejectsEndAtOrBeyondSequenceEnd() {
		CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c'}, 0, 3);

		sequence.subChars(0, 3);
	}
}
