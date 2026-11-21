package edu.emory.mathcs.nlp.component.tokenizer.dictionary;

import edu.emory.mathcs.nlp.common.util.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.*;

public class Abbreviation_ArrowTest {

    @Test
    public void testConstructorDefault() {
        Abbreviation abbreviation = new Abbreviation();
        assertNotNull(abbreviation);
    }

    @Test
    public void testConstructorWithInputStream() {
        String content = "Dr.\nMr.\nMrs.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        assertNotNull(abbreviation);
    }

    @Test
    public void testInit() {
        String content = "U.S.A.\nInc.\nLtd.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation();
        abbreviation.init(inputStream);
        assertNotNull(abbreviation);
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
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("hello"));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("world"));
    }

    @Test
    public void testIsAbbreviationEndingWithPeriodEmpty() {
        String content = "";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("dr."));
    }
}
