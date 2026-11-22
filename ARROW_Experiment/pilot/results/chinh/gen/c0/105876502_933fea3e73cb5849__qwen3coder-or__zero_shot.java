package org.ec4j.core.model;

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class PropertyType_ArrowTest {

    @Test
    public void testEndOfLineValueOfEndOfLineString() {
        assertEquals(PropertyType.EndOfLineValue.cr, PropertyType.EndOfLineValue.ofEndOfLineString("\r"));
        assertEquals(PropertyType.EndOfLineValue.crlf, PropertyType.EndOfLineValue.ofEndOfLineString("\r\n"));
        assertEquals(PropertyType.EndOfLineValue.lf, PropertyType.EndOfLineValue.ofEndOfLineString("\n"));
        assertNull(PropertyType.EndOfLineValue.ofEndOfLineString("invalid"));
    }

    @Test
    public void testEndOfLineValueValueSet() {
        Set<String> valueSet = PropertyType.EndOfLineValue.valueSet();
        assertEquals(3, valueSet.size());
        assertTrue(valueSet.contains("cr"));
        assertTrue(valueSet.contains("crlf"));
        assertTrue(valueSet.contains("lf"));
    }

    @Test
    public void testEndOfLineValueGetEndOfLineString() {
        assertEquals("\r", PropertyType.EndOfLineValue.cr.getEndOfLineString());
        assertEquals("\r\n", PropertyType.EndOfLineValue.crlf.getEndOfLineString());
        assertEquals("\n", PropertyType.EndOfLineValue.lf.getEndOfLineString());
    }

    @Test
    public void testIndentStyleValueValueSet() {
        Set<String> valueSet = PropertyType.IndentStyleValue.valueSet();
        assertEquals(2, valueSet.size());
        assertTrue(valueSet.contains("space"));
        assertTrue(valueSet.contains("tab"));
    }

    @Test
    public void testIndentStyleValueGetIndentChar() {
        assertEquals(' ', PropertyType.IndentStyleValue.space.getIndentChar());
        assertEquals('\t', PropertyType.IndentStyleValue.tab.getIndentChar());
    }

    @Test
    public void testLowerCasingPropertyTypeNormalizeIfNeeded() {
        PropertyType.LowerCasingPropertyType<String> type = 
            new PropertyType.LowerCasingPropertyType<>("test", "test", 
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER);
        
        assertEquals("hello", type.normalizeIfNeeded("HELLO"));
        assertEquals(null, type.normalizeIfNeeded(null));
    }

    @Test
    public void testPropertyValueInvalid() {
        PropertyType.PropertyValue<String> value = PropertyType.PropertyValue.invalid("source", "error");
        assertEquals("source", value.getSource());
        assertEquals(null, value.getParsed());
        assertEquals("error", value.getErrorMessage());
        assertFalse(value.isValid());
    }

    @Test
    public void testPropertyValueValid() {
        PropertyType.PropertyValue<String> value = PropertyType.PropertyValue.valid("source", "parsed");
        assertEquals("source", value.getSource());
        assertEquals("parsed", value.getParsed());
        assertEquals(null, value.getErrorMessage());
        assertTrue(value.isValid());
    }

    @Test
    public void testEnumValueParserParseNull() {
        PropertyType.PropertyValueParser<PropertyType.EndOfLineValue> parser = 
            new PropertyType.PropertyValueParser.EnumValueParser<>(PropertyType.EndOfLineValue.class);
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = parser.parse("test", null);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testEnumValueParserParseInvalid() {
        PropertyType.PropertyValueParser<PropertyType.EndOfLineValue> parser = 
            new PropertyType.PropertyValueParser.EnumValueParser<>(PropertyType.EndOfLineValue.class);
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = parser.parse("test", "invalid");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testEnumValueParserParseValid() {
        PropertyType.PropertyValueParser<PropertyType.EndOfLineValue> parser = 
            new PropertyType.PropertyValueParser.EnumValueParser<>(PropertyType.EndOfLineValue.class);
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = parser.parse("test", "cr");
        assertTrue(result.isValid());
        assertEquals(PropertyType.EndOfLineValue.cr, result.getParsed());
    }

    @Test
    public void testBooleanValueParserParseNull() {
        PropertyType.PropertyValueParser<Boolean> parser = PropertyType.PropertyValueParser.BOOLEAN_VALUE_PARSER;
        PropertyType.PropertyValue<Boolean> result = parser.parse("test", null);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testBooleanValueParserParseTrue() {
        PropertyType.PropertyValueParser<Boolean> parser = PropertyType.PropertyValueParser.BOOLEAN_VALUE_PARSER;
        PropertyType.PropertyValue<Boolean> result = parser.parse("test", "true");
        assertTrue(result.isValid());
        assertEquals(Boolean.TRUE, result.getParsed());
    }

    @Test
    public void testBooleanValueParserParseFalse() {
        PropertyType.PropertyValueParser<Boolean> parser = PropertyType.PropertyValueParser.BOOLEAN_VALUE_PARSER;
        PropertyType.PropertyValue<Boolean> result = parser.parse("test", "false");
        assertTrue(result.isValid());
        assertEquals(Boolean.FALSE, result.getParsed());
    }

    @Test
    public void testBooleanValueParserParseInvalid() {
        PropertyType.PropertyValueParser<Boolean> parser = PropertyType.PropertyValueParser.BOOLEAN_VALUE_PARSER;
        PropertyType.PropertyValue<Boolean> result = parser.parse("test", "invalid");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testIdentityValueParserParse() {
        PropertyType.PropertyValueParser<String> parser = PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER;
        PropertyType.PropertyValue<String> result = parser.parse("test", "value");
        assertTrue(result.isValid());
        assertEquals("value", result.getParsed());
    }

    @Test
    public void testIndentSizeValueParserParseTab() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.INDENT_SIZE_VALUE_PARSER;
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "tab");
        assertTrue(result.isValid());
        assertNull(result.getParsed());
    }

    @Test
    public void testIndentSizeValueParserParseNumber() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.INDENT_SIZE_VALUE_PARSER;
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "4");
        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(4), result.getParsed());
    }

    @Test
    public void testPositiveIntValueParserParseNegative() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "-1");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testPositiveIntValueParserParseZero() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "0");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testPositiveIntValueParserParseValid() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "5");
        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(5), result.getParsed());
    }

    @Test
    public void testPositiveIntValueParserParseInvalid() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "invalid");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testStandardTypes() {
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
    public void testPropertyTypeConstructorWithSet() {
        Set<String> possibleValues = PropertyType.toSet(new String[]{"a", "b"});
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, possibleValues);
        assertEquals("name", type.getName());
        assertEquals("desc", type.getDescription());
        assertEquals(possibleValues, type.getPossibleValues());
    }

    @Test
    public void testPropertyTypeConstructorWithVarargs() {
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "a", "b");
        assertEquals("name", type.getName());
        assertEquals("desc", type.getDescription());
        assertEquals(2, type.getPossibleValues().size());
    }

    @Test
    public void testPropertyTypeGetDescription() {
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "a");
        assertEquals("desc", type.getDescription());
    }

    @Test
    public void testPropertyTypeGetName() {
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "a");
        assertEquals("name", type.getName());
    }

    @Test
    public void testPropertyTypeGetPossibleValues() {
        Set<String> possibleValues = PropertyType.toSet(new String[]{"a", "b"});
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, possibleValues);
        assertEquals(possibleValues, type.getPossibleValues());
    }

    @Test
    public void testPropertyTypeNormalizeIfNeeded() {
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "a");
        assertEquals("value", type.normalizeIfNeeded("value"));
        assertEquals(null, type.normalizeIfNeeded(null));
    }

    @Test
    public void testPropertyTypeParse() {
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "a");
        PropertyType.PropertyValue<String> result = type.parse("value");
        assertTrue(result.isValid());
        assertEquals("value", result.getParsed());
    }

    @Test
    public void testPropertyTypeToString() {
        PropertyType<String> type = new PropertyType<>("name", "desc", 
            PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "a");
        assertEquals("name", type.toString());
    }

    @Test
    public void testCharsetPropertyType() {
        assertEquals("charset", PropertyType.charset.getName());
        assertEquals("set to latin1, utf-8, utf-8-bom, utf-16be or utf-16le to control the character set. Use of utf-8-bom is discouraged.", 
            PropertyType.charset.getDescription());
        assertTrue(PropertyType.charset instanceof PropertyType.LowerCasingPropertyType);
    }

    @Test
    public void testEndOfLinePropertyType() {
        assertEquals("end_of_line", PropertyType.end_of_line.getName());
        assertEquals("set to lf, cr, or crlf to control how line breaks are represented.", 
            PropertyType.end_of_line.getDescription());
        assertTrue(PropertyType.end_of_line instanceof PropertyType.LowerCasingPropertyType);
    }

    @Test
    public void testIndentSizePropertyType() {
        assertEquals("indent_size", PropertyType.indent_size.getName());
        assertEquals("a whole number defining the number of columns used for each indentation level and the width of soft tabs (when supported). When set to tab, the parsed of tab_width (if specified) will be used.", 
            PropertyType.indent_size.getDescription());
        assertTrue(PropertyType.indent_size instanceof PropertyType.LowerCasingPropertyType);
    }

    @Test
    public void testIndentStylePropertyType() {
        assertEquals("indent_style", PropertyType.indent_style.getName());
        assertEquals("set to tab or space to use hard tabs or soft tabs respectively.", 
            PropertyType.indent_style.getDescription());
        assertTrue(PropertyType.indent_style instanceof PropertyType.LowerCasingPropertyType);
    }

    @Test
    public void testInsertFinalNewlinePropertyType() {
        assertEquals("insert_final_newline", PropertyType.insert_final_newline.getName());
        assertEquals("set to true to ensure file ends with a newline when saving and false to ensure it doesn't.", 
            PropertyType.insert_final_newline.getDescription());
        assertTrue(PropertyType.insert_final_newline instanceof PropertyType.LowerCasingPropertyType);
    }

    @Test
    public void testRootPropertyType() {
        assertEquals("root", PropertyType.root.getName());
        assertEquals("special property that should be specified at the top of the file outside of any sections. Set to true to stop .editorconfig files search on current file.", 
            PropertyType.root.getDescription());
        assertTrue(PropertyType.root instanceof PropertyType.LowerCasingPropertyType);
    }

    @Test
    public void testTabWidthPropertyType() {
        assertEquals("tab_width", PropertyType.tab_width.getName());
        assertEquals("a whole number defining the number of columns used to represent a tab character. This defaults to the parsed of indent_size and doesn't usually need to be specified.", 
            PropertyType.tab_width.getDescription());
    }

    @Test
    public void testTrimTrailingWhitespacePropertyType() {
        assertEquals("trim_trailing_whitespace", PropertyType.trim_trailing_whitespace.getName());
        assertEquals("set to true to remove any whitespace characters preceding newline characters and false to ensure it doesn't.", 
            PropertyType.trim_trailing_whitespace.getDescription());
        assertTrue(PropertyType.trim_trailing_whitespace instanceof PropertyType.LowerCasingPropertyType);
    }
}
