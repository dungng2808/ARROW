package org.trie4j.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CharsCharSequence_ArrowTest {

    @Test
    public void lengthAndCharAtForWholeArray() {
        CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c'});

        assertEquals(3, sequence.length());
        assertEquals('b', sequence.charAt(1));
    }

    @Test
    public void lengthAndCharAtForRange() {
        CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'}, 1, 3);

        assertEquals(2, sequence.length());
        assertEquals('b', sequence.charAt(0));
        assertEquals('c', sequence.charAt(1));
    }

    @Test
    public void subSequenceReturnsRequestedRange() {
        CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'});

        CharSequence result = sequence.subSequence(1, 3);

        assertEquals(2, result.length());
        assertEquals('b', result.charAt(0));
        assertEquals('c', result.charAt(1));
    }

    @Test
    public void toCharArrayReturnsContents() {
        CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'}, 1, 3);

        assertArrayEquals(new char[] {'b', 'c'}, sequence.toCharArray());
    }

    @Test
    public void subCharsReturnsRequestedRange() {
        CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b', 'c', 'd'}, 0, 4);

        assertArrayEquals(new char[] {'b', 'c'}, sequence.subChars(1, 3));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void charAtRejectsIndexAtEnd() {
        CharsCharSequence sequence = new CharsCharSequence(new char[] {'a', 'b'});

        sequence.charAt(2);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void constructorRejectsEndBeyondArray() {
        new CharsCharSequence(new char[] {'a', 'b'}, 0, 3);
    }
}
