package cz.cvut.kbss.jopa.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MultilingualString_ArrowTest {

    @Test
    public void newInstanceIsEmpty() {
        MultilingualString value = new MultilingualString();

        assertTrue(value.isEmpty());
        assertNull(value.get());
        assertFalse(value.containsSimple());
        assertFalse(value.contains("en"));
        assertFalse(value.contains(null));
        assertTrue(value.getLanguages().isEmpty());
        assertTrue(value.getValue().isEmpty());
    }

    @Test
    public void constructorCopiesProvidedMap() {
        Map<String, String> translations = new HashMap<>();
        translations.put("en", "hello");
        MultilingualString value = new MultilingualString(translations);
        translations.put("fr", "bonjour");

        assertEquals("hello", value.get("en"));
        assertNull(value.get("fr"));
        assertEquals(1, value.getValue().size());
    }

    @Test
    public void setAndGetTranslations() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        value.set("fr", "bonjour");

        assertEquals("hello", value.get("en"));
        assertEquals("bonjour", value.get("fr"));
        assertNull(value.get("de"));
        assertTrue(value.contains("en"));
        assertFalse(value.contains("de"));
        assertEquals(2, value.getLanguages().size());
    }

    @Test
    public void settingExistingLanguageReplacesValue() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        value.set("en", "hi");

        assertEquals("hi", value.get("en"));
        assertEquals(1, value.getValue().size());
    }

    @Test
    public void setWithoutLanguageStoresSimpleLiteral() {
        MultilingualString value = new MultilingualString();
        value.set("plain");

        assertEquals("plain", value.get());
        assertEquals("plain", value.get(null));
        assertTrue(value.containsSimple());
        assertTrue(value.contains(null));
        assertEquals(1, value.getLanguages().size());
        assertTrue(value.getLanguages().contains(null));
    }

    @Test
    public void nullLanguageStoresSimpleLiteral() {
        MultilingualString value = new MultilingualString();
        value.set(null, "plain");

        assertEquals("plain", value.get());
        assertTrue(value.containsSimple());
        assertTrue(value.contains(null));
    }

    @Test
    public void getWithoutLanguagePrefersSimpleLiteral() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        value.set("plain");

        assertEquals("plain", value.get());
        assertEquals("hello", value.get("en"));
    }

    @Test
    public void getWithoutLanguageReturnsAvailableTranslationWhenNoSimpleLiteralExists() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");

        assertEquals("hello", value.get());
        assertEquals("hello", value.get(null));
        assertTrue(value.contains(null));
    }

    @Test
    public void removeTranslation() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        value.set("fr", "bonjour");
        value.remove("en");

        assertNull(value.get("en"));
        assertEquals("bonjour", value.get("fr"));
        assertFalse(value.contains("en"));
        assertEquals(1, value.getLanguages().size());
    }

    @Test
    public void removeSimpleLiteral() {
        MultilingualString value = new MultilingualString();
        value.set("plain");
        value.remove(null);

        assertFalse(value.containsSimple());
        assertTrue(value.isEmpty());
        assertNull(value.get());
    }

    @Test
    public void getValueIsUnmodifiableAndBackedByInstance() {
        MultilingualString value = new MultilingualString();
        Map<String, String> translations = value.getValue();
        value.set("en", "hello");

        assertEquals("hello", translations.get("en"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getValueCannotBeModified() {
        MultilingualString value = new MultilingualString();
        value.getValue().put("en", "hello");
    }

    @Test
    public void getLanguagesIsUnmodifiableAndBackedByInstance() {
        MultilingualString value = new MultilingualString();
        Set<String> languages = value.getLanguages();
        value.set("en", "hello");

        assertTrue(languages.contains("en"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLanguagesCannotBeModified() {
        MultilingualString value = new MultilingualString();
        value.getLanguages().add("en");
    }

    @Test
    public void equalsAndHashCodeUseTranslations() {
        MultilingualString first = new MultilingualString();
        first.set("en", "hello");
        MultilingualString second = new MultilingualString();
        second.set("en", "hello");
        MultilingualString different = new MultilingualString();
        different.set("en", "hi");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, "hello");
        assertEquals(first, first);
    }

    @Test
    public void createSetsInitialTranslation() {
        MultilingualString value = MultilingualString.create("hello", "en");

        assertEquals("hello", value.get("en"));
        assertTrue(value.contains("en"));
        assertFalse(value.containsSimple());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullMap() {
        new MultilingualString(null);
    }

    @Test(expected = NullPointerException.class)
    public void setLanguageRejectsNullValue() {
        MultilingualString value = new MultilingualString();
        value.set("en", null);
    }

    @Test(expected = NullPointerException.class)
    public void setSimpleRejectsNullValue() {
        MultilingualString value = new MultilingualString();
        value.set((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void createRejectsNullValue() {
        MultilingualString.create(null, "en");
    }
}
