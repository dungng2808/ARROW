package com.force.i18n.grammar.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class GrammaticalTermMapImpl_ArrowTest {

    @Test
    public void emptyMapHasExpectedState() {
        GrammaticalTermMapImpl<?> map = new GrammaticalTermMapImpl<>();

        assertTrue(map.isEmpty());
        assertFalse(map.isSkinny());
        assertTrue(map.keySet().isEmpty());
        assertTrue(map.values().isEmpty());
        assertTrue(map.entrySet().isEmpty());
        assertNull(map.get("missing"));
        assertFalse(map.containsKey("missing"));
    }

    @Test
    public void mutableMapExposesAndUpdatesItsContents() {
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<>();
        Object value = new Object();

        map.put("alpha", value);

        assertFalse(map.isEmpty());
        assertTrue(map.containsKey("alpha"));
        assertEquals(value, map.get("alpha"));
        assertTrue(map.keySet().contains("alpha"));
        assertTrue(map.values().contains(value));
        assertEquals(1, map.entrySet().size());
    }

    @Test
    public void constructorUsesProvidedMapWhenNotSkinny() {
        Map<String, Object> contents = new HashMap<>();
        Object value = new Object();
        contents.put("alpha", value);

        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<>(contents, false);

        assertFalse(map.isSkinny());
        assertEquals(value, map.get("alpha"));
        assertEquals(contents, map.entrySet().stream().collect(
                java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Test
    public void makeSkinnyCopiesContentsAndIsImmutable() {
        GrammaticalTermMapImpl<Object> original = new GrammaticalTermMapImpl<>();
        Object value = new Object();
        original.put("alpha", value);

        GrammaticalTermMapImpl<Object> skinny =
                (GrammaticalTermMapImpl<Object>) original.makeSkinny();

        assertTrue(skinny.isSkinny());
        assertEquals(value, skinny.get("alpha"));
        original.put("beta", new Object());
        assertFalse(skinny.containsKey("beta"));
    }

    @Test
    public void skinnyMapRejectsPut() {
        Map<String, Object> contents = new HashMap<>();
        contents.put("alpha", new Object());
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<>(contents, true);

        try {
            map.put("beta", new Object());
            fail("Expected skinny map to reject modification");
        } catch (RuntimeException expected) {
            assertEquals("This map is not able to modify", expected.getMessage());
        }
    }

    @Test
    public void skinnyMapRejectsPutAll() {
        Map<String, Object> contents = new HashMap<>();
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<>(contents, true);
        GrammaticalTermMapImpl<Object> other = new GrammaticalTermMapImpl<>();

        try {
            map.putAll(other);
            fail("Expected skinny map to reject modification");
        } catch (RuntimeException expected) {
            assertEquals("This map is not able to modify", expected.getMessage());
        }
    }

    @Test
    public void putAllCopiesEntriesFromAnotherMap() {
        GrammaticalTermMapImpl<Object> source = new GrammaticalTermMapImpl<>();
        Object value = new Object();
        source.put("alpha", value);
        GrammaticalTermMapImpl<Object> destination = new GrammaticalTermMapImpl<>();

        destination.putAll(source);

        assertEquals(value, destination.get("alpha"));
        assertEquals(1, destination.keySet().size());
    }

    @Test
    public void equalityAndHashCodeReflectContentsAndSkinnyState() {
        Map<String, Object> contents = new HashMap<>();
        contents.put("alpha", "value");
        GrammaticalTermMapImpl<Object> first = new GrammaticalTermMapImpl<>(contents, false);
        GrammaticalTermMapImpl<Object> equal = new GrammaticalTermMapImpl<>(contents, false);
        GrammaticalTermMapImpl<Object> skinny = new GrammaticalTermMapImpl<>(contents, true);

        assertTrue(first.equals(first));
        assertTrue(first.equals(equal));
        assertEquals(first.hashCode(), equal.hashCode());
        assertFalse(first.equals(skinny));
        assertFalse(first.equals(null));
        assertFalse(first.equals("not a map"));
        assertNotEquals(first.hashCode(), skinny.hashCode());
    }

    @Test
    public void keySetAndEntrySetAreBackedByMutableMap() {
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<>();
        Object value = new Object();
        map.put("alpha", value);

        Set<String> keys = map.keySet();
        Collection<Object> values = map.values();
        Set<Map.Entry<String, Object>> entries = map.entrySet();

        map.put("beta", new Object());

        assertEquals(2, keys.size());
        assertEquals(2, values.size());
        assertEquals(2, entries.size());
        assertNotNull(keys);
        assertTrue(new HashSet<>(keys).contains("alpha"));
    }
}
