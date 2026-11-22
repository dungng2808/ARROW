package org.cbioportal.genome_nexus.web.validation;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GenomicLocationValidator_ArrowTest {

    private GenomicLocationValidator validator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new GenomicLocationValidator();
    }

    @Test
    public void testValidGenomicLocation() {
        String validLocation = "1,100,200,T,A";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testValidGenomicLocationWithChromosomeX() {
        String validLocation = "X,100,200,T,A";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testValidGenomicLocationWithChromosomeY() {
        String validLocation = "Y,100,200,T,A";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testValidGenomicLocationWithChromosomeMT() {
        String validLocation = "MT,100,200,T,A";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testValidGenomicLocationWithChromosome24() {
        String validLocation = "24,100,200,T,A";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationTooFewFields() {
        String invalidLocation = "1,100,200,T";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationNonNumericChromosome() {
        String invalidLocation = "chr1,100,200,T,A";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationChromosomeOutOfRange() {
        String invalidLocation = "25,100,200,T,A";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationNegativeStart() {
        String invalidLocation = "1,-100,200,T,A";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationNegativeEnd() {
        String invalidLocation = "1,100,-200,T,A";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationInvalidVariantAllele() {
        String invalidLocation = "1,100,200,TX,A";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationInvalidReferenceAllele() {
        String invalidLocation = "1,100,200,T,AX";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationEmptyString() {
        String invalidLocation = "";
        assertFalse(validator.isValid(invalidLocation, null));
    }

    @Test
    public void testInvalidGenomicLocationNull() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    public void testValidGenomicLocationWithDeletions() {
        String validLocation = "1,100,200,-,A";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testValidGenomicLocationWithInsertions() {
        String validLocation = "1,100,200,T,-";
        assertTrue(validator.isValid(validLocation, null));
    }

    @Test
    public void testValidGenomicLocationWithComplexVariants() {
        String validLocation = "1,100,200,TA,-G";
        assertTrue(validator.isValid(validLocation, null));
    }
}
