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

    @Test
    public void rejectsNonNumericStart() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        try {
            validator.isValid("1,start,100,A,T", null);
            org.junit.Assert.fail("Expected a number format exception");
        } catch (NumberFormatException expected) {
            assertTrue(expected instanceof NumberFormatException);
        }
    }

    @Test
    public void rejectsNullLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        try {
            validator.isValid(null, null);
            org.junit.Assert.fail("Expected an exception for a null location");
        } catch (Exception expected) {
            assertTrue(expected instanceof NullPointerException);
        }
    }
}
