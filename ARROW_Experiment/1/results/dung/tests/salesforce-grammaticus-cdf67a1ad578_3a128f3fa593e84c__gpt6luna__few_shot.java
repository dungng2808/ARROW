package com.force.i18n.grammar.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.GrammaticalTermMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class GrammaticalTermMapImpl_ArrowTest {

    @Test
    public void emptyMapHasExpectedDefaults() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();

        assertTrue(map.isEmpty());
        assertFalse(map.isSkinny());
        assertTrue(map.keySet().isEmpty());
        assertTrue(map.values().isEmpty());
        assertTrue(map.entrySet().isEmpty());
        assertNull(map.get("missing"));
        assertFalse(map.containsKey("missing"));
    }

    @Test
    public void mutableMapSupportsBasicOperations() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();
        GrammaticalTerm term = null;

        map.put("term", term);

        assertTrue(map.containsKey("term"));
        assertNull(map.get("term"));
        assertEquals(1, map.keySet().size());
        assertEquals(1, map.values().size());
        assertFalse(map.isEmpty());
    }

    @Test
    public void constructorUsesProvidedMapWhenNotSkinny() {
        Map<String, GrammaticalTerm> source = new HashMap<>();
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(source, false);

        source.put("term", null);

        assertFalse(map.isSkinny());
        assertTrue(map.containsKey("term"));
        assertEquals(1, map.entrySet().size());
    }

    @Test
    public void makeSkinnyCopiesEntriesAndIsImmutable() {
        GrammaticalTermMapImpl<GrammaticalTerm> original = new GrammaticalTermMapImpl<>();
        original.put("term", null);

        GrammaticalTermMap<GrammaticalTerm> skinny = original.makeSkinny();

        assertTrue(skinny.isSkinny());
        assertTrue(skinny.containsKey("term"));
        assertNull(skinny.get("term"));
        try {
            skinny.put("another", null);
            fail("Expected a skinny map to reject modification");
        } catch (RuntimeException expected) {
            assertEquals("This map is not able to modify", expected.getMessage());
        }
    }

    @Test
    public void putAllCopiesEntriesFromAnotherMap() {
        GrammaticalTermMapImpl<GrammaticalTerm> source = new GrammaticalTermMapImpl<>();
        source.put("term", null);
        GrammaticalTermMapImpl<GrammaticalTerm> destination = new GrammaticalTermMapImpl<>();

        destination.putAll(source);

        assertTrue(destination.containsKey("term"));
        assertNull(destination.get("term"));
        assertEquals(1, destination.keySet().size());
    }

    @Test
    public void equalityAndHashCodeReflectContentsAndSkinnyState() {
        GrammaticalTermMapImpl<GrammaticalTerm> first = new GrammaticalTermMapImpl<>();
        first.put("term", null);
        GrammaticalTermMapImpl<GrammaticalTerm> same = new GrammaticalTermMapImpl<>();
        same.put("term", null);
        GrammaticalTermMapImpl<GrammaticalTerm> skinny =
                new GrammaticalTermMapImpl<>(new HashMap<String, GrammaticalTerm>(), true);

        assertEquals(first, first);
        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, skinny);
        assertNotEquals(first, "not a map");
        assertNotNull(first.toString());
    }
}
