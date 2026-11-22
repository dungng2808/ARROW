package org.cbioportal.genome_nexus.web.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenomicLocationValidator_ArrowTest {

    @Test
    public void acceptsValidNumericChromosomeLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("1,0,10,ACG,T-", null));
    }

    @Test
    public void acceptsValidNamedChromosomeLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("MT,1,2,-,A", null));
    }

    @Test
    public void rejectsLocationWithTooFewFields() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,0,10,ACG", null));
    }

    @Test
    public void rejectsChromosomeOutsideAllowedRange() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("25,0,10,ACG,T", null));
    }

    @Test
    public void rejectsNegativeCoordinates() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,-1,10,ACG,T", null));
    }

    @Test
    public void rejectsInvalidAlleleCharacters() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,0,10,ACGN,T", null));
    }

    @Test
    public void acceptsBoundaryNumericChromosomeLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("24,0,0,-,-", null));
    }

    @Test
    public void rejectsEmptyAlleleField() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,0,10,A,T,,", null));
    }

    @Test
    public void rejectsLocationWithNonNumericCoordinate() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,start,10,A,T", null));
    }

    @Test
    public void rejectsLocationWithNonNumericEndCoordinate() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("1,0,end,A,T", null));
    }
}
