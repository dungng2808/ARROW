package org.moskito.demo.fibonacci.calculator;

import org.junit.Test;
import static org.junit.Assert.*;

public class FibonacciCalculatorImpl_ArrowTest {

    @Test
    public void testCalculateForOrder1() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(1);
        assertEquals(1L, result);
    }

    @Test
    public void testCalculateForOrder2() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(2);
        assertEquals(1L, result);
    }

    @Test
    public void testCalculateForOrder3() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(3);
        assertEquals(2L, result);
    }

    @Test
    public void testCalculateForOrder4() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(4);
        assertEquals(3L, result);
    }

    @Test
    public void testCalculateForOrder5() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(5);
        assertEquals(5L, result);
    }

    @Test
    public void testCalculateForOrder6() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(6);
        assertEquals(8L, result);
    }

    @Test
    public void testCalculateForOrder7() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(7);
        assertEquals(13L, result);
    }

    @Test
    public void testCalculateForOrder8() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(8);
        assertEquals(21L, result);
    }

    @Test
    public void testCalculateForOrder9() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(9);
        assertEquals(34L, result);
    }

    @Test
    public void testCalculateForOrder10() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        long result = calculator.calculate(10);
        assertEquals(55L, result);
    }
}
