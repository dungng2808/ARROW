package edu.emory.mathcs.nlp.component.tokenizer.dictionary;

import edu.emory.mathcs.nlp.common.util.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Abbreviation_ArrowTest {

    @Test
    public void testAbbreviationConstructor() {
        Abbreviation abbreviation = new Abbreviation();
        // Just testing that constructor doesn't throw exception
        // We can't easily test the actual content without accessing internal state
    }

    @Test
    public void testAbbreviationConstructorWithInputStream() {
        String content = "Dr.\nMr.\nMrs.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        // Testing that constructor with InputStream doesn't throw exception
    }

    @Test
    public void testIsAbbreviationEndingWithPeriodTrue() {
        String content = "Dr.\nMr.\nMrs.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        
        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("dr."));
        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("mr."));
        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("mrs."));
    }

    @Test
    public void testIsAbbreviationEndingWithPeriodFalse() {
        String content = "Dr.\nMr.\nMrs.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("hello."));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("world."));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("test"));
    }

    @Test
    public void testIsAbbreviationEndingWithPeriodEmpty() {
        String content = "";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("dr."));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod(""));
    }
}
