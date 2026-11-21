package io.github.leberkleber.ljcm.parser.impl;

import io.github.leberkleber.ljcm.parser.ConfigurationParserException;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class BooleanParser_ArrowTest {

    @Test
    public void testGetResponsibleClasses() {
        BooleanParser parser = new BooleanParser();
        Set<Class> responsibleClasses = parser.getResponsibleClasses();
        
        assertNotNull(responsibleClasses);
        assertEquals(2, responsibleClasses.size());
        assertTrue(responsibleClasses.contains(Boolean.class));
        assertTrue(responsibleClasses.contains(boolean.class));
    }

    @Test
    public void testParseValueTrueValues() {
        BooleanParser parser = new BooleanParser();
        
        // Test various true values
        String[] trueValues = {"1", "true", "yes"};
        for (String value : trueValues) {
            Boolean result = parser.parseValue(value, Boolean.class);
            assertTrue("Value '" + value + "' should parse to true", result);
        }
    }

    @Test
    public void testParseValueFalseValues() {
        BooleanParser parser = new BooleanParser();
        
        // Test various false values
        String[] falseValues = {"0", "false", "no", "maybe", "test"};
        for (String value : falseValues) {
            Boolean result = parser.parseValue(value, Boolean.class);
            assertFalse("Value '" + value + "' should parse to false", result);
        }
    }

    @Test
    public void testParseValueCaseInsensitive() {
        BooleanParser parser = new BooleanParser();
        
        // Test case insensitivity
        String[] trueValues = {"TRUE", "True", "tRuE", "YES", "Yes", "yEs"};
        for (String value : trueValues) {
            Boolean result = parser.parseValue(value, Boolean.class);
            assertTrue("Value '" + value + "' should parse to true (case insensitive)", result);
        }
    }

    @Test(expected = ConfigurationParserException.class)
    public void testParseValueNull() {
        BooleanParser parser = new BooleanParser();
        parser.parseValue(null, Boolean.class);
    }

    @Test
    public void testParseValueNullReturnsFalseForNonTrueValues() {
        BooleanParser parser = new BooleanParser();
        
        // This test verifies that non-null values that aren't in our true set return false
        String[] nonTrueValues = {"0", "false", "no", "maybe", "test", "FALSE", "NO"};
        for (String value : nonTrueValues) {
            Boolean result = parser.parseValue(value, Boolean.class);
            assertFalse("Value '" + value + "' should parse to false", result);
        }
    }
}
