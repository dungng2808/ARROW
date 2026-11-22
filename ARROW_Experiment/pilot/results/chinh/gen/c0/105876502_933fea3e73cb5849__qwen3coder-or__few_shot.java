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
        PropertyType<String> charsetType = PropertyType.charset;
        assertNotNull(charsetType);
        assertEquals("charset", charsetType.getName());
        assertEquals("set to latin1, utf-8, utf-8-bom, utf-16be or utf-16le to control the character set. Use of utf-8-bom is discouraged.", charsetType.getDescription());
        
        // Test normalizeIfNeeded
        assertEquals("UTF-8", charsetType.normalizeIfNeeded("UTF-8"));
        assertEquals("utf-8", charsetType.normalizeIfNeeded("UTF-8"));
        assertNull(charsetType.normalizeIfNeeded(null));
    }

    @Test
    public void testEndOfLinePropertyType() {
        PropertyType<PropertyType.EndOfLineValue> endOfLineType = PropertyType.end_of_line;
        assertNotNull(endOfLineType);
        assertEquals("end_of_line", endOfLineType.getName());
        assertEquals("set to lf, cr, or crlf to control how line breaks are represented.", endOfLineType.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = endOfLineType.parse("lf");
        assertTrue(result.isValid());
        assertEquals(PropertyType.EndOfLineValue.lf, result.getParsed());
        
        // Test normalizeIfNeeded
        assertEquals("lf", endOfLineType.normalizeIfNeeded("LF"));
        assertEquals("cr", endOfLineType.normalizeIfNeeded("CR"));
        assertEquals("crlf", endOfLineType.normalizeIfNeeded("CRLF"));
        assertNull(endOfLineType.normalizeIfNeeded(null));
    }

    @Test
    public void testIndentSizePropertyType() {
        PropertyType<Integer> indentSizeType = PropertyType.indent_size;
        assertNotNull(indentSizeType);
        assertEquals("indent_size", indentSizeType.getName());
        assertEquals("a whole number defining the number of columns used for each indentation level and the width of soft tabs (when supported). When set to tab, the parsed of tab_width (if specified) will be used.", indentSizeType.getDescription());
        
        // Test parsing with numeric value
        PropertyType.PropertyValue<Integer> result1 = indentSizeType.parse("4");
        assertTrue(result1.isValid());
        assertEquals(Integer.valueOf(4), result1.getParsed());
        
        // Test parsing with "tab" value
        PropertyType.PropertyValue<Integer> result2 = indentSizeType.parse("tab");
        assertTrue(result2.isValid());
        assertNull(result2.getParsed());
        
        // Test normalizeIfNeeded
        assertEquals("4", indentSizeType.normalizeIfNeeded("4"));
        assertEquals("tab", indentSizeType.normalizeIfNeeded("TAB"));
        assertNull(indentSizeType.normalizeIfNeeded(null));
    }

    @Test
    public void testIndentStylePropertyType() {
        PropertyType<PropertyType.IndentStyleValue> indentStyleType = PropertyType.indent_style;
        assertNotNull(indentStyleType);
        assertEquals("indent_style", indentStyleType.getName());
        assertEquals("set to tab or space to use hard tabs or soft tabs respectively.", indentStyleType.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<PropertyType.IndentStyleValue> result = indentStyleType.parse("space");
        assertTrue(result.isValid());
        assertEquals(PropertyType.IndentStyleValue.space, result.getParsed());
        
        // Test normalizeIfNeeded
        assertEquals("space", indentStyleType.normalizeIfNeeded("SPACE"));
        assertEquals("tab", indentStyleType.normalizeIfNeeded("TAB"));
        assertNull(indentStyleType.normalizeIfNeeded(null));
    }

    @Test
    public void testInsertFinalNewlinePropertyType() {
        PropertyType<Boolean> insertFinalNewlineType = PropertyType.insert_final_newline;
        assertNotNull(insertFinalNewlineType);
        assertEquals("insert_final_newline", insertFinalNewlineType.getName());
        assertEquals("set to true to ensure file ends with a newline when saving and false to ensure it doesn't.", insertFinalNewlineType.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Boolean> result = insertFinalNewlineType.parse("true");
        assertTrue(result.isValid());
        assertEquals(Boolean.TRUE, result.getParsed());
        
        // Test normalizeIfNeeded
        assertEquals("true", insertFinalNewlineType.normalizeIfNeeded("TRUE"));
        assertEquals("false", insertFinalNewlineType.normalizeIfNeeded("FALSE"));
        assertNull(insertFinalNewlineType.normalizeIfNeeded(null));
    }

    @Test
    public void testRootPropertyType() {
        PropertyType<Boolean> rootType = PropertyType.root;
        assertNotNull(rootType);
        assertEquals("root", rootType.getName());
        assertEquals("special property that should be specified at the top of the file outside of any sections. Set to true to stop .editorconfig files search on current file.", rootType.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Boolean> result = rootType.parse("false");
        assertTrue(result.isValid());
        assertEquals(Boolean.FALSE, result.getParsed());
        
        // Test normalizeIfNeeded
        assertEquals("true", rootType.normalizeIfNeeded("TRUE"));
        assertEquals("false", rootType.normalizeIfNeeded("FALSE"));
        assertNull(rootType.normalizeIfNeeded(null));
    }

    @Test
    public void testTabWidthPropertyType() {
        PropertyType<Integer> tabWidthType = PropertyType.tab_width;
        assertNotNull(tabWidthType);
        assertEquals("tab_width", tabWidthType.getName());
        assertEquals("a whole number defining the number of columns used to represent a tab character. This defaults to the parsed of indent_size and doesn't usually need to be specified.", tabWidthType.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Integer> result = tabWidthType.parse("4");
        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(4), result.getParsed());
        
        // Test normalizeIfNeeded
        assertEquals("4", tabWidthType.normalizeIfNeeded("4"));
        assertNull(tabWidthType.normalizeIfNeeded(null));
    }

    @Test
    public void testTrimTrailingWhitespacePropertyType() {
        PropertyType<Boolean> trimTrailingWhitespaceType = PropertyType.trim_trailing_whitespace;
        assertNotNull(trimTrailingWhitespaceType);
        assertEquals("trim_trailing_whitespace", trimTrailingWhitespaceType.getName());
        assertEquals("set to true to remove any whitespace characters preceding newline characters and false to ensure it doesn't.", trimTrailingWhitespaceType.getDescription());
        
        // Test parsing
        PropertyType.PropertyValue<Boolean> result = trimTrailingWhitespaceType.parse("true");
        assertTrue(result.isValid());
        assertEquals(Boolean.TRUE, result.getParsed());
        
        // Test normalizeIfNeeded
        assertEquals("true", trimTrailingWhitespaceType.normalizeIfNeeded("TRUE"));
        assertEquals("false", trimTrailingWhitespaceType.normalizeIfNeeded("FALSE"));
        assertNull(trimTrailingWhitespaceType.normalizeIfNeeded(null));
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
    public void testIndentStyleValueValueSet() {
        Set<String> valueSet = PropertyType.IndentStyleValue.valueSet();
        assertNotNull(valueSet);
        assertEquals(2, valueSet.size());
        assertTrue(valueSet.contains("space"));
        assertTrue(valueSet.contains("tab"));
    }

    @Test
    public void testLowerCasingPropertyTypeNormalizeIfNeeded() {
        PropertyType.LowerCasingPropertyType<String> lowerCasingType = 
            new PropertyType.LowerCasingPropertyType<>("test", "test desc", 
                PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, "TEST", "test");
        
        assertEquals("test", lowerCasingType.normalizeIfNeeded("TEST"));
        assertEquals("test", lowerCasingType.normalizeIfNeeded("Test"));
        assertNull(lowerCasingType.normalizeIfNeeded(null));
    }

    @Test
    public void testPropertyValueValid() {
        PropertyType.PropertyValue<String> validValue = PropertyType.PropertyValue.valid("source", "value");
        assertEquals("source", validValue.getSource());
        assertEquals("value", validValue.getParsed());
        assertNull(validValue.getErrorMessage());
        assertTrue(validValue.isValid());
    }

    @Test
    public void testPropertyValueInvalid() {
        PropertyType.PropertyValue<String> invalidValue = PropertyType.PropertyValue.invalid("source", "error");
        assertEquals("source", invalidValue.getSource());
        assertNull(invalidValue.getParsed());
        assertEquals("error", invalidValue.getErrorMessage());
        assertFalse(invalidValue.isValid());
    }

    @Test
    public void testEnumValueParser() {
        PropertyType.PropertyValueParser<PropertyType.EndOfLineValue> parser = 
            new PropertyType.PropertyValueParser.EnumValueParser<>(PropertyType.EndOfLineValue.class);
        
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> result = parser.parse("test", "lf");
        assertTrue(result.isValid());
        assertEquals(PropertyType.EndOfLineValue.lf, result.getParsed());
        
        PropertyType.PropertyValue<PropertyType.EndOfLineValue> invalidResult = parser.parse("test", "invalid");
        assertFalse(invalidResult.isValid());
        assertNotNull(invalidResult.getErrorMessage());
    }

    @Test
    public void testBooleanValueParser() {
        PropertyType.PropertyValueParser<Boolean> parser = PropertyType.PropertyValueParser.BOOLEAN_VALUE_PARSER;
        
        PropertyType.PropertyValue<Boolean> trueResult = parser.parse("test", "true");
        assertTrue(trueResult.isValid());
        assertEquals(Boolean.TRUE, trueResult.getParsed());
        
        PropertyType.PropertyValue<Boolean> falseResult = parser.parse("test", "false");
        assertTrue(falseResult.isValid());
        assertEquals(Boolean.FALSE, falseResult.getParsed());
        
        PropertyType.PropertyValue<Boolean> invalidResult = parser.parse("test", "invalid");
        assertFalse(invalidResult.isValid());
        assertNotNull(invalidResult.getErrorMessage());
    }

    @Test
    public void testIdentityValueParser() {
        PropertyType.PropertyValueParser<String> parser = PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER;
        
        PropertyType.PropertyValue<String> result = parser.parse("test", "value");
        assertTrue(result.isValid());
        assertEquals("value", result.getParsed());
    }

    @Test
    public void testIndentSizeValueParser() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.INDENT_SIZE_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> tabResult = parser.parse("test", "tab");
        assertTrue(tabResult.isValid());
        assertNull(tabResult.getParsed());
        
        PropertyType.PropertyValue<Integer> numberResult = parser.parse("test", "4");
        assertTrue(numberResult.isValid());
        assertEquals(Integer.valueOf(4), numberResult.getParsed());
    }

    @Test
    public void testPositiveIntValueParser() {
        PropertyType.PropertyValueParser<Integer> parser = PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER;
        
        PropertyType.PropertyValue<Integer> result = parser.parse("test", "4");
        assertTrue(result.isValid());
        assertEquals(Integer.valueOf(4), result.getParsed());
        
        PropertyType.PropertyValue<Integer> negativeResult = parser.parse("test", "-1");
        assertFalse(negativeResult.isValid());
        assertNotNull(negativeResult.getErrorMessage());
        
        PropertyType.PropertyValue<Integer> invalidResult = parser.parse("test", "invalid");
        assertFalse(invalidResult.isValid());
        assertNotNull(invalidResult.getErrorMessage());
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
