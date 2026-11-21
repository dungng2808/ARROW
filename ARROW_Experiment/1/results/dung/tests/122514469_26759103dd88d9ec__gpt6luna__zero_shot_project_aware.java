package io.github.leberkleber.ljcm.parser.impl;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BooleanParser_ArrowTest {

    @Test
    public void getResponsibleClassesReturnsBooleanTypes() {
        BooleanParser parser = new BooleanParser();

        Set<Class> expected = new HashSet<>(Arrays.<Class>asList(Boolean.class, boolean.class));
        assertEquals(expected, parser.getResponsibleClasses());
    }

    @Test
    public void parseValueRecognizesTrueValuesIgnoringCase() {
        BooleanParser parser = new BooleanParser();

        assertEquals(Boolean.TRUE, parser.parseValue("true", Boolean.class));
        assertEquals(Boolean.TRUE, parser.parseValue("TRUE", Boolean.class));
        assertEquals(Boolean.TRUE, parser.parseValue("yes", Boolean.class));
        assertEquals(Boolean.TRUE, parser.parseValue("1", Boolean.class));
    }

    @Test
    public void parseValueReturnsFalseForOtherValues() {
        BooleanParser parser = new BooleanParser();

        assertEquals(Boolean.FALSE, parser.parseValue("false", Boolean.class));
        assertEquals(Boolean.FALSE, parser.parseValue("no", Boolean.class));
        assertEquals(Boolean.FALSE, parser.parseValue("", Boolean.class));
    }

    @Test(expected = io.github.leberkleber.ljcm.parser.ConfigurationParserException.class)
    public void parseValueRejectsNull() {
        BooleanParser parser = new BooleanParser();

        parser.parseValue(null, Boolean.class);
    }
}
