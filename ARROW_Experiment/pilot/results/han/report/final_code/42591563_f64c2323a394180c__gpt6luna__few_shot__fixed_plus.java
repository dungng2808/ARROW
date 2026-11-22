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
        assertFalse(validator.isValid("1,0,10,ACG,TN", null));
    }

    @Test
    public void rejectsNonNumericCoordinates() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        try {
            validator.isValid("1,abc,10,ACG,T", null);
            throw new AssertionError("Expected non-numeric coordinate to be rejected");
        } catch (NumberFormatException expected) {
            // Integer.valueOf in the validator rejects non-numeric coordinates.
        }
    }
}
