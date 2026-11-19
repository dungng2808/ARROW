package cz.cvut.kbss.jopa.model;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class MultilingualString_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        MultilingualString mls = new MultilingualString();
        assertNotNull(mls);
        assertTrue(mls.isEmpty());
        assertNull(mls.get(null));
        assertNull(mls.get("en"));
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, String> map = new HashMap<>();
        map.put("en", "Hello");
        map.put("cs", "Ahoj");
        MultilingualString mls = new MultilingualString(map);
        
        assertEquals("Hello", mls.get("en"));
        assertEquals("Ahoj", mls.get("cs"));
        assertFalse(mls.isEmpty());
    }

    @Test
    public void testSetWithStringAndLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        assertEquals("Hello", mls.get("en"));
        assertTrue(mls.contains("en"));
    }

    @Test
    public void testSetWithStringOnly() {
        MultilingualString mls = new MultilingualString();
        mls.set("Hello");
        assertEquals("Hello", mls.get(null));
        assertTrue(mls.containsSimple());
    }

    @Test
    public void testGetWithLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        assertEquals("Hello", mls.get("en"));
        assertEquals("Ahoj", mls.get("cs"));
        assertNull(mls.get("de"));
    }

    @Test
    public void testGetWithoutLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        mls.set("Hello");
        assertEquals("Hello", mls.get(null));
        assertEquals("Hello", mls.get(null));
    }

    @Test
    public void testContainsWithLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        assertTrue(mls.contains("en"));
        assertFalse(mls.contains("cs"));
    }

    @Test
    public void testContainsWithoutLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        assertTrue(mls.contains(null));
        mls.set("Hello");
        assertTrue(mls.contains(null));
    }

    @Test
    public void testContainsSimple() {
        MultilingualString mls = new MultilingualString();
        assertFalse(mls.containsSimple());
        mls.set("Hello");
        assertTrue(mls.containsSimple());
    }

    @Test
    public void testIsEmpty() {
        MultilingualString mls = new MultilingualString();
        assertTrue(mls.isEmpty());
        mls.set("en", "Hello");
        assertFalse(mls.isEmpty());
    }

    @Test
    public void testRemove() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        mls.set("Hello");
        mls.remove("en");
        assertFalse(mls.contains("en"));
        assertTrue(mls.containsSimple());
        mls.remove(null);
        assertFalse(mls.containsSimple());
    }

    @Test
    public void testGetLanguages() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        mls.set("Hello");
        Set<String> languages = mls.getLanguages();
        assertTrue(languages.contains("en"));
        assertTrue(languages.contains("cs"));
        assertTrue(languages.contains(null));
        assertEquals(3, languages.size());
    }

    @Test
    public void testGetValue() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        Map<String, String> value = mls.getValue();
        assertEquals("Hello", value.get("en"));
        assertEquals("Ahoj", value.get("cs"));
        assertTrue(value.containsKey(null));
    }

    @Test
    public void testEquals() {
        MultilingualString mls1 = new MultilingualString();
        MultilingualString mls2 = new MultilingualString();
        assertEquals(mls1, mls2);
        mls1.set("en", "Hello");
        assertNotEquals(mls1, mls2);
        mls2.set("en", "Hello");
        assertEquals(mls1, mls2);
    }

    @Test
    public void testHashCode() {
        MultilingualString mls1 = new MultilingualString();
        MultilingualString mls2 = new MultilingualString();
        assertEquals(mls1.hashCode(), mls2.hashCode());
        mls1.set("en", "Hello");
        assertNotEquals(mls1.hashCode(), mls2.hashCode());
        mls2.set("en", "Hello");
        assertEquals(mls1.hashCode(), mls2.hashCode());
    }

    @Test
    public void testCreate() {
        MultilingualString mls = MultilingualString.create("Hello", "en");
        assertEquals("Hello", mls.get("en"));
        assertEquals("Hello", mls.get(null));
    }
}
