package joptsimple.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbbreviationMap_ArrowTest {
    @Test
    public void putCreatesUniqueAbbreviationsAndExactMapping() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.put("good", "bye");

        assertEquals("bye", map.get("good"));
        assertEquals("bye", map.get("goo"));
        assertEquals("bye", map.get("go"));
        assertEquals("bye", map.get("g"));
        assertTrue(map.contains("g"));
        assertFalse(map.contains("x"));
    }

    @Test
    public void addingAnotherKeyMakesSharedPrefixAmbiguous() {
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
    public void replacingValueUpdatesExactKeyAndAbbreviations() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");

        map.put("good", "hello");

        assertEquals("hello", map.get("good"));
        assertEquals("hello", map.get("g"));
        assertEquals(Collections.singletonMap("good", "hello"), map.toJavaUtilMap());
    }

    @Test
    public void putAllAssociatesEveryKeyWithTheValue() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.putAll(Arrays.asList("cat", "dog"), "animal");

        assertEquals("animal", map.get("cat"));
        assertEquals("animal", map.get("c"));
        assertEquals("animal", map.get("dog"));
        assertEquals("animal", map.get("d"));
        assertEquals(2, map.toJavaUtilMap().size());
    }

    @Test
    public void removeRestoresAbbreviationWhenAmbiguousKeyIsRemoved() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");
        map.put("go", "fish");

        map.remove("go");

        assertEquals("bye", map.get("g"));
        assertEquals("bye", map.get("good"));
        assertNull(map.get("go"));
        assertEquals(Collections.singletonMap("good", "bye"), map.toJavaUtilMap());
    }

    @Test
    public void removingMissingKeyLeavesMappingsUnchanged() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("good", "bye");

        map.remove("gone");

        assertEquals("bye", map.get("g"));
        assertEquals(Collections.singletonMap("good", "bye"), map.toJavaUtilMap());
    }

    @Test
    public void removingOneOfTwoKeysWithDifferentPrefixesPreservesOtherKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("cat", "feline");
        map.put("dog", "canine");

        map.remove("cat");

        assertNull(map.get("c"));
        assertEquals("canine", map.get("d"));
        assertEquals(Collections.singletonMap("dog", "canine"), map.toJavaUtilMap());
    }

    @Test
    public void emptyMapHasNoMappings() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        assertNull(map.get("anything"));
        assertFalse(map.contains("anything"));
        assertTrue(map.toJavaUtilMap().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void getRejectsNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void containsRejectsNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.contains(null);
    }

    @Test(expected = NullPointerException.class)
    public void putRejectsNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.put(null, "value");
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

    @Test(expected = NullPointerException.class)
    public void removeRejectsNullKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.remove(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsEmptyKey() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.remove("");
    }

    @Test(expected = NullPointerException.class)
    public void putAllRejectsNullIterable() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();

        map.putAll(null, "value");
    }

    @Test
    public void toJavaUtilMapContainsOnlyFullKeysInSortedOrder() {
        AbbreviationMap<String> map = new AbbreviationMap<String>();
        map.put("zebra", "striped");
        map.put("apple", "fruit");

        Map<String, String> result = map.toJavaUtilMap();

        assertEquals(Arrays.asList("apple", "zebra"), Arrays.asList(
            result.keySet().toArray(new String[result.size()])));
        assertEquals("fruit", result.get("apple"));
        assertEquals("striped", result.get("zebra"));
    }
}
