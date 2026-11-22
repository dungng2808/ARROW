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
    public void acceptsZeroCoordinatesWithValidAlleles() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("MT,0,0,A,C", null));
    }

    @Test
    public void rejectsCoordinateWithTrailingNonNumericCharacters() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,20x,30,A,C", null));
    }
}
