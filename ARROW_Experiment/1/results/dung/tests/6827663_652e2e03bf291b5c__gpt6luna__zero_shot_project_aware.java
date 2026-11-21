package org.moskito.demo.fibonacci.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FibonacciCalculatorImpl_ArrowTest {

    @Test
    public void calculateReturnsOneForOrderOne() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(1L, calculator.calculate(1));
    }

    @Test
    public void calculateReturnsOneForOrderTwo() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(1L, calculator.calculate(2));
    }

    @Test
    public void calculateReturnsOneForOrderBelowThree() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(1L, calculator.calculate(0));
    }

    @Test
    public void calculateReturnsFibonacciValueForOrderThree() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(2L, calculator.calculate(3));
    }

    @Test
    public void calculateReturnsFibonacciValueForOrderTen() {
        FibonacciCalculatorImpl calculator = new FibonacciCalculatorImpl();
        assertEquals(55L, calculator.calculate(10));
    }
}
