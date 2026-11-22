package org.ec4j.core.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PropertyType_ArrowTest {

    @Test
    public void standardTypesContainsEightTypes() {
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
    public void exposesConstructorValuesAndStringForm() {
        Set<String> values = new LinkedHashSet<String>(Arrays.asList("one", "two"));
        PropertyType<String> type = new PropertyType<String>("sample", "description",
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, values);

        assertEquals("sample", type.getName());
        assertEquals("description", type.getDescription());
        assertEquals(values, type.getPossibleValues());
        assertEquals("sample", type.toString());
    }

    @Test
    public void varargsConstructorRemovesDuplicatePossibleValues() {
        PropertyType<String> type = new PropertyType<String>("sample", "description",
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "one", "two", "one");

        assertEquals(new LinkedHashSet<String>(Arrays.asList("one", "two")), type.getPossibleValues());
    }

    @Test
    public void defaultNormalizationReturnsOriginalValue() {
        PropertyType<String> type = new PropertyType<String>("sample", "description",
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER);

        assertSame("Value should be returned unchanged", "Mixed", type.normalizeIfNeeded("Mixed"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void lowerCasingNormalizationUsesUsLocaleRules() {
        PropertyType.LowerCasingPropertyType<String> type =
                new PropertyType.LowerCasingPropertyType<String>("sample", "description",
                        PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER,
                        (Set<String>) null);

        assertEquals("mixed", type.normalizeIfNeeded("MIXED"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void identityParserReturnsSourceAsParsedValue() {
        PropertyType.PropertyValue<String> result = PropertyType.charset.parse("Custom");

        assertTrue(result.isValid());
        assertEquals("Custom", result.getSource());
        assertEquals("Custom", result.getParsed());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void booleanParserAcceptsCaseInsensitiveTrue() {
        PropertyType.PropertyValue<Boolean> result = PropertyType.root.parse("TrUe");

        assertTrue(result.isValid());
        assertEquals(Boolean.TRUE, result.getParsed());
        assertEquals("TrUe", result.getSource());
    }

    @Test
    public void booleanParserReportsInvalidValue() {
        PropertyType.PropertyValue<Boolean> result = PropertyType.root.parse("yes");

        assertFalse(result.isValid());
        assertNull(result.getParsed());
        assertEquals("yes", result.getSource());
        assertEquals("Property 'root' expects a boolean. The parsed 'yes' is not a boolean.",
                result.getErrorMessage());
    }

    @Test
    public void booleanParserReportsNullValue() {
        PropertyType.PropertyValue<Boolean> result = PropertyType.root.parse(null);

        assertFalse(result.isValid());
        assertNull(result.getParsed());
        assertNull(result.getSource());
        assertEquals("Property 'root' expects a boolean; found: null", result.getErrorMessage());
    }

    @Test
    public void positiveIntegerParserAcceptsPositiveInteger() {
        PropertyType.PropertyValue<Integer> result = PropertyType.tab_width.parse("12");

        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(12), result.getParsed());
        assertEquals("12", result.getSource());
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
    public void indentSizeAcceptsTabWithNullParsedValue() {
        PropertyType.PropertyValue<Integer> result = PropertyType.indent_size.parse("TAB");

        assertTrue(result.isValid());
        assertNull(result.getParsed());
        assertEquals("TAB", result.getSource());
    }

    @Test
    public void enumParserParsesEndOfLine() {
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = PropertyType.end_of_line.parse("crlf");

        assertTrue(result.isValid());
        assertEquals(PropertyType.EndOfLineValue.crlf, result.getParsed());
    }

    @Test
    public void enumParserReportsUnexpectedValue() {
        PropertyType.PropertyValue<PropertyType.IndentStyleValue> result = PropertyType.indent_style.parse("spaces");

        assertFalse(result.isValid());
        assertEquals("Unexpected parsed \"spaces\" for enum org.ec4j.core.model.PropertyType$IndentStyleValue",
                result.getErrorMessage());
    }

    @Test
    public void enumParserReportsNullValue() {
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = PropertyType.end_of_line.parse(null);

        assertFalse(result.isValid());
        assertEquals("Cannot make enum org.ec4j.core.model.PropertyType$EndOfLineValue out of null",
                result.getErrorMessage());
    }

    @Test
    public void endOfLineValueRecognizesLineEndings() {
        assertEquals(PropertyType.EndOfLineValue.cr,
                PropertyType.EndOfLineValue.ofEndOfLineString("\r"));
        assertEquals(PropertyType.EndOfLineValue.crlf,
                PropertyType.EndOfLineValue.ofEndOfLineString("\r\n"));
        assertEquals(PropertyType.EndOfLineValue.lf,
                PropertyType.EndOfLineValue.ofEndOfLineString("\n"));
        assertNull(PropertyType.EndOfLineValue.ofEndOfLineString("other"));
    }

    @Test
    public void endOfLineValueSetContainsNames() {
        assertEquals(new LinkedHashSet<String>(Arrays.asList("cr", "crlf", "lf")),
                PropertyType.EndOfLineValue.valueSet());
    }

    @Test
    public void indentStyleValueSetContainsNamesAndCharacters() {
        assertEquals(new LinkedHashSet<String>(Arrays.asList("space", "tab")),
                PropertyType.IndentStyleValue.valueSet());
        assertEquals(' ', PropertyType.IndentStyleValue.space.getIndentChar());
        assertEquals('\t', PropertyType.IndentStyleValue.tab.getIndentChar());
    }

    @Test
    public void endOfLineEnumExposesEndOfLineStrings() {
        assertEquals("\r", PropertyType.EndOfLineValue.cr.getEndOfLineString());
        assertEquals("\r\n", PropertyType.EndOfLineValue.crlf.getEndOfLineString());
        assertEquals("\n", PropertyType.EndOfLineValue.lf.getEndOfLineString());
    }

    @Test
    public void propertyValuesExposeFieldsAndEquality() {
        PropertyType.PropertyValue<String> first = PropertyType.PropertyValue.valid("source", "parsed");
        PropertyType.PropertyValue<String> same = PropertyType.PropertyValue.valid("source", "parsed");
        PropertyType.PropertyValue<String> different = PropertyType.PropertyValue.valid("other", "parsed");

        assertEquals("source", first.getSource());
        assertEquals("parsed", first.getParsed());
        assertNull(first.getErrorMessage());
        assertTrue(first.isValid());
        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
        assertEquals("PropertyValue [errorMessage=null, parsed=parsed, source=source]", first.toString());
    }

    @Test
    public void invalidPropertyValueExposesError() {
        PropertyType.PropertyValue<String> result = PropertyType.PropertyValue.invalid("input", "bad");

        assertEquals("input", result.getSource());
        assertNull(result.getParsed());
        assertEquals("bad", result.getErrorMessage());
        assertFalse(result.isValid());
    }

    @Test
    public void endOfLineLookupReturnsNullForUnrecognizedValue() {
        assertNull(PropertyType.EndOfLineValue.ofEndOfLineString("other"));
    }

    @Test(expected = NullPointerException.class)
    public void endOfLineLookupRejectsNull() {
        PropertyType.EndOfLineValue.ofEndOfLineString(null);
    }
}
