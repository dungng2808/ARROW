package com.sexycode.codepractice.exam.primefactors;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class PrimeFactors_ArrowTest {

    @Test
    public void testGenerateWithOne() {
        List<Integer> result = PrimeFactors.generate(1);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGenerateWithTwo() {
        List<Integer> result = PrimeFactors.generate(2);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
    }

    @Test
    public void testGenerateWithThree() {
        List<Integer> result = PrimeFactors.generate(3);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get(0));
    }

    @Test
    public void testGenerateWithFour() {
        List<Integer> result = PrimeFactors.generate(4);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
    }

    @Test
    public void testGenerateWithSix() {
        List<Integer> result = PrimeFactors.generate(6);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
        assertEquals(Integer.valueOf(3), result.get(1));
    }

    @Test
    public void testGenerateWithEight() {
        List<Integer> result = PrimeFactors.generate(8);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
    }

    @Test
    public void testGenerateWithNine() {
        List<Integer> result = PrimeFactors.generate(9);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get(0));
    }

    @Test
    public void testGenerateWithTwelve() {
        List<Integer> result = PrimeFactors.generate(12);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
        assertEquals(Integer.valueOf(3), result.get(1));
    }

    @Test
    public void testGenerateWithThirty() {
        List<Integer> result = PrimeFactors.generate(30);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
        assertEquals(Integer.valueOf(3), result.get(1));
        assertEquals(Integer.valueOf(5), result.get(2));
    }

    @Test
    public void testGenerateWithPrimeNumber() {
        List<Integer> result = PrimeFactors.generate(17);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(17), result.get(0));
    }

    @Test
    public void testGenerateWithCompositeNumber() {
        List<Integer> result = PrimeFactors.generate(15);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get(0));
        assertEquals(Integer.valueOf(5), result.get(1));
    }
}
