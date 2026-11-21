import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Fibonacci_ArrowTest {

    @Test
    public void computeZero() {
        assertEquals(0L, Fibonacci.compute(0));
    }

    @Test
    public void computeOne() {
        assertEquals(1L, Fibonacci.compute(1));
    }

    @Test
    public void computeSeveralTerms() {
        assertEquals(55L, Fibonacci.compute(10));
    }

    @Test
    public void computeNegativeInput() {
        assertEquals(0L, Fibonacci.compute(-1));
    }

    @Test
    public void computeLessThanFourMillion() {
        assertEquals(5702887L, Fibonacci.computeLessThanFourMillion());
    }

    @Test
    public void checkEvenNumber() {
        assertTrue(Fibonacci.checkeven(4));
    }

    @Test
    public void checkOddNumber() {
        assertFalse(Fibonacci.checkeven(3));
    }

    @Test
    public void checkEvenZero() {
        assertTrue(Fibonacci.checkeven(0));
    }
}
