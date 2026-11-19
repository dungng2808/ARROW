package me.gosimple.nbvcxz.matching;

import me.gosimple.nbvcxz.matching.match.DictionaryMatch;
import me.gosimple.nbvcxz.matching.match.Match;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.Dictionary;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

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
    public void testTranslateLeet_WithLeetChars() {
        Configuration config = new Configuration.Builder()
                .withLeetTable(Collections.singletonMap('a', new Character[]{'@', '4'}))
                .build();
        List<String> result = DictionaryMatcher.translateLeet(config, "h3ll0");
        assertNotNull(result);
        assertEquals(0, result.size()); // No 'a' chars to replace
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
        assertEquals(Character.valueOf('3'), result.get(0)[0]);
        assertEquals(Character.valueOf('e'), result.get(0)[1]);
        assertEquals(Character.valueOf('0'), result.get(1)[0]);
        assertEquals(Character.valueOf('o'), result.get(1)[1]);
    }

    @Test
    public void testDistance_NullInputs() {
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
    public void testDistance_ThresholdExceeded() {
        int result = DictionaryMatcher.distance("test", "testing", 2);
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
    public void testMatch_NoMatches() {
        Configuration config = new Configuration.Builder().build();
        DictionaryMatcher matcher = new DictionaryMatcher();
        List<Match> result = matcher.match(config, "xyz");
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
