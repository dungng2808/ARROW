package org.trie4j.util;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;

public class CharsCharSequence_ArrowTest {

    @Test
    public void testConstructorWithAllChars() {
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
    public void testConstructorWithInvalidEnd() {
        char[] chars = {'a', 'b', 'c'};
        new CharsCharSequence(chars, 0, 5);
    }

    @Test
    public void testLength() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 4);
        assertEquals(3, sequence.length());
    }

    @Test
    public void testCharAt() {
        char[] chars = {'a', 'b', 'c', 'd'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 3);
        assertEquals('b', sequence.charAt(0));
        assertEquals('c', sequence.charAt(1));
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
        CharSequence sub = sequence.subSequence(0, 2);
        assertEquals(2, sub.length());
        assertEquals('b', sub.charAt(0));
        assertEquals('c', sub.charAt(1));
    }

    @Test
    public void testToCharArray() {
        char[] chars = {'a', 'b', 'c', 'd'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 3);
        char[] result = sequence.toCharArray();
        char[] expected = {'b', 'c'};
        assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void testSubChars() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 1, 4);
        char[] result = sequence.subChars(0, 2);
        char[] expected = {'b', 'c'};
        assertTrue(Arrays.equals(expected, result));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testSubCharsWithInvalidEnd() {
        char[] chars = {'a', 'b', 'c'};
        CharsCharSequence sequence = new CharsCharSequence(chars, 0, 2);
        sequence.subChars(0, 5);
    }
}
