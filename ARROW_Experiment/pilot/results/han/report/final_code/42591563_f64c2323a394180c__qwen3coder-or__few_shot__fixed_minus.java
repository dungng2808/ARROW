package org.cbioportal.genome_nexus.web.validation;

import org.junit.Test;
import static org.junit.Assert.*;

public class GenomicLocationValidator_ArrowTest {

    @Test
    public void testValidGenomicLocation() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocation = "1,100,200,ATG,T";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testInvalidNumberOfFields() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATG"; // only 4 fields
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidChromosome() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "25,100,200,ATG,T"; // chromosome 25 is invalid
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidStartEnd() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,-10,200,ATG,T"; // negative start position
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidAlleles() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATGX,T"; // invalid character 'X' in variant allele
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testValidSpecialChromosomes() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocation1 = "X,100,200,ATG,T";
        String validLocation2 = "Y,100,200,ATG,T";
        String validLocation3 = "MT,100,200,ATG,T";
        assertTrue(validator.isValid(validLocation1, null));
        assertTrue(validator.isValid(validLocation2, null));
        assertTrue(validator.isValid(validLocation3, null));
    }

    @Test
    public void testValidChromosomeNumbers() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        for (int i = 1; i <= 24; i++) {
            String location = i + ",100,200,ATG,T";
            assertTrue(validator.isValid(location, null));
        }
    }

    @Test
    public void testValidAllelesWithDash() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocation = "1,100,200,ATG-,T-";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testZeroPositions() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String validLocation = "1,0,0,ATG,T";
        assertTrue(validator.isValid(validLocation, null));
    }
    
    @Test
    public void testInvalidChromosomeNumber() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "0,100,200,ATG,T"; // chromosome 0 is invalid
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testInvalidStartEndNegative() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,-10,-20,ATG,T"; // negative start and end positions
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testInvalidAllelesWithInvalidChar() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATGZ,T"; // invalid character 'Z' in reference allele
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testInvalidAllelesWithInvalidCharInVariant() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,ATG,TZ"; // invalid character 'Z' in variant allele
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testInvalidStartEndNonInteger() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,abc,200,ATG,T"; // non-integer start position
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testInvalidAllelesWithEmpty() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,200,,T"; // empty reference allele
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testInvalidStartEndNonInteger2() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,100,def,ATG,T"; // non-integer end position
        assertFalse(validator.isValid(invalidLocation, null));
    }
    
    @Test
    public void testInvalidStartEndNonInteger3() {
        GenomicLocationValidator validator = new GenomicLocationValidator();
        String invalidLocation = "1,abc,def,ATG,T"; // non-integer start and end positions
        assertFalse(validator.isValid(invalidLocation, null));
    }
}
