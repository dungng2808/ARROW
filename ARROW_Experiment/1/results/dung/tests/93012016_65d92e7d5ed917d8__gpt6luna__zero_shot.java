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
        assertTrue(map.contains("goo"));
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
    public void putReplacesExistingValue() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("alpha", "first");
        map.put("alpha", "second");

        assertEquals("second", map.get("alpha"));
        assertEquals("second", map.get("a"));
    }

    @Test
    public void putAllAssociatesEveryKeyWithTheValue() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.putAll(Arrays.asList("cat", "dog"), "animal");

        assertEquals("animal", map.get("cat"));
        assertEquals("animal", map.get("c"));
        assertEquals("animal", map.get("dog"));
        assertEquals("animal", map.get("d"));
    }

    @Test
    public void removeRestoresRemainingUniqueAbbreviation() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");
        map.put("go", "fish");

        map.remove("go");

        assertEquals("bye", map.get("g"));
        assertEquals("bye", map.get("good"));
        assertNull(map.get("go"));
    }

    @Test
    public void removeUnknownKeyLeavesMappingsUnchanged() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");

        map.remove("gone");

        assertEquals("bye", map.get("g"));
        assertEquals("bye", map.get("good"));
    }

    @Test
    public void toJavaUtilMapContainsOnlyFullKeys() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("beta", "two");
        map.put("alpha", "one");

        Map<String, String> expected = new TreeMap<String, String>();
        expected.put("alpha", "one");
        expected.put("beta", "two");
        assertEquals(expected, map.toJavaUtilMap());
    }

    @Test
    public void emptyMapHasNoMappings() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        assertNull(map.get("x"));
        assertFalse(map.contains("x"));
        assertTrue(map.toJavaUtilMap().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void getRejectsNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void putRejectsNullValue() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("key", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putRejectsEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.remove("");
    }
}
