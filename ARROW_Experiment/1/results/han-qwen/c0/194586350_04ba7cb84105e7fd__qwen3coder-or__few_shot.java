package app.metatron.discovery.prep.parser.preparation.rule.expr;

import com.google.common.base.Function;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class Pair_ArrowTest {

    @Test
    public void testOf() {
        Pair<String, Integer> pair = Pair.of("test", 42);
        assertEquals("test", pair.lhs);
        assertEquals(Integer.valueOf(42), pair.rhs);
    }

    @Test
    public void testConstructor() {
        Pair<Double, String> pair = new Pair<>(3.14, "pi");
        assertEquals(Double.valueOf(3.14), pair.lhs);
        assertEquals("pi", pair.rhs);
    }

    @Test
    public void testEquals() {
        Pair<String, Integer> pair1 = Pair.of("test", 1);
        Pair<String, Integer> pair2 = Pair.of("test", 1);
        Pair<String, Integer> pair3 = Pair.of("different", 1);
        Pair<String, Integer> pair4 = Pair.of("test", 2);

        assertEquals(pair1, pair2);
        assertNotEquals(pair1, pair3);
        assertNotEquals(pair1, pair4);
        assertNotEquals(pair1, null);
        assertNotEquals(pair1, new Object());
    }

    @Test
    public void testHashCode() {
        Pair<String, Integer> pair1 = Pair.of("test", 1);
        Pair<String, Integer> pair2 = Pair.of("test", 1);
        Pair<String, Integer> pair3 = Pair.of("test", 2);

        assertEquals(pair1.hashCode(), pair2.hashCode());
        assertNotEquals(pair1.hashCode(), pair3.hashCode());
    }

    @Test
    public void testToString() {
        Pair<String, Integer> pair = Pair.of("hello", 123);
        String expected = "Pair{lhs=hello, rhs=123}";
        assertEquals(expected, pair.toString());
    }

    @Test
    public void testLhsFn() {
        Function<Pair<String, Integer>, String> lhsFunction = Pair.lhsFn();
        Pair<String, Integer> pair = Pair.of("test", 42);
        assertEquals("test", lhsFunction.apply(pair));
    }

    @Test
    public void testRhsFn() {
        Function<Pair<String, Integer>, Integer> rhsFunction = Pair.rhsFn();
        Pair<String, Integer> pair = Pair.of("test", 42);
        assertEquals(Integer.valueOf(42), rhsFunction.apply(pair));
    }

    @Test
    public void testLhsComparator() {
        Comparator<Pair<String, Integer>> comparator = Pair.lhsComparator(String.CASE_INSENSITIVE_ORDER);
        Pair<String, Integer> pair1 = Pair.of("apple", 1);
        Pair<String, Integer> pair2 = Pair.of("banana", 2);
        Pair<String, Integer> pair3 = Pair.of("Apple", 3);

        assertTrue(comparator.compare(pair1, pair2) < 0);
        assertTrue(comparator.compare(pair2, pair1) > 0);
        assertEquals(0, comparator.compare(pair1, pair3));
    }
}
