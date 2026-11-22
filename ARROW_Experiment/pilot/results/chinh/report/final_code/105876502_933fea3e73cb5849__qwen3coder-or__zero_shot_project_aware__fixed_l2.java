package org.ec4j.core.model;

import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

public class PropertyType_ArrowTest {

    @Test
    public void testStandardTypes() {
        Set<PropertyType<?>> standardTypes = PropertyType.standardTypes();
        assertNotNull(standardTypes);
        assertEquals(8, standardTypes.size());
        
        assertTrue(standardTypes.contains(PropertyType.charset));
        assertTrue(standardTypes.contains(PropertyType.end_of_line));
        assertTrue(standardTypes.contains(PropertyType.indent_size));
        assertTrue(standardTypes.contains(PropertyType.indent_style));
        assertTrue(standardTypes.contains(PropertyType.insert_final_newline));
        assertTrue(standardTypes.contains(PropertyType.root));
        assertTrue(standardTypes.contains(PropertyType.tab_width));
        assertTrue(standardTypes.contains(PropertyType.trim_trailing_whitespace));
    }

    @Test
    public void testCharsetPropertyType() {
        PropertyType<String> type = PropertyType.charset;
        assertNotNull(type);
        assertEquals("charset", type.getName());
        assertEquals("set to latin1, utf-8, utf-8-bom, utf-16be or utf-16le to control the character set. Use of utf-8-bom is discouraged.", type.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<String> result = type.parse("UTF-8");
        assertTrue(result.isValid());
        assertEquals("UTF-8", result.getParsed());
        
        // Test normalization
        assertEquals("utf-8", type.normalizeIfNeeded("UTF-8"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void testEndOfLinePropertyType() {
        PropertyType<PropertyType.EndOfLineValue> type = PropertyType.end_of_line;
        assertNotNull(type);
        assertEquals("end_of_line", type.getName());
        assertEquals("set to lf, cr, or crlf to control how line breaks are represented.", type.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = type.parse("lf");
        assertTrue(result.isValid());
        assertEquals(PropertyType.EndOfLineValue.lf, result.getParsed());
        
        // Test normalization
        assertEquals("lf", type.normalizeIfNeeded("LF"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void testIndentSizePropertyType() {
        PropertyType<Integer> type = PropertyType.indent_size;
        assertNotNull(type);
        assertEquals("indent_size", type.getName());
        assertEquals("a whole number defining the number of columns used for each indentation level and the width of soft tabs (when supported). When set to tab, the parsed of tab_width (if specified) will be used.", type.getDescription());
        
        // Test parsing with numeric value
        PropertyType.PropertyValue<Integer> result1 = type.parse("4");
        assertTrue(result1.isValid());
        assertEquals(Integer.valueOf(4), result1.getParsed());
        
        // Test parsing with "tab" value
        PropertyType.PropertyValue<Integer> result2 = type.parse("tab");
        assertTrue(result2.isValid());
        assertNull(result2.getParsed());
        
        // Test normalization
        assertEquals("4", type.normalizeIfNeeded("4"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void testIndentStylePropertyType() {
        PropertyType<PropertyType.IndentStyleValue> type = PropertyType.indent_style;
        assertNotNull(type);
        assertEquals("indent_style", type.getName());
        assertEquals("set to tab or space to use hard tabs or soft tabs respectively.", type.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<PropertyType.IndentStyleValue> result = type.parse("space");
        assertTrue(result.isValid());
        assertEquals(PropertyType.IndentStyleValue.space, result.getParsed());
        
        // Test normalization
        assertEquals("space", type.normalizeIfNeeded("SPACE"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void testInsertFinalNewlinePropertyType() {
        PropertyType<Boolean> type = PropertyType.insert_final_newline;
        assertNotNull(type);
        assertEquals("insert_final_newline", type.getName());
        assertEquals("set to true to ensure file ends with a newline when saving and false to ensure it doesn't.", type.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Boolean> result = type.parse("true");
        assertTrue(result.isValid());
        assertEquals(Boolean.TRUE, result.getParsed());
        
        // Test normalization
        assertEquals("true", type.normalizeIfNeeded("TRUE"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void testRootPropertyType() {
        PropertyType<Boolean> type = PropertyType.root;
        assertNotNull(type);
        assertEquals("root", type.getName());
        assertEquals("special property that should be specified at the top of the file outside of any sections. Set to true to stop .editorconfig files search on current file.", type.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Boolean> result = type.parse("false");
        assertTrue(result.isValid());
        assertEquals(Boolean.FALSE, result.getParsed());
        
        // Test normalization
        assertEquals("false", type.normalizeIfNeeded("FALSE"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void testTabWidthPropertyType() {
        PropertyType<Integer> type = PropertyType.tab_width;
        assertNotNull(type);
        assertEquals("tab_width", type.getName());
        assertEquals("a whole number defining the number of columns used to represent a tab character. This defaults to the parsed of indent_size and doesn't usually need to be specified.", type.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Integer> result = type.parse("8");
        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(8), result.getParsed());
    }

    @Test
    public void testTrimTrailingWhitespacePropertyType() {
        PropertyType<Boolean> type = PropertyType.trim_trailing_whitespace;
        assertNotNull(type);
        assertEquals("trim_trailing_whitespace", type.getName());
        assertEquals("set to true to remove any whitespace characters preceding newline characters and false to ensure it doesn't.", type.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Boolean> result = type.parse("true");
        assertTrue(result.isValid());
        assertEquals(Boolean.TRUE, result.getParsed());
        
        // Test normalization
        assertEquals("true", type.normalizeIfNeeded("TRUE"));
        assertNull(type.normalizeIfNeeded(null));
    }

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
        assertNotNull(valueSet);
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
        assertNotNull(valueSet);
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
            new PropertyType.LowerCasingPropertyType<>("test", "description", 
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "TEST");
        
        assertEquals("test", type.normalizeIfNeeded("TEST"));
        assertNull(type.normalizeIfNeeded(null));
    }

    @Test
    public void testPropertyValueInvalid() {
        PropertyType.PropertyValue<String> value = PropertyType.PropertyValue.invalid("source", "error");
        assertFalse(value.isValid());
        assertEquals("source", value.getSource());
        assertEquals("error", value.getErrorMessage());
        assertNull(value.getParsed());
    }

    @Test
    public void testPropertyValueValid() {
        PropertyType.PropertyValue<String> value = PropertyType.PropertyValue.valid("source", "parsed");
        assertTrue(value.isValid());
        assertEquals("source", value.getSource());
        assertEquals("parsed", value.getParsed());
        assertNull(value.getErrorMessage());
    }

    @Test
    public void testPropertyValueEquals() {
        PropertyType.PropertyValue<String> value1 = PropertyType.PropertyValue.valid("source", "parsed");
        PropertyType.PropertyValue<String> value2 = PropertyType.PropertyValue.valid("source", "parsed");
        PropertyType.PropertyValue<String> value3 = PropertyType.PropertyValue.invalid("source", "error");
        PropertyType.PropertyValue<String> value4 = PropertyType.PropertyValue.invalid("source", "error");
        
        assertEquals(value1, value2);
        assertEquals(value3, value4);
        assertNotEquals(value1, value3);
    }

    @Test
    public void testPropertyValueHashCode() {
        PropertyType.PropertyValue<String> value1 = PropertyType.PropertyValue.valid("source", "parsed");
        PropertyType.PropertyValue<String> value2 = PropertyType.PropertyValue.valid("source", "parsed");
        assertEquals(value1.hashCode(), value2.hashCode());
    }

    @Test
    public void testEnumValueParserParseWithNull() {
        PropertyType.PropertyValueParser<PropertyType.EndOfLineValue> parser = 
            new PropertyType.PropertyValueParser.EnumValueParser<>(PropertyType.EndOfLineValue.class);
        
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = parser.parse("test", null);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testEnumValueParserParseWithInvalidValue() {
        PropertyType.PropertyValueParser<PropertyType.EndOfLineValue> parser = 
            new PropertyType.PropertyValueParser.EnumValueParser<>(PropertyType.EndOfLineValue.class);
        
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = parser.parse("test", "invalid");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testEnumValueParserParseValidValue() {
        PropertyType.PropertyValueParser<PropertyType.EndOfLineValue> parser = 
            new PropertyType.PropertyValueParser.EnumValueParser<>(PropertyType.EndOfLineValue.class);
        
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = parser.parse("test", "lf");
        assertTrue(result.isValid());
        assertEquals(PropertyType.EndOfLineValue.lf, result.getParsed());
    }

    @Test
    public void testBooleanValueParserParseWithNull() {
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
    public void testIndentSizeValueParserWithTab() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.INDENT_SIZE_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "tab");
        assertTrue(result.isValid());
        assertNull(result.getParsed());
    }

    @Test
    public void testIndentSizeValueParserWithNumber() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.INDENT_SIZE_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "4");
        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(4), result.getParsed());
    }

    @Test
    public void testPositiveIntValueParserWithZero() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "0");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testPositiveIntValueParserWithNegative() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "-1");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testPositiveIntValueParserWithValidNumber() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "5");
        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(5), result.getParsed());
    }

    @Test
    public void testPositiveIntValueParserWithInvalidString() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "not_a_number");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testToString() {
        assertEquals("charset", PropertyType.charset.toString());
        assertEquals("end_of_line", PropertyType.end_of_line.toString());
        assertEquals("indent_size", PropertyType.indent_size.toString());
        assertEquals("indent_style", PropertyType.indent_style.toString());
        assertEquals("insert_final_newline", PropertyType.insert_final_newline.toString());
        assertEquals("root", PropertyType.root.toString());
        assertEquals("tab_width", PropertyType.tab_width.toString());
        assertEquals("trim_trailing_whitespace", PropertyType.trim_trailing_whitespace.toString());
    }
}
