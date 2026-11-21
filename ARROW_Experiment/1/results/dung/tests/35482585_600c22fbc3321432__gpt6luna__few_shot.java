package cz.cvut.kbss.jopa.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MultilingualString_ArrowTest {

    @Test
    public void emptyInstanceHasNoValues() {
        MultilingualString value = new MultilingualString();

        assertTrue(value.isEmpty());
        assertNull(value.get());
        assertNull(value.get("en"));
        assertFalse(value.contains("en"));
        assertFalse(value.containsSimple());
        assertTrue(value.getLanguages().isEmpty());
        assertTrue(value.getValue().isEmpty());
    }

    @Test
    public void setAndGetTranslations() {
        MultilingualString value = new MultilingualString();
        value.set("en", "Hello");
        value.set("fr", "Bonjour");

        assertEquals("Hello", value.get("en"));
        assertEquals("Bonjour", value.get("fr"));
        assertNull(value.get("de"));
        assertTrue(value.contains("en"));
        assertTrue(value.contains("fr"));
        assertFalse(value.contains("de"));
        assertTrue(value.contains(null));
        assertFalse(value.containsSimple());
        assertFalse(value.isEmpty());
        assertEquals(2, value.getLanguages().size());
        assertEquals("Hello", value.getValue().get("en"));
        assertEquals("Bonjour", value.getValue().get("fr"));
    }

    @Test
    public void simpleLiteralIsReturnedForNullLanguage() {
        MultilingualString value = new MultilingualString();
        value.set("Plain text");

        assertEquals("Plain text", value.get());
        assertEquals("Plain text", value.get(null));
        assertTrue(value.contains(null));
        assertTrue(value.containsSimple());
        assertEquals("Plain text", value.getValue().get(null));
        assertTrue(value.getLanguages().contains(null));
    }

    @Test
    public void nullLanguageSetOverridesSimpleLiteral() {
        MultilingualString value = new MultilingualString();
        value.set("First");
        value.set(null, "Second");

        assertEquals("Second", value.get());
        assertEquals("Second", value.get(null));
        assertTrue(value.containsSimple());
        assertEquals(1, value.getLanguages().size());
    }

    @Test
    public void settingExistingLanguageReplacesItsValue() {
        MultilingualString value = new MultilingualString();
        value.set("en", "First");
        value.set("en", "Second");

        assertEquals("Second", value.get("en"));
        assertEquals(1, value.getLanguages().size());
    }

    @Test
    public void removeDeletesSpecifiedTranslation() {
        MultilingualString value = new MultilingualString();
        value.set("en", "Hello");
        value.set("fr", "Bonjour");
        value.remove("en");

        assertNull(value.get("en"));
        assertFalse(value.contains("en"));
        assertTrue(value.contains("fr"));
        assertEquals(1, value.getLanguages().size());
    }

    @Test
    public void removeNullDeletesSimpleLiteral() {
        MultilingualString value = new MultilingualString();
        value.set("Plain text");
        value.remove(null);

        assertFalse(value.containsSimple());
        assertTrue(value.isEmpty());
        assertNull(value.get());
    }

    @Test
    public void constructorCopiesProvidedMap() {
        Map<String, String> translations = new HashMap<>();
        translations.put("en", "Hello");
        MultilingualString value = new MultilingualString(translations);
        translations.put("en", "Changed");
        translations.put("fr", "Bonjour");

        assertEquals("Hello", value.get("en"));
        assertNull(value.get("fr"));
        assertEquals(1, value.getLanguages().size());
    }

    @Test
    public void getValueAndLanguagesAreUnmodifiableViews() {
        MultilingualString value = new MultilingualString();
        value.set("en", "Hello");

        try {
            value.getValue().put("fr", "Bonjour");
        } catch (UnsupportedOperationException expected) {
            assertFalse(value.contains("fr"));
        }

        try {
            value.getLanguages().remove("en");
        } catch (UnsupportedOperationException expected) {
            assertTrue(value.contains("en"));
        }
    }

    @Test
    public void equalsAndHashCodeUseTranslations() {
        MultilingualString first = new MultilingualString();
        first.set("en", "Hello");
        MultilingualString second = new MultilingualString();
        second.set("en", "Hello");
        MultilingualString different = new MultilingualString();
        different.set("en", "Goodbye");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, "Hello");
    }

    @Test
    public void createBuildsInstanceWithSpecifiedTranslation() {
        MultilingualString value = MultilingualString.create("Hello", "en");

        assertEquals("Hello", value.get("en"));
        assertTrue(value.contains("en"));
        assertEquals(1, value.getLanguages().size());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullMap() {
        new MultilingualString((Map<String, String>) null);
    }

    @Test(expected = NullPointerException.class)
    public void setLanguageRejectsNullValue() {
        new MultilingualString().set("en", null);
    }

    @Test(expected = NullPointerException.class)
    public void setSimpleRejectsNullValue() {
        new MultilingualString().set((String) null);
    }
}
