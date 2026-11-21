package org.arquillian.algeron.pact.provider.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class StateTypeConverter_ArrowTest {

    enum TestEnum {
        VALUE1, VALUE2
    }

    @Test
    public void testConvertStringToString() {
        String result = StateTypeConverter.convert("test", String.class);
        assertEquals("test", result);
    }

    @Test
    public void testConvertStringToInteger() {
        Integer result = StateTypeConverter.convert("123", Integer.class);
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testConvertStringToInt() {
        Integer result = StateTypeConverter.convert("456", int.class);
        assertEquals(Integer.valueOf(456), result);
    }

    @Test
    public void testConvertStringToDouble() {
        Double result = StateTypeConverter.convert("123.45", Double.class);
        assertEquals(Double.valueOf(123.45), result);
    }

    @Test
    public void testConvertStringToDoublePrimitive() {
        Double result = StateTypeConverter.convert("678.90", double.class);
        assertEquals(Double.valueOf(678.90), result);
    }

    @Test
    public void testConvertStringToLong() {
        Long result = StateTypeConverter.convert("123456", Long.class);
        assertEquals(Long.valueOf(123456), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertStringToLongPrimitive() {
        StateTypeConverter.convert("789012", long.class);
    }

    @Test
    public void testConvertStringToBoolean() {
        Boolean result = StateTypeConverter.convert("true", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testConvertStringToBooleanPrimitive() {
        Boolean result = StateTypeConverter.convert("false", boolean.class);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testConvertStringToURL() throws MalformedURLException {
        URL result = StateTypeConverter.convert("http://example.com", URL.class);
        assertEquals(new URL("http://example.com"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertStringToURLInvalid() {
        StateTypeConverter.convert("invalid-url", URL.class);
    }

    @Test
    public void testConvertStringToURI() throws URISyntaxException {
        URI result = StateTypeConverter.convert("http://example.com", URI.class);
        assertEquals(new URI("http://example.com"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertStringToURIInvalid() {
        StateTypeConverter.convert("invalid-uri", URI.class);
    }

    @Test
    public void testConvertStringToEnum() {
        TestEnum result = StateTypeConverter.convert("value1", TestEnum.class);
        assertEquals(TestEnum.VALUE1, result);
    }

    @Test
    public void testConvertStringToEnumWithFullyQualifiedName() {
        TestEnum result = StateTypeConverter.convert("org.arquillian.algeron.pact.provider.core.StateTypeConverter_ArrowTest$TestEnum.VALUE2", TestEnum.class);
        assertEquals(TestEnum.VALUE2, result);
    }

    @Test
    public void testConvertStringToStringArray() {
        String[] result = StateTypeConverter.convert("a,b,c", String[].class);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testConvertStringToStringArrayEmpty() {
        String[] result = StateTypeConverter.convert("", String[].class);
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void testConvertStringToStringArrayWithSpaces() {
        String[] result = StateTypeConverter.convert(" a , b , c ", String[].class);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testConvertStringToStringArrayOnlyBlanks() {
        String[] result = StateTypeConverter.convert("   ,   ,   ", String[].class);
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void testConvertStringToCollection() {
        Collection<String> result = StateTypeConverter.convert("a,b,c", Collection.class);
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    public void testConvertStringToCollectionEmpty() {
        Collection<String> result = StateTypeConverter.convert("", Collection.class);
        assertEquals(new ArrayList<>(), result);
    }

    @Test
    public void testConvertStringToCollectionWithSpaces() {
        Collection<String> result = StateTypeConverter.convert(" a , b , c ", Collection.class);
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    public void testConvertStringToCharset() {
        Charset result = StateTypeConverter.convert("utf-8", Charset.class);
        assertEquals(Charset.forName("UTF-8"), result);
    }

    @Test
    public void testConvertStringToClass() throws Exception {
        Class<?> result = StateTypeConverter.convert("java.lang.String", Class.class);
        assertEquals(String.class, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertStringToClassNotFound() {
        StateTypeConverter.convert("non.existent.Class", Class.class);
    }

    @Test
    public void testConvertStringToClassViaReflection() throws Exception {
        Class<?> result = StateTypeConverter.convert("java.util.ArrayList", Class.class);
        assertEquals(ArrayList.class, result);
    }

    @Test
    public void testConvertNullStringToNonString() {
        assertNull(StateTypeConverter.convert(null, Integer.class));
    }

    @Test
    public void testConvertNullStringToString() {
        String result = StateTypeConverter.convert(null, String.class);
        assertNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNullStringToStringArray() {
        StateTypeConverter.convert(null, String[].class);
    }

    @Test
    public void testExtractEnumNameSimple() {
        TestEnum result = StateTypeConverter.convert("value1", TestEnum.class);
        assertEquals(TestEnum.VALUE1, result);
    }

    @Test
    public void testExtractEnumNameFullyQualified() {
        TestEnum result = StateTypeConverter.convert("org.arquillian.algeron.pact.provider.core.StateTypeConverter_ArrowTest$TestEnum.VALUE2", TestEnum.class);
        assertEquals(TestEnum.VALUE2, result);
    }

    @Test
    public void testTrimElements() {
        String[] result = StateTypeConverter.convert(" a , b , c ", String[].class);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testHasOnlyBlanksTrue() {
        String[] result = StateTypeConverter.convert("   ,   ,   ", String[].class);
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void testHasOnlyBlanksFalse() {
        String[] result = StateTypeConverter.convert("   , a ,   ", String[].class);
        assertArrayEquals(new String[]{"", "a", ""}, result);
    }

    @Test
    public void testIsBlankTrue() {
        String[] result = StateTypeConverter.convert("   ,   ,   ", String[].class);
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void testIsBlankFalse() {
        String[] result = StateTypeConverter.convert("   , a ,   ", String[].class);
        assertArrayEquals(new String[]{"", "a", ""}, result);
    }
}
