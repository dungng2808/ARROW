package io.github.leberkleber.ljcm.parser.impl;

import io.github.leberkleber.ljcm.parser.ConfigurationParserException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;

public class BooleanParser_ArrowTest {

    @Test
    public void testGetResponsibleClasses() {
        BooleanParser parser = new BooleanParser();
        Set<Class> result = parser.getResponsibleClasses();
        
        assertEquals(2, result.size());
        assertTrue(result.contains(Boolean.class));
        assertTrue(result.contains(boolean.class));
    }

    @Test
    public void testParseValueTrueString() {
        BooleanParser parser = new BooleanParser();
        Boolean result = parser.parseValue("true", Boolean.class);
        assertTrue(result);
    }

    @Test
    public void testParseValueFalseString() {
        BooleanParser parser = new BooleanParser();
        Boolean result = parser.parseValue("false", Boolean.class);
        assertFalse(result);
    }

    @Test
    public void testParseValueOneString() {
        BooleanParser parser = new BooleanParser();
        Boolean result = parser.parseValue("1", Boolean.class);
        assertTrue(result);
    }

    @Test
    public void testParseValueYesString() {
        BooleanParser parser = new BooleanParser();
        Boolean result = parser.parseValue("yes", Boolean.class);
        assertTrue(result);
    }

    @Test
    public void testParseValueIgnoreCase() {
        BooleanParser parser = new BooleanParser();
        Boolean result = parser.parseValue("TRUE", Boolean.class);
        assertTrue(result);
    }

    @Test
    public void testParseValueNull() {
        BooleanParser parser = new BooleanParser();
        try {
            parser.parseValue(null, Boolean.class);
            fail("Expected ConfigurationParserException");
        } catch (ConfigurationParserException e) {
            assertEquals("Could not parse 'null' to boolean", e.getMessage());
        }
    }

    @Test
    public void testParseValueInvalidString() {
        BooleanParser parser = new BooleanParser();
        Boolean result = parser.parseValue("invalid", Boolean.class);
        assertFalse(result);
    }
}
