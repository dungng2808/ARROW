package com.force.i18n.grammar.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.RenamingProvider;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
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
        testMap.put("key1", null);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, true);
        assertTrue(map.isSkinny());
        assertEquals(testMap, map.map);

        map = new GrammaticalTermMapImpl<>(testMap, false);
        assertFalse(map.isSkinny());
        assertEquals(testMap, map.map);
    }

    @Test
    public void testEquals() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", null);
        GrammaticalTermMapImpl<GrammaticalTerm> map1 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map2 = new GrammaticalTermMapImpl<>(testMap, true);
        GrammaticalTermMapImpl<GrammaticalTerm> map3 = new GrammaticalTermMapImpl<>(testMap, false);

        assertEquals(map1, map2);
        assertNotEquals(map1, map3);
        assertEquals(map1, map1);
        assertNotEquals(map1, new Object());
    }

    @Test
    public void testHashCode() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", null);
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

        map = new GrammaticalTermMapImpl<>(new HashMap<>(), true);
        assertTrue(map.isSkinny());
    }

    @Test
    public void testMakeSkinny() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", null);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        GrammaticalTermMapImpl<GrammaticalTerm> skinny = (GrammaticalTermMapImpl<GrammaticalTerm>) map.makeSkinny();

        assertTrue(skinny.isSkinny());
        assertEquals(map.map, skinny.map);
    }

    @Test
    public void testGet() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTerm term = new Noun() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void toJson(Appendable out) throws IOException {
                out.append("\"test\"");
            }
        };
        testMap.put("key1", term);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        assertEquals(term, map.get("key1"));
        assertNull(map.get("nonexistent"));
    }

    @Test
    public void testContainsKey() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTerm term = new Noun() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void toJson(Appendable out) throws IOException {
                out.append("\"test\"");
            }
        };
        testMap.put("key1", term);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsKey("nonexistent"));
    }

    @Test
    public void testKeySet() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", null);
        testMap.put("key2", null);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        Set<String> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
    }

    @Test
    public void testEntrySet() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", null);
        testMap.put("key2", null);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        Set<Map.Entry<String, GrammaticalTerm>> entries = map.entrySet();
        assertEquals(2, entries.size());
    }

    @Test
    public void testValues() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        testMap.put("key1", null);
        testMap.put("key2", null);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        Collection<GrammaticalTerm> values = map.values();
        assertEquals(2, values.size());
    }

    @Test
    public void testPut() {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);
        GrammaticalTerm term = new Noun() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void toJson(Appendable out) throws IOException {
                out.append("\"test\"");
            }
        };

        map.put("key1", term);
        assertEquals(term, map.get("key1"));

        // Test that skinny map throws exception
        GrammaticalTermMapImpl<GrammaticalTerm> skinnyMap = new GrammaticalTermMapImpl<>(testMap, true);
        try {
            skinnyMap.put("key2", term);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    @Test
    public void testPutAll() {
        Map<String, GrammaticalTerm> testMap1 = new HashMap<>();
        Map<String, GrammaticalTerm> testMap2 = new HashMap<>();
        GrammaticalTerm term = new Noun() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void toJson(Appendable out) throws IOException {
                out.append("\"test\"");
            }
        };
        testMap1.put("key1", term);
        testMap2.put("key2", term);

        GrammaticalTermMapImpl<GrammaticalTerm> map1 = new GrammaticalTermMapImpl<>(testMap1, false);
        GrammaticalTermMapImpl<GrammaticalTerm> map2 = new GrammaticalTermMapImpl<>(testMap2, false);

        map1.putAll(map2);
        assertEquals(term, map1.get("key1"));
        assertEquals(term, map1.get("key2"));

        // Test that skinny map throws exception
        GrammaticalTermMapImpl<GrammaticalTerm> skinnyMap = new GrammaticalTermMapImpl<>(testMap1, true);
        try {
            skinnyMap.putAll(map2);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    @Test
    public void testIsEmpty() {
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>();
        assertTrue(map.isEmpty());

        map.put("key1", null);
        assertFalse(map.isEmpty());
    }

    @Test
    public void testSerialization() throws Exception {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTerm term = new Noun() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void toJson(Appendable out) throws IOException {
                out.append("\"test\"");
            }
        };
        testMap.put("key1", term);
        GrammaticalTermMapImpl<GrammaticalTerm> original = new GrammaticalTermMapImpl<>(testMap, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        GrammaticalTermMapImpl<GrammaticalTerm> deserialized = (GrammaticalTermMapImpl<GrammaticalTerm>) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
    }

    @Test
    public void testWriteJsonWithTermsToInclude() throws Exception {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTerm term = new Noun() {
            @Override
            public String getName() {
                return "Test";
            }

            @Override
            public void toJson(Appendable out) throws IOException {
                out.append("\"test\"");
            }
        };
        testMap.put("test", term);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        StringBuilder sb = new StringBuilder();
        Set<String> termsToInclude = new HashSet<>();
        termsToInclude.add("test");

        // Mocking required dependencies
        RenamingProvider renamingProvider = null;
        LanguageDictionary dictionary = new LanguageDictionary() {
            @Override
            public HumanLanguage getLanguage() {
                return null;
            }

            @Override
            public Noun getNounOverride(Noun noun) {
                return noun;
            }
        };

        map.writeJson(sb, renamingProvider, dictionary, termsToInclude);
        assertEquals("{\"test\":\"test\"}", sb.toString());
        assertTrue(termsToInclude.isEmpty());
    }

    @Test
    public void testWriteJsonWithoutTermsToInclude() throws Exception {
        Map<String, GrammaticalTerm> testMap = new HashMap<>();
        GrammaticalTerm term = new Noun() {
            @Override
            public String getName() {
                return "Test";
            }

            @Override
            public void toJson(Appendable out) throws IOException {
                out.append("\"test\"");
            }
        };
        testMap.put("test", term);
        GrammaticalTermMapImpl<GrammaticalTerm> map = new GrammaticalTermMapImpl<>(testMap, false);

        StringBuilder sb = new StringBuilder();

        // Mocking required dependencies
        RenamingProvider renamingProvider = null;
        HumanLanguage lang = null;

        map.writeJson(sb, renamingProvider, lang);
        assertEquals("{\"test\":\"test\"}", sb.toString());
    }
}
