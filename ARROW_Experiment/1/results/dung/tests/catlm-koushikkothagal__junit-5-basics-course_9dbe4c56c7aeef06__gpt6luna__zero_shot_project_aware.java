package io.javabrains.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MathUtils_ArrowTest {

    @Test
    public void addReturnsSum() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(7, mathUtils.add(3, 4));
    }

    @Test
    public void subtractReturnsDifference() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(2, mathUtils.subtract(5, 3));
    }

    @Test
    public void multiplyReturnsProduct() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(12, mathUtils.multiply(3, 4));
    }

    @Test
    public void divideReturnsQuotient() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(3, mathUtils.divide(12, 4));
    }

    @Test
    public void divideByZeroThrowsArithmeticException() {
        MathUtils mathUtils = new MathUtils();
        assertThrows(ArithmeticException.class, () -> mathUtils.divide(1, 0));
    }

    @Test
    public void computeCircleAreaReturnsArea() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(Math.PI * 4 * 4, mathUtils.computeCircleArea(4), 0.0);
    }
}
