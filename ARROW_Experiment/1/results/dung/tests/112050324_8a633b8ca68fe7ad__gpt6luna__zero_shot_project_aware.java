package com.sexycode.codepractice.exam.primefactors;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PrimeFactors_ArrowTest {

    @Test
    public void generateReturnsEmptyListForOne() {
        assertEquals(Collections.emptyList(), PrimeFactors.generate(1));
    }

    @Test
    public void generateReturnsEmptyListForZero() {
        assertEquals(Collections.emptyList(), PrimeFactors.generate(0));
    }

    @Test
    public void generateReturnsEmptyListForNegativeNumber() {
        assertEquals(Collections.emptyList(), PrimeFactors.generate(-5));
    }

    @Test
    public void generateReturnsPrimeFactorsForCompositeNumber() {
        assertEquals(Arrays.asList(2, 3), PrimeFactors.generate(6));
    }

    @Test
    public void generateReturnsTheNumberWhenItIsPrime() {
        assertEquals(Arrays.asList(7), PrimeFactors.generate(7));
    }

    @Test
    public void generateReturnsDistinctPrimeFactors() {
        assertEquals(Arrays.asList(2, 3), PrimeFactors.generate(12));
    }

    @Test
    public void generateReturnsEmptyListForPrimePowerOfTwo() {
        assertEquals(Arrays.asList(2), PrimeFactors.generate(8));
    }
}
