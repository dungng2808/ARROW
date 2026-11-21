package com.sexycode.codepractice.exam.primefactors;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrimeFactors_ArrowTest {

    @Test
    public void testGenerateWithOne() {
        List<Integer> result = PrimeFactors.generate(1);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGenerateWithTwo() {
        List<Integer> result = PrimeFactors.generate(2);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
    }

    @Test
    public void testGenerateWithThree() {
        List<Integer> result = PrimeFactors.generate(3);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get(0));
    }

    @Test
    public void testGenerateWithFour() {
        List<Integer> result = PrimeFactors.generate(4);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
    }

    @Test
    public void testGenerateWithSix() {
        List<Integer> result = PrimeFactors.generate(6);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
        assertEquals(Integer.valueOf(3), result.get(1));
    }

    @Test
    public void testGenerateWithEight() {
        List<Integer> result = PrimeFactors.generate(8);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
    }

    @Test
    public void testGenerateWithNine() {
        List<Integer> result = PrimeFactors.generate(9);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get(0));
    }

    @Test
    public void testGenerateWithTen() {
        List<Integer> result = PrimeFactors.generate(10);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
        assertEquals(Integer.valueOf(5), result.get(1));
    }

    @Test
    public void testGenerateWithFifteen() {
        List<Integer> result = PrimeFactors.generate(15);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get(0));
        assertEquals(Integer.valueOf(5), result.get(1));
    }

    @Test
    public void testGenerateWithSeventeen() {
        List<Integer> result = PrimeFactors.generate(17);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(17), result.get(0));
    }

    @Test
    public void testGenerateWithTwentyOne() {
        List<Integer> result = PrimeFactors.generate(21);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get(0));
        assertEquals(Integer.valueOf(7), result.get(1));
    }

    @Test
    public void testGenerateWithThirty() {
        List<Integer> result = PrimeFactors.generate(30);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
        assertEquals(Integer.valueOf(3), result.get(1));
        assertEquals(Integer.valueOf(5), result.get(2));
    }
}
