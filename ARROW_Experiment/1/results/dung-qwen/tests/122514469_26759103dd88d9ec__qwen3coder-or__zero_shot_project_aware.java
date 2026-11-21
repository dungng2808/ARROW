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
        
        assertTrue(parser.parseValue("1", Boolean.class));
        assertTrue(parser.parseValue("true", Boolean.class));
        assertTrue(parser.parseValue("yes", Boolean.class));
        assertTrue(parser.parseValue("TRUE", Boolean.class));
        assertTrue(parser.parseValue("YES", Boolean.class));
        assertTrue(parser.parseValue("Yes", Boolean.class));
    }

    @Test
    public void testParseValueFalseValues() {
        BooleanParser parser = new BooleanParser();
        
        assertFalse(parser.parseValue("0", Boolean.class));
        assertFalse(parser.parseValue("false", Boolean.class));
        assertFalse(parser.parseValue("no", Boolean.class));
        assertFalse(parser.parseValue("maybe", Boolean.class));
        assertFalse(parser.parseValue("", Boolean.class));
    }

    @Test(expected = ConfigurationParserException.class)
    public void testParseValueNull() {
        BooleanParser parser = new BooleanParser();
        parser.parseValue(null, Boolean.class);
    }
}
