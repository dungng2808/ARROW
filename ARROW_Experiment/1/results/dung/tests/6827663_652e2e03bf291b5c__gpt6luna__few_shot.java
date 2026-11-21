package org.moskito.demo.fibonacci.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FibonacciCalculatorImpl_ArrowTest {

    @Test
    public void calculatesFirstOrder() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(1L, calculator.calculate(1));
    }

    @Test
    public void calculatesSecondOrder() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(1L, calculator.calculate(2));
    }

    @Test
    public void calculatesThirdOrder() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(2L, calculator.calculate(3));
    }

    @Test
    public void calculatesHigherOrder() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(55L, calculator.calculate(10));
    }

    @Test
    public void calculatesZeroOrder() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(1L, calculator.calculate(0));
    }
}
