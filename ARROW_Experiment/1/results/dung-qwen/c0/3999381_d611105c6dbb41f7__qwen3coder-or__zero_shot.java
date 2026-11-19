package org.trie4j.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharsCharSequence_ArrowTest {

    @Test
    public void testConstructorWithFullArray() {
        char[] chars = {'a', 'b', 'c', 'd'};
        CharsCharSequence sequence = new CharsCharSequence(chars);
        assertEquals(4, sequence.length());
        assertEquals('a', sequence.charAt(0));
        assertEquals('b', sequence.charAt(1));
        assertEquals('c', sequence.charAt(2));
        assertEquals('d', sequence.charAt(3));
    }

    @Test
    public void testConstructorWithRange() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 4);
        assertEquals(3, sequence.length());
        assertEquals('b', sequence.charAt(0));
        assertEquals('c', sequence.charAt(1));
        assertEquals('d', sequence.charAt(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testConstructorWithInvalidEndIndex() {
        char[] chars = {'a', 'b', 'c'};
        new CharsCharSequence(chars, 0, 5);
    }

    @Test
    public void testLength() {
        char[] chars = {'x', 'y', 'z'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 3);
        assertEquals(2, sequence.length());
    }

    @Test
    public void testCharAt() {
        char[] chars = {'p', 'q', 'r', 's'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 3);
        assertEquals('q', sequence.charAt(0));
        assertEquals('r', sequence.charAt(1));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testCharAtWithInvalidIndex() {
        char[] chars = {'a', 'b', 'c'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 0, 2);
        sequence.charAt(5);
    }

    @Test
    public void testSubSequence() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 4);
        CharSequence sub = sequence.subSequence(1, 3);
        assertEquals(2, sub.length());
        assertEquals('c', sub.charAt(0));
        assertEquals('d', sub.charAt(1));
    }

    @Test
    public void testToCharArray() {
        char[] chars = {'h', 'e', 'l', 'l', 'o'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 4);
        char[] result = sequence.toCharArray();
        assertEquals(3, result.length);
        assertEquals('e', result[0]);
        assertEquals('l', result[1]);
        assertEquals('l', result[2]);
    }

    @Test
    public void testSubChars() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 4);
        char[] result = sequence.subChars(1, 3);
        assertEquals(2, result.length);
        assertEquals('c', result[0]);
        assertEquals('d', result[1]);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testSubCharsWithInvalidEndIndex() {
        char[] chars = {'a', 'b', 'c'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 0, 2);
        sequence.subChars(0, 5);
    }
}
