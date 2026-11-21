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
    public void testCompute() {
        assertEquals(1L, Fibonacci.compute(1));
        assertEquals(2L, Fibonacci.compute(3));
        assertEquals(3L, Fibonacci.compute(4));
        assertEquals(5L, Fibonacci.compute(5));
        assertEquals(8L, Fibonacci.compute(6));
        assertEquals(13L, Fibonacci.compute(7));
        assertEquals(21L, Fibonacci.compute(8));
        assertEquals(34L, Fibonacci.compute(9));
        assertEquals(55L, Fibonacci.compute(10));
    }

    @Test
    public void testComputeLessThanFourMillion() {
        long result = Fibonacci.computeLessThanFourMillion();
        assertTrue(result >= 4000000);
    }

    @Test
    public void testCheckeven() {
        assertTrue(Fibonacci.checkeven(2));
        assertTrue(Fibonacci.checkeven(4));
        assertTrue(Fibonacci.checkeven(6));
        assertTrue(Fibonacci.checkeven(8));
        assertTrue(Fibonacci.checkeven(10));
        assertTrue(!Fibonacci.checkeven(1));
        assertTrue(!Fibonacci.checkeven(3));
        assertTrue(!Fibonacci.checkeven(5));
        assertTrue(!Fibonacci.checkeven(7));
        assertTrue(!Fibonacci.checkeven(9));
    }
}
