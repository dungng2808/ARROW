package com.force.i18n.grammar.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class GrammaticalTermMapImpl_ArrowTest {

    @Test
    public void emptyMapHasExpectedState() {
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<Object>();

        assertTrue(map.isEmpty());
        assertFalse(map.isSkinny());
        assertTrue(map.keySet().isEmpty());
        assertTrue(map.values().isEmpty());
        assertTrue(map.entrySet().isEmpty());
        assertNull(map.get("missing"));
        assertFalse(map.containsKey("missing"));
    }

    @Test
    public void mutableMapSupportsPutAndLookup() {
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<Object>();
        Object value = new Object();

        map.put("term", value);

        assertSame(value, map.get("term"));
        assertTrue(map.containsKey("term"));
        assertEquals(1, map.keySet().size());
        assertEquals(1, map.values().size());
        assertEquals(1, map.entrySet().size());
        assertFalse(map.isEmpty());
    }

    @Test
    public void constructorUsesProvidedMapWhenNotSkinny() {
        Map<String, Object> source = new HashMap<String, Object>();
        Object value = new Object();
        source.put("term", value);
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<Object>(source, false);

        assertFalse(map.isSkinny());
        assertSame(value, map.get("term"));
        assertSame(source.keySet(), map.keySet());
    }

    @Test
    public void makeSkinnyCopiesEntriesAndIsImmutable() {
        GrammaticalTermMapImpl<Object> original = new GrammaticalTermMapImpl<Object>();
        Object value = new Object();
        original.put("term", value);

        GrammaticalTermMapImpl<Object> skinny =
                (GrammaticalTermMapImpl<Object>) original.makeSkinny();

        assertTrue(skinny.isSkinny());
        assertSame(value, skinny.get("term"));
        assertEquals(original, skinny);
    }

    @Test(expected = RuntimeException.class)
    public void skinnyMapRejectsPut() {
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<Object>();
        map.makeSkinny().put("term", new Object());
    }

    @Test(expected = RuntimeException.class)
    public void skinnyMapRejectsPutAll() {
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<Object>();
        map.makeSkinny().putAll(new GrammaticalTermMapImpl<Object>());
    }

    @Test
    public void putAllCopiesEntries() {
        GrammaticalTermMapImpl<Object> source = new GrammaticalTermMapImpl<Object>();
        Object value = new Object();
        source.put("term", value);
        GrammaticalTermMapImpl<Object> destination = new GrammaticalTermMapImpl<Object>();

        destination.putAll(source);

        assertSame(value, destination.get("term"));
        assertEquals(1, destination.keySet().size());
    }

    @Test
    public void equalityAndHashCodeReflectEntriesAndSkinnyState() {
        GrammaticalTermMapImpl<Object> first = new GrammaticalTermMapImpl<Object>();
        GrammaticalTermMapImpl<Object> second = new GrammaticalTermMapImpl<Object>();
        Object value = new Object();
        first.put("term", value);
        second.put("term", value);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, first.makeSkinny());
        assertNotEquals(first, new Object());
        assertEquals(first, first);
    }

    @Test
    public void providedSkinnyMapCopiesEntries() {
        Map<String, Object> source = new HashMap<String, Object>();
        Object value = new Object();
        source.put("term", value);
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<Object>(source, true);
        source.clear();

        assertTrue(map.isSkinny());
        assertSame(value, map.get("term"));
        assertEquals(1, map.values().size());
    }

    @Test
    public void keySetAndValuesAreAvailable() {
        GrammaticalTermMapImpl<Object> map = new GrammaticalTermMapImpl<Object>();
        Object value = new Object();
        map.put("term", value);

        Set<String> keys = new HashSet<String>(map.keySet());
        Collection<Object> values = map.values();

        assertEquals(new HashSet<String>(java.util.Arrays.asList("term")), keys);
        assertTrue(values.contains(value));
        assertNotNull(map.entrySet().iterator().next());
    }
}
