package io.javabrains.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MathUtils_ArrowTest {

    @Test
    public void testAdd() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(5, mathUtils.add(2, 3));
    }

    @Test
    public void testSubtract() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(1, mathUtils.subtract(3, 2));
    }

    @Test
    public void testMultiply() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(6, mathUtils.multiply(2, 3));
    }

    @Test
    public void testDivide() {
        MathUtils mathUtils = new MathUtils();
        assertEquals(2, mathUtils.divide(6, 3));
    }

    @Test
    public void testComputeCircleArea() {
        MathUtils mathUtils = new MathUtils();
        double result = mathUtils.computeCircleArea(2.0);
        assertEquals(Math.PI * 4.0, result, 0.0001);
    }
}
