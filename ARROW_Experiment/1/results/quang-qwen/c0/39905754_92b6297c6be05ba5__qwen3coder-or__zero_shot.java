import junit.framework.TestCase;
import junit.framework.Assert;

public class Fibonacci_ArrowTest {

    @org.junit.Test
    public void testCompute() {
        Assert.assertEquals(0L, Fibonacci.compute(0));
        Assert.assertEquals(1L, Fibonacci.compute(1));
        Assert.assertEquals(1L, Fibonacci.compute(2));
        Assert.assertEquals(2L, Fibonacci.compute(3));
        Assert.assertEquals(3L, Fibonacci.compute(4));
        Assert.assertEquals(5L, Fibonacci.compute(5));
        Assert.assertEquals(8L, Fibonacci.compute(6));
        Assert.assertEquals(13L, Fibonacci.compute(7));
        Assert.assertEquals(21L, Fibonacci.compute(8));
        Assert.assertEquals(34L, Fibonacci.compute(9));
        Assert.assertEquals(55L, Fibonacci.compute(10));
    }

    @org.junit.Test
    public void testComputeLessThanFourMillion() {
        Assert.assertEquals(34L, Fibonacci.computeLessThanFourMillion());
    }

    @org.junit.Test
    public void testCheckeven() {
        Assert.assertTrue(Fibonacci.checkeven(2));
        Assert.assertFalse(Fibonacci.checkeven(3));
        Assert.assertTrue(Fibonacci.checkeven(4));
        Assert.assertFalse(Fibonacci.checkeven(5));
        Assert.assertTrue(Fibonacci.checkeven(6));
        Assert.assertFalse(Fibonacci.checkeven(7));
        Assert.assertTrue(Fibonacci.checkeven(8));
        Assert.assertFalse(Fibonacci.checkeven(9));
        Assert.assertTrue(Fibonacci.checkeven(10));
    }
}
