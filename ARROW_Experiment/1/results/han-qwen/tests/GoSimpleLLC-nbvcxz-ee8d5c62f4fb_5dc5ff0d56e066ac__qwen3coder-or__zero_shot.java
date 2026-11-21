package me.gosimple.nbvcxz.matching;

import me.gosimple.nbvcxz.matching.match.DictionaryMatch;
import me.gosimple.nbvcxz.matching.match.Match;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.Dictionary;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class DictionaryMatcher_ArrowTest {

    @Test
    public void testTranslateLeet_EmptyPassword() {
        Configuration config = new Configuration.Builder().build();
        List<String> result = DictionaryMatcher.translateLeet(config, "");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testTranslateLeet_NoLeetChars() {
        Configuration config = new Configuration.Builder().build();
        List<String> result = DictionaryMatcher.translateLeet(config, "hello");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetLeetSub_SameStrings() {
        List<Character[]> result = DictionaryMatcher.getLeetSub("hello", "hello");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetLeetSub_DifferentStrings() {
        List<Character[]> result = DictionaryMatcher.getLeetSub("h3ll0", "hello");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertArrayEquals(new Character[]{'3', 'e'}, result.get(0));
        assertArrayEquals(new Character[]{'0', 'o'}, result.get(1));
    }

    @Test
    public void testDistance_NullStrings() {
        try {
            DictionaryMatcher.distance(null, "test", 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Strings must not be null", e.getMessage());
        }

        try {
            DictionaryMatcher.distance("test", null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Strings must not be null", e.getMessage());
        }
    }

    @Test
    public void testDistance_NegativeThreshold() {
        try {
            DictionaryMatcher.distance("test", "test", -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Threshold must not be negative", e.getMessage());
        }
    }

    @Test
    public void testDistance_ExactMatch() {
        int result = DictionaryMatcher.distance("test", "test", 1);
        assertEquals(0, result);
    }

    @Test
    public void testDistance_OneEdit() {
        int result = DictionaryMatcher.distance("test", "best", 1);
        assertEquals(1, result);
    }

    @Test
    public void testDistance_EditDistanceTooLarge() {
        int result = DictionaryMatcher.distance("test", "xyz", 1);
        assertEquals(-1, result);
    }

    @Test
    public void testMatch_EmptyPassword() {
        Configuration config = new Configuration.Builder().build();
        DictionaryMatcher matcher = new DictionaryMatcher();
        List<Match> result = matcher.match(config, "");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testMatch_SingleCharacter() {
        Configuration config = new Configuration.Builder().build();
        DictionaryMatcher matcher = new DictionaryMatcher();
        List<Match> result = matcher.match(config, "a");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testReplaceAtIndex_RecursiveBuild() {
        TreeMap<Integer, Character[]> replacements = new TreeMap<>();
        replacements.put(0, new Character[]{'a', 'b'});
        replacements.put(1, new Character[]{'c', 'd'});

        List<String> results = new ArrayList<>();
        char[] password = {'x', 'y'};
        DictionaryMatcher.replaceAtIndex(replacements, 0, password, results);

        assertNotNull(results);
        assertEquals(4, results.size());
    }
}
