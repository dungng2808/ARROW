package edu.emory.mathcs.nlp.component.tokenizer.dictionary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Abbreviation_ArrowTest {

    @Test
    public void recognizesLoadedAbbreviation() {
        InputStream input = new ByteArrayInputStream("Dr.\n".getBytes(StandardCharsets.UTF_8));
        Abbreviation abbreviation = new Abbreviation(input);

        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("dr."));
    }

    @Test
    public void doesNotRecognizeMissingAbbreviation() {
        InputStream input = new ByteArrayInputStream("Dr.\n".getBytes(StandardCharsets.UTF_8));
        Abbreviation abbreviation = new Abbreviation(input);

        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("prof."));
    }

    @Test
    public void initReplacesAbbreviationSet() {
        Abbreviation abbreviation = new Abbreviation(
                new ByteArrayInputStream("Dr.\n".getBytes(StandardCharsets.UTF_8)));
        abbreviation.init(new ByteArrayInputStream("Prof.\n".getBytes(StandardCharsets.UTF_8)));

        assertTrue(abbreviation.isAbbreviationEndingWithPeriod("prof."));
        assertFalse(abbreviation.isAbbreviationEndingWithPeriod("dr."));
    }
}
