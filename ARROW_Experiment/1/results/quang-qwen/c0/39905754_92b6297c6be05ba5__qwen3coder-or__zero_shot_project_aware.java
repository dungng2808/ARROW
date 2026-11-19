import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Fibonacci_ArrowTest {

    @Test
    public void testComputeBaseCases() {
        assertEquals(0L, Fibonacci.compute(0));
        assertEquals(1L, Fibonacci.compute(1));
    }

    @Test
    public void testComputeSmallValues() {
        assertEquals(1L, Fibonacci.compute(2));
        assertEquals(2L, Fibonacci.compute(3));
        assertEquals(3L, Fibonacci.compute(4));
        assertEquals(5L, Fibonacci.compute(5));
        assertEquals(8L, Fibonacci.compute(6));
    }

    @Test
    public void testComputeLessThanFourMillion() {
        long result = Fibonacci.computeLessThanFourMillion();
        // The largest Fibonacci number less than 4,000,000 is 3,524,578
        // The next one is 5,702,887 which is greater than 4,000,000
        assertEquals(3524578L, result);
    }

    @Test
    public void testCheckEven() {
        assertTrue(Fibonacci.checkeven(2));
        assertTrue(Fibonacci.checkeven(4));
        assertTrue(Fibonacci.checkeven(6));
        assertTrue(Fibonacci.checkeven(8));
        assertTrue(Fibonacci.checkeven(10));
    }

    @Test
    public void testCheckOdd() {
        assertEquals(false, Fibonacci.checkeven(1));
        assertEquals(false, Fibonacci.checkeven(3));
        assertEquals(false, Fibonacci.checkeven(5));
        assertEquals(false, Fibonacci.checkeven(7));
        assertEquals(false, Fibonacci.checkeven(9));
    }
}
