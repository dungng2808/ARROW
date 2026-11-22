package org.performance;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Calculator_ArrowTest {

    @Test
    public void testAdd() {
        Calculator calculator = new Calculator();
        double result = calculator.add(2.0, 3.0);
        assertEquals(5.0, result, 0.0);
    }

    @Test
    public void testSub() {
        Calculator calculator = new Calculator();
        double result = calculator.sub(5.0, 3.0);
        assertEquals(2.0, result, 0.0);
    }

    @Test
    public void testMul() {
        Calculator calculator = new Calculator();
        double result = calculator.mul(2.0, 3.0);
        assertEquals(6.0, result, 0.0);
    }

    @Test
    public void testDiv() {
        Calculator calculator = new Calculator();
        double result = calculator.div(6.0, 2.0);
        assertEquals(3.0, result, 0.0);
    }

    @Test
    public void testMod() {
        Calculator calculator = new Calculator();
        double result = calculator.mod(7.0, 3.0);
        assertEquals(1.0, result, 0.0);
    }
}
