package chapter1.finaltask;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubString_ArrowTest {

    @Test
    public void containsFindsSubstring() {
        SubString substring = new SubString();
        assertTrue(substring.contains("asdsdfds", "ds"));
    }

    @Test
    public void containsReturnsFalseWhenSubstringIsAbsent() {
        SubString substring = new SubString();
        assertFalse(substring.contains("abcdef", "gh"));
    }

    @Test
    public void containsReturnsTrueForEmptySubstring() {
        SubString substring = new SubString();
        assertTrue(substring.contains("abc", ""));
    }

    @Test
    public void containsReturnsFalseWhenSourceIsEmptyAndSubstringIsNot() {
        SubString substring = new SubString();
        assertFalse(substring.contains("", "a"));
    }
}
