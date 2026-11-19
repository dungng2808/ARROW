package cz.cvut.kbss.jopa.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MultilingualString_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        MultilingualString ms = new MultilingualString();
        assertNotNull(ms);
        assertTrue(ms.isEmpty());
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, String> map = new HashMap<>();
        map.put("en", "Hello");
        map.put("cs", "Ahoj");

        MultilingualString ms = new MultilingualString(map);
        assertEquals("Hello", ms.get("en"));
        assertEquals("Ahoj", ms.get("cs"));
        assertFalse(ms.isEmpty());
    }

    @Test
    public void testSetWithStringAndLanguage() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        assertEquals("Hello", ms.get("en"));
    }

    @Test
    public void testSetWithStringOnly() {
        MultilingualString ms = new MultilingualString();
        ms.set("Hello");
        assertEquals("Hello", ms.get());
    }

    @Test
    public void testGetWithLanguage() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        ms.set("cs", "Ahoj");
        assertEquals("Hello", ms.get("en"));
        assertEquals("Ahoj", ms.get("cs"));
    }

    @Test
    public void testGetWithoutLanguage() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        ms.set("cs", "Ahoj");
        ms.set("Hello");
        assertEquals("Hello", ms.get());
    }

    @Test
    public void testContainsWithLanguage() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        assertTrue(ms.contains("en"));
        assertFalse(ms.contains("cs"));
    }

    @Test
    public void testContainsWithoutLanguage() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        assertTrue(ms.contains(null));
        ms.set("Hello");
        assertTrue(ms.contains(null));
    }

    @Test
    public void testContainsSimple() {
        MultilingualString ms = new MultilingualString();
        assertFalse(ms.containsSimple());
        ms.set("Hello");
        assertTrue(ms.containsSimple());
    }

    @Test
    public void testIsEmpty() {
        MultilingualString ms = new MultilingualString();
        assertTrue(ms.isEmpty());
        ms.set("en", "Hello");
        assertFalse(ms.isEmpty());
    }

    @Test
    public void testRemove() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        ms.set("cs", "Ahoj");
        ms.set("Hello");
        ms.remove("en");
        assertNull(ms.get("en"));
        ms.remove(null);
        assertNull(ms.get());
    }

    @Test
    public void testGetLanguages() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        ms.set("cs", "Ahoj");
        ms.set("Hello");
        Set<String> languages = ms.getLanguages();
        assertTrue(languages.contains("en"));
        assertTrue(languages.contains("cs"));
        assertTrue(languages.contains(null));
        assertEquals(3, languages.size());
    }

    @Test
    public void testGetValue() {
        MultilingualString ms = new MultilingualString();
        ms.set("en", "Hello");
        ms.set("cs", "Ahoj");
        ms.set("Hello");
        Map<String, String> value = ms.getValue();
        assertEquals("Hello", value.get("en"));
        assertEquals("Ahoj", value.get("cs"));
        assertEquals("Hello", value.get(null));
        assertEquals(3, value.size());
    }

    @Test
    public void testEquals() {
        MultilingualString ms1 = new MultilingualString();
        ms1.set("en", "Hello");
        MultilingualString ms2 = new MultilingualString();
        ms2.set("en", "Hello");
        assertEquals(ms1, ms2);
        ms2.set("cs", "Ahoj");
        assertNotEquals(ms1, ms2);
    }

    @Test
    public void testHashCode() {
        MultilingualString ms1 = new MultilingualString();
        ms1.set("en", "Hello");
        MultilingualString ms2 = new MultilingualString();
        ms2.set("en", "Hello");
        assertEquals(ms1.hashCode(), ms2.hashCode());
    }

    @Test
    public void testCreate() {
        MultilingualString ms = MultilingualString.create("Hello", "en");
        assertEquals("Hello", ms.get("en"));
    }
}
