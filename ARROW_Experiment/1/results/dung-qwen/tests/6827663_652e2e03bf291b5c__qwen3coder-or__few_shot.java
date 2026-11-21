package org.moskito.demo.fibonacci.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FibonacciCalculatorImpl_ArrowTest {

    @Test
    public void testCalculateForOrder1() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(1);
        assertEquals(1L, result);
    }

    @Test
    public void testCalculateForOrder2() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(2);
        assertEquals(1L, result);
    }

    @Test
    public void testCalculateForOrder3() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(3);
        assertEquals(1L, result);
    }

    @Test
    public void testCalculateForOrder4() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(4);
        assertEquals(2L, result);
    }

    @Test
    public void testCalculateForOrder5() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(5);
        assertEquals(3L, result);
    }

    @Test
    public void testCalculateForOrder6() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(6);
        assertEquals(5L, result);
    }

    @Test
    public void testCalculateForOrder7() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(7);
        assertEquals(8L, result);
    }

    @Test
    public void testCalculateForOrder8() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(8);
        assertEquals(13L, result);
    }

    @Test
    public void testCalculateForOrder9() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(9);
        assertEquals(21L, result);
    }

    @Test
    public void testCalculateForOrder10() {
        FibonacciCalculator calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(10);
        assertEquals(34L, result);
    }
}
