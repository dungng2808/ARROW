package org.performance;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Calculator_ArrowTest {

    @Test
    public void addReturnsSum() {
        Calculator calculator = new Calculator();
        assertEquals(7.0, calculator.add(3.0, 4.0), 0.0);
    }

    @Test
    public void subReturnsDifference() {
        Calculator calculator = new Calculator();
        assertEquals(2.0, calculator.sub(5.0, 3.0), 0.0);
    }

    @Test
    public void mulReturnsProduct() {
        Calculator calculator = new Calculator();
        assertEquals(12.0, calculator.mul(3.0, 4.0), 0.0);
    }

    @Test
    public void divReturnsQuotient() {
        Calculator calculator = new Calculator();
        assertEquals(2.5, calculator.div(5.0, 2.0), 0.0);
    }

    @Test
    public void modReturnsRemainder() {
        Calculator calculator = new Calculator();
        assertEquals(1.0, calculator.mod(7.0, 3.0), 0.0);
    }
}
