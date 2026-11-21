package me.gosimple.nbvcxz.matching;

import me.gosimple.nbvcxz.matching.match.DictionaryMatch;
import me.gosimple.nbvcxz.matching.match.Match;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.Dictionary;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DictionaryMatcher_ArrowTest {

    @Test
    public void testTranslateLeet_NoReplacements() {
        Configuration config = new Configuration();
        String password = "hello";
        List<String> result = DictionaryMatcher.translateLeet(config, password);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTranslateLeet_WithReplacements() {
        Configuration config = new Configuration();
        Map<Character, Character[]> leetTable = new HashMap<>();
        leetTable.put('a', new Character[]{'@', '4'});
        leetTable.put('e', new Character[]{'3'});
        config.setLeetTable(leetTable);
        
        String password = "h3ll0";
        List<String> result = DictionaryMatcher.translateLeet(config, password);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetLeetSub() {
        String password = "h3ll0";
        String unleetPassword = "hello";
        List<Character[]> result = DictionaryMatcher.getLeetSub(password, unleetPassword);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals('@', result.get(0)[0].charValue());
        assertEquals('a', result.get(0)[1].charValue());
        assertEquals('3', result.get(1)[0].charValue());
        assertEquals('e', result.get(1)[1].charValue());
    }

    @Test
    public void testDistance_NullInputs() {
        try {
            DictionaryMatcher.distance(null, "test", 1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Strings must not be null", e.getMessage());
        }

        try {
            DictionaryMatcher.distance("test", null, 1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Strings must not be null", e.getMessage());
        }

        try {
            DictionaryMatcher.distance("test", "test", -1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Threshold must not be negative", e.getMessage());
        }
    }

    @Test
    public void testDistance_SameStrings() {
        int result = DictionaryMatcher.distance("test", "test", 1);
        assertEquals(0, result);
    }

    @Test
    public void testDistance_DistanceWithinThreshold() {
        int result = DictionaryMatcher.distance("test", "best", 1);
        assertEquals(1, result);
    }

    @Test
    public void testDistance_DistanceBeyondThreshold() {
        int result = DictionaryMatcher.distance("test", "xyz", 1);
        assertEquals(-1, result);
    }

    @Test
    public void testMatch_EmptyPassword() {
        Configuration config = new Configuration();
        DictionaryMatcher matcher = new DictionaryMatcher();
        List<Match> result = matcher.match(config, "");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testMatch_SingleCharacter() {
        Configuration config = new Configuration();
        DictionaryMatcher matcher = new DictionaryMatcher();
        List<Match> result = matcher.match(config, "a");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testMatch_WithDictionaryMatch() {
        Configuration config = new Configuration();
        Dictionary dict = new Dictionary("test");
        Map<String, Integer> dictionaryMap = new HashMap<>();
        dictionaryMap.put("hello", 10);
        dict.setDictionary(dictionaryMap);
        List<Dictionary> dictionaries = new ArrayList<>();
        dictionaries.add(dict);
        config.setDictionaries(dictionaries);
        
        DictionaryMatcher matcher = new DictionaryMatcher();
        List<Match> result = matcher.match(config, "hello world");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0) instanceof DictionaryMatch);
    }
}
