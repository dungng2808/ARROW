package chapter1.finaltask;

import org.junit.Test;
import static org.junit.Assert.*;

public class SubString_ArrowTest {

    @Test
    public void testContainsTrue() {
        SubString subString = new SubString();
        boolean result = subString.contains("asdsdfds", "ds");
        assertTrue(result);
    }

    @Test
    public void testContainsFalse() {
        SubString subString = new SubString();
        boolean result = subString.contains("asdsdfds", "xx");
        assertFalse(result);
    }

    @Test
    public void testContainsEmptySubstring() {
        SubString subString = new SubString();
        boolean result = subString.contains("asdsdfds", "");
        assertTrue(result);
    }

    @Test
    public void testContainsEmptyString() {
        SubString subString = new SubString();
        boolean result = subString.contains("", "ds");
        assertFalse(result);
    }

    @Test
    public void testContainsSameStrings() {
        SubString subString = new SubString();
        boolean result = subString.contains("test", "test");
        assertTrue(result);
    }

    @Test
    public void testContainsSubstringAtBeginning() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "hello");
        assertTrue(result);
    }

    @Test
    public void testContainsSubstringAtEnd() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "world");
        assertTrue(result);
    }

    @Test
    public void testContainsSubstringInMiddle() {
        SubString subString = new SubString();
        boolean result = subString.contains("hello world", "o w");
        assertTrue(result);
    }

    @Test
    public void testContainsNoMatchWithSameLength() {
        SubString subString = new SubString();
        boolean result = subString.contains("abc", "xyz");
        assertFalse(result);
    }

    @Test
    public void testContainsSingleCharacterMatch() {
        SubString subString = new SubString();
        boolean result = subString.contains("abcdef", "c");
        assertTrue(result);
    }

    @Test
    public void testContainsSingleCharacterNoMatch() {
        SubString subString = new SubString();
        boolean result = subString.contains("abcdef", "z");
        assertFalse(result);
    }

    @Test
    public void testContainsOverlappingPattern() {
        SubString subString = new SubString();
        boolean result = subString.contains("aaaa", "aa");
        assertTrue(result);
    }
}
