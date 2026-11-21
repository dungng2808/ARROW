package com.force.i18n.grammar.impl;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.GrammaticalTermMap;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.impl.GrammaticalTermMapImpl.TermMapSerializer;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class GrammaticalTermMapImpl_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();
        assertNotNull(map);
        assertFalse(map.isSkinny());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testConstructorWithMapAndSkinny() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));
        testMap.put("key2", new MockGrammaticalTerm("key2"));

        GrammaticalTermMapImpl<GrammaticalTerm> skinnyMap = new GrammaticalTermMapImpl<>(testMap, true);
        assertTrue(skinnyMap.isSkinny());
        assertEquals(2, skinnyMap.size());

        GrammaticalTermMapImpl<GrammaticalTerm> regularMap = new GrammaticalTermMapImpl<>(testMap, false);
        assertFalse(regularMap.isSkinny());
        assertEquals(2, regularMap.size());
    }

    @Test
    public void testEqualsAndHashCode() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));

        GrammaticalTermMapImpl<GrammaticalTerm> map1 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map2 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map3 = new GrammaticalTermMapImpl<>(testMap, false);

        assertEquals(map1, map2);
        assertNotEquals(map1, map3);
        assertEquals(map1.hashCode(), map2.hashCode());
        assertNotEquals(map1.hashCode(), map3.hashCode());
    }

    @Test
    public void testMakeSkinny() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));

        GrammaticalTermMapImpl<GrammaticalTerm> regularMap = new GrammaticalTermMapImpl<>(testMap, false);
        GrammaticalTermMapImpl<GrammaticalTerm> skinnyMap = (GrammaticalTermMapImpl<GrammaticalTerm>) regularMap.makeSkinny();

        assertFalse(regularMap.isSkinny());
        assertTrue(skinnyMap.isSkinny());
    }

    @Test
    public void testGetAndContainsKey() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        MockGrammaticalTerm term = new MockGrammaticalTerm("testKey");
        testMap.put("testKey", term);

        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        assertEquals(term, map.get("testKey"));
        assertNull(map.get("nonExistent"));
        assertTrue(map.containsKey("testKey"));
        assertFalse(map.containsKey("nonExistent"));
    }

    @Test
    public void testKeySetEntrySetValues() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));
        testMap.put("key2", new MockGrammaticalTerm("key2"));

        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        assertEquals(testMap.keySet(), map.keySet());
        assertEquals(testMap.entrySet(), map.entrySet());
        assertEquals(testMap.values(), map.values());
    }

    @Test
    public void testPutAndPutAll() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));

        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        MockGrammaticalTerm newTerm = new MockGrammaticalTerm("key2");
        map.put("key2", newTerm);
        assertEquals(newTerm, map.get("key2"));

        Map<String, GrammaticalTerm> otherMap = new HashMap<>();
        otherMap.put("key3", new MockGrammaticalTerm("key3"));
        GrammaticalTermMapImpl<GrammaticalTerm> other = new GrammaticalTermMapImpl<>(otherMap, false);
        map.putAll(other);
        assertEquals(otherMap.get("key3"), map.get("key3"));
    }

    @Test(expected = RuntimeException.class)
    public void testPutOnSkinnyMapShouldThrow() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));

        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, true);
        map.put("key2", new MockGrammaticalTerm("key2"));
    }

    @Test(expected = RuntimeException.class)
    public void testPutAllOnSkinnyMapShouldThrow() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));

        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, true);
        Map<String, GrammaticalTerm> otherMap = new HashMap<>();
        otherMap.put("key2", new MockGrammaticalTerm("key2"));
        GrammaticalTermMapImpl<GrammaticalTerm> other = new GrammaticalTermMapImpl<>(otherMap, false);
        map.putAll(other);
    }

    @Test
    public void testIsEmpty() {
        GrammaticalTermMapImpl<GrammaticalTerm> emptyMap = new GrammaticalTermMapImpl<>();
        assertTrue(emptyMap.isEmpty());

        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));
        GrammaticalTermMapImpl<GrammaticalTerm> nonEmptyMap = new GrammaticalTermMapImpl<>(testMap, false);
        assertFalse(nonEmptyMap.isEmpty());
    }

    @Test
    public void testSerialization() throws Exception {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));

        GrammaticalTermMapImpl<GrammaticalTerm> original = new GrammaticalTermMapImpl<>(testMap, false);

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        GrammaticalTermMapImpl<GrammaticalTerm> deserialized = (GrammaticalTermMapImpl<GrammaticalTerm>) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
    }

    @Test
    public void testTermMapSerializer() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", new MockGrammaticalTerm("key1"));

        TermMapSerializer<GrammaticalTerm> serializer = new TermMapSerializer<>(testMap);
        assertEquals(testMap, serializer.getMap());
    }

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
