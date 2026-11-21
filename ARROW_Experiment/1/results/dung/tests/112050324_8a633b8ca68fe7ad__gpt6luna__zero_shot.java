package com.sexycode.codepractice.exam.primefactors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

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
    public void generateReturnsPrimeDivisorsInAscendingOrder() {
        assertEquals(Arrays.asList(2, 3, 5), PrimeFactors.generate(30));
    }

    @Test
    public void generateReturnsOnlyTheNumberWhenInputIsPrime() {
        assertEquals(Arrays.asList(7), PrimeFactors.generate(7));
    }

    @Test
    public void generateReturnsSinglePrimeDivisorForPowerOfPrime() {
        assertEquals(Arrays.asList(2), PrimeFactors.generate(8));
    }
}
