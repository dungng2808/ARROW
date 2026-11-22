package org.cbioportal.genome_nexus.web.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenomicLocationValidator_ArrowTest {

    @Test
    public void acceptsValidNumericChromosomeLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("1,0,100,ACGT,-", null));
    }

    @Test
    public void acceptsValidSpecialChromosomeLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("MT,10,20,A,T", null));
    }

    @Test
    public void rejectsLocationWithFewerThanFiveFields() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,0,100,A", null));
    }

    @Test
    public void rejectsChromosomeOutsideAllowedRange() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("25,0,100,A,T", null));
    }

    @Test
    public void rejectsNegativeStart() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,-1,100,A,T", null));
    }

    @Test
    public void rejectsAlleleWithInvalidCharacters() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,0,100,ATN,T", null));
    }

    @Test(expected = NumberFormatException.class)
    public void throwsForNonNumericStart() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        validator.isValid("1,start,100,A,T", null);
    }
}
