package org.arquillian.algeron.pact.provider.core;

import org.junit.Test;
import org.junit.Before;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class StateTypeConverter_ArrowTest {

    @Test
    public void shouldConvertStringToClass() {
        String value = "test";
        Class<String> to = String.class;
        String result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(value);
    }

    @Test
    public void shouldConvertStringToInteger() {
        String value = "123";
        Class<Integer> to = Integer.class;
        Integer result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(123);
    }

    @Test
    public void shouldConvertStringToInt() {
        String value = "123";
        Class<Integer> to = int.class;
        Integer result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(123);
    }

    @Test
    public void shouldConvertStringToDouble() {
        String value = "123.45";
        Class<Double> to = Double.class;
        Double result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(123.45);
    }

    @Test
    public void shouldConvertStringToDoublePrimitive() {
        String value = "123.45";
        Class<Double> to = double.class;
        Double result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(123.45);
    }

    @Test
    public void shouldConvertStringToLong() {
        String value = "123456789";
        Class<Long> to = Long.class;
        Long result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(123456789L);
    }

    @Test
    public void shouldConvertStringToLongPrimitive() {
        String value = "123456789";
        Class<Long> to = long.class;
        Long result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(123456789L);
    }

    @Test
    public void shouldConvertStringToBoolean() {
        String value = "true";
        Class<Boolean> to = Boolean.class;
        Boolean result = StateTypeConverter.convert(value, to);
        assertThat(result).isTrue();
    }

    @Test
    public void shouldConvertStringToBooleanPrimitive() {
        String value = "false";
        Class<Boolean> to = boolean.class;
        Boolean result = StateTypeConverter.convert(value, to);
        assertThat(result).isFalse();
    }

    @Test
    public void shouldConvertStringToURL() throws MalformedURLException {
        String value = "http://example.com";
        Class<URL> to = URL.class;
        URL result = StateTypeConverter.convert(value, to);
        assertThat(result.toString()).isEqualTo(value);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenConvertingInvalidURL() {
        String value = "invalid-url";
        Class<URL> to = URL.class;
        assertThatThrownBy(() -> StateTypeConverter.convert(value, to))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to convert value " + value + " to URL");
    }

    @Test
    public void shouldConvertStringToURI() throws URISyntaxException {
        String value = "http://example.com";
        Class<URI> to = URI.class;
        URI result = StateTypeConverter.convert(value, to);
        assertThat(result.toString()).isEqualTo(value);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenConvertingInvalidURI() {
        String value = "invalid-uri";
        Class<URI> to = URI.class;
        assertThatThrownBy(() -> StateTypeConverter.convert(value, to))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to convert value " + value + " to URI");
    }

    @Test
    public void shouldConvertStringToEnum() {
        String value = "one";
        Class<TestEnum> to = TestEnum.class;
        TestEnum result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(TestEnum.ONE);
    }

    @Test
    public void shouldConvertStringToFullyQualifiedEnum() {
        String value = "org.arquillian.algeron.pact.provider.core.StateTypeConverter_ArrowTest$TestEnum.TWO";
        Class<TestEnum> to = TestEnum.class;
        TestEnum result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(TestEnum.TWO);
    }

    @Test
    public void shouldConvertStringToArrayOfStrings() {
        String value = "a,b,c";
        Class<String[]> to = String[].class;
        String[] result = StateTypeConverter.convert(value, to);
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldConvertEmptyStringToArrayOfStrings() {
        String value = "";
        Class<String[]> to = String[].class;
        String[] result = StateTypeConverter.convert(value, to);
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldConvertStringWithSpacesToArrayOfStrings() {
        String value = " a , b , c ";
        Class<String[]> to = String[].class;
        String[] result = StateTypeConverter.convert(value, to);
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldConvertStringWithOnlyBlanksToArrayOfStrings() {
        String value = "   ,   ,   ";
        Class<String[]> to = String[].class;
        String[] result = StateTypeConverter.convert(value, to);
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldConvertStringToCollection() {
        String value = "a,b,c";
        Class<Collection> to = Collection.class;
        Collection result = StateTypeConverter.convert(value, to);
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldConvertStringToCharset() {
        String value = "utf-8";
        Class<Charset> to = Charset.class;
        Charset result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(Charset.forName("UTF-8"));
    }

    @Test
    public void shouldConvertStringToClassType() throws Exception {
        String value = "java.lang.String";
        Class<Class> to = Class.class;
        Class result = StateTypeConverter.convert(value, to);
        assertThat(result).isEqualTo(String.class);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenConvertingInvalidClass() {
        String value = "non.existent.Class";
        Class<Class> to = Class.class;
        assertThatThrownBy(() -> StateTypeConverter.convert(value, to))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to find class [" + value + "]");
    }

    @Test
    public void shouldReturnNullForNullValueAndNonStringType() {
        String value = null;
        Class<Integer> to = Integer.class;
        Integer result = StateTypeConverter.convert(value, to);
        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnNullForNullValueAndStringType() {
        String value = null;
        Class<String> to = String.class;
        String result = StateTypeConverter.convert(value, to);
        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnNullForNullValueAndStringArrayType() {
        String value = null;
        Class<String[]> to = String[].class;
        String[] result = StateTypeConverter.convert(value, to);
        assertThat(result).isNull();
    }

    enum TestEnum {
        ONE, TWO
    }
}
