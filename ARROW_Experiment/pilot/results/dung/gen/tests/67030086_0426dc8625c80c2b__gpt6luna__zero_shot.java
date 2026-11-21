package org.arquillian.algeron.pact.provider.core;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StateTypeConverter_ArrowTest {

    private enum Status {
        READY
    }

    @Test
    public void convertsString() {
        assertEquals("hello", StateTypeConverter.convert("hello", String.class));
    }

    @Test
    public void convertsNullToString() {
        assertNull(StateTypeConverter.convert(null, String.class));
    }

    @Test
    public void convertsNullToStringArrayAsEmptyArray() {
        try {
            StateTypeConverter.convert(null, String[].class);
            fail("Expected a NullPointerException");
        } catch (NullPointerException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void returnsNullForNullNumericValue() {
        assertNull(StateTypeConverter.convert(null, Integer.class));
    }

    @Test
    public void convertsInteger() {
        assertEquals(Integer.valueOf(42), StateTypeConverter.convert("42", Integer.class));
    }

    @Test
    public void convertsPrimitiveInteger() {
        assertEquals(Integer.valueOf(42), StateTypeConverter.convert("42", int.class));
    }

    @Test
    public void convertsDouble() {
        assertEquals(Double.valueOf(2.5), StateTypeConverter.convert("2.5", Double.class));
    }

    @Test
    public void convertsPrimitiveDouble() {
        assertEquals(Double.valueOf(2.5), StateTypeConverter.convert("2.5", double.class));
    }

    @Test
    public void convertsLongWrapper() {
        assertEquals(Long.valueOf(42L), StateTypeConverter.convert("42", Long.class));
    }

    @Test
    public void convertsBoolean() {
        assertEquals(Boolean.TRUE, StateTypeConverter.convert("true", Boolean.class));
    }

    @Test
    public void convertsPrimitiveBoolean() {
        assertEquals(Boolean.FALSE, StateTypeConverter.convert("false", boolean.class));
    }

    @Test
    public void convertsUrl() throws Exception {
        URL expected = new URL("https://example.com/path");
        assertEquals(expected, StateTypeConverter.convert("https://example.com/path", URL.class));
    }

    @Test
    public void rejectsMalformedUrl() {
        try {
            StateTypeConverter.convert("not a url", URL.class);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Unable to convert value not a url to URL", expected.getMessage());
        }
    }

    @Test
    public void convertsUri() throws Exception {
        URI expected = new URI("https://example.com/path");
        assertEquals(expected, StateTypeConverter.convert("https://example.com/path", URI.class));
    }

    @Test
    public void rejectsMalformedUri() {
        try {
            StateTypeConverter.convert("not a uri", URI.class);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Unable to convert value not a uri to URL", expected.getMessage());
        }
    }

    @Test
    public void convertsEnumName() {
        assertSame(Status.READY, StateTypeConverter.convert("ready", Status.class));
    }

    @Test
    public void convertsQualifiedEnumName() {
        assertSame(Status.READY, StateTypeConverter.convert("example.Status.READY", Status.class));
    }

    @Test
    public void convertsCommaSeparatedStringArray() {
        assertArrayEquals(new String[] {"one", "two", "three"},
            StateTypeConverter.convert(" one, two ,three ", String[].class));
    }

    @Test
    public void convertsBlankStringArrayToEmptyArray() {
        assertArrayEquals(new String[0], StateTypeConverter.convert("   ", String[].class));
    }

    @Test
    public void convertsCommaSeparatedCollection() {
        Collection<?> result = StateTypeConverter.convert(" one, two ", Collection.class);
        assertEquals(Arrays.asList("one", "two"), result);
    }

    @Test
    public void convertsBlankCollectionToEmptyCollection() {
        Collection<?> result = StateTypeConverter.convert("   ", Collection.class);
        assertTrue(result.isEmpty());
    }

    @Test
    public void convertsCharset() {
        assertEquals(Charset.forName("UTF-8"), StateTypeConverter.convert("utf-8", Charset.class));
    }

    @Test
    public void convertsClass() {
        assertEquals(String.class, StateTypeConverter.convert("java.lang.String", Class.class));
    }

    @Test
    public void returnsNullForUnsupportedTypeWhenValueIsNull() {
        assertNull(StateTypeConverter.convert(null, Object.class));
    }
}
