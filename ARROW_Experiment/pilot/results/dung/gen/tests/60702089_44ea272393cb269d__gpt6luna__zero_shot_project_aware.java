package chapter1.finaltask;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubString_ArrowTest {

    @Test
    public void containsFindsSubstringAndStoresSourceCharacters() {
        SubString substring = new SubString();

        boolean result = substring.contains("asdsdfds", "ds");

        assertTrue(result);
        assertArrayEquals("asdsdfds".toCharArray(), substring.original);
    }

    @Test
    public void containsReturnsFalseWhenSubstringIsAbsent() {
        SubString substring = new SubString();

        boolean result = substring.contains("abcdef", "gh");

        assertFalse(result);
        assertArrayEquals("abcdef".toCharArray(), substring.original);
    }

    @Test
    public void containsReturnsTrueForEmptySubstring() {
        SubString substring = new SubString();

        boolean result = substring.contains("abc", "");

        assertTrue(result);
        assertArrayEquals("abc".toCharArray(), substring.original);
    }

    @Test
    public void containsReturnsFalseWhenSubstringIsLongerThanSource() {
        SubString substring = new SubString();

        boolean result = substring.contains("ab", "abc");

        assertFalse(result);
        assertArrayEquals("ab".toCharArray(), substring.original);
    }
}
