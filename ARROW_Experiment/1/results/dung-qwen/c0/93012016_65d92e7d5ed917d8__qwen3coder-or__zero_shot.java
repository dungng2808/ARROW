package joptsimple.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import junit.framework.TestCase;

public class AbbreviationMap_ArrowTest extends TestCase {
    
    public void testContainsWhenKeyExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertTrue(map.contains("good"));
    }
    
    public void testContainsWhenAbbreviationExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertTrue(map.contains("goo"));
    }
    
    public void testContainsWhenNoMatch() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertFalse(map.contains("bad"));
    }
    
    public void testGetWhenKeyExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertEquals("bye", map.get("good"));
    }
    
    public void testGetWhenAbbreviationExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertEquals("bye", map.get("goo"));
    }
    
    public void testGetWhenNoMatch() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertNull(map.get("bad"));
    }
    
    public void testGetWhenAmbiguousAbbreviation() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        assertNull(map.get("g"));
    }
    
    public void testPutWithNullValue() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        try {
            map.put("key", null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
    
    public void testPutWithEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        try {
            map.put("", "value");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public void testPutAll() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.putAll(Arrays.asList("good", "go", "g"), "bye");
        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("go"));
        assertEquals("bye", map.get("g"));
    }
    
    public void testRemoveWhenKeyExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.remove("good");
        assertFalse(map.contains("good"));
        assertNull(map.get("good"));
    }
    
    public void testRemoveWhenKeyDoesNotExist() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        try {
            map.remove("bad");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public void testRemoveWithEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        try {
            map.remove("");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public void testRemoveMakesAbbreviationNonUnique() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        map.remove("go");
        assertEquals("bye", map.get("good"));
        assertNull(map.get("g"));
    }
    
    public void testToJavaUtilMap() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        Map<String, String> javaMap = map.toJavaUtilMap();
        assertEquals(3, javaMap.size());
        assertEquals("bye", javaMap.get("good"));
        assertEquals("fish", javaMap.get("go"));
        assertEquals("bye", javaMap.get("goo"));
    }
    
    public void testMultipleKeysWithSamePrefix() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("abc", "first");
        map.put("abd", "second");
        assertEquals("first", map.get("abc"));
        assertEquals("second", map.get("abd"));
        assertNull(map.get("ab"));
    }
    
    public void testComplexAbbreviationScenario() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("hello", "world");
        map.put("help", "me");
        map.put("helicopter", "rotor");
        
        assertEquals("world", map.get("hello"));
        assertEquals("me", map.get("help"));
        assertEquals("rotor", map.get("helicopter"));
        assertEquals("world", map.get("hel"));
        assertNull(map.get("he"));
    }
}
