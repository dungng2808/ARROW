package com.sexycode.codepractice.exam.primefactors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class PrimeFactors_ArrowTest {

    @Test
    public void generatesEmptyListForOne() {
        assertEquals(Collections.emptyList(), PrimeFactors.generate(1));
    }

    @Test
    public void generatesEmptyListForZero() {
        assertEquals(Collections.emptyList(), PrimeFactors.generate(0));
    }

    @Test
    public void generatesEmptyListForNegativeNumber() {
        assertEquals(Collections.emptyList(), PrimeFactors.generate(-5));
    }

    @Test
    public void generatesPrimeNumber() {
        assertEquals(Arrays.asList(7), PrimeFactors.generate(7));
    }

    @Test
    public void generatesDistinctPrimeFactors() {
        assertEquals(Arrays.asList(2, 3, 5), PrimeFactors.generate(30));
    }

    @Test
    public void generatesFactorsForPerfectPower() {
        assertEquals(Arrays.asList(2, 3), PrimeFactors.generate(36));
    }
}
