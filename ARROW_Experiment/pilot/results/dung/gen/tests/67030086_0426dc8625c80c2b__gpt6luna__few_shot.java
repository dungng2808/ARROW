package org.arquillian.algeron.pact.provider.core;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StateTypeConverter_ArrowTest {

    private enum Status {
        READY
    }

    @Test
    public void convertsStringAndNullString() {
        assertEquals("hello", StateTypeConverter.convert("hello", String.class));
        assertNull(StateTypeConverter.convert(null, String.class));
    }

    @Test
    public void convertsNumericAndBooleanTypes() {
        assertEquals(Integer.valueOf(12), StateTypeConverter.convert("12", Integer.class));
        assertEquals(Integer.valueOf(12), StateTypeConverter.convert("12", int.class));
        assertEquals(Double.valueOf(2.5), StateTypeConverter.convert("2.5", Double.class));
        assertEquals(Double.valueOf(2.5), StateTypeConverter.convert("2.5", double.class));
        assertEquals(Long.valueOf(42L), StateTypeConverter.convert("42", Long.class));
        assertEquals(Boolean.TRUE, StateTypeConverter.convert("true", Boolean.class));
        assertEquals(Boolean.FALSE, StateTypeConverter.convert("false", boolean.class));
    }

    @Test
    public void returnsNullForNullNonStringType() {
        assertNull(StateTypeConverter.convert(null, Integer.class));
    }

    @Test
    public void convertsUrlAndUri() {
        URL url = StateTypeConverter.convert("http://example.com/path", URL.class);
        URI uri = StateTypeConverter.convert("http://example.com/path", URI.class);

        assertEquals("http://example.com/path", url.toString());
        assertEquals(URI.create("http://example.com/path"), uri);
    }

    @Test
    public void convertsEnumAndCharset() {
        assertEquals(Status.READY, StateTypeConverter.convert("org.example.Status.READY", Status.class));
        assertEquals(Charset.forName("UTF-8"), StateTypeConverter.convert("utf-8", Charset.class));
    }

    @Test
    public void convertsCommaSeparatedStringArray() {
        assertArrayEquals(new String[]{"one", "two"}, StateTypeConverter.convert(" one, two ", String[].class));
        assertArrayEquals(new String[0], StateTypeConverter.convert("   ", String[].class));
    }

    @Test
    public void convertsCommaSeparatedCollection() {
        Collection<?> values = StateTypeConverter.convert(" one, two ", Collection.class);
        assertEquals(Arrays.asList("one", "two"), values);
        assertTrue(StateTypeConverter.convert("   ", Collection.class).isEmpty());
    }

    @Test
    public void convertsClassByName() {
        assertEquals(String.class, StateTypeConverter.convert("java.lang.String", Class.class));
    }

    @Test
    public void rejectsMalformedUri() {
        try {
            StateTypeConverter.convert("http://example.com/a b", URI.class);
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unable to convert value"));
            return;
        }
        assertFalse(true);
    }
}
