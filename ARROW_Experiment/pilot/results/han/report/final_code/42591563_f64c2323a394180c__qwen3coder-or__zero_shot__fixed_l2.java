package org.cbioportal.genome_nexus.web.validation;

import org.junit.Test;
import static org.junit.Assert.*;

public class GenomicLocationValidator_ArrowTest {

    @Test
    public void testIsValid_ValidGenomicLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocation = "1,100,200,ATG,T";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testIsValid_InvalidNumberOfFields() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATG"; // only 4 fields
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testIsValid_InvalidChromosome() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "Z,100,200,ATG,T"; // invalid chromosome
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testIsValid_InvalidChromosomeNumber() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "25,100,200,ATG,T"; // chromosome number too high
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testIsValid_NegativeStart() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,-100,200,ATG,T"; // negative start
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testIsValid_NegativeEnd() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,-200,ATG,T"; // negative end
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testIsValid_InvalidVariantAllele() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATGX,T"; // invalid character in variant allele
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testIsValid_InvalidReferenceAllele() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATG,TX"; // invalid character in reference allele
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testIsValid_ValidSpecialChromosomes() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocationX = "X,100,200,ATG,T";
        String validLocationY = "Y,100,200,ATG,T";
        String validLocationMT = "MT,100,200,ATG,T";
        assertTrue(validator.isValid(validLocationX, null));
        assertTrue(validator.isValid(validLocationY, null));
        assertTrue(validator.isValid(validLocationMT, null));
    }

    @Test
    public void testIsValid_ValidChromosomeNumbers() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        for (int i = 1; i <= 24; i++) {
            String location = i + ",100,200,ATG,T";
            assertTrue(validator.isValid(location, null));
        }
    }

    @Test
    public void testIsValid_ValidAllelesWithGaps() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocation = "1,100,200,ATG-,T-";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testIsValid_EmptyString() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String emptyLocation = "";
        assertFalse(validator.isValid(emptyLocation, null));
    }

    @Test
    public void testIsValid_NullString() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String nullLocation = null;
        assertFalse(validator.isValid(nullLocation, null));
    }
    
    @Test
    public void testIsValid_StartEqualsEnd() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocation = "1,100,100,ATG,T";
        assertTrue(validator.isValid(validLocation, null));
    }
    
    @Test
    public void testIsValid_StartGreaterThanEnd() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,200,100,ATG,T";
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testIsValid_EndLessThanStart() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATG,T";
        assertFalse(validator.isValid(invalidLocation, null));
    }
}
