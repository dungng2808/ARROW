package chapter1.finaltask;

import org.junit.Test;
import static org.junit.Assert.*;

public class SubString_ArrowTest {

    @Test
    public void testContains_WhenSubstringExistsAtBeginning() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "hello");
        assertTrue(result);
    }

    @Test
    public void testContains_WhenSubstringExistsAtEnd() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "world");
        assertTrue(result);
    }

    @Test
    public void testContains_WhenSubstringExistsInMiddle() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "llo wo");
        assertTrue(result);
    }

    @Test
    public void testContains_WhenSubstringDoesNotExist() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "xyz");
        assertFalse(result);
    }

    @Test
    public void testContains_WhenSubstringIsEmpty() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "");
        assertTrue(result);
    }

    @Test
    public void testContains_WhenOriginalStringIsEmpty() {
        SubString subString = new SubString();
        boolean result = subString.contains("", "hello");
        assertFalse(result);
    }

    @Test
    public void testContains_WhenBothStringsAreEmpty() {
        SubString subString = new SubString();
        boolean result = subString.contains("", "");
        assertTrue(result);
    }

    @Test
    public void testContains_WhenSubstringEqualsOriginalString() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello", "hello");
        assertTrue(result);
    }

    @Test
    public void testContains_WhenSubstringIsLongerThanOriginal() {
        SubString subString = new SubString();
        boolean result = subString.contains("hi", "hello");
        assertFalse(result);
    }

    @Test
    public void testContains_WhenCharactersMatchPartiallyButNotSequentially() {
        SubString subString = new SubString();
        boolean result = subString.contains("abcde", "ace");
        assertFalse(result);
    }

    @Test
    public void testContains_WhenSubstringHasRepeatedCharacters() {
        SubString subString = new SubString();
        boolean result = subString.contains("aabbcc", "abb");
        assertTrue(result);
    }

    @Test
    public void testContains_WhenOriginalStringHasRepeatedCharacters() {
        SubString subString = new SubString();
        boolean result = subString.contains("aaaaaa", "aaa");
        assertTrue(result);
    }
}
