package cz.cvut.kbss.jopa.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MultilingualString_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        MultilingualString mls = new MultilingualString();
        assertNotNull(mls);
        assertTrue(mls.isEmpty());
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, String> map = new HashMap<>();
        map.put("en", "Hello");
        map.put("cs", "Ahoj");
        MultilingualString mls = new MultilingualString(map);
        assertEquals(2, mls.getValue().size());
        assertEquals("Hello", mls.get("en"));
        assertEquals("Ahoj", mls.get("cs"));
    }

    @Test
    public void testSetWithLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        assertEquals("Hello", mls.get("en"));
    }

    @Test
    public void testSetWithoutLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("Hello");
        assertEquals("Hello", mls.get());
    }

    @Test
    public void testGetWithLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        assertEquals("Hello", mls.get("en"));
        assertEquals("Ahoj", mls.get("cs"));
    }

    @Test
    public void testGetWithoutLanguage() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        mls.set("Hello");
        assertEquals("Hello", mls.get());
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
        assertNull(mls.get("en"));
        mls.remove(null);
        assertNull(mls.get());
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
    }

    @Test
    public void testGetValue() {
        MultilingualString mls = new MultilingualString();
        mls.set("en", "Hello");
        mls.set("cs", "Ahoj");
        Map<String, String> value = mls.getValue();
        assertEquals(2, value.size());
        assertEquals("Hello", value.get("en"));
        assertEquals("Ahoj", value.get("cs"));
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
    }
}
