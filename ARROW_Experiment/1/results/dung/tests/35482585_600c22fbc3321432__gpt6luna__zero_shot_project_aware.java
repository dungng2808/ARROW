package cz.cvut.kbss.jopa.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MultilingualString_ArrowTest {

    @Test
    public void newInstanceIsEmpty() {
        MultilingualString value = new MultilingualString();

        assertTrue(value.isEmpty());
        assertNull(value.get());
        assertFalse(value.containsSimple());
        assertTrue(value.getLanguages().isEmpty());
        assertTrue(value.getValue().isEmpty());
    }

    @Test
    public void constructorCopiesProvidedValues() {
        Map<String, String> translations = new HashMap<>();
        translations.put("en", "hello");
        translations.put(null, "simple");

        MultilingualString value = new MultilingualString(translations);
        translations.put("fr", "bonjour");

        assertEquals(2, value.getValue().size());
        assertEquals("hello", value.get("en"));
        assertEquals("simple", value.get());
    }

    @Test
    public void constructorRejectsNullMap() {
        assertThrows(NullPointerException.class, () -> new MultilingualString(null));
    }

    @Test
    public void setAndGetLanguageSpecificValue() {
        MultilingualString value = new MultilingualString();

        value.set("en", "hello");

        assertEquals("hello", value.get("en"));
        assertNull(value.get("fr"));
        assertTrue(value.contains("en"));
        assertFalse(value.contains("fr"));
        assertFalse(value.containsSimple());
        assertFalse(value.isEmpty());
    }

    @Test
    public void setWithoutLanguageStoresSimpleLiteral() {
        MultilingualString value = new MultilingualString();

        value.set("hello");

        assertEquals("hello", value.get());
        assertEquals("hello", value.get(null));
        assertTrue(value.contains(null));
        assertTrue(value.containsSimple());
        assertEquals(Collections.singleton(null), value.getLanguages());
    }

    @Test
    public void setWithNullLanguageStoresSimpleLiteral() {
        MultilingualString value = new MultilingualString();

        value.set(null, "hello");

        assertEquals("hello", value.get());
        assertTrue(value.containsSimple());
    }

    @Test
    public void setOverridesExistingValue() {
        MultilingualString value = new MultilingualString();

        value.set("en", "first");
        value.set("en", "second");

        assertEquals("second", value.get("en"));
        assertEquals(1, value.getValue().size());
    }

    @Test
    public void setRejectsNullValueForLanguage() {
        MultilingualString value = new MultilingualString();

        assertThrows(NullPointerException.class, () -> value.set("en", null));
    }

    @Test
    public void setRejectsNullSimpleValue() {
        MultilingualString value = new MultilingualString();

        assertThrows(NullPointerException.class, () -> value.set((String) null));
    }

    @Test
    public void getWithoutLanguageReturnsSimpleLiteralWhenPresent() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        value.set("simple");

        assertEquals("simple", value.get());
        assertEquals("simple", value.get(null));
    }

    @Test
    public void getWithoutLanguageReturnsAvailableTranslationWhenNoSimpleLiteralExists() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");

        assertEquals("hello", value.get());
    }

    @Test
    public void containsNullIsTrueWhenAnyTranslationExists() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");

        assertTrue(value.contains(null));
        assertFalse(value.containsSimple());
    }

    @Test
    public void removeDeletesSpecifiedLanguage() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        value.set("fr", "bonjour");

        value.remove("en");

        assertFalse(value.contains("en"));
        assertTrue(value.contains("fr"));
        assertEquals("bonjour", value.get("fr"));
    }

    @Test
    public void removeNullDeletesSimpleLiteral() {
        MultilingualString value = new MultilingualString();
        value.set("simple");
        value.set("en", "hello");

        value.remove(null);

        assertFalse(value.containsSimple());
        assertTrue(value.contains("en"));
        assertEquals("hello", value.get());
    }

    @Test
    public void getLanguagesIsUnmodifiableAndBackedByInstance() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        Set<String> languages = value.getLanguages();

        value.set("fr", "bonjour");
        assertTrue(languages.contains("fr"));
        assertThrows(UnsupportedOperationException.class, () -> languages.add("de"));
    }

    @Test
    public void getValueIsUnmodifiableAndBackedByInstance() {
        MultilingualString value = new MultilingualString();
        value.set("en", "hello");
        Map<String, String> translations = value.getValue();

        value.set("fr", "bonjour");
        assertEquals("bonjour", translations.get("fr"));
        assertThrows(UnsupportedOperationException.class, () -> translations.put("de", "hallo"));
    }

    @Test
    public void equalsAndHashCodeUseTranslations() {
        MultilingualString first = new MultilingualString();
        first.set("en", "hello");
        first.set("simple");

        MultilingualString second = new MultilingualString();
        second.set("simple");
        second.set("en", "hello");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new MultilingualString());
        assertNotEquals(first, "hello");
        assertEquals(first, first);
    }

    @Test
    public void createBuildsInstanceWithSpecifiedTranslation() {
        MultilingualString value = MultilingualString.create("hello", "en");

        assertEquals("hello", value.get("en"));
        assertEquals(Collections.singleton("en"), value.getLanguages());
    }
}
