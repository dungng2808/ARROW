import junit.framework.TestCase;
import org.junit.Test;

public class Fibonacci_ArrowTest {

    @Test
    public void testCompute() {
        TestCase.assertEquals(0L, Fibonacci.compute(0));
        TestCase.assertEquals(1L, Fibonacci.compute(1));
        TestCase.assertEquals(1L, Fibonacci.compute(2));
        TestCase.assertEquals(2L, Fibonacci.compute(3));
        TestCase.assertEquals(3L, Fibonacci.compute(4));
        TestCase.assertEquals(5L, Fibonacci.compute(5));
        TestCase.assertEquals(8L, Fibonacci.compute(6));
        TestCase.assertEquals(13L, Fibonacci.compute(7));
        TestCase.assertEquals(21L, Fibonacci.compute(8));
        TestCase.assertEquals(34L, Fibonacci.compute(9));
        TestCase.assertEquals(55L, Fibonacci.compute(10));
    }

    @Test
    public void testComputeLessThanFourMillion() {
        TestCase.assertEquals(34L, Fibonacci.computeLessThanFourMillion());
    }

    @Test
    public void testCheckeven() {
        TestCase.assertTrue(Fibonacci.checkeven(2));
        TestCase.assertFalse(Fibonacci.checkeven(3));
        TestCase.assertTrue(Fibonacci.checkeven(4));
        TestCase.assertFalse(Fibonacci.checkeven(5));
        TestCase.assertTrue(Fibonacci.checkeven(6));
        TestCase.assertFalse(Fibonacci.checkeven(7));
        TestCase.assertTrue(Fibonacci.checkeven(8));
        TestCase.assertFalse(Fibonacci.checkeven(9));
        TestCase.assertTrue(Fibonacci.checkeven(10));
    }
}
