package org.arquillian.algeron.pact.provider.core;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class StateTypeConverter_ArrowTest {

    private enum Example {
        VALUE
    }

    @Test
    public void convertsString() {
        assertEquals("hello", StateTypeConverter.convert("hello", String.class));
    }

    @Test
    public void preservesNullString() {
        assertNull(StateTypeConverter.convert(null, String.class));
    }

    @Test
    public void convertsIntegerAndPrimitiveInteger() {
        assertEquals(Integer.valueOf(12), StateTypeConverter.convert("12", Integer.class));
        assertEquals(Integer.valueOf(12), StateTypeConverter.convert("12", int.class));
    }

    @Test
    public void convertsDoubleAndPrimitiveDouble() {
        assertEquals(Double.valueOf(2.5), StateTypeConverter.convert("2.5", Double.class));
        assertEquals(Double.valueOf(2.5), StateTypeConverter.convert("2.5", double.class));
    }

    @Test
    public void convertsLongWrapper() {
        assertEquals(Long.valueOf(12L), StateTypeConverter.convert("12", Long.class));
    }

    @Test
    public void convertsBooleanAndPrimitiveBoolean() {
        assertEquals(Boolean.TRUE, StateTypeConverter.convert("true", Boolean.class));
        assertEquals(Boolean.TRUE, StateTypeConverter.convert("true", boolean.class));
    }

    @Test
    public void returnsNullForNullNonStringType() {
        assertNull(StateTypeConverter.convert(null, Integer.class));
    }

    @Test
    public void convertsUrl() throws Exception {
        URL expected = new URL("http://example.com/path");
        assertEquals(expected, StateTypeConverter.convert("http://example.com/path", URL.class));
    }

    @Test
    public void convertsUri() throws Exception {
        URI expected = new URI("http://example.com/path");
        assertEquals(expected, StateTypeConverter.convert("http://example.com/path", URI.class));
    }

    @Test
    public void convertsEnumNameIgnoringCaseAndPackagePrefix() {
        assertEquals(Example.VALUE, StateTypeConverter.convert("some.package.VALUE", Example.class));
    }

    @Test
    public void convertsCommaSeparatedStringArrayAndTrimsElements() {
        assertArrayEquals(new String[] {"one", "two"}, StateTypeConverter.convert(" one, two ", String[].class));
    }

    @Test
    public void convertsBlankStringArrayToEmptyArray() {
        assertArrayEquals(new String[0], StateTypeConverter.convert("   ", String[].class));
    }

    @Test
    public void convertsCommaSeparatedCollectionAndTrimsElements() {
        List<String> result = StateTypeConverter.convert(" one, two ", List.class);
        assertEquals(Arrays.asList("one", "two"), result);
    }

    @Test
    public void convertsBlankCollectionToEmptyCollection() {
        assertEquals(Collections.emptyList(), StateTypeConverter.convert("   ", List.class));
    }

    @Test
    public void convertsCharset() {
        assertSame(Charset.forName("UTF-8"), StateTypeConverter.convert("utf-8", Charset.class));
    }

    @Test
    public void convertsClass() {
        assertEquals(String.class, StateTypeConverter.convert("java.lang.String", Class.class));
    }

    @Test
    public void convertsInstantiableClass() {
        Object result = StateTypeConverter.convert("java.lang.StringBuilder", Object.class);
        assertTrue(result instanceof StringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedUri() {
        StateTypeConverter.convert("http://example.com/invalid path", URI.class);
    }
}
