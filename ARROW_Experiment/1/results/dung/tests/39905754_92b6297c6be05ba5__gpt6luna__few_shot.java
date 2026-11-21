import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Fibonacci_ArrowTest {

    @Test
    public void computeReturnsZeroForZero() {
        assertEquals(0L, Fibonacci.compute(0));
    }

    @Test
    public void computeReturnsOneForOne() {
        assertEquals(1L, Fibonacci.compute(1));
    }

    @Test
    public void computeReturnsExpectedFibonacciNumber() {
        assertEquals(55L, Fibonacci.compute(10));
    }

    @Test
    public void computeLessThanFourMillionReturnsFirstValueAtLeastFourMillion() {
        assertEquals(5702887L, Fibonacci.computeLessThanFourMillion());
    }

    @Test
    public void checkevenReturnsTrueForEvenNumber() {
        assertTrue(Fibonacci.checkeven(4));
    }

    @Test
    public void checkevenReturnsFalseForOddNumber() {
        assertFalse(Fibonacci.checkeven(3));
    }

    @Test
    public void checkevenReturnsTrueForZero() {
        assertTrue(Fibonacci.checkeven(0));
    }
}
