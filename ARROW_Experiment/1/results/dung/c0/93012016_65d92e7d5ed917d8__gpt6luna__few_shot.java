package joptsimple.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbbreviationMap_ArrowTest {

    @Test
    public void putCreatesUniqueAbbreviations() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");

        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("goo"));
        assertEquals("bye", map.get("go"));
        assertEquals("bye", map.get("g"));
        assertTrue(map.contains("g"));
    }

    @Test
    public void addingAKeyMakesSharedAbbreviationAmbiguous() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");
        map.put("go", "fish");

        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("goo"));
        assertEquals("fish", map.get("go"));
        assertNull(map.get("g"));
        assertFalse(map.contains("g"));
    }

    @Test
    public void replacingValueUpdatesKeyAndAbbreviations() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");
        map.put("good", "hello");

        assertEquals("hello", map.get("good"));
        assertEquals("hello", map.get("g"));
        assertEquals(1, map.toJavaUtilMap().size());
    }

    @Test
    public void removeRestoresUniqueAbbreviation() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");
        map.put("go", "fish");

        map.remove("go");

        assertEquals("bye", map.get("g"));
        assertEquals("bye", map.get("good"));
        assertNull(map.get("go"));
        assertEquals(1, map.toJavaUtilMap().size());
    }

    @Test
    public void removeMissingKeyLeavesMappingsUnchanged() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");

        map.remove("gone");

        assertEquals("bye", map.get("g"));
        assertEquals("bye", map.get("good"));
        assertEquals(1, map.toJavaUtilMap().size());
    }

    @Test
    public void putAllAssociatesEveryKeyWithTheValue() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.putAll(Arrays.asList("alpha", "beta"), "value");

        assertEquals("value", map.get("alpha"));
        assertEquals("value", map.get("a"));
        assertEquals("value", map.get("beta"));
        assertEquals("value", map.get("b"));
        assertNull(map.get(""));
        assertEquals(2, map.toJavaUtilMap().size());
    }

    @Test
    public void toJavaUtilMapContainsOnlyFullKeys() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("cat", "one");
        map.put("car", "two");

        Map<String, String> expected = new TreeMap<String, String>();
        expected.put("cat", "one");
        expected.put("car", "two");

        assertEquals(expected, map.toJavaUtilMap());
    }

    @Test
    public void emptyMapHasNoMappings() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        assertNull(map.get("anything"));
        assertFalse(map.contains("anything"));
        assertTrue(map.toJavaUtilMap().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void putRejectsEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("", "value");
    }

    @Test(expected = NullPointerException.class)
    public void putRejectsNullValue() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("key", null);
    }

    @Test(expected = NullPointerException.class)
    public void getRejectsNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.remove("");
    }
}
