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
    public void acceptsBoundaryChromosomeAndZeroCoordinates() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertTrue(validator.isValid("24,0,0,TCGA,-", null));
    }

    @Test
    public void rejectsNonNumericCoordinate() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        boolean threw = false;
        try {
            validator.isValid("1,not-a-number,10,ACG,T", null);
        } catch (NumberFormatException expected) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void rejectsEmptyLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        assertFalse(validator.isValid("", null));
    }
}
