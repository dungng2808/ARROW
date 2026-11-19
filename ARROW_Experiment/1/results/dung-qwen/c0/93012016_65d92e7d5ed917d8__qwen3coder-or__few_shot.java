package joptsimple.internal;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class AbbreviationMap_ArrowTest {

    @Test
    public void testContainsWhenKeyExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertTrue(map.contains("good"));
    }

    @Test
    public void testContainsWhenAbbreviationExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertTrue(map.contains("goo"));
    }

    @Test
    public void testContainsWhenNoMatch() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertFalse(map.contains("bad"));
    }

    @Test
    public void testGetWhenKeyExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertEquals("bye", map.get("good"));
    }

    @Test
    public void testGetWhenAbbreviationExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertEquals("bye", map.get("goo"));
    }

    @Test
    public void testGetWhenNoMatch() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertNull(map.get("bad"));
    }

    @Test
    public void testGetWhenAmbiguousAbbreviation() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        assertNull(map.get("g"));
    }

    @Test
    public void testPutReplacesExistingValue() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("good", "hello");
        assertEquals("hello", map.get("good"));
    }

    @Test
    public void testPutAll() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.putAll(java.util.Arrays.asList("good", "go"), "bye");
        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("go"));
    }

    @Test
    public void testRemoveWhenKeyExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.remove("good");
        assertFalse(map.contains("good"));
    }

    @Test
    public void testRemoveWhenAbbreviationExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.remove("goo");
        assertFalse(map.contains("goo"));
    }

    @Test
    public void testRemoveMakesAbbreviationAmbiguous() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        map.remove("go");
        assertEquals("bye", map.get("good"));
        assertNull(map.get("g"));
    }

    @Test
    public void testToJavaUtilMap() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        Map<String, String> javaMap = map.toJavaUtilMap();
        assertEquals(2, javaMap.size());
        assertTrue(javaMap.containsKey("good"));
        assertTrue(javaMap.containsKey("go"));
        assertEquals("bye", javaMap.get("good"));
        assertEquals("fish", javaMap.get("go"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullValue() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("key", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutEmptyString() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEmptyString() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.remove("");
    }

    @Test(expected = NullPointerException.class)
    public void testPutAllNullKeys() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.putAll(null, "value");
    }

    @Test(expected = NullPointerException.class)
    public void testPutAllNullValue() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.putAll(java.util.Arrays.asList("key"), null);
    }
}
