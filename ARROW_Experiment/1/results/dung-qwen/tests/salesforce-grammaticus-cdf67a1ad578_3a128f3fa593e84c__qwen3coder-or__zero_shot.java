package com.force.i18n.grammar.impl;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.impl.GrammaticalTermMapImpl.TermMapSerializer;
import com.force.i18n.commons.util.collection.MapSerializer;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class GrammaticalTermMapImpl_ArrowTest {

    @Test
    public void testConstructorDefault() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();
        assertNotNull(map);
        assertFalse(map.isSkinny());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testConstructorWithMapAndSkinny() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, true);
        assertTrue(map.isSkinny());
        assertEquals(1, map.size());

        map = new GrammaticalTermMapImpl<>(testMap, false);
        assertFalse(map.isSkinny());
        assertEquals(1, map.size());
    }

    @Test
    public void testEquals() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));

        GrammaticalTermMapImpl<GrammaticalTerm> map1 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map2 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map3 = new GrammaticalTermMapImpl<>(testMap, false);

        assertEquals(map1, map2);
        assertNotEquals(map1, map3);
        assertNotEquals(map1, new Object());
        assertEquals(map1, map1);
    }

    @Test
    public void testHashCode() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));

        GrammaticalTermMapImpl<GrammaticalTerm> map1 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map2 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map3 = new GrammaticalTermMapImpl<>(testMap, false);

        assertEquals(map1.hashCode(), map2.hashCode());
        assertNotEquals(map1.hashCode(), map3.hashCode());
    }

    @Test
    public void testIsSkinny() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();
        assertFalse(map.isSkinny());

        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        GrammaticalTermMapImpl<GrammaticalTerm> skinnyMap = new GrammaticalTermMapImpl<>(testMap, true);
        assertTrue(skinnyMap.isSkinny());
    }

    @Test
    public void testMakeSkinny() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        GrammaticalTermMapImpl<GrammaticalTerm> skinny = (GrammaticalTermMapImpl<GrammaticalTerm>) map.makeSkinny();
        assertTrue(skinny.isSkinny());
        assertFalse(map.isSkinny());
    }

    @Test
    public void testGet() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        MockGrammaticalTerm term = new MockGrammaticalTerm("test");
        testMap.put("test", term);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        assertEquals(term, map.get("test"));
        assertNull(map.get("nonexistent"));
    }

    @Test
    public void testContainsKey() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        assertTrue(map.containsKey("test"));
        assertFalse(map.containsKey("nonexistent"));
    }

    @Test
    public void testKeySet() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        testMap.put("test2", new MockGrammaticalTerm("test2"));
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        assertEquals(testMap.keySet(), map.keySet());
    }

    @Test
    public void testEntrySet() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        testMap.put("test2", new MockGrammaticalTerm("test2"));
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        assertEquals(testMap.entrySet(), map.entrySet());
    }

    @Test
    public void testValues() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        MockGrammaticalTerm term1 = new MockGrammaticalTerm("test");
        MockGrammaticalTerm term2 = new MockGrammaticalTerm("test2");
        testMap.put("test", term1);
        testMap.put("test2", term2);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        assertEquals(testMap.values(), map.values());
    }

    @Test
    public void testPut() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        MockGrammaticalTerm term = new MockGrammaticalTerm("test");
        map.put("test", term);
        assertEquals(term, map.get("test"));
    }

    @Test(expected = RuntimeException.class)
    public void testPutOnSkinny() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, true);
        MockGrammaticalTerm term = new MockGrammaticalTerm("test");
        map.put("test", term);
    }

    @Test
    public void testPutAll() {
        Map<String, GrammaticalTerm> testMap1 = new HashMap<>();
        Map<String, GrammaticalTerm> testMap2 = new HashMap<>();
        MockGrammaticalTerm term1 = new MockGrammaticalTerm("test1");
        MockGrammaticalTerm term2 = new MockGrammaticalTerm("test2");
        testMap1.put("test1", term1);
        testMap2.put("test2", term2);
        GrammaticalTermMapImpl<GrammaticalTerm> map1 = new GrammaticalTermMapImpl<>(testMap1, false);
        GrammaticalTermMapImpl<GrammaticalTerm> map2 = new GrammaticalTermMapImpl<>(testMap2, false);
        map1.putAll(map2);
        assertEquals(term1, map1.get("test1"));
        assertEquals(term2, map1.get("test2"));
    }

    @Test(expected = RuntimeException.class)
    public void testPutAllOnSkinny() {
        Map<String, GrammaticalTerm> testMap1 = new HashMap<>();
        Map<String, GrammaticalTerm> testMap2 = new HashMap<>();
        GrammaticalTermMapImpl<GrammaticalTerm> map1 = new GrammaticalTermMapImpl<>(testMap1, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map2 = new GrammaticalTermMapImpl<>(testMap2, false);
        map1.putAll(map2);
    }

    @Test
    public void testIsEmpty() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();
        assertTrue(map.isEmpty());
        map.put("test", new MockGrammaticalTerm("test"));
        assertFalse(map.isEmpty());
    }

    @Test
    public void testSize() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();
        assertEquals(0, map.size());
        map.put("test", new MockGrammaticalTerm("test"));
        assertEquals(1, map.size());
    }

    @Test
    public void testTermMapSerializerInternKey() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        TermMapSerializer<GrammaticalTerm> serializer = new TermMapSerializer<>(testMap);
        assertEquals("test", serializer.internKey("test"));
    }

    @Test
    public void testTermMapSerializerGetMap() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("test", new MockGrammaticalTerm("test"));
        TermMapSerializer<GrammaticalTerm> serializer = new TermMapSerializer<>(testMap);
        assertEquals(testMap, serializer.getMap());
    }

    // Helper class for testing
    private static class MockGrammaticalTerm implements GrammaticalTerm {
        private final String name;

        public MockGrammaticalTerm(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void toJson(Appendable out) throws IOException {
            out.append("\"").append(name).append("\"");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MockGrammaticalTerm that = (MockGrammaticalTerm) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
