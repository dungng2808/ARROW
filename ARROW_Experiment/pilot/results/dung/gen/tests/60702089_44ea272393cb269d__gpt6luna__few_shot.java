package chapter1.finaltask;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubString_ArrowTest {

    @Test
    public void containsSubstring() {
        SubString subString = new SubString();
        assertTrue(subString.contains("asdsdfds", "ds"));
    }

    @Test
    public void returnsFalseWhenSubstringIsAbsent() {
        SubString subString = new SubString();
        assertFalse(subString.contains("abcdef", "xyz"));
    }

    @Test
    public void returnsTrueForEmptySubstring() {
        SubString subString = new SubString();
        assertTrue(subString.contains("abc", ""));
    }

    @Test
    public void returnsFalseWhenSubstringIsLongerThanSource() {
        SubString subString = new SubString();
        assertFalse(subString.contains("ab", "abc"));
    }

    @Test
    public void recordsOriginalSourceCharacters() {
        SubString subString = new SubString();
        subString.contains("hello", "ell");
        org.junit.Assert.assertArrayEquals(new char[] {'h', 'e', 'l', 'l', 'o'}, subString.original);
    }
}
