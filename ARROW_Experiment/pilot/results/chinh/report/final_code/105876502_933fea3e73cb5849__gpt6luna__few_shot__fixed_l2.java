package org.ec4j.core.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PropertyType_ArrowTest {

    @Test
    public void standardTypesContainsAllWidelySupportedTypes() {
        Set<PropertyType<?>> types = PropertyType.standardTypes();
        assertEquals(8, types.size());
        assertTrue(types.contains(PropertyType.charset));
        assertTrue(types.contains(PropertyType.end_of_line));
        assertTrue(types.contains(PropertyType.indent_size));
        assertTrue(types.contains(PropertyType.indent_style));
        assertTrue(types.contains(PropertyType.insert_final_newline));
        assertTrue(types.contains(PropertyType.root));
        assertTrue(types.contains(PropertyType.tab_width));
        assertTrue(types.contains(PropertyType.trim_trailing_whitespace));
    }

    @Test
    public void standardTypesIsUnmodifiable() {
        try {
            PropertyType.standardTypes().clear();
        } catch (UnsupportedOperationException expected) {
            assertEquals(8, PropertyType.standardTypes().size());
            return;
        }
        throw new AssertionError("Expected an unmodifiable set");
    }

    @Test
    public void constructorAndAccessorsPreserveValues() {
        Set<String> possibleValues = new LinkedHashSet<String>(Arrays.asList("one", "two"));
        PropertyType<String> type = new PropertyType<String>("sample", "description",
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, possibleValues);

        assertEquals("sample", type.getName());
        assertEquals("description", type.getDescription());
        assertSame(possibleValues, type.getPossibleValues());
        assertEquals("sample", type.toString());
    }

    @Test
    public void varargsConstructorCreatesPossibleValuesSet() {
        PropertyType<String> type = new PropertyType<String>("sample", "description",
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "one", "two", "one");

        assertEquals(new LinkedHashSet<String>(Arrays.asList("one", "two")), type.getPossibleValues());
    }

    @Test
    public void normalizeIfNeededReturnsOriginalValue() {
        PropertyType<String> type = new PropertyType<String>("sample", "description",
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER);
        String value = new String("MixedCase");

        assertSame(value, type.normalizeIfNeeded(value));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void lowerCasingNormalizeIfNeededUsesUsLocaleRules() {
        PropertyType.LowerCasingPropertyType<String> type =
                new PropertyType.LowerCasingPropertyType<String>("sample", "description",
                        PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER);

        assertEquals("mixed", type.normalizeIfNeeded("MIXED"));
        assertEquals("i", type.normalizeIfNeeded("I"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void identityParserReturnsSourceAsParsedValue() {
        PropertyType.PropertyValue<String> result = PropertyType.charset.parse("UTF-8");

        assertTrue(result.isValid());
        assertEquals("UTF-8", result.getSource());
        assertEquals("UTF-8", result.getParsed());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void booleanParserAcceptsTrueIgnoringCase() {
        PropertyType.PropertyValue<Boolean> result = PropertyType.insert_final_newline.parse("TrUe");

        assertTrue(result.isValid());
        assertEquals(Boolean.TRUE, result.getParsed());
        assertEquals("TrUe", result.getSource());
    }

    @Test
    public void booleanParserRejectsNull() {
        PropertyType.PropertyValue<Boolean> result = PropertyType.root.parse(null);

        assertFalse(result.isValid());
        assertNull(result.getParsed());
        assertEquals("Property 'root' expects a boolean; found: null", result.getErrorMessage());
    }

    @Test
    public void positiveIntegerParserAcceptsPositiveInteger() {
        PropertyType.PropertyValue<Integer> result = PropertyType.tab_width.parse("12");

        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(12), result.getParsed());
    }

    @Test
    public void positiveIntegerParserRejectsZero() {
        PropertyType.PropertyValue<Integer> result = PropertyType.tab_width.parse("0");

        assertFalse(result.isValid());
        assertEquals("Property 'tab_width' expects a positive integer; found '0'", result.getErrorMessage());
    }

    @Test
    public void positiveIntegerParserRejectsNonInteger() {
        PropertyType.PropertyValue<Integer> result = PropertyType.tab_width.parse("abc");

        assertFalse(result.isValid());
        assertEquals("Property 'tab_width' expects an integer. The parsed 'abc' is not an integer.",
                result.getErrorMessage());
    }

    @Test
    public void indentSizeParserAcceptsTabWithNullParsedValue() {
        PropertyType.PropertyValue<Integer> result = PropertyType.indent_size.parse("TAB");

        assertTrue(result.isValid());
        assertNull(result.getParsed());
        assertEquals("TAB", result.getSource());
    }

    @Test
    public void enumParserParsesEndOfLineValue() {
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = PropertyType.end_of_line.parse("CRLF");

        assertTrue(result.isValid());
        assertEquals(PropertyType.EndOfLineValue.crlf, result.getParsed());
    }

    @Test
    public void enumParserRejectsUnknownValue() {
        PropertyType.PropertyValue<PropertyType.IndentStyleValue> result =
                PropertyType.indent_style.parse("unknown");

        assertFalse(result.isValid());
        assertEquals("Unexpected parsed \"unknown\" for enum " + PropertyType.IndentStyleValue.class.getName(),
                result.getErrorMessage());
    }

    @Test
    public void endOfLineValueMapsStringsAndExposesValueSet() {
        assertEquals(PropertyType.EndOfLineValue.cr,
                PropertyType.EndOfLineValue.ofEndOfLineString("\r"));
        assertEquals(PropertyType.EndOfLineValue.crlf,
                PropertyType.EndOfLineValue.ofEndOfLineString("\r\n"));
        assertEquals(PropertyType.EndOfLineValue.lf,
                PropertyType.EndOfLineValue.ofEndOfLineString("\n"));
        assertNull(PropertyType.EndOfLineValue.ofEndOfLineString("invalid"));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("cr", "crlf", "lf")),
                PropertyType.EndOfLineValue.valueSet());
    }

    @Test
    public void indentStyleValuesExposeCharactersAndNames() {
        assertEquals(' ', PropertyType.IndentStyleValue.space.getIndentChar());
        assertEquals('\t', PropertyType.IndentStyleValue.tab.getIndentChar());
        assertEquals(new LinkedHashSet<String>(Arrays.asList("space", "tab")),
                PropertyType.IndentStyleValue.valueSet());
    }

    @Test
    public void propertyValuesCompareByContents() {
        PropertyType.PropertyValue<String> first = PropertyType.PropertyValue.valid("source", "parsed");
        PropertyType.PropertyValue<String> equal = PropertyType.PropertyValue.valid("source", "parsed");
        PropertyType.PropertyValue<String> different = PropertyType.PropertyValue.valid("other", "parsed");

        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertNotSame(first, equal);
        assertFalse(first.equals(different));
        assertEquals("PropertyValue [errorMessage=null, parsed=parsed, source=source]", first.toString());
    }

    @Test
    public void invalidPropertyValueExposesErrorAndSource() {
        PropertyType.PropertyValue<String> result = PropertyType.PropertyValue.invalid("bad", "invalid value");

        assertFalse(result.isValid());
        assertEquals("bad", result.getSource());
        assertNull(result.getParsed());
        assertEquals("invalid value", result.getErrorMessage());
    }

    @Test
    public void enumParserRejectsNullValue() {
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = PropertyType.end_of_line.parse(null);

        assertFalse(result.isValid());
        assertNull(result.getParsed());
        assertEquals("Cannot make enum " + PropertyType.EndOfLineValue.class.getName() + " out of null",
                result.getErrorMessage());
    }

    @Test
    public void endOfLineValueRejectsNullString() {
        assertNull(PropertyType.EndOfLineValue.ofEndOfLineString(null));
    }
}
