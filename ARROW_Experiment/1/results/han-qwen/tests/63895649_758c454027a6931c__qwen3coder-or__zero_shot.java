package edu.emory.mathcs.nlp.component.tokenizer.dictionary;

import edu.emory.mathcs.nlp.common.util.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
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
        String content = "Inc.\nCo.\nLtd.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation();
        abbreviation.init(inputStream);
        assertNotNull(abbreviation);
    }

    @Test
    public void testIsAbbreviationEndingWithPeriod_True() {
        String content = "Dr.\nMr.\nMrs.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("dr."));
        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("mr."));
        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("mrs."));
    }

    @Test
    public void testIsAbbreviationEndingWithPeriod_False() {
        String content = "Dr.\nMr.\nMrs.\n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("company."));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("hello."));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("world"));
    }

    @Test
    public void testIsAbbreviationEndingWithPeriod_EmptySet() {
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        Abbreviation abbreviation = new Abbreviation(inputStream);
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("dr."));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("mr."));
    }
}
