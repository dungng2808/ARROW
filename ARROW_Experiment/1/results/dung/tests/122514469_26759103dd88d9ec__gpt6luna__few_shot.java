package io.github.leberkleber.ljcm.parser.impl;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BooleanParser_ArrowTest {

    @Test
    public void getResponsibleClassesReturnsBooleanTypes() {
        BooleanParser parser = new BooleanParser();
        Set<Class> expected = new HashSet<>();
        expected.add(Boolean.class);
        expected.add(boolean.class);

        assertEquals(expected, parser.getResponsibleClasses());
    }

    @Test
    public void parseValueRecognizesTrueValuesIgnoringCase() {
        BooleanParser parser = new BooleanParser();

        assertTrue(parser.parseValue("1", Boolean.class));
        assertTrue(parser.parseValue("true", Boolean.class));
        assertTrue(parser.parseValue("TRUE", Boolean.class));
        assertTrue(parser.parseValue("yes", Boolean.class));
        assertTrue(parser.parseValue("YES", Boolean.class));
    }

    @Test
    public void parseValueReturnsFalseForOtherValues() {
        BooleanParser parser = new BooleanParser();

        assertFalse(parser.parseValue("false", Boolean.class));
        assertFalse(parser.parseValue("0", Boolean.class));
        assertFalse(parser.parseValue("", Boolean.class));
    }

    @Test(expected = io.github.leberkleber.ljcm.parser.ConfigurationParserException.class)
    public void parseValueRejectsNull() {
        BooleanParser parser = new BooleanParser();
        parser.parseValue(null, Boolean.class);
    }
}
