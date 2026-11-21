package joptsimple.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class AbbreviationMap_ArrowTest {

    @Test
    public void testContainsWhenKeyIsPresent() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertTrue(map.contains("good"));
    }

    @Test
    public void testContainsWhenKeyIsNotPresentButAbbreviationExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertTrue(map.contains("goo"));
    }

    @Test
    public void testContainsWhenKeyIsNotPresentAndNoAbbreviationExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertFalse(map.contains("bad"));
    }

    @Test
    public void testGetWhenKeyIsPresent() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertEquals("bye", map.get("good"));
    }

    @Test
    public void testGetWhenKeyIsAbbreviation() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertEquals("bye", map.get("goo"));
    }

    @Test
    public void testGetWhenKeyIsNotPresent() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertNull(map.get("bad"));
    }

    @Test
    public void testGetWhenKeyIsAmbiguousAbbreviation() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        assertNull(map.get("g"));
    }

    @Test
    public void testPutWithNewKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("goo"));
        assertEquals("bye", map.get("go"));
        assertEquals("bye", map.get("g"));
    }

    @Test
    public void testPutWithOverlappingKeys() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        assertEquals("bye", map.get("good"));
        assertEquals("fish", map.get("go"));
        assertEquals("bye", map.get("goo"));
        assertNull(map.get("g"));
    }

    @Test
    public void testPutAll() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.putAll(new String[]{"good", "go"}, "bye");
        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("go"));
        assertEquals("bye", map.get("goo"));
    }

    @Test
    public void testRemoveWhenKeyExists() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.remove("good");
        assertNull(map.get("good"));
        assertNull(map.get("goo"));
    }

    @Test
    public void testRemoveWhenKeyDoesNotExist() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.remove("bad");
        assertEquals("bye", map.get("good"));
    }

    @Test
    public void testRemoveWithOverlappingKeys() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        map.remove("go");
        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("goo"));
        assertNull(map.get("g"));
    }

    @Test
    public void testToJavaUtilMap() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("good", "bye");
        map.put("go", "fish");
        Map<String, String> javaMap = map.toJavaUtilMap();
        assertThat(javaMap.size(), is(2));
        assertThat(javaMap.get("good"), is("bye"));
        assertThat(javaMap.get("go"), is("fish"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetWithNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void testContainsWithNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.contains(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPutWithNullValue() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("key", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.put("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<>();
        map.remove("");
    }
}
