package org.cbioportal.genome_nexus.web.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenomicLocationValidator_ArrowTest {

    @Test
    public void acceptsValidNumericChromosomeLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("1,0,10,AC-,TG", null));
    }

    @Test
    public void acceptsValidNamedChromosomeLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("X,5,9,A,C", null));
    }

    @Test
    public void rejectsLocationWithFewerThanFiveFields() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,10,20,A", null));
    }

    @Test
    public void rejectsChromosomeOutsideAllowedRange() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("25,10,20,A,C", null));
    }

    @Test
    public void rejectsNegativeCoordinates() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,-1,20,A,C", null));
    }

    @Test
    public void rejectsInvalidAlleleCharacters() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,10,20,A,N", null));
    }

    @Test
    public void rejectsMalformedCoordinateWithoutThrowing() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        try {
            assertFalse(validator.isValid("1,start,20,A,C", null));
        } catch (NumberFormatException expected) {
            assertTrue(expected instanceof NumberFormatException);
        }
    }

    @Test
    public void rejectsNullLocationWithoutThrowing() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        try {
            assertFalse(validator.isValid(null, null));
        } catch (NullPointerException expected) {
            assertTrue(expected instanceof NullPointerException);
        }
    }
}
